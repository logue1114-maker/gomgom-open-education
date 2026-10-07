package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;import org.junit.Test;import static org.junit.Assert.*;
public class ProportionalPairRelationsTest {
 @Test public void bothRelationsCollectPublicPairsBeforeConstantAndResult(){
  Generator g=new Generator(new Random(1517));boolean negative=false;
  for(String id:List.of("sec_direct_proportion","sec_inverse_proportion"))for(int i=0;i<500;i++){
   Question q=g.create(Catalog.get(id));List<Rational> values=new ArrayList<>();Matcher m=Pattern.compile("[xy]=(-?\\d+)").matcher(q.prompt);while(m.find())values.add(Expression.number(m.group(1)));assertEquals(3,values.size());Rational x=values.get(0),y=values.get(1),next=values.get(2),constant=id.equals("sec_direct_proportion")?y.div(x):x.mul(y),answer=id.equals("sec_direct_proportion")?constant.mul(next):constant.div(next);negative|=x.compareTo(Rational.ZERO)<0||y.compareTo(Rational.ZERO)<0||next.compareTo(Rational.ZERO)<0;
   assertTrue(new Checker().check(q,List.of(),List.of(answer.toString())).correct());verify(q,List.of(x,y,next,constant,answer));
  }assertTrue(negative);
 }
 @Test public void fractionalPairsAndNegativeValuesRemainExact(){
  verify(new Question("sec_direct_proportion","y는 x에 정비례하고 x=-3/2일 때 y=1입니다. x=9/4일 때 y는?","","-3/2"),List.of(Rational.of(-3,2),Rational.ONE,Rational.of(9,4),Rational.of(-2,3),Rational.of(-3,2)));
  verify(new Question("sec_inverse_proportion","y는 x에 반비례하고 x=-3/2일 때 y=2/3입니다. x=1/4일 때 y는?","","-4"),List.of(Rational.of(-3,2),Rational.of(2,3),Rational.of(1,4),Rational.of(-1),Rational.of(-4)));
 }
 @Test public void oldConstantsCannotPopulateNewFirstXAndNewReadingsRestore(){
  Question q=new Question("sec_direct_proportion","y는 x에 정비례하고 x=-2일 때 y=6입니다. x=5일 때 y는?","","-15");HelpPlan.Draft d=new HelpPlan.Draft();d.questionId=q.id;d.stage=1;d.entries=new ArrayList<>(List.of("-3",""));HelpPlan p=HelpPlan.forQuestion(q);p.restore(d,q.id);assertEquals(0,d.stage);assertEquals("",d.entries.get(0));d.entries.set(0,"-2");d.stage=1;d.entries.add("");HelpPlan.Draft restored=HelpPlan.forQuestion(q).restore(d.copy(),q.id);assertEquals(1,restored.stage);assertEquals("-2",restored.entries.get(0));assertEquals("",restored.entries.get(1));
 }
 private static void verify(Question q,List<Rational> expected){String signature=q.signature();Arrays.fill(q.answers,"999999");q.givenNumbers.put("constant","999999");HelpPlan p=HelpPlan.forQuestion(q);assertEquals(signature,q.signature());assertFalse(p.canTransfer());assertEquals(5,p.size());for(int k=0;k<5;k++){assertTrue(p.step(k).accepts(expected.get(k).toString()));assertFalse(p.step(k).accepts(expected.get(k).add(Rational.ONE).toString()));assertFalse(p.step(k).before.matches("(?s).*\\d.*"));}}
}
