package de.dakror.common.debug;

public class Delta {
   static long time;

   public static void t(String msg) {
      if (time > 0L) {
         long delta = System.nanoTime() - time;
         System.out.printf("%s, %,12d\n", msg != null ? msg : "", delta);
      }

      time = System.nanoTime();
   }

   public static void r() {
      time = System.nanoTime();
   }

   public static void t() {
      t(null);
   }
}
