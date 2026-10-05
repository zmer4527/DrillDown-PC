package de.dakror.quarry.game;

import com.badlogic.gdx.math.RandomXS128;

/**
 * 模拟专用随机源。凡是会影响世界状态（存档内容、建筑产出等）的随机都必须走这里，
 * 保证「同种子 + 同调用顺序」可复现，为联机同步与回放打基础。
 *
 * 底层用 libGDX RandomXS128：纯位运算，跨 JVM 位级一致，且状态可以导出/恢复。
 * 注意不要把纯装饰性的随机（tile 变体贴图、音效音高）接到这里，否则渲染时机不同就会
 * 让随机序列错位；那类随机请用位置哈希（见 Layer.varRandom）。
 */
public class SimRandom {
   public static final SimRandom G = new SimRandom();
   private final RandomXS128 rng = new RandomXS128();
   private long seed;

   private SimRandom() {
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

   public int nextInt(int bound) {
      return this.rng.nextInt(bound);
   }

   public float nextFloat() {
      return this.rng.nextFloat();
   }

   public boolean nextBoolean() {
      return this.rng.nextBoolean();
   }
}
