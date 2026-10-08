package com.gomgomapps.math.core;
import java.io.*;import java.nio.charset.StandardCharsets;import org.junit.Test;import static org.junit.Assert.*;
public class StreamBytesTest {
 @Test public void exactUtf8BeyondBufferAndCallerKeepsOwnership()throws Exception{
  byte[] expected=("국가\tNamibia\r\n한국어\n".repeat(2000)).getBytes(StandardCharsets.UTF_8);boolean[] closed={false};
  InputStream input=new ByteArrayInputStream(expected){@Override public void close(){closed[0]=true;}};
  assertArrayEquals(expected,StreamBytes.read(input));assertFalse(closed[0]);assertArrayEquals(new byte[0],StreamBytes.read(new ByteArrayInputStream(new byte[0])));
 }
 @Test public void zeroAndPartialBulkReadsMakeProgress()throws Exception{
  byte[] expected={0,1,2,127,-1};InputStream input=new ByteArrayInputStream(expected){boolean zero=true;@Override public synchronized int read(byte[] b,int off,int len){zero=!zero;return !zero?0:super.read(b,off,Math.min(len,2));}};
  assertArrayEquals(expected,StreamBytes.read(input));
 }
 @Test public void readFailureIsPropagatedInsteadOfReturningTruncatedCurriculum(){
  InputStream input=new InputStream(){int n;public int read()throws IOException{if(n++==4)throw new IOException("read failed");return 1;}@Override public int read(byte[] b,int off,int len)throws IOException{b[off]=(byte)read();return 1;}};
  try{StreamBytes.read(input);fail("Truncated resource accepted");}catch(IOException expected){assertEquals("read failed",expected.getMessage());}
 }
}
