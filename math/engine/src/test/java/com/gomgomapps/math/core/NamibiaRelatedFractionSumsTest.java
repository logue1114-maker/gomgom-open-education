package com.gomgomapps.math.core;
import java.util.*;import org.junit.Test;import static org.junit.Assert.*;
public class NamibiaRelatedFractionSumsTest {
 @Test public void visibleDenominatorsControlRelation(){
  CurriculumLimits limits=new CurriculumLimits("relatedDenominators=true");
  assertTrue(limits.allows(new Question("fracAdd","(2/6) + (1/3)","(5/6) + (7/9)","2/3")));
  assertFalse(limits.allows(new Question("fracAdd","(5/6) + (7/9)","(2/6) + (1/3)","29/18")));
  for(String prompt:List.of("2 + 3","(1/3)","(1/0) + (1/3)"))assertFalse(limits.allows(new Question("fracAdd",prompt,"(2/6) + (1/3)","2/3")));
 }
 @Test public void originalUnreducedOperandsRemainCheckableAndHelpIsIndependent(){
  for(String id:List.of("fracAdd","fracSub")){
   String op=id.equals("fracAdd")?" + ":" - ";Question q=new Question(id,"(2/6)"+op+"(1/3)","hidden","wrong");
   assertTrue(GlobalCurriculum.limits("na-nied-primary-2024-v1",id,6).allows(q));
   assertTrue(new Checker().check(q,List.of(),List.of(id.equals("fracAdd")?"2/3":"0/1")).correct());
   HelpPlan help=HelpPlan.forQuestion(q);assertNotNull(help);assertEquals(11,help.size());assertFalse(help.canTransfer());
   assertEquals("6",q.studyGuide.frames.get(1).expected);assertEquals("3",q.studyGuide.frames.get(3).expected);
   for(int step=0;step<help.size();step++){assertEquals("",help.step(step).after);assertTrue(help.step(step).accepts(q.studyGuide.frames.get(step).expected));}
  }
 }
 @Test public void normalGradeSixSupplyHasFreshVisibleRelatedPairs(){
  var pack=GlobalCurriculum.packs("NA").stream().filter(p->p.id.equals("na-nied-primary-2024-v1")).findFirst().orElseThrow();
  for(String id:List.of("fracAdd","fracSub")){
   assertTrue(pack.inGrade(id,6));var limits=GlobalCurriculum.limits(pack.id,id,6);assertTrue(limits.variedFacts());
   Generator generator=new Generator(new Random(278));List<String> recent=new ArrayList<>();boolean unreduced=false,leftLargerDen=false,rightLargerDen=false;
   for(int i=0;i<100;i++){
    Question q=generator.next(id,recent,i%2==0,limits);assertFalse(recent.contains(q.signature()));recent.add(q.signature());int[] v=PublicFractionArithmetic.givens(q);assertNotNull(v);assertNotEquals(v[1],v[3]);assertTrue(v[1]%v[3]==0||v[3]%v[1]==0);assertTrue(limits.allows(q));
    int n=id.equals("fracAdd")?v[0]*v[3]+v[2]*v[1]:v[0]*v[3]-v[2]*v[1];assertTrue(n>=0);String answer=Rational.of(n,v[1]*v[3]).toString();assertTrue(new Checker().check(q,List.of(),List.of(answer)).correct());assertFalse(new Checker().check(q,List.of(),List.of(Rational.of(n+1,v[1]*v[3]).toString())).correct());
    unreduced|=gcd(v[0],v[1])>1||gcd(v[2],v[3])>1;leftLargerDen|=v[1]>v[3];rightLargerDen|=v[3]>v[1];
   }
   assertTrue(unreduced);assertTrue(leftLargerDen);assertTrue(rightLargerDen);
  }
 }
 private static int gcd(int a,int b){return java.math.BigInteger.valueOf(a).gcd(java.math.BigInteger.valueOf(b)).intValue();}
}
