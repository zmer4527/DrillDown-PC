package net.spookygames.gdx.sfx;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pool.Poolable;

public abstract class SfxMusicEffect implements Poolable {
   protected SfxMusic music;
   private Pool pool;
   private float duration;
   private Interpolation interpolation = Interpolation.linear;
   private boolean began;
   private boolean complete;

   public SfxMusic getMusic() {
      return this.music;
   }

   public void setMusic(SfxMusic music) {
      this.music = music;
      if (music == null && this.pool != null) {
         this.pool.free(this);
         this.pool = null;
      }
   }

   public Pool getPool() {
      return this.pool;
   }

   public void setPool(Pool pool) {
      this.pool = pool;
   }

   public float getDuration() {
      return this.duration;
   }

   public void setDuration(float duration) {
      this.duration = duration;
   }

   public Interpolation getInterpolation() {
      return this.interpolation;
   }

   public void setInterpolation(Interpolation interpolation) {
      this.interpolation = interpolation == null ? Interpolation.linear : interpolation;
   }

   public void reset() {
      this.music = null;
      this.pool = null;
      this.interpolation = Interpolation.linear;
      this.restart();
   }

   public void restart() {
      this.began = false;
      this.complete = false;
   }

   public boolean update(float position) {
      if (this.complete) {
         return true;
      } else {
         Pool pool = this.getPool();
         this.setPool(null);

         boolean var3;
         try {
            if (!this.began) {
               this.begin();
               this.began = true;
            }

            this.complete = this.apply(position);
            if (this.complete) {
               this.end();
            }

            var3 = this.complete;
         } finally {
            this.setPool(pool);
         }

         return var3;
      }
   }

   protected void begin() {
   }

   protected void end() {
   }

   public void stop(float position) {
   }

   protected abstract boolean apply(float var1);
}
