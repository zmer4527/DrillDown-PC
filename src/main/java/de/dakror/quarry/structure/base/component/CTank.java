package de.dakror.quarry.structure.base.component;

import de.dakror.common.libgdx.io.NBT;
import de.dakror.quarry.game.Item;
import de.dakror.quarry.structure.base.Dock;
import de.dakror.quarry.structure.base.FluidTubeStructure;
import de.dakror.quarry.structure.base.Structure;
import de.dakror.quarry.structure.logistics.Valve;
import de.dakror.quarry.util.Bounds;

public class CTank extends Component implements IStorage {
   Item.ItemType fluid;
   int count;
   int size;
   int maxOutput;
   float remainingDelay;
   float pumpOutDelay;
   int outputDock;
   Structure<?> outputTube;
   boolean hasTubeAtOutput;
   boolean outputEnabled;
   boolean draw;

   public CTank(int size) {
      this(size, -1);
   }

   public CTank(int size, int outputDock) {
      this(size, outputDock, outputDock > -1);
   }

   public CTank(int size, int outputDock, boolean outputEnabled) {
      this.outputDock = outputDock;
      this.size = size;
      this.pumpOutDelay = 0.5F;
      this.remainingDelay = this.pumpOutDelay;
      this.maxOutput = 500;
      this.draw = true;
      this.outputEnabled = outputEnabled;
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
   public void init() {
   }

   public CTank setPumpOutDelay(float pumpOutDelay) {
      this.pumpOutDelay = pumpOutDelay;
      this.remainingDelay = pumpOutDelay;
      return this;
   }

   public CTank setMaxOutput(int output) {
      this.maxOutput = output;
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

   public Item.ItemType getFluid() {
      return this.fluid;
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
            this.fluid = null;
            this.count = 0;
            this.remainingDelay = 0.0F;
         }

         if (this.outputDock != -1) {
            if (dirtyBounds.touches(this.structure)) {
               this.updateOutput();
            }

            if (this.outputEnabled && this.fluid != null && gameSpeed != 0) {
               this.remainingDelay = (float)((double)this.remainingDelay - deltaTime * (double)gameSpeed);
               if (this.remainingDelay <= 0.0F) {
                  this.remainingDelay = this.pumpOutDelay;
                  if (this.outputTube == null || this.fluid == null) {
                     return;
                  }

                  int dif = Math.min(this.maxOutput, this.count);
                  this.count = this.count - dif + this.outputTube.acceptFluid(this.fluid, dif, this.structure);
               }
            }
         }
      }
   }

   public boolean hasOutputTube() {
      return this.outputTube != null;
   }

   public boolean isOutputEnabled() {
      return this.outputEnabled;
   }

   public void setOutputEnabled(boolean outputEnabled) {
      this.outputEnabled = outputEnabled;
   }

   protected void updateOutput() {
      Dock output = this.outputDock == -1 ? null : this.structure.getDocks()[this.outputDock];
      if (output != null) {
         Structure<?> s = this.structure.layer.getStructure(this.structure.x + output.x + output.dir.dx, this.structure.y + output.y + output.dir.dy);
         this.outputTube = s instanceof FluidTubeStructure ? s : null;
         if (s instanceof Valve) {
            Valve v = (Valve)s;
            if (output.dir == v.getDirection()) {
               this.outputTube = null;
            }
         }
      }
   }

   @Override
   public void postLoad() {
      if (this.hasTubeAtOutput) {
         this.updateOutput();
      }
   }

   @Override
   public boolean canAccept(Item.ItemType item) {
      if (this.fluid != null && this.fluid != item) {
         return false;
      } else {
         return this.count >= this.size ? false : item.categories.contains(Item.ItemCategory.Fluid);
      }
   }

   @Override
   public int addWithRest(Item.ItemType item, int amount) {
      if (this.fluid != null && this.fluid != item) {
         return amount;
      } else if (this.count >= this.size) {
         return amount;
      } else if (!item.categories.contains(Item.ItemCategory.Fluid)) {
         return amount;
      } else {
         this.fluid = item;
         int dif = Math.min(amount, this.size - this.count);
         this.count += dif;
         return amount - dif;
      }
   }

   @Override
   public int removeWithRest(Item.ItemType item, int amount) {
      if (this.fluid != null && this.fluid == item) {
         if (this.count - amount >= this.size) {
            return amount;
         } else if (!item.categories.contains(Item.ItemCategory.Fluid)) {
            return amount;
         } else {
            this.fluid = item;
            int dif = Math.min(amount, this.count);
            this.removeUnsafe(dif);
            return amount - dif;
         }
      } else {
         return amount;
      }
   }

   public void removeUnsafe(int amount) {
      this.count -= amount;
      if (this.count <= 0) {
         this.fluid = null;
         this.count = 0;
      }
   }

   @Override
   public void addUnsafe(Item.ItemType item, int amount) {
      this.fluid = item;
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
      if (this.fluid != null) {
         b.Short("fluid", this.fluid.value).Int("amount", this.count);
      }

      b.Byte("output", (byte)(this.outputTube != null ? 1 : 0)).Byte("pump", (byte)(this.outputEnabled ? 1 : 0));
      b.End();
   }

   @Override
   public void loadData(NBT.CompoundTag tag) {
      short flu = tag.Short("fluid", (short)0);
      if (flu != 0) {
         this.fluid = Item.get(flu);
         this.count = tag.Int("amount", 0);
         if (this.count == 0) {
            this.fluid = null;
         }
      }

      this.hasTubeAtOutput = tag.Byte("output", (byte)0) == 1;
      this.outputEnabled = tag.Byte("pump", (byte)0) == 1;
   }

   @Override
   public Component clone() {
      CTank tank = new CTank(this.size, this.outputDock, this.outputEnabled);
      tank.maxOutput = this.maxOutput;
      tank.pumpOutDelay = this.pumpOutDelay;
      return tank;
   }

   @Override
   public float getFillRatio() {
      return (float)this.count / (float)this.size;
   }

   @Override
   public boolean add(Item.ItemType item, int amount) {
      return false;
   }

   @Override
   public boolean remove(Item.ItemType item, int amount) {
      if (amount > this.count || this.fluid == null) {
         return false;
      } else if (item == this.fluid) {
         this.count -= amount;
         if (this.count == 0) {
            this.fluid = null;
         }

         return true;
      } else {
         return false;
      }
   }

   @Override
   public int get(Item.ItemType item) {
      return item == this.fluid ? this.count : 0;
   }

   @Override
   public Item.Items.Amount[] getSimilar(Item.ItemType item) {
      return this.fluid != null && Item.base(item) == Item.base(this.fluid) ? new Item.Items.Amount[]{new Item.Items.Amount(this.fluid, this.count)} : null;
   }

   @Override
   public Item.Items.Amount[] get(Item.ItemCategory cat) {
      return this.fluid != null && this.fluid.categories.contains(cat) ? new Item.Items.Amount[]{new Item.Items.Amount(this.fluid, this.count)} : null;
   }

   @Override
   public boolean remove(Item.ItemCategory category, int amount) {
      return this.fluid != null && this.fluid.categories.contains(category) ? this.remove(this.fluid, amount) : false;
   }

   @Override
   public int getSum(Item.ItemCategory cat) {
      return this.fluid != null && this.fluid.categories.contains(cat) ? this.count : 0;
   }
}
