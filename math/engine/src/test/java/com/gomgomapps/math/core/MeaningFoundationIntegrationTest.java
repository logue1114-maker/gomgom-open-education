package com.gomgomapps.math.core;

import org.junit.Test;
import static org.junit.Assert.*;
import java.io.*;
import java.lang.reflect.*;
import java.time.LocalDate;
import java.util.*;

/** Actual save-graph tests; the short public examples are not native/student-effect evidence. */
public class MeaningFoundationIntegrationTest {
    private static final LocalDate DAY=LocalDate.of(2026,10,15);
    private Question question(String id,String prompt){return new Question(id,prompt,"poisoned private expression","999999");}
    private byte[] bytes(Object value)throws Exception {ByteArrayOutputStream b=new ByteArrayOutputStream();try(ObjectOutputStream o=new ObjectOutputStream(b)){o.writeObject(value);}return b.toByteArray();}
    private <T> T cold(T value)throws Exception {try(ObjectInputStream i=new ObjectInputStream(new ByteArrayInputStream(bytes(value)))){@SuppressWarnings("unchecked")T restored=(T)i.readObject();return restored;}}
    /** Serialization can change Set implementation/capacity; compare every saved semantic field. */
    private void reviewEquals(String path,Object expected,Object actual)throws Exception {
        if(expected==null){assertNull(path,actual);return;}
        assertNotNull(path,actual);
        if(expected instanceof Set<?> set){assertTrue(path,actual instanceof Set<?>);assertEquals(path,set,actual);if(set instanceof LinkedHashSet<?>)assertEquals(path+".insertionOrder",new ArrayList<>(set),new ArrayList<>((Set<?>)actual));return;}
        if(expected instanceof List<?> list){assertTrue(path,actual instanceof List<?>);List<?> other=(List<?>)actual;assertEquals(path+".size",list.size(),other.size());for(int i=0;i<list.size();i++)reviewEquals(path+"["+i+"]",list.get(i),other.get(i));return;}
        if(expected instanceof String||expected instanceof Number||expected instanceof Boolean||expected instanceof Enum<?>){assertEquals(path,expected,actual);return;}
        assertEquals(path+".type",expected.getClass(),actual.getClass());
        assertTrue(path+" must remain an explicitly supported Review model",expected instanceof Review.Track||expected instanceof Review.Work||expected instanceof Review.Event);
        for(Field field:expected.getClass().getDeclaredFields())if(!Modifier.isStatic(field.getModifiers())){field.setAccessible(true);reviewEquals(path+"."+field.getName(),field.get(expected),field.get(actual));}
    }
    private Learning.State state(Question q){
        Learning.State state=new Learning.State();Learning.Session s=new Learning.Session();state.session=s;s.countryCode="KR";s.educationSystem="kr-national";s.languageTag="ko";s.mode="practice";s.question=q;s.selected.add(q.skillId);s.queue.addAll(List.of(q.skillId,q.skillId));
        s.answers.add("777");s.steps.add("kept original work");s.stepKinds.add(Checker.StepKind.FULL);s.conceptHelp=new HelpPlan.Draft();s.conceptHelp.questionId=q.id;s.conceptHelp.teachingVersion="parent-help-kept";s.conceptHelp.stage=2;s.conceptHelp.entries.addAll(List.of("old1","old2","888"));
        state.savedSessions.put(s.id,s);state.needsPractice.add(q.skillId);Review.openFresh(state,DAY);Learning.markError(state,DAY);Review.helpUsed(state,DAY);
        s.conceptHelp.meaningFoundation=MeaningFoundationLesson.create(q);return state;
    }
    /** The child may change; everything else must retain exactly the same serialized graph. */
    private byte[] withoutChild(Learning.State state)throws Exception {Learning.State detached=LearningSnapshot.capture(state);detached.session.conceptHelp.meaningFoundation=null;return bytes(detached);}
    private MeaningFoundationLesson.Scene scene(Question q,MeaningFoundationLesson.Draft d){return MeaningFoundationLesson.model(q).scenes().get(MeaningFoundationLesson.state(q,d).stage());}
    private void solve(Question q,MeaningFoundationLesson.Draft d){
        var s=scene(q,d);switch(s.kind()){
            case "groups"->{for(int i=0;i<s.objects();){int n=Math.min(s.groupSize(),s.objects()-i);for(int k=0;k<n;k++)assertTrue(MeaningFoundationLesson.mark(q,d,i++));assertTrue(MeaningFoundationLesson.group(q,d));}}
            case "distribute"->{for(int i=0;i<s.objects();i++)assertTrue(MeaningFoundationLesson.place(q,d,i,i%s.bins()));}
            case "scale"->{int factor=s.id().startsWith("decimal")?10:2;assertTrue(MeaningFoundationLesson.scale(q,d,factor,true,!s.id().endsWith("one-side")));}
        }
        // Integration traverses the generated teaching contract; independent math lives in the unit test.
        String raw=MeaningFoundationLesson.guide(q).frames.get(MeaningFoundationLesson.state(q,d).stage()).expected;
        assertTrue(s.id(),MeaningFoundationLesson.advance(q,d,raw));
    }
    private void until(Question q,MeaningFoundationLesson.Draft d,String id){while(!scene(q,d).id().equals(id))solve(q,d);}

    @Test public void actualSnapshotAndJavaColdDetachPartialObjectsAndPreserveAliasesAndParentWork()throws Exception {
        Question q=question("mulIntro","3씩 4묶음은 모두 얼마인가요?");Learning.State live=state(q);var d=live.session.conceptHelp.meaningFoundation;solve(q,d);assertTrue(MeaningFoundationLesson.mark(q,d,0));assertTrue(MeaningFoundationLesson.mark(q,d,1));
        Review.Track initialTrack=Review.track(live.progress(q.skillId));
        boolean initiallyInPractice=live.needsPractice.contains(q.skillId),initialPractice=initialTrack.practice,initialPending=initialTrack.pending;
        Learning.State initial=LearningSnapshot.capture(live);
        Review.Track initialReview=Review.track(initial.progress(q.skillId));Review.Work initialWork=initial.session.reviewWork;
        var copy=live.session.conceptHelp.copy();assertNotSame(d,copy.meaningFoundation);assertNotSame(d.actions,copy.meaningFoundation.actions);
        Learning.State frozen=LearningSnapshot.capture(live);assertSame(frozen.session,frozen.savedSessions.get(frozen.session.id));assertNotSame(d,frozen.session.conceptHelp.meaningFoundation);assertNotSame(d.actions,frozen.session.conceptHelp.meaningFoundation.actions);byte[] frozenBytes=bytes(frozen);
        assertTrue(MeaningFoundationLesson.mark(q,d,2));assertTrue(MeaningFoundationLesson.group(q,d));assertArrayEquals(frozenBytes,bytes(frozen));
        Learning.State restored=cold(frozen);var rd=restored.session.conceptHelp.meaningFoundation;assertNotNull(MeaningFoundationLesson.restore(restored.session.question,rd));assertEquals(List.of(0,1),MeaningFoundationLesson.state(restored.session.question,rd).selected());
        assertEquals("Original body input survives cold restore",List.of("777"),restored.session.answers);
        assertEquals("Original working survives cold restore",List.of("kept original work"),restored.session.steps);
        assertEquals("Parent teaching stage is unchanged",2,restored.session.conceptHelp.stage);
        assertEquals("Parent teaching inputs are unchanged",List.of("old1","old2","888"),restored.session.conceptHelp.entries);
        assertEquals("Preserve actual practice-list membership; do not promote one error to practice",initiallyInPractice,restored.needsPractice.contains(q.skillId));
        Review.Track restoredTrack=Review.track(restored.progress(q.skillId));
        assertEquals("Practice policy is unchanged",initialPractice,restoredTrack.practice);
        assertEquals("Pending policy is unchanged",initialPending,restoredTrack.pending);
        assertTrue("The actual wrong answer still requires a fresh recheck",restoredTrack.pending);
        assertTrue("The failed public fact remains recorded",restoredTrack.failed.contains(q.signature()));
        assertTrue("Actual parent error is still recorded",restored.session.reviewWork.errorRecorded);
        assertTrue("Actual parent help use is still recorded",restored.session.reviewWork.helpUsed);
        reviewEquals("All Review events, failed/checked facts, generation and spacing survive",initialReview,restoredTrack);
        reviewEquals("The complete parent Review work survives",initialWork,restored.session.reviewWork);
        assertEquals("Child actions do not submit the parent",0,restored.session.completed);
        assertTrue("Child actions add no history",restored.history.isEmpty());
    }
    @Test public void wrongRecallReopenAndCompletionCannotSubmitParentOrResetPendingReview()throws Exception {
        for(Question q:List.of(question("decimalDiv","1.2 ÷ 0.3"),question("proportion","2 : 3 = 4 : x\nx의 값은?"))){
            Learning.State state=state(q);byte[] baseline=withoutChild(state);var d=state.session.conceptHelp.meaningFoundation;String prefix=q.skillId.equals("decimalDiv")?"decimal":"ratio";until(q,d,prefix+"-answer");assertTrue(MeaningFoundationLesson.input(q,d,"888"));assertFalse(MeaningFoundationLesson.advance(q,d,"888"));
            state=cold(LearningSnapshot.capture(state));q=state.session.question;d=state.session.conceptHelp.meaningFoundation;assertEquals("888",MeaningFoundationLesson.state(q,d).entry());byte[] wrong=bytes(d);assertEquals(prefix+"-scale",MeaningFoundationLesson.previousPicture(q,d).scene().id());assertEquals(prefix+"-one-side",MeaningFoundationLesson.counterexamplePicture(q,d).scene().id());assertArrayEquals(wrong,bytes(d));
            while(!MeaningFoundationLesson.state(q,d).complete())solve(q,d);assertArrayEquals(baseline,withoutChild(state));assertTrue(Review.track(state.progress(q.skillId)).pending);assertEquals(0,Review.track(state.progress(q.skillId)).recoveryChecks);assertEquals(0,state.session.completed);assertTrue(state.history.isEmpty());assertEquals("777",state.session.answers.get(0));assertEquals("888",state.session.conceptHelp.entries.get(2));assertTrue(MeaningFoundationLesson.state(q,cold(d)).complete());
        }
    }
    @Test public void deferredHelpDeepCopyKeepsChildPartialAndReturnsToTheUnchangedNewQuestion()throws Exception {
        Question original=question("mulIntro","3씩 4묶음은 모두 얼마인가요?");Learning.State state=state(original);Learning.Session s=state.session;var d=s.conceptHelp.meaningFoundation;solve(original,d);assertTrue(MeaningFoundationLesson.mark(original,d,0));Deferred.rememberSkip(s);
        Question next=question("mulIntro","2씩 5묶음은 모두 얼마인가요?");s.question=next;s.answers=new ArrayList<>(List.of("666"));s.steps=new ArrayList<>(List.of("new question work"));s.stepKinds=new ArrayList<>(List.of(Checker.StepKind.PARTIAL));s.conceptHelp=new HelpPlan.Draft();s.conceptHelp.questionId=next.id;s.conceptHelp.entries.add("555");
        Deferred.open(s,original.id);assertEquals(List.of("777"),s.answers);assertEquals(List.of(0),MeaningFoundationLesson.state(original,s.conceptHelp.meaningFoundation).selected());assertTrue(MeaningFoundationLesson.mark(original,s.conceptHelp.meaningFoundation,1));Deferred.leave(s);
        assertEquals(next.id,s.question.id);assertEquals(List.of("666"),s.answers);assertEquals(List.of("new question work"),s.steps);assertEquals(List.of("555"),s.conceptHelp.entries);assertNull(s.conceptHelp.meaningFoundation);
        Deferred.open(s,original.id);assertEquals(List.of(0,1),MeaningFoundationLesson.state(original,s.conceptHelp.meaningFoundation).selected());assertEquals(List.of("old1","old2","888"),s.conceptHelp.entries);
    }
    @Test public void actualUidOneStreamWithNoMeaningFieldRestoresNullAndPreservesOldInputs()throws Exception {
        // Synthetic pre-field UID1 descriptor with only the four old fields, not a claimed device save.
        String legacy="rO0ABXNyACdjb20uZ29tZ29tYXBwcy5tYXRoLmNvcmUuSGVscFBsYW4kRHJhZnQAAAAAAAAAAQIABEkABXN0YWdlTAAHZW50cmllc3QAEExqYXZhL3V0aWwvTGlzdDtMAApxdWVzdGlvbklkdAASTGphdmEvbGFuZy9TdHJpbmc7TAAPdGVhY2hpbmdWZXJzaW9ucQB+AAJ4cAAAAAJzcgATamF2YS51dGlsLkFycmF5TGlzdHiB0h2Zx2GdAwABSQAEc2l6ZXhwAAAAA3cEAAAAA3QAAjEydAABM3QAAzg4OHh0AA9sZWdhY3ktcXVlc3Rpb250AA5sZWdhY3ktaGVscC12MQ==";
        byte[] old=Base64.getDecoder().decode(legacy);assertFalse(new String(old,java.nio.charset.StandardCharsets.ISO_8859_1).contains("meaningFoundation"));HelpPlan.Draft loaded;
        try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(old))){loaded=(HelpPlan.Draft)in.readObject();}
        assertEquals(1L,ObjectStreamClass.lookup(HelpPlan.Draft.class).getSerialVersionUID());assertEquals(1L,ObjectStreamClass.lookup(MeaningFoundationLesson.Draft.class).getSerialVersionUID());assertNull(loaded.meaningFoundation);assertNull(loaded.copy().meaningFoundation);assertEquals("legacy-question",loaded.questionId);assertEquals("legacy-help-v1",loaded.teachingVersion);assertEquals(2,loaded.stage);assertEquals(List.of("12","3","888"),loaded.entries);
        Learning.State state=state(question("mulIntro","3씩 4묶음은 모두 얼마인가요?"));state.session.conceptHelp=loaded;Learning.State restored=cold(LearningSnapshot.capture(state));assertNull(restored.session.conceptHelp.meaningFoundation);assertEquals(loaded.entries,restored.session.conceptHelp.entries);
    }
    @Test public void unsupportedOrMismatchedDraftNeverOverwritesTheParentOrItsPreviousChild()throws Exception {
        Question q=question("mulIntro","3씩 4묶음은 모두 얼마인가요?");Learning.State state=state(q);var d=state.session.conceptHelp.meaningFoundation;solve(q,d);assertTrue(MeaningFoundationLesson.mark(q,d,0));byte[] before=bytes(state);
        Question other=question("mulIntro",q.prompt);assertNull(MeaningFoundationLesson.restore(other,d));assertNull(MeaningFoundationLesson.model(question("add9","2 + 3")));assertArrayEquals(before,bytes(state));assertSame(d,state.session.conceptHelp.meaningFoundation);
        var copy=state.session.conceptHelp.copy();copy.meaningFoundation.actions.add("forged-completion");assertNull(MeaningFoundationLesson.state(q,copy.meaningFoundation));assertNotNull(MeaningFoundationLesson.state(q,d));assertArrayEquals(before,bytes(state));
    }
}
