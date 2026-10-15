package com.gomgomapps.math.core;

import org.junit.Test;
import static org.junit.Assert.*;
import java.time.LocalDate;
import java.util.*;

public class KoreanLargeIntegrationTest {
    static final List<String> IDS=List.of("krLargeRead","krLargeWrite","krLargePlace","krLargeCompare","krLargeCount");
    static final String UNIT="KR|2022|4|1|1";
    static final LocalDate DAY=LocalDate.of(2026,10,15);
    static Learning.State student(){Learning.State s=new Learning.State();s.profile.grade=6;s.profile.term=2;s.profile.schoolYear=2026;s.profile.curriculum=2022;s.profile.ready=true;s.profile.foundationAscending=true;s.profile.lastLearnedSkill="unit:KR|2022|6|2|5";return s;}

    @Test public void ActualGeneratorCheckerHelpAndWorkPathsUseOnlyExactVisibleGivens(){
        Checker checker=new Checker();
        for(String id:IDS)for(boolean multiple:List.of(false,true)){
            Generator generator=new Generator(new Random(378));List<String> recent=new ArrayList<>();
            for(int i=0;i<16;i++){
                Question q=generator.next(id,recent,multiple);var g=KoreanLargeNumbers.read(q);assertNotNull(g);assertFalse(q.stepSupport);assertFalse(recent.contains(q.signature()));recent.add(q.signature());
                String expected=g.answerKey();assertTrue(checker.check(q,List.of(),List.of(expected)).correct());HelpPlan plan=HelpPlan.forQuestion(q);assertNotNull(plan);assertFalse(plan.canTransfer());
                if(id.equals("krLargeRead")||id.equals("krLargeCompare")){assertFalse(q.choices.isEmpty());assertEquals(1,q.choices.stream().filter(a->checker.check(q,List.of(),List.of(a)).correct()).count());assertTrue(q.choiceLabels.keySet().containsAll(q.choices));}
                q.answers=null;q.studyGuide=new StudyGuide().step("cached","cached = ","","999");q.correctChoice=999;q.choiceLabels.clear();
                assertTrue(checker.check(q,List.of(),List.of(expected)).correct());assertNotNull(HelpPlan.forQuestion(q));assertFalse(HelpPlan.forQuestion(q).canTransfer());
                assertEquals(Checker.Status.WRONG_STEP,checker.check(q,List.of("1+1=2"),List.of(expected),List.of(Checker.StepKind.FULL)).status);
                assertFalse(checker.checkSteps(q,List.of("1+1=2"),List.of(Checker.StepKind.FULL)).correct());assertEquals("",WorkAnswer.writtenAnswer(q,List.of("1+1=2"),List.of(Checker.StepKind.FULL)));assertTrue(WorkAnswer.writtenAnswers(q,List.of(),List.of()).isEmpty());
                q.prompt="invalid public text";assertEquals(Checker.Status.INPUT_NEEDED,checker.check(q,List.of(),List.of("999")).status);assertNull(HelpPlan.forQuestion(q));
            }
        }
    }

    @Test public void LearnedUnitHasFiveDifferentChecksOnlyInReviewedCountryVersionAndEndpoint(){
        Learning.State s=student();var p=s.profile;p.lastLearnedSkill="unit:KR|2022|3|2|6";assertTrue(KoreanFoundationUnits.scope(p).stream().noneMatch(IDS::contains));p.lastLearnedSkill="unit:KR|2022|4|1|1";
        var unit=KoreanFoundationUnits.learned(p).stream().filter(u->u.key().equals(UNIT)).findFirst().orElseThrow();assertTrue(unit.members().contains("largePlace"));assertEquals(Set.copyOf(IDS),Set.copyOf(unit.representatives()));assertEquals(6,unit.members().size());
        for(String id:IDS){assertTrue(Curriculum.inCurriculum(Catalog.get(id),2022));assertFalse(Curriculum.inCurriculum(Catalog.get(id),2015));}
        p.excluded.add("krLargeWrite");assertFalse(KoreanFoundationUnits.scope(p).contains("krLargeWrite"));assertTrue(KoreanFoundationUnits.scope(p).contains("krLargeRead"));p.excluded.clear();p.curriculum=2015;p.lastLearnedSkill="";assertTrue(KoreanFoundationUnits.available(p).stream().noneMatch(u->u.members().stream().anyMatch(IDS::contains)));
        p.curriculum=2022;p.countryCode="NA";p.educationSystem="na-national";assertTrue(KoreanFoundationUnits.available(p).isEmpty());assertTrue(LargePlaceFoundations.supports("largePlaceTrillion"));
    }

    @Test public void HelpAndBodyDraftsSurviveColdWithoutTurningTheHelpIntoAnIndependentSubmission(){
        Learning.State s=student();Random r=new Random(3781);Learning.beginDiagnostic(s,r);Learning.beginPractice(s,"practice",List.of("krLargeWrite"),20,false,r,Map.of("krLargeWrite",4));Question q=Learning.ensureQuestion(s,new Generator(r),DAY);
        HelpPlan plan=HelpPlan.forQuestion(q);s.session.conceptHelp=plan.restore(null,q.id);s.session.conceptHelp.entries.set(0,"7");s.session.answers.set(0,"9007199254740993");Review.helpUsed(s,DAY);
        Diagnosis.UnitScreen unit=Diagnosis.currentPlan(s).unitScreens.get(UNIT);assertEquals("help",unit.reasons.get(q.skillId));assertEquals(Diagnosis.UnitStatus.NEEDS_STUDY,unit.status);assertEquals(0,s.session.completed);assertEquals(0,s.progress(q.skillId).attempts);
        Learning.State cold=LearningSnapshot.capture(s);assertEquals(q.id,cold.session.question.id);assertEquals(List.of("9007199254740993"),cold.session.answers);assertEquals("7",cold.session.conceptHelp.entries.get(0));assertFalse(HelpPlan.forQuestion(cold.session.question).canTransfer());assertEquals("help",Diagnosis.currentPlan(cold).unitScreens.get(UNIT).reasons.get(q.skillId));assertTrue(Diagnosis.studyUnits(cold).stream().anyMatch(u->u.key.equals(UNIT)));
    }
}
