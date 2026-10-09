package com.gomgomapps.math.core;
import java.math.*;import java.util.*;import org.junit.Test;import static org.junit.Assert.*;
public class EnglandDecimalRoundingComparisonTest {
 @Test public void finiteTenthsRoundToWholeWithAllTiesAndPublicOnlyStudentHelp(){
  CurriculumLimits limits=GlobalCurriculum.limits("england-primary-2021-v1","el_decimal_round",4);var pool=DecimalRoundingSupply.candidates(limits);assertEquals(1000,pool.size());
  for(Question q:pool.values()){
   String raw=q.prompt.split("을")[0];BigDecimal x=new BigDecimal(raw),base=x.setScale(0,RoundingMode.DOWN),answer=x.setScale(0,RoundingMode.HALF_UP);int next=x.movePointRight(1).intValueExact()%10;assertTrue(new Checker().check(q,List.of(),List.of(answer.toPlainString())).correct());assertFalse(new Checker().check(q,List.of(),List.of(answer.add(BigDecimal.ONE).toPlainString())).correct());
   Question saved=new Question(q.skillId,q.prompt,"poison","999");HelpPlan help=HelpPlan.forQuestion(saved);assertFalse(help.canTransfer());List<String> values=List.of(raw,"1",""+next,next>=5?"raise":"keep",base.toPlainString(),next>=5?"1":"0",answer.toPlainString());assertEquals(7,help.size());for(int i=0;i<7;i++){assertTrue(q.prompt+" step"+i,help.step(i).accepts(values.get(i)));assertFalse(help.step(i).accepts(i==3?(next>=5?"keep":"raise"):"999"));assertEquals("",help.step(i).after);assertFalse(help.step(i).before.matches(".*[0-9].*"));}
  }
  assertTrue(pool.values().stream().anyMatch(q->q.prompt.startsWith("99.9")&&q.answers[0].equals("100")));List<String> recent=new ArrayList<>();Generator g=new Generator(new Random(81));for(int i=0;i<100;i++){Question q=g.next("el_decimal_round",recent,false,limits);assertFalse(recent.contains(q.signature()));recent.add(q.signature());}for(Question q:pool.values())recent.add(q.signature());assertTrue(recent.contains(g.next("el_decimal_round",recent,false,limits).signature()));
 }
 @Test public void comparisonKeepsSameDisplayedPrecisionZerosAndAllSignsWithoutRepeatedHundred(){
  CurriculumLimits limits=GlobalCurriculum.limits("england-primary-2021-v1","el_decimal_compare",4);Generator g=new Generator(new Random(810));List<String> recent=new ArrayList<>();Set<String> signs=new HashSet<>();Set<Integer> places=new HashSet<>();boolean zero=false,trailingZero=false,sameWhole=false;
  for(int i=0;i<600;i++){Question q=g.next("el_decimal_compare",recent,false,limits);assertFalse(recent.contains(q.signature()));recent.add(q.signature());String[] raw=q.prompt.split("\\s+□\\s+");BigDecimal a=new BigDecimal(raw[0]),b=new BigDecimal(raw[1]);assertEquals(a.scale(),b.scale());assertTrue(a.scale()==1||a.scale()==2);assertTrue(a.compareTo(new BigDecimal("100"))<0&&b.compareTo(new BigDecimal("100"))<0);places.add(a.scale());String sign=a.compareTo(b)<0?"<":a.compareTo(b)>0?">":"=";signs.add(sign);assertTrue(new Checker().check(q,List.of(),List.of(sign)).correct());zero|=a.signum()==0||b.signum()==0;trailingZero|=raw[0].endsWith("0")||raw[1].endsWith("0");sameWhole|=a.intValue()==b.intValue()&&!sign.equals("=");HelpPlan help=HelpPlan.forQuestion(q);assertFalse(help.canTransfer());assertTrue(help.step(help.size()-1).accepts(sign));}
  assertEquals(Set.of("<","=",">"),signs);assertEquals(Set.of(1,2),places);assertTrue(trailingZero&&sameWhole); // Zero boundary is checked explicitly rather than depending on a random draw.
  assertTrue(limits.allows(new Question("el_decimal_compare","0.00  □  0.01","","<")));assertFalse(limits.allows(new Question("el_decimal_compare","1.0  □  1.00","","=")));
 }
 @Test public void twoYearFourLinksAndOtherCountryPrecisionRemainIntact(){
  var pack=GlobalCurriculum.packs("GB").stream().filter(p->p.id.equals("england-primary-2021-v1")).findFirst().orElseThrow();for(String id:List.of("el_decimal_round","el_decimal_compare")){assertTrue(pack.inGrade(id,4));assertFalse(pack.inGrade(id,3));}
  Generator g=new Generator(new Random(811));for(int i=0;i<100;i++){Question q=g.next("el_decimal_round",List.of(),false,GlobalCurriculum.limits("ke-kicd-cbc-2024-v1","el_decimal_round",6));assertTrue(q.prompt.contains("소수 "));assertTrue(new BigDecimal(q.prompt.split("을")[0]).scale()<=4);assertNotNull(RoundingRelations.read(q));}
  for(String bad:List.of("roundingDecimalPlaces=-1","roundingDecimalPlaces=4","sameDecimalPlaces=false")){try{new CurriculumLimits(bad);fail(bad);}catch(IllegalArgumentException expected){}}
 }
}
