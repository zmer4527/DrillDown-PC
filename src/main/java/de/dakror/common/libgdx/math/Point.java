package de.dakror.common.libgdx.math;

public class Point implements Comparable<Point> {
   private final Double x;
   private final Double y;

   public Point(double x, double y) {
      this.x = x;
      this.y = y;
   }

   public Double getX() {
      return this.x;
   }

   public Double getY() {
      return this.y;
   }

   @Override
   public String toString() {
      return "(" + this.x + " " + this.y + ")";
   }

   @Override
   public boolean equals(Object obj) {
      return obj instanceof Point ? this.x.equals(((Point)obj).getX()) && this.y.equals(((Point)obj).getY()) : false;
   }

   @Override
   public int hashCode() {
      int tmp = (int)(this.y + (this.x + 1.0) / 2.0);
      return Math.abs((int)(this.x + (double)(tmp * tmp)));
   }

   public int compareTo(Point o) {
      return this.x.equals(o.x) ? this.y.compareTo(o.y) : this.x.compareTo(o.x);
   }
}
