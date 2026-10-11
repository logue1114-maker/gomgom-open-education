package com.gomgomapps.math.core;
import java.util.*;import java.math.*;import org.junit.Test;import static org.junit.Assert.*;
public class PublicFractionPercentEquivalenceTest {
 private static final Set<Integer> OLD=Set.of(2,4,5,10,20,25,40,50,100,125,200,250,500,1000);
 @Test public void selectedMultiplesAndLegacyFractionsHaveIndependentPublicChecks(){int count=0;Checker c=new Checker();for(int d=1;d<=1000;d++)if(d%10==0||d%25==0||OLD.contains(d))for(int n=0;n<=d;n++)if(n*1000%d==0){
  Question original=FractionPercentEquivalence.make(n,d),q=new Question(original.skillId,original.prompt,"poison","999","999");int thousandths=n*1000/d;
  String decimal=BigDecimal.valueOf(thousandths,3).toPlainString(),percent=BigDecimal.valueOf(thousandths,1).toPlainString();assertTrue(c.check(q,List.of(),List.of(decimal,percent)).correct());
  assertEquals(0,c.check(q,List.of(),List.of(BigDecimal.valueOf(thousandths+1,3).toPlainString(),percent)).index);assertEquals(1,c.check(q,List.of(),List.of(decimal,BigDecimal.valueOf(thousandths+1,1).toPlainString())).index);count++;
 }assertEquals(count,FractionPercentEquivalence.domainSize());assertTrue(count>2345);}
 @Test public void newDenominatorHelpAvoidsNonterminatingUnitWithoutFillingAnswer(){Question q=FractionPercentEquivalence.make(3,30);q.answers=new String[]{"poison","poison"};HelpPlan h=HelpPlan.forQuestion(q);assertEquals("fraction-percent-equivalence-v2",q.studyGuide.teachingVersion);assertEquals(4,h.size());assertFalse(h.canTransfer());for(int i=0;i<4;i++)assertTrue(h.step(i).accepts(List.of("30","3","0.1","10").get(i)));assertFalse(h.step(2).accepts("3/30"));
  assertEquals("fraction-percent-equivalence-v1",FractionPercentEquivalence.make(1,4).studyGuide.teachingVersion);
 }
 @Test public void nonterminatingRatesAreExplicitlyOutsideSelectedDecimalSupply(){assertNull(FractionPercentEquivalence.read(new Question(FractionPercentEquivalence.ID,"1/30를 소수와 백분율로 나타내세요.","","999","999")));try{FractionPercentEquivalence.make(1,30);fail();}catch(IllegalArgumentException expected){}
  Question q=FractionPercentEquivalence.make(3,30);Checker c=new Checker();for(List<String> values:List.of(List.of("3/30","10"),List.of("0.1","10%"),List.of("","10"),List.of("0.1")))assertEquals(Checker.Status.INPUT_NEEDED,c.check(q,List.of(),values).status);
 }
}
