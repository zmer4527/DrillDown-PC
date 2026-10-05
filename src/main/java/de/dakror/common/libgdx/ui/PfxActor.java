package de.dakror.common.libgdx.ui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class PfxActor extends Actor {
   protected ParticleEffect pe;
   protected boolean started;

   public PfxActor(ParticleEffect e) {
      this.pe = e;
   }

   public void draw(Batch batch, float parentAlpha) {
      if (this.started) {
         this.pe.draw(batch);
      }
   }

   public void act(float delta) {
      if (this.started) {
         this.pe.update(delta);
      }
   }

   public void reset() {
      this.pe.reset();
      this.started = false;
   }

   public void start() {
      this.started = true;
   }

   public boolean isComplete() {
      return this.pe.isComplete();
   }
}
