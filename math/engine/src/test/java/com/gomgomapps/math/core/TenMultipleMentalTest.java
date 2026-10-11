package com.gomgomapps.math.core;
import org.junit.Test;import static org.junit.Assert.*;import java.util.*;
public class TenMultipleMentalTest {
 @Test public void domainIsUniqueAndPublicArithmeticIsIndependent(){
  for(String id:List.of(TenMultipleMental.MUL,TenMultipleMental.DIV)){
   Set<String> seen=new HashSet<>();int count=TenMultipleMental.count(id);assertTrue(count>100);
   Set<Integer> indices=new LinkedHashSet<>(List.of(0,count-1));Random random=new Random(236);while(indices.size()<5000)indices.add(random.nextInt(count));
   for(int i:indices){Question q=TenMultipleMental.at(id,i);assertTrue(seen.add(q.prompt));int[] v=TenMultipleMental.read(q);assertNotNull(v);assertTrue(v[0]%10==0&&v[1]%10==0&&v[0]<=1000000&&v[1]<=1000000);int expected=id.equals(TenMultipleMental.MUL)?Math.multiplyExact(v[0],v[1]):v[0]/v[1];assertEquals(expected,v[2]);q.answers=new String[]{"poison"};q.expression="poison";assertTrue(new Checker().check(q,List.of(),List.of(""+expected)).correct());assertFalse(new Checker().check(q,List.of(),List.of(""+(expected+1))).correct());}
   int independentCount=0;for(int a=10;a<=1000000;a+=10)independentCount+=id.equals(TenMultipleMental.MUL)?1000000/a/10:1000000/a;assertEquals(independentCount,count);
  }
 }
 @Test public void officialExamplesHaveBlankUntransferredUnitSteps(){
  Question mul=TenMultipleMental.make(TenMultipleMental.MUL,40,70),div=TenMultipleMental.make(TenMultipleMental.DIV,2100,300);
  int[][] values={{10,4,10,7,28,100,2800},{100,21,3,7}};Question[] qs={mul,div};
  for(int k=0;k<2;k++){HelpPlan h=HelpPlan.forQuestion(qs[k]);assertEquals(values[k].length,h.size());assertFalse(h.canTransfer());assertTrue(h.restore(null,qs[k].id).entries.stream().allMatch(String::isEmpty));for(int j=0;j<h.size();j++)assertTrue(h.step(j).accepts(""+values[k][j]));}
  assertNull(TenMultipleMental.read(new Question(TenMultipleMental.DIV,"2101 ÷ 300","","7")));assertNull(TenMultipleMental.read(new Question(TenMultipleMental.MUL,"1000000 × 10","","0")));
 }
 @Test public void reviewedPlacementAndNormalVariedSupply(){
  var pack=GlobalCurriculum.packs("NA").stream().filter(p->p.id.equals("na-nied-primary-2024-v1")).findFirst().orElseThrow();
  for(var skill:TenMultipleMental.SKILLS){assertTrue(pack.inGrade(skill.id,6));assertFalse(pack.inGrade(skill.id,5));assertFalse(Curriculum.inCurriculum(skill,2022));Generator g=new Generator(new Random(236));var limits=GlobalCurriculum.limits(pack.id,skill.id,6);List<String> recent=new ArrayList<>();for(int i=0;i<200;i++){Question q=g.next(skill.id,recent,false,limits);assertNotNull(TenMultipleMental.read(q));assertFalse(recent.contains(q.signature()));assertTrue(limits.allows(q));recent.add(q.signature());}}
 }
}
