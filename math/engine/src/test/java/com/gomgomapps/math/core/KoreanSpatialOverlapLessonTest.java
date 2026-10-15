package com.gomgomapps.math.core;

import org.junit.Test;
import static org.junit.Assert.*;
import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;

public class KoreanSpatialOverlapLessonTest {
    private static final LocalDate DAY=LocalDate.of(2026,10,15);
    private Question question(String id,List<Integer> nearToFar){
        int n=nearToFar.size(),w=KoreanSpatialSolids.FRONT.equals(id)?2:n,d=KoreanSpatialSolids.FRONT.equals(id)?n:2;
        List<Integer> h=new ArrayList<>(Collections.nCopies(w*d,0));for(int i=0;i<n;i++)h.set(KoreanSpatialSolids.FRONT.equals(id)?i*w:n-1-i,nearToFar.get(i));
        Question q=new Question(id,KoreanSpatialSolids.prompt(id),"poison","poison");q.diagram=KoreanSpatialSolids.heightDiagram(w,d,h);KoreanSpatialSolids.choices(q,new Random(391));return q;
    }
    private HelpPlan.Draft parent(Question q){
        HelpPlan.Draft d=HelpPlan.forQuestion(q).restore(null,q.id);assertNotNull(KoreanSpatialLesson.restore(q,d));assertTrue(KoreanSpatialLesson.select(q,d,0));
        d.entries.set(0,"old-parent-888");d.spatialPendingInput="parent raw 999";assertTrue(KoreanSpatialOverlapLesson.available(q,d));return d;
    }
    private byte[] bytes(Object value)throws Exception{
        ByteArrayOutputStream b=new ByteArrayOutputStream();try(ObjectOutputStream out=new ObjectOutputStream(b)){out.writeObject(value);}return b.toByteArray();
    }
    private byte[] parentBytes(HelpPlan.Draft d)throws Exception{
        HelpPlan.Draft x=d.copy();x.spatialOverlapScope=null;x.spatialOverlapOpen=false;x.spatialOverlapActions=new ArrayList<>();x.spatialOverlapPendingInput=null;return bytes(x);
    }
    private void viewAndEnterDrawing(Question q,HelpPlan.Draft d){
        assertNotNull(KoreanSpatialOverlapLesson.open(q,d));assertFalse(KoreanSpatialOverlapLesson.finishView(q,d));assertFalse(KoreanSpatialOverlapLesson.advance(q,d,"ok"));
        assertTrue(KoreanSpatialOverlapLesson.watch(q,d));assertTrue(KoreanSpatialOverlapLesson.state(q,d).viewing());assertFalse(KoreanSpatialOverlapLesson.state(q,d).viewed());assertFalse(KoreanSpatialOverlapLesson.advance(q,d,"ok"));
        assertTrue(KoreanSpatialOverlapLesson.finishView(q,d));assertTrue(KoreanSpatialOverlapLesson.state(q,d).viewed());assertTrue(KoreanSpatialOverlapLesson.advance(q,d,"ok"));
    }
    private void drawAndFinish(Question q,HelpPlan.Draft d){
        int height=Collections.max(KoreanSpatialOverlapLesson.model(q,d).heights());
        for(int z=0;z<height;z++)assertTrue(KoreanSpatialOverlapLesson.toggle(q,d,z));
        assertTrue(KoreanSpatialOverlapLesson.advance(q,d,"ok"));assertFalse(KoreanSpatialOverlapLesson.advance(q,d,"999"));assertEquals("999",KoreanSpatialOverlapLesson.state(q,d).entry());
        assertTrue(KoreanSpatialOverlapLesson.advance(q,d,""+height));assertTrue(KoreanSpatialOverlapLesson.state(q,d).complete());
    }

    @Test public void allTwoAndThreeColumnSourcesRequireAViewCallbackExactDrawingAndSeparateHeight()throws Exception{
        for(String id:List.of(KoreanSpatialSolids.FRONT,KoreanSpatialSolids.SIDE))for(int count=2;count<=3;count++){
            int domain=count==2?16:64;for(int code=0;code<domain;code++){
                List<Integer> heights=new ArrayList<>();for(int i=0;i<count;i++)heights.add(code>>(2*i)&3);
                Question q=question(id,heights);HelpPlan.Draft d=parent(q);byte[] old=parentBytes(d),source=bytes(q);assertEquals(heights,KoreanSpatialOverlapLesson.model(q,d).heights());
                viewAndEnterDrawing(q,d);drawAndFinish(q,d);assertArrayEquals(old,parentBytes(d));assertArrayEquals(source,bytes(q));assertEquals(0,d.stage);assertEquals("old-parent-888",d.entries.get(0));
                KoreanSpatialOverlapLesson.close(q,d);assertNotNull(KoreanSpatialOverlapLesson.open(q,d));assertTrue(KoreanSpatialOverlapLesson.state(q,d).complete());assertFalse(KoreanSpatialOverlapLesson.watch(q,d));assertFalse(KoreanSpatialOverlapLesson.advance(q,d,"ok"));
            }
        }
    }
    @Test public void replayRequestsPreservePartialDrawingWrongEntryAndUnsubmittedText(){
        Question q=question(KoreanSpatialSolids.FRONT,List.of(1,0,3));HelpPlan.Draft d=parent(q);viewAndEnterDrawing(q,d);
        assertTrue(KoreanSpatialOverlapLesson.toggle(q,d,0));assertFalse(KoreanSpatialOverlapLesson.advance(q,d,"ok"));assertEquals("drawing",KoreanSpatialOverlapLesson.state(q,d).entry());assertNotNull(KoreanSpatialOverlapLesson.feedback(q,d));d.spatialOverlapPendingInput="unsubmitted888";
        for(int i=0;i<2;i++){
            assertTrue(KoreanSpatialOverlapLesson.watch(q,d));var s=KoreanSpatialOverlapLesson.state(q,d);assertEquals(1,s.phase());assertEquals(List.of(0),s.drawn());assertEquals("drawing",s.entry());assertFalse(s.ready());assertFalse(KoreanSpatialOverlapLesson.advance(q,d,"ok"));assertFalse(KoreanSpatialOverlapLesson.toggle(q,d,1));
            assertTrue(KoreanSpatialOverlapLesson.finishView(q,d));assertEquals("drawing",KoreanSpatialOverlapLesson.state(q,d).entry());assertEquals("unsubmitted888",d.spatialOverlapPendingInput);
        }
        assertTrue(KoreanSpatialOverlapLesson.toggle(q,d,1));assertEquals("",KoreanSpatialOverlapLesson.state(q,d).entry());assertTrue(KoreanSpatialOverlapLesson.toggle(q,d,2));assertTrue(KoreanSpatialOverlapLesson.advance(q,d,"ok"));
        assertFalse(KoreanSpatialOverlapLesson.advance(q,d,"999"));assertTrue(KoreanSpatialOverlapLesson.watch(q,d));assertEquals(2,KoreanSpatialOverlapLesson.state(q,d).phase());assertEquals("999",KoreanSpatialOverlapLesson.state(q,d).entry());assertTrue(KoreanSpatialOverlapLesson.finishView(q,d));assertEquals("999",KoreanSpatialOverlapLesson.state(q,d).entry());assertTrue(KoreanSpatialOverlapLesson.advance(q,d,"003"));assertEquals("old-parent-888",d.entries.get(0));
    }
    @Test public void v1NeverBecomesViewedAndColdPendingRequestNeedsItsActualCallback()throws Exception{
        Question q=question(KoreanSpatialSolids.SIDE,List.of(1,3));HelpPlan.Draft d=parent(q);byte[] old=parentBytes(d);KoreanSpatialOverlapLesson.open(q,d);
        d.spatialOverlapScope=d.spatialOverlapScope.replace(KoreanSpatialOverlapLesson.VERSION,"korean-spatial-overlap-v1");d.spatialOverlapActions=new ArrayList<>(List.of("place","try:overlap"));d.spatialOverlapPendingInput="legacy888";
        var fresh=KoreanSpatialOverlapLesson.restore(q,d);assertEquals(0,fresh.phase());assertFalse(fresh.viewed());assertFalse(fresh.viewing());assertTrue(d.spatialOverlapActions.isEmpty());assertFalse(d.spatialOverlapOpen);assertNull(d.spatialOverlapPendingInput);assertArrayEquals(old,parentBytes(d));
        assertNotNull(KoreanSpatialOverlapLesson.open(q,d));assertTrue(KoreanSpatialOverlapLesson.watch(q,d));assertFalse(KoreanSpatialOverlapLesson.watch(q,d));List<String> request=new ArrayList<>(d.spatialOverlapActions);
        assertNotNull(KoreanSpatialOverlapLesson.close(q,d));assertFalse(KoreanSpatialOverlapLesson.finishView(q,d));assertEquals(request,d.spatialOverlapActions);
        HelpPlan.Draft cold;try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes(d)))){cold=(HelpPlan.Draft)in.readObject();}
        assertNotNull(KoreanSpatialOverlapLesson.open(q,cold));assertTrue(KoreanSpatialOverlapLesson.state(q,cold).viewing());assertFalse(KoreanSpatialOverlapLesson.state(q,cold).viewed());assertFalse(KoreanSpatialOverlapLesson.advance(q,cold,"ok"));
        assertTrue(KoreanSpatialOverlapLesson.finishView(q,cold));assertTrue(KoreanSpatialOverlapLesson.advance(q,cold,"ok"));assertArrayEquals(old,parentBytes(cold));assertNotNull(KoreanSpatialOverlapLesson.restart(q,cold));assertFalse(KoreanSpatialOverlapLesson.state(q,cold).viewed());assertTrue(cold.spatialOverlapActions.isEmpty());
    }
    @Test public void generalOrderingPublicAuthorityAndStaleParentBindingRemainStrict()throws Exception{
        Question f=question(KoreanSpatialSolids.FRONT,List.of(1,0,3));assertEquals(List.of(0,2,4),KoreanSpatialOverlapLesson.model(f,parent(f)).cells());
        Question q=question(KoreanSpatialSolids.SIDE,List.of(1,0,3));HelpPlan.Draft d=parent(q);var m=KoreanSpatialOverlapLesson.model(q,d);assertEquals(List.of(2,1,0),m.cells());
        try{m.heights().set(0,3);fail();}catch(UnsupportedOperationException expected){}
        q.answers=null;q.expression="999=999";q.kind="complexRoots";q.studyGuide=new StudyGuide().step("private","","","999");q.correctChoice=999;q.choiceLabels.clear();q.choiceDiagrams.clear();assertEquals(m,KoreanSpatialOverlapLesson.model(q,d));viewAndEnterDrawing(q,d);drawAndFinish(q,d);
        for(int change=0;change<7;change++){
            q=question(KoreanSpatialSolids.SIDE,List.of(1,2,3));d=parent(q);KoreanSpatialOverlapLesson.open(q,d);KoreanSpatialOverlapLesson.watch(q,d);String previous=d.spatialOverlapScope;
            if(change==0)d.spatialPendingInput="different parent raw";else if(change==1)d.entries.set(0,"changed wrong");else if(change==2)Collections.reverse(q.choices);else if(change==3)d.teachingVersion="legacy";else if(change==4)q.id="other";else if(change==5)q.diagram.values[2]=0;else assertTrue(KoreanSpatialLesson.visit(q,d,0));
            byte[] old=parentBytes(d);assertNull(KoreanSpatialOverlapLesson.state(q,d));KoreanSpatialOverlapLesson.restore(q,d);assertFalse(d.spatialOverlapOpen);assertTrue(d.spatialOverlapActions.isEmpty());assertArrayEquals(old,parentBytes(d));if(KoreanSpatialOverlapLesson.available(q,d))assertNotEquals(previous,d.spatialOverlapScope);
        }
        assertNull(KoreanSpatialOverlapLesson.model(null,null));assertNull(KoreanSpatialOverlapLesson.restore(null,null));
    }
    @Test public void onlyAuthenticV2GrammarAndCorrectOrderedGatesCanComplete()throws Exception{
        Question q=question(KoreanSpatialSolids.FRONT,List.of(1,3));HelpPlan.Draft d=parent(q);KoreanSpatialOverlapLesson.open(q,d);byte[] old=parentBytes(d);
        List<List<String>> invalid=List.of(List.of("view:finish"),List.of("advance:ok"),List.of("view:request","advance:ok"),List.of("view:request","view:request"),List.of("toggle:0"),List.of("view"),List.of("place"),List.of("classify:new"),List.of("view:request","view:finish","advance:ok","advance:ok"),List.of("try:drawing"),List.of("answer:3"));
        for(List<String> actions:invalid){HelpPlan.Draft x=d.copy();x.spatialOverlapActions=new ArrayList<>(actions);x.spatialOverlapPendingInput="forged3";assertNull(KoreanSpatialOverlapLesson.state(q,x));assertNull(KoreanSpatialOverlapLesson.open(q,x));assertFalse(x.spatialOverlapOpen);assertTrue(x.spatialOverlapActions.isEmpty());assertNull(x.spatialOverlapPendingInput);assertArrayEquals(old,parentBytes(x));}
        HelpPlan.Draft x=d.copy();x.spatialOverlapActions.add(null);assertNull(KoreanSpatialOverlapLesson.state(q,x));x=d.copy();x.spatialOverlapActions=new ArrayList<>(Collections.nCopies(129,"view:request"));assertNull(KoreanSpatialOverlapLesson.state(q,x));
        viewAndEnterDrawing(q,d);drawAndFinish(q,d);d.spatialOverlapActions.add("view:request");assertNull(KoreanSpatialOverlapLesson.state(q,d));assertArrayEquals(old,parentBytes(d));
    }
    @Test public void hundredsOfRequestsWrongDrawingsAndNumericAttemptsKeepBoundedActualEvidence(){
        Question q=question(KoreanSpatialSolids.FRONT,List.of(3,3,3));HelpPlan.Draft d=parent(q);KoreanSpatialOverlapLesson.open(q,d);
        for(int i=0;i<350;i++){assertTrue(KoreanSpatialOverlapLesson.watch(q,d));assertFalse(KoreanSpatialOverlapLesson.advance(q,d,"ok"));assertTrue(KoreanSpatialOverlapLesson.finishView(q,d));}
        assertTrue(d.spatialOverlapActions.size()<=2);assertTrue(KoreanSpatialOverlapLesson.advance(q,d,"ok"));
        for(int i=0;i<350;i++)assertTrue(KoreanSpatialOverlapLesson.toggle(q,d,0));assertTrue(KoreanSpatialOverlapLesson.state(q,d).drawn().isEmpty());
        for(int i=0;i<350;i++){assertFalse(KoreanSpatialOverlapLesson.advance(q,d,"ok"));assertTrue(KoreanSpatialOverlapLesson.watch(q,d));assertEquals("drawing",KoreanSpatialOverlapLesson.state(q,d).entry());assertTrue(KoreanSpatialOverlapLesson.finishView(q,d));}
        for(int z=0;z<3;z++)assertTrue(KoreanSpatialOverlapLesson.toggle(q,d,z));assertTrue(KoreanSpatialOverlapLesson.advance(q,d,"ok"));
        for(int i=0;i<350;i++){assertFalse(KoreanSpatialOverlapLesson.advance(q,d,"999"));assertTrue(KoreanSpatialOverlapLesson.watch(q,d));assertEquals("999",KoreanSpatialOverlapLesson.state(q,d).entry());assertTrue(KoreanSpatialOverlapLesson.finishView(q,d));}
        assertTrue(d.spatialOverlapActions.size()<16);assertEquals("999",KoreanSpatialOverlapLesson.state(q,d).entry());assertTrue(KoreanSpatialOverlapLesson.advance(q,d,"3"));assertTrue(KoreanSpatialOverlapLesson.state(q,d).complete());assertEquals("old-parent-888",d.entries.get(0));
    }
    @Test public void snapshotAndSerializedPayloadKeepPendingReplayAndWrongDrawingWithoutLearningEffects()throws Exception{
        Learning.State state=new Learning.State();state.profile.grade=6;state.profile.term=2;state.profile.curriculum=2022;state.profile.schoolYear=2026;state.profile.ready=true;
        Learning.beginPractice(state,"practice",List.of(KoreanSpatialSolids.FRONT),20,false,new Random(39120));Question q=Learning.ensureQuestion(state,new Generator(new Random(39121)),DAY);HelpPlan.Draft d=parent(q);state.session.conceptHelp=d;state.session.answers.set(0,q.choices.get(0));Review.helpUsed(state,DAY);
        byte[] progress=bytes(state.progress),oldParent=parentBytes(d),source=bytes(q);long sequence=state.reviewSequence;String body=state.session.answers.get(0);
        Learning.State normalized=LearningSnapshot.capture(state);byte[] storedProgress=bytes(normalized.progress),storedQuestion=bytes(normalized.session.question);
        viewAndEnterDrawing(q,d);int h=Collections.max(KoreanSpatialOverlapLesson.model(q,d).heights());assertTrue(KoreanSpatialOverlapLesson.toggle(q,d,h==1?1:0));assertFalse(KoreanSpatialOverlapLesson.advance(q,d,"ok"));assertTrue(KoreanSpatialOverlapLesson.watch(q,d));d.spatialOverlapPendingInput="unsubmitted999";
        assertArrayEquals(progress,bytes(state.progress));assertArrayEquals(source,bytes(q));Learning.State snapshot=LearningSnapshot.capture(state);HelpPlan.Draft copy=snapshot.session.conceptHelp;assertNotSame(d.spatialOverlapActions,copy.spatialOverlapActions);assertEquals(KoreanSpatialOverlapLesson.state(q,d),KoreanSpatialOverlapLesson.state(snapshot.session.question,copy));
        // Same ObjectStream payload format as StateStore; Android AtomicFile/queue execution remains native QA.
        Path file=Files.createTempFile("spatial-overlap-v2-", ".bin");Learning.State cold;
        try{try(ObjectOutputStream out=new ObjectOutputStream(Files.newOutputStream(file))){out.writeObject(snapshot);}try(ObjectInputStream in=new ObjectInputStream(Files.newInputStream(file))){cold=(Learning.State)in.readObject();}}finally{Files.deleteIfExists(file);}
        var loaded=cold.session.conceptHelp;assertEquals(KoreanSpatialOverlapLesson.state(q,d),KoreanSpatialOverlapLesson.restore(cold.session.question,loaded));assertTrue(KoreanSpatialOverlapLesson.state(q,loaded).viewing());assertEquals("drawing",KoreanSpatialOverlapLesson.state(q,loaded).entry());assertEquals("unsubmitted999",loaded.spatialOverlapPendingInput);assertFalse(KoreanSpatialOverlapLesson.advance(q,loaded,"ok"));
        assertNotNull(KoreanSpatialOverlapLesson.open(cold.session.question,loaded));assertTrue(KoreanSpatialOverlapLesson.finishView(cold.session.question,loaded));assertEquals("drawing",KoreanSpatialOverlapLesson.state(q,loaded).entry());
        KoreanSpatialOverlapLesson.restart(cold.session.question,loaded);viewAndEnterDrawing(cold.session.question,loaded);drawAndFinish(cold.session.question,loaded);
        assertArrayEquals(oldParent,parentBytes(loaded));assertArrayEquals(storedQuestion,bytes(LearningSnapshot.capture(cold).session.question));assertArrayEquals(storedProgress,bytes(LearningSnapshot.capture(cold).progress));assertEquals(sequence,cold.reviewSequence);assertEquals(body,cold.session.answers.get(0));assertEquals(0,cold.session.completed);assertTrue(cold.history.isEmpty());
        copy.spatialOverlapActions.clear();assertFalse(d.spatialOverlapActions.isEmpty());HelpPlan.Draft legacy=d.copy();legacy.spatialOverlapScope=null;legacy.spatialOverlapActions=null;legacy.spatialOverlapOpen=false;assertNotNull(KoreanSpatialOverlapLesson.restore(q,legacy));assertTrue(legacy.spatialOverlapActions.isEmpty());assertArrayEquals(oldParent,parentBytes(legacy));
    }
}
