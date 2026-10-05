package de.dakror.common.libgdx.render;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Disposable;

public interface SpriteRenderer extends Disposable {
   void add(TextureRegion var1, float var2, float var3, float var4, float var5, float var6);

   void add(TextureRegion var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8, float var9, float var10, float var11);

   void add(float var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8, float var9);

   void add(
      float var1,
      float var2,
      float var3,
      float var4,
      float var5,
      float var6,
      float var7,
      float var8,
      float var9,
      float var10,
      float var11,
      float var12,
      float var13,
      float var14,
      boolean var15,
      boolean var16
   );

   void flush();

   ShaderProgram getShader();
}
