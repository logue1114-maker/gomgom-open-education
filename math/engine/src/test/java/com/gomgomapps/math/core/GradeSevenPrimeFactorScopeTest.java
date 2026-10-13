package com.gomgomapps.math.core;
import java.util.*;import org.junit.Test;import static org.junit.Assert.*;
public class GradeSevenPrimeFactorScopeTest {
 @Test public void ordinaryNamibiaSevenSuppliesAllPositiveOriginalsWithExplicitNoneForOne(){
  Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"NA");GlobalCurriculum.choosePack(p,"na-nied-primary-2024-v1");var pack=GlobalCurriculum.pack(p);assertTrue(pack.inGrade(PrimeFactorLists.ID,7));assertFalse(pack.inGrade(PrimeFactorLists.ID,6));assertFalse(Curriculum.inCurriculum(Catalog.get(PrimeFactorLists.ID),2022));
  CurriculumLimits limits=GlobalCurriculum.limits(pack.id,PrimeFactorLists.ID,7);Generator g=new Generator(new Random(341));List<String> recent=new ArrayList<>();Set<Integer> givens=new HashSet<>();
  for(int i=0;i<99;i++){Question q=g.next(PrimeFactorLists.ID,recent,false,limits);assertFalse(recent.contains(q.signature()));recent.add(q.signature());assertTrue(limits.allows(q));int n=PrimeFactorLists.read(q);assertTrue(givens.add(n));assertTrue(q.choices.isEmpty());assertFalse(HelpPlan.forQuestion(q).canTransfer());}
  assertEquals(99,givens.size());assertTrue(givens.contains(1));assertTrue(givens.contains(99));assertEquals(recent.get(0),g.next(PrimeFactorLists.ID,recent,false,limits).signature());assertTrue(limits.allows(PrimeFactorLists.make(1)));assertTrue(limits.allows(PrimeFactorLists.make(97)));assertTrue(limits.allows(PrimeFactorLists.make(99)));
 }
 @Test public void onlyCorrectSkillNoneIsExemptFromNumericResultLimits(){
  CurriculumLimits limits=GlobalCurriculum.limits("na-nied-primary-2024-v1",PrimeFactorLists.ID,7);Question one=PrimeFactorLists.make(1);assertTrue(limits.allows(one));one.answers=new String[]{CommonMultipleLists.NONE};assertFalse(limits.allows(one));Question two=PrimeFactorLists.make(2);two.answers=new String[]{PrimeFactorLists.NONE};assertFalse(limits.allows(two));
  assertFalse(limits.allows(new Question(PrimeFactorLists.ID,"0의 서로 다른 소인수를 모두 쓰세요.\n작은 수부터 쓰세요.","",PrimeFactorLists.NONE)));assertFalse(limits.allows(new Question(PrimeFactorLists.ID,"100의 서로 다른 소인수를 모두 쓰세요.\n작은 수부터 쓰세요.","","2","5")));
  CurriculumLimits common=GlobalCurriculum.limits("na-nied-primary-2024-v1",CommonMultipleLists.ID,6);Question noCommon=CommonMultipleLists.make(10,11);assertTrue(common.allows(noCommon));noCommon.answers=new String[]{PrimeFactorLists.NONE};assertFalse(common.allows(noCommon));
 }
}
