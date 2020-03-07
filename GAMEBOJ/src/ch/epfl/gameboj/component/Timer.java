package ch.epfl.gameboj.component;

import java.util.Objects;
import static ch.epfl.gameboj.Preconditions.checkBits16;
import static ch.epfl.gameboj.Preconditions.checkBits8;
import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.cpu.Cpu;
import ch.epfl.gameboj.component.cpu.Cpu.Interrupt;

/**
 * Class Timer, represents timer of the cpu
 * 
 * @author Omid Karimi (273816)
 * @author Gianni Lodetti (275085)
 */
public final class Timer implements Component, Clocked {
    private final Cpu cpu;
    private int mainTimer = 0;
    // secondary timer
    private int TIMA = 0;
    private int TMA;
    private int TAC;
    private boolean s0 = state();

    /**
     * Construct a timer of the cpu
     * 
     * @param cpu
     * @throws NullPointerException
     */
    public Timer(Cpu cpu) throws NullPointerException {
        this.cpu = Objects.requireNonNull(cpu);
    }

    @Override
    public void cycle(long cycle) {
        s0 = state();
        mainTimer = Bits.clip(16, mainTimer + 4);
        // increments secondary timer
        incIfChange(s0);
        // request interruption if overflow
        if (TIMA > 0xFF) {
            cpu.requestInterrupt(Interrupt.TIMER);
            TIMA = TMA;
        }
    }

    @Override
    public int read(int address) throws IllegalArgumentException {
        address = checkBits16(address);
        if (address == AddressMap.REG_DIV) {
            // because only the 8 MSB bits are at this address
            return (mainTimer >> 8);
        }
        if (address == AddressMap.REG_TIMA) {
            return TIMA;
        }
        if (address == AddressMap.REG_TMA) {
            return TMA;
        }
        if (address == AddressMap.REG_TAC) {
            return TAC;
        }
        return Component.NO_DATA;
    }

    @Override
    public void write(int address, int data) throws IllegalArgumentException {
        address = checkBits16(address);
        data = checkBits8(data);
        if (address == AddressMap.REG_DIV) {
            s0 = state();
            mainTimer = 0;
            incIfChange(s0);
        }
        if (address == AddressMap.REG_TIMA) {
            TIMA = data;
        }
        if (address == AddressMap.REG_TMA) {
            TMA = data;
        }
        if (address == AddressMap.REG_TAC) {
            s0 = state();
            TAC = data;
            incIfChange(s0);
        }
    }

    // returns the state of the timer 
    private boolean state() {
        return (Bits.test(TAC, 2) && Bits.test(mainTimer, mainTimerIndex()));
    }

    // find index in main timer, which is used to increment second timer
    private int mainTimerIndex() {
        switch (Bits.clip(2, TAC)) {
        case 0b00: {
            return 9;
        }
        case 0b01: {
            return 3;
        }
        case 0b10: {
            return 5;
        }
        case 0b11: {
            return 7;
        }
        default: {
            return 0; // should never happen
        }
        }
    }

    // increments secondary timer if the conditions are satisfied
    private void incIfChange(boolean previousState) {
        if (previousState && !state()) {
            TIMA++;
        }
    }

}
