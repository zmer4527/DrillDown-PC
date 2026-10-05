package de.dakror.common.libgdx.audio;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pool.Poolable;
import de.dakror.common.Callback;
import java.util.Iterator;

public class SoundManager {
   Array<SoundManager.SoundEntry> queue = new Array();
   Pool<SoundManager.SoundEntry> pool = new Pool<SoundManager.SoundEntry>(10, 100) {
      protected SoundManager.SoundEntry newObject() {
         return new SoundManager.SoundEntry();
      }
   };
   Music music;
   Music newMusic;
   float fadeProgress = -1.0F;
   boolean fadeOutMusic;
   boolean pauseMusic;
   boolean lastTickMusic;
   boolean playMusic;
   boolean playSound;
   float fadeSpeed;
   float soundVolume;
   float musicVolume;

   public SoundManager(float soundVolume, float musicVolume, float fadeSpeed) {
      this.fadeSpeed = fadeSpeed;
      this.soundVolume = soundVolume;
      this.musicVolume = musicVolume;
   }

   public void setMusicVolume(float musicVolume) {
      this.musicVolume = musicVolume;
      if (this.music != null) {
         this.music.setVolume(musicVolume);
      }
   }

   public float getMusicVolume() {
      return this.musicVolume;
   }

   public void setSoundVolume(float soundVolume) {
      this.soundVolume = soundVolume;
   }

   public float getSoundVolume() {
      return this.soundVolume;
   }

   public void setFadeSpeed(float fadeSpeed) {
      this.fadeSpeed = fadeSpeed;
   }

   public void setPlayMusic(boolean playMusic) {
      this.playMusic = playMusic;
   }

   public boolean isPlayMusic() {
      return this.playMusic;
   }

   public void setPlaySound(boolean playSound) {
      this.playSound = playSound;
   }

   public boolean isPlaySound() {
      return this.playSound;
   }

   public void update(float delta) {
      Iterator<SoundManager.SoundEntry> iter = this.queue.iterator();

      while (iter.hasNext()) {
         SoundManager.SoundEntry e = iter.next();
         e.delay -= delta;
         if (e.delay <= 0.0F) {
            if (this.playSound) {
               e.id = e.sound.play(this.soundVolume);
            }

            if (e.callback != null) {
               e.callback.call(e);
            }

            iter.remove();
         }
      }

      if (this.fadeProgress > -1.0F) {
         this.fadeProgress = this.fadeProgress + delta * this.fadeSpeed;
         if (this.newMusic != null) {
            this.newMusic.setVolume(this.musicVolume * Math.min(1.0F, this.fadeProgress));
            this.music.setVolume(this.musicVolume * (1.0F - Math.min(1.0F, this.fadeProgress)));
         } else if (this.fadeOutMusic) {
            this.music.setVolume(this.musicVolume * (1.0F - Math.min(1.0F, this.fadeProgress)));
         } else {
            this.music.setVolume(this.musicVolume * Math.min(1.0F, this.fadeProgress));
         }

         if (this.fadeProgress >= 1.0F) {
            this.fadeProgress = -1.0F;
            if (this.newMusic != null) {
               this.music.stop();
               this.music = this.newMusic;
               this.newMusic = null;
            }

            if (this.fadeOutMusic) {
               if (this.pauseMusic) {
                  this.music.pause();
               } else {
                  this.music.stop();
                  this.music = null;
               }

               this.fadeOutMusic = false;
               this.pauseMusic = false;
            }
         }
      }

      if (!this.fadeOutMusic && this.music != null && !this.playMusic && this.lastTickMusic && this.music.isPlaying()) {
         this.pauseMusic();
      }

      if (this.music != null && this.playMusic && !this.lastTickMusic && !this.music.isPlaying()) {
         this.resumeMusic();
      }

      this.lastTickMusic = this.playMusic;
   }

   public void stop(Sound sound) {
      sound.stop();
   }

   public void play(Sound sound) {
      this.play(sound, 0.0F, null);
   }

   public void play(Sound sound, float delay, Callback<SoundManager.SoundEntry> callback) {
      SoundManager.SoundEntry e = (SoundManager.SoundEntry)this.pool.obtain();
      e.sound = sound;
      e.delay = delay;
      e.callback = callback;
      this.queue.add(e);
   }

   public void pauseMusic() {
      if (this.music != null) {
         this.fadeProgress = 0.0F;
         this.fadeOutMusic = true;
         this.pauseMusic = true;
         this.newMusic = null;
      }
   }

   public void resumeMusic() {
      if (this.music != null) {
         this.fadeOutMusic = false;
         this.pauseMusic = false;
         this.fadeProgress = -1.0F;
         this.music.setVolume(this.musicVolume);
         if (!this.music.isPlaying()) {
            Music m = this.music;
            this.music = null;
            this.playMusic(m, true);
         }
      }
   }

   public void stopMusic() {
      if (this.music != null) {
         this.music.stop();
         this.fadeOutMusic = false;
         this.fadeProgress = -1.0F;
         this.music = null;
      }
   }

   public void playMusic(Music newMusic, boolean fade) {
      if (!this.playMusic) {
         if (this.music != null) {
            this.music.stop();
         }

         this.music = newMusic;
         newMusic.setLooping(true);
         newMusic.setVolume(0.0F);
         this.music.play();
         this.music.pause();
      } else if (fade) {
         if (this.music != null) {
            this.newMusic = newMusic;
            newMusic.setLooping(true);
            newMusic.setVolume(0.0F);
            newMusic.play();
            this.fadeOutMusic = false;
         } else {
            this.music = newMusic;
            this.music.setLooping(true);
            this.music.setVolume(0.0F);
            this.music.play();
            this.fadeOutMusic = false;
         }

         if (this.fadeProgress == -1.0F) {
            this.fadeProgress = 0.0F;
         }
      } else {
         if (this.music != null) {
            this.music.stop();
         }

         this.music = newMusic;
         newMusic.setLooping(true);
         newMusic.setVolume(this.musicVolume);
         newMusic.play();
         this.fadeProgress = -1.0F;
      }
   }

   public static class SoundEntry implements Poolable {
      Sound sound;
      float delay;
      Callback<SoundManager.SoundEntry> callback;
      long id;

      public void reset() {
         this.sound = null;
         this.delay = 0.0F;
         this.callback = null;
         this.id = 0L;
      }
   }
}
