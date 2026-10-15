package com.gomgomapps.math.core;

import org.junit.Test;
import static org.junit.Assert.*;
import java.io.*;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Consumer;

public class KoreanFoundationDetourTest {
    private static final LocalDate DAY=LocalDate.of(2026,10,15);
    private Learning.State student(int version){
        Learning.State state=new Learning.State();state.profile.grade=6;state.profile.term=2;
        state.profile.curriculum=version;state.profile.schoolYear=2026;state.profile.ready=true;state.profile.foundationAscending=true;
        state.profile.lastLearnedSkill="unit:KR|"+version+"|6|2|5";return state;
    }
    private Question next(Learning.State state,Random random){return Learning.ensureQuestion(state,new Generator(random),DAY);}
    private void finish(Learning.State state,Random random,boolean help){
        Question q=next(state,random);if(help)Review.helpUsed(state,DAY);
        // Only a unit fixture: published answers exercise the normal Checker/finish boundary.
        state.session.answers.clear();state.session.answers.addAll(Arrays.asList(q.answers));
        assertTrue(new Checker().check(q,state.session.steps,state.session.answers,state.session.stepKinds).correct());
        Learning.finishQuestion(state,false,DAY,random);
    }
    private Learning.State original(int version){
        Learning.State state=student(version);Random random=new Random(39201);Learning.beginDiagnostic(state,random);
        Diagnosis.Plan plan=Diagnosis.currentPlan(state);Diagnosis.UnitScreen screen=Diagnosis.screens(plan).stream().filter(u->u.representatives.contains("add9")).findFirst().orElseThrow();
        KoreanFoundationStudy.flagged(state,screen,"add9","unable",DAY);
        for(String id:List.of("count","join9")){Review.Track t=Review.track(state.progress(id));t.recent=true;t.pending=false;t.cleanRun=2;}
        Diagnosis.beginStudy(state,screen,random);Question q=next(state,random);assertEquals("add9",q.skillId);
        Learning.Session parent=state.session;parent.answers.set(0,"888");parent.steps.add("1+1=3");parent.stepKinds.add(Checker.StepKind.FULL);
        parent.scratch.add(new float[]{1,2,3});Learning.markError(state,DAY);Review.helpUsed(state,DAY);
        HelpPlan help=HelpPlan.forFoundation(q);assertNotNull(help);parent.conceptHelp=help.restore(null,q.id);parent.conceptHelp.entries.set(0,"999");
        return state;
    }
    private Learning.State cold(Learning.State state)throws Exception{
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();try(ObjectOutputStream out=new ObjectOutputStream(bytes)){out.writeObject(state);}
        try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))){return (Learning.State)in.readObject();}
    }
    @Test public void twoRealPrerequisitesRestoreExactParentWorkWithoutOriginalSubmission()throws Exception{
        Learning.State state=original(2022);Learning.Session parent=state.session;String pid=parent.id,qid=parent.question.id;
        List<String> queue=new ArrayList<>(parent.queue);List<String> entries=new ArrayList<>(parent.conceptHelp.entries);
        Review.Track originalTrack=Review.track(state.progress("add9"));int events=originalTrack.events.size();long generation=originalTrack.generation;
        Random random=new Random(39202);assertTrue(KoreanFoundationDetour.available(state));
        Learning.Session child=KoreanFoundationStudy.beginEasier(state,random);
        assertEquals(List.of("join9"),child.selected);assertEquals(List.of("join9","join9"),child.queue);assertEquals(2,child.target);
        assertSame(parent,state.savedSessions.get(pid));assertEquals(Catalog.get("add9").title,KoreanFoundationDetour.returnTitle(state));
        assertFalse(KoreanFoundationDetour.returnToQuestion(state,true));finish(state,random,false);
        state=cold(LearningSnapshot.capture(state));assertTrue(KoreanFoundationDetour.canReturn(state));finish(state,random,true);
        assertEquals(2,state.session.completed);assertTrue(state.session.finished);assertTrue(KoreanFoundationDetour.returnToQuestion(state,true));
        assertEquals(pid,state.session.id);assertEquals(qid,state.session.question.id);assertEquals(List.of("888"),state.session.answers);
        assertEquals(List.of("1+1=3"),state.session.steps);assertEquals(List.of(Checker.StepKind.FULL),state.session.stepKinds);
        assertArrayEquals(new float[]{1,2,3},state.session.scratch.get(0),0f);assertEquals(entries,state.session.conceptHelp.entries);assertEquals(queue,state.session.queue);
        assertTrue(state.session.reviewWork.errorRecorded);assertTrue(state.session.reviewWork.helpUsed);assertEquals(0,state.session.completed);
        assertEquals(0,state.progress("add9").attempts);assertEquals(generation,state.progress("add9").review.generation);assertEquals(events,state.progress("add9").review.events.size());
        Diagnosis.UnitScreen screen=Diagnosis.currentPlan(state).unitScreens.get(state.session.foundationUnitKey);
        assertEquals(Diagnosis.UnitStatus.NEEDS_STUDY,screen.status);assertFalse(screen.recoveredSkills.contains("add9"));assertFalse(KoreanFoundationDetour.canReturn(state));
    }
    @Test public void nestedEarlyReturnAndColdResumeUnwindOneExactQuestionAtATime()throws Exception{
        Learning.State state=original(2022);Random random=new Random(39203);Learning.Session root=state.session;
        Learning.Session first=KoreanFoundationDetour.begin(state,random);Question firstQuestion=next(state,random);first.answers.set(0,"777");
        Learning.Session second=KoreanFoundationDetour.begin(state,random);assertEquals(List.of("count","count"),second.queue);next(state,random);second.answers.set(0,"666");
        state=cold(LearningSnapshot.capture(state));assertTrue(KoreanFoundationDetour.returnToQuestion(state,false));assertEquals(first.id,state.session.id);
        assertEquals(firstQuestion.id,state.session.question.id);assertEquals(List.of("777"),state.session.answers);assertEquals(root.id,state.session.foundationReturnSessionId);
        assertTrue(KoreanFoundationDetour.returnToQuestion(state,false));assertEquals(root.id,state.session.id);assertEquals(List.of("888"),state.session.answers);
        Learning.resume(state,second.id);assertTrue(KoreanFoundationDetour.canReturn(state));assertEquals(List.of("666"),state.session.answers);
        assertTrue(KoreanFoundationDetour.returnToQuestion(state,false));assertEquals(first.id,state.session.id);
    }
    @Test public void aFiveReasonUnitDoesNotRestartItsOtherTargetsOrLoseItsQueue(){
        Learning.State state=student(2022);Random random=new Random(39204);Learning.beginDiagnostic(state,random);
        Diagnosis.Plan plan=Diagnosis.currentPlan(state);Diagnosis.UnitScreen screen=plan.unitScreens.get("KR|2022|6|2|2");assertNotNull(screen);
        for(String id:List.of(KoreanSpatialSolids.COUNT,KoreanSpatialSolids.TOP,KoreanSpatialSolids.FRONT,KoreanSpatialSolids.SIDE,KoreanSpatialSolids.RECONSTRUCT))
            KoreanFoundationStudy.flagged(state,screen,id,"unable",DAY);
        Diagnosis.beginStudy(state,screen,random);
        int limit=40;while(!KoreanSpatialSolids.SIDE.equals(next(state,random).skillId)){assertTrue(limit-->0);finish(state,random,false);}
        Learning.Session parent=state.session;List<String> queue=new ArrayList<>(parent.queue);Map<String,String> reasons=new LinkedHashMap<>(screen.reasons);int completed=parent.completed;
        KoreanFoundationDetour.begin(state,random);assertEquals(List.of(KoreanSpatialSolids.FRONT),state.session.selected);assertEquals(List.of(KoreanSpatialSolids.FRONT,KoreanSpatialSolids.FRONT),state.session.queue);
        assertTrue(KoreanFoundationDetour.returnToQuestion(state,false));assertSame(parent,state.session);assertEquals(queue,parent.queue);assertEquals(completed,parent.completed);assertEquals(reasons,screen.reasons);
    }
    @Test public void staleParentsProfileChangesDeferredAndForgedLinksCannotRetargetReturn(){
        List<Consumer<Learning.State>> changes=List.of(
            s->s.profile.countryCode="NA",s->s.profile.educationSystem="na-national",s->s.profile.foundationAscending=false,
            s->s.profile.excluded.add("join9"),s->s.profile.lastLearnedSkill="unit:KR|2022|1|1|3",
            s->s.savedSessions.get(s.session.foundationReturnSessionId).question.id="different",
            s->s.savedSessions.get(s.session.foundationReturnSessionId).advancePending=true,
            s->s.savedSessions.get(s.session.foundationReturnSessionId).finished=true,
            s->s.savedSessions.get(s.session.foundationReturnSessionId).reviewingId="active-deferred",
            s->s.session.target=3,
            s->s.session.foundationReturnScopeKey="wrong-scope",s->s.session.foundationUnitKey="KR|2022|4|2|3",
            s->s.session.selected.clear(),s->s.session.educationSystem="na-national");
        for(Consumer<Learning.State> change:changes){Learning.State state=original(2022);KoreanFoundationDetour.begin(state,new Random(39205));Learning.Session child=state.session;change.accept(state);assertFalse(KoreanFoundationDetour.canReturn(state));assertFalse(KoreanFoundationDetour.returnToQuestion(state,false));assertSame(child,state.session);}
        Learning.State state=original(2022);KoreanFoundationDetour.begin(state,new Random(39206));next(state,new Random(39207));Learning.finishQuestion(state,true,DAY,new Random(39208));
        assertFalse(state.session.deferred.isEmpty());Deferred.open(state.session,state.session.deferred.keySet().iterator().next());
        assertFalse(KoreanFoundationDetour.canReturn(state));assertFalse(KoreanFoundationDetour.returnToQuestion(state,false));
    }
    @Test public void linkedRulesPrerequisitesKeepTheirSmallPublicSupplyAndExplicitLimits(){
        Learning.State state=student(2022);Random random=new Random(39209);Learning.beginDiagnostic(state,random);Diagnosis.Plan plan=Diagnosis.currentPlan(state);
        Diagnosis.UnitScreen screen=plan.unitScreens.get("KR|2022|4|2|3");KoreanFoundationStudy.flagged(state,screen,KoreanRulesRelations.NUMBER,"unable",DAY);
        Diagnosis.beginStudy(state,screen,random);finish(state,random,false);finish(state,random,false);assertEquals(KoreanRulesRelations.NUMBER,next(state,random).skillId);
        KoreanFoundationDetour.begin(state,random);assertEquals(List.of("el_number_pattern"),state.session.selected);
        CurriculumLimits small=KoreanFoundationStudy.questionLimits(state,state.session,"el_number_pattern",CurriculumLimits.NONE);assertNotSame(CurriculumLimits.NONE,small);
        for(int i=0;i<2;i++){Question q=next(state,random);assertNotNull(SmallPatternObjects.read(q));finish(state,random,false);}
        CurriculumLimits explicit=new CurriculumLimits("maxGiven=60;maxResult=60");assertSame(explicit,KoreanFoundationStudy.questionLimits(state,state.session,"el_number_pattern",explicit));
        assertTrue(KoreanFoundationDetour.returnToQuestion(state,true));assertEquals(KoreanRulesRelations.NUMBER,state.session.question.skillId);
    }
    @Test public void aResumedArchivedLearnedUnitCanStartUnderAnExpandedEndpointButCannotReturnAfterAnotherScopeChange(){
        Learning.State state=original(2022);Learning.Session parent=state.session;String frozenPlan=parent.foundationPlanKey;
        state.profile.lastLearnedSkill="unit:KR|2022|6|2|6";
        assertNotEquals(frozenPlan,Diagnosis.scopeKey(state.profile));assertNull(Diagnosis.currentPlan(state));
        assertTrue(KoreanFoundationDetour.available(state));KoreanFoundationDetour.begin(state,new Random(39211));
        assertEquals(frozenPlan,state.session.foundationPlanKey);assertEquals(Diagnosis.scopeKey(state.profile),state.session.foundationReturnScopeKey);
        assertTrue(KoreanFoundationDetour.canReturn(state));assertTrue(KoreanFoundationDetour.returnToQuestion(state,false));assertSame(parent,state.session);
        KoreanFoundationDetour.begin(state,new Random(39212));Learning.Session child=state.session;
        state.profile.lastLearnedSkill="unit:KR|2022|6|2|5";
        assertFalse(KoreanFoundationDetour.returnToQuestion(state,false));assertSame(child,state.session);
    }
    @Test public void korean2015WorksWhileLegacyNullFieldsAndUnrelatedSessionsStayUnbound()throws Exception{
        Learning.State state=original(2015);Random random=new Random(39210);String id=state.session.id;
        KoreanFoundationDetour.begin(state,random);assertTrue(KoreanFoundationDetour.canReturn(state));assertTrue(KoreanFoundationDetour.returnToQuestion(state,false));assertEquals(id,state.session.id);
        assertEquals(1L,ObjectStreamClass.lookup(Learning.Session.class).getSerialVersionUID());
        Learning.Session legacy=new Learning.Session();assertNull(legacy.foundationReturnSessionId);assertNull(legacy.foundationReturnQuestionId);assertNull(legacy.foundationReturnScopeKey);
        state=original(2022);state=cold(state);assertFalse(KoreanFoundationDetour.canReturn(state));
        Learning.beginPractice(state,"practice",List.of("add9"),2,false,random);next(state,random);assertFalse(KoreanFoundationDetour.available(state));
        assertFalse(KoreanFoundationDetour.canReturn(state));assertFalse(KoreanFoundationDetour.returnToQuestion(state,false));
    }
}
