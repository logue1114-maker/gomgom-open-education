package com.gomgomapps.math.core;

import org.junit.Test;
import static org.junit.Assert.*;
import java.io.*;
import java.time.LocalDate;
import java.util.*;

public class KoreanCountArithmeticLessonTest {
    private static Question question(boolean sub){return KoreanLargeNumbers.make(new KoreanLargeNumbers.Givens(KoreanLargeNumbers.COUNT,sub?1393400:1113400,0,1,70000,sub));}
    private static HelpPlan.Draft parent(Question q){
        HelpPlan plan=HelpPlan.forQuestion(q);HelpPlan.Draft parent=plan.restore(null,q.id);var frames=KoreanLargeNumbers.guide(q).frames;
        for(int i=0;i<4;i++){parent.entries.set(i,frames.get(i).expected);parent.entries.add("");}parent.stage=4;parent.entries.set(4,"999");return parent;
    }
    private static void moveTo(Question q,HelpPlan.Draft parent,String role,int left){
        HelpPlan.Draft child=KoreanCountArithmeticLesson.active(q,parent);var model=KoreanCountArithmeticTeaching.model(q,parent.stage);
        while(child.stage<model.actions().size()){
            var action=model.actions().get(child.stage);if(action.role().equals(role)&&(left<0||action.smallLeft()==left))return;
            child.entries.set(child.stage,Integer.toString(action.expected()));assertTrue(KoreanCountArithmeticLesson.advance(q,parent));
        }fail("missing teaching action "+role);
    }
    private static HelpPlan.Draft cold(HelpPlan.Draft draft)throws Exception{
        ByteArrayOutputStream data=new ByteArrayOutputStream();try(ObjectOutputStream out=new ObjectOutputStream(data)){out.writeObject(draft);}
        try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(data.toByteArray()))){return (HelpPlan.Draft)in.readObject();}
    }
    @Test public void exactLessonNeverWritesTheParentAnswerOrAdvancesItsStage(){
        for(boolean sub:List.of(false,true)){
            Question q=question(sub);HelpPlan.Draft parent=parent(q);List<String> original=new ArrayList<>(parent.entries);HelpPlan.Draft child=KoreanCountArithmeticLesson.open(q,parent);
            assertNotNull(child);HelpPlan plan=HelpPlan.forCountArithmetic(q,parent.stage);assertFalse(plan.canTransfer());
            var model=KoreanCountArithmeticTeaching.model(q,4);for(var action:model.actions()){
                child.entries.set(child.stage,Integer.toString(action.expected()));assertTrue(KoreanCountArithmeticLesson.advance(q,parent));
            }
            assertEquals(plan.size(),child.stage);assertEquals(4,parent.stage);assertEquals(original,parent.entries);assertFalse(KoreanCountArithmeticLesson.advance(q,parent));
            KoreanCountArithmeticLesson.close(parent);assertNull(KoreanCountArithmeticLesson.active(q,parent));assertSame(child,KoreanCountArithmeticLesson.open(q,parent));
        }
    }
    @Test public void countAllTwelveObjectsOnceAndRetainPartialWorkAcrossCold()throws Exception{
        Question q=question(false);HelpPlan.Draft parent=parent(q);KoreanCountArithmeticLesson.open(q,parent);moveTo(q,parent,"digitAdd",5);
        assertTrue(KoreanCountArithmeticLesson.showObjects(q,parent));var child=KoreanCountArithmeticLesson.active(q,parent);child.entries.set(child.stage,"12");
        assertTrue(KoreanCountArithmeticLesson.touchObject(q,parent,0));assertFalse(KoreanCountArithmeticLesson.touchObject(q,parent,0));assertFalse(KoreanCountArithmeticLesson.advance(q,parent));
        parent=cold(parent);child=KoreanCountArithmeticLesson.active(q,parent);assertEquals(List.of(0),child.foundationCounted);assertEquals("12",child.entries.get(child.stage));
        for(int i=1;i<12;i++)assertTrue(KoreanCountArithmeticLesson.touchObject(q,parent,i));assertFalse(KoreanCountArithmeticLesson.touchObject(q,parent,12));
        assertTrue(KoreanCountArithmeticLesson.objectsComplete(q,parent));assertTrue(KoreanCountArithmeticLesson.advance(q,parent));assertFalse(child.countObjectsOpen);assertEquals("999",parent.entries.get(4));
    }
    @Test public void removeSevenFromFifteenThenCountEightWithTheSameObjectIdentities()throws Exception{
        Question q=question(true);HelpPlan.Draft parent=parent(q);KoreanCountArithmeticLesson.open(q,parent);moveTo(q,parent,"digitSubtract",15);
        assertTrue(KoreanCountArithmeticLesson.showObjects(q,parent));var child=KoreanCountArithmeticLesson.active(q,parent);child.entries.set(child.stage,"9");
        for(int i=0;i<7;i++)assertTrue(KoreanCountArithmeticLesson.touchObject(q,parent,i));assertFalse(KoreanCountArithmeticLesson.touchObject(q,parent,7));
        assertTrue(KoreanCountArithmeticLesson.touchObject(q,parent,0));assertFalse(KoreanCountArithmeticLesson.confirmRemoval(q,parent));assertTrue(KoreanCountArithmeticLesson.touchObject(q,parent,0));assertTrue(KoreanCountArithmeticLesson.confirmRemoval(q,parent));
        assertFalse(KoreanCountArithmeticLesson.touchObject(q,parent,0));assertTrue(KoreanCountArithmeticLesson.touchObject(q,parent,7));parent=cold(parent);child=KoreanCountArithmeticLesson.active(q,parent);
        assertEquals(7,child.foundationRemoved.size());assertEquals(List.of(7),child.foundationCounted);assertTrue(child.countRemovalConfirmed);
        for(int i=8;i<15;i++)assertTrue(KoreanCountArithmeticLesson.touchObject(q,parent,i));assertTrue(KoreanCountArithmeticLesson.objectsComplete(q,parent));assertFalse(KoreanCountArithmeticLesson.advance(q,parent));assertTrue(child.foundationChecked);
        child.entries.set(child.stage,"8");assertTrue(KoreanCountArithmeticLesson.advance(q,parent));assertEquals(4,parent.stage);
    }
    @Test public void resetKeepsRawInputAndDuplicateOrInvalidObjectIdsDoNotSatisfyTheGate(){
        Question q=question(false);var parent=parent(q);KoreanCountArithmeticLesson.open(q,parent);moveTo(q,parent,"digitAdd",5);KoreanCountArithmeticLesson.showObjects(q,parent);var child=KoreanCountArithmeticLesson.active(q,parent);
        child.entries.set(child.stage,"99");child.foundationCounted=new ArrayList<>(Arrays.asList(0,0,null,-1,99));assertFalse(KoreanCountArithmeticLesson.objectsComplete(q,parent));assertEquals(List.of(0),child.foundationCounted);
        KoreanCountArithmeticLesson.resetObjects(q,parent);assertEquals("99",child.entries.get(child.stage));assertTrue(child.foundationCounted.isEmpty());
    }
    @Test public void zeroNeedsExplicitInputEvenWhenThereAreNoRemainingObjects(){
        Question q=KoreanLargeNumbers.make(new KoreanLargeNumbers.Givens(KoreanLargeNumbers.COUNT,27000000000000L,0,1,9000000000000L,true));var parent=parent(q);KoreanCountArithmeticLesson.open(q,parent);moveTo(q,parent,"digitSubtract",9);
        KoreanCountArithmeticLesson.showObjects(q,parent);for(int i=0;i<9;i++)KoreanCountArithmeticLesson.touchObject(q,parent,i);assertTrue(KoreanCountArithmeticLesson.confirmRemoval(q,parent));assertTrue(KoreanCountArithmeticLesson.objectsComplete(q,parent));assertFalse(KoreanCountArithmeticLesson.advance(q,parent));
        var child=KoreanCountArithmeticLesson.active(q,parent);child.entries.set(child.stage,"0");assertTrue(KoreanCountArithmeticLesson.advance(q,parent));
    }
    @Test public void differentParentStagesQuestionIdsOrPublicOperandsCannotReuseTheChild(){
        Question q=question(false);var parent=parent(q);var first=KoreanCountArithmeticLesson.open(q,parent);parent.stage=3;assertNull(KoreanCountArithmeticLesson.active(q,parent));assertNull(KoreanCountArithmeticLesson.open(q,parent));
        parent.stage=4;q.id="another";assertNull(KoreanCountArithmeticLesson.active(q,parent));q.id=parent.questionId;
        Question changed=KoreanLargeNumbers.make(new KoreanLargeNumbers.Givens(KoreanLargeNumbers.COUNT,2113400,0,1,70000,false));changed.id=q.id;assertNull(KoreanCountArithmeticLesson.active(changed,parent));assertNotSame(first,KoreanCountArithmeticLesson.open(changed,parent));
        parent.teachingVersion=KoreanLargeNumbers.VERSION;assertNull(KoreanCountArithmeticLesson.open(changed,parent));
    }
    @Test public void snapshotAndDeferredCopyDetachTheNestedDraftWithoutCreatingProgress()throws Exception{
        Learning.State state=KoreanLargeIntegrationTest.student();Random random=new Random(380);LocalDate day=LocalDate.of(2026,10,15);Learning.beginDiagnostic(state,random);Learning.beginPractice(state,"practice",List.of(KoreanLargeNumbers.COUNT),20,false,random,Map.of(KoreanLargeNumbers.COUNT,4));
        state.session.question=question(false);state.session.answers=new ArrayList<>(List.of("777"));Review.openFresh(state,day);Review.helpUsed(state,day);state.session.conceptHelp=parent(state.session.question);var parent=state.session.conceptHelp;var child=KoreanCountArithmeticLesson.open(state.session.question,parent);
        child.entries.set(0,"wrong");var snapshot=LearningSnapshot.capture(state);var copied=parent.copy();var frozenChild=KoreanCountArithmeticLesson.active(snapshot.session.question,snapshot.session.conceptHelp);
        assertNotSame(child,frozenChild);assertNotSame(child,copied.countArithmeticLessons.get(4));child.entries.set(0,"125");assertEquals("wrong",frozenChild.entries.get(0));
        Deferred.rememberSkip(state.session);assertNotSame(parent.countArithmeticLessons.get(4),state.session.deferred.get(state.session.question.id).conceptHelp.countArithmeticLessons.get(4));
        assertEquals(List.of("777"),state.session.answers);assertEquals(0,state.session.completed);assertEquals(0,state.progress(KoreanLargeNumbers.COUNT).attempts);assertEquals(Diagnosis.UnitStatus.NEEDS_STUDY,Diagnosis.currentPlan(state).unitScreens.get(KoreanLargeIntegrationTest.UNIT).status);
        var restored=cold(copied);assertEquals("wrong",restored.countArithmeticLessons.get(4).entries.get(0));
    }
}
