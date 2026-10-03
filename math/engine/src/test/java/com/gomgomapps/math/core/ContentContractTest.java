package com.gomgomapps.math.core;

import org.junit.Test;
import static org.junit.Assert.*;
import java.io.*;
import java.util.*;
import java.util.regex.*;

public class ContentContractTest {
    private final Generator generator=new Generator(new Random(6090813));
    private Question next(String id){return generator.next(id,List.of(),true);}
    @Test public void everyBorrowingDrillRequiresExchangingATen(){
        Set<String> seen=new HashSet<>();
        for(int i=0;i<1200;i++){
            Question q=next("sub20");String[] parts=q.prompt.split(" - ");int a=Integer.parseInt(parts[0]),b=Integer.parseInt(parts[1]);
            assertTrue(a>=10&&a<=18);assertTrue(b>a%10&&b<=9);assertEquals(Integer.toString(a-b),q.answers[0]);seen.add(q.prompt);
        }
        assertEquals(45,seen.size());
    }
    @Test public void digitLabelsMatchBothOperandsAndProductsIncludeOne(){
        for(String id:List.of("add100","sub100","add1000","sub1000","mul2","mul3","mul22")){
            int min=id.endsWith("1000")||id.equals("mul3")?100:10,max=min*10-1;boolean multipliedByOne=false;
            for(int i=0;i<400;i++){
                Question q=next(id);Matcher numbers=Pattern.compile("\\d+").matcher(q.prompt);assertTrue(numbers.find());int a=Integer.parseInt(numbers.group());assertTrue(numbers.find());int b=Integer.parseInt(numbers.group());assertFalse(numbers.find());
                assertTrue(q.prompt,a>=min&&a<=max);
                boolean oneDigit=id.equals("mul2")||id.equals("mul3");assertTrue(q.prompt,b>=(oneDigit?1:10)&&b<=(oneDigit?9:max));
                int expected=id.startsWith("add")?a+b:id.startsWith("sub")?a-b:a*b;assertEquals(q.prompt,Integer.toString(expected),q.answers[0]);
                if(id.startsWith("sub"))assertTrue(a>=b);if(oneDigit&&b==1)multipliedByOne=true;
            }
            if(id.equals("mul2")||id.equals("mul3"))assertTrue(multipliedByOne);
        }
    }
    @Test public void signedIntegerDrillsIncludeMultiplicationAndExactDivision(){
        Set<String> operations=new HashSet<>();Set<Integer> signs=new HashSet<>();
        Pattern p=Pattern.compile("\\(?(-?\\d+)\\)? ([×÷]) \\(?(-?\\d+)\\)?");
        for(int i=0;i<800;i++){
            Question q=next("signedMul");Matcher m=p.matcher(q.prompt);assertTrue(q.prompt,m.matches());int a=Integer.parseInt(m.group(1)),b=Integer.parseInt(m.group(3));String op=m.group(2);operations.add(op);signs.add(Integer.signum(a)*2+Integer.signum(b));
            assertNotEquals(0,b);int expected=op.equals("×")?a*b:a/b;if(op.equals("÷")){assertEquals(0,a%b);assertTrue(Math.abs(expected)<=12);}
            assertEquals(Integer.toString(expected),q.answers[0]);
        }
        assertEquals(Set.of("×","÷"),operations);assertEquals(4,signs.size());
    }
    @Test public void rationalDrillsExerciseAllFourOperationsAndBothSigns(){
        Set<String> operations=new HashSet<>();boolean negativeAnswer=false;
        for(int i=0;i<1000;i++){
            Question q=next("rational");Matcher m=Pattern.compile(" ([-+*/]) ").matcher(q.prompt);assertTrue(m.find());operations.add(m.group(1));assertFalse(m.find());
            Rational answer=Expression.number(q.answers[0]);negativeAnswer|=answer.compareTo(Rational.ZERO)<0;assertEquals(answer,Expression.number(q.expression));
            if(!q.choices.isEmpty()){assertEquals(4,q.choices.size());assertEquals(4,q.choices.stream().map(Expression::number).distinct().count());assertEquals(answer,Expression.number(q.choices.get(q.correctChoice)));}
        }
        assertEquals(Set.of("+","-","*","/"),operations);assertTrue(negativeAnswer);
    }
    @Test public void firstYearSubstitutionUsesAnExpressionInsteadOfFunctionNotation(){
        Pattern p=Pattern.compile("x = (-?\\d+)일 때\\n(-?\\d+)x \\+ \\((-?\\d+)\\)의 값은\\?");
        for(int i=0;i<160;i++){
            Question q=next("substitute");Matcher m=p.matcher(q.prompt);assertTrue(q.prompt,m.matches());int x=Integer.parseInt(m.group(1)),a=Integer.parseInt(m.group(2)),b=Integer.parseInt(m.group(3));assertEquals(Integer.toString(a*x+b),q.answers[0]);assertFalse(q.prompt.contains("f("));
        }
        assertTrue(next("function").prompt.startsWith("f(x)"));
    }
    @Test public void fractionCoefficientsStayInTheFirstYearEquationUnitAndPreviousUnitLimit(){
        Catalog.Skill fraction=Catalog.get("linearFraction"),linear=Catalog.get("linear");assertEquals(7,fraction.grade);assertEquals(linear.order(),fraction.order());
        Learning.Profile profile=new Learning.Profile();profile.grade=7;profile.schoolYear=2026;profile.term=1;profile.currentSkill="linear";
        assertFalse(Learning.diagnosticScope(profile).stream().anyMatch(s->s.id.equals("linearFraction")));profile.currentSkill="";profile.term=2;
        assertTrue(Learning.diagnosticScope(profile).stream().anyMatch(s->s.id.equals("linearFraction")));
    }
    @Test public void displayPreservesSavedQuestionIdentityAndFractionMeaning()throws Exception{
        Question old=new Question("decimalMul","7.68 * 6.2","7.68 * 6.2","47.616");old.choices.addAll(List.of("4.7616","47.616","476.16","13.88"));old.correctChoice=1;String signature=old.signature();
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();new ObjectOutputStream(bytes).writeObject(old);Question restored=(Question)new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray())).readObject();
        assertEquals("7.68 × 6.2",MathText.display(restored.prompt));assertEquals(signature,restored.signature());assertEquals(old.id,restored.id);assertEquals(old.expression,restored.expression);assertEquals(old.choices,restored.choices);assertEquals(1,restored.correctChoice);
        assertEquals("(1/2) ÷ (3/4)",MathText.display("(1/2) / (3/4)"));assertEquals("2⁻³",MathText.display("2^(-3)"));assertEquals("x² − 3x + 1",MathText.display("x^2 - 3x + 1"));assertEquals("∫₀⁴ (3x²) dx의 값은?",MathText.display("∫₀^4 (3x^2) dx의 값은?"));
    }
}
