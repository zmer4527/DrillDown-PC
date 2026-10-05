package de.dakror.quarry.structure.base;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import de.dakror.common.libgdx.io.NBT;
import de.dakror.common.libgdx.render.SpriteRenderer;
import de.dakror.quarry.game.Item;
import de.dakror.quarry.scenes.Game;
import de.dakror.quarry.structure.base.component.IStorage;
import de.dakror.quarry.util.SpriterDelegateBatch;

public abstract class StorageStructure extends Structure<Schema> {
   protected boolean refundStorage;

   protected StorageStructure(int x, int y, Schema schema) {
      super(x, y, schema);
   }

   public boolean isEmpty() {
      return ((IStorage)this.components[0]).isEmpty();
   }

   public int getSpace() {
      return ((IStorage)this.components[0]).getSize() - ((IStorage)this.components[0]).getCount();
   }

   public boolean isRefundStorage() {
      return this.refundStorage;
   }

   public void setRefundStorage(boolean refundStorage) {
      this.refundStorage = refundStorage;
   }

   @Override
   public boolean acceptItem(Item.ItemType item, Structure<?> source, Direction dir) {
      return this.addToInventory(item, 1, source);
   }

   public boolean addToInventory(Item.ItemType item, int amount, Structure<?> source) {
      boolean result = ((IStorage)this.components[0]).add(item, amount);
      if (result) {
         if (!this.layer.fake) {
            Game.G.addResources(item, amount, false);
         }

         this.setItemNotifications();
         this.onItemCountChanged(item);
      }

      return result;
   }

   public int addToInventoryWithRest(Item.ItemType item, int amount) {
      int result = ((IStorage)this.components[0]).addWithRest(item, amount);
      this.setItemNotifications();
      this.onItemCountChanged(item);
      return result;
   }

   public int removeFromInventoryWithRest(Item.ItemType item, int amount) {
      int result = ((IStorage)this.components[0]).removeWithRest(item, amount);
      this.setItemNotifications();
      this.onItemCountChanged(item);
      return result;
   }

   @Override
   public boolean putBack(Item.ItemType item, int amount) {
      if (!this.layer.fake) {
         Game.G.addResources(item, amount, false);
      }

      ((IStorage)this.components[0]).addUnsafe(item, amount);
      this.setItemNotifications();
      return true;
   }

   protected abstract void onItemCountChanged(Item.ItemType var1);

   @Override
   public void drawFrame(SpriteRenderer spriter, ShapeRenderer shaper, SpriterDelegateBatch pfxBatch) {
      super.drawFrame(spriter, shaper, pfxBatch);
      if (!((IStorage)this.components[0]).hasSpace()) {
         this.drawFullState(spriter);
      }
   }

   @Override
   protected void loadData(NBT.CompoundTag tag) throws NBT.NBTException {
      super.loadData(tag);
      this.refundStorage = tag.Byte("refund", (byte)0) == 1;
   }

   @Override
   protected void saveData(NBT.Builder b) {
      super.saveData(b);
      b.Byte("refund", (byte)(this.refundStorage ? 1 : 0));
   }

   @Override
   protected void copyData(int[] copyRegion, NBT.Builder b) {
      super.copyData(copyRegion, b);
      b.Byte("refund", (byte)(this.refundStorage ? 1 : 0));
   }

   @Override
   protected void pasteData(int[] pasteRegion, NBT.CompoundTag tag) {
      super.pasteData(pasteRegion, tag);
      this.refundStorage = tag.Byte("refund", (byte)0) == 1;
   }
}
