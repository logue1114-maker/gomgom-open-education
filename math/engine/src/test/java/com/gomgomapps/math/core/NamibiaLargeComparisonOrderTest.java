package com.gomgomapps.math.core;
import org.junit.Test;import static org.junit.Assert.*;import java.util.*;
public class NamibiaLargeComparisonOrderTest {
 private static final String NA="na-nied-primary-2024-v1";
 @Test public void compareAllThreeRelationsFreshAndPublicHelp(){
  Generator generator=new Generator(new Random(255));List<String> recent=new ArrayList<>();Map<String,Integer> signs=new HashMap<>();var limits=GlobalCurriculum.limits(NA,"el_compare_10000",6);assertEquals(0,limits.givenMinimum(100));assertEquals(1000000,limits.wholeMaximum(-1));
  for(int i=0;i<150;i++){Question q=generator.next("el_compare_10000",recent,false,limits);assertFalse(recent.contains(q.signature()));recent.add(q.signature());String[] v=q.prompt.split("\\s+□\\s+");int a=Integer.parseInt(v[0]),b=Integer.parseInt(v[1]);assertTrue(a>=0&&b>=0&&a<=1000000&&b<=1000000);String sign=a<b?"<":a>b?">":"=";signs.merge(sign,1,Integer::sum);q.answers=new String[]{"poison"};assertTrue(new Checker().check(q,List.of(),List.of(sign)).correct());HelpPlan h=HelpPlan.forQuestion(q);assertFalse(h.canTransfer());assertTrue(h.step(0).accepts(v[0]));assertTrue(h.step(1).accepts(v[1]));assertTrue(h.step(h.size()-1).accepts(sign));}
  assertEquals(Set.of("<",">","="),signs.keySet());for(int count:signs.values())assertTrue(count>=25);
 }
 @Test public void zeroMillionAndEqualBoundariesAreAdmitted(){
  var limits=GlobalCurriculum.limits(NA,"el_compare_10000",6);for(int a:List.of(0,1,999999,1000000))for(int b:List.of(0,1,999999,1000000)){Question q=new Question("el_compare_10000",a+"  □  "+b,""+a+"-"+b,a<b?"<":a>b?">":"=");assertTrue(limits.allows(q));WholeCompareRelations.attach(q);assertFalse(HelpPlan.forQuestion(q).canTransfer());}
 }
 @Test public void millionOrderingBothDirectionsFreshAndStudentAnswers(){
  Generator generator=new Generator(new Random(256));var limits=GlobalCurriculum.limits(NA,"numberOrder",6);List<String> recent=new ArrayList<>();Set<Integer> directions=new HashSet<>();
  for(int i=0;i<150;i++){Question q=generator.next("numberOrder",recent,false,limits);assertFalse(recent.contains(q.signature()));recent.add(q.signature());double[] v=q.diagram.values;directions.add((int)v[3]);List<Integer> sorted=new ArrayList<>();for(int j=0;j<3;j++){assertTrue(v[j]>=0&&v[j]<=1000000);sorted.add((int)v[j]);}sorted.sort(v[3]==0?Comparator.naturalOrder():Comparator.reverseOrder());List<String> answer=sorted.stream().map(String::valueOf).toList();q.answers=new String[]{"poison"};assertTrue(new Checker().check(q,List.of(),answer).correct());HelpPlan h=HelpPlan.forQuestion(q);assertFalse(h.canTransfer());assertEquals(3,h.size());for(int j=0;j<3;j++)assertTrue(h.step(j).accepts(answer.get(j)));}
  assertEquals(Set.of(0,1),directions);for(int direction:List.of(0,1)){Question q=PrimaryOrdering.make("numberOrder",new int[]{1000000,0,999999,direction});assertTrue(limits.allows(q));assertFalse(GlobalCurriculum.limits(NA,"numberOrder",3).allows(q));}
 }
}
