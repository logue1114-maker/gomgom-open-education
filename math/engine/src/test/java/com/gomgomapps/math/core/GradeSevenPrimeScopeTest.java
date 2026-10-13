package com.gomgomapps.math.core;
import java.util.*;import org.junit.Test;import static org.junit.Assert.*;
public class GradeSevenPrimeScopeTest {
 @Test public void normalNamibiaGradeSevenSuppliesBothSkillsWithIndependentBounds(){
  Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"NA");GlobalCurriculum.choosePack(p,"na-nied-primary-2024-v1");var pack=GlobalCurriculum.pack(p);
  assertTrue(pack.inGrade(PrimeRecall.ID,7));assertTrue(pack.inGrade(PrimeVocabulary.CLASSIFY,7));assertFalse(pack.inGrade(PrimeVocabulary.FACTOR,7));
  CurriculumLimits list=GlobalCurriculum.limits(pack.id,PrimeRecall.ID,7),classification=GlobalCurriculum.limits(pack.id,PrimeVocabulary.CLASSIFY,7);
  assertTrue(list.allows(PrimeRecall.make(99)));assertFalse(list.allows(new Question(PrimeRecall.ID,"2 이상 100 이하의 소수를 모두 쓰세요.\n작은 수부터 쓰세요.","","2")));
  assertTrue(classification.allows(PrimeVocabulary.make(PrimeVocabulary.CLASSIFY,0,0)));assertTrue(classification.allows(PrimeVocabulary.make(PrimeVocabulary.CLASSIFY,99,0)));assertFalse(classification.allows(PrimeVocabulary.make(PrimeVocabulary.CLASSIFY,100,0)));
  Generator generator=new Generator(new Random(339));
  for(String id:List.of(PrimeRecall.ID,PrimeVocabulary.CLASSIFY)){
   CurriculumLimits limits=id.equals(PrimeRecall.ID)?list:classification;List<String> recent=new ArrayList<>();int count=id.equals(PrimeRecall.ID)?98:100;boolean zero=false,large=false;
   for(int i=0;i<count;i++){Question q=generator.next(id,recent,false,limits);assertTrue(limits.allows(q));assertFalse(recent.contains(q.signature()));recent.add(q.signature());int n=id.equals(PrimeRecall.ID)?PrimeRecall.read(q):PrimeVocabulary.read(q)[0];zero|=n==0;large|=n>19;}
   assertTrue(large);assertEquals(id.equals(PrimeVocabulary.CLASSIFY),zero);assertEquals(recent.get(0),generator.next(id,recent,false,limits).signature());
  }
 }
 @Test public void existingEnglandFiveAndNamibiaSixRetainTheirScopes(){
  CurriculumLimits en=GlobalCurriculum.limits("england-primary-2021-v1",PrimeRecall.ID,5),na=GlobalCurriculum.limits("na-nied-primary-2024-v1",PrimeVocabulary.CLASSIFY,6);
  assertTrue(en.allows(PrimeRecall.make(19)));assertFalse(en.allows(PrimeRecall.make(20)));assertTrue(na.allows(PrimeVocabulary.make(PrimeVocabulary.CLASSIFY,100,0)));assertFalse(na.allows(PrimeVocabulary.make(PrimeVocabulary.CLASSIFY,0,0)));assertFalse(na.allows(PrimeVocabulary.make(PrimeVocabulary.CLASSIFY,101,0)));
  assertFalse(Curriculum.inCurriculum(Catalog.get(PrimeRecall.ID),2022));
  for(int grade:new int[]{5,6}){CurriculumLimits classification=GlobalCurriculum.limits("england-primary-2021-v1",PrimeVocabulary.CLASSIFY,grade);assertFalse(classification.allows(PrimeVocabulary.make(PrimeVocabulary.CLASSIFY,0,0)));assertTrue(classification.allows(PrimeVocabulary.make(PrimeVocabulary.CLASSIFY,1,0)));assertTrue(classification.allows(PrimeVocabulary.make(PrimeVocabulary.CLASSIFY,144,0)));}
 }
}
