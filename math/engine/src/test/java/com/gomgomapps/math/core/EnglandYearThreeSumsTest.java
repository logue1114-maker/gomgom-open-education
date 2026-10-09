package com.gomgomapps.math.core;
import org.junit.Test;import static org.junit.Assert.*;import java.util.*;
public class EnglandYearThreeSumsTest {
 @Test public void yearThreeOperandsStopAtThreeDigitsWhileSumCanHaveFour(){
  for(String id:List.of("add1000","sub1000")){
   var limits=GlobalCurriculum.limits("england-primary-2021-v1",id,3);assertEquals(3,limits.wholeDigits(3));assertEquals(999,limits.givenMaximum(999));Generator g=new Generator(new Random(111));List<String> recent=new ArrayList<>();Set<Integer> positions=new HashSet<>();boolean small=false,large=false;
   for(int i=0;i<100;i++){Question q=g.next(id,recent,i%2==0,limits);assertFalse(recent.contains(q.signature()));recent.add(q.signature());String[] nums=q.prompt.split(" [+−-] ");int a=Integer.parseInt(nums[0]),b=Integer.parseInt(nums[1]);assertTrue(a>=0&&a<=999&&b>=0&&b<=999);small|=Math.min(a,b)<100;large|=Math.max(a,b)>=100;int expected=id.startsWith("add")?a+b:a-b;assertTrue(expected>=0);assertTrue(new Checker().check(q,List.of(),List.of(""+expected)).correct());assertFalse(new Checker().check(q,List.of(),List.of(""+(expected+1))).correct());HelpPlan h=HelpPlan.forQuestion(q);assertNotNull(h);assertFalse(h.canTransfer());assertEquals("column-relations-v1",q.studyGuide.teachingVersion);assertTrue(h.step(h.size()-1).accepts(""+expected));Question hidden=new Question(id,q.prompt,"hidden","wrong");HelpPlan other=HelpPlan.forQuestion(hidden);assertEquals(h.size(),other.size());for(int j=0;j<h.size();j++)assertEquals(q.studyGuide.frames.get(j).expected,hidden.studyGuide.frames.get(j).expected);if(!q.choices.isEmpty()){positions.add(q.correctChoice);assertEquals(4,new HashSet<>(q.choices).size());}}
   assertTrue(small&&large);assertEquals(Set.of(0,1,2,3),positions);
  }
 }
 @Test public void zeroMaximumCarryBorrowAndAdjacentGradesRemainValid(){
  for(String id:List.of("add1000","sub1000")){
   var limits=GlobalCurriculum.limits("england-primary-2021-v1",id,3);Question zero=new Generator(new Random(){@Override public int nextInt(int bound){return 0;}}).next(id,List.of(),false,limits);assertEquals(id.startsWith("add")?"0 + 0":"0 - 0",zero.prompt);
   assertEquals(9999,GlobalCurriculum.limits("england-primary-2021-v1",id,4).givenMaximum(9999));assertEquals(999999,GlobalCurriculum.limits("england-primary-2021-v1",id,5).givenMaximum(999999));
  }
  for(String prompt:List.of("999 + 999","100 - 1")){boolean add=prompt.contains("+");Question q=new Question(add?"add1000":"sub1000",prompt,"hidden","wrong");HelpPlan h=HelpPlan.forQuestion(q);assertFalse(h.canTransfer());assertTrue(h.step(h.size()-1).accepts(add?"1998":"99"));}
 }
}
