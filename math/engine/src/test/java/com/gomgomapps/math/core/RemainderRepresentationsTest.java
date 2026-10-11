package com.gomgomapps.math.core;
import org.junit.Test;import static org.junit.Assert.*;import java.util.*;import java.math.*;import java.io.*;
public class RemainderRepresentationsTest {
 @Test public void publicBoundaryValuesAndExactFormsIgnoreStoredAnswers(){
  for(int b=1;b<=99;b++)for(int a:new int[]{100,101,999,1000,1001,9999,10000,10001,99999,100000,100001,909090,999999,1000000}){
   Question q=RemainderRepresentations.question(a,b);q.answers=new String[]{"poison"};q.expression="poison";Checker c=new Checker();assertTrue(c.check(q,List.of(),List.of(a+"/"+b)).correct());assertTrue(c.check(q,List.of(),List.of((a*2)+"/"+(b*2))).correct());
   HelpPlan h=HelpPlan.forQuestion(q);assertEquals(7,h.size());assertFalse(h.canTransfer());assertTrue(h.restore(null,q.id).entries.stream().allMatch(String::isEmpty));assertTrue(h.step(0).accepts(""+a));assertTrue(h.step(1).accepts(""+b));assertTrue(h.step(2).accepts(""+(a/b)));assertTrue(h.step(3).accepts(""+(a%b)));assertTrue(h.step(4).accepts((a%b)+"/"+b));assertTrue(h.step(5).accepts("fraction"));assertTrue(h.step(6).accepts(a+"/"+b));
   boolean finite;String decimal=null;try{decimal=new BigDecimal(a).divide(new BigDecimal(b)).toPlainString();finite=true;}catch(ArithmeticException e){finite=false;}
   assertEquals(finite,RemainderRepresentations.terminating(a,b));assertEquals(finite,h.step(5).accepts("decimal"));
   if(finite){assertTrue(c.check(q,List.of(),List.of(decimal)).correct());assertTrue(h.step(6).accepts(decimal));}else{String rounded=new BigDecimal(a).divide(new BigDecimal(b),6,RoundingMode.DOWN).toPlainString();assertFalse(c.check(q,List.of(),List.of(rounded)).correct());}
  }
  for(String prompt:List.of("0 ÷ 2","1000001 ÷ 2","100 ÷ 0","100 ÷ 100")){Question q=RemainderRepresentations.question(100,2);q.prompt=prompt+"\n나머지까지 나누어 분수나 유한소수로 나타내세요.";assertNull(RemainderRepresentations.read(q));}
 }
 @Test public void normalPlacementAndSupplyRemainSelectedAndVaried(){
  String id=RemainderRepresentations.ID;var pack=GlobalCurriculum.packs("NA").stream().filter(p->p.id.equals("na-nied-primary-2024-v1")).findFirst().orElseThrow();assertTrue(pack.inGrade(id,6));assertFalse(pack.inGrade(id,5));assertFalse(Curriculum.inCurriculum(RemainderRepresentations.SKILL,2022));
  Generator g=new Generator(new Random(235));CurriculumLimits limits=GlobalCurriculum.limits(pack.id,id,6);List<String> recent=new ArrayList<>();boolean finite=false,repeat=false,large=false;
  for(int i=0;i<200;i++){Question q=g.next(id,recent,false,limits);assertFalse(recent.contains(q.signature()));recent.add(q.signature());int[] v=RemainderRepresentations.read(q);assertTrue(v[0]>=100&&v[0]<=1000000);assertTrue(v[1]>=1&&v[1]<=99);assertTrue(limits.allows(q));assertEquals("fraction",q.answerFormat);assertTrue(q.decimal);boolean f=RemainderRepresentations.terminating(v[0],v[1]);finite|=f;repeat|=!f;large|=v[0]>=100000;}assertTrue(finite&&repeat&&large);
 }
 @Test public void cachedWrongRemainderFractionRetainsDraftAndQuestion()throws Exception{
  Learning.Session s=new Learning.Session();s.question=RemainderRepresentations.question(101,3);HelpPlan h=HelpPlan.forQuestion(s.question);s.conceptHelp=h.restore(null,s.question.id);s.conceptHelp.entries=new ArrayList<>(List.of("101","3","33","2","1/3"));s.conceptHelp.stage=4;
  ByteArrayOutputStream bytes=new ByteArrayOutputStream();new ObjectOutputStream(bytes).writeObject(s);Learning.Session saved=(Learning.Session)new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray())).readObject();HelpPlan restored=HelpPlan.forQuestion(saved.question);HelpPlan.Draft d=restored.restore(saved.conceptHelp,saved.question.id);assertEquals(s.question.id,saved.question.id);assertEquals(4,d.stage);assertEquals("1/3",d.entries.get(4));assertFalse(restored.step(4).accepts(d.entries.get(4)));assertEquals("division-remainder-value-v1",d.teachingVersion);
 }
}
