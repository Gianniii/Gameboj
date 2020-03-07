package ch.epfl.gameboj.bits;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class BitVectorTest {
   /* @Disabled
    @Test
    void testNot() {
        int[] v = new int[8];
        int[] vt = new int[8];
        for (int i = 0; i < 8; i++) {
            v[i] = 0b1111_0000_1111_0000_1111_0000_1111_0000;
            vt[i] = 0b0000_1111_0000_1111_0000_1111_0000_1111;
        }
        BitVector b = new BitVector(v);
        for (int i = 0; i < 8; i++) {
            assertEquals(vt[i], b.not().vectorValues[i]);
        }
    }
    @Disabled
    @Test
    void testAnd() {
        int[] v = new int[8];
        int[] vt = new int[8];
        for (int i = 0; i < 8; i++) {
            v[i] = 0b1111_0000_1111_0000_1111_0000_1111_0000;
            vt[i] = 0b0000_1111_0000_1111_0000_1111_0000_1111;
        }
        BitVector a = new BitVector(v);
        BitVector b = new BitVector(vt);
        for (int i = 0; i < 8; i++) {
            assertEquals(0, a.and(b).vectorValues[i]);
        }
    }*/

 /*   @Test
    void testOr() {
        int[] v = new int[8];
        int[] vt = new int[8];
        for (int i = 0; i < 8; i++) {
            v[i] = 0b1111_0000_1111_0000_1111_0000_1111_0000;
            vt[i] = 0b0000_1111_0000_1111_0000_1111_0000_1111;
        }
        BitVector a = new BitVector(v);
        BitVector b = new BitVector(vt);
        for (int i = 0; i < 8; i++) {
            assertEquals(0b1111_1111_1111_1111_1111_1111_1111_1111,
                    a.or(b).vectorValues[i]);
        }
    }*/

    @Test
    void testTestBit() {
        int[] v = new int[2];
        for (int i = 0; i < 2; i++) {
            v[i] = 0b1111_0000_1111_0000_1111_0000_1111_0000;
        }
        BitVector a = new BitVector(v);
        // assertEquals(0 , (a.testBit(64) ? 1 : 0));
        // System.out.println(a.not().toString());
    }

    @Test
    void testExtract0() {

        BitVector a = new BitVector(32, true);
        BitVector test = new BitVector(64, true);
        BitVector x = test.extractZeroExtended(-16, 128);
        BitVector y = test.extractZeroExtended(30, 128);
        BitVector b = a.extractZeroExtended(-17, 128);
        BitVector c = a.extractZeroExtended(30, 128);
        BitVector d = a.extractZeroExtended(-31, 64);
        assertEquals(
                "0000000000000000000000000000000000000000000000000000000000000000000000000000"
                        + "0001111111111111111111111111111111100000000000000000",
                b.toString());
        assertEquals("11111111111111111111111111111111", a.toString());
        assertEquals(
                "0000000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000011",
                c.toString());
        assertEquals(
                "0111111111111111111111111111111110000000000000000000000000000000",
                d.toString());
        
        assertEquals("0000000000000000000000000000000000000000000000001111111111111111111111111111111"
                     + "1111111111111111111111111111111110000000000000000" , x.toString());
        assertEquals("000000000000000000000000000000000000000000000000000000000000000000000"
                + "00000000000000000000000001111111111111111111111111111111111" , y.toString());

    }

    @Test
    void testExtractWrapped() {

        BitVector test = new BitVector(64, true);
        // BitVector c = b.extractWrapped(11, 128);

        BitVector d = test.shift(8).extractWrapped(40, 128);
        BitVector e = test.shift(5).shift(-17).extractWrapped(-2, 128);
        BitVector f = test.shift(39).extractWrapped(328, 128);
        
        /*
         * System.out.println(test.toString());
         * System.out.println(x.toString()); System.out.println(y.toString());
         */
        //System.out.println(d.toString());
        
        //System.out.println(test.shift(39).toString());
        //System.out.println(f.toString());
        assertEquals("11111111111111111111111111111111000000001111111111111111111111111111111111"
                + "111111111111111111111100000000111111111111111111111111" , d.toString());
        assertEquals("000000000000000111111111111111111111111111111111111111"
                + "11111111000000000000000001111111111111111111111111111111111111111111111100" , e.toString());
        assertEquals("000000001111111111111111111111111000000000000000000000000000000000000000"
                + "11111111111111111111111110000000000000000000000000000000" , f.toString());



    }

    @Test
    void testShift() {

        BitVector a = new BitVector(32, true);
        BitVector a2 = new BitVector(64, true);
        BitVector a3 = new BitVector(256, true);
        BitVector c1 = new BitVector(256, false);
        BitVector b = a.extractZeroExtended(-8, 32);
        BitVector c = a2.extractZeroExtended(10, 64);
        /*System.out.println(a.shift(8).toString());
        System.out.println(b.toString());
        System.out.println(a.shift(8).equals(b));*/
        assertEquals(b, a.shift(8));
        assertEquals(c, a2.shift(-10));
        assertEquals(c1, a3.shift(256));
    }

    @Test
    void testBuilder() {

        BitVector v = new BitVector.Builder(64).setByte(0, 0b1111_0000)
                .setByte(1, 0b1010_1010).setByte(3, 0b1100_1100)
                .setByte(5, 0b1111_1111).setByte(4, 0b0000_1111).build();
        System.out.println(v);
        assertEquals("0000000000000000111111110000111111001100000000001010101011110000", v.toString());
        
        //System.out.println(v);
        //System.out.println(v.extractZeroExtended(30, 64));
        //System.out.println(v.extractWrapped(-10, 128));
    }
}
