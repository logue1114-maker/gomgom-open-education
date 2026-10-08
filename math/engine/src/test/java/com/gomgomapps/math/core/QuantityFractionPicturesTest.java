package com.gomgomapps.math.core;
import java.util.*;import org.junit.Test;import static org.junit.Assert.*;
public class QuantityFractionPicturesTest {
 @Test public void yearTwoPicturesExhaustActualWholesAndUnreducedFractions(){
  String id="el_fraction_of_number";CurriculumLimits limits=GlobalCurriculum.limits("england-primary-2021-v1",id,2);Generator g=new Generator(new Random(65));List<String> recent=new ArrayList<>();Set<String> pairs=new HashSet<>(),types=new HashSet<>();
  // Per representation:9 halves +5 thirds +4 quarters +4 two-quarters +4 three-quarters.
  for(int i=0;i<52;i++){
   Question q=g.next(id,recent,false,limits);assertFalse(recent.contains(q.signature()));recent.add(q.signature());StudyDiagram d=q.diagram;assertEquals(3,d.values.length);int total=(int)d.values[0],numerator=(int)d.values[1],denominator=(int)d.values[2];
   assertTrue(total<=20&&total>=denominator*2);assertEquals(0,total%denominator);assertTrue(Set.of("fractionQuantityObjects","fractionQuantityLength").contains(d.type));types.add(d.type);pairs.add(numerator+"/"+denominator);int answer=total/denominator*numerator;
   assertTrue(q.prompt.contains(numerator+"/"+denominator));assertTrue(q.prompt.contains(String.valueOf(total)));assertTrue(new Checker().check(q,List.of(),List.of(String.valueOf(answer))).correct());assertFalse(new Checker().check(q,List.of(),List.of(String.valueOf(answer+1))).correct());assertFalse(HelpPlan.forQuestion(q).canTransfer());assertEquals(5,q.studyGuide.frames.size());
  }
  assertEquals(2,types.size());assertEquals(Set.of("1/2","1/3","1/4","2/4","3/4"),pairs);assertEquals(recent.get(0),g.next(id,recent,false,limits).signature());
 }
 @Test public void optionalRepresentationsPreserveOtherCurriculaAndRejectMalformedOptions(){
  for(String bad:List.of("quantityRepresentations=answer","quantityRepresentations=objects,objects","quantityRepresentations=length,")){try{new CurriculumLimits(bad);fail(bad);}catch(IllegalArgumentException expected){}}
  Question normal=FractionSupply.next(Catalog.get("el_fraction_of_number"),new Random(1),CurriculumLimits.NONE,Map.of());assertEquals("fraction",normal.diagram.type);
  CurriculumLimits pairs=new CurriculumLimits("partFractions=2/4");Question shown=QuantityFractionPictures.create(Catalog.get("el_fraction_of_number"),2,4,3,"objects");assertTrue(pairs.allows(shown));assertFalse(pairs.allows(QuantityFractionPictures.create(Catalog.get("el_fraction_of_number"),1,2,3,"objects")));
 }
}
