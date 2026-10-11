package com.gomgomapps.math.core;
import org.junit.Test;import static org.junit.Assert.*;import java.util.*;import java.math.*;
public class NamibiaEstimateCheckTest {
 private static final String NA="na-nied-primary-2024-v1";
 private int round(int n,int unit){return BigDecimal.valueOf(n).divide(BigDecimal.valueOf(unit),0,RoundingMode.HALF_UP).intValueExact()*unit;}
 @Test public void boundedAdditionIndexMatchesIndependentEnumerationAtEverySmallCap(){
  int[] units={10,100};int max=5;
  for(int cap=0;cap<=2*max;cap++){
   Set<String> expected=new HashSet<>();
   for(int a=0;a<=max;a++)for(int b=0;b<=max;b++)if(a+b<=cap)for(int unit:units){int value=round(a,unit)+round(b,unit);for(int proposal:new int[]{value,value+unit,value+unit+1})expected.add(EstimateCalculationCheck.make(EstimateCalculationCheck.ADD,a,b,proposal,unit).signature());}
   Set<String> actual=new HashSet<>();long count=LargeEstimateSupply.additionCount(max,cap,units);assertEquals(expected.size(),count);
   for(long i=0;i<count;i++){Question q=LargeEstimateSupply.indexedAddition(max,cap,units,i);assertTrue(actual.add(q.signature()));int[] v=EstimateCalculationCheck.read(q);assertTrue(v[0]+v[1]<=cap);}
   assertEquals(expected,actual);
  }
 }
 @Test public void normalGradeSixSupplyKeepsExactRangeAndBothPlausibilityVerdicts(){
  var pack=GlobalCurriculum.packs("NA").stream().filter(p->p.id.equals(NA)).findFirst().orElseThrow();Generator g=new Generator(new Random(242));
  for(String id:List.of(EstimateCalculationCheck.ADD,EstimateCalculationCheck.SUB)){
   assertTrue(pack.inGrade(id,6));assertFalse(pack.inGrade(id,5));CurriculumLimits limits=GlobalCurriculum.limits(NA,id,6);List<String> recent=new ArrayList<>();Set<Integer> units=new HashSet<>();boolean inside=false,outside=false;
   for(int i=0;i<300;i++){
    Question q=g.next(id,recent,false,limits);assertFalse(recent.contains(q.signature()));recent.add(q.signature());int[] v=EstimateCalculationCheck.read(q);int exact=id.equals(EstimateCalculationCheck.ADD)?v[0]+v[1]:v[0]-v[1];assertTrue(v[0]<=1000000&&v[1]<=1000000&&exact>=0&&exact<=1000000);units.add(v[3]);
    int estimate=id.equals(EstimateCalculationCheck.ADD)?round(v[0],v[3])+round(v[1],v[3]):round(v[0],v[3])-round(v[1],v[3]);boolean in=Math.abs(v[2]-estimate)<=v[3];inside|=in;outside|=!in;q.answers=new String[]{"poison","poison"};q.expression="poison";
    assertTrue(new Checker().check(q,List.of(),List.of(""+estimate,in?"0":"1")).correct());assertFalse(new Checker().check(q,List.of(),List.of(""+(estimate+1),in?"0":"1")).correct());
    HelpPlan help=HelpPlan.forQuestion(q);assertEquals(17,help.size());assertFalse(help.canTransfer());assertTrue(help.step(14).accepts(""+estimate));
   }
   assertEquals(Set.of(10,100,1000,10000,100000),units);assertTrue(inside&&outside);
  }
 }
 @Test public void originalOperandsAndExactResultAreGuardedSeparatelyFromEstimateAndProposal(){
  CurriculumLimits add=GlobalCurriculum.limits(NA,EstimateCalculationCheck.ADD,6),sub=GlobalCurriculum.limits(NA,EstimateCalculationCheck.SUB,6);
  assertTrue(add.allows(EstimateCalculationCheck.make(EstimateCalculationCheck.ADD,1000000,0,1000000,100)));
  assertFalse(add.allows(EstimateCalculationCheck.make(EstimateCalculationCheck.ADD,500001,500000,1000000,100)));
  assertFalse(sub.allows(EstimateCalculationCheck.make(EstimateCalculationCheck.SUB,2000000,1000000,1000000,100)));
  assertTrue(add.allows(EstimateCalculationCheck.make(EstimateCalculationCheck.ADD,2515,485,3000,100)));
  Question approximate=EstimateCalculationCheck.make(EstimateCalculationCheck.ADD,2515,485,3010,100);assertTrue(add.allows(approximate));assertNotEquals(3010,2515+485);assertTrue(new Checker().check(approximate,List.of(),List.of("3000","0")).correct());
  for(String invalid:List.of("maxExactResult=-1","maxExactResult=20000001")){try{new CurriculumLimits(invalid);fail(invalid);}catch(IllegalArgumentException expected){}}
 }
}
