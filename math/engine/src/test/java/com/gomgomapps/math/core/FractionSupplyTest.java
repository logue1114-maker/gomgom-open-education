package com.gomgomapps.math.core;

import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class FractionSupplyTest {
    @Test public void singaporeArithmeticExhaustsEightyFactsAndDecimalSuppliesOneHundredUniqueTasks(){
        Generator g=new Generator(new Random(20261005211L));
        for(String id:List.of("fracAdd","fracSub","el_fraction_decimal")){
            List<String> recent=new ArrayList<>();CurriculumLimits limits=GlobalCurriculum.limits("sg-moe-primary-2021-v1",id,id.startsWith("el_")?4:3);
            for(int i=0;i<100;i++){
                Question q=g.next(id,recent,i%2==0,limits);if(id.startsWith("el_")||i<80)assertFalse(recent.contains(q.signature()));else assertEquals(recent.get(i-80),q.signature());recent.add(q.signature());assertEquals(Expression.number(q.expression),Expression.number(q.answers[0]));
                assertTrue(new Checker().check(q,List.of(),List.of(q.answers[0])).correct());assertTrue(limits.allows(q));
                if(id.startsWith("el_")){assertEquals("decimal",q.answerFormat);assertNotNull(q.diagram);assertNotNull(HelpPlan.forQuestion(q));assertTrue(HelpPlan.forQuestion(q).step(2).accepts(q.answers[0]));}
                else{assertEquals(5,q.choiceInputs.length);assertNotEquals(q.choiceInputs[0].d,q.choiceInputs[1].d);}
                if(!q.choices.isEmpty()){assertEquals(q.choices.size(),new HashSet<>(q.choices).size());assertEquals(q.answers[0],q.choices.get(q.correctChoice));}
            }
        }
    }
    @Test public void representationTasksRepeatOnlyAfterTheirFiniteDomainAndCountryLimitsRemain(){
        Generator g=new Generator(new Random(20261005212L));List<String> recent=new ArrayList<>();
        for(int i=0;i<66;i++){Question q=g.next("fractionPart",recent,false);assertFalse(recent.contains(q.signature()));recent.add(q.signature());String[] givens=q.prompt.replaceAll("[^0-9]+"," ").trim().split(" ");assertEquals(Rational.of(Integer.parseInt(givens[1]),Integer.parseInt(givens[0])).toString(),q.answers[0]);}
        assertEquals(recent.get(0),g.next("fractionPart",recent,false).signature());
        recent.clear();CurriculumLimits limits=GlobalCurriculum.limits("ke-kicd-cbc-2024-v1","fractionPart",2);
        for(int i=0;i<2;i++){Question q=g.next("fractionPart",recent,false,limits);assertFalse(recent.contains(q.signature()));recent.add(q.signature());assertTrue(Set.of("1/2","1/4").contains(q.answers[0]));}
        assertEquals(recent.get(0),g.next("fractionPart",recent,false,limits).signature());
        assertFalse(FractionSupply.supports("el_fraction_decimal",new CurriculumLimits("decimalPlaces=4")));
    }
}
