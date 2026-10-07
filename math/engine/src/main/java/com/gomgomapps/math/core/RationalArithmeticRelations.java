package com.gomgomapps.math.core;
import java.util.regex.*;
/** Learners supply visible fractions, common denominators, products and reduction. */
public final class RationalArithmeticRelations {
 private RationalArithmeticRelations(){}
 public static boolean supports(String id){return "rational".equals(id);}
 public static void attach(Question q){
  if(q==null||!supports(q.skillId)||q.prompt==null)return;
  Matcher m=Pattern.compile("\\((-?\\d+)/(\\d+)\\)\\s*([+*/×÷-])\\s*\\((-?\\d+)/(\\d+)\\)").matcher(q.prompt.replace('−','-'));if(!m.matches())return;
  long a=Long.parseLong(m.group(1)),b=Long.parseLong(m.group(2)),c=Long.parseLong(m.group(4)),d=Long.parseLong(m.group(5));String op=m.group(3);
  boolean add=op.equals("+"),subtract=op.equals("-"),divide=op.equals("/")||op.equals("÷");if(b<=0||d<=0||divide&&c==0)return;
  StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="rational-arithmetic-relations-v1";
  step(g,"첫 번째 분수의 분자를 부호와 함께 쓰세요.","첫 분자 a = ",a);
  step(g,"첫 번째 분수의 분모를 쓰세요.","첫 분모 b = ",b);
  step(g,"두 번째 분수의 분자를 부호와 함께 쓰세요.","둘째 분자 c = ",c);
  step(g,"두 번째 분수의 분모를 쓰세요.","둘째 분모 d = ",d);
  long numerator,denominator;
  if(add||subtract){
   denominator=b/gcd(b,d)*d;long p=denominator/b,r=denominator/d,u=a*p,v=c*r;numerator=add?u+v:u-v;
   step(g,"두 분모의 최소공배수를 구하세요.","공통분모 L = lcm(b, d) = ",denominator);
   step(g,"첫 분모를 공통분모로 바꾸는 배수를 구하세요.","첫 배수 p = L ÷ b = ",p);
   step(g,"둘째 분모를 공통분모로 바꾸는 배수를 구하세요.","둘째 배수 q = L ÷ d = ",r);
   step(g,"첫 분자에도 첫 배수를 곱하세요.","통분한 첫 분자 u = a × p = ",u);
   step(g,"둘째 분자에도 둘째 배수를 곱하세요.","통분한 둘째 분자 v = c × q = ",v);
   step(g,add?"통분한 분자끼리 더하세요.":"통분한 분자끼리 빼세요.",add?"계산한 분자 N = u + v = ":"계산한 분자 N = u − v = ",numerator);
  }else{
   long top=divide?d:c,bottom=divide?c:d;
   if(divide){step(g,"나누는 분수를 뒤집어 역수의 분자를 쓰세요.","역수의 분자 e = d = ",top);step(g,"역수의 분모에 원래 분자를 부호와 함께 쓰세요.","역수의 분모 f = c = ",bottom);}
   numerator=a*top;denominator=b*bottom;
   step(g,"분자끼리 곱하세요.",divide?"계산한 분자 N = a × e = ":"계산한 분자 N = a × c = ",numerator);
   step(g,"분모끼리 곱하세요.",divide?"계산한 분모 D = b × f = ":"계산한 분모 D = b × d = ",denominator);
  }
  String den=add||subtract?"L":"D";long common=gcd(numerator,denominator),divisor=denominator<0?-common:common;
  step(g,"계산한 분자와 분모의 절댓값으로 최대공약수를 구하세요.","최대공약수 g = gcd(|N|, |"+den+"|) = ",common);
  step(g,"분모와 같은 부호의 최대공약수를 쓰세요. 분모를 이 수로 나누면 양수가 됩니다.","약분에 쓸 수 t = ",divisor);
  step(g,"계산한 분자를 앞에서 구한 수로 나누세요.","최종 분자 = N ÷ t = ",numerator/divisor);
  step(g,"계산한 분모도 같은 수로 나누세요.","최종 분모 = "+den+" ÷ t = ",denominator/divisor);
  g.fractionResult(g.frames.size()-2,g.frames.size()-1);q.studyGuide=g;
 }
 private static long gcd(long a,long b){a=Math.abs(a);b=Math.abs(b);while(b!=0){long t=a%b;a=b;b=t;}return a;}
 private static void step(StudyGuide g,String text,String before,long n){g.step(text,before,"",Long.toString(n));}
}
