package de.dakror.quarry.structure.logistics;

import de.dakror.quarry.game.Item;
import de.dakror.quarry.game.Layer;
import de.dakror.quarry.game.Science;
import de.dakror.quarry.scenes.Game;
import de.dakror.quarry.structure.base.Direction;
import de.dakror.quarry.structure.base.Dock;
import de.dakror.quarry.structure.base.Schema;
import de.dakror.quarry.structure.base.Structure;
import de.dakror.quarry.structure.base.StructureType;
import de.dakror.quarry.structure.base.component.CTank;

public class TubeShaft extends Structure<Schema> {
   public static final Schema classSchema = new Schema(
         0,
         StructureType.TubeShaft,
         true,
         1,
         1,
         "tubeshaft",
         new Item.Items(Item.ItemType.SteelIngot, 10, Item.ItemType.SteelTube, 8),
         null,
         new Dock(0, 0, Direction.West, Dock.DockType.FluidOut),
         new Dock(0, 0, Direction.East, Dock.DockType.FluidIn)
      )
      .<Schema>components(new CTank(20000, 0).setPumpOutDelay(0.0F).setMaxOutput(1000))
      .sciences(Science.ScienceType.MineExpansion, Science.ScienceType.WaterUsage);
   TubeShaft other;
   int direction;

   public TubeShaft(int x, int y) {
      this(x, y, classSchema, 1);
   }

   protected TubeShaft(int x, int y, Schema schema, int direction) {
      super(x, y, schema);
      this.direction = direction;
   }

   @Override
   public void onPlacement(boolean fromLoading) {
      super.onPlacement(fromLoading);
      if (!fromLoading && this.layer != null && this.direction == 1) {
         Layer otherLayer = Game.G.getLayer(this.layer.getIndex() + this.direction);
         if (otherLayer != null) {
            this.other = new TubeShaftBelow(this.x, this.y);
            this.other.setUpDirection(this.upDirection);
            this.other.other = this;
            otherLayer.addStructure(this.other);
         }
      }
   }

   @Override
   public void postLoad() {
      super.postLoad();
      this.other = (TubeShaft)Game.G.getLayer(this.layer.getIndex() + this.direction).getStructure(this.x, this.y);
   }

   @Override
   public boolean canAccept(Item.ItemType item, int x, int y, Direction dir) {
      return item.categories.contains(Item.ItemCategory.Fluid)
         && this.isNextToDock(x, y, dir, this.getDocks()[1])
         && ((CTank)this.other.getComponents()[0]).hasSpace();
   }

   @Override
   public int acceptFluid(Item.ItemType item, int amount, Structure<?> source) {
      return !item.categories.contains(Item.ItemCategory.Fluid) ? amount : ((CTank)this.other.getComponents()[0]).addWithRest(item, amount);
   }

   @Override
   public void onDestroy() {
      super.onDestroy();
      if (this.other != null) {
         this.other.layer.removeStructure(this.other);
      }
   }
}
