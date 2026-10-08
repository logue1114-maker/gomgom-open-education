package com.gomgomapps.math.core;
import java.util.*;import org.junit.Test;import static org.junit.Assert.*;
public class EnglandObjectFractionsTest {
 private static final String PACK="england-primary-2021-v1";
 @Test public void groupingAndSharingUseTheSelectedVisibleSmallDomain(){
  for(String id:List.of("objectGroupTotal","objectShare")){
   Generator g=new Generator(new Random(63));List<String> recent=new ArrayList<>();Set<String> publicPairs=new HashSet<>();
   for(int i=0;i<16;i++){
    Question q=g.next(id,recent,false,GlobalCurriculum.limits(PACK,id,1));assertFalse(recent.contains(q.signature()));recent.add(q.signature());assertNotNull(q.diagram);
    int first=(int)q.diagram.values[0],second=(int)q.diagram.values[1],total=id.equals("objectShare")?first:first*second,unit=id.equals("objectShare")?second:first;
    assertTrue(total<=20&&total>0);assertTrue(Set.of(2,5,10).contains(unit));assertTrue(publicPairs.add(total+":"+unit));int answer=id.equals("objectShare")?total/unit:total;
    assertTrue(new Checker().check(q,List.of(),List.of(String.valueOf(answer))).correct());assertFalse(new Checker().check(q,List.of(),List.of(String.valueOf(answer+1))).correct());assertFalse(HelpPlan.forQuestion(q).canTransfer());
   }
   assertEquals(16,publicPairs.size());assertEquals(recent.get(0),g.next(id,recent,false,GlobalCurriculum.limits(PACK,id,1)).signature());
  }
 }
 @Test public void halfQuarterPicturesExhaustAndAnswerPositionVaries(){
  Generator g=new Generator(new Random(64));List<String> recent=new ArrayList<>();Set<Integer> positions=new HashSet<>();Set<Integer> denominators=new HashSet<>();
  for(int i=0;i<64;i++){
   Question q=g.next("fractionPiecePicture",recent,false,GlobalCurriculum.limits(PACK,"fractionPiecePicture",1));assertFalse(recent.contains(q.signature()));recent.add(q.signature());int target=q.prompt.contains("사분의 일")?4:2;denominators.add(target);
   String correct=null;int matching=0;for(String key:q.choices){StudyDiagram picture=q.choiceDiagrams.get(key);if(picture.values[0]==target&&Integer.bitCount((int)picture.values[1])==1){correct=key;matching++;}}
   assertEquals(1,matching);positions.add(q.choices.indexOf(correct));assertTrue(new Checker().check(q,List.of(),List.of(correct)).correct());assertFalse(HelpPlan.forQuestion(q).canTransfer());
  }
  assertEquals(Set.of(2,4),denominators);assertEquals(4,positions.size());assertEquals(recent.get(0),g.next("fractionPiecePicture",recent,false,GlobalCurriculum.limits(PACK,"fractionPiecePicture",1)).signature());
 }
 @Test public void writtenFractionsFollowActualEqualAreaCellsAndKeepUnreducedParts(){
  Generator g=new Generator(new Random(65));List<String> recent=new ArrayList<>();Set<String> pairs=new HashSet<>();CurriculumLimits limit=GlobalCurriculum.limits(PACK,"fractionPart",2);
  for(int i=0;i<17;i++){
   Question q=g.next("fractionPart",recent,false,limit);assertFalse(recent.contains(q.signature()));recent.add(q.signature());int denominator=(int)q.diagram.values[0],numerator=Integer.bitCount((int)q.diagram.values[1]);String answer=numerator+"/"+denominator;pairs.add(answer);
   assertTrue(Set.of("1/2","1/3","1/4","2/4","3/4").contains(answer));assertTrue(new Checker().check(q,List.of(),List.of(answer)).correct());assertFalse(HelpPlan.forQuestion(q).canTransfer());assertEquals("fraction",q.answerFormat);assertEquals(2,q.studyGuide.frames.size());
   if(numerator==2&&denominator==4){assertTrue(new Checker().check(q,List.of(),List.of("1/2")).correct());assertEquals(Checker.Status.INPUT_NEEDED,new Checker().check(q,List.of(),List.of("0.5")).status);}
  }
  assertEquals(Set.of("1/2","1/3","1/4","2/4","3/4"),pairs);assertEquals(recent.get(0),g.next("fractionPart",recent,false,limit).signature());
  Question outside=new Question("fractionPart","色","1/3","1/3");outside.diagram=new StudyDiagram("fractionSelection",new double[]{3,3});assertFalse(limit.allows(outside));outside.diagram=new StudyDiagram("fractionSelection",new double[]{4,17});assertFalse(limit.allows(outside));
 }
 @Test public void newConstraintIsOptionalValidatedAndLearnedScopeStaysEarlierOnly(){
  for(String bad:List.of("partFractions=0/2","partFractions=2/2","partFractions=1/13","partFractions=1/2,1/2","partFractions=1/2,")){try{new CurriculumLimits(bad);fail(bad);}catch(IllegalArgumentException expected){}}
  CurriculumLimits none=CurriculumLimits.NONE;Question twoThirds=new Question("fractionPart","色","2/3","2/3");twoThirds.diagram=new StudyDiagram("fractionSelection",new double[]{3,3});assertTrue(none.allows(twoThirds));
  Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"GB");GlobalCurriculum.choosePack(p,PACK);p.grade=1;assertFalse(GlobalCurriculum.scope(p).stream().anyMatch(s->ObjectGroups.supports(s.id)||FractionPieces.supports(s.id)));p.grade=2;assertTrue(GlobalCurriculum.scope(p).stream().anyMatch(s->s.id.equals("fractionPiecePicture")));assertFalse(GlobalCurriculum.scope(p).stream().anyMatch(s->s.id.equals("fractionPart")));
 }
}
