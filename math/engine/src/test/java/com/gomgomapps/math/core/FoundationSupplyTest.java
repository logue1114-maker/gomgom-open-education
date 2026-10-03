package com.gomgomapps.math.core;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;
import java.util.regex.*;
public class FoundationSupplyTest {
    @Test public void powersUseTheDisplayedLawAndMixCalculationWithExponentFrames(){
        Generator generator=new Generator(new Random(1002));Set<String> seen=new HashSet<>(),forms=new HashSet<>();Set<Integer> positions=new HashSet<>();
        Pattern p=Pattern.compile("(\\d+)\\^(\\d+) × (\\d+)\\^(\\d+)(?: = (\\d+)\\^□\\n□에 들어갈 지수는\\?)?");
        for(int i=0;i<600;i++){
            Question q=generator.next("powerLaw",List.copyOf(seen),true);Matcher m=p.matcher(q.prompt);assertTrue(q.prompt,m.matches());int base=Integer.parseInt(m.group(1)),a=Integer.parseInt(m.group(2)),b=Integer.parseInt(m.group(4));assertEquals(m.group(1),m.group(3));
            Rational expected;if(m.group(5)==null){expected=Rational.ONE;for(int k=0;k<a+b;k++)expected=expected.mul(Rational.of(base));forms.add("calculation");}else{assertEquals(m.group(1),m.group(5));expected=Rational.of(a+b);forms.add("exponent");assertTrue(HelpPlan.forQuestion(q).step(0).accepts(String.valueOf(a+b)));}
            assertEquals(q.prompt,expected,Expression.number(q.answers[0]));assertTrue(new Checker().check(q,List.of(),List.of(expected.toString())).correct());if(!q.choices.isEmpty()){positions.add(q.correctChoice);assertEquals(expected,Expression.number(q.choices.get(q.correctChoice)));assertEquals(4,q.choices.stream().map(Expression::number).distinct().count());}seen.add(q.signature());
        }
        assertTrue(seen.size()>=180);assertEquals(Set.of("calculation","exponent"),forms);assertEquals(Set.of(0,1,2,3),positions);
    }
    @Test public void probabilityUsesTheRequestedColourAmongAllVisibleBalls(){
        Generator generator=new Generator(new Random(3002));Set<String> recent=new LinkedHashSet<>(),colours=new HashSet<>();Set<Integer> positions=new HashSet<>();boolean two=false,three=false;
        Pattern p=Pattern.compile("빨간 공 (\\d+)개, 파란 공 (\\d+)개(?:, 초록 공 (\\d+)개)? 중 하나를 같은 가능성으로 뽑습니다\\.\\n(빨간|파란|초록) 공을 뽑을 확률은\\?");
        for(int i=0;i<600;i++){
            Question q=generator.next("probability",List.copyOf(recent),true);Matcher m=p.matcher(q.prompt);assertTrue(q.prompt,m.matches());int red=Integer.parseInt(m.group(1)),blue=Integer.parseInt(m.group(2)),green=m.group(3)==null?0:Integer.parseInt(m.group(3));int total=red+blue+green;int favourable=m.group(4).equals("빨간")?red:m.group(4).equals("파란")?blue:green;Rational expected=Rational.of(favourable,total);
            assertEquals(expected,Expression.number(q.answers[0]));assertTrue(new Checker().check(q,List.of(),List.of(expected.toString())).correct());HelpPlan plan=HelpPlan.forQuestion(q);assertTrue(plan.step(0).accepts(String.valueOf(total)));assertTrue(plan.step(1).accepts(expected.toString()));assertFalse(plan.step(0).accepts(String.valueOf(total-1)));assertEquals(4,q.choices.stream().map(Expression::number).distinct().count());assertEquals(expected,Expression.number(q.choices.get(q.correctChoice)));for(String choice:q.choices){Rational value=Expression.number(choice);assertTrue(value.compareTo(Rational.ZERO)>=0&&value.compareTo(Rational.ONE)<=0);}positions.add(q.correctChoice);colours.add(m.group(4));two|=green==0;three|=green>0;recent.add(q.signature());
        }
        assertEquals(600,recent.size());assertEquals(Set.of("빨간","파란","초록"),colours);assertTrue(two&&three);assertEquals(Set.of(0,1,2,3),positions);
    }
}
