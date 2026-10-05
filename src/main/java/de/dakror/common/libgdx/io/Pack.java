package de.dakror.common.libgdx.io;

import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;
import com.badlogic.gdx.utils.Pool.Poolable;

public class Pack implements Poolable {
   private static final Pool<Pack> POOL = Pools.get(Pack.class);
   protected final ObjectMap<String, Object> map = new ObjectMap();

   public static Pack get() {
      return (Pack)POOL.obtain();
   }

   public void free() {
      POOL.free(this);
   }

   public void reset() {
      this.map.clear();
   }

   public boolean has(String key) {
      return this.map.containsKey(key);
   }

   public boolean remove(String key) {
      return this.map.remove(key) != null;
   }

   public Pack put(String key, Object value) {
      this.map.put(key, value);
      return this;
   }

   public Object get(String key) {
      return this.map.get(key);
   }

   public int getInt(String key) {
      return (Integer)this.get(key);
   }

   public long getLong(String key) {
      return (Long)this.get(key);
   }

   public double getDouble(String key) {
      return (Double)this.get(key);
   }

   public float getFloat(String key) {
      return (Float)this.get(key);
   }

   public String getString(String key) {
      return (String)this.get(key);
   }

   public byte[] getBytes(String key) {
      return (byte[])this.get(key);
   }

   public String[] keys() {
      return (String[])this.map.keys().toArray().toArray(String.class);
   }

   @Override
   public String toString() {
      return this.map.toString();
   }
}
