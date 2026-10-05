package de.dakror.quarry.game;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import de.dakror.common.libgdx.io.NBT;
import java.util.Arrays;

/**
 * 世界状态哈希：把「会被存档的模拟状态」压成一个 64 位值（FNV-1a）。
 * 用来验证确定性——同种子、同 tick 必须得到同一个值；也是将来联机快照比对的雏形。
 *
 * 三条规则，破坏任意一条都会让哈希失去意义：
 * 1. CompoundTag 的 key 必须排序：{@link NBT.CompoundTag#data} 是 ObjectMap，迭代顺序不稳；
 * 2. List / 数组保持原顺序：{@link NBT.ListTag#data} 有序，且 Conveyor 的 "Items" 列表顺序就是槽位序
 *    （元素本身不含槽号，排序会丢掉语义）；
 * 3. 浮点按位参与（floatToIntBits / doubleToLongBits），不做任何舍入。
 *
 * 只覆盖「会进存档」的状态：不落档的派生量（电力网络、响度）、纯动画计时（Structure.time）、
 * 池化残留都不纳入——这正好也是将来快照同步的合理边界。
 */
public class SimHash {
   private static final long OFFSET_BASIS = -3750763034362895579L;
   private static final long PRIME = 1099511628211L;
   private long h = OFFSET_BASIS;

   public long value() {
      return this.h;
   }

   public void put(byte v) {
      this.mix(v & 255L, 1);
   }

   public void put(short v) {
      this.mix(v & 65535L, 2);
   }

   public void put(int v) {
      this.mix(v & 4294967295L, 4);
   }

   public void put(long v) {
      this.mix(v, 8);
   }

   public void put(boolean v) {
      this.put(v ? 1 : 0);
   }

   public void put(float v) {
      this.put(Float.floatToIntBits(v));
   }

   public void put(double v) {
      this.put(Double.doubleToLongBits(v));
   }

   public void put(String v) {
      if (v == null) {
         this.put(-1);
      } else {
         this.put(v.length());

         for (int i = 0; i < v.length(); i++) {
            this.put((short)v.charAt(i));
         }
      }
   }

   public void put(long[] v) {
      if (v == null) {
         this.put(-1);
      } else {
         this.put(v.length);

         for (long x : v) {
            this.put(x);
         }
      }
   }

   public void put(short[] v) {
      if (v == null) {
         this.put(-1);
      } else {
         this.put(v.length);

         for (short x : v) {
            this.put(x);
         }
      }
   }

   private void mix(long v, int bytes) {
      for (int i = 0; i < bytes; i++) {
         this.h ^= v >>> i * 8 & 255L;
         this.h *= PRIME;
      }
   }

   public void hashTag(NBT.Tag tag) {
      if (tag == null) {
         this.put(-1);
         return;
      }

      if (tag instanceof NBT.CompoundTag) {
         ObjectMap<String, NBT.Tag> data = ((NBT.CompoundTag)tag).data;
         String[] keys = new String[data.size];
         int i = 0;

         for (ObjectMap.Entry<String, NBT.Tag> e : data) {
            keys[i++] = e.key;
         }

         Arrays.sort(keys);
         this.put(keys.length);

         for (String k : keys) {
            this.put(k);
            this.hashTag(data.get(k));
         }
      } else if (tag instanceof NBT.ListTag) {
         Array<NBT.Tag> list = ((NBT.ListTag)tag).data;
         this.put(list.size);

         for (NBT.Tag t : list) {
            this.hashTag(t);
         }
      } else if (tag instanceof NBT.ByteArrayTag) {
         byte[] a = ((NBT.ByteArrayTag)tag).data;
         this.put(a.length);

         for (byte x : a) {
            this.put(x);
         }
      } else if (tag instanceof NBT.ShortArrayTag) {
         this.put(((NBT.ShortArrayTag)tag).data);
      } else if (tag instanceof NBT.IntArrayTag) {
         int[] a = ((NBT.IntArrayTag)tag).data;
         this.put(a.length);

         for (int x : a) {
            this.put(x);
         }
      } else if (tag instanceof NBT.LongArrayTag) {
         this.put(((NBT.LongArrayTag)tag).data);
      } else if (tag instanceof NBT.FloatArrayTag) {
         float[] a = ((NBT.FloatArrayTag)tag).data;
         this.put(a.length);

         for (float x : a) {
            this.put(x);
         }
      } else if (tag instanceof NBT.StringTag) {
         this.put(((NBT.StringTag)tag).data);
      } else if (tag instanceof NBT.ByteTag) {
         this.put(((NBT.ByteTag)tag).data);
      } else if (tag instanceof NBT.ShortTag) {
         this.put(((NBT.ShortTag)tag).data);
      } else if (tag instanceof NBT.IntTag) {
         this.put(((NBT.IntTag)tag).data);
      } else if (tag instanceof NBT.LongTag) {
         this.put(((NBT.LongTag)tag).data);
      } else if (tag instanceof NBT.FloatTag) {
         this.put(((NBT.FloatTag)tag).data);
      } else if (tag instanceof NBT.DoubleTag) {
         this.put(((NBT.DoubleTag)tag).data);
      } else {
         this.put(tag.getClass().getName());
      }
   }
}
