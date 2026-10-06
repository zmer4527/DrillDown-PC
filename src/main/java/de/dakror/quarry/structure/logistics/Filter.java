package de.dakror.quarry.structure.logistics;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import de.dakror.common.Callback;
import de.dakror.common.libgdx.io.NBT;
import de.dakror.quarry.game.Item;
import de.dakror.quarry.scenes.Game;
import de.dakror.quarry.structure.base.Direction;
import de.dakror.quarry.structure.base.RouterStructure;
import de.dakror.quarry.structure.base.StructureType;
import de.dakror.quarry.util.Util;

public class Filter extends RouterStructure {
   public static final RouterStructure.RouterSchema classSchema = new RouterStructure.RouterSchema(
      0, StructureType.Filter, true, "filter", new Item.Items(Item.ItemType.Stone, 2, Item.ItemType.IronIngot, 2), null
   );
   protected static Group ui;
   Item.ItemType[] filters = new Item.ItemType[4];

   public Filter(int x, int y) {
      super(x, y, classSchema);
   }

   @Override
   public boolean canAccept(Item.ItemType item, int x, int y, Direction dir) {
      if (super.canAccept(item, x, y, dir)) {
         for (int i = 0; i < 4; i++) {
            if (this.tubes[i] != null && (this.filters[i] == null || this.filters[i] == item)) {
               return true;
            }
         }
      }

      return false;
   }

   @Override
   protected boolean dispatchItem() {
      boolean anyNull = false;
      boolean anyTube = false;
      if (this.currentSource == null) {
         this.currentSource = this;
      }

      for (int i = 0; i < 4; i++) {
         anyTube = true;
         if (this.filters[i] == null) {
            anyNull = true;
         }

         if (this.filters[i] == this.currentItem) {
            if (this.currentSourceDirection == Direction.values[i]) {
               return false;
            }

            return this.layer.addItemEntity(this.currentItem, this, Direction.values[i], this.currentSource);
         }
      }

      if (anyTube && anyNull) {
         for (int i = 0; i < 4; i++) {
            if (this.tubes[i] != null && this.filters[i] == null && this.layer.addItemEntity(this.currentItem, this, Direction.values[i], this.currentSource)) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private void updateUI() {
      if (!Game.isRenderThread()) {
         Game.G.postUi(this::updateUI);
         return;
      }
      int i = 0;

      for (Actor a : ui.getChildren()) {
         if (a instanceof ImageButton) {
            ImageButton ib = (ImageButton)a;
            Item.ItemType t = ((Filter)ui.getUserObject()).filters[i];
            ib.setChecked(false);
            ImageButtonStyle ibs = new ImageButtonStyle(ib.getStyle());
            ibs.imageUp = t == null ? null : t.drawable;
            ib.setStyle(ibs);
            ib.getImage().invalidateHierarchy();
            i++;
         }
      }
   }

   @Override
   public void onClick(Table content) {
      super.onClick(content);
      if (ui == null) {
         ui = Util.lml("structure-filter");
         ui.setUserObject(this);
         int i = 2;
         int j = 0;

         for (Actor a : ui.getChildren()) {
            if (a instanceof Image) {
               ((Image)a).setOrigin(a.getWidth() / 2.0F, a.getHeight() / 2.0F);
               ((Image)a).setScale(1.5F);
               ((Image)a).setRotation((float)(90 * i));
               i--;
            } else {
               final ImageButton ib = (ImageButton)a;
               ib.getImageCell().size(36.0F).center().expand();
               final int me = j++;
               final Callback<Item.ItemType> callback = new Callback<Item.ItemType>() {
                  public void call(Item.ItemType data) {
                     ((Filter)Filter.ui.getUserObject()).filters[me] = data;
                     Filter.this.updateUI();
                     Filter.this.setItemNotifications();
                  }
               };
               a.addListener(new ClickListener() {
                  public void clicked(InputEvent event, float x, float y) {
                     if (!ib.isChecked()) {
                        Game.G.ui.itemSelection.hide();
                     } else {
                        for (Actor c : Filter.ui.getChildren()) {
                           if (c instanceof ImageButton && c != ib) {
                              ((ImageButton)c).setChecked(false);
                           }
                        }

                        Game.G.ui.itemSelection.show(callback, true);
                     }
                  }
               });
            }
         }
      }

      ui.setUserObject(this);
      this.updateUI();
      content.add(ui).center().grow();
   }

   @Override
   protected void loadData(NBT.CompoundTag tag) throws NBT.NBTException {
      super.loadData(tag);
      short[] s = tag.ShortArray("filters", null);
      if (s != null) {
         for (int i = 0; i < 4; i++) {
            this.filters[i] = s[i] == 0 ? null : Item.get(s[i]);
         }
      }
   }

   @Override
   protected void saveData(NBT.Builder b) {
      super.saveData(b);
      short[] arr = new short[4];

      for (int i = 0; i < 4; i++) {
         arr[i] = this.filters[i] == null ? 0 : this.filters[i].value;
      }

      b.ShortArray("filters", arr);
   }

   @Override
   protected void copyData(int[] copyRegion, NBT.Builder b) {
      super.copyData(copyRegion, b);
      short[] arr = new short[4];

      for (int i = 0; i < 4; i++) {
         arr[i] = this.filters[i] == null ? 0 : this.filters[i].value;
      }

      b.ShortArray("filters", arr);
   }

   @Override
   protected void pasteData(int[] pasteRegion, NBT.CompoundTag tag) {
      super.pasteData(pasteRegion, tag);
      short[] s = tag.ShortArray("filters", null);
      if (s != null) {
         for (int i = 0; i < 4; i++) {
            this.filters[i] = s[i] == 0 ? null : Item.get(s[i]);
         }
      }
   }
}
