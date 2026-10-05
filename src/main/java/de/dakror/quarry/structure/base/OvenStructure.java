package de.dakror.quarry.structure.base;

import com.badlogic.gdx.graphics.g2d.ParticleEffectPool.PooledEffect;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import de.dakror.common.libgdx.render.SpriteRenderer;
import de.dakror.quarry.scenes.Game;
import de.dakror.quarry.util.Bounds;
import de.dakror.quarry.util.SpriterDelegateBatch;

public abstract class OvenStructure extends ProducerStructure {
   protected PooledEffect pfx;
   SpriterDelegateBatch pfxBatch;

   protected OvenStructure(int x, int y, ProducerStructure.ProducerSchema schema) {
      super(x, y, schema);
   }

   @Override
   public void onPlacement(boolean fromLoading) {
      super.onPlacement(fromLoading);
      this.pfx = (PooledEffect)Game.G.firePfx.obtain();
      this.pfx.reset();
      this.initPfx();
   }

   protected abstract void initPfx();

   @Override
   public void update(double deltaTime, int gameSpeed, Bounds dirtyBounds) {
      super.update(deltaTime, gameSpeed, dirtyBounds);
      if (this.pfxBatch != null && this.pfx != null) {
         this.pfx.update((float)(deltaTime * (double)gameSpeed));
      }
   }

   @Override
   public void drawFrame(SpriteRenderer spriter, ShapeRenderer shaper, SpriterDelegateBatch pfxBatch) {
      super.drawFrame(spriter, shaper, pfxBatch);
      this.pfxBatch = pfxBatch;
      if (!this.sleeping && this.activeRecipe != null && (this.hasCapacity = this.hasCapacityForProduction()) && !this.noPower) {
         if (pfxBatch != null && this.pfx != null) {
            pfxBatch.setNextZ(-1.0F);
            this.pfx.draw(pfxBatch);
         }
      }
   }

   @Override
   public void onDestroy() {
      super.onDestroy();
      if (this.pfx != null) {
         this.pfx.free();
      }
   }
}
