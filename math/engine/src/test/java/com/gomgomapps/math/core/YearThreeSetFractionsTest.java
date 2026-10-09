package com.gomgomapps.math.core;
import java.util.*;import org.junit.Test;import static org.junit.Assert.*;
public class YearThreeSetFractionsTest {
 @Test public void all268UngroupedCollectionsHavePublicChecksAndBlankHelp(){
  Generator g=new Generator(new Random(137));CurriculumLimits limits=GlobalCurriculum.limits("england-primary-2021-v1",QuantityFractionCheck.ID,3);List<String> recent=new ArrayList<>();Set<String> conditions=new HashSet<>();Set<Integer> numerators=new HashSet<>(),denominators=new HashSet<>();
  for(int i=0;i<268;i++){
   Question q=g.next(QuantityFractionCheck.ID,recent,false,limits);assertTrue(conditions.add(q.signature()));recent.add(q.signature());assertEquals("fractionQuantityObjects",q.diagram.type);int[] v=QuantityFractionCheck.read(q);assertNotNull(v);assertTrue(v[0]<=100);assertEquals(0,v[0]%v[2]);numerators.add(v[1]);denominators.add(v[2]);
   // Neither internal key, expression nor diagram metadata may decide the result.
   q.answers=new String[]{"999"};q.expression="999";q.diagram=new StudyDiagram("fractionQuantityObjects",new double[]{999,999,999});
   assertTrue(new Checker().check(q,List.of(),List.of(""+v[4])).correct());assertEquals(Checker.Status.WRONG_ANSWER,new Checker().check(q,List.of(),List.of(""+(v[4]+1))).status);
   HelpPlan plan=HelpPlan.forQuestion(q);assertEquals(5,plan.size());assertFalse(plan.canTransfer());assertTrue(plan.restore(null,q.id).entries.stream().allMatch(String::isEmpty));for(int step=0;step<5;step++)assertTrue(plan.step(step).accepts(""+v[step]));
  }
  assertEquals(Set.of(2,3,4,5,8,10),denominators);assertTrue(numerators.contains(1)&&numerators.contains(9));assertEquals(recent.get(0),g.next(QuantityFractionCheck.ID,recent,false,limits).signature());
 }
 @Test public void allExistingRepresentationsDeriveTheSamePublicRelationship(){
  for(String repr:List.of("number","objects","length"))for(int den=2;den<=12;den++)for(int num=1;num<den;num++){
   Question q=QuantityFractionPictures.create(Catalog.get(QuantityFractionCheck.ID),num,den,3,repr);q.answers=new String[]{"0"};q.expression="0";int[] v=QuantityFractionCheck.read(q);assertArrayEquals(new int[]{den*3,num,den,3,num*3},v);assertTrue(new Checker().check(q,List.of(),List.of((num*6)+"/2")).correct());HelpPlan p=HelpPlan.forQuestion(q);assertFalse(p.canTransfer());assertTrue(p.step(4).accepts(""+(num*3)));
  }
 }
 @Test public void invalidPublicFractionsDoNotAcceptStoredAnswers(){
  for(String prompt:List.of("동그라미 7개의 1/3은 몇 개인가요?","동그라미 0개의 1/2은 몇 개인가요?","8의 1/0은 얼마인가요?","8의 0/2은 얼마인가요?","8의 2/2은 얼마인가요?","8의 1/2은 얼마인가요? junk")){
   Question q=new Question(QuantityFractionCheck.ID,prompt,"0","0");assertNull(QuantityFractionCheck.read(q));assertEquals(Checker.Status.INPUT_NEEDED,new Checker().check(q,List.of(),List.of("0")).status);
  }
 }
}
