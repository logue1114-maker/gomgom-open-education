package com.gomgomapps.math.core;
import java.math.BigDecimal;import java.util.*;import org.junit.Test;import static org.junit.Assert.*;
public class PowerTenDivisionTest {
 @Test public void decimalValuesAcceptWholeZeroWithoutForcingPointAndRejectFractionNotation(){
  Question q=PowerTenDivision.candidates(Catalog.get("wholeDivideTen"),new CurriculumLimits("wholeMaximum=9")).values().stream().filter(v->v.prompt.startsWith("0 ÷")).findFirst().orElseThrow();Checker checker=new Checker();assertTrue(checker.check(q,List.of(),List.of("0")).correct());assertTrue(checker.check(q,List.of(),List.of("0.0")).correct());assertEquals(Checker.Status.INPUT_NEEDED,checker.check(q,List.of(),List.of("0/10")).status);
 }
 @Test public void fourPlacementsHaveExactFiniteUnitDomainsAndNoHiddenAnswerHelp(){
  var pack=GlobalCurriculum.packs("GB").stream().filter(p->p.id.equals("england-primary-2021-v1")).findFirst().orElseThrow();
  for(int grade:List.of(3,4))for(Catalog.Skill skill:PowerTenDivision.SKILLS){if(grade==3&&!skill.id.equals("wholeDivideTen"))continue;assertTrue(pack.inGrade(skill.id,grade));var limits=GlobalCurriculum.limits(pack.id,skill.id,grade);Map<String,Question> pool=PowerTenDivision.candidates(skill,limits);int expected=grade==3?10:100;assertEquals(expected,pool.size());boolean tenth=skill.id.equals("tenthsDivideTen");BigDecimal unit=tenth?new BigDecimal("0.1"):BigDecimal.ONE;int divisor=skill.id.equals("wholeDivideHundred")?100:10;boolean zero=false,maximum=false;
   for(Question q:pool.values()){
    String[] parts=q.prompt.split(" ÷ ");BigDecimal x=new BigDecimal(parts[0]),d=new BigDecimal(parts[1]),answer=x.divide(d);assertEquals(divisor,d.intValueExact());assertEquals(tenth,parts[0].contains("."));zero|=x.signum()==0;maximum|=x.compareTo(new BigDecimal(grade==3?"9":tenth?"9.9":"99"))==0;assertTrue(new Checker().check(q,List.of(),List.of(answer.toPlainString())).correct());assertFalse(new Checker().check(q,List.of(),List.of(answer.add(unit).toPlainString())).correct());
    Question saved=new Question(q.skillId,q.prompt,"poison","999");HelpPlan help=HelpPlan.forQuestion(saved);assertEquals(6,help.size());assertFalse(help.canTransfer());List<BigDecimal> values=List.of(x,d,unit,unit.divide(d),x.divide(unit),answer);for(int i=0;i<6;i++){assertTrue(help.step(i).accepts(values.get(i).toPlainString()));assertFalse(help.step(i).accepts(values.get(i).add(BigDecimal.ONE).toPlainString()));assertEquals("",help.step(i).after);assertFalse(help.step(i).before.matches(".*[0-9].*"));}
   }assertTrue(zero&&maximum);List<String> recent=new ArrayList<>();Generator generator=new Generator(new Random(79+grade));for(int i=0;i<100;i++){Question q=generator.next(skill.id,recent,false,limits);assertEquals(i>=expected,recent.contains(q.signature()));recent.add(q.signature());}Question again=generator.next(skill.id,recent,false,limits);assertTrue(recent.contains(again.signature()));
  }
 }
 @Test public void unsupportedDivisorsAndInputPrecisionAreRejectedAndOldGeneralDivisionIsPreserved(){
  for(String prompt:List.of("2 ÷ 20","1.2 ÷ 100","1.23 ÷ 10","100 ÷ 10")){Question q=new Question("tenthsDivideTen",prompt,"","0");PowerTenDivision.attach(q);assertNull(q.studyGuide);}
  Generator g=new Generator(new Random(790));for(int i=0;i<100;i++){Question q=g.next("decimalDivInt",List.of(),false);String[] values=q.prompt.split(" / ");int divisor=new BigDecimal(values[1]).intValueExact();assertTrue(divisor>=2&&divisor<=9);assertTrue(new Checker().check(q,List.of(),List.of(new BigDecimal(values[0]).divide(new BigDecimal(values[1])).toPlainString())).correct());}
 }
}
