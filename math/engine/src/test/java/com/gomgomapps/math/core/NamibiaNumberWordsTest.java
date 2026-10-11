package com.gomgomapps.math.core;
import org.junit.Test;import static org.junit.Assert.*;import java.util.*;
public class NamibiaNumberWordsTest {
 private static final String NA="na-nied-primary-2024-v1";
 @Test public void realGradeSixBothDirectionsUseFreshNumbersAndPublicStudentHelp(){
  Learning.Profile profile=new Learning.Profile();GlobalCurriculum.chooseCountry(profile,"NA");GlobalCurriculum.choosePack(profile,NA);profile.grade=6;
  for(String id:List.of("englishWordsToNumber","numberToEnglishWords")){
   assertTrue(GlobalCurriculum.available(profile).stream().anyMatch(s->s.id.equals(id)));assertFalse(GlobalCurriculum.scope(profile).stream().anyMatch(s->s.id.equals(id)));CurriculumLimits limits=GlobalCurriculum.limits(NA,id,6);assertEquals(1000000,limits.wholeMaximum(-1));Generator g=new Generator(new Random(254));List<String> recent=new ArrayList<>();for(int i=0;i<150;i++){
    Question q=g.next(id,recent,true,limits);assertFalse(recent.contains(q.signature()));recent.add(q.signature());int n=EnglishNumberWords.publicValue(q);assertTrue(n>=0&&n<=1000000);assertTrue(q.choices.isEmpty());q.answers=new String[]{"poison"};assertTrue(new Checker().check(q,List.of(),List.of(id.equals("englishWordsToNumber")?""+n:EnglishNumberWords.words(n))).correct());HelpPlan h=HelpPlan.forQuestion(q);assertFalse(h.canTransfer());assertTrue(h.restore(null,q.id).entries.stream().allMatch(String::isEmpty));if(n>1000){String digits=""+n;assertEquals(digits.length(),h.size());for(int d=0;d<digits.length();d++)assertTrue(h.step(d).accepts(digits.substring(d,d+1)));}
   }
  }
 }
 @Test public void zeroOneMillionSpellingVariantsAndWrongNamesStayDistinct(){
  Map<Integer,String> cases=new LinkedHashMap<>();cases.put(0,"zero");cases.put(19,"nineteen");cases.put(40,"forty");cases.put(105,"one hundred and five");cases.put(1001,"one thousand and one");cases.put(600015,"six hundred thousand and fifteen");cases.put(999999,"nine hundred and ninety-nine thousand nine hundred and ninety-nine");cases.put(1000000,"one million");
  for(String id:List.of("englishWordsToNumber","numberToEnglishWords"))for(var e:cases.entrySet()){Question q=EnglishNumberWords.make(id,e.getKey());assertTrue(GlobalCurriculum.limits(NA,id,6).allows(q));q.answers=new String[]{"poison"};Checker c=new Checker();String answer=id.equals("englishWordsToNumber")?e.getKey().toString():e.getValue();assertTrue(c.check(q,List.of(),List.of(answer)).correct());assertFalse(c.check(q,List.of(),List.of(id.equals("englishWordsToNumber")?""+(e.getKey()+1):"and "+answer)).correct());if(id.equals("numberToEnglishWords"))assertTrue(c.check(q,List.of(),List.of(answer.replace(" and "," ").replace('-',' ').toUpperCase())).correct());assertFalse(HelpPlan.forQuestion(q).canTransfer());}
  assertFalse(GlobalCurriculum.limits(NA,"englishWordsToNumber",6).allows(EnglishNumberWords.make("englishWordsToNumber",1000001)));
 }
}
