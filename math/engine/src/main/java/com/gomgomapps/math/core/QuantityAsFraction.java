package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;import java.math.BigInteger;
/** Ordered, same-unit quantities expressed as a dimensionless fraction. */
public final class QuantityAsFraction {
 private QuantityAsFraction(){}
 public static final String ID="quantityAsFraction";
 static final List<String> UNITS=List.of("kg","m","L");
 record Givens(int first,int second,String unit){}
 static Givens read(Question q){if(q==null||!ID.equals(q.skillId)||q.prompt==null)return null;Matcher m=Pattern.compile("두 수량은 (\\d{1,3}) (kg|m|L)(?:과|와) (\\d{1,3}) (kg|m|L)입니다\\.\\n첫 번째 수량을 두 번째 수량의 분수로 나타내세요\\.").matcher(q.prompt);if(!m.matches()||!m.group(2).equals(m.group(4)))return null;int a=Integer.parseInt(m.group(1)),b=Integer.parseInt(m.group(3));return a>=0&&a<=100&&b>=1&&b<=100?new Givens(a,b,m.group(2)):null;}
 static Question make(int a,int b,String unit){Question q=new Question(ID,"두 수량은 "+a+" "+unit+(unit.equals("kg")?"과 ":"와 ")+b+" "+unit+"입니다.\n첫 번째 수량을 두 번째 수량의 분수로 나타내세요.","",b>0?Rational.of(a,b).toString():"");if(read(q)==null)throw new IllegalArgumentException("Same-unit ratio scope");q.answerFormat="fraction";q.stepSupport=false;attach(q);return q.withInputs(a,b);}
 static Question next(Random r,CurriculumLimits limits,Map<String,Integer> recent){return IndexedQuestionSupply.choose(30300,i->{String unit=UNITS.get(i%3);i/=3;return make(i/100,i%100+1,unit);},r,limits,recent);}
 static Checker.Result check(Question q,List<String> answers){Givens v=read(q);if(v==null)return new Checker.Result(Checker.Status.INPUT_NEEDED,-1,"문제 확인 필요");return PublicFractionArithmetic.checkValue(q,answers,Rational.of(v.first,v.second));}
 public static void attach(Question q){Givens v=read(q);if(v==null)return;int g=Generator.gcd(v.first,v.second);StudyGuide h=new StudyGuide().transfer(false);h.teachingVersion="quantity-as-fraction-v1";
  step(h,"첫 번째 수량의 값을 쓰세요.","첫 번째 수량 a",v.first);step(h,"두 번째 수량의 값을 쓰세요.","두 번째 수량 b",v.second);step(h,"두 수량의 최대공약수를 구하세요.","최대공약수 g = gcd(a, b)",g);step(h,"첫 번째 수량을 최대공약수로 나누어 분자를 구하세요.","분자 n = a ÷ g",v.first/g);step(h,"두 번째 수량을 같은 최대공약수로 나누어 분모를 구하세요.","분모 d = b ÷ g",v.second/g);q.studyGuide=h.fractionResult(3,4);
 }
 public static int errorPart(Question q,String raw){Givens v=read(q);if(v==null||raw==null||raw.length()>120||FractionInput.invalidPart(raw)!=0)return 0;String[] p=FractionInput.parts(raw);BigInteger n=new BigInteger(p[0]),d=new BigInteger(p[1]);Rational expected=Rational.of(v.first,v.second);if(n.multiply(expected.d).equals(expected.n.multiply(d)))return 0;if(d.equals(expected.d)||d.equals(BigInteger.valueOf(v.second)))return 1;if(n.equals(expected.n)||n.equals(BigInteger.valueOf(v.first)))return 2;return 0;}
 private static void step(StudyGuide g,String instruction,String before,int value){g.step(instruction,before+" = ","",Integer.toString(value));}
}
