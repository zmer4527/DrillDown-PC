package de.dakror.quarry.structure.power;

import de.dakror.quarry.game.Item;
import de.dakror.quarry.game.Science;
import de.dakror.quarry.structure.base.Schema;
import de.dakror.quarry.structure.base.StructureType;

/**
 * Insulated cable crossing: two perpendicular copper cables share this tile but stay electrically
 * independent. {@link CopperCable#walkToEnd()} continues straight through this tile, so each axis
 * forms its own power run.
 */
public class CopperCableCrossing extends CopperCable {
   public static final Schema classSchema = new Schema(
         0, StructureType.CopperCableCrossing, false, 1, 1, "copper_cable", new Item.Items(Item.ItemType.CopperWire, 1), null
      )
      .<Schema>sciences(Science.ScienceType.Electricity)
      .flags(Schema.Flags.NoDustEffect, Schema.Flags.NotRotatable);

   public CopperCableCrossing(int x, int y) {
      super(x, y, classSchema);
   }

   @Override
   protected int connectionLimit() {
      return 4;
   }
}
