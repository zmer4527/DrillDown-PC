package de.dakror.common;

@FunctionalInterface
public interface Callback<T> {
   void call(T var1);
}
