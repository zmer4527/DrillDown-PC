package de.dakror.quarry.structure.producer;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import de.dakror.common.libgdx.io.NBT;
import de.dakror.common.libgdx.render.SpriteRenderer;
import de.dakror.quarry.Const;
import de.dakror.quarry.game.Item;
import de.dakror.quarry.game.Science;
import de.dakror.quarry.scenes.Game;
import de.dakror.quarry.structure.base.ProducerStructure;
import de.dakror.quarry.structure.base.RecipeList;
import de.dakror.quarry.structure.base.Schema;
import de.dakror.quarry.structure.base.StructureType;
import de.dakror.quarry.util.Sfx;
import de.dakror.quarry.util.SpriterDelegateBatch;

public class AirPurifier extends ProducerStructure {
   public static final ProducerStructure.ProducerSchema classSchema = new ProducerStructure.ProducerSchema(
         0,
         StructureType.AirPurifier,
         3,
         3,
         "airpurifier",
         new Item.Items(Item.ItemType.StoneBrick, 8, Item.ItemType.SteelPlate, 25, Item.ItemType.Scaffolding, 30),
         new RecipeList() {
            @Override
            protected void init() {
               this.add(new RecipeList.Recipe(10.0F, "purify"));
            }
         },
         new Sfx("airpurifier" + Const.SFX_FORMAT),
         false
      )
      .<Schema>sciences(new Science.ScienceType[]{Science.ScienceType.MineExpansion})
      .flags(Schema.Flags.TextureAlwaysUpright);
   private static final float halfWidth = 24.5F;
   int currentHalfWidth = this.getWidth() / 2 + 1;

   public AirPurifier(int x, int y) {
      super(x, y, classSchema);
   }

   @Override
   public void onPlacement(boolean fromLoading) {
      super.onPlacement(fromLoading);
      if (this.layer != null && this.layer.getIndex() == 0) {
         this.sleeping = true;
      }

      if (this.layer != null) {
         this.unveilArea(this.getWidth() / 2 + 1);

         for (int i = this.getWidth() / 2; (float)i < 24.5F; i++) {
            for (int j = -i / 2; j < i / 2; j++) {
               if (this.layer.isInFogOfWar(this.x + this.getWidth() / 2 - i, this.y + this.getHeight() / 2 + j)
                  || this.layer.isInFogOfWar(this.x + this.getWidth() / 2 + i, this.y + this.getHeight() / 2 + j)
                  || this.layer.isInFogOfWar(this.x + this.getWidth() / 2 + j, this.y + this.getHeight() / 2 - i)
                  || this.layer.isInFogOfWar(this.x + this.getWidth() / 2 + j, this.y + this.getHeight() / 2 + i)) {
                  this.currentHalfWidth = i - 1;
                  return;
               }
            }
         }
      }
   }

   protected boolean unveilArea(int radius) {
      boolean any = false;
      int cx = this.x + this.getWidth() / 2;
      int cy = this.y + this.getHeight() / 2;

      for (int i = -radius; i <= radius; i++) {
         for (int j = -radius; j <= radius; j++) {
            if (!any && (this.layer.getMeta(i + cx, j + cy) & 1) != 0) {
               any = true;
            }

            this.layer.removeMeta(i + cx, j + cy, 1);
         }
      }

      return any;
   }

   @Override
   protected void doProductionStep() {
      if ((float)this.currentHalfWidth < 24.5F) {
         this.currentHalfWidth++;
         this.unveilArea(this.currentHalfWidth);
      } else {
         this.sleeping = true;
      }
   }

   @Override
   public void drawFrame(SpriteRenderer spriter, ShapeRenderer shaper, SpriterDelegateBatch pfxBatch) {
      super.drawFrame(spriter, shaper, pfxBatch);
      if (Game.G.activeStructure == this || this.clicked) {
         shaper.set(ShapeType.Filled);
         shaper.setColor(1.0F, 0.498F, 0.314F, 0.2F);
         shaper.rect(
            ((float)this.x + (float)this.getWidth() / 2.0F - 24.5F) * 64.0F, ((float)this.y + (float)this.getHeight() / 2.0F - 24.5F) * 64.0F, 3136.0F, 3136.0F
         );
      }
   }

   @Override
   protected void saveData(NBT.Builder b) {
      super.saveData(b);
      b.Byte("radius", (byte)this.currentHalfWidth);
   }

   @Override
   protected void loadData(NBT.CompoundTag tag) throws NBT.NBTException {
      super.loadData(tag);
      this.currentHalfWidth = tag.Byte("radius", (byte)0);
   }
}
