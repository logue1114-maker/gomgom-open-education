package com.gomgomapps.math.core;
import org.junit.Test;import java.util.*;import static org.junit.Assert.*;
public class DecimalSignedTeachingTest {
 @Test public void normalProductionQuestionsOfferEnteredStepsWithoutAnswerTransfer(){
  Generator g=new Generator(new Random(202610061011L));for(String id:List.of("decimalAdd","decimalSub","decimalMul","decimalDiv","decimalDivInt","linearFraction","rational"))for(int i=0;i<500;i++){
   Question q=g.next(id,List.of(),i%2==0);assertNotNull(id,q.studyGuide);HelpPlan p=HelpPlan.forQuestion(q);assertFalse(p.canTransfer());HelpPlan.Draft d=new HelpPlan.Draft();
   for(int j=0;j<p.size();j++){String expected=q.studyGuide.frames.get(j).expected;assertTrue(p.step(j).accepts(expected));assertFalse(p.step(j).accepts(Expression.number(expected).add(Rational.ONE).toString()));d.entries.add(expected);}d.stage=p.size();assertEquals(id,Expression.number(q.answers[0]),Expression.number(p.enteredAnswer(d)));
  }
 }
 @Test public void decimalsAlignZerosAndReturnExactPlaceValuesFromVisibleGivens(){
  String[] ids={"decimalAdd","decimalSub","decimalMul","decimalDiv","decimalDivInt"},expr={"1.05 + 0.9","1.05 - 0.9","1.05 * 0.9","0.315 / 0.9","0.3 / 3"};
  List<List<String>> values=List.of(List.of("100","105","90","195","1.95"),List.of("100","105","90","15","0.15"),List.of("105","9","3","945","0.945"),List.of("1000","315","900","0.35"),List.of("10","3","30","0.1"));
  for(int i=0;i<ids.length;i++){Question q=new Question(ids[i],expr[i],expr[i],"999");DecimalTeaching.attach(q);assertEquals(values.get(i),q.studyGuide.frames.stream().map(f->f.expected).toList());}
 }
 @Test public void signedFractionsNormalizeNegativeDenominatorsAndZero(){
  String[] expr={"(-2/3) / (-4/5)","(2/3) / (-4/5)","(-1/3) + (1/3)","(-1/3) - (-1/2)"},expected={"5/6","-5/6","0/1","1/6"};
  for(int i=0;i<expr.length;i++){Question q=new Question("rational",expr[i],expr[i],"999");FractionEquationTeaching.attach(q);HelpPlan p=HelpPlan.forQuestion(q);HelpPlan.Draft d=new HelpPlan.Draft();for(StudyGuide.Frame f:q.studyGuide.frames)d.entries.add(f.expected);d.stage=p.size();assertEquals(expected[i],p.enteredAnswer(d));assertTrue(Integer.parseInt(d.entries.get(d.stage-1))>0);}
 }
 @Test public void fractionalEquationsUseBothSidesForSignedAndZeroSolutions(){
  for(int a=2;a<=9;a++)for(int b=-9;b<=9;b++)for(int x=-9;x<=9;x++){
   String e="x/"+a+" + ("+b+") = "+(x+b);Question q=new Question("linearFraction",e,e,"999");FractionEquationTeaching.attach(q);assertEquals(List.of(""+(-b),""+x,""+(a*x)),q.studyGuide.frames.stream().map(f->f.expected).toList());assertFalse(HelpPlan.forQuestion(q).canTransfer());
  }
 }
}
