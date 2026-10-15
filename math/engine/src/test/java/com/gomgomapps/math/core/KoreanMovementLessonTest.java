package com.gomgomapps.math.core;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;
import java.io.*;

public class KoreanMovementLessonTest {
    private Question question(String id){return new Generator(new Random(3814)).create(Catalog.get(id));}
    private HelpPlan.Draft action(Question q){HelpPlan p=HelpPlan.forQuestion(q);var d=p.restore(null,q.id);var guide=KoreanPlaneMovement.guide(q);for(int i=0;i<3;i++){d.entries.set(i,guide.frames.get(i).expected);d.entries.add("");}d.stage=3;KoreanMovementLesson.restore(q,d);return d;}
    private HelpPlan.Draft cold(HelpPlan.Draft d)throws Exception{ByteArrayOutputStream bytes=new ByteArrayOutputStream();try(ObjectOutputStream o=new ObjectOutputStream(bytes)){o.writeObject(d);}try(ObjectInputStream i=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))){return (HelpPlan.Draft)i.readObject();}}
    private void solve(Question q,HelpPlan.Draft d){var g=KoreanPlaneMovement.read(q);int times="krPlaneTurn".equals(q.skillId)?g.amount()/90:g.amount();for(int i=0;i<times;i++)assertTrue(KoreanMovementLesson.move(q,d,g.direction()));assertTrue(KoreanMovementLesson.complete(q,d));}
    @Test public void explicitActionsAreRequiredAndNoParentAnswerIsTransferred(){for(String id:List.of("krPlaneSlide","krPlaneFlip","krPlaneTurn")){Question q=question(id);var d=action(q);assertFalse(KoreanMovementLesson.advance(q,d));assertFalse(KoreanMovementLesson.move(q,d,"notAction"));assertTrue(d.movementActions.isEmpty());solve(q,d);assertTrue(KoreanMovementLesson.advance(q,d));assertEquals(4,d.stage);assertEquals("ok",d.entries.get(3));assertFalse(HelpPlan.forQuestion(q).canTransfer());assertFalse(KoreanMovementLesson.move(q,d,KoreanPlaneMovement.read(q).direction()));}}
    @Test public void partialTraceWrongChoiceAndResetSurviveColdWithoutInventingProgress()throws Exception{
        Question q=question("krPlaneTurn");var d=action(q);assertTrue(KoreanMovementLesson.move(q,d,"ccw"));d.entries.set(3,"wrong");d=cold(d);assertEquals(List.of("ccw"),d.movementActions);assertEquals("wrong",d.entries.get(3));var before=KoreanMovementLesson.placement(q,d);assertNotNull(before);var copy=d.copy();copy.movementActions.clear();assertEquals(List.of("ccw"),d.movementActions);
        KoreanMovementLesson.reset(q,d);assertTrue(d.movementActions.isEmpty());assertEquals("wrong",d.entries.get(3));solve(q,d);assertTrue(KoreanMovementLesson.advance(q,d));d=cold(d);d=HelpPlan.forQuestion(q).restore(d,q.id);KoreanMovementLesson.restore(q,d);assertEquals(4,d.stage);
        Learning.State s=new Learning.State();s.session=new Learning.Session();s.session.question=q;s.session.conceptHelp=d;s.session.answers.add("");Learning.State snap=LearningSnapshot.capture(s);assertEquals(d.movementActions,snap.session.conceptHelp.movementActions);assertNotSame(d.movementActions,snap.session.conceptHelp.movementActions);assertEquals(List.of(""),snap.session.answers);assertEquals(0,snap.session.completed);assertTrue(snap.history.isEmpty());
    }
    @Test public void forgedSavedCompletionOrWrongKindTraceCannotPassTheActionGate(){
        Question q=question("krPlaneFlip");var d=action(q);d.entries.set(3,"ok");d.entries.add("");d.stage=4;KoreanMovementLesson.restore(q,d);assertEquals(3,d.stage);assertEquals("",d.entries.get(3));d.movementActions.add("left");KoreanMovementLesson.restore(q,d);assertTrue(d.movementActions.isEmpty());assertFalse(KoreanMovementLesson.complete(q,d));d.questionId="other";assertFalse(KoreanMovementLesson.move(q,d,"horizontal"));assertNull(KoreanMovementLesson.placement(q,d));
    }
    @Test public void teachingVersionResetDoesNotCarryActionsIntoAnotherLesson(){Question q=question("krPlaneSlide");var d=action(q);solve(q,d);d.teachingVersion="old-version";d=HelpPlan.forQuestion(q).restore(d,q.id);assertEquals(0,d.stage);assertTrue(d.movementActions.isEmpty());}
}
