package com.gomgomapps.math.core;
import org.junit.Test;import static org.junit.Assert.*;import java.util.*;
public class EnglandUpperSumsTest {
 @Test public void fourPlacementsOfferFreshPublicCalculationsAndBlankColumnHelp(){
  for(int year:List.of(4,5))for(String id:List.of("add1000","sub1000")){
   var limits=GlobalCurriculum.limits("england-primary-2021-v1",id,year);Generator g=new Generator(new Random(700+year));List<String> recent=new ArrayList<>();Set<Integer> choicePositions=new HashSet<>();boolean topRange=false,lower=false;
   for(int i=0;i<100;i++){
    Question q=g.next(id,recent,i%2==0,limits);assertFalse(recent.contains(q.signature()));recent.add(q.signature());String[] parts=q.prompt.split(" [+−-] ");int a=Integer.parseInt(parts[0]),b=Integer.parseInt(parts[1]),max=year==4?9999:999999;assertTrue(a>=0&&b>=0&&a<=max&&b<=max);topRange|=Math.max(a,b)>max/10;lower|=Math.min(a,b)<max/10;int result=id.startsWith("add")?a+b:a-b;assertTrue(result>=0);assertTrue(new Checker().check(q,List.of(),List.of(""+result)).correct());assertFalse(new Checker().check(q,List.of(),List.of(""+(result+1))).correct());HelpPlan plan=HelpPlan.forQuestion(q);assertNotNull(plan);assertFalse(plan.canTransfer());assertEquals("column-relations-v1",q.studyGuide.teachingVersion);assertTrue(plan.step(plan.size()-1).accepts(""+result));
    for(int j=0;j<plan.size();j++){assertEquals("",plan.step(j).after);assertTrue(plan.step(j).accepts(q.studyGuide.frames.get(j).expected));assertFalse(plan.step(j).accepts(""));String label=plan.step(j).before.replace("10","").replace("1","");assertFalse(label.matches(".*[0-9].*"));}
    Question saved=new Question(id,q.prompt,"hidden","99999999");HelpPlan restored=HelpPlan.forQuestion(saved);assertEquals(plan.size(),restored.size());for(int j=0;j<plan.size();j++)assertEquals(q.studyGuide.frames.get(j).expected,saved.studyGuide.frames.get(j).expected);
    if(!q.choices.isEmpty()){assertEquals(4,new HashSet<>(q.choices).size());choicePositions.add(q.correctChoice);assertEquals(""+result,q.choices.get(q.correctChoice));for(String c:q.choices)assertTrue(limits.allowsChoice(c));}
   }assertTrue(topRange);assertTrue(lower);assertEquals(Set.of(0,1,2,3),choicePositions);
  }
 }
 @Test public void zeroMaximumChainedBorrowAndDefaultOperandBoundsRemainHonest(){
  for(int year:List.of(4,5))for(String id:List.of("add1000","sub1000")){
   var limits=GlobalCurriculum.limits("england-primary-2021-v1",id,year);Question zero=new Generator(new Random(){@Override public int nextInt(int bound){return 0;}}).next(id,List.of(),false,limits);assertEquals(id.startsWith("add")?"0 + 0":"0 - 0",zero.prompt);assertTrue(new Checker().check(zero,List.of(),List.of("0")).correct());
   int max=year==4?9999:999999;Question boundary=new Question(id,max+(id.startsWith("add")?" + ":" - ")+max,"",""+(id.startsWith("add")?max*2:0));assertTrue(limits.allows(boundary));assertFalse(HelpPlan.forQuestion(boundary).canTransfer());assertTrue(HelpPlan.forQuestion(boundary).step(HelpPlan.forQuestion(boundary).size()-1).accepts(boundary.answers[0]));
  }
  Question chained=new Question("sub1000","100000 - 1","hidden","wrong");HelpPlan p=HelpPlan.forQuestion(chained);assertTrue(p.step(p.size()-1).accepts("99999"));assertFalse(p.canTransfer());
  Generator old=new Generator(new Random(700));for(int i=0;i<100;i++){Question q=old.next("add1000",List.of(),false,GlobalCurriculum.limits("sg-moe-primary-2021-v1","add1000",3));for(String part:q.prompt.split(" \\+ "))assertTrue(Integer.parseInt(part)>=1000);}
 }
}
