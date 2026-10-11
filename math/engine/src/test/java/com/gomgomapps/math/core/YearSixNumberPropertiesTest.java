package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;import org.junit.Test;import static org.junit.Assert.*;
public class YearSixNumberPropertiesTest {
 @Test public void commonMultipleUsesPublicBoundsAndRejectsMalformedInput(){
  Question q=new Question("el_common_multiple","11과 12의 4번째 공배수는?","","poison");Checker c=new Checker();assertTrue(c.check(q,List.of(),List.of("528")).correct());assertEquals(Checker.Status.INPUT_NEEDED,c.check(q,List.of(),Arrays.asList((String)null)).status);
  for(String prompt:List.of("999999999999999999999과 12의 4번째 공배수는?","13과 12의 4번째 공배수는?","11과 12의 5번째 공배수는?")){q.prompt=prompt;assertEquals(Checker.Status.INPUT_NEEDED,c.check(q,List.of(),List.of("528")).status);}
 }
 @Test public void normalSixthYearSupplyChecksVisibleNumbersWithBlankNonTransferHelp(){
  Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"GB");GlobalCurriculum.choosePack(p,"england-primary-2021-v1");Generator gen=new Generator(new Random(196));Checker checker=new Checker();
  for(String id:List.of(CompleteFactorPractice.COMMON,"el_common_multiple",PrimeVocabulary.CLASSIFY)){
   assertTrue(GlobalCurriculum.pack(p).inGrade(id,6));CurriculumLimits limits=GlobalCurriculum.limits(p.educationSystem,id,6);List<String> recent=new ArrayList<>();Set<List<String>> orders=new HashSet<>();
   for(int i=0;i<100;i++){
    Question q=gen.next(id,recent,false,limits);assertTrue(limits.allows(q));assertFalse(recent.contains(q.signature()));recent.add(q.signature());List<String> answer=new ArrayList<>();
    if(id.equals(CompleteFactorPractice.COMMON)){int[] a=CompleteFactorPractice.read(q);for(int d=1;d<=Math.min(a[0],a[1]);d++)if(a[0]%d==0&&a[1]%d==0)answer.add(""+d);}
    else if(id.equals(PrimeVocabulary.CLASSIFY)){int n=PrimeVocabulary.read(q)[0],count=0;for(int d=1;d<=n;d++)if(n%d==0)count++;answer.add(n==1?"0":count==2?"1":"2");orders.add(List.copyOf(q.choices));}
    else{Matcher m=Pattern.compile("^(\\d+)과 (\\d+)의 (\\d+)번째 공배수는\\?$").matcher(q.prompt);assertTrue(m.matches());int a=Integer.parseInt(m.group(1)),b=Integer.parseInt(m.group(2)),order=Integer.parseInt(m.group(3)),found=0,v=0;while(found<order){v++;if(v%a==0&&v%b==0)found++;}answer.add(""+v);}
    q.answers=new String[]{"poison"};q.expression="poison";q.correctChoice=999;assertTrue(checker.check(q,List.of(),answer).correct());List<String> wrong=new ArrayList<>(answer);wrong.set(0,id.equals(PrimeVocabulary.CLASSIFY)?answer.get(0).equals("1")?"2":"1":"0");assertEquals(Checker.Status.WRONG_ANSWER,checker.check(q,List.of(),wrong).status);
    HelpPlan h=HelpPlan.forQuestion(q);assertNotNull(h);assertFalse(h.canTransfer());assertTrue(h.restore(null,q.id).entries.stream().allMatch(String::isEmpty));
   }
   if(id.equals(PrimeVocabulary.CLASSIFY))assertTrue(orders.size()>1);
  }
  assertTrue(GlobalCurriculum.pack(p).inGrade("gcd",6));assertTrue(GlobalCurriculum.pack(p).inGrade("lcm",6));assertFalse(GlobalCurriculum.pack(p).inGrade(CompleteFactorPractice.PAIRS,6));assertFalse(GlobalCurriculum.pack(p).inGrade(PrimeVocabulary.FACTOR,6));
 }
}
