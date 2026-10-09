package com.gomgomapps.math.core;
import org.junit.Test;import static org.junit.Assert.*;import java.util.*;
public class PracticalPlaceStoriesTest {
 @Test public void all2002PublicInventoriesAndScaffoldWithoutAnswerTransfer(){
  Set<String> signatures=new HashSet<>();Checker checker=new Checker();
  for(int context=0;context<2;context++)for(int n=0;n<=1000;n++){
   Question q=PracticalPlaceStories.make(context,n);assertTrue(signatures.add(q.signature()));q.answers=new String[]{"poison"};assertTrue(checker.check(q,List.of(),List.of(""+n)).correct());assertEquals(Checker.Status.WRONG_ANSWER,checker.check(q,List.of(),List.of(""+(n+1))).status);
   HelpPlan p=HelpPlan.forQuestion(q);assertFalse(p.canTransfer());assertEquals(4,p.size());int[] expected={n/100,n/10%10,n%10,n};for(int s=0;s<4;s++)assertTrue(p.step(s).accepts(""+expected[s]));assertTrue(q.choices.isEmpty());
  }assertEquals(2002,signatures.size());
 }
 @Test public void finiteSupplyFreshAndOldestAndGradeBoundary(){
  Map<String,Question> pool=new LinkedHashMap<>();Map<String,Integer> recent=new HashMap<>();var limits=GlobalCurriculum.limits("england-primary-2021-v1",PracticalPlaceStories.ID,3);
  for(int c=0;c<2;c++)for(int n=0;n<=1000;n++){Question q=PracticalPlaceStories.make(c,n);assertTrue(limits.allows(q));pool.put(q.signature(),q);}Random random=new Random(129);String first=null;
  for(int i=0;i<2002;i++){Question q=FactFoundations.choose(pool,random,recent);assertFalse(recent.containsKey(q.signature()));if(first==null)first=q.signature();recent.put(q.signature(),i);}assertEquals(first,FactFoundations.choose(pool,random,recent).signature());
  Question actual=new Generator(random).next(PracticalPlaceStories.ID,List.of(),true,limits);assertNotNull(PracticalPlaceStories.read(actual));assertTrue(actual.choices.isEmpty());Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"GB");GlobalCurriculum.choosePack(p,"england-primary-2021-v1");assertTrue(GlobalCurriculum.pack(p).inGrade(PracticalPlaceStories.ID,3));assertFalse(GlobalCurriculum.pack(p).inGrade(PracticalPlaceStories.ID,2));
 }
 @Test public void malformedInventoryAndResponsesAreRejected(){
  for(String prompt:List.of("100개씩 든 상자: 1개",PracticalPlaceStories.make(0,1000).prompt.replace("낱개: 0","낱개: 1"),PracticalPlaceStories.make(0,900).prompt.replace("상자: 9","상자: 11"))){Question q=PracticalPlaceStories.make(0,0);q.prompt=prompt;assertNull(PracticalPlaceStories.read(q));assertFalse(new Checker().check(q,List.of(),List.of("0")).correct());}
  Question q=PracticalPlaceStories.make(0,1000);for(String s:List.of("","1e3","1,000","-1000","10000000"))assertEquals(Checker.Status.INPUT_NEEDED,new Checker().check(q,List.of(),List.of(s)).status);
 }
}
