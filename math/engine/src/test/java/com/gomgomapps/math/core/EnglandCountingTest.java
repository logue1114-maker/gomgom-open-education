package com.gomgomapps.math.core;
import org.junit.Test;import java.util.*;import java.util.regex.*;import static org.junit.Assert.*;
public class EnglandCountingTest {
 private static final String PACK="england-primary-2021-v1";
 @Test public void sixFiniteDomainsMatchPublicCountingDirectionsAndStarts(){
  for(int grade:List.of(1,2))for(Catalog.Skill skill:CountingSteps.SKILLS){
   boolean back=skill.id.equals("countBackward");int[] steps=skill.id.equals("countForward")?new int[]{1}:grade==1?(back?new int[]{1}:new int[]{2,5,10}):(back?new int[]{1,2,3,5,10}:new int[]{2,3,5,10});
   Set<String> expected=new HashSet<>();for(int step:steps){int maximum=step==1?110:100;for(int start=back?step:0;start<=(back?maximum:maximum-step);start+=(step==1||grade==2&&step==10)?1:step)expected.add(step+":"+start);}
   CurriculumLimits limits=GlobalCurriculum.limits(PACK,skill.id,grade);Map<String,Question> pool=CountingSteps.candidates(skill,limits);assertEquals(expected.size(),pool.size());Set<String> actual=new HashSet<>();
   for(Question q:pool.values()){
    Matcher m=Pattern.compile("(\\d+)씩 (앞으로|거꾸로) 세세요\\.\\n(\\d+) → □").matcher(q.prompt);assertTrue(m.matches());int step=Integer.parseInt(m.group(1)),start=Integer.parseInt(m.group(3)),answer=back?start-step:start+step;assertEquals(back,m.group(2).equals("거꾸로"));actual.add(step+":"+start);
    assertTrue(new Checker().check(q,List.of(),List.of(""+answer)).correct());assertFalse(new Checker().check(q,List.of(),List.of(""+(answer+1))).correct());q.answers=new String[]{"9999"};CountingSteps.attach(q);HelpPlan help=HelpPlan.forQuestion(q);assertFalse(help.canTransfer());assertTrue(help.step(0).accepts(""+start));assertTrue(help.step(1).accepts(""+step));assertTrue(help.step(2).accepts(""+answer));
   }
   assertEquals(expected,actual);Generator generator=new Generator(new Random(59));Set<String> seen=new LinkedHashSet<>();for(int n=0;n<pool.size();n++){Question q=generator.next(skill.id,seen,false,limits);assertTrue(seen.add(q.signature()));}assertTrue(seen.contains(generator.next(skill.id,seen,false,limits).signature()));
  }
 }
 @Test public void gradeBoundariesAndOtherCountriesKeepTheirExistingStartRules(){
  Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"GB");GlobalCurriculum.choosePack(p,PACK);p.grade=1;assertFalse(GlobalCurriculum.scope(p).stream().anyMatch(s->CountingSteps.supports(s.id)));p.grade=2;assertTrue(GlobalCurriculum.scope(p).stream().anyMatch(s->s.id.equals("countSkip")));assertEquals(80,CountingSteps.candidates(Catalog.get("countSkip"),GlobalCurriculum.limits(PACK,"countSkip",1)).size());
  assertFalse(GlobalCurriculum.limits("na-nied-primary-2024-v1","countSkip",2).countingAnyStart(10));assertTrue(GlobalCurriculum.limits("na-nied-primary-2024-v1","countSkip",3).countingAnyStart(3));
  for(String bad:List.of("countingAnyStartSteps=0","countingAnyStartSteps=101","countingAnyStartSteps=10,10","countingAnyStartSteps=")){try{new CurriculumLimits(bad);fail(bad);}catch(IllegalArgumentException expected){}}
 }
}
