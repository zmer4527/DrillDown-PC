package de.dakror.quarry.util;

import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;
import com.badlogic.gdx.utils.Pool.Poolable;

public class Param implements Poolable {
   public static final Pool<Param> pool = Pools.get(Param.class, 10);
   private String name;
   private Param.Type type;

   public static Param make(String name, Param.Type type) {
      return ((Param)pool.obtain()).set(name, type);
   }

   public static void free(Param p) {
      pool.free(p);
   }

   public Param set(String name, Param.Type type) {
      this.name = name;
      this.type = type;
      return this;
   }

   public String getName() {
      return this.name;
   }

   public Param.Type getType() {
      return this.type;
   }

   public void reset() {
      this.name = null;
      this.type = null;
   }

   @Override
   public String toString() {
      return this.type != null ? String.format("Param [name=%s, type=%s]", this.name, this.type.name()) : "uninit Param";
   }

   public static enum Type {
      String,
      Long,
      Double,
      Boolean,
      Bytes;
   }
}
