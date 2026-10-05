package com.gomgomapps.math.core;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;
public class ClockMinuteRangeTest {
 @Test public void australianYearThreeUsesEveryMinuteWhileExistingFiveMinuteClocksStayUnchanged(){
  Generator g=new Generator(new Random(20261006151L));CurriculumLimits au=GlobalCurriculum.limits("au-acara-v9-primary-v1","el_clock_minute",3);assertEquals(1,au.minuteStep());Set<Integer> minutes=new HashSet<>();List<String> recent=new ArrayList<>();
  for(int i=0;i<1000;i++){
   Question q=g.next("el_clock_minute",recent,true,au);recent.add(q.signature());int minute=(int)q.diagram.values[1];assertTrue(minute>=0&&minute<60);minutes.add(minute);assertEquals(String.valueOf(minute),q.answers[0]);assertEquals(q.answers[0],q.choices.get(q.correctChoice));
   assertEquals(4,q.choices.size());assertTrue(q.choices.stream().allMatch(v->v.length()==q.answers[0].length()));assertTrue(new Checker().check(q,List.of(),List.of(String.valueOf(minute))).correct());HelpPlan p=HelpPlan.forQuestion(q);assertNotNull(p);assertTrue(p.step(0).accepts(String.valueOf(minute)));assertFalse(p.canTransfer());
  }
  assertEquals(60,minutes.size());
  for(int i=0;i<200;i++){Question q=g.next("el_clock_minute",List.of(),false);assertEquals(0,(int)q.diagram.values[1]%5);Question h=g.next("el_clock_hour",List.of(),false,GlobalCurriculum.limits("au-acara-v9-primary-v1","el_clock_hour",3));assertEquals(String.valueOf((int)h.diagram.values[0]),h.answers[0]);}
 }
 @Test public void australianYearTwoDoesNotInheritYearThreeMinuteResolution(){
  CurriculumLimits earlier=GlobalCurriculum.limits("au-acara-v9-primary-v1","el_clock_hour",2);
  assertEquals(5,earlier.minuteStep());
  assertEquals(1,GlobalCurriculum.limits("au-acara-v9-primary-v1","el_clock_hour",3).minuteStep());
  Generator g=new Generator(new Random(20261006201L));
  for(int i=0;i<200;i++)assertEquals(0,(int)g.next("el_clock_hour",List.of(),false,earlier).diagram.values[1]%5);
 }
 @Test public void unsupportedMinuteStepsFailRatherThanInventAnUnevenClock(){for(int step:List.of(0,2,7,60))try{new CurriculumLimits("minuteStep="+step);fail();}catch(IllegalArgumentException expected){}}
}
