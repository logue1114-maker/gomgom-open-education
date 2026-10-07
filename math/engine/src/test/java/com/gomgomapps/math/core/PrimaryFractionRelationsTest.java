package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;import org.junit.Test;import static org.junit.Assert.*;
public class PrimaryFractionRelationsTest {
 private void check(Question q){
  String signature=q.signature();HelpPlan p=HelpPlan.forQuestion(q);assertNotNull(q.prompt,p);assertFalse(p.canTransfer());assertEquals(signature,q.signature());
  Matcher m=Pattern.compile("\\((\\d+)/(\\d+)\\)\\s*([+*/×÷-])\\s*\\((\\d+)/(\\d+)\\)").matcher(q.prompt.replace('−','-'));assertTrue(m.matches());
  long a=Long.parseLong(m.group(1)),b=Long.parseLong(m.group(2)),c=Long.parseLong(m.group(4)),d=Long.parseLong(m.group(5));String op=m.group(3);long n,den;
  if(op.equals("+")||op.equals("-")){n=a*d+(op.equals("+")?1:-1)*c*b;den=b*d;}else if(op.equals("/")||op.equals("÷")){n=a*d;den=b*c;}else{n=a*c;den=b*d;}
  List<String> values=q.studyGuide.frames.stream().map(f->f.expected).toList();int size=values.size();boolean like=q.skillId.endsWith("Like");long rn=Long.parseLong(values.get(like?3:size-2)),rd=Long.parseLong(values.get(like?2:size-1));assertEquals(n*rd,den*rn);assertTrue(rd>0);
  if(!like)for(long divisor=2;divisor<=rd;divisor++)assertFalse(rn%divisor==0&&rd%divisor==0);
  for(int i=0;i<size;i++){assertFalse(p.step(i).before.matches(".*[0-9].*"));assertEquals("",p.step(i).after);assertTrue(p.step(i).accepts(values.get(i)));assertFalse(p.step(i).accepts(""));assertFalse(p.step(i).accepts(Expression.number(values.get(i)).add(Rational.ONE).toString()));}
  HelpPlan.Draft draft=new HelpPlan.Draft();draft.entries=new ArrayList<>(values);draft.stage=size;assertEquals(Rational.of(n,den),Expression.number(p.enteredAnswer(draft)));
  q.expression="999";q.answers=new String[]{"999"};q.givenNumbers.put("result","999");HelpPlan.forQuestion(q);assertEquals(values,q.studyGuide.frames.stream().map(f->f.expected).toList());
 }
 @Test public void sixUnitsHaveDistinctProblemsAndCorrectResults(){Generator g=new Generator(new Random(710108));for(String id:List.of("fracAddLike","fracSubLike","fracAdd","fracSub","fracMul","fracDiv")){Set<String> seen=new HashSet<>();for(int i=0;i<4000;i++){Question q=g.next(id,List.of(),i%2==0);seen.add(q.prompt);check(q);assertEquals(id.endsWith("Like")?4:id.equals("fracMul")?9:11,q.studyGuide.frames.size());}assertTrue(id+" "+seen.size(),seen.size()>100);}}
 @Test public void zeroCancellationAndInvalidDivision(){for(String id:List.of("fracSubLike","fracSub"))check(new Question(id,"(3/7) - (3/7)","","0"));check(new Question("fracMul","(0/3) * (4/5)","","0"));for(String prompt:List.of("(1/2) / (0/3)","(1/0) / (2/3)")){Question q=new Question("fracDiv",prompt,"","0");PrimaryFractionRelations.attach(q);assertNull(q.studyGuide);}}
 @Test public void savedDraftsUseNewFrames(){Question q=new Question("fracAddLike","(2/5) + (1/5)","","3/5");q.studyGuide=new StudyGuide().step("old","2 + 1 = ","","3");HelpPlan.Draft d=new HelpPlan.Draft();d.questionId=q.id;d.stage=1;d.entries=new ArrayList<>(List.of("3"));HelpPlan p=HelpPlan.forQuestion(q);p.restore(d,q.id);assertEquals(0,d.stage);assertEquals(List.of(""),d.entries);d.entries.set(0,"2");d.stage=1;p.restore(d,q.id);assertEquals(1,d.stage);}
}
