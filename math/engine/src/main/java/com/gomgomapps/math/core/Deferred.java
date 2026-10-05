package com.gomgomapps.math.core;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.*;

/** Reopen the same unanswered question; this is a correction, not an independent recheck. */
public final class Deferred {
    private Deferred(){}
    public static final class Work implements Serializable {
        private static final long serialVersionUID=1L;
        public Question question;
        public int position,inkTarget;
        public int inkPart,answerFocus;
        public Map<Integer,FractionInput.Form> answerForms;
        public VerticalWork.Draft verticalWork;
        public boolean hadError,advancePending;
        public List<String> answers,steps;
        public List<Checker.StepKind> stepKinds;
        public List<float[]> scratch,pendingInk;
        public float scratchAspect,inkAspect;
        public Review.Work reviewWork;
        public HelpPlan.Draft conceptHelp;
        Work(){} // Detached save construction; normal deferred work still captures a session below.
        private Work(Learning.Session s,int position){
            // Generated Question instances are not modified after publication to a session.
            question=s.question;this.position=position;inkTarget=s.inkTarget;hadError=s.hadError;advancePending=s.advancePending;
            answers=new ArrayList<>(s.answers);steps=new ArrayList<>(s.steps);stepKinds=new ArrayList<>(s.stepKinds);
            inkPart=s.inkPart;answerFocus=s.answerFocus;answerForms=FractionInput.copy(s.answerForms);
            verticalWork=VerticalWork.copy(s.verticalWork);
            scratch=copyInk(s.scratch);pendingInk=copyInk(s.pendingInk);
            scratchAspect=s.scratchAspect;inkAspect=s.inkAspect;
            reviewWork=s.reviewWork;conceptHelp=s.conceptHelp==null?null:s.conceptHelp.copy();
        }
        private void restore(Learning.Session s){
            s.question=question;s.inkTarget=inkTarget;s.hadError=hadError;s.advancePending=advancePending;
            s.answers=new ArrayList<>(answers);s.steps=new ArrayList<>(steps);s.stepKinds=new ArrayList<>(stepKinds);
            s.inkPart=inkPart;s.answerFocus=answerFocus;s.answerForms=FractionInput.copy(answerForms);
            s.verticalWork=VerticalWork.copy(verticalWork);
            s.scratch=copyInk(scratch);s.pendingInk=copyInk(pendingInk);
            s.scratchAspect=scratchAspect;s.inkAspect=inkAspect;
            s.reviewWork=reviewWork;s.conceptHelp=conceptHelp==null?null:conceptHelp.copy();
        }
    }
    private static List<float[]> copyInk(List<float[]> ink){List<float[]> copy=new ArrayList<>();for(float[] stroke:ink)copy.add(stroke.clone());return copy;}
    public static boolean active(Learning.Session s){return s!=null&&s.reviewingId!=null&&!s.reviewingId.isEmpty();}
    public static int count(Learning.Session s){return s==null?0:s.deferred.size();}
    public static boolean resumable(Learning.Session s){return s!=null&&(!s.finished||count(s)>0||active(s));}
    public static void rememberSkip(Learning.Session s){s.deferred.putIfAbsent(s.question.id,new Work(s,s.completed+1));}
    public static void sync(Learning.Session s){
        if(!active(s)||s.advancePending)return;
        Work old=s.deferred.get(s.reviewingId);if(old!=null)s.deferred.put(s.reviewingId,new Work(s,old.position));
    }
    public static void sync(Learning.State state){sync(state.session);for(Learning.Session s:state.savedSessions.values())if(s!=state.session)sync(s);}
    public static void open(Learning.Session s,String questionId){
        if(!s.deferred.containsKey(questionId))throw new IllegalArgumentException("보류한 문제를 찾을 수 없음");
        if(active(s))sync(s);else s.resumeWork=new Work(s,s.completed+1);
        Work work=s.deferred.get(questionId);work.restore(s);s.reviewingId=questionId;s.advancePending=false;
    }
    public static void leave(Learning.Session s){
        if(!active(s))return;sync(s);
        if(s.resumeWork==null)throw new IllegalStateException("원래 공부 기록 확인 필요");
        s.resumeWork.restore(s);s.resumeWork=null;s.reviewingId="";
    }
    public static void settle(Learning.Session s){if(active(s)&&s.advancePending)leave(s);}
    public static void settle(Learning.State state){settle(state.session);for(Learning.Session s:state.savedSessions.values())settle(s);}
    public static void finish(Learning.State state,boolean skip,LocalDate day){
        Learning.Session s=state.session;if(!active(s)||s.advancePending)return;
        Work work=s.deferred.get(s.reviewingId);if(work==null)return;
        sync(s);
        if(!skip){
            Learning.Progress p=state.progress(s.question.skillId);
            if(s.skipped<1||p.skipped<1)throw new IllegalStateException("보류 문제 집계 확인 필요");
            boolean assisted=s.reviewWork!=null&&s.reviewWork.helpUsed;
            Review.finish(state,false,true,day);
            s.deferred.remove(s.reviewingId);s.skipped--;p.skipped--;p.consecutive=0;p.lastDay=day.toEpochDay();
            if(assisted){s.assisted++;p.assisted++;}else{s.corrected++;p.corrected++;}
            state.daily.merge(day.toString(),1,Integer::sum);
            for(Learning.Summary summary:state.history)if(s.id.equals(summary.sessionId)){summary.correct=s.correct;summary.corrected=s.corrected;summary.assisted=s.assisted;summary.skipped=s.skipped;}
        }
        // Keep the completed review until the next render. A repeated submit remains inert.
        s.advancePending=true;
    }
    public static Learning.Session resumeCandidate(Learning.State state){
        if(resumable(state.session)&&GlobalCurriculum.matches(state.profile,state.session))return state.session;
        Learning.Session latest=null;for(Learning.Session s:state.savedSessions.values())if(resumable(s)&&GlobalCurriculum.matches(state.profile,s))latest=s;return latest;
    }
}
