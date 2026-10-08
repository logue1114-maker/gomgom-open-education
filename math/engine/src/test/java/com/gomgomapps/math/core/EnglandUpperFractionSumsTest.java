package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;import org.junit.Test;import static org.junit.Assert.*;
public class EnglandUpperFractionSumsTest {
 @Test public void integerConversionKeepsExactBoundsWithoutNewAndroidMethods(){
  assertEquals(Integer.MIN_VALUE,Rational.of(Integer.MIN_VALUE).intValue());assertEquals(Integer.MAX_VALUE,Rational.of(Integer.MAX_VALUE).intValue());
  for(long value:new long[]{(long)Integer.MIN_VALUE-1,(long)Integer.MAX_VALUE+1}){try{Rational.of(value).intValue();fail("overflow accepted");}catch(ArithmeticException expected){}}
  try{Rational.of(1,2).intValue();fail("fraction accepted");}catch(IllegalArgumentException expected){}
 }
 @Test public void fourYearFivePlacementsHaveFreshPublicCalculationsAndStudentHelp(){
  var pack=GlobalCurriculum.packs("GB").stream().filter(p->p.id.equals("england-primary-2021-v1")).findFirst().orElseThrow();
  for(String id:List.of("fracAddLike","fracSubLike","fracAdd","fracSub")){
   assertTrue(pack.inGrade(id,5));var limits=GlobalCurriculum.limits(pack.id,id,5);Generator generator=new Generator(new Random(7205));List<String> recent=new ArrayList<>();boolean aboveOne=false,nonDividingPair=false;
   for(int i=0;i<100;i++){
    Question q=generator.next(id,recent,i%2==0,limits);assertFalse(recent.contains(q.signature()));recent.add(q.signature());Matcher m=Pattern.compile("\\((\\d+)/(\\d+)\\) ([+-]) \\((\\d+)/(\\d+)\\)").matcher(q.prompt);assertTrue(m.matches());int a=Integer.parseInt(m.group(1)),b=Integer.parseInt(m.group(2)),c=Integer.parseInt(m.group(4)),d=Integer.parseInt(m.group(5));boolean add=id.startsWith("fracAdd");int n=add?a*d+c*b:a*d-c*b;assertTrue(n>=0);assertTrue(new Checker().check(q,List.of(),List.of(n+"/"+(b*d))).correct());assertFalse(new Checker().check(q,List.of(),List.of((n+1)+"/"+(b*d))).correct());aboveOne|=n>b*d;
    if(id.endsWith("Like"))assertEquals(b,d);else{assertNotEquals(b,d);assertTrue(gcd(b,d)>1);nonDividingPair|=b%d!=0&&d%b!=0;}
    HelpPlan help=HelpPlan.forQuestion(q);assertNotNull(help);assertFalse(help.canTransfer());Question saved=new Question(id,q.prompt,"hidden","wrong");HelpPlan restored=HelpPlan.forQuestion(saved);assertEquals(help.size(),restored.size());for(int step=0;step<help.size();step++){assertEquals("",help.step(step).after);assertTrue(help.step(step).accepts(q.studyGuide.frames.get(step).expected));assertEquals(q.studyGuide.frames.get(step).expected,saved.studyGuide.frames.get(step).expected);assertFalse(help.step(step).before.matches(".*[0-9].*"));}
   }if(id.startsWith("fracAdd"))assertTrue(aboveOne);if(!id.endsWith("Like"))assertTrue(nonDividingPair);
  }
 }
 @Test public void sixAndNineAreAllowedButYearSixAndLegacyRulesStayDistinct(){
  Question q=new Question("fracAdd","(5/6) + (7/9)","(5/6) + (7/9)","29/18");var yearFive=GlobalCurriculum.limits("england-primary-2021-v1",q.skillId,5);assertTrue(yearFive.allows(q));assertFalse(new CurriculumLimits("relatedDenominators=true").allows(q));Question coprime=new Question("fracAdd","(2/3) + (3/5)","(2/3) + (3/5)","19/15");assertFalse(yearFive.allows(coprime));assertTrue(GlobalCurriculum.limits("england-primary-2021-v1",q.skillId,6).allows(coprime));assertFalse(GlobalCurriculum.limits("england-primary-2021-v1",q.skillId,6).variedFacts());assertTrue(new CurriculumLimits("").allows(coprime));
  assertFalse(new CurriculumLimits("commonDenominatorFactor=true").allows(new Question("add9","2 + 3","2 + 3","5")));
 }
 private static int gcd(int a,int b){return java.math.BigInteger.valueOf(a).gcd(java.math.BigInteger.valueOf(b)).intValue();}
}
