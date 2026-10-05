package de.dakror.quarry.structure.base.component;

import com.badlogic.gdx.utils.Array;
import de.dakror.common.libgdx.io.NBT;
import de.dakror.quarry.Quarry;
import de.dakror.quarry.game.Item;
import de.dakror.quarry.structure.base.Dock;
import de.dakror.quarry.structure.base.Structure;
import de.dakror.quarry.structure.logistics.Conveyor;
import de.dakror.quarry.structure.logistics.ElectricConveyorCore;
import de.dakror.quarry.util.Bounds;
import de.dakror.quarry.util.Util;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;

public class CInventory extends Component implements IStorage {
   int size;
   int count;
   int outputDock;
   HashMap<Item.ItemType, Integer> inventory = new HashMap<>();
   float pumpOutDelay = 0.5F;
   boolean isTubeAtOutput;
   float pumpOutSpeed = 0.5F;

   public CInventory() {
   }

   public CInventory(int size) {
      this(size, -1);
   }

   public CInventory(int size, int outputDock) {
      this();
      this.size = size;
      this.outputDock = outputDock;
   }

   public CInventory setPumpOutSpeed(float speed) {
      this.pumpOutSpeed = speed;
      return this;
   }

   public float getPumpOutSpeed() {
      return this.pumpOutSpeed;
   }

   @Override
   public void setSize(int size) {
      this.size = size;
   }

   @Override
   public void setOutput(int outputDock) {
      this.outputDock = outputDock;
   }

   @Override
   public int getOutput() {
      return this.outputDock;
   }

   @Override
   public void onPlacement() {
      if (this.structure.layer != null && this.outputDock > -1) {
         this.updateOutput();
      }
   }

   @Override
   public void update(double deltaTime, int gameSpeed, Bounds dirtyBounds) {
      if (this.structure.layer != null) {
         if (this.structure.layer.fake && this.size < Integer.MAX_VALUE) {
            this.size = Integer.MAX_VALUE;
         }

         if (this.outputDock != -1) {
            if (dirtyBounds.touches(this.structure)) {
               this.updateOutput();
            }

            if (gameSpeed != 0) {
               this.pumpOutDelay = (float)((double)this.pumpOutDelay - deltaTime * (double)gameSpeed);
               if (this.pumpOutDelay <= 0.0F) {
                  this.pumpOutDelay = this.pumpOutSpeed;
                  if (!this.isTubeAtOutput) {
                     return;
                  }

                  this.pumpOutItems();
               }
            }
         }
      }
   }

   protected void updateOutput() {
      Dock output = this.outputDock == -1 ? null : this.structure.getDocks()[this.outputDock];
      if (output != null) {
         Structure<?> s = this.structure.layer.getStructure(this.structure.x + output.x + output.dir.dx, this.structure.y + output.y + output.dir.dy);
         this.isTubeAtOutput = s instanceof Conveyor;
         if (s instanceof ElectricConveyorCore && ((ElectricConveyorCore)s).getDirection().isPerpendicular(output.dir)) {
            this.isTubeAtOutput = false;
         }
      }
   }

   public void clear() {
      this.inventory.clear();
      this.count = 0;
   }

   @Override
   public boolean isEmpty() {
      return this.count == 0;
   }

   @Override
   public boolean hasSpace() {
      return this.count < this.size;
   }

   @Override
   public float getFillRatio() {
      return Math.min(1.0F, (float)this.count / (float)this.size);
   }

   @Override
   public int getCount() {
      return this.count;
   }

   @Override
   public int getSize() {
      return this.size;
   }

   @Override
   public boolean add(Item.ItemType item, int amount) {
      if (this.count + amount > this.size) {
         return false;
      } else if (amount < 0) {
         return false;
      } else {
         this.addUnsafe(item, amount);
         return true;
      }
   }

   @Override
   public boolean remove(Item.ItemType item, int amount) {
      if (this.count < amount) {
         return false;
      } else {
         int i = this.get(item);
         if (i < amount) {
            return false;
         } else {
            this.count -= amount;
            int num = i - amount;
            if (num > 0) {
               this.inventory.put(item, num);
            } else {
               this.inventory.remove(item);
            }

            return true;
         }
      }
   }

   @Override
   public int removeWithRest(Item.ItemType item, int amount) {
      if (this.count == 0) {
         return amount;
      } else {
         int i = this.get(item);
         int dif = Math.min(i, amount);
         this.count -= dif;
         int num = i - dif;
         this.inventory.put(item, Math.max(0, num));
         return amount - dif;
      }
   }

   @Override
   public void addUnsafe(Item.ItemType item, int amount) {
      int i = this.get(item);
      this.count += amount;
      this.inventory.put(item, i + amount);
   }

   @Override
   public int addWithRest(Item.ItemType item, int amount) {
      if (amount < 0) {
         return amount;
      } else if (this.count == this.size) {
         return amount;
      } else {
         int dif = Math.min(amount, this.size - this.count);
         this.count += dif;
         this.inventory.put(item, this.get(item) + dif);
         return amount - dif;
      }
   }

   @Override
   public int get(Item.ItemType item) {
      Integer i = this.inventory.get(item);
      return i == null ? 0 : i;
   }

   @Override
   public Item.Items.Amount[] getSimilar(Item.ItemType item) {
      Array<Item.Items.Amount> amounts = new Array(Item.Items.Amount.class);
      int base = item.value & 255;

      for (Entry<Item.ItemType, Integer> e : this.inventory.entrySet()) {
         if ((e.getKey().value & 255) == base) {
            amounts.add(new Item.Items.Amount(e.getKey(), e.getValue()));
         }
      }

      return (Item.Items.Amount[])amounts.toArray();
   }

   @Override
   public Item.Items.Amount[] get(Item.ItemCategory cat) {
      Array<Item.Items.Amount> amounts = new Array(Item.Items.Amount.class);

      for (Entry<Item.ItemType, Integer> e : this.inventory.entrySet()) {
         if (e.getKey().categories.contains(cat)) {
            amounts.add(new Item.Items.Amount(e.getKey(), e.getValue()));
         }
      }

      return (Item.Items.Amount[])amounts.toArray();
   }

   @Override
   public int getSum(Item.ItemCategory cat) {
      int count = 0;

      for (Entry<Item.ItemType, Integer> e : this.inventory.entrySet()) {
         if (e.getKey().categories.contains(cat)) {
            count += e.getValue();
         }
      }

      return count;
   }

   public Set<Entry<Item.ItemType, Integer>> getAll() {
      return this.inventory.entrySet();
   }

   @Override
   public void saveData(NBT.Builder b) {
      b.Compound();
      Util.NBTwriteInventory(b, this.inventory);
      if (this.outputDock > -1) {
         b.Float("pumpDelay", this.pumpOutDelay).Byte("output", (byte)(this.isTubeAtOutput ? 1 : 0));
      }

      b.End();
   }

   @Override
   public void loadData(NBT.CompoundTag tag) {
      int c = 0;

      try {
         c = Util.NBTreadInventory(tag, this.inventory);
      } catch (NBT.NBTException var4) {
         Quarry.Q.pi.message(1, var4);
      }

      this.count = tag.Int("count", c);
      if (this.outputDock > -1) {
         this.pumpOutDelay = tag.Float("pumpDelay", 0.5F);
      }

      this.isTubeAtOutput = tag.Byte("output", (byte)0) == 1;
   }

   protected void pumpOutItems() {
      Dock output = this.outputDock == -1 ? null : this.structure.getDocks()[this.outputDock];
      if (output != null) {
         Iterator<Entry<Item.ItemType, Integer>> iter = this.inventory.entrySet().iterator();

         while (iter.hasNext()) {
            Entry<Item.ItemType, Integer> e = iter.next();
            if (this.structure.layer.addItemEntity(e.getKey(), this.structure, output, this.structure)) {
               this.structure.setItemNotifications();
               int v = e.getValue();
               e.setValue(v - 1);
               this.count--;
               if (e.getValue() <= 0) {
                  iter.remove();
               }
               break;
            }
         }
      }
   }

   @Override
   public Component clone() {
      return new CInventory(this.size, this.outputDock).setPumpOutSpeed(this.pumpOutSpeed);
   }

   @Override
   public boolean canAccept(Item.ItemType item) {
      return true;
   }

   @Override
   public boolean remove(Item.ItemCategory category, int amount) {
      int left = amount;
      Iterator<Entry<Item.ItemType, Integer>> iter = this.inventory.entrySet().iterator();

      while (iter.hasNext()) {
         Entry<Item.ItemType, Integer> e = iter.next();
         if (e.getKey().categories.contains(category)) {
            left = this.removeWithRest(e.getKey(), left);
            if (this.inventory.get(e.getKey()) == 0) {
               iter.remove();
            }

            if (left == 0) {
               return true;
            }
         }
      }

      return false;
   }

   @Override
   public void init() {
   }
}
