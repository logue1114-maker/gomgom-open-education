package com.gomgomapps.math.core;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;

public class KoreanPlaneIntegrationTest {
    private static final List<String> IDS=List.of(KoreanPlaneFoundations.LINE,KoreanPlaneFoundations.ANGLE,KoreanPlaneFoundations.TRIANGLE,KoreanPlaneFoundations.QUAD);
    @Test public void ActualGeneratorAlwaysUsesLabeledChoicesWithFreshIntrinsicFacts(){
        Checker checker=new Checker();for(String id:IDS){
            Question created=new Generator(new Random(376)).create(Catalog.get(id));assertNotNull(KoreanPlaneFoundations.read(created));assertFalse(created.choices.isEmpty());
            for(boolean mc:List.of(false,true)){
                Generator generator=new Generator(new Random(3761));List<String> recent=new ArrayList<>();int count=id.equals(KoreanPlaneFoundations.LINE)?3:id.equals(KoreanPlaneFoundations.ANGLE)?13:12;
                for(int i=0;i<count;i++){
                    Question q=generator.next(id,recent,mc);var g=KoreanPlaneFoundations.read(q);assertNotNull(g);assertEquals("choice",q.kind);assertFalse(q.stepSupport);assertFalse(recent.contains(q.signature()));recent.add(q.signature());assertEquals(KoreanPlaneFoundations.identity(q),q.signature());
                    assertEquals(id.equals(KoreanPlaneFoundations.LINE)||id.equals(KoreanPlaneFoundations.QUAD)?3:2,q.choices.size());assertEquals(q.choices.size(),new HashSet<>(q.choices).size());assertEquals(1,q.choices.stream().filter(a->checker.check(q,List.of(),List.of(a)).correct()).count());assertEquals(g.answerKey(),q.choices.get(q.correctChoice));assertTrue(q.choiceLabels.keySet().containsAll(q.choices));
                }
                assertEquals(recent.get(0),generator.next(id,recent,mc).signature());
            }
        }
    }
    @Test public void BodyAndDirectWorkApisCannotUseHiddenAnswerOrFabricateArithmeticTransfer(){
        Checker checker=new Checker();for(String id:IDS){
            Question q=new Generator(new Random(3762)).create(Catalog.get(id));var publicGiven=KoreanPlaneFoundations.read(q);String answer=publicGiven.answerKey();StudyGuide privateGuide=new StudyGuide().step("private","private = ","","999");q.answers=null;q.expression="999+999";q.kind="complexRoots";q.studyGuide=privateGuide;q.withInputs(999);
            assertTrue(checker.check(q,List.of(),List.of(answer)).correct());assertTrue(checker.checkSteps(q,List.of(),List.of()).correct());Checker.Result work=checker.check(q,List.of("999+999=1998"),List.of(answer),List.of(Checker.StepKind.FULL));assertEquals(Checker.Status.WRONG_STEP,work.status);assertEquals(Checker.InputTarget.WORK,work.inputTarget);assertFalse(checker.checkSteps(q,List.of("999+999=1998"),List.of(Checker.StepKind.FULL)).correct());
            assertEquals("",WorkAnswer.writtenAnswer(q,List.of("999+999=1998"),List.of(Checker.StepKind.FULL)));assertTrue(WorkAnswer.writtenAnswers(q,List.of("999+999=1998"),List.of(Checker.StepKind.FULL)).isEmpty());HelpPlan plan=HelpPlan.forQuestion(q);assertNotNull(plan);assertFalse(plan.canTransfer());assertSame(privateGuide,q.studyGuide);assertNull(q.answers);assertEquals("999+999",q.expression);assertEquals("complexRoots",q.kind);
            q.prompt="invalid public prompt";assertEquals(Checker.Status.INPUT_NEEDED,checker.check(q,List.of(),List.of("999")).status);assertEquals(Checker.Status.INPUT_NEEDED,checker.checkSteps(q,List.of(),List.of()).status);assertNull(HelpPlan.forQuestion(q));
        }
    }
    @Test public void ScopeIsKorean2022OnlyAndEndPointAndExclusionsRemainAuthoritative(){
        Learning.Profile p=new Learning.Profile();p.grade=6;p.term=2;p.schoolYear=2026;p.curriculum=2022;p.foundationAscending=true;p.lastLearnedSkill="unit:KR|2022|3|1|1";assertTrue(KoreanFoundationUnits.scope(p).stream().noneMatch(IDS::contains));p.lastLearnedSkill="unit:KR|2022|3|1|2";assertTrue(KoreanFoundationUnits.scope(p).containsAll(IDS));
        var unit=KoreanFoundationUnits.learned(p).stream().filter(u->u.key().equals("KR|2022|3|1|2")).findFirst().orElseThrow();assertEquals(Set.copyOf(IDS),Set.copyOf(unit.members()));assertEquals(Set.copyOf(IDS),Set.copyOf(unit.representatives()));assertFalse(unit.members().contains("el_shape_sides"));
        p.excluded.add(KoreanPlaneFoundations.QUAD);assertFalse(KoreanFoundationUnits.scope(p).contains(KoreanPlaneFoundations.QUAD));assertTrue(KoreanFoundationUnits.scope(p).contains(KoreanPlaneFoundations.LINE));for(String id:IDS){assertTrue(Curriculum.inCurriculum(Catalog.get(id),2022));assertFalse(Curriculum.inCurriculum(Catalog.get(id),2015));}
        p.curriculum=2015;assertTrue(KoreanFoundationUnits.available(p).stream().noneMatch(u->u.members().stream().anyMatch(IDS::contains)));p.curriculum=2022;p.countryCode="NA";p.educationSystem="na-national";assertTrue(KoreanFoundationUnits.available(p).isEmpty());
    }
    @Test public void PaperCornerVisitsAreSeparateFromAnswersCopiedAndResetWithTeachingIdentity(){
        Question q=new Generator(new Random(3763)).create(Catalog.get(KoreanPlaneFoundations.QUAD));HelpPlan plan=HelpPlan.forQuestion(q);HelpPlan.Draft d=plan.restore(null,q.id);d.entries.set(0,"4");d.stage=1;d.entries.add("");d.planeVisitStage=1;d.planeVertex=2;d.planeVisited=new ArrayList<>(List.of(0,2));
        HelpPlan.Draft copy=d.copy();assertEquals(List.of(0,2),copy.planeVisited);assertEquals(2,copy.planeVertex);assertEquals(1,copy.planeVisitStage);copy.planeVisited.clear();assertEquals(List.of(0,2),d.planeVisited);assertEquals("",d.entries.get(1));assertFalse(plan.canTransfer());
        Learning.State state=new Learning.State();state.session=new Learning.Session();state.session.question=q;state.session.conceptHelp=d;state.session.answers.add("");Learning.State cold=LearningSnapshot.capture(state);assertEquals(List.of(0,2),cold.session.conceptHelp.planeVisited);assertEquals(2,cold.session.conceptHelp.planeVertex);assertEquals(1,cold.session.conceptHelp.planeVisitStage);assertEquals(List.of(""),cold.session.answers);assertNotSame(d,cold.session.conceptHelp);
        d.teachingVersion="older teacher";HelpPlan.Draft reset=plan.restore(d,q.id);assertEquals(0,reset.stage);assertTrue(reset.planeVisited.isEmpty());assertEquals(-1,reset.planeVertex);assertEquals(-1,reset.planeVisitStage);
        boolean diagonalRight=false;for(int i=0;i<100;i++){var g=KoreanPlaneFoundations.read(new Generator(new Random(i)).create(Catalog.get(KoreanPlaneFoundations.ANGLE)));if(g.answerKey().equals("right")){var a=g.vertices().get(0);var v=g.vertices().get(1);diagonalRight|=a.x()!=v.x()&&a.y()!=v.y();}}assertTrue(diagonalRight);
    }
}
