package com.gomgomapps.math.core;
import java.util.*;import org.junit.Test;import static org.junit.Assert.*;
public class DoublingCalculationTest {
 @Test public void allPublicProductsAndQuotientsHaveBlankIndependentIntermediateSteps(){
  Checker checker=new Checker();for(String id:List.of(DoublingCalculation.MULTIPLY,DoublingCalculation.DIVIDE))for(int n=10;n<=99;n++)for(int b:List.of(2,4,8)){
   boolean mul=id.equals(DoublingCalculation.MULTIPLY);Question q=DoublingCalculation.make(id,n,b);q.answers=new String[]{"wrong key"};q.expression="wrong expression";
   assertTrue(checker.check(q,List.of(),List.of(Integer.toString(mul?n*b:n))).correct());assertFalse(checker.check(q,List.of(),List.of(Integer.toString((mul?n*b:n)+1))).correct());
   HelpPlan plan=HelpPlan.forQuestion(q);assertNotNull(plan);assertFalse(plan.canTransfer());assertEquals(2+Integer.numberOfTrailingZeros(b),plan.size());int value=mul?n:n*b;
   assertTrue(plan.step(0).accepts(Integer.toString(value)));assertTrue(plan.step(1).accepts(Integer.toString(b)));HelpPlan.Draft d=plan.restore(null,q.id);assertTrue(d.entries.stream().allMatch(String::isEmpty));
   for(int i=2;i<plan.size();i++){value=mul?value+value:value/2;assertTrue(plan.step(i).accepts(Integer.toString(value)));assertFalse(plan.step(i).accepts(Integer.toString(value+1)));assertEquals(mul?"직전 수 + 직전 수 = ":"직전 수 ÷ 2 = ",plan.step(i).before);}
   assertEquals(mul?n*b:n,value);assertArrayEquals(new String[]{"wrong key"},q.answers);
  }
 }
 @Test public void actualGradeThreeSupplyExhausts270BeforeOldestAndOtherGradesStayUnmapped(){
  Generator g=new Generator(new Random(135));for(String id:List.of(DoublingCalculation.MULTIPLY,DoublingCalculation.DIVIDE)){
   List<String> recent=new ArrayList<>();Set<String> seen=new HashSet<>();CurriculumLimits limits=GlobalCurriculum.limits("england-primary-2021-v1",id,3);
   for(int i=0;i<270;i++){Question q=g.next(id,recent,false,limits);assertTrue(limits.allows(q));assertNotNull(DoublingCalculation.read(q));assertTrue(seen.add(q.signature()));assertTrue(q.choices.isEmpty());recent.add(q.signature());}assertEquals(recent.get(0),g.next(id,recent,false,limits).signature());
   Learning.Profile profile=new Learning.Profile();GlobalCurriculum.chooseCountry(profile,"GB");GlobalCurriculum.choosePack(profile,"england-primary-2021-v1");for(int grade=1;grade<=6;grade++)assertEquals(grade==3,GlobalCurriculum.pack(profile).inGrade(id,grade));
  }
 }
 @Test public void badPublicOperandsDoNotUseHiddenAnswers(){
  for(String p:List.of("9 × 2","100 × 2","15 × 3","20 ÷ 8","800 ÷ 8","12 ÷ 0","20 ÷ 4 junk")){String id=p.contains("×")?DoublingCalculation.MULTIPLY:DoublingCalculation.DIVIDE;Question q=new Question(id,p,"0","0");assertNull(DoublingCalculation.read(q));assertEquals(Checker.Status.INPUT_NEEDED,new Checker().check(q,List.of(),List.of("0")).status);}
 }
}
