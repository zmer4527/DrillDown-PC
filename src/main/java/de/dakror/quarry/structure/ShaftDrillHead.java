package de.dakror.quarry.structure;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import de.dakror.common.libgdx.io.NBT;
import de.dakror.common.libgdx.render.SpriteRenderer;
import de.dakror.quarry.Quarry;
import de.dakror.quarry.game.Item;
import de.dakror.quarry.game.Layer;
import de.dakror.quarry.scenes.Game;
import de.dakror.quarry.structure.base.Schema;
import de.dakror.quarry.structure.base.Structure;
import de.dakror.quarry.structure.base.StructureType;
import de.dakror.quarry.util.SpriterDelegateBatch;

public class ShaftDrillHead extends Structure<Schema> {
   public static final Schema classSchema = new Schema(
         0, StructureType.ShaftDrillHead, true, ShaftDrill.classSchema.width, ShaftDrill.classSchema.height, "", new Item.Items(), null
      )
      .flags(Schema.Flags.Indestructible, Schema.Flags.TextureAlwaysUpright);
   static final TextureRegion core = Quarry.Q.atlas.findRegion("structure_shaft_drill_head");
   static final TextureRegion corner = Quarry.Q.atlas.findRegion("structure_shaft_drill_head_outer");
   ShaftDrill drill;
   int drillIndex;
   int drillLayer;
   float rotation = 0.0F;

   public ShaftDrillHead(int x, int y) {
      super(x, y, classSchema);
   }

   public ShaftDrillHead setDrill(ShaftDrill drill) {
      this.drill = drill;
      return this;
   }

   public ShaftDrill getDrill() {
      return this.drill;
   }

   @Override
   public void drawFrame(SpriteRenderer spriter, ShapeRenderer shaper, SpriterDelegateBatch pfxBatch) {
      if (this.drill.getActiveRecipe() != null && !this.drill.isSleeping()) {
         this.rotation = (this.rotation - 0.125F * (float)this.gameSpeed) % 360.0F;
      }

      spriter.add(core, (float)(this.x * 64), (float)(this.y * 64), -3.0F, 128.0F, 128.0F, 128.0F, 128.0F, 1.0F, 1.0F, this.rotation);
      spriter.add(core, (float)(this.x * 64), (float)(this.y * 64), -3.0F, 128.0F, 128.0F, 128.0F, 128.0F, 1.0F, 1.0F, this.rotation + 90.0F);
      spriter.add(core, (float)(this.x * 64), (float)(this.y * 64), -3.0F, 128.0F, 128.0F, 128.0F, 128.0F, 1.0F, 1.0F, this.rotation + 180.0F);
      spriter.add(core, (float)(this.x * 64), (float)(this.y * 64), -3.0F, 128.0F, 128.0F, 128.0F, 128.0F, 1.0F, 1.0F, this.rotation - 90.0F);
      if (this.drill.isClicked()) {
         this.drawHighlighting(shaper);
      }
   }

   @Override
   public void draw(SpriteRenderer spriter) {
      spriter.add(corner, (float)(this.x * 64), (float)(this.y * 64), -3.0F, 128.0F, 128.0F, 128.0F, 128.0F, 1.0F, 1.0F, 0.0F);
      spriter.add(corner, (float)(this.x * 64), (float)(this.y * 64), -3.0F, 128.0F, 128.0F, 128.0F, 128.0F, 1.0F, 1.0F, 90.0F);
      spriter.add(corner, (float)(this.x * 64), (float)(this.y * 64), -3.0F, 128.0F, 128.0F, 128.0F, 128.0F, 1.0F, 1.0F, 180.0F);
      spriter.add(corner, (float)(this.x * 64), (float)(this.y * 64), -3.0F, 128.0F, 128.0F, 128.0F, 128.0F, 1.0F, 1.0F, -90.0F);
   }

   @Override
   protected void saveData(NBT.Builder b) {
      super.saveData(b);
      b.Int("drill", this.drill.x * this.drill.layer.height + this.drill.y).Short("drillLayer", (short)this.drill.layer.getIndex());
   }

   @Override
   protected void loadData(NBT.CompoundTag tag) throws NBT.NBTException {
      super.loadData(tag);
      this.drillIndex = tag.Int("drill", -1);

      try {
         this.drillLayer = tag.Short("drillLayer");
      } catch (NBT.NBTException var3) {
         this.drillLayer = tag.Byte("drillLayer", (byte)0);
      }
   }

   @Override
   public void postLoad() {
      Layer l = Game.G.getLayer(this.drillLayer);
      if (l != null) {
         this.drill = (ShaftDrill)l.getStructure(this.drillIndex);
      }
   }
}
