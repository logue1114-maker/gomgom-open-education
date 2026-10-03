package com.gomgomapps.math.core;

import java.io.*;
import java.time.LocalDate;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class QuestionHistoryTest {
    private final LocalDate day=LocalDate.of(2026,9,16);
    private final Random random=new Random(416);
    private final Generator generator=new Generator(random);
    private Learning.State restore(Learning.State state)throws Exception{
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();
        new ObjectOutputStream(bytes).writeObject(LearningSnapshot.capture(state));
        return (Learning.State)new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray())).readObject();
    }
    @Test public void hundredQuestionRoundContinuesOverThreeDaysWithoutResetting()throws Exception{
        Learning.State state=new Learning.State();Learning.beginPractice(state,"practice",List.of("mul22"),100,true,random);
        Set<String> seen=new HashSet<>();String session=state.session.id;
        for(int i=0;i<100;i++){
            if(i==37||i==70)state=restore(state);
            LocalDate today=day.plusDays(i<37?0:i<70?1:2);
            Question q=Learning.ensureQuestion(state,generator,today);assertNotNull(q);assertTrue(seen.add(q.signature()));
            assertEquals(session,state.session.id);Learning.finishQuestion(state,false,today,random);
        }
        assertTrue(state.session.finished);assertEquals(100,state.session.completed);assertEquals(1,state.history.size());
        assertEquals(Integer.valueOf(37),state.daily.get(day.toString()));assertEquals(Integer.valueOf(33),state.daily.get(day.plusDays(1).toString()));assertEquals(Integer.valueOf(30),state.daily.get(day.plusDays(2).toString()));
    }
    @Test public void unitHistorySurvivesOtherUnitsAndColdRestore()throws Exception{
        Learning.State state=new Learning.State();Learning.beginPractice(state,"practice",List.of("mul22"),100,false,random);
        for(int i=0;i<100;i++){Learning.ensureQuestion(state,generator,day);Learning.finishQuestion(state,false,day,random);}
        Set<String> previous=new HashSet<>(QuestionHistory.recent(state,"mul22"));assertEquals(100,previous.size());
        Learning.beginPractice(state,"practice",List.of("decimalAdd"),170,false,random);
        for(int i=0;i<170;i++){Learning.ensureQuestion(state,generator,day);Learning.finishQuestion(state,false,day,random);}
        assertTrue(Collections.disjoint(state.recent,previous));state=restore(state);
        assertEquals(previous,new HashSet<>(QuestionHistory.recent(state,"mul22")));
        Learning.beginPractice(state,"practice",List.of("mul22"),100,false,random);
        for(int i=0;i<100;i++){
            Set<String> lastHundred=new HashSet<>(QuestionHistory.recent(state,"mul22"));
            Question q=Learning.ensureQuestion(state,generator,day.plusDays(1));assertFalse(lastHundred.contains(q.signature()));Learning.finishQuestion(state,false,day.plusDays(1),random);
        }
        assertEquals(100,QuestionHistory.recent(state,"mul22").size());
    }
    @Test public void smallNumberDomainsStillSupplyHundredQuestionsWithoutImmediateRepetition(){
        Learning.State state=new Learning.State();Learning.beginPractice(state,"practice",List.of("count"),100,false,random);String last="";
        for(int i=0;i<100;i++){Question q=Learning.ensureQuestion(state,generator,day);assertNotEquals(last,q.signature());last=q.signature();Learning.finishQuestion(state,false,day,random);}
        assertTrue(state.session.finished);assertEquals(9,QuestionHistory.recent(state,"count").size());
    }
    @Test public void savedCurriculumGradeAndRecentHistorySurviveNewCountrySelection()throws Exception{
        Learning.State state=new Learning.State();GlobalCurriculum.chooseCountry(state.profile,"ZA");GlobalCurriculum.choosePack(state.profile,"za-caps-r12-2011-v2");state.profile.grade=2;
        Learning.beginPractice(state,"practice",List.of("fractionPart"),100,false,random);Learning.ensureQuestion(state,generator,day);Learning.finishQuestion(state,false,day,random);
        GlobalCurriculum.chooseCountry(state.profile,"KR");state=restore(state);assertEquals(2,state.session.curriculumGrade);
        for(int i=0;i<15;i++){Question q=Learning.ensureQuestion(state,generator,day.plusDays(1));assertTrue(Set.of("1/2","1/3","1/4","1/5").contains(q.expression));Learning.finishQuestion(state,false,day.plusDays(1),random);}
    }
    @Test public void legacyProgressLazilyGetsHistoryWithoutLosingCounts(){
        Learning.State state=new Learning.State();Learning.Progress progress=state.progress("add9");progress.recentQuestions=null;progress.firstCorrect=23;
        Learning.beginPractice(state,"practice",List.of("add9"),100,false,random);Learning.ensureQuestion(state,generator,day);
        assertEquals(23,progress.firstCorrect);assertEquals(1,progress.recentQuestions.size());
    }
}
