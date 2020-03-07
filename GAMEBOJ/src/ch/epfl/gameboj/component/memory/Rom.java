package ch.epfl.gameboj.component.memory;

import java.util.Arrays;
import java.util.Objects;

/**
 * Class ROM (read-only memory)
 * 
 * @author Omid Karimi (273816)
 * @author Gianni Lodetti (275085)
 */
public final class Rom {

    private final byte[] data;

    /**
     * Construct an array of data with given data (memory)
     * 
     * @param data
     * @throws NullPointerException
     *             if received data is null
     */
    public Rom(byte[] data) throws NullPointerException {
        this.data = Arrays.copyOf(Objects.requireNonNull(data), data.length);
    }

    /**
     * 
     * @return size of the array of data
     */
    public int size() {
        return data.length;
    }

    /**
     * return the byte, as an unsigned integer, at given index in the data array
     * 
     * @param index
     * @return a value (data)
     * @throws IndexOutOfBoundsException
     *             if index is invalid
     */
    public int read(int index) throws IndexOutOfBoundsException {
        if (index >= 0 && index < data.length) {
            return Byte.toUnsignedInt(data[index]);
        } else {
            throw new IndexOutOfBoundsException();
        }
    }
}
