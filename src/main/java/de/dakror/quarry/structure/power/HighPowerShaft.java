package de.dakror.quarry.structure.power;

import de.dakror.quarry.game.Item;
import de.dakror.quarry.game.Layer;
import de.dakror.quarry.game.Science;
import de.dakror.quarry.game.power.PowerNetwork;
import de.dakror.quarry.scenes.Game;
import de.dakror.quarry.structure.base.Direction;
import de.dakror.quarry.structure.base.Dock;
import de.dakror.quarry.structure.base.StructureType;

public class HighPowerShaft extends Substation {
   public static final Substation.SubstationSchema classSchema = new Substation.SubstationSchema(
         0,
         StructureType.HighPowerShaft,
         1,
         1,
         200000,
         200,
         "highpowershaft",
         new Item.Items(Item.ItemType.SteelPlate, 35, Item.ItemType.SteelCable, 100, Item.ItemType.Brick, 50),
         null,
         1,
         new Dock(0, 0, Direction.East, Dock.DockType.BigPower)
      )
      .sciences(new Science.ScienceType[]{Science.ScienceType.MineExpansion, Science.ScienceType.HighPower});
   HighPowerShaft other;
   int direction;

   public HighPowerShaft(int x, int y) {
      this(x, y, classSchema, 1);
   }

   protected HighPowerShaft(int x, int y, Substation.SubstationSchema schema, int direction) {
      super(x, y, schema);
      this.direction = direction;
   }

   @Override
   public void onPlacement(boolean fromLoading) {
      super.onPlacement(fromLoading);
      if (!fromLoading && this.layer != null && this.direction == 1) {
         Layer otherLayer = Game.G.getLayer(this.layer.getIndex() + this.direction);
         if (otherLayer != null) {
            this.other = new HighPowerShaftBelow(this.x, this.y);
            this.other.setUpDirection(this.upDirection);
            this.other.other = this;
            otherLayer.addStructure(this.other);
            this.powerNetwork.getPowerGrid().mergeNetworks(this.powerNetwork, this.other.powerNetwork);
            this.powerNetwork.addEdge(this, 1, this.other, 1, PowerNetwork.NetworkStrength.PowerPole);
         }
      }
   }

   @Override
   public void postLoad() {
      super.postLoad();
      this.other = (HighPowerShaft)Game.G.getLayer(this.layer.getIndex() + this.direction).getStructure(this.x, this.y);
      if (this.direction == 1) {
         this.powerNetwork.getPowerGrid().mergeNetworks(this.powerNetwork, this.other.powerNetwork);
         this.powerNetwork.addEdge(this, 1, this.other, 1, PowerNetwork.NetworkStrength.PowerPole);
      }
   }

   @Override
   public void onDestroy() {
      super.onDestroy();
      this.other.layer.removeStructure(this.other);
   }
}
