package com.gomgomapps.math.core;
import org.junit.Test;import static org.junit.Assert.*;import java.util.*;
public class EnglandYearTwoPlaceTest {
 @Test public void bothYearTwoRelationsHaveFreshTwoDigitGivensAndStudentBlanks(){
  for(String id:List.of("place10",WholePlaceRelations.VALUE)){
   CurriculumLimits limits=GlobalCurriculum.limits("england-primary-2021-v1",id,2);Generator g=new Generator(new Random(110));List<String> recent=new ArrayList<>();Set<Integer> units=new HashSet<>();Set<Integer> positions=new HashSet<>();
   for(int i=0;i<100;i++){Question q=g.next(id,recent,i%2==0,limits);assertFalse(recent.contains(q.signature()));recent.add(q.signature());var v=WholePlaceRelations.read(q);assertNotNull(v);assertTrue(v.number()>=10&&v.number()<=99);assertTrue(v.unit()==1||v.unit()==10);units.add(v.unit());int digit=v.number()/v.unit()%10,expected=id.equals(WholePlaceRelations.VALUE)?digit*v.unit():digit;assertTrue(new Checker().check(q,List.of(),List.of(""+expected)).correct());assertFalse(new Checker().check(q,List.of(),List.of(""+(expected+1))).correct());HelpPlan h=HelpPlan.forQuestion(q);assertFalse(h.canTransfer());assertEquals(v.value()?4:3,h.size());assertTrue(h.step(0).accepts(""+v.number()));assertTrue(h.step(1).accepts(""+v.unit()));assertTrue(h.step(2).accepts(""+digit));if(v.value())assertTrue(h.step(3).accepts(""+expected));assertTrue(WholePlaceRelations.references(q,h,h.restore(null,q.id)).isEmpty());if(!q.choices.isEmpty()){assertEquals(4,new HashSet<>(q.choices).size());positions.add(q.correctChoice);}}
   assertEquals(Set.of(1,10),units);assertEquals(Set.of(0,1,2,3),positions);
  }
 }
 @Test public void yearTwoMappingKeepsOtherGradeDomains(){
  Learning.Profile p=new Learning.Profile();p.countryCode="GB";p.educationSystem="england-primary-2021-v1";var pack=GlobalCurriculum.pack(p);for(String id:List.of("place10",WholePlaceRelations.VALUE))assertTrue(pack.inGrade(id,2));assertEquals(999,GlobalCurriculum.limits("england-primary-2021-v1",WholePlaceRelations.VALUE,3).wholeMaximum(99999));assertFalse(pack.inGrade(WholePlaceRelations.VALUE,1));
 }
}
