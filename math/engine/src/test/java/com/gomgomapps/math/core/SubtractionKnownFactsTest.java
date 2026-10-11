package com.gomgomapps.math.core;
import org.junit.Test;import static org.junit.Assert.*;import java.util.*;import java.io.*;
public class SubtractionKnownFactsTest {
 @Test public void zeroAndSameAmountExplainThePublicFactsWithoutPrefilling() {
  for(int a=0;a<=20;a++)for(int b:new int[]{0,a}){
   Question q=new Question("sub20",a+" - "+b,"999","999");HelpPlan p=HelpPlan.forQuestion(q);
   assertEquals(3,p.size());assertFalse(p.canTransfer());assertTrue(p.step(0).accepts(""+a));assertTrue(p.step(1).accepts(""+b));
   assertTrue(p.step(2).accepts(""+(a-b)));assertFalse(p.step(2).accepts(""));assertFalse(p.step(2).accepts(""+(a-b+1)));
   assertTrue(p.step(2).instruction,b==0?p.step(2).instruction.startsWith("0을 빼면"):p.step(2).instruction.startsWith("같은 수를 모두 빼면"));
   assertEquals("첫 수 − 둘째 수 = ",p.step(2).before);assertEquals("",p.step(2).after);assertEquals("999",q.answers[0]);
  }
 }
 @Test public void serializedDraftsKeepTheSameQuestionEntriesAndStage() throws Exception {
  for(int[] pair:new int[][]{{3,0},{18,18},{0,0}}){Question q=new Question("sub20",pair[0]+" - "+pair[1],"",Integer.toString(pair[0]-pair[1]));HelpPlan p=HelpPlan.forQuestion(q);HelpPlan.Draft d=p.restore(null,q.id);d.entries=new ArrayList<>(List.of(""+pair[0],""+pair[1],"7"));d.stage=2;
   ByteArrayOutputStream out=new ByteArrayOutputStream();new ObjectOutputStream(out).writeObject(d);HelpPlan.Draft restored=(HelpPlan.Draft)new ObjectInputStream(new ByteArrayInputStream(out.toByteArray())).readObject();assertEquals(2,p.restore(restored,q.id).stage);assertEquals("7",restored.entries.get(2));assertEquals(d.teachingVersion,restored.teachingVersion);assertEquals(d.questionId,restored.questionId);
  }
 }
 @Test public void existingTenBreakdownAndOtherArithmeticRemainAvailable(){
  Question q=new Question("sub20","13 - 7","999","999");HelpPlan p=HelpPlan.forQuestion(q);assertEquals(5,p.size());int[] values={13,7,3,3,6};for(int i=0;i<values.length;i++)assertTrue(p.step(i).accepts(""+values[i]));
  for(String id:List.of("add20","sub20")){Question other=new Question(id,id.equals("add20")?"3 + 0":"8 - 3","","");HelpPlan help=HelpPlan.forQuestion(other);assertFalse(help.step(help.size()-1).instruction.startsWith("0을 빼면"));assertFalse(help.step(help.size()-1).instruction.startsWith("같은 수를 모두 빼면"));}
 }
}
