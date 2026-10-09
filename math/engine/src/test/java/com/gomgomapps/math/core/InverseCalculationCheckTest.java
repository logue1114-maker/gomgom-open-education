package com.gomgomapps.math.core;
import org.junit.Test;import static org.junit.Assert.*;import java.util.*;
public class InverseCalculationCheckTest {
 @Test public void eachGradeHasFreshCorrectAndIncorrectClaimsAndIndependentChecks(){
  for(int grade:new int[]{3,4})for(String id:List.of(InverseCalculationCheck.ADD,InverseCalculationCheck.SUB)){
   var limits=GlobalCurriculum.limits("england-primary-2021-v1",id,grade);assertEquals(grade==3?999:9999,limits.wholeMaximum(9999));Generator g=new Generator(new Random(115));List<String> recent=new ArrayList<>();boolean trueClaim=false,falseClaim=false;
   for(int i=0;i<100;i++){Question q=g.next(id,recent,i%2==0,limits);assertFalse(recent.contains(q.signature()));recent.add(q.signature());String[] equation=q.prompt.split("\n")[1].split(" [+−=] ");assertEquals(3,equation.length);int a=Integer.parseInt(equation[0]),b=Integer.parseInt(equation[1]),proposed=Integer.parseInt(equation[2]),value=id.equals(InverseCalculationCheck.ADD)?proposed-b:proposed+b;boolean correct=value==a;trueClaim|=correct;falseClaim|=!correct;assertTrue(a<=limits.wholeMaximum(9999)&&b<=limits.wholeMaximum(9999));assertTrue(value>=0);assertEquals(2,q.answers.length);assertTrue(q.choices.isEmpty());
    q.answers=new String[]{"poison","poison"};q.expression="wrong";assertTrue(new Checker().check(q,List.of(),List.of(""+value,correct?"0":"1")).correct());assertEquals(0,new Checker().check(q,List.of(),List.of(""+(value+1),correct?"0":"1")).index);assertEquals(1,new Checker().check(q,List.of(),List.of(""+value,correct?"1":"0")).index);
    HelpPlan h=HelpPlan.forQuestion(q);assertEquals(5,h.size());assertFalse(h.canTransfer());assertTrue(h.step(0).accepts(""+proposed));assertTrue(h.step(1).accepts(""+b));assertTrue(h.step(2).accepts(""+value));assertTrue(h.step(3).accepts(""+a));assertTrue(h.step(4).accepts(correct?"같음":"다름"));assertFalse(h.step(4).accepts(correct?"다름":"같음"));assertEquals(Set.of("같음","다름"),h.step(4).options.keySet());assertFalse(h.step(2).before.matches(".*\\d.*"));
   }assertTrue(trueClaim&&falseClaim);
  }
 }
 @Test public void zeroMaxAndWrongClaimAreNotReplacedByAnAnswerKey(){
  for(String id:List.of(InverseCalculationCheck.ADD,InverseCalculationCheck.SUB))for(int n:new int[]{0,999,9999}){
   boolean add=id.equals(InverseCalculationCheck.ADD);for(int error:new int[]{0,1,10,100}){int proposed=(add?n+n:0)+error;Question q=InverseCalculationCheck.make(id,n,n,proposed);int value=add?proposed-n:proposed+n;assertTrue(new Checker().check(q,List.of(),List.of(""+value,error==0?"0":"1")).correct());}
  }
  Question q=InverseCalculationCheck.make(InverseCalculationCheck.ADD,17,3,21);assertTrue(new Checker().check(q,List.of(),List.of("18","1")).correct());assertFalse(new Checker().check(q,List.of(),List.of("17","1")).correct());assertEquals(Checker.Status.INPUT_NEEDED,new Checker().check(q,List.of(),List.of("18","")).status);assertEquals(Checker.Status.INPUT_NEEDED,new Checker().check(q,List.of(),List.of("21-3","1")).status);q.prompt="invalid";assertEquals(Checker.Status.INPUT_NEEDED,new Checker().check(q,List.of(),List.of("18","1")).status);
 }
}
