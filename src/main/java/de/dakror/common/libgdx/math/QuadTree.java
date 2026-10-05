package de.dakror.common.libgdx.math;

import com.badlogic.gdx.utils.Array;

public class QuadTree<T extends QuadTree.Region> {
   public static final int REGION_SELF = -1;
   public static final int REGION_NW = 0;
   public static final int REGION_NE = 1;
   public static final int REGION_SW = 2;
   public static final int REGION_SE = 3;
   public static int maxNodes = 5;
   public static int maxLevel = 10;
   Array<T> nodes;
   QuadTree<T>[] regions;
   QuadTree.RectRegion zone;
   int level;
   final Array<T> temp = new Array();

   public static boolean contains(QuadTree.Region r, QuadTree.Region other) {
      return r.getWidth() > 0
         && r.getHeight() > 0
         && other.getWidth() > 0
         && other.getHeight() > 0
         && other.getX() >= r.getX()
         && other.getX() + other.getWidth() <= r.getX() + r.getWidth()
         && other.getY() >= r.getY()
         && other.getY() + other.getHeight() <= r.getY() + r.getHeight();
   }

   public QuadTree(QuadTree.RectRegion zone, int level) {
      this.nodes = new Array();
      this.zone = zone;
      this.level = level;
   }

   private int findRegion(QuadTree.Region r, boolean split) {
      int region = -1;
      if (this.nodes.size >= maxNodes && this.level < maxLevel) {
         if (this.regions == null && split) {
            this.regions = new QuadTree[4];
            int w = this.zone.w / 2;
            int h = this.zone.h / 2;
            int newLevel = this.level + 1;
            this.regions[0] = new QuadTree<>(new QuadTree.RectRegion(this.zone.x, this.zone.y + this.zone.h / 2, w, h), newLevel);
            this.regions[1] = new QuadTree<>(new QuadTree.RectRegion(this.zone.x + this.zone.w / 2, this.zone.y + this.zone.h / 2, w, h), newLevel);
            this.regions[2] = new QuadTree<>(new QuadTree.RectRegion(this.zone.x, this.zone.y, w, h), newLevel);
            this.regions[3] = new QuadTree<>(new QuadTree.RectRegion(this.zone.x + this.zone.w / 2, this.zone.y, w, h), newLevel);
         }

         if (this.regions != null) {
            for (int i = 0; i < 4; i++) {
               if (contains(this.regions[i].zone, r)) {
                  region = i;
                  break;
               }
            }
         }
      }

      return region;
   }

   public void insert(T element) {
      int region = this.findRegion(element, true);
      if (region != -1 && this.level != maxLevel) {
         this.regions[region].insert(element);
         if (this.nodes.size >= maxNodes && this.level < maxLevel) {
            this.temp.clear();
            this.temp.addAll(this.nodes);
            this.nodes.clear();

            for (T t : this.temp) {
               this.insert(t);
            }
         }
      } else {
         this.nodes.add(element);
      }
   }

   public Array<T> getElements(Array<T> list, QuadTree.RectRegion r) {
      int region = this.findRegion(r, false);
      list.addAll(this.nodes);
      if (region != -1) {
         this.regions[region].getElements(list, r);
      } else {
         this.getAllElements(list, true);
      }

      return list;
   }

   public Array<T> getAllElements(Array<T> list, boolean firstCall) {
      if (this.regions != null) {
         this.regions[0].getAllElements(list, false);
         this.regions[1].getAllElements(list, false);
         this.regions[2].getAllElements(list, false);
         this.regions[3].getAllElements(list, false);
      }

      if (!firstCall) {
         list.addAll(this.nodes);
      }

      return list;
   }

   public static class RectRegion implements QuadTree.Region {
      int x;
      int y;
      int w;
      int h;

      public RectRegion(int x, int y, int w, int h) {
         this.x = x;
         this.y = y;
         this.w = w;
         this.h = h;
      }

      @Override
      public int getX() {
         return this.x;
      }

      @Override
      public int getY() {
         return this.y;
      }

      @Override
      public int getWidth() {
         return this.w;
      }

      @Override
      public int getHeight() {
         return this.h;
      }
   }

   public interface Region {
      int getX();

      int getY();

      int getWidth();

      int getHeight();
   }
}
