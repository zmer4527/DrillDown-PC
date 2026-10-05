package de.dakror.common.libgdx.io;

import com.badlogic.gdx.files.FileHandleStream;
import com.badlogic.gdx.utils.StreamUtils.OptimizedByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class ByteArrayFileHandle extends FileHandleStream {
   OptimizedByteArrayOutputStream baos;
   ByteArrayInputStream bais;

   public ByteArrayFileHandle() {
      super("");
      this.baos = new OptimizedByteArrayOutputStream(1024);
   }

   public ByteArrayFileHandle(byte[] in) {
      super("");
      this.bais = new ByteArrayInputStream(in);
   }

   public OutputStream write(boolean append) {
      if (!append) {
         this.baos.reset();
      }

      return this.baos;
   }

   public InputStream read() {
      return this.bais;
   }

   public byte[] getBytes() {
      return this.baos.toByteArray();
   }
}
