package com.gomgomapps.math.core;
import java.math.BigDecimal;import java.util.*;import java.util.regex.*;
/** Reverse contextual representation: the public part/whole fraction as decimal and percent. */
public final class QuantityRatioEquivalence {
 private QuantityRatioEquivalence(){}
 public static final String ID="quantityRatioEquivalence";
 public static final List<Catalog.Skill> SKILLS=List.of(new Catalog.Skill(ID,"사용한 비율을 소수·백분율로",6,1,1,"","quantityRatioEquivalence",100,"fractionDecimalPercent","사용한 양과 전체 양의 비율을 소수·백분율로 나타낸다."));
 private static final int[] TOTALS={10,20,40,50,100};
 public static boolean supports(String id){return ID.equals(id);}
 public record Givens(int total,int part,String unit){}
 public static int domainSize(){return Arrays.stream(TOTALS).map(d->d+1).sum()*2;}
 public static Question make(int total,int part,String unit){
  if(Arrays.stream(TOTALS).noneMatch(d->d==total)||part<0||part>total||!Set.of("m","L").contains(unit))throw new IllegalArgumentException("Selected ratio scope");BigDecimal rate=BigDecimal.valueOf(part).divide(BigDecimal.valueOf(total));String prompt=unit.equals("L")?"물 "+total+" L 중 "+part+" L를 사용했습니다.\n사용한 양의 비율을 소수와 백분율로 나타내세요.":"끈 "+total+" m 중 "+part+" m를 잘랐습니다.\n잘라 낸 양의 비율을 소수와 백분율로 나타내세요.";
  Question q=new Question(ID,prompt,"",plain(rate),plain(rate.movePointRight(2)));q.labels=new String[]{"소수","백분율 (%)"};q.answerFormat="decimalValue";q.decimal=true;q.stepSupport=false;attach(q);return q;
 }
 static Question at(int index){String unit=index%2==0?"m":"L";index/=2;for(int d:TOTALS){if(index<=d)return make(d,index,unit);index-=d+1;}throw new IllegalArgumentException("Index outside supply");}
 static Question next(Random random,CurriculumLimits limits,Map<String,Integer> recent){return IndexedQuestionSupply.choose(domainSize(),QuantityRatioEquivalence::at,random,limits,recent);}
 public static Givens read(Question q){if(q==null||!supports(q.skillId)||q.prompt==null)return null;for(String unit:List.of("m","L")){String pattern=unit.equals("L")?"물 (\\d{1,3}) L 중 (\\d{1,3}) L를 사용했습니다.\\n사용한 양의 비율을 소수와 백분율로 나타내세요\\.":"끈 (\\d{1,3}) m 중 (\\d{1,3}) m를 잘랐습니다.\\n잘라 낸 양의 비율을 소수와 백분율로 나타내세요\\.";Matcher m=Pattern.compile(pattern).matcher(q.prompt);if(m.matches()){int d=Integer.parseInt(m.group(1)),n=Integer.parseInt(m.group(2));if(Arrays.stream(TOTALS).anyMatch(v->v==d)&&n<=d)return new Givens(d,n,unit);}}return null;}
 public static void attach(Question q){Givens v=read(q);if(v==null)return;BigDecimal rate=BigDecimal.valueOf(v.part()).divide(BigDecimal.valueOf(v.total()));StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="quantity-ratio-equivalence-v1";g.step("문제의 전체 양을 쓰세요.","전체 양 B = ","",""+v.total());g.step("문제에서 사용하거나 잘라 낸 양을 쓰세요.","사용한 양 A = ","",""+v.part());g.step("사용한 양을 전체 양으로 나누어 분수의 값을 소수로 쓰세요.","소수 비율 v = A ÷ B = ","",plain(rate));g.step("소수에 100을 곱해 백분율을 구하세요.","백분율 p = v × 100 = ","",plain(rate.movePointRight(2)));for(var frame:g.frames)frame.inputFormat="decimalValue";q.studyGuide=g;}
 private static String plain(BigDecimal x){return x.stripTrailingZeros().toPlainString();}
}
