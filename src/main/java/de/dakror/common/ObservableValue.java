package de.dakror.common;

import java.util.Observable;

public class ObservableValue<T> extends Observable {
   private T value;

   public ObservableValue() {
      this(null);
   }

   public ObservableValue(T initialValue) {
      this.value = initialValue;
   }

   public void set(T newValue) {
      synchronized (this) {
         this.value = newValue;
      }

      this.setChanged();
      this.notifyObservers(newValue);
   }

   public T get() {
      return this.value;
   }
}
