package com.gomgomapps.math.core;

import org.junit.Test;
import static org.junit.Assert.*;
import java.time.LocalDate;
import java.util.*;

public class KoreanSpatialIntegrationTest {
    private static final LocalDate DAY=LocalDate.of(2026,10,15);
    private static HelpPlan.Draft draft(Question q){HelpPlan.Draft d=HelpPlan.forQuestion(q).restore(null,q.id);assertNotNull(KoreanSpatialLesson.restore(q,d));return d;}
    private static void countAll(Question q,HelpPlan.Draft d){
        var g=KoreanSpatialSolids.read(q);for(int cell=0;cell<g.heights().size();cell++){
            assertTrue(KoreanSpatialLesson.select(q,d,cell));for(int z=0;z<g.height(cell);z++)assertTrue(KoreanSpatialLesson.cube(q,d,z));assertTrue(KoreanSpatialLesson.confirm(q,d,""+g.height(cell)));
        }
    }
    private static void compareLines(Question q,HelpPlan.Draft d){
        var g=KoreanSpatialSolids.read(q);for(int line=0;line<KoreanSpatialLesson.lineCount(g);line++){
            assertTrue(KoreanSpatialLesson.select(q,d,line));int value=0;for(int cell:KoreanSpatialLesson.lineCells(g,line)){
                assertTrue(KoreanSpatialLesson.visit(q,d,cell));value=KoreanSpatialSolids.TOP.equals(g.kind())?value+(g.height(cell)>0?1:0):Math.max(value,g.height(cell));
            }
            assertTrue(KoreanSpatialLesson.confirm(q,d,""+value));
        }
    }
    @Test public void normalGeneratorAndCheckerRetainPublicAuthorityInEitherQuestionFormat(){
        Checker checker=new Checker();for(String id:KoreanSpatialSolids.IDS)for(boolean mc:List.of(false,true)){
            Generator gen=new Generator(new Random(390100));List<String> seen=new ArrayList<>();
            for(int i=0;i<5;i++){
                Question q=gen.next(id,seen,mc);var g=KoreanSpatialSolids.read(q);assertNotNull(g);assertFalse(seen.contains(q.signature()));seen.add(q.signature());
                String answer=KoreanSpatialSolids.COUNT.equals(id)?""+g.total():KoreanSpatialSolidsTest.correct(q);
                assertEquals(KoreanSpatialSolids.COUNT.equals(id)?"number":"choice",q.kind);
                if(KoreanSpatialSolids.COUNT.equals(id))assertTrue(q.choices.isEmpty());else assertEquals(3,q.choices.size());
                q.answers=null;q.expression="999=999";q.kind="complexRoots";q.studyGuide=new StudyGuide().step("private","","","999");
                assertTrue(checker.check(q,List.of(),List.of(answer)).correct());assertFalse(HelpPlan.forQuestion(q).canTransfer());
                assertEquals(Checker.InputTarget.WORK,checker.check(q,List.of("1=1"),List.of(answer),List.of(Checker.StepKind.FULL)).inputTarget);
                assertFalse(checker.checkSteps(q,List.of("1=1"),List.of(Checker.StepKind.FULL)).correct());assertTrue(WorkAnswer.writtenAnswers(q,List.of("1=1"),List.of(Checker.StepKind.FULL)).isEmpty());
            }
            assertNotNull(KoreanSpatialSolids.read(gen.create(Catalog.get(id))));
        }
    }
    @Test public void countingRequiresEveryColumnAndBottomUpCubesIncludingExplicitZero(){
        Question q=KoreanSpatialSolidsTest.height(KoreanSpatialSolids.COUNT,2,2,List.of(2,0,1,3));HelpPlan.Draft d=draft(q);String[] cached=q.answers.clone();
        assertFalse(KoreanSpatialLesson.advance(q,d,"ok"));assertTrue(KoreanSpatialLesson.select(q,d,0));assertFalse(KoreanSpatialLesson.cube(q,d,1));
        assertTrue(KoreanSpatialLesson.cube(q,d,0));assertFalse(KoreanSpatialLesson.cube(q,d,0));assertFalse(KoreanSpatialLesson.confirm(q,d,"2"));
        assertFalse(KoreanSpatialLesson.confirm(q,d,"999"));assertEquals("999",KoreanSpatialLesson.state(q,d).entry());assertNotNull(KoreanSpatialLesson.confirmFeedback(q,d));
        assertTrue(KoreanSpatialLesson.cube(q,d,1));assertTrue(KoreanSpatialLesson.confirm(q,d,"02"));assertEquals("02",KoreanSpatialSolids.references(q,d).get("바닥칸 1 = "));
        assertTrue(KoreanSpatialLesson.select(q,d,1));assertFalse(KoreanSpatialLesson.cube(q,d,0));assertTrue(KoreanSpatialLesson.confirm(q,d,"0"));
        for(int cell=2;cell<4;cell++){assertTrue(KoreanSpatialLesson.select(q,d,cell));for(int z=0;z<(cell==2?1:3);z++)assertTrue(KoreanSpatialLesson.cube(q,d,z));assertTrue(KoreanSpatialLesson.confirm(q,d,cell==2?"1":"3"));}
        assertTrue(KoreanSpatialLesson.ready(q,d));assertTrue(KoreanSpatialLesson.advance(q,d,"ok"));assertFalse(KoreanSpatialLesson.advance(q,d,"7"));assertEquals("7",d.entries.get(1));
        assertTrue(KoreanSpatialLesson.advance(q,d,"6"));assertTrue(KoreanSpatialLesson.state(q,d).complete());assertArrayEquals(cached,q.answers);assertTrue(KoreanSpatialSolids.references(q,d).isEmpty());
    }
    @Test public void directionalLessonsCompareOcclusionThenRequireTheExactStudentDrawing(){
        for(String id:List.of(KoreanSpatialSolids.TOP,KoreanSpatialSolids.FRONT,KoreanSpatialSolids.SIDE)){
            Question q=KoreanSpatialSolidsTest.height(id,2,2,List.of(1,3,2,1));HelpPlan.Draft d=draft(q);assertFalse(KoreanSpatialLesson.advance(q,d,"ok"));
            compareLines(q,d);assertTrue(KoreanSpatialLesson.advance(q,d,"ok"));assertFalse(KoreanSpatialLesson.advance(q,d,"ok"));
            var bits=KoreanSpatialSolids.target(KoreanSpatialSolids.read(q)).bits();for(int i=0;i<bits.size();i++)if(bits.get(i)==1)assertTrue(KoreanSpatialLesson.toggle(q,d,i));
            assertTrue(KoreanSpatialLesson.ready(q,d));assertTrue(KoreanSpatialLesson.advance(q,d,"ok"));
            String wrong=q.choices.stream().filter(k->!KoreanSpatialSolids.check(q,List.of(k)).correct()).findFirst().orElseThrow();assertFalse(KoreanSpatialLesson.advance(q,d,wrong));assertEquals(wrong,d.entries.get(2));
            assertTrue(KoreanSpatialLesson.advance(q,d,KoreanSpatialSolidsTest.correct(q)));assertTrue(KoreanSpatialLesson.state(q,d).complete());
        }
    }
    @Test public void reconstructionChecksEveryPublicViewOfEveryCandidateBeforeTheFinalChoice(){
        Question q=KoreanSpatialSolidsTest.projections(2,2,List.of(2,1,1,2));HelpPlan.Draft d=draft(q);
        assertFalse(KoreanSpatialLesson.advance(q,d,"ok"));for(int i=0;i<3;i++)assertTrue(KoreanSpatialLesson.visit(q,d,i));assertFalse(KoreanSpatialLesson.visit(q,d,1));assertTrue(KoreanSpatialLesson.advance(q,d,"ok"));
        for(int c=0;c<3;c++){
            assertTrue(KoreanSpatialLesson.select(q,d,c));String answer=KoreanSpatialSolids.check(q,List.of(q.choices.get(c))).correct()?"fits":"notFits";
            assertFalse(KoreanSpatialLesson.confirm(q,d,answer));assertFalse(KoreanSpatialLesson.confirm(q,d,answer.equals("fits")?"notFits":"fits"));assertFalse(KoreanSpatialLesson.state(q,d).entry().isEmpty());
            for(int v=0;v<3;v++){assertNotNull(KoreanSpatialSolids.candidateProjection(q,q.choices.get(c),v));assertTrue(KoreanSpatialLesson.visit(q,d,v));}
            assertTrue(KoreanSpatialLesson.confirm(q,d,answer));
        }
        assertTrue(KoreanSpatialLesson.advance(q,d,"ok"));assertTrue(KoreanSpatialLesson.advance(q,d,KoreanSpatialSolidsTest.correct(q)));assertTrue(KoreanSpatialLesson.state(q,d).complete());
    }
    @Test public void coldSnapshotsAndInvalidTracesPreserveActualWrongInputsWithoutBodyOrReviewEffects(){
        Learning.State s=new Learning.State();s.profile.grade=6;s.profile.term=2;s.profile.curriculum=2022;s.profile.ready=true;s.profile.schoolYear=2026;
        Learning.beginPractice(s,"practice",List.of(KoreanSpatialSolids.COUNT),20,false,new Random(390103));Question q=Learning.ensureQuestion(s,new Generator(new Random(390104)),DAY);
        HelpPlan.Draft d=draft(q);s.session.conceptHelp=d;assertTrue(KoreanSpatialLesson.select(q,d,0));assertFalse(KoreanSpatialLesson.confirm(q,d,"999"));d.spatialPendingInput="0|raw888";
        Learning.State cold=LearningSnapshot.capture(s);HelpPlan.Draft copy=cold.session.conceptHelp;assertEquals(KoreanSpatialLesson.state(q,d),KoreanSpatialLesson.restore(cold.session.question,copy));
        assertNotSame(d.spatialActions,copy.spatialActions);assertEquals("0|raw888",copy.spatialPendingInput);assertEquals("999",KoreanSpatialLesson.state(q,copy).entry());
        assertEquals(List.of(""),cold.session.answers);assertEquals(0,cold.session.completed);assertTrue(cold.history.isEmpty());assertEquals(s.reviewSequence,cold.reviewSequence);
        copy.spatialActions.add("advance:ok");assertNull(KoreanSpatialLesson.state(q,copy));copy.entries.set(0,"888");assertNotNull(KoreanSpatialLesson.restore(q,copy));assertEquals("888",copy.entries.get(0));assertEquals(0,copy.stage);assertNull(copy.spatialPendingInput);
        copy=d.copy();copy.questionId="other";assertNull(KoreanSpatialLesson.restore(q,copy));copy=d.copy();copy.teachingVersion="old";assertNull(KoreanSpatialLesson.restore(q,copy));
        copy=d.copy();copy.spatialActions=new ArrayList<>(Collections.nCopies(129,"try:999"));assertNull(KoreanSpatialLesson.state(q,copy));assertEquals("999",KoreanSpatialLesson.state(q,d).entry());
    }
    @Test public void repeatedMistakesAndCancelledDrawingTogglesDoNotExhaustTheTraceOrInventProof(){
        Question q=KoreanSpatialSolidsTest.height(KoreanSpatialSolids.COUNT,3,3,Collections.nCopies(9,3));HelpPlan.Draft d=draft(q);
        assertTrue(KoreanSpatialLesson.select(q,d,0));for(int i=0;i<300;i++)assertFalse(KoreanSpatialLesson.confirm(q,d,"999"));assertEquals("999",KoreanSpatialLesson.state(q,d).entry());
        assertFalse(KoreanSpatialLesson.advance(q,d,"ok"));for(int z=0;z<3;z++)assertTrue(KoreanSpatialLesson.cube(q,d,z));assertTrue(KoreanSpatialLesson.confirm(q,d,"3"));
        for(int i=1;i<9;i++){assertTrue(KoreanSpatialLesson.select(q,d,i));for(int z=0;z<3;z++)assertTrue(KoreanSpatialLesson.cube(q,d,z));assertTrue(KoreanSpatialLesson.confirm(q,d,"3"));}
        assertTrue(KoreanSpatialLesson.advance(q,d,"ok"));assertTrue(KoreanSpatialLesson.advance(q,d,"27"));assertTrue(KoreanSpatialLesson.state(q,d).complete());assertTrue(d.spatialActions.size()<128);
        q=KoreanSpatialSolidsTest.height(KoreanSpatialSolids.FRONT,2,2,List.of(1,3,2,1));d=draft(q);compareLines(q,d);assertTrue(KoreanSpatialLesson.advance(q,d,"ok"));
        for(int i=0;i<300;i++)assertTrue(KoreanSpatialLesson.toggle(q,d,0));assertTrue(KoreanSpatialLesson.state(q,d).drawn().isEmpty());assertFalse(KoreanSpatialLesson.advance(q,d,"ok"));
        var target=KoreanSpatialSolids.target(KoreanSpatialSolids.read(q));for(int i=0;i<target.bits().size();i++)if(target.bits().get(i)==1)assertTrue(KoreanSpatialLesson.toggle(q,d,i));
        assertTrue(KoreanSpatialLesson.advance(q,d,"ok"));assertTrue(KoreanSpatialLesson.advance(q,d,KoreanSpatialSolidsTest.correct(q)));assertTrue(KoreanSpatialLesson.state(q,d).complete());
    }
    @Test public void reviewedKoreanPlacementDoesNotReplaceLegacyOrForeignRepresentatives(){
        Learning.Profile p=new Learning.Profile();p.grade=6;p.term=2;p.curriculum=2022;p.schoolYear=2026;p.foundationAscending=true;p.lastLearnedSkill="unit:KR|2022|6|2|2";
        var unit=KoreanFoundationUnits.learned(p).stream().filter(u->u.key().equals("KR|2022|6|2|2")).findFirst().orElseThrow();assertEquals(KoreanSpatialSolids.IDS,unit.representatives());
        for(String id:KoreanSpatialSolids.IDS){assertTrue(Curriculum.inCurriculum(Catalog.get(id),2022));assertFalse(Curriculum.inCurriculum(Catalog.get(id),2015));}
        p.excluded.add(KoreanSpatialSolids.FRONT);assertFalse(KoreanFoundationUnits.scope(p).contains(KoreanSpatialSolids.FRONT));assertTrue(KoreanFoundationUnits.available(p).stream().anyMatch(u->u.key().equals("KR|2022|6|2|2")));
        p.curriculum=2015;p.lastLearnedSkill="unit:KR|2015|6|2|3";assertTrue(KoreanFoundationUnits.scope(p).stream().noneMatch(KoreanSpatialSolids::supports));
        p.curriculum=2022;p.countryCode="NA";p.educationSystem="na-nied-primary-2024-v1";assertTrue(KoreanFoundationUnits.available(p).isEmpty());
        Question old=new Generator(new Random(390105)).next("earlyStacks",List.of(),false);assertEquals("earlyStacks",old.skillId);assertNotNull(old.diagram);assertFalse(KoreanSpatialSolids.supports(old.skillId));assertNull(KoreanSpatialSolids.guide(old));
    }
}
