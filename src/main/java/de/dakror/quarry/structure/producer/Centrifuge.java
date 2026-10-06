package de.dakror.quarry.structure.producer;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import de.dakror.common.libgdx.render.SpriteRenderer;
import de.dakror.quarry.Const;
import de.dakror.quarry.game.ConstantSupplyAmount;
import de.dakror.quarry.game.Item;
import de.dakror.quarry.game.Science;
import de.dakror.quarry.scenes.Game;
import de.dakror.quarry.structure.base.Direction;
import de.dakror.quarry.structure.base.Dock;
import de.dakror.quarry.structure.base.ProducerStructure;
import de.dakror.quarry.structure.base.RecipeList;
import de.dakror.quarry.structure.base.Schema;
import de.dakror.quarry.structure.base.StructureType;
import de.dakror.quarry.structure.base.component.CTank;
import de.dakror.quarry.util.Bounds;
import de.dakror.quarry.util.Sfx;
import de.dakror.quarry.util.SpriterDelegateBatch;

public class Centrifuge extends ProducerStructure {
   public static final ProducerStructure.ProducerSchema classSchema = new ProducerStructure.ProducerSchema(
         1,
         StructureType.Centrifuge,
         11,
         11,
         "centrifuge",
         new Item.Items(
            Item.ItemType.MachineFrame,
            25,
            Item.ItemType.SteelIngot,
            200,
            Item.ItemType.BronzeIngot,
            200,
            Item.ItemType.Dynamo,
            10,
            Item.ItemType.Magnet,
            200,
            Item.ItemType.CarbonBlock,
            60
         ),
         new RecipeList() {
            @Override
            protected void init() {
               this.add(
                  new RecipeList.Recipe(60.0F, "titanium", 1000)
                     .input(new Item.Items.Amount(Item.ItemType.IronDust, 30), new ConstantSupplyAmount(Item.ItemType.Lubricant, 80))
                     .output(new Item.Items.Amount(Item.ItemType.TitaniumDust, 1), new Item.Items.Amount(Item.ItemType.StoneDust, 20))
               );
               this.add(
                  new RecipeList.Recipe(115.0F, "stone", 2900)
                     .input(new Item.Items.Amount(Item.ItemType.StoneDust, 175), new ConstantSupplyAmount(Item.ItemType.Lubricant, 200))
                     .output(new Item.Items.Amount(Item.ItemType.QuartzDust, 85), new Item.Items.Amount(Item.ItemType.PyriteDust, 60))
               );
               this.add(
                  new RecipeList.Recipe(80.0F, "pyrite", 1500)
                     .input(new Item.Items.Amount(Item.ItemType.PyriteDust, 15), new ConstantSupplyAmount(Item.ItemType.Lubricant, 100))
                     .output(new Item.Items.Amount(Item.ItemType.IronDust, 5), new Item.Items.Amount(Item.ItemType.SulfurDust, 10))
               );
               this.add(
                  new RecipeList.Recipe(185.0F, "gold", 10000)
                     .input(new Item.Items.Amount(Item.ItemType.QuartzDust, 570), new ConstantSupplyAmount(Item.ItemType.Lubricant, 500))
                     .output(new Item.Items.Amount(Item.ItemType.GoldDust, 1), new Item.Items.Amount(Item.ItemType.SiliconDust, 210))
               );
            }

            @Override
            public void validateRecipes(Schema schema) {
               super.validateRecipes(schema);
               this.fluidInputs.set(0, 10000);
            }
         },
         new Sfx("centrifuge" + Const.SFX_FORMAT),
         true,
         new Dock(10, 4, Direction.East, Dock.DockType.ItemOut),
         new Dock(10, 6, Direction.East, Dock.DockType.ItemOut),
         new Dock(5, 10, Direction.North, Dock.DockType.BigPower),
         new Dock(5, 0, Direction.South, Dock.DockType.Power),
         new Dock(
            0,
            4,
            Direction.West,
            Dock.DockType.ItemIn,
            new Dock.DockFilter(Item.ItemType.StoneDust, Item.ItemType.QuartzDust, Item.ItemType.PyriteDust, Item.ItemType.IronDust)
         ),
         new Dock(0, 6, Direction.West, Dock.DockType.FluidIn, new Dock.DockFilter(Item.ItemType.Lubricant))
      )
      .<Schema>sciences(new Science.ScienceType[]{Science.ScienceType.MineralExtraction})
      .flags(Schema.Flags.MirroredTextureHorizontal, Schema.Flags.MirroredTextureVertical, Schema.Flags.TextureAlwaysUpright);
   public static final ProducerStructure.ProducerSchema classSchemaV103 = new ProducerStructure.ProducerSchema(
         0,
         StructureType.Centrifuge,
         11,
         11,
         "centrifuge",
         new Item.Items(
            Item.ItemType.MachineFrame,
            25,
            Item.ItemType.SteelIngot,
            200,
            Item.ItemType.BronzeIngot,
            200,
            Item.ItemType.Dynamo,
            10,
            Item.ItemType.Magnet,
            200,
            Item.ItemType.CarbonBlock,
            60
         ),
         new RecipeList() {
            @Override
            protected void init() {
               this.add(
                  new RecipeList.Recipe(115.0F, "stone", 2900)
                     .input(new Item.Items.Amount(Item.ItemType.StoneDust, 175), new ConstantSupplyAmount(Item.ItemType.Lubricant, 200))
                     .output(new Item.Items.Amount(Item.ItemType.QuartzDust, 85), new Item.Items.Amount(Item.ItemType.PyriteDust, 60))
               );
               this.add(
                  new RecipeList.Recipe(80.0F, "pyrite", 1500)
                     .input(new Item.Items.Amount(Item.ItemType.PyriteDust, 15), new ConstantSupplyAmount(Item.ItemType.Lubricant, 100))
                     .output(new Item.Items.Amount(Item.ItemType.IronDust, 5), new Item.Items.Amount(Item.ItemType.SulfurDust, 10))
               );
               this.add(
                  new RecipeList.Recipe(185.0F, "gold", 8500)
                     .input(new Item.Items.Amount(Item.ItemType.QuartzDust, 570), new ConstantSupplyAmount(Item.ItemType.Lubricant, 500))
                     .output(new Item.Items.Amount(Item.ItemType.GoldDust, 1), new Item.Items.Amount(Item.ItemType.SiliconDust, 210))
               );
            }

            @Override
            public void validateRecipes(Schema schema) {
               super.validateRecipes(schema);
               this.fluidInputs.set(0, 10000);
            }
         },
         new Sfx("centrifuge" + Const.SFX_FORMAT),
         true,
         new Dock(10, 4, Direction.East, Dock.DockType.ItemOut),
         new Dock(10, 6, Direction.East, Dock.DockType.ItemOut),
         new Dock(5, 10, Direction.North, Dock.DockType.BigPower),
         new Dock(5, 0, Direction.South, Dock.DockType.Power),
         new Dock(0, 4, Direction.West, Dock.DockType.ItemIn, new Dock.DockFilter(Item.ItemType.StoneDust, Item.ItemType.QuartzDust, Item.ItemType.PyriteDust)),
         new Dock(0, 6, Direction.West, Dock.DockType.FluidIn, new Dock.DockFilter(Item.ItemType.Lubricant))
      )
      .<Schema>sciences(new Science.ScienceType[]{Science.ScienceType.MineralExtraction})
      .flags(Schema.Flags.MirroredTextureHorizontal, Schema.Flags.MirroredTextureVertical, Schema.Flags.TextureAlwaysUpright);
   boolean tooLittleLubricant;

   public Centrifuge(int x, int y, int version) {
      super(x, y, selectSchema(version, new ProducerStructure.ProducerSchema[]{classSchema, classSchemaV103}));
   }

   public Centrifuge(int x, int y) {
      super(x, y, classSchema);
   }

   @Override
   public void update(double deltaTime, int gameSpeed, Bounds dirtyBounds) {
      super.update(deltaTime, gameSpeed, dirtyBounds);
      if (!this.sleeping && this.activeRecipe != null && (this.hasCapacity = this.hasCapacityForProduction()) && !this.noPower) {
         CTank lubricantTank = (CTank)this.inputInventories[1];
         if (!lubricantTank.isEmpty()) {
            this.tooLittleLubricant = (float)this.activeRecipe.getInput().entries[1].getAmount() / 60.0F > (float)lubricantTank.getCount();
            if (!this.tooLittleLubricant) {
               lubricantTank.removeWithRest(
                  Item.ItemType.Lubricant, (int)Math.round((double)(this.activeRecipe.getInput().entries[1].getAmount() * gameSpeed) * deltaTime)
               );
               Game.G.postUi(() -> Game.G.ui.updateStructureUIInventory());
            }
         }
      }
   }

   @Override
   protected boolean additionalWorkBlockingCondition() {
      return this.tooLittleLubricant;
   }

   @Override
   public void drawFrame(SpriteRenderer spriter, ShapeRenderer shaper, SpriterDelegateBatch pfxBatch) {
      super.drawFrame(spriter, shaper, pfxBatch);
      if (this.activeRecipe != null && this.tooLittleLubricant) {
         float size = 24.0F * (1.0F + 0.3F * (MathUtils.sin(this.time * 2.0F * (float) Math.PI) * 0.5F + 0.5F));
         spriter.add(
            Item.ItemType.Lubricant.icon,
            (float)((this.x + this.getWidth()) * 64) - (size - 24.0F) / 2.0F - 120.0F,
            (float)((this.y + this.getHeight()) * 64) - (size - 24.0F) / 2.0F - 30.0F,
            -1.0F,
            size,
            size
         );
      }
   }
}
