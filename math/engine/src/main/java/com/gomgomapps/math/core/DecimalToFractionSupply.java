package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;import java.math.*;
/** Displayed decimal precision is part of the question, including zero and trailing zeros. */
public final class DecimalToFractionSupply {
 private DecimalToFractionSupply(){}
 public static final String ID="el_decimal_fraction";
 public static boolean selected(String id,CurriculumLimits limits){return ID.equals(id)&&limits.variedFacts();}
 public record Givens(String raw,int places,BigInteger denominator,BigInteger numerator,BigInteger gcd,Rational value,boolean reduced){}
 public static Givens readGeneral(Question q){
  if(q==null||!ID.equals(q.skillId)||q.prompt==null)return null;Matcher m=Pattern.compile("(\\d{1,9}\\.\\d{1,9})의 값을 (기약)?분수로 나타내세요\\.").matcher(q.prompt);if(!m.matches())return null;
  String raw=m.group(1);int places=raw.length()-raw.indexOf('.')-1;BigInteger denominator=BigInteger.TEN.pow(places),numerator=new BigDecimal(raw).unscaledValue();return new Givens(raw,places,denominator,numerator,numerator.gcd(denominator),new Rational(numerator,denominator),m.group(2)!=null);
 }
 public static String read(Question q){Givens givens=readGeneral(q);return givens==null?null:givens.raw;}
 public static StudyGuide guide(Question q){
  Givens v=readGeneral(q);if(v==null)return null;StudyGuide h=new StudyGuide().transfer(false);h.teachingVersion="decimal-fraction-relations-v1";
  step(h,"문제의 소수를 쓰세요.","소수 v = ",v.raw);h.frames.get(0).inputFormat="decimal";
  step(h,"소수점 아래 자릿수를 세어 쓰세요.","소수점 아래 자릿수 p = ",Integer.toString(v.places));
  step(h,"첫째 자리의 분모는 10입니다. 자릿수가 하나 늘 때마다 분모가 10배가 됩니다.","분모 d = 자릿수 p에 맞는 분모 = ",v.denominator.toString());
  step(h,"소수에 분모를 곱해 분자를 구하세요.","분자 n = v × d = ",v.numerator.toString());h.fractionResult(3,2);
  if(v.reduced){h.teachingVersion="decimal-reduced-fraction-v1";step(h,"분자와 분모의 최대공약수를 구하세요.","최대공약수 g = ",v.gcd.toString());step(h,"분자를 최대공약수로 나누세요.","약분한 분자 N = n ÷ g = ",v.value.n.toString());step(h,"분모를 같은 최대공약수로 나누세요.","약분한 분모 D = d ÷ g = ",v.value.d.toString());h.fractionResult(5,6);}return h;
 }
 private static void step(StudyGuide guide,String instruction,String before,String expected){guide.step(instruction,before,"",expected);}
 /** Return only checked learner inputs that are needed for the current calculation. */
 public static Map<String,String> references(Question q,int stage,List<String> entries){
  StudyGuide h=guide(q);if(h==null||entries==null||stage<2||stage>=h.frames.size())return Map.of();Map<String,String> out=new LinkedHashMap<>();
  if(stage==2)reference(out,h,entries,1,"소수점 아래 자릿수 p = ");
  if(stage==3){reference(out,h,entries,0,"소수 v = ");reference(out,h,entries,2,"분모 d = ");}
  if(stage==4){reference(out,h,entries,3,"분자 n = ");reference(out,h,entries,2,"분모 d = ");}
  if(stage==5){reference(out,h,entries,3,"분자 n = ");reference(out,h,entries,4,"최대공약수 g = ");}
  if(stage==6){reference(out,h,entries,2,"분모 d = ");reference(out,h,entries,4,"최대공약수 g = ");}return out;
 }
 private static void reference(Map<String,String> out,StudyGuide h,List<String> entries,int index,String label){if(index<entries.size()&&new HelpPlan.Step(h.frames.get(index)).accepts(entries.get(index)))out.put(label,entries.get(index));}
 public static Question make(int whole,int fractional,int places){if(whole<0||whole>99||places<1||places>3||fractional<0||fractional>=Math.pow(10,places))throw new IllegalArgumentException("Selected decimal fraction scope");int denominator=(int)Math.pow(10,places),numerator=whole*denominator+fractional;String raw=whole+"."+String.format(Locale.ROOT,"%0"+places+"d",fractional);Question q=new Question(ID,raw+"의 값을 분수로 나타내세요.",raw,Rational.of(numerator,denominator).toString());q.stepSupport=false;q.answerFormat="fraction";DecimalFractionRelations.attach(q);return q.withInputs(Rational.of(numerator,denominator));}
 public static Question makeReduced(int whole,int fractional,int places){Question q=make(whole,fractional,places);q.prompt=q.prompt.replace("분수로","기약분수로");q.kind="reduced";DecimalFractionRelations.attach(q);return q;}
 static Question next(Random random,CurriculumLimits limits,Map<String,Integer> recent){int maxPlaces=Math.min(3,limits.decimalPlaces(2)),minimum=limits.givenMinimum(0),maximum=limits.givenMaximum(99),places=1+random.nextInt(maxPlaces),scale=(int)Math.pow(10,places),count=(maximum-minimum+1)*scale;return IndexedQuestionSupply.choose(count,index->limits.reducedDecimalFraction()?makeReduced(minimum+index/scale,index%scale,places):make(minimum+index/scale,index%scale,places),random,limits,recent);}
 static Checker.Result check(Question q,List<String> answers){Givens v=readGeneral(q);if(v==null||answers==null||answers.size()!=1||answers.get(0)==null)return new Checker.Result(Checker.Status.INPUT_NEEDED,-1,"답 입력 필요");String input;try{input=Expression.normalize(answers.get(0).trim());}catch(RuntimeException invalid){return new Checker.Result(Checker.Status.INPUT_NEEDED,-1,"수 입력 확인 필요");}if(input.isEmpty()||input.length()>120)return new Checker.Result(Checker.Status.INPUT_NEEDED,-1,"답 입력 필요");if("fraction".equals(q.answerFormat)&&!input.contains("/"))return new Checker.Result(Checker.Status.INPUT_NEEDED,-1,"분수로 입력 필요");if(!input.matches("[+-]?\\d+(?:\\.\\d+)?(?:/[+-]?\\d+)?"))return new Checker.Result(Checker.Status.INPUT_NEEDED,-1,"마지막 답은 수로 입력");try{if(!Expression.number(input).equals(v.value))return new Checker.Result(Checker.Status.WRONG_ANSWER,0,"이 답 확인");if(v.reduced||"reduced".equals(q.kind)){if(input.contains("."))return new Checker.Result(Checker.Status.INPUT_NEEDED,-1,"분수로 입력 필요");if(input.contains("/")){String[] pair=input.split("/");if(!new BigInteger(pair[0]).gcd(new BigInteger(pair[1])).equals(BigInteger.ONE))return new Checker.Result(Checker.Status.INPUT_NEEDED,-1,"약분 필요");}}return new Checker.Result(Checker.Status.CORRECT,-1,"계산 확인 완료");}catch(RuntimeException ex){return new Checker.Result(Checker.Status.INPUT_NEEDED,-1,"수 입력 확인 필요");}}
 public static int errorPart(Question q,String input){Givens v=readGeneral(q);if(v==null||input==null||input.length()>120||FractionInput.invalidPart(input)!=0)return 0;String[] pair=FractionInput.parts(input);BigInteger n=new BigInteger(pair[0]),d=new BigInteger(pair[1]);if(n.multiply(v.value.d).equals(v.value.n.multiply(d)))return 0;if(d.equals(v.denominator)||d.equals(v.value.d))return 1;if(n.equals(v.numerator)||n.equals(v.value.n))return 2;return 0;}
}
