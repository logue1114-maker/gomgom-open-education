package com.gomgomapps.math.core;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;
import java.time.LocalDate;

public class KoreanRulesIntegrationTest {
    private static final List<String> IDS=List.of("krNumberRule","krFigureRule","krCalculationRule","krEqualRelation","krEqualFill");
    private static final String UNIT="KR|2022|4|2|3";
    private Learning.State student(){Learning.State s=new Learning.State();s.profile.grade=6;s.profile.term=2;s.profile.schoolYear=2026;s.profile.curriculum=2022;s.profile.ready=true;s.profile.foundationAscending=true;s.profile.lastLearnedSkill="unit:KR|2022|6|2|5";return s;}
    @Test public void normalGeneratorAndChoicesUseTheReviewedRuleFamilyInBothPracticeFormats(){
        Checker checker=new Checker();
        for(String id:IDS)for(boolean mc:List.of(false,true)){
            Generator gen=new Generator(new Random(385));List<String> recent=new ArrayList<>();
            for(int i=0;i<8;i++){
                Question q=gen.next(id,recent,mc);assertNotNull(KoreanRulesRelations.read(q));assertFalse(q.stepSupport);assertEquals("choice",q.kind);
                assertEquals(id.equals("krEqualRelation")?2:4,q.choices.size());assertEquals(q.choices.size(),new HashSet<>(q.choices).size());assertTrue(q.choiceDiagrams.isEmpty());assertFalse(recent.contains(q.signature()));recent.add(q.signature());
                assertEquals(1,q.choices.stream().filter(x->checker.check(q,List.of(),List.of(x)).correct()).count());assertEquals(KoreanRulesRelations.identity(q),q.signature());
                if(id.equals("krFigureRule"))assertNotNull(q.diagram);
            }
        }
    }
    @Test public void actualEndpointAndExclusionsLimitKoreanRepresentativesWithoutChanging2015OrForeignPlans(){
        var p=student().profile;p.lastLearnedSkill="unit:KR|2022|4|2|2";assertTrue(KoreanFoundationUnits.scope(p).stream().noneMatch(IDS::contains));p.lastLearnedSkill="unit:"+UNIT;assertTrue(KoreanFoundationUnits.scope(p).containsAll(IDS));
        var u=KoreanFoundationUnits.learned(p).stream().filter(x->x.key().equals(UNIT)).findFirst().orElseThrow();assertEquals(IDS,u.members());assertEquals(IDS,u.representatives());
        p.excluded.add("krFigureRule");assertFalse(KoreanFoundationUnits.scope(p).contains("krFigureRule"));assertTrue(KoreanFoundationUnits.scope(p).contains("krEqualFill"));
        for(String id:IDS){assertTrue(Curriculum.inCurriculum(Catalog.get(id),2022));assertFalse(Curriculum.inCurriculum(Catalog.get(id),2015));}
        p.curriculum=2015;assertTrue(KoreanFoundationUnits.available(p).stream().noneMatch(x->x.members().stream().anyMatch(IDS::contains)));
        p.curriculum=2022;p.countryCode="NA";p.educationSystem="na-national";assertTrue(KoreanFoundationUnits.available(p).isEmpty());
    }
    @Test public void checkerDisplayAndHelpRebuildFromPublicDataRatherThanPoisonedCachedSolutions(){
        Checker checker=new Checker();
        for(String id:IDS){
            Question q=new Generator(new Random(3851)).create(Catalog.get(id));String correct=q.choices.stream().filter(x->checker.check(q,List.of(),List.of(x)).correct()).findFirst().orElseThrow();
            q.answers=null;q.correctChoice=-1;q.expression="99+99";q.studyGuide=new StudyGuide().step("private","","","198");q.choiceLabels.replaceAll((k,v)->"정답");
            assertTrue(checker.check(q,List.of(),List.of(correct)).correct());assertNotEquals("정답",KoreanRulesRelations.displayChoice(q,correct));assertNotNull(HelpPlan.forQuestion(q));assertFalse(HelpPlan.forQuestion(q).canTransfer());
            assertFalse(checker.check(q,List.of("99+99=198"),List.of(correct),List.of(Checker.StepKind.FULL)).correct());assertTrue(WorkAnswer.writtenAnswers(q,List.of("99+99=198"),List.of(Checker.StepKind.FULL)).isEmpty());
            q.prompt="invalid";assertNull(HelpPlan.forQuestion(q));assertEquals(Checker.Status.INPUT_NEEDED,checker.check(q,List.of(),List.of(correct)).status);
        }
    }
    @Test public void additiveUpgradePreservesTheRunningQuestionWrongInputAndExistingConceptHelp(){
        Learning.State s=student();Random r=new Random(3852);Learning.beginDiagnostic(s,r);Question q=Learning.ensureQuestion(s,new Generator(r),LocalDate.of(2026,10,15));Diagnosis.Plan old=Diagnosis.currentPlan(s);s.diagnostics.remove(old.key);
        old.scope.removeIf(IDS::contains);old.placements.keySet().removeIf(IDS::contains);var unit=old.unitScreens.get(UNIT);unit.members.clear();unit.representatives.clear();unit.label="이전 미지원 단원";
        Set<String> removed=new HashSet<>();for(var x:old.checks.entrySet())if(IDS.contains(x.getValue().skillId))removed.add(x.getKey());old.checks.keySet().removeAll(removed);old.pending.removeAll(removed);old.untested.removeAll(removed);
        List<String> sorted=new ArrayList<>(old.scope);Collections.sort(sorted);old.key=Curriculum.MAPPING_VERSION+"|"+old.schoolYear+"|"+old.curriculum+"|"+old.actualGrade+"|"+old.term+"||"+String.join(",",sorted)+"|foundation-unit-screen-v2|units-"+KoreanFoundationUnits.MAPPING_VERSION+"|"+old.lastLearnedSkill;for(var u:old.unitScreens.values())u.planKey=old.key;s.diagnostics.put(old.key,old);
        int remaining=Diagnosis.remainingChecks(old),count=old.scope.size();s.session.answers.set(0,"123");s.session.conceptHelp=new HelpPlan.Draft();s.session.conceptHelp.questionId=q.id;s.session.conceptHelp.entries.add("888");s.session.conceptHelp.oldTriangleEntries.add("70");s.session.conceptHelp.triangleFoldActions.add("axis:0");
        Diagnosis.migrate(s);var next=Diagnosis.currentPlan(s);assertNotSame(old,next);assertEquals(count+5,next.scope.size());assertEquals(remaining+5,Diagnosis.remainingChecks(next));assertEquals(remaining,Diagnosis.remainingChecks(old));assertSame(q,s.session.question);assertEquals(List.of("123"),s.session.answers);assertEquals(List.of("888"),s.session.conceptHelp.entries);assertEquals(List.of("70"),s.session.conceptHelp.oldTriangleEntries);assertEquals(List.of("axis:0"),s.session.conceptHelp.triangleFoldActions);assertSame(next,s.session.diagnosticRun.plan);
        var cold=LearningSnapshot.capture(s);assertEquals(q.id,cold.session.question.id);assertEquals(List.of("123"),cold.session.answers);assertEquals(List.of("888"),cold.session.conceptHelp.entries);assertSame(Diagnosis.currentPlan(cold),cold.session.diagnosticRun.plan);assertEquals(Diagnosis.UnitStatus.UNCHECKED,next.unitScreens.get(UNIT).status);
    }
    @Test public void helpCorrectionAndOneAnswerKeepTheUnitOnHomeAndDoNotReplaceLaterRecall(){
        for(String id:IDS){
            Learning.State s=student();Random r=new Random(3853);Learning.beginDiagnostic(s,r);var plan=Diagnosis.currentPlan(s);var unit=plan.unitScreens.get(UNIT);KoreanFoundationStudy.flagged(s,unit,id,"unable",LocalDate.of(2026,10,15));
            Learning.beginPractice(s,"practice",Set.of(id),20,false,r);Question q=Learning.ensureQuestion(s,new Generator(r),LocalDate.of(2026,10,15));Review.helpUsed(s,LocalDate.of(2026,10,15));int history=s.history.size(),completed=s.session.completed;
            var help=HelpPlan.forQuestion(q);var d=help.restore(null,q.id);s.session.conceptHelp=d;var guide=KoreanRulesRelations.guide(q);
            while(d.stage<help.size()){String value=guide.frames.get(d.stage).expected;assertTrue(help.step(d.stage).accepts(value));while(d.entries.size()<=d.stage)d.entries.add("");d.entries.set(d.stage,value);d.stage++;}
            assertEquals(history,s.history.size());assertEquals(completed,s.session.completed);assertEquals(List.of(""),s.session.answers);assertFalse(Review.track(s.progress(id)).recent);assertTrue(unit.recoveredSkills.isEmpty());
            String correct=q.choices.stream().filter(x->new Checker().check(q,List.of(),List.of(x)).correct()).findFirst().orElseThrow();s.session.answers.set(0,correct);Learning.finishQuestion(s,false,LocalDate.of(2026,10,15),r);
            assertEquals(Diagnosis.UnitStatus.NEEDS_STUDY,unit.status);assertTrue(Review.track(s.progress(id)).pending);assertTrue(unit.recoveredSkills.isEmpty());assertTrue(Diagnosis.studyUnits(s).stream().anyMatch(x->x.key.equals(UNIT)));
        }
    }
}
