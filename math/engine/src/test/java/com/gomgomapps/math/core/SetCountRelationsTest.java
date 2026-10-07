package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;import org.junit.Test;import static org.junit.Assert.*;
public class SetCountRelationsTest {
 private List<Rational> values(Question q){
  List<Rational> v=new ArrayList<>();Matcher m=Pattern.compile("[0-9]+").matcher(q.prompt);while(m.find())v.add(Expression.number(m.group()));
  if(q.prompt.startsWith("전체집합 U")){assertEquals(2,v.size());return List.of(v.get(0),v.get(1),v.get(0).sub(v.get(1)));}
  assertEquals(3,v.size());Rational a=v.get(0),b=v.get(1),c=v.get(2),sum=a.add(b);
  if(q.skillId.equals("sec_set_intersection"))return q.prompt.contains("n(A∪B)")?List.of(a,b,c,sum,sum.sub(c)):List.of(c);
  return q.skillId.equals("sec_set_union")?List.of(a,b,c,sum,sum.sub(c)):List.of(a,c,a.sub(c));
 }
 private void verify(Question q){
  List<Rational> v=values(q);assertTrue(new Checker().check(q,List.of(),List.of(v.get(v.size()-1).toString())).correct());
  String id=q.id,prompt=q.prompt,signature=q.signature();Arrays.fill(q.answers,"9999");q.givenNumbers.replaceAll((k,x)->"9999");q.choiceInputs=new Rational[]{Rational.of(9999)};
  HelpPlan p=HelpPlan.forQuestion(q);assertEquals(v.size(),p.size());assertFalse(p.canTransfer());assertEquals(id,q.id);assertEquals(prompt,q.prompt);assertEquals(signature,q.signature());
  for(int i=0;i<v.size();i++){assertTrue(p.step(i).accepts(v.get(i).toString()));assertFalse(p.step(i).accepts(v.get(i).add(Rational.ONE).toString()));assertEquals("",p.step(i).after);assertFalse(p.step(i).before.matches(".*[0-9].*"));}
 }
 @Test public void allSmallCountPromptsDetermineIndependentCalculations(){
  Generator g=new Generator(new Random(2501));for(String skill:List.of("sec_set_intersection","sec_set_union","sec_set_difference")){Set<String> prompts=new HashSet<>();Set<Integer> positions=new HashSet<>();
   for(int i=0;i<4000;i++){Question q=g.next(skill,List.of(),i%2==0);prompts.add(q.prompt);if(q.correctChoice>=0)positions.add(q.correctChoice);if(skill.equals("sec_set_intersection"))assertTrue(q.prompt.contains("n(A∪B)"));verify(q);}
   assertEquals(skill.equals("sec_set_difference")?248:105,prompts.size());assertEquals(Set.of(0,1,2,3),positions);
  }
 }
 @Test public void complementIncludesEmptyAndFullSetsWithoutPrefill(){
  for(int u=4;u<=16;u++)for(int a=0;a<=u;a++){
   Question q=new Question("sec_set_difference","전체집합 U에서 A⊆U이고 n(U)="+u+", n(A)="+a+"일 때 A의 여집합의 원소 수는?","",Integer.toString(u-a));verify(q);
  }
 }
 @Test public void publishedReadingExerciseAndZeroFullOverlapsKeepIdentityButResetOldHelp(){
  for(Question q:List.of(new Question("sec_set_intersection","n(A)=4, n(B)=5, n(A∩B)=2일 때 교집합의 원소 수는?","","2"),new Question("sec_set_intersection","n(A)=0, n(B)=3, n(A∪B)=3일 때 교집합의 원소 수는?","","0"),new Question("sec_set_union","n(A)=4, n(B)=4, n(A∩B)=4일 때 합집합의 원소 수는?","","4"),new Question("sec_set_difference","n(A)=4, n(B)=4, n(A∩B)=4일 때 차집합 A-B의 원소 수는?","","0"))){
   q.studyGuide=new StudyGuide().step("옛 도움","4 + 5 = ","","9");verify(q);HelpPlan p=HelpPlan.forQuestion(q);HelpPlan.Draft draft=new HelpPlan.Draft();draft.questionId=q.id;draft.stage=1;draft.entries=new ArrayList<>(List.of("4"));p.restore(draft,q.id);assertEquals(0,draft.stage);assertEquals("",draft.entries.get(0));draft.entries.set(0,values(q).get(0).toString());draft.stage=1;assertEquals(1,HelpPlan.forQuestion(q).restore(draft.copy(),q.id).stage);
  }
 }
}
