package com.gomgomapps.math.core;
import java.math.BigDecimal;import java.util.*;import org.junit.Test;import static org.junit.Assert.*;
public class YearFiveFractionDecimalTest {
 private CurriculumLimits limits(String id){return GlobalCurriculum.limits("england-primary-2021-v1",id,5);}
 @Test public void allProperPowerTenFractionsUseExactDecimalsAndBlankUnitFrames(){
  Catalog.Skill skill=Catalog.get("el_fraction_decimal");int count=0;
  for(int d:List.of(10,100,1000))for(int n=1;n<d;n++){
   Question q=ElementaryBasics.fractionDecimal(skill,n,d);assertTrue(limits(skill.id).allows(q));
   String expected=BigDecimal.valueOf(n).divide(BigDecimal.valueOf(d)).toPlainString();
   assertTrue(new Checker().check(q,List.of(),List.of(expected)).correct());
   // Restore only visible givens; poison hidden answers so they cannot provide help.
   Question saved=new Question(skill.id,q.prompt,"poison","999");HelpPlan h=HelpPlan.forQuestion(saved);
   assertEquals(4,h.size());assertFalse(h.canTransfer());List<String> entries=List.of(""+n,""+d,BigDecimal.ONE.divide(BigDecimal.valueOf(d)).toPlainString(),expected);
   for(int i=0;i<4;i++){assertTrue(h.step(i).accepts(entries.get(i)));assertEquals("",h.step(i).after);}
   if(d==1000)assertNull(saved.diagram);count++;
  }assertEquals(1107,count);
 }
 @Test public void selectedGradeAndGeneratorIncludeThirdPlacesWithoutChangingYearFour(){
  var pack=GlobalCurriculum.packs("GB").stream().filter(p->p.id.equals("england-primary-2021-v1")).findFirst().orElseThrow();assertTrue(pack.inGrade("el_fraction_decimal",5));
  assertEquals(2,GlobalCurriculum.limits(pack.id,"el_fraction_decimal",4).decimalPlaces(1));assertEquals(3,limits("el_decimal_fraction").decimalPlaces(1));
  Generator generator=new Generator(new Random(83));List<String> recent=new ArrayList<>();boolean thousandth=false;
  for(int i=0;i<100;i++){Question q=generator.next("el_fraction_decimal",recent,false,limits("el_fraction_decimal"));assertFalse(recent.contains(q.signature()));recent.add(q.signature());String[] raw=q.prompt.substring(0,q.prompt.indexOf('의')).split("/");BigDecimal n=new BigDecimal(raw[0]),d=new BigDecimal(raw[1]);thousandth|=d.intValue()==1000;assertTrue(new Checker().check(q,List.of(),List.of(n.divide(d).toPlainString())).correct());}assertTrue(thousandth);
 }
 @Test public void generatedDecimalToFractionKeepsExactThirdPlaceAndPublicOnlyHelp(){
  Generator generator=new Generator(new Random(830));boolean third=false,belowOne=false;
  for(int i=0;i<300;i++){Question q=generator.next("el_decimal_fraction",List.of(),false,limits("el_decimal_fraction"));String raw=q.prompt.substring(0,q.prompt.indexOf('의'));BigDecimal x=new BigDecimal(raw);int places=x.scale();third|=places==3;belowOne|=x.compareTo(BigDecimal.ONE)<0;String denominator=BigDecimal.TEN.pow(places).toPlainString(),numerator=x.movePointRight(places).toBigIntegerExact().toString();assertTrue(new Checker().check(q,List.of(),List.of(numerator+"/"+denominator)).correct());Question saved=new Question(q.skillId,q.prompt,"poison","999");HelpPlan h=HelpPlan.forQuestion(saved);assertFalse(h.canTransfer());List<String> inputs=List.of(raw,""+places,denominator,numerator);for(int s=0;s<4;s++)assertTrue(h.step(s).accepts(inputs.get(s)));}
  assertTrue(third);assertTrue(belowOne);
 }
}
