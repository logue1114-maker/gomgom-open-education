package com.gomgomapps.math.core;
import org.junit.Test;import static org.junit.Assert.*;import java.util.*;import java.util.regex.*;import java.io.*;
public class WholeNumberRelationsTest {
 private void verify(Question q){
  List<Integer> terms=new ArrayList<>();Matcher m=Pattern.compile("\\d+").matcher(q.prompt);while(m.find())terms.add(Integer.parseInt(m.group()));boolean add=q.skillId.startsWith("add");int a=terms.get(0),b=terms.get(1);List<Integer> expected=new ArrayList<>(List.of(a,b));
  if(terms.size()==3){int c=terms.get(2),partial=add?a+b:a-b;expected.add(c);expected.add(partial);expected.add(add?partial+c:partial-c);}
  else if(add&&a>0&&a<10&&b<10&&a+b>=10){expected.add(10-a);expected.add(a+b-10);expected.add(a+b);}
  else if(!add&&a>=10&&a<=18&&b>a%10&&b<=9){expected.add(a%10);expected.add(10-b);expected.add(a-b);}
  else expected.add(add?a+b:a-b);
  HelpPlan p=HelpPlan.forQuestion(q);assertNotNull(q.prompt,p);assertEquals(expected.size(),p.size());assertFalse(p.canTransfer());
  for(int i=0;i<p.size();i++){assertTrue(p.step(i).accepts(String.valueOf(expected.get(i))));assertFalse(p.step(i).accepts(""));assertFalse(p.step(i).accepts(String.valueOf(expected.get(i)+1)));assertEquals("",p.step(i).after);assertFalse(p.step(i).before,p.step(i).before.replace("10","").matches(".*[0-9].*"));}
 }
 @Test public void allEightUnitsAndCountryWholeDigitLimitsUseOriginalPublicTerms(){
  Generator g=new Generator(new Random(710813));for(String id:List.of("add9","sub9","add20","sub20","addThree9","subThree9","addThree100","subThree100"))for(int i=0;i<1000;i++){Question q=g.next(id,List.of(),false);String[] answer=q.answers.clone();String signature=q.signature();verify(q);Question poisoned=new Question(id,q.prompt,"999999","999999");poisoned.studyGuide=new StudyGuide().step("old","999 = ","","999");verify(poisoned);assertArrayEquals(answer,q.answers);assertEquals(signature,q.signature());}
  for(String id:List.of("addThree100","subThree100"))for(int i=0;i<200;i++)verify(g.next(id,List.of(),false,GlobalCurriculum.limits("br-bncc-fundamental-2017-v1",id,2)));
 }
 @Test public void quantitiesUseOnlyOriginalGivenCountsAndSavedDraftsVersion()throws Exception{
  Question q=new Question("add20","8 + 7","999","999");HelpPlan p=HelpPlan.forQuestion(q);assertEquals(8,p.step(2).filled);assertEquals(7,p.step(2).extra);assertEquals(10,p.step(2).denominator);
  HelpPlan.Draft d=new HelpPlan.Draft();d.questionId=q.id;d.stage=3;d.entries=new ArrayList<>(List.of("2","5","15"));d=p.restore(d,q.id);assertEquals(0,d.stage);d.entries.set(0,"8");d.stage=1;
  ByteArrayOutputStream out=new ByteArrayOutputStream();new ObjectOutputStream(out).writeObject(d);HelpPlan.Draft saved=(HelpPlan.Draft)new ObjectInputStream(new ByteArrayInputStream(out.toByteArray())).readObject();assertEquals(1,p.restore(saved,q.id).stage);verify(q);
  q=new Question("sub20","13 − 7","999","999");p=HelpPlan.forQuestion(q);assertEquals(10,p.step(3).filled);assertEquals(-7,p.step(3).extra);verify(q);
  StudyGuide.Frame legacy=new StudyGuide().step("old","","","1").frames.get(0);assertNull(legacy.pictureFilled);
 }
}
