package ch.epfl.gameboj.component.cartridge;

import static ch.epfl.gameboj.Preconditions.checkArgument;
import static ch.epfl.gameboj.Preconditions.checkBits16;
import static ch.epfl.gameboj.Preconditions.checkBits8;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.memory.Rom;

/**
 * Class Cartridge, containing the games for the GameBoy
 * 
 * @author Omid Karimi (273816)
 * @author Gianni Lodetti (275085)
 */
public final class Cartridge implements Component {
    
    private final Component mbc;
    private static final int CARTRIDGE_TYPE_ADDR = 0x147;
    private static final int RAM_SIZE_ADDR = 0x149;
    private static final int[] RAM_SIZE = {0, 2048, 8192, 32768};

    private Cartridge(Component mbc) {
        this.mbc = mbc;
    }

    /**
     * Construct cartridge from a romfile
     * 
     * @param romfile
     * @return Cartridge
     * @throws IllegalArgumentException
     * @throws IOException
     */
    public static Cartridge ofFile(File romfile) throws IllegalArgumentException, IOException {
        byte[] romData;
        System.out.println("1_0");
        try (InputStream stream = new FileInputStream(romfile)) {
            romData = stream.readAllBytes();
        }
        int cartridgeType = romData[CARTRIDGE_TYPE_ADDR];
        // checks if it's the type of cartridge we simulate
        System.out.println("1_1");
        checkArgument(cartridgeType >= 0 && cartridgeType <= 3);
        Rom rom = new Rom(romData);
        System.out.println("1_2");
        Component mbc = (cartridgeType == 0) ? new MBC0(rom) : new MBC1(rom, getRamSize(romData));
        return new Cartridge(mbc);
    }

    @Override
    public int read(int address) throws IllegalArgumentException {
        return mbc.read(address);
    }

    @Override
    public void write(int address, int data) throws IllegalArgumentException {
        address = checkBits16(address);
        data = checkBits8(data);
        mbc.write(address, data);
    }
    
    private static int getRamSize(byte[] romData) {
        return RAM_SIZE[romData[RAM_SIZE_ADDR]];
    }

    /**
     * If the cartridge is an MBC1 calls its save method
     * 
     * @param fileName
     */
    public void save(String fileName) {
        if (mbc instanceof MBC1)
            ((MBC1)mbc).save(fileName);
    }

    /**
     * If the cartridge is an MBC1 calls its load method
     * 
     * @param fileName
     */
    public void loadSave(String fileName) {
        if (mbc instanceof MBC1)
            ((MBC1)mbc).load(fileName);
    }
}
