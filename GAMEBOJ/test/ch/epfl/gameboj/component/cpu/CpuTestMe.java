package ch.epfl.gameboj.component.cpu;

import static org.junit.Assert.assertArrayEquals;

import java.io.IOException;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.Bus;
import ch.epfl.gameboj.DebugMain;
import ch.epfl.gameboj.component.memory.Ram;
import ch.epfl.gameboj.component.memory.RamController;

public class CpuTestMe {
    private Bus connect(Cpu cpu, Ram ram) {
        RamController rc = new RamController(ram, 0);
        Bus b = new Bus();
        cpu.attachTo(b);
        rc.attachTo(b);
        return b;
    }

    private void cycleCpu(Cpu cpu, long cycles) {
        for (long c = 0; c < cycles; ++c)
            cpu.cycle(c);
    }

   /* @Test
    void nopDoesNothing() {
        Cpu c = new Cpu();
        Ram r = new Ram(10);
        Bus b = connect(c, r);
        b.write(0, Opcode.NOP.encoding);
        cycleCpu(c, Opcode.NOP.cycles);
        
        b.write(1, Opcode.LD_B_N8.encoding);
        b.write(2, 0x30);
        cycleCpu(c, Opcode.LD_B_N8.cycles);
        assertArrayEquals(new int[] {3,0,0,0,0x30,0,0,0,0,0}, c._testGetPcSpAFBCDEHL());
    }
    */
    @Test
    void testADD_A_B() {
        Cpu c = new Cpu();
        Ram r = new Ram(11);
        Bus b = connect(c, r);
        b.write(0, Opcode.LD_B_N8.encoding);
        b.write(1, 0b0000_1000);
        b.write(2, Opcode.LD_A_N8.encoding);
        b.write(3, 0b0100_1000);
        b.write(4, Opcode.ADD_A_B.encoding);
        b.write(5, Opcode.CCF.encoding);
        
        //b.write(1, Opcode.POP_BC.encoding);
        cycleCpu(c, Opcode.LD_B_N8.cycles + Opcode.LD_A_N8.cycles + Opcode.ADD_A_B.cycles + Opcode.CCF.cycles);
        assertArrayEquals(new int[] {6, 0, 0b0101_0000, 0b0001_0000, 0b0000_1000, 0, 0, 0, 0, 0}, c._testGetPcSpAFBCDEHL()); 
    }
    
    @Test
    void testSWAP_B() {
        Cpu c = new Cpu();
        Ram r = new Ram(11);
        Bus b = connect(c, r);
        b.write(0, Opcode.LD_B_N8.encoding);
        b.write(1, 0b1000_0001);
        b.write(2, 0xCB);
        b.write(3, Opcode.SWAP_B.encoding);
        
        //b.write(1, Opcode.POP_BC.encoding);
        cycleCpu(c, Opcode.LD_B_N8.cycles + Opcode.ADD_A_B.cycles);
        assertArrayEquals(new int[] {4, 0, 0, 0, 0b0001_1000, 0, 0, 0, 0, 0}, c._testGetPcSpAFBCDEHL()); 
    }
    
    @Test
    void testDEC_R16SP() {
        Cpu c = new Cpu();
        Ram r = new Ram(11);
        Bus b = connect(c, r);
        b.write(0, Opcode.LD_SP_N16.encoding);
        b.write(1, 8);
        b.write(2, 0);
        b.write(3, Opcode.DEC_SP.encoding);
        
        //b.write(1, Opcode.POP_BC.encoding);
        cycleCpu(c, Opcode.LD_SP_N16.cycles + Opcode.DEC_SP.cycles);
        assertArrayEquals(new int[] {4, 7, 0, 0, 0, 0, 0, 0, 0, 0}, c._testGetPcSpAFBCDEHL()); 
    }
    
    @Disabled
    @Test
    void testFIB() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);
        byte[] opcodes = new byte[] {
                (byte)0x31, (byte)0xFF, (byte)0xFF, (byte)0x3E,
                (byte)0x0B, (byte)0xCD, (byte)0x0A, (byte)0x00,
                (byte)0x76, (byte)0x00, (byte)0xFE, (byte)0x02,
                (byte)0xD8, (byte)0xC5, (byte)0x3D, (byte)0x47,
                (byte)0xCD, (byte)0x0A, (byte)0x00, (byte)0x4F,
                (byte)0x78, (byte)0x3D, (byte)0xCD, (byte)0x0A,
                (byte)0x00, (byte)0x81, (byte)0xC1, (byte)0xC9, 
              };
       for (int i = 0; i < opcodes.length; ++i) {
           b.write(i, Byte.toUnsignedInt(opcodes[i]));
       }
       //cycleCpu(c, 5800);
       //int a = 0;
       
       /*while (c.Pc != 8) {
           if(a< 10) {
               //System.out.println(c.Pc);
           }
           c.cycle(a);
           ++a;
           //System.out.println(c.Pc);
       }
       */
       assertArrayEquals(new int[] {8, 0xFFFF, 89, 0, 0, 0, 0, 0, 0, 0}, c._testGetPcSpAFBCDEHL()); 
    }
    
    @Test
    void testFIBtest() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);
        byte[] opcodes = new byte[] {
                (byte)0x31, (byte)0xFF, (byte)0xFF, (byte)0x3E,
                (byte)0x0B, (byte)0xCD, (byte)0x0A, (byte)0x00,
              };
       for (int i = 0; i < opcodes.length; ++i) {
           b.write(i, Byte.toUnsignedInt(opcodes[i]));
       }
       cycleCpu(c, 9);
             
       assertArrayEquals(new int[] {0x00_0A, 0xFFFF-2, 11, 0, 0, 0, 0, 0, 0, 0}, c._testGetPcSpAFBCDEHL()); 
       
    }
    
    @Test
    void testInterrupt() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);
        
        c.write(AddressMap.REG_IE, 2);
        c.write(AddressMap.REG_IF, 2);
        b.write(0x48, 0x06);
        b.write(0x49, 8);
        b.write(0x4A, 0xD9);
        byte[] opcodes = new byte[] {
                (byte)0x31, (byte)0xFF, (byte)0xFF, (byte)0xFB,
                (byte)0x0E, (byte)0x10,
              };
       for (int i = 0; i < opcodes.length; ++i) {
           b.write(i, Byte.toUnsignedInt(opcodes[i]));
       }
       cycleCpu(c, 16);
       assertArrayEquals(new int[] {6, 0xFFFF, 0, 0, 8, 16, 0, 0, 0, 0}, c._testGetPcSpAFBCDEHL());
        
    }
    
    @Disabled
    @Test
    void DebugMainTest() throws IOException {
        String[][] s = {{"01-special.gb", "30000000"}, {"02-interrupts.gb", "30000000"}, {"03-op sp,hl.gb", "30000000"},
        {"04-op r,imm.gb", "30000000"}, {"05-op rp.gb", "30000000"}, {"06-ld r,r.gb", "30000000"}, 
        {"07-jr,jp,call,ret,rst.gb", "30000000"}, {"08-misc instrs.gb", "30000000"}, {"09-op r,r.gb", "30000000"},
        {"10-bit ops.gb", "30000000"}, {"11-op a,(hl).gb", "30000000"}, {"instr_timing.gb", "30000000"}};
        
        for (int i = 0; i < s.length; ++i) {
            DebugMain.main(s[i]);
        }
    }
}

