package ch.epfl.gameboj;

/**
 * Interface to simplify checking condition
 * 
 * @author Omid Karimi (273816)
 * @author Gianni Lodetti (275085)
 */
public interface Preconditions {

    /**
     * throws exception if received parameter is false
     * 
     * @param b
     * @throws IllegalArgumentException
     */
    public static void checkArgument(boolean b) throws IllegalArgumentException {
        if (!b) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * return value if it's smaller or equals to 8 bits else throws exception
     * 
     * @param v, for value
     * @return value
     * @throws IllegalArgumentException
     */
    public static int checkBits8(int v) throws IllegalArgumentException {
        checkArgument(v <= 0xFF && v >= 0);
        return v;
    }

    /**
     * return value if it's smaller or equals to 16 bits else throws exception
     * 
     * @param v, for value
     * @return value
     * @throws IllegalArgumentException
     */
    public static int checkBits16(int v) throws IllegalArgumentException {
        checkArgument(v <= 0xFFFF && v >= 0);
        return v;
    }
}
