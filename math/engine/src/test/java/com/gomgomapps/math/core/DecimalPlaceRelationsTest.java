package com.gomgomapps.math.core;
import java.math.BigDecimal;import java.util.*;import org.junit.Test;import static org.junit.Assert.*;
public class DecimalPlaceRelationsTest {
 @Test public void everyDigitAndValueIsDerivedFromVisibleDecimalAndBlankHelp(){
  var pool=DecimalPlaceRelations.candidates(new CurriculumLimits("wholeMaximum=9;decimalPlaces=2"));assertEquals(3000,pool.size());
  for(Question q:pool.values()){
   String raw=q.prompt.substring(0,4);int position=q.prompt.contains("일의 자리")?0:q.prompt.contains("첫째")?1:2;int digit=raw.charAt(position==0?0:position+1)-'0';BigDecimal unit=BigDecimal.ONE.movePointLeft(position),value=unit.multiply(BigDecimal.valueOf(digit));
   Checker checker=new Checker();assertTrue(checker.check(q,List.of(),List.of(""+digit,value.toPlainString())).correct());assertEquals(1,checker.check(q,List.of(),List.of(""+digit,value.add(BigDecimal.ONE).toPlainString())).index);assertEquals(0,checker.check(q,List.of(),List.of(""+(digit+1),value.toPlainString())).index);
   Question saved=new Question(q.skillId,q.prompt,"poison","999","999");HelpPlan help=HelpPlan.forQuestion(saved);assertEquals(4,help.size());assertFalse(help.canTransfer());List<String> inputs=List.of(raw,""+digit,unit.toPlainString(),value.toPlainString());for(int i=0;i<4;i++){assertTrue(help.step(i).accepts(inputs.get(i)));assertFalse(help.step(i).accepts("99"));assertEquals("",help.step(i).after);assertFalse(help.step(i).before.matches(".*[0-9].*"));}
  }
 }
 @Test public void gradePlacementAndHundredFreshQuestionsKeepUnreviewedFallbackExcluded(){
  var pack=GlobalCurriculum.packs("GB").stream().filter(p->p.id.equals("england-primary-2021-v1")).findFirst().orElseThrow();assertTrue(pack.inGrade(DecimalPlaceRelations.ID,4));assertFalse(pack.inGrade(DecimalPlaceRelations.ID,3));assertTrue(pack.inGrade(DecimalPlaceRelations.ID,5));assertFalse(pack.inGrade(DecimalPlaceRelations.ID,6));assertEquals(2,GlobalCurriculum.limits(pack.id,DecimalPlaceRelations.ID,4).decimalPlaces(0));assertEquals(3,GlobalCurriculum.limits(pack.id,DecimalPlaceRelations.ID,5).decimalPlaces(0));
  Generator g=new Generator(new Random(80));List<String> recent=new ArrayList<>();var limits=GlobalCurriculum.limits(pack.id,DecimalPlaceRelations.ID,4);for(int i=0;i<100;i++){Question q=g.next(DecimalPlaceRelations.ID,recent,false,limits);assertFalse(recent.contains(q.signature()));recent.add(q.signature());}
 }
 @Test public void unsupportedPromptsDoNotAcquireHelpAndLegacyDigitOnlyQuestionsRemain(){
  for(String prompt:List.of("10.00의 일의 자리 숫자와 그 숫자가 나타내는 값을 쓰세요.","4.5의 소수 첫째 자리 숫자와 그 숫자가 나타내는 값을 쓰세요.")){Question q=new Question(DecimalPlaceRelations.ID,prompt,"","0","0");DecimalPlaceRelations.attach(q);assertNull(q.studyGuide);}
  Question q=new Generator(new Random(81)).next("el_decimal_place",List.of(),false);assertEquals(1,q.answers.length);assertTrue(q.prompt.contains("자리 숫자는?"));
 }
}
