package com.gomgomapps.math.core;
import java.util.*;import org.junit.Test;import static org.junit.Assert.*;
public class FractionOrderingTest {
 @Test public void fourPlacementsHaveFreshIndependentlySortedPublicFractions(){
  var pack=GlobalCurriculum.packs("GB").stream().filter(p->p.id.equals("england-primary-2021-v1")).findFirst().orElseThrow();
  for(int grade:new int[]{3,5,6})for(String id:List.of("fractionOrder","likeFractionOrder")){
   if(id.equals("likeFractionOrder")&&grade!=3)continue;assertTrue(pack.inGrade(id,grade));Generator generator=new Generator(new Random(7400+grade));List<String> recent=new ArrayList<>();boolean ascending=false,descending=false,overOne=false;
   for(int i=0;i<100;i++){
    Question q=generator.next(id,recent,false,GlobalCurriculum.limits(pack.id,id,grade));assertFalse(recent.contains(q.signature()));recent.add(q.signature());List<String> given=FractionOrdering.givens(q);assertEquals(3,given.size());boolean reverse=q.prompt.startsWith("큰");ascending|=!reverse;descending|=reverse;List<String> sorted=new ArrayList<>(given);sorted.sort((a,b)->Expression.number(a).compareTo(Expression.number(b))*(reverse?-1:1));assertTrue(new Checker().check(q,List.of(),sorted).correct());Collections.swap(sorted,0,1);assertFalse(new Checker().check(q,List.of(),sorted).correct());
    int common=0,den=-1;for(String value:given){String[] pair=value.split("/");int a=Integer.parseInt(pair[0]),b=Integer.parseInt(pair[1]);if(id.equals("likeFractionOrder")){if(den==-1)den=b;assertEquals(den,b);}else if(grade==3)assertEquals(1,a);if(grade==5){assertTrue(a<=b);common=common==0?b:gcd(common,b);}if(grade==6){assertTrue(a<=3*b);overOne|=a>b;}}if(grade==5)assertTrue(common>1);
    HelpPlan help=HelpPlan.forQuestion(q);assertEquals(3,help.size());assertFalse(help.canTransfer());Question saved=new Question(id,q.prompt,"poisoned","wrong");HelpPlan restored=HelpPlan.forQuestion(saved);assertEquals(3,restored.size());for(int step=0;step<3;step++){assertEquals("",help.step(step).after);assertEquals(3-step,help.step(step).options.size());assertEquals(q.studyGuide.frames.get(step).expected,saved.studyGuide.frames.get(step).expected);assertTrue(given.contains(q.studyGuide.frames.get(step).expected));}
   }assertTrue(ascending&&descending);if(grade==6)assertTrue(overOne);
  }
 }
 @Test public void threeDenominatorsNeedOneSharedFactorAndBadDomainsFail(){
  CurriculumLimits limits=new CurriculumLimits("commonDenominatorFactor=true");assertFalse(limits.allows(new Question("fractionOrder","작은 분수부터 고르세요.\n1/6 · 1/10 · 1/15","1/6+1/10+1/15","1/15","1/10","1/6")));assertTrue(limits.allows(new Question("fractionOrder","작은 분수부터 고르세요.\n1/4 · 1/6 · 1/8","1/4+1/6+1/8","1/8","1/6","1/4")));
  try{FractionOrdering.create(Catalog.get("fractionOrder"),new Random(1),new CurriculumLimits("unitFractions=true;denominators=2,4"));fail("impossible domain");}catch(IllegalArgumentException expected){}
 }
 private static int gcd(int a,int b){return java.math.BigInteger.valueOf(a).gcd(java.math.BigInteger.valueOf(b)).intValue();}
}
