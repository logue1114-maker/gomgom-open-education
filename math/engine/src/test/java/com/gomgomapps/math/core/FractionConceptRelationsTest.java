package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;import java.io.*;import org.junit.Test;import static org.junit.Assert.*;
public class FractionConceptRelationsTest {
 private void check(Question q){
  String signature=q.signature();HelpPlan p=HelpPlan.forQuestion(q);assertNotNull(p);assertFalse(p.canTransfer());assertEquals(signature,q.signature());Matcher m=Pattern.compile("\\d+").matcher(q.prompt);List<Long> numbers=new ArrayList<>();while(m.find())numbers.add(Long.parseLong(m.group()));long x=numbers.get(0),y=numbers.get(1),z=numbers.get(2);List<Long> values;String result;
  switch(q.skillId){
   case "el_mixed_to_improper":values=List.of(x,y,z,x*z,x*z+y);result=(x*z+y)+"/"+z;break;
   case "el_improper_to_mixed":values=List.of(x,y,x/y,x%y);result=(x/y)+" "+(x%y)+"/"+y;assertTrue(x%y<y);break;
   case "el_fraction_common_den":values=List.of(x,y,z,z/y,x*(z/y));result=(x*(z/y))+"/"+z;assertEquals(x*z,(x*(z/y))*y);break;
   default:values=List.of(x,y,z,x/z,(x/z)*y);result=""+((x/z)*y);
  }
  assertEquals(values.size(),p.size());for(int i=0;i<p.size();i++){assertEquals("",p.step(i).after);assertFalse(p.step(i).before.matches(".*[0-9].*"));assertTrue(p.step(i).accepts(""+values.get(i)));assertFalse(p.step(i).accepts(""));assertFalse(p.step(i).accepts(""+(values.get(i)+1)));}
  HelpPlan.Draft d=new HelpPlan.Draft();d.stage=p.size();d.entries=new ArrayList<>(values.stream().map(String::valueOf).toList());assertEquals(result,p.enteredAnswer(d));assertFalse(FractionInput.available(q));
  List<String> before=q.studyGuide.frames.stream().map(f->f.expected).toList();q.expression="999";q.answers=new String[]{"999"};q.givenNumbers.put("solution","999");HelpPlan.forQuestion(q);assertEquals(before,q.studyGuide.frames.stream().map(f->f.expected).toList());
 }
 @Test public void fourConceptsHaveDistinctQuestionsAndBlankFrames(){Generator g=new Generator(new Random(710110));for(String id:List.of("el_mixed_to_improper","el_improper_to_mixed","el_fraction_common_den","el_fraction_of_number")){Set<String> seen=new HashSet<>();for(int i=0;i<4000;i++){Question q=g.next(id,List.of(),false);seen.add(q.prompt);check(q);}assertTrue(id+" "+seen.size(),seen.size()>100);}}
 @Test public void savedMixedResultSurvivesSerializationAndSnapshot()throws Exception{Question q=new Generator(new Random(710111)).next("el_improper_to_mixed",List.of(),false);check(q);ByteArrayOutputStream bytes=new ByteArrayOutputStream();new ObjectOutputStream(bytes).writeObject(q);Question saved=(Question)new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray())).readObject();assertEquals(q.studyGuide.resultWholeFrame,saved.studyGuide.resultWholeFrame);check(saved);}
 @Test public void oldDraftResetsAndNewDraftRestores(){Question q=new Question("el_mixed_to_improper","2와 1/3을 가분수로 나타내세요.\n□/3","","7");q.studyGuide=new StudyGuide().step("old","2 × 3 = ","","6");HelpPlan.Draft d=new HelpPlan.Draft();d.questionId=q.id;d.stage=1;d.entries=new ArrayList<>(List.of("6"));HelpPlan p=HelpPlan.forQuestion(q);d=p.restore(d,q.id);assertEquals(0,d.stage);d.entries.set(0,"2");d.stage=1;d=p.restore(d,q.id);assertEquals(1,d.stage);}
}
