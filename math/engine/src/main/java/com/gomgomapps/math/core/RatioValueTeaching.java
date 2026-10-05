package com.gomgomapps.math.core;
import java.util.Set;
import java.util.regex.*;
/** Ratios and substitution stages built from public givens, never an answer key. */
final class RatioValueTeaching {
 private RatioValueTeaching(){}
 static void attach(Question q){
  if(q.studyGuide!=null)return;
  if(q.skillId.equals("percent")){
   Matcher m=Pattern.compile("(\\d+)\\*(\\d+)/100").matcher(q.expression);if(!m.matches())return;int base=Integer.parseInt(m.group(1)),percent=Integer.parseInt(m.group(2));Rational rate=Rational.of(percent,100);
   q.studyGuide=new StudyGuide().transfer(false)
    .step("백분율을 100으로 나누어 소수로 나타내세요.",percent+" ÷ 100 = ","",rate.decimalText())
    .step("전체 수에 계산한 비율을 곱하세요.",base+" × "+rate.decimalText()+" = ","",Rational.of(base).mul(rate).decimalText());return;
  }
  if(q.skillId.equals("proportion")){
   Matcher m=Pattern.compile("(\\d+) : (\\d+) = (\\d+) : x").matcher(q.prompt);if(!m.find())return;int a=Integer.parseInt(m.group(1)),b=Integer.parseInt(m.group(2)),c=Integer.parseInt(m.group(3));if(a==0)return;Rational factor=Rational.of(c,a);
   q.studyGuide=new StudyGuide().transfer(false)
    .step("첫 번째 항이 몇 배가 되었는지 구하세요.",c+" ÷ "+a+" = ","",factor.toString())
    .step("두 번째 항에도 같은 배수를 곱하세요.",b+" × "+factor+" = ","",Rational.of(b).mul(factor).toString());return;
  }
  if(!Set.of("substitute","function","linearValue").contains(q.skillId))return;
  Matcher m=Pattern.compile("(\\(?-?\\d+(?:/\\d+)?\\)?)\\s*[*×]\\s*(\\(?-?\\d+\\)?)\\s*\\+\\s*(\\(?-?\\d+\\)?)").matcher(q.expression);if(!m.matches())return;
  Rational coefficient=Expression.number(m.group(1)),x=Expression.number(m.group(2)),constant=Expression.number(m.group(3)),product=coefficient.mul(x);
  q.studyGuide=new StudyGuide().transfer(false)
   .step("문자 x 자리에 문제에서 주어진 수를 쓰세요.","x = ","",x.toString())
   .step("계수와 대입한 수를 먼저 곱하세요.","("+coefficient+") × ("+x+") = ","",product.toString())
   .step("계산한 곱에 상수항을 더하세요.","("+product+") + ("+constant+") = ","",product.add(constant).toString());
 }
}
