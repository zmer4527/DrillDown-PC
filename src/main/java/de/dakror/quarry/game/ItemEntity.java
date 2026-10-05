package de.dakror.quarry.game;

import com.badlogic.gdx.utils.Pool.Poolable;
import de.dakror.common.libgdx.io.NBT;
import de.dakror.quarry.scenes.Game;
import de.dakror.quarry.structure.base.Direction;
import de.dakror.quarry.structure.base.Structure;
import de.dakror.quarry.util.Savable;

public class ItemEntity implements Poolable, Savable {
   public Item.ItemType item;
   public int x;
   public int y;
   public int slot;
   public int lastSlot;
   public float z;
   public double interp;
   public Direction dir;
   public Structure<?> src;
   public int srcIndex;
   public int srcLayer;

   public void reset() {
      this.interp = 0.0;
      this.item = null;
      this.src = null;
      this.x = 0;
      this.y = 0;
      this.slot = 0;
      this.lastSlot = -1;
      this.z = -4.0F;
      this.dir = null;
      this.srcIndex = -1;
   }

   @Override
   public void save(NBT.Builder b) {
      b.Compound()
         .Short("value", this.item.value)
         .Int("x", this.x)
         .Int("y", this.y)
         .Int("slot", this.slot)
         .Int("lastSlot", this.lastSlot)
         .Double("interp", this.interp);
      if (this.z != -4.0F) {
         b.Float("z", this.z);
      }

      if (this.dir == null) {
         b.Byte("dir", (byte)-1);
      } else {
         b.Byte("dir", (byte)this.dir.ordinal());
      }

      if (this.src == null) {
         b.Int("src", -1);
      } else {
         b.Int("src", this.src.x * this.src.layer.height + this.src.y).Int("layer", this.src.layer.index);
      }

      b.End();
   }

   public void load(NBT.CompoundTag tag) throws NBT.NBTException {
      this.item = Item.get(tag.Short("value"));
      this.x = tag.Int("x");
      this.y = tag.Int("y");
      this.slot = tag.Int("slot");
      this.lastSlot = tag.Int("lastSlot", -1);
      this.z = tag.Float("z", -4.0F);

      try {
         this.interp = tag.Double("interp") % 1.0;
      } catch (NBT.NBTException var3) {
         this.interp = (double)(tag.Float("interp") % 1.0F);
      }

      int d = tag.Byte("dir");
      if (d > -1) {
         this.dir = Direction.values[d];
      }

      this.srcIndex = tag.Int("src", -1);
      this.srcLayer = tag.Int("layer", 0);
   }

   public void postLoad() {
      if (this.srcIndex > -1) {
         Layer l = Game.G.getLayer(this.srcLayer);
         if (l != null) {
            this.src = l.getStructure(this.srcIndex);
         }
      }
   }
}
