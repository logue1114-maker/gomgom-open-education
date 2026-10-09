package com.gomgomapps.math.core;
import java.util.*;import java.io.*;import org.junit.Test;import static org.junit.Assert.*;
public class PrimaryPairEnumerationTest {
 @Test public void all747PublicDomainsRejectMissingDuplicateAndInvalidPairsInAnyOrder(){
  int conditions=0;Checker checker=new Checker();
  for(int a=1;a<=6;a++)for(int b=1;b<=6;b++)for(int c=2;c<=36;c++){
   List<List<String>> pairs=new ArrayList<>();for(int x=0;x<=c;x++)for(int y=0;y<=c;y++)if(a*x+b*y==c)pairs.add(List.of(""+x,""+y));if(pairs.size()<2)continue;conditions++;
   Question q=PrimaryPairEnumeration.create(a,b,c);List<String> own=new ArrayList<>(List.of("",""));assertFalse(checker.check(q,List.of(),own).correct());
   Collections.shuffle(pairs,new Random(a*1000+b*100+c));for(var pair:pairs){own.set(0,pair.get(0));own.set(1,pair.get(1));assertTrue(PrimaryPairEnumeration.add(q,own).correct());assertEquals("",own.get(0));assertEquals("",own.get(1));}
   q.answers=new String[]{"999"};assertTrue(checker.check(q,List.of(),own).correct());
   List<String> duplicate=new ArrayList<>(own);duplicate.addAll(pairs.get(0));assertEquals("이미 쓴 쌍",checker.check(q,List.of(),duplicate).message);
   PrimaryPairEnumeration.remove(own,0);assertEquals("아직 빠진 쌍이 있습니다.",checker.check(q,List.of(),own).message);
   own.set(0,"999");own.set(1,"999");List<String> before=new ArrayList<>(own);assertEquals(Checker.Status.WRONG_ANSWER,PrimaryPairEnumeration.add(q,own).status);assertEquals(before,own);
   own.set(0,"1/2");assertEquals(Checker.Status.INPUT_NEEDED,PrimaryPairEnumeration.add(q,own).status);
   HelpPlan plan=HelpPlan.forQuestion(q);assertFalse(plan.canTransfer());assertEquals(5,plan.size());for(var f:q.studyGuide.frames){assertFalse(f.before.matches(".*[0-9].*"));assertNull(f.inputFormat);}
  }assertEquals(747,conditions);
 }
 @Test public void duplicateAddKeepsDraftAndSerializationPreservesLearnerListAndSearchDraft()throws Exception{
  Learning.Session s=new Learning.Session();s.question=PrimaryPairEnumeration.create(2,3,12);s.answers=new ArrayList<>(List.of("3","2"));assertTrue(PrimaryPairEnumeration.add(s.question,s.answers).correct());
  s.answers.set(0,"3");s.answers.set(1,"2");List<String> before=new ArrayList<>(s.answers);assertEquals("이미 쓴 쌍",PrimaryPairEnumeration.add(s.question,s.answers).message);assertEquals(before,s.answers);
  var plan=HelpPlan.forQuestion(s.question);s.conceptHelp=plan.restore(null,s.question.id);s.conceptHelp.stage=3;s.conceptHelp.entries=new ArrayList<>(List.of("2","3","12",""));
  ByteArrayOutputStream bytes=new ByteArrayOutputStream();new ObjectOutputStream(bytes).writeObject(s);Learning.Session restored=(Learning.Session)new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray())).readObject();
  assertEquals(before,restored.answers);assertEquals(3,HelpPlan.forQuestion(restored.question).restore(restored.conceptHelp,restored.question.id).stage);
  assertEquals("쌍 추가 필요",new Checker().check(restored.question,List.of(),restored.answers).message);
 }
 @Test public void reviewedEnglandOnlyAnd100FreshQuestions(){
  Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"GB");GlobalCurriculum.choosePack(p,"england-primary-2021-v1");assertTrue(GlobalCurriculum.pack(p).inGrade(PrimaryPairEnumeration.ID,6));assertFalse(GlobalCurriculum.pack(p).inGrade(PrimaryPairEnumeration.ID,5));
  Generator gen=new Generator(new Random(105));Set<String> seen=new LinkedHashSet<>();for(int i=0;i<100;i++){Question q=gen.next(PrimaryPairEnumeration.ID,seen,false);assertTrue(seen.add(q.signature()));assertNotNull(PrimaryPairEnumeration.givens(q));assertEquals(2,q.answers.length);assertTrue(q.choices.isEmpty());}
 }
}
