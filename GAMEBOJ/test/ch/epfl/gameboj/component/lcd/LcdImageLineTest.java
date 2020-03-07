package ch.epfl.gameboj.component.lcd;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import ch.epfl.gameboj.bits.BitVector;

class LcdImageLineTest {

    @Test
    void testShift() {
        BitVector msb = new BitVector(64, true);
        BitVector lsb = new BitVector(64, true);
        BitVector opacity = new BitVector(64, true);
        LcdImageLine test = new LcdImageLine(msb, lsb, opacity);
        
        LcdImageLine line = new LcdImageLine(msb.shift(15), lsb.shift(15), opacity.shift(15)); 
        assertEquals(line, test.shift(15));
    }
    
    @Test
    void testMapColor() {
        BitVector msb = new BitVector(64, true);
        BitVector lsb = new BitVector(64, true);
        BitVector opacity = new BitVector(64, true);
        LcdImageLine test = new LcdImageLine(msb, lsb, opacity);
        
        LcdImageLine whiteLine = new LcdImageLine(msb.not(), lsb.not(), opacity);
        //System.out.println(test.mapColors(0b00000000).msb().toString());
        //System.out.println(test.mapColors(0b00000000).lsb().toString());
        //System.out.println(test.mapColors(0b00000000).opacity().toString());
        assertEquals(whiteLine, test.mapColors(0b00111100));
        
        BitVector m = new BitVector.Builder(64).setByte(0, 0b1010_0101)
                                                .build();
        BitVector l = new BitVector.Builder(64).setByte(0, 0b1111_1001)
                                                .build();
        BitVector m2 = new BitVector.Builder(64).setByte(0, 0b1111_1001)
                .setByte(1, 0b0000_0000).setByte(2, 0b0000_0000).setByte(3, 0b0000_0000)
                .setByte(4, 0b0000_0000).setByte(5, 0b0000_0000).setByte(6, 0b0000_0000)
                .setByte(7, 0b0000_0000).build();
        BitVector l2 = new BitVector.Builder(64).setByte(0, 0b0101_1010)
                .setByte(1, 0b1111_1111).setByte(2, 0b1111_1111).setByte(3, 0b1111_1111)
                .setByte(4, 0b1111_1111).setByte(5, 0b1111_1111).setByte(6, 0b1111_1111)
                .setByte(7, 0b1111_1111).build();
        
        LcdImageLine test2 = new LcdImageLine(m, l, opacity);
        
        /*System.out.println(m);
        System.out.println(l.toString());
        System.out.println(m2.toString());
        System.out.println(l2.toString());
        System.out.println();
        System.out.println("MSB " + test2.mapColors(0b10_00_11_01).msb());
        System.out.println("LSB " + test2.mapColors(0b10_00_11_01).lsb());*/
        
        assertEquals(new LcdImageLine(m2, l2, opacity), test2.mapColors(0b10_00_11_01));
    }
    
    @Test
    void completemapColorTest() {
        int palette = 0b00011011;

        BitVector.Builder lsb = new BitVector.Builder(64).setByte(0, 0b01010101)
                .setByte(1, 0b01010101).setByte(2, 0b01010101).setByte(4, 0b01010101).
                setByte(5, 0b01010101).setByte(6, 0b01010101).setByte(7, 0b01010101);

        BitVector.Builder msb = new BitVector.Builder(64).setByte(0, 0b00110011).
                setByte(1, 0b00110011).setByte(2,  0b00110011).setByte(4,  0b00110011).
                setByte(5, 0b00110011).setByte(6,  0b00110011).setByte(7,  0b00110011);

        BitVector lsbVect = lsb.build();

        LcdImageLine lineWithAllColors = new LcdImageLine(msb.build(), lsbVect, lsbVect);

        BitVector.Builder rlsb = new BitVector.Builder(64).setByte(0, 0b10101010).setByte(3, 0b1111_1111)
                .setByte(1,  0b10101010).setByte(2, 0b10101010).setByte(4,  0b10101010).
                setByte(5,  0b10101010).setByte(6,  0b10101010).setByte(7,  0b10101010);

        BitVector.Builder rmsb = new BitVector.Builder(64).setByte(0, 0b11001100).setByte(3, 0b1111_1111).
                setByte(1,0b11001100).setByte(2,  0b11001100).setByte(4,  0b11001100).
                setByte(5, 0b11001100).setByte(6,  0b11001100).setByte(7,  0b11001100);

        LcdImageLine resultingLine = new LcdImageLine(rmsb.build(), rlsb.build(), lsbVect);
        LcdImageLine lineWithAllColorsTransformed = lineWithAllColors.mapColors(palette);

        assertEquals(resultingLine , lineWithAllColorsTransformed);
        }
    
    @Test
    void testBelow() {
        BitVector m = new BitVector.Builder(64).setByte(0, 0b1010_0101)
                .build();
        BitVector l = new BitVector.Builder(64).setByte(0, 0b1111_1001)
                .build();
        BitVector opacity = new BitVector(64, true);
        
        BitVector lsb = new BitVector.Builder(64).setByte(0, 0b01010101)
                .setByte(1, 0b01010101).setByte(2, 0b01010101).setByte(4, 0b01010101).
                setByte(5, 0b01010101).setByte(6, 0b01010101).setByte(7, 0b01010101).build();

        BitVector msb = new BitVector.Builder(64).setByte(0, 0b00110011).
                setByte(1, 0b00110011).setByte(2,  0b00110011).setByte(4,  0b00110011).
                setByte(5, 0b00110011).setByte(6,  0b00110011).setByte(7,  0b00110011).build();
        
        LcdImageLine belowLine = new LcdImageLine(m, l, opacity);
        LcdImageLine upperLine = new LcdImageLine(msb, lsb, opacity.shift(32));
        LcdImageLine result = belowLine.below(upperLine);

        /*System.out.println(result.msb());
        System.out.println(result.lsb());
        System.out.println(result.opacity());*/
        assertEquals("0011001100110011001100110011001100000000000000000000000010100101", result.msb().toString());
        assertEquals("0101010101010101010101010101010100000000000000000000000011111001", result.lsb().toString());
 
    }
    
    @Test
    void testJoin() {
        BitVector m = new BitVector.Builder(64).setByte(0, 0b1010_0101)
                .build();
        BitVector l = new BitVector.Builder(64).setByte(0, 0b1111_1001)
                .build();
        BitVector opacity = new BitVector(64, true);
        
        BitVector lsb = new BitVector.Builder(64).setByte(0, 0b01010101)
                .setByte(1, 0b01010101).setByte(2, 0b01010101).setByte(4, 0b01010101).setByte(3, 0b11111111)
                .setByte(5, 0b01010101).setByte(6, 0b01010101).setByte(7, 0b01010101).build();

        BitVector msb = new BitVector.Builder(64).setByte(0, 0b00110011).
                setByte(1, 0b00110011).setByte(2,  0b00110011).setByte(4,  0b00110011).setByte(3, 0b11111111)
                .setByte(5, 0b00110011).setByte(6,  0b00110011).setByte(7,  0b00110011).build();
        
        LcdImageLine belowLine = new LcdImageLine(m, l, opacity);
        LcdImageLine upperLine = new LcdImageLine(msb, lsb, opacity.shift(32));
        LcdImageLine result = belowLine.join(upperLine, 16);
        
        /*System.out.println(result.msb());
        System.out.println(result.lsb());
        System.out.println(result.opacity());*/
        
        assertEquals("0011001100110011001100110011001111111111001100110000000010100101", result.msb().toString());
        assertEquals("0101010101010101010101010101010111111111010101010000000011111001", result.lsb().toString());
    }
    
    @Test
    void testJoinOpacities() {
        BitVector v = new BitVector.Builder(64).setByte(0, 0)
                .setByte(1, 0).setByte(2, 0).setByte(4, 0).setByte(3, 0)
                .setByte(5, 0).setByte(6, 0).setByte(7, 0).build();
        BitVector v1 = new BitVector.Builder(64).setByte(0, 0xFF)
                .setByte(1, 0xFF).setByte(2, 0xFF).setByte(4, 0xFF).setByte(3, 0xFF)
                .setByte(5, 0xFF).setByte(6, 0xFF).setByte(7, 0xFF).build();
        
        LcdImageLine above = new LcdImageLine(v, v, v);
        LcdImageLine below = new LcdImageLine(v1, v1 ,v1);
        LcdImageLine result = above.join(below, 16);
        System.out.println(above.opacity().toString());
        System.out.println(below.opacity().toString());
        System.out.println(result.opacity().toString());
        
    }
    @Test
    void testBuilder() {
        //is
    }
    
}
