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
   Matcher m=Pattern.compile("(\\d+)/(\\d+)의 값을 소수로 나타내세요\\.").matcher(q.prompt);if(!m.matches())return;
   long n=Long.parseLong(m.group(1)),d=Long.parseLong(m.group(2));if(d<=0)return;
   long t=d;while(t>1&&t%10==0)t/=10;if(t!=1)return;
   step(g,"문제의 분자를 쓰세요.","분자 n = ",Long.toString(n));step(g,"문제의 분모를 쓰세요.","분모 d = ",Long.toString(d));
   if((d==10||d==100)&&n>=0&&n<=d)q.diagram=new StudyDiagram("fraction",new double[]{n,d});
   step(g,"전체를 1로 보세요. 전체를 같은 크기로 나눈 한 부분의 크기를 소수로 쓰세요.","한 부분 u = 1 ÷ d = ",BigDecimal.ONE.divide(BigDecimal.valueOf(d)).stripTrailingZeros().toPlainString());
   g.frames.get(2).inputFormat="decimal";
   step(g,"한 부분의 크기에 분자의 부분 수를 곱해 소수로 쓰세요.","소수 v = u × n = ",BigDecimal.valueOf(n).divide(BigDecimal.valueOf(d)).stripTrailingZeros().toPlainString());
   g.frames.get(3).inputFormat="decimal";
  }else{
   Matcher m=Pattern.compile("(\\d+\\.\\d+)의 값을 분수로 나타내세요\\.").matcher(q.prompt);if(!m.matches())return;
   String raw=m.group(1);BigDecimal v=new BigDecimal(raw);int places=raw.length()-raw.indexOf('.')-1;if(places>9)return;
   long d=1;for(int i=0;i<places;i++)d*=10;java.math.BigInteger numerator=v.multiply(BigDecimal.valueOf(d)).toBigIntegerExact();if(numerator.bitLength()>63)throw new ArithmeticException("BigInteger out of long range");long n=numerator.longValue();
   step(g,"문제의 소수를 쓰세요.","소수 v = ",raw);g.frames.get(0).inputFormat="decimal";
   step(g,"소수점 아래 자릿수를 세어 쓰세요.","소수점 아래 자릿수 p = ",Integer.toString(places));
   step(g,"첫째 자리의 분모는 10입니다. 자릿수가 하나 늘 때마다 분모가 10배가 됩니다.","분모 d = 자릿수 p에 맞는 분모 = ",Long.toString(d));
   step(g,"소수에 분모를 곱해 분자를 구하세요.","분자 n = v × d = ",Long.toString(n));g.fractionResult(3,2);
  }
  q.studyGuide=g;
 }
 private static void step(StudyGuide g,String text,String before,String expected){g.step(text,before,"",expected);}
}
