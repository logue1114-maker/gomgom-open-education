package com.gomgomapps.math.core;
import java.util.*;import java.io.*;import org.junit.Test;import static org.junit.Assert.*;
public class SubtractionPartsTest {
 @Test public void everyPublicSubtractionThroughTwentyUsesValidBlankSteps(){
  int extended=0,small=0,twoDigit=0;
  for(int a=0;a<=20;a++)for(int b=0;b<=a;b++){
   Question q=new Question("sub20",a+" - "+b,"999","999");HelpPlan p=HelpPlan.forQuestion(q);assertFalse(p.canTransfer());
   List<Integer> values=new ArrayList<>(List.of(a,b));
   boolean oldTen=a>=10&&a<=18&&b>a%10&&b<=9;
   if(oldTen||a>=10&&b>0&&b<a&&b<=9){values.add(a-10);values.add(10-b);values.add(a-b);}
   else if(a>=10&&b>=10&&b<a){values.add(b/10*10);values.add(b%10);values.add(a-b/10*10);values.add(a-b);}
   else values.add(a-b);
   assertEquals(q.prompt,values.size(),p.size());for(int i=0;i<p.size();i++){assertTrue(q.prompt+" frame"+i,p.step(i).accepts(""+values.get(i)));assertFalse(p.step(i).accepts(""));assertFalse(p.step(i).accepts(""+(values.get(i)+1)));assertEquals("",p.step(i).after);}
   if("whole-subtract-parts-v2".equals(q.studyGuide.teachingVersion)){extended++;if(b<=9)small++;else twoDigit++;assertTrue(a>=10&&b>0&&b<a&&!oldTen);}
   String id=q.id,signature=q.signature();HelpPlan again=HelpPlan.forQuestion(q);assertEquals(p.size(),again.size());assertEquals(id,q.id);assertEquals(signature,q.signature());assertEquals("999",q.answers[0]);
  }
  assertEquals(109,extended);assertEquals(54,small);assertEquals(55,twoDigit);
 }
 private Learning.Session roundTrip(Learning.Session session)throws Exception{ByteArrayOutputStream bytes=new ByteArrayOutputStream();new ObjectOutputStream(bytes).writeObject(session);return (Learning.Session)new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray())).readObject();}
 @Test public void savedOriginalProblemKeepsItsThreeStepDraftAndPublicIdentity()throws Exception{
  for(int[] v:new int[][]{{20,9},{18,12},{19,2}}){Learning.Session session=new Learning.Session();session.question=new Question("sub20",v[0]+" - "+v[1],"999","999");session.question.studyGuide=new StudyGuide().transfer(false);session.question.studyGuide.teachingVersion="whole-number-relations-v1";session.question.studyGuide.step("old","","","999");HelpPlan original=HelpPlan.forQuestion(session.question);assertEquals(3,original.size());session.conceptHelp=original.restore(null,session.question.id);session.conceptHelp.entries=new ArrayList<>(List.of(""+v[0],""+v[1],"7"));session.conceptHelp.stage=2;session.answers.add("13");String id=session.question.id,signature=session.question.signature();
   Learning.Session saved=roundTrip(session);HelpPlan reconstructed=HelpPlan.forQuestion(saved.question);HelpPlan.Draft retained=reconstructed.restore(saved.conceptHelp,saved.question.id);assertEquals(3,reconstructed.size());assertEquals(2,retained.stage);assertEquals(session.conceptHelp.entries,retained.entries);assertEquals("whole-number-relations-v1",retained.teachingVersion);assertEquals(List.of("13"),saved.answers);assertEquals(id,saved.question.id);assertEquals(signature,saved.question.signature());assertTrue(reconstructed.step(2).accepts(""+(v[0]-v[1])));assertFalse(reconstructed.step(2).accepts("999"));
  }
 }
 @Test public void newSavedDecompositionKeepsTheCurrentUnfinishedValue()throws Exception{
  Learning.Session session=new Learning.Session();session.question=new Question("sub20","18 - 12","999","999");HelpPlan p=HelpPlan.forQuestion(session.question);session.conceptHelp=p.restore(null,session.question.id);session.conceptHelp.stage=3;session.conceptHelp.entries=new ArrayList<>(List.of("18","12","10","7"));Learning.Session saved=roundTrip(session);HelpPlan restored=HelpPlan.forQuestion(saved.question);assertEquals(6,restored.size());assertEquals(3,restored.restore(saved.conceptHelp,saved.question.id).stage);assertEquals("7",saved.conceptHelp.entries.get(3));assertFalse(restored.step(3).accepts("7"));assertTrue(restored.step(3).accepts("2"));assertEquals("whole-subtract-parts-v2",saved.conceptHelp.teachingVersion);
 }
}
