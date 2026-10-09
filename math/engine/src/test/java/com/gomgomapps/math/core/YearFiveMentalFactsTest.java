package com.gomgomapps.math.core;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

/** Exercise Year5 through the same curriculum and generator used by topic practice. */
public class YearFiveMentalFactsTest {
 @Test public void yearFiveOffersBothOperationsWithoutChangingEarlierPlacement(){
  Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"GB");GlobalCurriculum.choosePack(p,"england-primary-2021-v1");
  GlobalCurriculum.Pack pack=GlobalCurriculum.pack(p);
  for(String id:List.of(FactorPairMental.MENTAL,DoublingCalculation.MULTIPLY,DoublingCalculation.DIVIDE)){
   assertTrue(pack.inGrade(id,5));
   for(int grade=1;grade<=6;grade++)assertEquals(grade==5||grade==(id.equals(FactorPairMental.MENTAL)?4:3),pack.inGrade(id,grade));
  }
  assertFalse(pack.inGrade(FactorPairMental.PAIRS,5));
 }
 @Test public void gradeFiveFreshSupplyChecksPublicOperandsAndKeepsHelpBlank(){
  Generator generator=new Generator(new Random(167));Checker checker=new Checker();
  for(String id:List.of(FactorPairMental.MENTAL,DoublingCalculation.MULTIPLY,DoublingCalculation.DIVIDE)){
   CurriculumLimits limits=GlobalCurriculum.limits("england-primary-2021-v1",id,5);List<String> recent=new ArrayList<>();
   for(int i=0;i<100;i++){
    Question q=generator.next(id,recent,false,limits);assertTrue(limits.allows(q));assertFalse(recent.contains(q.signature()));recent.add(q.signature());assertTrue(q.choices.isEmpty());
    int expected=id.equals(FactorPairMental.MENTAL)?FactorPairMental.read(q)[6]:DoublingCalculation.read(q)[2];
    q.answers=new String[]{"poison"};q.expression="poison";
    assertTrue(checker.check(q,List.of(),List.of(""+expected)).correct());
    assertEquals(Checker.Status.WRONG_ANSWER,checker.check(q,List.of(),List.of(""+(expected+1))).status);
    HelpPlan h=HelpPlan.forQuestion(q);assertNotNull(h);assertFalse(h.canTransfer());assertTrue(h.restore(null,q.id).entries.stream().allMatch(String::isEmpty));
   }
  }
 }
}
