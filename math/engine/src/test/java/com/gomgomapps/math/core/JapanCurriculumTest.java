package com.gomgomapps.math.core;

import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class JapanCurriculumTest {
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
