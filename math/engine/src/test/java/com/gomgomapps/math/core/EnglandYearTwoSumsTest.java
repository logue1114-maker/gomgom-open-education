package com.gomgomapps.math.core;
import org.junit.Test;import static org.junit.Assert.*;import java.util.*;
public class EnglandYearTwoSumsTest {
 @Test public void gradeTwoCoversSmallOperandsTwoDigitsAndThreeOneDigitTerms(){
  for(String id:List.of("add100","sub100","addThree9")){
   var limits=GlobalCurriculum.limits("england-primary-2021-v1",id,2);boolean three=id.equals("addThree9"),add=id.startsWith("add");assertTrue(limits.hasWholeDigits());assertEquals(three?1:2,limits.wholeDigits(9));
   Generator g=new Generator(new Random(114));List<String> recent=new ArrayList<>();boolean low=false,high=false,aboveNine=false,tens=false;Set<Integer> positions=new HashSet<>();
   for(int i=0;i<100;i++){
    Question q=g.next(id,recent,true,limits);assertFalse(recent.contains(q.signature()));recent.add(q.signature());String[] terms=q.prompt.split(" [+−-] ");assertEquals(three?3:2,terms.length);int answer=0;
    for(int j=0;j<terms.length;j++){int n=Integer.parseInt(terms[j]);assertTrue(n>=0&&n<=(three?9:99));answer+=j==0||add?n:-n;if(j==1){low|=n<10;high|=n>=10;tens|=n>=10&&n%10==0;}}
    assertTrue(answer>=0);aboveNine|=answer>9;assertTrue(new Checker().check(q,List.of(),List.of(""+answer)).correct());assertFalse(new Checker().check(q,List.of(),List.of(""+(answer+1))).correct());HelpPlan h=HelpPlan.forQuestion(q);assertNotNull(h);assertFalse(h.canTransfer());assertTrue(h.step(h.size()-1).accepts(""+answer));if(three){assertEquals(5,h.size());assertEquals("whole-number-relations-v1",q.studyGuide.teachingVersion);}else assertEquals("column-relations-v1",q.studyGuide.teachingVersion);
    if(!q.choices.isEmpty()){assertEquals(4,new HashSet<>(q.choices).size());positions.add(q.correctChoice);}
   }
   assertTrue(low&&aboveNine);if(!three)assertTrue(high&&tens);assertEquals(Set.of(0,1,2,3),positions);
  }
 }
 @Test public void zeroMaximumAndPriorGradeStaySeparate(){
  for(String id:List.of("add100","sub100","addThree9")){
   var limits=GlobalCurriculum.limits("england-primary-2021-v1",id,2);Question zero=new Generator(new Random(){@Override public int nextInt(int bound){return 0;}}).next(id,List.of(),false,limits);assertTrue(new Checker().check(zero,List.of(),List.of("0")).correct());
   Question max=new Generator(new Random(){@Override public int nextInt(int bound){return bound-1;}}).next(id,List.of(),false,limits);assertEquals(id.equals("add100")?"99 + 99":id.equals("sub100")?"99 - 99":"9 + 9 + 9",max.prompt);assertTrue(new Checker().check(max,List.of(),List.of(id.equals("add100")?"198":id.equals("sub100")?"0":"27")).correct());
  }
  assertFalse(GlobalCurriculum.limits("na-nied-primary-2024-v1","addThree9",1).hasWholeDigits());
 }
}
