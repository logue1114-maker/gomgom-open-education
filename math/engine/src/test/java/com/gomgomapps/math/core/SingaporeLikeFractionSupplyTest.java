package com.gomgomapps.math.core;

import java.util.*;
import java.util.regex.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class SingaporeLikeFractionSupplyTest {
    private static final String SYSTEM="sg-moe-primary-2021-v1";
    private static final Pattern PAIR=Pattern.compile("\\((\\d+)/(\\d+)\\) ([+-]) \\((\\d+)/(\\d+)\\)");

    @Test public void selectedSupplyExhaustsVisiblePairsBeforeRepeatingAndKeepsBlankHelp(){
        for(String id:List.of("fracAddLike","fracSubLike")){
            CurriculumLimits limits=GlobalCurriculum.limits(SYSTEM,id,2);
            assertTrue(FractionSupply.supports(id,limits));
            Generator generator=new Generator(new Random(2026100956L));
            List<String> recent=new ArrayList<>();Set<String> expressions=new HashSet<>();
            boolean denominator12=false,unreduced=false,boundary=false;
            for(int i=0;i<286;i++){
                Question q=generator.next(id,recent,i%2==0,limits);
                assertTrue("duplicate visible expression: "+q.prompt,expressions.add(q.prompt));
                assertFalse(recent.contains(q.signature()));recent.add(q.signature());
                Matcher m=PAIR.matcher(q.prompt);assertTrue(q.prompt,m.matches());
                int a=Integer.parseInt(m.group(1)),d=Integer.parseInt(m.group(2));
                int b=Integer.parseInt(m.group(4)),e=Integer.parseInt(m.group(5));
                assertEquals(d,e);assertTrue(d>=2&&d<=12);assertTrue(a>0&&a<d&&b>0&&b<d);
                int numerator=id.equals("fracAddLike")?a+b:a-b;
                assertTrue(numerator>=0&&numerator<=d);
                Rational result=Rational.of(numerator,d);
                assertEquals(result,Expression.number(q.answers[0]));
                assertTrue(new Checker().check(q,List.of(),List.of(result.toString())).correct());
                assertFalse(new Checker().check(q,List.of(),List.of(result.add(Rational.ONE).toString())).correct());
                HelpPlan plan=HelpPlan.forQuestion(q);assertNotNull(plan);assertFalse(plan.canTransfer());
                List<String> values=List.of(""+a,""+b,""+d,""+numerator);
                for(int step=0;step<4;step++){
                    assertTrue(plan.step(step).accepts(values.get(step)));
                    assertFalse(plan.step(step).before.matches(".*[0-9].*"));
                    assertEquals("",plan.step(step).after);
                }
                assertTrue(limits.allows(q));
                denominator12|=d==12;unreduced|=Rational.of(a,d).d.intValue()!=d;
                boundary|=numerator==(id.equals("fracAddLike")?d:0);
                if(!q.choices.isEmpty()){
                    assertEquals(q.choices.size(),new HashSet<>(q.choices).size());
                    assertEquals(result,Expression.number(q.choices.get(q.correctChoice)));
                }
            }
            assertTrue(denominator12&&unreduced&&boundary);
            for(int i=0;i<20;i++){
                Question repeated=generator.next(id,recent,false,limits);
                assertEquals(recent.get(i),repeated.signature());recent.add(repeated.signature());
            }
        }
    }

    @Test public void existingOtherCountryRulesRemainUnchanged(){
        assertFalse(FractionSupply.supports("fracAddLike",GlobalCurriculum.limits("jp-mext-primary-2017-v1","fracAddLike",3)));
        assertFalse(FractionSupply.supports("fracSubLike",GlobalCurriculum.limits("na-nied-primary-2024-v1","fracSubLike",4)));
        // Higher grades reviewing this Primary 2 skill inherit its latest level rule.
        assertTrue(GlobalCurriculum.limits(SYSTEM,"fracAddLike",3).variedFacts());
    }
}
