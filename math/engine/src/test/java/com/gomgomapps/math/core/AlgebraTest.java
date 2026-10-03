package com.gomgomapps.math.core;

import org.junit.Test;
import static org.junit.Assert.*;
import static com.gomgomapps.math.core.Checker.StepKind.*;
import java.util.*;
import java.io.*;

public class AlgebraTest {
    private final Checker checker=new Checker();
    private Question q(String expression,String... nonzero){Question q=new Question("polynomialQuotient",expression,expression,"unused");q.kind="algebra";q.nonzeroVariables=new LinkedHashSet<>(List.of(nonzero));return q;}
    private Checker.Result answer(Question q,String value){return checker.check(q,List.of(),List.of(value));}
    @Test public void exactProductsQuotientsAndPowersRetainBothVariables(){
        for(String[] row:new String[][]{
                {"(-3xy^2)*(2x^2y)","-6x^3y^3"},{"(-2xy)^3","-8x^3y^3"},
                {"(-2xy)^4","16y⁴x⁴"},{"(6x^3y-9xy^2)/(-3xy)","3y-2x^2"},
                {"(1/2)x*(3x-4y)","(3/2)x^2-2xy"},{"(6xy-6xy)/(2xy)","0"}}){
            assertTrue(Arrays.toString(row),answer(q(row[0],"x","y"),row[1]).correct());
        }
    }
    @Test public void variableDivisionKeepsGivenDomainEvenAfterCancellation(){
        assertTrue(answer(q("6x/(2x)","x"),"3").correct());
        for(String expression:List.of("6x/(2x)","0/x","x^0"))assertEquals(Checker.Status.INPUT_NEEDED,answer(q(expression),"3").status);
        Question question=q("6xy/(2x)","x");assertTrue(answer(question,"3y").correct());
        for(String work:List.of("3y*(y/y)","3y+0/y","3y+(x-y)/(x-y)","3y/(x-x)","3y+x^(-1)")){
            Checker.Result r=checker.checkSteps(question,List.of(work),List.of(FULL));assertEquals(work,Checker.Status.INPUT_NEEDED,r.status);assertFalse(r.mathematicalError());
        }
        assertTrue(checker.checkSteps(question,List.of("3y*x/x"),List.of(FULL)).correct());
        assertTrue(answer(q("x^0","x"),"1").correct());assertTrue(answer(q("2^0"),"1").correct());
        assertThrows(IllegalArgumentException.class,()->Expression.parse("x/x")); // Existing equation protection unchanged.
    }
    @Test public void unfinishedCorrectExpressionsDoNotBecomeAnswers(){
        Question question=q("2x*(x+y)");
        for(String value:List.of("2x*(x+y)","2x*x+2xy","x^2+x^2+2xy","(4/2)x^2+2xy","2x^2+xy+yx","2x^2+2xy+0")){
            assertEquals(value,Checker.Status.INPUT_NEEDED,answer(question,value).status);assertEquals("",WorkAnswer.writtenAnswer(question,List.of(question.expression+"="+value),List.of(FULL)));
        }
        for(String value:List.of("2x²+2yx","2*y*x+2*x^2","2xy+2x^2"))assertTrue(value,answer(question,value).correct());
        assertTrue(answer(q("xy/2"),"(1/2)yx").correct());assertTrue(answer(q("xy/2"),"yx/2").correct());
        assertEquals(Checker.Status.WRONG_ANSWER,answer(question,"2x^2+y").status);
    }
    @Test public void firstWrongPartIsCheckedBeforeAnyCorrectFinalAnswer(){
        Question question=q("(6x^2y-9xy^2)/(3xy)","x","y");
        List<String> steps=List.of(question.expression+"=2x-3y","2x-3y=2x+3y","2x-3y");
        Checker.Result r=checker.check(question,steps,List.of("2x-3y"),List.of(FULL,FULL,FULL));
        assertEquals(Checker.Status.WRONG_STEP,r.status);assertEquals(1,r.index);assertEquals(1,r.part);
        r=checker.checkSteps(question,List.of("2x+3y=2x-3y"),List.of(FULL));assertEquals(0,r.part);
        assertEquals("",WorkAnswer.writtenAnswer(question,steps,List.of(FULL,FULL,FULL)));
    }
    @Test public void partialWorkNeverReplacesOriginalAndOnlyWrittenFinalIsCopied(){
        Question question=q("(6x^2y-9xy^2)/(3xy)","x","y");
        assertTrue(checker.checkSteps(question,List.of("6x^2y/(3xy)=2x"),List.of(PARTIAL)).correct());
        assertEquals(Checker.Status.WRONG_STEP,checker.checkSteps(question,List.of("6x^2y/(3xy)=2y"),List.of(PARTIAL)).status);
        assertEquals("",WorkAnswer.writtenAnswer(question,List.of("6x^2y/(3xy)=2x"),List.of(PARTIAL)));
        assertEquals(Checker.Status.WRONG_STEP,checker.checkSteps(question,List.of("6x^2y/(3xy)=2x"),List.of(FULL)).status);
        question.answers[0]="999"; // Original public expression, not this stored answer, decides correctness.
        assertEquals("-3y+2x",WorkAnswer.writtenAnswer(question,List.of(question.expression+"=-3y+2x"),List.of(FULL)));
        assertEquals("",WorkAnswer.writtenAnswer(question,List.of("2x-3y=2x-3y"),List.of(UNSPECIFIED)));
    }
    @Test public void incompleteUnsupportedAndBoundedInputIsNotAMathematicalError(){
        Question question=q("2x*(x+y)");
        for(String value:List.of("", "2x+", "x=2", "x/z", "x/0", "1/x", "x^9", "(x+y)^8*x", "(".repeat(34)+"x"+")".repeat(34), "x".repeat(241))){
            Checker.Result r=answer(question,value);assertEquals(value,Checker.Status.INPUT_NEEDED,r.status);assertFalse(r.mathematicalError());
        }
        assertEquals(Checker.Status.INPUT_NEEDED,checker.checkSteps(question,List.of(""),List.of(FULL)).status);
        Checker.Result blank=checker.checkSteps(question,List.of(question.expression+"="),List.of(FULL));assertEquals(Checker.Status.INPUT_NEEDED,blank.status);assertEquals(1,blank.part);
    }
    @Test public void generatedPublicExpressionsHaveVariedAnswersAndExplicitDenominators(){
        Generator g=new Generator(new Random(6090923));
        for(String id:List.of("monomialProduct","monomialQuotient","monomialPower","polynomialProduct","polynomialQuotient")){
            Set<String> expressions=new HashSet<>(),answers=new HashSet<>(),domains=new HashSet<>(),outerPowers=new HashSet<>();List<String> recent=new ArrayList<>();int fractions=0,negatives=0,bothVariables=0;
            for(int i=0;i<240;i++){
                Question question=g.next(id,recent,true);assertFalse(recent.contains(question.signature()));recent.add(question.signature());if(recent.size()>40)recent.remove(0);String publicExpression=question.prompt.split("\n")[0];
                assertEquals(publicExpression,question.expression);assertTrue(checker.check(question,List.of(),List.of(question.answers)).correct());assertTrue(question.choices.isEmpty());
                // Independent existing scalar evaluator after explicit variable substitution. Nine distinct
                // nonzero values in each variable prove equality for polynomials of degree <=8 in each.
                for(int x=1;x<=9;x++)for(int y=1;y<=9;y++)assertEquals(publicExpression,
                        evaluate(publicExpression,x,y),evaluate(question.answers[0],x,y));
                String denominator=publicExpression.contains("÷")?publicExpression.substring(publicExpression.indexOf('÷')):"";
                for(String variable:List.of("x","y"))assertEquals(publicExpression,denominator.contains(variable),Algebra.domain(question).contains(variable));
                if(!denominator.isEmpty())assertTrue(question.prompt.contains(Algebra.condition(question)));
                domains.add(Algebra.condition(question));expressions.add(publicExpression);answers.add(question.answers[0]);
                if(id.equals("monomialPower"))outerPowers.add(publicExpression.substring(publicExpression.lastIndexOf('^')+1));
                if(question.answers[0].contains("/"))fractions++;if(question.answers[0].contains("-"))negatives++;if(question.answers[0].contains("x")&&question.answers[0].contains("y"))bothVariables++;
            }
            System.out.println(id+": expressions="+expressions.size()+", answers="+answers.size()+", fractions="+fractions+", negative="+negatives+", xy="+bothVariables);
            // A smaller power pool naturally revisits a skill after the recent window. Require
            // varied expressions/answers, not the same sampling collision rate as two-operand drills.
            assertTrue(id,expressions.size()>150);assertTrue(id,answers.size()>90);assertTrue(id,fractions>15);assertTrue(id,negatives>0&&negatives<240);assertTrue(id,bothVariables>25);
            if(id.equals("monomialPower"))assertEquals(Set.of("2","3","4"),outerPowers);
            if(id.endsWith("Quotient"))assertEquals(3,domains.size());
        }
    }
    private Rational evaluate(String text,int x,int y){
        String explicit=text.replaceAll("(?<=[0-9xy)])(?=[xy(])","*");
        return Expression.number(explicit.replace("x","("+x+")").replace("y","("+y+")"));
    }
    @Test public void scopeDraftAndDomainSurviveSaveAndNullMeansNoExtraAssumptions()throws Exception{
        Learning.State s=new Learning.State();s.profile.grade=8;s.profile.term=1;s.profile.currentSkill="polynomialQuotient";
        assertFalse(Learning.diagnosticScope(s.profile).stream().anyMatch(k->k.id.equals("polynomialQuotient")));assertTrue(Learning.learningScope(s.profile).stream().anyMatch(k->k.id.equals("polynomialQuotient")));
        s.profile.term=2;s.profile.currentSkill="";assertTrue(Learning.diagnosticScope(s.profile).stream().anyMatch(k->k.id.equals("polynomialQuotient")));s.profile.excluded.add("polynomialQuotient");assertFalse(Learning.diagnosticScope(s.profile).stream().anyMatch(k->k.id.equals("polynomialQuotient")));
        Random r=new Random(7);Learning.beginPractice(s,"homework",List.of("polynomialQuotient"),20,false,r);Learning.ensureQuestion(s,new Generator(r));s.session.steps.add("x =");s.session.stepKinds.add(FULL);s.session.answers.set(0,"x+");s.session.workOpen=true;
        Set<String> domain=Algebra.domain(s.session.question);String id=s.session.question.id;ByteArrayOutputStream bytes=new ByteArrayOutputStream();try(ObjectOutputStream out=new ObjectOutputStream(bytes)){out.writeObject(s);}try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))){s=(Learning.State)in.readObject();}
        assertEquals(id,s.session.question.id);assertEquals(domain,Algebra.domain(s.session.question));assertEquals(List.of("x ="),s.session.steps);assertEquals(List.of("x+"),s.session.answers);assertTrue(s.session.workOpen);
        s.session.question.nonzeroVariables=null;assertTrue(Algebra.domain(s.session.question).isEmpty());assertEquals(Checker.Status.INPUT_NEEDED,answer(s.session.question,"0").status);
        Question old=q("2x*(x+y)");old.nonzeroVariables=null;assertTrue(answer(old,"2x^2+2xy").correct());
    }
}
