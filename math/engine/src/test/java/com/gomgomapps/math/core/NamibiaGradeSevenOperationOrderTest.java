package com.gomgomapps.math.core;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;
public class NamibiaGradeSevenOperationOrderTest {
 @Test public void gradeSevenEntryUsesBoundedPublicStepsWithoutChangingGradeSix(){
  String pack="na-nied-primary-2024-v1";
  Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"NA");GlobalCurriculum.choosePack(p,pack);
  assertTrue(GlobalCurriculum.pack(p).inGrade(WholeOperationOrder.ID,7));
  assertTrue(GlobalCurriculum.pack(p).inGrade(WholeOperationOrder.ID,6));
  assertFalse(GlobalCurriculum.pack(p).inGrade(WholeOperationOrder.ID,5));
  Generator generator=new Generator(new Random(325));Checker checker=new Checker();List<String>recent=new ArrayList<>();Set<String>operations=new HashSet<>();
  CurriculumLimits limits=GlobalCurriculum.limits(pack,WholeOperationOrder.ID,7);
  for(int i=0;i<100;i++){
   Question q=generator.next(WholeOperationOrder.ID,recent,false,limits);assertTrue(limits.allows(q));assertFalse(recent.contains(q.signature()));recent.add(q.signature());
   List<WholeOperationOrder.Step>steps=WholeOperationOrder.read(q);assertNotNull(steps);steps.forEach(s->operations.add(s.operation()));
   List<String>entered=steps.stream().map(s->Long.toString(s.value())).toList();q.answers=new String[]{"poison"};q.expression="poison";
   assertTrue(checker.check(q,List.of(),entered).correct());List<String>wrong=new ArrayList<>(entered);wrong.set(0,Long.toString(steps.get(0).value()+1));assertEquals(0,checker.check(q,List.of(),wrong).index);
   HelpPlan help=HelpPlan.forQuestion(q);assertNotNull(help);assertFalse(help.canTransfer());assertTrue(help.restore(null,q.id).entries.stream().allMatch(String::isEmpty));
  }
  assertEquals(Set.of("+","−","×","÷","of"),operations);
 }
}
