package com.gomgomapps.math.core;

import org.junit.Test;
import static org.junit.Assert.*;
import java.io.*;
import java.time.LocalDate;
import java.util.*;

public class KoreanRightAngleLessonTest {
    private Question question(String kind,int known){
        String prompt=kind.equals(KoreanRightAngles.RECT)?"직사각형의 한 각은 몇 도인가요?":kind.equals(KoreanRightAngles.SPLIT)?
            "직각을 두 각으로 나누었습니다. 한 각이 "+known+"도일 때, 다른 각은 몇 도인가요?":"직각보다 "+known+"도 큰 각은 몇 도인가요?";
        return new Question(KoreanRightAngles.ID,prompt,"123456","999");
    }
    private HelpPlan.Draft draft(Question q){
        HelpPlan plan=HelpPlan.forQuestion(q);assertNotNull(plan);assertFalse(plan.canTransfer());
        HelpPlan.Draft d=plan.restore(null,q.id);assertNotNull(KoreanRightAngleLesson.restore(q,d));return d;
    }
    private void observe(Question q,HelpPlan.Draft d){
        assertTrue(KoreanRightAngleLesson.observe(q,d));assertTrue(KoreanRightAngleLesson.finishObservation(q,d));
        assertTrue(KoreanRightAngleLesson.advance(q,d,"ok"));
    }
    private void complete(Question q,HelpPlan.Draft d){
        KoreanRightAngles.Givens g=KoreanRightAngles.read(q);
        assertTrue(KoreanRightAngleLesson.advance(q,d,g.kind().equals(KoreanRightAngles.RECT)?"same":g.kind().equals(KoreanRightAngles.SPLIT)?"sumRight":"rightPlus"));
        if(!g.kind().equals(KoreanRightAngles.RECT))assertTrue(KoreanRightAngleLesson.advance(q,d,g.kind().equals(KoreanRightAngles.SPLIT)?"subtract":"add"));
        int result=g.kind().equals(KoreanRightAngles.RECT)?90:g.kind().equals(KoreanRightAngles.SPLIT)?90-g.known():90+g.known();
        assertTrue(KoreanRightAngleLesson.advance(q,d,Integer.toString(result)));assertTrue(KoreanRightAngleLesson.state(q,d).complete());
    }
    private byte[] bytes(Object object)throws Exception{
        ByteArrayOutputStream data=new ByteArrayOutputStream();try(ObjectOutputStream out=new ObjectOutputStream(data)){out.writeObject(object);}return data.toByteArray();
    }
    private Learning.State cold(Learning.State state)throws Exception{
        try(ObjectInputStream input=new ObjectInputStream(new ByteArrayInputStream(bytes(state)))){return (Learning.State)input.readObject();}
    }
    @Test public void publicThreeKindsAndEveryKnownAngleProduceIndependentCorrectRelationsWithoutMutation()throws Exception{
        for(String kind:List.of(KoreanRightAngles.RECT,KoreanRightAngles.SPLIT,KoreanRightAngles.LARGER))for(int known=1;known<=(kind.equals(KoreanRightAngles.LARGER)?90:89);known++){
            Question q=question(kind,known);q.studyGuide=new StudyGuide().step("cached","","","-999");byte[] before=bytes(q);
            KoreanRightAngles.Givens g=KoreanRightAngles.read(q);assertNotNull(g);assertEquals(kind,g.kind());assertEquals(kind.equals(KoreanRightAngles.RECT)?0:known,g.known());
            HelpPlan p=HelpPlan.forQuestion(q);assertEquals(kind.equals(KoreanRightAngles.RECT)?3:4,p.size());assertFalse(p.canTransfer());
            assertEquals(Set.of("same","sumRight","rightPlus"),p.step(1).options.keySet());
            if(p.size()==4)assertEquals(Set.of("given","subtract","add"),p.step(2).options.keySet());
            HelpPlan.Draft d=draft(q);observe(q,d);complete(q,d);assertArrayEquals(before,bytes(q));
        }
    }
    @Test public void unsupportedMalformedAndOutOfRangePublicSentencesFailClosedDespiteCachedKeys(){
        for(String prompt:List.of("직각보다 9999999999999999999999999999999도 큰 각은 몇 도인가요?","직각보다 -1도 큰 각은 몇 도인가요?",
            "직각보다 1.5도 큰 각은 몇 도인가요?","직사각형의 한 각은 몇 도인가요? 정답은 90")){
            Question q=new Question(KoreanRightAngles.ID,prompt,"90","90");q.studyGuide=new StudyGuide().step("saved","","","90");
            assertNull(KoreanRightAngles.read(q));assertNull(KoreanRightAngles.guide(q));assertNull(HelpPlan.forQuestion(q));
        }
        Question other=new Question("el_proportional_split","전체 35을 2:3로 비례배분한 두 부분은?","","14","21");
        assertNull(KoreanRightAngles.read(other));assertEquals(7,HelpPlan.forQuestion(other).size());
        assertNull(KoreanRightAngles.read(null));assertFalse(KoreanRightAngles.supports(null));
    }
    @Test public void straightAngleIsIllustratedAndLegacyPublicNumericAnglesRemainAvailableWithoutCachedAuthority()throws Exception{
        Question straight=question(KoreanRightAngles.LARGER,90);assertEquals(90,KoreanRightAngles.read(straight).known());
        HelpPlan.Draft d=draft(straight);observe(straight,d);complete(straight,d);assertTrue(HelpPlan.forQuestion(straight).step(3).accepts("180"));
        for(String known:List.of("0","91","999999999999","0001")){
            Question q=new Question(KoreanRightAngles.ID,"직각보다 "+known+"도 큰 각은 몇 도인가요?","private expression","-555");
            q.studyGuide=new StudyGuide().step("cached","","","-555");byte[] before=bytes(q);
            assertNull(KoreanRightAngles.read(q));StudyGuide legacy=ElementarySplitAngleRelations.guide(q);assertNotNull(legacy);
            assertEquals(KoreanRightAngles.OLD_VERSION,legacy.teachingVersion);assertEquals(2,legacy.frames.size());assertFalse(legacy.transfer);
            Rational original=Expression.number(known),result=Rational.of(90).add(original);assertEquals(original.toString(),legacy.frames.get(0).expected);assertEquals(result.toString(),legacy.frames.get(1).expected);
            HelpPlan plan=HelpPlan.forQuestion(q);assertNotNull(plan);assertEquals(2,plan.size());assertFalse(plan.canTransfer());assertTrue(plan.step(0).accepts(known));assertTrue(plan.step(1).accepts(result.toString()));
            HelpPlan.Draft old=new HelpPlan.Draft();old.questionId=q.id;old.teachingVersion=KoreanRightAngles.OLD_VERSION;old.stage=1;old.entries=new ArrayList<>(List.of(known,"888"));
            plan.restore(old,q.id);assertEquals(1,old.stage);assertEquals("888",old.entries.get(1));assertTrue(old.oldAngleEntries.isEmpty());assertNull(KoreanRightAngleLesson.restore(q,old));
            assertArrayEquals(before,bytes(q));
        }
    }
    @Test public void observationRequestRequiresRealCallbackAndCannotBeForgedByACompletionEntry()throws Exception{
        Question q=question(KoreanRightAngles.SPLIT,37);HelpPlan.Draft d=draft(q);
        assertFalse(KoreanRightAngleLesson.finishObservation(q,d));assertFalse(KoreanRightAngleLesson.advance(q,d,"ok"));assertEquals(0,d.stage);
        assertTrue(KoreanRightAngleLesson.observe(q,d));assertTrue(KoreanRightAngleLesson.pending(q,d));assertFalse(KoreanRightAngleLesson.state(q,d).observed());
        assertFalse(KoreanRightAngleLesson.advance(q,d,"ok"));int length=d.rightAngleActions.size();assertTrue(KoreanRightAngleLesson.observe(q,d));assertEquals(length,d.rightAngleActions.size());
        HelpPlan.Draft forged=d.copy();forged.rightAngleActions=new ArrayList<>(List.of("answer:ok"));forged.stage=1;forged.entries=new ArrayList<>(List.of("ok",""));assertNull(KoreanRightAngleLesson.state(q,forged));
        assertTrue(KoreanRightAngleLesson.finishObservation(q,d));assertFalse(KoreanRightAngleLesson.finishObservation(q,d));assertTrue(KoreanRightAngleLesson.advance(q,d,"ok"));
        assertFalse(KoreanRightAngleLesson.observe(q,d));assertEquals(1,d.stage);
    }
    @Test public void wrongSelectionAndUnsubmittedNumericTextRestoreThroughSnapshotAndSerialization()throws Exception{
        Question q=question(KoreanRightAngles.LARGER,24);Learning.State state=new Learning.State();state.session=new Learning.Session();state.session.mode="practice";state.session.question=q;
        state.session.conceptHelp=draft(q);HelpPlan.Draft d=state.session.conceptHelp;assertTrue(KoreanRightAngleLesson.observe(q,d));
        state=cold(LearningSnapshot.capture(state));q=state.session.question;d=state.session.conceptHelp;
        assertTrue(KoreanRightAngleLesson.pending(q,d));assertFalse(KoreanRightAngleLesson.advance(q,d,"ok"));
        assertTrue(KoreanRightAngleLesson.observe(q,d));assertTrue(KoreanRightAngleLesson.finishObservation(q,d));assertTrue(KoreanRightAngleLesson.advance(q,d,"ok"));
        assertFalse(KoreanRightAngleLesson.advance(q,d,"sumRight"));assertEquals(1,d.stage);assertEquals("sumRight",KoreanRightAngleLesson.state(q,d).entry());
        state=cold(state);q=state.session.question;d=HelpPlan.forQuestion(q).restore(state.session.conceptHelp,q.id);assertNotNull(KoreanRightAngleLesson.restore(q,d));assertEquals("sumRight",KoreanRightAngleLesson.state(q,d).entry());
        assertTrue(KoreanRightAngleLesson.advance(q,d,"rightPlus"));assertFalse(KoreanRightAngleLesson.advance(q,d,"subtract"));assertEquals("subtract",KoreanRightAngleLesson.state(q,d).entry());
        assertTrue(KoreanRightAngleLesson.advance(q,d,"add"));d.entries.set(d.stage,"888");
        state=cold(LearningSnapshot.capture(state));q=state.session.question;d=HelpPlan.forQuestion(q).restore(state.session.conceptHelp,q.id);assertNotNull(KoreanRightAngleLesson.restore(q,d));
        assertEquals(3,d.stage);assertEquals("888",KoreanRightAngleLesson.state(q,d).entry());assertFalse(KoreanRightAngleLesson.advance(q,d,"888"));assertEquals(3,d.stage);
        assertTrue(KoreanRightAngleLesson.advance(q,d,"114"));assertTrue(KoreanRightAngleLesson.state(q,d).complete());
    }
    @Test public void legacyVersionAndNullVersionArchiveOnlyWhenEnteringThisNewAngleLesson(){
        Question q=question(KoreanRightAngles.SPLIT,33);HelpPlan p=HelpPlan.forQuestion(q);
        for(String version:Arrays.asList(KoreanRightAngles.OLD_VERSION,null)){
            HelpPlan.Draft d=new HelpPlan.Draft();d.questionId=q.id;d.teachingVersion=version;d.stage=1;d.entries=new ArrayList<>(List.of("33","888"));
            p.restore(d,q.id);assertEquals(KoreanRightAngles.VERSION,d.teachingVersion);assertEquals(0,d.stage);assertEquals(List.of(""),d.entries);
            assertEquals(List.of("33","888"),d.oldAngleEntries);assertEquals(1,d.oldAngleStage);assertEquals(version,d.oldAngleTeachingVersion);
            assertNotNull(KoreanRightAngleLesson.restore(q,d));HelpPlan.Draft copy=d.copy();copy.oldAngleEntries.set(0,"changed");assertEquals("33",d.oldAngleEntries.get(0));
            p.restore(d,q.id);assertEquals(List.of("33","888"),d.oldAngleEntries);
        }
        Question other=new Question("el_proportional_split","전체 35을 2:3로 비례배분한 두 부분은?","","14","21");
        HelpPlan.Draft old=new HelpPlan.Draft();old.questionId=other.id;old.teachingVersion=KoreanRightAngles.OLD_VERSION;old.entries.add("35");
        HelpPlan.forQuestion(other).restore(old,other.id);assertTrue(old.oldAngleEntries.isEmpty());assertNull(old.oldAngleTeachingVersion);
        HelpPlan.Draft wrongQuestion=new HelpPlan.Draft();wrongQuestion.questionId="another";wrongQuestion.teachingVersion=KoreanRightAngles.OLD_VERSION;wrongQuestion.entries.add("77");
        assertTrue(p.restore(wrongQuestion,q.id).oldAngleEntries.isEmpty());assertEquals(1L,ObjectStreamClass.lookup(HelpPlan.Draft.class).getSerialVersionUID());
    }
    @Test public void sourceVersionQuestionAndCommittedInputTamperingCannotReuseObservationProof(){
        Question q=question(KoreanRightAngles.SPLIT,30);HelpPlan.Draft d=draft(q);observe(q,d);
        HelpPlan.Draft changed=d.copy();changed.questionId="other";assertNull(KoreanRightAngleLesson.state(q,changed));assertNull(KoreanRightAngleLesson.restore(q,changed));
        changed=d.copy();changed.teachingVersion="old";assertNull(KoreanRightAngleLesson.state(q,changed));
        changed=d.copy();changed.entries.set(0,"wrong");assertNull(KoreanRightAngleLesson.state(q,changed));
        changed=d.copy();changed.rightAngleActions.add("view:finish");assertNull(KoreanRightAngleLesson.state(q,changed));
        String oldPrompt=q.prompt;q.prompt=question(KoreanRightAngles.SPLIT,31).prompt;assertNull(KoreanRightAngleLesson.state(q,d));assertNull(KoreanRightAngleLesson.restore(q,d));q.prompt=oldPrompt;
        changed=d.copy();changed.stage=4;assertNull(KoreanRightAngleLesson.state(q,changed));
        changed=d.copy();changed.rightAngleActions=new ArrayList<>(Collections.nCopies(65,"view:request"));assertNull(KoreanRightAngleLesson.state(q,changed));
    }
    @Test public void repeatedViewRequestsAreBoundedAndExplicitRestartPreservesOldArchive(){
        Question q=question(KoreanRightAngles.RECT,0);HelpPlan.Draft d=draft(q);d.oldAngleEntries.add("legacy888");d.oldAngleStage=1;d.oldAngleTeachingVersion=KoreanRightAngles.OLD_VERSION;
        for(int i=0;i<350;i++){assertTrue(KoreanRightAngleLesson.observe(q,d));assertTrue(KoreanRightAngleLesson.finishObservation(q,d));assertTrue(d.rightAngleActions.size()<=3);}
        assertTrue(KoreanRightAngleLesson.advance(q,d,"ok"));complete(q,d);HelpPlan.forQuestion(q).restore(d,q.id);assertTrue(KoreanRightAngleLesson.state(q,d).complete());assertFalse(KoreanRightAngleLesson.advance(q,d,"90"));
        HelpPlan.Draft copy=d.copy();copy.rightAngleActions.clear();assertFalse(d.rightAngleActions.isEmpty());assertTrue(KoreanRightAngleLesson.restart(q,d));
        assertEquals(0,d.stage);assertEquals(List.of(""),d.entries);assertEquals(List.of("legacy888"),d.oldAngleEntries);assertFalse(KoreanRightAngleLesson.state(q,d).observed());
    }
    @Test public void completingHelpDoesNotSubmitParentOrCreateReviewEvidenceAndFeedbackDoesNotFillTheAnswer()throws Exception{
        Learning.State state=new Learning.State();Learning.Session parent=new Learning.Session();state.session=parent;parent.mode="practice";parent.question=question(KoreanRightAngles.SPLIT,35);
        parent.answers.add("777");parent.steps.add("private work");parent.scratch.add(new float[]{1,2});parent.conceptHelp=draft(parent.question);Review.open(state,LocalDate.of(2026,10,15));
        byte[] review=bytes(state.progress(KoreanRightAngles.ID).review);long sequence=state.reviewSequence;Question q=parent.question;
        observe(q,parent.conceptHelp);assertFalse(KoreanRightAngleLesson.advance(q,parent.conceptHelp,"rightPlus"));String feedback=KoreanRightAngleLesson.feedback(q,parent.conceptHelp);assertNotNull(feedback);assertFalse(feedback.contains("55"));
        assertEquals("rightPlus",parent.conceptHelp.entries.get(1));assertTrue(KoreanRightAngleLesson.advance(q,parent.conceptHelp,"sumRight"));assertTrue(KoreanRightAngleLesson.advance(q,parent.conceptHelp,"subtract"));assertTrue(KoreanRightAngleLesson.advance(q,parent.conceptHelp,"55"));
        assertEquals(List.of("777"),parent.answers);assertEquals(List.of("private work"),parent.steps);assertArrayEquals(new float[]{1,2},parent.scratch.get(0),0f);assertEquals(0,parent.completed);assertTrue(state.history.isEmpty());assertTrue(state.daily.isEmpty());assertEquals(sequence,state.reviewSequence);assertArrayEquals(review,bytes(state.progress(KoreanRightAngles.ID).review));
        assertNull(KoreanRightAngles.feedback(q,1,"not an option"));assertNull(KoreanRightAngles.feedback(q,3,"expression()"));assertNull(KoreanRightAngles.feedback(q,3,"55"));
        Learning.State copy=LearningSnapshot.capture(state);copy.session.conceptHelp.oldAngleEntries.add("only copy");assertTrue(parent.conceptHelp.oldAngleEntries.isEmpty());
    }
}
