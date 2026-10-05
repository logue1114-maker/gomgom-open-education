package com.gomgomapps.math.core;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;
import java.io.*;
public class ClockFacesTest {
 @Test public void bothHandsMustMatchAndEveryPictureHasDistinctPublicGeometry(){
  Generator g=new Generator(new Random(20261006671L));List<String> recent=new ArrayList<>();Set<Integer> positions=new HashSet<>();
  for(int i=0;i<300;i++){
   Question q=g.next("el_digital_to_analog",recent,false,GlobalCurriculum.limits("au-acara-v9-primary-v1","el_digital_to_analog",3));assertFalse(recent.contains(q.signature()));recent.add(q.signature());if(recent.size()>100)recent.remove(0);
   String[] parts=q.prompt.split("\\n")[0].split(":");int h=Integer.parseInt(parts[0]),m=Integer.parseInt(parts[1]);assertEquals(4,q.choices.size());assertEquals(4,q.choiceDiagrams.size());assertTrue(q.choiceLabels.isEmpty());assertEquals(""+(h*60+m),q.choices.get(q.correctChoice));positions.add(q.correctChoice);
   Set<String> seen=new HashSet<>();int sameHour=0,sameMinute=0,both=0;
   for(String choice:q.choices){StudyDiagram diagram=q.choiceDiagrams.get(choice);assertEquals("clock",diagram.type);assertEquals(0,diagram.labels.length);assertEquals(2,diagram.values.length);int hh=(int)diagram.values[0],mm=(int)diagram.values[1];assertTrue(hh>=1&&hh<=12&&mm>=0&&mm<60);assertTrue(seen.add(hh+":"+mm));assertEquals(""+(hh*60+mm),choice);if(hh==h)sameHour++;if(mm==m)sameMinute++;if(hh==h&&mm==m)both++;}
   assertEquals(2,sameHour);assertEquals(2,sameMinute);assertEquals(1,both);
  }assertEquals(Set.of(0,1,2,3),positions);
 }
 @Test public void smallDefaultDomainExhaustsThenUsesOldestSuppliedTime(){
  Generator g=new Generator(new Random(20261006672L));List<String> recent=new ArrayList<>();
  for(int i=0;i<144;i++){Question q=g.next("el_digital_to_analog",recent,true);assertFalse(recent.contains(q.signature()));recent.add(q.signature());String[] source=q.prompt.split("\\n")[0].split(":");assertEquals(0,Integer.parseInt(source[1])%5);}
  assertEquals(recent.get(0),g.next("el_digital_to_analog",recent,true).signature());
 }
 @Test public void incorrectPictureIsRejectedAndGuideDoesNotChooseItForStudent(){
  Question q=new Generator(new Random(20261006673L)).next("el_digital_to_analog",List.of(),false);Checker checker=new Checker();
  for(String choice:q.choices)assertEquals(choice.equals(q.answers[0]),checker.check(q,List.of(),List.of(choice)).correct());HelpPlan plan=HelpPlan.forQuestion(q);assertEquals(2,plan.size());assertFalse(plan.canTransfer());
  Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"AU");GlobalCurriculum.choosePack(p,"au-acara-v9-primary-v1");assertTrue(GlobalCurriculum.pack(p).inGrade(q.skillId,3));assertFalse(GlobalCurriculum.pack(p).inGrade(q.skillId,2));assertFalse(Curriculum.inCurriculum(Catalog.get(q.skillId),2015));assertFalse(Curriculum.inCurriculum(Catalog.get(q.skillId),2022));
 }
 @Test public void savedClockOrderAndPicturesRemainAndOldQuestionsCanLackPictures()throws Exception{
  Question q=new Generator(new Random(20261006674L)).next("el_digital_to_analog",List.of(),false);ByteArrayOutputStream bytes=new ByteArrayOutputStream();new ObjectOutputStream(bytes).writeObject(q);Question saved=(Question)new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray())).readObject();assertEquals(q.signature(),saved.signature());assertEquals(q.choices,saved.choices);assertEquals(q.correctChoice,saved.correctChoice);for(String key:q.choices)assertArrayEquals(q.choiceDiagrams.get(key).values,saved.choiceDiagrams.get(key).values,0);
  Question old=new Generator(new Random(20261006675L)).next("el_clock_time",List.of(),false);old.choiceDiagrams=null;assertTrue(new Checker().check(old,List.of(),List.of(old.answers)).correct());
 }
}
