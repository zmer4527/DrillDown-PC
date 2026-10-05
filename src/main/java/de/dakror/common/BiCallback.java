package de.dakror.common;

@FunctionalInterface
public interface BiCallback<U, V> {
   void call(U var1, V var2);
}
