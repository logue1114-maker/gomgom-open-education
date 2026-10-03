package com.gomgomapps.math.core;

import org.junit.Test;
import static org.junit.Assert.*;
import java.io.*;
import java.time.LocalDate;
import java.util.*;

public class FractionWorkTest {
    private Question q(String prompt){return new Question("fracAdd",prompt,"999","999");}
    private FractionWork.Row row(boolean two,String op,String... parts){FractionWork.Row r=new FractionWork.Row(two,op);for(int i=0;i<parts.length;i++)r.set(List.of("ln","ld","rn","rd").get(i),parts[i]);return r;}
    private FractionWork.Draft draft(FractionWork.Row...rows){FractionWork.Draft d=new FractionWork.Draft();d.rows.addAll(List.of(rows));return d;}
    @Test public void supportsVisibleFractionsWithoutReadingStoredAnswers(){
        for(String expression:List.of("(2/3) + (11/12)","(-2/3) / (-3/4)","(2/3) / 4","3 * (1/2)")){Question q=q(expression);assertTrue(expression,FractionWork.supports(q));assertTrue(VerticalWork.supported(q));assertNotEquals("999",FractionWork.original(q).value.toString());}
        Question reduced=q("12/24을 기약분수로 나타내세요.");reduced.kind="reduced";assertEquals(Rational.of(1,2),FractionWork.original(reduced).value);
        for(String expression:List.of("2 + 3","1.25 / 0.5","3 / 4","분수를 비교하세요.","(1/0) + (2/3)","(2/3) / 0"))assertFalse(expression,FractionWork.supports(q(expression)));
    }
    @Test public void commonDenominatorsNeedNotBeLeastAndOnlyBadLineIsMarked(){
        Question q=q("(2/3) + (11/12)");FractionWork.Draft d=draft(row(true,"+","16","24","22","24"),row(false,"+","38","24"));
        assertFalse(FractionWork.check(q,d).error());d.rows.get(1).leftNumerator="39";assertEquals(Set.of("fraction:1"),FractionWork.check(q,d).wrong);
        d.rows.add(row(false,"+","19","12"));d.selected=2;VerticalWork.Draft all=new VerticalWork.Draft();all.fractions=d;assertEquals(List.of("19/12"),VerticalWork.answers(q,all));assertEquals("19/12",VerticalWork.answer(q,all));assertEquals(Set.of("fraction:1"),VerticalWork.check(q,all).wrong);
        d.rows.get(1).leftNumerator="38";assertFalse(VerticalWork.check(q,all).error());
    }
    @Test public void reciprocalAndCrossCancellationAreValidEquivalentExpressions(){
        Question divide=q("(2/3) / (4/5)");FractionWork.Draft d=draft(row(true,"*","2","3","5","4"),row(true,"*","1","3","5","2"),row(false,"+","5","6"));assertFalse(FractionWork.check(divide,d).error());
        d.rows.get(0).rightNumerator="4";d.rows.get(0).rightDenominator="5";assertEquals(Set.of("fraction:0"),FractionWork.check(divide,d).wrong);
        Question multiply=q("(2/3) * (3/4)");assertFalse(FractionWork.check(multiply,draft(row(true,"*","1","1","1","2"))).error());
    }
    @Test public void blankZeroAndUnreadableInputsAreNotMathErrorsAndNeverAutofill(){
        Question q=q("(2/3) + (1/3)");FractionWork.Draft d=draft(row(true,"+"));assertFalse(FractionWork.hasInput(d));assertTrue(FractionWork.check(q,d).inputNeeded);assertNull(FractionWork.answers(q,d));
        for(String[] values:List.of(new String[]{"1",""},new String[]{"1","0"},new String[]{"1.2","3"},new String[]{"1234567890123","3"})){d.rows.set(0,row(false,"+",values));VerticalWork.Result r=FractionWork.check(q,d);assertTrue(r.inputNeeded);assertFalse(r.error());assertNull(FractionWork.answers(q,d));assertEquals(values[1],d.rows.get(0).leftDenominator);}
        d.rows.set(0,row(true,"/","1","1","0","1"));assertFalse(FractionWork.check(q,d).error());assertTrue(FractionWork.check(q,d).inputNeeded);
    }
    @Test public void reducedFinalAnswerUsesNormalCheckerAndCopiesOnlyStudentFraction(){
        Question q=new Question("reduce","4/8을 기약분수로 나타내세요.","4/8","1/2");q.kind="reduced";FractionWork.Draft d=draft(row(false,"+","2","4"));
        assertFalse(FractionWork.check(q,d).error());assertEquals(List.of("2/4"),FractionWork.answers(q,d));assertFalse(new Checker().check(q,List.of(),FractionWork.answers(q,d)).correct());
        FractionWork.add(d);assertEquals("",d.rows.get(1).leftNumerator);assertEquals("",d.rows.get(1).leftDenominator);d.rows.set(1,row(false,"+","1","2"));assertTrue(new Checker().check(q,List.of(),FractionWork.answers(q,d)).correct());
        d.rows.get(1).two=true;assertNull(FractionWork.answers(q,d));d.rows.get(1).two=false;assertEquals(List.of("1/2"),FractionWork.answers(q,d));FractionWork.remove(q,d);assertEquals(0,d.selected);FractionWork.remove(q,d);assertFalse(FractionWork.hasInput(d));
    }
    @Test public void worksheetInsertionPreservesLaterWorkAndClearingRestoresOriginalShape(){
        Question q=q("(2/3)+(11/12)");FractionWork.Draft d=draft(row(true,"+","8","12","11","12"),row(false,"+","19","12"));
        FractionWork.add(d);assertEquals(1,d.selected);assertFalse(d.rows.get(1).two);assertFalse(FractionWork.hasInput(d.rows.get(1)));assertEquals("19",d.rows.get(2).leftNumerator);
        d.selected=0;FractionWork.remove(q,d);FractionWork.remove(q,d);FractionWork.remove(q,d);assertEquals(1,d.rows.size());assertTrue(d.rows.get(0).two);assertEquals("+",d.rows.get(0).operator);assertFalse(FractionWork.hasInput(d));
    }
    @Test public void serializationAndNormalDeferredCopiesPreserveRowsAndFocus()throws Exception{
        Learning.State state=new Learning.State();Learning.beginPractice(state,"practice",List.of("fracAdd"),3,false,new Random(2));Learning.ensureQuestion(state,new Generator(new Random(3)));Learning.Session session=state.session;String original=session.question.id;
        FractionWork.Draft d=FractionWork.draft(session);d.rows.get(0).leftNumerator="7";FractionWork.add(d);d.rows.get(1).rightDenominator="24";d.focus="rd";session.workOpen=true;session.workTab=2;
        Learning.finishQuestion(state,true,LocalDate.of(2026,9,8),new Random(4));Learning.ensureQuestion(state,new Generator(new Random(5)));assertNull(session.verticalWork);Deferred.open(session,original);
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();new ObjectOutputStream(bytes).writeObject(state);state=(Learning.State)new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray())).readObject();session=state.session;d=session.verticalWork.fractions;
        assertEquals(2,d.rows.size());assertEquals(1,d.selected);assertEquals("rd",d.focus);assertEquals("7",d.rows.get(0).leftNumerator);assertEquals("24",d.rows.get(1).rightDenominator);
        VerticalWork.Draft copy=VerticalWork.copy(session.verticalWork);copy.fractions.rows.get(0).leftNumerator="9";assertEquals("7",d.rows.get(0).leftNumerator);Deferred.leave(session);assertNull(session.verticalWork);
    }
    @Test public void sixHundredIntegerOraclesAgreeWithAllFourFractionOperations(){
        Random random=new Random(9308);String[] operators={"+","-","*","/"};
        for(int i=0;i<600;i++){
            long a=random.nextInt(25)+1,b=random.nextInt(24)+2,c=random.nextInt(25)+1,d=random.nextInt(24)+2;if(i%3==0)a=-a;if(i%7==0)c=-c;String op=operators[i%4];
            long numerator=switch(op){case "+"->a*d+c*b;case "-"->a*d-c*b;case "*"->a*c;default->a*d;};long denominator=op.equals("/")?b*c:b*d;
            Question q=q("("+a+"/"+b+")"+op+"("+c+"/"+d+")");int k=random.nextInt(8)+2,j=random.nextInt(8)+2;
            FractionWork.Draft work=draft(row(true,op,Long.toString(a*k),Long.toString(b*k),Long.toString(c*j),Long.toString(d*j)),row(false,"+",Long.toString(numerator),Long.toString(denominator)));
            assertFalse(q.prompt,FractionWork.check(q,work).error());work.selected=1;assertEquals(List.of(numerator+"/"+denominator),FractionWork.answers(q,work));work.rows.get(1).leftNumerator=Long.toString(numerator+1);assertEquals(q.prompt,Set.of("fraction:1"),FractionWork.check(q,work).wrong);
        }
    }
}
