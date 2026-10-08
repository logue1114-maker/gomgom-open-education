package com.gomgomapps.math.core;
import org.junit.Test;import static org.junit.Assert.*;import java.util.*;
public class PrimaryOrderingRangesTest {
 private static final String NA="na-nied-primary-2024-v1";
 private List<String> solve(Question q){
  double[] v=q.diagram.values;
  if(q.skillId.equals("numberOrder")){List<Integer> a=new ArrayList<>();for(int i=0;i<3;i++)a.add((int)v[i]);a.sort(v[3]==0?Comparator.naturalOrder():Comparator.reverseOrder());return a.stream().map(String::valueOf).toList();}
  int pos=0;for(int i=v[2]==0?0:(int)v[0]-1;v[2]==0?i<v[0]:i>=0;i+=v[2]==0?1:-1){pos++;if(i==(int)v[1])break;}return List.of(""+pos);
 }
 @Test public void compactDecoderMatchesOriginalEntireSmallDomain(){
  for(String id:List.of("numberOrder","objectOrdinal","numberCompareWords","objectCompareWords")){
   List<int[]> old=PrimaryOrdering.conditions(id,10);assertEquals(old.size(),PrimaryOrdering.conditionCount(id,10));for(int i=0;i<old.size();i++)assertArrayEquals(old.get(i),PrimaryOrdering.conditionAt(id,10,i));
  }
  assertEquals(1999800,PrimaryOrdering.conditionCount("numberOrder",100));assertEquals(249999000,PrimaryOrdering.conditionCount("numberOrder",500));
 }
 @Test public void actualGeneratorSuppliesHundredFreshIndependentConditionsAtEachNewPlacement(){
  Generator generator=new Generator(new Random(49));
  for(int grade:List.of(2,3))for(String id:List.of("numberOrder","objectOrdinal")){
   int maximum=id.equals("numberOrder")?(grade==2?100:500):(grade==2?20:30);Set<String> seen=new LinkedHashSet<>();CurriculumLimits limits=GlobalCurriculum.limits(NA,id,grade);boolean extended=false;
   for(int i=0;i<100;i++){
    Question q=generator.next(id,seen,false,limits);assertTrue(seen.add(q.signature()));assertTrue(limits.allows(q));List<String> answer=solve(q);assertTrue(new Checker().check(q,List.of(),answer).correct());
    for(int j=0;j<(id.equals("numberOrder")?3:1);j++){assertTrue(q.diagram.values[j]>=0&&q.diagram.values[j]<=maximum);extended|=q.diagram.values[j]>(id.equals("numberOrder")?20:10);}
    if(id.equals("numberOrder")){List<String> wrong=new ArrayList<>(answer);Collections.swap(wrong,1,2);assertEquals(1,new Checker().check(q,List.of(),wrong).index);}
    q.answers=new String[]{"999"};PrimaryOrdering.attach(q);assertFalse(HelpPlan.forQuestion(q).canTransfer());List<String> help=q.studyGuide.frames.stream().map(f->f.expected).toList();assertEquals(answer,id.equals("numberOrder")?help:List.of(help.get(1)));
   }
   assertTrue(extended);
  }
 }
 @Test public void fullOrdinalSupplyAndSortEndpointsUsePublishedGeometry(){
  for(int max:List.of(20,30)){
   Set<String> seen=new HashSet<>();for(int i=0;i<PrimaryOrdering.conditionCount("objectOrdinal",max);i++){Question q=PrimaryOrdering.make("objectOrdinal",PrimaryOrdering.conditionAt("objectOrdinal",max,i));assertTrue(seen.add(q.signature()));assertTrue(new Checker().check(q,List.of(),solve(q)).correct());}
   assertEquals(max*(max+1),seen.size());
  }
  for(int max:List.of(100,500))for(int reverse:List.of(0,1)){
   Question q=PrimaryOrdering.make("numberOrder",new int[]{max,0,max-1,reverse});assertTrue(GlobalCurriculum.limits(NA,"numberOrder",max==100?2:3).allows(q));assertTrue(new Checker().check(q,List.of(),solve(q)).correct());
  }
 }
 @Test public void diagnosisUsesPreviousGradeAndRetainsFirstGradeBounds(){
  Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"NA");GlobalCurriculum.choosePack(p,NA);p.grade=2;Question above=PrimaryOrdering.make("numberOrder",new int[]{100,0,99,0});assertFalse(GlobalCurriculum.limits(NA,"numberOrder",1).allows(above));assertTrue(GlobalCurriculum.scope(p).stream().anyMatch(s->s.id.equals("numberOrder")));p.grade=3;assertTrue(GlobalCurriculum.limits(NA,"numberOrder",2).allows(above));assertFalse(GlobalCurriculum.limits(NA,"numberOrder",2).allows(PrimaryOrdering.make("numberOrder",new int[]{500,0,499,0})));
 }
}
