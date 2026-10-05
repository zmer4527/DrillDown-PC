package de.dakror.quarry.util;

import net.spookygames.gdx.sfx.SfxSound;

public class Sfx {
   public SfxSound sound;
   public final String sfx;
   public final float pitch;

   public Sfx(String sfx) {
      this(sfx, 1.0F);
   }

   public Sfx(String sfx, float pitch) {
      this.sfx = sfx;
      this.pitch = pitch;
   }
}
