package de.dakror.quarry.structure.logistics;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Interpolation;
import de.dakror.common.libgdx.io.NBT;
import de.dakror.common.libgdx.render.SpriteRenderer;
import de.dakror.quarry.Quarry;
import de.dakror.quarry.game.Item;
import de.dakror.quarry.game.Layer;
import de.dakror.quarry.structure.base.Direction;
import de.dakror.quarry.structure.base.RouterStructure;
import de.dakror.quarry.structure.base.StructureType;
import de.dakror.quarry.util.Bounds;
import de.dakror.quarry.util.SpriterDelegateBatch;

public class Distributor extends RouterStructure {
   public static final RouterStructure.RouterSchema classSchema = new RouterStructure.RouterSchema(
      0, StructureType.Distributor, false, "distributor", new Item.Items(Item.ItemType.StoneBrick, 2, Item.ItemType.TinIngot, 2), null
   );
   int index;
   static TextureRegion cursor = Quarry.Q.atlas.findRegion("structure_distributor_cursor");
   static TextureRegion base = Quarry.Q.atlas.findRegion("structure_distributor_lower");
   int oldIndex;
   float visual;
   boolean anyTube;

   public Distributor(int x, int y) {
      super(x, y, classSchema);
   }

   @Override
   public void draw(SpriteRenderer spriter) {
      if (this.getDocks().length > 0) {
         this.drawDocks(spriter);
      }

      spriter.add(base, (float)(this.x * 64), (float)(this.y * 64), -3.0F, (float)(this.getWidth() * 64), (float)(this.getHeight() * 64));
   }

   @Override
   public void drawFrame(SpriteRenderer spriter, ShapeRenderer shaper, SpriterDelegateBatch pfxBatch) {
      int w = 36;
      int h = 16;
      int px = 8;
      int py = 8;
      int to = -this.index * 90;
      if (to == 0 && this.oldIndex != this.index) {
         to = -360;
      }

      float val = Interpolation.linear.apply((float)(-this.oldIndex * 90), (float)to, this.visual);
      spriter.add(
         cursor,
         ((float)this.x + 0.5F) * 64.0F - (float)px,
         ((float)this.y + 0.5F) * 64.0F - (float)py,
         -2.9F,
         (float)px,
         (float)py,
         (float)w,
         (float)h,
         1.0F,
         1.0F,
         val + 90.0F
      );
      super.drawFrame(spriter, shaper, pfxBatch);
   }

   @Override
   public void postLoad() {
      super.postLoad();

      for (int i = 0; i < 4; i++) {
         if (this.tubes[this.index] != null) {
            this.anyTube = true;
            break;
         }
      }
   }

   @Override
   public void update(double deltaTime, int gameSpeed, Bounds dirtyBounds) {
      super.update(deltaTime, gameSpeed, dirtyBounds);
      if (this.layer != null && dirtyBounds.touches(this)) {
         this.anyTube = false;

         for (int i = 0; i < 4; i++) {
            if (this.tubes[this.index] == null) {
               this.visual = 0.0F;
               this.oldIndex = this.index;
               this.index = (this.index + 1) % 4;
            } else {
               this.anyTube = true;
            }
         }
      }

      if (this.visual < 1.0F) {
         this.visual = (float)((double)this.visual + deltaTime * (double)gameSpeed * 4.0);
      } else {
         this.visual = 1.0F;
      }
   }

   @Override
   protected boolean dispatchItem() {
      if (!this.anyTube) {
         return false;
      } else {
         this.oldIndex = this.index;
         this.visual = 0.0F;
         boolean success = this.layer.addItemEntity(this.currentItem, this, Direction.values[this.index], this.currentSource);

         for (int i = 0; i < 4; i++) {
            int ind = (this.index + i + 1) % 4;
            if (this.isOutputOpen(ind)) {
               this.index = ind;
               break;
            }
         }

         return success;
      }
   }

   protected boolean isOutputOpen(int i) {
      Direction d = Direction.values[i];
      return this.tubes[i] != null && this.tubes[i].isItemSlotFree(Layer.getStartingSlot(d));
   }

   @Override
   protected void saveData(NBT.Builder b) {
      super.saveData(b);
      b.Int("index", this.index).Int("old", this.oldIndex);
   }

   @Override
   protected void loadData(NBT.CompoundTag tag) throws NBT.NBTException {
      super.loadData(tag);
      this.index = tag.Int("index", 0);
      this.oldIndex = tag.Int("old", 0);
   }
}
