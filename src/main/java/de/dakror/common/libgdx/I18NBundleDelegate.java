package de.dakror.common.libgdx;

import com.badlogic.gdx.utils.I18NBundle;
import java.util.Locale;
import java.util.MissingResourceException;

public class I18NBundleDelegate {
   private I18NBundle bundle;

   public I18NBundleDelegate(I18NBundle bundle) {
      I18NBundle.setExceptionOnMissingKey(true);
      this.bundle = bundle;
   }

   public I18NBundle getBundle() {
      return this.bundle;
   }

   public Locale getLocale() {
      return this.bundle.getLocale();
   }

   public String get(String key) {
      try {
         return this.bundle.get(key).trim();
      } catch (MissingResourceException var3) {
         System.err.println("[LANG] '" + key + "'");
         return "???" + key + "???";
      }
   }

   public String format(String key, Object... args) {
      try {
         return this.bundle.format(key, args).trim();
      } catch (MissingResourceException var4) {
         System.err.println("Missing Translation \"" + key + "\"");
         return "???" + key + "???";
      }
   }
}
