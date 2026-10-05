package de.dakror.common.debug;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

public class TreeDebugger {
   private static final Set<Class<?>> WRAPPER_TYPES = getWrapperTypes();

   public static String getChildrenRecursively(Object object, int maxDepth, Class<?>... exceptions) {
      if (maxDepth <= 0) {
         return "{}";
      } else {
         VariableNode[] nodes = getChildren(object);
         StringBuilder sb = new StringBuilder();
         sb.append("{\"type\":\"");
         sb.append(object.getClass().getName());
         sb.append("\"");
         if (nodes.length > 0) {
            sb.append(",\"children\": {");

            label61:
            for (int i = 0; i < Math.min(20, nodes.length); i++) {
               VariableNode v = nodes[i];
               if (!v.name.contains("reflection")) {
                  if (v.realObject != null) {
                     if (v.realObject.getClass().isEnum()) {
                        continue;
                     }

                     for (Class<?> c : exceptions) {
                        if (c.isAssignableFrom(v.realObject.getClass()) || v.realObject.getClass().getClass().isAssignableFrom(c)) {
                           continue label61;
                        }
                     }
                  }

                  sb.append("\"");
                  sb.append(v.name);
                  sb.append("\":");
                  if (v.couldHaveChildren) {
                     sb.append(getChildrenRecursively(v.realObject, maxDepth - 1, exceptions));
                  } else if (v.realObject != null) {
                     if (v.realObject instanceof String) {
                        sb.append("\"");
                     }

                     sb.append(v.realObject);
                     if (v.realObject instanceof String) {
                        sb.append("\"");
                     }
                  } else {
                     sb.append("null");
                  }

                  if (i < nodes.length - 1) {
                     sb.append(",");
                  }
               }
            }

            sb.append("}");
         }

         sb.append("}");
         return sb.toString();
      }
   }

   public static VariableNode[] getChildren(Object object) {
      ArrayList<VariableNode> nodes = new ArrayList<>();
      if (object.getClass().isArray()) {
         int length = Array.getLength(object);

         for (int i = 0; i < length; i++) {
            nodes.add(new VariableNode("[" + i + "]", Array.get(object, i)));
         }
      } else {
         Field[] fields = getAllFields(object.getClass());

         for (Field f : fields) {
            try {
               f.setAccessible(true);
            } catch (SecurityException var10) {
               var10.printStackTrace();
            }

            try {
               nodes.add(new VariableNode(f.getName(), f.get(object)));
            } catch (IllegalArgumentException var8) {
               var8.printStackTrace();
            } catch (IllegalAccessException var9) {
               var9.printStackTrace();
            }
         }
      }

      return nodes.toArray(new VariableNode[0]);
   }

   private static Field[] getAllFields(Class<?> clazz) {
      ArrayList<Field> fields = new ArrayList<>();

      for (Class<?> c = clazz; c != null; c = c.getSuperclass()) {
         Collections.addAll(fields, c.getDeclaredFields());
      }

      Collections.sort(fields, new Comparator<Field>() {
         public int compare(Field o1, Field o2) {
            return o1.getName().compareTo(o2.getName());
         }
      });
      return fields.toArray(new Field[0]);
   }

   public static boolean isWrapperType(Class<?> clazz) {
      return WRAPPER_TYPES.contains(clazz);
   }

   private static Set<Class<?>> getWrapperTypes() {
      Set<Class<?>> ret = new HashSet<>();
      ret.add(Boolean.class);
      ret.add(Character.class);
      ret.add(Byte.class);
      ret.add(Short.class);
      ret.add(Integer.class);
      ret.add(Long.class);
      ret.add(Float.class);
      ret.add(Double.class);
      ret.add(Void.class);
      ret.add(String.class);
      return ret;
   }
}
