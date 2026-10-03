package com.gomgomapps.math.core;

import java.io.*;
import java.lang.reflect.*;
import java.time.LocalDate;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class LearningSnapshotTest {
    private static final LocalDate DAY=LocalDate.of(2026,9,9);
    private static byte[] bytes(Object value)throws IOException{ByteArrayOutputStream out=new ByteArrayOutputStream();try(ObjectOutputStream stream=new ObjectOutputStream(out)){stream.writeObject(value);}return out.toByteArray();}
    private static Learning.State restored(Learning.State state)throws Exception{return (Learning.State)new ObjectInputStream(new ByteArrayInputStream(bytes(state))).readObject();}
    private static Learning.State populated(){
        Learning.State state=new Learning.State();Random random=new Random(90);Generator generator=new Generator(random);
        state.profile.grade=8;state.profile.term=2;state.profile.excluded.add("add9");state.profile.learnedCourses.add("algebra");
        Learning.beginDiagnostic(state,random);Learning.ensureQuestion(state,generator);Learning.markError(state);Learning.finishQuestion(state,true,DAY,random);
        for(Catalog.Skill skill:Catalog.ALL){
            Learning.beginPractice(state,"practice",List.of(skill.id),2,false,random);Learning.ensureQuestion(state,generator);
            Learning.Session session=state.session;session.answers.set(0,"12/7");session.steps.add("1+2=3");session.stepKinds.add(Checker.StepKind.PARTIAL);
            session.scratch.add(new float[]{.1f,.2f,0,.3f,.4f,12});session.pendingInk.add(new float[]{.7f,.6f,12});session.answerForms.put(0,new FractionInput.Form(true));
            VerticalWork.Draft draft=VerticalWork.draft(session);draft.cells.put("answer:0","3");draft.points.put("answer",1);draft.divisionPlace=2;draft.multiplicationPlace=1;
            session.conceptHelp=new HelpPlan.Draft();session.conceptHelp.questionId=session.question.id;session.conceptHelp.stage=1;session.conceptHelp.entries.add("3");
            draft.decimalDivision=new DecimalDivision.Draft();draft.decimalDivision.top="4.2";DecimalDivision.Board board=new DecimalDivision.Board();board.quotient.put(1,"3");board.brought.put(0,"4");board.product.put(0,"2");board.rest.put(0,"2");draft.decimalDivision.boards.put("4.2/2",board);
            draft.fractions=new FractionWork.Draft();FractionWork.Row row=new FractionWork.Row(true,"+");row.leftNumerator="2";row.leftDenominator="7";draft.fractions.rows.add(row);
            Learning.markError(state);Learning.finishQuestion(state,true,DAY,random);String old=session.question.id;
            Learning.ensureQuestion(state,generator);session.answers.set(0,"81");Deferred.open(session,old);session.answers.set(0,"13/7");session.reviewWork.inputIssues.add("unfinished");
        }
        state.history.add(new Learning.Summary(state.session,DAY));
        // Exercise an alias that must remain one object after capture and serialization.
        state.savedSessions.put(state.session.id,state.session);
        return state;
    }
    @Test public void allCurrentLearningValuesAreDetachedWithValuesAndAliasesIntact()throws Exception{
        Learning.State live=populated(),copy=LearningSnapshot.capture(live);
        compare(live,copy,new IdentityHashMap<>());
        assertSame(copy.session,copy.savedSessions.get(copy.session.id));
        assertSame(copy.session.reviewWork,copy.session.deferred.get(copy.session.reviewingId).reviewWork);
        Learning.Session diagnostic=copy.savedSessions.values().stream().filter(s->s.diagnosticRun!=null).findFirst().orElseThrow();
        assertSame(diagnostic.diagnosticRun.plan,copy.diagnostics.get(diagnostic.diagnosticRun.plan.key));
        byte[] before=bytes(copy);
        live.profile.excluded.clear();live.session.answers.set(0,"999");live.session.question.answers[0]="changed";
        live.session.scratch.get(0)[0]=.99f;live.session.reviewWork.inputIssues.clear();live.session.verticalWork.fractions.rows.get(0).leftDenominator="0";
        live.session.conceptHelp.entries.set(0,"changed");
        live.progress.values().iterator().next().review.events.clear();live.savedSessions.clear();live.diagnostics.clear();
        assertArrayEquals(before,bytes(copy));
    }
    @Test public void savedGraphKeepsDeferredWorkAndCanContinueWithVersionOne()throws Exception{
        Learning.State live=populated(),loaded=restored(LearningSnapshot.capture(live));
        assertEquals(1,loaded.version);assertSame(loaded.session,loaded.savedSessions.get(loaded.session.id));
        assertEquals("13/7",loaded.session.answers.get(0));assertEquals("7",loaded.session.verticalWork.fractions.rows.get(0).leftDenominator);
        assertEquals(1,loaded.session.conceptHelp.stage);assertEquals(List.of("3"),loaded.session.conceptHelp.entries);
        Deferred.leave(loaded.session);assertEquals("81",loaded.session.answers.get(0));
        String question=loaded.session.question.id;assertEquals(question,Learning.ensureQuestion(loaded,new Generator()).id);
    }
    @Test public void nullLegacyFieldsRemainNullUntilExistingMigrationRuns()throws Exception{
        Learning.State live=new Learning.State();live.session=new Learning.Session();live.session.stepKinds=null;live.session.answerForms=null;live.session.verticalWork=null;live.session.reviewWork=null;live.session.deferred=null;live.session.reviewingId=null;
        Learning.State copy=LearningSnapshot.capture(live);assertNull(copy.session.stepKinds);assertNull(copy.session.answerForms);
        Learning.State loaded=restored(copy);assertNotNull(loaded.session.stepKinds);assertNotNull(loaded.session.deferred);assertEquals("",loaded.session.reviewingId);
    }
    @Test public void unsupportedMutableValueIsRejectedInsteadOfShared(){
        Learning.State state=new Learning.State();((Map)state.daily).put("unknown",new StringBuilder("mutable"));
        assertThrows(IllegalStateException.class,()->LearningSnapshot.capture(state));
    }
    /** Independently traverse every serialized field, including newly added fields, and preserve aliases. */
    private static void compare(Object a,Object b,IdentityHashMap<Object,Object> visited)throws Exception{
        if(a==null){assertNull(b);return;}
        if(a instanceof String||a instanceof Number||a instanceof Boolean||a instanceof Character||a instanceof Enum<?>){assertEquals(a,b);return;}
        if(visited.containsKey(a)){assertSame(visited.get(a),b);return;}visited.put(a,b);
        if(a instanceof NumberBond||a instanceof Review.Event){
            for(Field field:a.getClass().getFields()){assertTrue(Modifier.isFinal(field.getModifiers()));compare(field.get(a),field.get(b),visited);}return;
        }
        assertNotSame(a,b);
        if(a.getClass().isArray()){assertEquals(Array.getLength(a),Array.getLength(b));for(int i=0;i<Array.getLength(a);i++)compare(Array.get(a,i),Array.get(b,i),visited);return;}
        if(a instanceof Map<?,?> map){Map<?,?> other=(Map<?,?>)b;assertEquals(map.keySet(),other.keySet());for(var entry:map.entrySet())compare(entry.getValue(),other.get(entry.getKey()),visited);return;}
        if(a instanceof Set<?>){assertEquals(a,b);return;}
        if(a instanceof Collection<?> collection){Iterator<?> other=((Collection<?>)b).iterator();assertEquals(collection.size(),((Collection<?>)b).size());for(Object item:collection)compare(item,other.next(),visited);return;}
        assertEquals(a.getClass(),b.getClass());
        for(Field field:a.getClass().getDeclaredFields())if(!Modifier.isStatic(field.getModifiers())&&!Modifier.isTransient(field.getModifiers())){field.setAccessible(true);compare(field.get(a),field.get(b),visited);}
    }
}
