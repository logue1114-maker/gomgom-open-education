package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;import org.junit.Test;import static org.junit.Assert.*;
public class EnglandNumbersHundredTest {
 private static final String PACK="england-primary-2021-v1";
 @Test public void bothGradeCollectionDomainsIncludeZeroAndHundredAndPreserveGrouping(){
  for(int grade:List.of(1,2)){
   Generator g=new Generator(new Random(61));List<String> recent=new ArrayList<>();Set<Integer> values=new HashSet<>();CurriculumLimits limits=GlobalCurriculum.limits(PACK,"collectionCount",grade);
   for(int i=0;i<401;i++){
    Question q=g.next("collectionCount",recent,false,limits);assertFalse(recent.contains(q.signature()));recent.add(q.signature());int n=CollectionGrouping.points(q,0).length;assertTrue(n<=100);values.add(n);assertTrue(new Checker().check(q,List.of(),List.of(""+n)).correct());assertFalse(new Checker().check(q,List.of(),List.of(""+(n+1))).correct());
    for(int size:List.of(2,5,10)){CollectionGrouping.group(q,size);assertEquals(n,CollectionGrouping.points(q,size).length);assertEquals(recent.get(i),q.signature());}assertFalse(HelpPlan.forQuestion(q).canTransfer());
   }
   assertEquals(101,values.size());assertTrue(values.contains(0)&&values.contains(100));assertEquals(recent.get(0),g.next("collectionCount",recent,false,limits).signature());
  }
 }
 @Test public void comparisonAndOrderingUsePublicValuesThroughHundred(){
  Generator g=new Generator(new Random(62));Set<String> seen=new LinkedHashSet<>();Set<String> signs=new HashSet<>();boolean zero=false,hundred=false;
  for(int i=0;i<600;i++){
   Question q=g.next("el_compare_10000",seen,false,GlobalCurriculum.limits(PACK,"el_compare_10000",2));assertTrue(seen.add(q.signature()));Matcher m=Pattern.compile("^(\\d+)\\s+□\\s+(\\d+)$").matcher(q.prompt);assertTrue(m.matches());int a=Integer.parseInt(m.group(1)),b=Integer.parseInt(m.group(2));assertTrue(a<=100&&b<=100);zero|=a==0||b==0;hundred|=a==100||b==100;String sign=a<b?"<":a>b?">":"=";signs.add(sign);assertTrue(new Checker().check(q,List.of(),List.of(sign)).correct());assertFalse(HelpPlan.forQuestion(q).canTransfer());
  }
  assertTrue(zero&&hundred);assertEquals(Set.of("<",">","="),signs);seen.clear();zero=false;hundred=false;
  for(int i=0;i<600;i++){
   Question q=g.next("numberOrder",seen,false,GlobalCurriculum.limits(PACK,"numberOrder",2));assertTrue(seen.add(q.signature()));int[] values=Arrays.stream(q.diagram.values).limit(3).mapToInt(v->(int)v).toArray();for(int n:values){assertTrue(n>=0&&n<=100);zero|=n==0;hundred|=n==100;}Arrays.sort(values);List<String> answer=q.diagram.values[3]==0?List.of(""+values[0],""+values[1],""+values[2]):List.of(""+values[2],""+values[1],""+values[0]);assertTrue(new Checker().check(q,List.of(),answer).correct());assertFalse(HelpPlan.forQuestion(q).canTransfer());
  }
  assertTrue(zero&&hundred);
 }
 @Test public void unlearnedCurrentGradeAndOtherCountryBoundsStaySeparate(){
  Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"GB");GlobalCurriculum.choosePack(p,PACK);p.grade=1;assertFalse(GlobalCurriculum.scope(p).stream().anyMatch(s->s.id.equals("collectionCount")));p.grade=2;assertTrue(GlobalCurriculum.scope(p).stream().anyMatch(s->s.id.equals("collectionCount")));assertFalse(GlobalCurriculum.scope(p).stream().anyMatch(s->Set.of("numberOrder","el_compare_10000").contains(s.id)));
  assertEquals(50,GlobalCurriculum.limits("na-nied-primary-2024-v1","collectionCount",2).wholeMaximum(100));assertEquals(500,GlobalCurriculum.limits("na-nied-primary-2024-v1","numberOrder",3).wholeMaximum(100));
 }
}
