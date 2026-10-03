package com.gomgomapps.math.core;

import java.io.*;
import java.time.LocalDate;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class WorkspaceStateTest {
    private static final LocalDate DAY=LocalDate.of(2026,9,8);
    private Learning.State start(){Learning.State state=new Learning.State();Learning.beginPractice(state,"practice",List.of("add20"),3,false,new Random(10));Learning.ensureQuestion(state,new Generator(new Random(12)));return state;}
    private Learning.State restore(Learning.State state)throws Exception{Deferred.sync(state);ByteArrayOutputStream bytes=new ByteArrayOutputStream();new ObjectOutputStream(bytes).writeObject(state);return (Learning.State)new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray())).readObject();}
    @Test public void reloadKeepsQuestionPaperAndOpenWorkspaceWithoutAdvancing()throws Exception{
        Learning.State state=start();Learning.Session s=state.session;String id=s.question.id;
        s.workOpen=true;s.workTab=1;s.answers.set(0,"19");s.steps.add("8+1=9");s.stepKinds.add(Checker.StepKind.PARTIAL);s.scratchAspect=.8f;s.inkAspect=2.4f;s.inkTarget=-1;
        float[] stroke={.12f,.25f,0,.2f,.3f,120};s.scratch.add(stroke);s.pendingInk.add(stroke.clone());
        state=restore(state);s=state.session;assertEquals(id,Learning.ensureQuestion(state,new Generator()).id);assertTrue(s.workOpen);assertEquals(1,s.workTab);assertEquals("19",s.answers.get(0));assertEquals("8+1=9",s.steps.get(0));assertEquals(Checker.StepKind.PARTIAL,s.stepKind(0));assertEquals(.8f,s.scratchAspect,0);assertEquals(2.4f,s.inkAspect,0);assertEquals(-1,s.inkTarget);assertArrayEquals(stroke,s.scratch.get(0),0);assertArrayEquals(stroke,s.pendingInk.get(0),0);assertEquals(0,s.completed);
    }
    @Test public void nextQuestionKeepsWorkspaceButStartsWithCleanPaperAndInputs(){
        Learning.State state=start();Learning.Session s=state.session;s.workOpen=true;s.workTab=1;s.scratchAspect=.7f;s.inkAspect=2.4f;s.scratch.add(new float[]{.2f,.3f,0});s.pendingInk.add(new float[]{.3f,.4f,0});s.inkTarget=0;s.answers.set(0,"9");String old=s.question.id;
        Learning.finishQuestion(state,false,DAY,new Random());Learning.finishQuestion(state,false,DAY,new Random());assertNotEquals(old,Learning.ensureQuestion(state,new Generator()).id);assertEquals(1,s.completed);assertTrue(s.workOpen);assertEquals(1,s.workTab);assertTrue(s.scratch.isEmpty());assertTrue(s.pendingInk.isEmpty());assertEquals(0,s.scratchAspect,0);assertEquals(0,s.inkAspect,0);assertEquals(-1000,s.inkTarget);assertEquals("",s.answers.get(0));
    }
    @Test public void deferredPaperRestoresItsOwnGeometryWithoutChangingWorkspacePreference()throws Exception{
        Learning.State state=start();Learning.Session s=state.session;String old=s.question.id;s.scratchAspect=.8f;s.inkAspect=2.4f;s.scratch.add(new float[]{.2f,.3f,0});Learning.finishQuestion(state,true,DAY,new Random());Learning.ensureQuestion(state,new Generator());s.scratchAspect=1.6f;s.inkAspect=2;s.workOpen=true;s.workTab=0;String current=s.question.id;
        state=restore(state);s=state.session;Deferred.open(s,old);assertEquals(.8f,s.scratchAspect,0);assertEquals(2.4f,s.inkAspect,0);assertTrue(s.workOpen);s.workTab=1;Deferred.leave(s);assertEquals(current,s.question.id);assertEquals(1.6f,s.scratchAspect,0);assertEquals(2,s.inkAspect,0);assertEquals(1,s.workTab);assertTrue(s.workOpen);
    }
}
