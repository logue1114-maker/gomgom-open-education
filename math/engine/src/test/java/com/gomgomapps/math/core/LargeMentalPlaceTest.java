package com.gomgomapps.math.core;
import java.util.*;import org.junit.Test;import static org.junit.Assert.*;
public class LargeMentalPlaceTest {
 @Test public void actualLargeSupplyUsesPublicArithmeticAndEveryPlace(){
  for(String id:List.of(MentalPlaceCalculations.ADD,MentalPlaceCalculations.SUB)){
   Generator g=new Generator(new Random(157));CurriculumLimits limits=GlobalCurriculum.limits("england-primary-2021-v1",id,5);List<String> recent=new ArrayList<>();Set<Integer> places=new HashSet<>();
   for(int i=0;i<500;i++){
    Question q=g.next(id,recent,false,limits);assertFalse(recent.contains(q.signature()));recent.add(q.signature());String[] parts=q.prompt.split(" ");int a=Integer.parseInt(parts[0]),b=Integer.parseInt(parts[2]);assertTrue(a>=1000&&a<=999999);int unit=1,t=b;while(t%10==0){t/=10;unit*=10;}assertTrue(t>=1&&t<=9);places.add(unit);int result=id.equals(MentalPlaceCalculations.ADD)?a+b:a-b;assertTrue(result>=0);q.answers=new String[]{"poison"};q.expression="poison";assertTrue(new Checker().check(q,List.of(),List.of(""+result)).correct());assertEquals(Checker.Status.WRONG_ANSWER,new Checker().check(q,List.of(),List.of(""+(result+1))).status);HelpPlan h=HelpPlan.forQuestion(q);assertFalse(h.canTransfer());assertTrue(h.restore(null,q.id).entries.stream().allMatch(String::isEmpty));for(int j=0;j<h.size();j++)assertFalse(h.step(j).before.matches(".*[0-9].*"));
   }
   assertEquals(Set.of(1,10,100,1000,10000,100000),places);
  }
 }
 @Test public void indexedReducedDomainsExhaustBeforeOldestAndMillionEdgesWork(){
  CurriculumLimits small=new CurriculumLimits("wholeDigits=4;secondDigits=4;maxGiven=1004;maxResult=2008;nonnegative=true");
  for(String id:List.of(MentalPlaceCalculations.ADD,MentalPlaceCalculations.SUB)){
   Catalog.Skill skill=Catalog.get(id);int count=MentalPlaceCalculations.upperCount(skill,small);assertEquals(140,count);Generator g=new Generator(new Random(157));List<String> recent=new ArrayList<>();for(int i=0;i<count;i++){Question q=g.next(id,recent,false,small);assertFalse(recent.contains(q.signature()));recent.add(q.signature());}assertEquals(recent.get(0),g.next(id,recent,false,small).signature());
  }
  Question add=MentalPlaceCalculations.make(MentalPlaceCalculations.ADD,999999,900000);assertTrue(new Checker().check(add,List.of(),List.of("1899999")).correct());
  Question sub=MentalPlaceCalculations.make(MentalPlaceCalculations.SUB,100000,1);HelpPlan h=HelpPlan.forQuestion(sub);int[] expected={10,10000,100000,0,1,100000,99999};assertEquals(expected.length,h.size());for(int i=0;i<expected.length;i++)assertTrue(h.step(i).accepts(""+expected[i]));assertTrue(new Checker().check(sub,List.of(),List.of("99999")).correct());
  assertNull(MentalPlaceCalculations.read(new Question(MentalPlaceCalculations.ADD,"1000000 + 1","","1000001")));assertNull(MentalPlaceCalculations.read(new Question(MentalPlaceCalculations.ADD,"1000 + 101","","1101")));
 }
}
