package de.dakror.quarry.desktop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import de.dakror.quarry.game.Item;
import de.dakror.quarry.game.Science;
import de.dakror.quarry.game.Tile;
import de.dakror.quarry.structure.base.ProducerStructure;
import de.dakror.quarry.structure.base.RecipeList;
import de.dakror.quarry.structure.base.Schema;
import de.dakror.quarry.structure.base.StructureType;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import javax.imageio.ImageIO;

public class MetaGen {
   static Color[] colors = new Color[]{Color.blue, Color.cyan, Color.green, Color.magenta, Color.orange, Color.pink, Color.red, Color.yellow, Color.black};

   static void generateOreGraph() {
      int layers = 25;
      int w = 200;
      int h = 400;
      int o = 20;
      float m = 1.1F;
      BufferedImage img = new BufferedImage(w * layers, h, 1);
      Graphics2D g = img.createGraphics();
      g.setColor(Color.white);
      g.fillRect(0, 0, img.getWidth(), img.getHeight());
      g.setColor(Color.black);
      g.fillRect(0, img.getHeight() - o, img.getWidth(), o);

      for (int i = 0; i < layers; i++) {
         g.setColor(Color.black);
         g.drawLine(w * i + w / 2, 0, w * i + w / 2, h);
         g.setColor(Color.lightGray);
         g.drawLine(w * i, 0, w * i, h - o);
         g.setColor(Color.white);
         g.drawString(i + "", w * i + w / 2 - 3, img.getHeight() - 5);
      }

      for (int i = 0; (float)i < m * 10.0F; i++) {
         if (i % 5 == 0) {
            g.setColor(Color.black);
         } else {
            g.setColor(Color.lightGray);
         }

         int y = (int)((float)(img.getHeight() - o) - (float)i / 10.0F * ((float)(h - o) / m));
         g.drawLine(0, y, img.getWidth(), y);
      }

      int strX = 20;
      g.setColor(Color.darkGray);
      g.fillRect(0, 0, Tile.OreType.values().length * 75, 30);

      for (Tile.OreType type : Tile.OreType.values()) {
         g.setColor(colors[type.ordinal()]);
         g.drawString(type.name(), strX, 20);
         strX += type.name().length() * 10;

         for (int i = 0; i < w * layers; i++) {
            double x = (double)i / (double)w - 0.5;
            double y = (double)(type.max - type.min)
                  * Math.pow(
                     (float) Math.E,
                     -Math.pow((x - (double)type.offset) / (double)(2.0F * (x < (double)type.offset ? type.leftUphold : type.rightUphold)), 2.0)
                  )
               + (double)type.min;
            if (y < 1.0E-6F) {
               y = 0.0;
            }

            if (y > 0.0) {
               g.fillOval(i - 1, (int)((double)(img.getHeight() - o) - y * (double)((float)(h - o) / m)) - 1, 3, 3);
            }
         }
      }

      g.dispose();

      try {
         ImageIO.write(img, "PNG", new File("../Development/stats/ores.png"));
      } catch (IOException var17) {
         var17.printStackTrace();
      }
   }

   static void calculateItemWorths() {
      EnumMap<Item.ItemType, Float> cache = new EnumMap<>(Item.ItemType.class);
      EnumMap<Item.ItemCategory, Float> catCache = new EnumMap<>(Item.ItemCategory.class);
      Array<RecipeList.Recipe> recipes = new Array();

      for (StructureType st : StructureType.values()) {
         try {
            Schema schema = (Schema)st.constr.getDeclaringClass().getField("classSchema").get(null);
            if (schema instanceof ProducerStructure.ProducerSchema) {
               recipes.addAll(((ProducerStructure.ProducerSchema)schema).recipeList.recipes);
            }
         } catch (Exception var11) {
            System.err.println(st + " no class schema");
         }
      }

      try {
         BufferedWriter bw = new BufferedWriter(new FileWriter(new File("../Development/stats/worths.csv")));

         for (Item.ItemType i : Item.ItemType.values()) {
            float worth = (float)i.worth;
            if (worth != 0.0F) {
               cache.put(i, worth);
               long time = (long)worth;
               bw.write(
                  String.format(
                     Locale.ENGLISH,
                     "%s;%,.0f;%dh:%02dmin:%02ds.%03dms\n",
                     i.name(),
                     worth,
                     time / 3600000L,
                     time % 3600000L / 60000L,
                     time % 60000L / 1000L,
                     time % 1000L
                  )
               );
            }
         }

         for (Item.ItemType ix : Item.ItemType.values()) {
            float worth = (float)ix.worth;
            if (worth == 0.0F) {
               worth = calculateItemWorth(ix, recipes, cache, catCache);
               cache.put(ix, worth);
               long time = (long)worth;
               System.out.println(ix.name() + ": " + Integer.toString((int)worth));
               bw.write(
                  String.format(
                     Locale.ENGLISH,
                     "%s;%,.0f;%dh:%02dmin:%02ds.%03dms\n",
                     ix.name(),
                     worth,
                     time / 3600000L,
                     time % 3600000L / 60000L,
                     time % 60000L / 1000L,
                     time % 1000L
                  )
               );
            }
         }

         bw.close();
      } catch (IOException var12) {
         var12.printStackTrace();
      }
   }

   static float calculateItemWorth(
      Item.ItemType item, Array<RecipeList.Recipe> recipes, EnumMap<Item.ItemType, Float> cache, EnumMap<Item.ItemCategory, Float> catCache
   ) {
      if (item == Item.ItemType.EmptyBarrel) {
         boolean minWorth = false;
      }

      if (cache.containsKey(item)) {
         return cache.get(item);
      } else {
         float minWorth = Float.MAX_VALUE;

         for (int i = 0; i < recipes.size; i++) {
            RecipeList.Recipe r = (RecipeList.Recipe)recipes.get(i);
            if (r.getOutput() != null) {
               for (Item.Items.Amount a : r.getOutput().entries) {
                  if (a.getItem() == item || Item.base(item) == a.getItem()) {
                     float s = r.workingTime * 10.0F * (r.getPower() > 0.0F ? (float)Math.pow((double)(r.getPower() * 60.0F), 0.5) : 1.0F);
                     Item.Element el = Item.element(item);
                     Item.Composite co = Item.composite(item);
                     if (r.getInput() != null) {
                        for (Item.Items.Amount b : r.getInput().entries) {
                           if (b.getItem() != null) {
                              float amount = (float)b.getAmount();
                              if (b.getItem().ordinal() >= item.ordinal()) {
                                 break;
                              }

                              if (b.getItem().categories.contains(Item.ItemCategory.Fluid)) {
                                 amount /= 1000.0F;
                              }

                              if (a instanceof RecipeList.ParameterizedAmount && (el != null || co != null)) {
                                 if (!Item.exists(b.getItem(), item)) {
                                    break;
                                 }

                                 s += calculateItemWorth(Item.get(b.getItem(), item), recipes, cache, catCache) * amount;
                              } else {
                                 s += calculateItemWorth(b.getItem(), recipes, cache, catCache) * amount;
                              }
                           } else if (b.getCat() != null) {
                              if (catCache.containsKey(b.getCat())) {
                                 s += catCache.get(b.getCat()) * (float)b.getAmount();
                              } else {
                                 float min = 0.0F;

                                 for (Item.ItemType j : Item.ItemType.values()) {
                                    if (j.categories.contains(b.getCat())) {
                                       float q = calculateItemWorth(j, recipes, cache, catCache);
                                       if (min == 0.0F || q < min) {
                                          min = q;
                                       }
                                    }
                                 }

                                 if (min == 0.0F) {
                                    System.err.println("No items found for category: " + b.getCat());
                                 }

                                 catCache.put(b.getCat(), min);
                                 s += min * (float)b.getAmount();
                              }
                           }
                        }
                     }

                     float value = s / (float)a.getAmount();
                     minWorth = Math.min(minWorth, value);
                  }
               }
            }
         }

         if (minWorth == Float.MAX_VALUE) {
            System.err.println("No recipe found for product: " + item);
            minWorth = 0.0F;
         }

         cache.put(item, minWorth);
         return minWorth;
      }
   }

   public static void run() {
      new File("../Development/stats").mkdirs();
      calculateItemWorths();
      generateOreGraph();
      StringBuilder build = new StringBuilder();
      StringBuilder recipes = new StringBuilder();
      StringBuilder buildTree = new StringBuilder();
      StringBuilder tree = new StringBuilder();
      StringBuilder sciences = new StringBuilder();
      buildTree.append("strict digraph {\r\n\toverlap=false;graph [dpi=300]; node[shape=record]; edge[tailport=s; headport=n];ranksep=1;nodesep=1;\r\n");
      tree.append("strict digraph {\r\n\toverlap=false;\r\n\t node[shape=record]; edge[tailport=s; headport=n; color=grey];graph [dpi=300];ranksep=1;\r\n");
      sciences.append("strict digraph {\r\n\toverlap=false;rankdir=LR;splines=true\r\n\tnode [shape=box];graph [dpi=300];\r\n");
      recipes.append("Structure;Input;;;;;;;;;;Time;Power;Output\n");
      Map<StructureType, EnumSet<Item.ItemType>> building = new HashMap<>();
      Map<StructureType, EnumSet<Item.ItemType>> outputs = new HashMap<>();
      Map<StructureType, EnumSet<Item.ItemType>> inputs = new HashMap<>();
      Array<Science.ScienceType> start = new Array();

      for (Science.ScienceType t : Science.ScienceType.values()) {
         sciences.append("\t" + t.name() + "\r\n");
         sciences.append("\t" + t.name() + "_p [shape=point]\r\n");
         sciences.append("\t" + t.name() + " -> " + t.name() + "_p [arrowhead=none,headport=w]\r\n");
         if (t.required.length == 0) {
            start.add(t);
         } else {
            for (Science.ScienceType s : t.required) {
               sciences.append("\t" + s.name() + "_p -> " + t.name() + "\r\n");
            }
         }
      }

      Map<EnumSet<Science.ScienceType>, Set<StructureType>> sameSciences = new HashMap<>();
      Map<EnumSet<Science.ScienceType>, Map<StructureType, Array<RecipeList.Recipe>>> sameScienceRecipes = new HashMap<>();
      EnumSet<Science.ScienceType> tmp = EnumSet.noneOf(Science.ScienceType.class);

      for (StructureType st : StructureType.values()) {
         try {
            Schema schema = (Schema)st.constr.getDeclaringClass().getField("classSchema").get(null);
            if (!schema.sciencesRequired.isEmpty()) {
               if (schema instanceof ProducerStructure.ProducerSchema) {
                  for (RecipeList.Recipe r : ((ProducerStructure.ProducerSchema)schema).recipeList.recipes) {
                     tmp.clear();
                     tmp.addAll(schema.sciencesRequired);
                     tmp.addAll(r.getRequiredSciences());
                     Map<StructureType, Array<RecipeList.Recipe>> set = sameScienceRecipes.getOrDefault(tmp, new HashMap<>());
                     Array<RecipeList.Recipe> re = set.containsKey(st) ? set.get(st) : new Array();
                     re.add(r);
                     set.put(st, re);
                     sameScienceRecipes.put(EnumSet.copyOf(tmp), set);
                  }
               }

               Set<StructureType> set = sameSciences.getOrDefault(schema.sciencesRequired, new HashSet<>());
               set.add(st);
               sameSciences.put(schema.sciencesRequired, set);
            }
         } catch (Exception var34) {
            System.err.println(st + " no class schema");
         }
      }

      for (Entry<EnumSet<Science.ScienceType>, Set<StructureType>> e : sameSciences.entrySet()) {
         String name = "";

         for (StructureType st : e.getValue()) {
            name = name + st.name() + "<br/>";
         }

         name = name.trim();
         sciences.append("\t\tst_" + Math.abs(e.hashCode()) + " [shape=ellipse,label=<" + name + ">]\r\n");

         for (Science.ScienceType st : e.getKey()) {
            sciences.append("\t\t" + st.name() + "_p -> st_" + Math.abs(e.hashCode()) + "\r\n");
         }
      }

      for (Entry<EnumSet<Science.ScienceType>, Map<StructureType, Array<RecipeList.Recipe>>> e : sameScienceRecipes.entrySet()) {
         String name = "";

         for (Entry<StructureType, Array<RecipeList.Recipe>> e1 : e.getValue().entrySet()) {
            name = name + "{" + e1.getKey().name() + "| {";

            for (int i = 0; i < e1.getValue().size; i++) {
               name = name + ((RecipeList.Recipe)e1.getValue().get(i)).name;
               if (i < e1.getValue().size - 1) {
                  name = name + " | ";
               }
            }

            name = name + "} } |";
         }

         name = name.trim().substring(0, name.length() - 1);
         sciences.append("\t\tst_" + Math.abs(e.hashCode()) + " [shape=record,label=\"" + name + "\"]\r\n");

         for (Science.ScienceType st : e.getKey()) {
            sciences.append("\t\t" + st.name() + "_p -> st_" + Math.abs(e.hashCode()) + "\r\n");
         }
      }

      sciences.append("\t{rank=same ");

      for (Science.ScienceType s : start) {
         sciences.append(s.name() + " ");
      }

      sciences.append("}\r\n");

      for (StructureType type : StructureType.values()) {
         try {
            Schema schema = (Schema)type.constr.getDeclaringClass().getField("classSchema").get(null);
            if (!(schema instanceof ProducerStructure.ProducerSchema)) {
               if (type == StructureType.Boiler) {
                  tree.append("\t" + type.name() + "[label=\"" + type.name() + "\"]\r\n");
                  inputs.put(type, EnumSet.of(Item.ItemType.Water, Item.ItemType.CoalOre, Item.ItemType.Charcoal));
                  outputs.put(type, EnumSet.of(Item.ItemType.PressurizedSteam));
               } else if (type == StructureType.Refinery) {
                  tree.append("\t" + type.name() + "[label=\"" + type.name() + "\"]\r\n");
                  inputs.put(type, EnumSet.of(Item.ItemType.CrudeOil, Item.ItemType.IntermediateOilToRefinery));
                  outputs.put(type, EnumSet.of(Item.ItemType.IntermediateOilToColumn, Item.ItemType.RefinedOil));
               }
            } else {
               tree.append("\t" + type.name() + "[label=\"" + type.name() + "\"]\r\n");
               EnumSet<Item.ItemType> out = EnumSet.noneOf(Item.ItemType.class);
               EnumSet<Item.ItemType> in = EnumSet.noneOf(Item.ItemType.class);

               for (RecipeList.Recipe r : ((ProducerStructure.ProducerSchema)schema).recipeList.recipes) {
                  recipes.append(type.name()).append(";");
                  int ix = 0;
                  if (r.getInput() != null) {
                     for (Item.Items.Amount o : r.getInput().entries) {
                        if (o.getItem() != null) {
                           recipes.append(o.getItem()).append(";");
                           in.add(o.getItem());
                        } else {
                           recipes.append(o.getCat()).append(";");

                           for (Item.ItemType it : Item.ItemType.values()) {
                              if (it.categories.contains(o.getCat())) {
                                 in.add(it);
                              }
                           }
                        }

                        recipes.append(o.getAmount()).append(";");
                        ix++;
                     }
                  }

                  while (ix < 5) {
                     recipes.append(";;");
                     ix++;
                  }

                  recipes.append((int)r.workingTime).append(";").append(r.getPower()).append(";");
                  if (r.getOutput() != null) {
                     for (Item.Items.Amount o : r.getOutput().entries) {
                        if (o.getItem() != null) {
                           out.add(o.getItem());
                           recipes.append(o.getItem()).append(";");
                        } else {
                           recipes.append(o.getCat()).append(";");

                           for (Item.ItemType itx : Item.ItemType.values()) {
                              if (itx.categories.contains(o.getCat())) {
                                 out.add(itx);
                              }
                           }
                        }

                        recipes.append(o.getAmount()).append(";");
                     }
                  }

                  recipes.append("\n");
               }

               inputs.put(type, in);
               outputs.put(type, out);
            }

            EnumSet<Item.ItemType> buildRes = EnumSet.noneOf(Item.ItemType.class);
            buildTree.append("\t" + type.name() + "[label=\"" + type.name() + "\"]\r\n");
            build.append(type.name() + ";");

            for (Item.Items.Amount a : schema.buildCosts.entries) {
               buildRes.add(a.getItem());
               build.append(a.getItem().name() + ";" + a.getAmount() + ";");
            }

            building.put(type, buildRes);
            build.append("\r\n");
         } catch (Exception var33) {
            var33.printStackTrace();
         }
      }

      LinkedHashSet<String> buildTreeEdges = new LinkedHashSet<>();
      LinkedHashSet<String> treeEdges = new LinkedHashSet<>();
      tree.append("\r\n");

      for (Entry<StructureType, EnumSet<Item.ItemType>> e : outputs.entrySet()) {
         for (Item.ItemType o : e.getValue()) {
            for (Entry<StructureType, EnumSet<Item.ItemType>> f : inputs.entrySet()) {
               if (f.getValue().contains(o) || f.getValue().contains(Item.base(o))) {
                  treeEdges.add("\t" + e.getKey().name() + " -> " + f.getKey().name() + " [xlabel=\"" + o.name() + "\"]\r\n");
               }
            }

            for (Entry<StructureType, EnumSet<Item.ItemType>> fx : building.entrySet()) {
               if (fx.getValue().contains(o) || fx.getValue().contains(Item.base(o))) {
                  buildTreeEdges.add("\t" + e.getKey().name() + " -> " + fx.getKey().name() + " [xlabel=\"" + o.name() + "\"]\r\n");
               }
            }
         }
      }

      for (Entry<StructureType, EnumSet<Item.ItemType>> e : inputs.entrySet()) {
         for (Item.ItemType o : e.getValue()) {
            for (Entry<StructureType, EnumSet<Item.ItemType>> fxx : outputs.entrySet()) {
               if (fxx.getValue().contains(o) || fxx.getValue().contains(Item.base(o))) {
                  treeEdges.add("\t" + fxx.getKey().name() + " -> " + e.getKey().name() + " [xlabel=\"" + o.name() + "\"]\r\n");
               }
            }
         }
      }

      for (Entry<StructureType, EnumSet<Item.ItemType>> e : building.entrySet()) {
         for (Item.ItemType o : e.getValue()) {
            for (Entry<StructureType, EnumSet<Item.ItemType>> fxxx : outputs.entrySet()) {
               if (fxxx.getValue().contains(o) || fxxx.getValue().contains(Item.base(o))) {
                  buildTreeEdges.add("\t" + fxxx.getKey().name() + " -> " + e.getKey().name() + " [xlabel=\"" + o.name() + "\"]\r\n");
               }
            }
         }
      }

      for (String s : buildTreeEdges) {
         buildTree.append(s);
      }

      for (String s : treeEdges) {
         tree.append(s);
      }

      buildTree.append("}");
      tree.append("}");
      sciences.append("}");
      Gdx.files.absolute("../Development/stats/build.csv").writeString(build.toString(), false);
      Gdx.files.absolute("../Development/stats/recipes.csv").writeString(recipes.toString(), false);
      Gdx.files.absolute("../Development/stats/tree.dot").writeString(tree.toString(), false);
      Gdx.files.absolute("../Development/stats/buildtree.dot").writeString(buildTree.toString(), false);
      Gdx.files.absolute("../Development/stats/sciences.dot").writeString(sciences.toString(), false);

      try {
         Runtime.getRuntime().exec(new String[]{"cmd", "/c", "dot -T jpg tree.dot > tree.jpg"}, null, new File("../Development/stats/")).waitFor();
         Runtime.getRuntime().exec(new String[]{"cmd", "/c", "dot -T jpg sciences.dot > sciences.jpg"}, null, new File("../Development/stats/")).waitFor();
      } catch (Exception var32) {
         var32.printStackTrace();
      }
   }
}
