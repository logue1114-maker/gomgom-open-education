package com.gomgomapps.math.core;

import java.util.*;

/** An explicit short operation practice, then the exact saved counting question and its own work. */
public final class CountArithmeticBreak {
    private CountArithmeticBreak(){}
    public static final String MODE="countArithmetic";
    private static String currentScope(Learning.State state){
        if(state==null||state.profile==null)return null;
        try{return Diagnosis.scopeKey(state.profile);}catch(IllegalArgumentException invalidLearnedEndpoint){return null;}
    }
    private static String eligibleSkill(Learning.State state){
        Learning.Session s=state==null?null:state.session;
        if(currentScope(state)==null||s==null||s.question==null||s.finished||s.advancePending||Deferred.active(s)||s.diagnosticRun!=null||MODE.equals(s.mode))return null;
        KoreanLargeCountTeaching.Model m=KoreanLargeCountTeaching.model(s.question);
        if(m==null||!"KR".equals(GlobalCurriculum.country(s))||!"kr-national".equals(GlobalCurriculum.system(s))||!Diagnosis.foundationAscending(state.profile)||Curriculum.version(state.profile)!=2022)return null;
        if(!Objects.equals(s.countryCode,state.profile.countryCode)||!Objects.equals(s.educationSystem,state.profile.educationSystem))return null;
        List<String> learned=KoreanFoundationUnits.scope(state.profile);String id=m.addition()?"add9":"sub9";
        if(!learned.contains(KoreanLargeNumbers.COUNT)||!learned.contains(id))return null;return id;
    }
    public static boolean available(Learning.State state){return eligibleSkill(state)!=null;}
    public static String skill(Learning.State state){
        String id=eligibleSkill(state);if(id==null)return null;Learning.Session s=state.session;
        KoreanLargeCountTeaching.Model m=KoreanLargeCountTeaching.model(s.question);
        HelpPlan.Draft d=s.conceptHelp;
        return d!=null&&Objects.equals(d.questionId,s.question.id)&&KoreanLargeCountTeaching.VERSION.equals(d.teachingVersion)&&d.stage>=0&&d.stage<m.actions().size()&&m.actions().get(d.stage).arithmetic()?id:null;
    }
    public static Learning.Session begin(Learning.State state,Random random){
        String id=skill(state);if(id==null)throw new IllegalArgumentException("이 계산의 기초를 다시 선택해 주세요.");
        Learning.Session original=state.session;String scope=currentScope(state),question=original.question.id;
        Learning.Session child=Learning.beginPractice(state,MODE,List.of(id),2,false,random,Map.of(id,Curriculum.grade(Catalog.get(id),Curriculum.version(state.profile))));
        child.countReturnSessionId=original.id;child.countReturnQuestionId=question;child.countReturnScopeKey=scope;
        return child;
    }
    private static Learning.Session destination(Learning.State state){
        Learning.Session child=state==null?null:state.session;
        if(child==null||!MODE.equals(child.mode)||child.countReturnSessionId==null||child.countReturnQuestionId==null||child.countReturnScopeKey==null||!Objects.equals(child.countReturnScopeKey,currentScope(state))||!Diagnosis.foundationAscending(state.profile))return null;
        Learning.Session original=state.savedSessions.get(child.countReturnSessionId);
        if(original==null||original.finished||original.advancePending||Deferred.active(original)||original.diagnosticRun!=null||original.question==null||!Objects.equals(original.question.id,child.countReturnQuestionId)||KoreanLargeCountTeaching.model(original.question)==null)return null;
        if(!"KR".equals(GlobalCurriculum.country(child))||!"KR".equals(GlobalCurriculum.country(original))||!"kr-national".equals(GlobalCurriculum.system(child))||!"kr-national".equals(GlobalCurriculum.system(original))||!Objects.equals(child.countryCode,state.profile.countryCode)||!Objects.equals(child.educationSystem,state.profile.educationSystem))return null;
        if(original.conceptHelp==null||!Objects.equals(original.conceptHelp.questionId,original.question.id)||!KoreanLargeCountTeaching.VERSION.equals(original.conceptHelp.teachingVersion)||!KoreanFoundationUnits.scope(state.profile).contains(KoreanLargeNumbers.COUNT))return null;
        return original;
    }
    public static boolean canReturn(Learning.State state){return destination(state)!=null;}
    public static boolean returnToQuestion(Learning.State state,boolean completedOnly){
        Learning.Session child=state==null?null:state.session;
        if(child==null||completedOnly&&(!child.finished||child.completed<child.target)||Deferred.active(child))return false;
        Learning.Session original=destination(state);if(original==null)return false;
        String scope=child.countReturnScopeKey;child.countReturnSessionId=null;child.countReturnQuestionId=null;child.countReturnScopeKey=null;
        Learning.resume(state,original.id);original.countReturnHelpPending=true;original.countReturnHelpScopeKey=scope;original.countReturnHelpQuestionId=original.question.id;return true;
    }
    public static boolean helpReturnPending(Learning.State state){
        Learning.Session s=state==null?null:state.session;
        return s!=null&&s.countReturnHelpPending&&s.countReturnHelpScopeKey!=null&&Objects.equals(s.countReturnHelpScopeKey,currentScope(state))&&Diagnosis.foundationAscending(state.profile)&&"KR".equals(GlobalCurriculum.country(s))&&"kr-national".equals(GlobalCurriculum.system(s))&&!s.finished&&!s.advancePending&&!Deferred.active(s)&&s.question!=null&&Objects.equals(s.countReturnHelpQuestionId,s.question.id)&&KoreanLargeCountTeaching.model(s.question)!=null&&s.conceptHelp!=null&&Objects.equals(s.conceptHelp.questionId,s.question.id)&&KoreanLargeCountTeaching.VERSION.equals(s.conceptHelp.teachingVersion);
    }
    public static boolean consumeHelpReturn(Learning.State state){
        if(!helpReturnPending(state))return false;state.session.countReturnHelpPending=false;state.session.countReturnHelpScopeKey=null;state.session.countReturnHelpQuestionId=null;return true;
    }
}
