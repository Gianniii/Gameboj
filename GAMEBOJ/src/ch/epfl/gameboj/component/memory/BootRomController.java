package ch.epfl.gameboj.component.memory;

import java.util.Objects;
import static ch.epfl.gameboj.Preconditions.checkBits16;
import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.cartridge.Cartridge;

/**
 * Class BootRomController, represents the controller of the BootRom
 * 
 * @author Omid Karimi (273816)
 * @author Gianni Lodetti (275085)
 */
public final class BootRomController implements Component {
    
    private final Cartridge cartridge;
    private boolean bootRomIsActive = true;
    private final Rom bootRom = new Rom(BootRom.DATA);

    /**
     * Construct a controller for the BootRom
     * 
     * @param cartridge
     * @throws NullPointerException
     */
    public BootRomController(Cartridge cartridge) throws NullPointerException {
        this.cartridge = Objects.requireNonNull(cartridge);
    }

    @Override
    public int read(int address) throws IllegalArgumentException {
        address = checkBits16(address);
        if (bootRomIsActive && address >= AddressMap.BOOT_ROM_START
                && address < AddressMap.BOOT_ROM_END) {
            return bootRom.read(address);
        } else if (!bootRomIsActive || address >= AddressMap.BOOT_ROM_END) {
            return cartridge.read(address);
        }
        return Component.NO_DATA;
    }

    @Override
    public void write(int address, int data) throws IllegalArgumentException {
        if (address == AddressMap.REG_BOOT_ROM_DISABLE) {
            bootRomIsActive = false;
        }
        cartridge.write(address, data);
    }

}
