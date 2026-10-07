package com.gomgomapps.math.core;
import org.junit.Test;import static org.junit.Assert.*;import java.util.*;import java.util.regex.*;import java.io.*;
public class WholeIntegerBatchTest {
 private static final List<String> UNITS=List.of("mulIntro","mul2","mul3","mul22","el_mul_3x2","divide2","remainder","el_div_2x2_rem","el_div_3x2_rem");
 private List<Long> expected(Question q){
  List<Long> values=new ArrayList<>();
  if(q.skillId.equals("mulIntro")){
   int each,groups,mode;String text=q.prompt;
   if(text.contains("씩 ")){Matcher m=Pattern.compile("(\\d+)씩 (\\d+)묶음.*").matcher(text);assertTrue(m.matches());each=Integer.parseInt(m.group(1));groups=Integer.parseInt(m.group(2));mode=0;}
   else{String[] sides=text.split("\\n")[0].split(" = "),terms=sides[0].split(" \\+ ");each=Integer.parseInt(terms[0]);groups=terms.length;mode=sides[1].equals("□")?1:sides[1].startsWith("□")?3:2;}
   values.add((long)(mode==3?groups:each));values.add((long)(mode==3?each:groups));if(mode<2)values.add((long)each*groups);return values;
  }
  Matcher m=Pattern.compile("(\\d+).*?[×÷]\\s*(\\d+)").matcher(q.prompt);assertTrue(m.find());long a=Long.parseLong(m.group(1)),b=Long.parseLong(m.group(2));values.add(a);values.add(b);
  if(WholeProductRelations.supports(q.skillId)){
   char[] left=String.valueOf(a).toCharArray(),right=String.valueOf(b).toCharArray();for(int j=right.length-1;j>=0;j--)for(int i=left.length-1;i>=0;i--){long x=left[i]-'0',y=right[j]-'0',product=x*y,power=(long)Math.pow(10,left.length-1-i+right.length-1-j);values.add(x);values.add(y);values.add(product);if(power>1)values.add(product*power);}values.add(a*b);
  }else{
   String digits=String.valueOf(a);int end=1;while(end<digits.length()&&Long.parseLong(digits.substring(0,end))<b)end++;long current=Long.parseLong(digits.substring(0,end));values.add(current);
   for(;;){values.add(current/b);values.add(current/b*b);values.add(current%b);if(end==digits.length())break;int digit=digits.charAt(end++)-'0';values.add((long)digit);current=current%b*10+digit;values.add(current);}values.add(a/b);if(WholeDivisionRelations.remainder(q.skillId))values.add(a%b);
  }return values;
 }
 private void verify(Question q){
  String[] answers=q.answers.clone();String signature=q.signature();List<Long> values=expected(q);HelpPlan p=HelpPlan.forQuestion(q);assertNotNull(q.prompt,p);assertFalse(p.canTransfer());assertEquals(q.prompt,values.size(),p.size());
  for(int i=0;i<p.size();i++){assertTrue(q.prompt+" step"+i,p.step(i).accepts(String.valueOf(values.get(i))));assertFalse(p.step(i).accepts(""));assertFalse(p.step(i).accepts(String.valueOf(values.get(i)+1)));assertEquals("",p.step(i).after);assertFalse(p.step(i).before.replaceAll("\\b10(?:0)*\\b",""),p.step(i).before.replaceAll("\\b10(?:0)*\\b","").matches(".*[0-9].*"));}
  assertArrayEquals(answers,q.answers);assertEquals(signature,q.signature());
 }
 @Test public void allNineUnitsKeepOriginalAnswersAndHaveBlankIndependentWork(){
  Generator g=new Generator(new Random(710815));for(String id:UNITS){Set<String> prompts=new HashSet<>();for(int i=0;i<600;i++){Question q=g.next(id,List.of(),i%2==0);assertNotNull(q.studyGuide);verify(q);prompts.add(q.prompt);Question old=new Question(id,q.prompt,"poison expression","poison answer");old.studyGuide=new StudyGuide().step("old","999 = ","999","999");verify(old);}assertTrue(id,prompts.size()>=100);}
 }
 @Test public void repeatedGroupsHaveAllFourModesAndRejectInconsistentVisibleFacts(){
  for(String prompt:List.of("3씩 4묶음은 모두 얼마인가요?","3 + 3 + 3 + 3 = □\n□에 들어갈 수는?","3 + 3 + 3 + 3 = 3 × □\n□에 들어갈 수는?","3 + 3 + 3 + 3 = □ × 4\n□에 들어갈 수는?")){verify(new Question("mulIntro",prompt,"999","999"));var v=RepeatedGroupingRelations.read(prompt);assertEquals(3,v.each());assertEquals(4,v.groups());}
  for(String prompt:List.of("3 + 4 = □\n□에 들어갈 수는?","3 + 3 = 4 × □\n□에 들어갈 수는?","3 + 3 = □ × 3\n□에 들어갈 수는?","999999999999씩 2묶음은 모두 얼마인가요?"))assertNull(RepeatedGroupingRelations.read(prompt));
 }
 @Test public void zeroDigitsLongProductsAndCountryDigitLimitsArePreserved(){
  for(long[] pair:new long[][]{{0,0},{999999,999999},{1000,101},{12345,10001},{100,1},{9,10}})for(String id:List.of("mul22","divide2","remainder")){if(id.equals("divide2")&&(pair[1]==0||pair[0]%pair[1]!=0))continue;if(!id.equals("mul22")&&pair[1]==0)continue;verify(new Question(id,pair[0]+(id.equals("mul22")?" × ":" ÷ ")+pair[1],"999","999"));}
  Generator g=new Generator(new Random(710816));for(String id:List.of("el_mul_3x2","el_div_3x2_rem"))for(int i=0;i<200;i++){Question q=g.next(id,List.of(),false,GlobalCurriculum.limits("ke-kicd-cbc-2024-v1",id,6));Matcher m=Pattern.compile("(\\d+) [×÷] (\\d+)").matcher(q.prompt);assertTrue(m.matches());assertEquals(4,m.group(1).length());assertEquals(id.equals("el_mul_3x2")?2:3,m.group(2).length());verify(q);}
 }
 @Test public void oldGuidesResetDraftsAndNewPartialWorkRestores()throws Exception{
  for(String id:UNITS){Question q=new Generator(new Random(50)).next(id,List.of(),false);HelpPlan p=HelpPlan.forQuestion(q);HelpPlan.Draft old=new HelpPlan.Draft();old.questionId=q.id;old.stage=1;old.entries=new ArrayList<>(List.of("999"));HelpPlan.Draft d=p.restore(old,q.id);assertEquals(0,d.stage);d.entries.set(0,String.valueOf(expected(q).get(0)));d.stage=1;ByteArrayOutputStream out=new ByteArrayOutputStream();new ObjectOutputStream(out).writeObject(d);HelpPlan.Draft saved=(HelpPlan.Draft)new ObjectInputStream(new ByteArrayInputStream(out.toByteArray())).readObject();assertEquals(1,p.restore(saved,q.id).stage);}
 }
}
