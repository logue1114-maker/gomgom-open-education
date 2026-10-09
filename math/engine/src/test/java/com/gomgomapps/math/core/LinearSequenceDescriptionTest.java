package com.gomgomapps.math.core;
import org.junit.Test;import static org.junit.Assert.*;import java.util.*;
public class LinearSequenceDescriptionTest {
 @Test public void everyPublicSequenceAcceptsOnlyItsDirectionAndPositiveInterval(){int count=0;Checker checker=new Checker();
  for(int d=-12;d<=12;d++)if(d!=0)for(int a=-100;a<=100;a++)if(a+3*d>=-100&&a+3*d<=100){count++;Question q=LinearSequenceDescription.create(a,d);String op=d>0?"0":"1",gap=""+Math.abs(d);assertTrue(checker.check(q,List.of(),List.of(op,gap)).correct());assertEquals(0,checker.check(q,List.of(),List.of(d>0?"1":"0",gap)).index);assertEquals(1,checker.check(q,List.of(),List.of(op,""+(Math.abs(d)+1))).index);assertFalse(checker.check(q,List.of(),List.of(op,"-1")).correct());assertEquals(Checker.Status.INPUT_NEEDED,checker.check(q,List.of(),List.of("",gap)).status);
   q.answers=new String[]{"1","999"};assertTrue(checker.check(q,List.of(),List.of(op,gap)).correct());LinearSequenceDescription.attach(q);var expected=q.studyGuide.frames.stream().map(f->f.expected).toList();assertEquals(List.of(""+a,""+(a+d),""+d,gap),expected);for(var f:q.studyGuide.frames){assertFalse(f.before.matches(".*[0-9].*"));assertEquals("",f.after);}assertFalse(HelpPlan.forQuestion(q).canTransfer());
  }assertEquals(4356,count);
 }
 @Test public void freshSupplyMapsOnlyEnglandYear6AndRestoresSignedTeachingDraft(){
  Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"GB");GlobalCurriculum.choosePack(p,"england-primary-2021-v1");var pack=GlobalCurriculum.pack(p);assertTrue(pack.inGrade(LinearSequenceDescription.ID,6));assertFalse(pack.inGrade(LinearSequenceDescription.ID,5));Generator generator=new Generator(new Random(107));Set<String> seen=new LinkedHashSet<>();for(int i=0;i<100;i++){Question q=generator.next(LinearSequenceDescription.ID,seen,false);assertTrue(seen.add(q.signature()));assertTrue(q.choices.isEmpty());}
  Question q=LinearSequenceDescription.create(-10,-12);HelpPlan plan=HelpPlan.forQuestion(q);HelpPlan.Draft draft=new HelpPlan.Draft();draft.questionId=q.signature();draft.teachingVersion="linear-sequence-description-v1";draft.stage=2;draft.entries=new ArrayList<>(List.of("-10","-22","-11",""));assertEquals(draft.entries,plan.restore(draft,q.signature()).entries);assertTrue(plan.step(2).accepts("−12"));assertTrue(plan.step(3).accepts("12"));
 }
}
