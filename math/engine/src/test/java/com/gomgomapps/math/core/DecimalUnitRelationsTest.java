package com.gomgomapps.math.core;
import java.math.BigDecimal;import java.util.*;import org.junit.Test;import static org.junit.Assert.*;
public class DecimalUnitRelationsTest {
 private static BigDecimal unit(String raw){String[] parts=raw.split("/");return parts.length==1?new BigDecimal(raw):new BigDecimal(parts[0]).divide(new BigDecimal(parts[1]));}
 @Test public void fractionUnitFramesAndInvalidInput(){Question q=new Question(DecimalUnitRelations.ID,"1/1000 × 7 = 1/100 × □","poison","999");HelpPlan h=HelpPlan.forQuestion(q);assertEquals(5,h.size());assertFalse(h.canTransfer());List<String> inputs=List.of("0.001","7","0.007","0.01","0.7");for(int i=0;i<5;i++){assertTrue(h.step(i).accepts(inputs.get(i)));assertEquals("",h.step(i).after);}assertTrue(new Checker().check(q,List.of(),List.of("0.70")).correct());assertEquals(Checker.Status.INPUT_NEEDED,new Checker().check(q,List.of(),List.of("7/10")).status);assertEquals(Checker.Status.INPUT_NEEDED,new Checker().check(new Question(q.skillId,"1/0 × 7 = 0.1 × □","","0"),List.of(),List.of("0")).status);}
 @Test public void allTwentyFourThousandConditionsRepresentTheSameAmount(){
  Map<String,Question> pool=DecimalUnitRelations.candidates();assertEquals(24000,pool.size());Set<String> pairs=new HashSet<>();boolean zero=false,fractionalCount=false;
  for(Question q:pool.values()){String[] parts=q.prompt.split(" [×=] ");BigDecimal u=unit(parts[0]),n=new BigDecimal(parts[1]),v=unit(parts[2]);q.answers=new String[]{"poison"};q.expression="poison";String count=u.multiply(n).divide(v).toPlainString();pairs.add(parts[0]+":"+parts[2]);zero|=n.signum()==0;fractionalCount|=new BigDecimal(count).stripTrailingZeros().scale()>0;assertTrue(new Checker().check(q,List.of(),List.of(count)).correct());assertFalse(new Checker().check(q,List.of(),List.of(new BigDecimal(count).add(BigDecimal.ONE).toPlainString())).correct());assertEquals(5,HelpPlan.forQuestion(q).size());assertFalse(q.stepSupport);}
  assertEquals(24,pairs.size());assertTrue(pairs.contains("1/1000:0.01")&&pairs.contains("0.1:1/100")&&pairs.contains("1/10:1/1000"));assertTrue(zero&&fractionalCount);
 }
 @Test public void restoredHelpUsesVisibleGivensAndNeverTransfers(){
  Question q=new Question(DecimalUnitRelations.ID,"0.001 × 7 = 0.01 × □","poison","999");HelpPlan h=HelpPlan.forQuestion(q);assertEquals(5,h.size());assertFalse(h.canTransfer());List<String> entries=List.of("0.001","7","0.007","0.01","0.7");for(int i=0;i<5;i++){assertTrue(h.step(i).accepts(entries.get(i)));assertEquals("",h.step(i).after);}assertFalse(h.step(4).accepts("7"));
 }
 @Test public void ordinaryGradeMappingGivesFreshHundredAndNoUnreviewedFallback(){
  var pack=GlobalCurriculum.packs("GB").stream().filter(p->p.id.equals("england-primary-2021-v1")).findFirst().orElseThrow();assertTrue(pack.inGrade(DecimalUnitRelations.ID,5));assertFalse(pack.inGrade(DecimalUnitRelations.ID,4));assertFalse(Curriculum.inCurriculum(Catalog.get(DecimalUnitRelations.ID),2022));
  Generator g=new Generator(new Random(84));List<String> recent=new ArrayList<>();for(int i=0;i<100;i++){Question q=g.next(DecimalUnitRelations.ID,recent,false,GlobalCurriculum.limits(pack.id,DecimalUnitRelations.ID,5));assertFalse(recent.contains(q.signature()));recent.add(q.signature());assertEquals(5,HelpPlan.forQuestion(q).size());}
 }
}
