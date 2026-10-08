package com.gomgomapps.math.core;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;
public class MedianPracticeTest {
 private long[] data(Question q){return Arrays.stream(q.prompt.substring(0,q.prompt.indexOf('의')).split(", ")).mapToLong(Long::parseLong).toArray();}
 @Test public void variedPublicListsDefineBothKindsOfMedianAndMeanExactly(){
  Generator g=new Generator(new Random(2026100815));
  for(String id:List.of("mean","median")){
   Set<String> seen=new LinkedHashSet<>();Set<Integer> sizes=new HashSet<>(),positions=new HashSet<>();boolean fractional=false;int choiceCount=0;
   for(int i=0;i<600;i++){
    Question q=g.next(id,seen,true);assertTrue(seen.add(q.signature()));long[] values=data(q);sizes.add(values.length);Arrays.sort(values);
    Rational expected=id.equals("mean")?Rational.of(Arrays.stream(values).sum(),values.length):values.length%2==1?Rational.of(values[values.length/2]):Rational.of(values[values.length/2-1]+values[values.length/2],2);
    assertEquals(expected,Expression.number(q.answers[0]));assertTrue(new Checker().check(q,List.of(),List.of(expected.toString())).correct());
    if(!q.choices.isEmpty()){assertEquals(4,q.choices.stream().map(Expression::number).distinct().count());assertEquals(expected,Expression.number(q.choices.get(q.correctChoice)));positions.add(q.correctChoice);choiceCount++;}fractional|=!expected.isInteger();
    Arrays.fill(q.answers,"999999");HelpPlan p=HelpPlan.forQuestion(q);assertFalse(p.canTransfer());
    if(id.equals("median")){
     for(int j=0;j<values.length;j++)assertTrue(p.step(j).accepts(Long.toString(values[j])));
     assertTrue(p.step(p.size()-1).accepts(expected.toString()));
    }
   }
   assertEquals(Set.of(3,4,5,6,7,8),sizes);assertEquals(Set.of(0,1,2,3),positions);assertTrue(fractional);assertTrue(choiceCount>500);
  }
 }
 @Test public void duplicateMiddleValuesAndHalfIntegerResultsUsePublicData(){
  for(String[] pair:new String[][]{{"9, 2, 2, 8","5"},{"0, 0, 1, 2","1/2"},{"3, 3","3"},{"2, 4, 7, 9, 10, 11","8"}}){
   Question q=new Question("median",pair[0]+"의 중앙값은?","999999","999999");
   HelpPlan p=HelpPlan.forQuestion(q);assertFalse(p.canTransfer());assertTrue(p.step(p.size()-1).accepts(pair[1]));assertFalse(p.step(p.size()-1).accepts("999999"));
   for(int j=0;j<p.size();j++)assertFalse(p.step(j).before.matches(".*[0-9].*[+] .*"));
  }
 }
 @Test public void oldOddDraftRetainsItsVerifiedSortedEntriesAndEvenDraftRestores(){
  Question old=new Question("median","9, 2, 2, 8, 1의 중앙값은?","","2");HelpPlan p=HelpPlan.forQuestion(old);HelpPlan.Draft d=new HelpPlan.Draft();d.questionId=old.id;d.stage=2;d.entries=new ArrayList<>(List.of("1","2",""));
  assertEquals(2,p.restore(d,old.id).stage);assertEquals(List.of("1","2",""),d.entries);
  Question even=new Question("median","9, 2, 2, 8의 중앙값은?","","5");p=HelpPlan.forQuestion(even);d=p.restore(null,even.id);d.entries.set(0,"2");d.stage=1;assertEquals(1,HelpPlan.forQuestion(even).restore(d.copy(),even.id).stage);
 }
}
