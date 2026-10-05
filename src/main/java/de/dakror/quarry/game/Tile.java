package de.dakror.quarry.game;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import de.dakror.quarry.Quarry;

public class Tile {
   public static final Tile.TileType[] tiles = new Tile.TileType[256];

   static {
      for (Tile.TileType t : Tile.TileType.values) {
         if (tiles[t.value] != null) {
            throw new IllegalStateException("Tile with ID " + t.value + " is already taken!");
         }

         tiles[t.value] = t;
      }
   }

   public static enum OreType {
      Dirt(1, Tile.TileType.Dirt, 1.0F, 4.0F, 6, 0.5F, 0.9F, 2.0F, 0.5F, 0.8F),
      Clay(2, Tile.TileType.Clay, 2.0F, 3.0F, 5, 0.0F, 0.5F, 1.5F, 0.2F, 1.0F),
      IronOre(3, Tile.TileType.IronOre, 2.0F, 3.25F, 7, 0.0F, 1.5F, 1.5F, 0.01F, 1.0F),
      CopperOre(4, Tile.TileType.CopperOre, 2.0F, 3.25F, 8, 3.0F, 1.0F, 1.8F, 0.01F, 1.0F),
      CoalOre(5, Tile.TileType.CoalOre, 2.0F, 3.0F, 10, 0.5F, 0.6F, 1.2F, 0.25F, 1.0F),
      TinOre(6, Tile.TileType.TinOre, 2.0F, 3.7F, 6, 5.0F, 0.6F, 1.6F, 0.005F, 1.0F),
      CrudeOil(7, Tile.TileType.CrudeOil, 2.0F, 4.5F, 2, 13.0F, 0.75F, 3.0F, 1.0E-4F, 1.0F);

      public final Tile.TileType tile;
      public final byte id;
      public final float minVeinRadius;
      public final float maxVeinRadius;
      public final float min;
      public final float max;
      public final int veins;
      public final float offset;
      public final float leftUphold;
      public final float rightUphold;
      public static final Tile.OreType[] values = values();

      private OreType(
         int id,
         Tile.TileType tile,
         float minVeinRadius,
         float maxVeinRadius,
         int veins,
         float offset,
         float leftUphold,
         float rightUphold,
         float min,
         float max
      ) {
         this.tile = tile;
         this.minVeinRadius = minVeinRadius;
         this.maxVeinRadius = maxVeinRadius;
         this.veins = veins;
         this.offset = offset;
         this.leftUphold = leftUphold;
         this.rightUphold = rightUphold;
         this.min = min;
         this.max = max;
         this.id = (byte)id;
      }

      public float getPropability(int level) {
         float val = (this.max - this.min)
               * (float)Math.pow(
                  (float) Math.E,
                  -Math.pow((double)(((float)level - this.offset) / (2.0F * ((float)level < this.offset ? this.leftUphold : this.rightUphold))), 2.0)
               )
            + this.min;
         if (val < 1.0E-6F) {
            val = 0.0F;
         }

         return val;
      }
   }

   public static class TileMeta {
      public static final int NO_META = 0;
      public static final int FOG_OF_WAR = 1;
      public static final int ALT_TEX = 2;
      public static final int ROT_TEX = 4;
      public static final int ROT_TEX_90 = 8;
      public static final int ROT_TEX_180 = 16;
      public static final int ROT_TEX_270 = 32;
      public static final int BLENDING = 64;
      public static final int BASE_TILE = 128;
   }

   public static enum TileType {
      Air(0, null, null, 0),
      Stone(1, "stone", Item.ItemType.Stone, null, 128),
      Dirt(2, "dirt", Item.ItemType.Dirt, null, 192),
      Clay(3, "clay", Item.ItemType.Clay, Dirt, 0),
      IronOre(20, "iron", Item.ItemType.IronOre, Stone, 6),
      CoalOre(21, "coal", Item.ItemType.CoalOre, Stone, 6),
      CopperOre(22, "copper", Item.ItemType.CopperOre, Stone, 6),
      TinOre(25, "tin", Item.ItemType.TinOre, Stone, 6),
      CrudeOil(50, "crude_oil", Item.ItemType.CrudeOil, null, 64);

      public static final Tile.TileType[] values = values();
      public final byte value;
      public final Tile.TileType base;
      public final String name;
      public final String title;
      public final byte meta;
      public final Item.ItemType itemDrop;
      public TextureRegion tex;
      public TextureRegion texAlt;

      private TileType(int value, String name, Tile.TileType base, int meta) {
         this(value, name, null, base, meta);
      }

      private TileType(int value, String name, Item.ItemType itemDrop, Tile.TileType base, int meta) {
         this.value = (byte)value;
         this.title = name;
         this.name = name == null ? null : Quarry.Q.i18n.get("tile." + name);
         this.itemDrop = itemDrop;
         this.base = base;
         this.meta = (byte)meta;
         if (name != null) {
            this.tex = Quarry.Q.atlas.findRegion("tile_" + name);
            if ((meta & 2) == 2) {
               this.texAlt = Quarry.Q.atlas.findRegion("tile_" + name + "_alt");
            }
         }
      }
   }
}
