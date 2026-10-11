package com.gomgomapps.math.core;
import java.util.*;import org.junit.Test;import static org.junit.Assert.*;
public class YearSixContextConnectionsTest {
 @Test public void fourContextFamiliesAreReachableWithFreshPublicChecks(){
  var pack=GlobalCurriculum.packs("GB").stream().filter(p->p.id.equals("england-primary-2021-v1")).findFirst().orElseThrow();
  for(String id:FactorPowerStoriesTest.IDS){
   assertTrue(pack.inGrade(id,5));assertTrue(pack.inGrade(id,6));assertFalse(pack.inGrade(id,4));
   Generator generator=new Generator(new Random(199));List<String> recent=new ArrayList<>();
   for(int i=0;i<150;i++){
    Question q=generator.next(id,recent,false,GlobalCurriculum.limits(pack.id,id,6));assertFalse(recent.contains(q.signature()));recent.add(q.signature());int[] v=FactorPowerStories.read(q);assertNotNull(v);
    int expected=0;
    if(id.equals(FactorPowerStories.FACTOR)){int d=v[0]%v[1]==0?v[1]:v[2];for(int left=v[0];left>0;left-=d)expected++;}
    else if(id.equals(FactorPowerStories.MULTIPLE)){while(expected<v[1])expected+=v[0];}
    else if(id.equals(FactorPowerStories.SQUARE)){for(int board=0;board<v[1];board++)for(int row=0;row<v[0];row++)expected+=v[0];}
    else{int blocks=0;for(int layer=0;layer<v[0];layer++)for(int row=0;row<v[0];row++)blocks+=v[0];expected=blocks/v[1];}
    q.answers=new String[]{"poison"};q.expression="poison";Checker checker=new Checker();assertTrue(checker.check(q,List.of(),List.of(""+expected)).correct());assertEquals(Checker.Status.WRONG_ANSWER,checker.check(q,List.of(),List.of(""+(expected+1))).status);HelpPlan plan=HelpPlan.forQuestion(q);assertFalse(plan.canTransfer());assertTrue(plan.restore(null,q.id).entries.stream().allMatch(String::isEmpty));
   }
  }
  assertTrue(pack.inGrade(TwoStepChangeStories.ID,6));assertTrue(pack.inGrade(RemainderContexts.VEHICLES,6));
 }
}
