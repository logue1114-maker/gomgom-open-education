package com.gomgomapps.math.core;

import java.io.*;
import java.math.*;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class VerticalExtensionTest {
    private Question q(String text){return new Question("add100",text,text,"unused");}
    private Question pair(String text){Question q=q(text);q.kind="pair";q.answers=new String[]{"unused","unused"};return q;}
    private VerticalWork.Draft draft(){return new VerticalWork.Draft();}
    private void row(VerticalWork.Draft d,String id,String digits,int shift){for(int p=0;p<digits.length();p++)d.cells.put(id+":"+(p+shift),digits.substring(digits.length()-p-1,digits.length()-p));}
    @Test public void decimalsAlignOperandsWithoutUsingTheHiddenAnswer(){Question q=q("6.4+3.28");VerticalWork.Layout l=VerticalWork.layout(q);assertEquals("640",l.top);assertEquals("328",l.bottom);assertEquals(2,l.topScale);assertEquals(2,l.bottomScale);assertEquals(4,l.columns);q.answers=new String[]{"999999"};assertEquals(4,VerticalWork.layout(q).columns);}
    @Test public void pointIsChosenByStudentAndIncompleteFractionalPlacesStayEmpty(){
        Question q=q("4.68*3.6");VerticalWork.Draft d=draft();row(d,"answer","16848",0);assertTrue(VerticalWork.check(q,d).inputNeeded);assertNull(VerticalWork.answer(q,d));
        d.points.put("answer",2);assertEquals(Set.of("point:answer"),VerticalWork.check(q,d).wrong);assertEquals("168.48",VerticalWork.answer(q,d));assertEquals("8",d.cells.get("answer:0"));
        d.points.put("answer",4);assertEquals(Set.of("point:answer"),VerticalWork.check(q,d).wrong);
        d.points.put("answer",3);assertFalse(VerticalWork.check(q,d).error());assertEquals("16.848",VerticalWork.answer(q,d));d.cells.remove("answer:1");assertNull(VerticalWork.answer(q,d));assertFalse(d.cells.containsKey("answer:1"));
    }
    @Test public void equivalentTrailingZeroRepresentationsAreAccepted(){
        Question q=q("0.2+0.20");VerticalWork.Draft a=draft();row(a,"answer","4",0);a.points.put("answer",1);assertFalse(VerticalWork.check(q,a).error());assertEquals("0.4",VerticalWork.answer(q,a));
        VerticalWork.Draft b=draft();row(b,"answer","40",0);b.points.put("answer",2);assertFalse(VerticalWork.check(q,b).error());assertEquals("0.40",VerticalWork.answer(q,b));
        VerticalWork.Draft incomplete=draft();row(incomplete,"answer","4",0);incomplete.points.put("answer",2);assertNull(VerticalWork.answer(q,incomplete));
    }
    @Test public void tinyProductsHaveSpaceForTheirPointAndExplicitZeros(){Question q=q("0.03*0.2");assertTrue(VerticalWork.layout(q).columns>=4);VerticalWork.Draft d=draft();row(d,"answer","006",0);d.points.put("answer",3);assertFalse(VerticalWork.check(q,d).error());assertEquals("0.006",VerticalWork.answer(q,d));}
    @Test public void divisionChecksEachWrittenProductAndRemainderWithoutRequiringEmptySteps(){
        Question q=pair("945/4");VerticalWork.Draft d=draft();row(d,"answer","236",0);row(d,"product2","8",2);row(d,"rest2","1",2);row(d,"product1","12",1);row(d,"rest1","2",1);row(d,"product0","24",0);row(d,"rest0","1",0);
        assertFalse(VerticalWork.check(q,d).error());assertEquals(List.of("236","1"),VerticalWork.answers(q,d));d.cells.put("product1:1","3");assertEquals(Set.of("product1:1"),VerticalWork.check(q,d).wrong);
        VerticalWork.Draft partial=draft();row(partial,"product1","12",1);assertFalse(VerticalWork.check(q,partial).error());assertNull(VerticalWork.answers(q,partial));
    }
    @Test public void zeroQuotientPlacesAndZeroRemaindersMustBeWrittenBeforeCopy(){Question q=pair("408/4");VerticalWork.Draft d=draft();row(d,"answer","102",0);assertNull(VerticalWork.answers(q,d));row(d,"rest0","0",0);assertEquals(List.of("102","0"),VerticalWork.answers(q,d));assertFalse(VerticalWork.check(q,d).error());d.cells.remove("answer:1");assertNull(VerticalWork.answers(q,d));}
    @Test public void decimalDivisionUsesItsOwnBoardWhileIntegerAndPairLayoutsStayCompatible(){assertTrue(VerticalWork.supported(q("1.2/3")));assertTrue(VerticalWork.supported(q("8/3")));assertNull(VerticalWork.layout(pair("10/0")));assertNotNull(VerticalWork.layout(q("84/7")));assertNotNull(VerticalWork.layout(pair("8/3")));}
    @Test public void oldDraftsAndDeferredCopiesPreservePointsAndDivisionSelection()throws Exception{
        VerticalWork.Draft d=draft();d.points=null;d.divisionPlace=2;d.focus="rest2:2";row(d,"rest2","1",2);VerticalWork.points(d).put("answer",2);VerticalWork.Draft copy=VerticalWork.copy(d);copy.points.put("answer",1);assertEquals(Integer.valueOf(2),d.points.get("answer"));
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();new ObjectOutputStream(bytes).writeObject(d);VerticalWork.Draft restored=(VerticalWork.Draft)new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray())).readObject();assertEquals(d.points,restored.points);assertEquals(d.divisionPlace,restored.divisionPlace);assertEquals(d.cells,restored.cells);
    }
    @Test public void independentlyComputedDecimalAndDivisionSamplesAgree(){
        Random random=new Random(113);for(int i=0;i<360;i++){BigDecimal a=BigDecimal.valueOf(random.nextInt(999)+1,2),b=BigDecimal.valueOf(random.nextInt(99)+1,1);String op=i%3==0?"+":i%3==1?"-":"*";if(op.equals("-")&&a.compareTo(b)<0){BigDecimal t=a;a=b;b=t;}BigDecimal value=op.equals("+")?a.add(b):op.equals("-")?a.subtract(b):a.multiply(b);Question q=q(a.toPlainString()+op+b.toPlainString());VerticalWork.Draft d=draft();String digits=value.unscaledValue().toString();while(digits.length()<value.scale())digits="0"+digits;row(d,"answer",digits,0);d.points.put("answer",value.scale());assertFalse(q.prompt,VerticalWork.check(q,d).error());assertEquals(0,value.compareTo(new BigDecimal(VerticalWork.answer(q,d))));}
        for(int i=0;i<360;i++){int b=random.nextInt(99)+1,a=random.nextInt(9999);Question q=pair(a+"/"+b);VerticalWork.Draft d=draft();row(d,"answer",Integer.toString(a/b),0);row(d,"rest0",Integer.toString(a%b),0);assertFalse(q.prompt,VerticalWork.check(q,d).error());assertEquals(List.of(Integer.toString(a/b),Integer.toString(a%b)),VerticalWork.answers(q,d));}
    }
}
