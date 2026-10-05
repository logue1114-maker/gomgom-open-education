package com.gomgomapps.math.core;
import java.util.*;
import java.util.regex.*;
import org.junit.Test;
import static org.junit.Assert.*;
public class ExtendedMetricTest {
 static final List<String> IDS=List.of("el_mass_g_mg","el_mass_t_kg","el_capacity_kl_l","el_capacity_ml_kl");
 static final Pattern PUBLIC=Pattern.compile("([0-9]+(?:\\.[0-9]+)?)([a-zA-Z]+) = □([a-zA-Z]+)");
 static Rational solve(String prompt){Matcher m=PUBLIC.matcher(prompt);assertTrue(prompt,m.find());Map<String,Long> base=Map.of("mg",1L,"g",1000L,"kg",1000000L,"t",1000000000L,"mL",1L,"L",1000L,"kL",1000000L,"ML",1000000000L);return Rational.decimal(m.group(1)).mul(Rational.of(base.get(m.group(2)),base.get(m.group(3))));}
 @Test public void fourAdditionalPairsUseExactRatiosBothDirectionsAndIndependentChoices(){
  Generator g=new Generator(new Random(20261006301L));int[] ranks=new int[4];
  for(String id:IDS){List<String> recent=new ArrayList<>();Set<String> fresh=new HashSet<>(),targets=new HashSet<>();boolean decimal=false;
   assertTrue(GlobalCurriculum.pack(profile()).inGrade(id,6));
   for(int i=0;i<500;i++){Question q=g.next(id,recent,true,GlobalCurriculum.limits("au-acara-v9-primary-v1",id,6));if(i<100)assertTrue(fresh.add(q.signature()));recent.add(q.signature());Rational answer=solve(q.prompt);assertEquals(answer,Expression.number(q.answers[0]));assertTrue(new Checker().check(q,List.of(),List.of(answer.decimalText())).correct());assertFalse(new Checker().check(q,List.of(),List.of(answer.add(Rational.ONE).decimalText())).correct());Matcher m=PUBLIC.matcher(q.prompt);assertTrue(m.find());targets.add(m.group(3));decimal|=m.group(1).contains(".");assertEquals(4,new HashSet<>(q.choices).size());assertEquals(q.answers[0],q.choices.get(q.correctChoice));assertTrue(q.choices.stream().allMatch(v->v.length()==q.answers[0].length()&&v.contains(".")==q.answers[0].contains(".")));ranks[(int)q.choices.stream().filter(v->Expression.number(v).compareTo(answer)<0).count()]++;HelpPlan help=HelpPlan.forQuestion(q);assertEquals(2,help.size());assertTrue(help.step(0).accepts("1000"));assertTrue(help.step(1).accepts(answer.decimalText()));assertFalse(help.canTransfer());
   }assertEquals(2,targets.size());assertTrue(decimal);
  }for(int n:ranks)assertTrue(n>300&&n<700);
 }
 private static Learning.Profile profile(){Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"AU");GlobalCurriculum.choosePack(p,"au-acara-v9-primary-v1");p.grade=6;return p;}
 @Test public void globalAdditionsDoNotEnterUnreviewedKoreanDiagnosisOrFutureAustralianScope(){
  Learning.Profile au=profile(),kr=new Learning.Profile();kr.grade=9;kr.term=2;
  for(String id:IDS){assertFalse(GlobalCurriculum.scope(au).stream().anyMatch(s->s.id.equals(id)));assertEquals(0,GlobalCurriculum.limits("au-acara-v9-primary-v1",id,5).metricDecimals());}
  for(Catalog.Skill skill:MetricConversions.SKILLS){assertFalse(Curriculum.inCurriculum(skill,2015));assertFalse(Curriculum.inCurriculum(skill,2022));assertFalse(Learning.diagnosticScope(kr).contains(skill));assertNotNull(new Generator(new Random(1)).create(skill));}
  assertTrue(Curriculum.inCurriculum(Catalog.get("el_mass_t_kg"),2022));
  Generator g=new Generator(new Random(4));for(int i=0;i<100;i++){Question q=g.next("el_mass_t_kg",List.of(),false);assertTrue(Expression.number(q.answers[0]).isInteger());assertFalse(q.prompt.contains("."));}
 }
 @Test public void savedMegaliitreQuestionKeepsCaseSensitivePublicUnitsAndHelp()throws Exception{
  Question q=new Generator(new Random(21)).next("el_capacity_ml_kl",List.of(),false,GlobalCurriculum.limits("au-acara-v9-primary-v1","el_capacity_ml_kl",6));java.io.ByteArrayOutputStream bytes=new java.io.ByteArrayOutputStream();new java.io.ObjectOutputStream(bytes).writeObject(q);Question restored=(Question)new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bytes.toByteArray())).readObject();assertEquals(q.prompt,restored.prompt);assertTrue(restored.prompt.contains("ML"));assertFalse(restored.prompt.contains("mL"));assertEquals(solve(restored.prompt),Expression.number(restored.answers[0]));assertFalse(HelpPlan.forQuestion(restored).canTransfer());
 }
}
