package net.spookygames.gdx.sfx;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Music.OnCompletionListener;
import com.badlogic.gdx.utils.Array;

public class SfxMusicWrapper implements SfxMusic {
   private final Music wrapped;
   private final String title;
   private final float duration;
   private final Array<SfxMusicEffect> effects = new Array(0);
   private float position = 0.0F;
   private boolean paused = false;
   private boolean stopPending = false;
   private float pan = 0.0F;
   private OnCompletionListener listener = null;

   public SfxMusicWrapper(Music wrappedMusic, String title, float duration) {
      this.wrapped = wrappedMusic;
      this.title = title;
      this.duration = duration;
   }

   @Override
   public String getTitle() {
      return this.title;
   }

   @Override
   public float getDuration() {
      return this.duration;
   }

   public void play() {
      this.wrapped.play();
      if (this.paused) {
         this.paused = false;
      } else {
         this.position = 0.0F;
         Array<SfxMusicEffect> effects = this.effects;
         int i = 0;

         for (int n = effects.size; i < n; i++) {
            SfxMusicEffect effect = (SfxMusicEffect)effects.get(i);
            effect.restart();
         }
      }

      this.stopPending = false;
   }

   public void pause() {
      this.paused = true;
      this.wrapped.pause();
   }

   public void stop() {
      if (this.hasEffects()) {
         this.stopPending = true;
         Array<SfxMusicEffect> effects = this.effects;
         int i = 0;

         for (int n = effects.size; i < n; i++) {
            SfxMusicEffect effect = (SfxMusicEffect)effects.get(i);
            effect.stop(this.position);
         }
      } else {
         this.doStop();
      }
   }

   private void doStop() {
      this.wrapped.stop();
      if (this.listener != null) {
         this.listener.onCompletion(this);
      }
   }

   public boolean isPlaying() {
      return this.wrapped.isPlaying();
   }

   public void setLooping(boolean isLooping) {
      this.wrapped.setLooping(isLooping);
   }

   public boolean isLooping() {
      return this.wrapped.isLooping();
   }

   public void setVolume(float volume) {
      this.wrapped.setVolume(volume);
   }

   public float getVolume() {
      return this.wrapped.getVolume();
   }

   @Override
   public float getPan() {
      return this.pan;
   }

   public void setPan(float pan, float volume) {
      this.pan = pan;
      this.wrapped.setPan(pan, volume);
   }

   public float getPosition() {
      return this.position;
   }

   public void setPosition(float position) {
      this.wrapped.setPosition(position);
      this.position = position;
   }

   public void setOnCompletionListener(OnCompletionListener listener) {
      this.wrapped.setOnCompletionListener(listener);
      this.listener = listener;
   }

   @Override
   public Array<SfxMusicEffect> getEffects() {
      return this.effects;
   }

   @Override
   public boolean hasEffects() {
      return this.effects.size > 0;
   }

   @Override
   public void addEffect(SfxMusicEffect effect) {
      if (!this.effects.contains(effect, true)) {
         effect.setMusic(this);
         this.effects.add(effect);
      }
   }

   @Override
   public void removeEffect(SfxMusicEffect effect) {
      if (this.effects.removeValue(effect, true)) {
         effect.setMusic(null);
      }
   }

   @Override
   public void clearEffects() {
      for (int i = this.effects.size - 1; i >= 0; i--) {
         ((SfxMusicEffect)this.effects.get(i)).setMusic(null);
      }

      this.effects.clear();
   }

   @Override
   public boolean update(float deltaTime) {
      if (!this.isPlaying()) {
         this.position = 0.0F;
         return true;
      } else {
         float position = this.position + deltaTime;
         this.position = position;
         boolean effectsOver = true;
         Array<SfxMusicEffect> effects = this.effects;
         int size = effects.size;
         if (size > 0) {
            for (int i = 0; i < size; i++) {
               SfxMusicEffect effect = (SfxMusicEffect)effects.get(i);
               effectsOver = effect.update(position) && effectsOver;
            }
         }

         if (this.stopPending && effectsOver) {
            this.doStop();
            position = 0.0F;
            return true;
         } else {
            if (this.isLooping()) {
               float duration = this.getDuration();
               if (position > duration) {
                  position -= duration;

                  for (int i = 0; i < size; i++) {
                     SfxMusicEffect effect = (SfxMusicEffect)effects.get(i);
                     effect.restart();
                  }
               }
            }

            return false;
         }
      }
   }

   public void dispose() {
      this.clearEffects();
      this.wrapped.dispose();
   }

   @Override
   public String toString() {
      return this.title + " [" + this.getPosition() + "/" + this.duration + "] (SfxMusicWrapper)";
   }
}
