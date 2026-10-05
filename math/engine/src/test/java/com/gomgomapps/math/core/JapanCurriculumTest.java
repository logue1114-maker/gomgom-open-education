package com.gomgomapps.math.core;

import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class JapanCurriculumTest {
    @Test public void extendedPrimaryOperandsKeepBothThreeFourDigitsAndIntegerMultipliers(){
        Generator generator=new Generator(new Random(2026100571));String pack="jp-mext-primary-2017-v1";
        for(String id:List.of("add1000","sub1000")){
            Set<Integer> lengths=new HashSet<>();CurriculumLimits limits=GlobalCurriculum.limits(pack,id,3);
            for(int i=0;i<500;i++){
                Question q=generator.next(id,List.of(),i%2==0,limits);
                java.util.regex.Matcher operands=java.util.regex.Pattern.compile("(\\d+)\\s*[+-]\\s*(\\d+)").matcher(q.expression);assertTrue(operands.matches());
                for(int k=1;k<=2;k++){int length=operands.group(k).length();assertTrue(length==3||length==4);lengths.add(length);}
                assertTrue(Expression.number(q.answers[0]).compareTo(Rational.ZERO)>=0);
            }
            assertEquals(Set.of(3,4),lengths);
        }
        CurriculumLimits integer=GlobalCurriculum.limits(pack,"decimalMul",4);boolean fractionalLeft=false,fractionalGrade5Right=false;
        for(int i=0;i<500;i++){
            Question q=generator.next("decimalMul",List.of(),i%2==0,integer);assertTrue(integer.allows(q));
            assertEquals(java.math.BigInteger.ONE,q.choiceInputs[1].d);fractionalLeft|=!q.choiceInputs[0].d.equals(java.math.BigInteger.ONE);
            HelpPlan help=HelpPlan.forQuestion(q);assertNotNull(help);assertEquals(3,help.size());assertFalse(help.canTransfer());
            if(!q.choices.isEmpty()){boolean integerAnswer=Expression.number(q.answers[0]).isInteger();assertTrue(q.choices.stream().filter(c->Expression.number(c).isInteger()==integerAnswer).count()>=2);}
            for(int step=0;step<help.size();step++){String expected=q.studyGuide.frames.get(step).expected;assertTrue(help.step(step).accepts(expected));assertFalse(help.step(step).accepts(Expression.number(expected).add(Rational.ONE).toString()));}
            Question later=generator.next("decimalMul",List.of(),false,GlobalCurriculum.limits(pack,"decimalMul",5));fractionalGrade5Right|=!later.choiceInputs[1].d.equals(java.math.BigInteger.ONE);
        }
        assertTrue(fractionalLeft);assertTrue("Grade5 still permits decimal multipliers",fractionalGrade5Right);
        assertFalse(integer.allows(new Question("decimalMul","2.3 * 1.2","2.3 * 1.2","2.76")));
        try{new CurriculumLimits("wholeDigits=3;minimumWholeDigits=4");fail("Invalid digit range must fail early");}catch(IllegalArgumentException expected){}
    }
    private Learning.Profile profile(){Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"JP");GlobalCurriculum.choosePack(p,"jp-mext-primary-2017-v1");return p;}
    @Test public void foreignGradeAndLearnedScopeKeepFractionProductsOutOfEarlierDiagnosis(){
        Learning.Profile p=profile();p.grade=3;GlobalCurriculum.Pack pack=GlobalCurriculum.pack(p);
        assertEquals(List.of(1,2,3,4,5,6),pack.levels());assertFalse(pack.inGrade("fracMul",5));assertTrue(pack.inGrade("fracMul",6));
        assertFalse(pack.grades.containsKey("el_prime"));assertFalse(pack.grades.containsKey("parallelPerpendicularGradient"));
        assertTrue(Learning.diagnosticScope(p).contains(Catalog.get("tables")));assertFalse(Learning.diagnosticScope(p).contains(Catalog.get("decimalAdd")));
        p.learnedSkills.add("decimalAdd");assertTrue(Learning.diagnosticScope(p).contains(Catalog.get("decimalAdd")));
        p.grade=6;assertFalse(Learning.diagnosticScope(p).contains(Catalog.get("fracMul")));
        p.grade=7;assertTrue(Learning.diagnosticScope(p).contains(Catalog.get("fracMul")));
    }
    @Test public void registeredArithmeticObeysJapaneseSelectedDigitAndSignRanges(){
        Generator generator=new Generator(new Random(2026100551));
        for(int i=0;i<100;i++){
            Question q=generator.next("add20",List.of(),false,GlobalCurriculum.limits("jp-mext-primary-2017-v1","add20",1));
            assertTrue("Grade1 includes carry across ten",Expression.number(q.answers[0]).compareTo(Rational.of(10))>=0);
        }
        for(String id:List.of("decimalAdd","decimalSub","el_decimal_place","el_decimal_compare")){
            CurriculumLimits limits=GlobalCurriculum.limits("jp-mext-primary-2017-v1",id,3);
            for(int i=0;i<300;i++){
                Question q=generator.next(id,List.of(),i%2==0,limits);assertTrue(limits.allows(q));
                for(Rational input:q.choiceInputs)assertEquals("Grade3 uses tenths",0,Rational.of(10).mul(input).d.compareTo(java.math.BigInteger.ONE));
            }
        }
        for(String id:List.of("fracSubLike","fracSub","substitute"))for(int i=0;i<200;i++){
            int grade=id.equals("fracSubLike")?3:id.equals("fracSub")?5:6;
            Question q=generator.next(id,List.of(),false,GlobalCurriculum.limits("jp-mext-primary-2017-v1",id,grade));
            assertTrue("Selected primary answers are nonnegative",Expression.number(q.answers[0]).compareTo(Rational.ZERO)>=0);
            if(id.equals("fracSubLike"))for(int operand=0;operand<2;operand++)assertTrue(q.choiceInputs[operand].compareTo(Rational.ONE)<=0);
        }
    }
}
