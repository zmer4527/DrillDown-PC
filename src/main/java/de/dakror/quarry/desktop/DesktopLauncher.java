package de.dakror.quarry.desktop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.github.czyzby.lml.parser.impl.tag.Dtd;
import de.dakror.common.libgdx.GameBase;
import de.dakror.common.libgdx.PlatformInterface;
import de.dakror.quarry.Quarry;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import net.spookygames.gdx.sfx.desktop.DesktopAudioDurationResolver;

public class DesktopLauncher implements PlatformInterface {
   String[] arg;
   String version;
   JFrame errorFrame;

   public static void main(String[] arg) {
      new DesktopLauncher(arg);
   }

   public DesktopLauncher(String[] arg) {
      try {
         UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      } catch (Exception var9) {
         var9.printStackTrace();
      }

      LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
      DisplayMode dm = LwjglApplicationConfiguration.getDesktopDisplayMode();
      GameBase.WindowMode mode = null;
      if (arg.length > 0 && (arg[0].equals("debug") || arg[0].equals("meta") || arg[0].equals("windowed"))) {
         if (dm.width == 1920) {
            config.width = 1280;
            config.height = 720;
         } else {
            config.width = 720;
            config.height = 405;
         }

         config.resizable = true;
      } else {
         config.width = dm.width;
         config.height = dm.height;
         mode = GameBase.WindowMode.Borderless;
      }

      // 关掉垂直同步：开了之后 Display.update() 会阻塞等垂直回扫，窗口被遮挡/最小化/切工作区时
      // 合成器可能把交换挂起，整个渲染循环卡住 → Game.update 不执行 → 模拟"静止"。
      // 关掉后由下面的 Display.sync(帧率) 用定时器限帧。
      config.vSyncEnabled = false;
      config.audioDeviceSimultaneousSources = 32;
      int versionCode = 122;
      String version = "v122";
      this.version = version;
      this.arg = arg;
      config.addIcon("icon-16.png", FileType.Internal);
      config.addIcon("icon-32.png", FileType.Internal);
      config.addIcon("icon-64.png", FileType.Internal);
      config.title = "Drill Down";
      if (arg.length > 0 && arg[0].equals("textures")) {
         try {
            TexturePacker.main(new String[]{"./Development/Textures/", "./android/assets/", "tex.atlas", "./android/assets/atlas-settings.json"});
         } catch (Exception var8) {
            var8.printStackTrace();
         }
      }

      DesktopAudioDurationResolver.initialize();
      Quarry game = new Quarry(this, true, versionCode, version, true, false, mode);
      new LwjglApplication(game, config);
   }

   @Override
   public Object message(int messageCode, Object payload) {
      switch (messageCode) {
         case -1230:
            if (this.arg.length > 0 && this.arg[0].equals("meta") && this.version.equals("debug")) {
               MetaGen.run();

               try {
                  Writer writer = Gdx.files.absolute("../Development/lml.dtd").writer(false);
                  new Dtd().setDisplayLogs(false).getDtdSchema(((Quarry)payload).lml, writer);
                  writer.close();
               } catch (Exception var9) {
                  var9.printStackTrace();
               }

               System.exit(0);
            }
            break;
         case 1:
            StringWriter sw = new StringWriter();
            ((Exception)payload).printStackTrace(new PrintWriter(sw));
            if (this.errorFrame == null) {
               this.errorFrame = new JFrame("Error!");
               this.errorFrame.setSize(500, 300);
               this.errorFrame.setAlwaysOnTop(true);
               this.errorFrame.setLocationRelativeTo(null);
               JPanel panel = new JPanel();
               panel.setLayout(new BorderLayout());
               JLabel l = new JLabel("<html>" + Quarry.Q.i18n.get("ui.error") + "<br>Details:</html>");
               l.setMaximumSize(new Dimension(450, 300));
               l.setBorder(new EmptyBorder(20, 20, 20, 20));
               l.setFont(l.getFont().deriveFont(14.0F));
               panel.add(l, "North");
               JTextPane l1 = new JTextPane();
               l1.setContentType("text/html");
               l1.setText("<html><pre>" + sw.toString() + "</pre></html>");
               l1.setFont(new Font("Monospaced", 0, 14));
               JScrollPane jsp = new JScrollPane(l1, 20, 30);
               jsp.setMaximumSize(new Dimension(450, 100));
               panel.add(jsp, "Center");
               panel.add(new JButton(new AbstractAction("Ok") {
                  private static final long serialVersionUID = 1L;

                  @Override
                  public void actionPerformed(ActionEvent e) {
                     DesktopLauncher.this.errorFrame.setVisible(false);
                     DesktopLauncher.this.errorFrame.dispose();
                     DesktopLauncher.this.errorFrame = null;
                  }
               }), "South");
               this.errorFrame.setContentPane(panel);
               this.errorFrame.setVisible(true);
            }

            ((Exception)payload).printStackTrace();
            break;
         case 13:
            return true;
         case 14:
            return (float)Toolkit.getDefaultToolkit().getScreenResolution() / 96.0F;
         case 15:
            return new int[4];
         case 17:
            try {
               return Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
            } catch (Exception var8) {
               return null;
            }
         case 18:
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(payload.toString()), null);
      }

      return null;
   }
}
