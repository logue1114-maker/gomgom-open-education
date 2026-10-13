package com.gomgomapps.math.core;
import java.util.*;import org.junit.Test;import static org.junit.Assert.*;
public class GradeSevenPrimeProductScopeTest {
 @Test public void normalSevenPlacementUsesWholeNumberContextNotPriorListNinetyNine(){
  Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"NA");GlobalCurriculum.choosePack(p,"na-nied-primary-2024-v1");var pack=GlobalCurriculum.pack(p);assertTrue(pack.inGrade(PrimeFactorProducts.ID,7));assertFalse(pack.inGrade(PrimeFactorProducts.ID,6));assertFalse(Curriculum.inCurriculum(Catalog.get(PrimeFactorProducts.ID),2022));
  CurriculumLimits limits=GlobalCurriculum.limits(pack.id,PrimeFactorProducts.ID,7);for(int n:new int[]{1,12,99,100,999983,1000000}){Question q=PrimeFactorProducts.make(n);assertTrue(limits.allows(q));assertTrue(q.choices.isEmpty());assertEquals(1,q.answers.length);assertFalse(HelpPlan.forQuestion(q).canTransfer());}
  Generator g=new Generator(new Random(342));List<String> recent=new ArrayList<>();for(int i=0;i<20;i++){Question q=g.next(PrimeFactorProducts.ID,recent,false,limits);assertFalse(recent.contains(q.signature()));recent.add(q.signature());assertTrue(limits.allows(q));assertTrue(PrimeFactorProducts.read(q)>0);}
 }
 @Test public void resultAndGivenLimitsBoundPublicOriginalInsteadOfInstructionOrExpressionDigits(){
  CurriculumLimits limited=new CurriculumLimits("minGiven=12;maxGiven=49;maxResult=49;nonnegative=true");assertTrue(limited.allows(PrimeFactorProducts.make(12)));assertTrue(limited.allows(PrimeFactorProducts.make(49)));assertFalse(limited.allows(PrimeFactorProducts.make(1)));assertFalse(limited.allows(PrimeFactorProducts.make(50)));
  CurriculumLimits result=new CurriculumLimits("minGiven=1;maxGiven=1000000;maxResult=99;nonnegative=true");assertTrue(result.allows(PrimeFactorProducts.make(99)));assertFalse(result.allows(PrimeFactorProducts.make(100)));
  Question poisoned=PrimeFactorProducts.make(12);poisoned.answers=new String[]{"999999999"};poisoned.expression="999999999";assertTrue(limited.allows(poisoned));assertFalse(limited.allows(new Question(PrimeFactorProducts.ID,"0을 소인수의 곱으로 나타내세요.\n소인수가 없으면 빈 곱의 값 1을 쓰세요.","","1")));
  CurriculumLimits existing=GlobalCurriculum.limits("na-nied-primary-2024-v1",PrimeFactorLists.ID,7);assertTrue(existing.allows(PrimeFactorLists.make(1)));assertTrue(existing.allows(PrimeFactorLists.make(99)));assertFalse(existing.allows(new Question(PrimeFactorLists.ID,"100의 서로 다른 소인수를 모두 쓰세요.\n작은 수부터 쓰세요.","","2","5")));
 }
}
