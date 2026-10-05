package de.dakror.quarry.structure.power;

import de.dakror.common.libgdx.io.NBT;
import de.dakror.common.libgdx.render.SpriteRenderer;
import de.dakror.quarry.game.Item;
import de.dakror.quarry.structure.base.Direction;
import de.dakror.quarry.structure.base.Dock;
import de.dakror.quarry.structure.base.Schema;
import de.dakror.quarry.structure.base.Structure;
import de.dakror.quarry.structure.base.StructureType;
import de.dakror.quarry.util.CopyRegionHelper;

public class PowerPoleGhost extends Structure<Schema> {
   public static final Schema classSchema = new Schema(0, StructureType.PowerPoleGhost, false, 1, 1, "powerpoleghost", new Item.Items(), null)
      .flags(Schema.Flags.Indestructible);
   PowerPole source;
   Structure<?> target;
   int sourceIndex;
   int targetIndex;
   int dockIndex = -1;
   int absDockIndex;

   public PowerPoleGhost(int x, int y) {
      super(x, y, classSchema);
   }

   public PowerPoleGhost(int x, int y, PowerPole source, Structure<?> target) {
      super(x, y, classSchema);
      this.source = source;
      this.target = target;
   }

   @Override
   public void postLoad() {
      super.postLoad();

      try {
         this.source = (PowerPole)this.layer.getStructure(this.sourceIndex);
         this.target = this.layer.getStructure(this.targetIndex);
      } catch (Exception var2) {
         this.layer.removeStructure(this);
      }
   }

   public int getDockIndex() {
      if (this.dockIndex == -1) {
         int i = 0;

         for (int j = 0; i < this.target.getDocks().length; i++) {
            Dock d = this.target.getDocks()[i];
            if (d.type == Dock.DockType.BigPower && this.target.isNextToDock(this.x, this.y, d.dir.inv(), d)) {
               this.dockIndex = j;
               this.absDockIndex = i;
               break;
            }

            if (d.type == Dock.DockType.Power || d.type == Dock.DockType.BigPower) {
               j++;
            }
         }
      }

      return this.dockIndex;
   }

   @Override
   public void draw(SpriteRenderer spriter) {
      this.getDockIndex();
      Direction dir = this.target.getDocks()[this.absDockIndex].dir;
      float var10002 = (float)(this.x * 64);
      spriter.add(
         this.getSchema().tex,
         var10002,
         (float)(this.y * 64),
         -2.0F,
         (float)this.getWidth() / 2.0F * 64.0F,
         (float)this.getHeight() / 2.0F * 64.0F,
         (float)(this.getWidth() * 64),
         (float)(this.getHeight() * 64),
         1.0F,
         1.0F,
         (float)(dir.rot - 90)
      );
   }

   public PowerPole getPowerPole() {
      return this.source;
   }

   @Override
   protected void saveData(NBT.Builder b) {
      super.saveData(b);
      b.Int("src", this.source.x * this.layer.height + this.source.y);
      b.Int("tgt", this.target.x * this.layer.height + this.target.y);
   }

   @Override
   protected void loadData(NBT.CompoundTag tag) throws NBT.NBTException {
      super.loadData(tag);
      this.sourceIndex = tag.Int("src");
      this.targetIndex = tag.Int("tgt");
   }

   @Override
   protected void copyData(int[] copyRegion, NBT.Builder b) {
      super.copyData(copyRegion, b);
      b.Int("src", CopyRegionHelper.indexGlobalToRelative(copyRegion, this.source.x * this.layer.height + this.source.y));
      b.Int("tgt", CopyRegionHelper.indexGlobalToRelative(copyRegion, this.target.x * this.layer.height + this.target.y));
   }

   @Override
   protected void pasteData(int[] pasteRegion, NBT.CompoundTag tag) {
      super.pasteData(pasteRegion, tag);
      Structure<?> s = CopyRegionHelper.getStructure(this, pasteRegion, tag.Int("src", 0));
      if (s instanceof PowerPole) {
         this.source = (PowerPole)s;
      } else {
         this.source = null;
      }

      this.target = CopyRegionHelper.getStructure(this, pasteRegion, tag.Int("tgt", 0));
   }
}
