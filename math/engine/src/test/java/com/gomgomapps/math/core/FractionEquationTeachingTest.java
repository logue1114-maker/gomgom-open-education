package com.gomgomapps.math.core;
import org.junit.Test;import java.util.*;import java.io.*;import static org.junit.Assert.*;
public class FractionEquationTeachingTest {
 @Test public void completionUsesCanonicalStudentValuesAndOldDraftsReset()throws Exception{
  Question q=new Question("fracDiv","(2/3) / (4/5)","(2/3) / (4/5)","999");FractionEquationTeaching.attach(q);HelpPlan.Draft draft=new HelpPlan.Draft();for(StudyGuide.Frame frame:q.studyGuide.frames)draft.entries.add(frame.expected);draft.stage=draft.entries.size();draft.entries.set(draft.stage-2,"5.0");draft.entries.set(draft.stage-1,"12/2");assertEquals("5/6",HelpPlan.forQuestion(q).enteredAnswer(draft));
  ByteArrayOutputStream bytes=new ByteArrayOutputStream();new ObjectOutputStream(bytes).writeObject(q);Question restored=(Question)new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray())).readObject();assertEquals("5/6",HelpPlan.forQuestion(restored).enteredAnswer(draft));
  q.studyGuide=new StudyGuide().transfer(false).step("기존 계산","3 + 1 = ","","4");draft.entries=new ArrayList<>(List.of("4"));draft.stage=1;draft.questionId=q.id;draft=HelpPlan.forQuestion(q).restore(draft,q.id);assertEquals(0,draft.stage);assertEquals(List.of(""),draft.entries);
 }
 @Test public void generatedTasksHaveEnteredFramesAndDoNotTransferTheMainAnswer(){
  Generator g=new Generator(new Random(20261006901L));for(String id:List.of("fracAdd","fracSub","fracMul","fracDiv","linear"))for(int i=0;i<300;i++){
   Question q=g.next(id,List.of(),i%2==0);assertNotNull(id,q.studyGuide);HelpPlan plan=HelpPlan.forQuestion(q);assertFalse(plan.canTransfer());assertEquals(id.equals("linear")?6:id.equals("fracMul")?9:11,plan.size());
   HelpPlan.Draft draft=new HelpPlan.Draft();for(int stage=0;stage<plan.size();stage++){String value=q.studyGuide.frames.get(stage).expected;assertTrue(plan.step(stage).accepts(value));assertFalse(plan.step(stage).accepts(Expression.number(value).add(Rational.ONE).toString()));draft.entries.add(value);}draft.stage=plan.size();assertEquals(Expression.number(q.answers[0]),Expression.number(plan.enteredAnswer(draft)));
  }
 }
 @Test public void visibleFractionsKeepCommonDenominatorsZeroAndReciprocalStages(){
  String[] ids={"fracAdd","fracSub","fracMul","fracDiv"},expressions={"(1/6) + (1/4)","(3/7) - (3/7)","(2/3) * (3/8)","(2/3) / (4/5)"};
  List<List<String>> expected=List.of(List.of("1","6","1","4","12","2","3","5","1","5","12"),List.of("3","7","3","7","7","3","3","0","7","0","1"),List.of("2","3","3","8","6","24","6","1","4"),List.of("2","3","4","5","5","4","10","12","2","5","6"));
  for(int i=0;i<ids.length;i++){Question q=new Question(ids[i],expressions[i],expressions[i],"999");FractionEquationTeaching.attach(q);assertEquals(expected.get(i),q.studyGuide.frames.stream().map(f->f.expected).toList());assertFalse(HelpPlan.forQuestion(q).canTransfer());}
 }
 @Test public void bothSidesOfSignedEquationsAndSerializedGuidesUseGivensInsteadOfAnswerMetadata()throws Exception{
  for(int a=-12;a<=12;a++)if(a!=0)for(int b=-12;b<=12;b++)for(int x=-12;x<=12;x++){
   int c=a*x+b;String expression=a+"x + ("+b+") = "+c;Question q=new Question("linear",expression,expression,"999");q.kind="equation";FractionEquationTeaching.attach(q);assertEquals(List.of(""+a,""+b,""+c,""+(-b),""+(c-b),""+x),q.studyGuide.frames.stream().map(f->f.expected).toList());assertFalse(HelpPlan.forQuestion(q).canTransfer());
  }
  Question q=new Generator(new Random(20261006902L)).next("fracDiv",List.of(),false);ByteArrayOutputStream bytes=new ByteArrayOutputStream();new ObjectOutputStream(bytes).writeObject(q);Question restored=(Question)new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray())).readObject();assertEquals(q.signature(),restored.signature());assertEquals(11,HelpPlan.forQuestion(restored).size());assertFalse(HelpPlan.forQuestion(restored).canTransfer());
 }
}
