package com.gomgomapps.math.core;
import java.util.*;import org.junit.Test;import static org.junit.Assert.*;
public class ProductContextStoriesTest {
 @Test public void everyPublicContextHasAnIndependentInverseAndThreeBlankSteps(){
  Checker checker=new Checker();int count=0;
  for(Catalog.Skill skill:ProductContextStories.SKILLS)for(int a=1;a<=(skill.id.equals(ProductContextStories.SCALE)?99:12);a++)for(int b:List.of(1,2,3,4,5,8,10))for(int blank=0;blank<3;blank++){
   Question q=ProductContextStories.make(skill.id,a,b,blank);q.answers=new String[]{"999"};q.expression="999";int[] given={a,b,a*b};int answer=given[blank];
   assertTrue(checker.check(q,List.of(),List.of(""+answer)).correct());assertEquals(Checker.Status.WRONG_ANSWER,checker.check(q,List.of(),List.of(""+(answer+1))).status);
   HelpPlan plan=HelpPlan.forQuestion(q);assertEquals(3,plan.size());assertFalse(plan.canTransfer());assertTrue(plan.restore(null,q.id).entries.stream().allMatch(String::isEmpty));
   int first=blank==2?0:2,second=blank==0?1:blank==1?0:1;assertTrue(plan.step(0).accepts(""+given[first]));assertTrue(plan.step(1).accepts(""+given[second]));assertTrue(plan.step(2).accepts(""+answer));
   assertFalse(plan.step(2).accepts(""+(answer+1)));assertFalse(plan.step(2).before.matches(".*[0-9].*"));assertEquals("999",q.answers[0]);count++;
  }assertEquals(2583,count);
 }
 @Test public void finiteGradeThreeSupplyExhaustsBeforeOldest(){
  Generator g=new Generator(new Random(136));for(Catalog.Skill skill:ProductContextStories.SKILLS){List<String> recent=new ArrayList<>();Set<String> seen=new HashSet<>();int count=skill.id.equals(ProductContextStories.SCALE)?2079:252;CurriculumLimits limits=GlobalCurriculum.limits("england-primary-2021-v1",skill.id,3);
   for(int i=0;i<count;i++){Question q=g.next(skill.id,recent,false,limits);assertTrue(limits.allows(q));assertNotNull(ProductContextStories.read(q));assertTrue(seen.add(q.signature()));recent.add(q.signature());}assertEquals(recent.get(0),g.next(skill.id,recent,false,limits).signature());
   Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"GB");GlobalCurriculum.choosePack(p,"england-primary-2021-v1");for(int grade=1;grade<=6;grade++)assertEquals(grade==3||grade==4&&skill.id.equals(ProductContextStories.SCALE),GlobalCurriculum.pack(p).inGrade(skill.id,grade));
  }
 }
 @Test public void invalidAndAmbiguousPublicConditionsRejectHiddenAnswers(){
  for(String id:List.of(ProductContextStories.GROUP,ProductContextStories.SCALE,ProductContextStories.PAIRS))for(String[] v:List.of(new String[]{"□","0","0"},new String[]{"□","3","10"},new String[]{"□","□","12"},new String[]{"1","1","1"},new String[]{"1","□","7"},new String[]{"100","1","□"})){
   Question q=new Question(id,String.format(Locale.ROOT,ProductContextStories.template(id),(Object[])v),"0","0");assertNull(ProductContextStories.read(q));assertEquals(Checker.Status.INPUT_NEEDED,new Checker().check(q,List.of(),List.of("0")).status);
  }
 }
}
