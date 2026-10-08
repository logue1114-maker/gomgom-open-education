package com.gomgomapps.math.core;

import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class StatisticsSupplyTest {
    private static Rational[] moments(String prompt){
        String values=prompt.substring(prompt.indexOf('[')+1,prompt.indexOf(']'));
        List<Rational> data=Arrays.stream(values.split(", ")).map(Expression::number).toList();
        Rational sum=Rational.ZERO,sumSquares=Rational.ZERO;
        for(Rational value:data){sum=sum.add(value);sumSquares=sumSquares.add(value.mul(value));}
        Rational count=Rational.of(data.size()),mean=sum.div(count);
        // E(X²)-E(X)² is independent of the generator's centered-square calculation.
        Rational variance=sumSquares.div(count).sub(mean.mul(mean));
        return new Rational[]{mean,variance.mul(count),variance,count};
    }

    @Test public void varianceUsesTheActualCountAndExactFractionalMean(){
        Generator generator=new Generator(new Random(10203));List<String> recent=new ArrayList<>();
        Set<String> seen=new HashSet<>();Set<Integer> counts=new HashSet<>(),positions=new HashSet<>();
        boolean fractionalMean=false,fractionalVariance=false;
        for(int i=0;i<600;i++){
            Question q=generator.next("sec_variance",recent,true);Rational[] m=moments(q.prompt);
            assertEquals(m[2],Expression.number(q.answers[0]));
            assertTrue(new Checker().check(q,List.of(),List.of(m[2].toString())).correct());
            if(!m[2].isZero())assertFalse(new Checker().check(q,List.of(),List.of(m[1].div(m[3].sub(Rational.ONE)).toString())).correct());
            HelpPlan plan=HelpPlan.forQuestion(q);assertFrames(q,plan,m,false);
            assertChoices(q,m[2]);positions.add(q.correctChoice);counts.add(m[3].intValue());
            fractionalMean|=!m[0].isInteger();fractionalVariance|=!m[2].isInteger();
            assertFalse(q.signature(),recent.contains(q.signature()));recent.add(q.signature());seen.add(q.signature());
        }
        assertEquals(Set.of(3,4,5,6),counts);assertTrue(fractionalMean&&fractionalVariance);
        assertEquals(600,seen.size());assertEquals(Set.of(0,1,2,3),positions);
    }

    @Test public void standardDeviationMixesVarianceGivenWithDataAndIncludesZero(){
        Generator generator=new Generator(new Random(20203));List<String> recent=new ArrayList<>();
        Set<String> seen=new HashSet<>();Set<Integer> positions=new HashSet<>(),counts=new HashSet<>();
        Map<Rational,Integer> frequencies=new HashMap<>();
        boolean given=false,dataset=false,fractional=false,zero=false;
        for(int i=0;i<600;i++){
            Question q=generator.next("sec_standard_deviation",recent,true);Rational expected;
            HelpPlan plan=HelpPlan.forQuestion(q);
            if(q.prompt.startsWith("분산이 ")){
                Rational variance=Expression.number(q.prompt.substring(4,q.prompt.indexOf("인 자료")));
                expected=variance.sqrt();given=true;assertEquals(2,plan.size());assertTrue(plan.step(0).accepts(variance.toString()));assertTrue(plan.step(1).accepts(expected.toString()));assertFalse(plan.canTransfer());
            }else{
                Rational[] m=moments(q.prompt);expected=m[2].sqrt();dataset=true;counts.add(m[3].intValue());assertFrames(q,plan,m,true);
            }
            assertEquals(expected,Expression.number(q.answers[0]));assertChoices(q,expected);
            assertTrue(new Checker().check(q,List.of(),List.of(expected.toString())).correct());
            if(!expected.isZero())assertFalse(new Checker().check(q,List.of(),List.of(expected.neg().toString())).correct());
            fractional|=!expected.isInteger();zero|=expected.isZero();positions.add(q.correctChoice);
            frequencies.merge(expected,1,Integer::sum);
            assertFalse(q.signature(),recent.contains(q.signature()));recent.add(q.signature());if(recent.size()>100)recent.remove(0);seen.add(q.signature());
        }
        assertTrue(given&&dataset&&fractional&&zero);assertTrue(counts.containsAll(Set.of(3,4,5,6)));
        assertTrue("actual multiset diversity="+seen.size(),seen.size()>=180);assertEquals(Set.of(0,1,2,3),positions);
        assertTrue("one result must not dominate the drill: "+frequencies,Collections.max(frequencies.values())<=90);
    }

    private static void assertFrames(Question q,HelpPlan plan,Rational[] m,boolean deviation){
        List<Rational> expected=new ArrayList<>(List.of(m[0].mul(m[3]),m[3],m[0]));
        String data=q.prompt.substring(q.prompt.indexOf('[')+1,q.prompt.indexOf(']'));
        for(String token:data.split(", ")){Rational delta=Expression.number(token).sub(m[0]);expected.add(delta);expected.add(delta.mul(delta));}
        expected.add(m[1]);expected.add(m[2]);if(deviation)expected.add(m[2].sqrt());
        assertEquals(expected.size(),plan.size());assertFalse(plan.canTransfer());
        for(int j=0;j<expected.size();j++){assertTrue(plan.step(j).accepts(expected.get(j).toString()));assertFalse(plan.step(j).accepts(expected.get(j).add(Rational.ONE).toString()));assertFalse(plan.step(j).before.matches(".*[0-9].*[+÷×].*"));}
    }
    private static void assertChoices(Question q,Rational expected){
        assertEquals(q.prompt,4,q.choices.size());assertEquals(4,q.choices.stream().map(Expression::number).distinct().count());
        assertEquals(expected,Expression.number(q.choices.get(q.correctChoice)));
        for(String choice:q.choices)assertTrue(choice,Expression.number(choice).compareTo(Rational.ZERO)>=0);
    }
}
