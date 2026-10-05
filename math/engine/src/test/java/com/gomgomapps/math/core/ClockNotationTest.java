package com.gomgomapps.math.core;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;
import java.io.*;
public class ClockNotationTest {
 @Test public void periodChoiceAcceptsOnlyItsAuthoredOptionKey(){
  HelpPlan.Step step=HelpPlan.forQuestion(ClockNotation.create("el_time_24_to_12",0)).step(1);
  assertTrue(step.accepts("AM"));assertFalse(step.accepts("PM"));assertFalse(step.accepts("am"));assertFalse(step.accepts("0"));assertFalse(step.accepts("AM/PM"));assertFalse(step.accepts(""));
 }
 @Test public void everyMinuteOfTheDayConvertsIncludingNoonAndMidnight(){
  Checker checker=new Checker();
  for(int time=0;time<1440;time++){
   int h=time/60,m=time%60;Question forward=ClockNotation.create("el_time_12_to_24",time),reverse=ClockNotation.create("el_time_24_to_12",time);
   String[] display=forward.prompt.split("\\n")[0].split("[: ]");int shown=Integer.parseInt(display[0]);
   int solved=shown%12+(display[2].equals("PM")?12:0);
   assertEquals(h,solved);assertEquals(m,Integer.parseInt(display[1]));assertArrayEquals(new String[]{""+solved,""+m},forward.answers);
   assertTrue(checker.check(forward,List.of(),List.of(String.format(Locale.ROOT,"%02d",h),String.format(Locale.ROOT,"%02d",m))).correct());
   assertEquals(String.format(Locale.ROOT,"%02d:%02d",h,m),reverse.prompt.split("\\n")[0]);assertEquals(""+time,reverse.answers[0]);
   HelpPlan plan=HelpPlan.forQuestion(reverse);assertEquals(3,plan.size());assertTrue(plan.step(0).accepts(""+(h%12==0?12:h%12)));assertTrue(plan.step(1).accepts(h<12?"AM":"PM"));assertFalse(plan.step(1).accepts(h<12?"PM":"AM"));assertTrue(plan.step(2).accepts(""+m));assertFalse(plan.canTransfer());
  }
 }
 @Test public void choicesAreUniqueNaturalTimesAndPositionsVary(){
  Generator g=new Generator(new Random(20261006611L));Set<Integer> positions=new HashSet<>();List<String> recent=new ArrayList<>();
  for(int i=0;i<160;i++){
   Question q=g.next("el_time_24_to_12",recent,false,GlobalCurriculum.limits("au-acara-v9-primary-v1","el_time_24_to_12",5));assertFalse(recent.contains(q.signature()));recent.add(q.signature());
   String[] source=q.prompt.split("\\n")[0].split(":");int time=Integer.parseInt(source[0])*60+Integer.parseInt(source[1]);assertEquals(""+time,q.choices.get(q.correctChoice));assertEquals(4,new HashSet<>(q.choices).size());assertEquals(4,new HashSet<>(q.choiceLabels.values()).size());positions.add(q.correctChoice);
   int am=0;for(Map.Entry<String,String> option:q.choiceLabels.entrySet()){assertTrue(option.getValue().matches("(?:0[1-9]|1[0-2]):[0-5][0-9] (?:AM|PM)"));String[] p=option.getValue().split("[: ]");int actual=(Integer.parseInt(p[0])%12+(p[2].equals("PM")?12:0))*60+Integer.parseInt(p[1]);assertEquals(Integer.parseInt(option.getKey()),actual);assertEquals(time%60,actual%60);if(p[2].equals("AM"))am++;}assertEquals(2,am);
  }assertEquals(Set.of(0,1,2,3),positions);
 }
 @Test public void boundedHistoryAvoidsRepeatingAndWrongPartIsMarked(){
  Generator g=new Generator(new Random(20261006612L));List<String> recent=new ArrayList<>();
  for(int i=0;i<400;i++){Question q=g.next("el_time_12_to_24",recent,false);assertFalse(recent.contains(q.signature()));recent.add(q.signature());if(recent.size()>100)recent.remove(0);assertEquals(0,Integer.parseInt(q.answers[1])%5);}
  Question q=ClockNotation.create("el_time_12_to_24",0);Checker checker=new Checker();assertEquals(0,checker.check(q,List.of(),List.of("12","0")).index);assertEquals(1,checker.check(q,List.of(),List.of("0","12")).index);assertFalse(HelpPlan.forQuestion(q).canTransfer());
  Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"AU");GlobalCurriculum.choosePack(p,"au-acara-v9-primary-v1");assertTrue(GlobalCurriculum.pack(p).inGrade(q.skillId,5));assertFalse(GlobalCurriculum.pack(p).inGrade(q.skillId,3));for(Catalog.Skill s:ClockNotation.SKILLS){assertFalse(Curriculum.inCurriculum(s,2015));assertFalse(Curriculum.inCurriculum(s,2022));}
 }
 @Test public void savedPeriodChoicesAndInputFramesSurviveSerialization()throws Exception{
  Question q=new Generator(new Random(20261006613L)).next("el_time_24_to_12",List.of(),false);ByteArrayOutputStream bytes=new ByteArrayOutputStream();new ObjectOutputStream(bytes).writeObject(q);Question saved=(Question)new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray())).readObject();assertEquals(q.signature(),saved.signature());assertEquals(q.choiceLabels,saved.choiceLabels);assertEquals(q.choices,saved.choices);assertEquals(q.correctChoice,saved.correctChoice);assertEquals(3,HelpPlan.forQuestion(saved).size());assertFalse(HelpPlan.forQuestion(saved).canTransfer());
 }
}
