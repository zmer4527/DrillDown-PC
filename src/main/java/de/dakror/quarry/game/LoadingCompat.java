package de.dakror.quarry.game;

import de.dakror.common.libgdx.io.NBT;
import de.dakror.quarry.Quarry;
import de.dakror.quarry.structure.base.Structure;
import de.dakror.quarry.structure.base.StructureType;

public class LoadingCompat {
   public static final LoadingCompat instance = new LoadingCompat();

   private LoadingCompat() {
   }

   public void upgrade(NBT.CompoundTag tag, int build) {
      for (int i = build + 1; i <= Quarry.Q.versionNumber; i++) {
         try {
            this.getClass().getMethod("version_" + i, NBT.CompoundTag.class).invoke(this, tag);
         } catch (NoSuchMethodException var5) {
         } catch (Exception var6) {
            Quarry.Q.pi.message(1, var6);
         }
      }
   }

   public void version_2(NBT.CompoundTag data) throws NBT.NBTException {
      for (NBT.Tag t : data.List("Map", NBT.TagType.Compound).data) {
         NBT.ListTag chunks = ((NBT.CompoundTag)t).List("Chunks", NBT.TagType.Compound);

         for (NBT.Tag t1 : ((NBT.CompoundTag)t).List("Items", NBT.TagType.Compound).data) {
            NBT.CompoundTag i = (NBT.CompoundTag)t1;

            for (NBT.Tag t2 : chunks.data) {
               for (NBT.Tag t3 : ((NBT.CompoundTag)t2).List("Structures", NBT.TagType.Compound).data) {
                  NBT.CompoundTag str = (NBT.CompoundTag)t3;
                  if (str.Short("type") == StructureType.Conveyor.id
                     || str.Short("type") == StructureType.ConveyorBridge.id
                     || str.Short("type") == StructureType.Hopper.id) {
                     if (!str.has("Items")) {
                        str.add(new NBT.ListTag("Items", NBT.TagType.Compound));
                     }

                     if (str.Int("x") == i.Int("x") && str.Int("y") == i.Int("y")) {
                        str.List("Items", NBT.TagType.Compound).add(i);
                     }
                  }
               }
            }
         }
      }
   }

   public void version_16(NBT.CompoundTag data) throws NBT.NBTException {
      for (NBT.Tag t : data.List("Map", NBT.TagType.Compound).data) {
         NBT.ListTag chunks = ((NBT.CompoundTag)t).List("Chunks", NBT.TagType.Compound);

         for (NBT.Tag t1 : chunks.data) {
            NBT.ListTag structs = ((NBT.CompoundTag)t1).List("Structures", NBT.TagType.Compound);

            for (NBT.Tag t2 : structs.data) {
               NBT.CompoundTag struct = (NBT.CompoundTag)t2;
               if (struct.has("activeTypes")) {
                  struct.add(new NBT.ByteArrayTag("activeCats", new byte[struct.ShortArray("activeTypes").length]));
               }
            }
         }
      }
   }

   public void version_20(NBT.CompoundTag data) throws NBT.NBTException {
      for (NBT.Tag tag : data.query("#Structures int,#power")) {
         tag.parent.remove(tag);
         tag.parent.add(new NBT.FloatTag("power", (float)((NBT.IntTag)tag).data));
      }
   }

   public void version_25(NBT.CompoundTag data) throws NBT.NBTException {
      for (NBT.Tag tag : data.query("#Structures>compound")) {
         NBT.CompoundTag t = (NBT.CompoundTag)tag;
         if (Structure.types[t.Short("type") & '\uffff'] == StructureType.CableShaft) {
            t.parent.remove(t);
         }
      }
   }

   public void version_109(NBT.CompoundTag data) throws NBT.NBTException {
      for (NBT.Tag tag : data.query("#Structures byte,#output")) {
         ((NBT.ByteTag)tag).data = 1;
      }
   }
}
