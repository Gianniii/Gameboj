package ch.epfl.gameboj;

/**
 * Interface Register
 * 
 * @author Omid Karimi (273816)
 * @author Gianni Lodetti (275085)
 */
public interface Register {

    int ordinal();

    /**
     *  Acts like ordinal()
     *  
     * @return index of the register in the enumeration
     */
    public default int index() {
        return ordinal();
    }
}
