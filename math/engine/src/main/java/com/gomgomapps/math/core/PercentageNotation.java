package com.gomgomapps.math.core;
import java.math.BigDecimal;import java.util.*;import java.util.regex.*;
/** Percent notation and its hundredths/decimal representation, reconstructed from public givens. */
public final class PercentageNotation {
 private PercentageNotation(){}
 public static final String FRACTION="percentHundredthsFraction",DECIMAL="percentDecimal";
 public static final List<Catalog.Skill> SKILLS=List.of(new Catalog.Skill(FRACTION,"백분율을 분수로",5,1,1,"","percentFrac",100,"","백분율을 분모가 100인 분수로 나타낸다."),new Catalog.Skill(DECIMAL,"백분율을 소수로",5,1,1,"","percentDecimal",100,"","백분율을 소수로 나타낸다."));
 public static boolean supports(String id){return FRACTION.equals(id)||DECIMAL.equals(id);}
 public static Question make(String id,int percent){
  if(!supports(id)||percent<0||percent>100)throw new IllegalArgumentException("Selected percentage notation scope");
  boolean fraction=FRACTION.equals(id);Question q=new Question(id,percent+"%를 "+(fraction?"분모가 100인 분수":"소수")+"로 나타내세요.","",fraction?percent+"/100":BigDecimal.valueOf(percent,2).stripTrailingZeros().toPlainString());q.answerFormat=fraction?"hundredthsFraction":"decimalValue";q.decimal=!fraction;q.stepSupport=false;attach(q);return q;
 }
 static Question next(Catalog.Skill s,Random random,CurriculumLimits limits,Map<String,Integer> recent){return IndexedQuestionSupply.choose(101,i->make(s.id,i),random,limits,recent);}
 public static Integer read(Question q){
  if(q==null||q.prompt==null||!supports(q.skillId))return null;Matcher m=Pattern.compile("(\\d{1,3})%를 "+(FRACTION.equals(q.skillId)?"분모가 100인 분수":"소수")+"로 나타내세요\\.").matcher(q.prompt);if(!m.matches())return null;int n=Integer.parseInt(m.group(1));return n<=100?n:null;
 }
 public static void attach(Question q){
  Integer p=read(q);if(p==null)return;StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="percentage-notation-v1";Map<String,String> labels=new LinkedHashMap<>();labels.put("100","100개 중 몇 개인지");labels.put("10","10개 중 몇 개인지");labels.put("1","1개 중 몇 개인지");g.choice("%가 뜻하는 관계를 고르세요.",labels,"100");
  g.step("전체를 같은 크기로 나눈 부분 수를 쓰세요.","분모 d = ","","100");g.step("문제의 백분율에서 부분 수를 쓰세요.","분자 n = ","",p.toString());
  if(DECIMAL.equals(q.skillId)){g.step("부분 수를 전체 부분 수로 나누어 소수로 쓰세요.","소수 v = n ÷ d = ","",BigDecimal.valueOf(p,2).stripTrailingZeros().toPlainString());g.frames.get(3).inputFormat="decimalValue";}q.studyGuide=g;
 }
 /** A task asking for a denominator of100 must preserve that representation. */
 public static boolean hundredths(String raw){String[] bits=raw.trim().split("/",-1);return bits.length==2&&bits[0].matches("\\d+")&&bits[1].matches("\\d+")&&new java.math.BigInteger(bits[1]).equals(java.math.BigInteger.valueOf(100));}
 public static int errorPart(Question q,String raw){Integer p=read(q);if(p==null||!FRACTION.equals(q.skillId)||FractionInput.invalidPart(raw)!=0)return 0;String[] bits=FractionInput.parts(raw);if(!new java.math.BigInteger(bits[1]).equals(java.math.BigInteger.valueOf(100)))return 2;return new java.math.BigInteger(bits[0]).equals(java.math.BigInteger.valueOf(p))?0:1;}
}
