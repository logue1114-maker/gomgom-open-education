package com.gomgomapps.math.core;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;

public class AnswerFormTest {
    private final Checker checker=new Checker();
    private Question factor(String expression,String answer){Question q=new Question("factor","인수분해하세요.\n"+expression,expression,answer);q.kind="factor";return q;}
    private Checker.Result check(Question q,String answer){return checker.check(q,List.of(),List.of(answer));}
    @Test public void unchangedPolynomialTimesConstantsDoesNotCompleteFactorization(){
        Question q=factor("x^2+3x+2","(x+1)(x+2)");
        for(String answer:List.of("(x^2+3x+2)(1)","1*(x^2+3x+2)","((x^2+3x+2))*((1))","(2)(x^2+3x+2)/2"))assertEquals(answer,Checker.Status.INPUT_NEEDED,check(q,answer).status);
        assertEquals(Checker.Status.WRONG_ANSWER,check(q,"(x+1)(x+3)").status);
    }
    @Test public void groupingExplicitMultiplicationAndConstantSignsPreserveFactoredForm(){
        Question q=factor("x^2+3x+2","(x+1)(x+2)");
        for(String answer:List.of("((x+1)(x+2))","(((x+1))*((x+2)))","(x+2)*(x+1)","-(1+x)(-x-2)","2(x+1)(x+2)/2"))assertTrue(answer,check(q,answer).correct());
        Question square=factor("x^2+2x+1","(x+1)^2");assertTrue(check(square,"((x+1)^2)").correct());
        Question zeroRoot=factor("x^2+2x","x(x+2)");assertTrue(check(zeroRoot,"x(x+2)").correct());assertTrue(check(zeroRoot,"(x)(x+2)").correct());
    }
    @Test public void factoredAnswersStillRejectUnfactoredSumsAndUndefinedExpressions(){
        Question q=factor("x^2+3x+2","(x+1)(x+2)");
        for(String answer:List.of("(x+1)(x+2)+0","(x+1)(x+2)/0","(x+1)(x+2)*","(x+1)(x+2)/(x-x)"))assertEquals(answer,Checker.Status.INPUT_NEEDED,check(q,answer).status);
    }
    @Test public void explicitCoefficientMultiplicationIsNotAnUncollectedLikeTerm(){
        Question q=new Question("likeTerms","3x+4x-2","3x+4x-2","7x-2");q.kind="polynomial";
        assertTrue(check(q,"7*x-2").correct());assertEquals(Checker.Status.INPUT_NEEDED,check(q,"3*x+4*x-2").status);assertTrue(check(q,"7*(x)-2").correct());assertTrue(check(q,"((7*x-2))").correct());assertEquals(Checker.Status.WRONG_ANSWER,check(q,"7*x+2").status);
        Question f=new Question("likeTerms","x/2+x","x/2+x","(3/2)x");f.kind="polynomial";assertTrue(check(f,"(3/2)*x").correct());
    }
    @Test public void decimalValueCannotReplaceRequestedReducedFraction(){
        Question q=new Question("reduce","6/8을 기약분수로 나타내세요.","6/8","3/4");q.kind="reduced";
        assertEquals(Checker.Status.INPUT_NEEDED,check(q,"0.75").status);assertEquals(Checker.Status.INPUT_NEEDED,check(q,"0.75/1").status);assertEquals(Checker.Status.INPUT_NEEDED,check(q,"6/8").status);assertTrue(check(q,"3/4").correct());assertEquals(Checker.Status.WRONG_ANSWER,check(q,"0.5").status);
        Question number=new Question("decimalMul","1.5*0.5","1.5*0.5","3/4");assertTrue(check(number,"0.75").correct());
    }
    @Test public void generatedFactorizationsAcceptGroupingAndRejectExpandedIdentityFactors(){
        Generator generator=new Generator(new Random(9918));
        for(int i=0;i<600;i++){Question q=generator.next("factor",List.of(),false);assertTrue(check(q,"("+q.answers[0]+")").correct());assertEquals(Checker.Status.INPUT_NEEDED,check(q,"("+q.expression+")(1)").status);assertEquals(Checker.Status.WRONG_ANSWER,check(q,"("+q.answers[0]+")+1").status);}
    }
}
