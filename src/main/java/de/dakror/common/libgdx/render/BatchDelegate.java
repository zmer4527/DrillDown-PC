package de.dakror.common.libgdx.render;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class BatchDelegate implements SpriteRenderer {
   private Batch batch;
   private Texture atlas;

   public BatchDelegate(Texture atlas, Batch batch) {
      this.atlas = atlas;
      this.batch = batch;
   }

   @Override
   public void add(TextureRegion region, float x, float y, float z, float width, float height) {
      this.batch.draw(region, x, y, width, height);
   }

   @Override
   public void add(
      float x,
      float y,
      float z,
      float originX,
      float originY,
      float width,
      float height,
      float scaleX,
      float scaleY,
      float rotation,
      float srcX,
      float srcY,
      float srcWidth,
      float srcHeight,
      boolean flipX,
      boolean flipY
   ) {
      this.batch
         .draw(this.atlas, x, y, originX, originY, width, height, scaleX, scaleY, rotation, (int)srcX, (int)srcY, (int)srcWidth, (int)srcHeight, flipX, flipY);
   }

   @Override
   public void add(
      TextureRegion region, float x, float y, float z, float originX, float originY, float width, float height, float scaleX, float scaleY, float rotation
   ) {
      this.batch.draw(region, x, y, originX, originY, width, height, scaleX, scaleY, rotation);
   }

   @Override
   public void add(float x, float y, float z, float width, float height, float u, float v, float u2, float v2) {
      this.batch.draw(this.atlas, x, y, width, height, u, v, u2, v2);
   }

   @Override
   public void flush() {
      this.batch.flush();
   }

   public Batch getBatch() {
      return this.batch;
   }

   @Override
   public ShaderProgram getShader() {
      return this.batch.getShader();
   }

   public void dispose() {
      this.batch.dispose();
   }
}
