package com.gomgomapps.math.core;
import java.util.regex.*;
/** Recover the quotient constant from public polynomial and divisor coefficients. */
public final class PolyDivisionRelations {
 private PolyDivisionRelations(){}
 public static boolean supports(String id){return "sec_poly_division".equals(id);}
 public static void attach(Question q){
  if(q==null||!supports(q.skillId)||q.prompt==null)return;
  String signed="[+-]\\d+(?:\\.\\d+)?(?:/\\d+)?",num="[+-]?\\d+(?:\\.\\d+)?(?:/\\d+)?";
  Matcher m=Pattern.compile("P\\(x\\)=x²("+signed+")x("+signed+")를 x-\\(("+num+")\\)로 나누면 몫은 x\\+b, 나머지는 ("+num+")입니다. b는\\?").matcher(q.prompt);if(!m.matches())return;
  Rational coefficient=Expression.number(m.group(1)),a=Expression.number(m.group(3));
  StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="poly-division-relations-v1";
  g.step("P(x)에서 x항의 계수를 부호까지 읽어 쓰세요.","x항 계수 = ","",coefficient.toString())
   .step("나누는 식 x−a에서 a를 부호까지 읽어 쓰세요.","나누는 식의 a = ","",a.toString())
   .step("P(x)=(x−a)(x+b)+r입니다. x항 계수는 b−a이므로 x항 계수에 a를 더하세요.","x항 계수 + a = ","",coefficient.add(a).toString());
  q.studyGuide=g;
 }
}
