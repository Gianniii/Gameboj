package ch.epfl.gameboj.component.lcd;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import ch.epfl.gameboj.bits.BitVector;

class LcdImageTest {

    @Test
    void testEquals() {
        BitVector msb = new BitVector(64, true);
        BitVector lsb = new BitVector(64, true);
        BitVector opacity = new BitVector(64, true);
        LcdImageLine test = new LcdImageLine(msb, lsb, opacity);
        LcdImageLine test2 = new LcdImageLine(msb.not(), lsb.not(), opacity.not());
        LcdImageLine testFake = new LcdImageLine(msb, lsb, opacity);

        List<LcdImageLine> lines = List.of(test, test2);
        List<LcdImageLine> lines2 = List.of(test, testFake);

        ArrayList<LcdImageLine> oui = new ArrayList<>(2);
        oui.add(test); oui.add(test2);
        LcdImage image = new LcdImage(64, 2, lines);
        LcdImage image2 = new LcdImage(64, 2, lines2);
        LcdImage that = new LcdImage(64, 2, lines);
        
        assertEquals(oui, lines);
        assertEquals(that, image);
        assertEquals(false, image2.equals(image));
    }
    
    @Test
    void imageBuilderForEmpty() {
        LcdImage.Builder b = new LcdImage.Builder(32, 5);
        LcdImage image = b.build();
        int[][] l = new int[image.height()][image.width()];
        for(int i = 0; i < image.height(); i++) {
            for(int j = 0; j < image.width(); j++) {
                l[i][j] = image.get(j, i);
            }
        }
        for(int i = 0; i < image.height(); i++) {
            for(int j = 0; j < image.width(); j++) {
                assert(0 == l[i][j]);
            }
        }
    }

}
