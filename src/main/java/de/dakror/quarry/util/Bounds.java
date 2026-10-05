package de.dakror.quarry.util;

import de.dakror.quarry.structure.DistillationColumn;
import de.dakror.quarry.structure.base.FluidTubeStructure;
import de.dakror.quarry.structure.base.GeneratorStructure;
import de.dakror.quarry.structure.base.ProducerStructure;
import de.dakror.quarry.structure.base.Structure;
import de.dakror.quarry.structure.logistics.Conveyor;
import de.dakror.quarry.structure.logistics.ItemLift;
import de.dakror.quarry.structure.logistics.ItemLiftBelow;
import de.dakror.quarry.structure.logistics.TubeShaft;
import de.dakror.quarry.structure.logistics.TubeShaftBelow;
import de.dakror.quarry.structure.power.CableShaft;
import de.dakror.quarry.structure.power.CableShaftBelow;
import de.dakror.quarry.structure.power.CopperCable;
import de.dakror.quarry.structure.power.GasTurbine;
import de.dakror.quarry.structure.power.PowerPole;
import de.dakror.quarry.structure.storage.Barrel;
import de.dakror.quarry.structure.storage.Storage;

public class Bounds {
   protected int x;
   protected int y;
   protected int width;
   protected int height;
   protected int flags;

   public void clear() {
      this.width = 0;
      this.height = 0;
      this.flags = 0;
   }

   public void add(Structure<?> s, int flags) {
      this.flags |= flags;
      if (s instanceof Conveyor) {
         this.flags |= 1;
      }

      if (s instanceof FluidTubeStructure) {
         this.flags |= 2;
      }

      if (s instanceof CopperCable || s instanceof PowerPole) {
         this.flags |= 4;
      }

      if (s instanceof ProducerStructure) {
         this.flags |= 8;
      }

      if (s instanceof Storage || s instanceof Barrel) {
         this.flags |= 16;
      }

      if (s instanceof GeneratorStructure) {
         this.flags |= 32;
      }

      if (s instanceof DistillationColumn) {
         this.flags |= 1024;
      }

      if (s instanceof GasTurbine) {
         this.flags |= 2048;
      }

      if (s.getSchema().powerDocks > 0) {
         this.flags |= 64;
      }

      if (s instanceof CableShaft
         || s instanceof CableShaftBelow
         || s instanceof ItemLift
         || s instanceof ItemLiftBelow
         || s instanceof TubeShaft
         || s instanceof TubeShaftBelow) {
         this.flags |= 128;
      }

      if (this.width == 0) {
         this.x = s.x;
         this.y = s.y;
         this.width = s.getWidth();
         this.height = s.getHeight();
      } else {
         int minX = Math.min(this.x, s.x);
         int maxX = Math.max(this.x + this.width, s.x + s.getWidth());
         this.x = minX;
         this.width = maxX - minX;
         int minY = Math.min(this.y, s.y);
         int maxY = Math.max(this.y + this.height, s.y + s.getHeight());
         this.y = minY;
         this.height = maxY - minY;
      }
   }

   public void set(Bounds o) {
      this.x = o.x;
      this.y = o.y;
      this.width = o.width;
      this.height = o.height;
      this.flags = o.flags;
   }

   public void set(int x, int y, int width, int height, int flags) {
      this.x = x;
      this.y = y;
      this.width = width;
      this.height = height;
      this.flags = flags;
   }

   public void add(Bounds o) {
      this.flags = this.flags | o.flags;
      this.width = Math.max(this.x + this.width, o.x + o.width) - Math.min(this.x, o.x);
      this.height = Math.max(this.y + this.height, o.y + o.height) - Math.min(this.y, o.y);
      this.x = Math.min(this.x, o.x);
      this.y = Math.min(this.y, o.y);
   }

   public int getX() {
      return this.x;
   }

   public int getY() {
      return this.y;
   }

   public int getWidth() {
      return this.width;
   }

   public int getHeight() {
      return this.height;
   }

   public boolean touches(Structure<?> structure) {
      return this.width == 0
         ? false
         : Math.max(this.x + this.width, structure.x + structure.getWidth()) - Math.min(this.x, structure.x) - (this.width + structure.getWidth()) <= 0
            && Math.max(this.y + this.height, structure.y + structure.getHeight()) - Math.min(this.y, structure.y) - (this.height + structure.getHeight()) <= 0;
   }

   public boolean touches(int x, int y, int width, int height) {
      return this.width == 0
         ? false
         : Math.max(this.x + this.width, x + width) - Math.min(this.x, x) - (this.width + width) <= 0
            && Math.max(this.y + this.height, y + height) - Math.min(this.y, y) - (this.height + height) <= 0;
   }

   public boolean intersects(int x, int y, int width, int height) {
      return this.width == 0 ? false : x <= this.x + this.width && x + width >= this.x && y + height >= this.y && y <= this.y + this.height;
   }

   public boolean isEmpty() {
      return this.width == 0 && this.flags == 0;
   }

   @Override
   public String toString() {
      return "[" + this.x + ":" + this.y + ", " + this.width + "x" + this.height + ", " + Integer.toBinaryString(this.flags) + "]";
   }

   public boolean hasFlag(int flags) {
      return !this.isEmpty() && (this.flags & flags) == flags;
   }

   public boolean hasAnyFlag(int flags) {
      return !this.isEmpty() && (this.flags & flags) != 0;
   }

   public static class Flags {
      public static final int CONVEYOR = 1;
      public static final int FLUIDTUBE = 2;
      public static final int CABLE = 4;
      public static final int PRODUCER = 8;
      public static final int STORAGE = 16;
      public static final int GENERATOR = 32;
      public static final int POWERNODE = 64;
      public static final int SHAFT = 128;
      public static final int CONSTRUCTION = 256;
      public static final int DESTRUCTION = 512;
      public static final int DISTILLATIONCOLUMN = 1024;
      public static final int GASTURBINE = 2048;
   }
}
