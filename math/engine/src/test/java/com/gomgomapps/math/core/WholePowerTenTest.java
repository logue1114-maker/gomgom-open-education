package com.gomgomapps.math.core;
import java.math.BigDecimal;import java.util.*;import java.io.*;import org.junit.Test;import static org.junit.Assert.*;
public class WholePowerTenTest {
 @Test public void publicBoundariesIncludeZeroAndSixPlaceExactQuotients(){
  for(String id:List.of(WholePowerTen.MUL,WholePowerTen.DIV))for(int power:new int[]{10,100,1000,10000,100000,1000000}){
   int max=id.equals(WholePowerTen.MUL)?1000000/power:1000000;
   for(int a:new int[]{0,1,Math.min(99,max),Math.max(0,max-1),max}){Question q=WholePowerTen.make(id,a,power);String expected=(id.equals(WholePowerTen.MUL)?BigDecimal.valueOf(a).multiply(BigDecimal.valueOf(power)):BigDecimal.valueOf(a).divide(BigDecimal.valueOf(power))).stripTrailingZeros().toPlainString();q.answers=new String[]{"poison"};q.expression="poison";assertTrue(new Checker().check(q,List.of(),List.of(expected)).correct());HelpPlan h=HelpPlan.forQuestion(q);assertEquals(6,h.size());assertFalse(h.canTransfer());assertTrue(h.restore(null,q.id).entries.stream().allMatch(String::isEmpty));assertTrue(h.step(0).accepts(""+a));assertTrue(h.step(1).accepts(""+power));assertTrue(h.step(2).accepts("1"));assertTrue(h.step(4).accepts(""+a));assertTrue(h.step(5).accepts(expected));}
  }
  Question small=WholePowerTen.make(WholePowerTen.DIV,1,1000000);assertTrue(new Checker().check(small,List.of(),List.of("0.000001")).correct());assertTrue(new Checker().check(small,List.of(),List.of("0.0000010")).correct());assertFalse(new Checker().check(small,List.of(),List.of("0")).correct());
  for(String prompt:List.of("1000001 ÷ 10","1 ÷ 0","1 ÷ 20","1.5 ÷ 10"))assertNull(WholePowerTen.read(new Question(WholePowerTen.DIV,prompt,"","0")));
 }
 @Test public void indexedDomainAndReviewedNormalSupplyAreVaried(){
  assertEquals(6000006,WholePowerTen.count(WholePowerTen.DIV));int count=0;for(int p=10;p<=1000000;p*=10)count+=1000000/p+1;assertEquals(count,WholePowerTen.count(WholePowerTen.MUL));
  var pack=GlobalCurriculum.packs("NA").stream().filter(p->p.id.equals("na-nied-primary-2024-v1")).findFirst().orElseThrow();
  for(var skill:WholePowerTen.SKILLS){assertTrue(pack.inGrade(skill.id,6));assertFalse(pack.inGrade(skill.id,5));assertFalse(Curriculum.inCurriculum(skill,2022));Set<Integer> indices=new HashSet<>(List.of(0,WholePowerTen.count(skill.id)-1));Random random=new Random(238);while(indices.size()<5000)indices.add(random.nextInt(WholePowerTen.count(skill.id)));Set<String> seen=new HashSet<>();for(int i:indices){Question q=WholePowerTen.at(skill.id,i);assertNotNull(WholePowerTen.read(q));assertTrue(seen.add(q.prompt));}
   Generator g=new Generator(new Random(238));var limits=GlobalCurriculum.limits(pack.id,skill.id,6);List<String> recent=new ArrayList<>();boolean large=false,nonMultiple=false,fractional=false;for(int i=0;i<200;i++){Question q=g.next(skill.id,recent,false,limits);assertFalse(recent.contains(q.signature()));recent.add(q.signature());int[] v=WholePowerTen.read(q);assertNotNull(v);assertTrue(limits.allows(q));large|=v[0]>=1000;nonMultiple|=v[0]%10!=0;fractional|=v[0]%v[1]!=0;}assertTrue(large&&nonMultiple);if(skill.id.equals(WholePowerTen.DIV))assertTrue(fractional);
  }
 }
 @Test public void decimalHelperDraftSurvivesSerialization()throws Exception{
  Learning.Session s=new Learning.Session();s.question=WholePowerTen.make(WholePowerTen.DIV,123457,1000000);HelpPlan h=HelpPlan.forQuestion(s.question);s.conceptHelp=h.restore(null,s.question.id);s.conceptHelp.stage=3;s.conceptHelp.entries=new ArrayList<>(List.of("123457","1000000","1","0.000002"));ByteArrayOutputStream b=new ByteArrayOutputStream();new ObjectOutputStream(b).writeObject(s);Learning.Session saved=(Learning.Session)new ObjectInputStream(new ByteArrayInputStream(b.toByteArray())).readObject();HelpPlan restored=HelpPlan.forQuestion(saved.question);HelpPlan.Draft d=restored.restore(saved.conceptHelp,saved.question.id);assertEquals(s.question.id,saved.question.id);assertEquals(3,d.stage);assertEquals("0.000002",d.entries.get(3));assertFalse(restored.step(3).accepts(d.entries.get(3)));assertEquals("whole-power-ten-units-v1",d.teachingVersion);
 }
}
