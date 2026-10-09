package com.gomgomapps.math.core;
import org.junit.Test;import static org.junit.Assert.*;import java.util.*;
public class ChangeStoriesTest {
 @Test public void everyWholeWithBoundaryPartsAndSixUnknownRelations(){
  Checker checker=new Checker();
  for(int whole=0;whole<=1000;whole++)for(int part:new HashSet<>(List.of(0,whole,whole/2,Math.max(0,whole-1))))for(int context=0;context<2;context++)for(boolean add:new boolean[]{true,false})for(int blank=0;blank<3;blank++){
   Question q=ChangeStories.make(context,add,whole,part,blank);int[] values=add?new int[]{part,whole-part,whole}:new int[]{whole,part,whole-part};assertArrayEquals(new int[]{values[0],values[1],values[2],add?1:0,blank,values[blank]},ChangeStories.read(q));q.answers=new String[]{"poison"};assertTrue(checker.check(q,List.of(),List.of(""+values[blank])).correct());assertEquals(Checker.Status.WRONG_ANSWER,checker.check(q,List.of(),List.of(""+(values[blank]+1))).status);
   HelpPlan plan=HelpPlan.forQuestion(q);assertEquals(3,plan.size());assertFalse(plan.canTransfer());int first=blank==0||blank==1&&add?2:0,second=blank==1?(add?0:2):1;assertTrue(plan.step(0).accepts(""+values[first]));assertTrue(plan.step(1).accepts(""+values[second]));assertTrue(plan.step(2).accepts(""+values[blank]));for(var f:q.studyGuide.frames)assertFalse(f.before.matches(".*[0-9].*"));assertTrue(q.choices.isEmpty());
  }
 }
 @Test public void actualStreamingFallbackExhaustsAndGrade3GenerationIsVaried(){
  Random fixed=new Random(){@Override public int nextInt(int bound){return 0;}@Override public boolean nextBoolean(){return true;}};Map<String,Integer> recent=new HashMap<>();String first=null;
  for(int i=0;i<72;i++){Question q=ChangeStories.nextDomain(fixed,CurriculumLimits.NONE,recent,2);assertFalse(recent.containsKey(q.signature()));if(first==null)first=q.signature();recent.put(q.signature(),i);}assertEquals(first,ChangeStories.nextDomain(fixed,CurriculumLimits.NONE,recent,2).signature());
  var limits=GlobalCurriculum.limits("england-primary-2021-v1",ChangeStories.ID,3);Generator g=new Generator(new Random(131));List<String> history=new ArrayList<>();Set<String> modes=new HashSet<>();for(int i=0;i<100;i++){Question q=g.next(ChangeStories.ID,history,true,limits);assertFalse(history.contains(q.signature()));history.add(q.signature());int[] v=ChangeStories.read(q);assertNotNull(v);modes.add(v[3]+":"+v[4]);assertTrue(limits.allows(q));assertTrue(q.choices.isEmpty());}assertEquals(6,modes.size());
  Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"GB");GlobalCurriculum.choosePack(p,"england-primary-2021-v1");assertTrue(GlobalCurriculum.pack(p).inGrade(ChangeStories.ID,3));assertFalse(GlobalCurriculum.pack(p).inGrade(ChangeStories.ID,2));
 }
 @Test public void malformedAndOutOfDomainPublicGivensCannotUseKeys(){
  Question q=ChangeStories.make(0,true,1000,500,2);for(String prompt:List.of(q.prompt.replace("500개를","1001개를"),q.prompt.replace("500개 있었","□개 있었"),q.prompt.replace("스티커","물건"),q.prompt.replace("□개예요","1000개예요"))){Question bad=ChangeStories.make(0,true,0,0,2);bad.prompt=prompt;assertNull(ChangeStories.read(bad));assertFalse(new Checker().check(bad,List.of(),List.of("0")).correct());}
  q=ChangeStories.make(0,false,1000,500,2);q.prompt=q.prompt.replace("1000개 있었","499개 있었");assertNull(ChangeStories.read(q));
 }
}
