package com.gomgomapps.math.core;
import java.util.Set;
import java.util.regex.*;
/** Ratios and substitution stages built from public givens, never an answer key. */
public final class RatioValueTeaching {
 private RatioValueTeaching(){}
 public static boolean supports(String id){return Set.of("percent","proportion","substitute","function","linearValue").contains(id);}
 private static StudyGuide guide(){StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="ratio-value-relations-v1";return g;}
 private static void given(StudyGuide g,String label,String expected){g.step("문제에서 "+label+"를 찾아 쓰세요.",label+" = ","",expected);}
 static void attach(Question q){
  if(q==null||!supports(q.skillId))return;
  if(q.skillId.equals("percent")){
   Matcher m=Pattern.compile("(\\d+)\\*(\\d+)/100").matcher(q.expression);if(!m.matches())return;int base=Integer.parseInt(m.group(1)),percent=Integer.parseInt(m.group(2));Rational rate=Rational.of(percent,100);
   StudyGuide g=guide();given(g,"전체 수",Integer.toString(base));g.step("문제에서 백분율을 찾아 쓰세요.","백분율 = ","%",Integer.toString(percent));
   q.studyGuide=g.step("백분율을 100으로 나누어 소수로 나타내세요.","백분율 ÷ 100 = ","",rate.decimalText())
    .step("전체 수에 계산한 비율을 곱하세요.","전체 수 × 계산한 비율 = ","",Rational.of(base).mul(rate).decimalText());return;
  }
  if(q.skillId.equals("proportion")){
   Matcher m=Pattern.compile("(\\d+) : (\\d+) = (\\d+) : x").matcher(q.prompt);if(!m.find())return;int a=Integer.parseInt(m.group(1)),b=Integer.parseInt(m.group(2)),c=Integer.parseInt(m.group(3));if(a==0)return;Rational factor=Rational.of(c,a);
   StudyGuide g=guide();given(g,"왼쪽 첫 번째 수",Integer.toString(a));given(g,"왼쪽 두 번째 수",Integer.toString(b));given(g,"오른쪽 첫 번째 수",Integer.toString(c));
   q.studyGuide=g.step("첫 번째 수가 몇 배가 되었는지 구하세요.","오른쪽 첫 번째 수 ÷ 왼쪽 첫 번째 수 = ","",factor.toString())
    .step("왼쪽 두 번째 수에 같은 배수를 곱하세요.","왼쪽 두 번째 수 × 계산한 배수 = ","",Rational.of(b).mul(factor).toString());return;
  }
  if(!Set.of("substitute","function","linearValue").contains(q.skillId))return;
  Matcher m=Pattern.compile("(\\(?-?\\d+(?:/\\d+)?\\)?)\\s*[*×]\\s*(\\(?-?\\d+\\)?)\\s*\\+\\s*(\\(?-?\\d+\\)?)").matcher(q.expression);if(!m.matches())return;
  Rational coefficient=Expression.number(m.group(1)),x=Expression.number(m.group(2)),constant=Expression.number(m.group(3)),product=coefficient.mul(x);
  StudyGuide g=guide().step("문자 x 자리에 문제에서 주어진 수를 쓰세요.","x = ","",x.toString());
  g.step("식에서 x에 곱하는 수를 찾아 쓰세요.","x에 곱하는 수 = ","",coefficient.toString())
   .step("찾은 수와 x의 값을 곱하세요.","x에 곱하는 수 × x = ","",product.toString())
   .step("식에서 더하는 수를 부호와 함께 쓰세요.","더하는 수 = ","",constant.toString());
  q.studyGuide=g.step("계산한 곱에 더하는 수를 더하세요.","계산한 곱 + 더하는 수 = ","",product.add(constant).toString());
 }
}
