package com.gomgomapps.math.core;

/** Blank mixed-number conversion and common-denominator work from the visible problem. */
public final class MixedFractionRelations {
 private MixedFractionRelations(){}
 public static boolean supports(String id){return "fracMixedAdd".equals(id)||"fracMixedSub".equals(id);}
 public static void attach(Question question){
  if(question==null||!supports(question.skillId))return;
  MixedFractions.Givens p=MixedFractions.read(question.prompt);if(p==null)return;
  boolean subtract=question.skillId.equals("fracMixedSub");if(!p.operator.equals(subtract?"-":"+"))return;
  StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="mixed-fraction-relations-v1";
  long u=(long)p.wholeLeft*p.denominatorLeft+p.numeratorLeft,v=(long)p.wholeRight*p.denominatorRight+p.numeratorRight;
  operand(g,true,p.wholeLeft,p.numeratorLeft,p.denominatorLeft,u);
  operand(g,false,p.wholeRight,p.numeratorRight,p.denominatorRight,v);
  long denominator=(long)p.denominatorLeft/gcd(p.denominatorLeft,p.denominatorRight)*p.denominatorRight;
  long leftScale=denominator/p.denominatorLeft,rightScale=denominator/p.denominatorRight;
  long left=u*leftScale,right=v*rightScale,numerator=subtract?left-right:left+right,common=gcd(numerator,denominator);
  step(g,"두 분모의 최소공배수를 구하세요.","공통 분모 D = lcm(b, d) = ",denominator);
  step(g,"공통 분모를 첫 분모로 나누세요.","첫 통분 배수 h = D ÷ b = ",leftScale);
  step(g,"공통 분모를 둘째 분모로 나누세요.","둘째 통분 배수 k = D ÷ d = ",rightScale);
  step(g,"첫 가분수의 분자에 첫 통분 배수를 곱하세요.","통분한 첫 분자 A = u × h = ",left);
  step(g,"둘째 가분수의 분자에 둘째 통분 배수를 곱하세요.","통분한 둘째 분자 C = v × k = ",right);
  step(g,subtract?"통분한 분자끼리 빼세요.":"통분한 분자끼리 더하세요.",subtract?"계산한 분자 N = A − C = ":"계산한 분자 N = A + C = ",numerator);
  step(g,"계산한 분자와 분모를 모두 나눌 수 있는 가장 큰 수를 구하세요.","최대공약수 g = gcd(N, D) = ",common);
  step(g,"계산한 분자를 최대공약수로 나누세요.","최종 분자 = N ÷ g = ",numerator/common);
  step(g,"분모도 같은 최대공약수로 나누세요.","최종 분모 = D ÷ g = ",denominator/common);
  question.studyGuide=g.fractionResult(g.frames.size()-2,g.frames.size()-1);
 }
 private static void operand(StudyGuide g,boolean first,long whole,long numerator,long denominator,long improper){
  String prefix=first?"첫":"둘째",w=first?"w":"z",n=first?"a":"c",d=first?"b":"d",u=first?"u":"v";
  step(g,first?"첫 대분수의 자연수 부분을 쓰세요.":"둘째 대분수의 자연수 부분을 쓰세요.",prefix+" 자연수 부분 "+w+" = ",whole);
  step(g,first?"첫 번째 분수의 분자를 쓰세요.":"두 번째 분수의 분자를 쓰세요.",prefix+" 분자 "+n+" = ",numerator);
  step(g,first?"첫 번째 분수의 분모를 쓰세요.":"두 번째 분수의 분모를 쓰세요.",prefix+" 분모 "+d+" = ",denominator);
  step(g,"자연수 부분에 분모를 곱한 뒤 분자를 더하세요.",prefix+" 가분수 분자 "+u+" = "+w+" × "+d+" + "+n+" = ",improper);
 }
 private static long gcd(long a,long b){a=Math.abs(a);while(b!=0){long remainder=a%b;a=b;b=remainder;}return a;}
 private static void step(StudyGuide g,String instruction,String before,long value){g.step(instruction,before,"",Long.toString(value));}
}
