package com.gomgomapps.math.core;
/** Learner-entered conversion, reciprocal, products and reduction from public operands. */
public final class FractionProductRelations {
 private FractionProductRelations(){}
 public static boolean supports(String id){return FractionProducts.SKILLS.contains(id)||"fracDivInt".equals(id);}
 public static void attach(Question q){
  if(q==null||!supports(q.skillId))return;FractionProducts.Givens p=FractionProducts.read(q.prompt==null?null:q.prompt.replace("(","").replace(")","").replace(" / "," ÷ "));if(p==null)return;
  StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="fraction-product-relations-v1";
  String left=operand(g,p.left,true),right=operand(g,p.right,false);boolean div=p.operator.equals("/");
  long ln=p.left.improperNumerator(),ld=p.left.denominator,rn=p.right.improperNumerator(),rd=p.right.denominator;
  if(div){
   step(g,"나누는 분수를 뒤집어 역수의 분자를 쓰세요.","역수의 분자 e = d = ",rd);
   step(g,"역수의 분모에 나누는 수의 분자를 쓰세요.","역수의 분모 f = "+right+" = ",rn);
  }
  long n=ln*(div?rd:rn),d=ld*(div?rn:rd),common=gcd(n,d);
  step(g,"분자끼리 곱하세요.","계산한 분자 N = "+left+" × "+(div?"e":right)+" = ",n);
  step(g,"분모끼리 곱하세요.",div?"계산한 분모 D = b × f = ":"계산한 분모 D = b × d = ",d);
  step(g,"계산한 분자와 분모를 모두 나눌 수 있는 가장 큰 수를 구하세요.","최대공약수 g = gcd(N, D) = ",common);
  step(g,"계산한 분자를 최대공약수로 나누세요.","최종 분자 = N ÷ g = ",n/common);
  step(g,"분모도 같은 최대공약수로 나누세요.","최종 분모 = D ÷ g = ",d/common);
  q.studyGuide=g.fractionResult(g.frames.size()-2,g.frames.size()-1);
 }
 private static String operand(StudyGuide g,FractionProducts.Operand p,boolean first){
  String num=first?"a":"c",den=first?"b":"d",whole=first?"w":"z",improper=first?"u":"v",prefix=first?"첫":"둘째";
  if(p.mixed())step(g,first?"첫 대분수의 자연수 부분을 쓰세요.":"둘째 대분수의 자연수 부분을 쓰세요.",prefix+" 자연수 부분 "+whole+" = ",p.whole);
  step(g,p.denominator==1?"자연수를 분자로 쓰세요.":first?"첫 번째 분수의 분자를 쓰세요.":"두 번째 분수의 분자를 쓰세요.",prefix+" 분자 "+num+" = ",p.denominator==1?p.whole:p.numerator);
  step(g,p.denominator==1?"자연수는 분모가 1인 분수로 쓸 수 있습니다. 분모를 쓰세요.":first?"첫 번째 분수의 분모를 쓰세요.":"두 번째 분수의 분모를 쓰세요.",prefix+" 분모 "+den+" = ",p.denominator);
  if(p.mixed()){step(g,"자연수 부분에 분모를 곱한 뒤 분자를 더하세요.",prefix+" 가분수 분자 "+improper+" = "+whole+" × "+den+" + "+num+" = ",p.improperNumerator());return improper;}
  return num;
 }
 private static long gcd(long a,long b){while(b!=0){long r=a%b;a=b;b=r;}return a;}
 private static void step(StudyGuide g,String text,String before,long value){g.step(text,before,"",Long.toString(value));}
}
