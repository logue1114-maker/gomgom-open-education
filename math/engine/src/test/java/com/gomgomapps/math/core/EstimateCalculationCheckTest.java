package com.gomgomapps.math.core;
import org.junit.Test;import static org.junit.Assert.*;import java.util.*;import java.math.*;
public class EstimateCalculationCheckTest {
 private int round(int n,int unit){return BigDecimal.valueOf(n).divide(BigDecimal.valueOf(unit),0,RoundingMode.HALF_UP).intValueExact()*unit;}
 @Test public void freshGradeClaimsUsePublicRoundingAndIndependentRangeChecks(){
  for(int grade:new int[]{3,4})for(String id:List.of(EstimateCalculationCheck.ADD,EstimateCalculationCheck.SUB)){
   var limits=GlobalCurriculum.limits("england-primary-2021-v1",id,grade);Generator g=new Generator(new Random(116));List<String> recent=new ArrayList<>();boolean inside=false,outside=false;
   for(int i=0;i<100;i++){Question q=g.next(id,recent,false,limits);assertFalse(recent.contains(q.signature()));recent.add(q.signature());String[] lines=q.prompt.split("\n"),eq=lines[1].split(" [+−=] ");int unit=Integer.parseInt(lines[0].split("의")[0]),a=Integer.parseInt(eq[0]),b=Integer.parseInt(eq[1]),proposed=Integer.parseInt(eq[2]),ra=round(a,unit),rb=round(b,unit);boolean add=id.equals(EstimateCalculationCheck.ADD);int estimate=add?ra+rb:ra-rb,delta=Math.abs(proposed-estimate);boolean in=delta<=unit;inside|=in;outside|=!in;assertTrue(a<=(grade==3?999:9999)&&b<=(grade==3?999:9999));assertTrue(Arrays.stream(limits.roundingUnits()).anyMatch(x->x==unit));assertTrue(Math.abs((add?a+b:a-b)-estimate)<=unit);
    q.answers=new String[]{"poison","poison"};q.expression="wrong";Checker c=new Checker();assertTrue(c.check(q,List.of(),List.of(""+estimate,in?"0":"1")).correct());assertEquals(0,c.check(q,List.of(),List.of(""+(estimate+1),in?"0":"1")).index);assertEquals(1,c.check(q,List.of(),List.of(""+estimate,in?"1":"0")).index);
    HelpPlan h=HelpPlan.forQuestion(q);assertEquals(17,h.size());assertFalse(h.canTransfer());for(int which=0;which<2;which++){int n=which==0?a:b,base=n/unit*unit,digit=(n%unit)/(unit/10),off=which*7;String[] values={""+n,""+unit,""+digit,digit>=5?"raise":"keep",""+base,""+(digit>=5?unit:0),""+round(n,unit)};for(int j=0;j<7;j++)assertTrue(h.step(off+j).instruction,h.step(off+j).accepts(values[j]));}assertTrue(h.step(14).accepts(""+estimate));assertTrue(h.step(15).accepts(""+delta));assertTrue(h.step(16).accepts(in?"안":"밖"));for(int j=0;j<h.size();j++)assertFalse(h.step(j).before.matches(".*\\d.*"));
   }assertTrue(inside&&outside);
  }
 }
 @Test public void conservativeEnvelopeDoesNotClaimExactCorrectness(){
  Question q=EstimateCalculationCheck.make(EstimateCalculationCheck.ADD,99,99,190,100);Checker c=new Checker();assertNotEquals(198,190);assertTrue(c.check(q,List.of(),List.of("200","0")).correct());
  for(String id:List.of(EstimateCalculationCheck.ADD,EstimateCalculationCheck.SUB))for(int unit:new int[]{10,100,1000})for(int a:new int[]{0,4,5,49,50,499,500,999,9999})for(int b:new int[]{0,5,50,500,9999}){boolean add=id.equals(EstimateCalculationCheck.ADD);if(!add&&a<b)continue;int estimate=add?round(a,unit)+round(b,unit):round(a,unit)-round(b,unit),actual=add?a+b:a-b;assertTrue(Math.abs(actual-estimate)<=unit);for(int d:new int[]{unit,unit+1}){q=EstimateCalculationCheck.make(id,a,b,estimate+d,unit);assertTrue(c.check(q,List.of(),List.of(""+estimate,d==unit?"0":"1")).correct());}}
  q=EstimateCalculationCheck.make(EstimateCalculationCheck.SUB,50,49,1,10);assertEquals(Checker.Status.INPUT_NEEDED,c.check(q,List.of(),List.of("0","")).status);assertEquals(Checker.Status.INPUT_NEEDED,c.check(q,List.of(),List.of("50-50","0")).status);q.prompt="invalid";assertEquals(Checker.Status.INPUT_NEEDED,c.check(q,List.of(),List.of("0","0")).status);
 }
}
