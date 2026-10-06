package de.dakror.quarry.structure;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import de.dakror.quarry.scenes.Game;
import de.dakror.common.libgdx.io.NBT;
import de.dakror.common.libgdx.render.SpriteRenderer;
import de.dakror.common.libgdx.ui.CropDrawable;
import de.dakror.quarry.Const;
import de.dakror.quarry.Quarry;
import de.dakror.quarry.game.Item;
import de.dakror.quarry.game.Science;
import de.dakror.quarry.scenes.GameUi;
import de.dakror.quarry.structure.base.Direction;
import de.dakror.quarry.structure.base.Dock;
import de.dakror.quarry.structure.base.PausableSchema;
import de.dakror.quarry.structure.base.PausableStructure;
import de.dakror.quarry.structure.base.Schema;
import de.dakror.quarry.structure.base.Structure;
import de.dakror.quarry.structure.base.StructureType;
import de.dakror.quarry.structure.base.component.CTank;
import de.dakror.quarry.structure.base.component.Component;
import de.dakror.quarry.structure.base.component.IStorage;
import de.dakror.quarry.util.Bounds;
import de.dakror.quarry.util.Sfx;
import de.dakror.quarry.util.SpriterDelegateBatch;

public class Boiler extends PausableStructure<PausableSchema> {
   public static final PausableSchema classSchema = new PausableSchema(
         0,
         StructureType.Boiler,
         true,
         5,
         3,
         "boiler",
         new Item.Items(Item.ItemType.CopperPlate, 35, Item.ItemType.CopperTube, 2),
         new Sfx("boiler" + Const.SFX_FORMAT),
         new Dock(2, 2, Direction.North, Dock.DockType.FluidOut),
         new Dock(0, 0, Direction.West, Dock.DockType.FluidIn, new Dock.DockFilter(Item.ItemType.Water)),
         new Dock(4, 0, Direction.East, Dock.DockType.ItemIn, new Dock.DockFilter(Item.ItemType.Charcoal, Item.ItemType.CoalOre))
      )
      .<Schema>components(new Component[]{new CTank(10000), new CTank(10000, 0).setPumpOutDelay(0.0F)})
      .sciences(Science.ScienceType.WaterUsage);
   public static final Boiler.BoilerRecipe recipe = new Boiler.BoilerRecipe();
   CTank tank = (CTank)this.components[0];
   static Table ui;
   static Widget thermo;
   static Container<Table> container;
   int heatLevel;
   float decay = 0.3F;
   float workingTime = 1.0F;

   public Boiler(int x, int y) {
      super(x, y, classSchema);
   }

   @Override
   public boolean canAccept(Item.ItemType item, int x, int y, Direction dir) {
      return item.categories.contains(Item.ItemCategory.Fluid)
            && this.isNextToDock(x, y, dir, this.getDocks()[1])
            && this.tank.hasSpace()
            && this.getDocks()[1].filter.accepts(item)
         || this.isNextToDock(x, y, dir, this.getDocks()[2]) && this.getDocks()[2].filter.accepts(item) && this.heatLevel <= 1000 - 150;
   }

   @Override
   public int acceptFluid(Item.ItemType item, int amount, Structure<?> source) {
      if (this.tank.hasSpace() && this.getDocks()[1].filter.accepts(item)) {
         int rest = this.tank.addWithRest(item, amount);
         if (this.clicked) {
            this.updateUI();
         }

         return rest;
      } else {
         return amount;
      }
   }

   @Override
   public void drawFrame(SpriteRenderer spriter, ShapeRenderer shaper, SpriterDelegateBatch pfxBatch) {
      super.drawFrame(spriter, shaper, pfxBatch);
      if (this.heatLevel > 0) {
         shaper.setColor(0.0F, 0.5F, 0.0F, 1.0F);
         float progress = 1.0F - this.workingTime / 1.0F;
         shaper.rect((float)(this.x * 64), (float)(this.y * 64), progress * (float)this.getWidth() * 64.0F, 8.0F);
      }

      this.drawBoostState(spriter);
      if (!((IStorage)this.components[1]).hasSpace()) {
         this.drawFullState(spriter);
      }
   }

   @Override
   public boolean acceptItem(Item.ItemType item, Structure<?> source, Direction dir) {
      if (this.getDocks()[2].filter.accepts(item) && this.heatLevel <= 1000 - 150) {
         this.heatLevel = Math.min(1000, this.heatLevel + 150);
         if (this.clicked) {
            this.updateUI();
         }

         return true;
      } else {
         return false;
      }
   }

   @Override
   public float getLoudness() {
      return (!this.sleeping && this.heatLevel >= 500 ? 1.0F : 0.1F) * this.getSchema().loudness * (float)this.speedScale;
   }

   @Override
   public void update(double deltaTime, int gameSpeed, Bounds dirtyBounds) {
      super.update(deltaTime, gameSpeed, dirtyBounds);
      if (gameSpeed == 0) {
         this.pauseSfx();
      } else if (!this.sleeping && this.heatLevel > 0) {
         if (this.tank.getCount() >= 3000 && this.heatLevel >= 500) {
            this.playSfx();
            this.workingTime = (float)((double)this.workingTime - deltaTime * (double)gameSpeed);
            if (this.workingTime <= 0.0F) {
               this.workingTime = 0.0F;
               if (((CTank)this.components[1]).hasSpace()) {
                  this.tank.removeWithRest(Item.ItemType.Water, 3000);
                  ((CTank)this.components[1]).addUnsafe(Item.ItemType.PressurizedSteam, 5000);
                  this.workingTime = 1.0F;
               }
            }
         } else if (this.tank.isEmpty() || this.heatLevel < 500) {
            this.pauseSfx();
         }

         this.decay = (float)((double)this.decay - deltaTime * (double)gameSpeed);
         if (this.decay <= 0.0F) {
            this.heatLevel = Math.max(0, this.heatLevel - 5 * gameSpeed);
            if (this.clicked) {
               this.updateUI();
            }

            this.setItemNotifications();
            this.decay = 0.3F;
         }
      } else {
         this.pauseSfx();
      }
   }

   private void updateUI() {
      if (!Game.isRenderThread()) {
         Game.G.postUi(this::updateUI);
         return;
      }
      if (container != null) {
         thermo.setUserObject(this);
         ((Label)((Table)ui.getChildren().get(0)).getChildren().get(1)).setText(GameUi.formatResourceAmount((float)this.tank.getCount() / 1000.0F, true) + "L");
      }
   }

   @Override
   public void onClick(Table content) {
      super.onClick(content);
      if (container == null) {
         container = new Container();
         container.fill();
         ui = new Table();
         ui.defaults().uniform().expand();
         ui.add(GameUi.createResourceTable(32, Quarry.Q.skin, "icon_water", "")).right();
         ui.add(thermo = new Widget() {
            final Scaling scale = Scaling.fill;
            final Drawable bg = Quarry.Q.skin.getDrawable("icon_thermo_outer");
            final CropDrawable fg = new CropDrawable((TextureRegionDrawable)Quarry.Q.skin.getDrawable("icon_thermo_inner"));

            public void draw(Batch batch, float parentAlpha) {
               super.draw(batch, parentAlpha);
               Vector2 v = this.scale.apply(this.bg.getMinWidth(), this.bg.getMinHeight(), this.getWidth(), this.getHeight());
               this.bg.draw(batch, this.getX() + (this.getWidth() - v.x) / 2.0F, this.getY() + (this.getHeight() - v.y) / 2.0F, v.x, v.y);
               this.fg.setHeightRatio((float)((Boiler)this.getUserObject()).heatLevel / 1000.0F);
               this.fg.draw(batch, this.getX() + (this.getWidth() - v.x) / 2.0F, this.getY() + (this.getHeight() - v.y) / 2.0F, v.x, v.y);
            }

            public float getMinWidth() {
               return this.bg.getMinWidth();
            }

            public float getMinHeight() {
               return this.bg.getMinHeight();
            }
         }).pad(20.0F).fillY().expandX();
         container.setActor(ui);
      }

      this.updateUI();
      content.add(container).grow();
   }

   @Override
   protected void saveData(NBT.Builder b) {
      super.saveData(b);
      b.Int("heat", this.heatLevel).Float("decay", this.decay).Float("workingTime", this.workingTime);
   }

   @Override
   protected void loadData(NBT.CompoundTag tag) throws NBT.NBTException {
      super.loadData(tag);
      this.heatLevel = tag.Int("heat", 0);
      this.decay = tag.Float("decay", 0.0F);
      this.workingTime = tag.Float("workingTime", 0.0F);
   }

   public static class BoilerRecipe {
      public final int maxHeat = 1000;
      public final int heatPerCoal = 150;
      public final int heatDecay = 5;
      public final int heatThreshold = 500;
      public final float heatDecayTime = 0.3F;
      public final int input = 3000;
      public final int output = 5000;
      public final float workingTime = 1.0F;
   }
}
