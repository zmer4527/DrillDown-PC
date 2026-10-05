package de.dakror.common.libgdx.io;

import net.jpountz.lz4.LZ4Factory;
import net.jpountz.xxhash.XXHashFactory;

public class IOUtils {
   public static byte[] twiddleBoolArray(boolean[] array) {
      int len = (int)Math.ceil((double)((float)array.length / 8.0F));
      byte[] data = new byte[len];

      for (int i = 0; i < array.length; i++) {
         if (array[i]) {
            data[i / 8] = (byte)(data[i / 8] | 1 << i % 8);
         }
      }

      return data;
   }

   public static boolean[] getBoolArray(byte[] data) {
      boolean[] bools = new boolean[data.length * 8];

      for (int i = 0; i < bools.length; i++) {
         bools[i] = (data[i / 8] & 1 << i % 8) != 0;
      }

      return bools;
   }

   public static LZ4Factory getLZ4() {
      try {
         LZ4Factory nativ = LZ4Factory.nativeInstance();
         if (nativ != null) {
            return nativ;
         }
      } catch (Throwable var1) {
      }

      return LZ4Factory.safeInstance();
   }

   public static XXHashFactory getXXHash() {
      try {
         XXHashFactory nativ = XXHashFactory.nativeInstance();
         if (nativ != null) {
            return nativ;
         }
      } catch (Throwable var1) {
      }

      return XXHashFactory.safeInstance();
   }
}
