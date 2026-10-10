package com.gomgomapps.math.core;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;
public class RemainderContextsTest {
 @Test public void sameDivisionRequiresDifferentAnswersInDifferentContexts(){
  Checker c=new Checker();String[] ids={RemainderContexts.GROUPS,RemainderContexts.LEFT,RemainderContexts.VEHICLES,RemainderContexts.LENGTH};String[] values={"3","2","4","17/5"};
  for(int i=0;i<ids.length;i++){Question q=RemainderContexts.indexed(ids[i],(17-1)*8+5-2);q.answers=new String[]{"poison"};q.expression="poison";assertTrue(c.check(q,List.of(),List.of(values[i])).correct());for(int j=0;j<values.length;j++)if(j!=i)assertFalse(c.check(q,List.of(),List.of(values[j])).correct());}
  Question length=RemainderContexts.indexed(RemainderContexts.LENGTH,(17-1)*8+3);assertTrue(c.check(length,List.of(),List.of("3.4")).correct());assertTrue(c.check(length,List.of(),List.of("34/10")).correct());assertEquals(1,FractionInput.errorPart(length,"16/5"));assertEquals(2,FractionInput.errorPart(length,"17/4"));
 }
 @Test public void allOperandsRespectZeroRemainderAndCapacityBoundaries(){
  Checker c=new Checker();for(int n=1;n<=9999;n++)for(int d=2;d<=9;d++)for(String id:List.of(RemainderContexts.GROUPS,RemainderContexts.LEFT,RemainderContexts.VEHICLES,RemainderContexts.LENGTH)){
   Question q=RemainderContexts.indexed(id,(n-1)*8+d-2);assertArrayEquals(new int[]{n,d,n/d,n%d},RemainderContexts.read(q));q.answers=new String[]{"poison"};
   String answer=id.equals(RemainderContexts.GROUPS)?""+(n/d):id.equals(RemainderContexts.LEFT)?""+(n%d):id.equals(RemainderContexts.VEHICLES)?""+(n/d+(n%d==0?0:1)):n+"/"+d;
   assertTrue(c.check(q,List.of(),List.of(answer)).correct());
  }
 }
 @Test public void blankHelpAsksForInterpretationAndNeverTransfers(){
  for(String id:List.of(RemainderContexts.GROUPS,RemainderContexts.LEFT,RemainderContexts.VEHICLES,RemainderContexts.LENGTH))for(int n:List.of(1,8,17,40,4001,9999)){
   Question q=RemainderContexts.indexed(id,(n-1)*8+2);HelpPlan h=HelpPlan.forQuestion(q);assertNotNull(h);assertFalse(h.canTransfer());assertTrue(h.restore(null,q.id).entries.stream().allMatch(String::isEmpty));assertTrue(h.step(0).accepts(""+n));assertTrue(h.step(1).accepts("4"));assertTrue(h.step(2).accepts(""+(n/4)));assertTrue(h.step(3).accepts(""+(n%4)));
   assertEquals(4,h.step(4).options.size());String choice=id.equals(RemainderContexts.GROUPS)?"groups":id.equals(RemainderContexts.LEFT)?"left":id.equals(RemainderContexts.VEHICLES)?"capacity":"share";assertTrue(h.step(4).accepts(choice));
   if(id.equals(RemainderContexts.VEHICLES))assertTrue(h.step(5).accepts(n%4==0?"0":"1"));
   assertTrue(h.step(h.size()-1).accepts(RemainderContexts.expected(id,n,4).toString()));
  }
 }
 @Test public void realYearFiveSupplyFreshAndOnlyYearFivePlacement(){
  Generator g=new Generator(new Random(169));Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"GB");GlobalCurriculum.choosePack(p,"england-primary-2021-v1");
  for(Catalog.Skill s:RemainderContexts.SKILLS){for(int grade=1;grade<=6;grade++)assertEquals(grade==5,GlobalCurriculum.pack(p).inGrade(s.id,grade));List<String> recent=new ArrayList<>();CurriculumLimits limits=GlobalCurriculum.limits(p.educationSystem,s.id,5);for(int i=0;i<100;i++){Question q=g.next(s.id,recent,false,limits);assertTrue(limits.allows(q));assertFalse(recent.contains(q.signature()));recent.add(q.signature());assertNotNull(RemainderContexts.read(q));assertTrue(q.choices.isEmpty());}}
  Question q=RemainderContexts.indexed(RemainderContexts.VEHICLES,0);q.prompt="corrupt public prompt";assertNull(RemainderContexts.read(q));assertEquals(Checker.Status.INPUT_NEEDED,new Checker().check(q,List.of(),List.of("1")).status);
 }
}
