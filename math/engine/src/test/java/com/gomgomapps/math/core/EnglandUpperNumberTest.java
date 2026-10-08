package com.gomgomapps.math.core;
import java.math.BigInteger;import java.util.*;import org.junit.Test;import static org.junit.Assert.*;
public class EnglandUpperNumberTest {
 private static final String PACK="england-primary-2021-v1";
 @Test public void largeResultLimitUsesExactDecimalText(){
  CurriculumLimits limits=new CurriculumLimits("maxResult=10000000;nonnegative=true");
  assertTrue(limits.allows(new Question("el_round","1을 10의 자리까지 반올림하면?","1","10000000")));
  assertFalse(limits.allows(new Question("el_round","1을 10의 자리까지 반올림하면?","1","10000001")));
  assertFalse(limits.allows(new Question("el_round","1을 10의 자리까지 반올림하면?","1","-1")));
 }
 @Test public void everySelectedGradeHasHundredFreshVisibleOrderAndCompareProblems(){
  for(int grade=3;grade<=6;grade++)for(String id:List.of("numberOrder","el_compare_10000")){
   int maximum=new int[]{1000,9999,1000000,10000000}[grade-3];Generator g=new Generator(new Random(67+grade));List<String> recent=new ArrayList<>();boolean aboveOldCap=false;
   for(int i=0;i<100;i++){Question q=g.next(id,recent,false,GlobalCurriculum.limits(PACK,id,grade));assertFalse(recent.contains(q.signature()));recent.add(q.signature());assertFalse(HelpPlan.forQuestion(q).canTransfer());
    if(id.equals("numberOrder")){int[] shown=Arrays.stream(q.diagram.values).limit(3).mapToInt(v->(int)v).toArray(),sorted=shown.clone();Arrays.sort(sorted);for(int n:shown){assertTrue(n>=0&&n<=maximum);aboveOldCap|=n>500;}if(q.diagram.values[3]==1){int swap=sorted[0];sorted[0]=sorted[2];sorted[2]=swap;}List<String> answers=new ArrayList<>();for(int n:sorted)answers.add(String.valueOf(n));assertTrue(new Checker().check(q,List.of(),answers).correct());Collections.swap(answers,0,1);assertFalse(new Checker().check(q,List.of(),answers).correct());}
    else {String[] shown=q.prompt.split("  □  ");int a=Integer.parseInt(shown[0]),b=Integer.parseInt(shown[1]);assertTrue(a>=0&&a<=maximum&&b>=0&&b<=maximum);aboveOldCap|=a>500||b>500;String answer=a<b?"<":a>b?">":"=";assertTrue(new Checker().check(q,List.of(),List.of(answer)).correct());}
   }assertTrue(aboveOldCap);
  }
 }
 @Test public void bigIndexesDecodeBoundariesWithoutOverflowOrDuplicateOperands(){
  for(int maximum:new int[]{1000,9999,1000000,10000000}){BigInteger size=LargeOrderingSupply.count(maximum);for(BigInteger index:List.of(BigInteger.ZERO,BigInteger.ONE,size.subtract(BigInteger.TWO),size.subtract(BigInteger.ONE))){int[] v=LargeOrderingSupply.at(maximum,index);for(int i=0;i<3;i++)assertTrue(v[i]>=0&&v[i]<=maximum);assertTrue(v[0]!=v[1]&&v[0]!=v[2]&&v[1]!=v[2]);}assertEquals(maximum,LargeOrderingSupply.at(maximum,size.subtract(BigInteger.ONE))[0]);}
  assertTrue(LargeOrderingSupply.count(10000000).compareTo(BigInteger.valueOf(Long.MAX_VALUE))>0);
 }
 @Test public void upperRoundingIncludesSmallValuesAndKeepsRequiredUnitRanges(){
  for(int grade=4;grade<=6;grade++){Generator g=new Generator(new Random(70+grade));CurriculumLimits limits=GlobalCurriculum.limits(PACK,"el_round",grade);List<String> recent=new ArrayList<>();Set<Integer> units=new HashSet<>();boolean small=false;
   Question sample=ElementaryBasics.create(Catalog.get("el_round"),new Random(70+grade),limits);assertTrue("Year"+grade+": "+sample.prompt+" / "+sample.expression,limits.allows(sample));
   for(int i=0;i<100;i++){Question q=g.next("el_round",recent,false,limits);assertFalse(recent.contains(q.signature()));recent.add(q.signature());RoundingRelations.Givens v=RoundingRelations.read(q);assertNotNull(v);int number=Integer.parseInt(v.first()),unit=v.unit().intValueExact();units.add(unit);small|=number<unit*2;int answer=((number+unit/2)/unit)*unit;assertTrue(new Checker().check(q,List.of(),List.of(String.valueOf(answer))).correct());assertFalse(new Checker().check(q,List.of(),List.of(String.valueOf(answer+unit))).correct());assertFalse(HelpPlan.forQuestion(q).canTransfer());}
   assertTrue(small);assertEquals(limits.roundingUnits().length,units.size());
  }
  // Explicit zero/max-boundary generation; public givens, not a forced learner state.
  for(int value:new int[]{0,4,5,9999}){Random boundaryRandom=new Random(){@Override public int nextInt(int bound){return bound==10000?value:0;}};Question q=new Generator(boundaryRandom).next("el_round",List.of(),false,GlobalCurriculum.limits(PACK,"el_round",4));assertTrue(new Checker().check(q,List.of(),List.of(String.valueOf(((value+5)/10)*10))).correct());}
 }
}
