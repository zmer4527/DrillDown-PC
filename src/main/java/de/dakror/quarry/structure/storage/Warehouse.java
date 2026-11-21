package de.dakror.quarry.structure.storage;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.modified.TooltipManager;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import de.dakror.common.BiCallback;
import de.dakror.common.libgdx.render.SpriteRenderer;
import de.dakror.quarry.Quarry;
import de.dakror.quarry.game.Item;
import de.dakror.quarry.game.Science;
import de.dakror.quarry.structure.base.Direction;
import de.dakror.quarry.structure.base.Dock;
import de.dakror.quarry.structure.base.Schema;
import de.dakror.quarry.structure.base.Structure;
import de.dakror.quarry.structure.base.StructureType;
import de.dakror.quarry.structure.base.component.CInventory;

public class Warehouse extends Storage {
   public static final Schema classSchema = new Schema(
         0,
         StructureType.Warehouse,
         true,
         10,
         4,
         "warehouse",
         new Item.Items(Item.ItemType.StoneBrick, 50, Item.ItemType.WoodPlank, 100, Item.ItemType.Scaffolding, 50),
         null,
         new Dock(0, 0, Direction.South, Dock.DockType.ItemIn),
         new Dock(9, 0, Direction.South, Dock.DockType.ItemOut)
      )
      .<Schema>components(new CInventory(10000).setPumpOutSpeed(0.25F))
      .<Schema>flags(Schema.Flags.ConfirmDestruction, Schema.Flags.MirroredTextureHorizontal, Schema.Flags.MirroredTextureVertical)
      .<Schema>button(new Schema.ButtonDef("icon_pump_out", "button.pump", Schema.ButtonDef.ButtonType.TempRadio, new BiCallback<Boolean, Structure<?>>() {
         public void call(Boolean on, Structure<?> data) {
            Warehouse st = (Warehouse)data;
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
            Warehouse st = (Warehouse)data;
            TooltipManager.getInstance().enabled = !on;
            st.refundStorage = on;
         }
      }))
      .sciences(Science.ScienceType.BetterStorage);
   static final TextureRegion fillStatus = Quarry.Q.atlas.findRegion("structure_warehouse_fill_status");

   public Warehouse(int x, int y) {
      super(x, y, classSchema);
   }

   @Override
   public boolean getButtonState(int buttonIndex) {
      return buttonIndex == 1 ? this.refundStorage : false;
   }

   @Override
   protected void drawFillStatus(SpriteRenderer spriter) {
      float rat = ((CInventory)this.components[0]).getFillRatio();
      if (rat > 0.0F) {
         float width = 232.0F;
         float height = 28.0F;
         float originX = width / 2.0F;
         float originY = height / 2.0F;
         spriter.add(
            (float)(this.x * 64) + ((float)(this.getWidth() * 64) - width) / 2.0F,
            (float)(this.y * 64) + ((float)(this.getHeight() * 64) - height) / 2.0F,
            -0.5F,
            originX,
            originY,
            width * rat,
            height,
            1.0F,
            1.0F,
            (float)(this.upDirection.rot - Direction.North.rot),
            (float)fillStatus.getRegionX(),
            (float)fillStatus.getRegionY(),
            (float)((int)(rat * (float)fillStatus.getRegionWidth())),
            (float)fillStatus.getRegionHeight(),
            false,
            false
         );
      }
   }
}
