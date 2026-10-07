package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;import java.math.BigInteger;
public final class QuadraticRangeRelations {
 private QuadraticRangeRelations(){}
 public static boolean supports(String id){return Set.of("sec_simultaneous_quadratic","sec_quadratic_inequality","sec_quadratic_extremum").contains(id);}
 private static Rational sqrt(Rational v){if(v.n.signum()<0)return null;BigInteger n=v.n.sqrt(),d=v.d.sqrt();return n.multiply(n).equals(v.n)&&d.multiply(d).equals(v.d)?Expression.number(n+"/"+d):null;}
 public static void attach(Question q){
  if(q==null||!supports(q.skillId)||q.prompt==null)return;
  String n="[+-]?\\d+(?:\\.\\d+)?(?:/\\d+)?";StudyGuide g=new StudyGuide().transfer(false);Matcher m;
  if(q.skillId.equals("sec_simultaneous_quadratic")){
   m=Pattern.compile("y=x²(?:\\+\\(("+n+")\\))?, y=("+n+")를 동시에 만족하고 x>0일 때 x는\\?").matcher(q.prompt);if(!m.matches())return;
   Rational b=m.group(1)==null?Rational.ZERO:Expression.number(m.group(1)),y=Expression.number(m.group(2)),square=y.sub(b),x=sqrt(square);if(x==null||x.compareTo(Rational.ZERO)<=0)return;g.teachingVersion="simultaneous-quadratic-relations-v1";
   g.step("첫 번째 식의 상수항 b를 쓰세요. 상수항이 없으면 0입니다.","상수항 b = ","",b.toString())
    .step("두 식이 함께 만족해야 하는 y값을 쓰세요.","y = ","",y.toString())
    .step("두 식의 y를 같게 놓고 상수항을 빼세요.","x² = y − b = ","",square.toString())
    .step("x>0 조건에 따라 양의 제곱근을 구하세요.","x = √(y − b) = ","",x.toString());
  }else if(q.skillId.equals("sec_quadratic_inequality")){
   m=Pattern.compile("\\(x-\\(("+n+")\\)\\)²(?:\\+\\(("+n+")\\))?≤("+n+")를 만족하는 정수 x는 모두 몇 개인가요\\?").matcher(q.prompt);if(!m.matches())return;
   Rational h=Expression.number(m.group(1)),b=m.group(2)==null?Rational.ZERO:Expression.number(m.group(2)),y=Expression.number(m.group(3)),square=y.sub(b),a=sqrt(square);if(a==null)return;
   Rational low=h.sub(a),high=h.add(a);BigInteger first=ceil(low),last=floor(high),count=last.subtract(first).add(BigInteger.ONE).max(BigInteger.ZERO);g.teachingVersion="quadratic-inequality-relations-v1";
   g.step("괄호 안 x−h의 h를 쓰세요.","중심 h = ","",h.toString())
    .step("왼쪽의 상수항 b를 쓰세요. 상수항이 없으면 0입니다.","상수항 b = ","",b.toString())
    .step("부등호 오른쪽의 값 y를 쓰세요.","오른쪽 값 y = ","",y.toString())
    .step("양변에서 상수항을 빼세요.","제곱의 상한 = y − b = ","",square.toString())
    .step("중심에서 양쪽으로 갈 수 있는 거리를 구하세요.","거리 a = √(y − b) = ","",a.toString())
    .step("≤는 양끝을 포함합니다. h−a 이상인 첫 정수를 쓰세요.","첫 정수 = ","",first.toString())
    .step("h+a 이하인 마지막 정수를 쓰세요.","마지막 정수 = ","",last.toString())
    .step("양끝을 포함하여 정수의 개수를 세세요.","마지막 정수 − 첫 정수 + 1 = ","",count.toString());
  }else{
   m=Pattern.compile("("+n+")≤x≤("+n+")에서 f\\(x\\)=("+n+")\\(x-\\(("+n+")\\)\\)²\\+\\(("+n+")\\)의 최솟값은\\?").matcher(q.prompt);if(!m.matches())return;
   Rational low=Expression.number(m.group(1)),high=Expression.number(m.group(2)),a=Expression.number(m.group(3)),h=Expression.number(m.group(4)),k=Expression.number(m.group(5));if(a.compareTo(Rational.ZERO)<=0||low.compareTo(high)>0)return;
   Rational x=h.compareTo(low)<0?low:h.compareTo(high)>0?high:h,difference=x.sub(h),square=difference.mul(difference);g.teachingVersion="quadratic-extremum-relations-v1";
   g.step("f(x)=a(x−h)²+k에서 h를 쓰세요.","꼭짓점의 x값 h = ","",h.toString())
    .step("제곱항의 계수 a를 쓰세요.","제곱항 계수 a = ","",a.toString())
    .step("상수항 k를 부호까지 쓰세요.","상수항 k = ","",k.toString())
    .step("a>0입니다. h가 구간 안이면 h를, 밖이면 h와 가까운 구간 끝값을 쓰세요.","최솟값이 되는 x = ","",x.toString())
    .step("선택한 x에서 h를 빼세요.","x − h = ","",difference.toString())
    .step("차를 제곱하세요.","(x − h)² = ","",square.toString())
    .step("제곱에 a를 곱하고 k를 더하세요.","a × (x − h)² + k = ","",a.mul(square).add(k).toString());
  }q.studyGuide=g;
 }
 private static BigInteger floor(Rational v){BigInteger[] q=v.n.divideAndRemainder(v.d);return v.n.signum()<0&&q[1].signum()!=0?q[0].subtract(BigInteger.ONE):q[0];}
 private static BigInteger ceil(Rational v){return floor(v.mul(Rational.of(-1))).negate();}
}
