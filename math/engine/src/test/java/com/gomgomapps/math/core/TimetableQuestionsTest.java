package com.gomgomapps.math.core;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;
import java.io.*;
public class TimetableQuestionsTest {
 static final String PACK="au-acara-v9-primary-v1";
 @Test public void juneJourneysHandleMidnightExactHoursAndBothDateEdges(){
  for(int day:List.of(1,27))for(int start:List.of(0,1,60,720,1380,1439))for(int duration:List.of(1,30,60,90,240))for(String id:List.of("el_time_add","el_time_difference","el_trip_departure")){
   Question q=TimetableQuestions.create(id,day,start,duration);int end=start+duration;String[] expected=id.equals("el_time_difference")?new String[]{""+(duration/60),""+(duration%60)}:id.equals("el_time_add")?new String[]{""+(day+end/1440),""+(end%1440/60),""+(end%60)}:new String[]{""+day,""+(start/60),""+(start%60)};
   assertArrayEquals(expected,q.answers);assertTrue(new Checker().check(q,List.of(),List.of(expected)).correct());assertTrue(q.choices.isEmpty());assertEquals("pair",q.kind);assertFalse(HelpPlan.forQuestion(q).canTransfer());
   for(StudyGuide.Frame frame:q.studyGuide.frames){assertTrue(HelpPlan.forQuestion(q).step(q.studyGuide.frames.indexOf(frame)).accepts(frame.expected));assertTrue(Integer.parseInt(frame.expected)>=0);}
  }
 }
 @Test public void selectedYear6ReusesExistingTypesWithoutChangingEarlierOrKoreanWork(){
  Generator g=new Generator(new Random(20261006711L));for(String id:List.of("el_time_add","el_time_difference")){
   assertFalse(CurriculumLimits.NONE.timetables());assertFalse(GlobalCurriculum.limits(PACK,id,5).timetables());assertTrue(GlobalCurriculum.limits(PACK,id,6).timetables());
   for(int i=0;i<120;i++){Question old=g.next(id,List.of(),false);assertFalse(old.prompt.startsWith("시간표"));assertEquals(id.equals("el_time_add")?2:1,old.answers.length);Question current=g.next(id,List.of(),false,GlobalCurriculum.limits(PACK,id,6));assertTrue(current.prompt.startsWith("시간표"));assertNotNull(current.studyGuide);}
  }
  assertEquals("시간표 — 도착 시각",GlobalCurriculum.title(PACK,"el_time_add",6));assertEquals("시간표 — 걸린 시간",GlobalCurriculum.title(PACK,"el_time_difference",6));assertEquals(Catalog.get("el_time_add").title,GlobalCurriculum.title(PACK,"el_time_add",5));assertFalse(Curriculum.inCurriculum(Catalog.get("el_trip_departure"),2015));assertFalse(Curriculum.inCurriculum(Catalog.get("el_trip_departure"),2022));
  Learning.Session saved=new Learning.Session();saved.educationSystem=PACK;saved.curriculumGrade=6;saved.question=g.next("el_time_add",List.of(),false);assertEquals(Catalog.get("el_time_add").title,GlobalCurriculum.title(saved,"el_time_add"));saved.question=TimetableQuestions.create("el_time_add",1,1430,20);assertEquals("시간표 — 도착 시각",GlobalCurriculum.title(saved,"el_time_add"));
 }
 @Test public void dayHourAndMinuteErrorsMarkOnlyTheirOwnField(){
  Question q=TimetableQuestions.create("el_time_add",27,1430,20);Checker checker=new Checker();assertArrayEquals(new String[]{"28","0","10"},q.answers);assertEquals(0,checker.check(q,List.of(),List.of("27","0","10")).index);assertEquals(1,checker.check(q,List.of(),List.of("28","24","10")).index);assertEquals(2,checker.check(q,List.of(),List.of("28","0","20")).index);
 }
 @Test public void largePracticeSpaceDoesNotRepeatRecentQuestionsAndVariesMidnight(){
  Generator g=new Generator(new Random(20261006712L));for(String id:List.of("el_time_add","el_time_difference","el_trip_departure")){
   List<String> recent=new ArrayList<>();int nextDay=0;Set<Integer> days=new HashSet<>();
   for(int i=0;i<400;i++){Question q=g.next(id,recent,false,GlobalCurriculum.limits(PACK,id,6));assertFalse(recent.contains(q.signature()));recent.add(q.signature());if(recent.size()>100)recent.remove(0);if(id.equals("el_time_difference")){String[] lines=q.prompt.split("\\n");if(!lines[2].split("일")[0].equals(lines[4].split("일")[0]))nextDay++;}else if(id.equals("el_time_add")){String[] lines=q.prompt.split("\\n");int departureDay=Integer.parseInt(lines[2].split(" ")[1].replace("일",""));if(Integer.parseInt(q.answers[0])!=departureDay)nextDay++;days.add(Integer.parseInt(q.answers[0]));}}
   if(!id.equals("el_trip_departure"))assertTrue(nextDay>10);if(id.equals("el_time_add"))assertTrue(days.size()>20);
  }
 }
 @Test public void savedTripRetainsDatesInputLabelsAndGuide(){
  try{Question q=TimetableQuestions.create("el_trip_departure",1,1439,1);ByteArrayOutputStream out=new ByteArrayOutputStream();new ObjectOutputStream(out).writeObject(q);Question saved=(Question)new ObjectInputStream(new ByteArrayInputStream(out.toByteArray())).readObject();assertEquals(q.signature(),saved.signature());assertArrayEquals(q.answers,saved.answers);assertArrayEquals(q.labels,saved.labels);assertFalse(HelpPlan.forQuestion(saved).canTransfer());}catch(Exception error){throw new AssertionError(error);}
 }
}
