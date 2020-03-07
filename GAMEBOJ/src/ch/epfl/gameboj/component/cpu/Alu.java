package ch.epfl.gameboj.component.cpu;

import ch.epfl.gameboj.bits.Bit;
import ch.epfl.gameboj.bits.Bits;

import static ch.epfl.gameboj.Preconditions.checkBits16;
import static ch.epfl.gameboj.Preconditions.checkBits8;

import java.util.Objects;

/**
 * Class Arithmetic Logic Unit (ALU)
 * 
 * @author Omid Karimi (273816)
 * @author Gianni Lodetti (275085)
 */
public final class Alu {

    private Alu() {
    }

    /**
     * Assigning flags to bits
     *
     */
    public enum Flag implements Bit {
        UNUSED_0, UNUSED_1, UNUSED_2, UNUSED_3, C, H, N, Z
    };
    
    /**
     * Direction of rotation
     * (Left positive, right is negative)
     *
     */
    public enum RotDir {
        LEFT, RIGHT
    };

    /**
     * create mask with values of the flags
     * 
     * @param z
     *            true if the value is equal to 0
     * @param n
     *            true if the operation is a subtraction
     * @param h
     *            true if the half carry of the operation is equal to 1
     * @param c
     *            true if the carry (of the operation) is equal to 1
     * @return mask
     */
    public static int maskZNHC(boolean z, boolean n, boolean h, boolean c) {
        int mask = 0;
        if (z)
            mask = Flag.Z.mask();
        if (n)
            mask = mask | Flag.N.mask();
        if (h)
            mask = mask | Flag.H.mask();
        if (c)
            mask = mask | Flag.C.mask();
        return mask;
    }

    /**
     * unpack value of the given package value/flags
     * 
     * @param valueFlags
     * @return value
     */
    public static int unpackValue(int valueFlags) {
        return valueFlags >> 8;
    }

    /**
     * unpack flags of the given package value/flags
     * 
     * @param valueFlags
     * @return flags
     */
    public static int unpackFlags(int valueFlags) {
        return Bits.clip(8, valueFlags);
    }

    /**
     * pack the value and the flags in a package value/flags
     * 
     * @param v, value
     * @param z
     * @param n
     * @param h
     * @param c
     * @return packedValueZNHC
     * @throws IllegalArgumentException 
     */
    private static int packValueZNHC(int v, boolean z, boolean n, boolean h, boolean c) {
        v = checkBits16(v);
        return (v << 8) | maskZNHC(z, n, h, c);
    }

    private static int boolToInt(boolean b) {
        return b ? 1 : 0;
    }
    
    /**
     * sum of two 8 bits inputs with a potential carry
     * 
     * @param l
     *            input 1
     * @param r
     *            input 2
     * @param c0
     *            carry
     * @return sum in a packed value/flags
     * @throws IllegalArgumentException 
     */
    public static int add(int l, int r, boolean c0) {
        check8Bits(l, r);
        int carry = boolToInt(c0);
        int sum = l + r + carry;
        boolean h = Bits.clip(4, r) + Bits.clip(4, l) + carry > 0xF;
        boolean c = sum > 0xFF;
        sum = Bits.clip(8, sum);
        return packValueZNHC(sum, sum == 0, false, h, c);
    }

    /**
     * sum of two 8 bits inputs
     * 
     * @param l input 1
     * @param r input 2
     * @return sum in a packed value/flags
     * @throws IllegalArgumentException 
     */
    public static int add(int l, int r) {
        return add(l, r, false);
    }

    /**
     * add two 16 bits inputs
     * 
     * @param l
     *            input 1
     * @param r
     *            input 2
     * @return sum in a value/flags package with the half carry and carry being
     *         that of the sum of the 8 lsb bits
     * @throws IllegalArgumentException 
     */
    public static int add16L(int l, int r) {
        l = checkBits16(l);
        r = checkBits16(r);
        int sum = l + r;
        boolean h = Bits.clip(4, l) + Bits.clip(4, r) > 0xF;
        boolean c = Bits.clip(8, l) + Bits.clip(8, r) > 0xFF;
        return packValueZNHC(Bits.clip(16, sum), false, false, h, c);
    }

    /**
     * add two 16 bits inputs
     * 
     * @param l
     *            input 1
     * @param r
     *            input 2
     * @return sum in a value/flags package with the half carry and carry being
     *         that of the sum of the 8 msb bits
     * @throws IllegalArgumentException 
     */
    public static int add16H(int l, int r) {
        l = checkBits16(l);
        r = checkBits16(r);
        int sum = l + r;
        int carry = Bits.clip(8, l) + Bits.clip(8, r) > 0xFF ? 1 : 0;
        boolean h = Bits.extract(l, 8, 4) + Bits.extract(r, 8 ,4) + carry > 0xF;
        boolean c = sum > 0xFFFF;
        return packValueZNHC(Bits.clip(16, sum), false, false, h, c);
    }

    /**
     * Compute the difference of the two 8 bits inputs with potential borrow
     * 
     * @param l
     *            input 1
     * @param r
     *            input 2
     * @return the difference in a value/flags package
     * @throws IllegalArgumentException 
     */
    public static int sub(int l, int r, boolean b0) {
        check8Bits(l, r);
        int borrow = boolToInt(b0);
        int sub = l - (r + borrow);
        boolean c = l < (r + borrow);
        boolean h = (Bits.clip(4, l) - (Bits.clip(4, r) + borrow)) < 0;
        sub = Bits.clip(8, sub);
        return packValueZNHC(sub, sub == 0, true, h, c);
    }

    /**
     * Compute the difference of the two 8 bits inputs
     * 
     * @param l
     *            input 1
     * @param r
     *            input 2
     * @return the difference in a value/flags package
     * @throws IllegalArgumentException 
     */
    public static int sub(int l, int r) {
        return sub(l, r, false);
    }

    /**
     * Adjusts the 8 given bits so that they are in DCB format
     * 
     * @param l
     *           input 1
     * @param n 
     *           flag n
     * @param h
     *           flag h
     * @param c
     *           flag c
     * @return formated value/flags package
     * @throws IllegalArgumentException 
     */
    public static int bcdAdjust(int v, boolean n, boolean h, boolean c) {
        v = checkBits8(v);
        boolean fixL = h | (!n & Bits.clip(4, v) > 0x9);
        boolean fixH = c | (!n & v > 0x99);
        int fix = (fixH ? 6 : 0) << 4 | (fixL ? 6 : 0);
        int adjustedValue = Bits.clip(8, v + (n ? -fix : fix));
        return packValueZNHC(adjustedValue, adjustedValue == 0, n, false, fixH);
    }

    /**
     * Computes the "and" bit to bit operation of the two given 8 bit inputs
     * 
     * @param l input 1
     * @param r input 2
     * @return 8 bit value, outcome of the operation
     * @throws IllegalArgumentException 
     */
    public static int and(int l, int r) {
        check8Bits(l, r);
        return packValueZNHC(l & r, (l & r) == 0, false, true, false);
    }

    /**
     * Computes the "or" bit to bit operation of the two given 8 bit inputs
     * 
     * @param l input 1
     * @param r input 2
     * @return 8 bit value, outcome of the operation
     * @throws IllegalArgumentException 
     */
    public static int or(int l, int r) {
        check8Bits(l, r);
        return packValueZNHC(l | r, (l | r) == 0, false, false, false);
    }

    /**
     * Computes the "xor" bit to bit operation of the two given 8 bit inputs
     * 
     * @param l input 1
     * @param r input 2
     * @return 8 bit value, outcome of the operation
     * @throws IllegalArgumentException 
     */
    public static int xor(int l, int r) {
        check8Bits(l, r);
        return packValueZNHC(l ^ r, (l ^ r) == 0, false, false, false);
    }

    /**
     * Shifts the given 8 bit value by 1 towards the left
     * 
     * @param v value
     * @return shifted value and the flags Z00C where C is the ejected value due
     *         to shift
     * @throws IllegalArgumentException        
     */
    public static int shiftLeft(int v) {
        checkBits8(v);
        // save the value of ejected bit
        boolean c = Bits.test(v, 7);
        v = Bits.clip(8, v << 1);
        return packValueZNHC(v, v == 0, false, false, c);
    }

    /**
     * Shifts the given 8 bit value arithmetically by 1 towards the right
     * 
     * @param v value
     * @return shifted value and the flags Z00C where C is the ejected value due
     *         to shift
     */
    public static int shiftRightA(int v) {
        checkBits8(v);
        // save the value of ejected bit
        boolean c = Bits.test(v, 0);
        v = Bits.clip(8, Bits.signExtend8(v) >> 1);
        return packValueZNHC(v, v == 0, false, false, c);
    }

    /**
     * Shifts the given 8 bit value logically by 1 towards the right
     * 
     * @param v value
     * @return shifted value and the flags Z00C where C is the ejected value due
     *         to shift
     * @throws IllegalArgumentException 
     */
    public static int shiftRightL(int v) {
        checkBits8(v);
        return packValueZNHC(v >>> 1, (v >>> 1) == 0, false, false, Bits.test(v, 0));
    }

    /**
     * Rotates the given 8 bit value in the given direction over a distance of 1 bit
     * 
     * @param d, direction
     * @param v, value
     * @return rotated value package with the flags Z00C where c contains the
     *         bit passed from one side to the other due to rotation
     */
    public static int rotate(RotDir d, int v) {
        v = checkBits8(v);
        // default values set to rotate right
        int distance = - 1;
        // save the value that passes from one side to the other
        boolean carry = Bits.test(v, 0);
        if (d == RotDir.LEFT) {
            distance = 1;
            carry = Bits.test(v, 7);
        }
        int rotatedBits = Bits.rotate(8, v, distance);
        return packValueZNHC(rotatedBits, rotatedBits == 0, false, false, carry);
    }

    /**
     * Rotates the value composed of the given 8 bit value and the boolean c in
     * given direction over distance of 1 bit
     * 
     * @param d, direction
     * @param v, value
     * @return rotated value package with the flags Z00C where c is the new MSB
     *         bit
     */
    public static int rotate(RotDir d, int v, boolean c) {
        v = checkBits8(v);
        int vWithCarry = Bits.set(v, 8, c);
        int distance = d == RotDir.LEFT ? 1 : -1;
        int rotatedBits = Bits.rotate(9, vWithCarry, distance);
        boolean carry = Bits.test(rotatedBits, 8);
        return packValueZNHC(Bits.clip(8, rotatedBits), Bits.clip(8, rotatedBits) == 0, false, false, carry);
    }

    /**
     * Swaps the 4 msb bits with the 4 lsb bits
     * 
     * @param v value
     * @return A value/flags package where the 4msb/lsb bits of the value are
     *         swapped
     */
    public static int swap(int v) {
        v = checkBits8(v);
        int lowB = Bits.clip(4, v);
        int highB = Bits.extract(v, 4, 4);
        int newValue = (lowB << 4) | highB;
        return packValueZNHC(newValue, newValue == 0, false, false, false);
    }

    /**
     * Returns the value 0 packed with the flags Z010 where Z is true if the the
     * bit at given index is 0
     * 
     * @param v value
     * @param bitIndex
     * @return the value 0 packed with the flags Z010 where Z is true if the the
     *         bit at given index is 0
     * @throws IndexOutOfBoundsException
     */
    public static int testBit(int v, int bitIndex) throws IndexOutOfBoundsException {
        v = checkBits8(v);
        Objects.checkIndex(bitIndex, Byte.SIZE);
        return packValueZNHC(0, !Bits.test(v, bitIndex), false, true, false);
    }

    // Verifies that two given inputs are 8 bit numbers
    private static void check8Bits(int l, int r) throws IllegalArgumentException {
        l = checkBits8(l);
        r = checkBits8(r);
    }
}
