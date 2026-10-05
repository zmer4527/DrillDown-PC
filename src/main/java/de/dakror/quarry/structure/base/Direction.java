package de.dakror.quarry.structure.base;

import com.badlogic.gdx.math.MathUtils;

public enum Direction {
   North(0, 1),
   East(1, 0),
   South(0, -1),
   West(-1, 0);

   public static final Direction[] values = values();
   public final int dx;
   public final int dy;
   public final int rot;
   private Direction inv;
   private Direction next;
   private Direction prev;

   private Direction(int dx, int dy) {
      this.dx = dx;
      this.dy = dy;
      this.rot = (int)((-180.0F / (float)Math.PI) * MathUtils.atan2((float)(-dy), (float)dx));
   }

   public Direction inv() {
      if (this.inv != null) {
         return this.inv;
      } else {
         this.inv = values[(this.ordinal() + 2) % 4];
         return this.inv;
      }
   }

   public Direction next() {
      if (this.next != null) {
         return this.next;
      } else {
         this.next = values[(this.ordinal() + 1) % 4];
         return this.next;
      }
   }

   public Direction prev() {
      if (this.prev != null) {
         return this.prev;
      } else {
         this.prev = values[(this.ordinal() + 3) % 4];
         return this.prev;
      }
   }

   public boolean isPerpendicular(Direction o) {
      return this.ordinal() % 2 != o.ordinal() % 2;
   }
}
