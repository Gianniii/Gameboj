package ch.epfl.gameboj.component.memory;

import static ch.epfl.gameboj.Preconditions.checkBits16;
import static ch.epfl.gameboj.Preconditions.checkBits8;
import static ch.epfl.gameboj.Preconditions.checkArgument;

import java.util.Objects;

import ch.epfl.gameboj.component.Component;

/**
 * Class RamController, controls access to ram
 * 
 * @author Omid Karimi (273816)
 * @author Gianni Lodetti (275085)
 */
public final class RamController implements Component {

    private final Ram ram;
    private final int startAddress, endAddress;

    /** 
     * Construct controller for given ram with an address interval
     * 
     * @param ram
     * @param startAddress
     * @param endAddress
     * @throws NullPointerException
     * @throws IllegalArgumentException
     */
    public RamController(Ram ram, int startAddress, int endAddress) 
            throws NullPointerException, IllegalArgumentException {
        Objects.requireNonNull(ram);
        checkArgument(startAddress < endAddress && endAddress - startAddress <= ram.size());
        this.ram = ram;
        this.startAddress = checkBits16(startAddress);
        this.endAddress = checkBits16(endAddress);
    }

    /**
     * Calls previous constructor, address interval includes all ram indexes
     * 
     * @param ram
     * @param startAddress
     */
    public RamController(Ram ram, int startAddress) {
        this(ram, startAddress, ram.size() + startAddress);
    }

    @Override
    public int read(int address) throws IllegalArgumentException {
        address = checkBits16(address);
        if (address >= startAddress && address < endAddress) {
            return ram.read(address - startAddress);
        }
        return NO_DATA;
    }

    @Override
    public void write(int address, int data) throws IllegalArgumentException {
        address = checkBits16(address);
        data = checkBits8(data);
        if (address >= startAddress && address < endAddress) {
            ram.write(address - startAddress, data);
        }
    }

}
