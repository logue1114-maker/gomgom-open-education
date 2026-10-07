package com.gomgomapps.math.core;
import org.junit.Test;import java.util.*;import static org.junit.Assert.*;
public class RatioValueTeachingTest {
 @Test public void normalProductionHasEnteredScaffoldsAndNoTransfer(){
  Generator g=new Generator(new Random(202610061101L));for(String id:List.of("percent","proportion","substitute","function","linearValue"))for(int i=0;i<500;i++){
   Question q=g.next(id,List.of(),i%2==0);assertNotNull(id,q.studyGuide);HelpPlan p=HelpPlan.forQuestion(q);assertFalse(p.canTransfer());assertEquals(id.equals("percent")?4:5,p.size());HelpPlan.Draft d=new HelpPlan.Draft();
   for(int j=0;j<p.size();j++){String expected=q.studyGuide.frames.get(j).expected;assertTrue(p.step(j).accepts(expected));assertFalse(p.step(j).accepts(Expression.number(expected).add(Rational.ONE).toString()));d.entries.add(expected);}d.stage=p.size();assertEquals(Expression.number(q.answers[0]),Expression.number(p.enteredAnswer(d)));
  }
 }
 @Test public void proportionUsesVisibleTermsInsteadOfAnswerContainingExpression(){
  Question q=new Question("proportion","3 : 5 = 12 : x\nx의 값은?","x=999","999");RatioValueTeaching.attach(q);assertEquals(List.of("3","5","12","4","20"),q.studyGuide.frames.stream().map(f->f.expected).toList());
 }
 @Test public void signedFractionalSubstitutionAndZeroUseGivens(){
  for(int n=-8;n<=8;n++)for(int den=1;den<=4;den++)for(int x=-8;x<=8;x++)for(int b=-3;b<=3;b++){
   String e="("+n+"/"+den+") × ("+x+") + ("+b+")";Question q=new Question("linearValue",e,e,"999");RatioValueTeaching.attach(q);Rational product=Rational.of(n*x,den);assertEquals(List.of(""+x,Rational.of(n,den).toString(),product.toString(),""+b,product.add(Rational.of(b)).toString()),q.studyGuide.frames.stream().map(f->f.expected).toList());
  }
 }
 @Test public void percentagesPreserveSmallExactDecimalValues(){
  Question q=new Question("percent","30의 5%는?","30*5/100","999");RatioValueTeaching.attach(q);assertEquals(List.of("30","5","0.05","1.5"),q.studyGuide.frames.stream().map(f->f.expected).toList());
 }
}
