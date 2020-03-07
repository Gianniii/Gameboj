package ch.epfl.gameboj.gui;

import ch.epfl.gameboj.component.lcd.LcdController;
import ch.epfl.gameboj.component.lcd.LcdImage;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

/**
 * Class ImageConverter, convert LcdImage to a javaFx Image
 * 
 * @author Omid Karimi (273816)
 * @author Gianni Lodetti (275085)
 */
public class ImageConverter {
    private static boolean originalColor = false;
    
    private static final int[] COLOR_MAP = new int[] { 0xFF_FF_FF_FF, 0xFF_D3_D3_D3,
            0xFF_A9_A9_A9, 0xFF_00_00_00 };
    
    private static final int[] COLOR_MAP_GREEN = new int[] { 0xFF_9B_BC_0F, 0xFF_8B_AC_0F,
            0xFF_30_62_30, 0xFF_0F_38_0F };
    
    /**
     * Converts given LcdImage to a javaFx Image 
     * 
     * @param LcdImage
     * @return given image converted to javaFx image
     */
    public static javafx.scene.image.Image convert(LcdImage image) {
        //SHOULD PROBABLY USE THE IMAGE SIZE INSTEAD OF CONSTANT BIG MISTAKE HERE 
        WritableImage im = new WritableImage(LcdController.LCD_WIDTH, LcdController.LCD_HEIGHT);
        PixelWriter pw = im.getPixelWriter();
        for (int y = 0 ; y < im.getHeight() ; ++y) {
            for (int x = 0 ; x < im.getWidth() ; ++x) {
                if (originalColor) {
                    pw.setArgb(x, y, COLOR_MAP_GREEN[image.get(x, y)]);
                } else {
                    pw.setArgb(x, y, COLOR_MAP[image.get(x, y)]);
                }
            }
        }
        return im;
    }

    /**
     * Switches color by modifying boolean originalColor
     */
    public static void changeColor() {
        originalColor = ! originalColor;
    }
    
}
