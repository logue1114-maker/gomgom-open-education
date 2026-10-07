package com.gomgomapps.math.core;
import java.util.regex.*;
/** Public scalar functions supply givens, while learners enter every operation. */
public final class FunctionValueRelations {
 private FunctionValueRelations(){}
 public static void attach(Question q){
  if(q==null||q.prompt==null)return;boolean rational="sec_rational_function".equals(q.skillId);if(!rational&&!"sec_radical_function".equals(q.skillId))return;
  Matcher m=Pattern.compile(rational?"f\\(x\\)=(-?\\d+)/\\(x-\\((-?\\d+)\\)\\)([+-]\\d+)일 때 f\\((-?\\d+)\\)의 값은\\?":"f\\(x\\)=√\\(x-\\((-?\\d+)\\)\\)([+-]\\d+)일 때 f\\((-?\\d+)\\)의 값은\\?").matcher(q.prompt);if(!m.matches())return;
  long a=rational?Long.parseLong(m.group(1)):0,h=Long.parseLong(m.group(rational?2:1)),k=Long.parseLong(m.group(rational?3:2)),x=Long.parseLong(m.group(rational?4:3)),inside=x-h;
  if(rational&&inside==0||!rational&&inside<0)return;long root=rational?0:(long)Math.sqrt(inside);if(!rational&&root*root!=inside)return;
  StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion=rational?"rational-value-relations-v1":"radical-value-relations-v1";
  if(rational)step(g,"함수의 분자를 찾아 쓰세요.","분자 a = ",Rational.of(a));
  step(g,"x에서 빼는 이동값을 찾아 쓰세요. 괄호 안의 부호도 확인하세요.","이동값 h = ",Rational.of(h));
  step(g,"마지막에 더하는 상수를 부호와 함께 쓰세요.","상수 k = ",Rational.of(k));
  step(g,"구할 함수값의 입력값을 찾아 쓰세요.","입력값 v = ",Rational.of(x));
  step(g,"입력값에서 이동값을 빼세요.",rational?"분모 d = v − h = ":"근호 안 t = v − h = ",Rational.of(inside));
  step(g,rational?"분모는 0이 될 수 없습니다. 분자를 앞에서 구한 분모로 나누세요.":"앞에서 구한 값의 음수가 아닌 제곱근을 구하세요.",rational?"나눈 값 u = a ÷ d = ":"제곱근 u = √t = ",rational?Rational.of(a,inside):Rational.of(root));
  step(g,"앞에서 구한 값에 상수를 더하세요.","함수값 f(v) = u + k = ",(rational?Rational.of(a,inside):Rational.of(root)).add(Rational.of(k)));q.studyGuide=g;
 }
 private static void step(StudyGuide g,String text,String before,Rational value){g.step(text,before,"",value.toString());}
}
