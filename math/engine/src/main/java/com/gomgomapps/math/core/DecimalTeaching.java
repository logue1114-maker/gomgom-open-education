package com.gomgomapps.math.core;
import java.math.BigDecimal;
import java.util.Set;
import java.util.regex.*;
/** Place-value stages derived only from visible operands. */
final class DecimalTeaching {
 private DecimalTeaching(){}
 static void attach(Question q){
  if(q.studyGuide!=null||!Set.of("decimalAdd","decimalSub","decimalMul","decimalDiv","decimalDivInt").contains(q.skillId))return;
  Matcher m=Pattern.compile("(\\d+(?:\\.\\d+)?) ([+*/-]) (\\d+(?:\\.\\d+)?)").matcher(q.expression);if(!m.matches())return;
  BigDecimal x=new BigDecimal(m.group(1)),y=new BigDecimal(m.group(3));String op=m.group(2);int px=x.scale(),py=y.scale();StudyGuide g=new StudyGuide().transfer(false);
  if(op.equals("*")){
   BigDecimal a=x.movePointRight(px),b=y.movePointRight(py),v=a.multiply(b);int places=px+py;
   g.step("첫 번째 수의 소수점을 뺀 수를 쓰세요.",t(x)+" → ","",t(a))
    .step("두 번째 수의 소수점을 뺀 수를 쓰세요.",t(y)+" → ","",t(b))
    .step("두 수의 소수 자릿수를 더하세요.",px+" + "+py+" = ","",String.valueOf(places))
    .step("소수점을 뺀 두 수를 곱하세요.",t(a)+" × "+t(b)+" = ","",t(v))
    .step("합한 소수 자릿수만큼 소수점을 왼쪽으로 옮기세요.",t(v)+" ÷ "+t(BigDecimal.TEN.pow(places))+" = ","",t(v.movePointLeft(places)));
  }else{
   int places=Math.max(px,py);BigDecimal unit=BigDecimal.TEN.pow(places),a=x.multiply(unit),b=y.multiply(unit);
   g.step("두 수를 모두 정수로 만드는 가장 작은 10의 거듭제곱을 쓰세요.","× ","",t(unit))
    .step("첫 번째 수에 같은 수를 곱하세요.",t(x)+" × "+t(unit)+" = ","",t(a))
    .step("두 번째 수에도 같은 수를 곱하세요.",t(y)+" × "+t(unit)+" = ","",t(b));
   if(op.equals("/")){
    if(b.signum()==0)return;BigDecimal v;try{v=a.divide(b);}catch(ArithmeticException e){return;}
    g.step("두 수에 같은 수를 곱해도 몫은 같아요. 나누세요.",t(a)+" ÷ "+t(b)+" = ","",t(v));
   }else{
    BigDecimal v=op.equals("+")?a.add(b):a.subtract(b);
    g.step(op.equals("+")?"같은 자리끼리 더하세요.":"같은 자리끼리 빼세요.",t(a)+(op.equals("+")?" + ":" − ")+t(b)+" = ","",t(v))
     .step("처음에 곱한 수로 나누어 소수 자릿값을 되돌리세요.",t(v)+" ÷ "+t(unit)+" = ","",t(v.divide(unit)));
   }
  }q.studyGuide=g;
 }
 private static String t(BigDecimal v){return v.stripTrailingZeros().toPlainString();}
}
