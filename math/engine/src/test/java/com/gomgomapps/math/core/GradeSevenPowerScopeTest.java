package com.gomgomapps.math.core;
import java.util.*;import org.junit.Test;import static org.junit.Assert.*;
public class GradeSevenPowerScopeTest {
 @Test public void ordinaryNamibiaGradeSevenProvidesListsAndEveryMembershipInteger(){
  Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"NA");GlobalCurriculum.choosePack(p,"na-nied-primary-2024-v1");var pack=GlobalCurriculum.pack(p);
  for(String id:List.of(PowerNumberLists.SQUARE,PowerNumberLists.CUBE,PrimaryPowers.SQUARE,PrimaryPowers.CUBE)){
   assertTrue(pack.inGrade(id,7));assertFalse(pack.inGrade(id,6));assertFalse(Curriculum.inCurriculum(Catalog.get(id),2022));
   CurriculumLimits limits=GlobalCurriculum.limits(pack.id,id,7);Generator gen=new Generator(new Random(340));List<String> recent=new ArrayList<>();
   int count=PowerNumberLists.supports(id)?11:PrimaryPowers.SQUARE.equals(id)?101:1001;Set<Integer> givens=new HashSet<>();
   for(int i=0;i<count;i++){Question q=gen.next(id,recent,false,limits);assertTrue(limits.allows(q));assertFalse(recent.contains(q.signature()));recent.add(q.signature());int n=PowerNumberLists.supports(id)?PowerNumberLists.read(q):PrimaryPowers.read(q)[0];assertTrue(givens.add(n));assertFalse(HelpPlan.forQuestion(q).canTransfer());}
   assertEquals(count,givens.size());assertTrue(givens.contains(0));assertTrue(givens.contains(count-1));assertEquals(recent.get(0),gen.next(id,recent,false,limits).signature());
  }
 }
 @Test public void baseAndResultBoundsAreSeparateAndLegacyCalculationsUnchanged(){
  var pack=GlobalCurriculum.packs("NA").stream().filter(p->p.id.equals("na-nied-primary-2024-v1")).findFirst().orElseThrow();
  for(String id:List.of(PowerNumberLists.SQUARE,PowerNumberLists.CUBE)){var limits=GlobalCurriculum.limits(pack.id,id,7);assertTrue(limits.allows(PowerNumberLists.make(id,10)));}
  for(String id:List.of(PrimaryPowers.SQUARE,PrimaryPowers.CUBE)){int max=PrimaryPowers.SQUARE.equals(id)?100:1000;var limits=GlobalCurriculum.limits(pack.id,id,7);Question q=new Question(id,max+"은 "+(max==100?"제곱수":"세제곱수")+"인가요?","","1");assertTrue(limits.allows(q));q.prompt=q.prompt.replace(""+max,""+(max+1));assertFalse(limits.allows(q));}
  for(String id:List.of("squareWhole","cubeWhole")){var limits=GlobalCurriculum.limits(pack.id,id,7);assertTrue(limits.allows(new Question(id,"10","10","10")));assertFalse(limits.allows(new Question(id,"11","11","11")));}
  for(String id:List.of(PrimaryPowers.SQUARE,PrimaryPowers.CUBE)){var limits=GlobalCurriculum.limits("england-primary-2021-v1",id,5);assertFalse(limits.powerRecognitionRange());List<String> recent=new ArrayList<>();Generator g=new Generator(new Random(341));for(int i=0;i<100;i++){Question q=g.next(id,recent,false,limits);assertTrue(PrimaryPowers.DOMAINS.get(id).contains(PrimaryPowers.read(q)[0]));recent.add(q.signature());}}
 }
}
