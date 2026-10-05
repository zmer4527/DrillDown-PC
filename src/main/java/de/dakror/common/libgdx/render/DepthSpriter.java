package de.dakror.common.libgdx.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import java.nio.Buffer;

public class DepthSpriter implements SpriteRenderer {
   private final float[] vertices;
   private int idx;
   protected Mesh staticMesh;
   protected Mesh dynMesh;
   protected ShaderProgram shader;
   protected Texture texture;
   protected int staticCount;
   protected boolean dynamic;
   protected boolean drawing;
   protected boolean caching;
   protected Matrix4 projectionMatrix;
   public static final int VERTEX_SIZE = 5;

   public DepthSpriter(Texture atlas, int size) {
      this.texture = atlas;
      this.staticMesh = new Mesh(
         false, size * 4, size * 6, new VertexAttribute[]{new VertexAttribute(1, 3, "a_position"), new VertexAttribute(16, 2, "a_texCoord0")}
      );
      this.dynMesh = new Mesh(
         false, size * 4, size * 6, new VertexAttribute[]{new VertexAttribute(1, 3, "a_position"), new VertexAttribute(16, 2, "a_texCoord0")}
      );
      this.vertices = new float[size * 4 * 5];
      int length = size * 6;
      short[] indices = new short[length];
      short j = 0;

      for (int i = 0; i < length; j = (short)(j + 4)) {
         indices[i + 0] = j;
         indices[i + 1] = (short)(j + 1);
         indices[i + 2] = (short)(j + 2);
         indices[i + 3] = (short)(j + 2);
         indices[i + 4] = (short)(j + 3);
         indices[i + 5] = j;
         i += 6;
      }

      this.staticMesh.setIndices(indices);
      this.dynMesh.setIndices(indices);
      this.shader = new ShaderProgram(Gdx.files.internal("glsl/base.vs"), Gdx.files.internal("glsl/base.fs"));
      if (!this.shader.isCompiled()) {
         throw new IllegalArgumentException("Error compiling shader: " + this.shader.getLog());
      }
   }

   @Override
   public ShaderProgram getShader() {
      return this.shader;
   }

   public int getIdx() {
      return this.idx;
   }

   public int inc() {
      return this.idx++;
   }

   public float[] getVertices() {
      return this.vertices;
   }

   public int getVertexCount() {
      return this.vertices.length;
   }

   @Override
   public void add(TextureRegion region, float x, float y, float z, float width, float height) {
      if (this.idx + 20 >= this.vertices.length) {
         this.flush();
      }

      float fx2 = x + width;
      float fy2 = y + height;
      float u = region.getU();
      float v = region.getV2();
      float u2 = region.getU2();
      float v2 = region.getV();
      this.vertices[this.idx++] = x;
      this.vertices[this.idx++] = y;
      this.vertices[this.idx++] = z;
      this.vertices[this.idx++] = u;
      this.vertices[this.idx++] = v;
      this.vertices[this.idx++] = x;
      this.vertices[this.idx++] = fy2;
      this.vertices[this.idx++] = z;
      this.vertices[this.idx++] = u;
      this.vertices[this.idx++] = v2;
      this.vertices[this.idx++] = fx2;
      this.vertices[this.idx++] = fy2;
      this.vertices[this.idx++] = z;
      this.vertices[this.idx++] = u2;
      this.vertices[this.idx++] = v2;
      this.vertices[this.idx++] = fx2;
      this.vertices[this.idx++] = y;
      this.vertices[this.idx++] = z;
      this.vertices[this.idx++] = u2;
      this.vertices[this.idx++] = v;
   }

   @Override
   public void add(
      TextureRegion region, float x, float y, float z, float originX, float originY, float width, float height, float scaleX, float scaleY, float rotation
   ) {
      if (this.idx + 20 >= this.vertices.length) {
         this.flush();
      }

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
      this.vertices[this.idx++] = x1;
      this.vertices[this.idx++] = y1;
      this.vertices[this.idx++] = z;
      this.vertices[this.idx++] = u;
      this.vertices[this.idx++] = v;
      this.vertices[this.idx++] = x2;
      this.vertices[this.idx++] = y2;
      this.vertices[this.idx++] = z;
      this.vertices[this.idx++] = u;
      this.vertices[this.idx++] = v2;
      this.vertices[this.idx++] = x3;
      this.vertices[this.idx++] = y3;
      this.vertices[this.idx++] = z;
      this.vertices[this.idx++] = u2;
      this.vertices[this.idx++] = v2;
      this.vertices[this.idx++] = x4;
      this.vertices[this.idx++] = y4;
      this.vertices[this.idx++] = z;
      this.vertices[this.idx++] = u2;
      this.vertices[this.idx++] = v;
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
      if (this.idx + 20 >= this.vertices.length) {
         this.flush();
      }

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

      this.vertices[this.idx++] = x1;
      this.vertices[this.idx++] = y1;
      this.vertices[this.idx++] = z;
      this.vertices[this.idx++] = u;
      this.vertices[this.idx++] = v;
      this.vertices[this.idx++] = x2;
      this.vertices[this.idx++] = y2;
      this.vertices[this.idx++] = z;
      this.vertices[this.idx++] = u;
      this.vertices[this.idx++] = v2;
      this.vertices[this.idx++] = x3;
      this.vertices[this.idx++] = y3;
      this.vertices[this.idx++] = z;
      this.vertices[this.idx++] = u2;
      this.vertices[this.idx++] = v2;
      this.vertices[this.idx++] = x4;
      this.vertices[this.idx++] = y4;
      this.vertices[this.idx++] = z;
      this.vertices[this.idx++] = u2;
      this.vertices[this.idx++] = v;
   }

   @Override
   public void add(float x, float y, float z, float width, float height, float u, float v, float u2, float v2) {
      if (this.idx + 20 >= this.vertices.length) {
         this.flush();
      }

      float fx2 = x + width;
      float fy2 = y + height;
      this.vertices[this.idx++] = x;
      this.vertices[this.idx++] = y;
      this.vertices[this.idx++] = z;
      this.vertices[this.idx++] = u;
      this.vertices[this.idx++] = v;
      this.vertices[this.idx++] = x;
      this.vertices[this.idx++] = fy2;
      this.vertices[this.idx++] = z;
      this.vertices[this.idx++] = u;
      this.vertices[this.idx++] = v2;
      this.vertices[this.idx++] = fx2;
      this.vertices[this.idx++] = fy2;
      this.vertices[this.idx++] = z;
      this.vertices[this.idx++] = u2;
      this.vertices[this.idx++] = v2;
      this.vertices[this.idx++] = fx2;
      this.vertices[this.idx++] = y;
      this.vertices[this.idx++] = z;
      this.vertices[this.idx++] = u2;
      this.vertices[this.idx++] = v;
   }

   public void beginCache() {
      if (this.caching) {
         throw new IllegalStateException("endCache must be called before beginCache.");
      } else {
         this.idx = 0;
         this.caching = true;
      }
   }

   public void endCache() {
      this.caching = false;
      int spritesInBatch = this.idx / 20;
      int count = spritesInBatch * 6;
      this.staticMesh.setVertices(this.vertices, 0, this.idx);
      ((Buffer)this.staticMesh.getIndicesBuffer()).position(0);
      ((Buffer)this.staticMesh.getIndicesBuffer()).limit(count);
      this.staticCount = count;
   }

   public void clear() {
      this.idx = 0;
   }

   public void setProjectionMatrix(Matrix4 proj) {
      this.projectionMatrix = proj;
   }

   public void begin(boolean dynamic) {
      if (this.drawing) {
         throw new IllegalStateException("end must be called before begin.");
      } else {
         this.dynamic = dynamic;
         Gdx.gl.glEnable(2929);
         this.shader.begin();
         this.shader.setUniformMatrix("u_projectionViewMatrix", this.projectionMatrix);
         this.shader.setUniformi("u_texture", 0);
         if (dynamic) {
            this.idx = 0;
         }

         this.texture.bind();
         this.drawing = true;
      }
   }

   @Override
   public void flush() {
      boolean caching = this.caching;
      if (caching) {
         this.endCache();
         this.begin(false);
      }

      this.draw();
      this.clear();
      if (caching) {
         this.end();
         this.beginCache();
      }
   }

   public void draw() {
      if (!this.drawing) {
         throw new IllegalStateException("begin must be called before draw.");
      } else {
         if (this.dynamic) {
            int spritesInBatch = this.idx / 20;
            int count = spritesInBatch * 6;
            this.dynMesh.setVertices(this.vertices, 0, this.idx);
            ((Buffer)this.dynMesh.getIndicesBuffer()).position(0);
            ((Buffer)this.dynMesh.getIndicesBuffer()).limit(count);
            this.dynMesh.render(this.shader, 4, 0, count);
         } else {
            this.staticMesh.render(this.shader, 4, 0, this.staticCount);
         }
      }
   }

   public void end() {
      if (!this.drawing) {
         throw new IllegalStateException("begin must be called before end.");
      } else {
         this.drawing = false;
         this.shader.end();
         Gdx.gl.glDisable(2929);
      }
   }

   public void dispose() {
      this.staticMesh.dispose();
      this.shader.dispose();
   }
}
