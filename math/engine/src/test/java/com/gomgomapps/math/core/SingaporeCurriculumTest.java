package com.gomgomapps.math.core;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;
public class SingaporeCurriculumTest {
 private static final String PACK="sg-moe-primary-2021-v1";
 private Learning.Profile profile(){Learning.Profile p=new Learning.Profile();p.languageTag="en";GlobalCurriculum.chooseCountry(p,"SG");GlobalCurriculum.choosePack(p,PACK);return p;}
 @Test public void standardPrimaryPackHasSixActualLevelsAndPreservesLanguage(){
  Learning.Profile p=profile();GlobalCurriculum.Pack pack=GlobalCurriculum.pack(p);assertEquals("en",p.languageTag);assertEquals(List.of(1,2,3,4,5,6),pack.levels());assertEquals("Primary 1",pack.level(1));assertTrue(pack.coverage.contains("Foundation track not included"));
  for(int level:pack.levels())assertTrue(pack.grades.keySet().stream().anyMatch(id->pack.inGrade(id,level)));
  assertFalse(pack.inGrade("decimalMul",3));assertTrue(pack.inGrade("decimalMul",4));assertFalse(pack.inGrade("fracDiv",5));assertTrue(pack.inGrade("fracDiv",6));
 }
 @Test public void tableSetsAndThreeDigitAdditionsFollowTheirSelectedLevels(){
  Generator g=new Generator(new Random(20261005104L));
  for(int i=0;i<100;i++){
   for(int grade:List.of(2,3)){
    Question q=g.next("tables",List.of(),false,GlobalCurriculum.limits(PACK,"tables",grade));int expected=FactFormsTest.solve(q.prompt);String[] parts=q.prompt.replace("□",String.valueOf(expected)).split(" = ",2)[0].split(" × ");int a=Integer.parseInt(parts[0]),b=Integer.parseInt(parts[1]);Set<Integer> allowed=grade==2?Set.of(2,3,4,5,10):Set.of(6,7,8,9);assertTrue(allowed.contains(a)||allowed.contains(b));assertEquals(String.valueOf(expected),q.answers[0]);
    Question d=g.next("divide",List.of(),false,GlobalCurriculum.limits(PACK,"divide",grade));int divisionExpected=FactFormsTest.solve(d.prompt);String[] operands=d.prompt.replace("□",String.valueOf(divisionExpected)).split(" = ",2)[0].split(" ÷ ");int left=Integer.parseInt(operands[0]),right=Integer.parseInt(operands[1]);assertTrue(allowed.contains(right));assertEquals(0,left%right);assertEquals(String.valueOf(divisionExpected),d.answers[0]);
   }
   Question q=g.next("add1000",List.of(),false,GlobalCurriculum.limits(PACK,"add1000",2));String[] operands=q.prompt.split(" \\+ ");assertTrue(Integer.parseInt(operands[0])<=999);assertTrue(Integer.parseInt(operands[1])<=999);
  }
 }
 @Test public void eachMappedLevelGeneratesAndChecksItsOwnQuestions(){
  GlobalCurriculum.Pack pack=GlobalCurriculum.pack(profile());Generator g=new Generator(new Random(20261005105L));
  for(int level:pack.levels())for(String id:pack.grades.keySet())if(pack.inGrade(id,level)){
   CurriculumLimits limits=GlobalCurriculum.limits(PACK,id,level);
   for(int i=0;i<12;i++){Question q=g.next(id,List.of(),i%2==0,limits);assertTrue(id+"/"+level,limits.allows(q));assertNotNull(q.answers);assertTrue(q.answers.length>0);assertTrue(new Checker().check(q,List.of(),Arrays.asList(q.answers)).correct());}
  }
 }
}
