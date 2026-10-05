package de.dakror.quarry.structure;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import de.dakror.common.libgdx.io.NBT;
import de.dakror.common.libgdx.render.SpriteRenderer;
import de.dakror.quarry.Const;
import de.dakror.quarry.Quarry;
import de.dakror.quarry.game.Item;
import de.dakror.quarry.game.Layer;
import de.dakror.quarry.game.Science;
import de.dakror.quarry.scenes.Game;
import de.dakror.quarry.structure.base.Direction;
import de.dakror.quarry.structure.base.Dock;
import de.dakror.quarry.structure.base.ProducerStructure;
import de.dakror.quarry.structure.base.RecipeList;
import de.dakror.quarry.structure.base.Schema;
import de.dakror.quarry.structure.base.Structure;
import de.dakror.quarry.structure.base.StructureType;
import de.dakror.quarry.util.Sfx;
import de.dakror.quarry.util.SpriterDelegateBatch;

public class ShaftDrill extends ProducerStructure {
   public static final ProducerStructure.ProducerSchema classSchema = new ProducerStructure.ProducerSchema(
         0,
         StructureType.ShaftDrill,
         4,
         4,
         "drill",
         new Item.Items(Item.ItemType.SteelIngot, 15, Item.ItemType.SteelPlate, 30, Item.ItemType.StoneBrick, 50),
         new RecipeList() {
            @Override
            protected void init() {
               this.add(
                  new RecipeList.Recipe(300.0F, "drill")
                     .input(
                        new Item.Items.Amount(Item.ItemType.Water, 75000),
                        new Item.Items.Amount(Item.ItemType.SteelIngot, 180),
                        new Item.Items.Amount(Item.ItemType.ConcretePowder, 120)
                     )
               );
            }
         },
         new Sfx("shaftdrill" + Const.SFX_FORMAT),
         false,
         new Dock(3, 0, Direction.East, Dock.DockType.FluidIn, new Dock.DockFilter(Item.ItemType.Water)),
         new Dock(2, 0, Direction.South, Dock.DockType.ItemIn, new Dock.DockFilter(Item.ItemType.SteelIngot)),
         new Dock(3, 0, Direction.South, Dock.DockType.ItemIn, new Dock.DockFilter(Item.ItemType.ConcretePowder))
      )
      .<Schema>flags(new Schema.Flags[]{Schema.Flags.ConfirmDestruction})
      .sciences(Science.ScienceType.MineExpansion);
   static TextureRegion jammedIcon = Quarry.Q.atlas.findRegion("icon_jammed");
   int depth;

   public ShaftDrill(int x, int y) {
      super(x, y, classSchema);
   }

   @Override
   public void onDestroy() {
      super.onDestroy();

      for (int i = this.layer.getIndex() + 1; i < Game.G.getLayerCount(); i++) {
         Layer l = Game.G.getLayer(i);
         Structure<?> s = l.getStructure(this.x, this.y);
         if (!(s instanceof ShaftDrillHead)) {
            break;
         }

         l.removeStructure(s);
      }
   }

   private boolean isJammed() {
      if (this.layer == null) {
         return false;
      } else if (this.layer.getIndex() == Game.G.getLayerCount() - 1) {
         return false;
      } else {
         for (int ind = 1; ind <= this.depth + 1; ind++) {
            Layer l = Game.G.getLayer(this.layer.getIndex() + ind);
            if (l == null) {
               break;
            }

            for (int i = 0; i < this.getWidth(); i++) {
               for (int j = 0; j < this.getHeight(); j++) {
                  Structure<?> s = l.getAny(this.x + i, this.y + i, true);
                  if (s != null && !(s instanceof ShaftDrillHead)) {
                     return true;
                  }
               }
            }
         }

         return false;
      }
   }

   @Override
   public void drawFrame(SpriteRenderer spriter, ShapeRenderer shaper, SpriterDelegateBatch pfxBatch) {
      super.drawFrame(spriter, shaper, pfxBatch);
      if (this.isJammed()) {
         float size = 24.0F * (1.0F + 0.3F * (MathUtils.sin(this.time * 2.0F * (float) Math.PI) * 0.5F + 0.5F));
         spriter.add(
            jammedIcon,
            (float)((this.x + this.getWidth()) * 64) - (size - 24.0F) / 2.0F - 90.0F,
            (float)((this.y + this.getHeight()) * 64) - (size - 24.0F) / 2.0F - 30.0F,
            -1.0F,
            size,
            size
         );
      }
   }

   @Override
   protected boolean additionalWorkBlockingCondition() {
      return this.isJammed();
   }

   @Override
   protected void doProductionStep() {
      Layer l = null;
      if (this.layer.getIndex() + this.depth == Game.G.getLayerCount() - 1) {
         l = Game.G.addLayer();
      } else {
         l = Game.G.getLayer(this.layer.getIndex() + this.depth + 1);
      }

      int outer = 2;

      for (int i = -outer; i < this.getWidth() + outer; i++) {
         for (int j = -outer; j < this.getHeight() + outer; j++) {
            l.removeMeta(this.x + i, this.y + j, 1);
         }
      }

      if (l.addStructure(new ShaftDrillHead(this.x, this.y).setDrill(this))) {
         this.depth++;
      }
   }

   @Override
   protected void playSfx() {
      if (!this.sfxPlaying && this.getSchema().sfx != null && Quarry.Q.sound.isPlaySound()) {
         if (this.sfxId != -1L) {
            Game.G.spatializedPlayer.resume(this.sfxId);
            this.sfxPlaying = true;
         } else {
            this.sfxId = Game.G.spatializedPlayer.play(this, this.getSchema().sfx.sound, this.getSchema().sfx.pitch, true, true);
            this.sfxPlaying = this.sfxId != -1L;
         }
      }
   }

   @Override
   protected void saveData(NBT.Builder b) {
      super.saveData(b);
      b.Int("depth", this.depth);
   }

   @Override
   protected void loadData(NBT.CompoundTag tag) throws NBT.NBTException {
      super.loadData(tag);
      this.depth = tag.Int("depth", 0);
   }
}
