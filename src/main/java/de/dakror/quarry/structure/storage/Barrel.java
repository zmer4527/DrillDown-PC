package de.dakror.quarry.structure.storage;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.modified.TooltipManager;
import de.dakror.common.BiCallback;
import de.dakror.common.Callback;
import de.dakror.common.libgdx.io.NBT;
import de.dakror.common.libgdx.render.SpriteRenderer;
import de.dakror.quarry.Quarry;
import de.dakror.quarry.game.Item;
import de.dakror.quarry.game.Science;
import de.dakror.quarry.scenes.Game;
import de.dakror.quarry.structure.base.Direction;
import de.dakror.quarry.structure.base.Dock;
import de.dakror.quarry.structure.base.Schema;
import de.dakror.quarry.structure.base.StorageStructure;
import de.dakror.quarry.structure.base.Structure;
import de.dakror.quarry.structure.base.StructureType;
import de.dakror.quarry.structure.base.component.CSingleInventory;
import de.dakror.quarry.ui.ResourceAmountLabel;
import de.dakror.quarry.util.Bounds;
import de.dakror.quarry.util.SpriterDelegateBatch;

public class Barrel extends StorageStructure {
   public static Schema classSchema = new Schema(
         0,
         StructureType.Barrel,
         true,
         1,
         1,
         "barrel",
         new Item.Items(Item.ItemType.Scaffolding, 5, Item.ItemType.WoodPlank, 24),
         null,
         new Dock(0, 0, Direction.South, Dock.DockType.ItemOut),
         new Dock(0, 0, Direction.North, Dock.DockType.ItemIn)
      )
      .<Schema>components(new CSingleInventory(350, 0, false))
      .<Schema>flags(Schema.Flags.ConfirmDestruction)
      .<Schema>sciences(Science.ScienceType.BetterStorage)
      .<Schema>button(new Schema.ButtonDef("icon_pump_out", "button.pump", Schema.ButtonDef.ButtonType.StateToggle, new BiCallback<Boolean, Structure<?>>() {
         public void call(Boolean on, Structure<?> data) {
            ((CSingleInventory)((Barrel)data).components[0]).setOutputEnabled(on);
         }
      }))
      .button(new Schema.ButtonDef("icon_destroy", "button.refund", Schema.ButtonDef.ButtonType.StateToggle, new BiCallback<Boolean, Structure<?>>() {
         public void call(Boolean on, Structure<?> data) {
            Barrel st = (Barrel)data;
            TooltipManager.getInstance().enabled = !on;
            st.refundStorage = on;
         }
      }));
   ResourceAmountLabel res;
   Table ui;
   Label fl;
   Item.ItemType uiType;

   public Barrel(int x, int y) {
      super(x, y, classSchema);
      ((CSingleInventory)this.components[0]).setPumpOutCallback(new Callback<Item.ItemType>() {
         public void call(Item.ItemType i) {
            Game.G.removeResources(i, 1, false);
            Barrel.this.onItemCountChanged(i);
         }
      });
   }

   @Override
   public boolean getButtonState(int buttonIndex) {
      if (buttonIndex == 0) {
         return ((CSingleInventory)this.components[0]).isOutputEnabled();
      } else {
         return buttonIndex == 1 ? this.refundStorage : false;
      }
   }

   @Override
   public boolean canAccept(Item.ItemType item, int x, int y, Direction dir) {
      CSingleInventory inv = (CSingleInventory)this.components[0];
      return this.isNextToDock(x, y, dir, this.getDocks()[1]) ? inv.canAccept(item) : false;
   }

   @Override
   public boolean putBack(Item.ItemType item, int amount) {
      CSingleInventory inv = (CSingleInventory)this.components[0];
      return inv.getItem() != null && inv.getItem() != item ? false : super.putBack(item, amount);
   }

   @Override
   public void update(double deltaTime, int gameSpeed, Bounds dirtyBounds) {
      super.update(deltaTime, gameSpeed, dirtyBounds);
      if (this.clicked) {
         this.updateUI();
      }
   }

   @Override
   public void drawFrame(SpriteRenderer spriter, ShapeRenderer shaper, SpriterDelegateBatch pfxBatch) {
      super.drawFrame(spriter, shaper, pfxBatch);
      CSingleInventory inv = (CSingleInventory)this.components[0];
      if (!inv.isEmpty()) {
         if (inv.getItem().stackable != null) {
            float var10002 = (float)(this.x * 64 + 16 + 5);
            spriter.add(inv.getItem().stackable.icon, var10002, (float)(this.y * 64 + 16 + 5), -2.9F, 22.0F, 22.0F);
         }

         float var5 = (float)(this.x * 64 + 16);
         spriter.add(inv.getItem().icon, var5, (float)(this.y * 64 + 16), -2.9F, 32.0F, 32.0F);
      }
   }

   @Override
   public void onDestroy() {
      super.onDestroy();
      CSingleInventory inv = (CSingleInventory)this.components[0];
      if (!inv.isEmpty()) {
         Game.G.removeResources(inv.getItem(), inv.getCount(), false);
      }
   }

   private void updateUI() {
      if (!Game.isRenderThread()) {
         Game.G.postUi(this::updateUI);
         return;
      }
      if (this.ui == null) {
         this.ui = new Table();
         this.fl = new Label("", Quarry.Q.skin);
         this.fl.setAlignment(1);
      }

      CSingleInventory ci = (CSingleInventory)this.components[0];
      if (this.res != null && this.uiType == ci.getItem()) {
         this.res.setAmount((float)ci.getCount());
      } else {
         this.uiType = ci.getItem();
         this.ui.clear();
         if (this.uiType != null) {
            this.res = new ResourceAmountLabel(32, Quarry.Q.skin, this.uiType, (float)ci.getCount());
            this.ui.add(this.res).grow();
         } else {
            this.res = null;
            this.ui.add().grow();
         }

         this.ui.row();
         this.ui.add(this.fl);
      }

      this.fl.setText(ci.getCount() + " / " + ci.getSize());
   }

   @Override
   public void onClick(Table content) {
      super.onClick(content);
      this.updateUI();
      content.add(this.ui).grow();
   }

   @Override
   protected void onItemCountChanged(Item.ItemType item) {
      this.updateUI();
      this.setItemNotifications();
   }

   @Override
   protected void copyData(int[] copyRegion, NBT.Builder b) {
      super.copyData(copyRegion, b);
      b.Byte("pumping", (byte)(((CSingleInventory)this.components[0]).isOutputEnabled() ? 1 : 0));
   }

   @Override
   protected void pasteData(int[] pasteRegion, NBT.CompoundTag tag) {
      super.pasteData(pasteRegion, tag);
      ((CSingleInventory)this.components[0]).setOutputEnabled(tag.Byte("pumping", (byte)0) == 1);
   }
}
