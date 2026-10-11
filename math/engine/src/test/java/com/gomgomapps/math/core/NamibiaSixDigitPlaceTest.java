package com.gomgomapps.math.core;
import org.junit.Test;import java.util.*;import static org.junit.Assert.*;
public class NamibiaSixDigitPlaceTest {
 private static final String NA="na-nied-primary-2024-v1";
 @Test public void digitAndValueUseSixDigitPublicNumbersAndSeparateRelations(){
  for(String id:List.of("largePlace",WholePlaceRelations.VALUE)){
   CurriculumLimits limits=GlobalCurriculum.limits(NA,id,6);Generator generator=new Generator(new Random(47));List<String> recent=new ArrayList<>();Set<Integer> units=new HashSet<>();
   for(int n=0;n<150;n++){
    Question q=generator.next(id,recent,false,limits);assertFalse(recent.contains(q.signature()));recent.add(q.signature());WholePlaceRelations.Givens g=WholePlaceRelations.read(q);assertNotNull(g);assertTrue(g.number()>=100000&&g.number()<=999999);units.add(g.unit());int digit=g.number()/g.unit()%10,answer=id.equals(WholePlaceRelations.VALUE)?digit*g.unit():digit;
    assertTrue(new Checker().check(q,List.of(),List.of(""+answer)).correct());assertFalse(new Checker().check(q,List.of(),List.of(""+(answer+1))).correct());q.answers=new String[]{"10000000"};HelpPlan help=HelpPlan.forQuestion(q);assertFalse(help.canTransfer());assertEquals(g.value()?4:3,help.size());assertTrue(help.step(0).accepts(""+g.number()));assertTrue(help.step(1).accepts(""+g.unit()));assertTrue(help.step(2).accepts(""+digit));if(g.value())assertTrue(help.step(3).accepts(""+answer));
   }
   assertEquals(Set.of(1,10,100,1000,10000,100000),units);
  }
 }
 @Test public void valueBoundsAndZeroDigitsRemainExact(){
  CurriculumLimits limits=GlobalCurriculum.limits(NA,WholePlaceRelations.VALUE,6);
  for(int number:List.of(100000,600001,999999))for(String place:List.of("일","십","십만")){
   Question q=new Question(WholePlaceRelations.VALUE,number+"에서 "+place+"의 자리 숫자가 나타내는 값은?","","99999999");WholePlaceRelations.Givens g=WholePlaceRelations.read(q);HelpPlan help=HelpPlan.forQuestion(q);assertTrue(help.step(3).accepts(""+(number/g.unit()%10*g.unit())));assertFalse(help.canTransfer());
  }
  for(int number:List.of(99999,1000000)){Question q=new Question(WholePlaceRelations.VALUE,number+"에서 십만의 자리 숫자가 나타내는 값은?","","0");assertFalse(limits.allows(q));}
  Learning.Profile profile=new Learning.Profile();GlobalCurriculum.chooseCountry(profile,"NA");GlobalCurriculum.choosePack(profile,NA);profile.grade=6;assertTrue(GlobalCurriculum.available(profile).stream().anyMatch(s->s.id.equals(WholePlaceRelations.VALUE)));assertFalse(GlobalCurriculum.scope(profile).stream().anyMatch(s->s.id.equals(WholePlaceRelations.VALUE)));
 }
}
