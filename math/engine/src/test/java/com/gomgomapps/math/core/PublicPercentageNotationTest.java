package com.gomgomapps.math.core;
import java.util.*;import java.math.*;import org.junit.Test;import static org.junit.Assert.*;
public class PublicPercentageNotationTest {
 @Test public void allWholePercentagesUseVisibleRatesAndRequestedRepresentation(){Checker c=new Checker();for(int p=0;p<=100;p++)for(String id:List.of(PercentageNotation.FRACTION,PercentageNotation.DECIMAL)){
  Question original=PercentageNotation.make(id,p),q=new Question(id,original.prompt,"poison","999");
  String expected=id.equals(PercentageNotation.FRACTION)?p+"/100":String.format(Locale.ROOT,"%d.%02d",p/100,p%100);
  assertTrue(expected,c.check(q,List.of(),List.of(expected)).correct());
  String wrong=id.equals(PercentageNotation.FRACTION)?(p+1)+"/100":BigDecimal.valueOf(p+1,2).toPlainString();assertEquals(Checker.Status.WRONG_ANSWER,c.check(q,List.of(),List.of(wrong)).status);
  if(id.equals(PercentageNotation.FRACTION)){assertFalse(c.check(q,List.of(),List.of((2*p)+"/200")).correct());assertEquals(2,FractionInput.errorPart(q,(2*p)+"/200"));assertEquals(1,FractionInput.errorPart(q,(p+1)+"/100"));}
  else assertEquals(Checker.Status.INPUT_NEEDED,c.check(q,List.of(),List.of(p+"/100")).status);
 }}
 @Test public void emptyHelpMeaningAndDraftVersionRemain(){for(String id:List.of(PercentageNotation.FRACTION,PercentageNotation.DECIMAL))for(int p:List.of(0,1,37,99,100)){
  Question q=PercentageNotation.make(id,p);q.answers[0]="poison";HelpPlan h=HelpPlan.forQuestion(q);assertFalse(h.canTransfer());assertEquals("percentage-notation-v1",q.studyGuide.teachingVersion);assertEquals(id.equals(PercentageNotation.FRACTION)?3:4,h.size());assertTrue(h.step(0).accepts("100"));assertFalse(h.step(0).accepts("10"));assertTrue(h.step(1).accepts("100"));assertTrue(h.step(2).accepts(""+p));if(h.size()==4)assertTrue(h.step(3).accepts(BigDecimal.valueOf(p,2).toPlainString()));
 }}
 @Test public void incompleteAndMalformedValuesDoNotPass(){Checker c=new Checker();for(String id:List.of(PercentageNotation.FRACTION,PercentageNotation.DECIMAL)){
  Question q=PercentageNotation.make(id,37);for(String raw:List.of("","abc","37%","3+7","37/0","9".repeat(121)))assertEquals(raw,Checker.Status.INPUT_NEEDED,c.check(q,List.of(),List.of(raw)).status);assertEquals(Checker.Status.INPUT_NEEDED,c.check(q,List.of(),Arrays.asList((String)null)).status);q.prompt="invalid";assertEquals(Checker.Status.INPUT_NEEDED,c.check(q,List.of(),List.of("0.37")).status);
 }}
}
