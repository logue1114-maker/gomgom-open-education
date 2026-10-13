package com.gomgomapps.math.core;
import java.util.*;import java.io.*;import org.junit.Test;import static org.junit.Assert.*;
public class GradeSevenThreeStepContextScopeTest {
 @Test public void normalGradeSevenRouteHasFourStoriesPublicLimitsAndFreshSupply(){
  CurriculumLimits limits=GlobalCurriculum.limits("na-nied-primary-2024-v1",ThreeStepWholeContexts.ID,7);Set<Integer>families=new HashSet<>();List<String>recent=new ArrayList<>();Generator g=new Generator(new Random(344));for(int i=0;i<100;i++){Question q=g.next(ThreeStepWholeContexts.ID,recent,false,limits);assertTrue(limits.allows(q));assertFalse(recent.contains(q.signature()));recent.add(q.signature());families.add(ThreeStepWholeContexts.read(q).family());}assertEquals(Set.of(0,1,2,3),families);
  Question publicOnly=ThreeStepWholeContexts.make(0,12,3,8,5);publicOnly.answers=new String[]{"999999999"};publicOnly.expression="999999999";assertTrue(limits.allows(publicOnly));assertFalse(new CurriculumLimits("maxGiven=10;maxResult=1000000").allows(publicOnly));assertFalse(new CurriculumLimits("maxGiven=1000000;maxResult=40").allows(publicOnly));
 }
 @Test public void ownWrittenMethodsUsePublicSituationAndPreserveStudentChoiceAcrossSave()throws Exception{
  Question q=ThreeStepWholeContexts.make(1,20,10,6,4);q.expression="100";q.answers=new String[]{"100"};Checker c=new Checker();assertTrue(c.check(q,List.of(),List.of("6")).correct());q.calculationMethod="written";
  assertTrue(c.check(q,List.of("(20+10-6)/4=6"),List.of("6"),List.of(Checker.StepKind.FULL)).correct());assertTrue(c.check(q,List.of("20+10=30","30-6=24","24/4=6"),List.of("6"),List.of(Checker.StepKind.PARTIAL,Checker.StepKind.PARTIAL,Checker.StepKind.PARTIAL)).correct());assertFalse(c.check(q,List.of("(20+10-6)/4=7"),List.of("6"),List.of(Checker.StepKind.FULL)).correct());assertTrue(c.checkSteps(q,List.of("(10+20-6)/4=6"),List.of(Checker.StepKind.FULL)).correct());
  ByteArrayOutputStream b=new ByteArrayOutputStream();try(ObjectOutputStream o=new ObjectOutputStream(b)){o.writeObject(q);}try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(b.toByteArray()))){q=(Question)in.readObject();}assertEquals("written",q.calculationMethod);assertTrue(c.check(q,List.of("(20+10-6)/4=6"),List.of("6"),List.of(Checker.StepKind.FULL)).correct());assertFalse(HelpPlan.forQuestion(q).canTransfer());
 }
}
