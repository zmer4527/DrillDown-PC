package net.spookygames.gdx.sfx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music.OnCompletionListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

public class SfxMusicPlaylist implements SfxMusic {
   private final Array<SfxMusicEffect> effects = new Array(0);
   private final Array<SfxMusic> content = new Array();
   private SfxMusic current = null;
   private int index = -1;
   private OnCompletionListener listener = null;
   private float volume = 1.0F;
   private float pan = 0.0F;
   private boolean play = false;
   private boolean pause = false;
   private boolean stopPending = false;
   private boolean repeat = false;

   public Array<SfxMusic> getContent() {
      return this.content;
   }

   public boolean isEmpty() {
      return this.content.size == 0;
   }

   public int size() {
      return this.content.size;
   }

   public void addMusic(SfxMusic music) {
      this.content.add(music);
   }

   public void containsMusic(SfxMusic music) {
      this.content.contains(music, true);
   }

   public boolean removeMusic(SfxMusic music) {
      int i = this.content.indexOf(music, true);
      if (i >= 0) {
         this.content.removeIndex(i);
         if (i < this.index) {
            this.index--;
         } else if (i == this.index) {
            if (this.content.size == 0) {
               this.stop();
            } else {
               this.next();
            }
         }

         return true;
      } else {
         return false;
      }
   }

   public void clearContent() {
      this.stop();
      this.content.clear();
   }

   public void setContent(Array<SfxMusic> playlist) {
      if (playlist != null && playlist.size != 0) {
         SfxMusic current = this.getCurrentlyPlayed();
         int newIndex = playlist.indexOf(current, true);
         if (newIndex < 0) {
            this.clearContent();
            this.content.addAll(playlist);
         } else {
            this.content.clear();
            this.content.addAll(playlist);
            this.index = newIndex;
         }
      } else {
         this.clearContent();
      }
   }

   public SfxMusic getCurrentlyPlayed() {
      return this.current;
   }

   @Override
   public String getTitle() {
      SfxMusic current = this.getCurrentlyPlayed();
      return current == null ? null : current.getTitle();
   }

   @Override
   public float getDuration() {
      float duration = 0.0F;
      Array<SfxMusic> content = this.content;
      int i = 0;

      for (int n = content.size; i < n; i++) {
         duration += ((SfxMusic)content.get(i)).getDuration();
      }

      return duration;
   }

   public void shuffle() {
      this.content.shuffle();
   }

   public void play() {
      if (!this.play) {
         if (this.content.size != 0) {
            this.play = true;
            this.stopPending = false;
            if (this.pause) {
               this.pause = false;
               if (this.current != null) {
                  this.current.play();
               }
            } else {
               this.next();
            }
         }
      }
   }

   public boolean isPlaying() {
      return this.play;
   }

   public void pause() {
      if (!this.pause && this.play) {
         this.pause = true;
         this.play = false;
         this.getCurrentlyPlayed().pause();
      }
   }

   public boolean isPaused() {
      return this.pause;
   }

   public void stop() {
      if (this.play) {
         this.stopPending = true;
         if (this.current != null) {
            this.current.stop();
         }

         this.resetIndex();
      }
   }

   public boolean isLooping() {
      return this.repeat;
   }

   public void setLooping(boolean isLooping) {
      this.repeat = isLooping;
   }

   public float getVolume() {
      return this.volume;
   }

   public void setVolume(float volume) {
      this.volume = volume;
      SfxMusic current = this.getCurrentlyPlayed();
      if (current != null) {
         current.setVolume(volume);
      }
   }

   @Override
   public float getPan() {
      return this.pan;
   }

   public void setPan(float pan) {
      this.pan = pan;
      SfxMusic current = this.getCurrentlyPlayed();
      if (current != null) {
         current.setPan(pan, this.volume);
      }
   }

   public void setPan(float pan, float volume) {
      this.pan = pan;
      SfxMusic current = this.getCurrentlyPlayed();
      if (current != null) {
         current.setPan(pan, volume);
      }
   }

   public void setPosition(float position) {
      throw new UnsupportedOperationException();
   }

   public float getPosition() {
      throw new UnsupportedOperationException();
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
         this.effects.add(effect);
      }

      SfxMusic current = this.getCurrentlyPlayed();
      if (current != null) {
         current.addEffect(effect);
      }
   }

   @Override
   public void removeEffect(SfxMusicEffect effect) {
      if (this.effects.removeValue(effect, true)) {
         SfxMusic current = this.getCurrentlyPlayed();
         if (current != null) {
            current.removeEffect(effect);
         }
      }
   }

   @Override
   public void clearEffects() {
      SfxMusic current = this.getCurrentlyPlayed();
      if (current != null) {
         int i = 0;

         for (int n = this.effects.size; i < n; i++) {
            current.removeEffect((SfxMusicEffect)this.effects.get(i));
         }
      }

      this.effects.clear();
   }

   @Override
   public boolean update(float deltaTime) {
      if (!this.play) {
         return true;
      } else {
         if (this.current == null || this.current.update(deltaTime)) {
            if (this.stopPending) {
               this.play = false;
               this.pause = false;
               return true;
            }

            this.next();
         }

         return false;
      }
   }

   public void next() {
      this.transition(this.nextMusic());
   }

   public void previous() {
      this.transition(this.previousMusic());
   }

   public void dispose() {
   }

   public OnCompletionListener getOnCompletionListener() {
      return this.listener;
   }

   public void setOnCompletionListener(OnCompletionListener listener) {
      this.listener = listener;
   }

   private void resetIndex() {
      this.index = -1;
   }

   private void tryPlay(SfxMusic music) {
      if (this.current != null) {
         this.removeEffectsFromMusic(this.current);
         this.current.stop();
      }

      this.addEffectsToMusic(music);
      music.setPan(this.pan, this.volume);
      music.play();
      if (music.isPlaying()) {
         this.current = music;
      } else {
         this.removeEffectsFromMusic(music);
         Gdx.app.debug("gdx-sfx", "Unable to actually play " + music);
      }
   }

   private void addEffectsToMusic(SfxMusic music) {
      int i = 0;

      for (int n = this.effects.size; i < n; i++) {
         music.addEffect((SfxMusicEffect)this.effects.get(i));
      }
   }

   private void removeEffectsFromMusic(SfxMusic music) {
      int i = 0;

      for (int n = this.effects.size; i < n; i++) {
         SfxMusicEffect effect = (SfxMusicEffect)this.effects.get(i);
         Pool pool = effect.getPool();
         music.removeEffect(effect);
         effect.setPool(pool);
      }
   }

   private void transition(SfxMusic music) {
      if (music == null) {
         this.stop();
      } else if (this.play) {
         this.tryPlay(music);
      }
   }

   private SfxMusic nextMusic() {
      this.index++;
      if (this.index >= this.content.size) {
         this.resetIndex();
         return this.repeat ? this.nextMusic() : null;
      } else {
         return (SfxMusic)this.content.get(this.index);
      }
   }

   private SfxMusic previousMusic() {
      this.index--;
      if (this.index < 0) {
         this.resetIndex();
         return null;
      } else {
         return (SfxMusic)this.content.get(this.index);
      }
   }

   @Override
   public String toString() {
      String output = "SfxMusicPlaylist";
      output = output + "\nPlaying? " + this.play + " - Paused? " + this.pause;
      output = output + "\nCurrent: " + this.getCurrentlyPlayed();
      output = output + "\nVolume: " + this.volume;
      return output + "\nPanning: " + this.pan;
   }
}
