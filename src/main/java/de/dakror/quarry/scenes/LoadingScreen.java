package de.dakror.quarry.scenes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.SkinLoader.SkinParameter;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.viewport.FitViewport;
import de.dakror.common.libgdx.ui.Scene;
import de.dakror.quarry.Const;
import de.dakror.quarry.Quarry;
import net.spookygames.gdx.sfx.SfxMusic;
import net.spookygames.gdx.sfx.SfxMusicLoader;
import net.spookygames.gdx.sfx.SfxSound;
import net.spookygames.gdx.sfx.SfxSoundLoader;

public class LoadingScreen extends Scene {
   long l;
   Label label;
   TextureRegion progress;
   TextureRegion bg;
   float prog;
   float visualProg;
   float oldVisualProg;
   float interp;
   boolean finishingUp = false;

   @Override
   public void init() {
      FileHandleResolver resolver = new InternalFileHandleResolver();
      AssetManager assets = new AssetManager(resolver);
      Quarry.Q.assets = assets;
      assets.setLoader(SfxMusic.class, new SfxMusicLoader(resolver));
      assets.setLoader(SfxSound.class, new SfxSoundLoader(resolver));
      assets.load("tex.atlas", TextureAtlas.class);
      FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("Roboto-Medium.ttf"));
      ObjectMap<String, Object> fontMap = new ObjectMap();
      fontMap.put("small-font", this.createFont(generator, 24.0F));
      fontMap.put("default-font", this.createFont(generator, 32.0F));
      fontMap.put("big-font", this.createFont(generator, 48.0F));
      SkinParameter param = new SkinParameter("tex.atlas", fontMap);
      assets.load("skin.json", Skin.class, param);
      assets.finishLoading();
      Quarry.Q.atlas = (TextureAtlas)assets.get("tex.atlas");
      Quarry.Q.skin = (Skin)assets.get("skin.json");
      Quarry.Q.font = Quarry.Q.skin.getFont("default-font");
      Quarry.Q.font.getData().markupEnabled = true;
      Quarry.Q.font.setFixedWidthGlyphs("0123456789-+");
      Quarry.Q.skin.getFont("small-font").setFixedWidthGlyphs("0123456789");
      this.progress = Quarry.Q.atlas.findRegion("structure_conveyor_we");
      this.bg = Quarry.Q.atlas.findRegion("button");
      this.stage = new Stage(new FitViewport(Const.UI_W, Const.UI_H));
      this.stage.setActionsRequestRendering(false);
      this.label = new Label(Quarry.Q.i18n.get("loading.sounds"), Quarry.Q.skin);
      this.label.setAlignment(1);
      Table t = new Table();
      t.setBackground(Quarry.Q.skin.getTiledDrawable("tile_stone"));
      t.add(this.label).grow();
      t.setSize(Const.UI_W, Const.UI_H);
      this.stage.addActor(t);
      assets.load("sfx/airpurifier" + Const.SFX_FORMAT, SfxSound.class);
      assets.load("sfx/anchorportal" + Const.SFX_FORMAT, SfxSound.class);
      assets.load("sfx/arcwelder" + Const.SFX_FORMAT, SfxSound.class);
      assets.load("sfx/assembler" + Const.SFX_FORMAT, SfxSound.class);
      assets.load("sfx/ballmill" + Const.SFX_FORMAT, SfxSound.class);
      assets.load("sfx/bender" + Const.SFX_FORMAT, SfxSound.class);
      assets.load("sfx/boiler" + Const.SFX_FORMAT, SfxSound.class);
      assets.load("sfx/booster" + Const.SFX_FORMAT, SfxSound.class);
      assets.load("sfx/carpenter" + Const.SFX_FORMAT, SfxSound.class);
      assets.load("sfx/centrifuge" + Const.SFX_FORMAT, SfxSound.class);
      assets.load("sfx/charcoalmound" + Const.SFX_FORMAT, SfxSound.class);
      assets.load("sfx/column" + Const.SFX_FORMAT, SfxSound.class);
      assets.load("sfx/compactor" + Const.SFX_FORMAT, SfxSound.class);
      assets.load("sfx/condenser" + Const.SFX_FORMAT, SfxSound.class);
      assets.load("sfx/crucible" + Const.SFX_FORMAT, SfxSound.class);
      assets.load("sfx/devicefabricator" + Const.SFX_FORMAT, SfxSound.class);
      assets.load("sfx/digitalstorage" + Const.SFX_FORMAT, SfxSound.class);
      assets.load("sfx/drawer" + Const.SFX_FORMAT, SfxSound.class);
      assets.load("sfx/excavator" + Const.SFX_FORMAT, SfxSound.class);
      assets.load("sfx/furnace" + Const.SFX_FORMAT, SfxSound.class);
      assets.load("sfx/injection" + Const.SFX_FORMAT, SfxSound.class);
      assets.load("sfx/kiln" + Const.SFX_FORMAT, SfxSound.class);
      assets.load("sfx/mason" + Const.SFX_FORMAT, SfxSound.class);
      assets.load("sfx/mine" + Const.SFX_FORMAT, SfxSound.class);
      assets.load("sfx/mixer" + Const.SFX_FORMAT, SfxSound.class);
      assets.load("sfx/node" + Const.SFX_FORMAT, SfxSound.class);
      assets.load("sfx/oilwell" + Const.SFX_FORMAT, SfxSound.class);
      assets.load("sfx/polarizer" + Const.SFX_FORMAT, SfxSound.class);
      assets.load("sfx/polymerizer" + Const.SFX_FORMAT, SfxSound.class);
      assets.load("sfx/pump1" + Const.SFX_FORMAT, SfxSound.class);
      assets.load("sfx/refinery" + Const.SFX_FORMAT, SfxSound.class);
      assets.load("sfx/rockcrusher" + Const.SFX_FORMAT, SfxSound.class);
      assets.load("sfx/rollingmachine" + Const.SFX_FORMAT, SfxSound.class);
      assets.load("sfx/sawmill" + Const.SFX_FORMAT, SfxSound.class);
      assets.load("sfx/science" + Const.SFX_FORMAT, SfxSound.class);
      assets.load("sfx/shaftdrill" + Const.SFX_FORMAT, SfxSound.class);
      assets.load("sfx/stacker" + Const.SFX_FORMAT, SfxSound.class);
      assets.load("sfx/turbine" + Const.SFX_FORMAT, SfxSound.class);
      assets.load("sfx/waterwheel" + Const.SFX_FORMAT, SfxSound.class);
      assets.load("sfx/woodcutter" + Const.SFX_FORMAT, SfxSound.class);
      assets.load("sfx/click3" + Const.SFX_FORMAT, Sound.class);
      assets.load("sfx/build" + Const.SFX_FORMAT, Sound.class);
      assets.load("sfx/destroy" + Const.SFX_FORMAT, Sound.class);
      assets.load("sfx/cable" + Const.SFX_FORMAT, Sound.class);
      assets.load("sfx/ambience_empty" + Const.SFX_FORMAT, Sound.class);
      assets.load("sfx/ambience_base" + Const.SFX_FORMAT, Sound.class);
      assets.load("sfx/ambience_heavy" + Const.SFX_FORMAT, Sound.class);
      assets.load("music/Fading_into_the_Dream" + Const.MUSIC_FORMAT, Music.class);
      assets.load("music/Impact Prelude" + Const.MUSIC_FORMAT, Music.class);
      this.l = System.currentTimeMillis();
   }

   protected BitmapFont createFont(FreeTypeFontGenerator gen, float dp) {
      FreeTypeFontParameter param = new FreeTypeFontParameter();
      param.magFilter = TextureFilter.Linear;
      param.minFilter = TextureFilter.Linear;
      param.characters = "\u0000ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890\"!`?'.,;:()[]{}<>|/@\\^$€-%+=#_&~*\u007f\u0080\u0081\u0082\u0083\u0084\u0085\u0086\u0087\u0088\u0089\u008a\u008b\u008c\u008d\u008e\u008f\u0090\u0091\u0092\u0093\u0094\u0095\u0096\u0097\u0098\u0099\u009a\u009b\u009c\u009d\u009e\u009f ¡¢£¤¥¦§¨©ª«¬\u00ad®¯°±²³´µ¶·¸¹º»¼½¾¿ÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷øùúûüýþÿ你缺乏一些科学知识来描绘这个蓝图。此自更新的游戏版本，无法加载该存档由完整创建在演示中请重启使语言改生效文件区域网格增强筑造电缆切断选择拆除用器：共享时间并且同步粘贴暂停从库取出将退还资源放入储打开技目录产摘要燃料元满托盘液体油材原矿粉可堆叠消否确定是删吗？不撤销恭喜已经成了采石场全部内容希望欢前往商店购买支持小型独立发者工作我会非常感激！论都结束保所以卡里如果现它继续玩栋其有物品永远失相连漏斗起钻头覆盖先当城高级机架池砖块青铜锭板丝碳纤维水泥木炭（）芯片土煤混凝碎管桶细炸药空罐天然气玻璃金火硬化钢属预处理蒸馏铁铅润滑剂磁熔融硅银锡钛纸张塑珠壳合压汽黄英精炼转子沙脚手隧道井段线硫磺晶涡轮正声音导读外夹回主菜单没另为教程输名种码意味着什么就按运或缩地基础升通过上写东西助推换附近速度很己组装巅峰之至少计算谨慎施心最终收多力供应待足大功率复杂获得被极而性制折弯和扭曲却够壮试扩械挖掘深提进术始动价值黑抽利泵集五颜六色实错但形状行滤解锁筛及航太阳能表量让坚圆长纯净必须把命变业优势等周围尘埃锚充低口配弧焊构接防耐汇编盐良好护套封底面填包裹根棒标准尺寸叶到杆固几台球磨较软数某式质炉任何类沸烧需温越受流热竖两邻层下对矩阵离分含见稀快致密粗巨冷传送带沿方向桥允许条互交叉每秒千焦均匀布坩埚拉设备模具字据耗即柱萃顶缓慢后引爆灌便于限双循环串联只边置冶铸倒注态降梯窑陶伐人植砍树匠割漂亮剩余浆与浸泡洗才仅侧聚培养链塔户厂普岩辊平锯验室摆弄看奇情况彼筒仓光核依次垛卸比再们去节点适冲超泄箱简绕轴真阀门控车洒项勤剪败拒绝访问活迎骤击息查第止世界诸达埋藏宝样特征直他“”键辅拖拽端志轨哎呀像题旋钮…误事返随指移视野屏幕滚箭鼠仔观察称初显左角右总首稳规、述求脉绿调位恢尽考虑报告反馈未胜谢您截翻修默认关闭减统帝盒际别鸣谈汉";
      if (Quarry.Q.desktop) {
         param.size = (int)(dp * (Float)Quarry.Q.pi.message(14, null));
      } else {
         param.size = (int)(dp * (Float)Quarry.Q.pi.message(14, null));
      }

      BitmapFont font = gen.generateFont(param);
      font.getData().markupEnabled = true;
      return font;
   }

   @Override
   public void update(double deltaTime) {
      super.update(deltaTime);
      float p = Quarry.Q.assets.getProgress();
      if (p != this.prog) {
         this.oldVisualProg = this.visualProg;
         this.interp = 0.0F;
         this.prog = p;
      }

      if (this.visualProg != this.prog) {
         if (this.interp >= 1.0F) {
            this.visualProg = this.prog;
            this.interp = 0.0F;
         } else {
            this.visualProg = this.oldVisualProg + (this.prog - this.oldVisualProg) * this.interp;
            this.interp = (float)((double)this.interp + deltaTime / 0.1F);
         }
      }

      if (Quarry.Q.assets.getProgress() > 0.9F) {
         this.label.setText(Quarry.Q.i18n.get("loading.buildings"));
      }

      try {
         if (!this.finishingUp && Quarry.Q.assets.update() && this.visualProg == 1.0F && !this.fadeOut) {
            System.out.println("Asset loading took " + (System.currentTimeMillis() - this.l) + " ms");
            this.finishingUp = true;
            Quarry.Q.loadingFinished();
         }
      } catch (Exception var5) {
         Quarry.Q.pi.message(1, var5);
      }
   }

   @Override
   public void draw() {
      Gdx.gl.glClearColor(0.49609375F, 0.49609375F, 0.49609375F, 1.0F);
      super.draw();
      this.stage.getBatch().begin();
      this.stage.getBatch().setColor(0.0F, 0.0F, 0.0F, 0.5F);
      int x = (int)((Const.UI_W - 600.0F) / 2.0F);
      this.stage.getBatch().draw(this.bg, (float)x, Const.UI_H / 4.0F, 600.0F, 40.0F);
      this.stage.getBatch().setColor(1.0F, 1.0F, 1.0F, 1.0F);
      float p = 600.0F * this.visualProg;

      for (int i = 0; i < (int)p / 64; i++) {
         this.stage.getBatch().draw(this.progress, (float)(x + i * 64), Const.UI_H / 4.0F - 12.0F);
      }

      float len = (float)((int)(p / 64.0F) * 64);
      this.stage
         .getBatch()
         .draw(
            this.progress.getTexture(),
            (float)x + len,
            Const.UI_H / 4.0F - 12.0F,
            p - len,
            64.0F,
            this.progress.getU(),
            this.progress.getV2(),
            (this.progress.getU2() - this.progress.getU()) * (p - len) / 64.0F + this.progress.getU(),
            this.progress.getV()
         );
      this.stage.getBatch().end();
   }
}
