package com.gomgomapps.math.core;
import java.util.*;import org.junit.Test;import static org.junit.Assert.*;
public class GroupFractionsTest {
 @Test public void everyVisibleGroupChecksCountsNotStoredAnswer(){Checker c=new Checker();int seen=0;for(String id:List.of(GroupFractions.NAME,GroupFractions.WRITE))for(int part=0;part<4;part++)for(int m=1;m<=3;m++)for(int start=0;start<GroupFractions.PARTS[part][1]*m;start++)for(int shape=0;shape<2;shape++){
  Question q=GroupFractions.make(id,part,m,start,shape);int[] v=GroupFractions.read(q);assertNotNull(v);int colored=0;for(int i=0;i<v[0];i++)if(((int)q.diagram.values[1]&(1<<i))!=0)colored++;
  assertEquals(GroupFractions.PARTS[part][0]*m,colored);q.answers=new String[]{"9/10"};q.expression="poison";
  assertTrue(c.check(q,List.of(),List.of(colored+"/"+v[0])).correct());assertFalse(c.check(q,List.of(),List.of((colored+1)+"/"+v[0])).correct());assertFalse(c.check(q,List.of(),List.of("")).correct());assertFalse(c.check(q,List.of(),List.of("1/0")).correct());
  q.diagram.values[1]=(1<<v[0]);assertNull(GroupFractions.read(q));assertFalse(c.check(q,List.of(),List.of(colored+"/"+v[0])).correct());seen++;
 }assertEquals(312,seen);}
 @Test public void normalFreshSupplyAndReviewedGradeOnly(){Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"NA");GlobalCurriculum.choosePack(p,"na-nied-primary-2024-v1");for(String id:List.of(GroupFractions.NAME,GroupFractions.WRITE)){
  assertTrue(GlobalCurriculum.pack(p).inGrade(id,3));assertFalse(GlobalCurriculum.pack(p).inGrade(id,2));List<String> recent=new ArrayList<>();Generator g=new Generator(new Random(212));Set<String> fractions=new HashSet<>();for(int i=0;i<156;i++){Question q=g.next(id,recent,false,CurriculumLimits.NONE);assertNotNull(GroupFractions.read(q));assertFalse(recent.contains(q.signature()));recent.add(q.signature());fractions.add(q.answers[0]);}assertEquals(Set.of("1/2","1/4","3/4","1/3"),fractions);
 }GlobalCurriculum.chooseCountry(p,"AQ");assertFalse(GlobalCurriculum.available(p).stream().anyMatch(s->GroupFractions.supports(s.id)));}
}
