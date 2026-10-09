package com.gomgomapps.math.core;
import org.junit.Test;import static org.junit.Assert.*;import java.util.*;
public class PrimaryLinearSequencesTest {
 @Test public void allDomainsBothDirectionsCheckEveryAnswerAndBlankPublicRelationships(){
  for(String id:List.of(PrimaryLinearSequences.BUILD,PrimaryLinearSequences.RULE)){int count=0;
   for(int d=-12;d<=12;d++)if(d!=0)for(int a=0;a<=100;a++)if(a+3*d>=0&&a+3*d<=100){count++;Question q=PrimaryLinearSequences.create(id,a,d);List<String> solved=id.equals(PrimaryLinearSequences.RULE)?List.of(""+d):List.of(""+(a+d),""+(a+2*d),""+(a+3*d));
    assertTrue(new Checker().check(q,List.of(),solved).correct());for(int i=0;i<solved.size();i++){List<String> bad=new ArrayList<>(solved);bad.set(i,""+(Integer.parseInt(bad.get(i))+1));assertEquals(i,new Checker().check(q,List.of(),bad).index);}
    HelpPlan plan=HelpPlan.forQuestion(q);assertFalse(plan.canTransfer());assertEquals(id.equals(PrimaryLinearSequences.BUILD)?5:3,plan.size());
    List<String> frames=q.studyGuide.frames.stream().map(f->f.expected).toList();for(var f:q.studyGuide.frames){assertFalse(f.before.matches(".*[0-9].*"));assertEquals("",f.after);}
    q.answers=new String[]{"999"};PrimaryLinearSequences.attach(q);assertEquals(frames,q.studyGuide.frames.stream().map(f->f.expected).toList());
   }assertEquals(1956,count);
  }
 }
 @Test public void hundredFreshPublicConditionsAndEnglandOnlyPlacement(){
  Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"GB");GlobalCurriculum.choosePack(p,"england-primary-2021-v1");var pack=GlobalCurriculum.pack(p);Generator generator=new Generator(new Random(106));
  for(var s:PrimaryLinearSequences.SKILLS){assertTrue(pack.inGrade(s.id,6));assertFalse(pack.inGrade(s.id,5));Set<String> seen=new LinkedHashSet<>();for(int i=0;i<100;i++){Question q=generator.next(s.id,seen,false);assertTrue(seen.add(q.signature()));assertTrue(q.choices.isEmpty());assertFalse(q.stepSupport);}}
 }
 @Test public void decreasingRuleAndDraftRestoreKeepStudentSignedInput(){
  Question q=PrimaryLinearSequences.create(PrimaryLinearSequences.BUILD,36,-12);HelpPlan plan=HelpPlan.forQuestion(q);HelpPlan.Draft draft=new HelpPlan.Draft();draft.questionId=q.signature();draft.teachingVersion="primary-linear-sequences-v1";draft.stage=2;draft.entries=new ArrayList<>(List.of("36","-12","25","",""));var restored=plan.restore(draft,q.signature());assertEquals(draft.entries,restored.entries);assertEquals(2,restored.stage);assertTrue(plan.step(2).accepts("24"));assertFalse(plan.step(2).accepts("25"));
  assertTrue(new Checker().check(PrimaryLinearSequences.create(PrimaryLinearSequences.RULE,36,-12),List.of(),List.of("−12")).correct());
 }
}
