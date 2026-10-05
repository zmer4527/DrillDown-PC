package net.spookygames.gdx.sfx.spatial;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.LongMap;
import com.badlogic.gdx.utils.Pool;
import java.util.Iterator;
import net.spookygames.gdx.sfx.SfxSound;

public class SpatializedSoundPlayer<T> {
   protected final Pool<SpatializedSound<T>> pool = new Pool<SpatializedSound<T>>() {
      protected SpatializedSound<T> newObject() {
         return SpatializedSoundPlayer.this.newObject();
      }
   };
   protected final LongMap<SpatializedSound<T>> sounds = new LongMap();
   protected Spatializer<T> spatializer;
   protected float volume = 1.0F;

   public Spatializer<T> getSpatializer() {
      return this.spatializer;
   }

   public void setSpatializer(Spatializer<T> spatializer) {
      this.spatializer = spatializer;
   }

   public float getVolume() {
      return this.volume;
   }

   public void setVolume(float volume) {
      this.volume = volume;
   }

   public long play(T position, SfxSound sound) {
      return this.play(position, sound, 1.0F, false);
   }

   public long play(T position, SfxSound sound, float pitch, boolean looping) {
      SpatializedSound<T> instance = (SpatializedSound<T>)this.pool.obtain();
      float duration = sound.getDuration();
      Spatializer<T> spatializer = this.spatializer;
      long id = instance.initialize(sound, looping, duration, position, 0.0F, pitch, 0.0F);
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

   public void update(float delta) {
      Spatializer<T> spatializer = this.spatializer;
      Iterator<SpatializedSound<T>> iterator = this.sounds.values();

      while (iterator.hasNext()) {
         SpatializedSound<T> instance = iterator.next();
         if (instance.update(delta)) {
            iterator.remove();
            this.pool.free(instance);
         } else {
            spatializer.spatialize(instance, this.volume);
         }
      }
   }

   public void stop() {
      Pool<SpatializedSound<T>> pool = this.pool;
      LongMap<SpatializedSound<T>> sounds = this.sounds;

      for (SpatializedSound<T> object : sounds.values()) {
         pool.free(object);
      }

      sounds.clear();
   }

   public void stop(long id) {
      SpatializedSound<T> sound = (SpatializedSound<T>)this.sounds.get(id);
      if (sound != null) {
         sound.stop();
         this.pool.free(sound);
      }
   }

   public void pause(long id) {
      SpatializedSound<T> sound = (SpatializedSound<T>)this.sounds.get(id);
      if (sound != null) {
         sound.pause();
      }
   }

   public void resume(long id) {
      SpatializedSound<T> sound = (SpatializedSound<T>)this.sounds.get(id);
      if (sound != null) {
         sound.resume();
      }
   }

   protected SpatializedSound<T> newObject() {
      return new SpatializedSound<>();
   }
}
