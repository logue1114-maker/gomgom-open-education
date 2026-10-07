package com.gomgomapps.math.core;
import java.util.Set;
import java.util.regex.*;
/** Entered stages derived only from visible fractions and equations. */
final class FractionEquationTeaching {
 private FractionEquationTeaching(){}
 static void attach(Question q){
  if(RationalArithmeticRelations.supports(q.skillId)){RationalArithmeticRelations.attach(q);return;}
  if(q.skillId.equals("reduce")){FractionReductionRelations.attach(q);return;}
  if(LinearEquationRelations.supports(q.skillId)){LinearEquationRelations.attach(q);return;}
  if(!Set.of("fracAddLike","fracSubLike","fracAdd","fracSub","fracMul","fracDiv","rational").contains(q.skillId))return;
  Matcher m=Pattern.compile("\\((-?\\d+)/(\\d+)\\)\\s*([+*/−-])\\s*\\((-?\\d+)/(\\d+)\\)").matcher(q.expression);if(!m.matches())return;
  int a=Integer.parseInt(m.group(1)),b=Integer.parseInt(m.group(2)),c=Integer.parseInt(m.group(4)),d=Integer.parseInt(m.group(5));String op=m.group(3);if(b<=0||d<=0||c==0)return;
  StudyGuide guide=new StudyGuide().transfer(false);int numerator,denominator;
  if(op.equals("+")||op.equals("-")||op.equals("−")){
   denominator=b/gcd(b,d)*d;int left=a*(denominator/b),right=c*(denominator/d);boolean add=op.equals("+");numerator=add?left+right:left-right;
   guide.step("두 분모의 최소공배수를 구하세요.","lcm("+b+", "+d+") = ","",String.valueOf(denominator));
   guide.step("첫 번째 분수의 분자에도 같은 수를 곱하세요.",a+" × ("+denominator+" ÷ "+b+") = ","",String.valueOf(left));
   guide.step("두 번째 분수의 분자에도 같은 수를 곱하세요.",c+" × ("+denominator+" ÷ "+d+") = ","",String.valueOf(right));
   guide.step(add?"통분한 분자끼리 더하세요.":"통분한 분자끼리 빼세요.",left+(add?" + ":" − ")+right+" = ","",String.valueOf(numerator));
  }else{
   boolean divide=op.equals("/");
   if(divide){guide.step("나누는 분수를 뒤집어 분자에 쓸 수를 입력하세요.","역수의 분자 = ","",String.valueOf(d));guide.step("뒤집은 분수의 분모에 쓸 수를 입력하세요.","역수의 분모 = ","",String.valueOf(c));}
   int top=divide?d:c,bottom=divide?c:d;numerator=a*top;denominator=b*bottom;
   guide.step("분자끼리 곱하세요.",a+" × "+top+" = ","",String.valueOf(numerator));
   guide.step("분모끼리 곱하세요.",b+" × "+bottom+" = ","",String.valueOf(denominator));
  }
  int common=gcd(numerator,denominator);
  guide.step("계산한 분자와 분모의 최대공약수를 구하세요.","gcd("+numerator+", "+denominator+") = ","",String.valueOf(common));
  int divisor=denominator<0?-common:common;
  guide.step(denominator<0?"분모를 양수로 만들려면 분자와 분모를 음의 최대공약수로 나누세요.":"분자를 최대공약수로 나누세요.",numerator+" ÷ "+(divisor<0?"("+divisor+")":divisor)+" = ","",String.valueOf(numerator/divisor));
  guide.step(denominator<0?"분모를 같은 음수로 나누세요.":"분모를 같은 최대공약수로 나누세요.",denominator+" ÷ "+(divisor<0?"("+divisor+")":divisor)+" = ","",String.valueOf(denominator/divisor));guide.fractionResult(guide.frames.size()-2,guide.frames.size()-1);q.studyGuide=guide;
 }
 private static int gcd(int a,int b){while(b!=0){int next=a%b;a=b;b=next;}return Math.abs(a);}
}
