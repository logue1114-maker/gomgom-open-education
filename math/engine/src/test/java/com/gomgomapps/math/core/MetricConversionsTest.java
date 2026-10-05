package com.gomgomapps.math.core;
import java.util.*;
import java.util.regex.*;
import org.junit.Test;
import static org.junit.Assert.*;
public class MetricConversionsTest {
 static final List<String> IDS=List.of("el_length_mm_cm","el_length_m_cm","el_length_km_m","el_mass_kg_g","el_capacity_l_ml");
 static final Pattern PUBLIC=Pattern.compile("([0-9]+(?:\\.[0-9]+)?)([a-zA-Z]+) = □([a-zA-Z]+)");
 static Rational solve(String prompt){Matcher m=PUBLIC.matcher(prompt);assertTrue(prompt,m.find());Map<String,Integer> base=Map.of("mm",1,"cm",10,"m",1000,"km",1000000,"g",1,"kg",1000,"mL",1,"L",1000);return Rational.decimal(m.group(1)).mul(Rational.of(base.get(m.group(2)),base.get(m.group(3))));}
 @Test public void decimalMetricConversionsUsePublicUnitsBothDirectionsAndStudentHelp(){
  Generator g=new Generator(new Random(20261006251L));Checker checker=new Checker();int[] ranks=new int[4];
  for(String id:IDS){List<String> recent=new ArrayList<>();Set<String> fresh=new HashSet<>();Set<String> targets=new HashSet<>();boolean decimalGiven=false,decimalResult=false;
   CurriculumLimits limits=GlobalCurriculum.limits("au-acara-v9-primary-v1",id,6);assertEquals(3,limits.metricDecimals());
   for(int i=0;i<500;i++){Question q=g.next(id,recent,true,limits);if(i<100)assertTrue(fresh.add(q.signature()));recent.add(q.signature());Rational answer=solve(q.prompt);assertEquals(answer,Expression.number(q.answers[0]));assertTrue(checker.check(q,List.of(),List.of(answer.decimalText())).correct());assertFalse(checker.check(q,List.of(),List.of(answer.add(Rational.ONE).decimalText())).correct());Matcher m=PUBLIC.matcher(q.prompt);assertTrue(m.find());targets.add(m.group(3));decimalGiven|=m.group(1).contains(".");decimalResult|=!answer.isInteger();
    assertEquals(4,q.choices.size());assertEquals(4,new HashSet<>(q.choices).size());assertEquals(q.answers[0],q.choices.get(q.correctChoice));assertTrue(q.choices.stream().allMatch(v->v.length()==q.answers[0].length()&&v.contains(".")==q.answers[0].contains(".")));
    ranks[(int)q.choices.stream().filter(v->Expression.number(v).compareTo(answer)<0).count()]++;
    HelpPlan plan=HelpPlan.forQuestion(q);assertEquals(2,plan.size());assertFalse(plan.canTransfer());assertTrue(plan.step(1).accepts(answer.decimalText()));
   }assertEquals(2,targets.size());assertTrue(decimalGiven);assertTrue(decimalResult);
  }for(int count:ranks)assertTrue("No smallest/largest-answer cue",count>400&&count<850);
 }
 @Test public void lowerGradesAndDefaultWholeUnitExercisesStayUnchanged(){Generator g=new Generator(new Random(20261006252L));for(String id:IDS){assertEquals(0,GlobalCurriculum.limits("au-acara-v9-primary-v1",id,5).metricDecimals());assertEquals(0,CurriculumLimits.NONE.metricDecimals());for(int i=0;i<100;i++){Question q=g.next(id,List.of(),false);assertFalse(q.prompt.contains("."));assertTrue(Expression.number(q.answers[0]).isInteger());}}}
 @Test public void rejectsUnsupportedPrecision(){for(int p:List.of(-1,4,9))try{new CurriculumLimits("metricDecimals="+p);fail();}catch(IllegalArgumentException expected){}}
}
