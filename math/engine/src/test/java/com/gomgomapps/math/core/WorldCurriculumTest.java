package com.gomgomapps.math.core;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;
import java.util.regex.*;

public class WorldCurriculumTest {
    private static final String US="us-ccss-2010-v1";
    private static final String ZA="za-caps-r12-2011-v2";
    private Learning.Profile profile(){Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"US");GlobalCurriculum.choosePack(p,US);return p;}
    @Test public void highSchoolBandNeverPretendsAnAdvancedTopicWasTaughtInGradeNine(){
        Learning.Profile p=profile();p.grade=10;p.currentSkill="quadratic";
        GlobalCurriculum.Pack pack=GlobalCurriculum.pack(p);assertEquals(12,pack.grade("complexMultiply"));
        assertTrue(pack.inGrade("complexMultiply",9));assertTrue(pack.inGrade("complexMultiply",12));assertFalse(pack.inGrade("complexMultiply",8));
        assertFalse(GlobalCurriculum.scope(p).stream().anyMatch(s->s.id.equals("complexMultiply")));
        p.learnedSkills.add("complexMultiply");assertTrue(GlobalCurriculum.scope(p).stream().anyMatch(s->s.id.equals("complexMultiply")));
    }
    @Test public void kindergartenIsARealSelectableLevel(){
        GlobalCurriculum.Pack pack=GlobalCurriculum.pack(profile());assertEquals("K",pack.level(0));assertEquals(0,pack.minGrade());assertEquals(12,pack.maxGrade());assertTrue(pack.inGrade("join9",0));
    }
    @Test public void ccssThirdGradeFractionsUseOnlyTheSpecifiedDenominators(){
        Generator generator=new Generator(new Random(819));Set<Integer> allowed=Set.of(2,3,4,6,8);Set<Integer> observed=new HashSet<>();
        for(int i=0;i<400;i++){
            Question q=generator.next("fractionPart",List.of(),true,GlobalCurriculum.limits(US,"fractionPart"));
            int denominator=Integer.parseInt(q.expression.split("/")[1]);assertTrue(q.prompt,allowed.contains(denominator));observed.add(denominator);
            for(String choice:q.choices)if(choice.contains("/"))assertTrue(allowed.contains(Integer.parseInt(choice.split("/")[1])));
        }
        assertEquals(allowed,observed);
    }
    @Test public void additionStaysWithinOneThousandIncludingItsResult(){
        Generator generator=new Generator(new Random(951));
        for(int i=0;i<400;i++){Question q=generator.next("add1000",List.of(),true,GlobalCurriculum.limits(US,"add1000"));assertTrue(Expression.number(q.answers[0]).compareTo(Rational.of(1000))<=0);for(String choice:q.choices){Rational value=Expression.number(choice);assertTrue(value.compareTo(Rational.of(0))>=0&&value.compareTo(Rational.of(1000))<=0);}}
    }
    @Test public void denominatorLimitAlsoChecksTheBlankOutputFraction(){
        CurriculumLimits limits=new CurriculumLimits("denominators=2,3,4,6,8");
        assertFalse(limits.allows(new Question("el_fraction_common_den","1/3 → □/15","1*5","5")));
        assertTrue(limits.allows(new Question("el_fraction_common_den","1/3 → □/6","1*2","2")));
    }
    @Test public void everyMappedAmericanFoundationCanGenerateRealQuestions(){
        Generator generator=new Generator(new Random(6003));
        for(Catalog.Skill skill:GlobalCurriculum.available(profile()))for(int i=0;i<20;i++){
            Question q=generator.next(skill.id,List.of(),i%2==0,GlobalCurriculum.limits(US,skill.id));assertNotNull(q);assertEquals(skill.id,q.skillId);assertTrue(q.answers.length>0);
            if(!q.choices.isEmpty())assertTrue(skill.id,new Checker().check(q,List.of(),List.of(q.choices.get(q.correctChoice))).correct());
        }
    }
    @Test public void normalSessionUsesItsOwnCurriculumAfterCountrySwitch(){
        Learning.State state=new Learning.State();state.profile=profile();state.profile.grade=3;
        Learning.beginPractice(state,"practice",List.of("fractionPart"),20,false,new Random(6));
        GlobalCurriculum.chooseCountry(state.profile,"KR");
        Question q=Learning.ensureQuestion(state,new Generator(new Random(918)));
        assertEquals(US,state.session.educationSystem);assertTrue(Set.of(2,3,4,6,8).contains(Integer.parseInt(q.expression.split("/")[1])));
    }
    @Test(expected=IllegalArgumentException.class)public void invalidLimitsCannotSilentlyBecomeUnrestricted(){new CurriculumLimits("maxReslt=100");}
    @Test public void oldKoreanSessionsRemainUnrestricted(){assertSame(CurriculumLimits.NONE,GlobalCurriculum.limits(null,"fractionPart"));assertSame(CurriculumLimits.NONE,GlobalCurriculum.limits("kr-national","fractionPart"));}
    private Learning.Profile southAfrica(int grade){Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"ZA");GlobalCurriculum.choosePack(p,ZA);p.grade=grade;return p;}
    @Test public void capsTablesWidenAtTheSpecifiedGradeAndIncludeTenAndTwelve(){
        Generator generator=new Generator(new Random(810));
        for(int grade=2;grade<=4;grade++){
            boolean sawTen=false,sawTwelve=false,sawNonEarlyTable=false;
            for(int i=0;i<600;i++){
                Question q=generator.next("tables",List.of(),true,GlobalCurriculum.limits(ZA,"tables",grade));
                String[] parts=q.expression.split(" × ");int a=Integer.parseInt(parts[0]),b=Integer.parseInt(parts[1]);
                assertTrue(a>0&&b>0);sawTen|=a==10||b==10;sawTwelve|=a==12||b==12;
                if(grade==2){assertTrue(Set.of(2,3,4,5).contains(a)||Set.of(2,3,4,5).contains(b));assertTrue(a*b<=50&&a<=10&&b<=10);}
                if(grade==3){assertTrue(Set.of(2,3,4,5,10).contains(a)||Set.of(2,3,4,5,10).contains(b));assertTrue(a*b<=100);}
                sawNonEarlyTable|=!Set.of(2,3,4,5).contains(a)&&!Set.of(2,3,4,5).contains(b);
            }
            assertTrue(sawTen);assertEquals(grade==4,sawTwelve);assertEquals(grade>=3,sawNonEarlyTable);
        }
    }
    @Test public void capsGradeTwoHasUnitFractionsAndGradeThreeIntroducesOtherNumerators(){
        Generator generator=new Generator(new Random(533));boolean sawNonUnit=false;
        for(int i=0;i<300;i++){
            Question early=generator.next("fractionPart",List.of(),true,GlobalCurriculum.limits(ZA,"fractionPart",2));
            String[] fraction=early.expression.split("/");assertEquals("1",fraction[0]);assertTrue(Set.of("2","3","4","5").contains(fraction[1]));
            Question later=generator.next("fractionPart",List.of(),true,GlobalCurriculum.limits(ZA,"fractionPart",3));sawNonUnit|=!later.expression.startsWith("1/");
        }
        assertTrue(sawNonUnit);
    }
    @Test public void diagnosticUsesMostRecentCompletedGradeWithoutCurrentGradeRules(){
        Learning.State state=new Learning.State();state.profile=southAfrica(3);state.profile.currentSkill="place100";
        Diagnosis.begin(state,new Random(33),true);Diagnosis.Plan plan=state.session.diagnosticRun.plan;
        assertEquals(Integer.valueOf(2),plan.placements.get("tables"));assertFalse(plan.scope.contains("divide"));
        state.profile.grade=4;Diagnosis.begin(state,new Random(34),true);
        assertEquals(Integer.valueOf(3),state.session.diagnosticRun.plan.placements.get("tables"));
        assertTrue(state.session.diagnosticRun.plan.scope.contains("divide"));
    }
    @Test public void everySouthAfricanPlacementGeneratesWithinItsOwnGrade(){
        Generator generator=new Generator(new Random(1515));GlobalCurriculum.Pack pack=GlobalCurriculum.pack(southAfrica(1));
        assertEquals(13,pack.levels().size());assertEquals("Grade R",pack.level(0));
        for(String skill:pack.grades.keySet())for(GlobalCurriculum.Placement placement:pack.placements(skill))for(int i=0;i<25;i++){
            CurriculumLimits limits=GlobalCurriculum.limits(ZA,skill,placement.through());Question q=generator.next(skill,List.of(),i%2==0,limits);
            assertTrue(skill+"/"+placement.through(),limits.allows(q));
            if(!q.choices.isEmpty())assertTrue(skill,new Checker().check(q,List.of(),List.of(q.choices.get(q.correctChoice))).correct());
        }
    }
    @Test public void archivedCurriculumRemainsReadableForSavedSessions(){
        Learning.Profile p=southAfrica(4);assertEquals(1,GlobalCurriculum.packs("ZA").size());
        p.educationSystem="za-caps-intermediate-2011-v1";assertNotNull(GlobalCurriculum.pack(p));assertEquals(4,GlobalCurriculum.pack(p).grade("tables"));
    }
    @Test public void capsQuadraticsDoNotIntroduceIrrationalRootsBeforeGradeEleven(){
        Generator generator=new Generator(new Random(963));boolean laterRadical=false;
        for(int grade=9;grade<=11;grade++)for(int i=0;i<150;i++){
            Question q=generator.next("quadratic",List.of(),false,GlobalCurriculum.limits(ZA,"quadratic",grade));
            for(String answer:q.answers){
                if(grade<11){Rational value=Expression.number(answer);if(grade==9)assertEquals(java.math.BigInteger.ONE,value.d);}
                else try{Expression.number(answer);}catch(RuntimeException e){laterRadical=true;}
            }
        }
        assertTrue(laterRadical);
    }
    @Test public void everyRwandanPlacementGeneratesWithinItsOwnGrade(){
        String system="rw-cbc-core-2015-2022-v1";Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"RW");GlobalCurriculum.choosePack(p,system);
        GlobalCurriculum.Pack pack=GlobalCurriculum.pack(p);assertEquals("P1",pack.level(1));assertEquals("S6",pack.level(12));
        Generator generator=new Generator(new Random(783));
        for(String skill:pack.grades.keySet())for(GlobalCurriculum.Placement placement:pack.placements(skill))for(int i=0;i<25;i++){
            CurriculumLimits limits=GlobalCurriculum.limits(system,skill,placement.through());Question q=generator.next(skill,List.of(),i%2==0,limits);
            assertTrue(skill,limits.allows(q));if(!q.choices.isEmpty())assertTrue(skill,new Checker().check(q,List.of(),List.of(q.choices.get(q.correctChoice))).correct());
        }
        assertEquals(1,pack.grade("divide"));assertEquals(5,pack.grade("signedAdd"));assertEquals(10,pack.grade("vectorDot"));assertFalse(pack.grades.containsKey("normalProbability"));
    }
    @Test public void rwandanFirstGradeUsesOnlyHalvesQuartersAndTheTwoTimesTable(){
        String system="rw-cbc-core-2015-2022-v1";Generator generator=new Generator(new Random(431));
        for(int i=0;i<250;i++){
            Question fraction=generator.next("fractionPart",List.of(),true,GlobalCurriculum.limits(system,"fractionPart",1));assertTrue(Set.of("1/2","1/4").contains(fraction.expression));
            Question table=generator.next("tables",List.of(),true,GlobalCurriculum.limits(system,"tables",1));assertTrue(table.expression.startsWith("2 × ")||table.expression.endsWith(" × 2"));assertTrue(Expression.number(table.answers[0]).compareTo(Rational.of(20))<=0);
        }
    }
}
