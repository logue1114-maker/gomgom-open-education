package com.gomgomapps.math.core;

import java.io.*;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class ComplexWorkTest {
    private final Checker checker=new Checker();
    private static final Checker.StepKind FULL=Checker.StepKind.FULL,PARTIAL=Checker.StepKind.PARTIAL;
    private static Question q(String expression,String answer){Question q=new Question("complexDivide",expression,expression,answer);q.kind="complex";return q;}
    @Test public void exactArithmeticSupportsSignsProductsConjugatesAndPowers(){
        String[][] cases={{"(2+3i)+(4-5i)","6-2i"},{"(2+3i)-(4-5i)","-2+8i"},{"(2+3i)*(4-5i)","23+2i"},{"(2+3i)/(4-5i)","(-7+22i)/41"},{"1/i","-i"},{"i^200","1"},{"i^(4*20+3)","-i"},{"-i^2","1"},{"(-i)^2","-1"},{"i^-3","i"},{"√(-8)*√(-2)","-4"},{"(√2+i)/(√2-i)","1/3+2√2i/3"}};
        for(String[] c:cases)assertEquals(c[0],Complex.parse(c[1]),Complex.parse(c[0]));
        try{Expression.number("i^2");fail("The real-only parser must remain real-only");}catch(IllegalArgumentException expected){}
    }
    @Test public void finalAnswersMustBeCollectedButEquivalentFractionsAreAllowed(){
        for(String s:List.of("6-2i","-2i+6","(1+2i)/3","-(1+2i)/3","1/2+3i/2","(1/2)i","i","-i","0","√2+i","1/3+2√2i/3"))assertTrue(s,Complex.simplified(s));
        for(String s:List.of("(2+4i)/6","2+3+4i","i+i","i^3","(2+i)*(3-i)","1/i","0+i","0i","2/4+i"))assertFalse(s,Complex.simplified(s));
        Question q=q("(2+3i)/(4-5i)","(-7+22i)/41");
        assertTrue(checker.check(q,List.of(),List.of("-7/41+22i/41")).correct());
        assertEquals(Checker.Status.WRONG_ANSWER,checker.check(q,List.of(),List.of("-7/41-22i/41")).status);
        assertEquals(Checker.Status.INPUT_NEEDED,checker.check(q,List.of(),List.of(q.expression)).status);
        assertEquals(Checker.Status.INPUT_NEEDED,checker.check(q,List.of(),List.of("(-14+44i)/82")).status);
    }
    @Test public void fullWorkComparesToTheProblemAndMarksOnlyTheFirstIncorrectPart(){
        Question q=q("(2+3i)*(4-5i)","23+2i");
        assertTrue(checker.checkSteps(q,List.of("8-10i+12i-15i^2=23+2i"),List.of(FULL)).correct());
        Checker.Result bad=checker.checkSteps(q,List.of("8-10i+12i-15i^2=-7+2i"),List.of(FULL));
        assertEquals(Checker.Status.WRONG_STEP,bad.status);assertEquals(0,bad.index);assertEquals(1,bad.part);assertEquals("이 칸 확인",bad.message);
        q.answers=new String[]{"100+i"};assertTrue(checker.checkSteps(q,List.of("23+2i"),List.of(FULL)).correct());
        assertEquals(Checker.Status.WRONG_STEP,checker.checkSteps(q,List.of("100+i"),List.of(FULL)).status);
    }
    @Test public void partialWorkAndUnfinishedExpressionsCannotAutoFillAnAnswer(){
        Question q=q("(2+3i)*(4-5i)","23+2i");
        assertTrue(checker.checkSteps(q,List.of("i^2=-1"),List.of(PARTIAL)).correct());
        assertTrue(WorkAnswer.writtenAnswers(q,List.of("i^2=-1"),List.of(PARTIAL)).isEmpty());
        assertTrue(WorkAnswer.writtenAnswers(q,List.of("8-10i+12i-15i^2"),List.of(FULL)).isEmpty());
        assertEquals(List.of("23+2i"),WorkAnswer.writtenAnswers(q,List.of("8-10i+12i-15i^2=23+2i"),List.of(FULL)));
        for(String s:List.of("1/(i-i)","i+","i^5000","√()","x+i","√(2i)","0^0","(".repeat(40)+"i"+")".repeat(40)))assertEquals(s,Checker.Status.INPUT_NEEDED,checker.checkSteps(q,List.of(s),List.of(FULL)).status);
    }
    @Test public void generationVariesPromptsAndResultsWithoutAForcedAnswerCycle(){
        Generator g=new Generator(new Random(6090920));
        for(String id:ComplexWork.SKILLS){Set<String> prompts=new HashSet<>(),answers=new HashSet<>();List<String> recent=new ArrayList<>();boolean repeatedAnswer=false;String previous="";
            for(int n=0;n<400;n++){Question q=g.next(id,recent,true);assertFalse(recent.contains(q.signature()));recent.add(q.signature());if(recent.size()>40)recent.remove(0);prompts.add(q.prompt);answers.add(q.answers[0]);if(previous.equals(q.answers[0]))repeatedAnswer=true;previous=q.answers[0];assertTrue(q.prompt,checker.check(q,List.of(),List.of(q.answers)).correct());assertTrue(q.choices.isEmpty());}
            assertTrue(id,prompts.size()>150);assertTrue(id,answers.size()>(id.equals("imaginaryPower")?3:120));if(id.equals("imaginaryPower"))assertTrue(repeatedAnswer);
        }
    }
    @Test public void coursePrerequisitesAndDiagnosticExclusionsUseTheNormalCatalog(){
        for(String id:ComplexWork.SKILLS){Catalog.Skill s=Catalog.get(id);assertEquals("공통수학 1",s.course);assertEquals(10,s.grade);assertEquals(1,s.term);assertEquals(2,s.unit);assertFalse(Catalog.foundationOrder(id).isEmpty());}
        Learning.Profile p=new Learning.Profile();p.grade=10;p.term=1;p.currentSkill="complexDivide";
        assertFalse(Learning.diagnosticScope(p).stream().anyMatch(s->ComplexWork.SKILLS.contains(s.id)));
    }
    @Test public void homeworkSaveRestoresComplexInputWithoutChangingTheSaveVersion()throws Exception{
        Learning.State state=new Learning.State();Learning.beginPractice(state,"homework",List.of("complexDivide"),10,false,new Random(2));Learning.ensureQuestion(state,new Generator(new Random(3)));
        state.session.question=q("(2+3i)/(4-5i)","(-7+22i)/41");state.session.answers.clear();state.session.answers.add("(-7+22i)/41");state.session.steps.add("(2+3i)*(4+5i)/41=(-7+22i)/41");state.session.stepKinds.add(FULL);
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();new ObjectOutputStream(bytes).writeObject(LearningSnapshot.capture(state));Learning.State restored=(Learning.State)new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray())).readObject();
        assertEquals(1,restored.version);assertEquals("complex",restored.session.question.kind);assertTrue(checker.check(restored.session.question,restored.session.steps,restored.session.answers,restored.session.stepKinds).correct());
    }
}
