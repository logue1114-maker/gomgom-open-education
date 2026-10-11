package com.gomgomapps.math.core;
import java.util.*;import org.junit.Test;import static org.junit.Assert.*;
public class NamibiaCompensationTest {
 private static final String NA="na-nied-primary-2024-v1";
 private int nearest(int b,int unit){int lower=b/unit*unit,upper=lower+unit;return b-lower<upper-b?lower:upper;}
 @Test public void exhaustiveSmallPublicDomainsIncludeEveryPermittedPairAndUnit(){
  for(String id:List.of(ConvenientNumberMental.ADD,ConvenientNumberMental.SUB))for(int min:new int[]{0,3})for(int max:new int[]{8,14})for(int cap:new int[]{0,5,14,28}){
   Set<String> expected=new HashSet<>(),actual=new HashSet<>();
   for(int a=min;a<=max;a++)for(int b=min;b<=max;b++)for(int unit:new int[]{10,100,1000,10000,100000,1000000}){int gap=Math.abs(nearest(b,unit)-b),answer=id.equals(ConvenientNumberMental.ADD)?a+b:a-b;if(gap>=1&&gap<=9&&answer>=0&&answer<=cap)expected.add(ConvenientNumberMental.make(id,a,b,unit).signature());}
   long count=ConvenientNumberMental.boundedCount(id,min,max,cap);assertEquals(expected.size(),count);
   for(long i=0;i<count;i++)assertTrue(actual.add(ConvenientNumberMental.bounded(id,min,max,cap,i).signature()));assertEquals(expected,actual);
  }
 }
 private void verify(Question q){String[] lines=q.prompt.split("\n"),parts=lines[0].split(" ");int a=Integer.parseInt(parts[0]),b=Integer.parseInt(parts[2]),unit=Integer.parseInt(lines[1].substring(ConvenientNumberMental.UNIT.length())),round=nearest(b,unit),gap=Math.abs(round-b);boolean add=parts[1].equals("+");int answer=add?a+b:a-b;int[] values={a,round,gap,add?a+round:a+round-b,answer};
  q.answers=new String[]{"poison"};q.expression="poison";assertTrue(new Checker().check(q,List.of(),List.of(""+answer)).correct());assertFalse(new Checker().check(q,List.of(),List.of(""+(answer+1))).correct());HelpPlan help=HelpPlan.forQuestion(q);assertEquals(5,help.size());assertFalse(help.canTransfer());for(int i=0;i<5;i++)assertTrue(help.step(i).accepts(""+values[i]));assertTrue(help.restore(null,q.id).entries.stream().allMatch(String::isEmpty));
 }
 @Test public void normalGradeSixSupplyRetainsRangeAndBothAdjustments(){
  var pack=GlobalCurriculum.packs("NA").stream().filter(p->p.id.equals(NA)).findFirst().orElseThrow();Generator generator=new Generator(new Random(244));
  for(String id:List.of(ConvenientNumberMental.ADD,ConvenientNumberMental.SUB)){assertTrue(pack.inGrade(id,6));CurriculumLimits limits=GlobalCurriculum.limits(NA,id,6);List<String> recent=new ArrayList<>();Set<Boolean> directions=new HashSet<>();
   for(int i=0;i<300;i++){Question q=generator.next(id,recent,false,limits);assertFalse(recent.contains(q.signature()));recent.add(q.signature());int[] v=ConvenientNumberMental.read(q);assertTrue(v[0]<=1000000&&v[1]<=1000000&&v[5]>=0&&v[5]<=1000000);directions.add(v[3]>v[1]);verify(q);}assertEquals(Set.of(true,false),directions);
  }
 }
 @Test public void smallZeroFirstAndMillionBoundaryAreNotArtificiallyFourDigitOnly(){
  for(int[] v:new int[][]{{0,1,10},{47,39,10},{73,38,10},{1000000,999999,1000000},{999999,1,10}})for(String id:List.of(ConvenientNumberMental.ADD,ConvenientNumberMental.SUB)){
   if(id.equals(ConvenientNumberMental.SUB)&&v[0]<v[1])continue;Question q=ConvenientNumberMental.make(id,v[0],v[1],v[2]);int answer=id.equals(ConvenientNumberMental.ADD)?v[0]+v[1]:v[0]-v[1];assertEquals(answer<=1000000,GlobalCurriculum.limits(NA,id,6).allows(q));verify(q);
  }
  Generator legacy=new Generator(new Random(244));for(String id:List.of(ConvenientNumberMental.ADD,ConvenientNumberMental.SUB)){CurriculumLimits limits=GlobalCurriculum.limits("england-primary-2021-v1",id,5);for(int i=0;i<100;i++){int[] v=ConvenientNumberMental.read(legacy.next(id,List.of(),false,limits));assertTrue(v[0]>=1000&&v[0]<=999999&&v[1]>=1000&&v[1]<=999999);}}
 }
}
