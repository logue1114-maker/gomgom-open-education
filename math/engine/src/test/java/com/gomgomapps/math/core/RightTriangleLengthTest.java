package com.gomgomapps.math.core;
import org.junit.Test;import static org.junit.Assert.*;import java.util.*;import java.math.*;import java.util.regex.*;
public class RightTriangleLengthTest {
 static BigDecimal given(String p,String symbol){Matcher m=Pattern.compile(symbol+" = ([0-9]+(?:\\.[0-9]+)?)").matcher(p);assertTrue(m.find());return new BigDecimal(m.group(1));}
 static BigDecimal squaredAnswer(String p){BigDecimal ac=given(p,"AC"),other=given(p,p.contains("AB의 길이는")?"BC":"AB");return p.contains("AB의 길이는")?other.multiply(other).subtract(ac.multiply(ac)):other.multiply(other).add(ac.multiply(ac));}
 static BigDecimal answer(String p){return squaredAnswer(p).sqrt(new MathContext(40));}
 @Test public void hundredDistinctPublishedProblemsHaveVariableExactLengthsAndNoAnswerTransfer(){
  Generator g=new Generator(new Random(367));Set<String> seen=new HashSet<>(),answers=new HashSet<>();Set<Integer> ranks=new HashSet<>();boolean leg=false,hyp=false,decimal=false;
  for(int i=0;i<100;i++){
   Question q=g.next("pythagoras",seen,true);assertTrue(q.prompt,seen.add(q.signature()));BigDecimal result=answer(q.prompt);String value=result.stripTrailingZeros().toPlainString();answers.add(value);leg|=q.prompt.contains("AB의 길이는");hyp|=q.prompt.contains("BC의 길이는");decimal|=q.prompt.matches("(?s).*\\d\\.\\d.*");
   assertTrue(new Checker().check(q,List.of(),List.of(value)).correct());assertFalse(new Checker().check(q,List.of(),List.of(result.add(BigDecimal.ONE).toPlainString())).correct());
   HelpPlan h=HelpPlan.forQuestion(q);assertEquals(2,h.size());assertFalse(h.canTransfer());assertTrue(h.step(0).accepts(squaredAnswer(q.prompt).toPlainString()));assertTrue(h.step(1).accepts(value));
   assertEquals(3,q.diagram.values.length);assertEquals("rightTriangleLength",q.diagram.type);assertTrue(given(q.prompt,"AC").compareTo(BigDecimal.valueOf(50))<=0);assertTrue(result.compareTo(BigDecimal.valueOf(50))<=0);
   assertEquals(4,q.choices.size());assertEquals(1,q.choices.stream().filter(choice->Expression.number(choice).equals(Expression.number(value))).count());assertEquals(Expression.number(value),Expression.number(q.choices.get(q.correctChoice)));assertEquals(4,q.choices.stream().map(Expression::number).distinct().count());ranks.add(q.correctChoice);
  }
  assertTrue(leg&&hyp&&decimal);assertTrue(answers.size()>30);assertEquals(Set.of(0,1,2,3),ranks);
 }
 @Test public void actualTriangleIsRightAndMatchesEveryPublishedLength(){Random random=new Random(431);Set<String> all=new HashSet<>();for(int i=0;i<6000;i++){
  Question q=RightTriangleLength.create(Catalog.get("pythagoras"),random);all.add(q.prompt);double[][] v=RightTriangleLength.vertices(q.diagram);assertEquals(0,(v[1][0]-v[0][0])*(v[2][0]-v[0][0])+(v[1][1]-v[0][1])*(v[2][1]-v[0][1]),1e-9);assertTrue(v[1][0]>0&&v[2][1]>0);
  assertEquals(given(q.prompt,"AC").doubleValue(),Math.hypot(v[2][0],v[2][1]),1e-9);boolean leg=q.prompt.contains("AB의 길이는");assertEquals(leg?answer(q.prompt).doubleValue():given(q.prompt,"AB").doubleValue(),Math.hypot(v[1][0],v[1][1]),1e-9);assertEquals(leg?given(q.prompt,"BC").doubleValue():answer(q.prompt).doubleValue(),Math.hypot(v[2][0]-v[1][0],v[2][1]-v[1][1]),1e-9);
 }assertTrue("Actual distinct given-length forms: "+all.size(),all.size()>=100);}
 @Test public void selectedBrazilGradeNineIncludesExistingPythagorasTopic(){Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"BR");GlobalCurriculum.choosePack(p,"br-bncc-fundamental-2017-v1");assertTrue(GlobalCurriculum.pack(p).inGrade("pythagoras",9));}
}
