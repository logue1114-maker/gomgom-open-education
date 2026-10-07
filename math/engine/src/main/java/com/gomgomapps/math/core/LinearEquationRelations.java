package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
/** Balance-preserving steps from the public equation, with no numeric prefill. */
public final class LinearEquationRelations {
 private LinearEquationRelations(){}
 public static boolean supports(String id){return Set.of("linear","linearFraction").contains(id);}
 public static void attach(Question q){
  if(q==null||!supports(q.skillId)||q.prompt==null)return;boolean fraction=q.skillId.equals("linearFraction");
  Matcher m=Pattern.compile((fraction?"x/(\\d+)":"(-?\\d+)x")+" \\+ \\((-?\\d+)\\) = (-?\\d+)(?:\\nx의 값은\\?)?").matcher(q.prompt.replace('−','-'));if(!m.matches())return;
  long a=Long.parseLong(m.group(1)),b=Long.parseLong(m.group(2)),c=Long.parseLong(m.group(3));if(a==0||fraction&&a<0)return;
  Rational right=Rational.of(c).sub(Rational.of(b)),answer=fraction?right.mul(Rational.of(a)):right.div(Rational.of(a));
  StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="linear-equation-relations-v1";
  step(g,fraction?"x를 나누는 분모를 찾아 쓰세요.":"x항의 계수를 부호와 함께 쓰세요.",fraction?"분모 d = ":"계수 a = ",Rational.of(a));
  step(g,"왼쪽 상수항을 부호와 함께 쓰세요.","상수항 b = ",Rational.of(b));
  step(g,"등호 오른쪽의 수를 부호와 함께 쓰세요.","우변 c = ",Rational.of(c));
  step(g,"상수항을 없애려면 양변에 상수항의 반대수를 더합니다. 그 수를 구하세요.","양변에 더할 수 t = −b = ",Rational.of(b).neg());
  step(g,"양변에 같은 수를 더하세요. 왼쪽 상수항이 없어지고 오른쪽은 다음과 같습니다.","새 우변 u = c + t = ",right);
  step(g,fraction?"양변에 분모를 곱하면 왼쪽은 x만 남습니다.":"양변을 x항의 계수로 나누면 왼쪽은 x만 남습니다.",fraction?"x/d = u ⇒ x = u × d = ":"ax = u ⇒ x = u ÷ a = ",answer);
  q.studyGuide=g;
 }
 private static void step(StudyGuide g,String text,String before,Rational n){g.step(text,before,"",n.toString());}
}
