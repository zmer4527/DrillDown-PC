package de.dakror.quarry.structure.storage;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.WindowedMean;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.modified.TooltipManager;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import de.dakror.common.BiCallback;
import de.dakror.common.libgdx.io.NBT;
import de.dakror.common.libgdx.render.SpriteRenderer;
import de.dakror.quarry.Const;
import de.dakror.quarry.Quarry;
import de.dakror.quarry.game.Item;
import de.dakror.quarry.game.Science;
import de.dakror.quarry.structure.base.Direction;
import de.dakror.quarry.structure.base.Dock;
import de.dakror.quarry.structure.base.ProducerStructure;
import de.dakror.quarry.structure.base.Schema;
import de.dakror.quarry.structure.base.Structure;
import de.dakror.quarry.structure.base.StructureType;
import de.dakror.quarry.structure.base.component.CInventory;
import de.dakror.quarry.structure.power.Substation;
import de.dakror.quarry.util.Bounds;
import de.dakror.quarry.util.Sfx;
import de.dakror.quarry.util.SpriterDelegateBatch;

public class DigitalStorage extends Storage {
   public static final Schema classSchema = new Schema(
         0,
         StructureType.DigitalStorage,
         true,
         3,
         3,
         "digitalstorage",
         new Item.Items(
            Item.ItemType.MachineFrame,
            20,
            Item.ItemType.AdvancedMachineFrame,
            5,
            Item.ItemType.TinPlate,
            80,
            Item.ItemType.Glass,
            80,
            Item.ItemType.CopperWire,
            100
         ),
         new Sfx("digitalstorage" + Const.SFX_FORMAT),
         new Dock(0, 1, Direction.West, Dock.DockType.ItemIn),
         new Dock(2, 1, Direction.East, Dock.DockType.ItemOut),
         new Dock(1, 2, Direction.North, Dock.DockType.Power)
      )
      .<Schema>components(new CInventory(20000).setPumpOutSpeed(0.0F))
      .<Schema>flags(Schema.Flags.ConfirmDestruction)
      .<Schema>button(new Schema.ButtonDef("icon_pump_out", "button.pump", Schema.ButtonDef.ButtonType.TempRadio, new BiCallback<Boolean, Structure<?>>() {
         public void call(Boolean on, Structure<?> data) {
            DigitalStorage st = (DigitalStorage)data;
            TooltipManager.getInstance().enabled = !on;
            st.outputSelectMode = on;

            for (Actor a : st.ui.getChildren()) {
               Table t = (Table)a;
               if (!st.outputs.contains((Item.ItemType)a.getUserObject(), true)) {
                  Drawable bg = null;
                  if (on) {
                     bg = Storage.lightBg;
                  }

                  t.setBackground(bg);
               }

               t.invalidateHierarchy();
            }
         }
      }))
      .<Schema>button(new Schema.ButtonDef("icon_destroy", "button.refund", Schema.ButtonDef.ButtonType.StateToggle, new BiCallback<Boolean, Structure<?>>() {
         public void call(Boolean on, Structure<?> data) {
            DigitalStorage st = (DigitalStorage)data;
            TooltipManager.getInstance().enabled = !on;
            st.refundStorage = on;
         }
      }))
      .sciences(Science.ScienceType.BetterStorage, Science.ScienceType.HighTech);
   static final TextureRegion fillStatus = Quarry.Q.atlas.findRegion("structure_digitalstorage_fill_status");
   public static final float POWER_PER_ITEM = 0.16175F;
   public static final float POWER_BASE = 100.0F;
   public static final float POWER_CAPACITY = 50000.0F;
   double powerLevel;
   double powerUse;
   boolean noPower;
   final WindowedMean powerLevelMean = new WindowedMean(60);
   protected int framesPassedWithPower;

   public DigitalStorage(int x, int y) {
      super(x, y, classSchema);
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
      return 50000.0;
   }

   @Override
   public boolean canAccept(Item.ItemType item, int x, int y, Direction dir) {
      return !this.noPower && super.canAccept(item, x, y, dir);
   }

   @Override
   public boolean addToInventory(Item.ItemType item, int amount, Structure<?> source) {
      return !this.noPower && super.addToInventory(item, amount, source);
   }

   @Override
   public int addToInventoryWithRest(Item.ItemType item, int amount) {
      return this.noPower ? amount : super.addToInventoryWithRest(item, amount);
   }

   @Override
   public int removeFromInventoryWithRest(Item.ItemType item, int amount) {
      return this.noPower ? amount : super.removeFromInventoryWithRest(item, amount);
   }

   @Override
   public double acceptPower(double amount, double networkStrength) {
      double realAmount = Math.min(amount, networkStrength - (double)this.powerReceivedThisTick);
      if (realAmount <= 0.0) {
         return amount;
      } else {
         double old = this.powerLevel;
         double add = Math.min(50000.0, this.powerLevel + realAmount);
         this.powerReceivedThisTick = (float)((double)this.powerReceivedThisTick + (add - old));
         this.powerLevel = add;
         return amount - (add - old);
      }
   }

   @Override
   public boolean getButtonState(int buttonIndex) {
      return buttonIndex == 1 ? this.refundStorage : false;
   }

   @Override
   public void postLoad() {
      super.postLoad();
      CInventory inv = (CInventory)this.components[0];
      this.powerUse = (double)((float)inv.getCount() * 0.16175F + 100.0F);
   }

   @Override
   public void update(double deltaTime, int gameSpeed, Bounds dirtyBounds) {
      if (gameSpeed == 0) {
         this.powerLevelMean.addValue(this.powerLevelMean.getLatest());
      } else {
         this.powerLevelMean.addValue((float)this.powerLevel);
      }

      CInventory inv = (CInventory)this.components[0];
      this.powerUse = (double)((float)inv.getCount() * 0.16175F + 100.0F);
      if (gameSpeed > 0) {
         this.noPower = this.powerLevel < this.powerUse / 2.0;
         if (!this.noPower) {
            this.playSfx();
            this.powerLevel = Math.max(0.0, this.powerLevel - this.powerUse * 60.0 * deltaTime * (double)gameSpeed);
            if (this.framesPassedWithPower == 0) {
               this.setItemNotifications();
            }

            this.framesPassedWithPower++;
            if (this.framesPassedWithPower > 10) {
               this.framesPassedWithPower = 10;
            }

            this.pumping = this.outputs.size > 0;
         } else {
            this.pauseSfx();
            this.framesPassedWithPower = 0;
            this.pumping = false;
         }
      } else {
         this.noPower = false;
      }

      super.update(deltaTime, gameSpeed, dirtyBounds);
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

      float scale = Math.min(1.0F, this.powerLevelMean.getMean() / 50000.0F);
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
   }

   @Override
   protected void drawFillStatus(SpriteRenderer spriter) {
      float rat = ((CInventory)this.components[0]).getFillRatio();
      if (rat > 0.0F) {
         float width = 76.0F;
         float height = 170.0F;
         float originX = width / 2.0F;
         float originY = height / 2.0F;
         spriter.add(
            (float)(this.x * 64) + ((float)(this.getWidth() * 64) - width) / 2.0F,
            (float)(this.y * 64) + ((float)(this.getHeight() * 64) - height) / 2.0F,
            -0.5F,
            originX,
            originY,
            width,
            height * rat,
            1.0F,
            1.0F,
            (float)(this.upDirection.rot - Direction.North.rot),
            (float)fillStatus.getRegionX(),
            (1.0F - rat) * (float)fillStatus.getRegionHeight() + (float)fillStatus.getRegionY(),
            (float)fillStatus.getRegionWidth(),
            rat * (float)fillStatus.getRegionHeight(),
            false,
            false
         );
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
      this.powerLevel = tag.Double("power");
   }
}
