package net.spookygames.gdx.sfx;

import com.badlogic.gdx.audio.Music;

public interface SfxMusic extends Music, SfxAudio, SfxEffectsContainer, Updateable {
   float getPan();
}
