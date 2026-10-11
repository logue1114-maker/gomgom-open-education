package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;import org.junit.Test;import static org.junit.Assert.*;
public class AddSubFamiliesTest {
 private static int calculation(String line){Matcher m=Pattern.compile("(\\d+) ([+−]) (\\d+) = □").matcher(line);assertTrue(line,m.matches());int first=Integer.parseInt(m.group(1)),second=Integer.parseInt(m.group(3));return m.group(2).equals("+")?first+second:first-second;}
 @Test public void everyFamilyAndPublicOperandChecking(){
  Set<String> unique=new HashSet<>();Checker checker=new Checker();
  for(int index=0;index<AddSubFamilies.count(99);index++){
   Question q=AddSubFamilies.at(99,index);assertTrue(unique.add(q.signature()));String[] lines=q.prompt.split("\\n");int first=calculation(lines[1]),second=calculation(lines[2]);assertTrue(first>=0&&first<=99&&second>=0&&second<=99);
   q.answers=new String[]{"poison","poison"};q.expression="poison";assertTrue(checker.check(q,List.of(),List.of(""+first,""+second)).correct());
   int wrong=first==99?98:first+1;Checker.Result result=checker.check(q,List.of(),List.of(""+wrong,""+second));assertEquals(Checker.Status.WRONG_ANSWER,result.status);assertEquals(0,result.index);
   wrong=second==99?98:second+1;result=checker.check(q,List.of(),List.of(""+first,""+wrong));assertEquals(Checker.Status.WRONG_ANSWER,result.status);assertEquals(1,result.index);
   assertEquals(Checker.Status.INPUT_NEEDED,checker.check(q,List.of(),Arrays.asList(""+first,null)).status);
   if(index<AddSubFamilies.count(20)){HelpPlan h=HelpPlan.forQuestion(q);assertEquals(5,h.size());assertFalse(h.canTransfer());assertTrue(h.restore(null,q.id).entries.stream().allMatch(String::isEmpty));for(int stage=0;stage<h.size();stage++)assertFalse(h.step(stage).before.matches(".*[0-9].*"));assertTrue(h.step(3).accepts(""+first));assertTrue(h.step(4).accepts(""+second));}
  }
  assertEquals(10100,unique.size());
  Question invalid=AddSubFamilies.make(2,3,false);invalid.prompt=invalid.prompt.replace("5 − 2 = 3","5 − 2 = 4");assertNull(AddSubFamilies.read(invalid));assertEquals(Checker.Status.INPUT_NEEDED,checker.check(invalid,List.of(),List.of("5","2")).status);
 }
 @Test public void selectedGradeRangesAndFreshSupply(){
  var pack=GlobalCurriculum.packs("NA").stream().filter(p->p.id.equals("na-nied-primary-2024-v1")).findFirst().orElseThrow();
  for(int grade=1;grade<=2;grade++){assertTrue(pack.inGrade(AddSubFamilies.ID,grade));int maximum=grade==1?20:99;CurriculumLimits limits=GlobalCurriculum.limits(pack.id,AddSubFamilies.ID,grade);Generator g=new Generator(new Random(204));List<String> recent=new ArrayList<>();Set<Integer> directions=new HashSet<>();
   for(int n=0;n<150;n++){Question q=g.next(AddSubFamilies.ID,recent,false,limits);assertFalse(recent.contains(q.signature()));recent.add(q.signature());int[] v=AddSubFamilies.read(q);assertNotNull(v);assertTrue(v[2]<=maximum);directions.add(v[3]);assertTrue(limits.allows(q));}assertEquals(Set.of(0,1),directions);
   Question edge=AddSubFamilies.at(maximum,AddSubFamilies.count(maximum)-1);assertTrue(limits.allows(edge));if(maximum<99)assertFalse(limits.allows(AddSubFamilies.make(maximum,1,true)));
  }
  assertFalse(pack.inGrade(AddSubFamilies.ID,3));assertFalse(Curriculum.inCurriculum(Catalog.get(AddSubFamilies.ID),2015));assertFalse(Curriculum.inCurriculum(Catalog.get(AddSubFamilies.ID),2022));
 }
}
