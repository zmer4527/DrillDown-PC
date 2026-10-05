package de.dakror.quarry.util;

import de.dakror.common.libgdx.Pair;
import de.dakror.common.libgdx.io.NBT;
import de.dakror.quarry.scenes.Game;
import de.dakror.quarry.structure.base.Structure;
import de.dakror.quarry.structure.power.CopperCable;

public class CopyRegionHelper {
   public static int indexGlobalToRelative(int[] copyRegion, int index) {
      int x = index / Game.G.layer.height;
      int y = index % Game.G.layer.height;
      return (x - copyRegion[0]) * Game.G.layer.height + (y - copyRegion[1]);
   }

   public static int indexRelativeToGlobal(int[] copyRegion, int index) {
      int x = index / Game.G.layer.height;
      int y = index % Game.G.layer.height;
      return (x + copyRegion[0]) * Game.G.layer.height + y + copyRegion[1];
   }

   public static Structure<?> getStructure(Structure<?> context, int[] copyRegion, int index) {
      Pair<Structure<?>, NBT.CompoundTag> t = null;
      Structure<?> s = null;
      int ind = (index / Game.G.layer.height + copyRegion[0]) * Game.G.layer.height + index % Game.G.layer.height + copyRegion[1];
      if (context.layer == null) {
         t = (Pair<Structure<?>, NBT.CompoundTag>)Game.G.copyStructures.get(ind);
         if (t != null) {
            s = t.getKey();
         }
      } else if (s == null) {
         s = context.layer.getStructure(ind);
      }

      return s;
   }

   public static CopperCable getCable(Structure<?> context, int[] copyRegion, int index) {
      Pair<Structure<?>, NBT.CompoundTag> t = null;
      Structure<?> s = null;
      int ind = (index / Game.G.layer.height + copyRegion[0]) * Game.G.layer.height + index % Game.G.layer.height + copyRegion[1];
      if (context.layer == null) {
         t = (Pair<Structure<?>, NBT.CompoundTag>)Game.G.copyCables.get(ind);
         if (t != null) {
            s = t.getKey();
         }
      } else if (s == null) {
         s = context.layer.getCable(ind);
      }

      return (CopperCable)s;
   }

   public static Structure<?> getAny(Structure<?> context, int[] copyRegion, int index, boolean structuresFirst) {
      if (structuresFirst) {
         Structure<?> s = getStructure(context, copyRegion, index);
         return (Structure<?>)(s != null ? s : getCable(context, copyRegion, index));
      } else {
         CopperCable c = getCable(context, copyRegion, index);
         return (Structure<?>)(c != null ? c : getStructure(context, copyRegion, index));
      }
   }
}
