package de.dakror.quarry.structure.base;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import de.dakror.common.libgdx.io.NBT;
import de.dakror.common.libgdx.render.SpriteRenderer;
import de.dakror.quarry.Quarry;
import de.dakror.quarry.util.SpriterDelegateBatch;

public abstract class PausableStructure<T extends PausableSchema> extends Structure<T> {
   public static final TextureRegion sleepingTex = Quarry.Q.atlas.findRegion("state_sleeping");
   protected boolean sleeping;

   protected PausableStructure(int x, int y, T schema) {
      super(x, y, schema);
   }

   @Override
   public boolean getButtonState(int buttonIndex) {
      return buttonIndex == 0 ? this.sleeping : false;
   }

   public void setSleeping(boolean sleeping) {
      this.sleeping = sleeping;
   }

   public boolean isSleeping() {
      return this.sleeping;
   }

   @Override
   public float getLoudness() {
      return (this.sleeping ? 0.1F : 1.0F) * super.getLoudness();
   }

   @Override
   public void drawFrame(SpriteRenderer spriter, ShapeRenderer shaper, SpriterDelegateBatch pfxBatch) {
      super.drawFrame(spriter, shaper, pfxBatch);
      if (this.sleeping) {
         float size = 24.0F * (1.0F + 0.3F * (MathUtils.sin(this.time * 2.0F * (float) Math.PI) * 0.5F + 0.5F));
         spriter.add(
            sleepingTex,
            (float)((this.x + this.getWidth()) * 64) - (size - 24.0F) / 2.0F - 60.0F,
            (float)((this.y + this.getHeight()) * 64) - (size - 24.0F) / 2.0F - 30.0F,
            -1.0F,
            size,
            size
         );
      }
   }

   @Override
   protected void saveData(NBT.Builder b) {
      super.saveData(b);
      b.Byte("sleeping", (byte)(this.sleeping ? 1 : 0));
   }

   @Override
   protected void loadData(NBT.CompoundTag tag) throws NBT.NBTException {
      super.loadData(tag);
      this.sleeping = tag.Byte("sleeping", (byte)0) == 1;
   }
}
