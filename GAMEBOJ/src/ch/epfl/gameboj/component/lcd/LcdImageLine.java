package ch.epfl.gameboj.component.lcd;

import static ch.epfl.gameboj.Preconditions.checkArgument;
import static ch.epfl.gameboj.Preconditions.checkBits8;

import java.util.Objects;

import ch.epfl.gameboj.bits.BitVector;
import ch.epfl.gameboj.bits.Bits;

/**
 * Class LcdImageLine, represents a line of a gameboy's image
 * 
 * @author Omid Karimi (273816)
 * @author Gianni Lodetti (275085)
 */
public final class LcdImageLine {
    private final BitVector msb, lsb, opacity;
    
    // pixels colors
    private enum Color {
        WHITE, GREY, DARK_GREY, BLACK;
    }

    /**
     * Construct a LcdImageLine from given msb, lsb and opacity bitVectors
     * 
     * @param msb, bitVector of msb
     * @param lsb, bitVector of lsb
     * @param opacity, opacity's bitVector
     */
    public LcdImageLine(BitVector msb, BitVector lsb, BitVector opacity) {
        checkArgument(msb.size() == lsb.size() && msb.size() == opacity.size());
        this.msb = msb;
        this.lsb = lsb;
        this.opacity = opacity;
    }
    
    /**
     * Get the size of the LcdImageLine
     * 
     * @return size
     */
    public int size() {
        return msb.size();
    }
    
    /**
     * Get the msb bitVector
     * 
     * @return msb bitVector
     */
    public BitVector msb() {
        return msb;
    }
    
    /**
     * Get the lsb bitVector
     * 
     * @return lsb bitVector
     */
    public BitVector lsb() {
        return lsb;
    }
    
    /**
     * Get the opaity bitVector
     * 
     * @return opacity bitVector
     */
    public BitVector opacity() {
        return opacity;
    }
    
    /**
     * Shifts line over given distance
     * 
     * @param distance
     * @return shifted new LcdImageLine
     */
    public LcdImageLine shift(int distance) {
        return new LcdImageLine(msb.shift(distance), lsb.shift(distance),
                opacity.shift(distance));
    }
    
    /**
     * Computes new LcdImageLine using infinite wrapped extension,
     *  starting at given index and of given size
     * 
     * @param index
     * @param size
     * @return new LcdImageLine
     */
    public LcdImageLine extractWrapped(int index, int size) {
        BitVector newMsb = msb.extractWrapped(index, size),
                newLsb = lsb.extractWrapped(index, size),
                newOpacity = opacity.extractWrapped(index, size);
        return new LcdImageLine(newMsb, newLsb, newOpacity);
    }
    
    /**
     * Computes new LcdImageLine by changing the colors of this with given palette
     * 
     * @param palette, color palette
     * @return new LcdImageLine with new colors
     */
    public LcdImageLine mapColors(int palette) {
        palette = checkBits8(palette);
        if (palette == 0b11100100)
            return this;
        // BitVector with 1 at the indexes of the pixels that need to change color
        BitVector indexes; 
        LcdImageLine newLine = new LcdImageLine(msb, lsb, opacity);
        if (extractColor(palette, 6) != Color.BLACK) {
            indexes = lsb.and(msb);
            newLine = changeColor(indexes, extractColor(palette, 6), newLine);
        }
        if (extractColor(palette, 4) != Color.DARK_GREY) {
            indexes = msb.and(lsb.not());
            newLine = changeColor(indexes, extractColor(palette, 4), newLine);
        }
        if (extractColor(palette, 2) != Color.GREY) {
            indexes = lsb.and(msb.not());
            newLine = changeColor(indexes, extractColor(palette, 2), newLine);
        }
        if (extractColor(palette, 0) != Color.WHITE) {
            indexes = lsb.or(msb).not();
            newLine = changeColor(indexes, extractColor(palette, 0), newLine);
        }
        return newLine;
    }
    
    // extract color in palette at given index
    private Color extractColor(int palette, int index) {
        return Color.values()[Bits.extract(palette, index, 2)];
    }
    
    // computes new line with new given colors at given indexes of the given line
    private LcdImageLine changeColor(BitVector indexes, Color color, LcdImageLine line) {
        BitVector newMsb = line.msb, newLsb = line.lsb;
        if (color == Color.WHITE) {
            newLsb = newLsb.and(indexes.not());
            newMsb = newMsb.and(indexes.not());
        }
        if (color == Color.GREY) {
            newLsb = newLsb.or(indexes);
            newMsb = newMsb.and(indexes.not());
        }
        if (color == Color.DARK_GREY) {
            newLsb = newLsb.and(indexes.not());
            newMsb = newMsb.or(indexes);
        }
        if (color == Color.BLACK) {
            newLsb = newLsb.or(indexes);
            newMsb = newMsb.or(indexes);
        }
        return new LcdImageLine(newMsb, newLsb, opacity);
    }
    
    /**
     * Composes new LcdImageLine of this line with a other one placed above it
     *  using opacity of the other line
     * 
     * @param other LcdImageLine 
     * @return new line as described above
     */
    public LcdImageLine below(LcdImageLine other) {
        return below(other, other.opacity);
    }
    
    /**
     * Composes new LcdImageLine of this line with a other one placed above it
     *  using given opacity
     *  
     * @param other LcdImageLine
     * @param opacity bitVector
     * @return new LcdImageLine 
     */
    public LcdImageLine below(LcdImageLine other, BitVector opacity) {
        checkArgument(other.size() == this.size());
        BitVector newMsb = other.msb.and(opacity).or(this.msb.and(opacity.not()));
        BitVector newLsb = other.lsb.and(opacity).or(this.lsb.and(opacity.not()));
        BitVector newOpacity = opacity.or(this.opacity);
        return new LcdImageLine(newMsb, newLsb, newOpacity);
    }
    
    /**
     * Joins this with given LcdImageLine from given index
     * 
     * @param other LcdImageLine
     * @param index
     * @return new LcdImageLine
     */
    public LcdImageLine join(LcdImageLine other, int index) {
        checkArgument(other.size() == this.size());
        LcdImageLine newLine;
        BitVector mask = new BitVector(size(), true).shift(index);
        newLine = below(other, mask);
        BitVector newOpacity = other.opacity.shift(index).or(this.opacity);
        return new LcdImageLine(newLine.msb, newLine.lsb, newOpacity);
    }
    
    @Override
    public boolean equals(Object that) {
        return ((that instanceof LcdImageLine) &&
                this.msb.equals(((LcdImageLine)that).msb) && 
                this.lsb.equals(((LcdImageLine)that).lsb) &&
                this.opacity.equals(((LcdImageLine)that).opacity));
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(msb, lsb, opacity);
    }
    
    /**
     * Builder of a line of an image
     * 
     * @author Omid Karimi (273816)
     * @author Gianni Lodetti (275085)
     */
    public final static class Builder {
        private BitVector.Builder msb, lsb;
        
        /**
         * Construct a builder of given width
         * 
         * @param width
         */
        public Builder(int width) {
            msb = new BitVector.Builder(width);
            lsb = new BitVector.Builder(width);
        }
        
        /**
         * Sets byte at given index with given msb and lsb bytes
         * 
         * @param index
         * @param msbByte
         * @param lsbByte
         * @return this
         * @throws IllegalStateException if the builder has already built
         */
        public LcdImageLine.Builder setBytes(int index, int msbByte, int lsbByte) {
            if (msb == null || lsb == null) 
                throw new IllegalStateException();
            msb = msb.setByte(index, checkBits8(msbByte));
            lsb = lsb.setByte(index, checkBits8(lsbByte));
            return this;
        }
        
        /**
         * Builds new LcdImageLine from msb and lsb bitVectors
         * 
         * @return new LcdImageLine
         * @throws IllegalStateException if the builder has already built
         */
        public LcdImageLine build() {
            if (msb == null || lsb == null) 
                throw new IllegalStateException();
            BitVector msbVect = msb.build();
            BitVector lsbVect = lsb.build();
            LcdImageLine line = new LcdImageLine(msbVect, lsbVect, msbVect.or(lsbVect));
            msb = null; lsb = null;
            return line;
        }
        
    }
    
}
