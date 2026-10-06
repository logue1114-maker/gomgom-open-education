package com.gomgomapps.math.core;
import java.math.BigDecimal;
import java.util.*;

/** Exact quantity calculations; the learner supplies both normalized components. */
public final class ScientificQuantity {
 private ScientificQuantity(){}
 public static final List<Catalog.Skill> SKILLS=List.of(
  new Catalog.Skill("scientificMassProduct","과학적 표기로 전체 질량 구하기",9,1,2,"","scientificMassProduct",100,"standardForm,powerLaw,decimalMul","한 개의 질량에 개수를 곱한다. 계수를 곱하고 지수를 더한 뒤 계수를 1 이상 10 미만으로 정리한다."),
  new Catalog.Skill("scientificSpeedQuotient","과학적 표기로 속력 구하기",9,1,2,"","scientificSpeedQuotient",100,"standardForm,negativePower,decimalDiv","이동 거리를 시간으로 나눈다. 계수를 나누고 지수를 뺀 뒤 계수를 1 이상 10 미만으로 정리한다."),
  new Catalog.Skill("scientificMassSum","과학적 표기로 두 질량 더하기",9,1,2,"","scientificMassSum",100,"standardForm,decimalAdd","10의 지수를 같게 맞춘 뒤 계수를 더하고 과학적 표기로 정리한다."),
  new Catalog.Skill("scientificLengthDifference","과학적 표기로 남은 길이 구하기",9,1,2,"","scientificLengthDifference",100,"standardForm,decimalSub","전체 길이와 사용한 길이의 지수를 같게 맞춘 뒤 계수를 빼고 과학적 표기로 정리한다."));
 public static boolean supports(String id){return SKILLS.stream().anyMatch(s->s.id.equals(id));}
 static Question create(Catalog.Skill s,Random r){
  if(s.id.equals("scientificMassSum")||s.id.equals("scientificLengthDifference"))return additive(s,r);
  boolean product=s.id.equals("scientificMassProduct");
  BigDecimal a=product?BigDecimal.valueOf(10+r.nextInt(90),1):BigDecimal.valueOf(1+r.nextInt(9));
  int b=product?1+r.nextInt(9):new int[]{2,4,5,8}[r.nextInt(4)];
  int u=product?-8+r.nextInt(8):-5+r.nextInt(12),v=product?1+r.nextInt(5):-3+r.nextInt(8);
  BigDecimal raw=product?a.multiply(BigDecimal.valueOf(b)):a.divide(BigDecimal.valueOf(b));
  int exponent=product?u+v:u-v,shift=raw.stripTrailingZeros().precision()-raw.stripTrailingZeros().scale()-1;
  BigDecimal coefficient=raw.scaleByPowerOfTen(-shift);int normalized=exponent+shift;
  String x=plain(a),y=String.valueOf(b),left=x+" × 10^("+u+")",right=y+" × 10^("+v+")";
  String context=product?"한 개의 질량: "+left+" g\n개수: "+right+"개\n전체 질량 = a × 10^n g":
   "이동 거리: "+left+" m\n걸린 시간: "+right+" s\n일정한 속력 = a × 10^n m/s";
  Question q=new Question(s.id,context+"\n1 ≤ a < 10. a와 n을 구하세요.","("+x+"*10^("+u+"))"+(product?"*":"/")+"("+y+"*10^("+v+"))",plain(coefficient),String.valueOf(normalized));
  q.kind="pair";q.decimal=true;q.labels=new String[]{"계수 a","지수 n"};q.stepSupport=false;
  q.studyGuide=new StudyGuide().transfer(false)
   .step(product?"계수끼리 곱하세요.":"계수끼리 나누세요.",x+(product?" × ":" ÷ ")+y+" = ","",plain(raw))
   .step(product?"10의 지수를 더하세요.":"10의 지수를 빼세요.","("+u+")"+(product?" + ":" - ")+"("+v+") = ","",String.valueOf(exponent))
   .step("계수를 1 이상 10 미만으로 정리하세요.",plain(raw)+" × 10^("+exponent+") = □ × 10^n\n□ = ","",plain(coefficient))
   .step("계수의 자릿값 변화에 맞춰 지수를 정리하세요.",plain(raw)+" × 10^("+exponent+") = "+plain(coefficient)+" × 10^□\n□ = ","",String.valueOf(normalized));
  return q.withInputs(Rational.decimal(x),Rational.of(b),Rational.of(u),Rational.of(v));
 }
 private static Question additive(Catalog.Skill s,Random r){
  boolean sum=s.id.equals("scientificMassSum");BigDecimal a,b,left,right;int u,v;
  do{
   a=BigDecimal.valueOf(10+r.nextInt(90),1);b=BigDecimal.valueOf(10+r.nextInt(90),1);u=-6+r.nextInt(13);v=u-2+r.nextInt(5);
   left=a.scaleByPowerOfTen(u);right=b.scaleByPowerOfTen(v);
  }while(!sum&&left.compareTo(right)==0);
  if(!sum&&left.compareTo(right)<0){BigDecimal swap=a;a=b;b=swap;int temp=u;u=v;v=temp;swap=left;left=right;right=swap;}
  int common=Math.min(u,v);BigDecimal alignedA=a.scaleByPowerOfTen(u-common),alignedB=b.scaleByPowerOfTen(v-common);
  BigDecimal raw=sum?alignedA.add(alignedB):alignedA.subtract(alignedB),total=sum?left.add(right):left.subtract(right);
  int n=total.stripTrailingZeros().precision()-total.stripTrailingZeros().scale()-1;String coefficient=plain(total.scaleByPowerOfTen(-n));
  String x=plain(a),y=plain(b),first=x+" × 10^("+u+")",second=y+" × 10^("+v+")";
  String context=sum?"첫째 물체의 질량: "+first+" g\n둘째 물체의 질량: "+second+" g\n두 질량의 합 = a × 10^n g":
   "전체 길이: "+first+" m\n사용한 길이: "+second+" m\n남은 길이 = a × 10^n m";
  Question q=new Question(s.id,context+"\n1 ≤ a < 10. a와 n을 구하세요.","("+x+"*10^("+u+"))"+(sum?"+":"-")+"("+y+"*10^("+v+"))",coefficient,String.valueOf(n));
  q.kind="pair";q.decimal=true;q.labels=new String[]{"계수 a","지수 n"};q.stepSupport=false;
  q.studyGuide=new StudyGuide().transfer(false)
   .step("첫째 양의 지수를 맞추세요.",first+" = □ × 10^("+common+")\n□ = ","",plain(alignedA))
   .step("둘째 양의 지수를 맞추세요.",second+" = □ × 10^("+common+")\n□ = ","",plain(alignedB))
   .step(sum?"지수가 같아진 두 계수를 더하세요.":"지수가 같아진 두 계수를 빼세요.",plain(alignedA)+(sum?" + ":" - ")+plain(alignedB)+" = ","",plain(raw))
   .step("계수를 1 이상 10 미만으로 정리하세요.",plain(raw)+" × 10^("+common+") = □ × 10^n\n□ = ","",coefficient)
   .step("계수의 자릿값 변화에 맞춰 지수를 정리하세요.",plain(raw)+" × 10^("+common+") = "+coefficient+" × 10^□\n□ = ","",String.valueOf(n));
  return q.withInputs(Rational.decimal(x),Rational.decimal(y),Rational.of(u),Rational.of(v));
 }
 private static String plain(BigDecimal x){return x.stripTrailingZeros().toPlainString();}
}
