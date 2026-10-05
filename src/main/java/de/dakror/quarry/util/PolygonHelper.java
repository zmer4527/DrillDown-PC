package de.dakror.quarry.util;

import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import de.dakror.common.libgdx.math.Point;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class PolygonHelper {
   public Polygon makePolygon(Iterable<Rectangle> rectangles) {
      Array<Point> points = this.calcPoints(rectangles);
      if (points.isEmpty()) {
         return null;
      } else {
         float[] vertices = new float[points.size * 2];

         for (int i = 0; i < points.size; i++) {
            vertices[2 * i] = (float)((Point)points.get(i)).getX().doubleValue();
            vertices[2 * i + 1] = (float)((Point)points.get(i)).getY().doubleValue();
         }

         System.out.println(points);
         System.out.println(Arrays.toString(vertices));
         return new Polygon(vertices);
      }
   }

   private Array<Point> calcPoints(Iterable<Rectangle> rectangles) {
      Array<Point> ret = new Array();
      FloatArray yCoords = new FloatArray();

      for (Float f : this.getAllYCoords(rectangles)) {
         yCoords.add(f);
      }

      yCoords.sort();
      yCoords.reverse();
      float previousLeftCoord = 0.0F;
      float previousRightCoord = 0.0F;

      for (int i = 0; i < yCoords.size; i++) {
         float yCoord = yCoords.get(i);
         float minimumXLeftCoord = this.minXLeftCoord(yCoord, rectangles);
         float maximumXRightCoord = this.maxXRightCoord(yCoord, rectangles);
         if (yCoord == yCoords.first()) {
            ret.add(new Point((double)minimumXLeftCoord, (double)yCoord));
            ret.add(new Point((double)maximumXRightCoord, (double)yCoord));
         } else {
            if (minimumXLeftCoord != previousLeftCoord) {
               ret.insert(0, new Point((double)previousLeftCoord, (double)yCoord));
               ret.insert(0, new Point((double)minimumXLeftCoord, (double)yCoord));
            } else {
               ret.insert(0, new Point((double)minimumXLeftCoord, (double)yCoord));
            }

            if (maximumXRightCoord != previousRightCoord) {
               ret.add(new Point((double)previousRightCoord, (double)yCoord));
               ret.add(new Point((double)maximumXRightCoord, (double)yCoord));
            } else {
               ret.add(new Point((double)maximumXRightCoord, (double)yCoord));
            }
         }

         previousLeftCoord = minimumXLeftCoord;
         previousRightCoord = maximumXRightCoord;
      }

      return ret;
   }

   private Set<Float> getAllYCoords(Iterable<Rectangle> rectangles) {
      Set<Float> allCoords = new HashSet<>();

      for (Rectangle r : rectangles) {
         allCoords.add(r.y);
         allCoords.add(r.y + r.height);
      }

      return allCoords;
   }

   private float minXLeftCoord(Float y, Iterable<Rectangle> rectangles) {
      Array<Rectangle> rects = this.rectanglesAtY(y, rectangles);
      float min = Float.MAX_VALUE;

      for (Rectangle r : rects) {
         min = Math.min(min, r.x);
      }

      return min;
   }

   private float maxXRightCoord(Float y, Iterable<Rectangle> rectangles) {
      Array<Rectangle> rects = this.rectanglesAtY(y, rectangles);
      float max = 0.0F;

      for (Rectangle r : rects) {
         max = Math.max(max, r.x + r.width);
      }

      return max;
   }

   private Array<Rectangle> rectanglesAtY(Float y, Iterable<Rectangle> rectangles) {
      Array<Rectangle> rectsAtYExcBottomLines = this.rectsAtYExcBottomLines(y, rectangles);
      return rectsAtYExcBottomLines.size > 0 ? rectsAtYExcBottomLines : this.rectsAtYIncBottomLines(y, rectangles);
   }

   private Array<Rectangle> rectsAtYExcBottomLines(Float y, Iterable<Rectangle> rectangles) {
      Array<Rectangle> rects = new Array();

      for (Rectangle r : rectangles) {
         if (r.y + r.height >= y && r.y < y) {
            rects.add(r);
         }
      }

      return rects;
   }

   private Array<Rectangle> rectsAtYIncBottomLines(Float y, Iterable<Rectangle> rectangles) {
      Array<Rectangle> rects = new Array();

      for (Rectangle r : rectangles) {
         if (r.y + r.height >= y && r.y == y) {
            rects.add(r);
         }
      }

      return rects;
   }
}
