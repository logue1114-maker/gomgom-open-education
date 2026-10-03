package com.gomgomapps.math.core;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;
import java.util.regex.*;

public class DrillCoverageTest {
    private final Generator generator=new Generator(new Random(2026090814L));
    private Question next(String id){return generator.next(id,List.of(),true);}
    @Test public void smallAdditionAndSubtractionIncludeZeroWithoutLeavingTheNumberRange(){
        for(String id:List.of("add9","sub9")){
            Set<String> seen=new HashSet<>();Set<String> answers=new HashSet<>();
            for(int i=0;i<1500;i++){
                Question q=next(id);String[] parts=q.prompt.split(id.equals("add9")?" \\+ ":" - ");int a=Integer.parseInt(parts[0]),b=Integer.parseInt(parts[1]);
                assertTrue(a>=0&&a<=9&&b>=0&&b<=9);int expected=id.equals("add9")?a+b:a-b;assertTrue(expected>=0&&expected<=9);assertEquals(Integer.toString(expected),q.answers[0]);seen.add(q.prompt);answers.add(q.answers[0]);
            }
            assertEquals(10,answers.size());assertTrue(seen.contains(id.equals("add9")?"0 + 0":"0 - 0"));assertTrue(seen.contains(id.equals("add9")?"0 + 9":"9 - 0"));assertTrue(seen.contains(id.equals("add9")?"9 + 0":"9 - 9"));
        }
    }
    @Test public void theFullOneDigitMultiplicationDomainIncludesZeroAndOneInEitherPosition(){
        Set<String> seen=new HashSet<>();for(int i=0;i<2500;i++){
            Question q=next("tables");String[] parts=q.prompt.split(" × ");int a=Integer.parseInt(parts[0]),b=Integer.parseInt(parts[1]);assertTrue(a>=0&&a<=9&&b>=0&&b<=9);assertEquals(Integer.toString(a*b),q.answers[0]);seen.add(q.prompt);
        }
        assertEquals(100,seen.size());
    }
    @Test public void carryingIncludesBothOnePlusNineAndNinePlusOne(){
        Set<String> seen=new HashSet<>();for(int i=0;i<1800;i++){
            Question q=next("add20");String[] parts=q.prompt.split(" \\+ ");int a=Integer.parseInt(parts[0]),b=Integer.parseInt(parts[1]);assertTrue(a>=1&&a<=9&&b>=1&&b<=9&&a+b>=10);seen.add(q.prompt);
        }
        assertTrue(seen.contains("1 + 9"));assertTrue(seen.contains("9 + 1"));
    }
    @Test public void unlikeFractionsHaveDifferentVisibleDenominatorsAndChoiceMetadataMatchesThem(){
        Pattern fraction=Pattern.compile("(\\d+)/(\\d+)");
        for(String id:List.of("fracAdd","fracSub")){
            boolean sharesFactor=false,coprime=false;
            for(int i=0;i<1800;i++){
                Question q=next(id);Matcher m=fraction.matcher(q.prompt);assertTrue(m.find());int a=Integer.parseInt(m.group(1)),b=Integer.parseInt(m.group(2));assertTrue(m.find());int c=Integer.parseInt(m.group(1)),d=Integer.parseInt(m.group(2));assertFalse(m.find());
                assertNotEquals(q.prompt,b,d);assertTrue(a>0&&a<b&&c>0&&c<d&&b<=12&&d<=12);assertEquals(b,q.choiceInputs[3].intValue());assertEquals(d,q.choiceInputs[4].intValue());
                long numerator=id.equals("fracAdd")?(long)a*d+(long)c*b:(long)a*d-(long)c*b;assertTrue(numerator>=0);assertEquals(Rational.of(numerator,(long)b*d),Expression.number(q.answers[0]));
                sharesFactor|=Generator.gcd(b,d)>1;coprime|=Generator.gcd(b,d)==1;
            }
            assertTrue(sharesFactor);assertTrue(coprime);
        }
    }
    @Test public void mixedArithmeticHasAllFourOperationsAndSafeIntermediateDivisions(){
        Set<String> patterns=new HashSet<>();for(int i=0;i<1800;i++){
            Question q=next("mixed");String shape=q.prompt.replaceAll("\\d+","n");patterns.add(shape);Matcher numbers=Pattern.compile("\\d+").matcher(q.prompt);int[] values=new int[3];for(int n=0;n<3;n++){assertTrue(numbers.find());values[n]=Integer.parseInt(numbers.group());assertTrue(values[n]>=0&&values[n]<=111);}assertFalse(numbers.find());
            int a=values[0],b=values[1],c=values[2];
            if(shape.endsWith("÷ n")){
                assertTrue(c>0);if(shape.startsWith("(n + n)"))assertEquals(0,(a+b)%c);else if(shape.startsWith("(n - n)"))assertEquals(0,(a-b)%c);else if(shape.contains("×"))assertEquals(0,a*b%c);else assertEquals(0,b%c);
            }else if(shape.startsWith("n ÷ n")){assertTrue(b>0);assertEquals(0,a%b);}
            Rational answer=Expression.number(q.answers[0]);assertEquals(answer,Expression.number(q.prompt));assertTrue(answer.isInteger()&&answer.compareTo(Rational.ZERO)>=0&&answer.compareTo(Rational.of(351))<=0);
            if(!q.choices.isEmpty()){assertEquals(4,q.choices.size());for(String choice:q.choices){Rational v=Expression.number(choice);assertTrue(v.isInteger()&&v.compareTo(Rational.ZERO)>=0);}}
        }
        assertEquals(Set.of("n + n × n","(n + n) × n","n - n × n","(n - n) × n","n + n ÷ n","(n + n) ÷ n","n - n ÷ n","(n - n) ÷ n","n × n ÷ n","n ÷ n × n","n + n - n","n - n + n"),patterns);
    }
}
