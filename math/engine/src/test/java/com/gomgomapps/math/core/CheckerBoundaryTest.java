package com.gomgomapps.math.core;

import org.junit.Test;
import java.util.List;
import static com.gomgomapps.math.core.Checker.StepKind.PARTIAL;
import static org.junit.Assert.*;

public class CheckerBoundaryTest {
    private final Checker checker=new Checker();

    @Test public void equivalentFractionWorkIsAcceptedButReducedAnswersStillRequireLowestTerms(){
        Question fraction=new Question("fraction","분수를 계산하세요.","1/2","1/2");
        assertTrue(checker.checkSteps(fraction,List.of("1/2=2/4"),List.of(PARTIAL)).correct());
        assertTrue(checker.check(fraction,List.of(),List.of("2/4")).correct());

        Question reduced=new Question("reduce","기약분수로 나타내세요.","1/2","1/2");
        reduced.kind="reduced";
        assertEquals(Checker.Status.INPUT_NEEDED,checker.check(reduced,List.of(),List.of("2/4")).status);
        assertTrue(checker.check(reduced,List.of(),List.of("1/2")).correct());
    }

    @Test public void unsupportedNotationRemainsInputNeededEvenWhenAnotherPartIsWrong(){
        Question sum=new Question("add100","47 + 28","47+28","75");
        for(String row:List.of("7+8=16=sqrt(2)","7+8=16=2π")){
            Checker.Result result=checker.checkSteps(sum,List.of(row),List.of(PARTIAL));
            assertEquals(row,Checker.Status.INPUT_NEEDED,result.status);
            assertFalse(result.mathematicalError());
        }
    }
}
