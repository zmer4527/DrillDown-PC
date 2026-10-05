package de.dakror.quarry.desktop;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;

public class ItemEdgeDetector {
   public static void main(String[] args) throws IOException {
      for (File f : new File("..\\Development\\item_icons").listFiles()) {
         if (!f.getName().contains("_nil.png")) {
            ArrayList<Integer> edgePixels = new ArrayList<>();
            BufferedImage bi = new BufferedImage(24, 24, 2);
            BufferedImage src = ImageIO.read(f);

            for (int i = 0; i < 24; i++) {
               for (int j = 0; j < 24; j++) {
                  int rgb = src.getRGB(i, j);
                  if (rgb >>> 24 > 0) {
                     boolean anyTransparent = i == 0 || j == 0 || i == 23 || j == 23;
                     if (!anyTransparent) {
                        label130:
                        for (int k = -1; k < 2; k++) {
                           for (int l = -1; l < 2; l++) {
                              if ((k == 0 || l == 0) && k != l) {
                                 int x = i + k;
                                 int y = j + l;
                                 if (x >= 0 && x <= 23 && y >= 0 && y <= 23) {
                                    int px = src.getRGB(x, y);
                                    if (px >>> 24 == 0) {
                                       anyTransparent = true;
                                       break label130;
                                    }
                                 }
                              }
                           }
                        }
                     }

                     if (!anyTransparent) {
                        bi.setRGB(i, j, rgb);
                     } else {
                        edgePixels.add(j * 24 + i);
                     }
                  }
               }
            }

            for (int i : edgePixels) {
               int x = i % 24;
               int y = i / 24;
               int r = 0;
               int g = 0;
               int b = 0;
               int c = 0;

               for (int k = -1; k < 2; k++) {
                  for (int lx = -1; lx < 2; lx++) {
                     if (k != lx || k != 0) {
                        int x1 = x + k;
                        int y1 = y + lx;
                        if (x1 >= 0 && x1 <= 23 && y1 >= 0 && y1 <= 23 && !edgePixels.contains(y1 * 24 + x1)) {
                           int px = src.getRGB(x1, y1);
                           if (px >>> 24 > 0) {
                              r += px >> 16 & 0xFF;
                              g += px >> 8 & 0xFF;
                              b += px & 0xFF;
                              c++;
                           }
                        }
                     }
                  }
               }

               if (c > 0) {
                  r /= c * 2;
                  g /= c * 2;
                  b /= c * 2;
                  bi.setRGB(x, y, 0xFF000000 | r << 16 | g << 8 | b);
               } else {
                  bi.setRGB(x, y, -65536);
               }
            }

            ImageIO.write(bi, "PNG", f);
            ImageIO.write(bi, "PNG", new File(f.getParentFile().getParentFile() + "/Textures", f.getName()));
         }
      }
   }
}
