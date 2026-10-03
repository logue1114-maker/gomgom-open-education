package com.gomgomapps.math.core;

import java.time.LocalDate;
import java.util.*;

/** A chosen duration is a session goal, never a daily obligation or a mastery score. */
public final class TimedStudy {
    private TimedStudy(){}
    public static boolean enabled(Learning.Session s){return s!=null&&s.durationMs>0;}
    /** A skipped question can be corrected while the original timed study stays paused. */
    public static boolean needsPauseScreen(Learning.Session s){return enabled(s)&&s.timedPaused&&!s.finished&&!Deferred.active(s);}
    public static long remaining(Learning.Session s){return enabled(s)?Math.max(0,s.durationMs-s.studyMs):0;}
    public static Learning.Session begin(Learning.State state,int minutes,Random random){
        if(minutes!=30&&minutes!=60)throw new IllegalArgumentException("30분 또는 1시간 선택 필요");
        List<String> selected=Learning.suggested(state);
        if(selected.isEmpty())throw new IllegalArgumentException("학습할 단원 선택 필요");
        Learning.parkSession(state);
        Learning.Session s=new Learning.Session();GlobalCurriculum.stamp(s,state.profile);s.mode=minutes==30?"timed30":"timed60";
        s.durationMs=minutes*60_000L;s.selected.addAll(selected);
        Learning.refillTimed(state,s,random);state.session=s;return s;
    }
    /** Never discard an in-progress answer at the deadline. */
    public static boolean finishIfDue(Learning.State state,LocalDate day){
        Learning.Session s=state.session;
        if(!enabled(s)||remaining(s)>0||Deferred.active(s)||s.question!=null&&!s.advancePending)return false;
        s.finished=true;
        if(!s.recorded){state.history.add(new Learning.Summary(s,day));while(state.history.size()>200)state.history.remove(0);s.recorded=true;}
        return true;
    }
    public static String clock(long milliseconds){
        long seconds=(Math.max(0,milliseconds)+999)/1000;
        return String.format(Locale.ROOT,"%02d:%02d",seconds/60,seconds%60);
    }
    public static String elapsed(long milliseconds){return clock(Math.max(0,milliseconds/1000)*1000);}
    /** Only a live screen can own this clock. Runtime anchors are never serialized. */
    public static final class Clock {
        public static final long IDLE_MS=8*60_000L;
        private Learning.Session session;
        private long anchor,lastInput;
        private boolean running;
        public void start(Learning.Session s,long now){
            if(running&&session==s)return;
            pause(now);session=s;anchor=lastInput=now;
            running=enabled(s)&&!s.finished&&!s.timedPaused&&!Deferred.active(s);
        }
        public boolean tick(long now){
            if(!running)return false;
            if(session.finished||session.timedPaused){running=false;return false;}
            // A reversed/replaced monotonic clock must not add a reboot or wall-clock gap.
            if(now<anchor){running=false;session.timedPaused=true;return false;}
            long until=Math.min(now,lastInput+IDLE_MS);
            session.studyMs+=Math.max(0,until-anchor);anchor=now;
            if(now>=lastInput+IDLE_MS){running=false;session.timedPaused=true;return true;}
            return false;
        }
        public void input(long now){if(running){tick(now);if(running)lastInput=now;}}
        public void pause(long now){tick(now);running=false;}
        public boolean running(){return running;}
    }
}
