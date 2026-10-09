package com.gomgomapps.math.core;
import java.util.*;import org.junit.Test;import static org.junit.Assert.*;
public class TenthUnitRelationsTest {
 @Test public void nineTenthConditionsExhaustBeforeReusingAndUnitBlankComesBeforeAmount(){
  var pack=GlobalCurriculum.packs("GB").stream().filter(p->p.id.equals("england-primary-2021-v1")).findFirst().orElseThrow();assertTrue(pack.inGrade("el_fraction_decimal",3));
  var limits=GlobalCurriculum.limits(pack.id,"el_fraction_decimal",3);Generator generator=new Generator(new Random(78));List<String> recent=new ArrayList<>();Set<String> arithmetic=new HashSet<>();
  for(int i=0;i<100;i++){
   Question q=generator.next("el_fraction_decimal",recent,false,limits);if(i<9)assertFalse(recent.contains(q.signature()));else assertTrue(recent.contains(q.signature()));recent.add(q.signature());String raw=q.prompt.substring(0,q.prompt.indexOf('의'));String[] v=raw.split("/");int n=Integer.parseInt(v[0]);assertEquals("10",v[1]);assertTrue(n>=1&&n<=9);arithmetic.add(raw);
   assertEquals("fraction",q.diagram.type);assertArrayEquals(new double[]{n,10},q.diagram.values,0);HelpPlan help=HelpPlan.forQuestion(q);assertEquals(4,help.size());assertFalse(help.canTransfer());assertTrue(help.step(2).accepts("0.1"));assertFalse(help.step(2).accepts("0.01"));assertTrue(help.step(3).accepts("0."+n));assertFalse(help.step(2).before.contains("0.1"));
   Question saved=new Question(q.skillId,q.prompt,"poison","999");saved.diagram=new StudyDiagram("fraction",new double[]{999,999});HelpPlan restored=HelpPlan.forQuestion(saved);assertArrayEquals(new double[]{n,10},saved.diagram.values,0);assertTrue(restored.step(2).accepts("0.1"));
  }assertEquals(9,arithmetic.size());
 }
 @Test public void changedTeachingResetsOldFramesButKeepsMainAnswerAndHundredthUnitIsExact(){
  Question q=new Question("el_fraction_decimal","71/100의 값을 소수로 나타내세요.","71/100","0.71");HelpPlan plan=HelpPlan.forQuestion(q);assertTrue(plan.step(2).accepts("0.01"));assertTrue(plan.step(3).accepts("0.71"));HelpPlan.Draft old=new HelpPlan.Draft();old.questionId=q.id;old.teachingVersion="decimal-fraction-relations-v1";old.stage=3;old.entries=new ArrayList<>(List.of("71","100","0.71"));HelpPlan.Draft restored=plan.restore(old,q.id);assertEquals(0,restored.stage);assertEquals(List.of(""),restored.entries);assertEquals("0.71",q.answers[0]);
 }
}
