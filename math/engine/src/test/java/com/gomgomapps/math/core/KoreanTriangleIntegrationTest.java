package com.gomgomapps.math.core;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;
import java.time.LocalDate;

public class KoreanTriangleIntegrationTest {
    private static final List<String> IDS=List.of("krTriangleSides","krTriangleAngles","krTriangleProperties");
    private static final String UNIT="KR|2022|4|2|2";
    private Learning.State student(){Learning.State s=new Learning.State();s.profile.grade=6;s.profile.term=2;s.profile.schoolYear=2026;s.profile.curriculum=2022;s.profile.ready=true;s.profile.foundationAscending=true;s.profile.lastLearnedSkill="unit:KR|2022|6|2|5";return s;}
    @Test public void normalGeneratorUsesPicturesEvenWithoutMultipleChoiceAndDoesNotRepeatPresentationVariants(){
        Checker checker=new Checker();for(String id:IDS)for(boolean mc:List.of(false,true)){
            Generator gen=new Generator(new Random(382));List<String> recent=new ArrayList<>();
            for(int i=0;i<8;i++){
                Question q=gen.next(id,recent,mc);assertNotNull(KoreanTriangleClassification.read(q));assertFalse(q.stepSupport);assertEquals("choice",q.kind);assertTrue(q.choices.size()>=3);assertTrue(q.choiceDiagrams.isEmpty());assertFalse(recent.contains(q.signature()));recent.add(q.signature());
                assertEquals(1,q.choices.stream().filter(a->checker.check(q,List.of(),List.of(a)).correct()).count());
                assertEquals(KoreanTriangleClassification.identity(q),q.signature());
            }
        }
    }
    @Test public void curriculumEndpointAndExclusionControlTheNewUnit(){
        var p=student().profile;p.lastLearnedSkill="unit:KR|2022|4|2|1";assertTrue(KoreanFoundationUnits.scope(p).stream().noneMatch(IDS::contains));p.lastLearnedSkill="unit:"+UNIT;assertTrue(KoreanFoundationUnits.scope(p).containsAll(IDS));
        var unit=KoreanFoundationUnits.learned(p).stream().filter(u->u.key().equals(UNIT)).findFirst().orElseThrow();assertEquals(IDS,unit.representatives());assertEquals(IDS,unit.members());
        p.excluded.add(IDS.get(1));assertFalse(KoreanFoundationUnits.scope(p).contains(IDS.get(1)));assertTrue(KoreanFoundationUnits.scope(p).contains(IDS.get(2)));
        for(String id:IDS){assertTrue(Curriculum.inCurriculum(Catalog.get(id),2022));assertFalse(Curriculum.inCurriculum(Catalog.get(id),2015));}
        p.curriculum=2015;assertTrue(KoreanFoundationUnits.available(p).stream().noneMatch(u->u.members().stream().anyMatch(IDS::contains)));
        p.curriculum=2022;p.countryCode="NA";p.educationSystem="na-national";assertTrue(KoreanFoundationUnits.available(p).isEmpty());
    }
    @Test public void publicCheckerDoesNotTransferAnArithmeticWorkAnswerOrTrustStoredKeys(){
        Checker checker=new Checker();for(String id:IDS){Question q=new Generator(new Random(3822)).create(Catalog.get(id));String correct=q.choices.stream().filter(k->checker.check(q,List.of(),List.of(k)).correct()).findFirst().orElseThrow();q.answers=null;q.correctChoice=-1;q.expression="99+99";q.studyGuide=new StudyGuide().step("private","","","198");q.choiceLabels.replaceAll((k,v)->"정답");
            assertTrue(checker.check(q,List.of(),List.of(correct)).correct());assertNotEquals("정답",KoreanTriangleClassification.displayChoice(q,correct));
            assertFalse(checker.check(q,List.of("99+99=198"),List.of(correct),List.of(Checker.StepKind.FULL)).correct());assertTrue(WorkAnswer.writtenAnswers(q,List.of("99+99=198"),List.of(Checker.StepKind.FULL)).isEmpty());
            assertNotNull(HelpPlan.forQuestion(q));assertFalse(HelpPlan.forQuestion(q).canTransfer());q.prompt="invalid";assertNull(HelpPlan.forQuestion(q));assertEquals(Checker.Status.INPUT_NEEDED,checker.check(q,List.of(),List.of(correct)).status);
        }
    }
    @Test public void additivePlanUpgradeKeepsAnActualRunningQuestionAndAddsExactlyThreeChecks(){
        Learning.State s=student();Random random=new Random(3823);Learning.beginDiagnostic(s,random);Question question=Learning.ensureQuestion(s,new Generator(random),LocalDate.of(2026,10,15));Diagnosis.Plan old=Diagnosis.currentPlan(s);s.diagnostics.remove(old.key);
        old.scope.removeIf(IDS::contains);old.placements.keySet().removeIf(IDS::contains);var unit=old.unitScreens.get(UNIT);unit.members.clear();unit.representatives.clear();unit.label="이전 미지원 단원";
        Set<String> removed=new HashSet<>();for(var x:old.checks.entrySet())if(IDS.contains(x.getValue().skillId))removed.add(x.getKey());old.checks.keySet().removeAll(removed);old.pending.removeAll(removed);old.untested.removeAll(removed);
        List<String> sorted=new ArrayList<>(old.scope);Collections.sort(sorted);old.key=Curriculum.MAPPING_VERSION+"|"+old.schoolYear+"|"+old.curriculum+"|"+old.actualGrade+"|"+old.term+"||"+String.join(",",sorted)+"|foundation-unit-screen-v2|units-"+KoreanFoundationUnits.MAPPING_VERSION+"|"+old.lastLearnedSkill;for(var u:old.unitScreens.values())u.planKey=old.key;s.diagnostics.put(old.key,old);
        int remaining=Diagnosis.remainingChecks(old),count=old.scope.size();s.session.answers.set(0,"123");s.session.conceptHelp=new HelpPlan.Draft();s.session.conceptHelp.questionId=question.id;s.session.conceptHelp.countArithmeticOpen=true;s.session.conceptHelp.entries.add("888");
        Diagnosis.migrate(s);var next=Diagnosis.currentPlan(s);assertNotSame(old,next);assertEquals(count+3,next.scope.size());assertEquals(remaining+3,Diagnosis.remainingChecks(next));assertEquals(remaining,Diagnosis.remainingChecks(old));assertSame(question,s.session.question);assertEquals(List.of("123"),s.session.answers);assertEquals(List.of("888"),s.session.conceptHelp.entries);assertTrue(s.session.conceptHelp.countArithmeticOpen);assertSame(next,s.session.diagnosticRun.plan);
        Learning.State cold=LearningSnapshot.capture(s);assertEquals(question.id,cold.session.question.id);assertEquals(List.of("123"),cold.session.answers);assertEquals(List.of("888"),cold.session.conceptHelp.entries);assertSame(Diagnosis.currentPlan(cold),cold.session.diagnosticRun.plan);assertEquals(Diagnosis.UnitStatus.UNCHECKED,next.unitScreens.get(UNIT).status);
    }
    @Test public void helpAndOneCorrectKeepHomeStudyAndConcreteLessonDoesNotCreateMastery(){
        Learning.State s=student();Random random=new Random(3826);Learning.beginDiagnostic(s,random);Diagnosis.Plan plan=Diagnosis.currentPlan(s);
        Diagnosis.UnitScreen unit=plan.unitScreens.get(UNIT);String id=IDS.get(0);KoreanFoundationStudy.flagged(s,unit,id,"unable",LocalDate.of(2026,10,15));
        Learning.beginPractice(s,"practice",Set.of(id),20,false,random);Question q=Learning.ensureQuestion(s,new Generator(random),LocalDate.of(2026,10,15));assertEquals(id,q.skillId);
        Review.helpUsed(s,LocalDate.of(2026,10,15));int history=s.history.size(),completed=s.session.completed;var d=HelpPlan.forQuestion(q).restore(null,q.id);s.session.conceptHelp=d;
        var h=KoreanTriangleClassification.guide(q);for(int i=0;i<3;i++)assertTrue(KoreanTriangleLesson.visit(q,d,i));while(d.stage<h.frames.size())assertTrue(KoreanTriangleLesson.advance(q,d,h.frames.get(d.stage).expected));
        assertEquals(history,s.history.size());assertEquals(completed,s.session.completed);assertEquals(List.of(""),s.session.answers);assertFalse(Review.track(s.progress(id)).recent);assertTrue(unit.recoveredSkills.isEmpty());
        String correct=q.choices.stream().filter(k->new Checker().check(q,List.of(),List.of(k)).correct()).findFirst().orElseThrow();s.session.answers.set(0,correct);Learning.finishQuestion(s,false,LocalDate.of(2026,10,15),random);
        assertEquals(Diagnosis.UnitStatus.NEEDS_STUDY,unit.status);assertTrue(Review.track(s.progress(id)).pending);assertTrue(unit.recoveredSkills.isEmpty());assertTrue(Diagnosis.studyUnits(s).stream().anyMatch(u->u.key.equals(UNIT)));
        Learning.State cold=LearningSnapshot.capture(s);assertEquals(Diagnosis.UnitStatus.NEEDS_STUDY,cold.diagnostics.get(plan.key).unitScreens.get(UNIT).status);
        Diagnosis.beginStudy(s,unit,random);assertEquals(id,s.session.queue.get(2));assertEquals(id,s.session.queue.get(5));
    }
}
