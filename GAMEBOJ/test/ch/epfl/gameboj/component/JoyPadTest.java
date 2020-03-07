package ch.epfl.gameboj.component;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.component.Joypad.Key;
import ch.epfl.gameboj.component.cpu.Cpu;

class JoyPadTest {

    @Test
    void test() {
    Cpu cpu = new Cpu();
    Joypad p = new Joypad(cpu);
    p.write((AddressMap.REG_P1), 0b1100_1111);
    assertEquals(207, p.read(AddressMap.REG_P1));
    p.keyPressed(Key.START);
    assertEquals(199, p.read(AddressMap.REG_P1));
    p.keyReleased(Key.START);
    assertEquals(207, p.read(AddressMap.REG_P1));
    }
    
    @Test
    void testDown() {
        Cpu cpu = new Cpu();
        Joypad p = new Joypad(cpu);
        p.write((AddressMap.REG_P1), 0b1110_1111);
        assertEquals(0b1110_1111, p.read(AddressMap.REG_P1));
        p.keyPressed(Key.DOWN);
        assertEquals(0b11100111, p.read(AddressMap.REG_P1));
        
        p.keyReleased(Key.DOWN);
        assertEquals(0b1110_1111, p.read(AddressMap.REG_P1));
    }

}
