package com.gomgomapps.math.core;
import java.util.*;import org.junit.Test;import static org.junit.Assert.*;
public class GradeSevenFactorExtremaScopeTest {
 @Test public void normalNaSevenRoutesExistingIdsToBoundedPublicPairsAndLargeResults(){
  for(String id:new String[]{"gcd","lcm"}){CurriculumLimits limits=GlobalCurriculum.limits("na-nied-primary-2024-v1",id,7);assertTrue(limits.factorExtremaRange());Generator g=new Generator(new Random(343));List<String>recent=new ArrayList<>();for(int i=0;i<20;i++){Question q=g.next(id,recent,false,limits);assertTrue(WholeFactorExtrema.selected(q));assertTrue(limits.allows(q));assertFalse(recent.contains(q.signature()));recent.add(q.signature());assertFalse(HelpPlan.forQuestion(q).canTransfer());}
   assertTrue(limits.allows(WholeFactorExtrema.make(id,0,0)));assertTrue(limits.allows(WholeFactorExtrema.make(id,0,1)));assertTrue(limits.allows(WholeFactorExtrema.make(id,1000000,999999)));
  }
  Learning.Profile england=new Learning.Profile();GlobalCurriculum.chooseCountry(england,"GB");GlobalCurriculum.choosePack(england,"england-primary-2021-v1");assertTrue(GlobalCurriculum.pack(england).inGrade("gcd",6));
  CurriculumLimits old=GlobalCurriculum.limits("england-primary-2021-v1","gcd",6);assertFalse(old.factorExtremaRange());Question legacy=new Generator(new Random(343)).next("gcd",List.of(),false,old);assertFalse(WholeFactorExtrema.selected(legacy));
 }
 @Test public void originalsResultAndNoneUseTheirOwnPublicSemantics(){
  CurriculumLimits source=new CurriculumLimits("factorExtremaRange=true;minGiven=12;maxGiven=18;maxResult=1000;nonnegative=true");assertTrue(source.allows(WholeFactorExtrema.make("gcd",12,18)));assertFalse(source.allows(WholeFactorExtrema.make("gcd",0,18)));Question q=WholeFactorExtrema.make("lcm",12,18);q.answers=new String[]{"9999999999999"};q.expression="9999999999999";assertTrue(source.allows(q));
  CurriculumLimits result=new CurriculumLimits("factorExtremaRange=true;minGiven=0;maxGiven=1000000;maxResult=1000000;nonnegative=true");assertFalse(result.allows(WholeFactorExtrema.make("lcm",1000000,999999)));assertTrue(result.allows(WholeFactorExtrema.make("gcd",1000000,999999)));assertTrue(result.allows(WholeFactorExtrema.make("lcm",0,999999)));
  assertFalse(WholeFactorExtrema.selected(new Question("gcd","12와 18의 최대공약수는?","","6")));
 }
}
