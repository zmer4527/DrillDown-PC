package de.dakror.quarry.util;

import de.dakror.quarry.structure.base.Structure;
import net.spookygames.gdx.sfx.SfxSound;
import net.spookygames.gdx.sfx.spatial.FadingSpatializedSoundPlayer;

public class QuarrySoundPlayer extends FadingSpatializedSoundPlayer<Structure<?>> {
   public long play(Structure<?> position, SfxSound sound, float pitch, boolean looping) {
      return ((StructureSoundSpatializer)this.getSpatializer()).canPlayAt(position) ? super.play(position, sound, pitch, looping) : -1L;
   }

   public long play(Structure<?> position, SfxSound sound, float pitch, boolean looping, boolean fadeIn) {
      return ((StructureSoundSpatializer)this.getSpatializer()).canPlayAt(position) ? super.play(position, sound, pitch, looping, fadeIn) : -1L;
   }
}
