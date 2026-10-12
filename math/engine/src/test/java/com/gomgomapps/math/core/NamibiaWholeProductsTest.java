package com.gomgomapps.math.core;
import java.util.*;import org.junit.Test;import static org.junit.Assert.*;
public class NamibiaWholeProductsTest {
 @Test public void gradeSixHasFreshPublicProductsAndOriginalPictures(){
  var pack=GlobalCurriculum.packs("NA").stream().filter(p->p.id.equals("na-nied-primary-2024-v1")).findFirst().orElseThrow();assertTrue(pack.inGrade("fracMulInt",6));assertFalse(pack.inGrade("fracMulInt",5));var limits=GlobalCurriculum.limits(pack.id,"fracMulInt",6);assertTrue(WholeFractionProducts.selected("fracMulInt",limits));Generator generator=new Generator(new Random(281));List<String> recent=new ArrayList<>();boolean forward=false,reversed=false,unreduced=false,integer=false;
  for(int i=0;i<150;i++){Question q=generator.next("fracMulInt",recent,i%2==0,limits);assertFalse(recent.contains(q.signature()));recent.add(q.signature());var p=WholeFractionProducts.givens(q);assertNotNull(p);assertTrue(limits.allows(q));Rational expected=p.left.value().mul(p.right.value());q.answers[0]="999";q.expression="poison";assertTrue(new Checker().check(q,List.of(),List.of(expected.n+"/"+expected.d)).correct());assertFalse(new Checker().check(q,List.of(),List.of(expected.add(Rational.ONE).n+"/"+expected.d)).correct());HelpPlan help=HelpPlan.forQuestion(q);assertEquals(9,help.size());assertFalse(help.canTransfer());for(int j=0;j<9;j++){assertTrue(help.step(j).accepts(q.studyGuide.frames.get(j).expected));assertEquals("",help.step(j).after);}assertEquals("wholeFractionProductGiven",q.diagram.type);forward|=p.left.denominator!=1;reversed|=p.left.denominator==1;var f=p.left.denominator==1?p.right:p.left;unreduced|=Generator.gcd(f.numerator,f.denominator)>1;integer|=expected.isInteger();}
  assertTrue(forward);assertTrue(reversed);assertTrue(unreduced);assertTrue(integer);
 }
 @Test public void officialExampleIsCheckedFromOriginalOperands(){
  Question q=WholeFractionProducts.make("fracMulInt",0,3,5,4,true);assertEquals("4 × 3/5",q.prompt);q.answers[0]="999";q.expression="hidden";assertTrue(new Checker().check(q,List.of(),List.of("12/5")).correct());assertEquals(1,WholeFractionProducts.errorPart(q,"13/5"));assertEquals(2,WholeFractionProducts.errorPart(q,"12/6"));assertFalse(HelpPlan.forQuestion(q).canTransfer());
 }
}
