package com.gomgomapps.math.core;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class CompleteFactorPracticeTest {
 private static List<String> independentlySolve(String id,int a,int b){
  List<String> result=new ArrayList<>();
  if(id.equals(CompleteFactorPractice.PAIRS)){for(int d=1;d<=a;d++)if(a%d==0&&d<=a/d){result.add(""+d);result.add(""+(a/d));}}
  else if(id.equals(CompleteFactorPractice.COMMON)){for(int d=1;d<=Math.min(a,b);d++)if(a%d==0&&b%d==0)result.add(""+d);}
  else{int sum=0;for(int i=0;i<5;i++){sum+=a;result.add(""+sum);}}
  return result;
 }
 @Test public void everyPublicConditionHasACompleteUniqueAnswerIndependentOfStoredKeys(){
  Checker checker=new Checker();
  for(String id:List.of(CompleteFactorPractice.PAIRS,CompleteFactorPractice.COMMON,CompleteFactorPractice.MULTIPLES)){
   Set<String> signatures=new HashSet<>();int independentCount=id.equals(CompleteFactorPractice.COMMON)?144*143/2:id.equals(CompleteFactorPractice.PAIRS)?144:143;
   assertEquals(independentCount,CompleteFactorPractice.count(id));assertTrue(independentCount>=100);
   for(int index=0;index<independentCount;index++){
    Question q=CompleteFactorPractice.indexed(id,index);assertTrue(signatures.add(q.signature()));
    int[] given=CompleteFactorPractice.read(q);assertNotNull(given);List<String> solution=independentlySolve(id,given[0],given.length==2?given[1]:0);
    assertEquals(solution.size(),q.labels.length);q.answers=new String[]{"poison"};q.expression="poison";
    assertTrue(checker.check(q,List.of(),solution).correct());
    List<String> wrong=new ArrayList<>(solution);int last=wrong.size()-1;wrong.set(last,""+(Integer.parseInt(wrong.get(last))+1));
    assertEquals(Checker.Status.WRONG_ANSWER,checker.check(q,List.of(),wrong).status);assertEquals(last,checker.check(q,List.of(),wrong).index);
   }
  }
 }
 @Test public void emptyFramesCheckPublicRelationsAndNeverTransferNumbers(){
  for(Question q:List.of(CompleteFactorPractice.make(CompleteFactorPractice.PAIRS,144,0),CompleteFactorPractice.make(CompleteFactorPractice.COMMON,120,144),CompleteFactorPractice.make(CompleteFactorPractice.MULTIPLES,144,0))){
   int[] given=CompleteFactorPractice.read(q);List<String> solution=independentlySolve(q.skillId,given[0],given.length==2?given[1]:0);
   q.answers=new String[]{"poison"};CompleteFactorPractice.attach(q);HelpPlan help=HelpPlan.forQuestion(q);assertFalse(help.canTransfer());
   assertTrue(help.restore(null,q.id).entries.stream().allMatch(String::isEmpty));int step=0;
   if(q.skillId.equals(CompleteFactorPractice.PAIRS))for(int i=0;i<solution.size();i+=2){assertTrue(help.step(step++).accepts(solution.get(i)));assertTrue(help.step(step++).accepts(solution.get(i+1)));assertTrue(help.step(step++).accepts(""+given[0]));}
   else if(q.skillId.equals(CompleteFactorPractice.COMMON))for(String value:solution){int d=Integer.parseInt(value);assertTrue(help.step(step++).accepts(value));assertTrue(help.step(step++).accepts(""+(given[0]/d)));assertTrue(help.step(step++).accepts(""+(given[1]/d)));}
   else for(int k=1;k<=5;k++){assertTrue(help.step(step++).accepts(""+k));assertTrue(help.step(step++).accepts(solution.get(k-1)));}
   assertEquals(step,help.size());for(int i=0;i<help.size();i++){assertFalse(help.step(i).before.matches("(?s).*[0-9].*"));assertFalse(help.step(i).accepts("999"));}
  }
 }
 @Test public void normalGeneratorUsesFreshSupplyThenOldestAndOnlyTheReviewedGrade(){
  Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"GB");GlobalCurriculum.choosePack(p,"england-primary-2021-v1");GlobalCurriculum.Pack pack=GlobalCurriculum.pack(p);
  Generator generator=new Generator(new Random(162));
  for(String id:List.of(CompleteFactorPractice.PAIRS,CompleteFactorPractice.COMMON,CompleteFactorPractice.MULTIPLES)){
   assertTrue(pack.inGrade(id,5));assertFalse(pack.inGrade(id,4));assertFalse(pack.inGrade(id,6));assertFalse(Curriculum.inCurriculum(Catalog.get(id),2022));
   CurriculumLimits limits=GlobalCurriculum.limits(pack.id,id,5);List<String> history=new ArrayList<>();
   for(int i=0;i<100;i++){Question q=generator.next(id,history,false,limits);assertNotNull(CompleteFactorPractice.read(q));assertFalse(history.contains(q.signature()));history.add(q.signature());}
   Map<String,Integer> all=new LinkedHashMap<>();for(int i=0;i<CompleteFactorPractice.count(id);i++)all.put(CompleteFactorPractice.indexed(id,i).signature(),i);
   assertEquals(CompleteFactorPractice.indexed(id,0).signature(),CompleteFactorPractice.next(id,new Random(1),limits,all).signature());
  }
 }
 @Test public void primesSquaresCoprimesMissingDuplicatesAndCorruptPromptsAreHandled(){
  Checker c=new Checker();Question pair=CompleteFactorPractice.make(CompleteFactorPractice.PAIRS,36,0);
  assertTrue(c.check(pair,List.of(),List.of("1","36","2","18","3","12","4","9","6","6")).correct());
  assertTrue(c.check(CompleteFactorPractice.make(CompleteFactorPractice.PAIRS,137,0),List.of(),List.of("1","137")).correct());
  assertTrue(c.check(CompleteFactorPractice.make(CompleteFactorPractice.PAIRS,1,0),List.of(),List.of("1","1")).correct());
  assertTrue(c.check(CompleteFactorPractice.make(CompleteFactorPractice.COMMON,13,17),List.of(),List.of("1")).correct());
  assertEquals(Checker.Status.WRONG_ANSWER,c.check(pair,List.of(),List.of("1","36")).status);
  assertEquals(-1,c.check(pair,List.of(),List.of("1","36")).index);
  assertEquals(2,c.check(pair,List.of(),List.of("1","36","1","36","3","12","4","9","6","6")).index);
  List<String> blank=new ArrayList<>(independentlySolve(CompleteFactorPractice.PAIRS,36,0));blank.set(3,"");assertEquals(3,c.check(pair,List.of(),blank).index);
  assertNull(CompleteFactorPractice.read(new Question(CompleteFactorPractice.PAIRS,"145의 모든 곱셈짝을 쓰세요.\n각 짝은 작은 수부터, 짝의 순서도 작은 수부터 쓰세요.","","1")));
  assertNull(CompleteFactorPractice.read(new Question(CompleteFactorPractice.COMMON,"12와 12의 공약수를 모두 쓰세요.\n작은 수부터 쓰세요.","","1")));
 }
}
