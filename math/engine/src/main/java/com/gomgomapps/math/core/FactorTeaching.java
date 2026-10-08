package com.gomgomapps.math.core;
import java.math.BigInteger;
import java.util.*;

/** Numeric blanks from public coefficients; never reads the question's answer key. */
public final class FactorTeaching {
 private FactorTeaching(){}
 public static boolean supports(String id){return Set.of("expand","factor","quadratic").contains(id);}
 private static Expression.Poly polynomial(Question q){return q.kind.equals("roots")?Expression.equation(q.expression):Expression.parse(q.expression);}
 private static Rational[] pair(Expression.Poly p){
  if(p.degree()!=2||!p.coefficient(2).equals(Rational.ONE))return null;
  Rational sum=p.coefficient(1),product=p.coefficient(0),d=sum.mul(sum).sub(Rational.of(4).mul(product));
  if(d.n.signum()<0)return null;BigInteger n=d.n.sqrt(),den=d.d.sqrt();if(!n.multiply(n).equals(d.n)||!den.multiply(den).equals(d.d))return null;
  Rational root=new Rational(n,den),u=sum.sub(root).div(Rational.of(2)),v=sum.add(root).div(Rational.of(2));return u.isInteger()&&v.isInteger()?new Rational[]{u,v}:null;
 }
 public static void attach(Question q){
  if(q==null||!supports(q.skillId)||q.studyGuide!=null)return;
  Expression.Poly p=polynomial(q);if(p.degree()!=2)return;Rational a=p.coefficient(2),b=p.coefficient(1),c=p.coefficient(0);Rational[] pair=pair(p);StudyGuide help=new StudyGuide().transfer(false);
  if(q.skillId.equals("expand")&&pair!=null){
   help.step("x의 계수끼리 곱하세요.","첫 인수의 x 계수 × 둘째 인수의 x 계수 = ","", "1")
    .step("두 상수를 더해 x의 계수를 구하세요.","첫 인수의 상수 + 둘째 인수의 상수 = ","",b.toString())
    .step("두 상수를 곱해 상수항을 구하세요.","첫 인수의 상수 × 둘째 인수의 상수 = ","",c.toString());
  }else if(pair!=null){
   help.step("x의 계수를 쓰세요.","두 수의 합 = ","",b.toString())
    .step("상수항을 쓰세요.","두 수의 곱 = ","",c.toString())
    .step("합과 곱을 만족하는 두 정수 중 작은 수를 쓰세요.","두 수의 합은 x의 계수, 곱은 상수항\n작은 수 = ","",pair[0].toString())
    .step("합에서 작은 수를 빼 큰 수를 구하세요.","두 수의 합 − 작은 수 = ","",pair[1].toString());
   if(q.skillId.equals("quadratic")){
    help.step("곱이 0이면 인수 중 하나가 0입니다. 첫째 해를 구하세요.","x + ("+pair[0]+") = 0\nx = −("+pair[0]+") = ","",pair[0].neg().toString());
    if(!pair[0].equals(pair[1]))help.step("다른 인수가 0일 때 둘째 해를 구하세요.","x + ("+pair[1]+") = 0\nx = −("+pair[1]+") = ","",pair[1].neg().toString());
   }
  }else if(q.skillId.equals("quadratic")){
   Rational d=b.mul(b).sub(Rational.of(4).mul(a).mul(c));if(d.n.signum()<0)return;
   help.step("x²의 계수 a를 쓰세요.","a = ","",a.toString()).step("x의 계수 b를 쓰세요.","b = ","",b.toString()).step("상수항 c를 쓰세요.","c = ","",c.toString())
    .step("판별식 D를 계산하세요.","("+b+")² − 4 × "+a+" × ("+c+") = ","",d.toString())
    .step("근의 공식에서 −b를 계산하세요.","−("+b+") = ","",b.neg().toString())
    .step("근의 공식의 분모 2a를 계산하세요.","2 × "+a+" = ","",a.mul(Rational.of(2)).toString());
  }
  if(!help.frames.isEmpty())q.studyGuide=help;
 }
 public static String inputTemplate(Question q){
  if(q==null||!supports(q.skillId))return null;
  if(q.skillId.equals("expand"))return "x² + □x + □";
  if(q.skillId.equals("factor"))return "(x + □)(x + □)";
  return pair(polynomial(q))!=null?"(x + □)(x + □) = 0":"x = (−b ± √D) ÷ (2a)";
 }
 /** Assembles only values the student already entered into the checked blanks. */
 public static String completedText(Question q,List<String> entries){
  if(q==null||!supports(q.skillId))return null;
  if(q.skillId.equals("expand")&&entries.size()>=3)return Expression.parse("x^2+("+entries.get(1)+")x+("+entries.get(2)+")").toString();
  if(q.skillId.equals("factor")&&entries.size()>=4)return "(x + ("+entries.get(2)+"))(x + ("+entries.get(3)+"))";
  if(q.skillId.equals("quadratic")&&pair(polynomial(q))!=null&&entries.size()>=5)return "x = "+entries.get(4)+(q.studyGuide.frames.size()==6&&entries.size()>=6?", "+entries.get(5):"");
  if(q.skillId.equals("quadratic")&&entries.size()>=6)return "x = ("+entries.get(4)+" ± √("+entries.get(3)+")) / ("+entries.get(5)+")";
  return null;
 }
}
