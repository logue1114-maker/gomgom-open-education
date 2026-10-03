package com.gomgomapps.math.core;

import java.io.*;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class ComplexQuadraticWorkTest {
    private final Checker checker=new Checker();
    private static final Checker.StepKind FULL=Checker.StepKind.FULL,PARTIAL=Checker.StepKind.PARTIAL;
    private static Question q(String expression,String...answers){Question q=new Question("quadraticComplex",expression,expression,answers);q.kind="complexRoots";q.labels=answers.length==1?new String[]{"해"}:new String[]{"해 1","해 2"};return q;}
    @Test public void acceptsEitherOrderWithRadicalImaginaryCoefficientsAndCommonFractions(){
        Question q=q("2x^2+x+3=0","(-1-√23i)/4","(-1+√23i)/4");
        assertTrue(checker.check(q,List.of(),List.of("(-1+i√23)/4","-(1+i√23)/4")).correct());
        assertTrue(checker.check(q,List.of(),List.of("-1/4-√23i/4","-1/4+√23i/4")).correct());
        assertEquals(Checker.Status.WRONG_ANSWER,checker.check(q,List.of(),List.of(q.answers[0],q.answers[0])).status);
        assertEquals(Checker.Status.INPUT_NEEDED,checker.check(q,List.of(),List.of("(-2-2√23i)/8",q.answers[1])).status);
        assertEquals(Checker.Status.WRONG_ANSWER,checker.check(q,List.of(),List.of("(-1-√23)/4",q.answers[1])).status);
    }
    @Test public void finalFormCollectsRootsWithoutRejectingValidImaginaryUnitOrder(){
        for(String s:List.of("(1+i√3)/2","-(1+√3i)/2","(√2+√3i)/2","1+√2+i","(1+√2)i","2i√3/5","i*3","i√3"))assertTrue(s,Complex.simplified(s));
        for(String s:List.of("(2+2i√3)/4","(√8+√12i)/2","1+2+i","i+i","1/i","(1+i)/1"))assertFalse(s,Complex.simplified(s));
    }
    @Test public void complexFactoringAndNonzeroComplexMultiplesPreserveTheSolutionSet(){
        Question q=q("x^2+1=0","-i","i");
        for(String row:List.of("x^2=-1","(x-i)*(x+i)=0","i*x^2+i=0","(1+i)*(x^2+1)=0"))assertTrue(row,checker.checkSteps(q,List.of(row),List.of(FULL)).correct());
        for(String row:List.of("x^2=1","(x-i)^2=0","x-i=0","0=0"))assertEquals(row,Checker.Status.WRONG_STEP,checker.checkSteps(q,List.of(row),List.of(FULL)).status);
        Question shifted=q("2x^2+x+3=0","(-1-√23i)/4","(-1+√23i)/4");
        assertTrue(checker.checkSteps(shifted,List.of("(x+1/4)^2=-23/16"),List.of(FULL)).correct());
    }
    @Test public void lostOrAddedBranchesAreNotAcceptedAndTheKeyCannotDriveWork(){
        Question q=q("x^2+1=0","-i","i");
        assertEquals(Checker.Status.INPUT_NEEDED,checker.checkSteps(q,List.of("x=i"),List.of(FULL)).status);
        assertTrue(checker.check(q,List.of("x=i"),List.of("i","-i"),List.of(FULL)).correct());
        assertTrue(checker.checkSteps(q,List.of("x=i","x=-i"),List.of(FULL,FULL)).correct());
        assertEquals(Checker.Status.WRONG_STEP,checker.checkSteps(q,List.of("x=±i,1"),List.of(FULL)).status);
        q.answers=new String[]{"100","200"};assertTrue(checker.checkSteps(q,List.of("x=±i"),List.of(FULL)).correct());
        assertEquals(Checker.Status.WRONG_STEP,checker.checkSteps(q,List.of("x=100,200"),List.of(FULL)).status);
    }
    @Test public void onlyStudentWrittenCompleteRootsCanBeCopied(){
        Question q=q("2x^2+x+3=0","(-1-√23i)/4","(-1+√23i)/4");
        assertEquals(List.of("(-1-√23i)/4","(-1+√23i)/4"),WorkAnswer.writtenAnswers(q,List.of("x=(-1±√23i)/4"),List.of(FULL)));
        assertTrue(WorkAnswer.writtenAnswers(q,List.of("x=(-1+√23i)/4"),List.of(FULL)).isEmpty());
        assertTrue(WorkAnswer.writtenAnswers(q,List.of("i^2=-1"),List.of(PARTIAL)).isEmpty());
        assertTrue(WorkAnswer.writtenAnswers(q,List.of("(x+1/4)^2=-23/16"),List.of(FULL)).isEmpty());
        for(String row:List.of("x/x=1","x^3+x=0","x=√(2i)","i^i=1","x=±±i","x=√()"))assertEquals(row,Checker.Status.INPUT_NEEDED,checker.checkSteps(q,List.of(row),List.of(FULL)).status);
    }
    @Test public void highSchoolRepeatedRealRootsStillAcceptEquivalentLinearWork(){
        Question q=q("4x^2-12x+9=0","3/2");
        assertTrue(checker.checkSteps(q,List.of("2x-3=0","x=3/2"),List.of(FULL,FULL)).correct());
        assertTrue(checker.check(q,List.of(),List.of("3/2")).correct());
        Question middle=new Question("quadratic","x²+1=0","x^2+1=0","0");middle.kind="roots";
        assertEquals(Checker.Status.INPUT_NEEDED,checker.checkSteps(middle,List.of("x=i"),List.of(FULL)).status);
    }
    @Test public void generatedQuestionsIncludeRealImaginaryRationalAndIrrationalCases(){
        Generator g=new Generator(new Random(6090930));Set<String> prompts=new HashSet<>(),answers=new HashSet<>();List<String> recent=new ArrayList<>();int real=0,imaginary=0,repeated=0,root=0,fraction=0;
        for(int n=0;n<800;n++){Question q=g.next("quadraticComplex",recent,true);assertFalse(recent.contains(q.signature()));recent.add(q.signature());if(recent.size()>40)recent.remove(0);assertEquals("방정식을 푸세요. (i² = −1)",q.prompt.split("\n")[0]);assertTrue(q.prompt,checker.check(q,List.of(),List.of(q.answers)).correct());String key=Arrays.toString(q.answers);if(key.contains("i"))imaginary++;else real++;if(q.answers.length==1)repeated++;if(key.contains("√"))root++;if(key.contains("/"))fraction++;prompts.add(q.prompt);answers.add(key);}
        assertTrue(real>120);assertTrue(imaginary>500);assertTrue(repeated>20);assertTrue(root>200);assertTrue(fraction>200);assertTrue(prompts.size()>500);assertTrue(answers.size()>400);
        assertTrue(Catalog.foundationOrder("quadraticComplex").contains("complexDivide"));assertEquals(Catalog.ALL.size(),Catalog.ALL.stream().map(s->s.id).distinct().count());
    }
    @Test public void existingSaveFormatRetainsTwoImaginaryRootsAndTheirWork()throws Exception{
        Learning.State state=new Learning.State();Learning.beginPractice(state,"homework",List.of("quadraticComplex"),10,false,new Random(2));Learning.ensureQuestion(state,new Generator(new Random(3)));
        state.session.question=q("x^2+1=0","-i","i");state.session.answers.clear();state.session.answers.addAll(List.of("i","-i"));state.session.steps.add("x=±i");state.session.stepKinds.add(FULL);
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();new ObjectOutputStream(bytes).writeObject(LearningSnapshot.capture(state));Learning.State restored=(Learning.State)new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray())).readObject();assertEquals(1,restored.version);assertTrue(checker.check(restored.session.question,restored.session.steps,restored.session.answers,restored.session.stepKinds).correct());
    }
}
