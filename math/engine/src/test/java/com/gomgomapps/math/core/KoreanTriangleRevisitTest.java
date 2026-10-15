package com.gomgomapps.math.core;

import java.util.*;
import java.time.LocalDate;
import java.io.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class KoreanTriangleRevisitTest {
    private static final List<String> KINDS=List.of(KoreanTriangleClassification.SIDES,KoreanTriangleClassification.ANGLES,KoreanTriangleClassification.PROPERTIES);
    private byte[] progress(Learning.State s)throws Exception{ByteArrayOutputStream bytes=new ByteArrayOutputStream();try(ObjectOutputStream out=new ObjectOutputStream(bytes)){out.writeObject(s.progress);}return bytes.toByteArray();}
    private int gate(Question q){return q.skillId.equals(KoreanTriangleClassification.PROPERTIES)?1:KoreanTriangleLesson.inspectionStage(q);}
    private void compare(Question q,HelpPlan.Draft d){
        if(q.skillId.equals(KoreanTriangleClassification.PROPERTIES)){
            for(int axis:KoreanTriangleFold.requiredAxes(q)){
                assertTrue(KoreanTriangleLesson.foldAction(q,d,"axis",axis));
                for(int n=0;n<4;n++)assertTrue(KoreanTriangleLesson.foldAction(q,d,"fold",0));
                var s=KoreanTriangleLesson.foldState(q,d);
                assertTrue(KoreanTriangleLesson.foldAction(q,d,"angle",s.movingIndex()));
                assertTrue(KoreanTriangleLesson.foldAction(q,d,"angle",s.fixedIndex()));
            }
        }else for(int i=0;i<3;i++)assertTrue(KoreanTriangleLesson.visit(q,d,i));
    }
    private void partial(Question q,HelpPlan.Draft d){
        if(q.skillId.equals(KoreanTriangleClassification.PROPERTIES)){
            assertTrue(KoreanTriangleLesson.foldAction(q,d,"axis",0));assertTrue(KoreanTriangleLesson.foldAction(q,d,"fold",0));
        }else assertTrue(KoreanTriangleLesson.visit(q,d,0));
    }
    private Learning.State wrong(String id){
        Learning.State s=new Learning.State();Random r=new Random(384);
        Learning.beginPractice(s,"practice",Set.of(id),20,false,r);
        Question q=Learning.ensureQuestion(s,new Generator(r),LocalDate.of(2026,10,15));
        var plan=HelpPlan.forQuestion(q);var d=plan.restore(null,q.id);s.session.conceptHelp=d;s.session.answers.set(0,"original-body-entry");
        var guide=KoreanTriangleClassification.guide(q);
        while(d.stage<plan.size()-1){if(d.stage==gate(q))compare(q,d);assertTrue(KoreanTriangleLesson.advance(q,d,guide.frames.get(d.stage).expected));}
        String bad=guide.frames.get(d.stage).options.keySet().stream().filter(x->!plan.step(d.stage).accepts(x)).findFirst().orElseThrow();
        assertFalse(KoreanTriangleLesson.advance(q,d,bad));return s;
    }
    @Test public void partialRecomparisonAndCancelSurviveColdWithoutLosingTheOriginalWrongStep()throws Exception{
        for(String id:KINDS){
            var s=wrong(id);Question q=s.session.question;var d=s.session.conceptHelp;
            byte[] before=progress(LearningSnapshot.capture(s));
            int originalStage=d.stage;String original=d.entries.get(d.stage),oldGate=d.entries.get(gate(q));
            var oldFold=new ArrayList<>(d.triangleFoldActions);var oldVisits=new ArrayList<>(d.triangleVisited);
            assertTrue(KoreanTriangleLesson.revisit(q,d));assertTrue(KoreanTriangleLesson.revisiting(q,d));
            assertFalse(KoreanTriangleLesson.revisit(q,d));partial(q,d);
            assertFalse(KoreanTriangleLesson.advance(q,d,KoreanTriangleClassification.guide(q).frames.get(gate(q)).expected));
            var cold=LearningSnapshot.capture(s);var cd=cold.session.conceptHelp;KoreanTriangleLesson.restore(cold.session.question,cd);
            assertTrue(KoreanTriangleLesson.revisiting(cold.session.question,cd));assertEquals(originalStage,cd.triangleRevisitReturnStage);
            assertEquals(original,cd.entries.get(originalStage));assertEquals(List.of("original-body-entry"),cold.session.answers);
            assertTrue(KoreanTriangleLesson.cancelRevisit(cold.session.question,cd));assertFalse(KoreanTriangleLesson.cancelRevisit(cold.session.question,cd));
            cold=LearningSnapshot.capture(cold);cd=cold.session.conceptHelp;KoreanTriangleLesson.restore(cold.session.question,cd);
            assertEquals(originalStage,cd.stage);assertEquals(original,cd.entries.get(originalStage));assertEquals(oldGate,cd.entries.get(gate(q)));
            assertEquals(oldFold,cd.triangleFoldActions);assertEquals(oldVisits,cd.triangleVisited);assertEquals(-1,cd.triangleRevisitReturnStage);assertNull(cd.triangleRevisitBefore);
            assertEquals(0,cold.session.completed);assertTrue(cold.history.isEmpty());assertArrayEquals(before,progress(cold));
        }
    }
    @Test public void freshCompletedComparisonReturnsToTheSameWrongEntryWithoutCompletingOrTransferringAnAnswer()throws Exception{
        for(String id:KINDS){
            var s=wrong(id);Question q=s.session.question;var d=s.session.conceptHelp;int target=d.stage;String raw=d.entries.get(target);
            byte[] before=progress(LearningSnapshot.capture(s));
            assertTrue(KoreanTriangleLesson.revisit(q,d));
            assertFalse(KoreanTriangleLesson.advance(q,d,KoreanTriangleClassification.guide(q).frames.get(gate(q)).expected));
            compare(q,d);assertTrue(KoreanTriangleLesson.advance(q,d,KoreanTriangleClassification.guide(q).frames.get(gate(q)).expected));
            assertEquals(target,d.stage);assertEquals(raw,d.entries.get(target));assertNull(d.triangleRevisitBefore);assertEquals(-1,d.triangleRevisitReturnStage);
            assertFalse(KoreanTriangleLesson.cancelRevisit(q,d));assertFalse(KoreanTriangleLesson.advance(q,d,"ok"));
            assertEquals(target,d.stage);assertEquals(List.of("original-body-entry"),s.session.answers);assertEquals(0,s.session.completed);assertTrue(s.history.isEmpty());assertArrayEquals(before,progress(s));
            assertFalse(HelpPlan.forQuestion(q).canTransfer());
        }
    }
    @Test public void immutableBackupAndDraftCopiesDoNotShareMutableActiveObservationLists(){
        for(String id:KINDS){
            var s=wrong(id);var q=s.session.question;var d=s.session.conceptHelp;assertTrue(KoreanTriangleLesson.revisit(q,d));partial(q,d);
            var copy=d.copy();copy.triangleFoldActions.clear();copy.triangleVisited.clear();
            if(id.equals(KoreanTriangleClassification.PROPERTIES))assertEquals(2,d.triangleFoldActions.size());else assertEquals(List.of(0),d.triangleVisited);
            assertNotSame(d.triangleRevisitBefore,copy.triangleRevisitBefore);
            try{copy.triangleRevisitBefore.visited().add(0);fail("Backup must be immutable");}catch(UnsupportedOperationException expected){}
            assertEquals(d.triangleRevisitBefore,copy.triangleRevisitBefore);
        }
    }
    @Test public void oldDefaultMarkersAndForgedPreviousProofCannotCreateACompletionOrSkip(){
        for(String id:KINDS){
            var s=wrong(id);var q=s.session.question;var d=s.session.conceptHelp;int target=d.stage;
            d.triangleRevisitReturnStage=0;KoreanTriangleLesson.restore(q,d);assertEquals(-1,d.triangleRevisitReturnStage);assertEquals(target,d.stage);
            assertTrue(KoreanTriangleLesson.revisit(q,d));
            d.triangleRevisitBefore=new KoreanTriangleLesson.Comparison(List.of(),List.of(),-1,gate(q),"ok");
            KoreanTriangleLesson.restore(q,d);assertFalse(KoreanTriangleLesson.revisiting(q,d));assertNull(d.triangleRevisitBefore);assertEquals(-1,d.triangleRevisitReturnStage);
            assertFalse(KoreanTriangleLesson.cancelRevisit(q,d));assertFalse(KoreanTriangleLesson.advance(q,d,KoreanTriangleClassification.guide(q).frames.get(gate(q)).expected));
        }
    }
    @Test public void otherQuestionOrTeachingVersionDropsTheDetourInsteadOfReusingItsProof(){
        for(String id:KINDS){
            var s=wrong(id);var q=s.session.question;var d=s.session.conceptHelp;assertTrue(KoreanTriangleLesson.revisit(q,d));
            var fresh=HelpPlan.forQuestion(q).restore(d,"a-different-question");assertNull(fresh.triangleRevisitBefore);assertEquals(-1,fresh.triangleRevisitReturnStage);
            d.teachingVersion="invalid-version";assertFalse(KoreanTriangleLesson.revisiting(q,d));assertFalse(KoreanTriangleLesson.cancelRevisit(q,d));assertFalse(KoreanTriangleLesson.revisit(q,d));
            fresh=HelpPlan.forQuestion(q).restore(d,q.id);assertNull(fresh.triangleRevisitBefore);assertEquals(-1,fresh.triangleRevisitReturnStage);
        }
    }
}
