package com.gomgomapps.math.core;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;
import java.io.*;

public class KoreanTriangleLessonTest {
    private Question question(String id){return new Generator(new Random(3824)).create(Catalog.get(id));}
    private HelpPlan.Draft gate(Question q){HelpPlan p=HelpPlan.forQuestion(q);var d=p.restore(null,q.id);int gate=KoreanTriangleLesson.inspectionStage(q);var guide=KoreanTriangleClassification.guide(q);while(d.stage<gate){assertTrue(KoreanTriangleLesson.advance(q,d,guide.frames.get(d.stage).expected));}KoreanTriangleLesson.restore(q,d);return d;}
    private HelpPlan.Draft cold(HelpPlan.Draft d)throws Exception{ByteArrayOutputStream bytes=new ByteArrayOutputStream();try(ObjectOutputStream o=new ObjectOutputStream(bytes)){o.writeObject(d);}try(ObjectInputStream i=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))){return (HelpPlan.Draft)i.readObject();}}
    @Test public void correctWordCannotReplaceComparingEverySideOrCorner(){
        for(String id:List.of("krTriangleSides","krTriangleAngles")){
            Question q=question(id);var d=gate(q);int gate=d.stage;String key=KoreanTriangleClassification.guide(q).frames.get(gate).expected;
            assertFalse(KoreanTriangleLesson.advance(q,d,key));assertEquals(gate,d.stage);
            assertFalse(KoreanTriangleLesson.visit(q,d,-1));assertFalse(KoreanTriangleLesson.visit(q,d,3));
            assertTrue(KoreanTriangleLesson.visit(q,d,0));assertTrue(KoreanTriangleLesson.visit(q,d,0));assertEquals(List.of(0),d.triangleVisited);
            assertFalse(KoreanTriangleLesson.advance(q,d,key));assertTrue(KoreanTriangleLesson.visit(q,d,2));assertFalse(KoreanTriangleLesson.advance(q,d,key));
            assertTrue(KoreanTriangleLesson.visit(q,d,1));assertTrue(KoreanTriangleLesson.advance(q,d,key));assertFalse(HelpPlan.forQuestion(q).canTransfer());
        }
    }
    @Test public void partialVisitsWrongSelectionAndCopyAreIndependentOfOriginalAnswer()throws Exception{
        Question q=question("krTriangleAngles");var d=gate(q);assertTrue(KoreanTriangleLesson.visit(q,d,1));assertFalse(KoreanTriangleLesson.advance(q,d,"wrong"));
        d=cold(d);d=HelpPlan.forQuestion(q).restore(d,q.id);KoreanTriangleLesson.restore(q,d);assertEquals(1,d.stage);assertEquals(List.of(1),d.triangleVisited);assertEquals(1,d.triangleVertex);assertEquals("wrong",d.entries.get(1));
        var copy=d.copy();copy.triangleVisited.clear();assertEquals(List.of(1),d.triangleVisited);
        Learning.State s=new Learning.State();s.session=new Learning.Session();s.session.question=q;s.session.conceptHelp=d;s.session.answers.add("");
        Learning.State snap=LearningSnapshot.capture(s);assertEquals(List.of(1),snap.session.conceptHelp.triangleVisited);assertNotSame(d.triangleVisited,snap.session.conceptHelp.triangleVisited);assertEquals(List.of(""),snap.session.answers);assertTrue(snap.history.isEmpty());assertEquals(0,snap.session.completed);
    }
    @Test public void savedFinalTokenWithoutVisitsReturnsToComparisonButValidColdCompletionSurvives()throws Exception{
        Question q=question("krTriangleSides");var d=gate(q);var h=KoreanTriangleClassification.guide(q);
        for(var f:h.frames)d.entries.add(f.expected);d.stage=h.frames.size();KoreanTriangleLesson.restore(q,d);assertEquals(0,d.stage);
        d=HelpPlan.forQuestion(q).restore(null,q.id);for(int i=0;i<3;i++)assertTrue(KoreanTriangleLesson.visit(q,d,i));
        while(d.stage<h.frames.size())assertTrue(KoreanTriangleLesson.advance(q,d,h.frames.get(d.stage).expected));
        d=cold(d);d=HelpPlan.forQuestion(q).restore(d,q.id);KoreanTriangleLesson.restore(q,d);assertEquals(h.frames.size(),d.stage);
        d.teachingVersion="old";d=HelpPlan.forQuestion(q).restore(d,q.id);assertTrue(d.triangleVisited.isEmpty());assertEquals(-1,d.triangleVertex);assertEquals(0,d.stage);
    }
    @Test public void anotherQuestionOrInvalidDuplicateVisitCannotPassGate(){Question q=question("krTriangleSides");var d=gate(q);d.triangleVisited.addAll(List.of(0,0,1));KoreanTriangleLesson.restore(q,d);assertTrue(d.triangleVisited.isEmpty());d.questionId="other";assertFalse(KoreanTriangleLesson.visit(q,d,0));assertFalse(KoreanTriangleLesson.advance(q,d,"three"));}
}
