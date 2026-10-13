package com.gomgomapps.math.core;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;
public class IdentityArithmeticTest {
 @Test public void publicNeutralAndApplicationIgnoreHiddenAnswers(){
  Checker checker=new Checker();
  for(String id:List.of(IdentityArithmetic.ADD,IdentityArithmetic.SUB,IdentityArithmetic.MUL))for(int b:new int[]{0,1,1000000})for(boolean left:new boolean[]{false,true}){
   if(left&&id.equals(IdentityArithmetic.SUB))continue;
   Question q=IdentityArithmetic.make(id,23,b,left);String neutral=id.equals(IdentityArithmetic.MUL)?"1":"0";q.answers=new String[]{"poison"};q.expression="poison";
   assertTrue(checker.check(q,List.of(),List.of(neutral,""+b)).correct());assertTrue(checker.check(q,List.of(),List.of(neutral+".0",b+"/1")).correct());
   assertEquals(0,checker.check(q,List.of(),List.of("2",""+b)).index);assertEquals(1,checker.check(q,List.of(),List.of(neutral,""+(b+1))).index);
   assertEquals(Checker.Status.INPUT_NEEDED,checker.check(q,List.of(),List.of("", "")).status);
   HelpPlan h=HelpPlan.forQuestion(q);assertEquals(2,h.size());assertFalse(h.canTransfer());assertTrue(h.restore(null,q.id).entries.stream().allMatch(String::isEmpty));
  }
 }
 @Test public void zeroMultiplicationCannotDefineUniqueNeutral(){
  try{IdentityArithmetic.make(IdentityArithmetic.MUL,0,5,false);fail();}catch(IllegalArgumentException expected){}
  try{IdentityArithmetic.make(IdentityArithmetic.SUB,5,4,true);fail();}catch(IllegalArgumentException expected){}
 }
 @Test public void gradeSevenMenuSupplyAndSmallExhaustion(){
  String pack="na-nied-primary-2024-v1";Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"NA");GlobalCurriculum.choosePack(p,pack);
  for(String id:List.of(IdentityArithmetic.ADD,IdentityArithmetic.SUB,IdentityArithmetic.MUL)){
   assertTrue(GlobalCurriculum.pack(p).inGrade(id,7));assertFalse(Curriculum.inCurriculum(Catalog.get(id),2022));assertFalse(GlobalCurriculum.pack(p).inGrade(id,6));
   Generator generator=new Generator(new Random(328));List<String>recent=new ArrayList<>();CurriculumLimits limit=GlobalCurriculum.limits(pack,id,7);
   for(int i=0;i<100;i++){Question q=generator.next(id,recent,false,limit);assertTrue(limit.allows(q));assertFalse(recent.contains(q.signature()));recent.add(q.signature());assertNotNull(IdentityArithmetic.read(q));}
   recent.clear();limit=new CurriculumLimits("minGiven=0;maxGiven=1;maxResult=1;nonnegative=true");int count=id.equals(IdentityArithmetic.ADD)?8:4;
   for(int i=0;i<count;i++){Question q=generator.next(id,recent,false,limit);assertFalse(recent.contains(q.signature()));recent.add(q.signature());}
   assertEquals(recent.get(0),generator.next(id,recent,false,limit).signature());
  }
 }
}
