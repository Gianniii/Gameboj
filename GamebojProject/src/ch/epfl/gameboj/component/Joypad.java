package ch.epfl.gameboj.component;

import static ch.epfl.gameboj.Preconditions.checkBits16;
import static ch.epfl.gameboj.Preconditions.checkBits8;

import java.util.Objects;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.cpu.Cpu;
import ch.epfl.gameboj.component.cpu.Cpu.Interrupt;

/**
 * Class Joypad, represents the GameBoy's keyboard
 * 
 * @author Omid Karimi (273816)
 * @author Gianni Lodetti (275085)
 */
public final class Joypad implements Component {
    private final Cpu cpu;
    private int p1 = 0;
    private int keyLine1 = 0;
    private int keyLine2 = 0;
    
    /**
     * Enumerate the different keys
     */
    public enum Key {
        RIGHT, LEFT, UP, DOWN, A, B, SELECT, START
    }
    
    /**
     * Construct Joypad with given Cpu
     * 
     * @param cpu, processor of the gameboy
     */
    public Joypad(Cpu cpu) {
        this.cpu = cpu;
    }

    @Override
    public int read(int address) throws IllegalArgumentException {
        address = checkBits16(address);
        if (address == AddressMap.REG_P1) {
            return Bits.complement8(p1);
        }
        return Component.NO_DATA;
    }

    @Override
    public void write(int address, int data) throws IllegalArgumentException {
        address = checkBits16(address);
        data = checkBits8(data);
        if (address == AddressMap.REG_P1) {
            p1 = (Bits.complement8(data) & 0b110000) | (p1 & 0b11001111);
            updateP1();
        }
    }
    
    /**
     * Updates key lines and then update register P1 to simulate given 
     *  key being pressed
     * 
     * @param pressed key
     */
    public void keyPressed(Key key) {
        Objects.requireNonNull(key);
        if (key.ordinal() <= 3) {
            keyLine1 = Bits.set(keyLine1, key.ordinal() % 4, true);
        } else {
            keyLine2 = Bits.set(keyLine2, key.ordinal() % 4, true);
        }
        updateP1();
    }
    
    /**
     * Updates key lines and then update register P1 to simulate given 
     *  key being released
     * 
     * @param released key 
     */
    public void keyReleased(Key key) {
        Objects.requireNonNull(key);
        if (key.ordinal() <= 3) {
            keyLine1 = Bits.set(keyLine1, key.ordinal() % 4, false);
        } else {
            keyLine2 = Bits.set(keyLine2, key.ordinal() % 4, false);
        }
        updateP1();
    }

    // Updates P1 with the lines
    private void updateP1() {
        int newp1 = p1;
        if(Bits.test(p1, 4)) {
            newp1 = (Bits.extract(p1, 4, 4) << 4) | keyLine1;
        } 
        if(Bits.test(p1, 5)) {
            newp1 = (Bits.extract(p1, 4, 4) << 4) | keyLine2;
        }
        if(Bits.test(p1, 4) && Bits.test(p1, 5)) {
            newp1 = (Bits.extract(p1, 4, 4) << 4) | (keyLine1 | keyLine2);
        }
        //here should only throw interrupt when we pass from 0 to 1
        if(newp1 != p1) {
            p1 = newp1;
            cpu.requestInterrupt(Interrupt.JOYPAD);
        }
    }
    
}
