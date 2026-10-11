package com.gomgomapps.math.core;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;
public class NamibiaPicturePlacementTest {
 @Test public void gradeOneDrawnOperationsAreMappedAndFreshAcrossTheFullDomain(){
  Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"NA");GlobalCurriculum.choosePack(p,"na-nied-primary-2024-v1");
  assertTrue(GlobalCurriculum.pack(p).inGrade(PictureArithmetic.ID,1));
  assertFalse(GlobalCurriculum.pack(p).inGrade(PictureArithmetic.ID,2));
  assertTrue(GlobalCurriculum.available(p).stream().anyMatch(s->s.id.equals(PictureArithmetic.ID)));
  CurriculumLimits limits=GlobalCurriculum.limits(p.educationSystem,PictureArithmetic.ID,1);
  Generator g=new Generator(new Random(224));Checker checker=new Checker();List<String> recent=new ArrayList<>();Set<Integer> operations=new HashSet<>(),answers=new HashSet<>();
  for(int i=0;i<462;i++){
   Question q=g.next(PictureArithmetic.ID,recent,false,limits);assertFalse(recent.contains(q.signature()));recent.add(q.signature());
   int[] v=PictureArithmetic.read(q);assertNotNull(v);assertTrue(limits.allows(q));assertTrue(v[0]>=0&&v[0]<=20&&v[1]>=0&&v[1]<=20);operations.add(v[2]);
   int expected=v[2]==1?v[0]+v[1]:v[0]-v[1];assertTrue(expected>=0&&expected<=20);answers.add(expected);
   q.answers=new String[]{"poison"};q.expression="poison";assertTrue(checker.check(q,List.of(),List.of(String.valueOf(expected))).correct());assertFalse(checker.check(q,List.of(),List.of(String.valueOf(expected+1))).correct());
   HelpPlan h=HelpPlan.forQuestion(q);assertEquals(4,h.size());assertFalse(h.canTransfer());assertTrue(h.restore(null,q.id).entries.stream().allMatch(String::isEmpty));assertTrue(h.step(0).accepts(v[2]==1?"put":"take"));assertTrue(h.step(1).accepts(String.valueOf(v[0])));assertTrue(h.step(2).accepts(String.valueOf(v[1])));assertTrue(h.step(3).accepts(String.valueOf(expected)));
  }
  assertEquals(Set.of(0,1),operations);assertEquals(21,answers.size());assertEquals(recent.get(0),g.next(PictureArithmetic.ID,recent,false,limits).signature());
  GlobalCurriculum.chooseCountry(p,"GB");GlobalCurriculum.choosePack(p,"england-primary-2021-v1");assertTrue(GlobalCurriculum.pack(p).inGrade(PictureArithmetic.ID,2));assertFalse(GlobalCurriculum.pack(p).inGrade(PictureArithmetic.ID,1));
 }
}
