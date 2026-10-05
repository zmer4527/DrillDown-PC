package de.dakror.quarry.game;

import com.badlogic.gdx.math.RandomXS128;

public class Generator {
   public static final Generator G = new Generator();
   private RandomXS128 rng = new RandomXS128();
   private long seed;

   private Generator() {
   }

   public long getSeed() {
      return this.seed;
   }

   public void setSeed(long seed) {
      this.seed = seed;
      this.rng.setSeed(seed);
   }

   public long[] getState() {
      return new long[]{this.rng.getState(0), this.rng.getState(1)};
   }

   public void setState(long[] state) {
      this.rng.setState(state[0], state[1]);
   }

   public void generate(Layer layer) {
      for (Tile.OreType type : Tile.OreType.values) {
         this.generateVeins(layer, type);
      }
   }

   private void generateVeins(Layer layer, Tile.OreType type) {
      float probability = layer.getIndex() < 40 ? type.getPropability(layer.getIndex()) : this.rng.nextFloat() * (1.0F + this.rng.nextFloat());
      float radius = type.minVeinRadius + (type.maxVeinRadius - type.minVeinRadius) * probability;

      for (int i = 0; i < type.veins; i++) {
         if (probability > this.rng.nextFloat()) {
            this.generateVein(layer, radius, type.tile);
         }
      }

      if (layer.getIndex() == 0 && type == Tile.OreType.CopperOre) {
         int ix = 0;

         while (ix <= 15) {
            ix += this.generateVein(layer, radius, type.tile);
         }
      }
   }

   private int generateVein(Layer layer, float rad, Tile.TileType type) {
      float x = this.rng.nextFloat() * (float)layer.width;
      float y = this.rng.nextFloat() * (float)layer.height;
      float radius = rad + this.rng.nextFloat() - 0.5F;
      layer.veinDebugs.add(new Generator.VeinDebug(x + 0.5F, y + 0.5F, radius));
      int produced = 0;

      for (int j = (int)(x - radius - 1.0F); j < (int)(x + radius + 1.0F); j++) {
         for (int k = (int)(y - radius - 1.0F); k < (int)(y + radius + 1.0F); k++) {
            float dist = (float)Math.sqrt((double)(((float)j - x) * ((float)j - x) + ((float)k - y) * ((float)k - y)));
            if (!(dist >= radius)) {
               if (type.base != null) {
                  float dst = dist / radius;
                  float gs = (float)Math.abs(this.rng.nextGaussian());
                  float rnd = gs / 3.0F * dst;
                  if (!(0.2F >= rnd)) {
                     continue;
                  }

                  layer.set(j, k, type.base);
                  if (layer.getIndex() > 0) {
                     layer.addMeta(j, k, 1);
                  }
               }

               float dstx = dist / radius;
               float gsx = (float)Math.abs(this.rng.nextGaussian());
               float rndx = gsx / 3.0F * dstx;
               if (0.1F >= rndx) {
                  produced++;
                  layer.set(j, k, type);
                  if (layer.getIndex() > 0) {
                     layer.addMeta(j, k, 1);
                  }
               }
            }
         }
      }

      return produced;
   }

   public static class VeinDebug {
      public float x;
      public float y;
      public float radius;

      public VeinDebug(float x, float y, float radius) {
         this.x = x;
         this.y = y;
         this.radius = radius;
      }
   }
}
