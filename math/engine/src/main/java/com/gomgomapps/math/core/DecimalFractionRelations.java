package com.gomgomapps.math.core;
import java.math.BigDecimal;import java.util.*;import java.util.regex.*;
/** Place-value frames read from the visible problem, never from answer metadata. */
public final class DecimalFractionRelations {
 private DecimalFractionRelations(){}
 public static boolean supports(String id){return Set.of("el_fraction_decimal","el_decimal_fraction").contains(id);}
 public static void attach(Question q){
  if(q==null||q.prompt==null||!supports(q.skillId))return;
  StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion=q.skillId.equals("el_fraction_decimal")?"decimal-fraction-unit-relations-v2":"decimal-fraction-relations-v1";
  if(q.skillId.equals("el_fraction_decimal")){
   StudyGuide visible=FractionDecimalDivision.guide(q);if(visible==null)return;
   FractionDecimalDivision.Givens v=FractionDecimalDivision.read(q);if(!v.rounded()&&(v.denominator().equals(java.math.BigInteger.TEN)||v.denominator().equals(java.math.BigInteger.valueOf(100)))&&v.numerator().compareTo(v.denominator())<=0)q.diagram=new StudyDiagram("fraction",new double[]{v.numerator().doubleValue(),v.denominator().doubleValue()});
   q.studyGuide=visible;return;
  }else{
   Matcher m=Pattern.compile("(\\d+\\.\\d+)의 값을 (?:기약)?분수로 나타내세요\\.").matcher(q.prompt);if(!m.matches())return;
   String raw=m.group(1);BigDecimal v=new BigDecimal(raw);int places=raw.length()-raw.indexOf('.')-1;if(places>9)return;
   long d=1;for(int i=0;i<places;i++)d*=10;java.math.BigInteger numerator=v.multiply(BigDecimal.valueOf(d)).toBigIntegerExact();if(numerator.bitLength()>63)throw new ArithmeticException("BigInteger out of long range");long n=numerator.longValue();
   step(g,"문제의 소수를 쓰세요.","소수 v = ",raw);g.frames.get(0).inputFormat="decimal";
   step(g,"소수점 아래 자릿수를 세어 쓰세요.","소수점 아래 자릿수 p = ",Integer.toString(places));
   step(g,"첫째 자리의 분모는 10입니다. 자릿수가 하나 늘 때마다 분모가 10배가 됩니다.","분모 d = 자릿수 p에 맞는 분모 = ",Long.toString(d));
   step(g,"소수에 분모를 곱해 분자를 구하세요.","분자 n = v × d = ",Long.toString(n));g.fractionResult(3,2);
   if(q.prompt.contains("기약분수")){long gcd=numerator.gcd(java.math.BigInteger.valueOf(d)).longValue();g.teachingVersion="decimal-reduced-fraction-v1";step(g,"분자와 분모의 최대공약수를 구하세요.","최대공약수 g = ",Long.toString(gcd));step(g,"분자를 최대공약수로 나누세요.","약분한 분자 N = n ÷ g = ",Long.toString(n/gcd));step(g,"분모를 같은 최대공약수로 나누세요.","약분한 분모 D = d ÷ g = ",Long.toString(d/gcd));g.fractionResult(5,6);}

  }
  q.studyGuide=g;
 }
 private static void step(StudyGuide g,String text,String before,String expected){g.step(text,before,"",expected);}
}
