package de.dakror.common.libgdx.io;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Pools;
import com.badlogic.gdx.utils.ObjectMap.Values;
import com.badlogic.gdx.utils.Pool.Poolable;
import java.io.BufferedReader;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import net.jpountz.lz4.LZ4FrameInputStream;
import net.jpountz.lz4.LZ4FrameOutputStream;
import net.jpountz.lz4.LZ4FrameOutputStream.BLOCKSIZE;
import net.jpountz.lz4.LZ4FrameOutputStream.FLG.Bits;

public class NBT {
   static final NBT.TagType[] reverseTags = new NBT.TagType[256];
   protected static final NBT nbt = new NBT();
   protected DataInput input;
   protected DataOutput output;
   private static final Pattern textRegex = Pattern.compile("(?:([a-zA-Z]+)(?:\\(\"(\\w+)\"\\))?: ?(?:\\d+ entries of type ([a-zA-Z]+)|(.+)))|\\{|\\}");

   protected NBT() {
   }

   protected String readName() throws IOException {
      NBT.StringTag tag = this.readTag(NBT.TagType.String);
      return tag.data;
   }

   protected <T extends NBT.Tag> T readTag(NBT.TagType type) throws IOException {
      return this.readPayload(type, (Class<T>)type.clazz);
   }

   protected <T extends NBT.Tag> T readPayload(NBT.TagType type, Class<T> expected) throws IOException {
      NBT.Tag tag;
      tag = null;
      label80:
      switch (type) {
         case End:
            tag = (NBT.Tag)Pools.obtain(NBT.EndTag.class);
            break;
         case Byte:
            tag = (NBT.Tag)Pools.obtain(NBT.ByteTag.class);
            ((NBT.ByteTag)tag).data = this.input.readByte();
            break;
         case Short:
            tag = (NBT.Tag)Pools.obtain(NBT.ShortTag.class);
            ((NBT.ShortTag)tag).data = this.input.readShort();
            break;
         case Int:
            tag = (NBT.Tag)Pools.obtain(NBT.IntTag.class);
            ((NBT.IntTag)tag).data = this.input.readInt();
            break;
         case Long:
            tag = (NBT.Tag)Pools.obtain(NBT.LongTag.class);
            ((NBT.LongTag)tag).data = this.input.readLong();
            break;
         case Float:
            tag = (NBT.Tag)Pools.obtain(NBT.FloatTag.class);
            ((NBT.FloatTag)tag).data = this.input.readFloat();
            break;
         case Double:
            tag = (NBT.Tag)Pools.obtain(NBT.DoubleTag.class);
            ((NBT.DoubleTag)tag).data = this.input.readDouble();
            break;
         case ByteArray:
            tag = (NBT.Tag)Pools.obtain(NBT.ByteArrayTag.class);
            NBT.IntTag length7 = this.readTag(NBT.TagType.Int);
            byte[] data7 = new byte[length7.data];
            this.input.readFully(data7);
            ((NBT.ByteArrayTag)tag).data = data7;
            break;
         case String:
            tag = (NBT.Tag)Pools.obtain(NBT.StringTag.class);
            NBT.ShortTag length8 = this.readTag(NBT.TagType.Short);
            byte[] data8 = new byte[length8.data];
            this.input.readFully(data8);
            ((NBT.StringTag)tag).data = new String(data8, "UTF-8");
            break;
         case List:
            tag = (NBT.Tag)Pools.obtain(NBT.ListTag.class);
            NBT.ByteTag type9 = this.readTag(NBT.TagType.Byte);
            NBT.IntTag length9 = this.readTag(NBT.TagType.Int);
            NBT.TagType tagtype9 = NBT.TagType.values()[type9.data];
            ((NBT.ListTag)tag).elementType = tagtype9;

            for (int i = 0; i < length9.data; i++) {
               ((NBT.ListTag)tag).add(this.readTag(tagtype9));
            }
            break;
         case Compound:
            tag = (NBT.Tag)Pools.obtain(NBT.CompoundTag.class);

            while (true) {
               NBT.Tag t = this.readTag(true, null);
               if (t instanceof NBT.EndTag) {
                  break label80;
               }

               ((NBT.CompoundTag)tag).add(t);
            }
         case IntArray:
            tag = (NBT.Tag)Pools.obtain(NBT.IntArrayTag.class);
            NBT.IntTag length11 = this.readTag(NBT.TagType.Int);
            int[] data11 = new int[length11.data];

            for (int i = 0; i < data11.length; i++) {
               data11[i] = this.input.readInt();
            }

            ((NBT.IntArrayTag)tag).data = data11;
            break;
         case LongArray:
            tag = (NBT.Tag)Pools.obtain(NBT.LongArrayTag.class);
            NBT.IntTag length12 = this.readTag(NBT.TagType.Int);
            long[] data12 = new long[length12.data];

            for (int i = 0; i < data12.length; i++) {
               data12[i] = this.input.readLong();
            }

            ((NBT.LongArrayTag)tag).data = data12;
            break;
         case ShortArray:
            tag = (NBT.Tag)Pools.obtain(NBT.ShortArrayTag.class);
            NBT.IntTag length13 = this.readTag(NBT.TagType.Int);
            short[] data13 = new short[length13.data];

            for (int i = 0; i < data13.length; i++) {
               data13[i] = this.input.readShort();
            }

            ((NBT.ShortArrayTag)tag).data = data13;
            break;
         case FloatArray:
            tag = (NBT.Tag)Pools.obtain(NBT.FloatArrayTag.class);
            NBT.IntTag length14 = this.readTag(NBT.TagType.Int);
            float[] data14 = new float[length14.data];

            for (int i = 0; i < data14.length; i++) {
               data14[i] = this.input.readFloat();
            }

            ((NBT.FloatArrayTag)tag).data = data14;
            break;
         default:
            throw new IOException("Unknown Tag Type: " + type);
      }

      if (expected != null && !expected.isInstance(tag)) {
         throw new IOException("Invalid Tag Type! Expected \"" + expected.getSimpleName() + "\", got \"" + tag.getClass().getSimpleName() + "\"");
      } else {
         return (T)tag;
      }
   }

   protected <T extends NBT.Tag> T readTag(boolean named, Class<T> expected) throws IOException {
      byte type = this.input.readByte();
      String name = null;
      if (named && type != 0) {
         name = this.readName();
      }

      T tag = this.readPayload(reverseTags[type], expected);
      tag.name = name;
      return tag;
   }

   protected NBT.CompoundTag readFile(InputStream is, NBT.CompressionType compression) throws IOException {
      NBT.Tag.idCounter = 0;
      InputStream stream = is;
      is.mark(Integer.MAX_VALUE);

      NBT.CompoundTag var6;
      try {
         if (compression == NBT.CompressionType.Fast) {
            if (Gdx.app != null && Gdx.app.getType() != ApplicationType.Desktop) {
               stream = new LZ4FrameInputStream(is, IOUtils.getLZ4().safeDecompressor(), IOUtils.getXXHash().hash32());
            } else {
               stream = new LZ4FrameInputStream(is);
            }
         } else if (compression == NBT.CompressionType.Small) {
            stream = new GZIPInputStream(is);
         }

         this.input = new DataInputStream(stream);
         return this.readTag(true, NBT.CompoundTag.class);
      } catch (IOException var10) {
         var10.printStackTrace();
         stream = is;
         is.reset();
         if (compression == NBT.CompressionType.Fast) {
            stream = new GZIPInputStream(is);
         }

         this.input = new DataInputStream(stream);
         NBT.CompoundTag t = this.readTag(true, NBT.CompoundTag.class);
         var6 = t;
      } finally {
         stream.close();
         NBT.Tag.idCounter = 0;
      }

      return var6;
   }

   protected void writeTag(NBT.Tag tag, boolean named) throws IOException {
      if (named) {
         this.output.writeByte(tag.type.value);
         if (tag.name != null) {
            this.output.writeShort(tag.name.length());
            this.output.write(tag.name.getBytes("UTF-8"));
         }
      }

      switch (tag.type) {
         case End:
            this.output.writeByte(0);
            break;
         case Byte:
            this.output.writeByte(((NBT.ByteTag)tag).data);
            break;
         case Short:
            this.output.writeShort(((NBT.ShortTag)tag).data);
            break;
         case Int:
            this.output.writeInt(((NBT.IntTag)tag).data);
            break;
         case Long:
            this.output.writeLong(((NBT.LongTag)tag).data);
            break;
         case Float:
            this.output.writeFloat(((NBT.FloatTag)tag).data);
            break;
         case Double:
            this.output.writeDouble(((NBT.DoubleTag)tag).data);
            break;
         case ByteArray:
            this.output.writeInt(((NBT.ByteArrayTag)tag).data.length);
            this.output.write(((NBT.ByteArrayTag)tag).data);
            break;
         case String:
            byte[] bytes = ((NBT.StringTag)tag).data.getBytes();
            this.output.writeShort(bytes.length);
            this.output.write(bytes);
            break;
         case List:
            NBT.ListTag lt = (NBT.ListTag)tag;
            this.output.writeByte(lt.elementType.value);
            this.output.writeInt(lt.data.size);

            for (NBT.Tag t : lt.data) {
               this.writeTag(t, false);
            }
            break;
         case Compound:
            Values var13 = ((NBT.CompoundTag)tag).data.values().iterator();

            while (var13.hasNext()) {
               NBT.Tag t = (NBT.Tag)var13.next();
               this.writeTag(t, true);
            }

            this.output.writeByte(0);
            break;
         case IntArray:
            this.output.writeInt(((NBT.IntArrayTag)tag).data.length);

            for (int i : ((NBT.IntArrayTag)tag).data) {
               this.output.writeInt(i);
            }
            break;
         case LongArray:
            this.output.writeInt(((NBT.LongArrayTag)tag).data.length);

            for (long i : ((NBT.LongArrayTag)tag).data) {
               this.output.writeLong(i);
            }
            break;
         case ShortArray:
            this.output.writeInt(((NBT.ShortArrayTag)tag).data.length);

            for (short i : ((NBT.ShortArrayTag)tag).data) {
               this.output.writeShort(i);
            }
            break;
         case FloatArray:
            this.output.writeInt(((NBT.FloatArrayTag)tag).data.length);

            for (float i : ((NBT.FloatArrayTag)tag).data) {
               this.output.writeFloat(i);
            }
            break;
         default:
            throw new IOException("Unknown Tag Type: " + tag.type);
      }
   }

   protected void writeFile(OutputStream os, NBT.CompoundTag data, NBT.CompressionType compression) throws IOException {
      NBT.Tag.idCounter = 0;
      if (compression == NBT.CompressionType.Fast) {
         if (Gdx.app != null && Gdx.app.getType() != ApplicationType.Desktop) {
            os = new LZ4FrameOutputStream(
               os, BLOCKSIZE.SIZE_4MB, -1L, IOUtils.getLZ4().fastCompressor(), IOUtils.getXXHash().hash32(), new Bits[]{Bits.BLOCK_INDEPENDENCE}
            );
         } else {
            os = new LZ4FrameOutputStream(os);
         }
      } else if (compression == NBT.CompressionType.Small) {
         os = new GZIPOutputStream(os);
      }

      this.output = new DataOutputStream(os);
      this.writeTag(data, true);
      this.output = null;
      os.flush();
      os.close();
      NBT.Tag.idCounter = 0;
   }

   public static NBT.CompoundTag read(InputStream is, NBT.CompressionType compression) throws IOException {
      return nbt.readFile(is, compression);
   }

   public static NBT.CompoundTag readText(InputStream is) throws IOException {
      NBT.CompoundTag root = null;
      BufferedReader br = new BufferedReader(new InputStreamReader(is));
      String s = null;
      Matcher m = textRegex.matcher("");
      int line = 0;
      NBT.Tag lastTag = null;
      NBT.CollectionTag currentParent = null;

      for (ArrayDeque<NBT.CollectionTag> stack = new ArrayDeque<>(); (s = br.readLine()) != null; line++) {
         s = s.trim();
         if (!m.reset(s).find()) {
            throw new IOException("Invalid file format. Could not parse line " + line);
         }

         if (root == null) {
            if (!m.group(1).equals("Compound")) {
               throw new IOException("Invalid root tag. Could not parse line " + line);
            }

            root = new NBT.CompoundTag(m.group(2));
            lastTag = root;
         } else if (m.group().equals("{")) {
            if (currentParent != null) {
               stack.push(currentParent);
            }

            if (!(lastTag instanceof NBT.CollectionTag)) {
               throw new IOException("Invalid parent tag. Could not parse line " + line);
            }

            currentParent = (NBT.CollectionTag)lastTag;
         } else if (m.group().equals("}")) {
            currentParent = stack.size() > 0 ? stack.pop() : null;
         } else {
            String t = m.group(1);
            String n = m.group(2);
            String v = m.group(4);

            try {
               if (t.equals("Byte")) {
                  lastTag = new NBT.ByteTag(n, Byte.parseByte(v));
               } else if (t.equals("Short")) {
                  lastTag = new NBT.ShortTag(n, Short.parseShort(v));
               } else if (t.equals("Int")) {
                  lastTag = new NBT.IntTag(n, Integer.parseInt(v));
               } else if (t.equals("Long")) {
                  lastTag = new NBT.LongTag(n, Long.parseLong(v));
               } else if (t.equals("Float")) {
                  lastTag = new NBT.FloatTag(n, Float.parseFloat(v));
               } else if (t.equals("Double")) {
                  lastTag = new NBT.DoubleTag(n, Double.parseDouble(v));
               } else if (t.equals("ByteArray")) {
                  byte[] b = new byte[0];
                  if (!v.equals("[]")) {
                     String[] nums = v.substring(1, v.length() - 1).split(", ");
                     b = new byte[nums.length];

                     for (int i = 0; i < b.length; i++) {
                        b[i] = Byte.parseByte(nums[i]);
                     }
                  }

                  lastTag = new NBT.ByteArrayTag(n, b);
               } else if (t.equals("String")) {
                  lastTag = new NBT.StringTag(n, v.substring(1, v.length() - 1));
               } else if (t.equals("List")) {
                  NBT.TagType type = null;
                  String p = m.group(3);
                  if (p.equals("Byte")) {
                     type = NBT.TagType.Byte;
                  } else if (p.equals("Short")) {
                     type = NBT.TagType.Short;
                  } else if (p.equals("Int")) {
                     type = NBT.TagType.Int;
                  } else if (p.equals("Long")) {
                     type = NBT.TagType.Long;
                  } else if (p.equals("Float")) {
                     type = NBT.TagType.Float;
                  } else if (p.equals("Double")) {
                     type = NBT.TagType.Double;
                  } else if (p.equals("ByteArray")) {
                     type = NBT.TagType.ByteArray;
                  } else if (p.equals("String")) {
                     type = NBT.TagType.String;
                  } else if (p.equals("List")) {
                     type = NBT.TagType.List;
                  } else if (p.equals("Compound")) {
                     type = NBT.TagType.Compound;
                  } else if (p.equals("IntArray")) {
                     type = NBT.TagType.IntArray;
                  } else if (p.equals("LongArray")) {
                     type = NBT.TagType.LongArray;
                  } else if (p.equals("ShortArray")) {
                     type = NBT.TagType.ShortArray;
                  } else {
                     if (!p.equals("FloatArray")) {
                        throw new IOException("Unkown tag type. Could not parse line " + line);
                     }

                     type = NBT.TagType.FloatArray;
                  }

                  lastTag = new NBT.ListTag(n, type);
               } else if (t.equals("Compound")) {
                  lastTag = new NBT.CompoundTag(n);
               } else if (t.equals("IntArray")) {
                  int[] b = new int[0];
                  if (!v.equals("[]")) {
                     String[] nums = v.substring(1, v.length() - 1).split(", ");
                     b = new int[nums.length];

                     for (int i = 0; i < b.length; i++) {
                        b[i] = Integer.parseInt(nums[i]);
                     }
                  }

                  lastTag = new NBT.IntArrayTag(n, b);
               } else if (t.equals("LongArray")) {
                  long[] b = new long[0];
                  if (!v.equals("[]")) {
                     String[] nums = v.substring(1, v.length() - 1).split(", ");
                     b = new long[nums.length];

                     for (int i = 0; i < b.length; i++) {
                        b[i] = Long.parseLong(nums[i]);
                     }
                  }

                  lastTag = new NBT.LongArrayTag(n, b);
               } else if (t.equals("ShortArray")) {
                  short[] b = new short[0];
                  if (!v.equals("[]")) {
                     String[] nums = v.substring(1, v.length() - 1).split(", ");
                     b = new short[nums.length];

                     for (int i = 0; i < b.length; i++) {
                        b[i] = Short.parseShort(nums[i]);
                     }
                  }

                  lastTag = new NBT.ShortArrayTag(n, b);
               } else {
                  if (!t.equals("FloatArray")) {
                     throw new IOException("Unkown tag type. Could not parse line " + line);
                  }

                  float[] b = new float[0];
                  if (!v.equals("[]")) {
                     String[] nums = v.substring(1, v.length() - 1).split(", ");
                     b = new float[nums.length];

                     for (int i = 0; i < b.length; i++) {
                        b[i] = Float.parseFloat(nums[i]);
                     }
                  }

                  lastTag = new NBT.FloatArrayTag(n, b);
               }

               currentParent.add(lastTag);
            } catch (RuntimeException var15) {
               throw new IOException("Could not parse line " + line, var15);
            }
         }
      }

      return root;
   }

   public static void write(OutputStream os, NBT.CompoundTag data, NBT.CompressionType compression) throws IOException {
      nbt.writeFile(os, data, compression);
   }

   public static NBT.Filter parseFilter(String s) {
      if (!s.contains(",")) {
         if (s.contains(">")) {
            int index = s.lastIndexOf(">");
            return new NBT.ParentFilter(parseFilter(s.substring(0, index)), parseFilter(s.substring(index + 1)));
         } else if (s.startsWith("#")) {
            return new NBT.NameFilter(s.substring(1));
         } else if (!s.equals("*") && s.length() != 0) {
            Matcher m = NBT.Filter.indexPattern.matcher(s);
            if (m.matches()) {
               return new NBT.IndexFilter(Integer.parseInt(m.group(1)));
            } else {
               String var6 = s.toLowerCase();
               switch (var6) {
                  case "end":
                     return new NBT.TypeFilter(NBT.TagType.End);
                  case "byte":
                     return new NBT.TypeFilter(NBT.TagType.Byte);
                  case "short":
                     return new NBT.TypeFilter(NBT.TagType.Short);
                  case "int":
                     return new NBT.TypeFilter(NBT.TagType.Int);
                  case "long":
                     return new NBT.TypeFilter(NBT.TagType.Long);
                  case "float":
                     return new NBT.TypeFilter(NBT.TagType.Float);
                  case "double":
                     return new NBT.TypeFilter(NBT.TagType.Double);
                  case "bytearray":
                     return new NBT.TypeFilter(NBT.TagType.ByteArray);
                  case "string":
                     return new NBT.TypeFilter(NBT.TagType.String);
                  case "list":
                     return new NBT.TypeFilter(NBT.TagType.List);
                  case "compound":
                     return new NBT.TypeFilter(NBT.TagType.Compound);
                  case "intarray":
                     return new NBT.TypeFilter(NBT.TagType.IntArray);
                  case "longarray":
                     return new NBT.TypeFilter(NBT.TagType.LongArray);
                  case "shortarray":
                     return new NBT.TypeFilter(NBT.TagType.ShortArray);
                  case "floatarray":
                     return new NBT.TypeFilter(NBT.TagType.FloatArray);
                  default:
                     throw new IllegalArgumentException("Invalid type filter name: " + s);
               }
            }
         } else {
            return new NBT.AnyFilter();
         }
      } else {
         String[] parts = s.split(",");
         NBT.Filter[] fs = new NBT.Filter[parts.length];

         for (int i = 0; i < fs.length; i++) {
            fs[i] = parseFilter(parts[i]);
         }

         return new NBT.CompoundFilter(fs);
      }
   }

   public static class AnyFilter implements NBT.Filter {
      @Override
      public boolean matches(NBT.Tag t) {
         return true;
      }

      @Override
      public String toString() {
         return "*";
      }
   }

   public static class Builder {
      protected NBT.CompoundTag root;
      protected NBT.CollectionTag current;

      public Builder(String name) {
         this.root = new NBT.CompoundTag(name);
         this.current = this.root;
      }

      public NBT.Builder Byte(String name, byte value) {
         this.current.add(new NBT.ByteTag(name, value));
         return this;
      }

      public NBT.Builder Byte(byte value) {
         this.current.add(new NBT.ByteTag(null, value));
         return this;
      }

      public NBT.Builder Short(String name, short value) {
         this.current.add(new NBT.ShortTag(name, value));
         return this;
      }

      public NBT.Builder Short(short value) {
         this.current.add(new NBT.ShortTag(null, value));
         return this;
      }

      public NBT.Builder Int(String name, int value) {
         this.current.add(new NBT.IntTag(name, value));
         return this;
      }

      public NBT.Builder Int(int value) {
         this.current.add(new NBT.IntTag(null, value));
         return this;
      }

      public NBT.Builder Long(String name, long value) {
         this.current.add(new NBT.LongTag(name, value));
         return this;
      }

      public NBT.Builder Long(long value) {
         this.current.add(new NBT.LongTag(null, value));
         return this;
      }

      public NBT.Builder Float(String name, float value) {
         this.current.add(new NBT.FloatTag(name, value));
         return this;
      }

      public NBT.Builder Float(float value) {
         this.current.add(new NBT.FloatTag(null, value));
         return this;
      }

      public NBT.Builder Double(String name, double value) {
         this.current.add(new NBT.DoubleTag(name, value));
         return this;
      }

      public NBT.Builder Double(double value) {
         this.current.add(new NBT.DoubleTag(null, value));
         return this;
      }

      public NBT.Builder ByteArray(String name, byte[] value) {
         this.current.add(new NBT.ByteArrayTag(name, value));
         return this;
      }

      public NBT.Builder ByteArray(byte[] value) {
         this.current.add(new NBT.ByteArrayTag(null, value));
         return this;
      }

      public NBT.Builder String(String name, String value) {
         this.current.add(new NBT.StringTag(name, value));
         return this;
      }

      public NBT.Builder String(String value) {
         this.current.add(new NBT.StringTag(null, value));
         return this;
      }

      public NBT.Builder List(String name, NBT.TagType type) {
         NBT.CollectionTag prev = this.current;
         this.current = new NBT.ListTag(name, type);
         this.current.parent = prev;
         prev.add(this.current);
         return this;
      }

      public NBT.Builder List(NBT.TagType type) {
         NBT.CollectionTag prev = this.current;
         this.current = new NBT.ListTag(null, type);
         this.current.parent = prev;
         prev.add(this.current);
         return this;
      }

      public NBT.Builder Compound(String name) {
         NBT.CollectionTag prev = this.current;
         this.current = new NBT.CompoundTag(name);
         this.current.parent = prev;
         prev.add(this.current);
         return this;
      }

      public NBT.Builder Compound() {
         NBT.CollectionTag prev = this.current;
         this.current = new NBT.CompoundTag();
         this.current.parent = prev;
         prev.add(this.current);
         return this;
      }

      public NBT.Builder IntArray(String name, int[] value) {
         this.current.add(new NBT.IntArrayTag(name, value));
         return this;
      }

      public NBT.Builder IntArray(int[] value) {
         this.current.add(new NBT.IntArrayTag(null, value));
         return this;
      }

      public NBT.Builder LongArray(String name, long[] value) {
         this.current.add(new NBT.LongArrayTag(name, value));
         return this;
      }

      public NBT.Builder LongArray(long[] value) {
         this.current.add(new NBT.LongArrayTag(null, value));
         return this;
      }

      public NBT.Builder ShortArray(String name, short[] value) {
         this.current.add(new NBT.ShortArrayTag(name, value));
         return this;
      }

      public NBT.Builder ShortArray(short[] value) {
         this.current.add(new NBT.ShortArrayTag(null, value));
         return this;
      }

      public NBT.Builder FloatArray(String name, float[] value) {
         this.current.add(new NBT.FloatArrayTag(name, value));
         return this;
      }

      public NBT.Builder FloatArray(float[] value) {
         this.current.add(new NBT.FloatArrayTag(null, value));
         return this;
      }

      public NBT.Builder add(NBT.Tag tag) {
         this.current.add(tag);
         return this;
      }

      public NBT.Builder End() {
         this.current = this.current.parent;
         return this;
      }

      public NBT.CompoundTag Get() {
         while (this.current != null) {
            this.End();
         }

         return this.root;
      }
   }

   public static class ByteArrayTag extends NBT.Tag {
      public byte[] data;

      public ByteArrayTag() {
         super(NBT.TagType.ByteArray);
      }

      public ByteArrayTag(String name, byte[] data) {
         super(NBT.TagType.ByteArray);
         this.data = data;
         this.name = name;
      }

      @Override
      public void reset() {
         super.reset();
         this.data = null;
      }

      @Override
      protected String toString(String pad) {
         return super.toString(pad) + Arrays.toString(this.data) + "\r\n";
      }

      @Override
      protected boolean dataEquals(NBT.Tag o) {
         return Arrays.equals(this.data, ((NBT.ByteArrayTag)o).data);
      }

      @Override
      public Object data() {
         return this.data;
      }
   }

   public static class ByteTag extends NBT.Tag {
      public byte data;

      public ByteTag() {
         super(NBT.TagType.Byte);
      }

      public ByteTag(String name, byte data) {
         super(NBT.TagType.Byte);
         this.data = data;
         this.name = name;
      }

      @Override
      public void reset() {
         super.reset();
         this.data = 0;
      }

      @Override
      protected String toString(String pad) {
         return super.toString(pad) + this.data + "\r\n";
      }

      @Override
      protected boolean dataEquals(NBT.Tag o) {
         return this.data == ((NBT.ByteTag)o).data;
      }

      @Override
      public Object data() {
         return this.data;
      }
   }

   public abstract static class CollectionTag extends NBT.Tag {
      public CollectionTag(NBT.TagType type) {
         super(type);
      }

      @Override
      public void reset() {
         super.reset();
         this.parent = null;
      }

      public abstract void add(NBT.Tag var1);

      public abstract boolean remove(NBT.Tag var1);

      protected abstract Iterable<NBT.Tag> getData();

      protected void getChildren(NBT.Filter[] query, int index, Set<NBT.Tag> targets) {
         NBT.Filter f = query[index];

         for (NBT.Tag t : this.getData()) {
            if (f.matches(t)) {
               if (index == query.length - 1) {
                  targets.add(t);
               }

               if (t instanceof NBT.CollectionTag) {
                  if (index < query.length - 1) {
                     ((NBT.CollectionTag)t).getChildren(query, index + 1, targets);
                  }

                  ((NBT.CollectionTag)t).getChildren(query, index, targets);
               }
            } else if (t instanceof NBT.CollectionTag) {
               ((NBT.CollectionTag)t).getChildren(query, index, targets);
            }
         }
      }

      public Set<NBT.Tag> query(String queryString) {
         String[] parts = queryString.split(" ");
         Set<NBT.Tag> results = new HashSet<>();
         NBT.Filter[] query = new NBT.Filter[parts.length];
         int index = 0;

         for (String s : parts) {
            query[index++] = NBT.parseFilter(s);
         }

         this.getChildren(query, 0, results);
         return results;
      }
   }

   public static class CompoundFilter implements NBT.Filter {
      private NBT.Filter[] filters;

      public CompoundFilter(NBT.Filter... filters) {
         this.filters = filters;
      }

      @Override
      public boolean matches(NBT.Tag t) {
         for (NBT.Filter f : this.filters) {
            if (!f.matches(t)) {
               return false;
            }
         }

         return true;
      }

      @Override
      public String toString() {
         String s = "{";

         for (NBT.Filter f : this.filters) {
            s = s + f;
         }

         return s + "}";
      }
   }

   public static class CompoundTag extends NBT.CollectionTag {
      public final ObjectMap<String, NBT.Tag> data = new ObjectMap();

      public CompoundTag() {
         super(NBT.TagType.Compound);
      }

      public CompoundTag(String name) {
         super(NBT.TagType.Compound);
         this.name = name;
      }

      @Override
      public void reset() {
         super.reset();
         this.data.clear();
      }

      @Override
      public void free() {
         Values var1 = this.data.values().iterator();

         while (var1.hasNext()) {
            NBT.Tag t = (NBT.Tag)var1.next();
            t.free();
         }

         super.free();
      }

      @Override
      protected String toString(String pad) {
         StringBuilder sb = new StringBuilder();
         sb.append(super.toString(pad));
         sb.append(this.data.size);
         sb.append(" entries\r\n");
         sb.append(pad);
         sb.append("{\r\n");
         Values var3 = this.data.values().iterator();

         while (var3.hasNext()) {
            NBT.Tag t = (NBT.Tag)var3.next();
            sb.append(t.toString(pad + "  "));
         }

         sb.append(pad);
         sb.append("}\r\n");
         return sb.toString();
      }

      @Override
      protected boolean dataEquals(NBT.Tag o) {
         return this.data.equals(((NBT.CompoundTag)o).data);
      }

      @Override
      public void add(NBT.Tag tag) {
         if (tag.name == null) {
            throw new RuntimeException("Compound Tag expects named tags, got no name");
         } else {
            tag.parent = this;
            this.data.put(tag.name, tag);
         }
      }

      @Override
      public boolean remove(NBT.Tag tag) {
         return this.data.remove(tag.name) != null;
      }

      public boolean has(String name) {
         return this.data.containsKey(name);
      }

      public byte Byte(String name) throws NBT.NBTException {
         return ((NBT.ByteTag)this.getWithException(name, NBT.TagType.Byte)).data;
      }

      public byte Byte(String name, byte defaultValue) {
         NBT.ByteTag tag = (NBT.ByteTag)this.get(name, NBT.TagType.Byte);
         return tag == null ? defaultValue : tag.data;
      }

      public short Short(String name) throws NBT.NBTException {
         NBT.Tag tag = this.getWithException(name);
         if (tag.type == NBT.TagType.Short) {
            return ((NBT.ShortTag)tag).data;
         } else if (tag.type == NBT.TagType.Byte) {
            return (short)((NBT.ByteTag)tag).data;
         } else {
            throw new NBT.NBTException("Invalid tag type for Short! Expected \"Short\" or \"Byte\", got \"" + tag.type + "\"");
         }
      }

      public short Short(String name, short defaultValue) {
         NBT.Tag tag = this.get(name);
         if (tag == null) {
            return defaultValue;
         } else if (tag.type == NBT.TagType.Short) {
            return ((NBT.ShortTag)tag).data;
         } else {
            return tag.type == NBT.TagType.Byte ? (short)((NBT.ByteTag)tag).data : defaultValue;
         }
      }

      public int Int(String name) throws NBT.NBTException {
         return ((NBT.IntTag)this.getWithException(name, NBT.TagType.Int)).data;
      }

      public int Int(String name, int defaultValue) {
         NBT.IntTag tag = (NBT.IntTag)this.get(name, NBT.TagType.Int);
         return tag == null ? defaultValue : tag.data;
      }

      public long Long(String name) throws NBT.NBTException {
         return ((NBT.LongTag)this.getWithException(name, NBT.TagType.Long)).data;
      }

      public long Long(String name, long defaultValue) {
         NBT.LongTag tag = (NBT.LongTag)this.get(name, NBT.TagType.Long);
         return tag == null ? defaultValue : tag.data;
      }

      public float Float(String name) throws NBT.NBTException {
         return ((NBT.FloatTag)this.getWithException(name, NBT.TagType.Float)).data;
      }

      public float Float(String name, float defaultValue) {
         NBT.FloatTag tag = (NBT.FloatTag)this.get(name, NBT.TagType.Float);
         return tag == null ? defaultValue : tag.data;
      }

      public double Double(String name) throws NBT.NBTException {
         return ((NBT.DoubleTag)this.getWithException(name, NBT.TagType.Double)).data;
      }

      public double Double(String name, double defaultValue) {
         NBT.DoubleTag tag = (NBT.DoubleTag)this.get(name, NBT.TagType.Double);
         return tag == null ? defaultValue : tag.data;
      }

      public byte[] ByteArray(String name) throws NBT.NBTException {
         return ((NBT.ByteArrayTag)this.getWithException(name, NBT.TagType.ByteArray)).data;
      }

      public byte[] ByteArray(String name, byte[] defaultValue) {
         NBT.ByteArrayTag tag = (NBT.ByteArrayTag)this.get(name, NBT.TagType.ByteArray);
         return tag == null ? defaultValue : tag.data;
      }

      public String String(String name) throws NBT.NBTException {
         return ((NBT.StringTag)this.getWithException(name, NBT.TagType.String)).data;
      }

      public String String(String name, String defaultValue) {
         NBT.StringTag tag = (NBT.StringTag)this.get(name, NBT.TagType.String);
         return tag == null ? defaultValue : tag.data;
      }

      public NBT.ListTag List(String name) throws NBT.NBTException {
         return (NBT.ListTag)this.getWithException(name, NBT.TagType.List);
      }

      public NBT.ListTag List(String name, NBT.TagType elementType) throws NBT.NBTException {
         NBT.ListTag tag = (NBT.ListTag)this.getWithException(name, NBT.TagType.List);
         if (tag.elementType != elementType) {
            throw new NBT.NBTException("Invalid element tag type! Expected \"" + this.type + "\", got \"" + tag.type + "\"");
         } else {
            return tag;
         }
      }

      public NBT.CompoundTag Compound(String name) throws NBT.NBTException {
         return (NBT.CompoundTag)this.getWithException(name, NBT.TagType.Compound);
      }

      public NBT.CompoundTag CompoundOpt(String name) {
         return (NBT.CompoundTag)this.get(name, NBT.TagType.Compound);
      }

      public int[] IntArray(String name) throws NBT.NBTException {
         return ((NBT.IntArrayTag)this.getWithException(name, NBT.TagType.IntArray)).data;
      }

      public int[] IntArray(String name, int[] defaultValue) {
         NBT.IntArrayTag tag = (NBT.IntArrayTag)this.get(name, NBT.TagType.IntArray);
         return tag == null ? defaultValue : tag.data;
      }

      public long[] LongArray(String name) throws NBT.NBTException {
         return ((NBT.LongArrayTag)this.getWithException(name, NBT.TagType.LongArray)).data;
      }

      public long[] LongArray(String name, long[] defaultValue) {
         NBT.LongArrayTag tag = (NBT.LongArrayTag)this.get(name, NBT.TagType.LongArray);
         return tag == null ? defaultValue : tag.data;
      }

      public short[] ShortArray(String name) throws NBT.NBTException {
         return ((NBT.ShortArrayTag)this.getWithException(name, NBT.TagType.ShortArray)).data;
      }

      public short[] ShortArray(String name, short[] defaultValue) {
         NBT.ShortArrayTag tag = (NBT.ShortArrayTag)this.get(name, NBT.TagType.ShortArray);
         return tag == null ? defaultValue : tag.data;
      }

      public float[] FloatArray(String name) throws NBT.NBTException {
         return ((NBT.FloatArrayTag)this.getWithException(name, NBT.TagType.FloatArray)).data;
      }

      public float[] FloatArray(String name, float[] defaultValue) {
         NBT.FloatArrayTag tag = (NBT.FloatArrayTag)this.get(name, NBT.TagType.FloatArray);
         return tag == null ? defaultValue : tag.data;
      }

      public NBT.Tag get(String name) {
         return (NBT.Tag)this.data.get(name);
      }

      public NBT.Tag getWithException(String name) throws NBT.NBTException {
         NBT.Tag t = (NBT.Tag)this.data.get(name);
         if (t == null) {
            throw new NBT.NBTException("No tag found with name \"" + name + "\"");
         } else {
            return t;
         }
      }

      public NBT.Tag get(String name, NBT.TagType type) {
         NBT.Tag tag = (NBT.Tag)this.data.get(name);
         if (tag == null) {
            return null;
         } else {
            return tag.type != type ? null : tag;
         }
      }

      public NBT.Tag getWithException(String name, NBT.TagType type) throws NBT.NBTException {
         NBT.Tag tag = (NBT.Tag)this.data.get(name);
         if (tag == null) {
            throw new NBT.NBTException("No tag found with name \"" + name + "\"");
         } else if (tag.type != type) {
            throw new NBT.NBTException("Invalid tag type! Expected \"" + type + "\", got \"" + tag.type + "\"");
         } else {
            return tag;
         }
      }

      @Override
      protected Iterable<NBT.Tag> getData() {
         return this.data.values();
      }

      @Override
      public Object data() {
         return this.data;
      }
   }

   public static enum CompressionType {
      Uncompressed,
      Fast,
      Small;
   }

   public static class DoubleTag extends NBT.Tag {
      public double data;

      public DoubleTag() {
         super(NBT.TagType.Double);
      }

      public DoubleTag(String name, double data) {
         super(NBT.TagType.Double);
         this.data = data;
         this.name = name;
      }

      @Override
      public void reset() {
         super.reset();
         this.data = 0.0;
      }

      @Override
      protected String toString(String pad) {
         return super.toString(pad) + this.data + "\r\n";
      }

      @Override
      protected boolean dataEquals(NBT.Tag o) {
         return this.data == ((NBT.DoubleTag)o).data;
      }

      @Override
      public Object data() {
         return this.data;
      }
   }

   public static class EndTag extends NBT.Tag {
      public EndTag() {
         super(NBT.TagType.End);
      }

      @Override
      protected boolean dataEquals(NBT.Tag o) {
         return true;
      }

      @Override
      public Object data() {
         return null;
      }
   }

   public interface Filter {
      Pattern indexPattern = Pattern.compile("^\\[(\\d+)\\]$");

      boolean matches(NBT.Tag var1);
   }

   public static class FloatArrayTag extends NBT.Tag {
      public float[] data;

      public FloatArrayTag() {
         super(NBT.TagType.FloatArray);
      }

      public FloatArrayTag(String name, float[] data) {
         super(NBT.TagType.FloatArray);
         this.data = data;
         this.name = name;
      }

      @Override
      public void reset() {
         super.reset();
         this.data = null;
      }

      @Override
      protected String toString(String pad) {
         return super.toString(pad) + Arrays.toString(this.data) + "\r\n";
      }

      @Override
      protected boolean dataEquals(NBT.Tag o) {
         return Arrays.equals(this.data, ((NBT.FloatArrayTag)o).data);
      }

      @Override
      public Object data() {
         return this.data;
      }
   }

   public static class FloatTag extends NBT.Tag {
      public float data;

      public FloatTag() {
         super(NBT.TagType.Float);
      }

      public FloatTag(String name, float data) {
         super(NBT.TagType.Float);
         this.data = data;
         this.name = name;
      }

      @Override
      public void reset() {
         super.reset();
         this.data = 0.0F;
      }

      @Override
      protected String toString(String pad) {
         return super.toString(pad) + this.data + "\r\n";
      }

      @Override
      protected boolean dataEquals(NBT.Tag o) {
         return this.data == ((NBT.FloatTag)o).data;
      }

      @Override
      public Object data() {
         return this.data;
      }
   }

   public static class IndexFilter implements NBT.Filter {
      private int index;

      public IndexFilter(int index) {
         this.index = index;
      }

      @Override
      public boolean matches(NBT.Tag t) {
         return t.parent != null && t.parent.type == NBT.TagType.List && ((NBT.ListTag)t.parent).data.indexOf(t, false) == this.index;
      }

      @Override
      public String toString() {
         return "[" + this.index + "]";
      }
   }

   public static class IntArrayTag extends NBT.Tag {
      public int[] data;

      public IntArrayTag() {
         super(NBT.TagType.IntArray);
      }

      public IntArrayTag(String name, int[] data) {
         super(NBT.TagType.IntArray);
         this.data = data;
         this.name = name;
      }

      @Override
      public void reset() {
         super.reset();
         this.data = null;
      }

      @Override
      protected String toString(String pad) {
         return super.toString(pad) + Arrays.toString(this.data) + "\r\n";
      }

      @Override
      protected boolean dataEquals(NBT.Tag o) {
         return Arrays.equals(this.data, ((NBT.IntArrayTag)o).data);
      }

      @Override
      public Object data() {
         return this.data;
      }
   }

   public static class IntTag extends NBT.Tag {
      public int data;

      public IntTag() {
         super(NBT.TagType.Int);
      }

      public IntTag(String name, int data) {
         super(NBT.TagType.Int);
         this.data = data;
         this.name = name;
      }

      @Override
      public void reset() {
         super.reset();
         this.data = 0;
      }

      @Override
      protected String toString(String pad) {
         return super.toString(pad) + this.data + "\r\n";
      }

      @Override
      protected boolean dataEquals(NBT.Tag o) {
         return this.data == ((NBT.IntTag)o).data;
      }

      @Override
      public Object data() {
         return this.data;
      }
   }

   public static class LazyTagContainer {
      ByteBuffer buf;
      public final NBT.LazyTagContainer.RawCompoundTag root;
      public final String rootName;

      public LazyTagContainer(byte[] data) throws IOException {
         this.buf = ByteBuffer.wrap(data);
         ((Buffer)this.buf).position(0);
         NBT.TagType type = NBT.reverseTags[this.buf.get()];
         if (type != null && type == NBT.TagType.Compound) {
            short len = this.buf.getShort();
            byte[] str = new byte[len];
            this.buf.get(str);
            this.rootName = new String(str, "UTF-8");
            this.root = this.readCompound();
         } else {
            throw new IOException("Unknown Tag Type: " + type);
         }
      }

      private NBT.LazyTagContainer.RawCompoundTag readCompound() throws IOException {
         NBT.LazyTagContainer.RawCompoundTag compound = (NBT.LazyTagContainer.RawCompoundTag)Pools.obtain(NBT.LazyTagContainer.RawCompoundTag.class);
         compound.position = this.buf.position();
         compound.container = this;

         while (this.buf.position() < this.buf.capacity()) {
            short len = this.buf.getShort();
            byte[] str = new byte[len];
            this.buf.get(str);
            String name = new String(str, "UTF-8");
            NBT.TagType type = NBT.reverseTags[this.buf.get()];
            if (type == null) {
               throw new IOException("Unknown Tag Type: " + type);
            }

            switch (type) {
               case End:
               default:
                  break;
               case Byte:
               case Short:
               case Int:
               case Long:
               case Float:
               case Double: {
                  NBT.LazyTagContainer.RawTag tag = (NBT.LazyTagContainer.RawTag)Pools.obtain(NBT.LazyTagContainer.RawTag.class);
                  tag.container = this;
                  tag.position = this.buf.position();
                  tag.type = type;
                  ((Buffer)this.buf).position(this.buf.position() + type.width);
                  this.root.children.put(name, tag);
                  break;
               }
               case ByteArray:
               case IntArray:
               case LongArray:
               case ShortArray:
               case FloatArray: {
                  NBT.LazyTagContainer.RawTag tag = (NBT.LazyTagContainer.RawTag)Pools.obtain(NBT.LazyTagContainer.RawTag.class);
                  tag.container = this;
                  tag.position = this.buf.position();
                  tag.type = type;
                  tag.size = this.buf.getInt();
                  ((Buffer)this.buf).position(this.buf.position() + tag.size * type.width);
                  break;
               }
               case String: {
                  NBT.LazyTagContainer.RawTag tag = (NBT.LazyTagContainer.RawTag)Pools.obtain(NBT.LazyTagContainer.RawTag.class);
                  tag.container = this;
                  tag.position = this.buf.position();
                  tag.type = type;
                  tag.size = this.buf.getShort();
                  this.root.children.put(name, tag);
                  ((Buffer)this.buf).position(this.buf.position() + tag.size);
                  break;
               }
               case List:
                  this.root.children.put(name, this.readList());
                  break;
               case Compound:
                  this.root.children.put(name, this.readCompound());
            }

            if (this.buf.get(this.buf.position()) == 0) {
               break;
            }
         }

         return compound;
      }

      private NBT.LazyTagContainer.RawListTag readList() throws IOException {
         NBT.LazyTagContainer.RawListTag tag = (NBT.LazyTagContainer.RawListTag)Pools.obtain(NBT.LazyTagContainer.RawListTag.class);
         tag.container = this;
         tag.childrenType = NBT.reverseTags[this.buf.get()];
         int pos = this.buf.position();
         switch (tag.childrenType) {
            case End:
            default:
               break;
            case Byte:
            case Short:
            case Int:
            case Long:
            case Float:
            case Double:
               ((Buffer)this.buf).position(pos + tag.children.length * tag.childrenType.width);
               break;
            case ByteArray:
            case IntArray:
            case LongArray:
            case ShortArray:
            case FloatArray:
               tag.children = new Object[this.buf.getInt()];

               for (int i = 0; i < tag.children.length; i++) {
                  int elems = this.buf.getInt();
                  pos += elems * tag.childrenType.width;
                  ((Buffer)this.buf).position(pos);
                  tag.size = elems;
                  tag.children[i] = pos;
               }
               break;
            case String:
               tag.children = new Object[this.buf.getInt()];

               for (int i = 0; i < tag.children.length; i++) {
                  int elems = this.buf.getShort();
                  pos += elems;
                  ((Buffer)this.buf).position(pos);
                  tag.children[i] = pos;
               }
               break;
            case List:
               tag.children = new Object[this.buf.getInt()];

               for (int i = 0; i < tag.children.length; i++) {
                  tag.children[i] = this.readList();
               }
               break;
            case Compound:
               tag.children = new Object[this.buf.getInt()];

               for (int i = 0; i < tag.children.length; i++) {
                  tag.children[i] = this.readCompound();
               }
         }

         return tag;
      }

      public static class RawCompoundTag extends NBT.LazyTagContainer.RawTag {
         ObjectMap<String, NBT.LazyTagContainer.RawTag> children = new ObjectMap();

         @Override
         public void reset() {
            this.children.clear();
         }
      }

      public static class RawListTag extends NBT.LazyTagContainer.RawTag {
         Object[] children;
         NBT.TagType childrenType;

         @Override
         public void reset() {
            this.children = null;
            this.childrenType = null;
         }
      }

      public static class RawTag implements Poolable {
         int position;
         NBT.TagType type;
         int size;
         NBT.LazyTagContainer container;

         public void reset() {
            this.position = 0;
            this.size = 0;
            this.type = null;
            this.container = null;
         }
      }
   }

   public static class ListTag extends NBT.CollectionTag {
      public NBT.TagType elementType;
      public final Array<NBT.Tag> data = new Array();

      public ListTag() {
         super(NBT.TagType.List);
      }

      public ListTag(String name, NBT.TagType type) {
         super(NBT.TagType.List);
         this.elementType = type;
         this.name = name;
      }

      @Override
      public void reset() {
         super.reset();
         this.data.clear();
         this.elementType = null;
      }

      @Override
      public void free() {
         for (NBT.Tag t : this.data) {
            t.free();
         }

         super.free();
      }

      @Override
      protected String toString(String pad) {
         StringBuilder sb = new StringBuilder();
         sb.append(super.toString(pad));
         sb.append(this.data.size);
         sb.append(" entries of type ");
         sb.append(this.elementType);
         sb.append("\r\n");
         sb.append(pad);
         sb.append("{\r\n");

         for (NBT.Tag t : this.data) {
            sb.append(t.toString(pad + "  "));
         }

         sb.append(pad);
         sb.append("}\r\n");
         return sb.toString();
      }

      @Override
      protected boolean dataEquals(NBT.Tag o) {
         return this.data.equals(((NBT.ListTag)o).data);
      }

      @Override
      public void add(NBT.Tag tag) {
         if (tag.type != this.elementType) {
            throw new RuntimeException("Incompatible Tag Types in List Tag, wanted \"" + this.elementType + "\", got \"" + tag.type + "\"");
         } else {
            tag.parent = this;
            this.data.add(tag);
         }
      }

      @Override
      protected Iterable<NBT.Tag> getData() {
         return this.data;
      }

      @Override
      public boolean remove(NBT.Tag tag) {
         Iterator<NBT.Tag> iter = this.data.iterator();

         while (iter.hasNext()) {
            if (iter.next().equals(tag)) {
               iter.remove();
               return true;
            }
         }

         return false;
      }

      @Override
      public Object data() {
         return this.data;
      }
   }

   public static class LongArrayTag extends NBT.Tag {
      public long[] data;

      public LongArrayTag() {
         super(NBT.TagType.LongArray);
      }

      public LongArrayTag(String name, long[] data) {
         super(NBT.TagType.LongArray);
         this.data = data;
         this.name = name;
      }

      @Override
      public void reset() {
         super.reset();
         this.data = null;
      }

      @Override
      protected String toString(String pad) {
         return super.toString(pad) + Arrays.toString(this.data) + "\r\n";
      }

      @Override
      protected boolean dataEquals(NBT.Tag o) {
         return Arrays.equals(this.data, ((NBT.LongArrayTag)o).data);
      }

      @Override
      public Object data() {
         return this.data;
      }
   }

   public static class LongTag extends NBT.Tag {
      public long data;

      public LongTag() {
         super(NBT.TagType.Long);
      }

      public LongTag(String name, long data) {
         super(NBT.TagType.Long);
         this.data = data;
         this.name = name;
      }

      @Override
      public void reset() {
         super.reset();
         this.data = 0L;
      }

      @Override
      protected String toString(String pad) {
         return super.toString(pad) + this.data + "\r\n";
      }

      @Override
      protected boolean dataEquals(NBT.Tag o) {
         return this.data == ((NBT.LongTag)o).data;
      }

      @Override
      public Object data() {
         return this.data;
      }
   }

   public static class NBTException extends Exception {
      private static final long serialVersionUID = 1L;

      public NBTException() {
      }

      public NBTException(String message, Throwable cause) {
         super(message, cause);
      }

      public NBTException(String message) {
         super(message);
      }

      public NBTException(Throwable cause) {
         super(cause);
      }
   }

   public static class NameFilter implements NBT.Filter {
      private String name;

      public NameFilter(String name) {
         this.name = name;
      }

      @Override
      public boolean matches(NBT.Tag t) {
         return t.name != null && t.name.equals(this.name);
      }

      @Override
      public String toString() {
         return "#" + this.name;
      }
   }

   public static class ParentFilter implements NBT.Filter {
      private NBT.Filter parent;
      private NBT.Filter child;

      public ParentFilter(NBT.Filter parent, NBT.Filter child) {
         this.parent = parent;
         this.child = child;
      }

      @Override
      public boolean matches(NBT.Tag t) {
         return t.parent != null && this.parent.matches(t.parent) && this.child.matches(t);
      }

      @Override
      public String toString() {
         return "[" + this.parent + ">" + this.child + "]";
      }
   }

   public static class ShortArrayTag extends NBT.Tag {
      public short[] data;

      public ShortArrayTag() {
         super(NBT.TagType.ShortArray);
      }

      public ShortArrayTag(String name, short[] data) {
         super(NBT.TagType.ShortArray);
         this.data = data;
         this.name = name;
      }

      @Override
      public void reset() {
         super.reset();
         this.data = null;
      }

      @Override
      protected String toString(String pad) {
         return super.toString(pad) + Arrays.toString(this.data) + "\r\n";
      }

      @Override
      protected boolean dataEquals(NBT.Tag o) {
         return Arrays.equals(this.data, ((NBT.ShortArrayTag)o).data);
      }

      @Override
      public Object data() {
         return this.data;
      }
   }

   public static class ShortTag extends NBT.Tag {
      public short data;

      public ShortTag() {
         super(NBT.TagType.Short);
      }

      public ShortTag(String name, short data) {
         super(NBT.TagType.Short);
         this.data = data;
         this.name = name;
      }

      @Override
      public void reset() {
         super.reset();
         this.data = 0;
      }

      @Override
      protected String toString(String pad) {
         return super.toString(pad) + this.data + "\r\n";
      }

      @Override
      protected boolean dataEquals(NBT.Tag o) {
         return this.data == ((NBT.ShortTag)o).data;
      }

      @Override
      public Object data() {
         return this.data;
      }
   }

   public static class StringTag extends NBT.Tag {
      public String data;

      public StringTag() {
         super(NBT.TagType.String);
      }

      public StringTag(String name, String data) {
         super(NBT.TagType.String);
         this.data = data;
         this.name = name;
      }

      @Override
      public void reset() {
         super.reset();
         this.data = null;
      }

      @Override
      protected String toString(String pad) {
         return super.toString(pad) + "\"" + this.data + "\"\r\n";
      }

      @Override
      protected boolean dataEquals(NBT.Tag o) {
         return this.data.equals(((NBT.StringTag)o).data);
      }

      @Override
      public Object data() {
         return this.data;
      }
   }

   public abstract static class Tag implements Poolable {
      static int idCounter = 0;
      public final int id;
      public final NBT.TagType type;
      public String name;
      public NBT.CollectionTag parent;

      public Tag(NBT.TagType type) {
         this.type = type;
         this.id = idCounter++;
      }

      public void reset() {
         this.name = null;
      }

      public void free() {
         Pools.free(this);
      }

      @Override
      public final String toString() {
         return this.toString("");
      }

      protected String toString(String pad) {
         return pad + this.type.name() + (this.name != null ? "(\"" + this.name + "\")" : "") + ": ";
      }

      @Override
      public boolean equals(Object obj) {
         return !this.getClass().isInstance(obj)
            ? false
            : this.type == ((NBT.Tag)obj).type
               && (this.name == null ? ((NBT.Tag)obj).name == null : this.name.equals(((NBT.Tag)obj).name))
               && this.dataEquals((NBT.Tag)obj);
      }

      public void checkName(String name) throws NBT.NBTException {
         if (!name.equals(this.name)) {
            throw new RuntimeException("Invalid Tag name, wanted \"" + name + "\", got \"" + this.name + "\"");
         }
      }

      protected abstract boolean dataEquals(NBT.Tag var1);

      public abstract Object data();

      @Override
      public int hashCode() {
         return Objects.hash(this.parent != null ? this.parent.id : 0, this.id);
      }
   }

   public static enum TagType {
      End(0, NBT.EndTag.class, 0),
      Byte(1, NBT.ByteTag.class, 1),
      Short(2, NBT.ShortTag.class, 2),
      Int(3, NBT.IntTag.class, 4),
      Long(4, NBT.LongTag.class, 8),
      Float(5, NBT.FloatTag.class, 4),
      Double(6, NBT.DoubleTag.class, 8),
      ByteArray(7, NBT.ByteArrayTag.class, 1),
      String(8, NBT.StringTag.class, 1),
      List(9, NBT.ListTag.class, 0),
      Compound(10, NBT.CompoundTag.class, 0),
      IntArray(11, NBT.IntArrayTag.class, 4),
      LongArray(12, NBT.LongArrayTag.class, 8),
      ShortArray(13, NBT.ShortArrayTag.class, 2),
      FloatArray(14, NBT.FloatArrayTag.class, 4);

      public byte value;
      public Class<? extends NBT.Tag> clazz;
      public final int width;

      private TagType(int val, Class<? extends NBT.Tag> clazz, int width) {
         this.value = (byte)val;
         this.clazz = clazz;
         this.width = width;
         NBT.reverseTags[this.value] = this;
      }
   }

   public static class TypeFilter implements NBT.Filter {
      private NBT.TagType type;

      public TypeFilter(NBT.TagType type) {
         this.type = type;
      }

      @Override
      public boolean matches(NBT.Tag t) {
         return t.type.equals(this.type);
      }

      @Override
      public String toString() {
         return this.type.name();
      }
   }
}
