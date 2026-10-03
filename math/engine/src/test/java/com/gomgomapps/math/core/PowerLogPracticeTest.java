package com.gomgomapps.math.core;

import java.math.BigInteger;
import java.util.*;
import java.util.regex.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class PowerLogPracticeTest {
    @Test public void recommendedFoundationsIncludeFractionAndNegativeExponentWorkBeforeLogarithms(){
        List<String> foundation=Catalog.foundationOrder("log");assertTrue(foundation.contains("rational"));assertTrue(foundation.contains("negativePower"));assertTrue(foundation.indexOf("rational")<foundation.indexOf("negativePower"));
        List<String> common=Catalog.foundationOrder("commonLog");assertTrue(common.indexOf("negativePower")<common.indexOf("log"));
    }
    private int[] numbers(String prompt){Matcher m=Pattern.compile("-?\\d+").matcher(prompt);List<Integer> n=new ArrayList<>();while(m.find())n.add(Integer.parseInt(m.group()));return n.stream().mapToInt(Integer::intValue).toArray();}
    private Rational value(int base,int exponent){BigInteger power=BigInteger.valueOf(base).pow(Math.abs(exponent));return exponent<0?new Rational(BigInteger.ONE,power):new Rational(power,BigInteger.ONE);}
    private int exponent(int base,Rational argument){
        for(int power=-8;power<=8;power++)if(value(base,power).equals(argument))return power;
        throw new AssertionError("No integer logarithm: "+argument);
    }
    private List<Rational> solve(String id,String prompt){
        int[] n=numbers(prompt);
        if(id.equals("negativePower")){
            if(prompt.contains("□")){int power=exponent(n[0],Rational.of(n[2]));return List.of(Rational.of(power),Rational.of(-power));}
            if(prompt.startsWith("(1/"))return List.of(Rational.of(n[1]),value(n[1],-n[2]));
            return List.of(value(n[0],-n[1]),value(n[0],n[1]));
        }
        if(id.equals("log")){
            if(prompt.contains("□")){if(n[1]>=0)return List.of(value(n[0],n[1]));return List.of(value(n[0],-n[1]),value(n[0],n[1]));}
            Rational arg=n.length==3?Rational.of(n[1],n[2]):Rational.of(n[1]);return List.of(Rational.of(exponent(n[0],arg)));
        }
        if(prompt.startsWith("양수")){
            Matcher log=Pattern.compile("= (\\d+\\.\\d+)").matcher(prompt);assertTrue(log.find());double publishedLog=Double.parseDouble(log.group(1));
            int integerDigits=BigInteger.valueOf((long)Math.floor(Math.pow(10,publishedLog))).toString().length();
            return List.of(Rational.of((int)Math.floor(publishedLog)),Rational.of(integerDigits));
        }
        // Literal 10 is the second number in N = coefficient × 10^exponent.
        BigInteger integer=BigInteger.valueOf(n[0]).multiply(BigInteger.TEN.pow(n[2]));
        return List.of(Rational.of(String.valueOf(n[0]).length()),Rational.of(integer.toString().length()));
    }
    @Test public void publicPowersLogarithmsAndDigitCountsAreIndependentlySolved(){
        Generator generator=new Generator(new Random(102070));Checker checker=new Checker();Set<Integer> positions=new HashSet<>();Set<String> forms=new HashSet<>();
        for(String id:List.of("negativePower","log","commonLog"))for(int i=0;i<500;i++){
            Question q=generator.next(id,List.of(),i%2==0);List<Rational> solved=solve(id,q.prompt);Rational answer=solved.get(solved.size()-1);
            assertEquals(q.prompt,answer,Expression.number(q.answers[0]));assertTrue(checker.check(q,List.of(),List.of(answer.toString())).correct());assertFalse(checker.check(q,List.of(),List.of(answer.add(Rational.ONE).toString())).correct());
            assertFalse(q.stepSupport);forms.add(id+(q.prompt.contains("□")?"blank":q.prompt.startsWith("(1/")?"reciprocal":q.prompt.startsWith("자연수")?"integer":"value"));
            if(i%2==0){assertEquals(q.prompt,4,q.choices.size());Set<Rational> choices=new HashSet<>();for(String choice:q.choices){Rational v=Expression.number(choice);assertTrue(choices.add(v));assertEquals(answer.isInteger(),v.isInteger());}assertEquals(answer,Expression.number(q.choices.get(q.correctChoice)));positions.add(q.correctChoice);}
        }
        assertEquals(7,forms.size());assertEquals(Set.of(0,1,2,3),positions);
    }
    @Test public void oneHundredItemsVaryPublicNumbersAndTargets(){
        for(String id:List.of("negativePower","log","commonLog")){
            Generator generator=new Generator(new Random(102071+id.hashCode()));List<String> recent=new ArrayList<>();Set<String> answers=new HashSet<>();
            for(int i=0;i<100;i++){Question q=generator.next(id,recent,false);assertFalse(q.prompt,recent.contains(q.signature()));recent.add(q.signature());answers.add(q.answers[0]);}
            assertTrue(id+" "+answers.size(),answers.size()>=(id.equals("commonLog")?8:20));
        }
    }
    @Test public void studentHelpRejectsWrongAndRestoresEnteredValuesWithoutAutofill(){
        Generator generator=new Generator(new Random(102072));
        for(String id:List.of("negativePower","log","commonLog"))for(int i=0;i<100;i++){
            Question q=generator.create(Catalog.get(id));HelpPlan help=HelpPlan.forQuestion(q);List<Rational> solved=solve(id,q.prompt);assertNotNull(help);assertFalse(help.canTransfer());assertEquals(q.prompt,solved.size(),help.size());HelpPlan.Draft draft=help.restore(null,q.id);
            for(int stage=0;stage<solved.size();stage++){
                Rational expected=solved.get(stage);HelpPlan.Step step=help.step(stage);assertTrue(q.prompt+" "+step.before,step.accepts(expected.toString()));assertFalse(step.accepts(""));assertFalse(step.accepts(expected.add(Rational.ONE).toString()));
                draft.entries.set(stage,expected.toString());draft=help.restore(draft.copy(),q.id);assertEquals(stage,draft.stage);assertEquals(expected.toString(),draft.entries.get(stage));draft.stage++;draft=help.restore(draft.copy(),q.id);assertEquals(stage+1,draft.stage);
            }
            assertEquals(q.answers[0],solved.get(solved.size()-1).toString());draft.entries.set(0,"999999");assertEquals(0,help.restore(draft,q.id).stage);
        }
    }
}
