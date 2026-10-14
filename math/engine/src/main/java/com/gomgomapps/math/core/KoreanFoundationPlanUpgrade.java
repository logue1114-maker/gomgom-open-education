package com.gomgomapps.math.core;

import java.util.*;

/** Only additive support for a formerly empty unit; never relabel existing evidence. */
final class KoreanFoundationPlanUpgrade {
    private KoreanFoundationPlanUpgrade(){}
    private static String system(String value){return value==null||value.isBlank()?"kr-national":value;}
    private static String key(Diagnosis.Probe p){return p.unitKey+"|"+p.skillId;}
    private static boolean compatible(Diagnosis.Plan old,Diagnosis.Plan fresh){
        if(!old.unitScreening||!old.foundationAscending||old.legacy||old.key.equals(fresh.key)||
            old.actualGrade!=fresh.actualGrade||old.term!=fresh.term||old.schoolYear!=fresh.schoolYear||old.curriculum!=fresh.curriculum||
            !system(old.educationSystem).equals(system(fresh.educationSystem))||!Objects.equals(old.lastLearnedSkill,fresh.lastLearnedSkill)||
            old.unitScreens==null||!old.unitScreens.keySet().equals(fresh.unitScreens.keySet())||!fresh.scope.containsAll(old.scope))return false;
        Set<String> added=new HashSet<>();
        for(var entry:old.unitScreens.entrySet()){
            Diagnosis.UnitScreen before=entry.getValue(),after=fresh.unitScreens.get(entry.getKey());
            if(before.reviewGrade!=after.reviewGrade||before.term!=after.term||before.unit!=after.unit||!Objects.equals(before.planKey,old.key))return false;
            if(before.members.equals(after.members)&&before.representatives.equals(after.representatives)){
                for(String id:before.members)if(!Objects.equals(old.placements.get(id),fresh.placements.get(id)))return false;
            }else{
                if(!before.members.isEmpty()||!before.representatives.isEmpty()||!before.completedRepresentatives.isEmpty()||
                    !before.reasons.isEmpty()||!before.flaggedAt.isEmpty()||!before.recoveredSkills.isEmpty()||!before.freshRechecks.isEmpty()||
                    before.status!=Diagnosis.UnitStatus.UNCHECKED||after.members.isEmpty()||!after.members.stream().allMatch(KoreanFirstTermArithmetic::supports))return false;
                added.addAll(after.members);
            }
        }
        Set<String> difference=new HashSet<>(fresh.scope);difference.removeAll(old.scope);
        List<String> sorted=new ArrayList<>(old.scope);Collections.sort(sorted);
        String expected=Curriculum.MAPPING_VERSION+"|"+old.schoolYear+"|"+old.curriculum+"|"+old.actualGrade+"|"+old.term+"||"+String.join(",",sorted)+"|foundation-unit-screen-v2|units-"+KoreanFoundationUnits.MAPPING_VERSION+"|"+old.lastLearnedSkill;
        if(!old.key.equals(expected)||added.isEmpty()||!difference.equals(added))return false;
        for(var entry:old.checks.entrySet()){
            Diagnosis.Probe before=entry.getValue(),after=fresh.checks.get(entry.getKey());
            if(after==null||!key(before).equals(entry.getKey())||!Objects.equals(before.skillId,after.skillId)||!Objects.equals(before.unitKey,after.unitKey)||before.reviewGrade!=after.reviewGrade)return false;
        }
        return true;
    }
    static void apply(Learning.State state,Diagnosis.Plan fresh){
        List<Diagnosis.Plan> candidates=state.diagnostics.values().stream().filter(p->compatible(p,fresh)).toList();
        if(candidates.size()!=1)return;
        Diagnosis.Plan old=candidates.get(0);
        Set<Learning.Session> sessions=Collections.newSetFromMap(new IdentityHashMap<>());
        if(state.session!=null)sessions.add(state.session);sessions.addAll(state.savedSessions.values());
        Set<Diagnosis.Run> runs=Collections.newSetFromMap(new IdentityHashMap<>());
        for(Learning.Session session:sessions){
            Diagnosis.Run run=session.diagnosticRun;if(session.finished||run==null||run.plan!=old)continue;
            if(!"KR".equals(GlobalCurriculum.country(session))||!"kr-national".equals(GlobalCurriculum.system(session)))return;
            List<Diagnosis.Probe> probes=new ArrayList<>(run.scheduled);if(run.current!=null)probes.add(run.current);
            for(Diagnosis.Probe probe:probes){Diagnosis.Probe expected=fresh.checks.get(key(probe));if(expected==null||expected.reviewGrade!=probe.reviewGrade||old.checks.get(key(probe))!=probe)return;}
            runs.add(run);
        }
        Diagnosis.Plan next=LearningSnapshot.copyPlan(old);next.key=fresh.key;next.scope=new ArrayList<>(fresh.scope);next.placements=new LinkedHashMap<>(fresh.placements);
        for(var entry:fresh.unitScreens.entrySet()){
            Diagnosis.UnitScreen target=next.unitScreens.get(entry.getKey());
            if(target.members.isEmpty()&&!entry.getValue().members.isEmpty()){target=entry.getValue();next.unitScreens.put(entry.getKey(),target);}
            target.planKey=next.key;
        }
        for(var entry:fresh.checks.entrySet())next.checks.putIfAbsent(entry.getKey(),entry.getValue());
        for(Diagnosis.Probe probe:next.checks.values())probe.reserved=false;
        // Preserve each running round and the exact question while moving its bookkeeping atomically.
        for(Diagnosis.Run run:runs){
            run.plan=next;
            if(run.current!=null){run.current=next.checks.get(key(run.current));run.current.reserved=true;}
            List<Diagnosis.Probe> scheduled=new ArrayList<>();
            for(Diagnosis.Probe probe:run.scheduled){Diagnosis.Probe rebound=next.checks.get(key(probe));rebound.reserved=true;scheduled.add(rebound);}
            run.scheduled=scheduled;
        }
        next.pending.clear();next.untested.clear();
        for(var entry:next.checks.entrySet()){
            Diagnosis.Probe probe=entry.getValue();Diagnosis.UnitScreen screen=next.unitScreens.get(probe.unitKey);
            if(!screen.completedRepresentatives.contains(probe.skillId)){next.untested.add(entry.getKey());if(!probe.reserved)next.pending.add(entry.getKey());}
        }
        state.diagnostics.put(next.key,next);
    }
}
