package de.dakror.quarry.structure.logistics;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.WindowedMean;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import de.dakror.common.libgdx.io.NBT;
import de.dakror.common.libgdx.render.SpriteRenderer;
import de.dakror.quarry.Quarry;
import de.dakror.quarry.game.Item;
import de.dakror.quarry.game.Science;
import de.dakror.quarry.scenes.GameUi;
import de.dakror.quarry.structure.base.Direction;
import de.dakror.quarry.structure.base.Dock;
import de.dakror.quarry.structure.base.ProducerStructure;
import de.dakror.quarry.structure.base.Schema;
import de.dakror.quarry.structure.base.Structure;
import de.dakror.quarry.structure.base.StructureType;
import de.dakror.quarry.structure.power.Substation;
import de.dakror.quarry.util.Bounds;
import de.dakror.quarry.util.SpriterDelegateBatch;
import java.util.HashSet;

public class ElectricConveyorCore extends Conveyor {
   public static final Conveyor.ConveyorSchema classSchema = new Conveyor.ConveyorSchema(
         0,
         StructureType.ElectricConveyorCore,
         true,
         1,
         1,
         "electric_conveyor_core",
         new Item.Items(Item.ItemType.SteelPlate, 1, Item.ItemType.CopperWire, 4, Item.ItemType.Rotor, 1),
         null,
         new Dock(0, 0, Direction.South, Dock.DockType.Power)
      )
      .<Schema>flags(new Schema.Flags[]{Schema.Flags.NoDustEffect})
      .<Schema>sciences(Science.ScienceType.Electricity)
      .loudness(0.5F);
   public static final float POWER_CAPACITY = 1000.0F;
   public static final float POWER_USE = 5.0F;
   public static final double POWER_USE_GROW = 1.275F;
   public static final int SPEED_BOOST = 5;
   Table ui;
   Container<Table> container;
   double powerLevel;
   double powerUse;
   boolean noPower;
   final WindowedMean powerLevelMean = new WindowedMean(60);
   protected int framesPassedWithPower;
   final HashSet<Integer> tmp = new HashSet<>();
   Array<ElectricConveyor> connectedConveyors = new Array();
   int[] conveyorsIndex;

   public ElectricConveyorCore(int x, int y) {
      super(x, y, classSchema);
      this.dir = Direction.East;
   }

   public ElectricConveyorCore(int x, int y, Direction direction) {
      super(x, y, classSchema);
      this.dir = direction;
   }

   @Override
   public void onPlacement(boolean fromLoading) {
      super.onPlacement(fromLoading);
      if (!fromLoading && this.layer != null) {
         this.resetAdjacentConveyors();
         this.tmp.clear();
         this.discoverAdjacentConveyors(this, this.tmp, 0);
      }
   }

   private void resetAdjacentConveyors() {
      for (ElectricConveyor p : this.connectedConveyors) {
         p.core = null;
         this.layer.dirtyBounds.add(p, 1);
      }

      this.connectedConveyors.clear();
   }

   private void discoverAdjacentConveyors(Structure<?> of, HashSet<Integer> history, int depth) {
      if ((of instanceof ElectricConveyor && ((ElectricConveyor)of).core == null || of == this) && !history.contains(of.getX() * this.layer.height + of.getY())
         )
       {
         history.add(of.getX() * this.layer.height + of.getY());
         if (of instanceof ElectricConveyor) {
            ElectricConveyor e = (ElectricConveyor)of;
            this.connectedConveyors.add(e);
            e.core = this;
         }

         if (((Conveyor)of).structures[3] != null) {
            this.discoverAdjacentConveyors(((Conveyor)of).structures[3], history, depth + 1);
         }
      }

      this.powerUse = 5.0 * Math.pow(1.275F, (double)this.connectedConveyors.size);
      if (this.clicked) {
         this.updateUI();
      }
   }

   private void updateUI() {
      if (this.container == null) {
         this.container = new Container();
      }

      if (this.ui == null) {
         this.ui = GameUi.createResourceTable(32, Quarry.Q.skin, Quarry.Q.skin.getDrawable("icon_power"), "");
         this.container.setActor(this.ui);
      }

      ((Label)this.ui.getChildren().get(1)).setText(GameUi.formatPowerAmount(this.powerUse * 60.0) + "/s");
   }

   @Override
   public void onClick(Table content) {
      super.onClick(content);
      this.updateUI();
      content.add(this.container).grow();
   }

   @Override
   public void postLoad() {
      super.postLoad();

      for (int i : this.conveyorsIndex) {
         Structure<?> p = this.layer.getStructure(i);
         if (p instanceof ElectricConveyor) {
            this.connectedConveyors.add((ElectricConveyor)p);
            ((ElectricConveyor)p).core = this;
         }
      }

      this.powerUse = 5.0 * Math.pow(1.275F, (double)this.connectedConveyors.size);
   }

   @Override
   public void onDestroy() {
      super.onDestroy();
      this.resetAdjacentConveyors();
   }

   @Override
   public double getPowerCapacity() {
      return 1000.0;
   }

   @Override
   public int getReceiverPriority() {
      return 50;
   }

   @Override
   public double getPowerLevel() {
      return this.powerLevel;
   }

   @Override
   public double acceptPower(double amount, double networkStrength) {
      double realAmount = Math.min(amount, networkStrength - (double)this.powerReceivedThisTick);
      if (realAmount <= 0.0) {
         return amount;
      } else {
         double old = this.powerLevel;
         double add = Math.min(1000.0, this.powerLevel + realAmount);
         this.powerReceivedThisTick = (float)((double)this.powerReceivedThisTick + (add - old));
         this.powerLevel = add;
         return amount - (add - old);
      }
   }

   @Override
   public void update(double deltaTime, int gameSpeed, Bounds dirtyBounds) {
      if (gameSpeed == 0) {
         this.powerLevelMean.addValue(this.powerLevelMean.getLatest());
      } else {
         this.powerLevelMean.addValue((float)this.powerLevel);
      }

      if (gameSpeed > 0) {
         this.noPower = this.powerLevel < this.powerUse / 2.0;
         if (!this.noPower) {
            this.powerLevel = Math.max(0.0, this.powerLevel - this.powerUse * 60.0 * deltaTime * (double)gameSpeed);
            if (this.framesPassedWithPower == 0) {
               this.notifyNeighbors(true);
            }

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

      if (dirtyBounds.hasFlag(1)) {
         this.resetAdjacentConveyors();
         this.tmp.clear();
         this.discoverAdjacentConveyors(this, this.tmp, 0);
      }

      super.update(deltaTime, gameSpeed * 5, dirtyBounds);
   }

   @Override
   public void postUpdate(Bounds dirtyBounds) {
      super.postUpdate(dirtyBounds);
      if (dirtyBounds.hasFlag(1)) {
         this.resetAdjacentConveyors();
         this.tmp.clear();
         this.discoverAdjacentConveyors(this, this.tmp, 0);
      }
   }

   @Override
   public void updateItems(double deltaTime, int gameSpeed, boolean chain) {
      super.updateItems(deltaTime, this.noPower ? 0 : 5 * gameSpeed, chain);
   }

   @Override
   public boolean isItemSlotFree(int slot) {
      return this.noPower ? false : super.isItemSlotFree(slot);
   }

   @Override
   public void draw(SpriteRenderer spriter) {
      this.drawDocks(spriter);
      super.draw(spriter);
   }

   @Override
   public void drawFrame(SpriteRenderer spriter, ShapeRenderer shaper, SpriterDelegateBatch pfxBatch) {
      super.drawFrame(spriter, shaper, pfxBatch);
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

      float scale = Math.min(1.0F, this.powerLevelMean.getMean() / 1000.0F);
      if (scale > 0.0F) {
         spriter.add(
            (float)(this.x * 64 + 1),
            (float)(this.y * 64 + 1),
            -1.0F,
            4.0F,
            (float)(this.getHeight() * 64 - 2) * scale,
            Substation.powermeterTex.getU(),
            Substation.powermeterTex.getV2(),
            Substation.powermeterTex.getU2(),
            Substation.powermeterTex.getV2() + (Substation.powermeterTex.getV() - Substation.powermeterTex.getV2()) * scale
         );
      }

      if (this.clicked) {
         shaper.set(ShapeType.Filled);
         shaper.setColor(Color.PURPLE);
         shaper.rect(
            ((float)this.x + (float)this.getWidth() * 0.5F) * 64.0F - 10.0F, ((float)this.y + (float)this.getHeight() * 0.5F) * 64.0F - 10.0F, 20.0F, 20.0F
         );
         Structure<?> q = this;

         for (Structure<?> p : this.connectedConveyors) {
            shaper.rectLine(
               ((float)q.x + (float)q.getWidth() * 0.5F) * 64.0F,
               ((float)q.y + (float)q.getHeight() * 0.5F) * 64.0F,
               ((float)p.x + (float)p.getWidth() * 0.5F) * 64.0F,
               ((float)p.y + (float)p.getHeight() * 0.5F) * 64.0F,
               3.0F
            );
            shaper.rect(((float)p.x + (float)p.getWidth() * 0.5F) * 64.0F - 6.0F, ((float)p.y + (float)p.getHeight() * 0.5F) * 64.0F - 6.0F, 12.0F, 12.0F);
            q = p;
         }
      }
   }

   @Override
   public void setRotation(Direction direction) {
      super.setRotation(direction);
      this.setUpDirection(direction.prev());
   }

   @Override
   public Object clone() {
      ElectricConveyorCore core = new ElectricConveyorCore(this.x, this.y, this.dir);
      core.setUpDirection(this.upDirection);
      return core;
   }

   @Override
   protected void saveData(NBT.Builder b) {
      super.saveData(b);
      int[] arr = new int[this.connectedConveyors.size];

      for (int i = 0; i < arr.length; i++) {
         arr[i] = ((ElectricConveyor)this.connectedConveyors.get(i)).x * this.layer.height + ((ElectricConveyor)this.connectedConveyors.get(i)).y;
      }

      b.IntArray("conveyors", arr).Double("power", this.powerLevel);
   }

   @Override
   protected void loadData(NBT.CompoundTag tag) throws NBT.NBTException {
      super.loadData(tag);
      this.conveyorsIndex = tag.IntArray("conveyors", new int[0]);
      this.powerLevel = tag.Double("power");
   }
}
