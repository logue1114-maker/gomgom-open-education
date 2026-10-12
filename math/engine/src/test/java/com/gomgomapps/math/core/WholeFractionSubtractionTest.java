package com.gomgomapps.math.core;
import java.util.*;import org.junit.Test;import static org.junit.Assert.*;
public class WholeFractionSubtractionTest {
 @Test public void officialExampleAndPublicConditionsIgnoreHiddenKey(){
  Question q=new Question(WholeFractionSubtraction.ID,"5 - (3/8)","999","999");q.answerFormat="fraction";
  assertTrue(new Checker().check(q,List.of(),List.of("37/8")).correct());assertFalse(new Checker().check(q,List.of(),List.of("999/1")).correct());assertFalse(new Checker().check(q,List.of(),List.of("5-3/8")).correct());
  assertEquals(1,PublicFractionArithmetic.errorPart(q,"38/8"));assertEquals(2,PublicFractionArithmetic.errorPart(q,"37/9"));
  HelpPlan help=HelpPlan.forQuestion(q);assertNotNull(help);assertEquals(8,help.size());assertFalse(help.canTransfer());List<String> values=List.of("5","3","8","40","37","1","37","8");for(int i=0;i<8;i++){assertTrue(help.step(i).accepts(values.get(i)));assertEquals("",help.step(i).after);}
 }
 @Test public void boundsAndUnreducedSubtrahendsAreChecked(){
  for(String bad:List.of("0 - (1/2)","13 - (1/2)","5 + (1/2)","5 - (0/8)","5 - (8/8)","5 - (1/0)","5 - (1/13)"))assertNull(PublicFractionArithmetic.givens(new Question(WholeFractionSubtraction.ID,bad,"1","1")));
  Question q=WholeFractionSubtraction.make(1,6,12);assertTrue(new Checker().check(q,List.of(),List.of("1/2")).correct());assertEquals("1 - (6/12)",q.prompt);assertEquals("2",q.studyGuide.frames.get(7).expected);
 }
 @Test public void normalNaSixUsesFreshOriginalConditions(){
  var pack=GlobalCurriculum.packs("NA").stream().filter(p->p.id.equals("na-nied-primary-2024-v1")).findFirst().orElseThrow();assertTrue(pack.inGrade(WholeFractionSubtraction.ID,6));assertFalse(pack.inGrade(WholeFractionSubtraction.ID,5));var limits=GlobalCurriculum.limits(pack.id,WholeFractionSubtraction.ID,6);Generator g=new Generator(new Random(280));List<String> recent=new ArrayList<>();boolean unreduced=false,one=false,twelve=false;
  for(int i=0;i<150;i++){Question q=g.next(WholeFractionSubtraction.ID,recent,i%2==0,limits);assertFalse(recent.contains(q.signature()));recent.add(q.signature());int[] v=PublicFractionArithmetic.givens(q);assertNotNull(v);assertEquals(1,v[1]);assertTrue(limits.allows(q));assertTrue(new Checker().check(q,List.of(),List.of(Rational.of(v[0]*v[3]-v[2],v[3]).toString())).correct());unreduced|=Generator.gcd(v[2],v[3])>1;one|=v[0]==1;twelve|=v[0]==12;}
  assertTrue(unreduced);assertTrue(one);assertTrue(twelve);
 }
}
