package de.dakror.quarry.structure;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.WindowedMean;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import de.dakror.common.libgdx.io.NBT;
import de.dakror.common.libgdx.render.SpriteRenderer;
import de.dakror.quarry.Const;
import de.dakror.quarry.Quarry;
import de.dakror.quarry.game.Item;
import de.dakror.quarry.game.Science;
import de.dakror.quarry.scenes.Game;
import de.dakror.quarry.structure.base.Direction;
import de.dakror.quarry.structure.base.Dock;
import de.dakror.quarry.structure.base.PausableSchema;
import de.dakror.quarry.structure.base.PausableStructure;
import de.dakror.quarry.structure.base.ProducerStructure;
import de.dakror.quarry.structure.base.Schema;
import de.dakror.quarry.structure.base.Structure;
import de.dakror.quarry.structure.base.StructureType;
import de.dakror.quarry.structure.base.component.CTank;
import de.dakror.quarry.structure.power.Substation;
import de.dakror.quarry.util.Bounds;
import de.dakror.quarry.util.Sfx;
import de.dakror.quarry.util.SpriterDelegateBatch;

public class DistillationColumn extends PausableStructure<PausableSchema> {
   public static final PausableSchema classSchema = new PausableSchema(
         0,
         StructureType.DistillationColumn,
         true,
         3,
         4,
         "distillationcolumn",
         new Item.Items(
            Item.ItemType.MachineFrame, 8, Item.ItemType.Brick, 80, Item.ItemType.SteelTube, 15, Item.ItemType.CarbonBlock, 50, Item.ItemType.BronzePlate, 35
         ),
         new Sfx("column" + Const.SFX_FORMAT),
         new Dock(2, 0, Direction.East, Dock.DockType.FluidOut),
         new Dock(0, 1, Direction.West, Dock.DockType.Power),
         new Dock(1, 0, Direction.South, Dock.DockType.FluidIn, new Dock.DockFilter(Item.ItemType.IntermediateOilToColumn)),
         new Dock(0, -1, Direction.South, Dock.DockType.StackIn),
         new Dock(1, -1, Direction.South, Dock.DockType.StackIn),
         new Dock(2, -1, Direction.South, Dock.DockType.StackIn),
         new Dock(0, 4, Direction.North, Dock.DockType.StackOut),
         new Dock(1, 4, Direction.North, Dock.DockType.StackOut),
         new Dock(2, 4, Direction.North, Dock.DockType.StackOut)
      )
      .<Schema>sciences(new Science.ScienceType[]{Science.ScienceType.OilProcessing})
      .<Schema>components(new CTank(2000), new CTank(2000, 0))
      .flags(Schema.Flags.Stackable, Schema.Flags.MirroredTextureHorizontal);
   public static final DistillationColumn.DistillationRecipe recipe = new DistillationColumn.DistillationRecipe();
   static final TextureRegion top = Quarry.Q.atlas.findRegion("structure_distillationcolumn_top");
   double powerLevel;
   boolean noPower;
   final WindowedMean powerLevelMean = new WindowedMean(60);
   protected int framesPassedWithPower;
   int level = 0;
   float workingTime;
   CTank inputTank = (CTank)this.components[0];
   CTank outputTank0 = (CTank)this.components[1];
   CTank outputTank1;
   CTank outputTank2;
   DistillationColumn level1;
   DistillationColumn level2;
   protected Container<Actor> ui;

   public DistillationColumn(int x, int y) {
      super(x, y, classSchema);
      this.workingTime = -1.0F;
   }

   @Override
   public boolean canAccept(Item.ItemType item, int x, int y, Direction dir) {
      if (!item.categories.contains(Item.ItemCategory.Fluid)) {
         return false;
      } else {
         return item != Item.ItemType.IntermediateOilToColumn ? false : this.isNextToDock(x, y, dir, this.getDocks()[2]) && this.inputTank.hasSpace();
      }
   }

   @Override
   public int acceptFluid(Item.ItemType item, int amount, Structure<?> source) {
      return item == Item.ItemType.IntermediateOilToColumn ? this.inputTank.addWithRest(item, amount) : amount;
   }

   @Override
   public double acceptPower(double amount, double networkStrength) {
      if (this.sleeping) {
         return amount;
      } else {
         double realAmount = Math.min(amount, networkStrength - (double)this.powerReceivedThisTick);
         if (realAmount <= 0.0) {
            return amount;
         } else {
            double old = this.powerLevel;
            double add = Math.min(84.0, this.powerLevel + realAmount);
            this.powerReceivedThisTick = (float)((double)this.powerReceivedThisTick + (add - old));
            this.powerLevel = add;
            return amount - (add - old);
         }
      }
   }

   public int getLevels() {
      if (this.level == 0) {
         if (this.level2 != null && this.level1 != null) {
            return 3;
         } else {
            return this.level1 != null ? 2 : 1;
         }
      } else {
         Structure<?> level0 = this.layer
            .getStructure(this.x - this.getWidth() * this.level * this.upDirection.dx, this.y - this.getHeight() * this.level * this.upDirection.dy);
         return level0 != null && level0 instanceof DistillationColumn ? ((DistillationColumn)level0).getLevels() : 1;
      }
   }

   @Override
   public float getLoudness() {
      return (!this.sleeping && this.workingTime != -1.0F && !this.noPower ? 1.0F : 0.1F) * (float)this.speedScale;
   }

   @Override
   public int getReceiverPriority() {
      return 100;
   }

   @Override
   public double getPowerLevel() {
      return this.powerLevel;
   }

   @Override
   public double getPowerCapacity() {
      return 84.0;
   }

   private void updateLevels() {
      Structure<?> s = this.layer.getStructure(this.x + this.getWidth() * this.upDirection.dx, this.y + this.getHeight() * this.upDirection.dy);
      if (s instanceof DistillationColumn && s.x == this.x + this.getWidth() * this.upDirection.dx && s.y == this.y + this.getHeight() * this.upDirection.dy) {
         this.level1 = (DistillationColumn)s;
         this.outputTank1 = (CTank)s.getComponents()[1];
         this.level1.setLevel(1);
         this.level1.setDirty();
         s = this.layer.getStructure(this.x + this.getWidth() * 2 * this.upDirection.dx, this.y + this.getHeight() * 2 * this.upDirection.dy);
         if (s instanceof DistillationColumn
            && s.x == this.x + this.getWidth() * 2 * this.upDirection.dx
            && s.y == this.y + this.getHeight() * 2 * this.upDirection.dy) {
            this.level2 = (DistillationColumn)s;
            this.outputTank2 = (CTank)s.getComponents()[1];
            this.level2.setLevel(2);
            this.level2.setDirty();
         } else {
            this.level2 = null;
            this.outputTank2 = null;
         }
      } else {
         this.level1 = null;
         this.outputTank1 = null;
         this.level2 = null;
         this.outputTank2 = null;
      }
   }

   @Override
   public void postLoad() {
      super.postLoad();
      if (this.level == 0) {
         this.updateLevels();
      }
   }

   private void updateLevel(DistillationColumn level, double deltaTime, int gameSpeed) {
      if (level != null) {
         if (gameSpeed == 0) {
            level.powerLevelMean.addValue(level.powerLevelMean.getLatest());
         } else {
            level.powerLevelMean.addValue((float)level.powerLevel);
         }

         if (this.sleeping) {
            level.pauseSfx();
         } else {
            if (gameSpeed != 0 && this.workingTime != -1.0F && this.canWork()) {
               level.playSfx();
            } else {
               level.pauseSfx();
            }

            if (this.workingTime > -1.0F && gameSpeed > 0) {
               level.noPower = level.powerLevel < 84.0;
               if (!level.noPower) {
                  level.powerLevel = Math.max(0.0, level.powerLevel - (double)(84.0F * 60.0F) * deltaTime * (double)gameSpeed);
                  level.framesPassedWithPower++;
                  if (level.framesPassedWithPower > 10) {
                     level.framesPassedWithPower = 10;
                  }
               } else {
                  level.framesPassedWithPower = 0;
               }
            } else {
               level.noPower = false;
            }
         }
      }
   }

   private boolean canWork() {
      return this.inputTank.getCount() >= 1000
         && !this.noPower
         && this.outputTank0.hasSpace()
         && (this.level1 == null || this.level1.isSleeping() || !this.level1.noPower && this.outputTank1.hasSpace())
         && (this.level2 == null || this.level2.isSleeping() || !this.level2.noPower && this.outputTank2.hasSpace());
   }

   @Override
   public void update(double deltaTime, int gameSpeed, Bounds dirtyBounds) {
      super.update(deltaTime, gameSpeed, dirtyBounds);
      if (dirtyBounds.hasAnyFlag(1536)) {
         this.setLevel(0);
         Structure<?> s = this.layer.getStructure(this.x - this.getWidth() * this.upDirection.dx, this.y - this.getHeight() * this.upDirection.dy);
         if (s instanceof DistillationColumn && s.x == this.x - s.getWidth() * this.upDirection.dx && s.y == this.y - s.getHeight() * this.upDirection.dy) {
            this.setLevel(((DistillationColumn)s).getLevel() + 1);
            this.setDirty();
         }
      }

      if (this.level == 0) {
         this.updateLevel(this, deltaTime, gameSpeed);
         this.updateLevel(this.level1, deltaTime, gameSpeed);
         this.updateLevel(this.level2, deltaTime, gameSpeed);
         float powerSpeedScale = Math.max(0.0F, this.powerLevelMean.getMean() / 84.0F * 2.0F - 1.0F);
         if (this.canWork() && this.workingTime >= 0.0F) {
            this.workingTime = (float)((double)this.workingTime - deltaTime * (double)gameSpeed * (double)powerSpeedScale);
            if (this.workingTime <= 0.0F) {
               this.inputTank.removeWithRest(Item.ItemType.IntermediateOilToColumn, 1000);
               this.outputTank0.addUnsafe(Item.ItemType.IntermediateOilToRefinery, 600);
               if (this.level1 != null && !this.level1.isSleeping()) {
                  float level1PowerSpeedScale = Math.max(0.0F, this.level1.powerLevelMean.getMean() / 84.0F * 2.0F - 1.0F);
                  this.outputTank1.addUnsafe(Item.ItemType.Lubricant, (int)(200.0F * level1PowerSpeedScale));
                  if (this.level2 != null && !this.level2.isSleeping()) {
                     float level2PowerSpeedScale = Math.max(0.0F, this.level2.powerLevelMean.getMean() / 84.0F * 2.0F - 1.0F) * level1PowerSpeedScale;
                     this.outputTank2.addUnsafe(Item.ItemType.NaturalGas, (int)(50.0F * level2PowerSpeedScale));
                  }
               }

               this.workingTime = -1.0F;
            }
         }

         if (this.workingTime == -1.0F && this.inputTank.getCount() >= 1000) {
            this.workingTime = 2.0F;
         }
      }
   }

   @Override
   public void postUpdate(Bounds dirtyBounds) {
      super.postUpdate(dirtyBounds);
      if (this.level == 0 && dirtyBounds.hasFlag(1024)) {
         this.updateLevels();
      }
   }

   @Override
   public void draw(SpriteRenderer spriter) {
      if (this.getDocks().length > 0) {
         this.drawDocks(spriter);
      }

      float x = (float)(this.x * 64);
      float y = (float)(this.y * 64);
      float z = -3.0F;
      float width = (float)(this.schema.width * 64);
      float height = (float)(this.schema.height * 64);
      float originX;
      float originY;
      if (this.schema.width % 2 == 0 && this.schema.height % 2 == 0) {
         originX = (float)this.schema.width / 2.0F * 64.0F;
         originY = (float)this.schema.height / 2.0F * 64.0F;
      } else if (this.upDirection.dx != 0) {
         originX = (float)this.getHeight() / 2.0F * 64.0F;
         originY = (float)this.getWidth() / 2.0F * 64.0F;
         x += (float)(this.getWidth() - this.getHeight()) / 2.0F * 64.0F;
         y += (float)(this.getHeight() - this.getWidth()) / 2.0F * 64.0F;
      } else {
         originX = (float)this.schema.width / 2.0F * 64.0F;
         originY = (float)this.schema.height / 2.0F * 64.0F;
      }

      spriter.add(
         this.level == 2 ? top : this.getSchema().tex,
         x,
         y,
         z,
         originX,
         originY,
         width * (this.getSchema().has(Schema.Flags.MirroredTextureHorizontal) ? 0.5F : 1.0F),
         height * (this.getSchema().has(Schema.Flags.MirroredTextureHorizontal) ? 1.0F : 0.5F),
         1.0F,
         1.0F,
         (float)(this.upDirection.rot - Direction.North.rot)
      );
      spriter.add(
         this.level == 2 ? top : this.getSchema().tex,
         x,
         y,
         z,
         originX,
         originY,
         width * (this.getSchema().has(Schema.Flags.MirroredTextureHorizontal) ? 0.5F : 1.0F),
         height * (this.getSchema().has(Schema.Flags.MirroredTextureHorizontal) ? 1.0F : 0.5F),
         this.getSchema().has(Schema.Flags.MirroredTextureHorizontal) ? -1.0F : 1.0F,
         this.getSchema().has(Schema.Flags.MirroredTextureHorizontal) ? 1.0F : -1.0F,
         (float)(this.upDirection.rot - Direction.North.rot)
      );
   }

   @Override
   public void drawFrame(SpriteRenderer spriter, ShapeRenderer shaper, SpriterDelegateBatch pfxBatch) {
      super.drawFrame(spriter, shaper, pfxBatch);
      float scale = this.powerLevelMean.getMean() / 84.0F;
      if (scale > 0.0F) {
         spriter.add(
            (float)(this.x * 64 + 1),
            (float)(this.y * 64 + 1),
            -1.0F,
            4.0F,
            (float)(this.getHeight() * 64) * scale - 2.0F,
            Substation.powermeterTex.getU(),
            Substation.powermeterTex.getV2(),
            Substation.powermeterTex.getU2(),
            Substation.powermeterTex.getV2() + (Substation.powermeterTex.getV() - Substation.powermeterTex.getV2()) * scale
         );
      }

      this.drawBoostState(spriter);
      if (!this.outputTank0.hasSpace()) {
         this.drawFullState(spriter);
      }

      if (this.level == 0 && this.workingTime > 0.0F) {
         if (this.framesPassedWithPower < 10) {
            float size = 24.0F * (1.0F + 0.3F * (MathUtils.sin(this.time * 2.0F * (float) Math.PI) * 0.5F + 0.5F));
            spriter.add(
               ProducerStructure.nopowerTex,
               (float)((this.x + this.getWidth()) * 64) - (size - 24.0F) / 2.0F - 60.0F,
               (float)((this.y + this.getHeight()) * 64) - (size - 24.0F) / 2.0F - 60.0F,
               -1.0F,
               size,
               size
            );
         }

         if (this.level1 != null && this.level1.framesPassedWithPower < 10) {
            float size = 24.0F * (1.0F + 0.3F * (MathUtils.sin(this.time * 2.0F * (float) Math.PI) * 0.5F + 0.5F));
            spriter.add(
               ProducerStructure.nopowerTex,
               (float)((this.level1.x + this.getWidth()) * 64) - (size - 24.0F) / 2.0F - 90.0F,
               (float)((this.level1.y + this.getHeight()) * 64) - (size - 24.0F) / 2.0F - 30.0F,
               -1.0F,
               size,
               size
            );
         }

         if (this.level2 != null && this.level2.framesPassedWithPower < 10) {
            float size = 24.0F * (1.0F + 0.3F * (MathUtils.sin(this.time * 2.0F * (float) Math.PI) * 0.5F + 0.5F));
            spriter.add(
               ProducerStructure.nopowerTex,
               (float)((this.level2.x + this.getWidth()) * 64) - (size - 24.0F) / 2.0F - 90.0F,
               (float)((this.level2.y + this.getHeight()) * 64) - (size - 24.0F) / 2.0F - 30.0F,
               -1.0F,
               size,
               size
            );
         }

         shaper.setColor(0.0F, 0.5F, 0.0F, 1.0F);
         float progress = 1.0F - this.workingTime / 2.0F;
         shaper.rect((float)(this.x * 64), (float)(this.y * 64), progress * (float)this.getWidth() * 64.0F, 8.0F);
      }
   }

   @Override
   public void onClick(Table content) {
      super.onClick(content);
      if (this.ui == null) {
         this.ui = new Container();
         this.ui.fill();
      }

      content.add(this.ui).width(360.0F);
      this.ui.setActor(Game.G.ui.distRecipe);
   }

   @Override
   public Object clone() {
      DistillationColumn d = (DistillationColumn)super.clone();
      d.setLevel(this.level);
      return d;
   }

   public int getLevel() {
      return this.level;
   }

   public void setLevel(int level) {
      if (!Game.G.pasteMode) {
         this.level = level;
      }
   }

   @Override
   public void clearTransientState() {
      super.clearTransientState();
      this.powerLevelMean.clear();
   }

   protected void saveData(NBT.Builder b) {
      super.saveData(b);
      b.Byte("level", (byte)this.level).Double("power", this.powerLevel);
   }

   @Override
   protected void loadData(NBT.CompoundTag tag) throws NBT.NBTException {
      super.loadData(tag);
      this.level = tag.Byte("level", (byte)0);

      try {
         this.powerLevel = tag.Double("power");
      } catch (NBT.NBTException var3) {
         this.powerLevel = (double)tag.Float("power", 0.0F);
      }
   }

   @Override
   protected void copyData(int[] copyRegion, NBT.Builder b) {
      super.copyData(copyRegion, b);
      b.Byte("level", (byte)this.level);
   }

   @Override
   protected void pasteData(int[] pasteRegion, NBT.CompoundTag tag) {
      super.pasteData(pasteRegion, tag);
      this.level = tag.Byte("level", (byte)0);
   }

   public static class DistillationRecipe {
      public final float workingTime = 2.0F;
      public final int inputOil = 1000;
      public final float power = 84.0F;
      public final int outputOil = 600;
      public final int outputLubricant = 200;
      public final int outputGas = 50;
   }
}
