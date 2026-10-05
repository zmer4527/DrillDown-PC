package de.dakror.quarry.util;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.math.Matrix4;
import de.dakror.common.libgdx.render.DepthSpriter;

public class SpriterDelegateBatch implements Batch {
   DepthSpriter spriter;
   float z;

   public SpriterDelegateBatch(DepthSpriter spriter) {
      this.spriter = spriter;
   }

   public void dispose() {
      this.spriter.dispose();
   }

   public void begin() {
      this.spriter.begin(true);
   }

   public void end() {
      this.spriter.end();
   }

   public void setColor(Color tint) {
      throw new RuntimeException("not implemented");
   }

   public void setColor(float r, float g, float b, float a) {
      throw new RuntimeException("not implemented");
   }

   public Color getColor() {
      throw new RuntimeException("not implemented");
   }

   public void setPackedColor(float packedColor) {
      throw new RuntimeException("not implemented");
   }

   public float getPackedColor() {
      throw new RuntimeException("not implemented");
   }

   public void setNextZ(float z) {
      this.z = z;
   }

   public void draw(
      Texture texture,
      float x,
      float y,
      float originX,
      float originY,
      float width,
      float height,
      float scaleX,
      float scaleY,
      float rotation,
      int srcX,
      int srcY,
      int srcWidth,
      int srcHeight,
      boolean flipX,
      boolean flipY
   ) {
      throw new RuntimeException("not implemented");
   }

   public void draw(Texture texture, float x, float y, float width, float height, int srcX, int srcY, int srcWidth, int srcHeight, boolean flipX, boolean flipY) {
      throw new RuntimeException("not implemented");
   }

   public void draw(Texture texture, float x, float y, int srcX, int srcY, int srcWidth, int srcHeight) {
      throw new RuntimeException("not implemented");
   }

   public void draw(Texture texture, float x, float y, float width, float height, float u, float v, float u2, float v2) {
      throw new RuntimeException("not implemented");
   }

   public void draw(Texture texture, float x, float y) {
      throw new RuntimeException("not implemented");
   }

   public void draw(Texture texture, float x, float y, float width, float height) {
      throw new RuntimeException("not implemented");
   }

   public void draw(Texture texture, float[] spriteVertices, int offset, int count) {
      if (this.spriter.getIdx() + 20 >= this.spriter.getVertexCount()) {
         this.flush();
      }

      int i = 0;
      float[] vert = this.spriter.getVertices();
      vert[this.spriter.inc()] = spriteVertices[i++];
      vert[this.spriter.inc()] = spriteVertices[i++];
      i++;
      vert[this.spriter.inc()] = this.z;
      vert[this.spriter.inc()] = spriteVertices[i++];
      vert[this.spriter.inc()] = spriteVertices[i++];
      vert[this.spriter.inc()] = spriteVertices[i++];
      vert[this.spriter.inc()] = spriteVertices[i++];
      i++;
      vert[this.spriter.inc()] = this.z;
      vert[this.spriter.inc()] = spriteVertices[i++];
      vert[this.spriter.inc()] = spriteVertices[i++];
      vert[this.spriter.inc()] = spriteVertices[i++];
      vert[this.spriter.inc()] = spriteVertices[i++];
      i++;
      vert[this.spriter.inc()] = this.z;
      vert[this.spriter.inc()] = spriteVertices[i++];
      vert[this.spriter.inc()] = spriteVertices[i++];
      vert[this.spriter.inc()] = spriteVertices[i++];
      vert[this.spriter.inc()] = spriteVertices[i++];
      i++;
      vert[this.spriter.inc()] = this.z;
      vert[this.spriter.inc()] = spriteVertices[i++];
      vert[this.spriter.inc()] = spriteVertices[i++];
   }

   public void draw(TextureRegion region, float x, float y) {
      throw new RuntimeException("not implemented");
   }

   public void draw(TextureRegion region, float x, float y, float width, float height) {
      throw new RuntimeException("not implemented");
   }

   public void draw(TextureRegion region, float x, float y, float originX, float originY, float width, float height, float scaleX, float scaleY, float rotation) {
      throw new RuntimeException("not implemented");
   }

   public void draw(
      TextureRegion region,
      float x,
      float y,
      float originX,
      float originY,
      float width,
      float height,
      float scaleX,
      float scaleY,
      float rotation,
      boolean clockwise
   ) {
      throw new RuntimeException("not implemented");
   }

   public void draw(TextureRegion region, float width, float height, Affine2 transform) {
      throw new RuntimeException("not implemented");
   }

   public void flush() {
      this.spriter.flush();
   }

   public void disableBlending() {
      throw new RuntimeException("not implemented");
   }

   public void enableBlending() {
      throw new RuntimeException("not implemented");
   }

   public void setBlendFunction(int srcFunc, int dstFunc) {
   }

   public void setBlendFunctionSeparate(int srcFuncColor, int dstFuncColor, int srcFuncAlpha, int dstFuncAlpha) {
   }

   public int getBlendSrcFunc() {
      return 0;
   }

   public int getBlendDstFunc() {
      return 0;
   }

   public int getBlendSrcFuncAlpha() {
      return 0;
   }

   public int getBlendDstFuncAlpha() {
      return 0;
   }

   public Matrix4 getProjectionMatrix() {
      return null;
   }

   public Matrix4 getTransformMatrix() {
      return null;
   }

   public void setProjectionMatrix(Matrix4 projection) {
      this.spriter.setProjectionMatrix(projection);
   }

   public void setTransformMatrix(Matrix4 transform) {
   }

   public void setShader(ShaderProgram shader) {
   }

   public ShaderProgram getShader() {
      return this.spriter.getShader();
   }

   public boolean isBlendingEnabled() {
      return false;
   }

   public boolean isDrawing() {
      return false;
   }
}
