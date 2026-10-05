package de.dakror.quarry.structure.base;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
import de.dakror.quarry.Quarry;
import de.dakror.quarry.game.Item;
import de.dakror.quarry.game.Science;
import java.util.EnumSet;

public abstract class RecipeList {
   public final RecipeList.Recipe[] recipes;
   public final EnumSet<Item.ItemType> outputItems;
   public final EnumSet<Item.ItemCategory> outputCategories;
   public boolean usesPower;
   private final Array<RecipeList.Recipe> rec = new Array(RecipeList.Recipe.class);
   public final boolean hasInputs;
   public final IntArray outputSizes;
   public final IntArray fluidInputs;

   public RecipeList() {
      this.init();
      this.recipes = (RecipeList.Recipe[])this.rec.toArray();
      this.rec.clear();
      boolean inputs = false;
      boolean withoutInputs = false;
      this.outputItems = EnumSet.noneOf(Item.ItemType.class);
      this.outputCategories = EnumSet.noneOf(Item.ItemCategory.class);
      this.outputSizes = new IntArray();
      this.fluidInputs = new IntArray();

      for (RecipeList.Recipe r : this.recipes) {
         if (r.output != null) {
            for (int i = 0; i < r.output.entries.length; i++) {
               Item.Items.Amount e = r.output.entries[i];
               if (this.outputSizes.size <= i) {
                  this.outputSizes.add(e.getAmount() * 2);
               } else {
                  this.outputSizes.set(i, Math.max(e.getAmount() * 2, this.outputSizes.get(i)));
               }

               if (e.getCat() != null) {
                  this.outputCategories.add(e.getCat());
               }

               if (e.getItem() != null) {
                  this.outputItems.add(e.getItem());
               }
            }
         }

         if (r.input == null) {
            withoutInputs = true;
         } else {
            for (int i = 0; i < r.input.entries.length; i++) {
               Item.Items.Amount ex = r.input.entries[i];
               if (ex.getCat() == Item.ItemCategory.Fluid || ex.getItem() != null && ex.getItem().categories.contains(Item.ItemCategory.Fluid)) {
                  if (this.fluidInputs.size <= i) {
                     this.fluidInputs.add(ex.getAmount() * 2);
                  } else {
                     this.fluidInputs.set(i, Math.max(ex.getAmount() * 2, this.fluidInputs.get(i)));
                  }
               }
            }

            inputs = true;
         }
      }

      if (inputs && withoutInputs) {
         throw new IllegalArgumentException("Either all or no recipes have to contain inputs!");
      } else {
         this.hasInputs = inputs;
      }
   }

   public void validateRecipes(Schema schema) {
      int outputs = 0;
      int inputs = 0;
      Array<Dock.DockFilter> filters = new Array();

      for (Dock d : schema.getDocks()) {
         if (d.type == Dock.DockType.ItemIn || d.type == Dock.DockType.FluidIn) {
            inputs++;
            if (d.filter != null) {
               filters.add(d.filter);
            }
         } else if (d.type == Dock.DockType.ItemOut || d.type == Dock.DockType.FluidOut) {
            outputs++;
         }
      }

      int maxIns = 0;

      for (RecipeList.Recipe r : this.recipes) {
         if (r.getInput() != null) {
            maxIns = Math.max(maxIns, r.getInput().entries.length);
         }

         r.description = Quarry.Q.i18n.get("structure." + schema.type.name().toLowerCase() + ".recipe." + r.description);
      }

      if (maxIns != inputs) {
         System.out.println(schema.type.name() + ": " + inputs + " docks, but up to " + maxIns + " inputs");
      }

      if (filters.size != 0) {
         for (RecipeList.Recipe r : this.recipes) {
            if (r.input != null) {
               for (Item.Items.Amount a : r.input.entries) {
                  if (a instanceof RecipeList.ParameterizedAmount) {
                     throw new IllegalArgumentException("Parameterized input amounts are not allowed!");
                  }

                  boolean any = false;

                  for (Dock.DockFilter f : filters) {
                     if (a.getItem() != null && f.accepts(a.getItem()) || a.getCat() != null && f.accepts(a.getCat())) {
                        any = true;
                        break;
                     }
                  }

                  if (!any) {
                     System.out.println(schema.type + ": I think no filter accepts item '" + (a.getItem() == null ? a.getCat() : a.getItem()) + "'!");
                  }
               }

               if (r.output != null) {
                  if (r.output.entries.length > outputs) {
                     throw new IllegalArgumentException("Invalid amount of output amounts!");
                  }

                  for (Item.Items.Amount a : r.output.entries) {
                     if (a.getCat() != null) {
                        throw new IllegalArgumentException("Output categories are not allowed!");
                     }

                     if (a instanceof RecipeList.ParameterizedAmount) {
                        Item.Items.Amount in = r.input.entries[((RecipeList.ParameterizedAmount)a).inputParameter];
                        if (in == null) {
                           throw new IllegalArgumentException("Invalid parameterized output!");
                        }
                     } else if (a instanceof RecipeList.SameAmount) {
                        Item.Items.Amount src = r.input.entries[((RecipeList.SameAmount)a).inputParameter];
                        a.setCat(src.getCat());
                        a.setItem(src.getItem());
                     }
                  }
               }

               if (r.getPower() > 0.0F) {
                  this.usesPower = true;
               }
            }
         }
      }
   }

   protected void add(RecipeList.Recipe r) {
      this.rec.add(r);
   }

   public boolean containsOutput(Item.ItemType item) {
      return this.outputItems.contains(item);
   }

   public boolean containsOutput(Item.ItemCategory cat) {
      return this.outputCategories.contains(cat);
   }

   protected abstract void init();

   public static class ParameterizedAmount extends Item.Items.Amount {
      public final int inputParameter;

      public ParameterizedAmount(Item.ItemType item, int amount, int inputParameter) {
         super(item, amount);
         this.inputParameter = inputParameter;
      }

      public ParameterizedAmount(Item.ItemCategory cat, int amount, int inputParameter) {
         super(cat, amount);
         this.inputParameter = inputParameter;
      }

      @Override
      public String toString() {
         return super.toString() + " (p" + this.inputParameter + ")";
      }
   }

   public static class Recipe {
      public final float workingTime;
      private String description;
      public final String name;
      private float power;
      protected Item.Items input;
      protected Item.Items output;
      protected EnumSet<Science.ScienceType> requiredSciences;

      public Recipe(float workingTime, String description) {
         this(workingTime, description, 0);
      }

      public Recipe(float workingTime, String description, int power) {
         this.workingTime = workingTime;
         this.description = description;
         this.power = (float)power;
         this.name = description;
         this.description = description;
         this.requiredSciences = EnumSet.noneOf(Science.ScienceType.class);
      }

      public String getDescription() {
         return this.description;
      }

      public Item.Items getInput() {
         return this.input;
      }

      public Item.Items getOutput() {
         return this.output;
      }

      public EnumSet<Science.ScienceType> getRequiredSciences() {
         return this.requiredSciences;
      }

      public RecipeList.Recipe input(Item.Items.Amount... amounts) {
         this.input = new Item.Items(amounts);
         return this;
      }

      public RecipeList.Recipe output(Item.Items.Amount... amounts) {
         this.output = new Item.Items(amounts);
         return this;
      }

      public RecipeList.Recipe science(Science.ScienceType... sciences) {
         for (Science.ScienceType s : sciences) {
            this.requiredSciences.add(s);
         }

         return this;
      }

      public float getPower() {
         return this.power;
      }
   }

   public static class SameAmount extends Item.Items.Amount {
      public final int inputParameter;

      public SameAmount(int inputParameter, int amount) {
         super((Item.ItemCategory)null, amount);
         this.inputParameter = inputParameter;
      }

      @Override
      public String toString() {
         return super.toString() + " (s" + this.inputParameter + ")";
      }
   }
}
