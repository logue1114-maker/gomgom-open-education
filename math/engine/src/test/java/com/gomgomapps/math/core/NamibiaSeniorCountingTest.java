package com.gomgomapps.math.core;
import org.junit.Test;import java.util.*;import java.util.regex.*;import static org.junit.Assert.*;
public class NamibiaSeniorCountingTest {
 private static final String NA="na-nied-primary-2024-v1";
 @Test public void savedRecentConditionsOfferEveryIntervalBeforeReusingOne(){
  for(String id:List.of("countSkip","countBackward"))for(int seed:List.of(1,44,91)){
   CurriculumLimits limits=GlobalCurriculum.limits(NA,id,6);LinkedHashMap<String,Integer> history=new LinkedHashMap<>();Random random=new Random(seed);
   for(int round=0;round<12;round++){
    Set<Integer> steps=new HashSet<>();for(int n=0;n<9;n++){
     Question q=CountingSteps.next(Catalog.get(id),random,limits,history);Matcher m=Pattern.compile("^(\\d+)씩").matcher(q.prompt);assertTrue(m.find());assertTrue("Repeated interval before the full set",steps.add(Integer.parseInt(m.group(1))));assertFalse(history.containsKey(q.signature()));history.put(q.signature(),round*9+n);
    }
    assertEquals(Set.of(10,100,1000,10000,100000,15,30,45,60),steps);
   }
  }
 }
 @Test public void seniorSupplyUsesPublicDirectionAndAuthorizedStartingNumbers(){
  for(String id:List.of("countSkip","countBackward")){
   CurriculumLimits limits=GlobalCurriculum.limits(NA,id,6);Set<String> seen=new HashSet<>();Random random=new Random(91);
   for(int i=0;i<250;i++){
    Question q=CountingSteps.next(Catalog.get(id),random,limits,new LinkedHashMap<>(seen.stream().collect(java.util.stream.Collectors.toMap(s->s,s->1))));assertTrue(seen.add(q.signature()));
    Matcher m=Pattern.compile("(\\d+)씩 (앞으로|거꾸로) 세세요\\.\\n(\\d+) → □").matcher(q.prompt);assertTrue(m.matches());int step=Integer.parseInt(m.group(1)),start=Integer.parseInt(m.group(3));
    assertTrue(Set.of(10,100,1000,10000,100000,15,30,45,60).contains(step));if(Set.of(15,30,45,60).contains(step))assertEquals(0,start%step);
    int answer=id.equals("countBackward")?start-step:start+step;assertTrue(start>=0&&start<=1000000&&answer>=0&&answer<=1000000);
    assertTrue(new Checker().check(q,List.of(),List.of(""+answer)).correct());HelpPlan help=HelpPlan.forQuestion(q);assertFalse(help.canTransfer());assertEquals(3,help.size());assertTrue(help.step(0).accepts(""+start));assertTrue(help.step(1).accepts(""+step));assertTrue(help.step(2).accepts(""+answer));
   }
  }
 }
 @Test public void everyStepIncludesBothWholeNumberEndpointsWithoutLargePools(){
  for(int step:List.of(10,100,1000,10000,100000,15,30,45,60))for(String id:List.of("countSkip","countBackward")){
   boolean any=Set.of(10,100,1000,10000,100000).contains(step),back=id.equals("countBackward");CurriculumLimits limits=new CurriculumLimits("wholeMaximum=1000000;maxGiven=1000000;maxResult=1000000;countingBounds="+step+":1000000"+(any?";countingAnyStart=true":""));
   Question first=CountingSteps.next(Catalog.get(id),new Random(){public int nextInt(int bound){return 0;}},limits,Map.of());assertEquals(back?"0":""+step,first.answers[0]);
   Question last=CountingSteps.next(Catalog.get(id),new Random(){public int nextInt(int bound){return bound-1;}},limits,Map.of());int start=back?(any?1000000:(1000000/step)*step):(any?1000000-step:((1000000-step)/step)*step);assertTrue(last.prompt.contains(start+" → □"));assertEquals(""+(back?start-step:start+step),last.answers[0]);
  }
 }
}
