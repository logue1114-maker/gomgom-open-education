package com.gomgomapps.math.core;

import org.junit.Test;
import static org.junit.Assert.*;
import static com.gomgomapps.math.core.Checker.StepKind.*;
import java.util.*;
import java.util.regex.*;
import java.io.*;

public class LinearSystemTest {
    private final Checker checker=new Checker();
    private Question question(){Question q=new Question("linearSystem","2x + y = 7\nx - y = 2\nx, y의 값은?","2x+y=7;x-y=2","3","1");q.kind="system";q.labels=new String[]{"x","y"};return q;}
    @Test public void generatedPublicEquationsHaveExactlyOneIndependentlyEnumeratedAnswer(){
        Generator g=new Generator(new Random(6090911));Set<String> pairs=new HashSet<>(),xs=new HashSet<>(),ys=new HashSet<>();
        Pattern pattern=Pattern.compile("(-?\\d*)x ([+-]) (\\d*)y = (-?\\d+)");
        for(int i=0;i<1000;i++){
            Question q=g.next("linearSystem",List.of(),true);String[] lines=q.prompt.split("\n");assertEquals(3,lines.length);int[][] eq=new int[2][3];
            for(int k=0;k<2;k++){
                Matcher m=pattern.matcher(lines[k]);assertTrue(lines[k],m.matches());
                eq[k][0]=m.group(1).equals("")?1:m.group(1).equals("-")?-1:Integer.parseInt(m.group(1));
                eq[k][1]=(m.group(3).isEmpty()?1:Integer.parseInt(m.group(3)))*(m.group(2).equals("-")?-1:1);eq[k][2]=Integer.parseInt(m.group(4));
            }
            assertNotEquals(0,eq[0][0]*eq[1][1]-eq[0][1]*eq[1][0]);
            List<String> solution=List.of();int count=0;
            for(int x=-9;x<=9;x++)for(int y=-9;y<=9;y++)if(eq[0][0]*x+eq[0][1]*y==eq[0][2]&&eq[1][0]*x+eq[1][1]*y==eq[1][2]){count++;solution=List.of(""+x,""+y);}
            assertEquals(1,count);assertEquals(solution,Arrays.asList(q.answers));assertArrayEquals(new String[]{"x","y"},q.labels);assertTrue(q.choices.isEmpty());
            assertTrue(checker.check(q,List.of(),solution).correct());pairs.add(solution.toString());xs.add(solution.get(0));ys.add(solution.get(1));
        }
        assertTrue(pairs.size()>300);assertEquals(19,xs.size());assertEquals(19,ys.size());assertTrue(xs.contains("0")&&ys.contains("0"));
    }
    @Test public void additionSubtractionSubstitutionAndDifferentOrdersAreAccepted(){
        for(List<String> rows:List.of(List.of("3x=9","x=3","y=1"),List.of("y=x-2","2x+(x-2)=7","1=y","3=x"),List.of("-4x-2y=-14","(2x+y)/2=7/2","x+y=4"))){
            assertTrue(rows.toString(),checker.checkSteps(question(),rows,Collections.nCopies(rows.size(),FULL)).correct());
        }
        assertTrue(checker.check(question(),List.of(),List.of("x=3","y=1")).correct());
        Checker.Result swapped=checker.check(question(),List.of(),List.of("1","3"));assertEquals(Checker.Status.WRONG_ANSWER,swapped.status);assertEquals(0,swapped.index);
    }
    @Test public void firstWrongRelationCannotBeHiddenByLaterCorrectAnswers(){
        Checker.Result r=checker.check(question(),List.of("3x=8","x=3","y=1"),List.of("3","1"),List.of(FULL,FULL,FULL));
        assertEquals(Checker.Status.WRONG_STEP,r.status);assertEquals(0,r.index);assertEquals(-1,r.part);assertEquals("이 줄 확인",r.message);
        r=checker.check(question(),List.of("x=3","y=2"),List.of("3","1"),List.of(FULL,FULL));assertEquals(1,r.index);
        assertEquals(Checker.Status.INPUT_NEEDED,checker.check(question(),List.of("x=3"),List.of("3",""),List.of(FULL)).status);
        assertTrue(checker.checkSteps(question(),List.of("0=0"),List.of(FULL)).correct());assertTrue(WorkAnswer.writtenAnswers(question(),List.of("0=0"),List.of(FULL)).isEmpty());
    }
    @Test public void unsupportedOrIncompleteInputIsNotCountedAsMathError(){
        for(String row:List.of("xy=3","x^2=9","x/y=3","x/x=1","x/0=1","x=","=1","x+y","x=z","x=3=3","(".repeat(33)+"x"+")".repeat(33)+"=3")){
            Checker.Result r=checker.checkSteps(question(),List.of(row),List.of(FULL));assertEquals(row,Checker.Status.INPUT_NEEDED,r.status);assertFalse(r.mathematicalError());
        }
        assertEquals(Checker.Status.INPUT_NEEDED,checker.checkSteps(question(),List.of(),List.of()).status);
        assertTrue(checker.checkSteps(question(),List.of("2+3=5","2(x+y)=2x+2y"),List.of(PARTIAL,PARTIAL)).correct());
        assertEquals(Checker.Status.WRONG_STEP,checker.checkSteps(question(),List.of("2+3=6"),List.of(PARTIAL)).status);
        assertEquals(Checker.Status.INPUT_NEEDED,checker.checkSteps(question(),List.of("x=3"),List.of(PARTIAL)).status);
    }
    @Test public void onlyBothExplicitStudentValuesAreCopiedVerbatim(){
        List<String> rows=new ArrayList<>(List.of("6/2 = x","y = +1"));List<String> copy=new ArrayList<>(rows);
        assertEquals(List.of("6/2","+1"),WorkAnswer.writtenAnswers(question(),rows,List.of(FULL,FULL)));assertEquals(copy,rows);
        assertTrue(WorkAnswer.writtenAnswers(question(),List.of("x=3"),List.of(FULL)).isEmpty());
        assertTrue(WorkAnswer.writtenAnswers(question(),List.of("x+y=4","x-y=2"),List.of(FULL,FULL)).isEmpty());
        assertTrue(WorkAnswer.writtenAnswers(question(),List.of("x=3","y=2"),List.of(FULL,FULL)).isEmpty());
        assertEquals("",WorkAnswer.writtenAnswer(question(),rows,List.of(FULL,FULL)));
        Checker.Result r=checker.check(question(),List.of(),List.of("3","2"));assertEquals(Checker.Status.WRONG_ANSWER,r.status);assertEquals(1,r.index);
    }
    @Test public void workUsesOnlyTheGivenRelationsAndKeepsBothConstraints(){
        Question q=question();q.answers=new String[]{"999","999"};
        assertTrue(checker.checkSteps(q,List.of("x=3","y=1"),List.of(FULL,FULL)).correct());
        assertEquals(Checker.Status.WRONG_STEP,checker.checkSteps(q,List.of("x=3","y=99"),List.of(FULL,FULL)).status);
        Random r=new Random(77);
        for(int i=0;i<500;i++){
            int a=r.nextInt(11)-5,b=r.nextInt(11)-5,x=2*a+b,y=a-b,c=7*a+2*b;
            String left=x+"*x+("+y+")*y=";
            assertTrue(checker.checkSteps(q,List.of(left+c),List.of(FULL)).correct());
            assertEquals(Checker.Status.WRONG_STEP,checker.checkSteps(q,List.of(left+(c+1)),List.of(FULL)).status);
        }
    }
    @Test public void scopeAndSavedAnswersWorkAndFractionPresentationStaySeparate()throws Exception{
        Learning.State s=new Learning.State();s.profile.grade=8;s.profile.term=1;s.profile.currentSkill="linearSystem";
        assertFalse(Learning.diagnosticScope(s.profile).stream().anyMatch(k->k.id.equals("linearSystem")));
        assertTrue(Learning.learningScope(s.profile).stream().anyMatch(k->k.id.equals("linearSystem")));
        s.profile.currentSkill="";s.profile.term=2;assertTrue(Learning.diagnosticScope(s.profile).stream().anyMatch(k->k.id.equals("linearSystem")));
        s.profile.excluded.add("linearSystem");assertFalse(Learning.diagnosticScope(s.profile).stream().anyMatch(k->k.id.equals("linearSystem")));
        assertTrue(Catalog.foundationOrder("linearSystem").containsAll(List.of("linear","likeTerms","signedMul")));
        Random r=new Random(1);Learning.beginPractice(s,"homework",List.of("linearSystem"),10,false,r);Learning.ensureQuestion(s,new Generator(r));String id=s.session.question.id;
        s.session.answers.set(0,"0");s.session.answers.set(1,"-3/1");s.session.steps.add("x+y=2");s.session.stepKinds.add(FULL);s.session.workOpen=true;FractionInput.form(s.session,1).fraction=true;s.session.answerFocus=1;
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();try(ObjectOutputStream out=new ObjectOutputStream(bytes)){out.writeObject(s);}
        try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))){s=(Learning.State)in.readObject();}
        assertEquals(id,s.session.question.id);assertEquals(List.of("0","-3/1"),s.session.answers);assertEquals(List.of("x+y=2"),s.session.steps);assertEquals(List.of(FULL),s.session.stepKinds);assertTrue(s.session.workOpen);assertTrue(FractionInput.form(s.session,1).fraction);assertFalse(FractionInput.form(s.session,0).fraction);assertEquals(1,s.session.answerFocus);assertEquals(8,s.profile.grade);
    }
}
