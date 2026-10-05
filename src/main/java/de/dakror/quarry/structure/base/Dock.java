package de.dakror.quarry.structure.base;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import de.dakror.quarry.Quarry;
import de.dakror.quarry.game.Item;
import java.util.Arrays;
import java.util.EnumSet;

public class Dock {
   public static final Dock.ConnectionType[] connectionTypes = new Dock.ConnectionType[10];
   public final int x;
   public final int y;
   public final Direction dir;
   public final Dock.DockType type;
   public final Dock.DockFilter filter;

   public Dock(int x, int y, Direction dir, Dock.DockType type, Dock.DockFilter filter) {
      this.x = x;
      this.y = y;
      this.dir = dir;
      this.type = type;
      this.filter = filter;
   }

   public Dock(int x, int y, Direction dir, Dock.DockType type) {
      this(x, y, dir, type, null);
   }

   public Dock(Dock o) {
      this.x = o.x;
      this.y = o.y;
      this.dir = o.dir;
      this.type = o.type;
      if (o.filter != null) {
         this.filter = new Dock.DockFilter(o.filter);
      } else {
         this.filter = null;
      }
   }

   public static enum ConnectionType {
      No(1),
      Open(2),
      Dock(3),
      Closed(4),
      DockOutput(8),
      DockInput(9);

      public final byte value;

      private ConnectionType(int value) {
         this.value = (byte)value;
         connectionTypes[value] = this;
      }
   }

   public static class DockFilter {
      final EnumSet<Item.ItemCategory> categories = EnumSet.noneOf(Item.ItemCategory.class);
      final EnumSet<Item.ItemCategory> excludedCategories = EnumSet.noneOf(Item.ItemCategory.class);
      final EnumSet<Item.ItemType> items = EnumSet.noneOf(Item.ItemType.class);
      final EnumSet<Item.ItemType> excludedItems = EnumSet.noneOf(Item.ItemType.class);

      public DockFilter(Dock.DockFilter o) {
         this.categories.addAll(o.categories);
         this.excludedCategories.addAll(o.excludedCategories);
         this.items.addAll(o.items);
         this.excludedItems.addAll(o.excludedItems);
      }

      public DockFilter(Object... filters) {
         for (Object o : filters) {
            if (o instanceof Item.ItemCategory) {
               this.categories.add((Item.ItemCategory)o);
            } else {
               if (!(o instanceof Item.ItemType)) {
                  throw new IllegalArgumentException("Illegal parameter type for " + o);
               }

               this.items.add((Item.ItemType)o);
            }
         }
      }

      public Dock.DockFilter exclude(Item.ItemType... items) {
         this.excludedItems.addAll(Arrays.asList(items));
         return this;
      }

      public Dock.DockFilter exclude(Item.ItemCategory... cat) {
         this.excludedCategories.addAll(Arrays.asList(cat));
         return this;
      }

      public boolean accepts(Item.ItemType item) {
         if (!this.excludedItems.contains(item) && !this.excludedItems.contains(Item.base(item))) {
            for (Item.ItemCategory c : this.excludedCategories) {
               if (item.categories.contains(c)) {
                  return false;
               }
            }

            if (!this.items.contains(item) && !this.items.contains(Item.base(item))) {
               for (Item.ItemCategory cx : this.categories) {
                  if (item.categories.contains(cx)) {
                     return true;
                  }
               }

               return false;
            } else {
               return true;
            }
         } else {
            return false;
         }
      }

      public boolean accepts(Item.ItemCategory category) {
         for (Item.ItemCategory c : this.excludedCategories) {
            if (category == c) {
               return false;
            }
         }

         for (Item.ItemCategory cx : this.categories) {
            if (category == cx) {
               return true;
            }
         }

         return false;
      }
   }

   public static enum DockType {
      ItemIn("itemin"),
      ItemOut("itemout"),
      FluidIn("fluidin"),
      FluidOut("fluidout"),
      Power("power"),
      BigPower("bigpower"),
      StackIn(null),
      StackOut(null);

      public final TextureRegion tex;

      private DockType(String name) {
         if (name != null) {
            this.tex = Quarry.Q.atlas.findRegion("dock_" + name);
         } else {
            this.tex = null;
         }
      }
   }
}
