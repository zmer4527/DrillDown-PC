package de.dakror.common.libgdx;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pool.Poolable;

public class ChangeNotifier<T> {
   private final Pool<ChangeNotifier.Event<T>> pool = new Pool<ChangeNotifier.Event<T>>() {
      protected ChangeNotifier.Event<T> newObject() {
         return new ChangeNotifier.Event<>();
      }
   };
   protected Array<ChangeNotifier.Listener<T>> listeners;
   protected final Object lock = new Object();

   public ChangeNotifier() {
      this.listeners = new Array();
   }

   public void notify(ChangeNotifier.Event.Type type, T data) {
      synchronized (this.lock) {
         ChangeNotifier.Event<T> e = (ChangeNotifier.Event<T>)this.pool.obtain();
         e.set(type, data);

         for (ChangeNotifier.Listener<T> l : this.listeners) {
            l.onChangeEvent(e);
         }

         this.pool.free(e);
      }
   }

   public void addListener(ChangeNotifier.Listener<T> listener) {
      this.listeners.add(listener);
   }

   public boolean removeListener(ChangeNotifier.Listener<T> listener) {
      return this.removeListener(listener, true);
   }

   public boolean removeListener(ChangeNotifier.Listener<T> listener, boolean identity) {
      synchronized (this.lock) {
         return this.listeners.removeValue(listener, identity);
      }
   }

   public static class Event<T> implements Poolable {
      protected ChangeNotifier.Event.Type type;
      protected T data;

      protected Event() {
      }

      public void set(ChangeNotifier.Event.Type type, T data) {
         this.type = type;
         this.data = data;
      }

      public T getData() {
         return this.data;
      }

      public ChangeNotifier.Event.Type getType() {
         return this.type;
      }

      public void reset() {
         this.type = null;
         this.data = null;
      }

      public static enum Type {
         ADD,
         REMOVE,
         CHANGE,
         RESET,
         BULK_ADD,
         BULD_REMOVE,
         BULK_CHANGE;
      }
   }

   public interface Listener<T> {
      void onChangeEvent(ChangeNotifier.Event<T> var1);
   }
}
