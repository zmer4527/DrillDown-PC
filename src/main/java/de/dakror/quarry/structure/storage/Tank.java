package de.dakror.quarry.structure.storage;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.modified.TextTooltip;
import de.dakror.common.BiCallback;
import de.dakror.common.libgdx.io.NBT;
import de.dakror.common.libgdx.render.SpriteRenderer;
import de.dakror.quarry.Quarry;
import de.dakror.quarry.game.Item;
import de.dakror.quarry.game.Science;
import de.dakror.quarry.scenes.GameUi;
import de.dakror.quarry.structure.base.Direction;
import de.dakror.quarry.structure.base.Dock;
import de.dakror.quarry.structure.base.Schema;
import de.dakror.quarry.structure.base.Structure;
import de.dakror.quarry.structure.base.StructureType;
import de.dakror.quarry.structure.base.component.CTank;
import de.dakror.quarry.structure.base.component.IStorage;
import de.dakror.quarry.util.Bounds;
import de.dakror.quarry.util.SpriterDelegateBatch;

public class Tank extends Structure<Schema> {
   public static final Schema classSchema = new Schema(
         0,
         StructureType.Tank,
         true,
         4,
         2,
         "tank",
         new Item.Items(Item.ItemType.Scaffolding, 5, Item.ItemType.CopperPlate, 40, Item.ItemType.CopperTube, 2),
         null,
         new Dock(0, 0, Direction.West, Dock.DockType.FluidIn),
         new Dock(3, 0, Direction.East, Dock.DockType.FluidOut)
      )
      .<Schema>components(new CTank(20000, 1, false).setPumpOutDelay(0.0F).setMaxOutput(50))
      .<Schema>flags(Schema.Flags.ConfirmDestruction)
      .<Schema>button(new Schema.ButtonDef("icon_pump_out", "button.pump", Schema.ButtonDef.ButtonType.StateToggle, new BiCallback<Boolean, Structure<?>>() {
         public void call(Boolean on, Structure<?> data) {
            ((CTank)((Tank)data).components[0]).setOutputEnabled(on);
         }
      }))
      .sciences(Science.ScienceType.WaterUsage);
   Table ui;
   Container<Table> container;
   Item.ItemType uiType;

   public Tank(int x, int y) {
      super(x, y, classSchema);
   }

   protected Tank(int x, int y, Schema schema) {
      super(x, y, schema);
   }

   @Override
   public void drawFrame(SpriteRenderer spriter, ShapeRenderer shaper, SpriterDelegateBatch pfxBatch) {
      super.drawFrame(spriter, shaper, pfxBatch);
      if (!((IStorage)this.components[0]).hasSpace()) {
         this.drawFullState(spriter);
      }
   }

   @Override
   public boolean getButtonState(int buttonIndex) {
      return buttonIndex == 0 ? ((CTank)this.components[0]).isOutputEnabled() : false;
   }

   @Override
   public boolean canAccept(Item.ItemType item, int x, int y, Direction dir) {
      CTank tank = (CTank)this.components[0];
      return this.isNextToDock(x, y, dir, this.getDocks()[0]) ? tank.canAccept(item) : false;
   }

   @Override
   public int acceptFluid(Item.ItemType item, int amount, Structure<?> source) {
      if (item.categories.contains(Item.ItemCategory.IntermediateOil)) {
         item = Item.ItemType.CrudeOil;
      }

      return ((CTank)this.components[0]).addWithRest(item, amount);
   }

   @Override
   public void update(double deltaTime, int gameSpeed, Bounds dirtyBounds) {
      super.update(deltaTime, gameSpeed, dirtyBounds);
      if (this.clicked) {
         this.updateUI();
      }
   }

   private void updateUI() {
      if (this.container == null) {
         this.container = new Container();
      }

      if (this.ui != null && this.uiType == ((CTank)this.components[0]).getFluid()) {
         ((Label)this.ui.getChildren().get(1)).setText(GameUi.formatResourceAmount((float)((CTank)this.components[0]).getCount() / 1000.0F, true) + "L");
         ((TextTooltip)this.ui.getListeners().get(0))
            .getActor()
            .setText(GameUi.formatResourceAmount((float)((CTank)this.components[0]).getCount() / 1000.0F, true) + "L " + this.uiType.title);
      } else {
         this.uiType = ((CTank)this.components[0]).getFluid();
         if (this.uiType != null) {
            this.ui = GameUi.createResourceTable(
               32, Quarry.Q.skin, this.uiType, GameUi.formatResourceAmount((float)((CTank)this.components[0]).getCount() / 1000.0F, true) + "L"
            );
            this.container.setActor(this.ui);
         } else {
            this.ui = null;
            this.container.setActor(null);
         }
      }
   }

   @Override
   public void onClick(Table content) {
      super.onClick(content);
      this.updateUI();
      content.add(this.container).grow();
   }

   @Override
   protected void copyData(int[] copyRegion, NBT.Builder b) {
      super.copyData(copyRegion, b);
      b.Byte("pumping", (byte)(((CTank)this.components[0]).isOutputEnabled() ? 1 : 0));
   }

   @Override
   protected void pasteData(int[] pasteRegion, NBT.CompoundTag tag) {
      super.pasteData(pasteRegion, tag);
      ((CTank)this.components[0]).setOutputEnabled(tag.Byte("pumping", (byte)0) == 1);
   }
}
