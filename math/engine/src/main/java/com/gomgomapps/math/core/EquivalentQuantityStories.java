package com.gomgomapps.math.core;
import java.math.BigDecimal;import java.util.*;import java.util.regex.*;
/** Same total/part relationship in fraction, decimal and percentage story forms. */
public final class EquivalentQuantityStories {
 private EquivalentQuantityStories(){}
 public static final String FRACTION="fractionQuantityStory",DECIMAL="decimalQuantityPart",PERCENT="percentQuantityStory";
 public static final List<Catalog.Skill> SKILLS=List.of(new Catalog.Skill(FRACTION,"분수만큼 사용한 양",5,1,1,"","quantityPartStory",100,"fractionDecimalPercent","전체 양과 분수로 사용한 양을 구한다."),new Catalog.Skill(DECIMAL,"소수만큼 사용한 양",5,1,1,"","quantityPartStory",100,"fractionDecimalPercent","전체 양과 소수 비율로 사용한 양을 구한다."),new Catalog.Skill(PERCENT,"백분율만큼 사용한 양",5,1,1,"","quantityPartStory",100,"percentDecimal","전체 양과 백분율로 사용한 양을 구한다."));
 public static String identity(String signature){int pipe=signature.indexOf('|');if(pipe<0||!supports(signature.substring(0,pipe)))return signature;return signature.replace("를 사용했습니다.","만큼을 사용했습니다.").replace("를 잘랐습니다.","만큼을 잘랐습니다.");}
 public static boolean supports(String id){return Set.of(FRACTION,DECIMAL,PERCENT).contains(id);}
 public record Givens(int total,String unit,String displayedRate,BigDecimal rate,Integer numerator,Integer denominator){}
 public static int domainSize(String id){if(!supports(id))throw new IllegalArgumentException("Unknown quantity story");return (FRACTION.equals(id)?FractionPercentEquivalence.domainSize():1001)*20;}
 public static Question make(String id,int total,String unit,String raw){
  if(!supports(id)||total<10||total>100||total%10!=0||!Set.of("m","L").contains(unit))throw new IllegalArgumentException("Selected quantity story scope");BigDecimal rate=parseRate(id,raw);if(rate==null)throw new IllegalArgumentException("Invalid visible rate");String text=raw+(DECIMAL.equals(id)?"배":PERCENT.equals(id)?"%":"");
  String prompt=unit.equals("L")?"물 "+total+" L의 "+text+"만큼을 사용했습니다.\n사용한 물은 몇 L인가요?":"끈 "+total+" m의 "+text+"만큼을 잘랐습니다.\n잘라 낸 끈은 몇 m인가요?";
  Question q=new Question(id,prompt,"",plain(rate.multiply(BigDecimal.valueOf(total))));q.answerFormat="decimalValue";q.decimal=true;q.stepSupport=false;attach(q);return q;
 }
 private static BigDecimal parseRate(String id,String raw){
  try{if(FRACTION.equals(id)){Matcher m=Pattern.compile("(\\d{1,4})/(\\d{1,4})").matcher(raw);if(!m.matches())return null;var v=FractionPercentEquivalence.read(new Question(FractionPercentEquivalence.ID,raw+"를 소수와 백분율로 나타내세요.","",""));return v==null?null:BigDecimal.valueOf(v.numerator()).divide(BigDecimal.valueOf(v.denominator()));}if(!raw.matches("\\d+(?:\\.\\d{1,3})?"))return null;BigDecimal rate=new BigDecimal(raw);if(PERCENT.equals(id))rate=rate.movePointLeft(2);return rate.signum()<0||rate.compareTo(BigDecimal.ONE)>0||rate.stripTrailingZeros().scale()>3?null:rate;}catch(ArithmeticException|IllegalArgumentException error){return null;}
 }
 private static Question at(String id,int index){int variant=index%20,condition=index/20,total=(variant/2+1)*10;String unit=variant%2==0?"m":"L",raw;if(FRACTION.equals(id)){var x=FractionPercentEquivalence.conditionAt(condition);raw=x.numerator()+"/"+x.denominator();}else raw=BigDecimal.valueOf(condition,DECIMAL.equals(id)?3:1).toPlainString();return make(id,total,unit,raw);}
 static Question next(Catalog.Skill s,Random random,CurriculumLimits limits,Map<String,Integer> recent){return IndexedQuestionSupply.choose(domainSize(s.id),i->at(s.id,i),random,limits,recent);}
 public static Givens read(Question q){if(q==null||q.prompt==null||!supports(q.skillId))return null;String raw=FRACTION.equals(q.skillId)?"(\\d{1,4}/\\d{1,4})":"(\\d+(?:\\.\\d{1,3})?)",suffix=DECIMAL.equals(q.skillId)?"배":PERCENT.equals(q.skillId)?"%":"";
  for(String unit:List.of("m","L")){String pattern=unit.equals("L")?"물 (\\d{1,3}) L의 "+raw+suffix+"(?:만큼을|를) 사용했습니다.\\n사용한 물은 몇 L인가요\\?":"끈 (\\d{1,3}) m의 "+raw+suffix+"(?:만큼을|를) 잘랐습니다.\\n잘라 낸 끈은 몇 m인가요\\?";Matcher m=Pattern.compile(pattern).matcher(q.prompt);if(m.matches()){int total=Integer.parseInt(m.group(1));if(total<10||total>100||total%10!=0)return null;String displayed=m.group(2);BigDecimal rate=parseRate(q.skillId,displayed);if(rate==null)return null;Integer n=null,d=null;if(FRACTION.equals(q.skillId)){String[] parts=displayed.split("/");n=Integer.valueOf(parts[0]);d=Integer.valueOf(parts[1]);}return new Givens(total,unit,displayed,rate,n,d);}}return null;
 }
 public static void attach(Question q){Givens v=read(q);if(v==null)return;StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="equivalent-quantity-story-v1";g.step("문제의 전체 양을 쓰세요.","전체 양 B = ","",Integer.toString(v.total()));
  if(FRACTION.equals(q.skillId)){g.step("문제의 분자를 쓰세요.","분자 n = ","",v.numerator().toString());g.step("문제의 분모를 쓰세요.","분모 d = ","",v.denominator().toString());step(g,"분자를 분모로 나누어 비율을 소수로 쓰세요.","소수 비율 v = n ÷ d = ",v.rate());}
  else if(PERCENT.equals(q.skillId)){step(g,"문제의 백분율을 쓰세요.","백분율 p = ",new BigDecimal(v.displayedRate()));step(g,"백분율을 100으로 나누어 비율을 소수로 쓰세요.","소수 비율 v = p ÷ 100 = ",v.rate());}
  else step(g,"문제의 소수 비율을 쓰세요.","소수 비율 v = ",v.rate());step(g,"전체 양에 소수 비율을 곱해 사용한 양을 구하세요.","사용한 양 A = B × v = ",v.rate().multiply(BigDecimal.valueOf(v.total())));q.studyGuide=g;
 }
 private static void step(StudyGuide g,String instruction,String frame,BigDecimal value){g.step(instruction,frame,"",plain(value));g.frames.get(g.frames.size()-1).inputFormat="decimalValue";}
 private static String plain(BigDecimal v){return v.stripTrailingZeros().toPlainString();}
}
