package de.dakror.quarry.structure.power;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.WindowedMean;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import de.dakror.common.libgdx.io.NBT;
import de.dakror.common.libgdx.render.SpriteRenderer;
import de.dakror.quarry.Const;
import de.dakror.quarry.Quarry;
import de.dakror.quarry.game.Item;
import de.dakror.quarry.game.Science;
import de.dakror.quarry.scenes.GameUi;
import de.dakror.quarry.structure.base.Direction;
import de.dakror.quarry.structure.base.Dock;
import de.dakror.quarry.structure.base.Schema;
import de.dakror.quarry.structure.base.Structure;
import de.dakror.quarry.structure.base.StructureType;
import de.dakror.quarry.util.Bounds;
import de.dakror.quarry.util.Sfx;
import de.dakror.quarry.util.SpriterDelegateBatch;

public class Substation extends Structure<Substation.SubstationSchema> {
   public static final TextureRegion powermeterTex = Quarry.Q.atlas.findRegion("power_meter");
   public static final Substation.SubstationSchema classSchema = new Substation.SubstationSchema(
         0,
         StructureType.Substation,
         1,
         1,
         200000,
         20,
         "power_node",
         new Item.Items(Item.ItemType.Brick, 8, Item.ItemType.BronzePlate, 10, Item.ItemType.CopperWire, 48),
         new Sfx("node" + Const.SFX_FORMAT),
         0,
         new Dock(0, 0, Direction.North, Dock.DockType.Power),
         new Dock(0, 0, Direction.East, Dock.DockType.Power),
         new Dock(0, 0, Direction.South, Dock.DockType.Power),
         new Dock(0, 0, Direction.West, Dock.DockType.Power)
      )
      .<Schema>sciences(new Science.ScienceType[]{Science.ScienceType.Electricity})
      .flags(Schema.Flags.NotRotatable);
   double powerLevel;
   private final WindowedMean powerLevelMean = new WindowedMean(60);
   private final WindowedMean powerInMean = new WindowedMean(60);
   private final WindowedMean powerOutMean = new WindowedMean(60);
   float powerInTick;
   float powerOutTick;
   Table ui;
   Container<Table> container;
   float timePassed;

   public Substation(int x, int y) {
      this(x, y, classSchema);
   }

   protected Substation(int x, int y, Substation.SubstationSchema schema) {
      super(x, y, schema);
   }

   @Override
   public void postLoad() {
      super.postLoad();
   }

   @Override
   public double acceptPower(double amount, double networkStrength) {
      double realAmount = Math.min(amount, networkStrength - (double)this.powerReceivedThisTick);
      if (realAmount <= 0.0) {
         return amount;
      } else {
         double old = this.powerLevel;
         double add = Math.min((double)this.getSchema().capacity, this.powerLevel + realAmount);
         this.powerReceivedThisTick = (float)((double)this.powerReceivedThisTick + (add - old));
         this.powerInTick = (float)((double)this.powerInTick + (add - old));
         this.powerLevel = add;
         return amount - (add - old);
      }
   }

   @Override
   public void refundPower(double power) {
      this.powerLevel += power;
      this.powerOutTick = (float)((double)this.powerOutTick - power);
   }

   @Override
   public void drawFrame(SpriteRenderer spriter, ShapeRenderer shaper, SpriterDelegateBatch pfxBatch) {
      super.drawFrame(spriter, shaper, pfxBatch);
      if (this.powerLevel > 0.0) {
         float scale = Math.min(1.0F, this.powerLevelMean.getMean() / (float)this.getSchema().capacity);
         if (scale > 0.0F) {
            spriter.add(
               (float)(this.x * 64 + 1),
               (float)(this.y * 64 + 1),
               -1.0F,
               4.0F,
               (float)(this.getHeight() * 64 - 2) * scale,
               powermeterTex.getU(),
               powermeterTex.getV2(),
               powermeterTex.getU2(),
               powermeterTex.getV2() + (powermeterTex.getV() - powermeterTex.getV2()) * scale
            );
         }
      }
   }

   @Override
   public void update(double deltaTime, int gameSpeed, Bounds dirtyBounds) {
      if (gameSpeed == 0) {
         if (this.powerLevelMean.getValueCount() == 0) {
            this.powerLevelMean.addValue((float)this.powerLevel);
            this.powerInMean.addValue(0.0F);
            this.powerOutMean.addValue(0.0F);
         } else {
            this.powerLevelMean.addValue(this.powerLevelMean.getLatest());
            this.powerInMean.addValue(this.powerInMean.getLatest());
            this.powerOutMean.addValue(this.powerOutMean.getLatest());
         }

         this.pauseSfx();
      } else {
         this.powerLevelMean.addValue((float)this.powerLevel);
         this.powerInMean.addValue(this.powerInTick);
         this.powerOutMean.addValue(this.powerOutTick);
         this.powerInTick = 0.0F;
         this.powerOutTick = 0.0F;
         if (this.powerLevel > 0.0) {
            this.playSfx();
            double old = this.powerLevel;
            this.powerLevel = Math.max(0.0, this.powerLevel - (double)this.getSchema().leakage * deltaTime * (double)gameSpeed);
            this.powerOutTick = (float)((double)this.powerOutTick + (old - this.powerLevel));
            this.powerNetwork.addOutTick(old - this.powerLevel);
         } else {
            this.pauseSfx();
         }

         if (this.timePassed >= 1.0F) {
            if (this.clicked) {
               this.updateUI();
            }

            this.timePassed = 0.0F;
         } else {
            this.timePassed = (float)((double)this.timePassed + deltaTime);
         }
      }
   }

   @Override
   protected void saveData(NBT.Builder b) {
      super.saveData(b);
      b.Double("power", this.powerLevel);
   }

   @Override
   protected void loadData(NBT.CompoundTag tag) throws NBT.NBTException {
      super.loadData(tag);

      try {
         this.powerLevel = tag.Double("power");
      } catch (NBT.NBTException var3) {
         this.powerLevel = (double)tag.Float("power", 0.0F);
      }
   }

   private void updateUI() {
      if (this.container == null) {
         this.container = new Container().fill();
      }

      if (this.ui == null) {
         this.ui = new Table();
         this.ui.defaults().left().expandX();
         this.ui.row();
         this.ui.add(GameUi.createResourceTable(32, Quarry.Q.skin, Quarry.Q.skin.getDrawable("icon_power"), "Net-IN: "));
         this.ui.add(new Label("", Quarry.Q.skin)).right();
         this.ui.row();
         this.ui.add(GameUi.createResourceTable(32, Quarry.Q.skin, Quarry.Q.skin.getDrawable("icon_power"), "IN: "));
         this.ui.add(new Label("", Quarry.Q.skin)).right();
         this.ui.row();
         this.ui.add(GameUi.createResourceTable(32, Quarry.Q.skin, Quarry.Q.skin.getDrawable("icon_power"), "STR: "));
         this.ui.add(new Label("", Quarry.Q.skin)).right();
         this.ui.row();
         this.ui.add(GameUi.createResourceTable(32, Quarry.Q.skin, Quarry.Q.skin.getDrawable("icon_power"), "OUT: "));
         this.ui.add(new Label("", Quarry.Q.skin)).right();
         this.ui.row();
         this.ui.add(GameUi.createResourceTable(32, Quarry.Q.skin, Quarry.Q.skin.getDrawable("icon_power"), "Net-OUT: "));
         this.ui.add(new Label("", Quarry.Q.skin)).right();
         this.container.setActor(this.ui);
      }

      ((Label)this.ui.getChildren().get(1)).setText(GameUi.formatPowerAmount((double)this.powerNetwork.getMeanInPerSecond()) + "/s");
      ((Label)this.ui.getChildren().get(3))
         .setText(GameUi.formatPowerAmount((double)(this.powerInMean.getMean() / this.powerNetwork.getTimeTickMean())) + "/s");
      ((Label)this.ui.getChildren().get(5)).setText(GameUi.formatPowerAmount(this.powerLevel));
      ((Label)this.ui.getChildren().get(7))
         .setText(GameUi.formatPowerAmount((double)(this.powerOutMean.getMean() / this.powerNetwork.getTimeTickMean())) + "/s");
      ((Label)this.ui.getChildren().get(9)).setText(GameUi.formatPowerAmount((double)this.powerNetwork.getMeanOutPerSecond()) + "/s");
   }

   @Override
   public void onClick(Table content) {
      super.onClick(content);
      this.updateUI();
      content.add(this.container).grow();
   }

   @Override
   public int getDonorPriority() {
      return 10;
   }

   @Override
   public int getReceiverPriority() {
      return 10;
   }

   @Override
   public double getPowerLevel() {
      return this.powerLevel;
   }

   @Override
   public double getPowerCapacity() {
      return (double)this.getSchema().capacity;
   }

   @Override
   public double requestPower(double power, double networkStrength) {
      double deducted = Math.min(power, this.powerLevel);
      this.powerLevel -= deducted;
      this.powerOutTick = (float)((double)this.powerOutTick + deducted);
      return deducted;
   }

   public static class SubstationSchema extends Schema {
      public final int capacity;
      public final int leakage;

      public SubstationSchema(
         int version,
         StructureType type,
         int width,
         int height,
         int capacity,
         int leakage,
         String tex,
         Item.Items buildCosts,
         Sfx sfx,
         int numFakeDocks,
         Dock... docks
      ) {
         super(version, type, true, width, height, tex, buildCosts, sfx, docks);
         this.capacity = capacity;
         this.leakage = leakage;
         this.powerDocks += numFakeDocks;
      }
   }
}
