package de.dakror.quarry.structure.storage;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.modified.TextTooltip;
import com.badlogic.gdx.scenes.scene2d.ui.modified.TooltipManager;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;
import de.dakror.common.BiCallback;
import de.dakror.common.libgdx.io.NBT;
import de.dakror.common.libgdx.render.SpriteRenderer;
import de.dakror.quarry.Quarry;
import de.dakror.quarry.game.Item;
import de.dakror.quarry.scenes.Game;
import de.dakror.quarry.scenes.GameUi;
import de.dakror.quarry.structure.base.Direction;
import de.dakror.quarry.structure.base.Dock;
import de.dakror.quarry.structure.base.Schema;
import de.dakror.quarry.structure.base.StorageStructure;
import de.dakror.quarry.structure.base.Structure;
import de.dakror.quarry.structure.base.StructureType;
import de.dakror.quarry.structure.base.component.CInventory;
import de.dakror.quarry.structure.logistics.Conveyor;
import de.dakror.quarry.structure.logistics.ElectricConveyorCore;
import de.dakror.quarry.util.Bounds;
import de.dakror.quarry.util.SpriterDelegateBatch;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.Map.Entry;

public class Storage extends StorageStructure {
   static final Drawable lightBg = Quarry.Q.skin.getDrawable("light-bg");
   static final Drawable greenBg = Quarry.Q.skin.getDrawable("green-bg");
   static final TextureRegion fillStatus = Quarry.Q.atlas.findRegion("structure_storage_fill_status");
   public static final Schema classSchema = new Schema(
         0,
         StructureType.Storage,
         true,
         3,
         2,
         "storage",
         new Item.Items(Item.ItemType.Stone, 16, Item.ItemType.StoneBrick, 2, Item.ItemType.Scaffolding, 8),
         null,
         new Dock(0, 0, Direction.South, Dock.DockType.ItemIn),
         new Dock(2, 0, Direction.South, Dock.DockType.ItemOut)
      )
      .<Schema>components(new CInventory(400))
      .<Schema>flags(Schema.Flags.ConfirmDestruction)
      .<Schema>button(new Schema.ButtonDef("icon_pump_out", "button.pump", Schema.ButtonDef.ButtonType.TempRadio, new BiCallback<Boolean, Structure<?>>() {
         public void call(Boolean on, Structure<?> data) {
            Storage st = (Storage)data;
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
      .button(new Schema.ButtonDef("icon_destroy", "button.refund", Schema.ButtonDef.ButtonType.StateToggle, new BiCallback<Boolean, Structure<?>>() {
         public void call(Boolean on, Structure<?> data) {
            Storage st = (Storage)data;
            TooltipManager.getInstance().enabled = !on;
            st.refundStorage = on;
         }
      }));
   protected HorizontalGroup ui;
   protected ScrollPane sp;
   protected Label fl;
   protected EnumMap<Item.ItemType, Table> cells;
   protected Array<Item.ItemType> outputs = new Array();
   protected boolean outputSelectMode;
   protected boolean pumping;
   protected boolean isTubeAtOutput;
   protected float pumpOutDelay;
   protected int pumpOutItemIndex;

   public Storage(int x, int y) {
      this(x, y, classSchema);
   }

   protected Storage(int x, int y, Schema schema) {
      super(x, y, schema);
      this.cells = new EnumMap<>(Item.ItemType.class);
   }

   @Override
   public void drawFrame(SpriteRenderer spriter, ShapeRenderer shaper, SpriterDelegateBatch pfxBatch) {
      super.drawFrame(spriter, shaper, pfxBatch);
      this.drawFillStatus(spriter);
   }

   protected void drawFillStatus(SpriteRenderer spriter) {
      float rat = ((CInventory)this.components[0]).getFillRatio();
      if (rat > 0.0F) {
         float width = 42.0F;
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

   @Override
   public void update(double deltaTime, int gameSpeed, Bounds dirtyBounds) {
      super.update(deltaTime, gameSpeed, dirtyBounds);
      Dock d = this.getDocks()[1];
      if (dirtyBounds.touches(this)) {
         Structure<?> s = this.layer.getStructure(this.x + d.x + d.dir.dx, this.y + d.y + d.dir.dy);
         this.isTubeAtOutput = s instanceof Conveyor;
         if (s instanceof ElectricConveyorCore && ((ElectricConveyorCore)s).getDirection().isPerpendicular(d.dir)) {
            this.isTubeAtOutput = false;
         }
      }

      if (this.pumping && !this.outputSelectMode && gameSpeed > 0) {
         this.pumpOutDelay = (float)((double)this.pumpOutDelay - deltaTime * (double)gameSpeed);
         if (this.pumpOutDelay <= 0.0F) {
            this.pumpOutDelay = ((CInventory)this.components[0]).getPumpOutSpeed();
            if (!this.isTubeAtOutput) {
               return;
            }

            this.pumpOutItems();
         }
      }
   }

   protected void pumpOutItems() {
      CInventory inv = (CInventory)this.components[0];

      for (int i = 0; i < this.outputs.size; i++) {
         int index = (this.pumpOutItemIndex + i) % this.outputs.size;
         Item.ItemType t = (Item.ItemType)this.outputs.get(index);
         if (inv.remove(t, 1)) {
            if (this.layer.addItemEntity(t, this, this.getDocks()[1], this)) {
               if (this.ui != null) {
                  this.setUIAmount(t, inv.get(t));
               }

               this.onItemCountChanged(t);
               Game.G.removeResources(t, 1, false);
               this.setItemNotifications();
               this.pumpOutItemIndex += i + 1;
               return;
            }

            inv.addUnsafe(t, 1);
         }
      }

      if (this.ui != null) {
         this.ui.getChildren().sort(new Comparator<Actor>() {
            public int compare(Actor a, Actor b) {
               return Integer.compare(((Item.ItemType)a.getUserObject()).value & 0xFF, ((Item.ItemType)b.getUserObject()).value & 0xFF);
            }
         });
      }
   }

   @Override
   public boolean canAccept(Item.ItemType item, int x, int y, Direction dir) {
      CInventory inventory = (CInventory)this.components[0];
      if (!inventory.hasSpace()) {
         return false;
      } else if (item.categories.contains(Item.ItemCategory.Fluid)) {
         return false;
      } else {
         Dock d = this.getDocks()[0];
         return this.isNextToDock(x, y, dir, d) && (d.filter == null || inventory.hasSpace() && d.filter.accepts(item));
      }
   }

   public boolean hasItemType(Item.ItemType item) {
      CInventory inventory = (CInventory)this.components[0];
      return inventory.get(item) > 0;
   }

   protected void setUIAmount(final Item.ItemType type, int amount) {
      Table t = this.cells.get(type);
      if (amount <= 0 && !this.outputs.contains(type, true)) {
         if (t != null) {
            this.ui.removeActor(t);
            this.cells.remove(type);
         }
      } else {
         if (t == null) {
            t = GameUi.createResourceTable(32, Quarry.Q.skin, type, amount + "", type);
            t.setTouchable(Touchable.enabled);
            t.addListener(new ClickListener() {
               public void clicked(InputEvent event, float x, float y) {
                  if (Storage.this.outputSelectMode) {
                     Storage.this.setOutput(type, !Storage.this.outputs.contains(type, true));
                  }
               }
            });
            ((Label)((Cell)t.getCells().get(1)).width(92.0F).height(48.0F).getActor()).setAlignment(8);
            this.ui.addActor(t);
            this.ui.invalidateHierarchy();
            this.cells.put(type, t);
            if (this.outputs.contains(type, true)) {
               t.setBackground(greenBg);
            }
         } else {
            ((Label)t.getChildren().get(1)).setText(amount + "");
            ((TextTooltip)t.getListeners().get(0)).getActor().setText(amount + " " + type.title);
         }
      }
   }

   protected void setOutput(Item.ItemType type, boolean output) {
      Table t = this.cells.get(type);
      if (output) {
         this.outputs.add(type);
      } else {
         this.outputs.removeValue(type, true);
      }

      this.pumping = this.outputs.size > 0;
      if (t != null) {
         Drawable d = lightBg;
         if (output) {
            d = greenBg;
         }

         t.setBackground(d);
      }
   }

   @Override
   public void onDestroy() {
      super.onDestroy();

      for (Entry<Item.ItemType, Integer> e : ((CInventory)this.components[0]).getAll()) {
         Game.G.removeResources(e.getKey(), e.getValue(), false);
      }
   }

   @Override
   public boolean getButtonState(int buttonIndex) {
      return buttonIndex == 1 ? this.refundStorage : false;
   }

   @Override
   protected void saveData(NBT.Builder b) {
      super.saveData(b);
      b.Byte("output", (byte)(this.isTubeAtOutput ? 1 : 0));
      if (this.pumping) {
         short[] outs = new short[this.outputs.size];

         for (int i = 0; i < outs.length; i++) {
            outs[i] = ((Item.ItemType)this.outputs.get(i)).value;
         }

         b.ShortArray("pumping", outs);
      }
   }

   @Override
   protected void loadData(NBT.CompoundTag tag) throws NBT.NBTException {
      super.loadData(tag);
      this.isTubeAtOutput = tag.Byte("output", (byte)0) == 1;
      short[] outs = tag.ShortArray("pumping", null);
      if (outs != null) {
         for (short s : outs) {
            Item.ItemType t = Item.get(s);
            if (t != null) {
               this.setOutput(t, true);
            }
         }
      }
   }

   @Override
   protected void copyData(int[] copyRegion, NBT.Builder b) {
      super.copyData(copyRegion, b);
      b.Byte("output", (byte)(this.isTubeAtOutput ? 1 : 0));
      if (this.pumping) {
         short[] outs = new short[this.outputs.size];

         for (int i = 0; i < outs.length; i++) {
            outs[i] = ((Item.ItemType)this.outputs.get(i)).value;
         }

         b.ShortArray("pumping", outs);
      }
   }

   @Override
   protected void pasteData(int[] pasteRegion, NBT.CompoundTag tag) {
      super.pasteData(pasteRegion, tag);
      this.isTubeAtOutput = tag.Byte("output", (byte)0) == 1;
      short[] outs = tag.ShortArray("pumping", null);
      if (outs != null) {
         for (short s : outs) {
            Item.ItemType t = Item.get(s);
            if (t != null) {
               this.setOutput(t, true);
            }
         }
      }
   }

   @Override
   public void onClick(Table content) {
      super.onClick(content);
      if (this.ui == null) {
         this.ui = new HorizontalGroup();
         this.ui.wrap().rowAlign(8);
         this.sp = new ScrollPane(this.ui, Quarry.Q.skin, "container");
         this.sp.addListener(new InputListener() {
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
               super.enter(event, x, y, pointer, fromActor);
               event.getStage().setScrollFocus(event.getTarget());
            }

            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
               super.exit(event, x, y, pointer, toActor);
               event.getStage().setScrollFocus(toActor);
            }
         });
         this.sp.setScrollbarsVisible(true);
         this.sp.setScrollbarsOnTop(true);
         this.sp.setFadeScrollBars(true);
         this.sp.setScrollingDisabled(true, false);
         this.fl = new Label("", Quarry.Q.skin);
         this.fl.setAlignment(1);
      }

      this.sp.setScrollX(0.0F);
      this.sp.updateVisualScroll();
      this.sp.invalidateHierarchy();
      this.sp.validate();
      CInventory inv = (CInventory)this.components[0];
      this.fl.setText(inv.getCount() + " / " + inv.getSize());

      for (Entry<Item.ItemType, Table> e : this.cells.entrySet()) {
         this.setUIAmount(e.getKey(), ((CInventory)this.components[0]).get(e.getKey()));
      }

      for (Entry<Item.ItemType, Integer> e : ((CInventory)this.components[0]).getAll()) {
         this.setUIAmount(e.getKey(), e.getValue());
      }

      for (Item.ItemType i : this.outputs) {
         this.setUIAmount(i, ((CInventory)this.components[0]).get(i));
      }

      this.ui.getChildren().sort(new Comparator<Actor>() {
         public int compare(Actor a, Actor b) {
            return Integer.compare(((Item.ItemType)a.getUserObject()).value & 0xFF, ((Item.ItemType)b.getUserObject()).value & 0xFF);
         }
      });
      content.add(this.sp).growX().top().left().expand();
      content.row();
      content.add(this.fl).growX();
      content.getStage().setScrollFocus(this.sp);
   }

   @Override
   protected void onItemCountChanged(Item.ItemType item) {
      if (this.ui != null) {
         CInventory inv = (CInventory)this.components[0];
         this.setUIAmount(item, inv.get(item));
         this.fl.setText(inv.getCount() + " / " + inv.getSize());
         this.ui.getChildren().sort(new Comparator<Actor>() {
            public int compare(Actor a, Actor b) {
               return Integer.compare(((Item.ItemType)a.getUserObject()).value & 0xFF, ((Item.ItemType)b.getUserObject()).value & 0xFF);
            }
         });
      }
   }
}
