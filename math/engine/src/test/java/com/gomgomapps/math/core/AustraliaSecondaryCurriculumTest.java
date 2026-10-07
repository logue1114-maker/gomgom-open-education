package com.gomgomapps.math.core;
import org.junit.Test;
import java.util.*;
import static org.junit.Assert.*;
public class AustraliaSecondaryCurriculumTest {
 static final String PACK="au-acara-v9-primary-v1";
 @Test public void secondarySelectionPreservesCountryLanguageAndPriorDiagnosis(){
  Learning.Profile p=new Learning.Profile();p.languageTag="fr";GlobalCurriculum.chooseCountry(p,"AU");GlobalCurriculum.choosePack(p,PACK);p.grade=7;
  assertEquals("fr",p.languageTag);GlobalCurriculum.Pack pack=GlobalCurriculum.pack(p);assertEquals(10,pack.maxGrade());assertEquals("Year 10",pack.level(10));
  assertFalse(GlobalCurriculum.scope(p).stream().anyMatch(s->s.id.equals("rootWhole")));p.grade=8;assertTrue(GlobalCurriculum.scope(p).stream().anyMatch(s->s.id.equals("rootWhole")));assertFalse(GlobalCurriculum.scope(p).stream().anyMatch(s->s.id.equals("powerQuotient")));p.grade=9;assertTrue(GlobalCurriculum.scope(p).stream().anyMatch(s->s.id.equals("powerQuotient")));assertFalse(pack.inGrade("complexAdd",10));assertFalse(pack.inGrade("derivative",10));
 }
 @Test public void everySecondaryPlacementIssuesBothFormatsAndRetainsItsConceptDefinition(){
  Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"AU");GlobalCurriculum.choosePack(p,PACK);GlobalCurriculum.Pack pack=GlobalCurriculum.pack(p);Generator g=new Generator(new Random(20261006781L));int placements=0;
  for(int grade=7;grade<=10;grade++)for(String id:pack.grades.keySet())if(pack.inGrade(id,grade)){
   List<String> recent=new ArrayList<>();CurriculumLimits limits=GlobalCurriculum.limits(PACK,id,grade);
   for(int i=0;i<20;i++){Question q=g.next(id,recent,i%2==0,limits);assertTrue(id,limits.allows(q));assertTrue(id,new Checker().check(q,List.of(),List.of(q.answers)).correct());assertFalse(id,Catalog.get(id).concept.isBlank());HelpPlan plan=HelpPlan.forQuestion(q);if(q.studyGuide!=null)for(int stage=0;stage<q.studyGuide.frames.size();stage++){String entered=q.studyGuide.frames.get(stage).expected;try{entered=Expression.number(entered).toString();}catch(RuntimeException symbolic){}if(stage==plan.size()-1&&plan.canTransfer()){if("decimal".equals(q.answerFormat)){entered=Expression.number(entered).decimalText();if(!entered.contains("."))entered+=".0";}if("fraction".equals(q.answerFormat)&&!entered.contains("/"))entered+="/1";}if("decimal".equals(q.studyGuide.frames.get(stage).inputFormat)){entered=Expression.number(entered).decimalText();if(!entered.contains("."))entered+=".0";}assertTrue(id,plan.step(stage).accepts(entered));}recent.add(q.signature());if(recent.size()>10)recent.remove(0);}
   placements++;
  }
  assertEquals(125,placements);
 }
 @Test public void year7EquationsYear8ExponentsAndYear9QuadraticsHonorDomains(){
  Generator g=new Generator(new Random(20261006782L));boolean signed=false,negativeExponent=false,nonMonic=false;
  for(int i=0;i<200;i++){
   Question linear=g.next("linear",List.of(),false,GlobalCurriculum.limits(PACK,"linear",7));assertTrue(Integer.parseInt(linear.answers[0])>=0);
   Question later=g.next("linear",List.of(),false,GlobalCurriculum.limits(PACK,"linear",8));signed|=Integer.parseInt(later.answers[0])<0;
   Question exponent=g.next("powerQuotient",List.of(),false,GlobalCurriculum.limits(PACK,"powerQuotient",8));assertTrue(Integer.parseInt(exponent.answers[0])>=0);
   Question advanced=g.next("powerQuotient",List.of(),false,GlobalCurriculum.limits(PACK,"powerQuotient",9));negativeExponent|=Integer.parseInt(advanced.answers[0])<0;
   Question quadratic=g.next("quadratic",List.of(),false,GlobalCurriculum.limits(PACK,"quadratic",9));assertEquals(Rational.ONE,Expression.parse(quadratic.expression.split("=")[0]).coefficient(2));for(String answer:quadratic.answers)assertTrue(Expression.number(answer).d.equals(java.math.BigInteger.ONE));
   Question laterQuadratic=g.next("quadratic",List.of(),false,GlobalCurriculum.limits(PACK,"quadratic",10));nonMonic|=!Expression.parse(laterQuadratic.expression.split("=")[0]).coefficient(2).equals(Rational.ONE);
  }
  assertTrue(signed);assertTrue(negativeExponent);assertTrue(nonMonic);
 }
 @Test public void linearExpressionComponentDoesNotIssueQuadraticPolynomialTasks(){
  Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"AU");GlobalCurriculum.choosePack(p,PACK);GlobalCurriculum.Pack pack=GlobalCurriculum.pack(p);assertFalse(pack.inGrade("polyAdd",8));assertTrue(pack.inGrade("polyAdd",9));Generator g=new Generator(new Random(20261006811L));for(int i=0;i<100;i++){Question q=g.next("likeTerms",List.of(),false,GlobalCurriculum.limits(PACK,"likeTerms",8));assertTrue(Expression.parse(q.expression).degree()<=1);}
 }
}
