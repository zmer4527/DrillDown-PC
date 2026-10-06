package de.dakror.quarry.structure.base;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.IntSet;
import de.dakror.common.libgdx.io.NBT;
import de.dakror.common.libgdx.math.QuadTree;
import de.dakror.common.libgdx.render.SpriteRenderer;
import de.dakror.quarry.Quarry;
import de.dakror.quarry.game.Item;
import de.dakror.quarry.game.Layer;
import de.dakror.quarry.game.power.PowerNetwork;
import de.dakror.quarry.scenes.Game;
import de.dakror.quarry.structure.Booster;
import de.dakror.quarry.structure.base.component.Component;
import de.dakror.quarry.util.Bounds;
import de.dakror.quarry.util.Savable;
import de.dakror.quarry.util.SpriterDelegateBatch;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public abstract class Structure<T extends Schema> implements Savable, QuadTree.Region {
   public static final StructureType[] types = new StructureType[65536];
   public int x;
   public int y;
   protected T schema;
   public Layer layer;
   protected boolean clicked;
   protected long sfxId = -1L;
   protected boolean sfxPlaying;
   protected Component[] components;
   public static final TextureRegion fullStateIcon = Quarry.Q.atlas.findRegion("icon_cinventory");
   public static final TextureRegion powerStateIcon = Quarry.Q.atlas.findRegion("icon_power");
   public static final TextureRegion boostStateIcon = Quarry.Q.atlas.findRegion("symb_ff");
   public static final Color selectionColor = Color.valueOf("#4286f4a0");
   protected Booster nearbyBooster;
   protected PowerNetwork powerNetwork;
   protected float powerReceivedThisTick;
   protected int speedScale = 1;
   protected int gameSpeed;
   protected float time;
   protected int index;
   protected byte version;
   protected boolean destroyed;
   protected Direction upDirection;
   protected Dock[] docks;
   static Set<StructureType> schemaVersionCheck = new HashSet<>();

   protected Structure(int x, int y, T schema) {
      this.x = x;
      this.y = y;
      this.schema = schema;
      this.version = schema.version;
      this.upDirection = Direction.North;
      this.docks = schema.copyDocks(this);
      this.components = schema.copyComponents(this);
      this.index = -1;
   }

   public Direction getUpDirection() {
      return this.upDirection;
   }

   public void setUpDirection(Direction upDirection) {
      this.upDirection = upDirection;
      this.docks = this.schema.copyDocks(this);
      this.setDirty();
   }

   public Dock[] getDocks() {
      return this.docks;
   }

   public Booster getNearbyBooster() {
      return this.nearbyBooster;
   }

   public void setNearbyBooster(Booster booster) {
      this.nearbyBooster = booster;
      if (booster == null) {
         this.speedScale = 1;
      }
   }

   public T getSchema() {
      return this.schema;
   }

   @Override
   public int getX() {
      return this.x;
   }

   @Override
   public int getY() {
      return this.y;
   }

   public int getIndex() {
      if (this.layer == null) {
         return Game.G.layer.getIndex() * Game.G.layer.width * Game.G.layer.height + this.x * Game.G.layer.height + this.y;
      } else {
         if (this.index == -1) {
            this.index = this.layer.getIndex() * this.layer.width * this.layer.height + this.x * this.layer.height + this.y;
         }

         return this.index;
      }
   }

   public int getDonorPriority() {
      return 0;
   }

   public int getReceiverPriority() {
      return 0;
   }

   public double getPowerLevel() {
      return 0.0;
   }

   public double getPowerCapacity() {
      return 0.0;
   }

   public double getPowerRatio() {
      double level = this.getPowerLevel();
      return level == 0.0 ? 0.0 : level / this.getPowerCapacity();
   }

   public double acceptPower(double power, double networkStrength) {
      return power;
   }

   public double requestPower(double power, double networkStrength) {
      return 0.0;
   }

   public void refundPower(double power) {
   }

   public double getPowerReceivedThisTick() {
      return (double)this.powerReceivedThisTick;
   }

   public PowerNetwork getPowerNetwork() {
      return this.powerNetwork;
   }

   public void setPowerNetwork(PowerNetwork network) {
      this.powerNetwork = network;
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.getSchema().type, this.getIndex());
   }

   @Override
   public boolean equals(Object obj) {
      return obj instanceof Structure && obj.hashCode() == this.hashCode();
   }

   @Override
   public int getWidth() {
      return this.upDirection.dx != 0 ? this.schema.height : this.schema.width;
   }

   @Override
   public int getHeight() {
      return this.upDirection.dx != 0 ? this.schema.width : this.schema.height;
   }

   public float getLoudness() {
      return this.getSchema().loudness * (float)this.speedScale;
   }

   public int getSpeedScale() {
      return this.speedScale;
   }

   public boolean isDestroyed() {
      return this.destroyed;
   }

   public void update(double deltaTime, int gameSpeed, Bounds dirtyBounds) {
      this.gameSpeed = gameSpeed;
      if (gameSpeed > 0) {
         this.time = (float)((double)this.time + deltaTime);
         if (this.time > 1.0F) {
            this.time--;
         }
      }

      for (Component c : this.components) {
         c.update(deltaTime, gameSpeed, dirtyBounds);
      }
   }

   public void postUpdate(Bounds dirtyBounds) {
      if (this.nearbyBooster != null) {
         this.speedScale = this.nearbyBooster.isBoostActive() ? 3 : 1;
      }

      this.powerReceivedThisTick = 0.0F;
   }

   public void postLoad() {
      for (Component c : this.components) {
         c.postLoad();
      }
   }

   /** 清空未存档的瞬态历史（滑动窗口、倒计时等）。联机发世界前主机会调用，让两端起点一致。 */
   public void clearTransientState() {
      for (Component c : this.components) {
         c.clearTransientState();
      }
   }

   public void reloadPowerNetwork() {
      if (this.getSchema().powerDocks > 0) {
         this.powerNetwork.updateConnections(this);
      }
   }

   protected void setDirty() {
      if (this.layer != null) {
         this.layer.markDirty(this, 0);
      }
   }

   public boolean canAccept(Item.ItemType item, int x, int y, Direction dir) {
      return false;
   }

   public boolean acceptItem(Item.ItemType item, Structure<?> source, Direction dir) {
      return false;
   }

   public int acceptFluid(Item.ItemType item, int amount, Structure<?> source) {
      return amount;
   }

   public boolean isNextToDock(int x, int y, Direction dir, Dock d) {
      return x == this.x + d.x + d.dir.dx && y == this.y + d.y + d.dir.dy && dir != null && d.dir == dir.inv();
   }

   protected boolean isAdjacentTo(Structure<?> other) {
      return this.x + this.getWidth() == other.x
         || other.x + other.getWidth() == this.x
         || this.y + this.getHeight() == other.y
         || other.y + other.getHeight() == this.y;
   }

   public boolean putBack(Item.ItemType item, int amount) {
      System.err.println("Operation putBack not supported on " + this.getClass());
      return false;
   }

   public void drawFrame(SpriteRenderer spriter, ShapeRenderer shaper, SpriterDelegateBatch pfxBatch) {
      for (Component c : this.components) {
         c.drawFrame(spriter, shaper);
      }

      if (this.clicked) {
         this.drawHighlighting(shaper);
      }
   }

   protected void drawBoostState(SpriteRenderer spriter) {
      if (this.gameSpeed > 1) {
         float size = 24.0F * (1.0F + 0.3F * (MathUtils.sin(this.time * 2.0F * (float) Math.PI) * 0.5F + 0.5F));
         spriter.add(
            boostStateIcon,
            (float)(this.x * 64) - (size - 24.0F) / 2.0F + 6.0F,
            (float)((this.y + this.getHeight()) * 64) - (size - 24.0F) / 2.0F - 30.0F,
            -1.0F,
            size,
            size
         );
      }
   }

   public void drawHighlighting(ShapeRenderer shaper) {
      shaper.setColor(selectionColor);
      shaper.line((float)(this.x * 64), (float)(this.y * 64), (float)((this.x + this.getWidth()) * 64), (float)(this.y * 64));
      shaper.line(
         (float)(this.x * 64), (float)((this.y + this.getHeight()) * 64), (float)((this.x + this.getWidth()) * 64), (float)((this.y + this.getHeight()) * 64)
      );
      shaper.line((float)(this.x * 64), (float)(this.y * 64), (float)(this.x * 64), (float)((this.y + this.getHeight()) * 64));
      shaper.line(
         (float)((this.x + this.getWidth()) * 64), (float)(this.y * 64), (float)((this.x + this.getWidth()) * 64), (float)((this.y + this.getHeight()) * 64)
      );
      shaper.setColor(selectionColor.r, selectionColor.g, selectionColor.b, 0.25F);
      shaper.rect((float)(this.x * 64), (float)(this.y * 64), (float)(this.getWidth() * 64), (float)(this.getHeight() * 64));
   }

   public void draw(SpriteRenderer spriter) {
      if (this.docks.length > 0) {
         this.drawDocks(spriter);
      }

      float x = (float)(this.x * 64);
      float y = (float)(this.y * 64);
      float z = -3.0F;
      float width = (float)(this.schema.width * 64);
      float height = (float)(this.schema.height * 64);
      float originX;
      float originY;
      if (this.upDirection.dx != 0) {
         originX = (float)this.getHeight() / 2.0F * 64.0F;
         originY = (float)this.getWidth() / 2.0F * 64.0F;
         x += (float)(this.getWidth() - this.getHeight()) / 2.0F * 64.0F;
         y += (float)(this.getHeight() - this.getWidth()) / 2.0F * 64.0F;
      } else {
         originX = (float)this.schema.width / 2.0F * 64.0F;
         originY = (float)this.schema.height / 2.0F * 64.0F;
      }

      float rotation = (float)(this.upDirection.rot - Direction.North.rot);
      if (this.getSchema().has(Schema.Flags.TextureAlwaysUpright)) {
         rotation = 0.0F;
      }

      if (!this.getSchema().has(Schema.Flags.MirroredTextureHorizontal) && !this.getSchema().has(Schema.Flags.MirroredTextureVertical)) {
         spriter.add(this.getSchema().tex, x, y, z, originX, originY, width, height, 1.0F, 1.0F, rotation);
      } else if (this.getSchema().has(Schema.Flags.MirroredTextureHorizontal) && this.getSchema().has(Schema.Flags.MirroredTextureVertical)) {
         spriter.add(this.getSchema().tex, x, y, z, originX, originY, width * 0.5F, height * 0.5F, 1.0F, 1.0F, rotation);
         spriter.add(this.getSchema().tex, x, y, z, originX, originY, width * 0.5F, height * 0.5F, 1.0F, -1.0F, rotation);
         spriter.add(this.getSchema().tex, x, y, z, originX, originY, width * 0.5F, height * 0.5F, -1.0F, -1.0F, rotation);
         spriter.add(this.getSchema().tex, x, y, z, originX, originY, width * 0.5F, height * 0.5F, -1.0F, 1.0F, rotation);
      } else {
         spriter.add(
            this.getSchema().tex,
            x,
            y,
            z,
            originX,
            originY,
            width * (this.getSchema().has(Schema.Flags.MirroredTextureHorizontal) ? 0.5F : 1.0F),
            height * (this.getSchema().has(Schema.Flags.MirroredTextureHorizontal) ? 1.0F : 0.5F),
            1.0F,
            1.0F,
            rotation
         );
         spriter.add(
            this.getSchema().tex,
            x,
            y,
            z,
            originX,
            originY,
            width * (this.getSchema().has(Schema.Flags.MirroredTextureHorizontal) ? 0.5F : 1.0F),
            height * (this.getSchema().has(Schema.Flags.MirroredTextureHorizontal) ? 1.0F : 0.5F),
            this.getSchema().has(Schema.Flags.MirroredTextureHorizontal) ? -1.0F : 1.0F,
            this.getSchema().has(Schema.Flags.MirroredTextureHorizontal) ? 1.0F : -1.0F,
            rotation
         );
      }
   }

   protected void drawFullState(SpriteRenderer spriter) {
      float size = 24.0F * (1.0F + 0.3F * (MathUtils.sin(this.time * 2.0F * (float) Math.PI) * 0.5F + 0.5F));
      spriter.add(
         fullStateIcon,
         (float)((this.x + this.getWidth()) * 64) - (size - 24.0F) / 2.0F - 30.0F,
         (float)((this.y + this.getHeight()) * 64) - (size - 24.0F) / 2.0F - 30.0F,
         -1.0F,
         size,
         size
      );
   }

   protected void drawDocks(SpriteRenderer spriter) {
      int o = 24;
      float s = (float)(64 - o);

      for (Dock d : this.getDocks()) {
         float dx = (float)((this.x + d.x) * 64 + 12 + d.dir.dx * 52);
         float dy = (float)((this.y + d.y) * 64 + 12 + d.dir.dy * 52);
         TextureRegion tr = d.type.tex;
         if (tr != null) {
            spriter.add(tr, dx, dy, d.type == Dock.DockType.Power ? -1.99F : -4.99F, s / 2.0F, s / 2.0F, s, 13.0F, 1.0F, 1.0F, (float)(d.dir.ordinal() * -90));
         }
      }
   }

   @Override
   public Object clone() {
      try {
         Structure<?> s = null;
         if (this.getSchema().type.versionSwitch) {
            s = (Structure<?>)this.getSchema().type.constr.newInstance(this.x, this.y, this.version);
         } else {
            s = (Structure<?>)this.getSchema().type.constr.newInstance(this.x, this.y);
         }

         s.setUpDirection(this.upDirection);
         return s;
      } catch (Exception var2) {
         Quarry.Q.pi.message(1, var2);
         return null;
      }
   }

   public void onDestroy() {
      this.destroyed = true;
   }

   public void onPlacement(boolean fromLoading) {
      if (!fromLoading) {
         for (Component c : this.components) {
            c.onPlacement();
         }
      }
   }

   public boolean getButtonState(int buttonIndex) {
      return false;
   }

   public void onClick(Table content) {
      this.clicked = true;
   }

   public boolean isClicked() {
      return this.clicked;
   }

   public void onUnclick() {
      this.clicked = false;
   }

   public Component[] getComponents() {
      return this.components;
   }

   public void setItemNotifications() {
      if (this.getSchema().inputDocks != 0 && this.layer != null) {
         for (Dock d : this.getDocks()) {
            if (d.type == Dock.DockType.ItemIn) {
               this.layer.setItemNotification(this.x + d.x + d.dir.dx, this.y + d.y + d.dir.dy);
            }
         }
      }
   }

   protected void playSfx() {
      if (!this.sfxPlaying && this.getSchema().sfx != null && Quarry.Q.sound.isPlaySound() && Game.G.layer == this.layer) {
         if (this.sfxId != -1L) {
            Game.G.spatializedPlayer.resume(this.sfxId);
            this.sfxPlaying = true;
         } else {
            this.sfxId = Game.G.spatializedPlayer.play(this, this.getSchema().sfx.sound, this.getSchema().sfx.pitch, true, true);
            this.sfxPlaying = this.sfxId != -1L;
         }
      }
   }

   protected void pauseSfx() {
      if (this.sfxPlaying && this.sfxId != -1L) {
         Game.G.spatializedPlayer.pause(this.sfxId);
         this.sfxPlaying = false;
      }
   }

   public void stopSfx() {
      if (this.sfxId != -1L) {
         Game.G.spatializedPlayer.stop(this.sfxId);
         this.sfxId = -1L;
         this.sfxPlaying = false;
      }
   }

   @Override
   public final void save(NBT.Builder b) {
      b.Compound().Short("type", this.getSchema().type.id).Int("x", this.x).Int("y", this.y);
      this.saveData(b);
      b.End();
   }

   public final NBT.CompoundTag copy(int[] copyRegion) {
      NBT.Builder b = new NBT.Builder(null)
         .Short("type", this.getSchema().type.id)
         .Byte("version", this.version)
         .Int("x", this.x - copyRegion[0])
         .Int("y", this.y - copyRegion[1]);
      this.copyData(copyRegion, b);
      b.End();
      return b.Get();
   }

   protected void copyData(int[] copyRegion, NBT.Builder b) {
      b.Byte("upDir", (byte)this.upDirection.ordinal());
   }

   public final void paste(int[] pasteRegion, NBT.CompoundTag tag) {
      this.pasteData(pasteRegion, tag);
   }

   protected void pasteData(int[] pasteRegion, NBT.CompoundTag tag) {
      if (tag.has("upDir")) {
         this.setUpDirection(Direction.values[tag.Byte("upDir", (byte)0)]);
      }
   }

   @Override
   public String toString() {
      return Integer.toString(this.getIndex());
   }

   protected void saveData(NBT.Builder b) {
      b.Byte("upDir", (byte)this.upDirection.ordinal()).Byte("version", this.version);
      if (this.components.length > 0) {
         b.List("Components", NBT.TagType.Compound);

         for (Component c : this.components) {
            c.saveData(b);
         }

         b.End();
      }
   }

   protected void loadData(NBT.CompoundTag tag) throws NBT.NBTException {
      if (tag.has("upDir")) {
         this.setUpDirection(Direction.values[tag.Byte("upDir", (byte)0)]);
      }

      if (this.components.length > 0) {
         try {
            NBT.ListTag list = tag.List("Components");

            for (int i = 0; i < list.data.size; i++) {
               this.components[i].loadData((NBT.CompoundTag)list.data.get(i));
            }
         } catch (NBT.NBTException var4) {
            Quarry.Q.pi.message(1, var4);
         }
      }
   }

   public static <T extends Schema> T selectSchema(int version, T... schemas) {
      if (!schemaVersionCheck.contains(schemas[0].type)) {
         IntSet s = new IntSet();

         for (T t : schemas) {
            if (s.contains(t.version)) {
               throw new RuntimeException(t.type.name() + ": Multiple schemas with same version!");
            }

            s.add(t.version);
         }

         schemaVersionCheck.add(schemas[0].type);
      }

      for (T t : schemas) {
         if (t.version == version) {
            return t;
         }
      }

      return null;
   }

   public static Structure<?> load(NBT.CompoundTag tag) throws NBT.NBTException {
      short b = tag.Short("type");
      StructureType t = types[b & '\uffff'];
      if (t == null) {
         System.err.println("Not loading Structure with id: " + b);
         return null;
      } else {
         Structure<?> s = null;
         int x = tag.Int("x");
         int y = tag.Int("y");
         int version = tag.Byte("version", (byte)0) & 255;

         try {
            if (t.versionSwitch) {
               s = (Structure<?>)t.constr.newInstance(x, y, version);
            } else {
               s = (Structure<?>)t.constr.newInstance(x, y);
            }
         } catch (Exception var8) {
            Quarry.Q.pi.message(1, var8);
         }

         s.loadData(tag);
         return s;
      }
   }

   public static Structure<?> loadPaste(int[] pasteRegion, NBT.CompoundTag tag) throws NBT.NBTException {
      short b = tag.Short("type");
      StructureType t = types[b & '\uffff'];
      if (t == null) {
         System.err.println("Not loading Structure with id: " + b);
         return null;
      } else {
         Structure<?> s = null;
         int x = tag.Int("x") + pasteRegion[0];
         int y = tag.Int("y") + pasteRegion[1];
         int version = tag.Byte("version", (byte)0) & 255;

         try {
            if (t.versionSwitch) {
               s = (Structure<?>)t.constr.newInstance(x, y, version);
            } else {
               s = (Structure<?>)t.constr.newInstance(x, y);
            }
         } catch (Exception var9) {
            Quarry.Q.pi.message(1, var9);
         }

         return s;
      }
   }
}
