package ch.epfl.gameboj.component.memory;

import static ch.epfl.gameboj.Preconditions.checkArgument;
import static ch.epfl.gameboj.Preconditions.checkBits8;

/**
 * Class RAM (random access memory)
 * 
 * @author Omid Karimi (273816)
 * @author Gianni Lodetti (275085)
 */
public final class Ram {

    private final byte[] data;

    /**
     * Construct a an array of given representing a memory
     * 
     * @param size
     * @throws IllegalArgumentException
     *             if size is negative
     */
    public Ram(int size) throws IllegalArgumentException {
        checkArgument(size >= 0);
        data = new byte[size];
    }

    /**
     * 
     * @return size of the array
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
        if (index >= 0 && index < size()) {
            return Byte.toUnsignedInt(data[index]);
        } else {
            throw new IndexOutOfBoundsException();
        }
    }

    /**
     * write given data at the given index in the array
     * 
     * @param index
     * @param value
     * @throws IndexOutOfBoundsException
     *             if invalid index
     * @throws IllegalArgumentException
     */
    public void write(int index, int value) throws IndexOutOfBoundsException, IllegalArgumentException {
        if (index < 0 && index >= size()) {
            throw new IndexOutOfBoundsException();
        }
        data[index] = (byte)checkBits8(value);
    }
}
