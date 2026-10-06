package com.gomgomapps.math.core;
import java.math.*;
import java.util.*;
import java.util.regex.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class SuccessivePercentTest {
 private static final String PACK="br-bncc-fundamental-2017-v1";
 @Test public void hundredFinalPricesAreDistinctAndUseTheUpdatedBase(){verify("successivePercentPrice",false);}
 @Test public void hundredOverallRatesAreDistinctAndKeepTheSign(){verify("successivePercentRate",true);}
 private void verify(String id,boolean net){
  Generator g=new Generator(new Random(net?20261006942L:20261006941L));Set<String> seen=new HashSet<>(),directions=new HashSet<>();Set<Integer> positions=new HashSet<>();boolean equalOpposite=false;
  for(int i=0;i<100;i++){
   Question q=g.next(id,seen,i%2==0,GlobalCurriculum.limits(PACK,id,9));assertTrue(seen.add(q.signature()));
   Matcher m=Pattern.compile("처음 가격: (\\d+)\\n첫째 변화: (\\d+)% (인상|인하)\\n둘째 변화: (\\d+)% (인상|인하)\\n.*",Pattern.DOTALL).matcher(q.prompt);assertTrue(q.prompt,m.matches());
   BigDecimal initial=new BigDecimal(m.group(1)),p=new BigDecimal(m.group(2)),r=new BigDecimal(m.group(4));int first=m.group(3).equals("인상")?1:-1,second=m.group(5).equals("인상")?1:-1;
   // Calculate each price change in full money amounts, rather than reuse generator multipliers.
   BigDecimal delta1=initial.multiply(p).divide(new BigDecimal(100)).multiply(BigDecimal.valueOf(first));BigDecimal middle=initial.add(delta1);
   BigDecimal delta2=middle.multiply(r).divide(new BigDecimal(100)).multiply(BigDecimal.valueOf(second)),end=middle.add(delta2),difference=end.subtract(initial),rate=difference.divide(initial).multiply(new BigDecimal(100));
   assertEquals(0,end.compareTo(new BigDecimal(q.answers[0])));if(net)assertEquals(0,rate.compareTo(new BigDecimal(q.answers[1])));
   List<String> correct=net?List.of(text(end),text(rate)):List.of(text(end));Checker c=new Checker();assertTrue(c.check(q,List.of(),correct).correct());assertFalse(c.check(q,List.of(),net?List.of(text(end),text(rate.add(BigDecimal.ONE))):List.of(text(end.add(BigDecimal.ONE)))).correct());
   BigDecimal wrong=initial.add(delta1).add(initial.multiply(r).divide(new BigDecimal(100)).multiply(BigDecimal.valueOf(second)));assertTrue(end.compareTo(wrong)!=0);
   String[] values=net?new String[]{text(BigDecimal.ONE.add(p.multiply(BigDecimal.valueOf(first)).movePointLeft(2))),text(middle),text(BigDecimal.ONE.add(r.multiply(BigDecimal.valueOf(second)).movePointLeft(2))),text(end),text(difference),text(rate)}:new String[]{text(BigDecimal.ONE.add(p.multiply(BigDecimal.valueOf(first)).movePointLeft(2))),text(middle),text(BigDecimal.ONE.add(r.multiply(BigDecimal.valueOf(second)).movePointLeft(2))),text(end)};
   assertEquals(values.length,q.studyGuide.frames.size());for(int k=0;k<values.length;k++)assertEquals(values[k],q.studyGuide.frames.get(k).expected);assertFalse(q.studyGuide.transfer);assertFalse(HelpPlan.forQuestion(q).canTransfer());
   if(!q.choices.isEmpty()){assertEquals(4,new HashSet<>(q.choices).size());assertEquals(0,new BigDecimal(q.answers[0]).compareTo(new BigDecimal(q.choices.get(q.correctChoice))));positions.add(q.correctChoice);}else if(net)assertEquals("pair",q.kind);
   directions.add(first+","+second);if(p.equals(r)&&first!=second){assertTrue(end.compareTo(initial)<0);equalOpposite=true;}
  }assertEquals(100,seen.size());assertEquals(4,directions.size());assertTrue(equalOpposite);if(!net)assertEquals(Set.of(0,1,2,3),positions);
 }
 private static String text(BigDecimal value){return value.stripTrailingZeros().toPlainString();}
 @Test public void equalTenPercentChangesDoNotCancel(){Generator g=new Generator(new Random(10));for(int i=0;i<10000;i++){Question q=g.next("successivePercentRate",List.of(),false);if(q.prompt.contains("첫째 변화: 10% 인상\n둘째 변화: 10% 인하")||q.prompt.contains("첫째 변화: 10% 인하\n둘째 변화: 10% 인상")){Matcher m=Pattern.compile("처음 가격: (\\d+)").matcher(q.prompt);assertTrue(m.find());BigDecimal price=new BigDecimal(m.group(1));assertEquals(0,price.multiply(new BigDecimal("0.99")).compareTo(new BigDecimal(q.answers[0])));assertEquals("-1",q.answers[1]);return;}}fail("Expected equal opposite ten-percent case");}
 @Test public void selectedNinthGradePricesAreNotInPriorGradeDiagnosis(){Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"BR");GlobalCurriculum.choosePack(p,PACK);p.grade=9;for(var s:SuccessivePercent.SKILLS){assertTrue(GlobalCurriculum.pack(p).inGrade(s.id,9));assertFalse(GlobalCurriculum.scope(p).stream().anyMatch(x->x.id.equals(s.id)));}}
}
