package com.gomgomapps.math.core;
import java.util.*;import java.math.BigDecimal;import org.junit.Test;import static org.junit.Assert.*;
public class EnglandDecimalLinksTest {
 @Test public void twoSelectedPlacementsHaveOneHundredFreshPublicConversionsAndBlankHelp(){
  var pack=GlobalCurriculum.packs("GB").stream().filter(p->p.id.equals("england-primary-2021-v1")).findFirst().orElseThrow();
  for(String id:List.of("el_fraction_decimal","el_decimal_fraction")){int grade=id.equals("el_fraction_decimal")?4:5;assertTrue(pack.inGrade(id,grade));var limits=GlobalCurriculum.limits(pack.id,id,grade);Generator gen=new Generator(new Random(7600+grade));List<String> recent=new ArrayList<>();boolean belowOne=false,wholeAbove=false,ten=false,hundred=false;
   for(int k=0;k<100;k++){Question q=gen.next(id,recent,false,limits);assertFalse(recent.contains(q.signature()));recent.add(q.signature());String raw=q.prompt.substring(0,q.prompt.indexOf('의'));List<String> steps;String answer;
    if(grade==4){String[] pair=raw.split("/");int n=Integer.parseInt(pair[0]),d=Integer.parseInt(pair[1]);assertTrue(d==10||d==100);assertTrue(n>0&&n<d);ten|=d==10;hundred|=d==100;answer=BigDecimal.valueOf(n).divide(BigDecimal.valueOf(d)).toPlainString();steps=List.of(pair[0],pair[1],answer);}
    else{BigDecimal value=new BigDecimal(raw);belowOne|=value.compareTo(BigDecimal.ONE)<0;wholeAbove|=value.compareTo(BigDecimal.ONE)>0;int places=raw.length()-raw.indexOf('.')-1;assertTrue(places<=2);int d=(int)Math.pow(10,places);long n=value.multiply(BigDecimal.valueOf(d)).longValueExact();answer=n+"/"+d;steps=List.of(raw,""+places,""+d,""+n);}
    assertTrue(new Checker().check(q,List.of(),List.of(answer)).correct());assertFalse(new Checker().check(q,List.of(),List.of("999/1")).correct());HelpPlan h=HelpPlan.forQuestion(q);assertEquals(grade==4?3:4,h.size());assertFalse(h.canTransfer());for(int i=0;i<h.size();i++){assertTrue(h.step(i).accepts(steps.get(i)));assertEquals("",h.step(i).after);}
    Question saved=new Question(id,q.prompt,"poison","999");HelpPlan restored=HelpPlan.forQuestion(saved);for(int i=0;i<h.size();i++)assertTrue(restored.step(i).accepts(steps.get(i)));
   }if(grade==4)assertTrue(ten&&hundred);else assertTrue(belowOne&&wholeAbove);
  }
 }
 @Test public void defaultWholeRangePreservedAndExplicitOnePlaceRespected(){
  Generator g=new Generator(new Random(761));for(int i=0;i<100;i++){Question q=g.next("el_decimal_fraction",List.of(),false);assertTrue(Expression.number(q.expression).compareTo(Rational.of(1))>0);Question small=g.next("el_decimal_fraction",List.of(),false,new CurriculumLimits("minGiven=0;maxGiven=1;decimalPlaces=1"));assertEquals(1,small.expression.length()-small.expression.indexOf('.')-1);assertTrue(Expression.number(small.expression).compareTo(Rational.of(1))<=0);}
 }
}
