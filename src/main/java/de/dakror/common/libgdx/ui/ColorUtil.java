package de.dakror.common.libgdx.ui;

import com.badlogic.gdx.graphics.Color;

public class ColorUtil {
   public static Color HSLtoRGB(float h, float s, float l, float alpha) {
      if (s < 0.0F || s > 100.0F) {
         String message = "Color parameter outside of expected range - Saturation";
         throw new IllegalArgumentException(message);
      } else if (l < 0.0F || l > 100.0F) {
         String message = "Color parameter outside of expected range - Luminance";
         throw new IllegalArgumentException(message);
      } else if (!(alpha < 0.0F) && !(alpha > 1.0F)) {
         h %= 360.0F;
         h /= 360.0F;
         s /= 100.0F;
         l /= 100.0F;
         float q = 0.0F;
         if ((double)l < 0.5) {
            q = l * (1.0F + s);
         } else {
            q = l + s - s * l;
         }

         float p = 2.0F * l - q;
         float r = Math.max(0.0F, HueToRGB(p, q, h + 0.33333334F));
         float g = Math.max(0.0F, HueToRGB(p, q, h));
         float b = Math.max(0.0F, HueToRGB(p, q, h - 0.33333334F));
         r = Math.min(r, 1.0F);
         g = Math.min(g, 1.0F);
         b = Math.min(b, 1.0F);
         return new Color(r, g, b, alpha);
      } else {
         String message = "Color parameter outside of expected range - Alpha";
         throw new IllegalArgumentException(message);
      }
   }

   private static float HueToRGB(float p, float q, float h) {
      if (h < 0.0F) {
         h++;
      }

      if (h > 1.0F) {
         h--;
      }

      if (6.0F * h < 1.0F) {
         return p + (q - p) * 6.0F * h;
      } else if (2.0F * h < 1.0F) {
         return q;
      } else {
         return 3.0F * h < 2.0F ? p + (q - p) * 6.0F * (0.6666667F - h) : p;
      }
   }

   public static Color HSVtoRGB(float h, float s, float v) {
      h = (float)Math.max(0.0, Math.min(360.0, (double)h));
      s = (float)Math.max(0.0, Math.min(100.0, (double)s));
      v = (float)Math.max(0.0, Math.min(100.0, (double)v));
      s /= 100.0F;
      v /= 100.0F;
      h /= 60.0F;
      int i = (int)Math.floor((double)h);
      float f = h - (float)i;
      float p = v * (1.0F - s);
      float q = v * (1.0F - s * f);
      float t = v * (1.0F - s * (1.0F - f));
      int r;
      int g;
      int b;
      switch (i) {
         case 0:
            r = Math.round(255.0F * v);
            g = Math.round(255.0F * t);
            b = Math.round(255.0F * p);
            break;
         case 1:
            r = Math.round(255.0F * q);
            g = Math.round(255.0F * v);
            b = Math.round(255.0F * p);
            break;
         case 2:
            r = Math.round(255.0F * p);
            g = Math.round(255.0F * v);
            b = Math.round(255.0F * t);
            break;
         case 3:
            r = Math.round(255.0F * p);
            g = Math.round(255.0F * q);
            b = Math.round(255.0F * v);
            break;
         case 4:
            r = Math.round(255.0F * t);
            g = Math.round(255.0F * p);
            b = Math.round(255.0F * v);
            break;
         default:
            r = Math.round(255.0F * v);
            g = Math.round(255.0F * p);
            b = Math.round(255.0F * q);
      }

      return new Color((float)r / 255.0F, (float)g / 255.0F, (float)b / 255.0F, 1.0F);
   }
}
