package com.gomgomapps.math.core;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class CheckerInputTargetTest {
    @Test public void missingAnswerAndWorkKeepDifferentTargets() {
        Checker checker=new Checker();Question q=new Question("inputRoute","3 + 4","3 + 4","7");
        Checker.Result answer=checker.check(q,List.of(),List.of(""));
        assertEquals(Checker.Status.INPUT_NEEDED,answer.status);assertEquals(Checker.InputTarget.ANSWER,answer.inputTarget);
        Checker.Result work=checker.checkSteps(q,List.of(),List.of());
        assertEquals(Checker.Status.INPUT_NEEDED,work.status);assertEquals(Checker.InputTarget.WORK,work.inputTarget);
        assertTrue(checker.check(q,List.of(),List.of("7")).correct());
    }
    @Test public void unfinishedWorkStillBlocksAnOtherwiseCorrectAnswer() {
        Checker checker=new Checker();Question q=new Question("inputRoute","3 + 4","3 + 4","7");
        Checker.Result result=checker.check(q,List.of("3 + 4 = "),List.of("7"),List.of(Checker.StepKind.FULL));
        assertEquals(Checker.Status.INPUT_NEEDED,result.status);assertEquals(Checker.InputTarget.WORK,result.inputTarget);
        assertEquals(0,result.index);assertEquals(1,result.part);
        Checker.Result wrong=checker.check(q,List.of("3 + 4 = 8"),List.of("7"),List.of(Checker.StepKind.FULL));
        assertEquals(Checker.Status.WRONG_STEP,wrong.status);assertEquals(Checker.InputTarget.WORK,wrong.inputTarget);
        assertTrue(checker.check(q,List.of("3 + 4 = 7"),List.of("7"),List.of(Checker.StepKind.FULL)).correct());
    }
    @Test public void indexedSpecialAnswerErrorsRemainInTheirAnswerArea() {
        Checker checker=new Checker();Question q=SubtractionPatterns.make(200,37,100);
        Checker.Result first=checker.check(q,List.of(),List.of("",""));
        assertEquals(Checker.Status.INPUT_NEEDED,first.status);assertEquals(0,first.index);
        assertEquals(Checker.InputTarget.ANSWER,first.inputTarget);
        Checker.Result second=checker.check(q,List.of(),List.of("263",""));
        assertEquals(Checker.Status.INPUT_NEEDED,second.status);assertEquals(1,second.index);
        assertEquals(Checker.InputTarget.ANSWER,second.inputTarget);
        assertFalse(second.mathematicalError());assertTrue(checker.check(q,List.of(),List.of("263","363")).correct());
    }
}
