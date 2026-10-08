package com.gomgomapps.math.core;
import java.io.*;
/** Read asset/resource bytes on every supported Android version. Caller owns the stream. */
public final class StreamBytes {
 private StreamBytes(){}
 public static byte[] read(InputStream input)throws IOException{
  ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] buffer=new byte[8192];int count;
  while((count=input.read(buffer))!=-1){
   if(count==0){int single=input.read();if(single==-1)break;out.write(single);}else out.write(buffer,0,count);
  }
  return out.toByteArray();
 }
}
