package com.gomgomapps.math.core;
import org.junit.Test;import java.util.*;import java.util.regex.*;import static org.junit.Assert.*;
public class DoubleHalfTest {
 private static final String NA="na-nied-primary-2024-v1";
 @Test public void independentlySolveEveryOfficialInputWithoutPrematureRepeats(){
  Generator generator=new Generator(new Random(43));
  for(int grade:List.of(1,2))for(Catalog.Skill skill:DoubleHalf.SKILLS){
   if(grade==1&&skill.id.equals("objectHalfRemainder"))continue;
   List<Integer> inputs=new ArrayList<>();
   if(grade==1)inputs=skill.id.equals("objectDouble")?List.of(1,2,3,4,5,10):List.of(2,4,6,8,10,20);
   else for(int n=1;n<=(skill.id.equals("objectHalfRemainder")?19:50);n++)if(skill.id.equals("objectDouble")||skill.id.equals("objectHalf")&&n%2==0||skill.id.equals("objectHalfRemainder")&&n%2==1)inputs.add(n);
   CurriculumLimits limits=GlobalCurriculum.limits(NA,skill.id,grade);Set<String> seen=new LinkedHashSet<>();Set<Integer> generated=new HashSet<>();
   for(int i=0;i<100;i++){
    Question q=generator.next(skill.id,seen,false,limits);if(i<inputs.size())assertTrue(seen.add(q.signature()));
    Matcher number=Pattern.compile("\\d+").matcher(q.prompt);assertTrue(number.find());int n=Integer.parseInt(number.group());assertFalse(number.find());assertTrue(inputs.contains(n));generated.add(n);
    List<String> answer=skill.id.equals("objectDouble")?List.of(""+(n+n)):skill.id.equals("objectHalf")?List.of(""+(n/2)):List.of(""+(n/2),""+(n%2));
    assertTrue(new Checker().check(q,List.of(),answer).correct());List<String> wrong=new ArrayList<>(answer);wrong.set(0,"999");assertFalse(new Checker().check(q,List.of(),wrong).correct());
    assertFalse(q.prompt.contains("×")||q.prompt.contains("÷"));assertEquals("",q.expression);assertFalse(q.stepSupport);assertTrue(limits.allows(q));
    assertEquals(n,(int)q.diagram.values[0]);assertEquals(skill.id.equals("objectDouble")?"doubleObjects":"sharingObjects",q.diagram.type);
    List<String> expected=skill.id.equals("objectDouble")?List.of(""+n,""+n,""+(n+n)):List.of(""+n,""+(n/2),""+(n%2),""+(n/2));
    q.answers=new String[]{"999"};DoubleHalf.attach(q);assertEquals(expected,q.studyGuide.frames.stream().map(f->f.expected).toList());HelpPlan help=HelpPlan.forQuestion(q);assertFalse(help.canTransfer());
    for(int step=0;step<help.size();step++){assertTrue(help.step(step).accepts(expected.get(step)));assertFalse(help.step(step).accepts("999"));assertFalse(q.studyGuide.frames.get(step).before.contains(expected.get(step)+" ="));}
   }
   assertEquals(new HashSet<>(inputs),generated);
  }
 }
 @Test public void mappingsAndDiagnosisKeepGradeOneRemaindersOut(){
  Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"NA");GlobalCurriculum.choosePack(p,NA);p.grade=1;
  assertFalse(GlobalCurriculum.pack(p).inGrade("objectHalfRemainder",1));assertTrue(GlobalCurriculum.pack(p).inGrade("objectHalfRemainder",2));
  assertFalse(GlobalCurriculum.scope(p).stream().anyMatch(s->DoubleHalf.supports(s.id)));p.grade=2;
  assertTrue(GlobalCurriculum.scope(p).stream().anyMatch(s->s.id.equals("objectDouble")));assertFalse(GlobalCurriculum.scope(p).stream().anyMatch(s->s.id.equals("objectHalfRemainder")));
 }
 @Test public void explicitInputLimitsAreValidated(){for(String invalid:List.of("doubleHalfInputs=0","doubleHalfInputs=51","doubleHalfInputs=2,2","doubleHalfInputs=")){try{new CurriculumLimits(invalid);fail(invalid);}catch(IllegalArgumentException expected){}}}
}
