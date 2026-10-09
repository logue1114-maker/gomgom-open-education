package com.gomgomapps.math.core;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class YearFourFractionQuantityTest {
 private static final String SYSTEM="england-primary-2021-v1",ID="el_fraction_of_number";
 @Test public void everySelectedQuantityHasWholeResultAndBlankRelations(){
  CurriculumLimits limits=GlobalCurriculum.limits(SYSTEM,ID,4);Set<String> signatures=new HashSet<>();
  for(int d=2;d<=12;d++)for(int n=1;n<d;n++)for(int part=2;part<=100;part++){
   Question q=QuantityFractionPictures.create(Catalog.get(ID),n,d,part,"number");assertTrue(limits.allows(q));assertTrue(signatures.add(q.signature()));
   int answer=0;for(int i=0;i<n;i++)answer+=part;
   q.answers=new String[]{"-999"};q.expression="-999";
   assertTrue(new Checker().check(q,List.of(),List.of(String.valueOf(answer))).correct());assertFalse(new Checker().check(q,List.of(),List.of(String.valueOf(answer+1))).correct());
   HelpPlan help=HelpPlan.forQuestion(q);assertEquals(5,help.size());assertFalse(help.canTransfer());int[] expected={d*part,n,d,part,answer};
   for(int i=0;i<5;i++){assertTrue(help.step(i).accepts(String.valueOf(expected[i])));assertFalse(help.step(i).accepts(String.valueOf(expected[i]+1)));assertFalse(help.step(i).before.matches(".*[0-9].*"));}
  }
  assertEquals(6534,signatures.size());
 }
 @Test public void actualSupplyUsesFreshConditionsAndOldestPublicHistory(){
  CurriculumLimits limits=GlobalCurriculum.limits(SYSTEM,ID,4);Generator generator=new Generator(new Random(150));List<String> history=new ArrayList<>();boolean larger=false,nonUnit=false;
  for(int i=0;i<300;i++){Question q=generator.next(ID,history,false,limits);assertFalse(history.contains(q.signature()));history.add(q.signature());int[] givens=QuantityFractionCheck.read(q);assertNotNull(givens);larger|=givens[3]>12;nonUnit|=givens[1]>1;assertTrue(givens[0]<=1200);}
  assertTrue(larger);assertTrue(nonUnit);history.clear();
  for(int d=2;d<=12;d++)for(int n=1;n<d;n++)for(int part=2;part<=100;part++)history.add(QuantityFractionPictures.create(Catalog.get(ID),n,d,part,"number").signature());
  assertEquals(history.get(0),generator.next(ID,history,false,limits).signature());
 }
 @Test public void earlierGradesAndUnselectedDefaultsStaySmall(){
  assertEquals(12,GlobalCurriculum.limits(SYSTEM,ID,3).quantityPartsMaximum());assertEquals(12,CurriculumLimits.NONE.quantityPartsMaximum());assertEquals(50,new CurriculumLimits("unitFractions=true").quantityPartsMaximum());
  for(String bad:List.of("quantityPartsMaximum=1","quantityPartsMaximum=101","quantityPartsMaximum=no")){try{new CurriculumLimits(bad);fail(bad);}catch(IllegalArgumentException expected){}}
 }
}
