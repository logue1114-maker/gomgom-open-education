package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;import org.junit.Test;import static org.junit.Assert.*;
public class SubsetRelationsTest {
 private List<Integer> numbers(String s){List<Integer> v=new ArrayList<>();Matcher m=Pattern.compile("[0-9]+").matcher(s);while(m.find())v.add(Integer.parseInt(m.group()));return v;}
 private List<Integer> values(Question q){
  List<Integer> result=new ArrayList<>();Matcher m=Pattern.compile("B=\\{([^}]*)\\}").matcher(q.prompt);
  if(q.prompt.endsWith("몇 개인가요?")){
   int n=m.find()?new HashSet<>(numbers(m.group(1))).size():numbers(q.prompt).get(0);result.add(n);result.add(2);result.add(1);for(int i=1;i<=n;i++)result.add(1<<i);return result;
  }
  assertTrue(m.find());Set<Integer> b=new HashSet<>(numbers(m.group(1)));if(m.group(1).contains("...")){int end=numbers(m.group(1)).get(2);for(int i=1;i<=end;i++)b.add(i);}
  m=Pattern.compile("A=\\{([^}]*)\\}").matcher(q.prompt);assertTrue(m.find());Set<Integer> a=new LinkedHashSet<>(numbers(m.group(1)));for(int x:a){result.add(x);result.add(b.contains(x)?1:0);}result.add(b.containsAll(a)?1:0);return result;
 }
 private void verify(Question q){
  List<Integer> v=values(q);assertTrue(new Checker().check(q,List.of(),List.of(Integer.toString(v.get(v.size()-1)))).correct());String id=q.id,prompt=q.prompt,signature=q.signature();Arrays.fill(q.answers,"9999");q.givenNumbers.replaceAll((k,x)->"9999");q.choiceInputs=new Rational[]{Rational.of(9999)};
  HelpPlan p=HelpPlan.forQuestion(q);assertEquals(v.size(),p.size());assertFalse(p.canTransfer());assertEquals(id,q.id);assertEquals(prompt,q.prompt);assertEquals(signature,q.signature());
  for(int i=0;i<v.size();i++){var step=p.step(i);assertTrue(step.accepts(Integer.toString(v.get(i))));assertFalse(step.accepts(Integer.toString(step.options.isEmpty()?v.get(i)+1:1-v.get(i))));assertEquals("",step.after);assertFalse(step.before.matches(".*[0-9].*"));}
 }
 @Test public void variedExplicitSetsSupportCountingAndBothContainmentResults(){
  Generator g=new Generator(new Random(2601));Set<String> counts=new HashSet<>(),yes=new HashSet<>(),no=new HashSet<>();Set<Integer> positions=new HashSet<>();
  for(int i=0;i<4000;i++){Question q=g.next("sec_subset",List.of(),i%2==0);assertFalse(q.prompt.contains("..."));assertFalse(q.prompt.startsWith("원소가"));if(q.prompt.startsWith("집합 B=")){counts.add(q.prompt);if(q.correctChoice>=0)positions.add(q.correctChoice);}else if(values(q).get(4)==1)yes.add(q.prompt);else no.add(q.prompt);verify(q);}
  assertEquals(120,counts.size());assertTrue(yes.size()>100);assertTrue(no.size()>100);assertEquals(Set.of(0,1,2,3),positions);
 }
 @Test public void savedCountAndEllipsisSetsKeepIdentityAndResetOldDraft(){
  for(Question q:List.of(new Question("sec_subset","원소가 4개인 집합의 부분집합은 모두 몇 개인가요?","","16"),new Question("sec_subset","B={1, 2, ..., 6}일 때 A={1, 7}가 B의 부분집합인지 고르세요.","","0"),new Question("sec_subset","집합 B={}의 부분집합은 모두 몇 개인가요?","","1"),new Question("sec_subset","집합 B={2, 2, 5}의 부분집합은 모두 몇 개인가요?","","4"),new Question("sec_subset","B={2, 4}일 때 A={}가 B의 부분집합인지 고르세요.","","1"))){
   q.studyGuide=new StudyGuide().step("옛 도움","2^4 = ","","16");verify(q);HelpPlan p=HelpPlan.forQuestion(q);HelpPlan.Draft draft=new HelpPlan.Draft();draft.questionId=q.id;draft.stage=1;draft.entries=new ArrayList<>(List.of("4"));p.restore(draft,q.id);assertEquals(0,draft.stage);assertEquals("",draft.entries.get(0));draft.entries.set(0,Integer.toString(values(q).get(0)));draft.stage=1;assertEquals(1,HelpPlan.forQuestion(q).restore(draft.copy(),q.id).stage);
  }
 }
}
