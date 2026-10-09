package com.gomgomapps.math.core;
import java.math.*;import java.util.*;import org.junit.Test;import static org.junit.Assert.*;
public class YearSixDecimalRoundingTest {
 private CurriculumLimits limits(){return GlobalCurriculum.limits("england-primary-2021-v1","el_decimal_round",6);}
 @Test public void allThreeHundredThousandConditionsRoundExactPublicDigits(){
  var l=limits();assertEquals(300000,DecimalRoundingSupply.count(l));Set<Integer> targets=new HashSet<>();Checker checker=new Checker();
  for(int i=0;i<300000;i++){Question q=DecimalRoundingSupply.at(l,i);var v=RoundingRelations.read(q);assertNotNull(v);assertTrue(l.allows(q));assertEquals(3,new BigDecimal(v.first()).scale());targets.add(v.decimalPlaces());BigDecimal expected=new BigDecimal(v.first()).setScale(v.decimalPlaces(),RoundingMode.HALF_UP);assertEquals(0,expected.compareTo(new BigDecimal(q.answers[0])));assertTrue(checker.check(q,List.of(),List.of(expected.toPlainString())).correct());}
  assertEquals(Set.of(0,1,2),targets);
 }
 @Test public void tiesCarryAndZeroUsePublicBlankHelpWithoutTransfer(){
  for(String raw:List.of("0.000","0.005","0.050","0.500","9.995","99.999"))for(int target=0;target<3;target++){
   int index=new BigDecimal(raw).movePointRight(3).intValueExact()*3+target;Question q=DecimalRoundingSupply.at(limits(),index);q.expression="poison";q.answers=new String[]{"999"};HelpPlan h=HelpPlan.forQuestion(q);assertFalse(h.canTransfer());BigDecimal x=new BigDecimal(raw),unit=BigDecimal.ONE.movePointLeft(target),base=x.divideToIntegralValue(unit).multiply(unit);int digit=x.subtract(base).movePointRight(target+1).intValue();List<String> inputs=new ArrayList<>(List.of(raw));if(target>0)inputs.add(""+target);inputs.addAll(List.of(unit.toPlainString(),""+digit,digit>=5?"raise":"keep",base.toPlainString(),digit>=5?unit.toPlainString():"0",x.setScale(target,RoundingMode.HALF_UP).toPlainString()));assertEquals(inputs.size(),h.size());for(int i=0;i<inputs.size();i++){assertTrue(raw+" target"+target+" step"+i,h.step(i).accepts(inputs.get(i)));assertEquals("",h.step(i).after);}assertFalse(h.step(h.size()-1).accepts(x.setScale(target,RoundingMode.HALF_UP).add(unit).toPlainString()));
  }
 }
 @Test public void gradeLinkAndFreshConditionsReuseExistingType(){
  var pack=GlobalCurriculum.packs("GB").stream().filter(x->x.id.equals("england-primary-2021-v1")).findFirst().orElseThrow();assertTrue(pack.inGrade("el_decimal_round",6));Generator g=new Generator(new Random(95));List<String> recent=new ArrayList<>();Set<Integer> targets=new HashSet<>();for(int i=0;i<150;i++){Question q=g.next("el_decimal_round",recent,false,limits());assertFalse(recent.contains(q.signature()));recent.add(q.signature());targets.add(RoundingRelations.read(q).decimalPlaces());}assertEquals(Set.of(0,1,2),targets);
 }
}
