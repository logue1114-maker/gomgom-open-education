package com.gomgomapps.math.core;

import java.io.*;
import java.time.LocalDate;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class VerticalWorkTest {
    private Question q(String prompt){return new Question("add20",prompt,prompt,"unused");}
    private VerticalWork.Draft draft(String... pairs){VerticalWork.Draft d=new VerticalWork.Draft();for(int i=0;i<pairs.length;i+=2)d.cells.put(pairs[i],pairs[i+1]);return d;}
    @Test public void supportedLayoutComesOnlyFromVisibleOperands(){
        Question q=q("37 × 24");assertEquals(4,VerticalWork.layout(q).columns);assertEquals(2,VerticalWork.layout(q).rows.stream().filter(r->r.id.startsWith("part")).count());q.answers=new String[]{"999999999"};assertEquals(4,VerticalWork.layout(q).columns);
        assertNotNull(VerticalWork.layout(q("2.3+4.8")));assertNotNull(VerticalWork.layout(q("84/7")));
        for(String text:List.of("3/4+1/2","5-9","x+2","3+4*2","37/4","가".repeat(241)))assertNull(VerticalWork.layout(q(text)));
    }
    @Test public void addChecksOnlyWrittenCellsAndReportsNoCorrectedValue(){
        Question q=q("58+67");VerticalWork.Draft d=draft("carry:1","1","answer:0","5");Map<String,String> before=new HashMap<>(d.cells);
        assertFalse(VerticalWork.check(q,d).error());assertFalse(VerticalWork.check(q,d).inputNeeded);assertEquals(before,d.cells);
        d.cells.put("carry:1","0");d.cells.put("answer:1","3");assertEquals(Set.of("carry:1","answer:1"),VerticalWork.check(q,d).wrong);
        assertFalse(VerticalWork.check(q,d).message.contains("125"));assertEquals("0",d.cells.get("carry:1"));
    }
    @Test public void borrowingAcrossZerosAcceptsEquivalentRegroupingAndPartialNotes(){
        Question q=q("302-187");assertFalse(VerticalWork.check(q,draft("borrow:0","12","borrow:1","9","borrow:2","2")).error());
        assertFalse(VerticalWork.check(q,draft("borrow:0","22","borrow:1","8","borrow:2","2")).error());
        assertFalse(VerticalWork.check(q,draft("borrow:0","12")).error());
        assertEquals(Set.of("borrow:2"),VerticalWork.check(q,draft("borrow:0","12","borrow:2","0")).wrong);
        assertEquals(Set.of("borrow:0","borrow:1"),VerticalWork.check(q("62-18"),draft("borrow:0","12","borrow:1","4")).wrong);
    }
    @Test public void multiplicationUsesShiftedPartialProductsAndDoesNotFillZeros(){
        Question q=q("37*24");VerticalWork.Draft d=draft("part0:0","8","part0:1","4","part0:2","1","part1:1","4","part1:2","7","answer:0","8","answer:1","8","answer:2","8");
        assertFalse(VerticalWork.check(q,d).error());assertEquals("888",VerticalWork.answer(q,d));assertFalse(d.cells.containsKey("part1:0"));d.cells.put("part1:1","7");assertEquals(Set.of("part1:1"),VerticalWork.check(q,d).wrong);
    }
    @Test public void answerCopiesOnlyTheStudentsContiguousDigits(){
        Question q=q("99+99");assertNull(VerticalWork.answer(q,draft()));assertNull(VerticalWork.answer(q,draft("answer:2","1","answer:0","8")));
        assertEquals("12",VerticalWork.answer(q,draft("answer:1","1","answer:0","2")));assertEquals("0",VerticalWork.answer(q,draft("answer:0","0")));
        VerticalWork.Result result=VerticalWork.check(q,draft("answer:0","?"));assertTrue(result.inputNeeded);assertFalse(result.error());assertTrue(VerticalWork.check(q,draft()).inputNeeded);
    }
    @Test public void normalStateSerializationAndDeferredWorkKeepIndependentDrafts()throws Exception{
        Learning.State state=new Learning.State();Learning.beginPractice(state,"practice",List.of("add20"),3,false,new Random(2));Learning.ensureQuestion(state,new Generator(new Random(3)));Learning.Session s=state.session;String first=s.question.id;
        s.verticalWork=draft("answer:0","7");s.verticalWork.focus="carry:1";s.workOpen=true;s.workTab=2;
        Learning.finishQuestion(state,true,LocalDate.of(2026,9,8),new Random(4));Learning.ensureQuestion(state,new Generator(new Random(5)));assertNull(s.verticalWork);s.verticalWork=draft("answer:0","9");String next=s.question.id;
        Deferred.open(s,first);assertEquals("7",s.verticalWork.cells.get("answer:0"));assertEquals("carry:1",s.verticalWork.focus);s.verticalWork.cells.put("answer:0","6");Deferred.sync(state);
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();new ObjectOutputStream(bytes).writeObject(state);state=(Learning.State)new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray())).readObject();s=state.session;
        assertEquals(first,s.question.id);assertEquals("6",s.verticalWork.cells.get("answer:0"));assertEquals(1,s.completed);Deferred.leave(s);assertEquals(next,s.question.id);assertEquals("9",s.verticalWork.cells.get("answer:0"));assertTrue(s.workOpen);assertEquals(2,s.workTab);
    }
    @Test public void finalRowsAgreeWithIndependentIntegerArithmeticAcrossThreeOperations(){
        Random random=new Random(93);for(int i=0;i<900;i++){int a=random.nextInt(10000),b=random.nextInt(a+1);String op=List.of("+","-","*").get(i%3);long answer=op.equals("+")?(long)a+b:op.equals("-")?a-b:(long)a*b;Question q=q(a+op+b);VerticalWork.Draft d=new VerticalWork.Draft();String text=Long.toString(answer);for(int p=0;p<text.length();p++)d.cells.put("answer:"+p,text.substring(text.length()-1-p,text.length()-p));assertFalse(q.prompt,VerticalWork.check(q,d).error());assertEquals(text,VerticalWork.answer(q,d));}
    }
}
