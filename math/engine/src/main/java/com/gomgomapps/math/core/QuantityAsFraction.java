package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;import java.math.BigInteger;
/** Ordered compatible public quantities; selected supply bounds do not constrain public mathematics. */
public final class QuantityAsFraction {
 private QuantityAsFraction(){}
 public static final String ID="quantityAsFraction";
 static final List<String> UNITS=List.of("kg","m","L");
 private record Unit(String dimension,String base,Rational multiplier){}
 private static final Map<String,Unit> CONVERSIONS=Map.ofEntries(
  Map.entry("kg",new Unit("mass","g",Rational.of(1000))),Map.entry("g",new Unit("mass","g",Rational.ONE)),Map.entry("mg",new Unit("mass","g",Rational.of(1,1000))),
  Map.entry("km",new Unit("length","mm",Rational.of(1000000))),Map.entry("m",new Unit("length","mm",Rational.of(1000))),Map.entry("cm",new Unit("length","mm",Rational.of(10))),Map.entry("mm",new Unit("length","mm",Rational.ONE)),
  Map.entry("L",new Unit("volume","mL",Rational.of(1000))),Map.entry("mL",new Unit("volume","mL",Rational.ONE)),
  Map.entry("hour",new Unit("time","min",Rational.of(60))),Map.entry("h",new Unit("time","min",Rational.of(60))),Map.entry("min",new Unit("time","min",Rational.ONE)),Map.entry("s",new Unit("time","min",Rational.of(1,60))));
 public record Givens(String firstRaw,String secondRaw,Rational first,Rational second,String firstUnit,String secondUnit,String commonUnit,Rational firstMultiplier,Rational secondMultiplier,BigInteger quantization,BigInteger firstCount,BigInteger secondCount){
  public boolean legacy(){return firstUnit.equals(secondUnit)&&first.isInteger()&&second.isInteger();}
  public Rational value(){return new Rational(firstCount,secondCount);}
  public String unit(){return firstUnit;}
 }
 private static final Pattern PROMPT=Pattern.compile("두 수량은 ([0-9]+(?:\\.[0-9]+)?) ([A-Za-z]+)(?:과|와) ([0-9]+(?:\\.[0-9]+)?) ([A-Za-z]+)입니다\\.\\n첫 번째 수량을 두 번째 수량의 분수로 나타내세요\\.");
 public static Givens read(Question q){
  if(q==null||!ID.equals(q.skillId)||q.prompt==null||q.prompt.length()>240)return null;
  try{Matcher m=PROMPT.matcher(q.prompt);if(!m.matches()||m.group(1).length()>30||m.group(3).length()>30)return null;Rational a=Rational.decimal(m.group(1)),b=Rational.decimal(m.group(3));if(a.n.signum()<0||b.n.signum()<=0)return null;Unit u=CONVERSIONS.get(m.group(2)),v=CONVERSIONS.get(m.group(4));if(u==null||v==null||!u.dimension.equals(v.dimension))return null;
   boolean same=m.group(2).equals(m.group(4));Rational af=same?Rational.ONE:u.multiplier,bf=same?Rational.ONE:v.multiplier,A=a.mul(af),B=b.mul(bf);BigInteger quantum=A.d.divide(A.d.gcd(B.d)).multiply(B.d);Rational ca=A.mul(new Rational(quantum,BigInteger.ONE)),cb=B.mul(new Rational(quantum,BigInteger.ONE));return new Givens(m.group(1),m.group(3),a,b,m.group(2),m.group(4),same?m.group(2):u.base,af,bf,quantum,ca.n,cb.n);
  }catch(RuntimeException invalid){return null;}
 }
 static Question make(int a,int b,String unit){if(a<0||a>100||b<1||b>100||!UNITS.contains(unit))throw new IllegalArgumentException("Selected same-unit supply scope");return make(a,unit,b,unit);}
 private static Question make(int a,String au,int b,String bu){Question q=new Question(ID,"두 수량은 "+a+" "+au+(au.equals("kg")?"과 ":"와 ")+b+" "+bu+"입니다.\n첫 번째 수량을 두 번째 수량의 분수로 나타내세요.","","");Givens v=read(q);if(v==null)throw new IllegalArgumentException("Compatible quantities required");q.answers=new String[]{v.value().toString()};q.answerFormat="fraction";q.stepSupport=false;attach(q);return q.withInputs(a,b);}
 private static Question legacy(int i){String unit=UNITS.get(i%3);i/=3;return make(i/100,i%100+1,unit);}
 private static Question time(int i){if(i<1212)return make(i/12,"min",i%12+1,"hour");i-=1212;return make(i/100+1,"hour",i%100+1,"min");}
 static Question next(Random r,CurriculumLimits limits,Map<String,Integer> recent){
  if(!limits.quantityUnitConversions())return IndexedQuestionSupply.choose(30300,QuantityAsFraction::legacy,r,limits,recent);
  boolean conversion=r.nextBoolean();Question candidate;try{candidate=conversion?IndexedQuestionSupply.choose(2412,QuantityAsFraction::time,r,limits,recent):IndexedQuestionSupply.choose(30300,QuantityAsFraction::legacy,r,limits,recent);if(!recent.containsKey(candidate.signature()))return candidate;}catch(IllegalStateException emptySelectedFamily){}
  return IndexedQuestionSupply.choose(32712,i->i<30300?legacy(i):time(i-30300),r,limits,recent);
 }
 static Checker.Result check(Question q,List<String> answers){Givens v=read(q);if(v==null)return new Checker.Result(Checker.Status.INPUT_NEEDED,-1,"문제 확인 필요");return PublicFractionArithmetic.checkValue(q,answers,v.value());}
 public static StudyGuide guide(Question q){Givens v=read(q);if(v==null)return null;StudyGuide h=new StudyGuide().transfer(false);BigInteger a=v.firstCount,b=v.secondCount,g=a.gcd(b);h.teachingVersion=v.legacy()?"quantity-as-fraction-v1":"quantity-as-fraction-conversion-v1";
  step(h,"첫 번째 수량의 값을 쓰세요.","첫 번째 수량 a",v.first.toString());step(h,"두 번째 수량의 값을 쓰세요.","두 번째 수량 b",v.second.toString());
  if(v.legacy()){step(h,"두 수량의 최대공약수를 구하세요.","최대공약수 g = gcd(a, b)",g.toString());step(h,"첫 번째 수량을 최대공약수로 나누어 분자를 구하세요.","분자 n = a ÷ g",a.divide(g).toString());step(h,"두 번째 수량을 같은 최대공약수로 나누어 분모를 구하세요.","분모 d = b ÷ g",b.divide(g).toString());return h.fractionResult(3,4);}
  step(h,"첫 번째 수량을 같은 단위의 정수로 바꾸세요.","첫 번째 환산 수량 A",a.toString());step(h,"두 번째 수량을 같은 단위의 정수로 바꾸세요.","두 번째 환산 수량 B",b.toString());step(h,"환산한 두 수량의 최대공약수를 구하세요.","최대공약수 g = gcd(A, B)",g.toString());step(h,"환산한 첫 번째 수량을 최대공약수로 나누어 분자를 구하세요.","분자 n = A ÷ g",a.divide(g).toString());step(h,"환산한 두 번째 수량을 같은 최대공약수로 나누어 분모를 구하세요.","분모 d = B ÷ g",b.divide(g).toString());return h.fractionResult(5,6);
 }
 public static void attach(Question q){StudyGuide g=guide(q);if(g!=null)q.studyGuide=g;}
 public static int errorPart(Question q,String raw){Givens v=read(q);if(v==null||raw==null||raw.length()>120||FractionInput.invalidPart(raw)!=0)return 0;try{String[] p=FractionInput.parts(raw);BigInteger n=new BigInteger(p[0]),d=new BigInteger(p[1]);Rational expected=v.value();if(n.multiply(expected.d).equals(expected.n.multiply(d)))return 0;if(d.equals(expected.d)||d.equals(v.secondCount))return 1;if(n.equals(expected.n)||n.equals(v.firstCount))return 2;return 0;}catch(RuntimeException invalid){return 0;}}
 private static void step(StudyGuide g,String instruction,String before,String value){g.step(instruction,before+" = ","",value);}
}
