package de.dakror.quarry.structure.base;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.WindowedMean;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import de.dakror.common.libgdx.io.NBT;
import de.dakror.common.libgdx.render.SpriteRenderer;
import de.dakror.quarry.Quarry;
import de.dakror.quarry.game.Item;
import de.dakror.quarry.game.SimRandom;
import de.dakror.quarry.scenes.Game;
import de.dakror.quarry.scenes.GameUi;
import de.dakror.quarry.structure.base.component.CInventory;
import de.dakror.quarry.structure.base.component.CRecipeSlotStorage;
import de.dakror.quarry.structure.base.component.CTank;
import de.dakror.quarry.structure.base.component.Component;
import de.dakror.quarry.structure.base.component.IStorage;
import de.dakror.quarry.structure.power.Substation;
import de.dakror.quarry.util.Bounds;
import de.dakror.quarry.util.Sfx;
import de.dakror.quarry.util.SpriterDelegateBatch;

public abstract class ProducerStructure extends PausableStructure<ProducerStructure.ProducerSchema> {
   protected float workDelay;
   protected int activeRecipeIndex;
   protected RecipeList.Recipe activeRecipe;
   protected final IStorage[] inputInventories;
   protected final IStorage[] outputInventories;
   protected Item.Items activeItems;
   protected boolean hasCapacity;
   protected double powerLevel;
   protected double powerCapacity;
   protected final WindowedMean powerLevelMean = new WindowedMean(60);
   protected boolean noPower;
   protected int framesPassedWithPower;
   protected Container<Actor> ui;
   protected Label timeLabel;
   protected Label waitingLabel;
   public static final TextureRegion nopowerTex = Quarry.Q.atlas.findRegion("state_nopower");

   protected ProducerStructure(int x, int y, ProducerStructure.ProducerSchema schema) {
      super(x, y, schema);
      this.inputInventories = new IStorage[schema.inputDocks];
      this.outputInventories = new IStorage[schema.outputDocks];
      this.initInventories();
   }

   protected void initInventories() {
      int in = 0;
      int fluidIn = 0;
      int out = 0;
      int comps = this.schema.components.size;

      for (int i = 0; i < this.docks.length; i++) {
         Dock d = this.docks[i];
         if (d.type == Dock.DockType.ItemIn) {
            this.inputInventories[in] = new CRecipeSlotStorage(this.schema.recipeList, in);
            in++;
         } else if (d.type == Dock.DockType.FluidIn) {
            this.inputInventories[in] = new CTank(this.schema.recipeList.fluidInputs.get(fluidIn));
            in++;
            fluidIn++;
         } else if (d.type == Dock.DockType.ItemOut) {
            Component c = new CInventory(this.schema.outputBuffer ? this.schema.recipeList.outputSizes.get(out) : 0, i);
            c.setStructure(this);
            c.init();
            this.outputInventories[out] = (IStorage)c;
            this.components[comps++] = c;
            out++;
         } else if (d.type == Dock.DockType.FluidOut) {
            Component c = new CTank(this.schema.outputBuffer ? this.schema.recipeList.outputSizes.get(out) : 0, i);
            c.setStructure(this);
            c.init();
            this.outputInventories[out] = (IStorage)c;
            this.components[comps++] = c;
            out++;
         }
      }
   }

   @Override
   public float getLoudness() {
      return (!this.sleeping && this.activeRecipe != null && this.hasCapacity && !this.noPower ? 1.0F : 0.1F)
         * this.getSchema().loudness
         * (float)this.speedScale;
   }

   @Override
   public double acceptPower(double amount, double networkStrength) {
      if (this.sleeping) {
         return amount;
      } else {
         double realAmount = Math.min(amount, networkStrength - (double)this.powerReceivedThisTick);
         if (realAmount <= 0.0) {
            return amount;
         } else {
            double old = this.powerLevel;
            double add = Math.min(this.powerCapacity, this.powerLevel + realAmount);
            this.powerReceivedThisTick = (float)((double)this.powerReceivedThisTick + (add - old));
            this.powerLevel = add;
            return amount - (add - old);
         }
      }
   }

   @Override
   public double getPowerCapacity() {
      return this.powerCapacity;
   }

   @Override
   public double getPowerLevel() {
      return this.powerLevel;
   }

   @Override
   public boolean canAccept(Item.ItemType item, int x, int y, Direction dir) {
      int i = 0;
      boolean isFluid = item.categories.contains(Item.ItemCategory.Fluid);

      for (Dock d : this.getDocks()) {
         if (d.type == Dock.DockType.ItemIn) {
            if (!isFluid && this.isNextToDock(x, y, dir, d)) {
               return this.inputInventories[i].canAccept(item) && (d.filter == null || d.filter.accepts(item));
            }

            i++;
         } else if (d.type == Dock.DockType.FluidIn && d.filter != null) {
            if (isFluid && this.isNextToDock(x, y, dir, d)) {
               CTank tank = (CTank)this.inputInventories[i];
               return (tank.getFluid() == null || tank.getFluid() == item) && this.inputInventories[i].hasSpace() && d.filter.accepts(item);
            }

            i++;
         }
      }

      return false;
   }

   @Override
   public boolean acceptItem(Item.ItemType item, Structure<?> source, Direction dir) {
      int i = 0;

      for (Dock d : this.getDocks()) {
         if (d.type == Dock.DockType.ItemIn) {
            if ((d.filter == null || d.filter.accepts(item)) && this.inputInventories[i].canAccept(item)) {
               this.inputInventories[i].add(item, 1);
               if (this.isClicked()) {
                  Game.G.postUi(() -> Game.G.ui.updateStructureUIInventory());
               }

               if (this.activeRecipe == null) {
                  this.pickRandomActiveRecipe();
               }

               return true;
            }

            i++;
         } else if (d.type == Dock.DockType.FluidIn) {
            i++;
         }
      }

      return false;
   }

   @Override
   public int acceptFluid(Item.ItemType item, int amount, Structure<?> source) {
      int i = 0;

      for (Dock d : this.getDocks()) {
         if (d.type == Dock.DockType.FluidIn && d.filter != null) {
            if (this.inputInventories[i].hasSpace() && d.filter.accepts(item)) {
               int rest = this.inputInventories[i].addWithRest(item, amount);
               if (this.isClicked()) {
                  Game.G.postUi(() -> Game.G.ui.updateStructureUIInventory());
               }

               if (this.activeRecipe == null) {
                  this.pickRandomActiveRecipe();
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

   public IStorage[] getInputInventories() {
      return this.inputInventories;
   }

   public boolean isNoPower() {
      return this.noPower;
   }

   @Override
   public void onClick(Table content) {
      super.onClick(content);
      if (this.ui == null) {
         this.ui = new Container();
         this.ui.fill();
         if (this.waitingLabel == null) {
            this.waitingLabel = new Label(Quarry.Q.i18n.get("structure.UI.waiting_for_inputs"), Quarry.Q.skin, "small-font", Color.WHITE);
            this.waitingLabel.setAlignment(1);
         }
      }

      content.add(this.ui).grow();
      this.updateUI();
   }

   @Override
   public void onPlacement(boolean fromLoading) {
      super.onPlacement(fromLoading);
      if (this.layer != null && this.activeRecipe == null) {
         this.pickRandomActiveRecipe();
      }
   }

   public RecipeList.Recipe getActiveRecipe() {
      return this.activeRecipe;
   }

   @Override
   public void onDestroy() {
      super.onDestroy();
      this.stopSfx();
   }

   protected void pickRandomActiveRecipe() {
      RecipeList s = this.getSchema().recipeList;
      if (s.hasInputs) {
         for (int index = 0; index < s.recipes.length; index++) {
            RecipeList.Recipe r = s.recipes[index];
            boolean ready = true;
            Item.Items.Amount[] inputs = new Item.Items.Amount[r.input.entries.length];

            for (int i = 0; i < r.input.entries.length; i++) {
               IStorage ci = this.inputInventories[i];
               Item.Items.Amount en = r.input.entries[i];
               if (en != null) {
                  if (en.getCat() != null) {
                     Item.Items.Amount[] es = ci.get(en.getCat());
                     if (es == null || es.length == 0) {
                        ready = false;
                        break;
                     }

                     int sum = 0;

                     for (Item.Items.Amount e : es) {
                        if (e != null) {
                           sum += e.getAmount();
                        }
                     }

                     if (sum < en.getAmount()) {
                        ready = false;
                        break;
                     }

                     if (es.length != 1 && es[0].getAmount() != en.getAmount()) {
                        inputs[i] = en;
                     } else {
                        inputs[i] = new Item.Items.Amount(es[0].getItem(), en.getAmount());
                     }
                  } else if (en.getItem() == Item.base(en.getItem())) {
                     Item.Items.Amount[] ex = ci.getSimilar(en.getItem());
                     if (ex == null) {
                        ready = false;
                        break;
                     }

                     Item.Items.Amount correctAmount = null;

                     for (Item.Items.Amount amount : ex) {
                        if (amount.getAmount() >= en.getAmount()) {
                           correctAmount = amount;
                           break;
                        }
                     }

                     if (correctAmount == null) {
                        ready = false;
                        break;
                     }

                     inputs[i] = new Item.Items.Amount(correctAmount.getItem(), en.getAmount());
                  } else {
                     int exx = ci.get(en.getItem());
                     if (exx < en.getAmount()) {
                        ready = false;
                        break;
                     }

                     inputs[i] = new Item.Items.Amount(en.getItem(), en.getAmount());
                  }
               }
            }

            if (ready) {
               for (int ix = 0; ix < r.input.entries.length; ix++) {
                  Item.Items.Amount exx = inputs[ix];
                  if (exx != null) {
                     if (exx.getCat() != null) {
                        this.inputInventories[ix].remove(exx.getCat(), exx.getAmount());
                     } else {
                        this.inputInventories[ix].remove(exx.getItem(), exx.getAmount());
                     }
                  }
               }

               if (this.isClicked()) {
                  Game.G.postUi(() -> Game.G.ui.updateStructureUIInventory());
               }

               this.activeItems = new Item.Items(inputs);
               this.setRecipe(index);
               return;
            }
         }

         this.setRecipe(-1);
      } else {
         this.setRecipe(SimRandom.G.nextInt(s.recipes.length));
      }
   }

   protected void setRecipe(int index) {
      this.setItemNotifications();
      this.activeRecipeIndex = index;
      if (index > -1) {
         this.activeRecipe = this.getSchema().recipeList.recipes[this.activeRecipeIndex];
         this.workDelay = this.activeRecipe.workingTime;
         this.powerCapacity = (double)this.activeRecipe.getPower();
      } else {
         this.activeRecipe = null;
         this.activeItems = null;
      }

      this.updateUI();
   }

   protected void updateUI() {
      if (!Game.isRenderThread()) {
         Game.G.postUi(this::updateUI);
         return;
      }
      if (this.ui != null) {
         if (this.activeRecipe == null) {
            this.ui.setActor(this.waitingLabel);
         } else {
            this.ui.setActor(GameUi.renderRecipe(Quarry.Q.skin, this.activeRecipe, true));
            this.timeLabel = (Label)this.ui.findActor("time");
         }

         this.ui.invalidateHierarchy();
      }
   }

   @Override
   public void update(double deltaTime, int gameSpeed, Bounds dirtyBounds) {
      super.update(deltaTime, gameSpeed, dirtyBounds);
      if (gameSpeed == 0) {
         this.powerLevelMean.addValue(this.powerLevelMean.getLatest());
      } else {
         this.powerLevelMean.addValue((float)this.powerLevel);
      }

      if (this.activeRecipe != null && this.activeRecipe.getPower() > 0.0F && gameSpeed > 0) {
         this.noPower = this.powerLevel < (double)(this.activeRecipe.getPower() / 2.0F);
         if (!this.noPower) {
            this.powerLevel = Math.max(0.0, this.powerLevel - (double)(this.activeRecipe.getPower() * 60.0F) * deltaTime * (double)gameSpeed);
            this.framesPassedWithPower++;
            if (this.framesPassedWithPower > 10) {
               this.framesPassedWithPower = 10;
            }
         } else {
            this.framesPassedWithPower = 0;
         }
      } else {
         this.noPower = false;
      }

      if (!this.sleeping
         && this.activeRecipe != null
         && (this.hasCapacity = this.hasCapacityForProduction())
         && !this.noPower
         && !this.additionalWorkBlockingCondition()) {
         if (gameSpeed == 0) {
            this.pauseSfx();
         } else {
            this.workDelay = (float)(
               (double)this.workDelay
                  - deltaTime
                     * (double)gameSpeed
                     * (double)Math.max(
                        0.0F, this.activeRecipe.getPower() == 0.0F ? 1.0F : this.powerLevelMean.getMean() / this.activeRecipe.getPower() * 2.0F - 1.0F
                     )
            );
            this.playSfx();
         }

         if (this.ui != null) {
            // 数值在这里（持有 simLock 时）算好，只把 setText 丢回渲染线程——
            // 否则 lambda 稍后执行时 activeRecipe 可能已经变成 null
            final String time = GameUi.formatResourceAmount(Math.min((float)((int)this.workDelay + 1), this.activeRecipe.workingTime)) + "s";
            Game.G.postUi(() -> this.timeLabel.setText(time));
         }

         if (this.workDelay <= 0.0F) {
            this.doProductionStep();
            this.setRecipe(-1);
            this.pickRandomActiveRecipe();
         }
      } else {
         this.pauseSfx();
      }
   }

   protected boolean additionalWorkBlockingCondition() {
      return false;
   }

   protected void drawRecipeProgress(ShapeRenderer shaper) {
      if (this.activeRecipe != null && this.hasCapacity) {
         shaper.setColor(0.0F, 0.5F, 0.0F, 1.0F);
         shaper.set(ShapeType.Filled);
         float progress = 1.0F - this.workDelay / this.activeRecipe.workingTime;
         shaper.rect((float)(this.x * 64), (float)(this.y * 64), progress * (float)this.getWidth() * 64.0F, 8.0F);
      }
   }

   @Override
   public void drawFrame(SpriteRenderer spriter, ShapeRenderer shaper, SpriterDelegateBatch pfxBatch) {
      super.drawFrame(spriter, shaper, pfxBatch);
      this.drawRecipeProgress(shaper);
      if (this.activeRecipe != null && this.activeRecipe.getPower() > 0.0F && this.framesPassedWithPower < 10) {
         float size = 24.0F * (1.0F + 0.3F * (MathUtils.sin(this.time * 2.0F * (float) Math.PI) * 0.5F + 0.5F));
         spriter.add(
            nopowerTex,
            (float)((this.x + this.getWidth()) * 64) - (size - 24.0F) / 2.0F - 30.0F,
            (float)((this.y + this.getHeight()) * 64) - (size - 24.0F) / 2.0F - 60.0F,
            -1.0F,
            size,
            size
         );
      }

      if (this.powerCapacity > 0.0) {
         float scale = (float)Math.min(1.0, (double)this.powerLevelMean.getMean() / this.powerCapacity);
         if (scale > 0.0F) {
            spriter.add(
               (float)(this.x * 64 + 1),
               (float)(this.y * 64 + 1),
               -1.0F,
               4.0F,
               (float)(this.getHeight() * 64 - 2) * scale,
               Substation.powermeterTex.getU(),
               Substation.powermeterTex.getV2(),
               Substation.powermeterTex.getU2(),
               Substation.powermeterTex.getV2() + (Substation.powermeterTex.getV() - Substation.powermeterTex.getV2()) * scale
            );
         }
      }

      this.drawBoostState(spriter);
      if (this.activeRecipe != null && !this.hasCapacity) {
         this.drawFullState(spriter);
      }
   }

   protected boolean hasCapacityForProduction() {
      if (this.activeRecipe.output != null) {
         for (int i = 0; i < this.activeRecipe.output.entries.length; i++) {
            Item.Items.Amount a = this.activeRecipe.output.entries[i];
            if (a != null
               && (
                  this.getSchema().outputBuffer && this.outputInventories[i].getSize() - this.outputInventories[i].getCount() < a.getAmount()
                     || !this.getSchema().outputBuffer && !this.outputInventories[i].isEmpty()
               )) {
               return false;
            }
         }
      }

      return true;
   }

   protected void doProductionStep() {
      if (this.activeRecipe.output != null) {
         for (int i = 0; i < this.activeRecipe.output.entries.length; i++) {
            Item.Items.Amount a = this.activeRecipe.output.entries[i];
            if (a != null) {
               if (a instanceof RecipeList.ParameterizedAmount) {
                  if (this.activeItems == null) {
                     this.setRecipe(-1);
                     return;
                  }

                  this.outputInventories[i]
                     .addUnsafe(Item.get(a.getItem(), this.activeItems.entries[((RecipeList.ParameterizedAmount)a).inputParameter].getItem()), a.getAmount());
               } else if (a instanceof RecipeList.SameAmount) {
                  if (this.activeItems == null) {
                     this.setRecipe(-1);
                     return;
                  }

                  this.outputInventories[i].addUnsafe(this.activeItems.entries[((RecipeList.SameAmount)a).inputParameter].getItem(), a.getAmount());
               } else {
                  this.outputInventories[i].addUnsafe(a.getItem(), a.getAmount());
               }
            }
         }
      }
   }

   @Override
   public boolean putBack(Item.ItemType item, int amount) {
      if (this.outputInventories.length <= 1) {
         this.outputInventories[0].addUnsafe(item, amount);
      } else if (this.getSchema().recipeList.containsOutput(item)) {
         for (RecipeList.Recipe r : this.getSchema().recipeList.recipes) {
            for (int i = 0; i < r.getOutput().entries.length; i++) {
               Item.Items.Amount a = r.getOutput().entries[i];
               if (a != null && (a.getItem() == item || Item.base(item) == a.getItem())) {
                  this.outputInventories[i].addUnsafe(item, amount);
                  return true;
               }
            }
         }
      }

      return false;
   }

   @Override
   protected void saveData(NBT.Builder b) {
      super.saveData(b);
      b.Float("workDelay", this.workDelay).Int("recipe", this.activeRecipeIndex).Double("power", this.powerLevel);
      b.List("filterInventories", NBT.TagType.Compound);

      for (IStorage in : this.inputInventories) {
         if (in != null) {
            ((Component)in).saveData(b);
         }
      }

      b.End();
      if (this.activeItems != null) {
         short[] types = new short[this.activeItems.entries.length];
         byte[] categories = new byte[this.activeItems.entries.length];
         int[] amounts = new int[types.length];

         for (int i = 0; i < types.length; i++) {
            Item.Items.Amount e = this.activeItems.entries[i];
            if (e != null) {
               if (e.getItem() != null) {
                  types[i] = e.getItem().value;
               } else {
                  categories[i] = e.getCat().id;
               }

               amounts[i] = e.getAmount();
            }
         }

         b.ByteArray("activeCats", categories).ShortArray("activeTypes", types).IntArray("activeAmounts", amounts);
      }
   }

   @Override
   protected void loadData(NBT.CompoundTag tag) throws NBT.NBTException {
      super.loadData(tag);
      this.workDelay = tag.Float("workDelay", 0.0F);

      try {
         this.powerLevel = tag.Double("power");
      } catch (NBT.NBTException var7) {
         this.powerLevel = (double)tag.Float("power", 0.0F);
      }

      this.activeRecipeIndex = tag.Int("recipe", -1);
      if (this.activeRecipeIndex > -1) {
         try {
            this.activeRecipe = this.getSchema().recipeList.recipes[this.activeRecipeIndex];
            this.powerCapacity = (double)this.activeRecipe.getPower();
            short[] types = tag.ShortArray("activeTypes", null);
            byte[] categories = tag.ByteArray("activeCats", null);
            if (types != null && categories != null) {
               int[] amounts = tag.IntArray("activeAmounts");
               Item.Items.Amount[] entries = new Item.Items.Amount[types.length];

               for (int i = 0; i < types.length; i++) {
                  if (types[i] != 0) {
                     entries[i] = new Item.Items.Amount(Item.get(types[i]), amounts[i]);
                  } else if (categories[i] != 0) {
                     entries[i] = new Item.Items.Amount(Item.category(categories[i]), amounts[i]);
                  }
               }

               this.activeItems = new Item.Items(entries);
            }
         } catch (NBT.NBTException var9) {
            Quarry.Q.pi.message(1, var9);
            this.setRecipe(-1);
         }
      }

      try {
         NBT.ListTag filterInvs = tag.List("filterInventories");

         for (int ix = 0; ix < this.inputInventories.length; ix++) {
            if (this.inputInventories[ix] != null) {
               ((Component)this.inputInventories[ix]).loadData((NBT.CompoundTag)filterInvs.data.get(ix));
            }
         }
      } catch (Exception var8) {
         Quarry.Q.pi.message(1, var8);
      }
   }

   @Override
   public int getReceiverPriority() {
      return 100;
   }

   public static class ProducerSchema extends PausableSchema {
      public final RecipeList recipeList;
      public final boolean outputBuffer;

      public ProducerSchema(
         int version,
         StructureType type,
         int width,
         int height,
         String tex,
         Item.Items buildCosts,
         RecipeList recipes,
         Sfx sfx,
         boolean outputBuffer,
         Dock... docks
      ) {
         super(version, type, true, width, height, tex, buildCosts, sfx, docks);
         this.outputBuffer = outputBuffer;
         recipes.validateRecipes(this);
         this.recipeList = recipes;
      }

      @Override
      public Component[] copyComponents(Structure<?> structure) {
         Component[] c = new Component[this.components.size + this.recipeList.outputSizes.size];
         int i = 0;

         for (Component comp : this.components) {
            c[i] = comp.clone();
            c[i].setStructure(structure);
            c[i].init();
            i++;
         }

         return c;
      }
   }
}
