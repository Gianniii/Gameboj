package ch.epfl.gameboj.component;

import ch.epfl.gameboj.Bus;

/**
 * Interface Component
 * 
 * @author Omid Karimi (273816)
 * @author Gianni Lodetti (275085)
 */
public interface Component {

    /**
     * signals that there no data at given address
     */
    public final static int NO_DATA = 0x100;

    /**
     * return byte at given address or returns NO_DATA if there nothing at the
     * address
     * 
     * @param address
     * @return a data
     * @throws IllegalArgumentException
     *             if given address is bigger than 16 bits
     */
    public abstract int read(int address) throws IllegalArgumentException;

    /**
     * write given data at given address or does nothing if component does not
     * allow to write data at given address
     * 
     * @param address
     * @param data
     * @throws IllegalArgumentException
     *             if given address is bigger than 16 bits or data is bigger
     *             than 8 bits
     */
    public abstract void write(int address, int data) throws IllegalArgumentException;

    /**
     * Attaches component to given bus
     * 
     * @param bus
     */
    default void attachTo(Bus bus) {
        bus.attach(this);
    }
}
