package com.gomgomapps.math.core;

import org.junit.Test;
import static org.junit.Assert.*;
import java.io.*;
import java.time.LocalDate;
import java.util.*;

/** Public-geometry fixtures select a fact; Learning, study queues, Review and snapshots are real core paths. */
public class KoreanFiniteLineReviewTest {
    private static final LocalDate DAY=LocalDate.of(2026,10,15);
    private static final String LINE=KoreanPlaneFoundations.LINE,UNIT="KR|2022|3|1|2";
    private final Random random=new Random(37642);
    private Learning.State student(){Learning.State s=new Learning.State();s.profile.grade=6;s.profile.term=2;s.profile.schoolYear=2026;s.profile.curriculum=2022;s.profile.ready=true;s.profile.foundationAscending=true;s.profile.lastLearnedSkill="unit:"+UNIT;Learning.beginDiagnostic(s,random);return s;}
    private Review.Track track(Learning.State s){return Review.track(s.progress(LINE));}
    private Diagnosis.UnitScreen screen(Learning.State s){return Diagnosis.currentPlan(s).unitScreens.get(UNIT);}
    private Question line(int fact){Question q=new Question(LINE,KoreanPlaneFoundations.LINE_PROMPT,"","segment");q.diagram=new StudyDiagram("krPlaneLine",new double[]{0,0,6,0,fact==2?1:0,fact==0?0:1});q.answers=new String[]{KoreanPlaneFoundations.read(q).answerKey()};q.kind="choice";q.stepSupport=false;KoreanPlaneFoundations.choices(q,random);KoreanPlaneFoundations.attach(q);return q;}
    /** Explicit public-geometry fixture for selected-fact/old-ID boundary cases only.
     * Normal generation and finite fallback are exercised separately with desired=null. */
    private Question ensure(Learning.State s,Question desired,LocalDate day){
        Question actual=Learning.ensureQuestion(s,new Generator(random),day);
        if(desired==null||actual==null||!actual.skillId.equals(desired.skillId))return actual;
        s.session.question=desired;s.session.reviewWork=null;s.session.answers.clear();for(String ignored:desired.answers)s.session.answers.add("");
        s.recent.addLast(desired.signature());while(s.recent.size()>160)s.recent.removeFirst();QuestionHistory.record(s,desired.skillId,desired.signature());Review.openFresh(s,day);return desired;
    }
    private Question open(Learning.State s,Question desired,LocalDate day){Learning.beginPractice(s,"practice",List.of(desired.skillId),1,false,random);return ensure(s,desired,day);}
    private void finish(Learning.State s,LocalDate day){Question q=s.session.question;assertTrue(new Checker().check(q,List.of(),List.of(q.answers[0])).correct());Learning.finishQuestion(s,false,day,random);}
    private Question wrong(Learning.State s,int fact){Question q=open(s,line(fact),DAY);String wrong=q.choiceLabels.keySet().stream().filter(key->!key.equals(KoreanPlaneFoundations.read(q).answerKey())).findFirst().orElseThrow();assertEquals(Checker.Status.WRONG_ANSWER,new Checker().check(q,List.of(),List.of(wrong)).status);Learning.markError(s,DAY);finish(s,DAY);return q;}
    private void gap(Learning.State s,LocalDate day){Learning.beginPractice(s,"practice",List.of("earlyShapes"),Review.GAP,false,random);while(!s.session.finished){Learning.ensureQuestion(s,new Generator(random),day);finish(s,day);}}
    private Learning.State cold(Learning.State s)throws Exception{ByteArrayOutputStream bytes=new ByteArrayOutputStream();try(ObjectOutputStream out=new ObjectOutputStream(bytes)){out.writeObject(s);}try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))){Learning.State copy=(Learning.State)in.readObject();Review.migrate(copy);return copy;}}
    private long events(Learning.State s,Review.Outcome outcome){return track(s).events.stream().filter(e->e.outcome==outcome).count();}
    private Learning.State study(Learning.State s,int first,int second,boolean restoreAfterFirst)throws Exception{
        Diagnosis.beginRecheck(s,screen(s),random);List<Integer> facts=new ArrayList<>(List.of(first,second));int target=0;
        while(!s.session.finished){
            int fact=target<facts.size()?facts.get(target):0;Question q=ensure(s,fact<0?null:line(fact),DAY);
            if(LINE.equals(q.skillId)){target++;if(restoreAfterFirst&&target==1){assertTrue(s.session.reviewWork.recheck);s=cold(s);}}
            finish(s,DAY);
        }assertEquals(2,target);return s;
    }

    @Test public void TwoOrAllThreeFailedFactsCanRecoverThroughRealStudyAndTwoDistinctLaterRetrievals()throws Exception{
        for(int failed:List.of(2,3)){
            Learning.State s=student();for(int fact=0;fact<failed;fact++)wrong(s,fact);assertEquals(failed,track(s).failed.size());assertTrue(track(s).pending);assertEquals(Diagnosis.UnitStatus.NEEDS_STUDY,screen(s).status);
            Set<String> failures=new LinkedHashSet<>(track(s).failed);long oldErrors=events(s,Review.Outcome.FIRST_WRONG)+events(s,Review.Outcome.RECHECK_WRONG);s=study(s,failed==3?-1:0,failed==3?-1:1,true);
            assertFalse(track(s).pending);assertTrue(track(s).recent);assertFalse(track(s).practice);assertEquals(2,track(s).recoveryChecks);assertEquals(2,events(s,Review.Outcome.RECHECK_CORRECT));assertTrue(track(s).failed.isEmpty());assertEquals(failures,track(s).finiteFailedHistory);assertEquals(oldErrors,events(s,Review.Outcome.FIRST_WRONG)+events(s,Review.Outcome.RECHECK_WRONG));assertEquals(2,screen(s).freshRechecks.get(LINE).size());assertEquals(Diagnosis.UnitStatus.RECENTLY_CHECKED,screen(s).status);assertTrue(Diagnosis.studyUnits(s).contains(screen(s)));assertEquals(1,track(s).checked.size());assertEquals(DAY.plusDays(1).toEpochDay(),track(s).dueDay);
            wrong(s,2);assertTrue(track(s).pending);assertFalse("Archived failures must not make one new error count as two",track(s).practice);assertEquals(1,track(s).failed.size());assertTrue(track(s).finiteFailedHistory.containsAll(failures));
        }
    }

    @Test public void ARepeatedIntrinsicFactWithinTheRecoveryRoundCannotProvideTheSecondSuccess()throws Exception{
        Learning.State s=student();wrong(s,0);wrong(s,1);wrong(s,2);s=study(s,0,0,false);assertTrue(track(s).pending);assertEquals(1,track(s).recoveryChecks);assertEquals(1,screen(s).freshRechecks.get(LINE).size());assertEquals(Diagnosis.UnitStatus.NEEDS_STUDY,screen(s).status);assertFalse(Review.diagnosticRecent(s,LINE));assertTrue(track(s).pending);
        s=study(s,1,2,false);assertFalse(track(s).pending);assertEquals(2,track(s).recoveryChecks);assertEquals(Diagnosis.UnitStatus.RECENTLY_CHECKED,screen(s).status);
    }

    @Test public void SameQuestionImmediateCorrectionAndHelpNeverCountAsIndependentLaterRetrieval(){
        Learning.State s=student();Question failed=wrong(s,0);assertEquals(0,track(s).recoveryChecks);Question immediate=open(s,line(0),DAY);assertNotEquals(failed.id,immediate.id);assertFalse(s.session.reviewWork.recheck);finish(s,DAY);assertEquals(0,track(s).recoveryChecks);
        gap(s,DAY);Question candidate=open(s,line(1),DAY);assertTrue(s.session.reviewWork.recheck);Review.helpUsed(s,DAY);finish(s,DAY);assertEquals(0,track(s).recoveryChecks);assertEquals(1,events(s,Review.Outcome.ASSISTED_CORRECT));gap(s,DAY);
        open(s,failed,DAY);assertFalse("An archived question ID is not a new question",s.session.reviewWork.recheck);finish(s,DAY);assertEquals(0,track(s).recoveryChecks);gap(s,DAY);
        Question later=open(s,line(0),DAY);assertNotEquals(failed.id,later.id);assertTrue(s.session.reviewWork.recheck);finish(s,DAY);assertEquals(1,track(s).recoveryChecks);assertEquals(1,events(s,Review.Outcome.RECHECK_CORRECT));assertTrue(track(s).finiteQuestionIds.containsAll(List.of(failed.id,immediate.id,candidate.id,later.id)));
    }

    @Test public void FurtherDueRoundsStayPossibleAfterAllThreeLifetimeFactsWereSeen()throws Exception{
        Learning.State s=student();wrong(s,0);wrong(s,1);wrong(s,2);s=study(s,0,1,false);long checked=track(s).rechecked;Set<String> history=new LinkedHashSet<>(track(s).finiteFailedHistory);
        open(s,line(2),DAY);assertFalse(s.session.reviewWork.recheck);finish(s,DAY);assertEquals(checked,track(s).rechecked);
        int[] intervals={2,4,8,8,8};for(int round=0;round<intervals.length;round++){
            LocalDate due=LocalDate.ofEpochDay(track(s).dueDay);int fact=round%3;while(track(s).checked.contains(KoreanPlaneFoundations.identity(line(fact))))fact=(fact+1)%3;
            Question q=open(s,line(fact),due);assertTrue(Review.due(s,LINE,due));assertTrue(s.session.reviewWork.recheck);finish(s,due);assertEquals(++checked,track(s).rechecked);assertEquals(due.plusDays(intervals[round]).toEpochDay(),track(s).dueDay);assertEquals(Set.of(q.signature()),track(s).checked);assertEquals(history,track(s).finiteFailedHistory);assertFalse(track(s).pending);
        }
    }

    @Test public void OldNullFieldsMigrateFromKnownEvidenceAndSnapshotsDetachFiniteHistory()throws Exception{
        Learning.State s=student();Question failed=wrong(s,0);Review.Track t=track(s);t.finiteFailedHistory=null;t.finiteQuestionIds=null;s=cold(s);gap(s,DAY);open(s,line(1),DAY);t=track(s);assertTrue(t.finiteFailedHistory.contains(failed.signature()));assertTrue(t.finiteQuestionIds.contains(failed.id));assertFalse(t.finiteQuestionIds.contains("unknown-old-question"));
        Learning.State copy=LearningSnapshot.capture(s);assertNotSame(t.finiteFailedHistory,track(copy).finiteFailedHistory);assertNotSame(t.finiteQuestionIds,track(copy).finiteQuestionIds);track(copy).finiteFailedHistory.clear();track(copy).finiteQuestionIds.clear();assertFalse(t.finiteFailedHistory.isEmpty());assertFalse(t.finiteQuestionIds.isEmpty());
        s=cold(s);assertTrue(s.session.reviewWork.recheck);finish(s,DAY);assertEquals(1,track(s).recoveryChecks);
        Learning.State oldRecent=student();wrong(oldRecent,0);wrong(oldRecent,1);oldRecent=study(oldRecent,0,1,false);Review.Track legacy=track(oldRecent);legacy.checked.clear();for(int fact=0;fact<3;fact++)legacy.checked.add(line(fact).signature());legacy.finiteFailedHistory=null;legacy.finiteQuestionIds=null;oldRecent=cold(oldRecent);
        LocalDate due=LocalDate.ofEpochDay(track(oldRecent).dueDay);open(oldRecent,line(0),due);assertTrue("Legacy lifetime checked facts must not exhaust future due rounds",oldRecent.session.reviewWork.recheck);assertEquals(1,track(oldRecent).checked.size());finish(oldRecent,due);assertEquals(3,track(oldRecent).rechecked);
    }

    @Test public void StaleGenerationAndDeferredCorrectionCannotUseTheFiniteException(){
        Learning.State s=student();wrong(s,0);gap(s,DAY);Question issued=open(s,line(1),DAY);assertTrue(s.session.reviewWork.recheck);String oldSession=s.session.id;long generation=s.session.reviewWork.generation;
        wrong(s,2);gap(s,DAY);Learning.resume(s,oldSession);assertEquals(issued.id,s.session.question.id);assertTrue(track(s).generation>generation);finish(s,DAY);assertEquals(0,track(s).recoveryChecks);
        gap(s,DAY);Question skipped=open(s,line(1),DAY);Learning.finishQuestion(s,true,DAY,random);Deferred.open(s.session,skipped.id);assertTrue(Deferred.active(s.session));long before=track(s).rechecked;Learning.finishQuestion(s,false,DAY,random);assertEquals(before,track(s).rechecked);assertEquals(0,track(s).recoveryChecks);
    }

    @Test public void InvalidPublicGeometryAndOtherFamiliesDoNotReceiveFiniteLineRecovery(){
        Learning.State s=student();wrong(s,0);gap(s,DAY);Question invalid=line(1);invalid.diagram.values[4]=2;open(s,invalid,DAY);assertFalse(s.session.reviewWork.recheck);assertEquals(Checker.Status.INPUT_NEEDED,new Checker().check(invalid,List.of(),List.of("ray")).status);Review.finish(s,false,false,DAY);assertEquals(0,track(s).recoveryChecks);assertTrue(track(s).pending);
        Question angle=new Question(KoreanPlaneFoundations.ANGLE,KoreanPlaneFoundations.ANGLE_PROMPT,"","right");angle.diagram=new StudyDiagram("krPlaneAngle",new double[]{4,0,0,0,0,3},"A","B","C");angle.kind="choice";angle.stepSupport=false;open(s,angle,DAY);Learning.markError(s,DAY);finish(s,DAY);gap(s,DAY);
        Question repeated=new Question(angle.skillId,angle.prompt,"","right");repeated.diagram=new StudyDiagram(angle.diagram.type,angle.diagram.values,angle.diagram.labels);repeated.kind="choice";repeated.stepSupport=false;open(s,repeated,DAY);assertFalse(s.session.reviewWork.recheck);finish(s,DAY);Review.Track other=Review.track(s.progress(angle.skillId));assertTrue(other.pending);assertEquals(0,other.recoveryChecks);assertNull(other.finiteFailedHistory);assertNull(other.finiteQuestionIds);
    }
}
