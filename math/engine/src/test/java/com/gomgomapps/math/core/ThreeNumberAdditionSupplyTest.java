package com.gomgomapps.math.core;
import org.junit.Test;import static org.junit.Assert.*;import java.util.*;
public class ThreeNumberAdditionSupplyTest {
 @Test public void allOrderedTriplesThroughTwentyHavePublicFiveBlankSteps(){
  Set<String> seen=new HashSet<>();int[] sums=new int[21];
  for(int i=0;i<ThreeNumberAdditionSupply.count(20);i++){
   Question q=ThreeNumberAdditionSupply.at(20,i);assertTrue(seen.add(q.signature()));
   String[] terms=q.prompt.split(" \\+ ");int a=Integer.parseInt(terms[0]),b=Integer.parseInt(terms[1]),c=Integer.parseInt(terms[2]),sum=a+b+c;sums[sum]++;
   assertTrue(sum<=20);assertTrue(new Checker().check(q,List.of(),List.of(""+sum)).correct());
   q.answers[0]="999";HelpPlan h=HelpPlan.forQuestion(q);assertEquals(5,h.size());assertFalse(h.canTransfer());
   int[] expected={a,b,c,a+b,sum};for(int k=0;k<5;k++){assertTrue(h.step(k).accepts(""+expected[k]));assertFalse(h.step(k).accepts(""));assertFalse(h.step(k).accepts(""+(expected[k]+1)));}
  }
  assertEquals(1771,seen.size());for(int s=0;s<=20;s++)assertEquals((s+1)*(s+2)/2,sums[s]);
 }
 @Test public void normalCurriculumSupplyIncludesLargeSumsAndRetainsOtherRoutes(){
  var pack=GlobalCurriculum.packs("NA").stream().filter(p->p.id.equals("na-nied-primary-2024-v1")).findFirst().orElseThrow();assertTrue(pack.inGrade("addThree9",1));
  CurriculumLimits limits=GlobalCurriculum.limits(pack.id,"addThree9",1);assertTrue(ThreeNumberAdditionSupply.selected("addThree9",limits));
  Generator g=new Generator(new Random(230));List<String> recent=new ArrayList<>();boolean large=false;
  for(int i=0;i<150;i++){Question q=g.next("addThree9",recent,false,limits);assertFalse(recent.contains(q.signature()));recent.add(q.signature());int sum=Integer.parseInt(q.answers[0]);assertTrue(sum<=20);large|=sum>9;assertEquals(5,HelpPlan.forQuestion(q).size());}assertTrue(large);
  assertFalse(ThreeNumberAdditionSupply.selected("addThree9",CurriculumLimits.NONE));assertFalse(ThreeNumberAdditionSupply.selected("addSubChain",limits));
  assertEquals(10,AddSubChains.maximum(GlobalCurriculum.limits(pack.id,AddSubChains.ID,1)));
  for(int i=0;i<50;i++)assertTrue(Integer.parseInt(g.next("addThree9",List.of(),false).answers[0])<=9);
 }
}
