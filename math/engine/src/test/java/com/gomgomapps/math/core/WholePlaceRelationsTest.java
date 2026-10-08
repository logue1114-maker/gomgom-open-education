package com.gomgomapps.math.core;
import org.junit.Test;import static org.junit.Assert.*;import java.util.*;
public class WholePlaceRelationsTest {
 @Test public void allSixSelectedPlacementsHaveHundredFreshPublicRelations(){
  for(int grade=3;grade<=6;grade++)for(String id:grade>=5?List.of(WholePlaceRelations.VALUE,"largePlace"):List.of(WholePlaceRelations.VALUE)){
   int maximum=new int[]{999,9999,1000000,10000000}[grade-3];CurriculumLimits limits=GlobalCurriculum.limits("england-primary-2021-v1",id,grade);Generator g=new Generator(new Random(69+grade));List<String> recent=new ArrayList<>();Set<Integer> positions=new HashSet<>();Set<Integer> choices=new HashSet<>();boolean lower=false,above=false;
   for(int i=0;i<100;i++){Question q=g.next(id,recent,i%2==0,limits);assertFalse(recent.contains(q.signature()));recent.add(q.signature());var v=WholePlaceRelations.read(q);assertNotNull(v);assertTrue(v.number()>=0&&v.number()<=maximum);positions.add(v.unit());lower|=v.number()<maximum/10;above|=v.number()>99999;int expected=v.number()/v.unit()%10*(id.equals(WholePlaceRelations.VALUE)?v.unit():1);assertTrue(new Checker().check(q,List.of(),List.of(""+expected)).correct());assertFalse(new Checker().check(q,List.of(),List.of(""+(expected+1))).correct());HelpPlan plan=HelpPlan.forQuestion(q);assertNotNull(plan);assertFalse(plan.canTransfer());assertEquals(id.equals(WholePlaceRelations.VALUE)?4:3,plan.size());assertTrue(plan.step(0).accepts(""+v.number()));assertTrue(plan.step(1).accepts(""+v.unit()));assertTrue(plan.step(2).accepts(""+(v.number()/v.unit()%10)));if(v.value())assertTrue(plan.step(3).accepts(""+expected));if(!q.choices.isEmpty()){assertEquals(4,new HashSet<>(q.choices).size());assertEquals(q.answers[0],q.choices.get(q.correctChoice));for(String c:q.choices)assertTrue(limits.allowsChoice(c));choices.add(q.correctChoice);}
   }assertTrue(lower);assertTrue(positions.size()>=3);if(grade>=5)assertTrue(above);assertEquals(Set.of(0,1,2,3),choices);
  }
 }
 @Test public void publicBoundaryGivensOverrideAnswerMetadataAndKeepBlanks(){
  for(int number:new int[]{0,5,1000000,10000000})for(String place:List.of("일","십","천만")){
   Question q=new Question(WholePlaceRelations.VALUE,number+"에서 "+place+"의 자리 숫자가 나타내는 값은?","hidden","999");HelpPlan p=HelpPlan.forQuestion(q);assertNotNull(p);var v=WholePlaceRelations.read(q);assertTrue(p.step(3).accepts(""+(number/v.unit()%10*v.unit())));assertFalse(p.canTransfer());var draft=p.restore(null,q.id);assertTrue(WholePlaceRelations.references(q,p,draft).isEmpty());draft.entries.set(0,""+number);draft.stage=1;assertEquals(Map.of("문제의 수",""+number),WholePlaceRelations.references(q,p,draft));draft.entries.set(0,"9999");assertTrue(WholePlaceRelations.references(q,p,draft).isEmpty());
  }
 }
 @Test public void explicitMappingsDoNotEnterUnreviewedDefaults(){
  assertFalse(Curriculum.inCurriculum(Catalog.get(WholePlaceRelations.VALUE),2022));assertFalse(GlobalCurriculum.available(new Learning.Profile()).stream().anyMatch(s->s.id.equals(WholePlaceRelations.VALUE)));
 }
}
