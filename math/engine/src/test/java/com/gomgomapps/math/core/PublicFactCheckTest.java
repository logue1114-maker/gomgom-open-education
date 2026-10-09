package com.gomgomapps.math.core;
import org.junit.Test;import static org.junit.Assert.*;import java.util.*;
public class PublicFactCheckTest {
 private final Checker checker=new Checker();
 private void verify(String id,String prompt,long expected){
  Question q=new Question(id,prompt,"corrupt expression","999999");
  assertTrue(prompt,checker.check(q,List.of(),List.of(Long.toString(expected))).correct());
  assertEquals(Checker.Status.WRONG_ANSWER,checker.check(q,List.of(),List.of(Long.toString(expected+1))).status);
  HelpPlan p=HelpPlan.forQuestion(q);assertNotNull(p);assertFalse(p.canTransfer());
  assertTrue(p.step(p.size()-1).accepts(Long.toString(expected)));
  for(int i=0;i<p.size();i++)assertFalse(p.step(i).before.matches(".*[0-9].*"));
  assertArrayEquals(new String[]{"999999"},q.answers);
 }
 @Test public void wholeKnownTablesAndBothMissingOperandsUsePublicEquations(){
  for(int a=0;a<=12;a++)for(int b=0;b<=12;b++){
   verify("tables",a+" × "+b,(long)a*b);
   if(b>0){verify("divide",a*b+" ÷ "+b,a);verify("divide","□ ÷ "+b+" = "+a,(long)a*b);verify("tables","□ × "+b+" = "+a*b,a);}
   if(a>0)verify("tables",a+" × □ = "+a*b,b);
   if(a>0&&b>0)verify("divide",a*b+" ÷ □ = "+a,b);
  }
 }
 @Test public void malformedAmbiguousAndNonIntegralFactsCannotFallBackToKeys(){
  for(String prompt:List.of("□ × 0 = 0","0 × □ = 0","□ × □ = 12","3 × □ = 10","3 ÷ 0","3 ÷ 2","12 ÷ □ = 0","0 ÷ □ = 0","0 ÷ □ = 4","4 × 3 = 12","□ ÷ 0 = 0","3 × -4","1000000 × 2","12 ÷ 3 junk","3 × □")){
   Question q=new Question(prompt.contains("×")?"tables":"divide",prompt,"0","0");
   assertEquals(prompt,Checker.Status.INPUT_NEEDED,checker.check(q,List.of(),List.of("0")).status);
  }
  assertEquals(Checker.Status.INPUT_NEEDED,checker.check(new Question("tables","12 ÷ 3","4","4"),List.of(),List.of("4")).status);
  Question q=new Question("divide","12 ÷ 3","999","999");
  for(String s:List.of("+4","4.0","8/2"))assertTrue(checker.check(q,List.of(),List.of(s)).correct());
  for(String s:List.of("","four","4/0","2+2"))assertEquals(Checker.Status.INPUT_NEEDED,checker.check(q,List.of(),List.of(s)).status);
  assertTrue(checker.check(new Question("tables","999999 × 999999","bad","bad"),List.of(),List.of("999998000001")).correct());
 }
 @Test public void englandYearThreeSupplyStaysFreshWithinItsKnownTables(){
  Generator generator=new Generator(new Random(133));
  for(String id:List.of("tables","divide")){
   List<String> recent=new ArrayList<>();Set<String> seen=new HashSet<>(),forms=new HashSet<>();
   CurriculumLimits limits=GlobalCurriculum.limits("england-primary-2021-v1",id,3);
   for(int i=0;i<100;i++){
    Question q=generator.next(id,recent,false,limits);assertTrue(limits.allows(q));
    long expected=FactFormsTest.solve(q.prompt);q.answers=new String[]{"corrupt"};q.expression="corrupt";
    assertTrue(checker.check(q,List.of(),List.of(Long.toString(expected))).correct());
    assertTrue(seen.add(q.signature()));recent.add(q.signature());forms.add(q.prompt.startsWith("□")?"left":q.prompt.contains("□")?"right":"direct");
   }
   assertEquals(Set.of("direct","left","right"),forms);
  }
 }
}
