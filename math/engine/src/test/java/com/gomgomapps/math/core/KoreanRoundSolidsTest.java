package com.gomgomapps.math.core;

import org.junit.Test;
import static org.junit.Assert.*;
import java.io.*;
import java.time.LocalDate;
import java.util.*;

/** Public drawing fixtures are unit evidence; actual Android editing and student learning are separate. */
public class KoreanRoundSolidsTest {
    private static final LocalDate DAY=LocalDate.of(2026,10,15);
    private static final String UNIT="KR|2022|6|2|6";
    private static final List<String> SHAPES=List.of("cylinder","cone","sphere","prism");
    private Question question(String id,String solid,String property,int radius,int height){
        var g=new KoreanRoundSolids.Givens(id,solid,property,radius,height);
        Question q=new Question(id,KoreanRoundSolids.prompt(g),"poison","poison");q.diagram=KoreanRoundSolids.diagram(g);q.kind=KoreanRoundSolids.NET.equals(id)?id:"choice";q.stepSupport=false;
        KoreanRoundSolids.choices(q,new Random(394));KoreanRoundSolids.attach(q);return q;
    }
    private Question shape(String solid){return question(KoreanRoundSolids.IDENTIFY,solid,"none",3,"sphere".equals(solid)?0:5);}
    private String answer(Question q){if("add9".equals(q.skillId)){var small=FoundationTeaching.read(q);return ""+(small.left()+small.right());}var g=KoreanRoundSolids.read(q);if(KoreanRoundSolids.NET.equals(q.skillId))return "net:wrap:"+g.radius()+":top:"+g.radius()+":bottom";if(KoreanRoundSolids.IDENTIFY.equals(q.skillId))return g.solid();return switch(g.property()){case "baseCount"->g.solid().equals("cylinder")?"2":"1";case "vertex"->"1";case "height"->"perpendicular";default->"same";};}
    private Checker checker(){return new Checker();}
    private Learning.State student(){Learning.State s=new Learning.State();s.profile.grade=6;s.profile.term=2;s.profile.curriculum=2022;s.profile.schoolYear=2026;s.profile.ready=true;s.profile.foundationAscending=true;s.profile.lastLearnedSkill="unit:"+UNIT;Learning.beginDiagnostic(s,new Random(394));return s;}
    private Question open(Learning.State s,Question fixture,LocalDate day){
        Learning.beginPractice(s,"practice",List.of(fixture.skillId),1,false,new Random(394));Learning.ensureQuestion(s,new Generator(new Random(394)),day);
        // Explicit public fixture for precise old-ID/finite-history boundaries, not native-flow proof.
        s.session.question=fixture;s.session.reviewWork=null;s.session.answers=new ArrayList<>(List.of(""));Review.openFresh(s,day);return fixture;
    }
    private void finish(Learning.State s,LocalDate day){assertTrue(checker().check(s.session.question,List.of(),List.of(answer(s.session.question))).correct());Learning.finishQuestion(s,false,day,new Random(394));}
    private Review.Track track(Learning.State s){return Review.track(s.progress(KoreanRoundSolids.IDENTIFY));}
    private Diagnosis.UnitScreen screen(Learning.State s){return Diagnosis.currentPlan(s).unitScreens.get(UNIT);}
    private Question wrong(Learning.State s,String solid){Question q=open(s,shape(solid),DAY);assertEquals(Checker.Status.WRONG_ANSWER,checker().check(q,List.of(),List.of(solid.equals("cone")?"sphere":"cone")).status);Learning.markError(s,DAY);finish(s,DAY);return q;}
    private <T> T cold(T value)throws Exception{ByteArrayOutputStream bytes=new ByteArrayOutputStream();try(ObjectOutputStream out=new ObjectOutputStream(bytes)){out.writeObject(value);}try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))){@SuppressWarnings("unchecked")T restored=(T)in.readObject();return restored;}}

    @Test public void AllPublicDrawingBoundsAndPoisonedKeysUseOnlyVisibleSource(){
        for(String solid:SHAPES)for(int r=1;r<=9;r++)for(int h=1;h<=18;h++){
            Question q=question(KoreanRoundSolids.IDENTIFY,solid,"none",r,solid.equals("sphere")?0:h);String prompt=q.prompt;double[] values=q.diagram.values.clone();q.answers=null;q.expression="1/0";q.kind="complexRoots";q.choiceLabels=Map.of("cone","fake");q.correctChoice=999;q.studyGuide=new StudyGuide().step("poison","","","0");q.choiceInputs=null;
            assertNotNull(KoreanRoundSolids.read(q));assertTrue(checker().check(q,List.of(),List.of(solid)).correct());assertEquals(prompt,q.prompt);assertArrayEquals(values,q.diagram.values,0);assertNull(q.answers);assertEquals("poison",q.studyGuide.frames.get(0).instruction);
        }
    }
    @Test public void EveryPublicPropertyIsIndependentlyChecked(){
        String[][] rows={{"cylinder","baseCount","2"},{"cylinder","equalBases","same"},{"cylinder","height","perpendicular"},{"cone","baseCount","1"},{"cone","vertex","1"},{"sphere","centerDistance","same"}};
        for(String[] row:rows){Question q=question(KoreanRoundSolids.PROPERTIES,row[0],row[1],2,row[0].equals("sphere")?0:6);assertTrue(checker().check(q,List.of(),List.of(row[2])).correct());for(String key:KoreanRoundSolids.answerOptions(KoreanRoundSolids.read(q)).keySet())assertEquals(key.equals(row[2]),checker().check(q,List.of(),List.of(key)).correct());}
    }
    @Test public void NetAcceptsBothCircleOrdersAndWholeRotationEvenWhenHeightExceedsCircumference(){
        for(int r=1;r<=9;r++)for(int h=1;h<=18;h++){
            Question q=question(KoreanRoundSolids.NET,"cylinder","net",r,h);var g=KoreanRoundSolids.read(q);assertNotNull(g);
            for(int angle:List.of(0,90,180,270))for(boolean swapped:List.of(false,true)){
                String raw="net:wrap:"+r+":"+(swapped?"bottom":"top")+":"+r+":"+(swapped?"top":"bottom")+":"+angle;
                assertTrue(checker().check(q,List.of(),List.of(raw)).correct());assertTrue(HelpPlan.forQuestion(q).step(3).accepts(raw));
            }
            assertTrue(checker().check(q,List.of(),List.of(answer(q))).correct());
            for(String raw:List.of("net:diameter:"+r+":top:"+r+":bottom","net:radius:"+r+":top:"+r+":bottom","net:wrap:"+r+":top:"+r+":top","net:wrap:"+r+":left:"+r+":right"))assertEquals(Checker.Status.WRONG_ANSWER,checker().check(q,List.of(),List.of(raw)).status);
            int wrongRadius=r==9?8:r+1;assertEquals(Checker.Status.WRONG_ANSWER,checker().check(q,List.of(),List.of("net:wrap:"+wrongRadius+":top:"+r+":bottom")).status);
        }
        Question q=question(KoreanRoundSolids.NET,"cylinder","net",3,7);
        for(int radius:List.of(10,11,12))for(String raw:List.of("net:wrap:"+radius+":top:3:bottom","net:wrap:3:top:"+radius+":bottom","net:wrap:"+radius+":top:"+radius+":bottom:90")){
            assertNotNull(KoreanRoundSolids.net(raw));var result=checker().check(q,List.of(),List.of(raw));assertEquals(Checker.Status.WRONG_ANSWER,result.status);assertTrue(result.message.contains("반지름"));
        }
    }
    @Test public void MalformedSourceAndIncompleteNetFailClosedWithoutCachedGuideFallback(){
        Question q=shape("cone");q.prompt+=" extra";assertNull(KoreanRoundSolids.read(q));assertNull(HelpPlan.forQuestion(q));assertEquals(Checker.Status.INPUT_NEEDED,checker().check(q,List.of(),List.of("cone")).status);
        for(double invalid:List.of(Double.NaN,Double.POSITIVE_INFINITY,-1.0,0.5,10.0)){q=shape("cone");q.diagram.values[1]=invalid;assertNull(KoreanRoundSolids.read(q));}
        q=shape("sphere");q.diagram.values[2]=1;assertNull(KoreanRoundSolids.read(q));q=shape("cylinder");q.diagram.rotationDegrees=new double[]{90};assertNull(KoreanRoundSolids.read(q));q=shape("cylinder");q.diagram.labels=new String[]{"answer"};assertNull(KoreanRoundSolids.read(q));
        q=question(KoreanRoundSolids.NET,"cylinder","net",3,7);for(String raw:List.of("","net:unset:0:none:0:none:0","net:wrap:3:top:3:bottom:45","net:wrap:3:top:3:bottom:0:extra","net:wrap:03:top:3:bottom","net:wrap:3:top"))assertEquals(Checker.Status.INPUT_NEEDED,checker().check(q,List.of(),List.of(raw)).status);
    }
    @Test public void HelpUsesCheckedPastInputsButNeverTransfersIntoOriginalAnswer()throws Exception{
        Learning.State s=student();Question q=open(s,question(KoreanRoundSolids.NET,"cylinder","net",2,5),DAY);s.session.answers.set(0,"net:diameter:2:top:2:top:0");HelpPlan plan=HelpPlan.forQuestion(q);assertFalse(plan.canTransfer());s.session.conceptHelp=plan.restore(null,q.id);var d=s.session.conceptHelp;
        StudyGuide h=KoreanRoundSolids.guide(q);assertEquals(4,h.frames.size());assertFalse(h.transfer);assertEquals(KoreanRoundSolids.NET,h.frames.get(3).inputFormat);
        assertTrue(KoreanRoundSolids.references(q,1,List.of("oneCircleRect")).isEmpty());assertFalse(KoreanRoundSolids.references(q,1,List.of("twoCirclesRect")).isEmpty());
        for(int i=0;i<plan.size();i++){String raw=i==3?"net:wrap:2:bottom:2:top:180":h.frames.get(i).expected;assertTrue(plan.step(i).accepts(raw));while(d.entries.size()<=i)d.entries.add("");d.entries.set(i,raw);d.stage=i+1;}
        assertTrue(KoreanRoundSolids.references(q,3,d.entries).isEmpty());Learning.State restored=cold(LearningSnapshot.capture(s));assertEquals("net:diameter:2:top:2:top:0",restored.session.answers.get(0));assertEquals(4,restored.session.conceptHelp.stage);assertEquals(0,restored.session.completed);assertTrue(restored.history.isEmpty());assertEquals(0,restored.reviewSequence);
    }
    @Test public void IdentityDoesNotTurnSizeOrChoiceOrderIntoNewClassificationFacts(){
        Question q=shape("cylinder"),scaled=question(KoreanRoundSolids.IDENTIFY,"cylinder","none",9,18);assertEquals(q.signature(),scaled.signature());Collections.reverse(q.choices);assertEquals(q.signature(),scaled.signature());
        Question a=question(KoreanRoundSolids.NET,"cylinder","net",2,4),b=question(KoreanRoundSolids.NET,"cylinder","net",3,6),c=question(KoreanRoundSolids.NET,"cylinder","net",3,7);assertEquals(a.signature(),b.signature());assertNotEquals(a.signature(),c.signature());
        assertNull(KoreanRoundSolids.displayChoice(a,"poison"));q.choiceLabels=Map.of("cylinder","fake");assertEquals("원기둥",KoreanRoundSolids.displayChoice(q,"cylinder"));
    }
    @Test public void NormalSupplyCreatesTypedNetAndAlwaysVisibleRecognitionChoices(){
        for(String id:KoreanRoundSolids.IDS){assertNotNull(KoreanRoundSolids.read(new Generator(new Random(394)).create(Catalog.get(id))));Set<String> signatures=new LinkedHashSet<>();for(int seed=0;seed<64;seed++)for(boolean mc:List.of(false,true)){
            Question q=new Generator(new Random(seed)).next(id,signatures,mc);assertNotNull(KoreanRoundSolids.read(q));assertEquals(1,q.answers.length);assertFalse(q.stepSupport);assertEquals(KoreanRoundSolids.NET.equals(id)?id:"choice",q.kind);assertTrue(checker().check(q,List.of(),List.of(answer(q))).correct());assertEquals(q.signature(),KoreanRoundSolids.identity(q));
            if(KoreanRoundSolids.NET.equals(id))assertTrue(q.choices.isEmpty());else{assertEquals(KoreanRoundSolids.answerOptions(KoreanRoundSolids.read(q)).size(),new HashSet<>(q.choices).size());assertEquals(1,q.choices.stream().filter(key->checker().check(q,List.of(),List.of(key)).correct()).count());}signatures.add(q.signature());
        }assertTrue(signatures.size()>=3);}
        Question first=KoreanRoundSolids.create(KoreanRoundSolids.IDENTIFY,new Random(1));Map<String,Integer> recent=new LinkedHashMap<>();for(String solid:SHAPES)recent.put(shape(solid).signature(),solid.equals("cone")?0:9);Question oldest=KoreanRoundSolids.next(KoreanRoundSolids.IDENTIFY,new Random(2),CurriculumLimits.NONE,recent);assertEquals("cone",KoreanRoundSolids.read(oldest).solid());assertNotNull(first);
        try{KoreanRoundSolids.next(KoreanRoundSolids.IDENTIFY,new Random(1),new CurriculumLimits("maxGiven=5"),Map.of());fail();}catch(IllegalArgumentException expected){}
    }
    @Test public void ReviewedKoreanEndpointIncludesThreeNewRepresentativesAndPreservesOtherSystems(){
        Learning.State s=student();var unit=KoreanFoundationUnits.learned(s.profile).stream().filter(u->u.key().equals(UNIT)).findFirst().orElseThrow();assertEquals(KoreanRoundSolids.IDS,unit.representatives());assertFalse(KoreanFoundationUnits.missingRepresentatives(s.profile).stream().anyMatch(u->u.key().equals(UNIT)));
        for(String id:KoreanRoundSolids.IDS){assertTrue(Curriculum.inCurriculum(Catalog.get(id),2022));assertFalse(Curriculum.inCurriculum(Catalog.get(id),2015));assertTrue(Diagnosis.currentPlan(s).scope.contains(id));}
        s.profile.excluded.add(KoreanRoundSolids.NET);assertFalse(KoreanFoundationUnits.scope(s.profile).contains(KoreanRoundSolids.NET));s.profile.curriculum=2015;s.profile.lastLearnedSkill="unit:KR|2015|6|2|6";assertTrue(KoreanFoundationUnits.scope(s.profile).stream().noneMatch(KoreanRoundSolids::supports));
        s.profile.countryCode="NA";s.profile.educationSystem="na-nied-primary-2024-v1";assertTrue(GlobalCurriculum.available(s.profile).stream().noneMatch(x->KoreanRoundSolids.supports(x.id)));s.profile.educationSystem="common-foundations";assertTrue(GlobalCurriculum.available(s.profile).stream().noneMatch(x->KoreanRoundSolids.supports(x.id)));
    }
    @Test public void AlgebraCannotBypassTheDrawingOrEditorAndWorkAnswerRemainsExplicitlyUnavailable(){
        for(String id:KoreanRoundSolids.IDS){Question q=KoreanRoundSolids.create(id,new Random(394));var result=checker().check(q,List.of("1=1"),List.of(answer(q)),List.of(Checker.StepKind.FULL));assertEquals(Checker.Status.WRONG_STEP,result.status);assertEquals(Checker.InputTarget.WORK,result.inputTarget);assertEquals(Checker.Status.WRONG_STEP,checker().checkSteps(q,List.of("1=1"),List.of(Checker.StepKind.FULL)).status);assertTrue(WorkAnswer.writtenAnswers(q,List.of("1=1"),List.of(Checker.StepKind.FULL)).isEmpty());assertEquals("",WorkAnswer.writtenAnswer(q,List.of("1=1"),List.of(Checker.StepKind.FULL)));}
    }
    @Test public void AllFourFailedFactsCanRecoverWithDistinctLaterFactsAndKeepFutureDueRounds()throws Exception{
        Learning.State s=student();for(String solid:SHAPES)wrong(s,solid);Set<String> failures=new LinkedHashSet<>(track(s).failed);assertEquals(4,failures.size());assertTrue(track(s).pending);
        LocalDate later=DAY.plusDays(1);
        open(s,shape("cylinder"),later);assertTrue("First later retrieval is eligible",s.session.reviewWork.recheck);finish(s,later);s=cold(LearningSnapshot.capture(s));
        open(s,shape("cone"),later);assertFalse("Immediate second retrieval cannot bypass GAP",s.session.reviewWork.recheck);
        // Two actual unrelated independent submissions, not a direct sequence injection.
        for(String[] warmup:new String[][]{{"2 + 3","5"},{"3 + 4","7"}}){Question warm=new Question("add9",warmup[0],warmup[0],warmup[1]);open(s,warm,later);finish(s,later);}
        open(s,shape("cone"),later);assertTrue("Two actual intervening submissions satisfy GAP",s.session.reviewWork.recheck);finish(s,later);s=cold(LearningSnapshot.capture(s));
        assertFalse(track(s).pending);assertEquals(2,track(s).recoveryChecks);assertEquals(failures,track(s).finiteFailedHistory);assertEquals(Diagnosis.UnitStatus.RECENTLY_CHECKED,screen(s).status);assertTrue(Diagnosis.studyUnits(s).contains(screen(s)));assertEquals(1,track(s).checked.size());
        LocalDate due=LocalDate.ofEpochDay(track(s).dueDay);open(s,shape("sphere"),due);assertTrue(s.session.reviewWork.recheck);finish(s,due);assertEquals(due.plusDays(2).toEpochDay(),track(s).dueDay);assertEquals(failures,track(s).finiteFailedHistory);
    }
    @Test public void SameFactSameQuestionHelpAndInvalidGeometryCannotClearPending(){
        Learning.State s=student();Question old=wrong(s,"cylinder");wrong(s,"cone");open(s,shape("cylinder"),DAY);assertFalse(s.session.reviewWork.recheck);finish(s,DAY);
        LocalDate later=DAY.plusDays(1);open(s,old,later);assertFalse(s.session.reviewWork.recheck);finish(s,later);open(s,shape("cylinder"),later);assertTrue(s.session.reviewWork.recheck);finish(s,later);open(s,shape("cylinder"),later.plusDays(1));assertFalse(s.session.reviewWork.recheck);finish(s,later.plusDays(1));assertEquals(1,track(s).recoveryChecks);assertFalse(Review.diagnosticRecent(s,KoreanRoundSolids.IDENTIFY));assertTrue(track(s).pending);
        open(s,shape("sphere"),later.plusDays(1));Review.helpUsed(s,later.plusDays(1));finish(s,later.plusDays(1));assertTrue(track(s).pending);assertEquals(0,track(s).recoveryChecks);Question malformed=shape("prism");malformed.diagram.values[0]=99;open(s,malformed,later.plusDays(2));assertFalse(s.session.reviewWork.recheck);assertFalse(Review.diagnosticRecent(s,KoreanRoundSolids.IDENTIFY));assertTrue(track(s).pending);
        for(String id:List.of(KoreanRoundSolids.PROPERTIES,KoreanRoundSolids.NET)){
            Learning.State other=student();Question first=question(id,"cylinder",KoreanRoundSolids.NET.equals(id)?"net":"baseCount",2,5);open(other,first,DAY);Learning.markError(other,DAY);finish(other,DAY);
            Question next=question(id,"cylinder",KoreanRoundSolids.NET.equals(id)?"net":"baseCount",2,5);assertNotEquals(first.id,next.id);open(other,next,later);assertTrue(other.session.reviewWork.recheck);assertFalse(Review.diagnosticRecent(other,id));assertTrue(Review.track(other.progress(id)).pending);
        }
    }
    @Test public void ExistingLineAndNonfiniteReviewDoNotLoseTheirDifferentPolicies(){
        Learning.State s=new Learning.State();Question q=new Question("add9","2 + 3","2+3","5");open(s,q,DAY);Learning.markError(s,DAY);finish(s,DAY);open(s,new Question("add9","2 + 3","2+3","5"),DAY.plusDays(1));assertFalse(s.session.reviewWork.recheck);assertNull(Review.track(s.progress("add9")).finiteFailedHistory);
        Question line=KoreanPlaneFoundations.next(KoreanPlaneFoundations.LINE,new Random(1),CurriculumLimits.NONE,Map.of());assertNotNull(KoreanPlaneFoundations.read(line));assertFalse(KoreanRoundSolids.supports(line.skillId));Question legacy=new Question("add9","2 + 3","2+3","5");WholeNumberRelations.attach(legacy);assertEquals("whole-number-relations-v1",legacy.studyGuide.teachingVersion);assertNotNull(HelpPlan.forQuestion(legacy));
    }
}
