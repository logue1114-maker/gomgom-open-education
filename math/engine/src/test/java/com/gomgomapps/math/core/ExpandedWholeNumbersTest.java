package com.gomgomapps.math.core;
import org.junit.Test;import java.util.*;import static org.junit.Assert.*;
public class ExpandedWholeNumbersTest {
 @Test public void normalCorePipelineGeneratesChecksAndRestoresStudentHelp(){
  for(String id:List.of(ExpandedWholeNumbers.EXPAND,ExpandedWholeNumbers.COMBINE)){
   Generator generator=new Generator(new Random(72));List<String> history=new ArrayList<>();CurriculumLimits limits=new CurriculumLimits("wholeMaximum=1000000;maxGiven=1000000;maxResult=1000000;nonnegative=true");
   for(int i=0;i<100;i++){
    Question q=generator.next(id,history,false,limits);assertFalse(history.contains(q.signature()));history.add(q.signature());Integer n=ExpandedWholeNumbers.read(q);assertNotNull(n);List<String> expected=id.equals(ExpandedWholeNumbers.EXPAND)?ExpandedWholeNumbers.terms(n).stream().map(t->""+t.value()).toList():List.of(""+n);assertTrue(new Checker().check(q,List.of(),expected).correct());HelpPlan help=HelpPlan.forQuestion(q);assertNotNull(help);assertFalse(help.canTransfer());
   }
  }
 }
 @Test public void canonicalBoundaryFormsUsePlaceValuesAndPreserveZero(){
  Map<Integer,List<Integer>> samples=Map.of(0,List.of(0),6,List.of(6),600001,List.of(600000,1),628015,List.of(600000,20000,8000,10,5),1000000,List.of(1000000));
  for(var e:samples.entrySet())for(String id:List.of(ExpandedWholeNumbers.EXPAND,ExpandedWholeNumbers.COMBINE)){
   Question q=ExpandedWholeNumbers.make(id,e.getKey());assertEquals(e.getKey(),ExpandedWholeNumbers.read(q));List<String> correct=id.equals(ExpandedWholeNumbers.EXPAND)?e.getValue().stream().map(Object::toString).toList():List.of(e.getKey().toString());assertTrue(ExpandedWholeNumbers.check(q,correct).correct());q.answers=new String[]{"9999999"};ExpandedWholeNumbers.attach(q);assertFalse(q.studyGuide.transfer);assertTrue(ExpandedWholeNumbers.check(q,correct).correct());
  }
 }
 @Test public void ordinaryPartitionsAndWrongComponentsAreNotExpandedNotation(){
  for(String form:List.of("300000 + 300000","600000 + 0 + 1","1 + 600000","0600000 + 1","1000001"))assertNull(ExpandedWholeNumbers.read(new Question(ExpandedWholeNumbers.COMBINE,form+"\n하나의 수로 쓰세요.","","0")));
  Question q=ExpandedWholeNumbers.make(ExpandedWholeNumbers.EXPAND,628015);assertFalse(ExpandedWholeNumbers.check(q,List.of("628015")).correct());assertFalse(ExpandedWholeNumbers.check(q,List.of("600000","20000","8000","15","0")).correct());
 }
 @Test public void supplyHasFreshConditionsAndOldestFallbackWithoutChangingRange(){
  Catalog.Skill skill=ExpandedWholeNumbers.SKILLS.get(0);Map<String,Integer> history=new LinkedHashMap<>();CurriculumLimits limits=new CurriculumLimits("wholeMaximum=10;maxResult=10");
  for(int i=0;i<11;i++){Question q=ExpandedWholeNumbers.next(skill,new Random(i),limits,history);assertFalse(history.containsKey(q.signature()));history.put(q.signature(),i);}
  Question repeated=ExpandedWholeNumbers.next(skill,new Random(19),limits,history);assertEquals(history.keySet().iterator().next(),repeated.signature());
 }
}
