package com.gomgomapps.math.core;
import java.util.Set;import java.util.regex.*;
/** Public coefficients only; no supplied intermediates or answer transfer. */
public final class AlgebraRelations {
 private AlgebraRelations(){}
 public static boolean supports(String id){return Set.of("sec_identity_coefficient","sec_inverse_function").contains(id);}
 public static void attach(Question q){
  if(q==null||!supports(q.skillId)||q.prompt==null)return;
  String n="[+-]?\\d+(?:\\.\\d+)?(?:/\\d+)?",s="[+-]\\d+(?:\\.\\d+)?(?:/\\d+)?";
  StudyGuide g=new StudyGuide().transfer(false);
  if(q.skillId.equals("sec_identity_coefficient")){
   Matcher m=Pattern.compile("("+n+")x("+s+") \\+ \\(("+n+")x\\) ≡ kx("+s+")일 때 k는\\?").matcher(q.prompt);
   if(!m.matches()||!Expression.number(m.group(2)).equals(Expression.number(m.group(4))))return;
   Rational a=Expression.number(m.group(1)),b=Expression.number(m.group(3));g.teachingVersion="identity-coefficient-relations-v1";
   g.step("왼쪽 첫 번째 x항의 계수를 부호까지 쓰세요.","첫 번째 x항 계수 = ","",a.toString())
    .step("괄호 안 x항의 계수를 부호까지 쓰세요.","두 번째 x항 계수 = ","",b.toString())
    .step("항등식은 모든 x에서 성립합니다. 같은 차수의 계수를 비교하여 두 x항의 계수를 더하세요.","첫 번째 계수 + 두 번째 계수 = ","",a.add(b).toString());
  }else{
   Matcher m=Pattern.compile("f\\(x\\)=("+n+")x("+s+")일 때 f⁻¹\\(("+n+")\\)의 값은\\?").matcher(q.prompt);if(!m.matches())return;
   Rational a=Expression.number(m.group(1)),b=Expression.number(m.group(2)),y=Expression.number(m.group(3));if(a.equals(Rational.ZERO))return;
   g.teachingVersion="inverse-function-relations-v1";
   g.step("f⁻¹(y)는 f(x)=y를 만족하는 x입니다. 문제에서 y를 읽어 쓰세요.","함숫값 y = ","",y.toString())
    .step("f(x)=ax+b에서 상수항 b를 부호까지 쓰세요.","상수항 b = ","",b.toString())
    .step("f(x)=ax+b에서 x의 계수 a를 부호까지 쓰세요.","x의 계수 a = ","",a.toString())
    .step("y=ax+b의 양변에서 b를 빼세요.","y − b = ","",y.sub(b).toString())
    .step("a는 0이 아닙니다. y−b를 a로 나누어 원래 입력 x를 구하세요.","(y − b) ÷ a = ","",y.sub(b).div(a).toString());
  }
  q.studyGuide=g;
 }
}
