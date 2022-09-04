package ch.epfl.gameboj.component.lcd;

import static ch.epfl.gameboj.Preconditions.checkArgument;
import static ch.epfl.gameboj.Preconditions.checkBits16;
import static ch.epfl.gameboj.Preconditions.checkBits8;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.Bus;
import ch.epfl.gameboj.Register;
import ch.epfl.gameboj.RegisterFile;
import ch.epfl.gameboj.bits.Bit;
import ch.epfl.gameboj.bits.BitVector;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.Clocked;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.cpu.Cpu;
import ch.epfl.gameboj.component.cpu.Cpu.Interrupt;
import ch.epfl.gameboj.component.memory.Ram;

/**
 * Class LcdController, represents the screen's controller
 * 
 * @author Omid Karimi (273816)
 * @author Gianni Lodetti (275085)
 */
public final class LcdController implements Component, Clocked {
    public static final int LCD_WIDTH = 160;
    public static final int LCD_HEIGHT = 144;
    private static final int CYCLES_PER_LINE = 114;
    private final Cpu cpu;
    private Bus bus;
    private final Ram videoRam = new Ram(AddressMap.VIDEO_RAM_SIZE);
    private final Ram oamRam = new Ram(AddressMap.OAM_RAM_SIZE);
    private final RegisterFile<Reg> bench = new RegisterFile<>(Reg.values());
    private LcdImage currentImage;
    private LcdImage.Builder nextImageBuilder;
    private long nextNonIdleCycle = Long.MAX_VALUE;
    private int winY = 0;
    private boolean oamCopyActive = false;
    private int oamCopyCounter = 0;
    
    // represents register of the controller
    private enum Reg implements Register {
        LCDC, STAT, SCY, SCX, LY, LYC, DMA, BGP, OBP0, OBP1, WY, WX 
    }
    
    // represents bits of the STAT register
    private enum STATBits implements Bit {
        MODE0, MODE1, LYC_EQ_LY, INT_MODE0, INT_MODE1, INT_MODE2, INT_LYC, UNUSED
    }
    
    // represents bits of the LCDC register
    private enum LCDCBits implements Bit {
        BG, OBJ, OBJ_SIZE, BG_AREA, TILE_SOURCE, WIN, WIN_AREA, LCD_STATUS
    }
    
    // sprite's characteristics
    private enum Sprite {
        Y, X, TILE_INDEX, OA
    }
    
    // sprite's attributes (bits of OA)
    private enum OA implements Bit {
        UNUSED_0, UNUSED_1, UNUSED_2, UNUSED_3, PALETTE, FLIP_H, FLIP_V, BEHIND_BG
    }
    
    /**
     * Controller a LcdController with given Cpu
     * 
     * @param cpu, processor of the gameboy
     */
    public LcdController(Cpu cpu) {
        this.cpu = cpu;
    }
    
    @Override
    public void attachTo(Bus bus) {
        this.bus = bus;
        Component.super.attachTo(bus);
    }
    
    /**
     * Getter for the current image
     * 
     * @return currentImage or empty image if current image is null
     */
    public LcdImage currentImage() {
        if (currentImage == null) {
            BitVector zeroVect = new BitVector(LCD_WIDTH);
            List<LcdImageLine> lines = Collections.nCopies(LCD_HEIGHT,
                    new LcdImageLine(zeroVect, zeroVect, zeroVect));
            return new LcdImage(LCD_WIDTH, LCD_HEIGHT, lines);
        }
        return currentImage;
    }
    
    @Override
    public int read(int address) throws IllegalArgumentException {
        address = checkBits16(address);
        if (address >= AddressMap.VIDEO_RAM_START && address < AddressMap.VIDEO_RAM_END) {
            return videoRam.read(address - AddressMap.VIDEO_RAM_START);
        }
        if (address >= AddressMap.OAM_START && address < AddressMap.OAM_END) {
            return oamRam.read(address - AddressMap.OAM_START);
        }
        if (address >= AddressMap.REGS_LCDC_START && address < AddressMap.REGS_LCDC_END) {
            return bench.get(getReg(address));
        }
        return Component.NO_DATA;
    }

    @Override
    public void write(int address, int data) throws IllegalArgumentException {
        address = checkBits16(address);
        data = checkBits8(data);
        if (address >= AddressMap.VIDEO_RAM_START && address < AddressMap.VIDEO_RAM_END) {
            videoRam.write(address - AddressMap.VIDEO_RAM_START,  data);
        }
        if (address >= AddressMap.OAM_START && address < AddressMap.OAM_END) {
            oamRam.write(address - AddressMap.OAM_START, data);
        }
        if (address >= AddressMap.REGS_LCDC_START && address < AddressMap.REGS_LCDC_END) {
            Reg r = getReg(address);
            if (r == Reg.LCDC) {
                //if LCD_STATUS changes to false 
                if(!Bits.test(data, 7) && bench.testBit(r, LCDCBits.LCD_STATUS)) {
                    changeModeTo(0);
                    modifyLYorLYC(Reg.LY, 0);
                    nextNonIdleCycle = Long.MAX_VALUE;
                }
                bench.set(r, data);
            } else if (r == Reg.STAT) {
                int value = (data & 0b11111000) | (bench.get(r) & 0b111);
                bench.set(r, value);
            } else if (r == Reg.LYC) {
                modifyLYorLYC(r, data);
            } else if (r == Reg.DMA) {
                bench.set(r, data);
                oamCopyActive = true;
            } else if (r == Reg.LY) {
                return;
            } else {
            bench.set(r, data);
            }
        }
    }
    
    @Override
    public void cycle(long cycle) {
        // handles OAM copy into oamRam
        if (oamCopyActive) {
            int data = bus.read((bench.get(Reg.DMA) << 8) + oamCopyCounter);
            write(AddressMap.OAM_START + oamCopyCounter, data);
            ++oamCopyCounter;
            if (oamCopyCounter == oamRam.size()) {
                oamCopyActive = false;
                oamCopyCounter = 0;
            }
        }
        // handles turning on of the screen
        if (nextNonIdleCycle == Long.MAX_VALUE && bench.testBit(Reg.LCDC, LCDCBits.LCD_STATUS)) {
            changeModeTo(2);
            // the first cycle executes immediately, so +19 instead of 20
            nextNonIdleCycle = cycle + 19;
        }
        if (nextNonIdleCycle == cycle && nextNonIdleCycle != Long.MAX_VALUE) {
            reallyCycle();
        }

    }

    private void reallyCycle() {
        int currMode = getMode();
        int lineIndex = currLineIndex();
        if (currMode == 2 && lineIndex == 0) {
            nextImageBuilder = new LcdImage.Builder(LCD_WIDTH, LCD_HEIGHT);
        }
        if (currMode == 1 && lineIndex == LCD_HEIGHT) {
            currentImage = nextImageBuilder.build();
            winY = 0;
        }
        if (currMode == 3) {
            nextImageBuilder.setLine(lineIndex, computeLine(lineIndex));
        }
        changeToNextMode();
    }
    
    // computes displayed line of given index
    private LcdImageLine computeLine(int index) {
        BitVector zeroVect = new BitVector(LCD_WIDTH);
        LcdImageLine line = new LcdImageLine(zeroVect, zeroVect, zeroVect);
        LcdImageLine winLine = line;
        LcdImageLine bgSpriteLine = line;
        LcdImageLine fgSpriteLine = line;
        
        int tileSrc = AddressMap.TILE_SOURCE[bench.testBit(Reg.LCDC, LCDCBits.TILE_SOURCE) ? 1 : 0];
        int bgArea = AddressMap.BG_DISPLAY_DATA[bench.testBit(Reg.LCDC, LCDCBits.BG_AREA) ? 1 : 0];
        int winArea = AddressMap.BG_DISPLAY_DATA[bench.testBit(Reg.LCDC, LCDCBits.WIN_AREA) ? 1 : 0];
        int WX = bench.get(Reg.WX) - 7;
        
        // if needed compute background
        if (bench.testBit(Reg.LCDC, LCDCBits.BG)) {
            line = computeLine(bgArea, tileSrc, index + bench.get(Reg.SCY)).mapColors(bench.get(Reg.BGP))
                    .extractWrapped(bench.get(Reg.SCX), LCD_WIDTH);
        }
        // if needed compute window and combines it with background
        if (bench.testBit(Reg.LCDC, LCDCBits.WIN) && WX < 160 && bench.get(Reg.WY) <= currLineIndex()) {
            if (WX < 0) 
                WX = 0;
            winLine = computeLine(winArea, tileSrc, winY).mapColors(bench.get(Reg.BGP))
                      .extractWrapped(0, LCD_WIDTH).shift(WX);
            ++winY;
            line = line.join(winLine, WX);
        }
        // compute foreground and background sprite lines if necessary
        if (bench.testBit(Reg.LCDC, LCDCBits.OBJ)) {
            int[] indexes = spritesIntersectingLine();
            for (int i : indexes) {
                if (Bits.test(readInOam(i, Sprite.OA), OA.BEHIND_BG)) {
                    bgSpriteLine = spriteLine(i).below(bgSpriteLine);
                } else {
                    fgSpriteLine = spriteLine(i).below(fgSpriteLine);
                }
            }
        }
        // combine background + window with sprites
        BitVector opacity = bgSpriteLine.opacity().and(line.opacity().not());
        line = line.below(bgSpriteLine, opacity);
        return line.below(fgSpriteLine);
    }
    
    private LcdImageLine computeLine(int area, int tileSrc, int index) {
        LcdImageLine.Builder lineBuilder = new LcdImageLine.Builder(256);
        int indexInTile = index % Byte.SIZE;
        int lsb = 0, msb = 0;
        // Handle vertical wrapping
        index = Math.floorMod(index, 256);
        // find Y coordinate of tile
        index = Math.floorDiv(index, 8);
        
        for (int x = 0 ; x < 32 ; ++x) {
            int tileCode = read(area + index* 32 + x);
            int tileIndex = tileCode * 16;
            // reads tile line in correct address range
            if((tileCode >= 0x80 && tileCode <= 0xFF) || tileSrc == 0x8000) {
                lsb = read(0x8000 + tileIndex + indexInTile * 2);
                msb = read(0x8000 + tileIndex + indexInTile * 2 + 1);
            }
            if (tileCode < 0x80 && tileSrc == 0x8800) {
                lsb = read(0x9000 + tileIndex + indexInTile * 2);
                msb = read(0x9000 + tileIndex + indexInTile * 2 + 1);
            }
            lineBuilder.setBytes(x, Bits.reverse8(msb), Bits.reverse8(lsb));
        }
        return lineBuilder.build();
    }

    private int currLineIndex() {
        return bench.get(Reg.LY);
    }

    //return sorted indexes of sprites to draw intersecting current line
    private int[] spritesIntersectingLine() {
        int[] sprites = new int[10];
        int spriteHeight = bench.testBit(Reg.LCDC, LCDCBits.OBJ_SIZE) ? 16 : 8;
        int i = 0;
        int spriteNumber = 0;
        // finds up to 10 sprites intersecting the line
        while (i < 40 && spriteNumber < sprites.length) {
            int spriteY = readInOam(i, Sprite.Y) - 16;
            if (spriteY <= currLineIndex() && spriteY + spriteHeight > currLineIndex()) {
                int spriteX = readInOam(i, Sprite.X);
                sprites[spriteNumber] = (spriteX << 8) | i;
                ++spriteNumber;
            }
            ++i;
        }
        Arrays.sort(sprites, 0, spriteNumber);
        int[] spritesIndexes = new int[spriteNumber];
        for (int j = 0 ; j < spriteNumber ; ++j) {
            spritesIndexes[j] = Bits.clip(8, sprites[j]);
        }
        return spritesIndexes;
    }
    
    // computes a line containing the sprite at given sprite index in oamRam
    private LcdImageLine spriteLine(int spriteIndex) {
        LcdImageLine.Builder lineBuilder = new LcdImageLine.Builder(160);
        int spriteX = readInOam(spriteIndex, Sprite.X) - 8;
        int spriteY = readInOam(spriteIndex, Sprite.Y) - 16;
        int indexInTile = currLineIndex() - spriteY;
        int spriteTileCode = readInOam(spriteIndex, Sprite.TILE_INDEX);
        int palette = Bits.test(readInOam(spriteIndex, Sprite.OA), OA.PALETTE) ? 
                bench.get(Reg.OBP1) : bench.get(Reg.OBP0);
        //for 16 bit spries the tilecode must be even number.
        if(spriteTileCode % 2 != 0 && bench.testBit(Reg.LCDC, LCDCBits.OBJ_SIZE)) {
            spriteTileCode -= 1;
        }
        // handles 16 pixels height sprites
        if (indexInTile >= 8) {
            ++spriteTileCode;
            indexInTile -= 8;
        }
        // handles vertical flips for 8 and 16 pixels height sprites
        if (Bits.test(readInOam(spriteIndex, Sprite.OA), OA.FLIP_V)) {
            indexInTile = 8 - indexInTile;
            if(bench.testBit(Reg.LCDC, LCDCBits.OBJ_SIZE) ) {
                if(spriteTileCode != readInOam(spriteIndex, Sprite.TILE_INDEX)) {
                    --spriteTileCode;
                } else {
                    ++spriteTileCode;
                }
            }
        }
        
        int spriteTileIndex = spriteTileCode * 16;
        int lsb = read(0x8000 + spriteTileIndex + indexInTile * 2);
        int msb = read(0x8000 + spriteTileIndex + indexInTile * 2 + 1); 
        // handles horizontal flips
        if (!Bits.test(readInOam(spriteIndex, Sprite.OA), OA.FLIP_H)) {
            lsb = Bits.reverse8(lsb);
            msb = Bits.reverse8(msb);
        }
        return lineBuilder.setBytes(0, msb, lsb).build().shift(spriteX).mapColors(palette);
    }
    
    private int readInOam(int spriteIndex, Sprite s) {
        return oamRam.read(spriteIndex * 4 + s.ordinal());
    }
    
    // handles transition to next mode
    private void changeToNextMode() {
        int currMode = getMode();
        int lineIndex = currLineIndex();
        if (currMode == 2) {
            changeModeTo(3);
            nextNonIdleCycle += 43;
        }
        if (currMode == 3) {
            changeModeTo(0);
            nextNonIdleCycle += 51;
        }
        if (currMode == 0 && lineIndex < LCD_HEIGHT - 1) {
            changeModeTo(2);
            nextNonIdleCycle += 20;
            modifyLYorLYC(Reg.LY, (lineIndex + 1));
        }
        // when image is finished 
        if (currMode == 0 && lineIndex == LCD_HEIGHT - 1) {
            changeModeTo(1);
            nextNonIdleCycle += CYCLES_PER_LINE;
            modifyLYorLYC(Reg.LY, (lineIndex + 1));
        }
        if (currMode == 1 && lineIndex < 153) {
            nextNonIdleCycle += CYCLES_PER_LINE;
            modifyLYorLYC(Reg.LY, (lineIndex + 1));
        }
        // when vertical blank has ended 
        if (currMode == 1 && lineIndex == 153) {
            changeModeTo(2);
            nextNonIdleCycle += 20;
            modifyLYorLYC(Reg.LY, 0);
        }
    }
    
    private Reg getReg(int address) {
        return Reg.values()[address - AddressMap.REGS_LCDC_START];
    }
    
    // handles modifications of LY and LYC registers
    private void modifyLYorLYC(Reg r, int data) {
        bench.set(r, data);
        if (bench.get(Reg.LYC) == bench.get(Reg.LY)) {
            bench.setBit(Reg.STAT, STATBits.LYC_EQ_LY, true);
            if (bench.testBit(Reg.STAT, STATBits.INT_LYC) && bench.testBit(Reg.LCDC, LCDCBits.LCD_STATUS)) {
                cpu.requestInterrupt(Interrupt.LCD_STAT);
            }
        } else {
            bench.setBit(Reg.STAT, STATBits.LYC_EQ_LY, false);
        }
    }
    
    // changes current mode to given mode and handles eventual interruptions
    private void changeModeTo(int newMode) {
        checkArgument(newMode >= 0 && newMode <= 3);
        bench.setBit(Reg.STAT, STATBits.MODE0, Bits.test(newMode, 0));
        bench.setBit(Reg.STAT, STATBits.MODE1, Bits.test(newMode, 1));
        //check if new mode needs interruption
        if (newMode <= 2 && bench.testBit(Reg.STAT, STATBits.values()[getMode() + STATBits.INT_MODE0.index()])
                && bench.testBit(Reg.LCDC, LCDCBits.LCD_STATUS)) {
            cpu.requestInterrupt(Interrupt.LCD_STAT);
        }
        if (newMode == 1) {
            cpu.requestInterrupt(Interrupt.VBLANK);
        }
    }
    
    private int getMode() {
        return Bits.clip(2, bench.get(Reg.STAT));
    }

}
