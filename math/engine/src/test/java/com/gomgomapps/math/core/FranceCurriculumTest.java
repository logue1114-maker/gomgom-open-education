package com.gomgomapps.math.core;

import java.util.*;
import java.util.regex.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class FranceCurriculumTest {
    private static final String PACK="fr-men-cycles23-2024-2025-v1";
    private Learning.Profile profile(int grade){Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"FR");GlobalCurriculum.choosePack(p,PACK);p.grade=grade;return p;}
    @Test public void frenchLevelsAndEarlierDiagnosisFollowPublishedYears(){
        Learning.Profile p=profile(2);GlobalCurriculum.Pack pack=GlobalCurriculum.pack(p);
        assertEquals(List.of(1,2,3,4,5,6,7,8,9),pack.levels());
        assertEquals(List.of("CP","CE1","CE2","CM1","CM2","6e","5e","4e","3e"),pack.levels().stream().map(pack::level).toList());
        assertTrue(pack.inGrade("fractionPart",2));assertFalse(pack.inGrade("fractionPart",1));
        assertFalse(pack.inGrade("fracMul",7));assertTrue(pack.inGrade("fracMul",8));assertFalse(pack.grades.containsKey("quadratic"));
        assertFalse(Learning.diagnosticScope(p).contains(Catalog.get("fractionPart")));
        p.learnedSkills.add("fractionPart");assertTrue(Learning.diagnosticScope(p).contains(Catalog.get("fractionPart")));
        p.currentSkill="fractionPart";assertFalse(Learning.diagnosticScope(p).contains(Catalog.get("fractionPart")));
        assertTrue(Learning.learningScope(p).contains(Catalog.get("fractionPart")));
        Learning.Profile later=profile(3);assertTrue(Learning.diagnosticScope(later).contains(Catalog.get("fractionPart")));
        assertTrue(pack.placements("decimalDivInt").stream().anyMatch(v->v.from()==5&&v.reference().contains("effective for CM2 from2026")));
        assertTrue(pack.coverage.contains("ne sont pas encore disponibles"));
    }
    @Test public void ce1PartitionUsesTheDisplayedNumberOfPartsNotTheReducedAnswer(){
        CurriculumLimits limits=GlobalCurriculum.limits(PACK,"fractionPart",2);
        Question nine=new Question("fractionPart","전체를 똑같이 9조각으로 나눈 것 중 3조각을 분수로 나타내세요.","1/3","1/3");
        assertFalse("Reducing 3/9 to 1/3 does not turn a nine-part whole into three parts",limits.allows(nine));
        Generator g=new Generator(new Random(2026100801));Set<Integer> seen=new HashSet<>();Set<Integer> allowed=Set.of(2,3,4,5,6,8,10);
        for(int i=0;i<600;i++){
            Question q=g.next("fractionPart",List.of(),i%2==0,limits);
            assertNotNull(q.diagram);assertEquals("fractionSelection",q.diagram.type);int parts=(int)q.diagram.values[0],selected=Integer.bitCount((int)q.diagram.values[1]);
            assertTrue(allowed.contains(parts));seen.add(parts);assertTrue(selected>0&&selected<parts);
            assertEquals(Rational.of(selected,parts),Expression.number(q.answers[0]));assertTrue(limits.allows(q));
        }
        assertEquals(allowed,seen);
        // The unrestricted public generator retains the old2–12 domain.
        Set<Integer> unrestricted=new HashSet<>();for(int i=0;i<400;i++){Question q=g.next("fractionPart",List.of(),false);unrestricted.add(q.choiceInputs[1].intValue());}
        assertEquals(new HashSet<>(java.util.stream.IntStream.rangeClosed(2,12).boxed().toList()),unrestricted);
    }
    @Test public void sameDenominatorCe1AnswersStayInsideOneWhole(){
        Generator g=new Generator(new Random(2026100802));
        for(String id:List.of("fracAddLike","fracSubLike"))for(int i=0;i<200;i++){
            Question q=g.next(id,List.of(),i%2==0,GlobalCurriculum.limits(PACK,id,2));
            Matcher m=Pattern.compile("\\((\\d+)/(\\d+)\\) ([+-]) \\((\\d+)/(\\d+)\\)").matcher(q.expression);assertTrue(q.expression,m.matches());
            int den=Integer.parseInt(m.group(2));assertEquals(den,Integer.parseInt(m.group(5)));assertTrue(Set.of(2,3,4,5,6,8,10).contains(den));
            Rational a=Rational.of(Integer.parseInt(m.group(1)),den),b=Rational.of(Integer.parseInt(m.group(4)),den);
            Rational expected=m.group(3).equals("+")?a.add(b):a.sub(b);assertEquals(expected,Expression.number(q.answers[0]));assertTrue(expected.compareTo(Rational.ZERO)>=0&&expected.compareTo(Rational.ONE)<=0);
        }
    }
    @Test public void ce2RelatedDenominatorsDoNotCarryIntoLaterFractionCalculations(){
        Generator g=new Generator(new Random(2026100803));
        for(String id:List.of("fracAdd","fracSub")){
            Set<String> seen=new HashSet<>();List<String> recent=new ArrayList<>();boolean laterUnrelated=false,unreduced=false;
            for(int i=0;i<250;i++){
                Question q=g.next(id,recent,i%2==0,GlobalCurriculum.limits(PACK,id,3));
                Matcher publicFractions=Pattern.compile("\\((\\d+)/(\\d+)\\) ([+-]) \\((\\d+)/(\\d+)\\)").matcher(q.prompt);assertTrue(publicFractions.matches());
                int a=Integer.parseInt(publicFractions.group(2)),b=Integer.parseInt(publicFractions.group(5));assertNotEquals(a,b);assertTrue(a%b==0||b%a==0);
                Rational left=Rational.of(Integer.parseInt(publicFractions.group(1)),a),right=Rational.of(Integer.parseInt(publicFractions.group(4)),b);
                assertTrue(left.compareTo(Rational.ONE)<0&&right.compareTo(Rational.ONE)<0);assertEquals(id.equals("fracAdd")?left.add(right):left.sub(right),Expression.number(q.answers[0]));
                assertFalse("One hundred recent problems do not repeat",recent.contains(q.signature()));seen.add(q.signature());recent.add(q.signature());if(recent.size()>100)recent.remove(0);
                unreduced|=left.d.intValueExact()!=a||right.d.intValueExact()!=b;
                HelpPlan help=HelpPlan.forQuestion(q);assertNotNull(help);assertFalse(help.canTransfer());
                assertTrue(help.step(0).accepts(publicFractions.group(1)));assertTrue(help.step(1).accepts(publicFractions.group(2)));
                Question later=g.next(id,List.of(),false,GlobalCurriculum.limits(PACK,id,4));int c=later.choiceInputs[0].d.intValueExact(),d=later.choiceInputs[1].d.intValueExact();laterUnrelated|=c%d!=0&&d%c!=0;
            }
            assertTrue(seen.size()>=100);assertTrue("Original visible parts include equivalent unreduced fractions",unreduced);assertTrue("CM1 uses broader fraction calculation",laterUnrelated);
        }
    }
    @Test public void decimalIntegerMultiplierAndPrecisionChangeWithFrenchGrade(){
        Generator g=new Generator(new Random(2026100804));boolean cm2Thousandths=false,sixthFractionalFactor=false;
        for(int grade:List.of(4,5,6))for(int i=0;i<250;i++){
            Question q=g.next("decimalMul",List.of(),i%2==0,GlobalCurriculum.limits(PACK,"decimalMul",grade));
            assertTrue(GlobalCurriculum.limits(PACK,"decimalMul",grade).allows(q));
            Matcher decimals=Pattern.compile("\\d+\\.(\\d+)").matcher(q.expression);while(decimals.find())assertTrue(decimals.group(1).length()<=(grade==4?2:3));
            if(grade<6)assertTrue(q.choiceInputs[1].isInteger());else sixthFractionalFactor|=!q.choiceInputs[1].isInteger();
            if(grade==5)cm2Thousandths|=!q.choiceInputs[0].mul(Rational.of(100)).isInteger();
            assertEquals(q.choiceInputs[0].mul(q.choiceInputs[1]),Expression.number(q.answers[0]));
        }
        assertTrue(cm2Thousandths);assertTrue(sixthFractionalFactor);
    }
    @Test public void registeredFoundationsGenerateInsideTheMappedGradeLimits(){
        GlobalCurriculum.Pack pack=GlobalCurriculum.pack(profile(1));Generator g=new Generator(new Random(2026100805));
        for(String id:pack.grades.keySet())for(GlobalCurriculum.Placement placement:pack.placements(id)){
            CurriculumLimits limits=GlobalCurriculum.limits(PACK,id,placement.from());
            for(int i=0;i<12;i++){Question q=g.next(id,List.of(),i%2==0,limits);assertEquals(id,q.skillId);assertTrue(PACK+"/"+id+"/"+placement.from(),limits.allows(q));}
        }
    }
    @Test public void publicWholeNumberOperationsStayWithinTheFrenchNumberFields(){
        Generator g=new Generator(new Random(2026100806));int[] maxima={0,100,1000,10000,999999};
        for(int grade=1;grade<=4;grade++)for(String operation:List.of("add","sub")){
            String id=operation+(grade==1?"100":"1000");
            for(int i=0;i<150;i++){
                Question q=g.next(id,List.of(),false,GlobalCurriculum.limits(PACK,id,grade));
                Matcher m=Pattern.compile("(\\d+) ([+-]) (\\d+)").matcher(q.prompt);assertTrue(q.prompt,m.matches());
                long a=Long.parseLong(m.group(1)),b=Long.parseLong(m.group(3)),expected=m.group(2).equals("+")?a+b:a-b;
                assertTrue(a<=maxima[grade]&&b<=maxima[grade]&&expected>=0&&expected<=maxima[grade]);assertEquals(Rational.of(expected),Expression.number(q.answers[0]));
            }
        }
    }
}
