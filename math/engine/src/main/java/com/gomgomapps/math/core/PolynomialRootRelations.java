package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;import java.math.BigInteger;
/** Derive factor decisions and roots solely from the public equation. */
public final class PolynomialRootRelations {
 private PolynomialRootRelations(){}
 public static boolean supports(String id){return Set.of("sec_factor_theorem","sec_cubic_equation","sec_quartic_equation").contains(id);}
 private static Rational sqrt(Rational v){if(v.n.signum()<0)return null;BigInteger n=v.n.sqrt(),d=v.d.sqrt();return n.multiply(n).equals(v.n)&&d.multiply(d).equals(v.d)?Expression.number(n+"/"+d):null;}
 public static void attach(Question q){
  if(q==null||!supports(q.skillId)||q.prompt==null)return;
  String n="[+-]?\\d+(?:\\.\\d+)?(?:/\\d+)?";StudyGuide g=new StudyGuide().transfer(false);
  if(q.skillId.equals("sec_factor_theorem")){
   Matcher m=Pattern.compile("P\\(x\\)=x²\\+\\(("+n+")\\)x\\+\\(("+n+")\\)일 때 x-\\(("+n+")\\)가 P\\(x\\)의 인수인지 고르세요\\.").matcher(q.prompt);if(!m.matches())return;
   Rational l=Expression.number(m.group(1)),c=Expression.number(m.group(2)),t=Expression.number(m.group(3)),square=t.mul(t),product=l.mul(t),value=square.add(product).add(c);g.teachingVersion="factor-theorem-relations-v1";
   g.step("x−t가 인수인지 확인할 후보값 t를 쓰세요.","후보값 t = ","",t.toString())
    .step("P(x)=x²+Lx+C에서 x항 계수 L을 쓰세요.","x항 계수 L = ","",l.toString())
    .step("P(x)의 상수항 C를 부호까지 쓰세요.","상수항 C = ","",c.toString())
    .step("후보값을 제곱하세요.","t² = ","",square.toString())
    .step("x항 계수와 후보값을 곱하세요.","L × t = ","",product.toString())
    .step("계산한 두 항과 상수항을 더하세요.","t² + L × t + C = ","",value.toString());
   LinkedHashMap<String,String> labels=new LinkedHashMap<>();labels.put("1","인수이다");labels.put("0","인수가 아니다");
   g.choice("P(t)=0이면 x−t는 인수입니다. 계산 결과로 판단하세요.",labels,value.equals(Rational.ZERO)?"1":"0");
  }else if(q.skillId.equals("sec_cubic_equation")){
   Matcher m=Pattern.compile("(?:("+n+")×)?\\(x-\\(("+n+")\\)\\)³=0을 만족하는 x는\\?").matcher(q.prompt);if(!m.matches())return;
   Rational k=m.group(1)==null?Rational.ONE:Expression.number(m.group(1)),a=Expression.number(m.group(2));if(k.equals(Rational.ZERO))return;g.teachingVersion="cubic-equation-relations-v1";
   g.step("세제곱식 앞의 계수 k를 쓰세요. 계수가 없으면 1입니다.","앞의 계수 k = ","",k.toString())
    .step("괄호 안 x−a에서 a를 부호까지 쓰세요.","괄호 안의 a = ","",a.toString())
    .step("k는 0이 아니므로 괄호 안의 세제곱이 0입니다. x−a=0을 풀어 x를 구하세요.","x = a = ","",a.toString());
  }else{
   Matcher m=Pattern.compile("x⁴(?:\\+\\(("+n+")\\))?=("+n+")의 양의 해는\\?").matcher(q.prompt);if(!m.matches())return;
   Rational b=m.group(1)==null?Rational.ZERO:Expression.number(m.group(1)),y=Expression.number(m.group(2)),power=y.sub(b),square=sqrt(power),root=square==null?null:sqrt(square);if(root==null||root.compareTo(Rational.ZERO)<=0)return;g.teachingVersion="quartic-equation-relations-v1";
   g.step("왼쪽의 상수항 b를 부호까지 쓰세요. 상수항이 없으면 0입니다.","상수항 b = ","",b.toString())
    .step("등호 오른쪽의 값 y를 쓰세요.","오른쪽 값 y = ","",y.toString())
    .step("양변에서 상수항을 빼세요.","x⁴ = y − b = ","",power.toString())
    .step("x²는 음수가 아닙니다. 앞에서 계산한 x⁴의 양의 제곱근을 구하세요.","x² = √(y − b) = ","",square.toString())
    .step("양의 해 조건에 따라 x²의 양의 제곱근을 구하세요.","x = √(x²) = ","",root.toString());
  }
  q.studyGuide=g;
 }
}
