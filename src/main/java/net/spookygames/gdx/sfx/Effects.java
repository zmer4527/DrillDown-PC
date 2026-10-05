package net.spookygames.gdx.sfx;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;

public class Effects {
   public static <T extends SfxMusicEffect> T effect(Class<T> type) {
      Pool<T> pool = Pools.get(type);
      T effect = (T)pool.obtain();
      effect.setPool(pool);
      return effect;
   }

   public static FadeIn fadeIn(float duration) {
      return fadeIn(duration, null);
   }

   public static FadeIn fadeIn(float duration, Interpolation interpolation) {
      FadeIn effect = effect(FadeIn.class);
      effect.setDuration(duration);
      effect.setInterpolation(interpolation);
      return effect;
   }

   public static FadeOut fadeOut(float duration) {
      return fadeOut(duration, null);
   }

   public static FadeOut fadeOut(float duration, Interpolation interpolation) {
      FadeOut effect = effect(FadeOut.class);
      effect.setDuration(duration);
      effect.setInterpolation(interpolation);
      return effect;
   }

   static {
      Pools.set(FadeIn.class, new Pool<FadeIn>() {
         protected FadeIn newObject() {
            return new FadeIn();
         }
      });
      Pools.set(FadeOut.class, new Pool<FadeOut>() {
         protected FadeOut newObject() {
            return new FadeOut();
         }
      });
   }
}
