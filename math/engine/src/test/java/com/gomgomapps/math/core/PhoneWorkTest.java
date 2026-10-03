package com.gomgomapps.math.core;

import org.junit.Test;
import static org.junit.Assert.*;
import static com.gomgomapps.math.core.Checker.StepKind.*;
import java.util.*;

public class PhoneWorkTest {
    private final Checker checker=new Checker();
    private Question factor(){Question q=new Question("factor","인수분해하세요.\nx^2+7x-18","x^2+7x-18","(x+9)(x-2)");q.kind="factor";return q;}
    @Test public void wrongLeftIsNotBlamedOnCorrectRight(){
        Checker.Result r=checker.checkSteps(factor(),List.of("x^2+8x-18=(x+9)(x-2)"),List.of(FULL));
        assertEquals(Checker.Status.WRONG_STEP,r.status);assertEquals(0,r.index);assertEquals(0,r.part);
        assertFalse(r.message.contains("(x+9)"));
    }
    @Test public void wrongRightIsLocatedAfterCorrectLeft(){
        Checker.Result r=checker.checkSteps(factor(),List.of("x^2+7x-18=(x+8)(x-2)"),List.of(FULL));
        assertEquals(Checker.Status.WRONG_STEP,r.status);assertEquals(1,r.part);
        r=checker.checkSteps(factor(),List.of("=(x+8)(x-2)"),List.of(FULL));assertEquals(1,r.part);
    }
    @Test public void incompletePartIsInputIssueWithoutInventingAnAnswer(){
        for(String row:List.of("x^2+7x-18=","x^2+7x-18=?")){
            Checker.Result r=checker.checkSteps(factor(),List.of(row),List.of(FULL));
            assertEquals(Checker.Status.INPUT_NEEDED,r.status);assertEquals(1,r.part);
        }
    }
    @Test public void equationAndPartialRelationsDoNotInventABadSide(){
        Question q=new Question("eq1","2x+4=10","2x+4=10","3");
        Checker.Result r=checker.checkSteps(q,List.of("2x=8"),List.of(FULL));assertEquals(Checker.Status.WRONG_STEP,r.status);assertEquals(-1,r.part);
        r=checker.checkSteps(q,List.of("7+8=16"),List.of(PARTIAL));assertEquals(Checker.Status.WRONG_STEP,r.status);assertEquals(-1,r.part);
    }
    @Test public void studentAnswerIsCopiedVerbatimAndPartialWorkDoesNotReplaceIt(){
        List<String> steps=new ArrayList<>(List.of("x^2+7x-18 = (x - 2) * (x + 9)","9-2=7"));
        assertEquals("(x - 2) * (x + 9)",WorkAnswer.writtenAnswer(factor(),steps,List.of(FULL,PARTIAL)));
        assertEquals(List.of("x^2+7x-18 = (x - 2) * (x + 9)","9-2=7"),steps);
    }
    @Test public void noAnswerIsGeneratedForMissingWrongUnfinishedOrOnlyPartialWork(){
        Question q=factor();assertEquals("",WorkAnswer.writtenAnswer(q,List.of(),List.of()));
        assertEquals("",WorkAnswer.writtenAnswer(q,List.of("x^2+7x-18="),List.of(FULL)));
        assertEquals("",WorkAnswer.writtenAnswer(q,List.of("x^2+7x-18=(x+8)(x-2)"),List.of(FULL)));
        assertEquals("",WorkAnswer.writtenAnswer(q,List.of("x^2+7x-18=x^2+7x-18"),List.of(FULL)));
        assertEquals("",WorkAnswer.writtenAnswer(q,List.of("9-2=7"),List.of(PARTIAL)));
    }
    @Test public void explicitSingleAnswerEquationCanUseTheWrittenResult(){
        Question q=new Question("eq1","2x+4=10","2x+4=10","3");q.kind="equation";
        assertEquals("3",WorkAnswer.writtenAnswer(q,List.of("2x=6","x=3"),List.of(FULL,FULL)));
        q.answers=new String[]{"3","-3"};assertEquals("",WorkAnswer.writtenAnswer(q,List.of("x=3"),List.of(FULL)));
    }
}
