package de.dakror.quarry.structure.base;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;
import de.dakror.common.BiCallback;
import de.dakror.quarry.Quarry;
import de.dakror.quarry.game.Item;
import de.dakror.quarry.game.Science;
import de.dakror.quarry.structure.base.component.Component;
import de.dakror.quarry.util.Sfx;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import net.spookygames.gdx.sfx.SfxSound;

public class Schema {
   public final int width;
   public final int height;
   private final Dock[] docks;
   public final TextureRegion tex;
   public final Drawable icon;
   public final String name;
   public final String texName;
   public final String description;
   public final boolean clickable;
   public EnumSet<Schema.Flags> flags;
   public final Sfx sfx;
   public float loudness;
   public byte version;
   public int outputDocks;
   public int inputDocks;
   public int powerDocks;
   public boolean highPower;
   public boolean lowPower;
   public final Item.Items buildCosts;
   public final Array<Schema.ButtonDef> buttons;
   public final StructureType type;
   public final EnumSet<Science.ScienceType> sciencesRequired;
   protected final Array<Component> components;
   static final Vector2 tmp = new Vector2();

   public Schema(int version, StructureType type, boolean clickable, int width, int height, String tex, Item.Items buildCosts, Sfx sfx, Dock... docks) {
      this.clickable = clickable;
      this.width = width;
      this.type = type;
      this.height = height;
      this.texName = tex;
      this.sfx = sfx;
      this.version = (byte)version;
      this.loudness = 1.0F;
      if (this.sfx != null) {
         this.sfx.sound = (SfxSound)Quarry.Q.assets.get("sfx/" + sfx.sfx);
      }

      TextureRegion tr = Quarry.Q.atlas.findRegion("structure_" + tex);
      if (tr == null) {
         tr = Quarry.Q.atlas.findRegion("structure_redstone");
         System.err.println("[TEX]  structure '" + type + "'");
      }

      this.name = Quarry.Q.i18n.get("structure." + type.name().toLowerCase());
      this.tex = tr;
      Drawable icon = null;

      try {
         icon = Quarry.Q.skin.getDrawable("icon_" + tex);
      } catch (Exception var17) {
         System.err.println("[ICON] structure '" + type + "'");
      }

      if (icon == null) {
         try {
            icon = Quarry.Q.skin.getDrawable("structure_" + tex);
         } catch (Exception var16) {
            icon = Quarry.Q.skin.getDrawable("structure_redstone");
         }
      }

      this.icon = icon;
      this.docks = docks;
      this.sciencesRequired = EnumSet.noneOf(Science.ScienceType.class);
      this.buttons = new Array(3);
      this.components = new Array();
      this.description = this instanceof ProducerStructure.ProducerSchema ? "" : Quarry.Q.i18n.get("structure." + type.name().toLowerCase() + ".desc");
      this.buildCosts = buildCosts;
      buildCosts.sort();

      for (Dock d : docks) {
         if (d.type == Dock.DockType.ItemIn || d.type == Dock.DockType.FluidIn) {
            this.inputDocks++;
         } else if (d.type == Dock.DockType.ItemOut || d.type == Dock.DockType.FluidOut) {
            this.outputDocks++;
         } else if (d.type == Dock.DockType.Power || d.type == Dock.DockType.BigPower) {
            this.powerDocks++;
            if (d.type == Dock.DockType.Power) {
               this.lowPower = true;
            }

            if (d.type == Dock.DockType.BigPower) {
               this.highPower = true;
            }
         }
      }
   }

   public <T extends Schema> T flags(Schema.Flags... flags) {
      if (this.flags != null) {
         throw new IllegalStateException("Multiple calls to flags!");
      } else {
         this.flags = EnumSet.noneOf(Schema.Flags.class);

         for (Schema.Flags f : flags) {
            this.flags.add(f);
         }

         if (this.has(Schema.Flags.TextureAlwaysUpright) && this.width != this.height) {
            throw new RuntimeException("Width must equal width");
         } else {
            return (T)this;
         }
      }
   }

   public <T extends Schema> T removeFlags(Schema.Flags... flags) {
      this.flags.removeAll(Arrays.asList(flags));
      return (T)this;
   }

   public boolean has(Schema.Flags flag) {
      return this.flags != null && this.flags.contains(flag);
   }

   public <T extends Schema> T sciences(Science.ScienceType... sciences) {
      Collections.addAll(this.sciencesRequired, sciences);
      return (T)this;
   }

   public <T extends Schema> T components(Component... components) {
      this.components.addAll(components);
      return (T)this;
   }

   public <T extends Schema> T loudness(float loudness) {
      this.loudness = loudness;
      return (T)this;
   }

   public <T extends Schema> T button(Schema.ButtonDef button) {
      if (this.buttons.size >= 3) {
         throw new IllegalArgumentException("3 buttons is the maximum");
      } else {
         this.buttons.add(button);
         return (T)this;
      }
   }

   public Component[] copyComponents(Structure<?> structure) {
      if (this.components == null) {
         return new Component[0];
      } else {
         Component[] c = new Component[this.components.size];
         int i = 0;

         for (Component comp : this.components) {
            c[i] = comp.clone();
            c[i].setStructure(structure);
            c[i].init();
            i++;
         }

         return c;
      }
   }

   public Dock[] copyDocks(Structure<?> structure) {
      Dock[] docks = new Dock[this.docks.length];

      for (int i = 0; i < docks.length; i++) {
         Dock d = this.docks[i];
         if (structure.getUpDirection() != Direction.North) {
            float originX;
            float originY;
            if (structure.getUpDirection().dx != 0) {
               originX = (float)structure.getHeight() / 2.0F - 0.5F;
               originY = (float)structure.getWidth() / 2.0F - 0.5F;
            } else {
               originX = (float)this.width / 2.0F - 0.5F;
               originY = (float)this.height / 2.0F - 0.5F;
            }

            tmp.set((float)d.x, (float)d.y).sub(originX, originY).rotate((float)(structure.getUpDirection().rot - Direction.North.rot)).add(originX, originY);
            if (structure.getUpDirection().dx != 0) {
               tmp.x = tmp.x + (float)(structure.getWidth() - structure.getHeight()) / 2.0F;
               tmp.y = tmp.y + (float)(structure.getHeight() - structure.getWidth()) / 2.0F;
            }

            Direction newDir = Direction.values[(d.dir.ordinal() + (structure.getUpDirection().ordinal() - Direction.North.ordinal())) % 4];
            if (d.filter != null) {
               docks[i] = new Dock(Math.round(tmp.x), Math.round(tmp.y), newDir, d.type, new Dock.DockFilter(d.filter));
            } else {
               docks[i] = new Dock(Math.round(tmp.x), Math.round(tmp.y), newDir, d.type);
            }
         } else {
            docks[i] = new Dock(d);
         }
      }

      return docks;
   }

   public Array<Component> getComponents() {
      return this.components;
   }

   public Dock[] getDocks() {
      return this.docks;
   }

   public static class ButtonDef {
      public final String icon;
      public final String tooltip;
      public final Schema.ButtonDef.ButtonType type;
      public final BiCallback<Boolean, Structure<?>> listener;

      public ButtonDef(String icon, String tooltip, Schema.ButtonDef.ButtonType type, BiCallback<Boolean, Structure<?>> callback) {
         this.icon = icon;
         this.tooltip = tooltip;
         this.type = type;
         this.listener = callback;
      }

      public static enum ButtonType {
         SinglePress,
         StateToggle,
         TempRadio;
      }
   }

   public static enum Flags {
      Indestructible,
      Draggable,
      ConfirmDestruction,
      NoDustEffect,
      MirroredTextureHorizontal,
      MirroredTextureVertical,
      NotRotatable,
      TextureAlwaysUpright,
      Stackable;
   }
}
