package com.gomgomapps.math.core;
import java.util.*;import org.junit.Test;import static org.junit.Assert.*;
public class WholeApproximationContextsTest {
 private final Checker checker=new Checker();
 private void correct(Question q,int value){q.answers=new String[]{"poison"};q.expression="poison";assertTrue(checker.check(q,List.of(),List.of(""+value)).correct());assertTrue(checker.check(q,List.of(),List.of(value+".0")).correct());assertFalse(checker.check(q,List.of(),List.of(""+(value+1))).correct());assertEquals(Checker.Status.INPUT_NEEDED,checker.check(q,List.of(),List.of("")).status);assertFalse(q.studyGuide.transfer);}
 @Test public void publicOrderAndHalfUpBoundaries(){
  correct(WholeApproximationContexts.make("sumThenRound",149,149,100),300);correct(WholeApproximationContexts.make("roundThenSum",149,149,100),200);
  correct(WholeApproximationContexts.make("differenceThenRound",151,149,100),0);correct(WholeApproximationContexts.make("roundThenDifference",151,149,100),100);
  correct(WholeApproximationContexts.make("roundThenSum",50000,950000,100000),1100000);
  for(int[]v:new int[][]{{0,0},{49,0},{50,100},{149,100},{150,200},{999999,1000000},{1000000,1000000}})correct(WholeApproximationContexts.make("single",v[0],0,100),v[1]);
  Question q=WholeApproximationContexts.make("sumThenRound",149,149,100);assertTrue(q.studyGuide.frames.stream().anyMatch(f->f.before.equals("149 + 149 = ")));assertTrue(q.studyGuide.frames.stream().anyMatch(f->f.before.equals("어림할 수 = ")&&f.expected.equals("298")));
 }
 @Test public void malformedPublicQuantitiesAndMethodsReject(){
  for(String prompt:List.of("도서관 자료 보고\n책 수: 1000001권\n보고 방법: 수량을 반올림\n보고 단위: 100권\n보고할 근삿값은?","행사 참가자 보고\n첫날: 1000000명 · 둘째 날: 1명\n보고 방법: 정확한 합을 구한 뒤 반올림\n보고 단위: 100명\n보고할 근삿값은?"))assertNull(WholeApproximationContexts.read(new Question(WholeApproximationContexts.ID,prompt,"","0")));
  CurriculumLimits only10=new CurriculumLimits("wholeMaximum=1000;roundingUnits=10");assertFalse(only10.allows(WholeApproximationContexts.make("single",151,0,100)));assertTrue(only10.allows(WholeApproximationContexts.make("single",151,0,10)));
  Question q=WholeApproximationContexts.make("single",151,0,100);q.prompt=q.prompt.replace("반올림","올림");assertNull(WholeApproximationContexts.read(q));assertEquals(Checker.Status.INPUT_NEEDED,checker.check(q,List.of(),List.of("200")).status);
  for(String method:List.of("single","sumThenRound","differenceThenRound","roundThenSum","roundThenDifference")){try{WholeApproximationContexts.make(method,1000001,0,100);fail();}catch(IllegalArgumentException expected){}}
 }
 @Test public void normalGradeSupplyAndSmallPoolRepeat(){
  String pack="na-nied-primary-2024-v1";Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"NA");GlobalCurriculum.choosePack(p,pack);assertTrue(GlobalCurriculum.pack(p).inGrade(WholeApproximationContexts.ID,7));assertFalse(GlobalCurriculum.pack(p).inGrade(WholeApproximationContexts.ID,6));
  CurriculumLimits limits=GlobalCurriculum.limits(pack,WholeApproximationContexts.ID,7);assertTrue(limits.allows(WholeApproximationContexts.make("roundThenSum",50000,950000,100000)));Generator generator=new Generator(new Random(334));List<String>recent=new ArrayList<>();Set<String>methods=new HashSet<>();
  for(int i=0;i<200;i++){Question q=generator.next(WholeApproximationContexts.ID,recent,false,limits);assertTrue(limits.allows(q));assertFalse(recent.contains(q.signature()));recent.add(q.signature());var v=WholeApproximationContexts.read(q);assertNotNull(v);methods.add(v.method());correct(q,v.approximate());}
  assertEquals(5,methods.size());CurriculumLimits small=new CurriculumLimits("wholeMaximum=1;minGiven=0;maxGiven=100;maxResult=100;roundingUnits=10;nonnegative=true");recent.clear();for(int i=0;i<40;i++){Question q=generator.next(WholeApproximationContexts.ID,recent,false,small);assertNotNull(q);recent.add(q.signature());}
 }
 @Test public void completeIndexedConditionsBeforeOldestRepeat(){
  for(int max:new int[]{1,2,3}){
   CurriculumLimits limits=new CurriculumLimits("wholeMaximum="+max+";minGiven=0;maxGiven=100;maxResult=100;roundingUnits=10;nonnegative=true");Set<String> independent=new HashSet<>();
   for(int a=0;a<=max;a++){independent.add(WholeApproximationContexts.make("single",a,0,10).signature());for(int b=0;b<=max;b++){
    if(a+b<=max)for(String method:List.of("sumThenRound","roundThenSum"))independent.add(WholeApproximationContexts.make(method,a,b,10).signature());
    if(b<=a)for(String method:List.of("differenceThenRound","roundThenDifference"))independent.add(WholeApproximationContexts.make(method,a,b,10).signature());
   }}
   assertEquals((max+1)+2*(max+1)*(max+2),independent.size());assertEquals(independent.size(),WholeApproximationContexts.conditionCount(max,1));Set<String> indexed=new HashSet<>();for(long i=0;i<WholeApproximationContexts.conditionCount(max,1);i++)assertTrue(indexed.add(WholeApproximationContexts.at(i,max,List.of(10)).signature()));assertEquals(independent,indexed);
   Random pinned=new Random(){@Override public int nextInt(int bound){return 0;}@Override public long nextLong(){return 0;}};Map<String,Integer>recent=new LinkedHashMap<>();String oldest=null;
   while(recent.size()<independent.size()){Question q=WholeApproximationContexts.next(pinned,limits,recent);assertTrue(limits.allows(q));assertTrue(independent.contains(q.signature()));assertFalse(recent.containsKey(q.signature()));if(oldest==null)oldest=q.signature();recent.put(q.signature(),recent.size());}
   assertEquals(oldest,WholeApproximationContexts.next(pinned,limits,recent).signature());recent.put(oldest,recent.size());assertEquals(1,(int)recent.get(WholeApproximationContexts.next(pinned,limits,recent).signature()));
  }
  assertEquals(5,WholeApproximationContexts.conditionCount(0,1));Set<String> zero=new HashSet<>();for(int i=0;i<5;i++)zero.add(WholeApproximationContexts.at(i,0,List.of(10)).signature());assertEquals(5,zero.size());
  long total=WholeApproximationContexts.conditionCount(1000000,5);assertTrue(total>Integer.MAX_VALUE);assertNotNull(WholeApproximationContexts.read(WholeApproximationContexts.at(total-1,1000000,List.of(10,100,1000,10000,100000))));
  for(long bad:new long[]{-1,total})try{WholeApproximationContexts.at(bad,1000000,List.of(10,100,1000,10000,100000));fail();}catch(IllegalArgumentException expected){}
 }
 @Test public void indexedFallbackPreservesAdditionalLimits(){
  CurriculumLimits limits=new CurriculumLimits("wholeMaximum=3;minGiven=0;maxGiven=2;maxResult=100;roundingUnits=10;nonnegative=true");Set<String> allowed=new HashSet<>();for(long i=0;i<WholeApproximationContexts.conditionCount(3,1);i++){Question q=WholeApproximationContexts.at(i,3,List.of(10));if(limits.allows(q))allowed.add(q.signature());}
  CurriculumLimits positive=new CurriculumLimits("wholeMaximum=3;minGiven=1;maxGiven=100;maxResult=100;roundingUnits=10;nonnegative=true");Set<String> positiveConditions=new HashSet<>();for(long i=0;i<WholeApproximationContexts.conditionCount(3,1);i++){Question q=WholeApproximationContexts.at(i,3,List.of(10));if(positive.allows(q))positiveConditions.add(q.signature());}assertFalse(positiveConditions.isEmpty());assertTrue(positiveConditions.size()<44);Map<String,Integer> seen=new LinkedHashMap<>();Random pinned=new Random(){@Override public int nextInt(int bound){return 0;}@Override public long nextLong(){return 0;}};while(seen.size()<positiveConditions.size()){Question q=WholeApproximationContexts.next(pinned,positive,seen);assertTrue(positiveConditions.contains(q.signature()));assertFalse(seen.containsKey(q.signature()));seen.put(q.signature(),seen.size());}assertEquals(0,(int)seen.get(WholeApproximationContexts.next(pinned,positive,seen).signature()));
  // maxGiven also checks the public unit10, so this domain has no permitted question.
  assertTrue(allowed.isEmpty());try{WholeApproximationContexts.next(new Random(335),limits,Map.of());fail();}catch(IllegalStateException expected){}
 }
}
