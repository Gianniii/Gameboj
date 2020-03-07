package ch.epfl.gameboj;

import static ch.epfl.gameboj.Preconditions.checkBits8;

import ch.epfl.gameboj.bits.Bit;
import ch.epfl.gameboj.bits.Bits;

/**
 * Class RegisterFile, "bench of registers"
 * 
 * @author Omid Karimi (273816)
 * @author Gianni Lodetti (275085)
 */
public final class RegisterFile<E extends Register> {

    private final byte[] bench;

    /**
     * Construct a bench of registers
     * 
     * @param allregs
     */
    public RegisterFile(E[] allregs) {
        bench = new byte[allregs.length];
    }

    /**
     * get the value inside the register
     * 
     * @param register
     * @return data
     */
    public int get(E reg) {
        return Byte.toUnsignedInt(bench[reg.index()]);
    }

    /**
     * set value in the given register to given new Value
     * 
     * @param reg
     * @param newValue
     * @throws IllegalArgumentException
     */
    public void set(E reg, int newValue) throws IllegalArgumentException {
        newValue = checkBits8(newValue);
        bench[reg.index()] = (byte) newValue;
    }

    /**
     * test the given bit of the given register
     * 
     * @param reg
     * @param b
     * @return true if bit is 1, else false
     */
    public boolean testBit(E reg, Bit b) {
        return Bits.test(get(reg), b);
    }

    /**
     * modifies the given bit of the given register with the new value
     * 
     * @param reg
     * @param bit
     * @param newValue
     */
    public void setBit(E reg, Bit bit, boolean newValue) {
        set(reg, Bits.set(get(reg), bit.index(), newValue));
    }
}
