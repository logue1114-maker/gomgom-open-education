package com.gomgomapps.math.core;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;
import java.time.LocalDate;

public class KoreanTriangleFoldIntegrationTest {
    private Question question(){return new Generator(new Random(383)).create(Catalog.get(KoreanTriangleClassification.PROPERTIES));}
    private HelpPlan.Draft gate(Question q){var d=HelpPlan.forQuestion(q).restore(null,q.id);assertTrue(KoreanTriangleLesson.advance(q,d,KoreanTriangleClassification.guide(q).frames.get(0).expected));return d;}
    private void perform(Question q,HelpPlan.Draft d){for(int axis:KoreanTriangleFold.requiredAxes(q)){assertTrue(KoreanTriangleLesson.foldAction(q,d,"axis",axis));for(int i=0;i<4;i++)assertTrue(KoreanTriangleLesson.foldAction(q,d,"fold",0));var s=KoreanTriangleLesson.foldState(q,d);assertTrue(s.overlap());assertTrue(KoreanTriangleLesson.foldAction(q,d,"angle",s.movingIndex()));assertTrue(KoreanTriangleLesson.foldAction(q,d,"angle",s.fixedIndex()));}}
    @Test public void actionTokenAndOneObservedAngleDoNotReplaceActuallyFoldingTheOriginalTriangle(){
        Question q=question();var d=gate(q);assertFalse(KoreanTriangleLesson.advance(q,d,"ok"));assertEquals(1,d.stage);assertTrue(KoreanTriangleLesson.foldAction(q,d,"axis",0));for(int i=0;i<4;i++)assertTrue(KoreanTriangleLesson.foldAction(q,d,"fold",0));var state=KoreanTriangleLesson.foldState(q,d);assertTrue(KoreanTriangleLesson.foldAction(q,d,"angle",state.movingIndex()));assertFalse(KoreanTriangleLesson.advance(q,d,"ok"));assertTrue(KoreanTriangleLesson.foldAction(q,d,"angle",state.fixedIndex()));
        for(int axis:KoreanTriangleFold.requiredAxes(q))if(!KoreanTriangleLesson.foldState(q,d).checkedAxes().contains(axis)){assertTrue(KoreanTriangleLesson.foldAction(q,d,"axis",axis));for(int i=0;i<4;i++)assertTrue(KoreanTriangleLesson.foldAction(q,d,"fold",0));state=KoreanTriangleLesson.foldState(q,d);assertTrue(KoreanTriangleLesson.foldAction(q,d,"angle",state.movingIndex()));assertTrue(KoreanTriangleLesson.foldAction(q,d,"angle",state.fixedIndex()));}
        assertTrue(KoreanTriangleLesson.advance(q,d,"ok"));assertEquals(2,d.stage);assertFalse(KoreanTriangleLesson.foldAction(q,d,"fold",0));assertFalse(HelpPlan.forQuestion(q).canTransfer());
        d.triangleFoldActions.clear();KoreanTriangleLesson.restore(q,d);assertEquals(1,d.stage);assertEquals("",d.entries.get(1));
    }
    @Test public void partialFoldCopiesAndColdRestoreKeepTheBodyAndLearningEvidenceUnchanged(){
        Learning.State state=new Learning.State();Random random=new Random(3832);Learning.beginPractice(state,"practice",Set.of(KoreanTriangleClassification.PROPERTIES),20,false,random);Question q=Learning.ensureQuestion(state,new Generator(random),LocalDate.of(2026,10,15));state.session.answers.set(0,"999");var d=gate(q);state.session.conceptHelp=d;
        assertTrue(KoreanTriangleLesson.foldAction(q,d,"axis",0));assertTrue(KoreanTriangleLesson.foldAction(q,d,"fold",0));assertTrue(KoreanTriangleLesson.foldAction(q,d,"fold",0));assertFalse(KoreanTriangleLesson.advance(q,d,"ok"));
        var copy=d.copy();copy.triangleFoldActions.clear();assertEquals(3,d.triangleFoldActions.size());Learning.State cold=LearningSnapshot.capture(state);var restored=HelpPlan.forQuestion(cold.session.question).restore(cold.session.conceptHelp,q.id);KoreanTriangleLesson.restore(cold.session.question,restored);assertEquals(2,KoreanTriangleLesson.foldState(cold.session.question,restored).step());assertEquals("ok",restored.entries.get(1));assertEquals(List.of("999"),cold.session.answers);assertEquals(0,cold.session.completed);assertTrue(cold.history.isEmpty());assertEquals(q.signature(),cold.session.question.signature());
        while(KoreanTriangleLesson.foldState(q,restored).step()>0)assertTrue(KoreanTriangleLesson.foldAction(q,restored,"unfold",0));assertTrue(KoreanTriangleLesson.foldAction(q,restored,"reset",0));assertEquals("ok",restored.entries.get(1));perform(q,restored);assertTrue(KoreanTriangleLesson.advance(q,restored,"ok"));var h=KoreanTriangleClassification.guide(q);while(restored.stage<h.frames.size())assertTrue(KoreanTriangleLesson.advance(q,restored,h.frames.get(restored.stage).expected));assertEquals(List.of("999"),cold.session.answers);assertTrue(cold.history.isEmpty());assertEquals(0,cold.session.completed);
    }
    @Test public void oldFourFramePropertiesEntriesRemainReadableWithoutPretendingTheNewFoldWasDone(){
        Question q=question();var old=new HelpPlan.Draft();old.questionId=q.id;old.teachingVersion=KoreanTriangleClassification.VERSION;old.stage=2;old.entries=new ArrayList<>(List.of("abAc","baseEqual","999"));old.movementActions.add("cw");var d=HelpPlan.forQuestion(q).restore(old,q.id);
        assertEquals(0,d.stage);assertEquals(KoreanTriangleClassification.FOLD_VERSION,d.teachingVersion);assertEquals(KoreanTriangleFold.VERSION,d.teachingVersion);assertEquals(List.of("abAc","baseEqual","999"),d.oldTriangleEntries);assertEquals(2,d.oldTriangleStage);assertEquals(KoreanTriangleClassification.VERSION,d.oldTriangleTeachingVersion);assertTrue(d.triangleFoldActions.isEmpty());assertTrue(d.movementActions.isEmpty());assertFalse(KoreanTriangleFold.complete(q,KoreanTriangleLesson.foldState(q,d)));assertEquals(4,KoreanTriangleClassification.previousPropertiesGuide(q).frames.size());
        var copy=d.copy();copy.oldTriangleEntries.set(2,"30");assertEquals("999",d.oldTriangleEntries.get(2));assertSame(d,HelpPlan.forQuestion(q).restore(d,q.id));assertEquals("999",d.oldTriangleEntries.get(2));
    }
    @Test public void unchangedSideAndCornerLessonsKeepTheirExistingPartialVisitsAndVersion(){
        for(String id:List.of(KoreanTriangleClassification.SIDES,KoreanTriangleClassification.ANGLES)){
            Question q=new Generator(new Random(3834)).create(Catalog.get(id));var plan=HelpPlan.forQuestion(q);var d=plan.restore(null,q.id);if(id.equals(KoreanTriangleClassification.ANGLES))assertTrue(KoreanTriangleLesson.advance(q,d,"3"));assertTrue(KoreanTriangleLesson.visit(q,d,2));d.entries.set(d.stage,"wrong");int stage=d.stage;var restored=plan.restore(d,q.id);KoreanTriangleLesson.restore(q,restored);assertEquals(KoreanTriangleClassification.VERSION,restored.teachingVersion);assertEquals(stage,restored.stage);assertEquals(List.of(2),restored.triangleVisited);assertEquals("wrong",restored.entries.get(stage));assertNull(restored.oldTriangleTeachingVersion);
        }
    }
}
