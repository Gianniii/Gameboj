package ch.epfl.gameboj.component.lcd;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import ch.epfl.gameboj.component.DebugMain2;
import ch.epfl.gameboj.component.DebugMain3;

class LcdControllerTest {
    @Disabled
    @Test
    void DebugMainTest() throws IOException {
       String[][] s = {{"01-special.gb", "30000000"}/*, {"02-interrupts.gb", "30000000"}, {"03-op sp,hl.gb", "30000000"},
        {"04-op r,imm.gb", "30000000"}, {"05-op rp.gb", "30000000"}, {"06-ld r,r.gb", "30000000"}, 
        {"07-jr,jp,call,ret,rst.gb", "30000000"}, {"08-misc instrs.gb", "30000000"}, {"09-op r,r.gb", "30000000"},
        {"10-bit ops.gb", "30000000"}, {"11-op a,(hl).gb", "30000000"}, {"instr_timing.gb", "30000000"}*/};
        
        for (int i = 0; i < s.length; ++i) {
            DebugMain2.main(s[i]);
        }
       
      /*  String[][] smd = {{"flappyboy.gb", "30000000"}};
        for (int i = 0; i < s.length; ++i) {
            DebugMain2.main(smd[i]);
        }*/
    }

    @Test
    void DebugMainTest3() throws IOException {
         
         String[] s = {"sprite_priority.gb"};
        
         DebugMain3.main(s);
         
     }


}
