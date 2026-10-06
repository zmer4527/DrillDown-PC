package de.dakror.quarry.structure;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.WindowedMean;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.modified.TextTooltip;
import de.dakror.common.libgdx.io.NBT;
import de.dakror.common.libgdx.render.SpriteRenderer;
import de.dakror.quarry.Const;
import de.dakror.quarry.Quarry;
import de.dakror.quarry.game.Item;
import de.dakror.quarry.game.Science;
import de.dakror.quarry.scenes.GameUi;
import de.dakror.quarry.structure.base.Direction;
import de.dakror.quarry.structure.base.Dock;
import de.dakror.quarry.structure.base.FluidTubeStructure;
import de.dakror.quarry.structure.base.PausableSchema;
import de.dakror.quarry.structure.base.PausableStructure;
import de.dakror.quarry.structure.base.ProducerStructure;
import de.dakror.quarry.structure.base.Schema;
import de.dakror.quarry.structure.base.Structure;
import de.dakror.quarry.structure.base.StructureType;
import de.dakror.quarry.structure.base.component.CTank;
import de.dakror.quarry.structure.base.component.Component;
import de.dakror.quarry.structure.logistics.Valve;
import de.dakror.quarry.structure.power.Substation;
import de.dakror.quarry.util.Bounds;
import de.dakror.quarry.util.Sfx;
import de.dakror.quarry.util.SpriterDelegateBatch;

public class Refinery extends PausableStructure<PausableSchema> {
   public static final PausableSchema classSchema = new PausableSchema(
         0,
         StructureType.Refinery,
         true,
         9,
         9,
         "refinery",
         new Item.Items(
            Item.ItemType.MachineFrame,
            15,
            Item.ItemType.SteelTube,
            60,
            Item.ItemType.CopperTube,
            100,
            Item.ItemType.CopperPlate,
            150,
            Item.ItemType.StoneBrick,
            250,
            Item.ItemType.SteelWire,
            250
         ),
         new Sfx("refinery" + Const.SFX_FORMAT),
         new Dock(8, 4, Direction.East, Dock.DockType.FluidOut),
         new Dock(0, 4, Direction.West, Dock.DockType.FluidIn, new Dock.DockFilter(Item.ItemType.CrudeOil)),
         new Dock(0, 1, Direction.West, Dock.DockType.FluidIn, new Dock.DockFilter(Item.ItemType.IntermediateOilToRefinery)),
         new Dock(1, 0, Direction.South, Dock.DockType.FluidOut),
         new Dock(1, 8, Direction.North, Dock.DockType.FluidIn, new Dock.DockFilter(Item.ItemType.IntermediateOilToRefinery)),
         new Dock(0, 7, Direction.West, Dock.DockType.FluidOut),
         new Dock(8, 7, Direction.East, Dock.DockType.FluidIn, new Dock.DockFilter(Item.ItemType.IntermediateOilToRefinery)),
         new Dock(7, 8, Direction.North, Dock.DockType.FluidOut),
         new Dock(7, 0, Direction.South, Dock.DockType.FluidIn, new Dock.DockFilter(Item.ItemType.IntermediateOilToRefinery)),
         new Dock(8, 1, Direction.East, Dock.DockType.FluidOut),
         new Dock(4, 8, Direction.North, Dock.DockType.Power)
      )
      .<Schema>components(new Component[]{new CTank(50000), new CTank(100000), new CTank(100000, 0).setPumpOutDelay(0.0F)})
      .<Schema>sciences(Science.ScienceType.OilProcessing)
      .flags(Schema.Flags.MirroredTextureHorizontal, Schema.Flags.TextureAlwaysUpright);
   public static final Refinery.RefineryRecipe recipe = new Refinery.RefineryRecipe();
   Structure<?>[] outputs;
   int enabledOutputs;
   CTank t0;
   CTank t1;
   CTank t2;
   private static final float CRUDE_OIL_PROCESSING_RATIO = 7500.0F / 10000.0F;
   private static final float OIL_PROCESSING_RATIO = 18000.0F / 24000.0F;
   float workingTime0;
   float workingTime1;
   double powerLevel;
   private final WindowedMean powerLevelMean = new WindowedMean(60);
   boolean noPower;
   protected int framesPassedWithPower;
   Table ui;
   Container<Table> container;

   public Refinery(int x, int y) {
      super(x, y, classSchema);
      this.outputs = new Structure[4];
      this.t0 = (CTank)this.components[0];
      this.t1 = (CTank)this.components[1];
      this.t2 = (CTank)this.components[2];
      this.workingTime0 = -1.0F;
      this.workingTime1 = -1.0F;
   }

   @Override
   public float getLoudness() {
      return !this.sleeping && !this.noPower
         ? ((this.workingTime0 > -1.0F ? 1.0F : 0.3F) + (this.workingTime1 > -1.0F ? 1.0F : 0.3F)) * (float)this.speedScale
         : 0.1F;
   }

   private void updateOutputs() {
      int enabled = 0;
      int i = 3;

      for (int k = 0; i < this.getDocks().length; k++) {
         Structure<?> s = this.layer
            .getStructure(this.x + this.getDocks()[i].x + this.getDocks()[i].dir.dx, this.y + this.getDocks()[i].y + this.getDocks()[i].dir.dy);
         label20:
         if (s instanceof FluidTubeStructure) {
            if (s instanceof Valve) {
               Valve v = (Valve)s;
               if (this.getDocks()[i].dir == v.getDirection()) {
                  this.outputs[k] = null;
                  break label20;
               }
            }

            this.outputs[k] = s;
            enabled++;
         } else {
            this.outputs[k] = null;
         }

         i += 2;
      }

      this.enabledOutputs = enabled;
   }

   @Override
   public void postLoad() {
      super.postLoad();
      this.updateOutputs();
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
      return 600.0;
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
            double add = Math.min(600.0, this.powerLevel + realAmount);
            this.powerReceivedThisTick = (float)((double)this.powerReceivedThisTick + (add - old));
            this.powerLevel = add;
            return amount - (add - old);
         }
      }
   }

   @Override
   public void onPlacement(boolean fromLoading) {
      super.onPlacement(fromLoading);
      if (!fromLoading && this.layer != null) {
         this.updateOutputs();
      }
   }

   @Override
   public boolean canAccept(Item.ItemType item, int x, int y, Direction dir) {
      if (!item.categories.contains(Item.ItemCategory.Fluid)) {
         return false;
      } else if (item == Item.ItemType.CrudeOil) {
         return this.isNextToDock(x, y, dir, this.getDocks()[1]) && this.t0.hasSpace();
      } else if (item != Item.ItemType.IntermediateOilToRefinery) {
         return false;
      } else {
         boolean any = false;

         for (int i = 2; i < this.getDocks().length; i += 2) {
            if (this.isNextToDock(x, y, dir, this.getDocks()[i])) {
               any = true;
               break;
            }
         }

         return !any ? false : this.t1.hasSpace();
      }
   }

   @Override
   public int acceptFluid(Item.ItemType item, int amount, Structure<?> source) {
      if (item == Item.ItemType.CrudeOil) {
         return this.t0.addWithRest(item, amount);
      } else {
         return item == Item.ItemType.IntermediateOilToRefinery ? this.t1.addWithRest(item, amount) : amount;
      }
   }

   @Override
   public void drawFrame(SpriteRenderer spriter, ShapeRenderer shaper, SpriterDelegateBatch pfxBatch) {
      super.drawFrame(spriter, shaper, pfxBatch);
      if (this.workingTime0 > 0.0F) {
         shaper.setColor(0.0F, 0.5F, 0.0F, 1.0F);
         float progress = 1.0F - this.workingTime0 / 2.0F;
         shaper.rect((float)(this.x * 64), (float)(this.y * 64), progress * (float)this.getWidth() * 64.0F, 8.0F);
      }

      if (this.workingTime1 > 0.0F) {
         shaper.setColor(0.0F, 0.5F, 0.0F, 1.0F);
         float progress = 1.0F - this.workingTime1 / 5.0F;
         shaper.rect((float)(this.x * 64), (float)(this.y * 64 + 10), progress * (float)this.getWidth() * 64.0F, 8.0F);
      }

      if ((this.workingTime0 > 0.0F || this.workingTime1 > 0.0F) && this.framesPassedWithPower < 10) {
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

      float scale = this.powerLevelMean.getMean() / 600.0F;
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
      if (this.enabledOutputs == 0 && !this.t0.isEmpty() || !this.t1.hasSpace() || !this.t2.hasSpace()) {
         this.drawFullState(spriter);
      }
   }

   @Override
   public void update(double deltaTime, int gameSpeed, Bounds dirtyBounds) {
      super.update(deltaTime, gameSpeed, dirtyBounds);
      if (gameSpeed == 0) {
         this.powerLevelMean.addValue(this.powerLevelMean.getLatest());
      } else {
         this.powerLevelMean.addValue((float)this.powerLevel);
      }

      if (dirtyBounds.touches(this)) {
         this.updateOutputs();
      }

      if (this.clicked) {
         this.updateUI();
      }

      if (this.sleeping) {
         this.pauseSfx();
      } else {
         if (gameSpeed != 0 && (this.workingTime0 != -1.0F || this.workingTime1 != -1.0F) && !this.noPower) {
            this.playSfx();
         } else {
            this.pauseSfx();
         }

         if (this.workingTime0 > -1.0F || this.workingTime1 > -1.0F && gameSpeed > 0) {
            this.noPower = this.powerLevel < 600.0;
            if (!this.noPower) {
               this.powerLevel = Math.max(0.0, this.powerLevel - (double)(600.0F * 60.0F) * deltaTime * (double)gameSpeed);
               this.framesPassedWithPower++;
               if (this.framesPassedWithPower > 10) {
                  this.framesPassedWithPower = 10;
               }
            } else {
               this.framesPassedWithPower = 0;
            }
         } else {
            this.noPower = false;
         }

         if (this.workingTime0 == -1.0F && this.t0.getCount() >= 10000) {
            this.workingTime0 = 2.0F;
         }

         float powerSpeedScale = Math.max(0.0F, this.powerLevelMean.getMean() / 600.0F * 2.0F - 1.0F);
         if (this.t0.getCount() >= 10000 && this.enabledOutputs > 0 && this.workingTime0 >= 0.0F && !this.noPower) {
            this.workingTime0 = (float)((double)this.workingTime0 - deltaTime * (double)gameSpeed * (double)powerSpeedScale);
            if (this.workingTime0 <= 0.0F) {
               int dif = Math.round(10000.0F / (float)this.enabledOutputs);

               for (int i = 0; i < this.outputs.length; i++) {
                  if (this.outputs[i] != null) {
                     this.t0
                        .removeUnsafe(
                           dif
                              - (int)(
                                 (float)this.outputs[i]
                                       .acceptFluid(
                                          Item.ItemType.IntermediateOilToColumn,
                                          (int)((float)Math.min(this.t0.getCount(), dif) * CRUDE_OIL_PROCESSING_RATIO),
                                          this
                                       )
                                    / CRUDE_OIL_PROCESSING_RATIO
                              )
                        );
                  }
               }

               this.workingTime0 = -1.0F;
            }
         }

         if (this.workingTime1 == -1.0F && this.t1.getCount() >= 24000) {
            this.workingTime1 = 5.0F;
         }

         if (this.t1.getCount() >= 24000 && this.t2.getCount() + 18000 <= this.t2.getSize() && this.workingTime1 >= 0.0F && !this.noPower) {
            this.workingTime1 = (float)((double)this.workingTime1 - deltaTime * (double)gameSpeed * (double)powerSpeedScale);
            if (this.workingTime1 <= 0.0F) {
               this.t1.removeUnsafe(24000 - (int)((float)this.t2.addWithRest(Item.ItemType.RefinedOil, 18000) / OIL_PROCESSING_RATIO));
               this.workingTime1 = -1.0F;
            }
         }
      }
   }

   private void updateUI() {
      if (this.container == null) {
         this.container = new Container();
      }

      if (this.ui == null) {
         this.ui = new Table();
         this.ui.defaults().uniform().growX().pad(10.0F);
         this.ui
            .add(
               GameUi.createResourceTable(
                  32, Quarry.Q.skin, Item.ItemType.CrudeOil, GameUi.formatResourceAmount((float)this.t0.getCount() / 1000.0F, true) + "L"
               )
            );
         this.ui.row();
         this.ui
            .add(
               GameUi.createResourceTable(
                  32, Quarry.Q.skin, Item.ItemType.IntermediateOilToRefinery, GameUi.formatResourceAmount((float)this.t1.getCount() / 1000.0F, true) + "L"
               )
            );
         this.ui.row();
         this.ui
            .add(
               GameUi.createResourceTable(
                  32, Quarry.Q.skin, Item.ItemType.RefinedOil, GameUi.formatResourceAmount((float)this.t2.getCount() / 1000.0F, true) + "L"
               )
            );
         this.ui.row();
         this.container.setActor(this.ui);
      } else {
         ((Label)((Table)this.ui.getChildren().get(0)).getChildren().get(1))
            .setText(GameUi.formatResourceAmount((float)this.t0.getCount() / 1000.0F, true) + "L");
         ((TextTooltip)((Actor)this.ui.getChildren().get(0)).getListeners().get(0))
            .getActor()
            .setText(GameUi.formatResourceAmount((float)this.t0.getCount() / 1000.0F, true) + "L " + Item.ItemType.CrudeOil.title);
         ((Label)((Table)this.ui.getChildren().get(1)).getChildren().get(1))
            .setText(GameUi.formatResourceAmount((float)this.t1.getCount() / 1000.0F, true) + "L");
         ((TextTooltip)((Actor)this.ui.getChildren().get(1)).getListeners().get(0))
            .getActor()
            .setText(GameUi.formatResourceAmount((float)this.t1.getCount() / 1000.0F, true) + "L " + Item.ItemType.IntermediateOilToRefinery.title);
         ((Label)((Table)this.ui.getChildren().get(2)).getChildren().get(1))
            .setText(GameUi.formatResourceAmount((float)this.t2.getCount() / 1000.0F, true) + "L");
         ((TextTooltip)((Actor)this.ui.getChildren().get(2)).getListeners().get(0))
            .getActor()
            .setText(GameUi.formatResourceAmount((float)this.t2.getCount() / 1000.0F, true) + "L " + Item.ItemType.RefinedOil.title);
      }
   }

   @Override
   public void onClick(Table content) {
      super.onClick(content);
      this.updateUI();
      content.add(this.container).grow();
   }

   @Override
   protected void saveData(NBT.Builder b) {
      super.saveData(b);
      b.Double("power", this.powerLevel).Float("wt0", this.workingTime0).Float("wt1", this.workingTime1);
   }

   @Override
   protected void loadData(NBT.CompoundTag tag) throws NBT.NBTException {
      super.loadData(tag);

      try {
         this.powerLevel = tag.Double("power");
      } catch (NBT.NBTException var3) {
         this.powerLevel = (double)tag.Float("power", 0.0F);
      }

      this.workingTime0 = tag.Float("wt0", -1.0F);
      this.workingTime1 = tag.Float("wt1", -1.0F);
   }

   public static class RefineryRecipe {
      public final float crudeTime = 2.0F;
      public final float refTime = 5.0F;
      public final int crudeInput = 10000;
      public final int refInput = 24000;
      public final int crudeOutput = 7500;
      public final int refOutput = 18000;
      public final float power = 600.0F;
   }
}
