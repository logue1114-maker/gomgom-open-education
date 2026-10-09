package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;import java.math.BigDecimal;
import org.junit.Test;import static org.junit.Assert.*;
public class YearFourQuarterDecimalTest {
 private static final String ID="el_fraction_decimal",SYSTEM="england-primary-2021-v1";
 @Test public void actualSupplyExhausts120VisibleFractionsBeforeOldest(){
  CurriculumLimits limits=GlobalCurriculum.limits(SYSTEM,ID,4);Generator g=new Generator(new Random(152));List<String> history=new ArrayList<>();Set<String> fractions=new HashSet<>();boolean zero=false,whole=false;
  for(int i=0;i<120;i++){
   Question q=g.next(ID,history,false,limits);assertFalse(history.contains(q.signature()));history.add(q.signature());Matcher m=Pattern.compile("(\\d+)/(\\d+)의 값을 소수로 나타내세요\\.").matcher(q.prompt);assertTrue(m.matches());int n=Integer.parseInt(m.group(1)),d=Integer.parseInt(m.group(2));assertTrue(Set.of(2,4,10,100).contains(d));assertTrue(n>=0&&n<=d);assertTrue(fractions.add(n+"/"+d));String result=BigDecimal.valueOf(n).divide(BigDecimal.valueOf(d)).toPlainString();if(!result.contains("."))result+=".0";
   assertEquals(Expression.number(result),Expression.number(q.answers[0]));assertTrue(new Checker().check(q,List.of(),List.of(result)).correct());assertFalse(new Checker().check(q,List.of(),List.of(new BigDecimal(result).add(BigDecimal.ONE).toPlainString())).correct());assertEquals(Checker.Status.INPUT_NEEDED,new Checker().check(q,List.of(),List.of(n+"/"+d)).status);
   HelpPlan h=HelpPlan.forQuestion(q);assertFalse(h.canTransfer());assertEquals(d==2||d==4?3:4,h.size());List<String> values=new ArrayList<>(List.of(""+n,""+d));if(d==10||d==100)values.add(BigDecimal.ONE.divide(BigDecimal.valueOf(d)).toPlainString());values.add(result);
   for(int j=0;j<h.size();j++){assertTrue(h.step(j).accepts(values.get(j)));assertFalse(h.step(j).accepts(""));assertFalse(h.step(j).before.contains(" = "+result));}zero|=n==0;whole|=n==d;
  }
  assertTrue(fractions.containsAll(List.of("1/4","1/2","3/4")));assertTrue(zero&&whole);assertEquals(history.get(0),g.next(ID,history,false,limits).signature());
 }
 @Test public void quarterAndHalfDivisionFramesIgnoreHiddenKeys(){
  for(int[] f:new int[][]{{1,4},{1,2},{3,4}}){Question q=ElementaryBasics.fractionDecimal(Catalog.get(ID),f[0],f[1]);q.answers=new String[]{"999.0"};q.expression="999.0";HelpPlan h=HelpPlan.forQuestion(q);assertEquals(3,h.size());assertFalse(h.canTransfer());assertTrue(h.step(0).accepts(""+f[0]));assertTrue(h.step(1).accepts(""+f[1]));String expected=BigDecimal.valueOf(f[0]).divide(BigDecimal.valueOf(f[1])).toPlainString();assertTrue(h.step(2).accepts(expected));assertFalse(h.step(2).accepts(f[0]+"/"+f[1]));assertEquals("소수 v = n ÷ d = ",h.step(2).before);}
 }
 @Test public void earlierGradeAndOtherCountriesKeepTheirConversionDomains(){
  CurriculumLimits earlier=GlobalCurriculum.limits(SYSTEM,ID,3);assertFalse(earlier.variedFacts());assertArrayEquals(new int[]{10},earlier.fractionDenominators());assertFalse(GlobalCurriculum.limits("sg-moe-primary-2021-v1",ID,4).variedFacts());
 }
}
