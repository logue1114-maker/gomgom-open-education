package com.gomgomapps.math.core;
import java.util.*;import org.junit.Test;import static org.junit.Assert.*;
public class FractionComparisonSupplyTest {
 @Test public void publicComparisonAndNoTransferForAllRelations(){for(String id:List.of("fracCompare","el_fraction_compare"))for(int[]v:List.of(new int[]{1,2,1,2},new int[]{1,2,1,3},new int[]{2,4,1,2},new int[]{2,3,1,3})){if(id.equals("fracCompare")!=(v[1]==v[3]))continue;Question q=FractionComparisonSupply.make(id,v);String sign=q.answers[0];q.answers=new String[]{"poison"};q.expression="poison";for(String candidate:List.of("<","=",">"))assertEquals(candidate.equals(sign),new Checker().check(q,List.of(),List.of(candidate)).correct());HelpPlan h=HelpPlan.forQuestion(q);assertEquals(id.equals("fracCompare")?4:8,h.size());assertFalse(h.canTransfer());assertTrue(h.restore(null,q.id).entries.stream().allMatch(String::isEmpty));}}
 @Test public void fullSmallDomainBeforeOldest(){CurriculumLimits l=new CurriculumLimits("variedFacts=true;denominators=2,3;maxGiven=3;nonnegative=true");for(String id:List.of("fracCompare","el_fraction_compare")){Map<String,Integer>recent=new LinkedHashMap<>();int size=id.equals("fracCompare")?5:4;Random r=new Random(273);for(int i=0;i<size;i++){Question q=FractionComparisonSupply.next(id,r,l,recent);assertFalse(recent.containsKey(q.signature()));recent.put(q.signature(),i);}assertEquals(recent.keySet().iterator().next(),FractionComparisonSupply.next(id,r,l,recent).signature());}}
 @Test public void ordinaryNA6HasBothTopicsFreshSupplyAndAllSigns(){Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"NA");String pack="na-nied-primary-2024-v1";GlobalCurriculum.choosePack(p,pack);for(String id:List.of("fracCompare","el_fraction_compare")){assertTrue(GlobalCurriculum.pack(p).inGrade(id,6));CurriculumLimits l=GlobalCurriculum.limits(pack,id,6);assertTrue(FractionComparisonSupply.selected(id,l));Generator g=new Generator(new Random(273));List<String>recent=new ArrayList<>();Map<String,Integer>count=new HashMap<>();for(int i=0;i<100;i++){Question q=g.next(id,recent,false,l);assertFalse(recent.contains(q.signature()));recent.add(q.signature());count.merge(q.answers[0],1,Integer::sum);}for(String sign:List.of("<","=",">"))assertTrue(count.getOrDefault(sign,0)>=15);}assertFalse(FractionComparisonSupply.selected("fracCompare",CurriculumLimits.NONE));assertTrue(FractionComparisonSupply.selected("fracCompare",new CurriculumLimits("variedFacts=true;denominators=10,100")));}
 @Test public void indexedSignsMatchIndependentEnumerationAndBoundary(){
  CurriculumLimits small=new CurriculumLimits("variedFacts=true;denominators=2,3,4,5,6,7");
  for(String id:List.of("fracCompare","el_fraction_compare")){
   FractionComparisonSupply.Domain d=FractionComparisonSupply.domain(id,small);
   List<Set<String>>expected=List.of(new HashSet<>(),new HashSet<>(),new HashSet<>(),new HashSet<>());
   for(int b=2;b<=7;b++)for(int den=2;den<=7;den++)if(id.equals("fracCompare")==(b==den))for(int a=1;a<b;a++)for(int c=1;c<den;c++){
    String key=Arrays.toString(new int[]{a,b,c,den});expected.get(0).add(key);expected.get(Integer.compare(a*den,c*b)+2).add(key);
   }
   for(int sign=0;sign<4;sign++){Set<String>actual=new HashSet<>();for(long i=0;i<d.size(sign);i++)assertTrue(actual.add(Arrays.toString(d.at(sign,i))));assertEquals(expected.get(sign),actual);}
  }
  CurriculumLimits wide=new CurriculumLimits("variedFacts=true;denominators=2,100;maxGiven=100;nonnegative=true");
  for(String id:List.of("fracCompare","el_fraction_compare")){
   FractionComparisonSupply.Domain d=FractionComparisonSupply.domain(id,wide);assertEquals(2,d.left.length);
   for(int sign=0;sign<4;sign++)if(d.size(sign)>0)for(long i:new long[]{0,d.size(sign)-1}){int[]v=d.at(sign,i);Question q=FractionComparisonSupply.make(id,v);assertTrue(wide.allows(q));if(sign>0)assertEquals(sign-2,Integer.compare(v[0]*v[3],v[2]*v[1]));}
  }
 }
 @Test public void denominator100DomainStaysCompressed(){
  String den=java.util.stream.IntStream.rangeClosed(2,100).mapToObj(Integer::toString).collect(java.util.stream.Collectors.joining(","));
  CurriculumLimits l=new CurriculumLimits("variedFacts=true;denominators="+den+";maxGiven=100;nonnegative=true");
  for(String id:List.of("fracCompare","el_fraction_compare")){
   FractionComparisonSupply.Domain d=FractionComparisonSupply.domain(id,l);
   assertEquals(id.equals("fracCompare")?99:9702,d.left.length);
   assertEquals(id.equals("fracCompare")?328350L:24174150L,d.size(0));
   assertEquals(d.size(0),d.size(1)+d.size(2)+d.size(3));
   Random r=new Random(275);Map<String,Integer>recent=new LinkedHashMap<>();boolean above12=false;
   for(int i=0;i<100;i++){Question q=FractionComparisonSupply.next(id,r,l,recent);assertTrue(l.allows(q));assertFalse(recent.containsKey(q.signature()));recent.put(q.signature(),i);above12|=java.util.Arrays.stream(q.prompt.split("[^0-9]+" )).filter(x->!x.isEmpty()).mapToInt(Integer::parseInt).anyMatch(x->x>12);}
   assertTrue(above12);
  }
 }

}

