package de.dakror.common.libgdx;

import com.badlogic.gdx.utils.Pool.Poolable;
import java.util.Objects;

public class Pair<K, V> implements Poolable {
   protected K key;
   protected V val;

   public Pair() {
   }

   public Pair(K key, V val) {
      this.key = key;
      this.val = val;
   }

   public Pair<K, V> set(K key, V val) {
      this.key = key;
      this.val = val;
      return this;
   }

   public K getKey() {
      return this.key;
   }

   public V getVal() {
      return this.val;
   }

   public void reset() {
      this.key = null;
      this.val = null;
   }

   @Override
   public boolean equals(Object obj) {
      return !(obj instanceof Pair)
         ? false
         : (this.key == ((Pair)obj).key || this.key.equals(((Pair)obj).key)) && (this.val == ((Pair)obj).val || this.val.equals(((Pair)obj).val));
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.key, this.val);
   }
}
