package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;import java.math.BigInteger;
/** Public equations, endpoint inclusion and real-square semantics. */
public final class IntervalRelations {
 private IntervalRelations(){}
 public static boolean supports(String id){return Set.of("sec_quadratic_line_intersections","sec_linear_inequality_system","sec_absolute_linear_inequality").contains(id);}
 public static void attach(Question q){
  if(q==null||!supports(q.skillId)||q.prompt==null)return;
  String n="[+-]?\\d+(?:\\.\\d+)?(?:/\\d+)?";Matcher m;StudyGuide g=new StudyGuide().transfer(false);
  if(q.skillId.equals("sec_quadratic_line_intersections")){
   m=Pattern.compile("이차함수 y=(?:x²|("+n+")\\(x-\\(("+n+")\\)\\)²\\+\\(("+n+")\\))과 직선 y=("+n+")의 교점은 몇 개인가요\\?").matcher(q.prompt);if(!m.matches())return;
   Rational a=m.group(1)==null?Rational.ONE:Expression.number(m.group(1)),b=m.group(3)==null?Rational.ZERO:Expression.number(m.group(3)),k=Expression.number(m.group(4));if(a.equals(Rational.ZERO))return;Rational difference=k.sub(b),square=difference.div(a);int count=square.compareTo(Rational.ZERO)>0?2:square.equals(Rational.ZERO)?1:0;g.teachingVersion="quadratic-intersection-relations-v1";
   g.step("제곱항의 계수 a를 쓰세요. 계수가 없으면 1입니다.","제곱항 계수 a = ","",a.toString())
    .step("이차함수의 상수항 b를 쓰세요. 상수항이 없으면 0입니다.","이차함수 상수항 b = ","",b.toString())
    .step("직선 y=k의 k를 쓰세요.","직선의 y값 k = ","",k.toString())
    .step("두 식의 y를 같게 놓고 b를 빼세요.","k − b = ","",difference.toString())
    .step("a는 0이 아닙니다. 상수항의 차를 a로 나누세요.","(x − h)² = (k − b) ÷ a = ","",square.toString())
    .step("제곱값이 음수이면 실수해가 없습니다. 0이면 x−h는 한 값, 양수이면 반대 부호의 두 값입니다. 교점 수를 쓰세요.","교점 수 = ","",String.valueOf(count));
  }else if(q.skillId.equals("sec_linear_inequality_system")){
   m=Pattern.compile("x-\\(("+n+")\\)>("+n+")이고 x-\\(("+n+")\\)≤("+n+")를 모두 만족하는 정수 x는 몇 개인가요\\?").matcher(q.prompt);if(!m.matches()||!Expression.number(m.group(1)).equals(Expression.number(m.group(3))))return;
   Rational h=Expression.number(m.group(1)),a=Expression.number(m.group(2)),b=Expression.number(m.group(4));if(a.compareTo(b)>=0)return;Rational low=a.add(h),high=b.add(h);BigInteger first=floor(low).add(BigInteger.ONE),last=floor(high),count=last.subtract(first).add(BigInteger.ONE).max(BigInteger.ZERO);g.teachingVersion="linear-inequality-system-relations-v1";
   g.step("두 식에서 빼는 h를 부호까지 쓰세요.","이동값 h = ","",h.toString())
    .step("첫 부등식 오른쪽의 a를 쓰세요.","아래 경계의 a = ","",a.toString())
    .step("둘째 부등식 오른쪽의 b를 쓰세요.","위 경계의 b = ","",b.toString())
    .step("첫 부등식의 양변에 h를 더하세요.","x가 넘어야 하는 값 = a + h = ","",low.toString())
    .step("둘째 부등식의 양변에 h를 더하세요.","x의 상한 = b + h = ","",high.toString())
    .step(">는 경계를 포함하지 않습니다. a+h보다 큰 첫 정수를 쓰세요.","첫 정수 = ","",first.toString())
    .step("≤는 경계를 포함합니다. b+h 이하인 마지막 정수를 쓰세요.","마지막 정수 = ","",last.toString())
    .step("두 부등식을 모두 만족하는 정수의 개수를 세세요.","마지막 정수 − 첫 정수 + 1 = ","",count.toString());
  }else{
   m=Pattern.compile("\\|x-\\(("+n+")\\)\\|≤("+n+")를 만족하는 정수 x는 몇 개인가요\\?").matcher(q.prompt);if(!m.matches())return;
   Rational h=Expression.number(m.group(1)),a=Expression.number(m.group(2));if(a.compareTo(Rational.ZERO)<0)return;Rational low=h.sub(a),high=h.add(a);BigInteger first=ceil(low),last=floor(high),count=last.subtract(first).add(BigInteger.ONE).max(BigInteger.ZERO);g.teachingVersion="absolute-linear-inequality-relations-v1";
   g.step("절댓값 안 x−h의 h를 쓰세요.","중심 h = ","",h.toString())
    .step("중심에서 허용되는 거리 a를 쓰세요.","거리 a = ","",a.toString())
    .step("중심에서 거리를 빼 아래 경계를 구하세요.","아래 경계 = h − a = ","",low.toString())
    .step("중심에 거리를 더해 위 경계를 구하세요.","위 경계 = h + a = ","",high.toString())
    .step("≤는 양끝을 포함합니다. h−a 이상인 첫 정수를 쓰세요.","첫 정수 = ","",first.toString())
    .step("h+a 이하인 마지막 정수를 쓰세요.","마지막 정수 = ","",last.toString())
    .step("양끝을 포함하여 정수의 개수를 세세요.","마지막 정수 − 첫 정수 + 1 = ","",count.toString());
  }q.studyGuide=g;
 }
 private static BigInteger floor(Rational v){BigInteger[] q=v.n.divideAndRemainder(v.d);return v.n.signum()<0&&q[1].signum()!=0?q[0].subtract(BigInteger.ONE):q[0];}
 private static BigInteger ceil(Rational v){return floor(v.mul(Rational.of(-1))).negate();}
}
