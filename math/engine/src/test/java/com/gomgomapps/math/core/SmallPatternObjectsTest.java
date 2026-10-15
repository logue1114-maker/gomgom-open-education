package com.gomgomapps.math.core;

import org.junit.Test;
import static org.junit.Assert.*;
import java.time.LocalDate;
import java.util.*;

public class SmallPatternObjectsTest {
    private static final LocalDate DAY=LocalDate.of(2026,10,15);
    private Question question(int first,int delta,int blank){
        List<String> terms=new ArrayList<>();
        for(int i=0;i<5;i++)terms.add(i==blank?"□":Integer.toString(first+i*delta));
        return new Question("el_number_pattern",String.join(" → ",terms)+"\n수의 규칙을 찾아 빈 수를 구하세요.","999","999");
    }
    private HelpPlan.Draft draft(Question q){
        HelpPlan.Draft draft=new HelpPlan.Draft();draft.questionId=q.id;draft.teachingVersion="existing-lesson";
        draft.stage=1;draft.entries.addAll(List.of("888","777"));return draft;
    }
    private void markAll(Question q,HelpPlan.Draft draft,int count){
        for(int i=count-1;i>=0;i--)assertTrue(SmallPatternObjects.mark(q,draft,i));
        assertTrue(SmallPatternObjects.state(q,draft).ready());
    }
    private void finish(Question q,HelpPlan.Draft draft){
        var g=SmallPatternObjects.read(q);
        for(int count:List.of(g.first(),g.second(),g.delta())){
            markAll(q,draft,count);assertTrue(SmallPatternObjects.next(q,draft));
        }
    }

    @Test public void everySmallVisibleArrayIsReconstructedDespitePoisonedCachedAnswers(){
        for(int first=1;first<=20;first++)for(int delta=1;delta<=4;delta++)if(first+4*delta<=20)
            for(int blank=2;blank<=4;blank++){
                Question q=question(first,delta,blank);q.answers=null;q.expression="-1000";q.kind="complexRoots";
                StudyGuide cache=new StudyGuide().step("private","","","0");q.studyGuide=cache;
                var g=SmallPatternObjects.read(q);assertNotNull(g);assertEquals(first,g.first());
                assertEquals(first+delta,g.second());assertEquals(delta,g.delta());assertEquals(blank,g.blank());
                for(int i=0;i<5;i++)assertEquals(Integer.valueOf(first+i*delta),g.terms().get(i));
                assertNull(q.answers);assertSame(cache,q.studyGuide);assertEquals("-1000",q.expression);
                try{g.terms().set(0,99);fail("public originals must be immutable");}catch(UnsupportedOperationException expected){}
            }
    }

    @Test public void unsupportedLargeDecreasingOrInconsistentPublicSourcesStayOutsideTheTool(){
        for(Question q:List.of(question(1,1,0),question(1,1,1),question(5,4,2),question(1,5,2),question(10,-1,2),question(1,0,2)))
            assertNull(q.prompt,SmallPatternObjects.read(q));
        Question q=question(1,4,2);String original=q.prompt;
        for(String bad:List.of(original+"\n",original+" ",original.replace("13","14"),
                original.replace("1 → 5","□ → 5"),original.replace("1 → 5","0 → 5"),
                original.replace("수의 규칙을 찾아 빈 수를 구하세요.","다른 문제"),original.replace(" → ","→"))){
            q.prompt=bad;assertNull(bad,SmallPatternObjects.read(q));
        }
        q.prompt=original;q.skillId=KoreanRulesRelations.NUMBER;assertNull(SmallPatternObjects.read(q));
        q.skillId="el_number_pattern";q.prompt=null;assertNull(SmallPatternObjects.read(q));assertNull(SmallPatternObjects.read(null));
    }

    @Test public void eachObjectMustBeMarkedOnceBeforeMovingToTheNextGroup(){
        Question q=question(2,2,2);HelpPlan.Draft draft=draft(q);
        assertTrue(SmallPatternObjects.open(q,draft));assertFalse(SmallPatternObjects.next(q,draft));
        assertFalse(SmallPatternObjects.mark(q,draft,-1));assertFalse(SmallPatternObjects.mark(q,draft,2));
        assertTrue(SmallPatternObjects.mark(q,draft,0));List<String> once=List.copyOf(draft.patternObjectActions);
        assertFalse(SmallPatternObjects.mark(q,draft,0));assertFalse(SmallPatternObjects.next(q,draft));
        assertEquals(once,draft.patternObjectActions);assertEquals(List.of(0),SmallPatternObjects.state(q,draft).marked());
        assertTrue(SmallPatternObjects.mark(q,draft,1));assertTrue(SmallPatternObjects.next(q,draft));
        var second=SmallPatternObjects.state(q,draft);assertEquals(1,second.phase());assertTrue(second.marked().isEmpty());assertFalse(second.ready());
        markAll(q,draft,4);assertTrue(SmallPatternObjects.next(q,draft));
        assertEquals(2,SmallPatternObjects.state(q,draft).phase());assertFalse(SmallPatternObjects.mark(q,draft,2));
        markAll(q,draft,2);assertTrue(SmallPatternObjects.next(q,draft));assertTrue(SmallPatternObjects.state(q,draft).complete());
        assertFalse(SmallPatternObjects.mark(q,draft,0));assertFalse(SmallPatternObjects.next(q,draft));
        try{second.marked().add(3);fail("immutable replay result");}catch(UnsupportedOperationException expected){}
    }

    @Test public void completedPairComparisonNeverFillsOrAdvancesTheOriginalWrongLesson(){
        Question q=question(1,4,2);HelpPlan.Draft draft=draft(q);String prompt=q.prompt;
        String[] answers=q.answers;StudyGuide cached=new StudyGuide().step("private","","","999");q.studyGuide=cached;
        assertTrue(SmallPatternObjects.open(q,draft));finish(q,draft);
        assertEquals(3,SmallPatternObjects.state(q,draft).phase());assertTrue(SmallPatternObjects.state(q,draft).complete());
        assertEquals(List.of("888","777"),draft.entries);assertEquals(1,draft.stage);
        assertEquals("existing-lesson",draft.teachingVersion);assertEquals(q.id,draft.questionId);
        assertSame(answers,q.answers);assertArrayEquals(new String[]{"999"},q.answers);assertSame(cached,q.studyGuide);assertEquals(prompt,q.prompt);
        SmallPatternObjects.close(q,draft);assertFalse(draft.patternObjectsOpen);assertTrue(SmallPatternObjects.state(q,draft).complete());
        assertTrue(SmallPatternObjects.open(q,draft));assertTrue(SmallPatternObjects.state(q,draft).complete());
    }

    @Test public void coldSnapshotAndDraftCopyKeepActionsSeparateWithoutCreatingReviewOrSubmission(){
        Learning.State state=new Learning.State();Random random=new Random(387);
        Learning.beginPractice(state,"practice",List.of("el_number_pattern"),20,false,random);
        Question q=question(1,2,2);state.session.question=q;state.session.answers.add("555");
        Review.helpUsed(state,DAY);state.session.conceptHelp=draft(q);HelpPlan.Draft draft=state.session.conceptHelp;
        long sequence=state.reviewSequence;int events=Review.track(state.progress(q.skillId)).events.size();
        int attempts=state.progress(q.skillId).attempts;Map<String,Integer> daily=new LinkedHashMap<>(state.daily);
        assertTrue(SmallPatternObjects.open(q,draft));markAll(q,draft,1);assertTrue(SmallPatternObjects.next(q,draft));
        assertTrue(SmallPatternObjects.mark(q,draft,1));
        var before=SmallPatternObjects.state(q,draft);HelpPlan.Draft copied=draft.copy();
        assertEquals(before,SmallPatternObjects.state(q,copied));assertNotSame(draft.patternObjectActions,copied.patternObjectActions);
        assertTrue(SmallPatternObjects.mark(q,copied,0));assertEquals(List.of(1),SmallPatternObjects.state(q,draft).marked());
        Learning.State cold=LearningSnapshot.capture(state);HelpPlan.Draft restored=cold.session.conceptHelp;
        assertEquals(before,SmallPatternObjects.state(cold.session.question,restored));assertTrue(restored.patternObjectsOpen);
        assertNotSame(draft.patternObjectActions,restored.patternObjectActions);
        assertTrue(SmallPatternObjects.mark(cold.session.question,restored,0));
        assertEquals(before,SmallPatternObjects.state(q,draft));assertEquals(List.of("555"),cold.session.answers);
        assertEquals(List.of("888","777"),restored.entries);assertEquals(1,restored.stage);
        assertEquals(sequence,cold.reviewSequence);assertEquals(events,Review.track(cold.progress(q.skillId)).events.size());
        assertEquals(attempts,cold.progress(q.skillId).attempts);assertEquals(daily,cold.daily);
        assertEquals(0,cold.session.completed);assertTrue(cold.history.isEmpty());
    }

    @Test public void invalidTraceQuestionPromptOrVersionCannotSupplyCompletion(){
        Question q=question(1,1,2);
        List<List<String>> invalid=new ArrayList<>(List.of(List.of("next"),List.of("m:0","m:0"),
                List.of("m:1"),List.of("m:00"),List.of("m:-1"),List.of("done"),List.of("m:0","next","next")));
        invalid.add(Arrays.asList((String)null));invalid.add(Collections.nCopies(65,"m:0"));
        for(List<String> trace:invalid){
            HelpPlan.Draft draft=draft(q);assertTrue(SmallPatternObjects.open(q,draft));
            draft.patternObjectActions=new ArrayList<>(trace);List<String> unchanged=new ArrayList<>(trace);
            assertNull(SmallPatternObjects.state(q,draft));assertEquals(unchanged,draft.patternObjectActions);
            assertFalse(SmallPatternObjects.open(q,draft));assertFalse(draft.patternObjectsOpen);
            assertEquals(0,SmallPatternObjects.state(q,draft).phase());assertTrue(draft.patternObjectActions.isEmpty());
            assertEquals(List.of("888","777"),draft.entries);
        }
        HelpPlan.Draft draft=draft(q);assertTrue(SmallPatternObjects.open(q,draft));finish(q,draft);
        Question another=question(1,1,2);assertNull(SmallPatternObjects.state(another,draft));
        SmallPatternObjects.restore(another,draft);assertFalse(draft.patternObjectsOpen);assertFalse(SmallPatternObjects.state(another,draft).complete());
        assertTrue(SmallPatternObjects.open(another,draft));draft.patternObjectsScope="old-version|"+draft.patternObjectsScope;
        assertNull(SmallPatternObjects.state(another,draft));SmallPatternObjects.restore(another,draft);assertTrue(draft.patternObjectActions.isEmpty());
        assertTrue(SmallPatternObjects.open(another,draft));assertTrue(SmallPatternObjects.mark(another,draft,0));
        another.prompt=question(2,1,2).prompt;assertNull(SmallPatternObjects.state(another,draft));
        SmallPatternObjects.restore(another,draft);assertEquals(0,SmallPatternObjects.state(another,draft).phase());assertFalse(draft.patternObjectsOpen);
    }

    @Test public void cancelReopenAndExplicitRestartChangeOnlyTheOptionalTool(){
        Question q=question(2,1,4);HelpPlan.Draft draft=draft(q);
        assertTrue(SmallPatternObjects.open(q,draft));assertTrue(SmallPatternObjects.mark(q,draft,1));
        var before=SmallPatternObjects.state(q,draft);SmallPatternObjects.close(q,draft);
        assertFalse(SmallPatternObjects.mark(q,draft,0));assertFalse(SmallPatternObjects.next(q,draft));
        assertEquals(before,SmallPatternObjects.state(q,draft));assertTrue(SmallPatternObjects.open(q,draft));
        assertEquals(before,SmallPatternObjects.state(q,draft));assertTrue(SmallPatternObjects.restart(q,draft));
        assertEquals(0,SmallPatternObjects.state(q,draft).phase());assertTrue(SmallPatternObjects.state(q,draft).marked().isEmpty());
        assertTrue(draft.patternObjectsOpen);assertEquals(1,draft.stage);assertEquals(List.of("888","777"),draft.entries);
        q.prompt="unsupported";assertFalse(SmallPatternObjects.open(q,draft));assertFalse(draft.patternObjectsOpen);
        assertNull(SmallPatternObjects.state(q,draft));assertTrue(draft.patternObjectActions.isEmpty());assertEquals(List.of("888","777"),draft.entries);
    }
}
