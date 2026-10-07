package com.gomgomapps.math.core;
import java.math.BigDecimal;import java.util.*;import java.util.regex.*;
/** Blank place-value relationships derived from the original visible decimal operands. */
public final class DecimalArithmeticRelations {
 private DecimalArithmeticRelations(){}
 public static boolean supports(String id){return Set.of("decimalAdd","decimalSub","decimalMul","decimalDiv","decimalDivInt").contains(id);}
 public static void attach(Question q){
  if(q==null||q.prompt==null||!supports(q.skillId))return;
  String raw=q.prompt.replace('×','*').replace('÷','/').replace('−','-');Matcher m=Pattern.compile("(\\d+(?:\\.\\d+)?) ([+*/-]) (\\d+(?:\\.\\d+)?)").matcher(raw);if(!m.matches())return;
  String op=m.group(2),wanted=switch(q.skillId){case "decimalAdd"->"+";case "decimalSub"->"-";case "decimalMul"->"*";default->"/";};if(!op.equals(wanted))return;
  BigDecimal x=new BigDecimal(m.group(1)),y=new BigDecimal(m.group(3));int px=x.scale(),py=y.scale();if(op.equals("/")&&y.signum()==0)return;
  StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="decimal-arithmetic-relations-v1";
  step(g,"왼쪽 수를 쓰세요.","왼쪽 수 x = ",x,true);step(g,"오른쪽 수를 쓰세요.","오른쪽 수 y = ",y,true);
  if(op.equals("*")){
   if(py==0){BigDecimal a=x.movePointRight(px),r=a.multiply(y);step(g,"왼쪽 수의 소수점 아래 자릿수를 쓰세요.","소수 자릿수 p = ",BigDecimal.valueOf(px),false);step(g,"왼쪽 수의 소수점을 오른쪽으로 p칸 옮기세요.","왼쪽 정수 A = x의 소수점 p칸 이동 = ",a,false);step(g,"두 정수를 곱하세요.","정수 곱 r = A × y = ",r,false);step(g,"정수 곱의 소수점을 왼쪽으로 p칸 옮기세요.","소수 곱 v = r의 소수점 p칸 이동 = ",r.movePointLeft(px),true);q.studyGuide=g;return;}
   step(g,"왼쪽 수의 소수점 아래 자릿수를 쓰세요.","왼쪽 소수 자릿수 p = ",BigDecimal.valueOf(px),false);step(g,"오른쪽 수의 소수점 아래 자릿수를 쓰세요.","오른쪽 소수 자릿수 q = ",BigDecimal.valueOf(py),false);
   BigDecimal a=x.movePointRight(px),b=y.movePointRight(py),r=a.multiply(b);
   step(g,"왼쪽 수의 소수점을 오른쪽으로 p칸 옮기세요.","왼쪽 정수 A = x의 소수점 p칸 이동 = ",a,false);step(g,"오른쪽 수의 소수점을 오른쪽으로 q칸 옮기세요.","오른쪽 정수 B = y의 소수점 q칸 이동 = ",b,false);
   step(g,"두 정수를 곱하세요.","정수 곱 r = A × B = ",r,false);step(g,"두 소수 자릿수를 더하세요.","돌려놓을 자릿수 k = p + q = ",BigDecimal.valueOf(px+py),false);step(g,"정수 곱의 소수점을 왼쪽으로 k칸 옮기세요.","소수 곱 v = r의 소수점 k칸 이동 = ",r.movePointLeft(px+py),true);
  }else{
   boolean div=op.equals("/"),wholeDiv=q.skillId.equals("decimalDivInt");if(wholeDiv&&y.stripTrailingZeros().scale()>0)return;
   int places=div?(wholeDiv?px:py):Math.max(px,py);BigDecimal u=BigDecimal.TEN.pow(places),a=x.multiply(u),b=y.multiply(u);
   step(g,div?(wholeDiv?"왼쪽 수의 소수점 아래 자릿수를 쓰세요.":"나누는 수의 소수점 아래 자릿수를 쓰세요."):"두 수의 소수 자릿수 중 더 긴 자릿수를 쓰세요.","옮길 자릿수 p = ",BigDecimal.valueOf(places),false);
   step(g,"한 칸이면 10배, 두 칸이면 100배입니다. 옮길 자릿수에 맞는 배수를 쓰세요. 0칸이면 배수는 1입니다.","자릿값 배수 u = p칸 이동에 맞는 배수 = ",u,false);
   step(g,"왼쪽 수의 소수점을 오른쪽으로 p칸 옮기세요.","옮긴 왼쪽 수 A = x × u = ",a,true);
   if(wholeDiv){BigDecimal r;try{r=a.divide(y);}catch(ArithmeticException e){return;}step(g,"옮긴 왼쪽 수를 자연수로 나누세요.","먼저 구한 몫 r = A ÷ y = ",r,true);step(g,"몫의 소수점을 왼쪽으로 p칸 되돌리세요.","소수 몫 v = r ÷ u = ",r.divide(u),true);}
   else{
    step(g,"오른쪽 수의 소수점도 오른쪽으로 p칸 옮기세요.","옮긴 오른쪽 수 B = y × u = ",b,true);
    if(div){BigDecimal v;try{v=a.divide(b);}catch(ArithmeticException e){return;}step(g,"두 수의 소수점을 같은 칸만큼 옮겨도 몫은 같습니다. 나누세요.","소수 몫 v = A ÷ B = ",v,true);}
    else{BigDecimal r=op.equals("+")?a.add(b):a.subtract(b);step(g,op.equals("+")?"같은 자리끼리 더하세요.":"같은 자리끼리 빼세요.",op.equals("+")?"정수 합 r = A + B = ":"정수 차 r = A − B = ",r,false);step(g,"계산한 값의 소수점을 왼쪽으로 p칸 되돌리세요.","소수 결과 v = r ÷ u = ",r.divide(u),true);}
   }
  }
  q.studyGuide=g;
 }
 private static void step(StudyGuide g,String text,String before,BigDecimal expected,boolean decimal){g.step(text,before,"",expected.stripTrailingZeros().toPlainString());if(decimal)g.frames.get(g.frames.size()-1).inputFormat="decimalValue";}
}
