package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;import org.junit.Test;import static org.junit.Assert.*;
public class EnglandFractionRelationsTest {
 @Test public void allEightNewPlacementsHaveIndependentArithmeticAndBlankRestorableHelp(){
  var pack=GlobalCurriculum.packs("GB").stream().filter(p->p.id.equals("england-primary-2021-v1")).findFirst().orElseThrow();
  for(int grade:new int[]{3,4,5,6})for(String id:List.of("el_fraction_compare","el_fraction_common_den","el_mixed_to_improper","el_improper_to_mixed")){
   boolean selected=id.equals("el_fraction_compare")?grade!=4:id.equals("el_fraction_common_den")?grade>=4:grade==5;if(!selected)continue;assertTrue(pack.inGrade(id,grade));Generator generator=new Generator(new Random(7300+grade));List<String> recent=new ArrayList<>();boolean aboveOne=false,nonDividing=false;
   for(int i=0;i<100;i++){
    Question q=generator.next(id,recent,false,GlobalCurriculum.limits(pack.id,id,grade));assertFalse(recent.contains(q.signature()));recent.add(q.signature());Matcher m;
    if(id.equals("el_fraction_compare")){
     m=Pattern.compile("(\\d+)/(\\d+)\\s+□\\s+(\\d+)/(\\d+)").matcher(q.prompt);assertTrue(m.matches());int a=value(m,1),b=value(m,2),c=value(m,3),d=value(m,4);int cmp=Integer.compare(a*d,c*b);String sign=cmp<0?"<":cmp>0?">":"=";assertTrue(new Checker().check(q,List.of(),List.of(sign)).correct());assertFalse(new Checker().check(q,List.of(),List.of(sign.equals("<")?">":"<")).correct());
     if(grade==3){assertEquals(1,a);assertEquals(1,c);}if(grade==5){assertTrue(gcd(b,d)>1);assertTrue(a<=b&&c<=d);nonDividing|=b%d!=0&&d%b!=0;}if(grade==6){assertTrue(a<=3*b&&c<=3*d);aboveOne|=a>b||c>d;}
    }else if(id.equals("el_fraction_common_den")){
     m=Pattern.compile("(\\d+)/(\\d+)을 분모가 (\\d+)인 분수로 통분하세요\\.\\n□/(\\d+)").matcher(q.prompt);assertTrue(m.matches());int a=value(m,1),b=value(m,2),d=value(m,3);assertEquals(0,d%b);assertTrue(new Checker().check(q,List.of(),List.of(String.valueOf(a*(d/b)))).correct());
    }else if(id.equals("el_mixed_to_improper")){
     m=Pattern.compile("(\\d+)와 (\\d+)/(\\d+)을 가분수로 나타내세요\\.\\n□/(\\d+)").matcher(q.prompt);assertTrue(m.matches());assertTrue(new Checker().check(q,List.of(),List.of(String.valueOf(value(m,1)*value(m,3)+value(m,2)))).correct());
    }else{
     m=Pattern.compile("(\\d+)/(\\d+)을 대분수로 나타내세요\\.\\n□와 □/(\\d+)").matcher(q.prompt);assertTrue(m.matches());int a=value(m,1),b=value(m,2);assertTrue(new Checker().check(q,List.of(),List.of(String.valueOf(a/b),String.valueOf(a%b))).correct());
    }
    HelpPlan help=HelpPlan.forQuestion(q);assertNotNull(help);assertFalse(help.canTransfer());Question saved=new Question(id,q.prompt,"hidden","wrong");HelpPlan restored=HelpPlan.forQuestion(saved);assertEquals(help.size(),restored.size());for(int step=0;step<help.size();step++){assertEquals("",help.step(step).after);assertEquals(q.studyGuide.frames.get(step).expected,saved.studyGuide.frames.get(step).expected);}
   }if(grade==5&&id.equals("el_fraction_compare"))assertTrue(nonDividing);if(grade==6&&id.equals("el_fraction_compare"))assertTrue(aboveOne);
  }
 }
 @Test public void comparisonRulesReadPublicFractionsAndLeaveLegacyProperSupplyAlone(){
  CurriculumLimits units=GlobalCurriculum.limits("england-primary-2021-v1","el_fraction_compare",3);assertTrue(units.allows(new Question("el_fraction_compare","1/3  □  1/7","7-3",">")));assertFalse(units.allows(new Question("el_fraction_compare","2/3  □  1/7","14-3",">")));
  Question overOne=new Question("el_fraction_compare","7/3  □  9/4","28-27",">");assertFalse(GlobalCurriculum.limits("england-primary-2021-v1","el_fraction_compare",5).allows(overOne));assertTrue(GlobalCurriculum.limits("england-primary-2021-v1","el_fraction_compare",6).allows(overOne));
  Generator g=new Generator(new Random(7326));for(int i=0;i<100;i++){Question q=g.next("el_fraction_compare",List.of(),false);Matcher m=Pattern.compile("(\\d+)/(\\d+)\\s+□\\s+(\\d+)/(\\d+)").matcher(q.prompt);assertTrue(m.matches());assertTrue(value(m,1)<value(m,2)&&value(m,3)<value(m,4));assertTrue(value(m,2)<=9&&value(m,4)<=9);}
 }
 private static int value(Matcher m,int n){return Integer.parseInt(m.group(n));}private static int gcd(int a,int b){return java.math.BigInteger.valueOf(a).gcd(java.math.BigInteger.valueOf(b)).intValue();}
}
