package com.gomgomapps.math.core;
import java.util.regex.*;
/** Public mixed-number notation; never parsed as concatenation or multiplication. */
final class MixedFractionValue {
 static final String PATTERN="(?:\\d{1,3} )?\\d{1,5}/\\d{1,3}";
 record Parts(int whole,int numerator,int denominator){int improper(){return whole*denominator+numerator;}Rational value(){return Rational.of(improper(),denominator);}}
 static Parts read(String raw){
  Matcher m=Pattern.compile("^(?:(\\d{1,3}) )?(\\d{1,5})/(\\d{1,3})$").matcher(raw);if(!m.matches())throw new IllegalArgumentException("fraction notation");
  int w=m.group(1)==null?0:Integer.parseInt(m.group(1)),n=Integer.parseInt(m.group(2)),d=Integer.parseInt(m.group(3));
  if(w>100||d<1||d>100||n<1||n>10000||w>0&&n>=d)throw new IllegalArgumentException("fraction bounds");return new Parts(w,n,d);
 }
 static Rational value(String raw){return read(raw).value();}
}
