package ch.epfl.gameboj.bits;

/**
 * Interface Bit
 * 
 * @author Omid Karimi (273816)
 * @author Gianni Lodetti (275085)
 */
public interface Bit {

    int ordinal();

    /**
     * better name for the method ordinal()
     * 
     * @return same value as ordinal
     */
    default int index() {
        return ordinal();
    }

    /**
     * returns the mask corresponding to the bit
     * 
     * @return
     */
    default int mask() {
        return Bits.mask(index());
    }
}
