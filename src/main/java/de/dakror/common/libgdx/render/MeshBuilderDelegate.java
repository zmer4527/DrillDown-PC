package de.dakror.common.libgdx.render;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder.VertexInfo;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;

public class MeshBuilderDelegate implements SpriteRenderer {
   private static final VertexInfo vertTmp1 = new VertexInfo();
   private static final VertexInfo vertTmp2 = new VertexInfo();
   private static final VertexInfo vertTmp3 = new VertexInfo();
   private static final VertexInfo vertTmp4 = new VertexInfo();
   private static final MeshBuilder builder = new MeshBuilder();
   private Mesh mesh;
   private VertexAttributes attributes;
   private Texture texture;

   public MeshBuilderDelegate(VertexAttributes attributes, Texture texture) {
      this.attributes = attributes;
      this.texture = texture;
   }

   public Mesh getMesh() {
      return this.mesh;
   }

   public void begin() {
      if (this.mesh != null) {
         this.mesh.dispose();
         this.mesh = null;
      }

      builder.begin(this.attributes, 4);
   }

   public void end() {
      this.mesh = builder.end();
   }

   private VertexInfo set(VertexInfo i, float x, float y, float z, float u, float v) {
      return i.set(null, null, null, null).setPos(x, y, z).setUV(u, v);
   }

   @Override
   public void add(TextureRegion region, float x, float y, float z, float width, float height) {
      float fx2 = x + width;
      float fy2 = y + height;
      float u = region.getU();
      float v = region.getV2();
      float u2 = region.getU2();
      float v2 = region.getV();
      builder.rect(
         this.set(vertTmp1, x, y, z, u, v), this.set(vertTmp2, x, fy2, z, u, v2), this.set(vertTmp3, fx2, fy2, z, u2, v2), this.set(vertTmp4, fx2, y, z, u2, v)
      );
   }

   @Override
   public void add(
      TextureRegion region, float x, float y, float z, float originX, float originY, float width, float height, float scaleX, float scaleY, float rotation
   ) {
      float worldOriginX = x + originX;
      float worldOriginY = y + originY;
      float fx = -originX;
      float fy = -originY;
      float fx2 = width - originX;
      float fy2 = height - originY;
      if (scaleX != 1.0F || scaleY != 1.0F) {
         fx *= scaleX;
         fy *= scaleY;
         fx2 *= scaleX;
         fy2 *= scaleY;
      }

      float x1;
      float y1;
      float x2;
      float y2;
      float x3;
      float y3;
      float x4;
      float y4;
      if (rotation != 0.0F) {
         float cos = MathUtils.cosDeg(rotation);
         float sin = MathUtils.sinDeg(rotation);
         x1 = cos * fx - sin * fy;
         y1 = sin * fx + cos * fy;
         x2 = cos * fx - sin * fy2;
         y2 = sin * fx + cos * fy2;
         x3 = cos * fx2 - sin * fy2;
         y3 = sin * fx2 + cos * fy2;
         x4 = x1 + (x3 - x2);
         y4 = y3 - (y2 - y1);
      } else {
         x1 = fx;
         y1 = fy;
         x2 = fx;
         y2 = fy2;
         x3 = fx2;
         y3 = fy2;
         x4 = fx2;
         y4 = fy;
      }

      x1 += worldOriginX;
      y1 += worldOriginY;
      x2 += worldOriginX;
      y2 += worldOriginY;
      x3 += worldOriginX;
      y3 += worldOriginY;
      x4 += worldOriginX;
      y4 += worldOriginY;
      float u = region.getU();
      float v = region.getV2();
      float u2 = region.getU2();
      float v2 = region.getV();
      builder.rect(
         this.set(vertTmp1, x1, y1, z, u, v), this.set(vertTmp2, x2, y2, z, u, v2), this.set(vertTmp3, x3, y3, z, u2, v2), this.set(vertTmp4, x4, y4, z, u2, v)
      );
   }

   @Override
   public void add(float x, float y, float z, float width, float height, float u, float v, float u2, float v2) {
      float fx2 = x + width;
      float fy2 = y + height;
      builder.rect(
         this.set(vertTmp1, x, y, z, u, v), this.set(vertTmp2, x, fy2, z, u, v2), this.set(vertTmp3, fx2, fy2, z, u2, v2), this.set(vertTmp4, fx2, y, z, u2, v)
      );
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
      float worldOriginX = x + originX;
      float worldOriginY = y + originY;
      float fx = -originX;
      float fy = -originY;
      float fx2 = width - originX;
      float fy2 = height - originY;
      if (scaleX != 1.0F || scaleY != 1.0F) {
         fx *= scaleX;
         fy *= scaleY;
         fx2 *= scaleX;
         fy2 *= scaleY;
      }

      float x1;
      float y1;
      float x2;
      float y2;
      float x3;
      float y3;
      float x4;
      float y4;
      if (rotation != 0.0F) {
         float cos = MathUtils.cosDeg(rotation);
         float sin = MathUtils.sinDeg(rotation);
         x1 = cos * fx - sin * fy;
         y1 = sin * fx + cos * fy;
         x2 = cos * fx - sin * fy2;
         y2 = sin * fx + cos * fy2;
         x3 = cos * fx2 - sin * fy2;
         y3 = sin * fx2 + cos * fy2;
         x4 = x1 + (x3 - x2);
         y4 = y3 - (y2 - y1);
      } else {
         x1 = fx;
         y1 = fy;
         x2 = fx;
         y2 = fy2;
         x3 = fx2;
         y3 = fy2;
         x4 = fx2;
         y4 = fy;
      }

      x1 += worldOriginX;
      y1 += worldOriginY;
      x2 += worldOriginX;
      y2 += worldOriginY;
      x3 += worldOriginX;
      y3 += worldOriginY;
      x4 += worldOriginX;
      y4 += worldOriginY;
      float invTexWidth = 1.0F / (float)this.texture.getWidth();
      float invTexHeight = 1.0F / (float)this.texture.getHeight();
      float u = srcX * invTexWidth;
      float v = (srcY + srcHeight) * invTexHeight;
      float u2 = (srcX + srcWidth) * invTexWidth;
      float v2 = srcY * invTexHeight;
      if (flipX) {
         float tmp = u;
         u = u2;
         u2 = tmp;
      }

      if (flipY) {
         float tmp = v;
         v = v2;
         v2 = tmp;
      }

      builder.rect(
         this.set(vertTmp1, x1, y1, z, u, v), this.set(vertTmp2, x2, y2, z, u, v2), this.set(vertTmp3, x3, y3, z, u2, v2), this.set(vertTmp4, x4, y4, z, u2, v)
      );
   }

   @Override
   public void flush() {
   }

   @Override
   public ShaderProgram getShader() {
      return null;
   }

   public void dispose() {
      if (this.mesh != null) {
         this.mesh.dispose();
         this.mesh = null;
      }
   }
}
