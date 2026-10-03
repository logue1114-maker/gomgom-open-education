package com.gomgomapps.math.core;

import java.io.*;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class RadicalTest {
    private final Checker checker=new Checker();
    private static final Checker.StepKind FULL=Checker.StepKind.FULL,PARTIAL=Checker.StepKind.PARTIAL;
    private static Question question(){Question q=new Question("rootSimplify","근호 안의 수를 간단히 하세요.\n√(72)","√(72)","6√(2)");q.kind="radical";return q;}
    private void equal(String left,String right){assertEquals(left,Radical.parse(right),Radical.parse(left));}
    @Test public void arithmeticIsExactAcrossEquivalentRootForms(){
        equal("√72","6sqrt(2)");equal("√8+√18","5√2");equal("√8-2√2","0");equal("√2*√3","√6");
        equal("√18/√12","√6/2");equal("√(2/3)","√6/3");equal("-√9^2","-9");equal("(√2)^2","2");
        equal("1/√2","√2/2");equal("1/(1+√2)","√2-1");equal("(√3+√2)/(√3-√2)","5+2√6");
        equal("(√2+√3+√6)/(√2+√3+√6)","1");equal("(√2)^(-2)","1/2");
        assertNotEquals(Radical.parse("1.414213562373"),Radical.parse("√2"));
    }
    @Test public void undefinedAndUnsupportedInputNeverBecomesAnApproximation(){
        for(String raw:List.of("√(-1)","1/(√8-2√2)","√(√2)","√1000001","x+√2","0^0","√()","√(2","9^9")){
            try{Radical.parse(raw);fail(raw);}catch(IllegalArgumentException|ArithmeticException expected){}
        }
        assertEquals(Checker.Status.INPUT_NEEDED,checker.check(question(),List.of(),List.of("√(-1)")).status);
    }
    @Test public void finalFormAllowsWrittenFractionsAndReorderedDistinctTerms(){
        for(String raw:List.of("6√2","6*sqrt(2)","(3/2)√2","(-3/2)√2","2+(-3/2)√2","(√2+√3)/2","(1+√5)/2","3√2/2","(3√2)/2","(√2)/2","sqrt2/2","-√3+2","2-√3","0","-1/2","1.5"))assertTrue(raw,Radical.simplified(raw));
        for(String raw:List.of("√72","3√2+3√2","√8+√18","2*3√2","1/√2","2√2/4","(2+2√3)/2","(1+√2)/1","0√2","√1","2/4","6√2+0"))assertFalse(raw,Radical.simplified(raw));
    }
    @Test public void checkingMarksTheFirstIncorrectPartWithoutSupplyingAnAnswer(){
        Question q=question();Checker.Result result=checker.checkSteps(q,List.of("√72=3√8=5√2"),List.of(FULL));
        assertEquals(Checker.Status.WRONG_STEP,result.status);assertEquals(0,result.index);assertEquals(2,result.part);assertFalse(result.message.contains("6"));
        result=checker.checkSteps(q,List.of("√72=√(36*2)=6√2"),List.of(FULL));assertTrue(result.correct());
        q.answers[0]="1000";assertTrue(checker.checkSteps(q,List.of("6√2"),List.of(FULL)).correct());
    }
    @Test public void partialWorkAndUnfinishedCalculationsDoNotFillTheAnswer(){
        Question q=question();assertTrue(checker.checkSteps(q,List.of("√3+√3=2√3"),List.of(PARTIAL)).correct());
        assertTrue(WorkAnswer.writtenAnswers(q,List.of("√3+√3=2√3"),List.of(PARTIAL)).isEmpty());
        assertTrue(WorkAnswer.writtenAnswers(q,List.of("√72=3√8"),List.of(FULL)).isEmpty());
        assertEquals(List.of("6√2"),WorkAnswer.writtenAnswers(q,List.of("√72=6√2"),List.of(FULL)));
        assertEquals(Checker.Status.INPUT_NEEDED,checker.check(q,List.of(),List.of("3√8")).status);
        assertEquals(Checker.Status.WRONG_ANSWER,checker.check(q,List.of(),List.of("7√2")).status);
        assertTrue(checker.check(q,List.of(),List.of("6*sqrt2")).correct());
        assertEquals(Checker.Status.WRONG_STEP,checker.checkSteps(q,List.of("√3+√3=2√3"),List.of(FULL)).status);
    }
    @Test public void newUnitRespectsPriorTermAndExcludedContent(){
        Learning.Profile p=new Learning.Profile();p.grade=9;p.term=1;p.schoolYear=2026;
        assertFalse(Learning.diagnosticScope(p).stream().anyMatch(s->RadicalWork.SKILLS.contains(s.id)));
        p.currentSkill="rootRationalize";assertFalse(Learning.diagnosticScope(p).stream().anyMatch(s->RadicalWork.SKILLS.contains(s.id)));
        p.currentSkill="";p.term=2;Set<String> ids=new HashSet<>();for(Catalog.Skill s:Learning.diagnosticScope(p))ids.add(s.id);assertTrue(ids.containsAll(RadicalWork.SKILLS));
        p.excluded.add("rootProduct");assertFalse(Learning.diagnosticScope(p).stream().anyMatch(s->s.id.equals("rootProduct")));
        for(String id:RadicalWork.SKILLS)assertTrue(Catalog.foundationOrder(id).contains("root"));
    }
    @Test public void generatedFormsChoicesAndRoundTripsStayConsistent(){
        Generator g=new Generator(new Random(6090906));
        for(String id:RadicalWork.SKILLS){
            Set<String> answers=new HashSet<>();List<String> recent=new ArrayList<>();int[] positions=new int[4],ranks=new int[4];int choices=0;
            for(int i=0;i<180;i++){
                Question q=g.next(id,recent,true);assertFalse(recent.contains(q.signature()));recent.add(q.signature());if(recent.size()>40)recent.remove(0);
                answers.add(q.answers[0]);assertTrue(q.prompt,checker.check(q,List.of(),List.of(q.answers)).correct());equal(q.expression,q.answers[0]);assertTrue(Radical.simplified(q.answers[0]));
                assertEquals(List.of(q.answers[0]),WorkAnswer.writtenAnswers(q,List.of(q.expression+"="+q.answers[0]),List.of(FULL)));
                if(!q.choices.isEmpty()){
                    choices++;assertEquals(4,q.choices.size());positions[q.correctChoice]++;Set<Radical> values=new HashSet<>();int correct=0;
                    int rank=0;
                    for(String choice:q.choices){assertTrue(Radical.simplified(choice));values.add(Radical.parse(choice));assertEquals(q.answers[0].contains("/"),choice.contains("/"));if(Radical.parse(choice).choiceMagnitude()<Radical.parse(q.answers[0]).choiceMagnitude())rank++;if(checker.check(q,List.of(),List.of(choice)).correct())correct++;}
                    ranks[rank]++;
                    assertEquals(4,values.size());assertEquals(1,correct);
                }
            }
            assertTrue(id,answers.size()>25);assertTrue(id,choices>140);for(int position:positions)assertTrue(id+Arrays.toString(positions),position>15);for(int rank:ranks)assertTrue(id+Arrays.toString(ranks),rank>15);
        }
    }
    @Test public void savedQuestionAndWorkUseTheExistingStateFormat()throws Exception{
        Learning.State state=new Learning.State();Learning.beginPractice(state,"homework",List.of("rootSimplify"),10,false,new Random(77));
        Learning.ensureQuestion(state,new Generator(new Random(78)));state.session.question=question();state.session.steps.add("√72=6√2");state.session.stepKinds.add(FULL);state.session.answers.set(0,"6√2");
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();new ObjectOutputStream(bytes).writeObject(LearningSnapshot.capture(state));
        Learning.State copy=(Learning.State)new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray())).readObject();
        assertEquals(1,copy.version);assertTrue(checker.check(copy.session.question,copy.session.steps,copy.session.answers,copy.session.stepKinds).correct());
        assertFalse(FractionInput.available(copy.session.question));assertEquals(0,copy.session.completed);
    }
}
