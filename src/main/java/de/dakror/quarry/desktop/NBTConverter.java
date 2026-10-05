package de.dakror.quarry.desktop;

import de.dakror.common.libgdx.io.NBT;
import java.awt.event.ActionEvent;
import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;

public class NBTConverter extends JFrame {
   private static final long serialVersionUID = 1L;

   public NBTConverter() {
      super("NBTConverter");
      JPanel p = new JPanel();
      p.add(new JButton(new AbstractAction("BIN to TXT") {
         private static final long serialVersionUID = 1L;

         @Override
         public void actionPerformed(ActionEvent e) {
            JFileChooser jfc = new JFileChooser(new File("C:\\Users\\Dakror\\TheQuarry\\saves"));
            jfc.setMultiSelectionEnabled(false);
            jfc.setFileSelectionMode(0);
            if (jfc.showOpenDialog(NBTConverter.this) == 0) {
               try {
                  NBT.CompoundTag t = NBT.read(new BufferedInputStream(new FileInputStream(jfc.getSelectedFile())), NBT.CompressionType.Fast);
                  if (jfc.showSaveDialog(NBTConverter.this) == 0) {
                     BufferedWriter bw = new BufferedWriter(new FileWriter(jfc.getSelectedFile()));
                     bw.write(t.toString());
                     bw.close();
                     JOptionPane.showMessageDialog(NBTConverter.this, jfc.getSelectedFile().getPath() + " saved.");
                  }
               } catch (FileNotFoundException var5) {
                  var5.printStackTrace();
               } catch (IOException var6) {
                  var6.printStackTrace();
               }
            }
         }
      }));
      p.add(new JButton(new AbstractAction("TXT to BIN") {
         private static final long serialVersionUID = 1L;

         @Override
         public void actionPerformed(ActionEvent e) {
            JFileChooser jfc = new JFileChooser(new File("C:\\Users\\Dakror\\TheQuarry\\saves"));
            jfc.setMultiSelectionEnabled(false);
            jfc.setFileSelectionMode(0);
            if (jfc.showOpenDialog(NBTConverter.this) == 0) {
               try {
                  NBT.CompoundTag t = NBT.readText(new FileInputStream(jfc.getSelectedFile()));
                  if (jfc.showSaveDialog(NBTConverter.this) == 0) {
                     NBT.write(new FileOutputStream(jfc.getSelectedFile()), t, NBT.CompressionType.Fast);
                     JOptionPane.showMessageDialog(NBTConverter.this, jfc.getSelectedFile().getPath() + " saved.");
                  }
               } catch (FileNotFoundException var4) {
                  var4.printStackTrace();
               } catch (IOException var5) {
                  var5.printStackTrace();
               }
            }
         }
      }));
      this.setContentPane(p);
      this.pack();
      this.setResizable(false);
      this.setLocationRelativeTo(null);
      this.setDefaultCloseOperation(3);
      this.setVisible(true);
   }

   public static void main(String[] args) {
      try {
         UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      } catch (Exception var2) {
         var2.printStackTrace();
      }

      new NBTConverter();
   }
}
