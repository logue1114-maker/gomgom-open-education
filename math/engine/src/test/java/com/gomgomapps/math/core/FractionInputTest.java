package com.gomgomapps.math.core;

import java.io.*;
import java.time.LocalDate;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class FractionInputTest {
    private static final LocalDate DAY=LocalDate.of(2026,9,8);
    private Learning.State start(){Learning.State s=new Learning.State();Learning.beginPractice(s,"practice",List.of("fracAdd"),3,false,new Random(32));Learning.ensureQuestion(s,new Generator(new Random(8)));return s;}
    private Learning.State reload(Learning.State state)throws Exception{Deferred.sync(state);ByteArrayOutputStream out=new ByteArrayOutputStream();new ObjectOutputStream(out).writeObject(state);return (Learning.State)new ObjectInputStream(new ByteArrayInputStream(out.toByteArray())).readObject();}
    @Test public void existingUnreducedSignedAndPartialAnswersArePreserved(){
        Learning.Session s=start().session;
        for(String value:List.of("004/008","-2/-3","4/","/8","-/+")){
            s.answers.set(0,value);assertTrue(FractionInput.form(s,0).fraction);assertEquals(value,s.answers.get(0));String[] parts=FractionInput.parts(value);assertEquals(value,FractionInput.join(parts[0],parts[1]));
        }
        s.answers.set(0,"1//2");assertFalse(FractionInput.form(s,0).fraction);assertEquals("1//2",s.answers.get(0));
    }
    @Test public void numberConversionKeepsValueAndDoesNotSupplyAnAnswer(){
        assertArrayEquals(new String[]{"",""},FractionInput.parts(""));assertArrayEquals(new String[]{"-5","4"},FractionInput.parts("−1.25"));assertArrayEquals(new String[]{"0","1"},FractionInput.parts("0"));assertArrayEquals(new String[]{"003","1"},FractionInput.parts("003"));assertNull(FractionInput.parts("1+2"));
        Learning.Session s=start().session;s.answers.set(0,"1.25");FractionInput.form(s,0);assertEquals("1.25",s.answers.get(0));
    }
    @Test public void defaultModeDependsOnProblemTypeNotTheHiddenAnswer(){
        Question q=start().session.question;assertTrue(FractionInput.defaultFraction(q));q.answers=new String[]{"0"};assertTrue(FractionInput.defaultFraction(q));q.answers=new String[]{"123/456"};assertTrue(FractionInput.defaultFraction(q));
        q.choices=List.of("0","1","2","3");assertFalse(FractionInput.available(q));q.choices=List.of();q.kind="symbol";assertFalse(FractionInput.available(q));
    }
    @Test public void incompleteAndZeroDenominatorsAreInputIssues(){
        Learning.Session s=start().session;assertTrue(FractionInput.setPart(s,0,1,"5"));assertEquals("5/",s.answers.get(0));assertEquals(2,FractionInput.invalidPart(s.answers.get(0)));assertEquals("분모 입력 필요",FractionInput.inputMessage(s.answers.get(0)));
        assertTrue(FractionInput.setPart(s,0,2,"0"));assertEquals(2,FractionInput.invalidPart(s.answers.get(0)));assertFalse(new Checker().check(s.question,List.of(),s.answers).mathematicalError());
        assertFalse(FractionInput.setPart(s,0,2,"1/2"));assertEquals("5/0",s.answers.get(0));assertTrue(FractionInput.setPart(s,0,2,"8"));assertEquals("5/8",s.answers.get(0));assertEquals(0,FractionInput.invalidPart(s.answers.get(0)));
    }
    @Test public void reloadKeepsPartFocusModeAndPendingInk()throws Exception{
        Learning.State state=start();Learning.Session s=state.session;String id=s.question.id;s.answers.set(0,"5/");FractionInput.form(s,0).part=2;s.inkTarget=0;s.inkPart=2;s.pendingInk.add(new float[]{.1f,.2f,0,.2f,.3f,100});
        state=reload(state);s=state.session;assertEquals(id,Learning.ensureQuestion(state,new Generator()).id);assertEquals("5/",s.answers.get(0));assertTrue(FractionInput.form(s,0).fraction);assertEquals(2,FractionInput.form(s,0).part);assertEquals(2,s.inkPart);assertEquals(1,s.pendingInk.size());assertEquals(0,s.completed);
    }
    @Test public void deferredDraftAndNextQuestionDoNotShareModeOrParts()throws Exception{
        Learning.State state=start();Learning.Session s=state.session;String id=s.question.id;s.answers.set(0,"3/");FractionInput.form(s,0).part=2;s.inkTarget=0;s.inkPart=2;
        Learning.finishQuestion(state,true,DAY,new Random());Learning.ensureQuestion(state,new Generator());assertTrue(FractionInput.forms(s).isEmpty());assertEquals(0,s.inkPart);assertEquals("",s.answers.get(0));
        String current=s.question.id;s.answers.set(0,"7");FractionInput.form(s,0).fraction=false;
        state=reload(state);s=state.session;Deferred.open(s,id);assertEquals("3/",s.answers.get(0));assertTrue(FractionInput.form(s,0).fraction);assertEquals(2,FractionInput.form(s,0).part);assertEquals(2,s.inkPart);
        FractionInput.setPart(s,0,2,"8");FractionInput.form(s,0).fraction=false;Deferred.leave(s);assertEquals(current,s.question.id);assertEquals("7",s.answers.get(0));assertFalse(FractionInput.form(s,0).fraction);Deferred.open(s,id);assertEquals("3/8",s.answers.get(0));assertFalse(FractionInput.form(s,0).fraction);
    }
    @Test public void missingOldMetadataUsesDefaultsWithoutDroppingTheAnswer(){
        Learning.Session s=start().session;s.answerForms=null;s.answers.set(0,"2/4");assertTrue(FractionInput.form(s,0).fraction);assertEquals("2/4",s.answers.get(0));assertEquals(0,s.completed);
    }
    @Test public void wrongFractionIsMarkedAsOneAnswerAndReductionRemainsSeparate(){
        Question q=new Question("fracAdd","(1/4) + (1/4)","1/2","1/2");Checker c=new Checker();assertTrue(c.check(q,List.of(),List.of("2/4")).correct());Checker.Result wrong=c.check(q,List.of(),List.of("2/3"));assertEquals(Checker.Status.WRONG_ANSWER,wrong.status);assertEquals(0,wrong.index);assertEquals("이 답 확인",wrong.message);
        q.kind="reduced";assertEquals(Checker.Status.INPUT_NEEDED,c.check(q,List.of(),List.of("2/4")).status);
    }
}
