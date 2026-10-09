package com.gomgomapps.math.core;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;
public class ShortWrittenDivisionTest {
 @Test public void allOperandConditionsUsePublicDivisionAndMarkOnlyWrongAnswer(){
  Checker c=new Checker();
  for(int a=1;a<=9999;a++)for(int b=2;b<=9;b++){
   Question q=ShortWrittenDivision.indexed((a-1)*8+b-2);q.answers=new String[]{"poison","poison"};q.expression="poison";
   assertArrayEquals(new int[]{a,b,a/b,a%b},ShortWrittenDivision.read(q));
   assertTrue(c.check(q,List.of(),List.of(""+(a/b),""+(a%b))).correct());
   assertEquals(1,c.check(q,List.of(),List.of(""+(a/b),""+(a%b+1))).index);
  }
 }
 @Test public void shortWorkspaceRetainsInternalZerosAndOnlyLearnerFinalRows(){
  Question q=ShortWrittenDivision.indexed((4013-1)*8+4-2);VerticalWork.Layout l=VerticalWork.layout(q);assertNotNull(l);assertEquals(4,l.columns);assertEquals(5,l.rows.size());assertTrue(l.rows.stream().noneMatch(r->r.id.startsWith("product")));
  VerticalWork.Draft d=new VerticalWork.Draft();assertNull(VerticalWork.answers(q,d));
  d.cells.put("rest3:3","0");d.cells.put("rest2:2","0");d.cells.put("rest1:1","1");d.cells.put("rest0:0","1");
  d.cells.put("answer:3","1");d.cells.put("answer:2","0");d.cells.put("answer:0","3");assertNull(VerticalWork.answers(q,d));d.cells.put("answer:1","0");
  assertEquals(List.of("1003","1"),VerticalWork.answers(q,d));assertFalse(VerticalWork.check(q,d).error());
  d.cells.put("rest1:1","2");assertEquals(Set.of("rest1:1"),VerticalWork.check(q,d).wrong);d.cells.put("rest1:1","1");assertFalse(VerticalWork.check(q,d).error());
 }
 @Test public void smallDividendZeroRemainderAndHelpDoNotFillAnswers(){
  for(int a:List.of(1,8,40,101,4001,9999))for(int b:List.of(2,4,9)){
   Question q=ShortWrittenDivision.indexed((a-1)*8+b-2);HelpPlan h=HelpPlan.forQuestion(q);assertNotNull(h);assertFalse(h.canTransfer());assertEquals(2+3*Integer.toString(a).length(),h.size());assertTrue(h.restore(null,q.id).entries.stream().allMatch(String::isEmpty));
   assertTrue(h.step(0).accepts(""+a));assertTrue(h.step(1).accepts(""+b));int rest=0,i=2;
   for(char digit:Integer.toString(a).toCharArray()){int current=rest*10+digit-'0';assertTrue(h.step(i++).accepts(""+current));assertTrue(h.step(i++).accepts(""+(current/b)));rest=current%b;assertTrue(h.step(i++).accepts(""+rest));}
  }
 }
 @Test public void actualYearFiveFreshSupplyAndMalformedPublicInputs(){
  Generator g=new Generator(new Random(168));List<String> recent=new ArrayList<>();CurriculumLimits limits=GlobalCurriculum.limits("england-primary-2021-v1",ShortWrittenDivision.ID,5);
  for(int i=0;i<100;i++){Question q=g.next(ShortWrittenDivision.ID,recent,false,limits);assertFalse(recent.contains(q.signature()));recent.add(q.signature());assertTrue(limits.allows(q));assertNotNull(ShortWrittenDivision.read(q));}
  Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"GB");GlobalCurriculum.choosePack(p,"england-primary-2021-v1");for(int year=1;year<=6;year++)assertEquals(year==5,GlobalCurriculum.pack(p).inGrade(ShortWrittenDivision.ID,year));
  for(String prompt:List.of("0 ÷ 2","10000 ÷ 2","12 ÷ 0","12 ÷ 1","12 ÷ 10","12 ÷ 4 junk")){Question q=new Question(ShortWrittenDivision.ID,prompt,"",new String[]{"3","0"});q.kind="pair";assertNull(ShortWrittenDivision.read(q));assertNull(VerticalWork.layout(q));}
 }
}
