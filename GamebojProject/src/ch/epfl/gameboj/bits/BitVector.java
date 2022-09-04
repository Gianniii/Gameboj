package ch.epfl.gameboj.bits;

import static ch.epfl.gameboj.Preconditions.checkArgument;
import static ch.epfl.gameboj.Preconditions.checkBits8;

import java.util.Arrays;
import java.util.Objects;


/**
 * Class BitVector, represents a vector of bits
 * 
 * @author Omid Karimi (273816)
 * @author Gianni Lodetti (275085)
 */
public final class BitVector {
    private final int[] vectorValues;

    // the two type of extraction
    private enum ExtractType {
        ZERO_EXT, WRAPPED;
    }

    /**
     * Construct a bit vector with given values
     * 
     * @param vectorValues, array of values
     */
    public BitVector(int[] vectorValues) {
        this.vectorValues = vectorValues; //
    }

    /**
     * Construct a bit vector of given size 
     * with all bits having the given value
     * 
     * @param size, size of vector
     * @param value, value assigned to all bits
     */
    public BitVector(int size, boolean value) {
        this(arrayBuilder(size, value));
    }

    /**
     * Construct a bit vector of given size 
     *  with all bits set at 0
     * 
     * @param size, size of vector
     */
    public BitVector(int size) {
        this(arrayBuilder(size, false));
    }

    // used by constructors to build an array filled with a given value
    private static int[] arrayBuilder(int size, boolean value) {
        checkArgument(size > 0 && size % Integer.SIZE == 0);
        int[] vectorValues = new int[size / Integer.SIZE];
        Arrays.fill(vectorValues, value ? 0xFFFFFFFF : 0);
        return vectorValues;
    }

    /**
     * Getter for the size of the bitVector
     * 
     * @return size of the bitVector
     */
    public int size() {
        return vectorValues.length * Integer.SIZE;
    }

    /**
     * Returns the value of the bit at given index
     * 
     * @param index
     * @return boolean value of the bit
     */
    public boolean testBit(int index) {
        if (index < 0 || index >= size()) {
            throw new IndexOutOfBoundsException();
        }
        return Bits.test(vectorValues[indexInArray(index)], indexInInt(index));
    }

    /**
     * Computes new bitVector with all bits inverted
     * 
     * @return bitVector with all bits inverted
     */
    public BitVector not() {
        int[] newVectValues = new int[vectorValues.length];
        for (int i = 0; i < vectorValues.length; i++) {
            newVectValues[i] = ~vectorValues[i];
        }
        return new BitVector(newVectValues);
    }

    /**
     * Computes new bitVector of the conjunction of this
     *  with given bitVector
     * 
     * @param that, given bitVector
     * @return the conjunction of this with that
     */
    public BitVector and(BitVector that) {
        checkArgument(that.size() == this.size());
        int[] newVectValues = new int[vectorValues.length];
        for (int i = 0; i < vectorValues.length; i++) {
            newVectValues[i] = that.vectorValues[i] & this.vectorValues[i];
        }
        return new BitVector(newVectValues);
    }

    /**
     * Computes new bitVector of the disjunction of this
     *  with given bitVector
     * 
     * @param that, given bitVector
     * @return the disjunction of this with that
     */
    public BitVector or(BitVector that) {
        checkArgument(that.size() == this.size());
        int[] newVectValues = new int[vectorValues.length];
        for (int i = 0; i < vectorValues.length; i++) {
            newVectValues[i] = that.vectorValues[i] | this.vectorValues[i];
        }
        return new BitVector(newVectValues);
    }

    /**
     * Extract bitVector from the infinite zero extension of this
     *  of given size, starting from given index (inclusive)
     * 
     * @param index
     * @param size
     * @return bitVector, computed as said above
     */
    public BitVector extractZeroExtended(int index, int size) {
        checkArgument(size > 0 && size % Integer.SIZE == 0);
        return extract(index, size, ExtractType.ZERO_EXT);
    }

    /**
     * Extract bitVector from the infinite wrapping extension of this
     *  of given size, starting from given index (inclusive)
     * 
     * @param index
     * @param size
     * @return bitVector, computed as said above
     */
    public BitVector extractWrapped(int index, int size) {
        checkArgument(size >= 0 && size % Integer.SIZE == 0);
        return extract(index, size, ExtractType.WRAPPED);
    }

    /**
     * Compute new shifted bitVector of this over a given distance
     * 
     * @param distance of shift
     * @return new shifted bitVector
     */
    public BitVector shift(int distance) {
        return extractZeroExtended(-distance, size());
    }

    // used by extraction method to compute wanted bitVector
    private BitVector extract(int index, int size, ExtractType type) {
        int[] values = new int[size / Integer.SIZE];
        for (int i = 0; i < values.length; ++i) {
            values[values.length - 1 - i] = extractElement(index + i * 32, type);
        }
        return new BitVector(values);
    }

    // extracts 32 bits from index using given extract type
    private int extractElement(int index, ExtractType type) {
        int element = 0;
        if (type == ExtractType.ZERO_EXT) {
            // if entire element to extract is contained in array
            if (index < size() - Integer.SIZE && index >= 0) {
                int lsb = Bits.extract(vectorValues[indexInArray(index)],
                        indexInInt(index), Integer.SIZE - indexInInt(index));
                int msb = Bits.extract(vectorValues[indexInArray(index) - 1], 0,
                        indexInInt(index));
                return (msb << Integer.SIZE - indexInInt(index)) | lsb; 
            }
            // if index belongs to [-31, 0[
            if (index < 0 && Integer.SIZE + index >= 0) {
                element = Bits.extract(vectorValues[vectorValues.length - 1], 0,
                        Integer.SIZE + index) << -index;
            }
            // if index belongs to [size-32, size[
            if (index >= size() - Integer.SIZE && index < size()) {
                element = Bits.extract(vectorValues[0],
                        indexInInt(index), Integer.SIZE - indexInInt(index));
            }
        } else {
            // compute and extracts indexes of msb and lsb in original bitVector to simulate an 
            // infinitely wrapped bitVector
            int lsbIndex = indexInArray(Math.floorMod(index, size()));
            int msbIndex = indexInArray(Math.floorMod(index + Integer.SIZE, size()));

            int lsb = Bits.extract(vectorValues[lsbIndex], indexInInt(index), Integer.SIZE - indexInInt(index));
            int msb = Bits.extract(vectorValues[msbIndex], 0, indexInInt(index));

            return (msb << Integer.SIZE - indexInInt(index) | lsb);
       }
        return element;
    }
    
    private int indexInArray(int index) {
        return vectorValues.length - 1 - Math.floorDiv(index, Integer.SIZE);
    }

    private int indexInInt(int index) {
        return Math.floorMod(index, Integer.SIZE);
    }

    @Override
    public boolean equals(Object that) {
        return (that instanceof BitVector) && 
                Arrays.equals(((BitVector)that).vectorValues, this.vectorValues);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(vectorValues);
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        for (int i = size() - 1; i >= 0; --i)
            b.append(testBit(i) ? 1 : 0);
        return b.toString();
    }

    /**
     * Builder of a BitVector
     * 
     * @author Omid Karimi (273816)
     * @author Gianni Lodetti (275085)
     */
    public final static class Builder {
        private int[] values;

        /**
         * Construct a bitVector builder of given size
         * 
         * @param size
         */
        public Builder(int size) {
            checkArgument(size >= 0 && size % Integer.SIZE == 0);
            values = new int[size/Integer.SIZE];
        }

        /**
         * Set given byte at given index in the builder
         * 
         * @param index
         * @param b, byte value
         * @return this
         * @throws IllegalStateException if the builder as already been built
         */
        public BitVector.Builder setByte(int index, int b) {
            b = checkBits8(b);
            int indexInInt = (index * Byte.SIZE) % Integer.SIZE;
            int indexInArray = values.length - 1 - index/4;
            if (values == null) 
                throw new IllegalStateException();
            Objects.checkIndex(index, values.length * Integer.SIZE);
            values[indexInArray] |= b << indexInInt;
            return this;
        }

        /**
         * Builds new bitVector with the builder values
         * 
         * @return new bitVector
         * @throws IllegalStateException if the builder as already been built
         */
        public BitVector build() {
            if (values == null) 
                throw new IllegalStateException();
            BitVector v = new BitVector(values);
            values = null;
            return v;
        }

    }

}
