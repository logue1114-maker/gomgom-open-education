package com.gomgomapps.math.core;

import java.io.*;
import java.time.LocalDate;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class FunctionWorkTest {
    private final Checker checker=new Checker();
    private static final Checker.StepKind FULL=Checker.StepKind.FULL,PARTIAL=Checker.StepKind.PARTIAL;
    private Question value(){Question q=new Question("linearValue","y = 2x + 1\nx = 3일 때, y의 값은?","2*3+1","7");q.resultSymbol="y";q.givenNumbers.put("x","3");return q;}
    private Question xIntercept(){Question q=new Question("linearXIntercept","y = 2x + 4의 그래프에서\nx절편은?","(0-4)/2","-2");q.resultSymbol="x";q.givenNumbers.put("y","0");return q;}
    @Test public void givenVariablesAndFirstIncorrectPartAreCheckedWithoutAnswerDisclosure(){
        Question q=value();assertTrue(checker.checkSteps(q,List.of("y = 2x + 1 = 7"),List.of(FULL)).correct());
        Checker.Result wrong=checker.checkSteps(q,List.of("y = 2x + 1 = 8"),List.of(FULL));assertEquals(Checker.Status.WRONG_STEP,wrong.status);assertEquals(0,wrong.index);assertEquals(2,wrong.part);assertFalse(wrong.message.contains("7"));
        wrong=checker.checkSteps(q,List.of("y = 2*4 + 1 = 9"),List.of(FULL));assertEquals(Checker.Status.WRONG_STEP,wrong.status);assertEquals(1,wrong.part);
        assertTrue(checker.checkSteps(q,List.of("2x = 6"),List.of(PARTIAL)).correct());
        Checker.Result unknown=checker.checkSteps(q,List.of("y = 2z + 1"),List.of(FULL));assertEquals(Checker.Status.INPUT_NEEDED,unknown.status);assertFalse(unknown.mathematicalError());
        q.answers[0]="1000";assertTrue(checker.checkSteps(q,List.of("y=7"),List.of(FULL)).correct());
    }
    @Test public void interceptEquationsCanBeTransformedBeforeWritingTheFinalCoordinate(){
        Question q=xIntercept();List<String> work=List.of("y=2x+4","0=2x+4","-4=2x","x=-2");List<Checker.StepKind> kinds=List.of(FULL,FULL,FULL,FULL);
        assertTrue(checker.checkSteps(q,work,kinds).correct());assertEquals(List.of("-2"),WorkAnswer.writtenAnswers(q,work,kinds));
        assertTrue(checker.check(q,work,List.of("x=-2"),kinds).correct());
        assertEquals(Checker.Status.WRONG_STEP,checker.checkSteps(q,List.of("x=2"),List.of(FULL)).status);
        Question y=new Question("linearYIntercept","y=3x-4의 그래프에서 y절편은?","3*0-4","-4");y.resultSymbol="y";y.givenNumbers.put("x","0");
        assertTrue(checker.checkSteps(y,List.of("y=3x-4=-4"),List.of(FULL)).correct());assertTrue(checker.check(y,List.of(),List.of("y=-4")).correct());
    }
    @Test public void templatesIdentitiesAndPartialWorkNeverFillTheFinalAnswer(){
        Question q=value();
        for(String row:List.of("","y=2x+1","y=y","2*3+1=","2x=6"))assertTrue(row,WorkAnswer.writtenAnswers(q,List.of(row),List.of(FULL)).isEmpty());
        assertTrue(WorkAnswer.writtenAnswers(q,List.of("3+4=7"),List.of(PARTIAL)).isEmpty());
        assertEquals(List.of("7.0"),WorkAnswer.writtenAnswers(q,List.of("y=7.0"),List.of(FULL)));
        assertEquals(Checker.Status.INPUT_NEEDED,checker.check(q,List.of(),List.of("y")).status);
    }
    @Test public void fullTransformationsPreserveTheUnknownInsteadOfTestingOnlyItsAnswer(){
        Question q=xIntercept();
        for(String row:List.of("x^2=4","x=x","0*x=0","(x+2)*(x-3)=0")){
            Checker.Result result=checker.checkSteps(q,List.of(row),List.of(FULL));
            assertEquals(row,Checker.Status.WRONG_STEP,result.status);
            assertEquals(row,-1,result.part);
            assertTrue(WorkAnswer.writtenAnswers(q,List.of(row,"x=-2"),List.of(FULL,FULL)).isEmpty());
        }
        assertTrue(checker.checkSteps(q,List.of("(x+2)^2=0","x=-2"),List.of(FULL,FULL)).correct());
        assertTrue(checker.checkSteps(q,List.of("x^2=4"),List.of(PARTIAL)).correct());
        assertEquals(Checker.Status.INPUT_NEEDED,checker.checkSteps(q,List.of("1/x=-1/2"),List.of(FULL)).status);
        assertEquals(Checker.Status.WRONG_STEP,checker.checkSteps(value(),List.of("y^2=49","y=7"),List.of(FULL,FULL)).status);
    }
    @Test public void middleScopeAndExplicitExclusionsControlTheNewUnit(){
        Learning.Profile profile=new Learning.Profile();profile.grade=8;profile.term=1;profile.schoolYear=2026;
        assertFalse(Learning.diagnosticScope(profile).stream().anyMatch(s->FunctionWork.SKILLS.contains(s.id)));
        profile.currentSkill="linearValue";assertFalse(Learning.diagnosticScope(profile).stream().anyMatch(s->FunctionWork.SKILLS.contains(s.id)));
        assertTrue(Learning.learningScope(profile).stream().anyMatch(s->s.id.equals("linearValue")));
        profile.currentSkill="";profile.term=2;Set<String> ids=new HashSet<>();for(Catalog.Skill skill:Learning.diagnosticScope(profile))ids.add(skill.id);assertTrue(ids.containsAll(FunctionWork.SKILLS));assertFalse(ids.contains("function"));
        profile.excluded.add("linearSlope");assertFalse(Learning.diagnosticScope(profile).stream().anyMatch(s->s.id.equals("linearSlope")));
        assertEquals(10,Catalog.get("function").grade);assertEquals(7,Catalog.get("substitute").grade);
        for(String id:FunctionWork.SKILLS){Catalog.Skill skill=Catalog.get(id);assertEquals(8,skill.grade);assertEquals(1,skill.term);assertFalse(Catalog.foundationOrder(id).isEmpty());}
    }
    @Test public void freshProblemsVaryGivensAnswersAndPositions(){
        Generator generator=new Generator(new Random(6090951));
        for(String id:FunctionWork.SKILLS){
            List<String> recent=new ArrayList<>();Set<String> answers=new HashSet<>();int[] positions=new int[4];int positive=0,negative=0,fraction=0;
            for(int i=0;i<400;i++){
                Question q=generator.next(id,recent,true);assertFalse(recent.contains(q.signature()));recent.add(q.signature());if(recent.size()>40)recent.remove(0);
                Rational answer=Expression.number(q.answers[0]);answers.add(q.answers[0]);if(answer.compareTo(Rational.ZERO)>0)positive++;if(answer.compareTo(Rational.ZERO)<0)negative++;if(!answer.isInteger())fraction++;
                assertEquals(4,q.choices.size());assertEquals(4,new HashSet<>(q.choices).size());positions[q.correctChoice]++;assertTrue(checker.check(q,List.of(),List.of(q.answers)).correct());
                assertEquals(q.prompt,answer,Expression.number(q.expression));
                if(!id.equals("linearSlope")){
                    String givenEquation=q.prompt.split("\n")[0].replace("의 그래프에서","");
                    assertTrue(q.prompt,checker.checkSteps(q,List.of(givenEquation),List.of(FULL)).correct());
                    assertTrue(WorkAnswer.writtenAnswers(q,List.of(givenEquation),List.of(FULL)).isEmpty());
                }
                assertTrue(checker.checkSteps(q,List.of(q.expression+"="+q.answers[0]),List.of(FULL)).correct());
                assertFalse(WorkAnswer.writtenAnswers(q,List.of(q.expression+"="+q.answers[0]),List.of(FULL)).isEmpty());
            }
            assertTrue(id,answers.size()>=20);assertTrue(id,positive>80&&negative>80);for(int n:positions)assertTrue(id+Arrays.toString(positions),n>55&&n<145);
            if(!id.equals("linearYIntercept"))assertTrue(id,fraction>30);
        }
    }
    @Test public void contextAndStudentInputSurviveSnapshotAndVersionOneReload()throws Exception{
        Learning.State state=new Learning.State();Learning.beginPractice(state,"homework",List.of("linearValue"),10,false,new Random(1));state.session.question=value();state.session.answers.add("7");state.session.steps.add("y=2x+1=7");state.session.stepKinds.add(FULL);
        Learning.State frozen=LearningSnapshot.capture(state);state.session.question.givenNumbers.put("x","4");
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();new ObjectOutputStream(bytes).writeObject(frozen);Learning.State loaded=(Learning.State)new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray())).readObject();
        assertEquals("3",loaded.session.question.givenNumbers.get("x"));assertEquals("y",loaded.session.question.resultSymbol);assertTrue(checker.checkSteps(loaded.session.question,loaded.session.steps,loaded.session.stepKinds).correct());
        assertEquals(0,loaded.session.completed);assertEquals(1,loaded.version);
        Learning.finishQuestion(loaded,false,LocalDate.of(2026,9,9),new Random(2));assertEquals(1,loaded.session.completed);
    }
    @Test public void otherQuestionKindsKeepTheirExistingVariableRules(){
        Question plain=new Question("add9","1+3","1+3","4");plain.givenNumbers=null;plain.resultSymbol=null;
        assertEquals(Checker.Status.INPUT_NEEDED,checker.check(plain,List.of(),List.of("y=4")).status);
        Question equation=new Question("linear","2x+4=0","2x+4=0","-2");equation.kind="equation";
        assertEquals(Checker.Status.INPUT_NEEDED,checker.checkSteps(equation,List.of("1/x=-1/2"),List.of(FULL)).status);
    }
}
