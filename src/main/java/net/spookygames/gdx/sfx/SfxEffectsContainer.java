package net.spookygames.gdx.sfx;

import com.badlogic.gdx.utils.Array;

public interface SfxEffectsContainer {
   Array<SfxMusicEffect> getEffects();

   boolean hasEffects();

   void addEffect(SfxMusicEffect var1);

   void removeEffect(SfxMusicEffect var1);

   void clearEffects();
}
