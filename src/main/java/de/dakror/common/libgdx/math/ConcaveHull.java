package de.dakror.common.libgdx.math;

import de.dakror.common.libgdx.Pair;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;

public class ConcaveHull {
   private Double euclideanDistance(Point a, Point b) {
      return Math.sqrt(Math.pow(a.getX() - b.getX(), 2.0) + Math.pow(a.getY() - b.getY(), 2.0));
   }

   private ArrayList<Point> kNearestNeighbors(ArrayList<Point> l, Point q, Integer k) {
      ArrayList<Pair<Double, Point>> nearestList = new ArrayList<>();

      for (Point o : l) {
         nearestList.add(new Pair<>(this.euclideanDistance(q, o), o));
      }

      Collections.sort(nearestList, new Comparator<Pair<Double, Point>>() {
         public int compare(Pair<Double, Point> o1, Pair<Double, Point> o2) {
            return o1.getKey().compareTo(o2.getKey());
         }
      });
      ArrayList<Point> result = new ArrayList<>();

      for (int i = 0; i < Math.min(k, nearestList.size()); i++) {
         result.add(nearestList.get(i).getVal());
      }

      return result;
   }

   private Point findMinYPoint(ArrayList<Point> l) {
      Collections.sort(l, new Comparator<Point>() {
         public int compare(Point o1, Point o2) {
            return o1.getY().compareTo(o2.getY());
         }
      });
      return l.get(0);
   }

   private Double calculateAngle(Point o1, Point o2) {
      return Math.atan2(o2.getY() - o1.getY(), o2.getX() - o1.getX());
   }

   private Double angleDifference(Double a1, Double a2) {
      if (a1 > 0.0 && a2 >= 0.0 && a1 > a2) {
         return Math.abs(a1 - a2);
      } else if (a1 >= 0.0 && a2 > 0.0 && a1 < a2) {
         return (Math.PI * 2) + a1 - a2;
      } else if (a1 < 0.0 && a2 <= 0.0 && a1 < a2) {
         return (Math.PI * 2) + a1 + Math.abs(a2);
      } else if (a1 <= 0.0 && a2 < 0.0 && a1 > a2) {
         return Math.abs(a1 - a2);
      } else if (a1 <= 0.0 && 0.0 < a2) {
         return (Math.PI * 2) + a1 - a2;
      } else {
         return a1 >= 0.0 && 0.0 >= a2 ? a1 + Math.abs(a2) : 0.0;
      }
   }

   private ArrayList<Point> sortByAngle(ArrayList<Point> l, final Point q, final Double a) {
      Collections.sort(l, new Comparator<Point>() {
         public int compare(Point o1, Point o2) {
            Double a1 = ConcaveHull.this.angleDifference(a, ConcaveHull.this.calculateAngle(q, o1));
            Double a2 = ConcaveHull.this.angleDifference(a, ConcaveHull.this.calculateAngle(q, o2));
            return a2.compareTo(a1);
         }
      });
      return l;
   }

   private Boolean intersect(Point l1p1, Point l1p2, Point l2p1, Point l2p2) {
      Double a1 = l1p2.getY() - l1p1.getY();
      Double b1 = l1p1.getX() - l1p2.getX();
      Double c1 = a1 * l1p1.getX() + b1 * l1p1.getY();
      Double a2 = l2p2.getY() - l2p1.getY();
      Double b2 = l2p1.getX() - l2p2.getX();
      Double c2 = a2 * l2p1.getX() + b2 * l2p1.getY();
      Double tmp = a1 * b2 - a2 * b1;
      Double pX = (c1 * b2 - c2 * b1) / tmp;
      if ((!(pX > l1p1.getX()) || !(pX > l1p2.getX()))
         && (!(pX > l2p1.getX()) || !(pX > l2p2.getX()))
         && (!(pX < l1p1.getX()) || !(pX < l1p2.getX()))
         && (!(pX < l2p1.getX()) || !(pX < l2p2.getX()))) {
         Double pY = (a1 * c2 - a2 * c1) / tmp;
         return (!(pY > l1p1.getY()) || !(pY > l1p2.getY()))
               && (!(pY > l2p1.getY()) || !(pY > l2p2.getY()))
               && (!(pY < l1p1.getY()) || !(pY < l1p2.getY()))
               && (!(pY < l2p1.getY()) || !(pY < l2p2.getY()))
            ? true
            : false;
      } else {
         return false;
      }
   }

   private boolean pointInPolygon(Point p, ArrayList<Point> pp) {
      boolean result = false;
      int i = 0;

      for (int j = pp.size() - 1; i < pp.size(); j = i++) {
         if (pp.get(i).getY() > p.getY() != pp.get(j).getY() > p.getY()
            && p.getX() < (pp.get(j).getX() - pp.get(i).getX()) * (p.getY() - pp.get(i).getY()) / (pp.get(j).getY() - pp.get(i).getY()) + pp.get(i).getX()) {
            result = !result;
         }
      }

      return result;
   }

   public ArrayList<Point> calculateConcaveHull(ArrayList<Point> pointArrayList, Integer k) {
      ArrayList<Point> concaveHull = new ArrayList<>();
      HashSet<Point> set = new HashSet<>(pointArrayList);
      ArrayList<Point> pointArraySet = new ArrayList<>(set);
      Integer kk = Math.max(k, 3);
      if (pointArraySet.size() < 3) {
         return pointArraySet;
      } else {
         kk = Math.min(kk, pointArraySet.size() - 1);
         Point firstPoint = this.findMinYPoint(pointArraySet);
         concaveHull.add(firstPoint);
         Point currentPoint = firstPoint;
         pointArraySet.remove(firstPoint);
         Double previousAngle = 0.0;

         for (Integer step = 2; (currentPoint != firstPoint || step == 2) && pointArraySet.size() > 0; step = step + 1) {
            if (step == 5) {
               pointArraySet.add(firstPoint);
            }

            ArrayList<Point> kNearestPoints = this.kNearestNeighbors(pointArraySet, currentPoint, kk);
            ArrayList<Point> clockwisePoints = this.sortByAngle(kNearestPoints, currentPoint, previousAngle);
            Boolean its = true;
            int i = -1;

            label66:
            while (true) {
               if (its && i < clockwisePoints.size() - 1) {
                  i++;
                  int lastPoint = 0;
                  if (clockwisePoints.get(i) == firstPoint) {
                     lastPoint = 1;
                  }

                  int j = 2;
                  its = false;

                  while (true) {
                     if (its || j >= concaveHull.size() - lastPoint) {
                        continue label66;
                     }

                     its = this.intersect(concaveHull.get(step - 2), clockwisePoints.get(i), concaveHull.get(step - 2 - j), concaveHull.get(step - 1 - j));
                     j++;
                  }
               }

               if (its) {
                  return this.calculateConcaveHull(pointArrayList, k + 1);
               }

               currentPoint = clockwisePoints.get(i);
               concaveHull.add(currentPoint);
               pointArraySet.remove(currentPoint);
               previousAngle = this.calculateAngle(concaveHull.get(step - 1), concaveHull.get(step - 2));
               break;
            }
         }

         Boolean insideCheck = true;

         for (int i = pointArraySet.size() - 1; insideCheck && i > 0; i--) {
            insideCheck = this.pointInPolygon(pointArraySet.get(i), concaveHull);
         }

         return !insideCheck ? this.calculateConcaveHull(pointArrayList, k + 1) : concaveHull;
      }
   }
}
