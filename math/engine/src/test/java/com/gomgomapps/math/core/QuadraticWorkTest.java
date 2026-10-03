package com.gomgomapps.math.core;

import java.io.*;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class QuadraticWorkTest {
    private final Checker checker=new Checker();
    private static final Checker.StepKind FULL=Checker.StepKind.FULL,PARTIAL=Checker.StepKind.PARTIAL;
    private static Question q(String expression,String...answers){Question q=new Question("quadratic",expression,expression,answers);q.kind="roots";q.labels=answers.length==1?new String[]{"해"}:new String[]{"해 1","해 2"};return q;}
    @Test public void answersAcceptExactRadicalsCommonFractionsAndEitherOrder(){
        Question q=q("2x^2-3x-1=0","(3-√17)/4","(3+√17)/4");
        assertTrue(checker.check(q,List.of(),List.of("x=(3+√17)/4","(3-√17)/4")).correct());
        assertEquals(Checker.Status.WRONG_ANSWER,checker.check(q,List.of(),List.of(q.answers[0],q.answers[0])).status);
        assertEquals(Checker.Status.INPUT_NEEDED,checker.check(q,List.of(),List.of("(6-√68)/8",q.answers[1])).status);
        assertEquals(Checker.Status.INPUT_NEEDED,checker.check(q,List.of(),List.of(q.answers[0],"")).status);
        assertEquals(Checker.Status.WRONG_ANSWER,checker.check(q,List.of(),List.of("-0.2807764064",q.answers[1])).status);
        Question negative=q("x^2+x-1=0","(-1-√5)/2","(-1+√5)/2");
        assertTrue(checker.check(negative,List.of(),List.of("-(1+√5)/2","(√5-1)/2")).correct());
    }
    @Test public void zeroAndRepeatedRootsHaveTheirOwnAnswerCount(){
        Question zero=q("x^2-3x=0","0","3");
        assertTrue(checker.check(zero,List.of(),List.of("3","0")).correct());
        Question repeated=q("4x^2-12x+9=0","3/2");
        assertTrue(checker.check(repeated,List.of("(2x-3)^2=0","x=3/2"),List.of("3/2"),List.of(FULL,FULL)).correct());
        assertTrue(checker.check(q("x^2=0","0"),List.of("x=0"),List.of("0"),List.of(FULL)).correct());
    }
    @Test public void branchCollectionCannotLoseOrAddARoot(){
        Question q=q("x^2-3x=0","0","3");
        assertEquals(Checker.Status.INPUT_NEEDED,checker.checkSteps(q,List.of("x=3"),List.of(FULL)).status);
        assertTrue(checker.check(q,List.of("x=3"),List.of("3","0"),List.of(FULL)).correct());
        assertTrue(checker.checkSteps(q,List.of("x=3","x=0"),List.of(FULL,FULL)).correct());
        assertEquals(Checker.Status.WRONG_STEP,checker.checkSteps(q,List.of("x-3=0"),List.of(FULL)).status);
        assertEquals(Checker.Status.WRONG_STEP,checker.checkSteps(q,List.of("x=0 또는 x=3 또는 x=5"),List.of(FULL)).status);
        assertEquals(Checker.Status.WRONG_STEP,checker.checkSteps(q,List.of("0=0"),List.of(FULL)).status);
        assertEquals(Checker.Status.INPUT_NEEDED,checker.checkSteps(q,List.of("x=3","x=3"),List.of(FULL,FULL)).status);
    }
    @Test public void equivalentEquationsAndPlusMinusDoNotConsultTheAnswerKey(){
        Question q=q("x^2-2x-1=0","1-√2","1+√2");
        assertTrue(checker.checkSteps(q,List.of("(x-1)^2=2","x=1±√2"),List.of(FULL,FULL)).correct());
        assertTrue(checker.checkSteps(q,List.of("(x-(1-√2))*(x-(1+√2))=0"),List.of(FULL)).correct());
        q.answers=new String[]{"1000","2000"};
        assertTrue(checker.checkSteps(q,List.of("x=1-√2,1+√2"),List.of(FULL)).correct());
        assertEquals(Checker.Status.WRONG_STEP,checker.checkSteps(q,List.of("x=1000,2000"),List.of(FULL)).status);
    }
    @Test public void partialAndUnsupportedWorkCannotCompleteOrAutofill(){
        Question q=q("x^2-2=0","-√2","√2");
        assertTrue(checker.checkSteps(q,List.of("√8=2√2"),List.of(PARTIAL)).correct());
        assertTrue(WorkAnswer.writtenAnswers(q,List.of("√8=2√2"),List.of(PARTIAL)).isEmpty());
        assertTrue(WorkAnswer.writtenAnswers(q,List.of("x^2=2"),List.of(FULL)).isEmpty());
        assertTrue(WorkAnswer.writtenAnswers(q,List.of("x=√2"),List.of(FULL)).isEmpty());
        assertEquals(List.of("-√2","+√2"),WorkAnswer.writtenAnswers(q,List.of("x=±√2"),List.of(FULL)));
        for(String row:List.of("(x^2-2)^2=0","x/x=1","x=√(-1)","x=±±√2","x=√()"))assertEquals(row,Checker.Status.INPUT_NEEDED,checker.checkSteps(q,List.of(row),List.of(FULL)).status);
        Question formula=q("2x^2-3x-1=0","(3-√17)/4","(3+√17)/4");
        assertEquals(List.of("(3-√17)/4","(3+√17)/4"),WorkAnswer.writtenAnswers(formula,List.of("x=(3±√17)/4"),List.of(FULL)));
    }
    @Test public void olderOrderedQuestionsKeepTheirLabelsAndWrittenValues(){
        Question q=q("x^2-9=0","-3","3");q.labels=new String[]{"작은 해","큰 해"};
        assertEquals(Checker.Status.WRONG_ANSWER,checker.check(q,List.of(),List.of("3","-3")).status);
        assertEquals(List.of("-3","3"),WorkAnswer.writtenAnswers(q,List.of("x=3","x=-3"),List.of(FULL,FULL)));
    }
    @Test public void generatedQuestionsCoverDistinctRationalRepeatedZeroAndIrrationalRoots(){
        Generator g=new Generator(new Random(6090910));int repeated=0,zero=0,irrational=0,fraction=0;Set<String> prompts=new HashSet<>(),answers=new HashSet<>();List<String> recent=new ArrayList<>();
        for(int i=0;i<800;i++){
            Question q=g.next("quadratic",recent,true);assertFalse(recent.contains(q.signature()));recent.add(q.signature());if(recent.size()>40)recent.remove(0);prompts.add(q.prompt);answers.add(Arrays.toString(q.answers));
            assertTrue(q.prompt,checker.check(q,List.of(),List.of(q.answers)).correct());assertTrue(q.choices.isEmpty());
            if(q.answers.length==1)repeated++;if(Arrays.asList(q.answers).contains("0"))zero++;if(Arrays.toString(q.answers).contains("√"))irrational++;if(Arrays.toString(q.answers).contains("/"))fraction++;
        }
        assertTrue(repeated>100);assertTrue(zero>60);assertTrue(irrational>100);assertTrue(fraction>100);assertTrue(prompts.size()>450);assertTrue(answers.size()>400);
        assertTrue(Catalog.foundationOrder("quadratic").contains("rootRationalize"));
        Learning.Profile p=new Learning.Profile();p.grade=9;p.term=1;p.currentSkill="quadratic";assertFalse(Learning.diagnosticScope(p).stream().anyMatch(s->s.id.equals("quadratic")));
    }
    @Test public void theExistingSaveFormatPreservesRepeatedRootAndBranchWork()throws Exception{
        Learning.State state=new Learning.State();Learning.beginPractice(state,"homework",List.of("quadratic"),10,false,new Random(2));Learning.ensureQuestion(state,new Generator(new Random(3)));
        state.session.question=q("4x^2-12x+9=0","3/2");state.session.answers.clear();state.session.answers.add("3/2");state.session.steps.add("x=3/2");state.session.stepKinds.add(FULL);
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();new ObjectOutputStream(bytes).writeObject(LearningSnapshot.capture(state));Learning.State restored=(Learning.State)new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray())).readObject();
        assertEquals(1,restored.version);assertEquals(1,restored.session.answers.size());assertTrue(checker.check(restored.session.question,restored.session.steps,restored.session.answers,restored.session.stepKinds).correct());
    }
}
