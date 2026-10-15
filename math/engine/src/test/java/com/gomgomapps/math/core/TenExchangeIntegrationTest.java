package com.gomgomapps.math.core;

import org.junit.Test;
import static org.junit.Assert.*;
import java.io.*;
import java.util.*;

public class TenExchangeIntegrationTest {
    private Question question(int a,int b,boolean add){return new Question(add?"add100":"sub100",a+(add?" + ":" − ")+b,"poisoned private expression","999999");}
    private TenExchangeLesson.Source source(Question q){return TenExchangeLesson.read(q.id,q.skillId,q.prompt);}
    private byte[] bytes(Object value)throws Exception{ByteArrayOutputStream b=new ByteArrayOutputStream();try(ObjectOutputStream out=new ObjectOutputStream(b)){out.writeObject(value);}return b.toByteArray();}
    private Learning.State cold(Learning.State value)throws Exception{try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes(value)))){return (Learning.State)in.readObject();}}
    private Learning.State state(Question q){
        Learning.State state=new Learning.State();Learning.Session s=new Learning.Session();state.session=s;s.mode="practice";s.countryCode="KR";s.languageTag="ko";s.question=q;
        s.answers.add("777");s.steps.add("kept original work");s.queue.add(q.skillId);s.conceptHelp=new HelpPlan.Draft();s.conceptHelp.questionId=q.id;s.conceptHelp.stage=2;s.conceptHelp.entries.addAll(List.of("old1","old2","888"));
        s.conceptHelp.tenExchange=TenExchangeLesson.start(source(q));state.needsPractice.add(q.skillId);state.savedSessions.put(s.id,s);return state;
    }
    private void manipulate(TenExchangeLesson.Source s,TenExchangeLesson.Draft d){
        var initial=TenExchangeLesson.state(s,d);assertNotNull(initial);int value=initial.boardValue();
        if(s.addition()&&initial.ones()>=10){for(int i=0;i<10;i++)assertTrue(TenExchangeLesson.selectOne(s,d,i));assertTrue(TenExchangeLesson.beginGroup(s,d));assertEquals(value,TenExchangeLesson.state(s,d).boardValue());assertFalse(TenExchangeLesson.hidePicture(s,d));assertTrue(TenExchangeLesson.finishMotion(s,d));}
        if(!s.addition()&&initial.ones()<s.right()%10){assertTrue(TenExchangeLesson.selectTen(s,d,0));assertTrue(TenExchangeLesson.beginSplit(s,d));assertEquals(value,TenExchangeLesson.state(s,d).boardValue());assertTrue(TenExchangeLesson.finishMotion(s,d));}
        assertEquals(value,TenExchangeLesson.state(s,d).boardValue());
        if(!s.addition()){for(int i=0;i<s.right()%10;i++)assertTrue(TenExchangeLesson.selectOne(s,d,i));for(int i=0;i<s.right()/10;i++)assertTrue(TenExchangeLesson.selectTen(s,d,i));assertTrue(TenExchangeLesson.removeSelected(s,d));assertFalse(TenExchangeLesson.removeSelected(s,d));}
    }
    @Test public void everySupportedPublicPairUsesConservedObjectsAndManualRecallWithoutReadingPrivateKeys()throws Exception{
        int count=0;
        for(boolean add:new boolean[]{true,false})for(int a=0;a<=99;a++)for(int b=0;b<=99;b++){
            if(add?a+b>99:a<b)continue;Question q=question(a,b,add);q.studyGuide=new StudyGuide().step("poisoned guide","","","-999");byte[] before=bytes(q);
            var source=source(q);assertNotNull(source);var draft=TenExchangeLesson.start(source);manipulate(source,draft);
            assertEquals(add?a+b:a-b,TenExchangeLesson.state(source,draft).boardValue());assertTrue(TenExchangeLesson.hidePicture(source,draft));
            assertTrue(TenExchangeScene.tiles(TenExchangeLesson.state(source,draft),320,0).isEmpty());
            assertTrue(TenExchangeLesson.enter(source,draft,"888"));assertFalse(TenExchangeLesson.check(source,draft));assertEquals("888",TenExchangeLesson.state(source,draft).entry());
            assertTrue(TenExchangeLesson.enter(source,draft,String.valueOf(add?a+b:a-b)));assertTrue(TenExchangeLesson.check(source,draft));assertFalse(TenExchangeLesson.state(source,draft).picturesVisible());assertArrayEquals(before,bytes(q));count++;
        }assertEquals(10100,count);
    }
    @Test public void actualSnapshotAndSerializationDetachPendingMotionAndKeepParentAnswerHelpAndNeedsPractice()throws Exception{
        Question q=question(32,18,false);Learning.State live=state(q);var source=source(q);var draft=live.session.conceptHelp.tenExchange;
        assertTrue(TenExchangeLesson.selectTen(source,draft,0));assertTrue(TenExchangeLesson.beginSplit(source,draft));
        Learning.State frozen=LearningSnapshot.capture(live);assertSame(frozen.session,frozen.savedSessions.get(frozen.session.id));assertNotSame(draft,frozen.session.conceptHelp.tenExchange);
        byte[] frozenBytes=bytes(frozen);assertTrue(TenExchangeLesson.finishMotion(source,draft));assertArrayEquals(frozenBytes,bytes(frozen));
        Learning.State loaded=cold(frozen);var restored=loaded.session.conceptHelp.tenExchange;
        assertEquals(TenExchangeLesson.Motion.SPLIT,TenExchangeLesson.state(source,restored).pending());assertEquals(3,TenExchangeLesson.state(source,restored).tens());assertEquals(2,TenExchangeLesson.state(source,restored).ones());
        assertEquals(List.of("777"),loaded.session.answers);assertEquals(List.of("kept original work"),loaded.session.steps);assertEquals(2,loaded.session.conceptHelp.stage);assertEquals(List.of("old1","old2","888"),loaded.session.conceptHelp.entries);assertTrue(loaded.needsPractice.contains("sub100"));assertEquals(0,loaded.session.completed);
        assertTrue(TenExchangeLesson.finishMotion(source,restored));assertFalse(TenExchangeLesson.finishMotion(source,restored));
    }
    @Test public void wrongRawSurvivesRevisitAndColdCopyWithoutCompletingParentOrClearingPractice()throws Exception{
        Question q=question(27,18,true);Learning.State state=state(q);var s=source(q);var d=state.session.conceptHelp.tenExchange;
        manipulate(s,d);assertTrue(TenExchangeLesson.hidePicture(s,d));assertTrue(TenExchangeLesson.enter(s,d,"999"));assertFalse(TenExchangeLesson.check(s,d));
        Learning.State restored=cold(LearningSnapshot.capture(state));var copied=restored.session.conceptHelp.tenExchange;
        assertTrue(TenExchangeLesson.state(s,copied).checked());assertEquals("999",TenExchangeLesson.state(s,copied).entry());assertTrue(TenExchangeLesson.revisit(s,copied));assertTrue(TenExchangeLesson.hidePicture(s,copied));assertEquals("999",TenExchangeLesson.state(s,copied).entry());
        assertTrue(TenExchangeLesson.enter(s,copied,"00045"));assertTrue(TenExchangeLesson.check(s,copied));assertEquals(List.of("777"),restored.session.answers);assertEquals(2,restored.session.conceptHelp.stage);assertTrue(restored.needsPractice.contains("add100"));assertEquals(0,restored.session.completed);
    }
    @Test public void deferredWorkUsesHelpPlanDeepCopyAndReturnsToTheUnchangedNewQuestion()throws Exception{
        Question original=question(8,7,true);Learning.State state=state(original);Learning.Session session=state.session;var s=source(original);var d=session.conceptHelp.tenExchange;
        for(int i=0;i<10;i++)TenExchangeLesson.selectOne(s,d,i);assertTrue(TenExchangeLesson.beginGroup(s,d));Deferred.rememberSkip(session);
        session.question=question(42,7,false);String current=session.question.id;session.answers=new ArrayList<>(List.of("666"));session.conceptHelp=new HelpPlan.Draft();session.conceptHelp.questionId=current;session.conceptHelp.entries.add("555");
        Deferred.open(session,original.id);assertEquals(List.of("777"),session.answers);assertEquals(TenExchangeLesson.Motion.GROUP,TenExchangeLesson.state(s,session.conceptHelp.tenExchange).pending());assertTrue(TenExchangeLesson.finishMotion(s,session.conceptHelp.tenExchange));
        Deferred.leave(session);assertEquals(current,session.question.id);assertEquals(List.of("666"),session.answers);assertEquals(List.of("555"),session.conceptHelp.entries);assertNull(session.conceptHelp.tenExchange);
        Deferred.open(session,original.id);assertEquals(1,TenExchangeLesson.state(s,session.conceptHelp.tenExchange).tens());assertEquals(5,TenExchangeLesson.state(s,session.conceptHelp.tenExchange).ones());
    }
    @Test public void mismatchedOrUnsupportedPublicTasksKeepTheirExistingDraftAndGeneralHelp(){
        Question q=question(8,7,true);var s=source(q);var d=TenExchangeLesson.start(s);var other=source(question(8,7,true));assertNull(TenExchangeLesson.restore(other,d));assertNotNull(TenExchangeLesson.state(s,d));
        assertNull(TenExchangeLesson.read("q",null,"8+7"));assertNull(TenExchangeLesson.read("q","add100","99+99"));assertNull(TenExchangeLesson.read("q","sub100","1-2"));assertNull(TenExchangeLesson.read("q","add1000","108+7"));
        HelpPlan.Draft legacy=new HelpPlan.Draft();legacy.stage=1;legacy.entries.add("777");assertNull(legacy.copy().tenExchange);assertEquals(List.of("777"),legacy.copy().entries);
        legacy.tenExchange=d;HelpPlan.Draft copy=legacy.copy();assertNotSame(d,copy.tenExchange);assertTrue(TenExchangeLesson.selectOne(s,d,0));assertTrue(TenExchangeLesson.state(s,copy.tenExchange).selectedOnes().isEmpty());
    }
}
