package com.gomgomapps.math.core;

import java.io.Serializable;
import java.util.*;

/** Short diagnostic rounds over a frozen learned scope. Counts are trial settings, not norms. */
public final class Diagnosis {
    private Diagnosis(){}
    public static final int ROUND_LIMIT=8,BLOCK_LIMIT=4;
    public enum Outcome { UNCHECKED, UNCERTAIN, RECENTLY_CORRECT, NEEDS_PRACTICE }
    public static final class Probe implements Serializable {
        private static final long serialVersionUID=1L;
        public final String skillId;
        public final int reviewGrade;
        public int independent,errors,skipped,correctRun;
        public boolean followUp,reserved;
        public boolean diagramHistoryRenewed;
        public Outcome outcome=Outcome.UNCHECKED;
        public Set<String> seen=new HashSet<>();
        Probe(String id,int grade,boolean followUp){skillId=id;reviewGrade=grade;this.followUp=followUp;}
    }
    public static final class Plan implements Serializable {
        private static final long serialVersionUID=1L;
        public String key;
        public int actualGrade,term,schoolYear,curriculum;
        public String currentSkill;
        public String educationSystem;
        public Map<String,Integer> placements=new LinkedHashMap<>();
        public List<String> scope=new ArrayList<>(),pending=new ArrayList<>();
        public Map<String,Probe> checks=new LinkedHashMap<>();
        public Set<String> untested=new LinkedHashSet<>();
        public boolean initialRoundFinished,legacy;
    }
    public static final class Run implements Serializable {
        private static final long serialVersionUID=1L;
        public Plan plan;
        public boolean standalone;
        public int startCompleted,phaseStart,phaseSize,phase=1,limit;
        public List<Probe> scheduled=new ArrayList<>();
        public Probe current;
        Run(Plan plan,boolean standalone,int limit){this.plan=plan;this.standalone=standalone;this.limit=limit;}
    }
    public static String scopeKey(Learning.Profile p){
        List<String> ids=new ArrayList<>();for(Catalog.Skill s:Learning.diagnosticScope(p))ids.add(s.id);Collections.sort(ids);
        String key=Curriculum.MAPPING_VERSION+"|"+p.schoolYear+"|"+Curriculum.version(p)+"|"+p.grade+"|"+p.term+"|"+p.currentSkill+"|"+String.join(",",ids);
        return GlobalCurriculum.foreign(p)?GlobalCurriculum.identity(p)+"|"+key:key;
    }
    public static Plan currentPlan(Learning.State state){return state.diagnostics==null?null:state.diagnostics.get(scopeKey(state.profile));}
    public static boolean hasInitialRound(Learning.State state){Plan p=currentPlan(state);return p!=null&&p.initialRoundFinished;}
    public static int pendingCount(Plan plan){return plan==null?0:plan.pending.size();}
    public static int remainingChecks(Learning.State state){return pendingCount(currentPlan(state));}
    public static List<String> practiceSkills(Learning.State state,Plan plan){
        List<String> ids=new ArrayList<>();if(plan!=null)for(Probe probe:plan.checks.values()){
            Learning.Progress progress=state.progress.get(probe.skillId);
            if(probe.outcome==Outcome.NEEDS_PRACTICE&&progress!=null&&progress.weak())ids.add(probe.skillId);
        }return ids;
    }
    private static Plan create(Learning.Profile profile){
        Plan p=new Plan();p.key=scopeKey(profile);p.actualGrade=profile.grade;p.term=profile.term;p.schoolYear=profile.schoolYear;p.curriculum=Curriculum.version(profile);p.currentSkill=profile.currentSkill;
        p.educationSystem=profile.educationSystem;
        for(Catalog.Skill skill:Learning.diagnosticScope(profile)){p.scope.add(skill.id);p.placements.put(skill.id,GlobalCurriculum.foreign(profile)?GlobalCurriculum.reviewGrade(p.educationSystem,skill,profile.grade):Curriculum.grade(skill,p.curriculum));}
        if(p.scope.isEmpty())throw new IllegalArgumentException("진단할 계산 범위 선택 필요");
        p.untested.addAll(p.scope);seedUntested(p);return p;
    }
    private static int reviewGrade(Plan plan,Catalog.Skill skill){if(plan.placements!=null&&plan.placements.containsKey(skill.id))return plan.placements.get(skill.id);return plan.educationSystem==null||plan.educationSystem.equals("kr-national")?Curriculum.grade(skill,plan.curriculum):GlobalCurriculum.reviewGrade(plan.educationSystem,skill);}
    private static void seedUntested(Plan plan){
        if(!plan.pending.isEmpty())return;
        int highest=-1;for(String id:plan.untested)if(!plan.checks.containsKey(id))highest=Math.max(highest,reviewGrade(plan,Catalog.get(id)));
        if(highest<0)return;
        for(String id:plan.untested)if(!plan.checks.containsKey(id)&&reviewGrade(plan,Catalog.get(id))==highest)addProbe(plan,id,highest,false);
    }
    private static Probe addProbe(Plan plan,String id,int grade,boolean followUp){
        Probe existing=plan.checks.get(id);
        if(existing!=null)return existing;
        Probe probe=new Probe(id,grade,followUp);plan.checks.put(id,probe);plan.pending.add(id);return probe;
    }
    public static Learning.Session begin(Learning.State state,Random random,boolean restart){
        Review.migrate(state);
        if(state.diagnostics==null)state.diagnostics=new LinkedHashMap<>();
        Plan plan=restart?null:currentPlan(state);
        if(plan!=null){
            refreshFoundations(state,plan);
            if(resumable(state.session,plan))return state.session;
            for(Learning.Session saved:new ArrayList<>(state.savedSessions.values()))if(resumable(saved,plan)){Learning.resume(state,saved.id);return state.session;}
            seedUntested(plan);
        }
        if(plan==null){plan=create(state.profile);state.diagnostics.put(plan.key,plan);}
        if(plan.pending.isEmpty())throw new IllegalArgumentException("현재 확인할 계산이 없음");
        Learning.parkSession(state);Learning.Session s=new Learning.Session();GlobalCurriculum.stamp(s,state.profile);s.mode="diagnostic";s.diagnosticScope.addAll(plan.scope);
        s.diagnosticRun=new Run(plan,true,ROUND_LIMIT);reserveBlock(s,BLOCK_LIMIT,random,false);
        state.session=s;return s;
    }
    private static boolean resumable(Learning.Session s,Plan plan){return s!=null&&!s.finished&&s.diagnosticRun!=null&&s.diagnosticRun.standalone&&s.diagnosticRun.plan==plan;}
    public static int attachDaily(Learning.State state,Learning.Session s,int count,Random random){
        Plan p=currentPlan(state);if(p==null||!p.initialRoundFinished)return 0;
        refreshFoundations(state,p);
        seedUntested(p);if(p.pending.isEmpty())return 0;
        s.diagnosticRun=new Run(p,false,count);reserveBlock(s,count,random,false);return s.diagnosticRun.scheduled.size();
    }
    private static void reserveBlock(Learning.Session s,int maximum,Random random,boolean followUpsOnly){
        Run run=s.diagnosticRun;Plan plan=run.plan;List<Probe> candidates=new ArrayList<>();
        for(String id:plan.pending){Probe p=plan.checks.get(id);if(!p.reserved&&(!followUpsOnly||p.followUp))candidates.add(p);}
        Collections.shuffle(candidates,random);candidates.sort(Comparator.comparingInt(p->p.followUp?0:1));
        int left=maximum;for(Probe p:candidates){if(left<=0)break;plan.pending.remove(p.skillId);p.reserved=true;int copies=Math.min(2,left);for(int i=0;i<copies;i++)run.scheduled.add(p);left-=copies;}
        Collections.shuffle(run.scheduled,random);run.phaseStart=s.completed;run.phaseSize=run.scheduled.size();
        if(run.standalone)s.target=s.completed+run.phaseSize;
    }
    public static String nextSkill(Learning.Session session){
        Run run=session.diagnosticRun;if(run==null)return null;
        run.current=null;if(run.scheduled.isEmpty())return null;
        run.current=run.scheduled.remove(0);session.stageGrade=run.current.reviewGrade;return run.current.skillId;
    }
    public static void record(Learning.State state,Learning.Session s,boolean independent,boolean mathematicalError,boolean skipped,Random random){
        Run run=s.diagnosticRun;Probe probe=run.current;if(probe==null)return;Plan plan=run.plan;
        plan.untested.remove(probe.skillId);
        if(s.question.hasDiagramSignature()&&!probe.diagramHistoryRenewed){
            probe.diagramHistoryRenewed=true;
            if(probe.seen.stream().anyMatch(key->!key.contains("|diagram:"))){
                // Do not combine an unidentified old picture with a possibly identical one.
                probe.correctRun=0;probe.seen.add(s.question.signature());
            }
        }
        if(probe.seen.add(s.question.signature())){
            if(independent){probe.independent++;probe.correctRun++;}
            else {probe.correctRun=0;if(mathematicalError)probe.errors++;if(skipped)probe.skipped++;}
        }
        run.current=null;
        if(!run.scheduled.contains(probe)){
            probe.reserved=false;
            if(probe.correctRun>=2&&Review.diagnosticRecent(state,probe.skillId)){probe.outcome=Outcome.RECENTLY_CORRECT;}
            else if(probe.errors>=2&&Review.needsPractice(state.progress(probe.skillId))){probe.outcome=Outcome.NEEDS_PRACTICE;queueRelatedFoundation(state,plan,probe);}
            else {probe.outcome=Outcome.UNCERTAIN;probe.followUp=true;if(!plan.pending.contains(probe.skillId))plan.pending.add(probe.skillId);}
        }
        if(!run.standalone||!run.scheduled.isEmpty())return;
        int left=run.limit-(s.completed-run.startCompleted);
        boolean followUp=plan.pending.stream().anyMatch(id->plan.checks.get(id).followUp);
        if(left>0&&followUp){run.phase++;reserveBlock(s,Math.min(BLOCK_LIMIT,left),random,true);}
        if(run.scheduled.isEmpty()){
            s.finished=true;plan.initialRoundFinished=true;
            if(currentPlan(state)==plan)state.profile.diagnosed=true;
        }
    }
    private static void refreshFoundations(Learning.State state,Plan plan){
        for(String id:practiceSkills(state,plan))queueRelatedFoundation(state,plan,plan.checks.get(id));
    }
    private static void queueRelatedFoundation(Learning.State state,Plan plan,Probe gap){
        int lower=gap.reviewGrade-1;if(lower<0)return;
        Deque<String> pending=new ArrayDeque<>(Catalog.get(gap.skillId).prerequisites);Set<String> visited=new HashSet<>();
        while(!pending.isEmpty()){
            String pre=pending.removeFirst();if(!visited.add(pre))continue;
            Catalog.Skill skill=Catalog.get(pre);
            if(plan.scope.contains(pre)&&reviewGrade(plan,skill)<=lower){
                Probe known=plan.checks.get(pre);
                Learning.Progress progress=state.progress.get(pre);Review.Track track=progress==null?null:Review.track(progress);
                // Current independent evidence takes precedence over an older diagnostic label.
                if(track!=null&&track.recent&&!track.pending&&track.cleanRun>=2)continue;
                if(known==null){addProbe(plan,pre,lower,true);continue;}
                if(known.outcome!=Outcome.NEEDS_PRACTICE||progress==null||!progress.weak()){
                    known.followUp=true;
                    if(!known.reserved&&!plan.pending.contains(pre))plan.pending.add(pre);
                    continue;
                }
            }
            pending.addAll(skill.prerequisites);
        }
    }
    public static int displayedDone(Learning.Session s){return s.diagnosticRun!=null&&s.diagnosticRun.standalone?s.completed-s.diagnosticRun.phaseStart:s.completed;}
    public static int displayedTarget(Learning.Session s){return s.diagnosticRun!=null&&s.diagnosticRun.standalone?s.diagnosticRun.phaseSize:s.target;}
    public static boolean additionalBlock(Learning.Session s){return s.diagnosticRun!=null&&s.diagnosticRun.standalone&&s.diagnosticRun.phase>1;}

    /** Preserve old work and history, without inferring mathematical failures from old skips. */
    public static void migrate(Learning.State state){
        Curriculum.restoreProfile(state.profile);
        if(state.diagnostics==null){
            state.diagnostics=new LinkedHashMap<>();
            if(state.profile.diagnosed&&!Learning.diagnosticScope(state.profile).isEmpty()){
                Plan plan=create(state.profile);plan.initialRoundFinished=true;plan.legacy=true;state.diagnostics.put(plan.key,plan);
            }
        }
        Set<Learning.Session> sessions=Collections.newSetFromMap(new IdentityHashMap<>());if(state.session!=null)sessions.add(state.session);sessions.addAll(state.savedSessions.values());
        for(Learning.Session s:sessions){
            if(!"diagnostic".equals(s.mode)||s.finished||s.diagnosticRun!=null)continue;
            Plan plan=new Plan();plan.key="legacy:"+s.id;plan.legacy=true;plan.actualGrade=state.profile.grade;plan.term=state.profile.term;plan.schoolYear=state.profile.schoolYear;plan.curriculum=Curriculum.version(state.profile);plan.currentSkill=state.profile.currentSkill;plan.scope.addAll(s.diagnosticScope);plan.untested.addAll(plan.scope);
            Set<String> currentIds=new HashSet<>();for(Catalog.Skill skill:Learning.diagnosticScope(state.profile))currentIds.add(skill.id);
            if(new HashSet<>(plan.scope).equals(currentIds))plan.key=scopeKey(state.profile);
            Run run=new Run(plan,true,ROUND_LIMIT);run.startCompleted=s.completed;run.phaseStart=s.completed;s.diagnosticRun=run;
            if(s.question!=null&&!s.advancePending){Probe p=addProbe(plan,s.question.skillId,s.stageGrade,false);plan.pending.remove(p.skillId);p.reserved=true;run.current=p;run.phaseSize++;}
            for(String id:s.queue){Probe p=addProbe(plan,id,s.stageGrade,false);if(run.phaseSize<BLOCK_LIMIT){plan.pending.remove(id);p.reserved=true;run.scheduled.add(p);run.phaseSize++;}}
            s.queue.clear();s.target=s.completed+run.phaseSize;state.diagnostics.put(plan.key,plan);
            if(run.phaseSize==0){s.finished=true;plan.initialRoundFinished=true;}
        }
    }
}
