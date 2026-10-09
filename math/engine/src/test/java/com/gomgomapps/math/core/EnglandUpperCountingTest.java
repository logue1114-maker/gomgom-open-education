package com.gomgomapps.math.core;
import org.junit.Test;import static org.junit.Assert.*;import java.util.*;import java.util.regex.*;

public class EnglandUpperCountingTest {
 @Test public void selectedGradesUseAllRequiredMultiplesWithoutAnyStartDrift(){
  for(int grade:new int[]{3,4}){
   var limits=GlobalCurriculum.limits("england-primary-2021-v1","countSkip",grade);
   var expected=grade==3?Map.of(4,1000,8,1000,50,1000,100,1000):Map.of(6,1000,7,1000,9,1000,25,1000,1000,10000);
   assertEquals(expected,limits.countingBounds());
   var pool=CountingSteps.candidates(Catalog.get("countSkip"),limits);assertEquals(grade==3?405:469,pool.size());Set<Integer> steps=new HashSet<>();boolean zero=false,top=false;
   for(Question q:pool.values()){
    Matcher m=Pattern.compile("(\\d+)씩 앞으로 세세요\\.\\n(\\d+) → □").matcher(q.prompt);assertTrue(m.matches());int step=Integer.parseInt(m.group(1)),start=Integer.parseInt(m.group(2)),answer=start+step;
    steps.add(step);assertEquals(0,start%step);assertTrue(answer<=expected.get(step));zero|=start==0;top|=answer==expected.get(step);assertTrue(new Checker().check(q,List.of(),List.of(""+answer)).correct());assertFalse(new Checker().check(q,List.of(),List.of(""+(answer+1))).correct());
    CountingSteps.attach(q);HelpPlan h=HelpPlan.forQuestion(q);assertEquals(3,h.size());assertFalse(h.canTransfer());assertTrue(h.step(0).accepts(""+start));assertTrue(h.step(1).accepts(""+step));assertTrue(h.step(2).accepts(""+answer));assertFalse(h.step(2).before.contains(""+answer));
   }
   assertEquals(expected.keySet(),steps);assertTrue(zero&&top);
   Generator g=new Generator(new Random(113));List<String> recent=new ArrayList<>();
   for(int i=0;i<100;i++){Question q=g.next("countSkip",recent,true,limits);assertFalse(recent.contains(q.signature()));recent.add(q.signature());assertTrue(pool.containsKey(q.signature()));assertTrue(q.choices.isEmpty());}

  }
 }
 @Test public void largerIntervalsKeepOtherGradeStartsAndRejectInvalidDefinitions(){
  assertFalse(GlobalCurriculum.limits("england-primary-2021-v1","countSkip",1).countingAnyStart(10));assertTrue(GlobalCurriculum.limits("england-primary-2021-v1","countSkip",2).countingAnyStart(10));assertTrue(GlobalCurriculum.limits("na-nied-primary-2024-v1","countSkip",3).countingAnyStart(4));
  for(String value:List.of("0:100","1001:10000","1000:999","10:10001","10:100,10:200"))try{new CurriculumLimits("countingBounds="+value);fail(value);}catch(IllegalArgumentException expected){}
 }
}
