package ch.epfl.gameboj;

import java.util.Objects;

import static ch.epfl.gameboj.Preconditions.checkArgument;

import ch.epfl.gameboj.component.Joypad;
import ch.epfl.gameboj.component.Timer;
import ch.epfl.gameboj.component.cartridge.Cartridge;
import ch.epfl.gameboj.component.cpu.Cpu;
import ch.epfl.gameboj.component.lcd.LcdController;
import ch.epfl.gameboj.component.memory.BootRomController;
import ch.epfl.gameboj.component.memory.Ram;
import ch.epfl.gameboj.component.memory.RamController;

/**
 * Class GameBoy !
 * 
 * @author Omid Karimi (273816)
 * @author Gianni Lodetti (275085)
 */
public final class GameBoy {
    public static final long CYCLES_PER_SECOND = 1 << 20;
    public static final double CYCLES_PER_NS = CYCLES_PER_SECOND * Math.pow(10, -9);
    private final Bus bus;
    private final Cpu cpu;
    private long cycles = 0;
    private final Timer timer;
    private final LcdController lcdController;
    private final Joypad joypad;

    /**
     * Construct the gameboy
     * 
     * @param cartridge
     */
    public GameBoy(Cartridge cartridge) throws NullPointerException {
        Ram workRam = new Ram(AddressMap.WORK_RAM_SIZE);
        RamController workRamController = 
                new RamController(workRam, AddressMap.WORK_RAM_START, AddressMap.WORK_RAM_END);
        RamController workRamCopyController = 
                new RamController(workRam, AddressMap.ECHO_RAM_START, AddressMap.ECHO_RAM_END);
        BootRomController bootRomController = 
                new BootRomController(Objects.requireNonNull(cartridge));
        bus = new Bus();
        cpu = new Cpu();
        timer = new Timer(cpu);
        lcdController = new LcdController(cpu);
        joypad = new Joypad(cpu);
        bus.attach(workRamController);
        bus.attach(workRamCopyController);
        cpu.attachTo(bus);
        lcdController.attachTo(bus);
        bus.attach(bootRomController);
        bus.attach(timer);
        bus.attach(joypad);
    }

    /**
     * Returns the bus of the gameboy
     * 
     * @return bus
     */
    public Bus bus() {
        return bus;
    }

    /**
     * Returns the cpu of the gameboy
     * 
     * @return cpu
     */
    public Cpu cpu() {
        return cpu;
    }

    /**
     * Calls cycle method of all clocked components until given cycle -1
     * 
     * @param cycle
     * @throws IllegalArgumentException
     */
    public void runUntil(long cycle) throws IllegalArgumentException {
        checkArgument(cycles <= cycle);
        while (cycles < cycle) {
            timer.cycle(cycles);
            lcdController.cycle(cycles);
            cpu.cycle(cycles);
            ++cycles;
        }
    }

    /**
     * Returns total numbers of cycles simulated
     * 
     * @return cycles
     */
    public long cycles() {
        return cycles;
    }

    /**
     * Returns the timer of the gameboy
     * 
     * @return timer
     */
    public Timer timer() {
        return timer;
    }
    
    /**
     * 
     * @return
     */
    public LcdController lcdController() {
        return lcdController;
    }
    
    /**
     * 
     * @return
     */
    public Joypad joypad() {
        return joypad;
    }
}
