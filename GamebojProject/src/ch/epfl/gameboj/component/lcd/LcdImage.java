package ch.epfl.gameboj.component.lcd;

import static ch.epfl.gameboj.Preconditions.checkArgument;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import ch.epfl.gameboj.bits.BitVector;

/**
 * Class LcdImage, represents an image of the GameBoy
 * 
 * @author Omid Karimi (273816)
 * @author Gianni Lodetti (275085)
 */
public final class LcdImage {
    private final int width, height;
    private final List<LcdImageLine> lines;
    
    /**
     * Construct an LcdImage of given width, height and lines
     * 
     * @param width
     * @param height
     * @param lines, list of lines
     */
    public LcdImage(int width, int height, List<LcdImageLine> lines) {
        checkArgument(lines.size() == height);
        this.width = width;
        this.height = height;
        this.lines = Collections.unmodifiableList(new ArrayList<>(lines));
    }
    
    /**
     * Getter for the width
     * 
     * @return width
     */
    public int width() {
        return width;
    }
    
    /**
     * Getter for the height
     * 
     * @return height
     */
    public int height() {
        return height;
    }
    
    /**
     * Returns value of pixel in position (x, y)
     * 
     * @param x coordinate
     * @param y coordinate
     * @return value of the pixel
     */
    public int get(int x, int y) {
        checkArgument(x < width && y < height);
        LcdImageLine line = lines.get(y);
        return ((line.msb().testBit(x) ? 1 : 0) << 1) | 
                (line.lsb().testBit(x) ? 1 : 0);
    }
    
    @Override
    public boolean equals(Object that) {
        return ((that instanceof LcdImage) &&
                this.width == ((LcdImage)that).width && 
                this.height == ((LcdImage)that).height &&
                this.lines.equals(((LcdImage)that).lines));
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(lines, width, height);
    }
    
    /**
     * Builder of a Gameboy's image
     * 
     * @author Omid Karimi (273816)
     * @author Gianni Lodetti (275085)
     */
    public final static class Builder {
        private int width, height;
        private List<LcdImageLine> lines;
        
        /**
         * Construct a LcdImage builder of given width and height
         * 
         * @param width
         * @param height
         */
        public Builder(int width, int height) {
            this.width = width;
            this.height = height;
            lines = new ArrayList<>(Collections.nCopies(height, new LcdImageLine(
                    new BitVector(width), new BitVector(width), new BitVector(width))));
        }
        
        /**
         * Sets given line at given index 
         * 
         * @param index
         * @param line
         * @return this
         * @throws IllegalStateException if the builder has already built
         */
        public LcdImage.Builder setLine(int index, LcdImageLine line) {
            if (lines == null) 
                throw new IllegalStateException();
            Objects.checkIndex(index, height);
            lines.set(index, line);
            return this;
        }
        
        /**
         * Builds new LcdImageLine from msb and lsb bitVectors
         * 
         * @return new LcdImage
         * @throws IllegalStateException if the builder has already built
         */
        public LcdImage build() {
            if (lines == null) 
                throw new IllegalStateException();
            LcdImage image = new LcdImage(width, height, lines);
            lines = null;
            return image;
        }
        
    }
    
}
