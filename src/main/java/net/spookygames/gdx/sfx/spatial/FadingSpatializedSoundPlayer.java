package net.spookygames.gdx.sfx.spatial;

import com.badlogic.gdx.Gdx;
import java.util.Iterator;
import net.spookygames.gdx.sfx.SfxSound;

public class FadingSpatializedSoundPlayer<T> extends SpatializedSoundPlayer<T> {
   private float fadeTime = 0.0F;

   public void setFadeTime(float fadeTime) {
      this.fadeTime = fadeTime;
   }

   public float getFadeTime() {
      return this.fadeTime;
   }

   @Override
   protected SpatializedSound<T> newObject() {
      return new FadingSpatializedSound<>();
   }

   public long play(T position, SfxSound sound, float pitch, boolean looping, boolean fadeIn) {
      FadingSpatializedSound<T> instance = (FadingSpatializedSound<T>)this.pool.obtain();
      float duration = sound.getDuration();
      Spatializer<T> spatializer = this.spatializer;
      long id = instance.initialize(sound, looping, duration, position, 0.0F, pitch, 0.0F, this.fadeTime, fadeIn);
      if (id == -1L) {
         this.pool.free(instance);
         Gdx.app.error("gdx-sfx", "Couldn't play sound " + sound);
      } else {
         instance.setLooping(looping);
         spatializer.spatialize(instance, this.volume);
         this.sounds.put(id, instance);
      }

      return id;
   }

   @Override
   public void update(float delta) {
      Spatializer<T> spatializer = this.spatializer;
      Iterator<SpatializedSound<T>> iterator = this.sounds.values();

      while (iterator.hasNext()) {
         SpatializedSound<T> instance = iterator.next();
         if (instance.update(delta)) {
            iterator.remove();
            this.pool.free(instance);
         } else if (!((FadingSpatializedSound)instance).isFading()) {
            spatializer.spatialize(instance, this.volume);
         }
      }
   }

   @Override
   public void stop(long id) {
      SpatializedSound<T> sound = (SpatializedSound<T>)this.sounds.get(id);
      if (sound != null) {
         sound.stop();
      }
   }
}
