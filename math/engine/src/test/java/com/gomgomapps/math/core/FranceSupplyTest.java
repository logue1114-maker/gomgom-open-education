package com.gomgomapps.math.core;

import java.util.*;
import java.util.regex.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class FranceSupplyTest {
    private static final String PACK="fr-men-cycles23-2024-2025-v1";
    @Test public void cpMissingOperandsProvideOneHundredDistinctRelationsWithoutLargerNumbers(){
        Generator g=new Generator(new Random(2026100808));
        for(String id:List.of("add9","sub9","add20","sub20")){
            Set<String> seen=new HashSet<>();List<String> recent=new ArrayList<>();Set<String> forms=new HashSet<>();
            CurriculumLimits limits=GlobalCurriculum.limits(PACK,id,1);
            for(int i=0;i<100;i++){
                Question q=g.next(id,recent,i%2==0,limits);assertTrue(q.prompt,seen.add(q.signature()));recent.add(q.signature());
                Matcher m=Pattern.compile("(\\d+|□) ([+-]) (\\d+|□)(?: = (\\d+))?").matcher(q.prompt);assertTrue(q.prompt,m.matches());
                long answer=Long.parseLong(q.answers[0]);long a=m.group(1).equals("□")?answer:Long.parseLong(m.group(1)),b=m.group(3).equals("□")?answer:Long.parseLong(m.group(3));
                long result=m.group(4)==null?answer:Long.parseLong(m.group(4));assertEquals(result,m.group(2).equals("+")?a+b:a-b);
                int bound=id.endsWith("9")?9:18;assertTrue(a>=0&&a<=bound&&b>=0&&b<=bound&&result>=0&&result<=bound);assertTrue(limits.allows(q));
                String form=m.group(1).equals("□")?"first":m.group(3).equals("□")?"second":"result";forms.add(form);
                if(!form.equals("result")){assertNotNull(q.studyGuide);assertFalse(q.studyGuide.transfer);}
                if(!q.choices.isEmpty()){assertTrue(q.correctChoice>=0&&q.correctChoice<q.choices.size());assertEquals(Expression.number(q.answers[0]),Expression.number(q.choices.get(q.correctChoice)));}
            }
            assertEquals(Set.of("first","second","result"),forms);
        }
    }
    @Test public void ce1SameDenominatorSupplyDoesNotRepeatAvailablePublicExpressions(){
        Generator g=new Generator(new Random(2026100809));
        for(String id:List.of("fracAddLike","fracSubLike")){
            List<String> recent=new ArrayList<>();Set<String> seen=new HashSet<>();CurriculumLimits limits=GlobalCurriculum.limits(PACK,id,2);
            for(int i=0;i<100;i++){
                Question q=g.next(id,recent,i%2==0,limits);assertTrue(q.prompt,seen.add(q.signature()));recent.add(q.signature());
                Matcher m=Pattern.compile("\\((\\d+)/(\\d+)\\) ([+-]) \\((\\d+)/(\\d+)\\)").matcher(q.prompt);assertTrue(m.matches());
                long a=Long.parseLong(m.group(1)),d=Long.parseLong(m.group(2)),b=Long.parseLong(m.group(4));assertEquals(d,Long.parseLong(m.group(5)));
                assertTrue(Set.of(2L,3L,4L,5L,6L,8L,10L).contains(d));assertTrue(a>0&&a<d&&b>0&&b<d);
                long n=m.group(3).equals("+")?a+b:a-b;assertTrue(n>=0&&n<=d);assertEquals(Rational.of(n,d),Expression.number(q.answers[0]));assertTrue(limits.allows(q));
                assertEquals(Rational.of(d),q.choiceInputs[3]);assertEquals(Rational.of(d),q.choiceInputs[4]);
            }
        }
    }
    @Test public void cm1DivisionKeepsFactBoundsAndOffersMissingDividendAndDivisor(){
        Generator g=new Generator(new Random(2026100810));CurriculumLimits limits=GlobalCurriculum.limits(PACK,"divide",4);
        Set<String> seen=new HashSet<>(),forms=new HashSet<>();List<String> recent=new ArrayList<>();
        for(int i=0;i<100;i++){
            Question q=g.next("divide",recent,i%2==0,limits);assertTrue(q.prompt,seen.add(q.signature()));recent.add(q.signature());
            Matcher m=Pattern.compile("(\\d+|□) ÷ (\\d+|□)(?: = (\\d+))?").matcher(q.prompt);assertTrue(q.prompt,m.matches());long answer=Long.parseLong(q.answers[0]);
            long dividend=m.group(1).equals("□")?answer:Long.parseLong(m.group(1)),divisor=m.group(2).equals("□")?answer:Long.parseLong(m.group(2)),quotient=m.group(3)==null?answer:Long.parseLong(m.group(3));
            assertEquals(dividend,divisor*quotient);assertTrue(dividend<=81&&divisor>=2&&divisor<=9&&quotient>=1&&quotient<=9);assertTrue(limits.allows(q));forms.add(m.group(1).equals("□")?"dividend":m.group(2).equals("□")?"divisor":"quotient");
            if(q.prompt.contains("□")){assertNotNull(q.studyGuide);assertFalse(q.studyGuide.transfer);}
        }
        assertEquals(Set.of("dividend","divisor","quotient"),forms);
    }
    @Test public void sameDenominatorEnumerationIsOptedInAndLegacyScopesAreUnchanged(){
        assertFalse(FractionSupply.supports("fracAddLike",CurriculumLimits.NONE));assertFalse(FractionSupply.supports("fracSubLike",CurriculumLimits.NONE));
        assertTrue(FractionSupply.supports("fracAddLike",GlobalCurriculum.limits(PACK,"fracAddLike",2)));
        assertFalse(GlobalCurriculum.limits(PACK,"add100",1).variedSums());
    }
}
