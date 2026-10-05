package net.spookygames.gdx.sfx.spatial;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Pool.Poolable;

public class SpatializedSound<T> implements Poolable {
   private Sound sound;
   private long id;
   private float duration;
   private T position;
   private float volume;
   private float pitch;
   private float pan;
   protected float elapsed;
   private boolean running = false;
   private boolean looping = false;

   public void reset() {
      if (this.sound != null) {
         this.sound.stop(this.id);
      }

      this.sound = null;
      this.id = -1L;
      this.duration = -1.0F;
      this.position = null;
      this.volume = 1.0F;
      this.pitch = 1.0F;
      this.pan = 0.0F;
      this.elapsed = Float.MAX_VALUE;
      this.running = false;
      this.looping = false;
   }

   public long initialize(Sound sound, boolean looping, float duration, T position, float volume, float pitch, float panning) {
      this.sound = sound;
      this.duration = duration;
      this.position = position;
      this.elapsed = 0.0F;
      this.running = true;
      return looping
         ? (this.id = sound.loop(this.volume = volume, this.pitch = pitch, this.pan = panning))
         : (this.id = sound.play(this.volume = volume, this.pitch = pitch, this.pan = panning));
   }

   public long getId() {
      return this.id;
   }

   public Sound getSound() {
      return this.sound;
   }

   public float getDuration() {
      return this.duration;
   }

   public T getPosition() {
      return this.position;
   }

   public void setPosition(T position) {
      this.position = position;
   }

   public float getPitch() {
      return this.pitch;
   }

   public void setPitch(float pitch) {
      if (this.pitch != pitch) {
         this.pitch = pitch;
         this.sound.setPitch(this.id, pitch);
      }
   }

   public float getVolume() {
      return this.volume;
   }

   public void setVolume(float volume) {
      if (this.volume != volume) {
         this.volume = volume;
         this.sound.setVolume(this.id, volume);
      }
   }

   public float getPan() {
      return this.pan;
   }

   public void setPan(float pan, float volume) {
      if (this.pan != pan || this.volume != volume) {
         this.pan = pan;
         this.volume = volume;
         this.sound.setPan(this.id, pan, volume);
      }
   }

   public boolean isLooping() {
      return this.looping;
   }

   public void setLooping(boolean looping) {
      if (this.looping != looping) {
         this.looping = looping;
         this.sound.setLooping(this.id, looping);
      }
   }

   public boolean update(float deltaTime) {
      if (this.running) {
         this.elapsed += deltaTime;
      }

      if (this.elapsed >= this.duration) {
         this.elapsed = this.elapsed - this.duration;
         if (this.looping) {
            return false;
         } else {
            this.running = false;
            return true;
         }
      } else {
         return false;
      }
   }

   public void stop() {
      this.reset();
   }

   public void resume() {
      this.sound.resume(this.id);
      this.running = true;
   }

   public void pause() {
      this.sound.pause(this.id);
      this.running = false;
   }
}
