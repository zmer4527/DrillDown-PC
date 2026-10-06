package de.dakror.quarry.structure.base.component;

import de.dakror.common.Callback;
import de.dakror.common.libgdx.io.NBT;
import de.dakror.quarry.game.Item;
import de.dakror.quarry.structure.base.Dock;
import de.dakror.quarry.structure.base.Structure;
import de.dakror.quarry.structure.logistics.Conveyor;
import de.dakror.quarry.structure.logistics.ElectricConveyorCore;
import de.dakror.quarry.util.Bounds;

public class CSingleInventory extends Component implements IStorage {
   Item.ItemType item;
   int count;
   int size;
   float remainingDelay;
   float pumpOutDelay;
   int outputDock;
   boolean hasTubeAtOutput;
   boolean outputEnabled;
   boolean draw;
   boolean showFullState;
   Callback<Item.ItemType> pumpOutCallback;

   public CSingleInventory(int size) {
      this(size, -1);
   }

   public CSingleInventory(int size, int outputDock) {
      this(size, outputDock, outputDock > -1);
   }

   public CSingleInventory(int size, int outputDock, boolean outputEnabled) {
      this.outputDock = outputDock;
      this.size = size;
      this.pumpOutDelay = 0.5F;
      this.remainingDelay = this.pumpOutDelay;
      this.draw = true;
      this.outputEnabled = outputEnabled;
      this.showFullState = true;
   }

   @Override
   public void setSize(int size) {
      this.size = size;
   }

   @Override
   public void setOutput(int outputDock) {
      this.outputDock = outputDock;
   }

   public CSingleInventory setPumpOutDelay(float pumpOutDelay) {
      this.pumpOutDelay = pumpOutDelay;
      this.remainingDelay = pumpOutDelay;
      return this;
   }

   public CSingleInventory setPumpOutCallback(Callback<Item.ItemType> callback) {
      this.pumpOutCallback = callback;
      return this;
   }

   public CSingleInventory setShowFullState(boolean showState) {
      this.showFullState = showState;
      return this;
   }

   @Override
   public void onPlacement() {
      if (this.structure.layer != null && this.outputDock > -1) {
         this.updateOutput();
      }
   }

   @Override
   public int getOutput() {
      return this.outputDock;
   }

   public Item.ItemType getItem() {
      return this.item;
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
   public void update(double deltaTime, int gameSpeed, Bounds dirtyBounds) {
      if (this.structure.layer != null) {
         if (this.structure.layer.fake && this.size < Integer.MAX_VALUE) {
            this.size = Integer.MAX_VALUE;
         }

         if (this.count <= 0) {
            this.item = null;
            this.count = 0;
         }

         if (this.outputDock != -1) {
            if (dirtyBounds.touches(this.structure)) {
               this.updateOutput();
            }

            if (this.outputEnabled && this.item != null && gameSpeed != 0) {
               this.remainingDelay = (float)((double)this.remainingDelay - deltaTime * (double)gameSpeed);
               if (this.remainingDelay <= 0.0F) {
                  Dock output = this.structure.getDocks()[this.outputDock];
                  this.remainingDelay = this.pumpOutDelay;
                  if (!this.hasTubeAtOutput || this.item == null) {
                     return;
                  }

                  if (this.structure.layer.addItemEntity(this.item, this.structure, output, this.structure)) {
                     this.count--;
                     if (this.pumpOutCallback != null) {
                        this.pumpOutCallback.call(this.item);
                     }

                     if (this.count <= 0) {
                        this.item = null;
                        this.count = 0;
                     }
                  }
               }
            }
         }
      }
   }

   public boolean isOutputEnabled() {
      return this.outputEnabled;
   }

   public void setOutputEnabled(boolean outputEnabled) {
      this.outputEnabled = outputEnabled;
   }

   protected void updateOutput() {
      if (this.structure.getIndex() == 50565) {
         boolean output = false;
      }

      Dock output = this.outputDock == -1 ? null : this.structure.getDocks()[this.outputDock];
      if (output != null) {
         Structure<?> s = this.structure.layer.getStructure(this.structure.x + output.x + output.dir.dx, this.structure.y + output.y + output.dir.dy);
         this.hasTubeAtOutput = s instanceof Conveyor;
         if (s instanceof ElectricConveyorCore && ((ElectricConveyorCore)s).getDirection().isPerpendicular(output.dir)) {
            this.hasTubeAtOutput = false;
         }
      }
   }

   @Override
   public void postLoad() {
      if (this.hasTubeAtOutput && this.outputEnabled) {
         this.updateOutput();
      }
   }

   @Override
   public boolean canAccept(Item.ItemType item) {
      if (this.item != null && this.item != item) {
         return false;
      } else {
         return this.count >= this.size ? false : !item.categories.contains(Item.ItemCategory.Fluid);
      }
   }

   @Override
   public int addWithRest(Item.ItemType item, int amount) {
      if (this.item != null && this.item != item) {
         return amount;
      } else if (this.count >= this.size) {
         return amount;
      } else if (item.categories.contains(Item.ItemCategory.Fluid)) {
         return amount;
      } else {
         this.item = item;
         int dif = Math.min(amount, this.size - this.count);
         this.count += dif;
         return amount - dif;
      }
   }

   @Override
   public int removeWithRest(Item.ItemType item, int amount) {
      if (this.item != null && this.item != item) {
         return amount;
      } else if (this.count - amount >= this.size) {
         return amount;
      } else if (item.categories.contains(Item.ItemCategory.Fluid)) {
         return amount;
      } else {
         this.item = item;
         int dif = Math.min(amount, this.count);
         this.removeUnsafe(dif);
         return amount - dif;
      }
   }

   public void removeUnsafe(int amount) {
      this.count -= amount;
      if (this.count <= 0) {
         this.item = null;
         this.count = 0;
      }
   }

   @Override
   public void addUnsafe(Item.ItemType item, int amount) {
      this.item = item;
      this.count += amount;
   }

   @Override
   public boolean hasSpace() {
      return this.count < this.size;
   }

   @Override
   public boolean isEmpty() {
      return this.count == 0;
   }

   @Override
   public void clearTransientState() {
      this.remainingDelay = this.pumpOutDelay;
   }

   public void saveData(NBT.Builder b) {
      b.Compound();
      if (this.item != null) {
         b.Short("item", this.item.value).Int("amount", this.count);
      }

      b.Byte("output", (byte)(this.hasTubeAtOutput ? 1 : 0)).Byte("pump", (byte)(this.outputEnabled ? 1 : 0));
      b.End();
   }

   @Override
   public void loadData(NBT.CompoundTag tag) {
      short flu = tag.Short("item", (short)0);
      if (flu != 0) {
         this.item = Item.get(flu);
         this.count = tag.Int("amount", 0);
         if (this.count == 0) {
            this.item = null;
         }
      }

      this.hasTubeAtOutput = tag.Byte("output", (byte)0) == 1;
      this.outputEnabled = tag.Byte("pump", (byte)0) == 1;
   }

   @Override
   public Component clone() {
      CSingleInventory tank = new CSingleInventory(this.size, this.outputDock, this.outputEnabled);
      tank.pumpOutDelay = this.pumpOutDelay;
      return tank;
   }

   @Override
   public float getFillRatio() {
      return (float)this.count / (float)this.size;
   }

   @Override
   public boolean add(Item.ItemType item, int amount) {
      if (this.item != null && this.item != item) {
         return false;
      } else if (this.count >= this.size) {
         return false;
      } else if (item.categories.contains(Item.ItemCategory.Fluid)) {
         return false;
      } else {
         this.item = item;
         this.count += amount;
         return true;
      }
   }

   @Override
   public boolean remove(Item.ItemType item, int amount) {
      if (amount > this.count || this.item == null) {
         return false;
      } else if (item == this.item) {
         this.count -= amount;
         if (this.count == 0) {
            this.item = null;
         }

         return true;
      } else {
         return false;
      }
   }

   @Override
   public int get(Item.ItemType item) {
      return item == this.item ? this.count : 0;
   }

   @Override
   public Item.Items.Amount[] getSimilar(Item.ItemType item) {
      return this.item != null && Item.base(item) == Item.base(this.item) ? new Item.Items.Amount[]{new Item.Items.Amount(this.item, this.count)} : null;
   }

   @Override
   public Item.Items.Amount[] get(Item.ItemCategory cat) {
      return this.item != null && this.item.categories.contains(cat) ? new Item.Items.Amount[]{new Item.Items.Amount(this.item, this.count)} : null;
   }

   @Override
   public int getSum(Item.ItemCategory cat) {
      return this.item != null && this.item.categories.contains(cat) ? this.count : 0;
   }

   @Override
   public boolean remove(Item.ItemCategory category, int amount) {
      return this.item != null && this.item.categories.contains(category) ? this.remove(this.item, amount) : false;
   }

   @Override
   public void init() {
   }
}
