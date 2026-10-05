package de.dakror.quarry.structure.producer;

import de.dakror.quarry.Const;
import de.dakror.quarry.game.Item;
import de.dakror.quarry.game.Science;
import de.dakror.quarry.structure.base.Direction;
import de.dakror.quarry.structure.base.Dock;
import de.dakror.quarry.structure.base.ProducerStructure;
import de.dakror.quarry.structure.base.RecipeList;
import de.dakror.quarry.structure.base.Schema;
import de.dakror.quarry.structure.base.StructureType;
import de.dakror.quarry.structure.base.component.CInventory;
import de.dakror.quarry.util.Sfx;

public class Polymerizer extends ProducerStructure {
   public static final ProducerStructure.ProducerSchema classSchema = new ProducerStructure.ProducerSchema(
         0,
         StructureType.Polymerizer,
         4,
         4,
         "polymerizer",
         new Item.Items(
            Item.ItemType.MachineFrame, 20, Item.ItemType.Dynamo, 25, Item.ItemType.SteelTube, 50, Item.ItemType.TinPlate, 90, Item.ItemType.Glass, 90
         ),
         new RecipeList() {
            @Override
            protected void init() {
               this.add(
                  new RecipeList.Recipe(70.0F, "plasticbeads", 667)
                     .input(new Item.Items.Amount(Item.ItemType.RefinedOil, 10000))
                     .output(new Item.Items.Amount(Item.ItemType.PlasticBeads, 80))
               );
            }
         },
         new Sfx("polymerizer" + Const.SFX_FORMAT),
         true,
         new Dock(3, 2, Direction.East, Dock.DockType.ItemOut),
         new Dock(2, 0, Direction.South, Dock.DockType.Power),
         new Dock(0, 2, Direction.West, Dock.DockType.FluidIn, new Dock.DockFilter(Item.ItemType.RefinedOil))
      )
      .<Schema>sciences(new Science.ScienceType[]{Science.ScienceType.OilProcessing})
      .flags(Schema.Flags.TextureAlwaysUpright);

   public Polymerizer(int x, int y) {
      super(x, y, classSchema);
      ((CInventory)this.outputInventories[0]).setPumpOutSpeed(0.0F);
   }
}
