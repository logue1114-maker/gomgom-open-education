package com.gomgomapps.math.core;

import org.junit.Test;
import static org.junit.Assert.*;
import java.time.LocalDate;
import java.util.*;

public class KoreanPolygonIntegrationTest {
    private static final LocalDate DAY=LocalDate.of(2026,10,15);
    private static final String UNIT="KR|2022|4|2|6";
    private Learning.State student(){Learning.State s=new Learning.State();s.profile.grade=6;s.profile.term=2;s.profile.curriculum=2022;s.profile.schoolYear=2026;s.profile.ready=true;s.profile.foundationAscending=true;s.profile.lastLearnedSkill="unit:KR|2022|6|2|5";return s;}
    private HelpPlan.Draft draft(Question q){return HelpPlan.forQuestion(q).restore(null,q.id);}
    private void inspect(Question q,HelpPlan.Draft d){for(int index:KoreanPolygonLesson.required(q,d.stage))assertTrue(KoreanPolygonLesson.visit(q,d,index));}

    @Test public void normalCreateNextCheckerChoicesAndWorkAnswerUsePublicPicturesInBothFormats(){
        Checker checker=new Checker();
        for(String id:KoreanPolygons.IDS)for(boolean mc:List.of(false,true)){
            Generator generator=new Generator(new Random(38810));List<String> recent=new ArrayList<>();
            for(int i=0;i<6;i++){
                Question q=generator.next(id,recent,mc);var g=KoreanPolygons.read(q);assertNotNull(g);assertEquals("choice",q.kind);assertFalse(q.stepSupport);
                assertFalse(recent.contains(q.signature()));recent.add(q.signature());assertEquals(KoreanPolygons.identity(q),q.signature());
                assertEquals(q.choices.size(),new HashSet<>(q.choices).size());assertEquals(1,q.choices.stream().filter(x->checker.check(q,List.of(),List.of(x)).correct()).count());
                q.answers=null;q.expression="999=999";q.studyGuide=new StudyGuide().step("private","","","999");q.kind="complexRoots";
                assertTrue(checker.check(q,List.of(),List.of(g.answerKey())).correct());
                assertEquals(Checker.InputTarget.WORK,checker.check(q,List.of("1=1"),List.of(g.answerKey()),List.of(Checker.StepKind.FULL)).inputTarget);
                assertFalse(checker.checkSteps(q,List.of("1=1"),List.of(Checker.StepKind.FULL)).correct());
                assertTrue(WorkAnswer.writtenAnswers(q,List.of("999=999"),List.of(Checker.StepKind.FULL)).isEmpty());
                assertEquals("",WorkAnswer.writtenAnswer(q,List.of("999=999"),List.of(Checker.StepKind.FULL)));
                assertNotNull(HelpPlan.forQuestion(q));assertFalse(HelpPlan.forQuestion(q).canTransfer());
            }
            assertNotNull(KoreanPolygons.read(generator.create(Catalog.get(id))));
        }
    }
    @Test public void onlyReviewedKorean2022UnitAddsRepresentativesAndExclusionsRemainReversible(){
        Learning.Profile p=student().profile;p.lastLearnedSkill="unit:KR|2022|4|2|5";assertTrue(KoreanFoundationUnits.scope(p).stream().noneMatch(KoreanPolygons::supports));
        p.lastLearnedSkill="unit:"+UNIT;var unit=KoreanFoundationUnits.learned(p).stream().filter(u->u.key().equals(UNIT)).findFirst().orElseThrow();
        assertEquals(KoreanPolygons.IDS,unit.members());assertEquals(KoreanPolygons.IDS,unit.representatives());
        for(String id:KoreanPolygons.IDS){assertTrue(Curriculum.inCurriculum(Catalog.get(id),2022));assertFalse(Curriculum.inCurriculum(Catalog.get(id),2015));}
        p.excluded.add(KoreanPolygons.DIAGONALS);assertFalse(KoreanFoundationUnits.scope(p).contains(KoreanPolygons.DIAGONALS));assertTrue(KoreanFoundationUnits.available(p).stream().anyMatch(u->u.key().equals(UNIT)));
        p.curriculum=2015;assertTrue(KoreanFoundationUnits.available(p).stream().noneMatch(u->u.members().stream().anyMatch(KoreanPolygons::supports)));
        p.curriculum=2022;p.countryCode="NA";p.educationSystem="na-nied-primary-2024-v1";assertTrue(KoreanFoundationUnits.available(p).isEmpty());
        assertNotNull(ElementaryBasics.create(Catalog.get("el_shape_sides"),new Random(1)));assertFalse(Curriculum.inCurriculum(Catalog.get(PolygonConstruction.ID),2022));
    }
    @Test public void eachLessonRequiresRealInspectionAndCorrectRelationsBeforeItsSeparateFinalAnswer(){
        for(String id:KoreanPolygons.IDS){
            Question q=KoreanPolygons.create(id,new Random(38811));StudyGuide guide=KoreanPolygons.guide(q);HelpPlan.Draft d=draft(q);
            assertNotNull(KoreanPolygonLesson.restore(q,d));String[] published=q.answers.clone();
            while(d.stage<guide.frames.size()){
                int stage=d.stage;var frame=guide.frames.get(stage);
                if(KoreanPolygonLesson.isAction(q,stage)){
                    assertFalse(KoreanPolygonLesson.advance(q,d,"ok"));assertEquals(stage,d.stage);
                    inspect(q,d);assertTrue(KoreanPolygonLesson.ready(q,d));
                }else{
                    String wrong=frame.options.keySet().stream().filter(x->!x.equals(frame.expected)).findFirst().orElseThrow();
                    assertFalse(KoreanPolygonLesson.advance(q,d,wrong));assertEquals(stage,d.stage);assertEquals(wrong,d.entries.get(stage));
                }
                assertTrue(KoreanPolygonLesson.advance(q,d,frame.expected));assertEquals(stage+1,d.stage);
            }
            assertTrue(KoreanPolygonLesson.state(q,d).complete());assertArrayEquals(published,q.answers);assertFalse(HelpPlan.forQuestion(q).canTransfer());
            assertFalse(KoreanPolygonLesson.advance(q,d,"ok"));
        }
    }
    @Test public void observationResetAndColdCopiesRetainWrongInputAndOnlyActuallyVisitedTargets(){
        Learning.State state=student();Random random=new Random(38812);Learning.beginPractice(state,"practice",List.of(KoreanPolygons.NAME),20,false,random);
        Question q=Learning.ensureQuestion(state,new Generator(random),DAY);HelpPlan.Draft d=draft(q);state.session.conceptHelp=d;
        assertNotNull(KoreanPolygonLesson.restore(q,d));assertTrue(KoreanPolygonLesson.visit(q,d,0));assertFalse(KoreanPolygonLesson.visit(q,d,0));
        assertFalse(KoreanPolygonLesson.advance(q,d,"ok"));assertEquals("ok",d.entries.get(0));
        assertTrue(KoreanPolygonLesson.resetInspection(q,d));assertTrue(KoreanPolygonLesson.marked(q,d).isEmpty());assertEquals("ok",d.entries.get(0));
        inspect(q,d);assertTrue(KoreanPolygonLesson.advance(q,d,"ok"));assertTrue(KoreanPolygonLesson.visit(q,d,1));
        Learning.State cold=LearningSnapshot.capture(state);HelpPlan.Draft restored=cold.session.conceptHelp;
        assertNotSame(d.polygonActions,restored.polygonActions);assertEquals(KoreanPolygonLesson.state(q,d),KoreanPolygonLesson.restore(cold.session.question,restored));
        assertEquals(List.of(1),KoreanPolygonLesson.marked(q,restored));assertTrue(KoreanPolygonLesson.visit(q,restored,0));assertEquals(List.of(1),KoreanPolygonLesson.marked(q,d));
        assertEquals(List.of(""),cold.session.answers);assertEquals(0,cold.session.completed);assertTrue(cold.history.isEmpty());
        HelpPlan.Draft invalid=d.copy();invalid.polygonActions.add("advance:ok");assertNull(KoreanPolygonLesson.state(q,invalid));
        invalid=d.copy();invalid.questionId="other";assertNull(KoreanPolygonLesson.restore(q,invalid));
        invalid=d.copy();invalid.teachingVersion="old";assertNull(KoreanPolygonLesson.restore(q,invalid));
        invalid=d.copy();invalid.polygonActions=new ArrayList<>(List.of("advance:ok"));invalid.entries=new ArrayList<>(List.of("888"));
        assertNotNull(KoreanPolygonLesson.restore(q,invalid));assertEquals(List.of("888"),invalid.entries);assertEquals(0,invalid.stage);
    }
    @Test public void diagonalToolRejectsSelfAndNeighborsInsteadOfAcceptingACompletionToken(){
        Question q=KoreanPolygons.create(KoreanPolygons.DIAGONALS,new Random(38813));var g=KoreanPolygons.read(q);HelpPlan.Draft d=draft(q);KoreanPolygonLesson.restore(q,d);
        int n=g.vertices().size();assertFalse(KoreanPolygonLesson.visit(q,d,g.origin()));assertFalse(KoreanPolygonLesson.visit(q,d,(g.origin()+1)%n));assertFalse(KoreanPolygonLesson.visit(q,d,(g.origin()+n-1)%n));
        assertFalse(KoreanPolygonLesson.advance(q,d,"ok"));assertTrue(d.polygonActions.isEmpty());
        for(int end:g.diagonalEnds())assertTrue(KoreanPolygonLesson.visit(q,d,end));assertEquals(g.diagonalEnds(),KoreanPolygonLesson.marked(q,d));
        assertTrue(KoreanPolygonLesson.advance(q,d,"ok"));assertEquals(1,d.stage);
        assertEquals("selfAndNeighbors",KoreanPolygons.guide(q).frames.get(1).expected);
    }
    @Test public void formerlyEmptyUnitUpgradeAddsFourChecksWithoutRewritingAnActiveRunOrSavedHelp(){
        Learning.State s=student();Random r=new Random(38814);Learning.beginDiagnostic(s,r);Question q=Learning.ensureQuestion(s,new Generator(r),DAY);
        Diagnosis.Plan old=Diagnosis.currentPlan(s);s.diagnostics.remove(old.key);var oldUnit=old.unitScreens.get(UNIT);
        old.scope.removeIf(KoreanPolygons::supports);old.placements.keySet().removeIf(KoreanPolygons::supports);oldUnit.members.clear();oldUnit.representatives.clear();oldUnit.label="이전 미지원 단원";
        Set<String> removed=new HashSet<>();for(var x:old.checks.entrySet())if(KoreanPolygons.supports(x.getValue().skillId))removed.add(x.getKey());
        old.checks.keySet().removeAll(removed);old.pending.removeAll(removed);old.untested.removeAll(removed);
        List<String> scope=new ArrayList<>(old.scope);Collections.sort(scope);
        old.key=Curriculum.MAPPING_VERSION+"|"+old.schoolYear+"|"+old.curriculum+"|"+old.actualGrade+"|"+old.term+"||"+String.join(",",scope)+"|foundation-unit-screen-v2|units-"+KoreanFoundationUnits.MAPPING_VERSION+"|"+old.lastLearnedSkill;
        for(var unit:old.unitScreens.values())unit.planKey=old.key;s.diagnostics.put(old.key,old);
        int remaining=Diagnosis.remainingChecks(old),count=old.scope.size();s.session.answers.set(0,"123");s.session.conceptHelp=new HelpPlan.Draft();s.session.conceptHelp.questionId=q.id;s.session.conceptHelp.entries.add("888");s.session.conceptHelp.planeVisited.add(1);s.session.conceptHelp.planeVisitStage=2;
        Diagnosis.migrate(s);Diagnosis.Plan current=Diagnosis.currentPlan(s);assertNotSame(old,current);assertEquals(count+4,current.scope.size());assertEquals(remaining+4,Diagnosis.remainingChecks(current));assertEquals(remaining,Diagnosis.remainingChecks(old));
        assertEquals(KoreanPolygons.IDS,current.unitScreens.get(UNIT).representatives);assertTrue(oldUnit.representatives.isEmpty());assertTrue(s.diagnostics.containsValue(old));
        assertSame(q,s.session.question);assertEquals(List.of("123"),s.session.answers);assertEquals(List.of("888"),s.session.conceptHelp.entries);assertEquals(List.of(1),s.session.conceptHelp.planeVisited);assertEquals(2,s.session.conceptHelp.planeVisitStage);assertSame(current,s.session.diagnosticRun.plan);
        Diagnosis.migrate(s);assertSame(current,Diagnosis.currentPlan(s));Learning.State cold=LearningSnapshot.capture(s);
        assertEquals(q.id,cold.session.question.id);assertEquals(List.of("123"),cold.session.answers);assertEquals(List.of("888"),cold.session.conceptHelp.entries);assertSame(Diagnosis.currentPlan(cold),cold.session.diagnosticRun.plan);
    }
}
