package com.gomgomapps.math.core;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;
public class NamibiaGradeSevenEstimateTest {
 @Test public void gradeSevenBoundedEstimateRetainsPlausibilityMeaning(){
  String pack="na-nied-primary-2024-v1";Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"NA");GlobalCurriculum.choosePack(p,pack);
  for(String id:List.of(EstimateCalculationCheck.ADD,EstimateCalculationCheck.SUB)){
   assertTrue(GlobalCurriculum.pack(p).inGrade(id,7));assertTrue(GlobalCurriculum.pack(p).inGrade(id,6));assertFalse(GlobalCurriculum.pack(p).inGrade(id,5));
   CurriculumLimits limits=GlobalCurriculum.limits(pack,id,7);Generator g=new Generator(new Random(333));List<String>recent=new ArrayList<>();boolean inside=false,outside=false;
   for(int i=0;i<100;i++){
    Question q=g.next(id,recent,false,limits);assertTrue(limits.allows(q));assertFalse(recent.contains(q.signature()));recent.add(q.signature());int[] v=EstimateCalculationCheck.read(q);assertNotNull(v);
    int exact=id.equals(EstimateCalculationCheck.ADD)?v[0]+v[1]:v[0]-v[1];assertTrue(exact>=0&&exact<=1000000);boolean plausible=v[7]<=v[3];inside|=plausible;outside|=!plausible;
    q.answers=new String[]{"poison"};q.expression="poison";assertTrue(new Checker().check(q,List.of(),List.of(""+v[6],plausible?"0":"1")).correct());
   }
   assertTrue(inside&&outside);
  }
  Question q=EstimateCalculationCheck.make(EstimateCalculationCheck.ADD,2515,485,3010,100);assertTrue(GlobalCurriculum.limits(pack,EstimateCalculationCheck.ADD,7).allows(q));
  assertNotEquals(3010,2515+485);assertTrue(new Checker().check(q,List.of(),List.of("3000","0")).correct());
 }
}
