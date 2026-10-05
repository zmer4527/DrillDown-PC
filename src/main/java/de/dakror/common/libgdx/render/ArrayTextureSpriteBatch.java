package de.dakror.common.libgdx.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.LifecycleListener;
import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.Mesh.VertexDataType;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

public class ArrayTextureSpriteBatch implements Batch {
   private int idx = 0;
   private final Mesh mesh;
   private final float[] vertices;
   private final int spriteVertexSize = 5;
   private final int spriteFloatSize = 5;
   private final int maxTextureSlots;
   private static final boolean isWebGL = Gdx.app.getType().equals(ApplicationType.WebGL);
   private final Texture[] usedTextures;
   private int usedTexturesNextSwapSlot;
   private final IntBuffer FBO_READ_INTBUFF;
   private final FrameBuffer copyFramebuffer;
   private int arrayTextureHandle;
   private int arrayTextureMagFilter;
   private int arrayTextureMinFilter;
   private int maxTextureWidth;
   private int maxTextureHeight;
   private float invTexWidth;
   private float invTexHeight;
   private float invMaxTextureWidth;
   private float invMaxTextureHeight;
   private float subImageScaleWidth;
   private float subImageScaleHeight;
   private boolean drawing = false;
   private boolean useMipMaps = true;
   private boolean mipMapsDirty = true;
   private final Matrix4 transformMatrix = new Matrix4();
   private final Matrix4 projectionMatrix = new Matrix4();
   private final Matrix4 combinedMatrix = new Matrix4();
   private boolean blendingDisabled = false;
   private int blendSrcFunc = 770;
   private int blendDstFunc = 771;
   private int blendSrcFuncAlpha = 770;
   private int blendDstFuncAlpha = 771;
   private ShaderProgram shader = null;
   private ShaderProgram customShader = null;
   private boolean ownsShader;
   private final Color color = new Color(1.0F, 1.0F, 1.0F, 1.0F);
   private float colorPacked = Color.WHITE_FLOAT_BITS;
   public int renderCalls = 0;
   public int totalRenderCalls = 0;
   public int maxSpritesInBatch = 0;
   private int currentTextureLFUSize = 0;
   private int currentTextureLFUSwaps = 0;
   private final LifecycleListener contextRestoreListener;

   public ArrayTextureSpriteBatch(int maxSprites, int maxTextureWidth, int maxTextureHeight, int maxConcurrentTextures, int texFilterMag, int texFilterMin) throws IllegalStateException {
      this(maxSprites, maxTextureWidth, maxTextureHeight, maxConcurrentTextures, texFilterMag, texFilterMin, null);
   }

   public ArrayTextureSpriteBatch(
      int maxSprites, int maxTextureWidth, int maxTextureHeight, int maxConcurrentTextures, int texFilterMag, int texFilterMin, ShaderProgram defaultShader
   ) throws IllegalStateException {
      if (Gdx.gl30 == null) {
         throw new IllegalStateException("GL30 is not available. Remember to set \"useGL30 = true\" in your application config.");
      } else if (maxSprites > 8191) {
         throw new IllegalArgumentException("Can't have more than 8191 sprites per batch: " + maxSprites);
      } else if (maxConcurrentTextures < 1 || maxConcurrentTextures > 256) {
         throw new IllegalArgumentException("maxConcurrentTextures out of range [1,256]: " + maxConcurrentTextures);
      } else if (maxTextureWidth >= 1 && maxTextureHeight >= 1) {
         this.maxTextureSlots = maxConcurrentTextures;
         this.maxTextureWidth = maxTextureWidth;
         this.maxTextureHeight = maxTextureHeight;
         this.invMaxTextureWidth = 1.0F / (float)maxTextureWidth;
         this.invMaxTextureHeight = 1.0F / (float)maxTextureHeight;
         if (defaultShader == null) {
            this.shader = createDefaultShader();
            this.ownsShader = true;
         } else {
            this.shader = defaultShader;
            this.ownsShader = false;
         }

         this.FBO_READ_INTBUFF = ByteBuffer.allocateDirect(64).order(ByteOrder.nativeOrder()).asIntBuffer();
         this.usedTextures = new Texture[this.maxTextureSlots];
         this.arrayTextureMagFilter = texFilterMag;
         this.arrayTextureMinFilter = texFilterMin;
         this.initializeArrayTexture();
         this.copyFramebuffer = new FrameBuffer(Format.RGBA8888, maxTextureWidth, maxTextureHeight, false, false);
         this.mesh = new Mesh(
            VertexDataType.VertexBufferObjectWithVAO,
            false,
            maxSprites * 4,
            maxSprites * 6,
            new VertexAttribute[]{
               new VertexAttribute(1, 2, "a_position"),
               new VertexAttribute(4, 4, "a_color"),
               new VertexAttribute(16, 2, "a_texCoord0"),
               new VertexAttribute(32, 1, "texture_index")
            }
         );
         this.projectionMatrix.setToOrtho2D(0.0F, 0.0F, (float)Gdx.graphics.getWidth(), (float)Gdx.graphics.getHeight());
         this.vertices = new float[maxSprites * 9];
         int len = maxSprites * 6;
         short[] indices = new short[len];
         short j = 0;

         for (int i = 0; i < len; j = (short)(j + 4)) {
            indices[i] = j;
            indices[i + 1] = (short)(j + 1);
            indices[i + 2] = (short)(j + 2);
            indices[i + 3] = (short)(j + 2);
            indices[i + 4] = (short)(j + 3);
            indices[i + 5] = j;
            i += 6;
         }

         this.mesh.setIndices(indices);
         this.contextRestoreListener = new LifecycleListener() {
            final ApplicationType appType = Gdx.app.getType();

            public void resume() {
               if (this.appType == ApplicationType.Android) {
                  ArrayTextureSpriteBatch.this.initializeArrayTexture();
               }
            }

            public void pause() {
               if (this.appType == ApplicationType.Android) {
                  ArrayTextureSpriteBatch.this.disposeArrayTexture();
               }
            }

            public void dispose() {
            }
         };
         Gdx.app.addLifecycleListener(this.contextRestoreListener);
      } else {
         throw new IllegalArgumentException("Maximum Texture width / height must both be greater than zero: " + maxTextureWidth + " / " + maxTextureHeight);
      }
   }

   private void initializeArrayTexture() {
      this.currentTextureLFUSize = 0;
      this.arrayTextureHandle = Gdx.gl30.glGenTexture();
      Gdx.gl30.glBindTexture(35866, this.arrayTextureHandle);
      Gdx.gl30.glTexImage3D(35866, 0, 6408, this.maxTextureWidth, this.maxTextureHeight, this.maxTextureSlots, 0, 6408, 5121, null);
      this.setArrayTextureFilter(this.arrayTextureMagFilter, this.arrayTextureMinFilter);
      Gdx.gl30.glTexParameteri(35866, 10242, 10497);
      Gdx.gl30.glTexParameteri(35866, 10243, 10497);
      Gdx.gl30.glBindTexture(35866, 0);
   }

   private void disposeArrayTexture() {
      Gdx.gl30.glBindTexture(35866, 0);
      Gdx.gl30.glDeleteTexture(this.arrayTextureHandle);
   }

   public void dispose() {
      Gdx.app.removeLifecycleListener(this.contextRestoreListener);
      this.disposeArrayTexture();
      this.copyFramebuffer.dispose();
      this.mesh.dispose();
      if (this.ownsShader && this.shader != null) {
         this.shader.dispose();
      }
   }

   public void setArrayTextureFilter(int glTextureMagFilter, int glTextureMinFilter) {
      this.arrayTextureMagFilter = glTextureMagFilter;
      this.arrayTextureMinFilter = glTextureMinFilter;
      Gdx.gl30.glBindTexture(35866, this.arrayTextureHandle);
      Gdx.gl30.glTexParameteri(35866, 10240, glTextureMagFilter);
      Gdx.gl30.glTexParameteri(35866, 10241, glTextureMinFilter);
      if (glTextureMagFilter >= 9984 && glTextureMagFilter <= 9987) {
         this.useMipMaps = true;
      } else if (glTextureMinFilter >= 9984 && glTextureMinFilter <= 9987) {
         this.useMipMaps = true;
      } else {
         this.useMipMaps = false;
      }

      this.mipMapsDirty = this.useMipMaps;
   }

   public static ShaderProgram createDefaultShader() {
      String vertexShader = "in vec4 a_position;\nin vec4 a_color;\nin vec2 a_texCoord0;\nin float texture_index;\nuniform mat4 u_projTrans;\nout vec4 v_color;\nout vec2 v_texCoords;\nout float v_texture_index;\n\nvoid main()\n{\n   v_color = a_color;\n   v_color.a = v_color.a * (255.0/254.0);\n   v_texCoords = a_texCoord0;\n   v_texture_index = texture_index;\n   gl_Position =  u_projTrans * a_position;\n}\n";
      String fragmentShader = "#ifdef GL_ES\n#define LOWP lowp\nprecision mediump float;\n#else\n#define LOWP\n#endif\nin LOWP vec4 v_color;\nin vec2 v_texCoords;\nin float v_texture_index;\nuniform mediump sampler2DArray u_texturearray;\nout vec4 diffuseColor;\nvoid main()\n{\n  vec4 col = v_color * texture(u_texturearray, vec3(v_texCoords, v_texture_index));\n  if (col.a == 0.0) discard;\n  diffuseColor = col;\n}";
      ApplicationType appType = Gdx.app.getType();
      if (appType != ApplicationType.Android && appType != ApplicationType.iOS && appType != ApplicationType.WebGL) {
         vertexShader = "#version 150\n" + vertexShader;
         fragmentShader = "#version 150\n" + fragmentShader;
      } else {
         vertexShader = "#version 300 es\n" + vertexShader;
         fragmentShader = "#version 300 es\n" + fragmentShader;
      }

      ShaderProgram shader = new ShaderProgram(vertexShader, fragmentShader);
      if (!shader.isCompiled()) {
         throw new IllegalArgumentException("Error compiling shader: " + shader.getLog());
      } else {
         return shader;
      }
   }

   public void begin() {
      if (this.drawing) {
         throw new IllegalStateException("ArrayTextureSpriteBatch.end must be called before begin.");
      } else {
         this.renderCalls = 0;
         this.currentTextureLFUSwaps = 0;
         Gdx.gl30.glDepthMask(false);
         Gdx.gl30.glActiveTexture(33984);
         Gdx.gl30.glBindTexture(35866, this.arrayTextureHandle);
         if (this.customShader != null) {
            this.customShader.begin();
         } else {
            this.shader.begin();
         }

         this.setupMatrices();
         this.drawing = true;
      }
   }

   public void end() {
      if (!this.drawing) {
         throw new IllegalStateException("ArrayTextureSpriteBatch.begin must be called before end.");
      } else {
         if (this.idx > 0) {
            this.flush();
         }

         this.drawing = false;
         Gdx.gl30.glDepthMask(true);
         if (this.isBlendingEnabled()) {
            Gdx.gl30.glDisable(3042);
         }

         if (this.customShader != null) {
            this.customShader.end();
         } else {
            this.shader.end();
         }
      }
   }

   public void setColor(Color tint) {
      this.color.set(tint);
      this.colorPacked = tint.toFloatBits();
   }

   public void setColor(float r, float g, float b, float a) {
      this.color.set(r, g, b, a);
      this.colorPacked = this.color.toFloatBits();
   }

   public Color getColor() {
      return this.color;
   }

   public void setPackedColor(float packedColor) {
      Color.abgr8888ToColor(this.color, packedColor);
      this.colorPacked = packedColor;
   }

   public float getPackedColor() {
      return this.colorPacked;
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
      if (!this.drawing) {
         throw new IllegalStateException("ArrayTextureSpriteBatch.begin must be called before draw.");
      } else {
         float[] vertices = this.vertices;
         this.flushIfFull();
         float ti = (float)this.activateTexture(texture);
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
         float u = (float)srcX * this.invTexWidth;
         float v = (float)(srcY + srcHeight) * this.invTexHeight;
         float u2 = (float)(srcX + srcWidth) * this.invTexWidth;
         float v2 = (float)srcY * this.invTexHeight;
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

         float color = this.colorPacked;
         vertices[this.idx++] = x1;
         vertices[this.idx++] = y1;
         vertices[this.idx++] = color;
         vertices[this.idx++] = u;
         vertices[this.idx++] = v;
         vertices[this.idx++] = ti;
         vertices[this.idx++] = x2;
         vertices[this.idx++] = y2;
         vertices[this.idx++] = color;
         vertices[this.idx++] = u;
         vertices[this.idx++] = v2;
         vertices[this.idx++] = ti;
         vertices[this.idx++] = x3;
         vertices[this.idx++] = y3;
         vertices[this.idx++] = color;
         vertices[this.idx++] = u2;
         vertices[this.idx++] = v2;
         vertices[this.idx++] = ti;
         vertices[this.idx++] = x4;
         vertices[this.idx++] = y4;
         vertices[this.idx++] = color;
         vertices[this.idx++] = u2;
         vertices[this.idx++] = v;
         vertices[this.idx++] = ti;
      }
   }

   public void draw(Texture texture, float x, float y, float width, float height, int srcX, int srcY, int srcWidth, int srcHeight, boolean flipX, boolean flipY) {
      if (!this.drawing) {
         throw new IllegalStateException("ArrayTextureSpriteBatch.begin must be called before draw.");
      } else {
         float[] vertices = this.vertices;
         this.flushIfFull();
         float ti = (float)this.activateTexture(texture);
         float u = (float)srcX * this.invTexWidth;
         float v = (float)(srcY + srcHeight) * this.invTexHeight;
         float u2 = (float)(srcX + srcWidth) * this.invTexWidth;
         float v2 = (float)srcY * this.invTexHeight;
         float fx2 = x + width;
         float fy2 = y + height;
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

         float color = this.colorPacked;
         vertices[this.idx++] = x;
         vertices[this.idx++] = y;
         vertices[this.idx++] = color;
         vertices[this.idx++] = u;
         vertices[this.idx++] = v;
         vertices[this.idx++] = ti;
         vertices[this.idx++] = x;
         vertices[this.idx++] = fy2;
         vertices[this.idx++] = color;
         vertices[this.idx++] = u;
         vertices[this.idx++] = v2;
         vertices[this.idx++] = ti;
         vertices[this.idx++] = fx2;
         vertices[this.idx++] = fy2;
         vertices[this.idx++] = color;
         vertices[this.idx++] = u2;
         vertices[this.idx++] = v2;
         vertices[this.idx++] = ti;
         vertices[this.idx++] = fx2;
         vertices[this.idx++] = y;
         vertices[this.idx++] = color;
         vertices[this.idx++] = u2;
         vertices[this.idx++] = v;
         vertices[this.idx++] = ti;
      }
   }

   public void draw(Texture texture, float x, float y, int srcX, int srcY, int srcWidth, int srcHeight) {
      if (!this.drawing) {
         throw new IllegalStateException("ArrayTextureSpriteBatch.begin must be called before draw.");
      } else {
         float[] vertices = this.vertices;
         this.flushIfFull();
         float ti = (float)this.activateTexture(texture);
         float u = (float)srcX * this.invTexWidth;
         float v = (float)(srcY + srcHeight) * this.invTexHeight;
         float u2 = (float)(srcX + srcWidth) * this.invTexWidth;
         float v2 = (float)srcY * this.invTexHeight;
         float fx2 = x + (float)srcWidth;
         float fy2 = y + (float)srcHeight;
         float color = this.colorPacked;
         vertices[this.idx++] = x;
         vertices[this.idx++] = y;
         vertices[this.idx++] = color;
         vertices[this.idx++] = u;
         vertices[this.idx++] = v;
         vertices[this.idx++] = ti;
         vertices[this.idx++] = x;
         vertices[this.idx++] = fy2;
         vertices[this.idx++] = color;
         vertices[this.idx++] = u;
         vertices[this.idx++] = v2;
         vertices[this.idx++] = ti;
         vertices[this.idx++] = fx2;
         vertices[this.idx++] = fy2;
         vertices[this.idx++] = color;
         vertices[this.idx++] = u2;
         vertices[this.idx++] = v2;
         vertices[this.idx++] = ti;
         vertices[this.idx++] = fx2;
         vertices[this.idx++] = y;
         vertices[this.idx++] = color;
         vertices[this.idx++] = u2;
         vertices[this.idx++] = v;
         vertices[this.idx++] = ti;
      }
   }

   public void draw(Texture texture, float x, float y, float width, float height, float u, float v, float u2, float v2) {
      if (!this.drawing) {
         throw new IllegalStateException("ArrayTextureSpriteBatch.begin must be called before draw.");
      } else {
         float[] vertices = this.vertices;
         this.flushIfFull();
         float ti = (float)this.activateTexture(texture);
         float fx2 = x + width;
         float fy2 = y + height;
         float color = this.colorPacked;
         u *= this.subImageScaleWidth;
         v *= this.subImageScaleHeight;
         u2 *= this.subImageScaleWidth;
         v2 *= this.subImageScaleHeight;
         vertices[this.idx++] = x;
         vertices[this.idx++] = y;
         vertices[this.idx++] = color;
         vertices[this.idx++] = u;
         vertices[this.idx++] = v;
         vertices[this.idx++] = ti;
         vertices[this.idx++] = x;
         vertices[this.idx++] = fy2;
         vertices[this.idx++] = color;
         vertices[this.idx++] = u;
         vertices[this.idx++] = v2;
         vertices[this.idx++] = ti;
         vertices[this.idx++] = fx2;
         vertices[this.idx++] = fy2;
         vertices[this.idx++] = color;
         vertices[this.idx++] = u2;
         vertices[this.idx++] = v2;
         vertices[this.idx++] = ti;
         vertices[this.idx++] = fx2;
         vertices[this.idx++] = y;
         vertices[this.idx++] = color;
         vertices[this.idx++] = u2;
         vertices[this.idx++] = v;
         vertices[this.idx++] = ti;
      }
   }

   public void draw(Texture texture, float x, float y) {
      this.draw(texture, x, y, (float)texture.getWidth(), (float)texture.getHeight());
   }

   public void draw(Texture texture, float x, float y, float width, float height) {
      if (!this.drawing) {
         throw new IllegalStateException("ArrayTextureSpriteBatch.begin must be called before draw.");
      } else {
         float[] vertices = this.vertices;
         this.flushIfFull();
         float ti = (float)this.activateTexture(texture);
         float fx2 = x + width;
         float fy2 = y + height;
         float u = 0.0F;
         float v = this.subImageScaleWidth;
         float u2 = this.subImageScaleHeight;
         float v2 = 0.0F;
         float color = this.colorPacked;
         vertices[this.idx++] = x;
         vertices[this.idx++] = y;
         vertices[this.idx++] = color;
         vertices[this.idx++] = 0.0F;
         vertices[this.idx++] = v;
         vertices[this.idx++] = ti;
         vertices[this.idx++] = x;
         vertices[this.idx++] = fy2;
         vertices[this.idx++] = color;
         vertices[this.idx++] = 0.0F;
         vertices[this.idx++] = 0.0F;
         vertices[this.idx++] = ti;
         vertices[this.idx++] = fx2;
         vertices[this.idx++] = fy2;
         vertices[this.idx++] = color;
         vertices[this.idx++] = u2;
         vertices[this.idx++] = 0.0F;
         vertices[this.idx++] = ti;
         vertices[this.idx++] = fx2;
         vertices[this.idx++] = y;
         vertices[this.idx++] = color;
         vertices[this.idx++] = u2;
         vertices[this.idx++] = v;
         vertices[this.idx++] = ti;
      }
   }

   public void draw(Texture texture, float[] spriteVertices, int offset, int count) {
      if (!this.drawing) {
         throw new IllegalStateException("ArrayTextureSpriteBatch.begin must be called before draw.");
      } else {
         this.flushIfFull();
         float ti = (float)this.activateTexture(texture);

         for (int srcPos = 0; srcPos < count; srcPos += 5) {
            System.arraycopy(spriteVertices, srcPos, this.vertices, this.idx, 5);
            this.idx += 3;
            this.vertices[this.idx++] *= this.subImageScaleWidth;
            this.vertices[this.idx++] *= this.subImageScaleHeight;
            this.vertices[this.idx++] = ti;
         }
      }
   }

   public void draw(TextureRegion region, float x, float y) {
      this.draw(region, x, y, (float)region.getRegionWidth(), (float)region.getRegionHeight());
   }

   public void draw(TextureRegion region, float x, float y, float width, float height) {
      if (!this.drawing) {
         throw new IllegalStateException("ArrayTextureSpriteBatch.begin must be called before draw.");
      } else {
         float[] vertices = this.vertices;
         this.flushIfFull();
         float ti = (float)this.activateTexture(region.getTexture());
         float fx2 = x + width;
         float fy2 = y + height;
         float u = region.getU() * this.subImageScaleWidth;
         float v = region.getV2() * this.subImageScaleHeight;
         float u2 = region.getU2() * this.subImageScaleWidth;
         float v2 = region.getV() * this.subImageScaleHeight;
         float color = this.colorPacked;
         vertices[this.idx++] = x;
         vertices[this.idx++] = y;
         vertices[this.idx++] = color;
         vertices[this.idx++] = u;
         vertices[this.idx++] = v;
         vertices[this.idx++] = ti;
         vertices[this.idx++] = x;
         vertices[this.idx++] = fy2;
         vertices[this.idx++] = color;
         vertices[this.idx++] = u;
         vertices[this.idx++] = v2;
         vertices[this.idx++] = ti;
         vertices[this.idx++] = fx2;
         vertices[this.idx++] = fy2;
         vertices[this.idx++] = color;
         vertices[this.idx++] = u2;
         vertices[this.idx++] = v2;
         vertices[this.idx++] = ti;
         vertices[this.idx++] = fx2;
         vertices[this.idx++] = y;
         vertices[this.idx++] = color;
         vertices[this.idx++] = u2;
         vertices[this.idx++] = v;
         vertices[this.idx++] = ti;
      }
   }

   public void draw(TextureRegion region, float x, float y, float originX, float originY, float width, float height, float scaleX, float scaleY, float rotation) {
      if (!this.drawing) {
         throw new IllegalStateException("ArrayTextureSpriteBatch.begin must be called before draw.");
      } else {
         float[] vertices = this.vertices;
         this.flushIfFull();
         float ti = (float)this.activateTexture(region.getTexture());
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
         float u = region.getU() * this.subImageScaleWidth;
         float v = region.getV2() * this.subImageScaleHeight;
         float u2 = region.getU2() * this.subImageScaleWidth;
         float v2 = region.getV() * this.subImageScaleHeight;
         float color = this.colorPacked;
         vertices[this.idx++] = x1;
         vertices[this.idx++] = y1;
         vertices[this.idx++] = color;
         vertices[this.idx++] = u;
         vertices[this.idx++] = v;
         vertices[this.idx++] = ti;
         vertices[this.idx++] = x2;
         vertices[this.idx++] = y2;
         vertices[this.idx++] = color;
         vertices[this.idx++] = u;
         vertices[this.idx++] = v2;
         vertices[this.idx++] = ti;
         vertices[this.idx++] = x3;
         vertices[this.idx++] = y3;
         vertices[this.idx++] = color;
         vertices[this.idx++] = u2;
         vertices[this.idx++] = v2;
         vertices[this.idx++] = ti;
         vertices[this.idx++] = x4;
         vertices[this.idx++] = y4;
         vertices[this.idx++] = color;
         vertices[this.idx++] = u2;
         vertices[this.idx++] = v;
         vertices[this.idx++] = ti;
      }
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
      if (!this.drawing) {
         throw new IllegalStateException("ArrayTextureSpriteBatch.begin must be called before draw.");
      } else {
         float[] vertices = this.vertices;
         this.flushIfFull();
         float ti = (float)this.activateTexture(region.getTexture());
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
         float u2;
         float v2;
         float u3;
         float v3;
         float u4;
         float v4;
         float u1;
         float v1;
         if (clockwise) {
            u1 = region.getU2();
            v1 = region.getV2();
            u2 = region.getU();
            v2 = region.getV2();
            u3 = region.getU();
            v3 = region.getV();
            u4 = region.getU2();
            v4 = region.getV();
         } else {
            u1 = region.getU();
            v1 = region.getV();
            u2 = region.getU2();
            v2 = region.getV();
            u3 = region.getU2();
            v3 = region.getV2();
            u4 = region.getU();
            v4 = region.getV2();
         }

         u1 *= this.subImageScaleWidth;
         u2 *= this.subImageScaleWidth;
         u3 *= this.subImageScaleWidth;
         u4 *= this.subImageScaleWidth;
         v1 *= this.subImageScaleHeight;
         v2 *= this.subImageScaleHeight;
         v3 *= this.subImageScaleHeight;
         v4 *= this.subImageScaleHeight;
         float color = this.colorPacked;
         vertices[this.idx++] = x1;
         vertices[this.idx++] = y1;
         vertices[this.idx++] = color;
         vertices[this.idx++] = u1;
         vertices[this.idx++] = v1;
         vertices[this.idx++] = ti;
         vertices[this.idx++] = x2;
         vertices[this.idx++] = y2;
         vertices[this.idx++] = color;
         vertices[this.idx++] = u2;
         vertices[this.idx++] = v2;
         vertices[this.idx++] = ti;
         vertices[this.idx++] = x3;
         vertices[this.idx++] = y3;
         vertices[this.idx++] = color;
         vertices[this.idx++] = u3;
         vertices[this.idx++] = v3;
         vertices[this.idx++] = ti;
         vertices[this.idx++] = x4;
         vertices[this.idx++] = y4;
         vertices[this.idx++] = color;
         vertices[this.idx++] = u4;
         vertices[this.idx++] = v4;
         vertices[this.idx++] = ti;
      }
   }

   public void draw(TextureRegion region, float width, float height, Affine2 transform) {
      if (!this.drawing) {
         throw new IllegalStateException("ArrayTextureSpriteBatch.begin must be called before draw.");
      } else {
         float[] vertices = this.vertices;
         this.flushIfFull();
         float ti = (float)this.activateTexture(region.getTexture());
         float x1 = transform.m02;
         float y1 = transform.m12;
         float x2 = transform.m01 * height + transform.m02;
         float y2 = transform.m11 * height + transform.m12;
         float x3 = transform.m00 * width + transform.m01 * height + transform.m02;
         float y3 = transform.m10 * width + transform.m11 * height + transform.m12;
         float x4 = transform.m00 * width + transform.m02;
         float y4 = transform.m10 * width + transform.m12;
         float u = region.getU() * this.subImageScaleWidth;
         float v = region.getV2() * this.subImageScaleHeight;
         float u2 = region.getU2() * this.subImageScaleWidth;
         float v2 = region.getV() * this.subImageScaleHeight;
         float color = this.colorPacked;
         vertices[this.idx++] = x1;
         vertices[this.idx++] = y1;
         vertices[this.idx++] = color;
         vertices[this.idx++] = u;
         vertices[this.idx++] = v;
         vertices[this.idx++] = ti;
         vertices[this.idx++] = x2;
         vertices[this.idx++] = y2;
         vertices[this.idx++] = color;
         vertices[this.idx++] = u;
         vertices[this.idx++] = v2;
         vertices[this.idx++] = ti;
         vertices[this.idx++] = x3;
         vertices[this.idx++] = y3;
         vertices[this.idx++] = color;
         vertices[this.idx++] = u2;
         vertices[this.idx++] = v2;
         vertices[this.idx++] = ti;
         vertices[this.idx++] = x4;
         vertices[this.idx++] = y4;
         vertices[this.idx++] = color;
         vertices[this.idx++] = u2;
         vertices[this.idx++] = v;
         vertices[this.idx++] = ti;
      }
   }

   private void flushIfFull() {
      if (this.vertices.length - this.idx < 6) {
         this.flush();
      }
   }

   public void flush() {
      if (this.idx != 0) {
         this.renderCalls++;
         this.totalRenderCalls++;
         int spritesInBatch = this.idx / 9;
         if (spritesInBatch > this.maxSpritesInBatch) {
            this.maxSpritesInBatch = spritesInBatch;
         }

         int count = spritesInBatch * 6;
         if (this.useMipMaps && this.mipMapsDirty) {
            Gdx.gl30.glGenerateMipmap(35866);
            this.mipMapsDirty = false;
         }

         Mesh mesh = this.mesh;
         mesh.setVertices(this.vertices, 0, this.idx);
         ((Buffer)mesh.getIndicesBuffer()).position(0);
         ((Buffer)mesh.getIndicesBuffer()).limit(count);
         if (this.blendingDisabled) {
            Gdx.gl.glDisable(3042);
         } else {
            Gdx.gl.glEnable(3042);
            if (this.blendSrcFunc != -1) {
               Gdx.gl.glBlendFuncSeparate(this.blendSrcFunc, this.blendDstFunc, this.blendSrcFuncAlpha, this.blendDstFuncAlpha);
            }
         }

         if (this.customShader != null) {
            mesh.render(this.customShader, 4, 0, count);
         } else {
            mesh.render(this.shader, 4, 0, count);
         }

         this.idx = 0;
      }
   }

   private int activateTexture(Texture texture) {
      this.subImageScaleWidth = (float)texture.getWidth() * this.invMaxTextureWidth;
      this.subImageScaleHeight = (float)texture.getHeight() * this.invMaxTextureHeight;
      if (!(this.subImageScaleWidth > 1.0F) && !(this.subImageScaleHeight > 1.0F)) {
         this.invTexWidth = this.subImageScaleWidth / (float)texture.getWidth();
         this.invTexHeight = this.subImageScaleHeight / (float)texture.getHeight();
         int textureSlot = this.findTextureCacheIndex(texture);
         if (textureSlot >= 0) {
            if (textureSlot == this.usedTexturesNextSwapSlot) {
               this.usedTexturesNextSwapSlot = (this.usedTexturesNextSwapSlot + 1) % this.currentTextureLFUSize;
            }

            return textureSlot;
         } else if (this.currentTextureLFUSize < this.maxTextureSlots) {
            this.usedTextures[this.currentTextureLFUSize] = texture;
            this.copyTextureIntoArrayTexture(texture, this.currentTextureLFUSize);
            this.currentTextureLFUSwaps++;
            return this.currentTextureLFUSize++;
         } else {
            if (this.idx > 0) {
               this.flush();
            }

            int slot = this.usedTexturesNextSwapSlot;
            this.usedTexturesNextSwapSlot = (this.usedTexturesNextSwapSlot + 1) % this.currentTextureLFUSize;
            this.usedTextures[slot] = texture;
            this.copyTextureIntoArrayTexture(texture, slot);
            this.currentTextureLFUSwaps++;
            return slot;
         }
      } else {
         throw new IllegalStateException(
            "Texture "
               + texture.getTextureObjectHandle()
               + " is larger than the Array Texture: ["
               + texture.getWidth()
               + ","
               + texture.getHeight()
               + "] > ["
               + this.maxTextureWidth
               + ","
               + this.maxTextureHeight
               + "]"
         );
      }
   }

   private int findTextureCacheIndex(Texture texture) {
      int textureHandle = texture.getTextureObjectHandle();

      for (int i = 0; i < this.currentTextureLFUSize; i++) {
         if (textureHandle == this.usedTextures[i].getTextureObjectHandle()) {
            return i;
         }
      }

      return -1;
   }

   public void reloadTexture(Texture texture) {
      int textureSlot = this.findTextureCacheIndex(texture);
      if (textureSlot < 0) {
         this.activateTexture(texture);
      } else {
         this.copyTextureIntoArrayTexture(texture, textureSlot);
      }
   }

   private void copyTextureIntoArrayTexture(Texture texture, int slot) {
      int previousFrameBufferHandle = 0;
      if (!isWebGL) {
         Gdx.gl30.glGetIntegerv(36006, this.FBO_READ_INTBUFF);
         previousFrameBufferHandle = this.FBO_READ_INTBUFF.get(0);
      }

      this.copyFramebuffer.bind();
      Gdx.gl30.glFramebufferTexture2D(36008, 36064, 3553, texture.getTextureObjectHandle(), 0);
      Gdx.gl30.glReadBuffer(36064);
      Gdx.gl30.glBindTexture(35866, this.arrayTextureHandle);
      Gdx.gl30.glCopyTexSubImage3D(35866, 0, 0, 0, slot, 0, 0, this.copyFramebuffer.getWidth(), this.copyFramebuffer.getHeight());
      if (!isWebGL) {
         Gdx.gl30.glBindFramebuffer(36160, previousFrameBufferHandle);
      }

      if (this.useMipMaps) {
         this.mipMapsDirty = true;
      }
   }

   public int getTextureLFUSwaps() {
      return this.currentTextureLFUSwaps;
   }

   public int getTextureLFUSize() {
      return this.currentTextureLFUSize;
   }

   public int getTextureLFUCapacity() {
      return this.maxTextureSlots;
   }

   public void disableBlending() {
      if (!this.blendingDisabled) {
         this.flush();
         this.blendingDisabled = true;
      }
   }

   public void enableBlending() {
      if (this.blendingDisabled) {
         this.flush();
         this.blendingDisabled = false;
      }
   }

   public void setBlendFunction(int srcFunc, int dstFunc) {
      this.setBlendFunctionSeparate(srcFunc, dstFunc, srcFunc, dstFunc);
   }

   public void setBlendFunctionSeparate(int srcFuncColor, int dstFuncColor, int srcFuncAlpha, int dstFuncAlpha) {
      if (this.blendSrcFunc != srcFuncColor
         || this.blendDstFunc != dstFuncColor
         || this.blendSrcFuncAlpha != srcFuncAlpha
         || this.blendDstFuncAlpha != dstFuncAlpha) {
         this.flush();
         this.blendSrcFunc = srcFuncColor;
         this.blendDstFunc = dstFuncColor;
         this.blendSrcFuncAlpha = srcFuncAlpha;
         this.blendDstFuncAlpha = dstFuncAlpha;
      }
   }

   public int getBlendSrcFunc() {
      return this.blendSrcFunc;
   }

   public int getBlendDstFunc() {
      return this.blendDstFunc;
   }

   public int getBlendSrcFuncAlpha() {
      return this.blendSrcFuncAlpha;
   }

   public int getBlendDstFuncAlpha() {
      return this.blendDstFuncAlpha;
   }

   public boolean isBlendingEnabled() {
      return !this.blendingDisabled;
   }

   public boolean isDrawing() {
      return this.drawing;
   }

   public Matrix4 getProjectionMatrix() {
      return this.projectionMatrix;
   }

   public Matrix4 getTransformMatrix() {
      return this.transformMatrix;
   }

   public void setProjectionMatrix(Matrix4 projection) {
      if (this.drawing) {
         this.flush();
      }

      this.projectionMatrix.set(projection);
      if (this.drawing) {
         this.setupMatrices();
      }
   }

   public void setTransformMatrix(Matrix4 transform) {
      if (this.drawing) {
         this.flush();
      }

      this.transformMatrix.set(transform);
      if (this.drawing) {
         this.setupMatrices();
      }
   }

   private void setupMatrices() {
      this.combinedMatrix.set(this.projectionMatrix).mul(this.transformMatrix);
      if (this.customShader != null) {
         this.customShader.setUniformMatrix("u_projTrans", this.combinedMatrix);
      } else {
         this.shader.setUniformMatrix("u_projTrans", this.combinedMatrix);
      }
   }

   public void setShader(ShaderProgram shader) {
      if (this.drawing) {
         this.flush();
         if (this.customShader != null) {
            this.customShader.end();
         } else {
            this.shader.end();
         }
      }

      this.customShader = shader;
      if (this.drawing) {
         if (this.customShader != null) {
            this.customShader.begin();
         } else {
            this.shader.begin();
         }

         this.setupMatrices();
      }
   }

   public ShaderProgram getShader() {
      return this.customShader != null ? this.customShader : this.shader;
   }
}
