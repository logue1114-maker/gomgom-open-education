package com.gomgomapps.math.core;
import java.util.*;
import java.util.regex.*;
import org.junit.Test;
import static org.junit.Assert.*;
public class MatrixWholePracticeTest {
 @Test public void wholeAnswersAndEveryBlankStageFollowPublicMatrices(){
  Generator g=new Generator(new Random(2026100830));Checker checker=new Checker();
  for(String id:List.of("matrixWholeAdd","matrixWholeSub")){
   Set<String> signatures=new LinkedHashSet<>();Set<String> shapes=new HashSet<>();
   for(int n=0;n<200;n++){
    Question q=g.next(id,signatures,false);assertTrue(signatures.add(q.signature()));
    String[] lines=q.prompt.split("\n");List<Integer> a=numbers(lines[0]),b=numbers(lines[1]);int rows=lines[0].split("\\],\\[").length,cols=a.size()/rows;shapes.add(rows+"x"+cols);
    List<String> answers=new ArrayList<>();for(int i=0;i<a.size();i++)answers.add(Integer.toString(id.endsWith("Sub")?a.get(i)-b.get(i):a.get(i)+b.get(i)));
    assertEquals(answers,Arrays.asList(q.answers));assertTrue(checker.check(q,List.of(),answers).correct());
    for(int i=0;i<answers.size();i++){List<String> wrong=new ArrayList<>(answers);wrong.set(i,Integer.toString(Integer.parseInt(answers.get(i))+1));Checker.Result result=checker.check(q,List.of(),wrong);assertEquals(Checker.Status.WRONG_ANSWER,result.status);assertEquals(i,result.index);}
    String signature=q.signature();q.answers=new String[]{"99999"};q.studyGuide=new StudyGuide().step("old","99999 = ","","99999");HelpPlan h=HelpPlan.forQuestion(q);
    assertEquals(signature,q.signature());assertFalse(h.canTransfer());assertEquals(a.size()*3,h.size());
    for(int i=0;i<a.size();i++){
     for(int j=0;j<3;j++){String expected=j==0?a.get(i).toString():j==1?b.get(i).toString():answers.get(i);assertTrue(h.step(i*3+j).accepts(expected));assertFalse(h.step(i*3+j).accepts("99999"));assertFalse(h.step(i*3+j).before.matches(".*\\d.*"));}
    }
    HelpPlan.Draft draft=h.restore(null,q.id);draft.entries.set(0,a.get(0).toString());draft.stage=1;assertEquals(1,HelpPlan.forQuestion(q).restore(draft.copy(),q.id).stage);
   }assertEquals(Set.of("2x2","2x3","3x2","3x3"),shapes);
  }
 }
 @Test public void kenyaGradeNineIncludesFullMatricesAndDiagnosisWaitsForLearning(){
  Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"KE");GlobalCurriculum.choosePack(p,"ke-kicd-cbc-2024-v1");p.grade=9;
  assertTrue(GlobalCurriculum.pack(p).inGrade("matrixWholeAdd",9));assertTrue(GlobalCurriculum.pack(p).inGrade("matrixWholeSub",9));
  assertFalse(Learning.diagnosticScope(p).contains(Catalog.get("matrixWholeAdd")));p.learnedSkills.add("matrixWholeAdd");assertTrue(Learning.diagnosticScope(p).contains(Catalog.get("matrixWholeAdd")));
 }
 private static List<Integer> numbers(String line){List<Integer> result=new ArrayList<>();Matcher m=Pattern.compile("-?\\d+").matcher(line);while(m.find())result.add(Integer.valueOf(m.group()));return result;}
}
