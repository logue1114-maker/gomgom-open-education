package com.gomgomapps.math.core;

import java.io.*;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class DiagramQuestionHistoryTest {
    private final LocalDate day=LocalDate.of(2026,9,17);
    private final Random random=new Random(917);
    private final Generator generator=new Generator(random);
    private Learning.State restore(Learning.State state)throws Exception{
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();
        new ObjectOutputStream(bytes).writeObject(LearningSnapshot.capture(state));
        return (Learning.State)new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray())).readObject();
    }
    @Test public void hundredClockQuestionsUseAllHoursWithoutAdjacentRepeatsAcrossRestore()throws Exception{
        Learning.State state=new Learning.State();Learning.beginPractice(state,"practice",List.of("earlyClock"),100,false,random);
        Set<Double> hours=new HashSet<>();Double last=null;
        for(int i=0;i<100;i++){
            if(i==37)state=restore(state);
            Question q=Learning.ensureQuestion(state,generator,day.plusDays(i/37));
            assertNotEquals(last,Double.valueOf(q.diagram.values[0]));last=q.diagram.values[0];hours.add(last);
            Learning.finishQuestion(state,false,day.plusDays(i/37),random);
        }
        assertEquals(12,hours.size());assertEquals(12,QuestionHistory.recent(state,"earlyClock").size());
        assertEquals(100,state.session.completed);
    }
    @Test public void lineRotationAndChoiceOrderDoNotCreateNewLearningEvidence(){
        Question a=generator.next("linePairs",List.of(),true);
        String key=a.signature();a.diagram.values[1]+=5;Collections.reverse(a.choices);
        assertEquals(key,a.signature());
        a.diagram.values[0]=(a.diagram.values[0]+1)%3;assertNotEquals(key,a.signature());
    }
    @Test public void boxplotGivensSupportHundredDistinctQuestions(){
        Learning.State state=new Learning.State();Learning.beginPractice(state,"practice",List.of("boxplotRead"),100,false,random);
        Set<String> pictures=new HashSet<>();
        for(int i=0;i<100;i++){
            Question q=Learning.ensureQuestion(state,generator,day);
            assertTrue(pictures.add(q.prompt+Arrays.toString(q.diagram.values)));
            Learning.finishQuestion(state,false,day,random);
        }
    }
    @Test public void oldPublishedPictureAndWorkSurviveAndNextPictureDiffers()throws Exception{
        Learning.State state=new Learning.State();Learning.beginPractice(state,"practice",List.of("earlyClock"),100,false,random);
        Question q=Learning.ensureQuestion(state,generator,day);
        // Absent primitive fields deserialize as zero in pre-change Java records.
        Field version=Question.class.getDeclaredField("signatureVersion");version.setAccessible(true);version.setInt(q,0);
        String oldKey=q.skillId+"|"+q.prompt;state.recent.clear();state.recent.add(oldKey);
        state.progress(q.skillId).recentQuestions.clear();state.progress(q.skillId).recentQuestions.add(oldKey);
        state.session.answers.set(0,"7");String id=q.id;double hour=q.diagram.values[0];
        state=restore(state);q=Learning.ensureQuestion(state,generator,day.plusDays(1));
        assertEquals(oldKey,q.signature());assertEquals(id,q.id);assertEquals("7",state.session.answers.get(0));
        assertEquals(hour,q.diagram.values[0],0);Learning.finishQuestion(state,false,day.plusDays(1),random);
        assertNotEquals(hour,Learning.ensureQuestion(state,generator,day.plusDays(1)).diagram.values[0],0);
        assertEquals(1,state.session.completed);
    }
    @Test public void ambiguousOldReviewRequiresNewDistinctEvidenceAndKeepsTotals()throws Exception{
        Learning.State state=new Learning.State();Learning.beginPractice(state,"practice",List.of("earlyClock"),100,false,random);
        Learning.Progress p=state.progress("earlyClock");p.attempts=42;p.corrected=4;
        Review.Track t=Review.track(p);t.pending=true;t.practice=true;t.recoveryChecks=1;
        t.failed.add("earlyClock|몇 시인가요?");t.checked.add("earlyClock|몇 시인가요?");
        Learning.ensureQuestion(state,generator,day);assertFalse(state.session.reviewWork.recheck);
        assertEquals(0,t.recoveryChecks);assertTrue(t.practice);assertEquals(42,p.attempts);
        Learning.finishQuestion(state,false,day,random);state=restore(state);
        assertEquals(43,state.progress("earlyClock").attempts);assertEquals(4,state.progress("earlyClock").corrected);
        Learning.ensureQuestion(state,generator,day.plusDays(1));assertTrue(state.session.reviewWork.recheck);
        Learning.finishQuestion(state,false,day.plusDays(1),random);assertTrue(state.progress("earlyClock").weak());
    }
    @Test public void oldDiagnosticPictureDoesNotCompleteAProbeWithOneNewAnswer(){
        Learning.State state=new Learning.State();Learning.beginPractice(state,"practice",List.of("earlyClock"),100,false,random);
        Diagnosis.Plan plan=new Diagnosis.Plan();Diagnosis.Probe probe=new Diagnosis.Probe("earlyClock",1,false);
        probe.correctRun=1;probe.independent=1;probe.seen.add("earlyClock|몇 시인가요?");
        Diagnosis.Run run=new Diagnosis.Run(plan,false,4);state.session.diagnosticRun=run;
        run.scheduled.add(probe); // Keep this probe open while checking evidence accumulation.
        for(int hour=1;hour<=3;hour++){
            Question q=new Question("earlyClock","몇 시인가요?","",String.valueOf(hour));
            q.diagram=new StudyDiagram("clock",new double[]{hour,0});state.session.question=q;run.current=probe;
            Diagnosis.record(state,state.session,true,false,false,random);
            assertEquals(hour-1,probe.correctRun);
        }
        assertEquals(3,probe.independent); // One preserved old total plus two new independent pictures.
    }
}
