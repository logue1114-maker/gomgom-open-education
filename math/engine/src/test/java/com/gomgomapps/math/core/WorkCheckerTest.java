package com.gomgomapps.math.core;

import org.junit.Test;
import static org.junit.Assert.*;
import static com.gomgomapps.math.core.Checker.StepKind.*;
import java.io.*;
import java.util.*;

public class WorkCheckerTest {
    private final Checker checker=new Checker();
    private final Question sum=new Question("add100","47 + 28","47+28","75");

    @Test public void correctPartialCalculationDoesNotNeedToEqualTheWholeProblem(){
        assertTrue(checker.check(sum,List.of("7+8=15"),List.of("75"),List.of(PARTIAL)).correct());
        assertTrue(checker.check(sum,List.of("7+8=15"),List.of("75")).correct());
        assertEquals(Checker.Status.INPUT_NEEDED,checker.check(sum,List.of("7+8=15"),List.of(""),List.of(PARTIAL)).status);
        assertEquals(Checker.Status.WRONG_ANSWER,checker.check(sum,List.of("7+8=15"),List.of("15"),List.of(PARTIAL)).status);
    }
    @Test public void wrongPartialIsLocatedWithoutFillingOrRevealingTheCorrectAnswer(){
        List<String> steps=new ArrayList<>(List.of("40+20=60","7+8=16"));
        Checker.Result result=checker.check(sum,steps,List.of("75"),List.of(PARTIAL,PARTIAL));
        assertEquals(Checker.Status.WRONG_STEP,result.status);assertEquals(1,result.index);
        assertEquals(List.of("40+20=60","7+8=16"),steps);
        assertFalse(result.message.contains("15"));assertFalse(result.message.contains("75"));
    }
    @Test public void alternateWholeCalculationsAndLeadingEqualsAreAccepted(){
        assertTrue(checker.check(sum,List.of("47+20+8","67+8","=75"),List.of("75"),List.of(FULL,FULL,FULL)).correct());
        assertTrue(checker.check(sum,List.of("47+3+25","50+25=75"),List.of("75"),List.of(FULL,FULL)).correct());
        assertEquals(Checker.Status.WRONG_STEP,checker.check(sum,List.of("7+8=15"),List.of("75"),List.of(FULL)).status);
    }
    @Test public void unclassifiedOrUnreadableWorkDoesNotCountAsAMathematicalError(){
        for(String row:List.of("7+8","?","7+8=","sqrt(2)=1.414")){
            Checker.Result result=checker.check(sum,List.of(row),List.of("75"));
            assertEquals(row,Checker.Status.INPUT_NEEDED,result.status);assertFalse(result.mathematicalError());
        }
        assertEquals(Checker.Status.INPUT_NEEDED,checker.checkSteps(sum,List.of("7+8"),List.of(PARTIAL)).status);
        assertEquals(Checker.Status.WRONG_STEP,checker.check(sum,List.of("47+28=76"),List.of("75"),List.of(FULL)).status);
    }
    @Test public void malformedChainsAreInputNeededBeforeAnEarlierWrongPartIsClassified(){
        for(String row:List.of("7+8=16=","=7+8=16","7+8==16","7+8=16=?")){
            Checker.Result result=checker.checkSteps(sum,List.of(row),List.of(PARTIAL));
            assertEquals(row,Checker.Status.INPUT_NEEDED,result.status);assertFalse(result.mathematicalError());
        }
        assertEquals(Checker.Status.INPUT_NEEDED,checker.checkSteps(sum,List.of("7+8=16="),List.of(FULL)).status);
        Checker.Result connected=checker.checkSteps(sum,List.of("47+28=7+8=15"),List.of(PARTIAL));
        assertEquals(Checker.Status.WRONG_STEP,connected.status);assertFalse(connected.correct());
    }
    @Test public void partialIdentitiesAreDistinctFromEquationsWithUnknownConditions(){
        assertTrue(checker.checkSteps(sum,List.of("2(x+1)=2x+2"),List.of(PARTIAL)).correct());
        Checker.Result unknown=checker.checkSteps(sum,List.of("x=3"),List.of(PARTIAL));
        assertEquals(Checker.Status.INPUT_NEEDED,unknown.status);assertFalse(unknown.mathematicalError());
        assertEquals(Checker.Status.WRONG_STEP,checker.checkSteps(sum,List.of("x+1=x+2"),List.of(PARTIAL)).status);
    }
    @Test public void partialWorkDoesNotReplaceThePreviousWholeEquation(){
        Question q=new Question("linear","2x+4=10","2x+4=10","3");q.kind="equation";
        assertTrue(checker.check(q,List.of("7+8=15","2x=6","x=3"),List.of("3"),List.of(PARTIAL,FULL,FULL)).correct());
        assertEquals(Checker.Status.WRONG_STEP,checker.check(q,List.of("2x=6","x=4"),List.of("3"),List.of(FULL,FULL)).status);
    }
    @Test public void equationsCompareRealSolutionSetsAndDeferUnsupportedTransformations(){
        assertEquals(Expression.Relation.SAME,Expression.equationRelation("x^2-4x+4=0","x=2"));
        assertEquals(Expression.Relation.SAME,Expression.equationRelation("x^2+1=0","1=0"));
        assertEquals(Expression.Relation.DIFFERENT,Expression.equationRelation("x^2=4","x=2"));
        assertEquals(Expression.Relation.DIFFERENT,Expression.equationRelation("x=2","x^2=4"));
        assertEquals(Expression.Relation.DIFFERENT,Expression.equationRelation("x=x","x=0"));
        Question q=new Question("equation","x^3=0","x^3=0","0");
        Checker.Result result=checker.check(q,List.of("x=0"),List.of("0"),List.of(FULL));
        assertEquals(Checker.Status.INPUT_NEEDED,result.status);assertFalse(result.mathematicalError());
        assertEquals(Checker.Status.INPUT_NEEDED,checker.check(sum,List.of("x/x=1"),List.of("75"),List.of(PARTIAL)).status);
    }
    @Test public void workOnlyAndUnsupportedWholeQuestionsStillAllowIndependentPartialChecks(){
        sum.stepSupport=false;
        assertTrue(checker.checkSteps(sum,List.of("7+8=15"),List.of(PARTIAL)).correct());
        assertEquals(Checker.Status.INPUT_NEEDED,checker.checkSteps(sum,List.of(),List.of()).status);
        assertEquals(Checker.Status.INPUT_NEEDED,checker.checkSteps(sum,List.of("75"),List.of(FULL)).status);
    }
    @Test public void saveRestoresStepRolesAndMigratesUndeclaredOldRoles()throws Exception{
        Learning.Session session=new Learning.Session();session.steps.add("7+8=15");session.stepKinds.add(PARTIAL);
        Learning.Session restored=copy(session);assertEquals(PARTIAL,restored.stepKind(0));assertEquals(session.steps,restored.steps);
        session.stepKinds=null;restored=copy(session);assertEquals(UNSPECIFIED,restored.stepKind(0));
        assertTrue(checker.check(sum,restored.steps,List.of("75"),restored.stepKinds).correct());
    }
    private Learning.Session copy(Learning.Session source)throws Exception{
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();try(ObjectOutputStream out=new ObjectOutputStream(bytes)){out.writeObject(source);}
        try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))){return (Learning.Session)in.readObject();}
    }
}
