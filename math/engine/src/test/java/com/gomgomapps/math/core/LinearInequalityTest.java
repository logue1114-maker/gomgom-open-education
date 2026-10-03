package com.gomgomapps.math.core;

import org.junit.Test;
import static org.junit.Assert.*;
import static com.gomgomapps.math.core.Checker.StepKind.*;
import java.util.*;
import java.util.regex.*;
import java.io.*;

public class LinearInequalityTest {
    private final Checker checker=new Checker();
    private Question question(){Question q=new Question("linearInequality","-2x + 3 ≤ 8","-2x + 3 ≤ 8",">=","-5/2");q.kind="inequality";q.labels=new String[]{"부등호","답의 수"};return q;}
    private int[] affine(String s){
        Matcher m=Pattern.compile("(-?\\d*)x(?: ([+-]) (\\d+))?").matcher(s);
        if(!m.matches())return new int[]{0,Integer.parseInt(s)};
        int a=m.group(1).isEmpty()?1:m.group(1).equals("-")?-1:Integer.parseInt(m.group(1));
        return new int[]{a,m.group(2)==null?0:Integer.parseInt(m.group(3))*(m.group(2).equals("-")?-1:1)};
    }
    private boolean satisfies(int comparison,String op){return switch(op){case "<"->comparison<0;case "≤"->comparison<=0;case ">"->comparison>0;case "≥"->comparison>=0;default->throw new AssertionError(op);};}
    @Test public void publicProblemsHaveIndependentBoundaryAndBothSidesOfTheSolution(){
        Generator generator=new Generator(new Random(6090922));Map<String,Integer> signs=new HashMap<>();Set<String> bounds=new HashSet<>(),problems=new HashSet<>();int fractions=0,zero=0,flips=0;
        for(int i=0;i<1200;i++){
            Question q=generator.next("linearInequality",List.of(),true);String line=q.prompt.split("\n")[0];Matcher split=Pattern.compile("(.+) ([<≤>≥]) (.+)").matcher(line);assertTrue(line,split.matches());
            int[] left=affine(split.group(1)),right=affine(split.group(3));int slope=left[0]-right[0];assertNotEquals(0,slope);
            Rational boundary=Rational.of(right[1]-left[1],slope);assertEquals(boundary,Expression.number(q.answers[1]));
            String original=split.group(2),expected=slope>0?original:switch(original){case "<"->">";case "≤"->"≥";case ">"->"<";default->"≤";};
            assertEquals(expected,LinearInequality.displaySign(q.answers[0]));assertTrue(q.choices.isEmpty());
            for(Rational probe:List.of(boundary.sub(Rational.ONE),boundary,boundary.add(Rational.ONE))){
                int comparison=Rational.of(left[0]).mul(probe).add(Rational.of(left[1])).compareTo(Rational.of(right[0]).mul(probe).add(Rational.of(right[1])));
                assertEquals(satisfies(comparison,original),satisfies(probe.compareTo(boundary),expected));
            }
            assertTrue(checker.check(q,List.of(),List.of(q.answers)).correct());signs.merge(q.answers[0],1,Integer::sum);bounds.add(q.answers[1]);problems.add(line);if(!boundary.isInteger())fractions++;if(boundary.isZero())zero++;if(slope<0)flips++;
        }
        assertEquals(4,signs.size());for(int count:signs.values())assertTrue(count>200);assertTrue(bounds.size()>100);assertTrue(problems.size()>1100);assertTrue(fractions>500);assertTrue(zero>10);assertTrue(flips>450);
    }
    @Test public void negativeDivisionReversalAndEquivalentRearrangementsPass(){
        for(List<String> steps:List.of(List.of("-2x<=5","x>=-5/2"),List.of("-5<=2x","-2.5<=x"),List.of("-x+3/2<=4","x>=-2.5"))){assertTrue(steps.toString(),checker.checkSteps(question(),steps,Collections.nCopies(steps.size(),FULL)).correct());}
        assertTrue(checker.check(question(),List.of(),List.of("≥","-10/4")).correct());
    }
    @Test public void directionEndpointAndFirstWrongWorkCannotBeHidden(){
        for(String row:List.of("x<=-5/2","x>-5/2","x>=5/2","0<=0","0>0"))assertEquals(row,Checker.Status.WRONG_STEP,checker.checkSteps(question(),List.of(row),List.of(FULL)).status);
        Checker.Result result=checker.check(question(),List.of("-2x<=5","x<=-5/2","x>=-5/2"),List.of(">=","-5/2"),List.of(FULL,FULL,FULL));assertEquals(Checker.Status.WRONG_STEP,result.status);assertEquals(1,result.index);assertEquals(-1,result.part);
        result=checker.check(question(),List.of(),List.of(">","-5/2"));assertEquals(Checker.Status.WRONG_ANSWER,result.status);assertEquals(0,result.index);
        result=checker.check(question(),List.of(),List.of(">=","5/2"));assertEquals(1,result.index);
    }
    @Test public void malformedUnsupportedAndBlankInputNeedsInputRatherThanMathPenalty(){
        for(String row:List.of("x^2<=4","x/x<=1","x/0<=1","y<=2","x<=","<=3","x=3","0<x<3","x□3","x".repeat(241))){Checker.Result r=checker.checkSteps(question(),List.of(row),List.of(FULL));assertEquals(row,Checker.Status.INPUT_NEEDED,r.status);assertFalse(r.mathematicalError());}
        for(List<String> answer:List.of(List.<String>of(),List.of(">="),List.of("","0"),List.of(">=",""),List.of(">=","1/0"),List.of(">=","1".repeat(241)),List.of(">=","1+2")))assertEquals(Checker.Status.INPUT_NEEDED,checker.check(question(),List.of(),answer).status);
        assertEquals(Checker.Status.INPUT_NEEDED,checker.checkSteps(question(),List.of(""),List.of(FULL)).status);
    }
    @Test public void partialArithmeticDoesNotCompleteOrReplaceTheOriginalInequality(){
        assertTrue(checker.checkSteps(question(),List.of("8-3=5","2<3"),List.of(PARTIAL,PARTIAL)).correct());
        assertEquals(Checker.Status.WRONG_STEP,checker.checkSteps(question(),List.of("8-3=6"),List.of(PARTIAL)).status);
        assertEquals(Checker.Status.INPUT_NEEDED,checker.checkSteps(question(),List.of("x>=-5/2"),List.of(PARTIAL)).status);
        assertTrue(WorkAnswer.writtenAnswers(question(),List.of("2<3"),List.of(PARTIAL)).isEmpty());
        assertTrue(checker.checkSteps(question(),List.of("8-3=5","x>=-5/2"),List.of(UNSPECIFIED,FULL)).correct());
    }
    @Test public void copiesOnlyExplicitIsolatedStudentValues(){
        assertEquals(List.of(">=","-10/4"),WorkAnswer.writtenAnswers(question(),List.of("x ≥ -10/4"),List.of(FULL)));
        assertEquals(List.of(">=","-2.5"),WorkAnswer.writtenAnswers(question(),List.of("-2.5 ≤ x"),List.of(FULL)));
        assertTrue(WorkAnswer.writtenAnswers(question(),List.of("-2x<=5"),List.of(FULL)).isEmpty());
        assertTrue(WorkAnswer.writtenAnswers(question(),List.of("x<=-5/2"),List.of(FULL)).isEmpty());
        assertEquals("",WorkAnswer.writtenAnswer(question(),List.of("x>=-5/2"),List.of(FULL)));
    }
    @Test public void checkingUsesGivenInequalityEvenWithCorruptedStoredAnswer(){
        Question q=question();q.answers=new String[]{"<","999"};
        assertTrue(checker.checkSteps(q,List.of("x>=-5/2"),List.of(FULL)).correct());
        assertTrue(checker.check(q,List.of(),List.of(">=","-5/2")).correct());
        for(int a=-9;a<=9;a++)if(a!=0){String op=a>0?"<=":">=";String row=(-2*a)+"x + ("+(3*a)+") "+op+" "+(8*a);assertTrue(row,checker.checkSteps(q,List.of(row),List.of(FULL)).correct());}
    }
    @Test public void currentUnitExclusionAndSavedSignFractionDraftArePreserved()throws Exception{
        Learning.State s=new Learning.State();s.profile.grade=8;s.profile.term=1;s.profile.currentSkill="linearInequality";
        assertFalse(Learning.diagnosticScope(s.profile).stream().anyMatch(k->k.id.equals("linearInequality")));assertTrue(Learning.learningScope(s.profile).stream().anyMatch(k->k.id.equals("linearInequality")));
        s.profile.currentSkill="";s.profile.term=2;assertTrue(Learning.diagnosticScope(s.profile).stream().anyMatch(k->k.id.equals("linearInequality")));s.profile.excluded.add("linearInequality");assertFalse(Learning.diagnosticScope(s.profile).stream().anyMatch(k->k.id.equals("linearInequality")));
        Random random=new Random(11);Learning.beginPractice(s,"practice",List.of("linearInequality"),20,false,random);Learning.ensureQuestion(s,new Generator(random));s.session.answers.set(0,">=");s.session.answers.set(1,"-5/2");FractionInput.form(s.session,1).fraction=true;s.session.answerFocus=1;s.session.steps.add("x □ -5/2");s.session.stepKinds.add(FULL);s.session.workOpen=true;String id=s.session.question.id;
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();try(ObjectOutputStream out=new ObjectOutputStream(bytes)){out.writeObject(s);}try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))){s=(Learning.State)in.readObject();}
        assertEquals(id,s.session.question.id);assertEquals(List.of(">=","-5/2"),s.session.answers);assertTrue(FractionInput.form(s.session,1).fraction);assertEquals(List.of("x □ -5/2"),s.session.steps);assertArrayEquals(new String[]{"x","□","-5/2"},LinearInequality.parts(s.session.steps.get(0)));assertEquals(8,s.profile.grade);assertTrue(s.session.workOpen);
    }
}
