package com.gomgomapps.math.core;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;
import java.io.*;
public class ClockReadingsTest {
 @Test public void selectedYear2AndYear3UseTheirOwnMinuteResolution(){
  for(int grade:List.of(2,3))for(String id:List.of("el_clock_time","el_clock_digital_match")){
   CurriculumLimits limits=GlobalCurriculum.limits("au-acara-v9-primary-v1",id,grade);int size=grade==2?48:720;Generator g=new Generator(new Random(20261006511L));List<String> recent=new ArrayList<>();Set<Integer> minutes=new HashSet<>();
   for(int i=0;i<size;i++){Question q=g.next(id,recent,true,limits);assertFalse(recent.contains(q.signature()));recent.add(q.signature());int hour=(int)q.diagram.values[0],minute=(int)q.diagram.values[1];assertTrue(hour>=1&&hour<=12);assertTrue(minute>=0&&minute<60);minutes.add(minute);if(grade==2)assertEquals(0,minute%15);
    if(id.equals("el_clock_time")){assertArrayEquals(new String[]{String.valueOf(hour),String.valueOf(minute)},q.answers);assertArrayEquals(new String[]{"시","분"},q.labels);assertTrue(q.choices.isEmpty());assertTrue(new Checker().check(q,List.of(),List.of(String.valueOf(hour),String.format(Locale.ROOT,"%02d",minute))).correct());}
    else{assertEquals(4,new HashSet<>(q.choices).size());String expected=String.format(Locale.ROOT,"%02d:%02d",hour,minute);assertEquals(expected,q.choiceLabels.get(q.choices.get(q.correctChoice)));assertEquals(4,new HashSet<>(q.choiceLabels.values()).size());for(String display:q.choiceLabels.values())assertTrue(display.matches("(?:0[1-9]|1[0-2]):[0-5][0-9]"));}
   }assertEquals(grade==2?Set.of(0,15,30,45):new HashSet<>(java.util.stream.IntStream.range(0,60).boxed().toList()),minutes);assertEquals(recent.get(0),g.next(id,recent,true,limits).signature());
  }
 }
 @Test public void twoInputsMarkOnlyTheWrongPartAndGuidesDoNotTransferAnswers(){
  Question q=new Generator(new Random(19)).next("el_clock_time",List.of(),false);Checker checker=new Checker();assertEquals(1,checker.check(q,List.of(),List.of(q.answers[0],"99")).index);assertEquals(0,checker.check(q,List.of(),List.of("99",q.answers[1])).index);HelpPlan plan=HelpPlan.forQuestion(q);assertEquals(2,plan.size());assertTrue(plan.step(0).accepts(q.answers[0]));assertTrue(plan.step(1).accepts(q.answers[1]));assertFalse(plan.canTransfer());
 }
 @Test public void defaultClockStepsAndKoreanDiagnosisRemainUnchanged(){
  Generator g=new Generator(new Random(9));for(int i=0;i<200;i++){Question q=g.next("el_clock_time",List.of(),false);assertEquals(0,(int)q.diagram.values[1]%5);}for(Catalog.Skill skill:ClockReadings.SKILLS){assertFalse(Curriculum.inCurriculum(skill,2015));assertFalse(Curriculum.inCurriculum(skill,2022));}assertEquals(5,GlobalCurriculum.limits("default","el_clock_hour",3).minuteStep());assertEquals(5,GlobalCurriculum.limits("au-acara-v9-primary-v1","el_clock_hour",2).minuteStep());
 }
 @Test public void savedQuestionRetainsBothAnswerLabelsAndHandGeometry()throws Exception{
  Question q=new Generator(new Random(23)).next("el_clock_time",List.of(),false);ByteArrayOutputStream out=new ByteArrayOutputStream();new ObjectOutputStream(out).writeObject(q);Question saved=(Question)new ObjectInputStream(new ByteArrayInputStream(out.toByteArray())).readObject();assertEquals(q.signature(),saved.signature());assertArrayEquals(q.answers,saved.answers);assertArrayEquals(q.labels,saved.labels);assertFalse(HelpPlan.forQuestion(saved).canTransfer());
 }
}
