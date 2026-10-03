package com.gomgomapps.math.core;

import java.io.*;
import java.math.*;
import java.time.LocalDate;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class DecimalDivisionTest {
    private Question q(String prompt){return new Question("decimalDiv",prompt,prompt,"unused");}
    private DecimalDivision.Draft draft(){return new DecimalDivision.Draft();}
    private DecimalDivision.Draft applied(Question q,String top,String bottom){DecimalDivision.Draft d=draft();d.top=top;d.bottom=bottom;assertTrue(DecimalDivision.apply(q,d));return d;}
    private void fill(DecimalDivision.Board b,String name,int place,String value){DecimalDivision.values(b,name).put(place,value);}
    @Test public void supportsVisibleDecimalAndNonIntegralDivisionWithoutHiddenAnswers(){
        for(String prompt:List.of("1.2/3","3.6/1.2","1/8","8/3")){Question q=q(prompt);q.answers=new String[]{"999999"};assertTrue(VerticalWork.supported(q));assertNotNull(DecimalDivision.original(q));}
        assertFalse(DecimalDivision.supports(q("84/7")));assertFalse(DecimalDivision.supports(q("1/0")));assertFalse(DecimalDivision.supports(q("3/4+1/2")));Question pair=q("8/3");pair.kind="pair";pair.answers=new String[]{"2","2"};assertFalse(DecimalDivision.supports(pair));assertTrue(VerticalWork.supported(pair));
    }
    @Test public void transformationAcceptsEquivalentWorkWithoutGivingTheNewOperands(){
        Question q=q("3.6/1.2");DecimalDivision.Draft d=draft();assertTrue(DecimalDivision.transforming(q,d));assertNull(DecimalDivision.working(q,d));assertEquals("",d.top);assertEquals("",d.bottom);
        d.top="36";d.bottom="1.2";assertEquals(Set.of("transform:top","transform:bottom"),DecimalDivision.checkTransform(q,d).wrong);assertFalse(DecimalDivision.apply(q,d));assertEquals("36",d.top);
        d.bottom="0";assertTrue(DecimalDivision.checkTransform(q,d).inputNeeded);assertFalse(DecimalDivision.checkTransform(q,d).error());d.top="3.6";d.bottom="1.2";assertTrue(DecimalDivision.checkTransform(q,d).inputNeeded);assertFalse(DecimalDivision.checkTransform(q,d).error());
        d.top="36";d.bottom="12";assertTrue(DecimalDivision.apply(q,d));assertEquals("36",DecimalDivision.working(q,d).topText);assertFalse(DecimalDivision.transforming(q,d));d.top="3";d.bottom="1";assertTrue(DecimalDivision.apply(q,d));assertEquals("3",DecimalDivision.working(q,d).topText);
    }
    @Test public void decimalPlacesCheckBroughtProductAndRemainderAtTheirOwnPlace(){
        Question q=q("1.25/0.5");DecimalDivision.Draft d=applied(q,"12.5","5");DecimalDivision.Spec s=DecimalDivision.working(q,d);DecimalDivision.Board b=DecimalDivision.board(d,s);
        b.point=0;b.quotient.put(0,"2");b.quotient.put(-1,"5");b.brought.put(0,"12");b.product.put(0,"10");b.rest.put(0,"2");b.brought.put(-1,"25");b.product.put(-1,"25");b.rest.put(-1,"0");
        assertFalse(DecimalDivision.check(q,d).error());assertEquals(List.of("2.5"),DecimalDivision.answers(q,d));b.product.put(-1,"20");assertEquals(Set.of("product:-1"),DecimalDivision.check(q,d).wrong);assertEquals("20",b.product.get(-1));assertEquals("0",b.rest.get(-1));
    }
    @Test public void continuingBelowTheGivenDigitsLeavesNewColumnsEmpty(){
        Question q=q("1/8");DecimalDivision.Draft d=draft();DecimalDivision.Spec s=DecimalDivision.working(q,d);DecimalDivision.Board b=DecimalDivision.board(d,s);assertEquals(0,s.minPlace(b));
        for(int count=1;count<=3;count++){assertTrue(DecimalDivision.addPlace(s,b));assertEquals(-count,s.minPlace(b));assertTrue(b.quotient.isEmpty());assertEquals("",s.digit(-count));}
        b.point=0;b.quotient.put(0,"0");b.quotient.put(-1,"1");b.quotient.put(-2,"2");b.quotient.put(-3,"5");b.brought.put(-1,"10");b.product.put(-1,"8");b.rest.put(-1,"2");b.brought.put(-2,"20");b.product.put(-2,"16");b.rest.put(-2,"4");b.brought.put(-3,"40");b.product.put(-3,"40");b.rest.put(-3,"0");
        assertFalse(DecimalDivision.check(q,d).error());assertEquals(List.of("0.125"),DecimalDivision.answers(q,d));b.quotient.remove(-2);assertNull(DecimalDivision.answers(q,d));assertFalse(b.quotient.containsKey(-2));
        b.quotient.clear();b.quotient.put(0,"1");b.point=-1;assertNull(DecimalDivision.answers(q,d));assertFalse(b.quotient.containsKey(-1));
    }
    @Test public void pointAndIncorrectPartialWorkReachTheNormalVerticalCheck(){
        Question q=q("1.2/3");DecimalDivision.Draft d=draft();DecimalDivision.Spec s=DecimalDivision.working(q,d);DecimalDivision.Board b=DecimalDivision.board(d,s);b.quotient.put(0,"0");b.quotient.put(-1,"4");
        VerticalWork.Draft all=new VerticalWork.Draft();all.decimalDivision=d;assertTrue(VerticalWork.hasInput(all));assertTrue(VerticalWork.check(q,all).inputNeeded);assertNull(VerticalWork.answers(q,all));b.point=-1;assertEquals(Set.of("point"),VerticalWork.check(q,all).wrong);b.point=0;assertEquals(List.of("0.4"),VerticalWork.answers(q,all));
        assertEquals("0.4",VerticalWork.answer(q,all));b.rest.put(0,"2");assertEquals(Set.of("rest:0"),VerticalWork.check(q,all).wrong);b.rest.put(0,"1");assertFalse(VerticalWork.check(q,all).error());d.top="12";d.bottom="30";assertTrue(DecimalDivision.apply(q,d));assertTrue(DecimalDivision.board(d,DecimalDivision.working(q,d)).quotient.isEmpty());d.top="1.2";d.bottom="3";assertTrue(DecimalDivision.apply(q,d));assertEquals("4",DecimalDivision.board(d,DecimalDivision.working(q,d)).quotient.get(-1));
    }
    @Test public void serializationAndDeferredCopiesKeepAllBoardsAndSelection()throws Exception{
        Learning.State state=new Learning.State();Learning.beginPractice(state,"practice",List.of("decimalDiv"),3,false,new Random(2));Learning.ensureQuestion(state,new Generator(new Random(3)));Learning.Session session=state.session;String first=session.question.id;
        DecimalDivision.Draft d=DecimalDivision.draft(session);DecimalDivision.Spec original=DecimalDivision.original(session.question);d.top=original.top.movePointRight(1).toPlainString();d.bottom=original.bottom.movePointRight(1).toPlainString();assertTrue(DecimalDivision.apply(session.question,d));DecimalDivision.Board b=DecimalDivision.board(d,DecimalDivision.working(session.question,d));b.quotient.put(-1,"4");b.extraPlaces=3;b.place=-1;b.point=0;b.focus="rest:-1";b.rest.put(-1,"2");
        Learning.finishQuestion(state,true,LocalDate.of(2026,9,8),new Random(4));Learning.ensureQuestion(state,new Generator(new Random(5)));assertNull(session.verticalWork);Deferred.open(session,first);assertEquals("2",DecimalDivision.board(session.verticalWork.decimalDivision,DecimalDivision.working(session.question,session.verticalWork.decimalDivision)).rest.get(-1));
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();new ObjectOutputStream(bytes).writeObject(state);state=(Learning.State)new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray())).readObject();session=state.session;DecimalDivision.Draft restored=session.verticalWork.decimalDivision;DecimalDivision.Board rb=DecimalDivision.board(restored,DecimalDivision.working(session.question,restored));assertEquals(Integer.valueOf(-1),rb.place);assertEquals("rest:-1",rb.focus);assertEquals(3,rb.extraPlaces);assertEquals(Integer.valueOf(0),rb.point);
        VerticalWork.Draft copy=VerticalWork.copy(session.verticalWork);copy.decimalDivision.boards.values().iterator().next().rest.put(-1,"9");assertEquals("2",rb.rest.get(-1));Deferred.leave(session);assertNull(session.verticalWork);
    }
    @Test public void exactArithmeticAgreesAcrossSixHundredGeneratedLongDivisions(){
        Random random=new Random(825);
        for(int sample=0;sample<600;sample++){
            int divisor=random.nextInt(98)+2;BigDecimal quotient=BigDecimal.valueOf(random.nextInt(9999),random.nextInt(3)),dividend=quotient.multiply(BigDecimal.valueOf(divisor));
            Question q=q(dividend.movePointLeft(1).toPlainString()+"/"+BigDecimal.valueOf(divisor,1).toPlainString());DecimalDivision.Draft d=applied(q,dividend.stripTrailingZeros().toPlainString(),Integer.toString(divisor));DecimalDivision.Spec s=DecimalDivision.working(q,d);DecimalDivision.Board b=DecimalDivision.board(d,s);while(-s.minPlace(b)<quotient.scale())assertTrue(DecimalDivision.addPlace(s,b));b.point=0;
            BigInteger remainder=BigInteger.ZERO;String source=dividend.setScale(-s.minPlace(b)).toPlainString().replace(".","");int integerCount=s.maxPlace()+1;int needed=integerCount-s.minPlace(b);source="0".repeat(Math.max(0,needed-source.length()))+source;
            for(int index=0;index<source.length();index++){int place=s.maxPlace()-index;BigInteger brought=remainder.multiply(BigInteger.TEN).add(BigInteger.valueOf(source.charAt(index)-'0'));BigInteger[] pair=brought.divideAndRemainder(BigInteger.valueOf(divisor));fill(b,"quotient",place,pair[0].toString());fill(b,"brought",place,brought.toString());fill(b,"product",place,pair[0].multiply(BigInteger.valueOf(divisor)).toString());fill(b,"rest",place,pair[1].toString());remainder=pair[1];}
            assertFalse(q.prompt,DecimalDivision.check(q,d).error());assertEquals(0,quotient.compareTo(new BigDecimal(DecimalDivision.answers(q,d).get(0))));String before=b.product.get(s.minPlace(b));b.product.put(s.minPlace(b),new BigInteger(before).add(BigInteger.ONE).toString());assertEquals(q.prompt,Set.of("product:"+s.minPlace(b)),DecimalDivision.check(q,d).wrong);
        }
    }
    @Test public void recurringDecimalsRemainPartialAndNoRoundedAnswerIsInvented(){
        Question q=q("8/3");q.answers=new String[]{"8/3"};DecimalDivision.Draft d=draft();DecimalDivision.Spec s=DecimalDivision.working(q,d);DecimalDivision.Board b=DecimalDivision.board(d,s);DecimalDivision.addPlace(s,b);b.point=0;b.quotient.put(0,"2");b.quotient.put(-1,"6");b.rest.put(-1,"2");assertFalse(DecimalDivision.check(q,d).error());assertEquals(List.of("2.6"),DecimalDivision.answers(q,d));assertFalse(new Checker().check(q,List.of(),DecimalDivision.answers(q,d)).correct());assertTrue(new Checker().check(q,List.of(),List.of("8/3")).correct());
    }
}
