package com.gomgomapps.math.core;
import java.math.BigDecimal;import java.util.*;import org.junit.Test;import static org.junit.Assert.*;
public class YearFivePowerTenTest {
 @Test public void completeSelectedDomainsAndEarlierBounds(){
  for(String id:List.of(DecimalPowerTen.MUL,DecimalPowerTen.DIV)){
   var limits=GlobalCurriculum.limits("england-primary-2021-v1",id,5);assertEquals(300000,DecimalPowerTen.count(id,limits));assertTrue(DecimalPowerTen.at(id,1000,limits).prompt.startsWith("1 "));
   for(int i=0;i<300000;i++){Question q=DecimalPowerTen.at(id,i,limits);String[] givens=q.prompt.split(" ");BigDecimal x=new BigDecimal(givens[0]),power=new BigDecimal(givens[2]),expected=id.equals(DecimalPowerTen.MUL)?x.multiply(power):x.divide(power);assertEquals(expected.stripTrailingZeros().toPlainString(),q.answers[0]);assertTrue(limits.allows(q));assertTrue(new Checker().check(q,List.of(),List.of(expected.toPlainString())).correct());}
  }
  assertEquals(11100,DecimalPowerTen.count(DecimalPowerTen.DIV,GlobalCurriculum.limits("england-primary-2021-v1",DecimalPowerTen.DIV,6)));
  assertFalse(new CurriculumLimits("answerDecimalPlaces=3").allows(DecimalPowerTen.make(DecimalPowerTen.DIV,"0.001",1000)));
  for(String invalid:List.of("answerDecimalPlaces=7","secondDecimalPlaces=5")){try{new CurriculumLimits(invalid);fail();}catch(IllegalArgumentException expected){}}
 }
 @Test public void publicOperandsDriveCheckAndBlankHelp(){
  Question q=new Question(DecimalPowerTen.DIV,"0.001 ÷ 1000","poison","999");Checker c=new Checker();
  assertTrue(c.check(q,List.of(),List.of("0.000001")).correct());assertEquals(Checker.Status.WRONG_ANSWER,c.check(q,List.of(),List.of("999")).status);
  for(String input:List.of("","1/1000000","0.000001+0","abc"))assertEquals(Checker.Status.INPUT_NEEDED,c.check(q,List.of(),List.of(input)).status);
  HelpPlan h=HelpPlan.forQuestion(q);assertEquals(7,h.size());assertFalse(h.canTransfer());var entries=List.of("0.001","1000","3","1000","1","0.001","0.000001");for(int i=0;i<h.size();i++){assertTrue(h.step(i).accepts(entries.get(i)));assertFalse(h.step(i).accepts(""));}
  for(String prompt:List.of("0.001 × 1000","0.0001 ÷ 1000","100 ÷ 1000")){Question bad=new Question(DecimalPowerTen.DIV,prompt,"1","1");assertEquals(Checker.Status.INPUT_NEEDED,c.check(bad,List.of(),List.of("1")).status);}
 }
 @Test public void normalGeneratorHasFreshGradeFiveQuestions(){
  var pack=GlobalCurriculum.packs("GB").stream().filter(p->p.id.equals("england-primary-2021-v1")).findFirst().orElseThrow();
  for(String id:List.of(DecimalPowerTen.MUL,DecimalPowerTen.DIV)){assertTrue(pack.inGrade(id,5));assertTrue(pack.inGrade(id,6));Generator generator=new Generator(new Random(170));List<String> recent=new ArrayList<>();Set<String> powers=new HashSet<>();boolean fractional=false;
   for(int i=0;i<150;i++){Question q=generator.next(id,recent,false,GlobalCurriculum.limits(pack.id,id,5));assertFalse(recent.contains(q.signature()));recent.add(q.signature());powers.add(q.prompt.split(" ")[2]);fractional|=new BigDecimal(q.prompt.split(" ")[0]).stripTrailingZeros().scale()>0;assertFalse(HelpPlan.forQuestion(q).canTransfer());}
   assertEquals(Set.of("10","100","1000"),powers);assertTrue(fractional);
  }
 }
}
