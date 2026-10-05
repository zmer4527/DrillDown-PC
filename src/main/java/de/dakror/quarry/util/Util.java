package de.dakror.quarry.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;
import de.dakror.common.libgdx.io.NBT;
import de.dakror.common.libgdx.lml.CustomTag;
import de.dakror.quarry.Quarry;
import de.dakror.quarry.game.Item;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Util {
   public static <T extends Actor> T lml(String file) {
      Array<Actor> arr = Quarry.Q.lml.parseTemplate(Gdx.files.internal("lml/" + file + ".xml"));
      if (arr.size == 0) {
         throw new RuntimeException("No actors found for LML file: lml/" + file + ".xml");
      } else {
         T a = (T)arr.first();
         if (a instanceof CustomTag) {
            ((CustomTag)a).postInit();
         }

         return a;
      }
   }

   public static <T extends Actor> T id(String id) {
      return (T)Quarry.Q.lml.getActorsMappedByIds().get(id);
   }

   public static String md5(String string) {
      try {
         MessageDigest md = MessageDigest.getInstance("MD5");
         return new BigInteger(1, md.digest(string.getBytes())).toString(16).toLowerCase();
      } catch (NoSuchAlgorithmException var2) {
         var2.printStackTrace();
         return null;
      }
   }

   public static long hash(String string) {
      long h = 1125899906842597L;
      int len = string.length();

      for (int i = 0; i < len; i++) {
         h = 31L * h + (long)string.charAt(i);
      }

      return h;
   }

   public static void NBTwriteInventory(NBT.Builder builder, Map<Item.ItemType, Integer> map) {
      // map 通常是 HashMap，直接迭代 entrySet() 的顺序取决于枚举的身份 hashCode，跨进程不稳定；
      // 这里按 item 数值排序后再写，保证同一份库存序列化出的字节完全一致（存档可复现、状态哈希可复现）。
      // 读侧 NBTreadInventory 是按对读回，与顺序无关，所以本改动不影响存档格式。
      List<Entry<Item.ItemType, Integer>> entries = new ArrayList<>(map.entrySet());
      entries.sort(Comparator.comparingInt(e -> e.getKey().value));
      short[] types = new short[entries.size()];
      int[] amounts = new int[entries.size()];

      for (int i = 0; i < entries.size(); i++) {
         types[i] = entries.get(i).getKey().value;
         amounts[i] = entries.get(i).getValue();
      }

      builder.ShortArray("Types", types).IntArray("Amounts", amounts);
   }

   public static int NBTreadInventory(NBT.CompoundTag tag, Map<Item.ItemType, Integer> map) throws NBT.NBTException {
      short[] types = tag.ShortArray("Types");
      int[] amounts = tag.IntArray("Amounts");
      if (types.length != amounts.length) {
         throw new IllegalStateException("Not the same amount of item types and amounts!");
      } else {
         int sum = 0;
         map.clear();

         for (int i = 0; i < types.length; i++) {
            Item.ItemType t = Item.get(types[i]);
            if (t != null) {
               map.put(t, amounts[i]);
               sum += amounts[i];
            }
         }

         return sum;
      }
   }
}
