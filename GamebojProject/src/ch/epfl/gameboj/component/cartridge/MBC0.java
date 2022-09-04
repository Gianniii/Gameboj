package ch.epfl.gameboj.component.cartridge;

import static ch.epfl.gameboj.Preconditions.checkArgument;
import static ch.epfl.gameboj.Preconditions.checkBits16;
import java.util.Objects;

import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.memory.Rom;

/**
 * Class MBC0, represents a memory bank controller (type 0)
 * 
 * @author Omid Karimi (273816)
 * @author Gianni Lodetti (275085)
 */
public final class MBC0 implements Component {
    
    private final Rom rom;

    /**
     * Construct a controller for given Rom
     * 
     * @param rom
     * @throws NullPointerException
     * @throws IllegalArgumentException
     */
    public MBC0(Rom rom) throws NullPointerException, IllegalArgumentException {
        checkArgument(rom.size() == 0x8000);
        this.rom = Objects.requireNonNull(rom);
    }

    @Override
    public int read(int address) throws IllegalArgumentException {
        address = checkBits16(address);
        if (address >= rom.size()) {
            return Component.NO_DATA;
        }
        return rom.read(address);
    }

    @Override
    public void write(int address, int data) throws IllegalArgumentException {
        // Does nothing
    }

}
