package de.dakror.common;

@FunctionalInterface
public interface Response<T, V> {
   V call(T var1);
}
