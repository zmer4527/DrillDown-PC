package de.dakror.common.libgdx;

@FunctionalInterface
public interface PlatformInterface {
   int MSG_EXCEPTION = 1;
   int MSG_BROWSER_INTENT = 2;

   Object message(int var1, Object var2);
}
