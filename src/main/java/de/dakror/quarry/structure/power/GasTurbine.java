package de.dakror.quarry.structure.power;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Array;
import de.dakror.quarry.Const;
import de.dakror.quarry.Quarry;
import de.dakror.quarry.game.ConstantSupplyAmount;
import de.dakror.quarry.game.Item;
import de.dakror.quarry.game.Science;
import de.dakror.quarry.scenes.Game;
import de.dakror.quarry.scenes.GameUi;
import de.dakror.quarry.structure.base.Direction;
import de.dakror.quarry.structure.base.Dock;
import de.dakror.quarry.structure.base.GeneratorStructure;
import de.dakror.quarry.structure.base.RecipeList;
import de.dakror.quarry.structure.base.Schema;
import de.dakror.quarry.structure.base.Structure;
import de.dakror.quarry.structure.base.StructureType;
import de.dakror.quarry.structure.base.component.CTank;
import de.dakror.quarry.structure.base.component.Component;
import de.dakror.quarry.structure.base.component.IStorage;
import de.dakror.quarry.util.Bounds;
import de.dakror.quarry.util.Sfx;

public class GasTurbine extends GeneratorStructure {
   public static final GeneratorStructure.GeneratorSchema classSchema = new GeneratorStructure.GeneratorSchema(
         0,
         StructureType.GasTurbine,
         9,
         5,
         "gasturbine",
         new Item.Items(
            Item.ItemType.AdvancedMachineFrame,
            10,
            Item.ItemType.BronzePlate,
            200,
            Item.ItemType.Turbine,
            10,
            Item.ItemType.SteelTube,
            150,
            Item.ItemType.SteelCable,
            180
         ),
         new RecipeList() {
            @Override
            protected void init() {
               this.add(
                  new GasTurbine.GasTurbineRecipe("single", 500000, 1.1F)
                     .input(
                        new Item.Items.Amount[]{new Item.Items.Amount(Item.ItemType.NaturalGas, 1000), new ConstantSupplyAmount(Item.ItemType.Lubricant, 1500)}
                     )
               );
               this.add(
                  new GasTurbine.GasTurbineRecipe("dual", 600000, 1.75F)
                     .input(
                        new Item.Items.Amount[]{
                           new Item.Items.Amount(Item.ItemType.NaturalGas, 1000),
                           new ConstantSupplyAmount(Item.ItemType.Lubricant, 1200),
                           new Item.Items.Amount(Item.ItemType.Water, 5000)
                        }
                     )
                     .output(new Item.Items.Amount(Item.ItemType.PressurizedSteam, 9000))
               );
            }

            @Override
            public void validateRecipes(Schema schema) {
               super.validateRecipes(schema);
               this.fluidInputs.set(0, 20000);
               this.fluidInputs.set(1, 20000);
               this.fluidInputs.set(2, 20000);
            }
         },
         new Sfx("turbine" + Const.SFX_FORMAT, 2.0F),
         new Dock(0, 2, Direction.West, Dock.DockType.FluidIn, new Dock.DockFilter(Item.ItemType.NaturalGas)),
         new Dock(1, 0, Direction.South, Dock.DockType.FluidIn, new Dock.DockFilter(Item.ItemType.Lubricant)),
         new Dock(1, 4, Direction.North, Dock.DockType.FluidIn, new Dock.DockFilter(Item.ItemType.Water)),
         new Dock(4, 4, Direction.North, Dock.DockType.BigPower),
         new Dock(7, 0, Direction.South, Dock.DockType.FluidOut),
         new Dock(-1, 0, Direction.West, Dock.DockType.StackIn),
         new Dock(-1, 1, Direction.West, Dock.DockType.StackIn),
         new Dock(-1, 2, Direction.West, Dock.DockType.StackIn),
         new Dock(-1, 3, Direction.West, Dock.DockType.StackIn),
         new Dock(-1, 4, Direction.West, Dock.DockType.StackIn),
         new Dock(9, 0, Direction.East, Dock.DockType.StackOut),
         new Dock(9, 1, Direction.East, Dock.DockType.StackOut),
         new Dock(9, 2, Direction.East, Dock.DockType.StackOut),
         new Dock(9, 3, Direction.East, Dock.DockType.StackOut),
         new Dock(9, 4, Direction.East, Dock.DockType.StackOut)
      )
      .<Schema>sciences(new Science.ScienceType[]{Science.ScienceType.OilProcessing, Science.ScienceType.HighPower})
      .flags(Schema.Flags.Stackable, Schema.Flags.MirroredTextureVertical);
   int currentScaling = 1;
   boolean isLevel0;
   GasTurbine level0;
   Array<GasTurbine> levels = new Array();
   GasTurbine.InstancedScalingGasTurbineRecipe[] instancedRecipes = new GasTurbine.InstancedScalingGasTurbineRecipe[this.schema.recipeList.recipes.length];

   public GasTurbine(int x, int y) {
      super(x, y, classSchema);

      for (int i = 0; i < this.instancedRecipes.length; i++) {
         this.instancedRecipes[i] = new GasTurbine.InstancedScalingGasTurbineRecipe((GasTurbine.GasTurbineRecipe)this.schema.recipeList.recipes[i]);
      }
   }

   @Override
   protected void initInventories() {
      int in = 0;
      int fluidIn = 0;
      int out = 0;
      int comps = this.schema.getComponents().size;

      for (int i = 0; i < this.docks.length; i++) {
         Dock d = this.docks[i];
         if (d.type == Dock.DockType.FluidIn) {
            this.inputInventories[in] = new CTank(this.schema.recipeList.fluidInputs.get(fluidIn));
            in++;
            fluidIn++;
         } else if (d.type == Dock.DockType.FluidOut) {
            Component c = new CTank(this.schema.outputBuffer ? this.schema.recipeList.outputSizes.get(out) : 0, i).setMaxOutput(10000).setPumpOutDelay(0.0F);
            c.setStructure(this);
            c.init();
            this.outputInventories[out] = (IStorage)c;
            this.components[comps++] = c;
            out++;
         }
      }
   }

   @Override
   public int acceptFluid(Item.ItemType item, int amount, Structure<?> source) {
      int i = 0;

      for (Dock d : this.getDocks()) {
         if (d.type == Dock.DockType.FluidIn && d.filter != null) {
            if (this.inputInventories[i].hasSpace() && d.filter.accepts(item)) {
               int rest = this.inputInventories[i].addWithRest(item, amount);
               if (this.isClicked()) {
                  Game.G.ui.updateStructureUIInventory();
               }

               if (this.level0.activeRecipe == null) {
                  this.level0.pickRandomActiveRecipe();
               }

               return rest;
            }

            i++;
         } else if (d.type == Dock.DockType.ItemIn) {
            i++;
         }
      }

      return amount;
   }

   @Override
   protected void offerPower(double deltaTime, int gameSpeed) {
      if (this.isLevel0
         && gameSpeed != 0
         && !this.sleeping
         && this.activeRecipe != null
         && (this.hasCapacity = this.hasCapacityForProduction())
         && !this.noPower
         && this.workDelay != -1.0F
         && !this.additionalWorkBlockingCondition()) {
         double power = (double)((GeneratorStructure.GeneratorRecipe)this.activeRecipe).getPowerGeneration() * deltaTime * (double)gameSpeed;
         double sliceSize = (double)((float)Math.ceil(power / (double)this.currentScaling));

         for (GasTurbine t : this.levels) {
            double slice = Math.min(sliceSize, power);
            if (t.powerNetwork != null) {
               t.powerNetwork.offerPower(deltaTime, gameSpeed, slice, t);
            }

            power -= slice;
         }
      }
   }

   @Override
   protected void drawRecipeProgress(ShapeRenderer shaper) {
      if (this.activeRecipe != null && this.hasCapacity) {
         shaper.setColor(0.0F, 0.5F, 0.0F, 1.0F);
         shaper.set(ShapeType.Filled);
         float progress = 1.0F - this.workDelay / this.activeRecipe.workingTime;
         Direction fwd = this.getForwardDirection();
         if (fwd.dy == 0) {
            shaper.rect(
               (float)((fwd.dx > 0 ? this.x : this.x - (this.levels.size - 1) * this.getWidth()) * 64),
               (float)(this.y * 64),
               progress * (float)this.getWidth() * (float)this.currentScaling * 64.0F,
               8.0F
            );
         } else {
            shaper.rect(
               (float)(this.x * 64),
               (float)((fwd.dy > 0 ? this.y : this.y - (this.levels.size - 1) * this.getHeight()) * 64),
               8.0F,
               progress * (float)this.getHeight() * (float)this.currentScaling * 64.0F
            );
         }
      }
   }

   @Override
   protected void doProductionStep() {
      if (this.activeRecipe.getOutput() != null) {
         Item.Items.Amount steam = this.activeRecipe.getOutput().entries[0];
         int amount = steam.getAmount();
         int slice = amount / this.levels.size;

         for (GasTurbine t : this.levels) {
            int min = Math.min(slice, amount);
            amount -= min;
            t.outputInventories[0].addUnsafe(steam.getItem(), min);
         }

         if (amount > 0) {
            this.outputInventories[0].addUnsafe(steam.getItem(), amount);
         }
      }
   }

   @Override
   protected boolean hasCapacityForProduction() {
      for (GasTurbine t : this.levels) {
         if (!t.outputInventories[0].hasSpace()) {
            return false;
         }
      }

      return true;
   }

   @Override
   protected void pickRandomActiveRecipe() {
      if (!this.isLevel0) {
         this.setRecipe(-1);
      } else {
         int water = 0;
         int lubricant = 0;

         for (GasTurbine t : this.levels) {
            lubricant += t.inputInventories[1].getCount();
            water += t.inputInventories[2].getCount();
         }

         int waterNeeded = this.instancedRecipes[1].getInput().getAmount(Item.ItemType.Water);
         int lubricantNeeded = 0;
         int gasNeeded = 0;
         int recipe = 0;
         if (water >= waterNeeded) {
            lubricantNeeded = this.instancedRecipes[1].getInput().getAmount(Item.ItemType.Lubricant);
            gasNeeded = this.instancedRecipes[1].getInput().getAmount(Item.ItemType.NaturalGas);
            recipe = 1;
         } else {
            lubricantNeeded = this.instancedRecipes[0].getInput().getAmount(Item.ItemType.Lubricant);
            gasNeeded = this.instancedRecipes[0].getInput().getAmount(Item.ItemType.NaturalGas);
         }

         if (lubricant >= lubricantNeeded && this.inputInventories[0].getCount() >= gasNeeded) {
            this.inputInventories[0].remove(Item.ItemType.NaturalGas, gasNeeded);
            int lubricantSlice = (int)Math.ceil((double)((float)lubricantNeeded / (float)this.levels.size));
            int waterSlice = (int)Math.ceil((double)((float)waterNeeded / (float)this.levels.size));

            for (GasTurbine t : this.levels) {
               int lubeIn = Math.min(lubricantNeeded, lubricantSlice);
               lubricantNeeded -= lubeIn - t.inputInventories[1].removeWithRest(Item.ItemType.Lubricant, lubeIn);
               int waterIn = Math.min(waterNeeded, waterSlice);
               waterNeeded -= waterIn + t.inputInventories[2].removeWithRest(Item.ItemType.Water, waterIn);
            }

            for (GasTurbine t : this.levels) {
               if (lubricantNeeded > 0) {
                  lubricantNeeded = t.inputInventories[1].removeWithRest(Item.ItemType.Lubricant, lubricantNeeded);
               }

               if (waterNeeded > 0) {
                  waterNeeded = t.inputInventories[2].removeWithRest(Item.ItemType.Water, waterNeeded);
               }
            }

            this.setRecipe(recipe);

            for (GasTurbine t : this.levels) {
               if (t.isClicked()) {
                  Game.G.ui.updateStructureUIInventory();
               }
            }
         } else {
            this.setRecipe(-1);
         }
      }
   }

   @Override
   protected void setRecipe(int index) {
      this.setItemNotifications();
      this.activeRecipeIndex = index;
      if (index > -1) {
         this.activeRecipe = this.instancedRecipes[this.activeRecipeIndex];
         this.workDelay = this.activeRecipe.workingTime;
      } else {
         this.activeRecipe = null;
         this.activeItems = null;
      }

      this.updateUI();
   }

   @Override
   public void onPlacement(boolean fromLoading) {
      if (!fromLoading) {
         if (this.layer != null) {
            this.updateLevel();
         }

         super.onPlacement(fromLoading);
      }
   }

   @Override
   public void postLoad() {
      super.postLoad();
      this.updateLevel();
      if (this.layer != null && this.activeRecipe == null) {
         this.pickRandomActiveRecipe();
      }
   }

   @Override
   protected void playSfx() {
      super.playSfx();
      if (this.isLevel0) {
         for (GasTurbine t : this.levels) {
            if (t != this) {
               t.playSfx();
            }
         }
      }
   }

   @Override
   protected void pauseSfx() {
      super.pauseSfx();
      if (this.isLevel0) {
         for (GasTurbine t : this.levels) {
            if (t != this) {
               t.pauseSfx();
            }
         }
      }
   }

   @Override
   public void stopSfx() {
      super.stopSfx();
      if (this.isLevel0) {
         for (GasTurbine t : this.levels) {
            if (t != this) {
               t.stopSfx();
            }
         }
      }
   }

   public GasTurbine.InstancedScalingGasTurbineRecipe[] getInstancedRecipes() {
      return this.instancedRecipes;
   }

   @Override
   public void setSleeping(boolean sleeping) {
      super.setSleeping(sleeping);
      this.layer.dirtyBounds.add(this, 0);
   }

   private Structure<?> getStructureAt(int x, int y) {
      Structure<?> s = this.layer.getStructure(x, y);
      return s != null && s.x == x && s.y == y && s.getUpDirection() == this.upDirection ? s : null;
   }

   public Direction getForwardDirection() {
      return Direction.values[(this.upDirection.ordinal() - Direction.North.ordinal() + Direction.East.ordinal()) % 4];
   }

   private void updateLevel() {
      Direction dir = this.getForwardDirection();
      this.isLevel0 = !(this.getStructureAt(this.x - this.getWidth() * dir.dx, this.y - this.getHeight() * dir.dy) instanceof GasTurbine);
      this.currentScaling = 1;
      if (this.isLevel0) {
         this.level0 = this;
         this.levels.clear();
         this.levels.add(this);

         for (int i = this.schema.width; i < 64; i += this.schema.width) {
            Structure<?> s = this.getStructureAt(this.x + i * dir.dx, this.y + i * dir.dy);
            if (!(s instanceof GasTurbine)) {
               break;
            }

            this.levels.add((GasTurbine)s);
            ((GasTurbine)s).setLevel0(this);
         }
      }
   }

   public int getScaling() {
      return this.currentScaling;
   }

   @Override
   public void update(double deltaTime, int gameSpeed, Bounds dirtyBounds) {
      if (this.isLevel0) {
         if (gameSpeed > 0) {
            this.currentScaling = 0;

            for (GasTurbine t : this.levels) {
               if (!t.isSleeping()) {
                  this.currentScaling++;
               }
            }
         }

         super.update(deltaTime, gameSpeed, dirtyBounds);
      } else {
         this.gameSpeed = gameSpeed;
         if (gameSpeed > 0) {
            this.time = (float)((double)this.time + deltaTime);
            if (this.time > 1.0F) {
               this.time--;
            }
         }

         for (Component c : this.components) {
            c.update(deltaTime, gameSpeed, dirtyBounds);
         }
      }

      if (dirtyBounds.hasFlag(2048)) {
         this.updateLevel();
      }
   }

   @Override
   protected void updateUI() {
      if (this.ui != null) {
         if (this.level0.activeRecipe == null) {
            this.ui.setActor(this.waitingLabel);
         } else {
            this.ui.setActor(GameUi.renderRecipe(Quarry.Q.skin, this.level0.activeRecipe, true));
            this.timeLabel = (Label)this.ui.findActor("time");
         }

         this.ui.invalidateHierarchy();
      }
   }

   public boolean isLevel0() {
      return this.isLevel0;
   }

   public void setLevel0(GasTurbine level0) {
      this.level0 = level0;
   }

   public int getLevel() {
      return this.currentScaling;
   }

   public GasTurbine getLevel0() {
      return this.level0;
   }

   public static class GasTurbineRecipe extends GeneratorStructure.GeneratorRecipe {
      private final float scale;

      public GasTurbineRecipe(String description, int power, float scale) {
         super(description, power);
         this.scale = scale;
      }

      public float getScale() {
         return this.scale;
      }
   }

   public class InstancedScalingAmount extends Item.Items.Amount {
      public InstancedScalingAmount(Item.Items.Amount a) {
         super(a.getItem(), a.getAmount());
      }

      @Override
      public int getAmount() {
         return super.getAmount() * GasTurbine.this.currentScaling;
      }
   }

   public class InstancedScalingConstantSupplyAmount extends ConstantSupplyAmount {
      public InstancedScalingConstantSupplyAmount(ConstantSupplyAmount a) {
         super(a.getItem(), a.getAmount());
      }

      @Override
      public int getAmount() {
         return super.getAmount() * GasTurbine.this.currentScaling;
      }
   }

   public class InstancedScalingGasTurbineRecipe extends GasTurbine.GasTurbineRecipe {
      public InstancedScalingGasTurbineRecipe(GasTurbine.GasTurbineRecipe recipe) {
         super(recipe.getDescription(), recipe.getPowerGeneration(), recipe.getScale());
         Item.Items.Amount[] entries = recipe.getInput().entries;
         Item.Items.Amount[] newEntries = new Item.Items.Amount[entries.length];

         for (int i = 0; i < entries.length; i++) {
            if (entries[i] instanceof ConstantSupplyAmount) {
               newEntries[i] = GasTurbine.this.new InstancedScalingConstantSupplyAmount((ConstantSupplyAmount)entries[i]);
            } else {
               newEntries[i] = GasTurbine.this.new InstancedScalingAmount(entries[i]);
            }
         }

         this.input(newEntries);
         if (recipe.getOutput() != null) {
            entries = recipe.getOutput().entries;
            newEntries = new Item.Items.Amount[entries.length];

            for (int ix = 0; ix < entries.length; ix++) {
               newEntries[ix] = GasTurbine.this.new InstancedScalingAmount(entries[ix]);
            }

            this.output(newEntries);
         }
      }

      @Override
      public int getPowerGeneration() {
         return GasTurbine.this.currentScaling == 1
            ? super.getPowerGeneration()
            : (int)((float)super.getPowerGeneration() * this.getScale() * (float)GasTurbine.this.currentScaling);
      }
   }
}
