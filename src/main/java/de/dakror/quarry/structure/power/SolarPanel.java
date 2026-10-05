package de.dakror.quarry.structure.power;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import de.dakror.common.libgdx.render.SpriteRenderer;
import de.dakror.quarry.game.Item;
import de.dakror.quarry.game.Science;
import de.dakror.quarry.structure.base.ProducerStructure;
import de.dakror.quarry.structure.base.Schema;
import de.dakror.quarry.structure.base.Structure;
import de.dakror.quarry.structure.base.StructureType;
import de.dakror.quarry.util.SpriterDelegateBatch;

public class SolarPanel extends Structure<Schema> {
   public static final Schema classSchema = new Schema(
         0,
         StructureType.SolarPanel,
         true,
         3,
         3,
         "solarpanel",
         new Item.Items(Item.ItemType.SiliconWafer, 60, Item.ItemType.Glass, 50, Item.ItemType.CopperWire, 30),
         null
      )
      .<Schema>loudness(0.1F)
      .<Schema>flags(Schema.Flags.MirroredTextureHorizontal)
      .sciences(Science.ScienceType.SolarPower);
   SolarPanelOutlet outlet;

   public SolarPanel(int x, int y) {
      super(x, y, classSchema);
   }

   @Override
   public void drawFrame(SpriteRenderer spriter, ShapeRenderer shaper, SpriterDelegateBatch pfxBatch) {
      super.drawFrame(spriter, shaper, pfxBatch);
      if (this.outlet == null && this.layer != null) {
         float size = 24.0F * (1.0F + 0.3F * (MathUtils.sin(this.time * 2.0F * (float) Math.PI) * 0.5F + 0.5F));
         spriter.add(
            ProducerStructure.nopowerTex,
            (float)((this.x + this.getWidth()) * 64) - (size - 24.0F) / 2.0F - 60.0F,
            (float)((this.y + this.getHeight()) * 64) - (size - 24.0F) / 2.0F - 60.0F,
            -1.0F,
            size,
            size
         );
      } else if (this.clicked) {
         shaper.set(ShapeType.Filled);
         shaper.setColor(Color.PURPLE);
         shaper.rect(
            ((float)this.x + (float)this.getWidth() * 0.5F) * 64.0F - 6.0F, ((float)this.y + (float)this.getHeight() * 0.5F) * 64.0F - 6.0F, 12.0F, 12.0F
         );
         shaper.rectLine(
            ((float)this.x + (float)this.getWidth() * 0.5F) * 64.0F,
            ((float)this.y + (float)this.getHeight() * 0.5F) * 64.0F,
            ((float)this.outlet.x + (float)this.outlet.getWidth() * 0.5F) * 64.0F,
            ((float)this.outlet.y + (float)this.outlet.getHeight() * 0.5F) * 64.0F,
            3.0F
         );
         shaper.rect(
            ((float)this.outlet.x + (float)this.outlet.getWidth() * 0.5F) * 64.0F - 6.0F,
            ((float)this.outlet.y + (float)this.outlet.getHeight() * 0.5F) * 64.0F - 6.0F,
            12.0F,
            12.0F
         );
      }
   }

   @Override
   public int getDonorPriority() {
      return 1;
   }
}
