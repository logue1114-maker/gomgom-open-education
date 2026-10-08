package com.gomgomapps.math.core;
import org.junit.Test;import java.util.*;import java.util.regex.*;import static org.junit.Assert.*;
public class CountingStepsTest {
 private static final String NA="na-nied-primary-2024-v1";
 private Map<Integer,Integer> official(String id,int grade){
  if(id.equals("countForward"))return Map.of(1,grade==1?100:grade==2?500:1000);
  if(id.equals("countBackward")&&grade<3)return grade==1?Map.of(1,20):Map.of(1,30,2,20);
  if(grade==1)return Map.of(2,30,5,100,10,100);
  if(grade==2)return Map.of(2,100,3,30,4,40,5,500,10,500);
  Map<Integer,Integer> result=new LinkedHashMap<>(Map.of(2,500,3,60,4,80,5,1000,10,1000,20,500,30,180,50,1000,100,1000));if(id.equals("countBackward"))result.put(1,1000);return result;
 }
 @Test public void everyPublishedCountingConditionMatchesIndependentOfficialDomain(){
  for(int grade:List.of(1,2,3))for(Catalog.Skill skill:CountingSteps.SKILLS){
   boolean back=skill.id.equals("countBackward");Map<Integer,Integer> expected=official(skill.id,grade);Set<String> domain=new HashSet<>();
   for(var bound:expected.entrySet())for(int start=back?bound.getKey():0;start<=(back?bound.getValue():bound.getValue()-bound.getKey());start+=grade==3?1:bound.getKey())domain.add(bound.getKey()+":"+start);
   CurriculumLimits limits=GlobalCurriculum.limits(NA,skill.id,grade);assertEquals(expected,limits.countingBounds());
   Map<String,Question> actual=CountingSteps.candidates(skill,limits);assertEquals(domain.size(),actual.size());Set<String> conditions=new HashSet<>();
   for(Question q:actual.values()){
    Matcher m=Pattern.compile("(\\d+)씩 (앞으로|거꾸로) 세세요\\.\\n(\\d+) → □").matcher(q.prompt);assertTrue(m.matches());int step=Integer.parseInt(m.group(1)),start=Integer.parseInt(m.group(3));assertEquals(back,m.group(2).equals("거꾸로"));conditions.add(step+":"+start);
    int next=back?start-step:start+step;assertTrue(new Checker().check(q,List.of(),List.of(""+next)).correct());assertEquals("",q.expression);assertNull(q.diagram);assertFalse(q.stepSupport);
   }
   assertEquals(domain,conditions);
   Generator gen=new Generator(new Random(44));Set<String> seen=new LinkedHashSet<>();
   for(int i=0;i<100;i++){
    Question q=gen.next(skill.id,seen,false,limits);if(i<domain.size())assertTrue(seen.add(q.signature()));
    Matcher m=Pattern.compile("\\d+").matcher(q.prompt);assertTrue(m.find());int step=Integer.parseInt(m.group());assertTrue(m.find());int start=Integer.parseInt(m.group());int next=back?start-step:start+step;
    List<String> frames=List.of(""+start,""+step,""+next);q.answers=new String[]{"999999"};CountingSteps.attach(q);assertEquals(frames,q.studyGuide.frames.stream().map(f->f.expected).toList());HelpPlan help=HelpPlan.forQuestion(q);assertFalse(help.canTransfer());
    for(int n=0;n<3;n++){assertTrue(help.step(n).accepts(frames.get(n)));assertFalse(help.step(n).accepts("999999"));}
   }
  }
 }
 @Test public void invalidStepBoundsAndStartsAreRejected(){for(String bad:List.of("countingBounds=0:20","countingBounds=2:1","countingBounds=2:1001","countingBounds=1:20,1:30","countingBounds=","countingAnyStart=false")){try{new CurriculumLimits(bad);fail(bad);}catch(IllegalArgumentException expected){}}}
 @Test public void currentGradeSkillsDoNotBecomeUnlearnedDiagnosis(){Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"NA");GlobalCurriculum.choosePack(p,NA);p.grade=1;assertFalse(GlobalCurriculum.scope(p).stream().anyMatch(s->CountingSteps.supports(s.id)));p.grade=2;assertTrue(GlobalCurriculum.scope(p).stream().anyMatch(s->s.id.equals("countSkip")));}
}
