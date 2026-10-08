package com.gomgomapps.math.core;
import java.util.*;import org.junit.Test;import static org.junit.Assert.*;
public class EnglishNumberWordsTest {
 @Test public void explicitSpellingsAndStudentTextAreChecked(){
  String[] known={"zero","one","two","three","four","five","six","seven","eight","nine","ten","eleven","twelve","thirteen","fourteen","fifteen","sixteen","seventeen","eighteen","nineteen","twenty"};
  for(int i=0;i<known.length;i++)assertEquals(known[i],EnglishNumberWords.words(i));
  assertEquals("forty-two",EnglishNumberWords.words(42));assertEquals("eighty-nine",EnglishNumberWords.words(89));assertEquals("one hundred",EnglishNumberWords.words(100));
  Question q=EnglishNumberWords.make("numberToEnglishWords",42);Checker c=new Checker();
  for(String answer:List.of("forty-two"," FORTY TWO ","forty   two"))assertTrue(c.check(q,List.of(),List.of(answer)).correct());
  for(String wrong:List.of("42","fourty two","fortytwo","forty-three","forty two extra"))assertEquals(Checker.Status.WRONG_ANSWER,c.check(q,List.of(),List.of(wrong)).status);
  assertEquals(Checker.Status.INPUT_NEEDED,c.check(q,List.of(),List.of(" ")).status);
 }
 @Test public void gradeDomainsExhaustBeforeRepeatingAndDoNotOfferWordAnswers(){
  for(int grade:List.of(1,2))for(String id:List.of("numberToEnglishWords","englishWordsToNumber")){
   List<String> recent=new ArrayList<>();Set<Integer> values=new HashSet<>();Generator g=new Generator(new Random(62));int size=grade==1?20:101;
   for(int i=0;i<size;i++){
    Question q=g.next(id,recent,true,GlobalCurriculum.limits("england-primary-2021-v1",id,grade));assertFalse(recent.contains(q.signature()));recent.add(q.signature());assertTrue(q.choices.isEmpty());
    String given=q.prompt.split("\n")[1];int value=id.equals("numberToEnglishWords")?Integer.parseInt(given):Arrays.asList(names()).indexOf(given);assertTrue(value>= (grade==1?1:0)&&value<=(grade==1?20:100));values.add(value);
    assertTrue(new Checker().check(q,List.of(),List.of(id.equals("numberToEnglishWords")?names()[value]:String.valueOf(value))).correct());assertFalse(HelpPlan.forQuestion(q).canTransfer());
    assertEquals(value==100?3:2,q.studyGuide.frames.size());assertFalse(q.studyGuide.frames.stream().anyMatch(f->f.before.contains(names()[value])||f.after.contains(names()[value])));
   }
   assertEquals(size,values.size());assertEquals(recent.get(0),g.next(id,recent,false,GlobalCurriculum.limits("england-primary-2021-v1",id,grade)).signature());
  }
 }
 private static String[] names(){String[] result=new String[101];for(int i=0;i<=100;i++)result[i]=EnglishNumberWords.words(i);return result;}
 @Test public void existingCountriesAndDiagnosisDoNotAcquireUnlearnedEnglishSpelling(){
  Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"GB");GlobalCurriculum.choosePack(p,"england-primary-2021-v1");p.grade=1;assertFalse(GlobalCurriculum.scope(p).stream().anyMatch(s->EnglishNumberWords.supports(s.id)));p.grade=2;assertTrue(GlobalCurriculum.scope(p).stream().anyMatch(s->EnglishNumberWords.supports(s.id)));
  GlobalCurriculum.chooseCountry(p,"NA");GlobalCurriculum.choosePack(p,"na-nied-primary-2024-v1");assertFalse(GlobalCurriculum.scope(p).stream().anyMatch(s->EnglishNumberWords.supports(s.id)));
  GlobalCurriculum.chooseCountry(p,"KR");p.grade=3;assertFalse(Learning.diagnosticScope(p).stream().anyMatch(s->EnglishNumberWords.supports(s.id)));assertFalse(GlobalCurriculum.available(p).stream().anyMatch(s->EnglishNumberWords.supports(s.id)));
  GlobalCurriculum.chooseCountry(p,"AQ");assertFalse(GlobalCurriculum.available(p).stream().anyMatch(s->EnglishNumberWords.supports(s.id)));
 }
}
