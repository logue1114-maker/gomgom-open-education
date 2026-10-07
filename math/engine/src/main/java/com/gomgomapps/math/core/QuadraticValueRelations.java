package com.gomgomapps.math.core;
import java.util.regex.*;
/** Evaluate a quadratic using public coefficients and learner-entered intermediate values. */
public final class QuadraticValueRelations {
 private QuadraticValueRelations(){}
 public static boolean supports(String id){return "sec_quadratic_value".equals(id);}
 public static void attach(Question q){
  if(q==null||!supports(q.skillId)||q.prompt==null)return;
  String num="[+-]?\\d+(?:\\.\\d+)?(?:/\\d+)?";Matcher m=Pattern.compile("f\\(x\\)=("+num+")\\(x-\\(("+num+")\\)\\)²([+-]\\d+(?:\\.\\d+)?(?:/\\d+)?)일 때 f\\(("+num+")\\)의 값은\\?").matcher(q.prompt);if(!m.matches())return;
  Rational a=Expression.number(m.group(1)),h=Expression.number(m.group(2)),k=Expression.number(m.group(3)),x=Expression.number(m.group(4));if(a.equals(Rational.ZERO))return;Rational shifted=x.sub(h),square=shifted.mul(shifted),product=a.mul(square);
  StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="quadratic-value-relations-v1";
  g.step("문제에서 대입할 x의 값을 찾아 쓰세요.","대입할 x = ","",x.toString())
   .step("f(x)=a(x−h)²+k에서 a에 해당하는 수를 쓰세요.","계수 a = ","",a.toString())
   .step("f(x)=a(x−h)²+k에서 h에 해당하는 수를 쓰세요.","기준값 h = ","",h.toString())
   .step("f(x)=a(x−h)²+k에서 k에 해당하는 수를 쓰세요.","상수 k = ","",k.toString())
   .step("대입할 x에서 h를 빼세요. h가 음수이면 음수를 빼는 계산입니다.","x − h = ","",shifted.toString())
   .step("x−h의 계산 결과를 제곱하세요.","(x−h) × (x−h) = ","",square.toString())
   .step("제곱한 값에 계수 a를 곱하세요.","a × (x−h)² = ","",product.toString())
   .step("계수까지 곱한 값에 상수 k를 더하세요.","a × (x−h)² + k = ","",product.add(k).toString());q.studyGuide=g;
 }
}
