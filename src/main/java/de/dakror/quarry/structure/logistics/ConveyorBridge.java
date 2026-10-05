package de.dakror.quarry.structure.logistics;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import de.dakror.common.libgdx.io.NBT;
import de.dakror.common.libgdx.render.SpriteRenderer;
import de.dakror.quarry.Quarry;
import de.dakror.quarry.game.Item;
import de.dakror.quarry.game.Layer;
import de.dakror.quarry.scenes.Game;
import de.dakror.quarry.structure.base.Direction;
import de.dakror.quarry.structure.base.IFlippable;
import de.dakror.quarry.structure.base.IItemZModifier;
import de.dakror.quarry.structure.base.Schema;
import de.dakror.quarry.structure.base.Structure;
import de.dakror.quarry.structure.base.StructureType;

public class ConveyorBridge extends Conveyor implements IItemZModifier, IFlippable {
   public static final Conveyor.ConveyorSchema classSchema = new Conveyor.ConveyorSchema(
         0, StructureType.ConveyorBridge, true, 1, 1, "conveyorbridge", new Item.Items(Item.ItemType.Stone, 4, Item.ItemType.Scaffolding, 2), null
      )
      .flags(new Schema.Flags[]{Schema.Flags.NoDustEffect});
   static final TextureRegion tex2 = Quarry.Q.atlas.findRegion("structure_conveyor_we");
   static final TextureRegion tex3 = Quarry.Q.atlas.findRegion("structure_conveyorbridge3");
   protected boolean dir2Prev;

   public ConveyorBridge(int x, int y) {
      this(x, y, Direction.South, classSchema);
   }

   protected ConveyorBridge(int x, int y, Conveyor.ConveyorSchema schema) {
      this(x, y, Direction.South, schema);
   }

   public ConveyorBridge(int x, int y, Direction direction, Conveyor.ConveyorSchema schema) {
      super(x, y, schema);
      this.dir = direction;
      this.dir2Prev = true;
   }

   public ConveyorBridge(int x, int y, Direction direction, boolean prev, Conveyor.ConveyorSchema schema) {
      super(x, y, schema);
      this.dir = direction;
      this.dir2Prev = prev;
   }

   @Override
   public void draw(SpriteRenderer spriter) {
      float var10002 = (float)(this.x * 64);
      spriter.add(this.getSchema().tex, var10002, (float)(this.y * 64), -5.0F, 32.0F, 32.0F, 64.0F, 64.0F, 1.0F, 1.0F, (float)this.getDirection2().rot);
      spriter.add(tex2, (float)(this.x * 64), (float)(this.y * 64), -4.99F, 32.0F, 32.0F, 64.0F, 64.0F, 1.0F, 1.0F, (float)this.dir.rot);
      spriter.add(tex3, (float)(this.x * 64 + 12), (float)(this.y * 64 + 12), -3.1F, 20.0F, 20.0F, 40.0F, 40.0F, 1.0F, 1.0F, (float)this.dir.rot);
   }

   @Override
   public void updateStructures() {
      Layer l = this.layer == null ? Game.G.layer : this.layer;
      Structure<?> s = (Structure<?>)Game.G.activeStructureTrail.get((this.x + this.dir.dx) * l.height + this.y + this.dir.dy);
      if (s == null) {
         s = l.getStructure(this.x + this.dir.dx, this.y + this.dir.dy);
      }

      this.structures[0] = s;
      s = (Structure<?>)Game.G.activeStructureTrail.get((this.x - this.dir.dx) * l.height + (this.y - this.dir.dy));
      if (s == null) {
         s = l.getStructure(this.x - this.dir.dx, this.y - this.dir.dy);
      }

      this.structures[2] = s;
      Direction d2 = this.getDirection2();
      s = (Structure<?>)Game.G.activeStructureTrail.get((this.x + d2.dx) * l.height + this.y + d2.dy);
      if (s == null) {
         s = l.getStructure(this.x + d2.dx, this.y + d2.dy);
      }

      this.structures[1] = s;
      s = (Structure<?>)Game.G.activeStructureTrail.get((this.x - d2.dx) * l.height + (this.y - d2.dy));
      if (s == null) {
         s = l.getStructure(this.x - d2.dx, this.y - d2.dy);
      }

      this.structures[3] = s;
   }

   @Override
   protected void notifyNeighbors(boolean chain) {
      if (this.structures[2] instanceof Conveyor && this.isItemSlotFree(Layer.getStartingSlot(this.dir))) {
         ((Conveyor)this.structures[2]).itemChanges = true;
         if (chain) {
            ((Conveyor)this.structures[2]).updateItems(0.0, 1, true, false);
         }
      }

      if (this.structures[3] instanceof Conveyor && this.isItemSlotFree(Layer.getStartingSlot(this.getDirection2()))) {
         ((Conveyor)this.structures[3]).itemChanges = true;
         if (chain) {
            ((Conveyor)this.structures[3]).updateItems(0.0, 1, true, false);
         }
      }
   }

   @Override
   public boolean isItemSlotFree(int slot) {
      return this.items[slot] == null;
   }

   @Override
   public Structure<?> getStructureInDirection(Direction dir) {
      if (dir == this.dir) {
         return this.structures[0];
      } else if (dir == this.getDirection2()) {
         return this.structures[1];
      } else if (dir == this.dir.inv()) {
         return this.structures[2];
      } else {
         return dir == this.getDirection2().inv() ? this.structures[3] : null;
      }
   }

   @Override
   public Direction getNextDirection(Item.ItemType item, Direction prev, float currentZ, int slot) {
      if (prev != null) {
         return prev != this.dir.inv() && prev != this.getDirection2().inv() ? prev : prev.inv();
      } else {
         return currentZ == -4.0F ? this.getDirection2() : this.dir;
      }
   }

   @Override
   public Direction getCurrentDirection(Item.ItemType item, Direction prev, float currentZ, int slot) {
      return this.getNextDirection(item, prev, currentZ, slot);
   }

   public Direction getDirection2() {
      return this.dir2Prev ? this.dir.prev() : this.dir.next();
   }

   @Override
   public float getItemZ(Item.ItemType type, Direction direction) {
      return Math.abs(direction.dx) == Math.abs(this.dir.dx) ? -2.1F : -4.0F;
   }

   @Override
   public void flip() {
      this.dir2Prev = !this.dir2Prev;
      this.setDirty();
   }

   @Override
   public Object clone() {
      return new ConveyorBridge(this.x, this.y, this.dir, this.dir2Prev, classSchema);
   }

   @Override
   protected void saveData(NBT.Builder b) {
      super.saveData(b);
      b.Byte("prev", (byte)(this.dir2Prev ? 1 : 0));
   }

   @Override
   protected void loadData(NBT.CompoundTag tag) throws NBT.NBTException {
      super.loadData(tag);
      this.dir2Prev = tag.Byte("prev", (byte)1) == 1;
   }

   @Override
   protected void copyData(int[] copyRegion, NBT.Builder b) {
      super.copyData(copyRegion, b);
      b.Byte("prev", (byte)(this.dir2Prev ? 1 : 0));
   }

   @Override
   protected void pasteData(int[] pasteRegion, NBT.CompoundTag tag) {
      super.pasteData(pasteRegion, tag);
      this.dir2Prev = tag.Byte("prev", (byte)1) == 1;
   }
}
