package com.gomgomapps.math.core;

import java.io.*;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class MultiplicationCarryTest {
    private Question q(String prompt){return new Question("mul22",prompt,prompt,"unused");}
    private VerticalWork.Draft draft(String... pairs){VerticalWork.Draft d=new VerticalWork.Draft();for(int i=0;i<pairs.length;i+=2)d.cells.put(pairs[i],pairs[i+1]);return d;}
    @Test public void eachMultiplierHasIndependentNotesAndOnlyWrittenErrorsAreMarked(){
        Question q=q("37*24");VerticalWork.Draft d=draft("mulCarry0:1","2","mulCarry0:2","1","mulCarry1:1","1","mulCarry1:2","0");Map<String,String> before=new LinkedHashMap<>(d.cells);
        assertFalse(VerticalWork.check(q,d).error());assertEquals(before,d.cells);assertNull(VerticalWork.answers(q,d));
        d.cells.put("mulCarry1:1","2");assertEquals(Set.of("mulCarry1:1"),VerticalWork.check(q,d).wrong);assertEquals("2",d.cells.get("mulCarry1:1"));assertEquals("표시한 칸 확인",VerticalWork.check(q,d).message);
        assertFalse(d.cells.containsKey("part0:0"));assertFalse(d.cells.containsKey("answer:0"));
    }
    @Test public void summingShiftedProductsHasSeparateCarryNotes(){
        Question q=q("99*99");VerticalWork.Draft d=draft("mulCarry0:1","8","mulCarry1:1","8","sumCarry:1","0","sumCarry:2","1","sumCarry:3","1");
        assertFalse(VerticalWork.check(q,d).error());d.cells.put("sumCarry:2","8");assertEquals(Set.of("sumCarry:2"),VerticalWork.check(q,d).wrong);
        assertFalse(VerticalWork.check(q,draft("answer:3","9","answer:2","8","answer:1","0","answer:0","1")).error());
    }
    @Test public void singleDigitZeroOneAndDecimalUseOptionalWholeDigitCarries(){
        for(String prompt:List.of("99*0","99*1","0*99"))assertFalse(prompt,VerticalWork.check(q(prompt),draft("mulCarry0:1","0")).error());
        assertFalse(VerticalWork.layout(q("99*8")).rows.stream().anyMatch(r->r.id.startsWith("part")||r.id.equals("sumCarry")));
        Question decimal=q("6.31*4.8");VerticalWork.Draft d=draft("mulCarry0:1","0","mulCarry0:2","2","mulCarry0:3","5","mulCarry1:1","0","mulCarry1:2","1","mulCarry1:3","2");
        assertFalse(VerticalWork.check(decimal,d).error());assertFalse(VerticalWork.check(decimal,d).inputNeeded);assertTrue(d.points.isEmpty());assertNull(VerticalWork.answer(decimal,d));
    }
    @Test public void carriesAgreeWithIndependentLowPartArithmetic(){
        Random random=new Random(419);for(int sample=0;sample<600;sample++){
            int a=random.nextInt(999999)+1,b=random.nextInt(999999);Question q=q(a+"*"+b);VerticalWork.Layout l=VerticalWork.layout(q);VerticalWork.Draft d=new VerticalWork.Draft();
            for(VerticalWork.Row row:l.rows)if(row.notes)for(int p=row.shift;p<row.shift+row.width;p++){
                BigInteger power=BigInteger.TEN.pow(p),expected;
                if(row.id.startsWith("mulCarry")){int stage=Integer.parseInt(row.id.substring(8));int digit=BigInteger.valueOf(b).divide(BigInteger.TEN.pow(stage)).mod(BigInteger.TEN).intValue();expected=BigInteger.valueOf(a).mod(power).multiply(BigInteger.valueOf(digit)).divide(power);}
                else{BigInteger lowSum=BigInteger.ZERO;for(int stage=0;stage<Integer.toString(b).length();stage++){int digit=BigInteger.valueOf(b).divide(BigInteger.TEN.pow(stage)).mod(BigInteger.TEN).intValue();BigInteger part=BigInteger.valueOf(a).multiply(BigInteger.valueOf(digit)).multiply(BigInteger.TEN.pow(stage));lowSum=lowSum.add(part.mod(power));}expected=lowSum.divide(power);}
                d.cells.put(row.id+":"+p,expected.toString());
            }
            assertFalse(q.prompt,VerticalWork.check(q,d).error());Map<String,String> before=new LinkedHashMap<>(d.cells);assertNull(VerticalWork.answers(q,d));assertEquals(before,d.cells);
            String key=d.cells.keySet().iterator().next();int saved=Integer.parseInt(d.cells.get(key));d.cells.put(key,Integer.toString((saved+1)%10));assertEquals(q.prompt,Set.of(key),VerticalWork.check(q,d).wrong);
        }
    }
    @Test public void stageAndNotesSurviveDeferredWorkAndSerializationWithoutCrossQuestionLeaks()throws Exception{
        Learning.State state=new Learning.State();Learning.beginPractice(state,"practice",List.of("mul22"),3,false,new Random(2));Learning.ensureQuestion(state,new Generator(new Random(3)));Learning.Session s=state.session;String first=s.question.id;
        s.verticalWork=draft("mulCarry0:1","2","mulCarry1:1","1","sumCarry:2","0");s.verticalWork.multiplicationPlace=1;s.verticalWork.focus="mulCarry1:1";
        Learning.finishQuestion(state,true,LocalDate.of(2026,9,8),new Random(4));Learning.ensureQuestion(state,new Generator(new Random(5)));assertNull(s.verticalWork);s.verticalWork=draft("mulCarry0:1","8");s.verticalWork.multiplicationPlace=0;String next=s.question.id;
        Deferred.open(s,first);assertEquals(Integer.valueOf(1),s.verticalWork.multiplicationPlace);assertEquals("mulCarry1:1",s.verticalWork.focus);s.verticalWork.cells.put("mulCarry1:1","3");Deferred.sync(state);
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();new ObjectOutputStream(bytes).writeObject(state);state=(Learning.State)new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray())).readObject();s=state.session;
        assertEquals(first,s.question.id);assertEquals("3",s.verticalWork.cells.get("mulCarry1:1"));assertEquals(Integer.valueOf(1),s.verticalWork.multiplicationPlace);Deferred.leave(s);assertEquals(next,s.question.id);assertEquals("8",s.verticalWork.cells.get("mulCarry0:1"));assertEquals(Integer.valueOf(0),s.verticalWork.multiplicationPlace);
    }
}
