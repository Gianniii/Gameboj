package ch.epfl.gameboj;

import static ch.epfl.gameboj.Preconditions.checkBits16;
import static ch.epfl.gameboj.Preconditions.checkBits8;

import java.util.ArrayList;
import java.util.Objects;

import ch.epfl.gameboj.component.Component;

/**
 * Class Bus, represents data and address bus
 * 
 * @author Omid Karimi (273816)
 * @author Gianni Lodetti (275085)
 */
public final class Bus {

    private final ArrayList<Component> components = new ArrayList<Component>();

    /**
     * Attaches given component to bus
     * 
     * @param component
     * @throws NullPointerException
     *             if component is null
     */
    public void attach(Component component) throws NullPointerException {
        components.add(Objects.requireNonNull(component));
    }

    /**
     * returns value at given address of one of the component attached to the
     * bus
     * 
     * @param address
     * @return a value (data)
     * @throws IllegalArgumentException
     *             if the address is not a 16 bits value
     */
    public int read(int address) throws IllegalArgumentException {
        int data = Component.NO_DATA;
        address = checkBits16(address);
        for (Component c : components) {
            data = c.read(address);
            if (data != Component.NO_DATA) {
                return data;
            }
        }
        return 0xFF;
    }

    /**
     * write given value in given address for all attached components
     * 
     * @param address
     * @param data
     * @throws IllegalArgumentException
     *             if given address is bigger than 16 bits or data is bigger
     *             than 8 bits
     */
    public void write(int address, int data) throws IllegalArgumentException {
        address = checkBits16(address);
        data = checkBits8(data);
        for (Component c : components) {
            c.write(address, data);
        }
    }
}
