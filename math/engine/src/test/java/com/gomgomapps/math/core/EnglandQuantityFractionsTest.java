package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;import org.junit.Test;import static org.junit.Assert.*;
/** Solve the public numerical quantity, independently of stored answer metadata. */
public class EnglandQuantityFractionsTest {
 @Test public void halfAndQuarterQuantitiesExhaustFortyFiveVisibleConditions(){
  String pack="england-primary-2021-v1",id="el_fraction_of_number";
  CurriculumLimits limits=GlobalCurriculum.limits(pack,id,1);Generator generator=new Generator(new Random(64));List<String> recent=new ArrayList<>();Set<String> facts=new HashSet<>();Set<Integer> denominators=new HashSet<>();
  for(int i=0;i<45;i++){
   Question q=generator.next(id,recent,false,limits);assertFalse(recent.contains(q.signature()));recent.add(q.signature());
   Matcher m=Pattern.compile("(?:동그라미 )?(\\d+)(?:의 |개의 | cm의 )1/(2|4)은 (?:얼마인가요|몇 개인가요|몇 cm인가요)\\?").matcher(q.prompt);assertTrue(q.prompt,m.matches());int total=Integer.parseInt(m.group(1)),denominator=Integer.parseInt(m.group(2)),answer=total/denominator;
   assertTrue(total<=20&&total>0);assertEquals(0,total%denominator);assertTrue(facts.add(total+":"+denominator+":"+q.diagram.type));denominators.add(denominator);
   Checker checker=new Checker();assertTrue(checker.check(q,List.of(),List.of(String.valueOf(answer))).correct());assertFalse(checker.check(q,List.of(),List.of(String.valueOf(answer+1))).correct());
   assertFalse(HelpPlan.forQuestion(q).canTransfer());assertEquals(5,q.studyGuide.frames.size());
  }
  assertEquals(Set.of(2,4),denominators);assertEquals(recent.get(0),generator.next(id,recent,false,limits).signature());
 }
 @Test public void diagnosisDoesNotIntroduceCurrentYearQuantityPractice(){
  Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"GB");GlobalCurriculum.choosePack(p,"england-primary-2021-v1");p.grade=1;
  assertFalse(GlobalCurriculum.scope(p).stream().anyMatch(s->s.id.equals("el_fraction_of_number")));p.grade=2;
  assertTrue(GlobalCurriculum.scope(p).stream().anyMatch(s->s.id.equals("el_fraction_of_number")));
 }
}
