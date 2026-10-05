package com.gomgomapps.math.core;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;
import java.io.*;
public class DotCollectionsTest {
 @Test public void emptyCollectionBelongsOnlyToTheSelectedCountingRange(){
  CurriculumLimits au=GlobalCurriculum.limits("au-acara-v9-primary-v1","count",0);Generator g=new Generator(new Random(20261006411L));List<String> recent=new ArrayList<>();Set<Integer> values=new HashSet<>();
  for(int i=0;i<21;i++){Question q=g.next("count",recent,true,au);int dots=(int)q.prompt.chars().filter(c->c=='●').count();assertTrue(values.add(dots));recent.add(q.signature());assertEquals(String.valueOf(dots),q.answers[0]);assertTrue(new Checker().check(q,List.of(),List.of(String.valueOf(dots))).correct());if(dots==0){assertNotNull(q.diagram);assertEquals("dotCollection",q.diagram.type);assertArrayEquals(new double[]{0},q.diagram.values,0);}else assertNull(q.diagram);for(String choice:q.choices)assertTrue(Integer.parseInt(choice)>=0&&Integer.parseInt(choice)<=20);}
  assertTrue(values.contains(0));assertTrue(values.contains(20));assertEquals(recent.get(0),g.next("count",recent,false,au).signature());
  recent.clear();for(int i=0;i<9;i++){Question q=g.next("count",recent,true);assertTrue(Integer.parseInt(q.answers[0])>=1);assertNull(q.diagram);assertFalse(q.choices.contains("0"));recent.add(q.signature());}
 }
 @Test public void all381PublicDotPatternsAreUsedBeforeTheOldestRepeat(){
  Generator g=new Generator(new Random(20261006412L));List<String> recent=new ArrayList<>();Set<Integer> counts=new HashSet<>();Set<String> visible=new HashSet<>();int[] positions=new int[4];
  for(int i=0;i<381;i++){
   Question q=g.next("el_subitise",recent,true);assertFalse(recent.contains(q.signature()));assertTrue(visible.add(Arrays.toString(q.diagram.values)));recent.add(q.signature());int count=(q.diagram.values.length-1)/2;counts.add(count);assertEquals(String.valueOf(count),q.answers[0]);assertEquals("dotFlash",q.diagram.type);assertEquals(0,q.diagram.labels.length);assertFalse(q.stepSupport);assertEquals(4,new HashSet<>(q.choices).size());assertEquals(String.valueOf(count),q.choices.get(q.correctChoice));positions[q.correctChoice]++;
   for(int a=1;a<q.diagram.values.length;a+=2){assertTrue(q.diagram.values[a]>=.25&&q.diagram.values[a]<=.75);assertTrue(q.diagram.values[a+1]>=.25&&q.diagram.values[a+1]<=.75);for(int b=a+2;b<q.diagram.values.length;b+=2)assertTrue(Math.hypot(q.diagram.values[a]-q.diagram.values[b],q.diagram.values[a+1]-q.diagram.values[b+1])>=.25);}
   assertTrue(new Checker().check(q,List.of(),List.of(String.valueOf(count))).correct());assertFalse(new Checker().check(q,List.of(),List.of("6")).correct());
  }
  assertEquals(Set.of(1,2,3,4,5),counts);assertEquals(recent.get(0),g.next("el_subitise",recent,false).signature());for(int n:positions)assertTrue(n>60&&n<140);
 }
 @Test public void diagramsAndStudentDraftsCanRestoreWithoutChangingThePattern()throws Exception{
  Question q=new Generator(new Random(16)).next("el_subitise",List.of(),false);ByteArrayOutputStream out=new ByteArrayOutputStream();new ObjectOutputStream(out).writeObject(q);Question saved=(Question)new ObjectInputStream(new ByteArrayInputStream(out.toByteArray())).readObject();assertEquals(q.signature(),saved.signature());assertArrayEquals(q.diagram.values,saved.diagram.values,0);
  assertFalse(Curriculum.inCurriculum(Catalog.get("el_subitise"),2015));assertFalse(Curriculum.inCurriculum(Catalog.get("el_subitise"),2022));Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"AU");GlobalCurriculum.choosePack(p,"au-acara-v9-primary-v1");p.grade=0;assertTrue(GlobalCurriculum.pack(p).inGrade("el_subitise",0));
 }
}
