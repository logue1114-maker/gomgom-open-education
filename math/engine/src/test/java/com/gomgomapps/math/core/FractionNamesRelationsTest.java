package com.gomgomapps.math.core;
import java.util.*;import org.junit.Test;import static org.junit.Assert.*;
public class FractionNamesRelationsTest {
 @Test public void explanationUpdatesPreserveStoredPublicConditionIdentity(){
  Generator g=new Generator(new Random(68));List<String> old=new ArrayList<>();String first=null;
  for(int i=0;i<4;i++){Question q=g.next("halfQuarterEquivalent",old,false,GlobalCurriculum.limits("england-primary-2021-v1","halfQuarterEquivalent",2));String canonical=q.signature();if(first==null)first=canonical;String previous="halfQuarterEquivalent|Old explanation.\n"+canonical.substring("halfQuarterEquivalent|".length());old.add(previous);assertEquals(canonical,FractionNamesRelations.identity(previous));q.prompt="Another explanation.\n"+q.prompt.substring(q.prompt.lastIndexOf('\n')+1);assertEquals(canonical,q.signature());}
  assertEquals(first,g.next("halfQuarterEquivalent",old,false,GlobalCurriculum.limits("england-primary-2021-v1","halfQuarterEquivalent",2)).signature());
 }
 @Test public void namesKeepThePresentedPartsAndSmallShuffledChoices(){
  for(int grade=1;grade<=2;grade++){Generator g=new Generator(new Random(66));CurriculumLimits limits=GlobalCurriculum.limits("england-primary-2021-v1","fractionNamePicture",grade);List<String> recent=new ArrayList<>();Set<Integer> positions=new HashSet<>();int size=grade==1?6:17;
   for(int i=0;i<size;i++){Question q=g.next("fractionNamePicture",recent,false,limits);assertFalse(recent.contains(q.signature()));recent.add(q.signature());int denominator=(int)q.diagram.values[0],numerator=Integer.bitCount((int)q.diagram.values[1]);String correct=numerator+"/"+denominator;assertEquals(grade==1?2:3,q.choices.size());assertTrue(q.choiceLabels.containsKey(correct));positions.add(q.choices.indexOf(correct));assertTrue(new Checker().check(q,List.of(),List.of(correct)).correct());for(String wrong:q.choices)if(!wrong.equals(correct))assertFalse(new Checker().check(q,List.of(),List.of(wrong)).correct());assertFalse(HelpPlan.forQuestion(q).canTransfer());
    if(correct.equals("2/4"))assertFalse(new Checker().check(q,List.of(),List.of("1/2")).correct());
   }assertTrue(positions.size()>1);assertEquals(recent.get(0),g.next("fractionNamePicture",recent,false,limits).signature());
  }
 }
 @Test public void equivalentWholesHaveEqualAreaInBothDirectionsAndNoAnswerTransfer(){
  Generator g=new Generator(new Random(67));List<String> recent=new ArrayList<>();Set<Integer> answers=new HashSet<>();
  for(int i=0;i<4;i++){Question q=g.next("halfQuarterEquivalent",recent,false,GlobalCurriculum.limits("england-primary-2021-v1","halfQuarterEquivalent",2));assertFalse(recent.contains(q.signature()));recent.add(q.signature());StudyDiagram d=q.diagram;int leftDen=(int)d.values[0],leftNum=Integer.bitCount((int)d.values[1]),rightDen=(int)d.values[2],rightNum=Integer.bitCount((int)d.values[3]);assertEquals(leftNum*rightDen,rightNum*leftDen);assertEquals("fractionEquivalentPair",d.type);assertTrue(q.prompt.contains(leftNum+"/"+leftDen+" = □/"+rightDen));answers.add(rightNum);assertTrue(new Checker().check(q,List.of(),List.of(String.valueOf(rightNum))).correct());assertFalse(new Checker().check(q,List.of(),List.of(String.valueOf(rightNum+1))).correct());assertFalse(HelpPlan.forQuestion(q).canTransfer());assertEquals(4,q.studyGuide.frames.size());}
  assertEquals(Set.of(1,2),answers);assertEquals(recent.get(0),g.next("halfQuarterEquivalent",recent,false,GlobalCurriculum.limits("england-primary-2021-v1","halfQuarterEquivalent",2)).signature());
 }
 @Test public void reviewedPlacementDoesNotExpandUnmappedCountryOrCurrentYearDiagnosis(){
  Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"GB");GlobalCurriculum.choosePack(p,"england-primary-2021-v1");p.grade=1;assertFalse(GlobalCurriculum.scope(p).stream().anyMatch(s->FractionNamesRelations.supports(s.id)));p.grade=2;assertTrue(GlobalCurriculum.scope(p).stream().anyMatch(s->s.id.equals("fractionNamePicture")));assertFalse(GlobalCurriculum.scope(p).stream().anyMatch(s->s.id.equals("halfQuarterEquivalent")));
  GlobalCurriculum.chooseCountry(p,"AQ");assertFalse(GlobalCurriculum.available(p).stream().anyMatch(s->FractionNamesRelations.supports(s.id)));
 }
}
