package de.dakror.quarry.structure.logistics;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import de.dakror.common.libgdx.render.SpriteRenderer;
import de.dakror.quarry.game.Item;
import de.dakror.quarry.game.Science;
import de.dakror.quarry.structure.base.Direction;
import de.dakror.quarry.structure.base.ProducerStructure;
import de.dakror.quarry.structure.base.Schema;
import de.dakror.quarry.structure.base.StructureType;
import de.dakror.quarry.util.SpriterDelegateBatch;

public class ElectricConveyor extends Conveyor {
   public static final Conveyor.ConveyorSchema classSchema = new Conveyor.ConveyorSchema(
         0, StructureType.ElectricConveyor, true, 1, 1, "electric_conveyor", new Item.Items(Item.ItemType.SteelPlate, 1, Item.ItemType.CopperWire, 4), null
      )
      .<Schema>flags(new Schema.Flags[]{Schema.Flags.Draggable, Schema.Flags.NoDustEffect})
      .<Schema>sciences(Science.ScienceType.Electricity)
      .loudness(0.5F);
   public ElectricConveyorCore core;

   public ElectricConveyor(int x, int y) {
      super(x, y, classSchema);
      this.dir = Direction.East;
   }

   public ElectricConveyor(int x, int y, Direction direction) {
      super(x, y, classSchema);
      this.dir = direction;
   }

   @Override
   public void drawFrame(SpriteRenderer spriter, ShapeRenderer shaper, SpriterDelegateBatch pfxBatch) {
      super.drawFrame(spriter, shaper, pfxBatch);
      if (this.layer != null && (this.core == null || this.core.noPower)) {
         float size = 24.0F * (1.0F + 0.3F * (MathUtils.sin(this.time * 2.0F * (float) Math.PI) * 0.5F + 0.5F));
         spriter.add(
            ProducerStructure.nopowerTex,
            (float)((this.x + this.getWidth()) * 64) - (size - 24.0F) / 2.0F - 60.0F,
            (float)((this.y + this.getHeight()) * 64) - (size - 24.0F) / 2.0F - 60.0F,
            -1.0F,
            size,
            size
         );
      }
   }

   @Override
   public void updateItems(double deltaTime, int gameSpeed, boolean chain) {
      super.updateItems(deltaTime, this.core != null && !this.core.noPower ? 5 * gameSpeed : 0, chain);
   }

   @Override
   public boolean isItemSlotFree(int slot) {
      return this.core != null && !this.core.noPower ? super.isItemSlotFree(slot) : false;
   }

   @Override
   public Object clone() {
      return new ElectricConveyor(this.x, this.y, this.dir);
   }
}
