package com.gomgomapps.math.core;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.*;

/** Recent evidence and spaced fresh questions. Counts/intervals are trial settings, not norms. */
public final class Review {
    private Review(){}
    public static final int GAP=2,RECOVERY_CHECKS=2,EVENT_LIMIT=32;
    public enum Outcome { FIRST_CORRECT, FIRST_WRONG, CORRECTED, CORRECTION_WRONG, RECHECK_CORRECT, RECHECK_WRONG, HELP_USED, ASSISTED_CORRECT, SKIPPED, INPUT_UNCERTAIN, STUDY_REQUESTED }
    public enum Status { UNKNOWN, RECHECK, PRACTICE, RECENT }
    public static final class Event implements Serializable {
        private static final long serialVersionUID=1L;
        public final String questionId,signature,sourceId,reason;
        public final Outcome outcome;
        public final long day,sequence;
        Event(Learning.Session s,Outcome outcome,String reason,LocalDate day,long sequence){
            questionId=s.question.id;signature=s.question.signature();sourceId=s.reviewWork.sourceId;
            this.outcome=outcome;this.reason=reason;this.day=day.toEpochDay();this.sequence=sequence;
        }
    }
    public static final class Track implements Serializable {
        private static final long serialVersionUID=1L;
        public List<Event> events=new ArrayList<>();
        public Set<String> failed=new LinkedHashSet<>(),checked=new LinkedHashSet<>();
        /** Optional history for public-validated line/round-solid finite facts; other families keep their prior policy. */
        public Set<String> finiteFailedHistory,finiteQuestionIds;
        public boolean pending,practice,legacy,recent;
        public int recoveryChecks,rechecked,spacedSuccesses,cleanRun;
        public boolean diagramHistoryRenewed;
        public long generation,dueDay,eligibleAfter,updatedDay;
        public String sourceId="";
    }
    /** Stored with a published question, including in deferred snapshots. */
    public static final class Work implements Serializable {
        private static final long serialVersionUID=1L;
        public String questionId,sourceId="";
        /** Optional projection stamp; absent in older in-progress saves (UID stays 1). */
        public String foundationPlanKey,foundationUnitKey,foundationReason;
        public long generation,issuedAfter;
        public boolean recheck,errorRecorded,helpUsed,finished;
        public Set<String> inputIssues=new HashSet<>();
    }
    public static Track track(Learning.Progress p){
        if(p.review==null){
            p.review=new Track();p.review.legacy=p.attempts>0;p.review.pending=p.attempts>0;
            p.review.dueDay=p.lastDay;
        }
        return p.review;
    }
    public static void migrate(Learning.State state){
        for(String id:new ArrayList<>(state.needsPractice)){
            Learning.Progress p=state.progress(id);boolean old=p.review==null;
            Track t=track(p);if(old){t.legacy=true;t.pending=true;}
        }
        for(var entry:state.progress.entrySet())sync(state,entry.getKey(),track(entry.getValue()));
        KoreanFoundationStudy.recordIntervention(state);
    }
    public static Status status(Learning.Progress p){
        Track t=track(p);return t.practice?Status.PRACTICE:t.pending||t.legacy?Status.RECHECK:t.recent?Status.RECENT:Status.UNKNOWN;
    }
    public static boolean needsPractice(Learning.Progress p){return track(p).practice;}
    private static void sync(Learning.State state,String id,Track t){if(t.practice)state.needsPractice.add(id);else state.needsPractice.remove(id);}
    private static boolean ready(Learning.State state,Track t,LocalDate day){
        long now=day.toEpochDay();return (t.pending||t.recent)&&t.dueDay<=now&&(state.reviewSequence>=t.eligibleAfter||now>t.updatedDay);
    }
    private static boolean finiteTopic(String id){return KoreanPlaneFoundations.LINE.equals(id)||KoreanRoundSolids.supports(id);}
    private static boolean finitePublic(Learning.Session s){return s!=null&&s.question!=null&&(KoreanPlaneFoundations.LINE.equals(s.question.skillId)&&KoreanPlaneFoundations.read(s.question)!=null||KoreanRoundSolids.supports(s.question.skillId)&&KoreanRoundSolids.read(s.question)!=null);}
    private static void rememberFinite(Track t){
        if(t.finiteFailedHistory==null)t.finiteFailedHistory=new LinkedHashSet<>();
        if(t.finiteQuestionIds==null)t.finiteQuestionIds=new LinkedHashSet<>();
        t.finiteFailedHistory.addAll(t.failed);
        if(t.sourceId!=null&&!t.sourceId.isBlank())t.finiteQuestionIds.add(t.sourceId);
        for(Event event:t.events)if(event.outcome!=Outcome.INPUT_UNCERTAIN)t.finiteQuestionIds.add(event.questionId);
        if(!t.pending&&t.recent&&t.checked.size()>1){String last=null;for(String signature:t.checked)last=signature;t.checked.clear();if(last!=null)t.checked.add(last);}
    }
    public static boolean due(Learning.State state,String id,LocalDate day){Learning.Progress p=state.progress.get(id);return p!=null&&ready(state,track(p),day);}
    public static String nextDue(Learning.State state,Collection<String> selected,LocalDate day){
        String best=null;for(String id:selected)if(due(state,id,day)){
            Track t=track(state.progress(id)),b=best==null?null:track(state.progress(best));
            if(b==null||t.pending&&!b.pending||t.pending==b.pending&&t.dueDay<b.dueDay)best=id;
        }return best;
    }
    public static Collection<String> avoid(Learning.State state,String id){
        Set<String> avoid=new LinkedHashSet<>(QuestionHistory.recent(state,id));
        // Move the most recently issued questions to the end for finite-domain fallback.
        for(String signature:state.recent){avoid.remove(signature);avoid.add(signature);}
        Learning.Progress p=state.progress.get(id);
        if(p!=null){Track t=track(p);avoid.addAll(t.failed);avoid.addAll(t.checked);}return avoid;
    }
    public static void open(Learning.State state,LocalDate day){
        open(state,day,false);
    }
    static void openFresh(Learning.State state,LocalDate day){open(state,day,true);}
    private static void open(Learning.State state,LocalDate day,boolean fresh){
        Learning.Session s=state.session;if(s==null||s.question==null)return;
        if(s.reviewWork!=null&&s.question.id.equals(s.reviewWork.questionId)){
            KoreanFoundationStudy.recordIntervention(state);return;
        }
        Work w=new Work();w.questionId=s.question.id;w.issuedAfter=state.reviewSequence;s.reviewWork=w;
        Track t=track(state.progress(s.question.skillId));w.generation=t.generation;w.sourceId=t.sourceId;
        if(fresh&&s.question.hasDiagramSignature()&&!t.diagramHistoryRenewed){
            t.diagramHistoryRenewed=true;
            // Old text-only evidence cannot identify which picture was answered. Preserve
            // totals/events/practice, but restart recovery using distinct new pictures.
            boolean ambiguous=t.failed.stream().anyMatch(key->!key.contains("|diagram:"))||t.checked.stream().anyMatch(key->!key.contains("|diagram:"));
            if(ambiguous){
                t.recoveryChecks=0;t.cleanRun=0;t.checked.clear();t.checked.add(s.question.signature());
            }
        }
        boolean finite=finitePublic(s),lineTopic=finiteTopic(s.question.skillId);
        if(finite)rememberFinite(t);
        w.recheck=fresh&&!Deferred.active(s)&&ready(state,t,day)&&!t.checked.contains(s.question.signature())&&
            (finite?!t.finiteQuestionIds.contains(s.question.id):!lineTopic&&!t.failed.contains(s.question.signature()));
    }
    private static void event(Learning.State state,Outcome outcome,String reason,LocalDate day){
        Learning.Session s=state.session;Track t=track(state.progress(s.question.skillId));
        t.events.add(new Event(s,outcome,reason,day,state.reviewSequence));while(t.events.size()>EVENT_LIMIT)t.events.remove(0);
    }
    public static void inputIssue(Learning.State state,String reason,LocalDate day){
        Learning.Session s=state.session;if(s==null||s.question==null||s.advancePending||s.finished&&!Deferred.active(s))return;
        open(state,day);if(s.reviewWork.inputIssues.add(reason))event(state,Outcome.INPUT_UNCERTAIN,reason,day);
    }
    /** An explicit study request schedules fresh checks without inventing failed answers. */
    public static void requestRecheck(Learning.State state,String id,String reason,LocalDate day){
        Track t=track(state.progress(id));
        Learning.Session s=state.session;
        if(s!=null&&s.question!=null&&id.equals(s.question.skillId)){
            open(state,day);event(state,Outcome.STUDY_REQUESTED,reason,day);
        }
        if(t.pending)return;
        t.pending=true;t.legacy=false;t.recent=false;t.generation++;
        t.sourceId=s!=null&&s.question!=null&&id.equals(s.question.skillId)?s.question.id:"";
        t.recoveryChecks=0;t.spacedSuccesses=0;t.cleanRun=0;t.checked.clear();
        t.updatedDay=day.toEpochDay();t.dueDay=t.updatedDay;t.eligibleAfter=state.reviewSequence+GAP;
        state.progress(id).reviewDay=t.dueDay;sync(state,id,t);
    }
    /** Mark explicit concept help without inventing a mathematical error. */
    public static void helpUsed(Learning.State state,LocalDate day){
        Learning.Session s=state.session;if(s==null||s.question==null||s.advancePending||s.finished&&!Deferred.active(s))return;
        open(state,day);Work w=s.reviewWork;if(w.helpUsed)return;w.helpUsed=true;
        event(state,Outcome.HELP_USED,"",day);
        if(finitePublic(s)){Track finiteTrack=track(state.progress(s.question.skillId));rememberFinite(finiteTrack);finiteTrack.finiteQuestionIds.add(s.question.id);}
        if(w.errorRecorded)return;
        Track t=track(state.progress(s.question.skillId));
        t.pending=true;t.legacy=false;t.recent=false;t.generation++;t.sourceId=s.question.id;
        t.recoveryChecks=0;t.spacedSuccesses=0;t.cleanRun=0;t.checked.clear();
        t.updatedDay=day.toEpochDay();t.dueDay=t.updatedDay;t.eligibleAfter=state.reviewSequence+GAP+1;
        sync(state,s.question.skillId,t);
        KoreanFoundationStudy.recordIntervention(state);
    }
    public static void error(Learning.State state,LocalDate day){
        Learning.Session s=state.session;open(state,day);Work w=s.reviewWork;if(w.errorRecorded)return;
        Track t=track(state.progress(s.question.skillId));w.errorRecorded=true;
        event(state,Deferred.active(s)?Outcome.CORRECTION_WRONG:w.recheck?Outcome.RECHECK_WRONG:Outcome.FIRST_WRONG,"",day);
        t.failed.add(s.question.signature());while(t.failed.size()>6)t.failed.remove(t.failed.iterator().next());
        if(finitePublic(s)){rememberFinite(t);t.finiteQuestionIds.add(s.question.id);}
        t.practice=t.practice||t.failed.size()>=2;t.pending=true;t.legacy=false;t.recent=false;
        t.generation++;t.sourceId=s.question.id;t.recoveryChecks=0;t.spacedSuccesses=0;t.cleanRun=0;t.checked.clear();
        t.updatedDay=day.toEpochDay();t.dueDay=t.updatedDay;t.eligibleAfter=state.reviewSequence+GAP+(Deferred.active(s)?0:1);
        sync(state,s.question.skillId,t);
        KoreanFoundationStudy.recordIntervention(state);
    }
    public static void finish(Learning.State state,boolean skip,boolean correction,LocalDate day){
        Learning.Session s=state.session;open(state,day);Work w=s.reviewWork;if(w.finished)return;
        Track t=track(state.progress(s.question.skillId));
        boolean finite=finitePublic(s),lineTopic=finiteTopic(s.question.skillId),newFiniteId=false;
        if(finite){rememberFinite(t);newFiniteId=!t.finiteQuestionIds.contains(s.question.id);t.finiteQuestionIds.add(s.question.id);}
        // Legacy in-progress work carries a confirmed error bit, but no invented past event.
        if(s.hadError&&!w.errorRecorded)error(state,day);
        if(!Deferred.active(s))state.reviewSequence++;
        if(skip){event(state,Outcome.SKIPPED,"",day);return;}
        w.finished=true;
        if(w.helpUsed){
            event(state,Outcome.ASSISTED_CORRECT,"",day);
        }else if(correction){
            event(state,Outcome.CORRECTED,"",day);
            if(!t.recent&&!t.pending){t.pending=true;t.dueDay=day.toEpochDay();t.updatedDay=t.dueDay;t.eligibleAfter=state.reviewSequence+GAP;t.sourceId=s.question.id;}
        }else{
            t.cleanRun++;
            boolean checked=w.recheck&&w.generation==t.generation&&(!finite||!Deferred.active(s))&&
                (w.issuedAfter>=t.eligibleAfter||day.toEpochDay()>t.updatedDay)&&
                !t.checked.contains(s.question.signature())&&(finite?newFiniteId:!lineTopic&&!t.failed.contains(s.question.signature()));
            event(state,checked?Outcome.RECHECK_CORRECT:Outcome.FIRST_CORRECT,"",day);
            if(checked){
                t.rechecked++;s.rechecked++;t.checked.add(s.question.signature());while(t.checked.size()>6)t.checked.remove(t.checked.iterator().next());
                if(t.pending){t.recoveryChecks++;if(t.recoveryChecks>=RECOVERY_CHECKS){t.pending=false;t.practice=false;t.legacy=false;t.recent=true;t.failed.clear();t.spacedSuccesses=0;}}
                else {t.recent=true;t.spacedSuccesses++;}
                // A recovery needs two different facts. Later due rounds remember only the last
                // successful fact, leaving another fact available in these small finite domains.
                if(finite&&!t.pending){t.checked.clear();t.checked.add(s.question.signature());}
                t.updatedDay=day.toEpochDay();t.eligibleAfter=state.reviewSequence+GAP;
                t.dueDay=t.updatedDay+(t.pending?0:Math.min(14,1L<<Math.min(t.spacedSuccesses,3)));
            }else if(!t.pending){t.recent=true;t.legacy=false;if(t.dueDay==0)t.dueDay=day.toEpochDay()+1;}
        }
        state.progress(s.question.skillId).reviewDay=t.dueDay;sync(state,s.question.skillId,t);
    }
    public static boolean diagnosticRecent(Learning.State state,String id){
        Track t=track(state.progress(id));if(t.cleanRun<2)return false;
        if(finiteTopic(id)){
            if(!finitePublic(state.session)||!id.equals(state.session.question.skillId)||t.pending)return false;
            rememberFinite(t);return t.recent;
        }
        t.pending=false;t.practice=false;t.legacy=false;t.recent=true;t.failed.clear();t.recoveryChecks=0;
        t.dueDay=state.progress(id).lastDay+1;t.updatedDay=state.progress(id).lastDay;t.eligibleAfter=state.reviewSequence+GAP;sync(state,id,t);
        return true;
    }
}
