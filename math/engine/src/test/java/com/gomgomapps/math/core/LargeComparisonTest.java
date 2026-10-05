package com.gomgomapps.math.core;
import java.util.*;
import java.math.BigInteger;
import org.junit.Test;
import static org.junit.Assert.*;
public class LargeComparisonTest {
 @Test public void publicIntegersDetermineEveryRelationAndGuideCountWithoutIntOverflow(){
  Generator g=new Generator(new Random(2026100598));Set<String> relations=new HashSet<>(),unique=new HashSet<>();boolean differentLengths=false,commonPrefix=false;
  for(int i=0;i<1000;i++){
   Question q=g.next("largeCompareTrillion",List.of(),true,GlobalCurriculum.limits("jp-mext-primary-2017-v1","largeCompareTrillion",4));String[] parts=q.prompt.split("\n");assertEquals(3,parts.length);assertEquals("□",parts[1]);BigInteger a=new BigInteger(parts[0]),b=new BigInteger(parts[2]);String answer=a.compareTo(b)<0?"<":a.compareTo(b)>0?">":"=";assertEquals(answer,q.answers[0]);relations.add(answer);unique.add(q.signature());differentLengths|=parts[0].length()!=parts[2].length();commonPrefix|=!parts[0].equals(parts[2])&&parts[0].substring(0,4).equals(parts[2].substring(0,4));
   assertEquals("symbol",q.kind);HelpPlan h=HelpPlan.forQuestion(q);assertNotNull(h);assertEquals(3,h.size());assertFalse(h.canTransfer());assertTrue(h.step(0).accepts(String.valueOf(parts[0].length())));assertTrue(h.step(1).accepts(String.valueOf(parts[2].length())));assertTrue(h.step(2).accepts(answer));for(String other:List.of("<","=",">"))if(!other.equals(answer))assertFalse(h.step(2).accepts(other));
  }assertEquals(Set.of("<","=",">"),relations);assertTrue(differentLengths);assertTrue(commonPrefix);assertTrue(unique.size()>990);
 }
 @Test public void foreignPlacementIsNotACompletedUnitInEarlierDiagnosis(){
  Catalog.Skill skill=Catalog.get("largeCompareTrillion");assertFalse(Curriculum.inCurriculum(skill,2022));assertFalse(Curriculum.inCurriculum(skill,2015));Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"JP");GlobalCurriculum.choosePack(p,"jp-mext-primary-2017-v1");p.grade=4;assertTrue(GlobalCurriculum.pack(p).inGrade(skill.id,4));assertFalse(GlobalCurriculum.pack(p).inGrade(skill.id,3));assertFalse(Learning.diagnosticScope(p).contains(skill));p.learnedSkills.add(skill.id);assertTrue(Learning.diagnosticScope(p).contains(skill));
 }
}
