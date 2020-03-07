package ch.epfl.gameboj.component.cpu;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import ch.epfl.gameboj.component.cpu.Alu.RotDir;

class AluTestMe {

    @Test
    void unpackValueTest() {      
        assertEquals(0xFF, Alu.unpackValue(0xFF70)); 
        assertEquals(0x70, Alu.unpackFlags(0xFF70));
        assertEquals(0x70, Alu.maskZNHC(false, true, true, true));
    }
    
    @Test
    void addTest1() {
        int a = 0x10;
        int b = 0x15;
       
        assertEquals(0x25, Alu.unpackValue(Alu.add(a, b))); 
        assertEquals(0x00, Alu.unpackFlags(Alu.add(a, b)));
    }
    
    @Test
    void addTest2() {
        int a = 0x08;
       
        assertEquals(0x10, Alu.unpackValue(Alu.add(a, a))); 
        assertEquals(0x20, Alu.unpackFlags(Alu.add(a, a)));
    }
    
    @Test
    void addTest3() {
        int a = 0x80;
        int b = 0x7F;
       
        assertEquals(0x00, Alu.unpackValue(Alu.add(a, b, true))); 
        assertEquals(0xB0, Alu.unpackFlags(Alu.add(a, b, true)));
    }
    
    @Test
    void subTest1() {
        int a = 0x10;
       
        assertEquals(0x00, Alu.unpackValue(Alu.sub(a, a))); 
        assertEquals(0xC0, Alu.unpackFlags(Alu.sub(a, a)));
    }
    
    @Test
    void subTest2() {
        int a = 0x10;
        int b = 0x80;
        
        assertEquals(0x90, Alu.unpackValue(Alu.sub(a, b))); 
        assertEquals(0x50, Alu.unpackFlags(Alu.sub(a, b)));
    }
    
    @Test
    void subTest3() {
        int a = 0x01;
        int b = 0x01;
        
        assertEquals(0xFF, Alu.unpackValue(Alu.sub(a, b, true))); 
        assertEquals(0x70, Alu.unpackFlags(Alu.sub(a, b, true)));
    }
    
    @Test
    void bcdAdjustTest1() {
        assertEquals(0x73, Alu.unpackValue(Alu.bcdAdjust(0x6D, false, false, false))); 
        assertEquals(0x00, Alu.unpackFlags(Alu.bcdAdjust(0x6D, false, false, false))); 
    }
    
    @Test
    void bcdAdjustTest2() {
        assertEquals(0x09, Alu.unpackValue(Alu.bcdAdjust(0x0f, true, true, false))); 
        assertEquals(0x40, Alu.unpackFlags(Alu.bcdAdjust(0x0f, true, true, false))); 
    }
    
    @Test
    void andOrXorTest() {
        int a = 0x53;
        int b = 0xA7;
        assertEquals(0x03, Alu.unpackValue(Alu.and(a, b)));
        assertEquals(0xF7, Alu.unpackValue(Alu.or(a, b)));
        assertEquals(0xF4, Alu.unpackValue(Alu.xor(a, b)));
        
        assertEquals(0x20, Alu.unpackFlags(Alu.and(a, b)));
        assertEquals(0x00, Alu.unpackFlags(Alu.or(a, b)));
        assertEquals(0x00, Alu.unpackFlags(Alu.xor(a, b)));
    }
    
    @Test
    void shiftLeftTest() {
        int b = 0x80;
        
        assertEquals(0x00, Alu.unpackValue(Alu.shiftLeft(b))); 
        assertEquals(0x90, Alu.unpackFlags(Alu.shiftLeft(b)));
    }
    
    @Test
    void shiftRightLTest() {
        int b = 0x80;
        
        assertEquals(0x40, Alu.unpackValue(Alu.shiftRightL(b))); 
        assertEquals(0x00, Alu.unpackFlags(Alu.shiftRightL(b)));
    }
    
    @Test
    void shiftRightATest() {
        int b = 0x80;
        
        assertEquals(0xC0, Alu.unpackValue(Alu.shiftRightA(b))); 
        assertEquals(0x00, Alu.unpackFlags(Alu.shiftRightA(b)));
    }
    
    @Test
    void rotateTest1() {
        int b = 0x80;
        
        assertEquals(0x40, Alu.unpackValue(Alu.rotate(RotDir.RIGHT, b))); 
        assertEquals(0x00, Alu.unpackFlags(Alu.rotate(RotDir.RIGHT, b)));
    }
    
    @Test
    void rotateTest2() {
        int b = 0x80;
        
        assertEquals(0x00, Alu.unpackValue(Alu.rotate(RotDir.LEFT, b, false))); 
        assertEquals(0x90, Alu.unpackFlags(Alu.rotate(RotDir.LEFT, b, false)));
    }
    
    @Test
    void rotateTest3() {
        int b = 0x00;
        
        assertEquals(0x01, Alu.unpackValue(Alu.rotate(RotDir.LEFT, b, true))); 
        assertEquals(0x00, Alu.unpackFlags(Alu.rotate(RotDir.LEFT, b, true)));
    }
    
    @Test
    void add16LTest() {
        assertEquals(0x1200, Alu.unpackValue(Alu.add16L(0x11ff, 0x0001))); 
        assertEquals(0x30, Alu.unpackFlags(Alu.add16L(0x11ff, 0x0001)));
    }
    
    @Test
    void add16HTest() {
        assertEquals(0x1200, Alu.unpackValue(Alu.add16H(0x11ff, 0x0001))); 
        assertEquals(0x00, Alu.unpackFlags(Alu.add16H(0x11ff, 0x0001)));
    }
    
    @Test
    void swapTest() {
        assertEquals(0b0000_1010, Alu.unpackValue(Alu.swap(0b1010_0000))); 
        assertEquals(0b10000000, Alu.unpackFlags(Alu.swap(0b00000000)));
    }

    @Test
    void testBitTest() {
        assertEquals(0b00, Alu.unpackValue(Alu.testBit(0b0100_0000, 6))); 
        assertEquals(0b0010_0000, Alu.unpackFlags(Alu.testBit(0b0100_0000, 6)));
    }
}
