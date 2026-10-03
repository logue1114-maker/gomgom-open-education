package com.gomgomapps.math.core;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;
import java.io.*;

public class GlobalCurriculumTest {
    @Test public void legacyProfileKeepsKoreanScopeAndDiagnosticKey(){
        Learning.Profile p=new Learning.Profile();p.grade=4;p.term=2;String before=Diagnosis.scopeKey(p);
        p.countryCode=null;p.languageTag=null;p.educationSystem=null;p.learnedSkills=null;
        assertEquals(before,Diagnosis.scopeKey(p));assertEquals("KR",p.countryCode);assertEquals("ko",p.languageTag);assertFalse(p.globalSetup);
    }
    @Test public void countryListDoesNotGrantNationalCurriculumCoverage(){
        for(String country:Locale.getISOCountries()){
            Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,country);
            if(!country.equals("KR"))assertEquals(GlobalCurriculum.COMMON,p.educationSystem);
        }
        assertTrue(GlobalCurriculum.packs("TZ").isEmpty());
    }
    @Test public void commonStudyUsesExplicitKnowledgeInsteadOfKoreanGrade(){
        Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"KE");p.grade=12;p.currentSkill="fracAdd";
        assertEquals(Set.of("count","compare"),new HashSet<>(Learning.diagnosticScope(p).stream().map(s->s.id).toList()));
        p.learnedSkills.add("reduce");assertTrue(Learning.diagnosticScope(p).stream().anyMatch(s->s.id.equals("reduce")));
        assertFalse(Learning.diagnosticScope(p).stream().anyMatch(s->s.id.equals("fracAdd")));
        assertTrue(Learning.learningScope(p).stream().anyMatch(s->s.id.equals("fracAdd")));
    }
    @Test public void englandUsesItsOwnPlacementAndExcludesCurrentYearUntilConfirmed(){
        Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"GB");GlobalCurriculum.choosePack(p,"england-primary-2021-v1");p.grade=2;
        Set<String> scope=new HashSet<>(Learning.diagnosticScope(p).stream().map(s->s.id).toList());
        assertTrue(scope.contains("add20"));assertFalse(scope.contains("add100"));
        p.learnedSkills.add("add100");assertTrue(Learning.diagnosticScope(p).stream().anyMatch(s->s.id.equals("add100")));
        p.currentSkill="add100";assertFalse(Learning.diagnosticScope(p).stream().anyMatch(s->s.id.equals("add100")));
    }
    @Test public void africaPackIsExplicitAboutItsGradeRange(){
        Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"ZA");GlobalCurriculum.choosePack(p,"za-caps-intermediate-2011-v1");p.grade=5;
        assertEquals(4,GlobalCurriculum.pack(p).minGrade());assertEquals(6,GlobalCurriculum.pack(p).maxGrade());
        assertTrue(Learning.diagnosticScope(p).stream().anyMatch(s->s.id.equals("mul22")));
        assertFalse(Learning.diagnosticScope(p).stream().anyMatch(s->s.id.equals("fractionPart")));
        p.excluded.add("mul22");assertFalse(Learning.diagnosticScope(p).stream().anyMatch(s->s.id.equals("mul22")));
    }
    @Test(expected=IllegalArgumentException.class)public void crossCountryPackCannotBeSelected(){Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"GH");GlobalCurriculum.choosePack(p,"england-primary-2021-v1");}
    @Test public void changingCountryKeepsPracticeRecordsAndStoredSession()throws Exception{
        Learning.State s=new Learning.State();s.profile.currentSkill="add9";Learning.Session old=TimedStudy.begin(s,30,new Random(1));Learning.ensureQuestion(s,new Generator(new Random(1)));old.answers.set(0,"3");old.studyMs=41_000;
        GlobalCurriculum.chooseCountry(s.profile,"ZA");s.profile.languageTag="en";s.profile.globalSetup=true;s.progress("add9").firstCorrect=5;
        Learning.State frozen=LearningSnapshot.capture(s);ByteArrayOutputStream bytes=new ByteArrayOutputStream();new ObjectOutputStream(bytes).writeObject(frozen);Learning.State restored=(Learning.State)new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray())).readObject();
        assertEquals("ZA",restored.profile.countryCode);assertEquals("en",restored.profile.languageTag);assertTrue(restored.profile.globalSetup);assertEquals(5,restored.progress("add9").firstCorrect);assertEquals("KR",restored.session.countryCode);assertEquals("3",restored.session.answers.get(0));assertEquals(41_000,restored.session.studyMs);
    }
    @Test public void countryChangesDoNotReuseAnotherDiagnosticPlan(){
        Learning.State s=new Learning.State();GlobalCurriculum.chooseCountry(s.profile,"KE");String a=Diagnosis.scopeKey(s.profile);GlobalCurriculum.chooseCountry(s.profile,"TZ");assertNotEquals(a,Diagnosis.scopeKey(s.profile));
    }
    @Test public void timedRefillUsesItsSavedLearningScopeAfterCountryChange(){
        Learning.State state=new Learning.State();state.profile.currentSkill="add100";
        Learning.Session session=TimedStudy.begin(state,30,new Random(2));Set<String> original=new HashSet<>(session.selected);
        GlobalCurriculum.chooseCountry(state.profile,"KE");state.profile.currentSkill="fracDiv";
        session.queue.clear();Learning.refillTimed(state,session,new Random(3));
        assertEquals("add100",session.plannedCurrentSkill);assertTrue(original.containsAll(session.queue));assertFalse(session.queue.contains("fracDiv"));
    }
    @Test public void curriculumMappingsSeparateFractionByIntegerFromFractionByFraction(){
        Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"GB");GlobalCurriculum.choosePack(p,"england-primary-2021-v1");
        assertEquals(6,GlobalCurriculum.pack(p).grade("fracDivInt"));assertEquals(-1,GlobalCurriculum.pack(p).grade("fracDiv"));
    }
}
