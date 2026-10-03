package com.gomgomapps.math.core;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;
import java.util.regex.*;
import java.math.BigDecimal;

public class KenyaUpperCurriculumTest {
    private static final String KE="ke-kicd-cbc-2024-v1";
    private Learning.Profile profile(int grade){Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"KE");GlobalCurriculum.choosePack(p,KE);p.grade=grade;return p;}
    @Test public void onlySupportedOutcomesEnterTheGrade(){
        GlobalCurriculum.Pack pack=GlobalCurriculum.pack(profile(4));
        assertTrue(pack.inGrade("el_decimal_place",4));assertFalse(pack.inGrade("decimalAdd",4));
        assertFalse(pack.inGrade("fracAdd",4));assertFalse(pack.inGrade("fracMul",5));assertFalse(pack.inGrade("fracMul",6));
        assertFalse(pack.inGrade("el_circle_area",6));assertFalse(pack.inGrade("el_quadrilateral_angle_sum",6));
        assertFalse(pack.inGrade("el_rect_prism_volume",4));assertTrue(pack.inGrade("el_rect_prism_volume",5));
        assertFalse(pack.inGrade("el_capacity_l_ml",4));assertTrue(pack.inGrade("el_capacity_l_ml",5));
        Learning.State s=new Learning.State();s.profile=profile(4);s.profile.currentSkill="el_decimal_place";
        Diagnosis.begin(s,new Random(29),true);assertFalse(s.session.diagnosticRun.plan.scope.contains("el_decimal_place"));
        assertEquals(Integer.valueOf(3),s.session.diagnosticRun.plan.placements.get("add1000"));
    }
    @Test public void decimalPlaceDigitsComeFromTheDisplayedNumber(){
        Generator generator=new Generator(new Random(411));
        for(int grade=4;grade<=6;grade++){
            int highest=0;Set<Integer> queriedPlaces=new HashSet<>();
            for(int i=0;i<600;i++){
                Question q=generator.next("el_decimal_place",List.of(),false,GlobalCurriculum.limits(KE,"el_decimal_place",grade));
                Matcher m=Pattern.compile("^(\\d+\\.\\d+)에서 (\\d+)분의 1").matcher(q.prompt);assertTrue(q.prompt,m.find());
                BigDecimal value=new BigDecimal(m.group(1));int place=Integer.parseInt(m.group(2));queriedPlaces.add(place);highest=Math.max(highest,value.scale());
                assertTrue(value.scale()<=grade-2);int expected=value.multiply(BigDecimal.valueOf(place)).intValue()%10;
                assertTrue(new Checker().check(q,List.of(),List.of(String.valueOf(expected))).correct());
                assertFalse(new Checker().check(q,List.of(),List.of(String.valueOf((expected+1)%10))).correct());
            }
            assertEquals(grade-2,highest);assertTrue(queriedPlaces.contains((int)Math.pow(10,grade-2)));
        }
    }
    @Test public void decimalComparisonAndArithmeticReachTheirAllowedPrecision(){
        Generator generator=new Generator(new Random(533));
        for(int grade=4;grade<=6;grade++)for(String id:grade==4?List.of("el_decimal_compare"):List.of("el_decimal_compare","decimalAdd","decimalSub")){
            int highest=0;
            for(int i=0;i<600;i++){
                Question q=generator.next(id,List.of(),false,GlobalCurriculum.limits(KE,id,grade));
                Matcher m=Pattern.compile("\\d+(?:\\.\\d+)?").matcher(id.equals("el_decimal_compare")?q.prompt:q.expression);
                assertTrue(m.find());BigDecimal a=new BigDecimal(m.group());assertTrue(m.find());BigDecimal b=new BigDecimal(m.group());
                highest=Math.max(highest,Math.max(a.scale(),b.scale()));assertTrue(a.scale()<=grade-2&&b.scale()<=grade-2);
                String expected=id.equals("el_decimal_compare")?(a.compareTo(b)==0?"=":a.compareTo(b)>0?">":"<"):(id.equals("decimalAdd")?a.add(b):a.subtract(b)).toPlainString();
                assertTrue(new Checker().check(q,List.of(),List.of(expected)).correct());
            }
            assertEquals(grade-2,highest);
        }
    }
    @Test public void roundingUsesOnlyTheOfficialPlacesAndPublicOperand(){
        Generator generator=new Generator(new Random(83));
        for(int grade=4;grade<=6;grade++){
            Set<Integer> observed=new HashSet<>(),allowed=grade==4?Set.of(10):grade==5?Set.of(100,1000):Set.of(1000);
            for(int i=0;i<500;i++){
                Question q=generator.next("el_round",List.of(),false,GlobalCurriculum.limits(KE,"el_round",grade));
                Matcher m=Pattern.compile("^(\\d+)을 (\\d+)의 자리까지").matcher(q.prompt);assertTrue(m.find());int number=Integer.parseInt(m.group(1)),unit=Integer.parseInt(m.group(2));
                assertTrue(allowed.contains(unit));observed.add(unit);if(grade==4)assertTrue(number<=1000);
                long expected=((number+unit/2)/unit)*(long)unit;
                assertTrue(new Checker().check(q,List.of(),List.of(String.valueOf(expected))).correct());
            }
            assertEquals(allowed,observed);
        }
    }
    @Test public void oneDenominatorRenamingStopsAtGradeFive(){
        Generator generator=new Generator(new Random(401));boolean unrelatedAtSix=false;
        for(int grade:List.of(5,6))for(String id:List.of("fracAdd","fracSub"))for(int i=0;i<500;i++){
            Question q=generator.next(id,List.of(),false,GlobalCurriculum.limits(KE,id,grade));
            Matcher m=Pattern.compile("(\\d+)/(\\d+)").matcher(q.expression);assertTrue(m.find());long a=Long.parseLong(m.group(1)),b=Long.parseLong(m.group(2));assertTrue(m.find());long c=Long.parseLong(m.group(1)),d=Long.parseLong(m.group(2));
            boolean related=b%d==0||d%b==0;assertTrue(b<=12&&d<=12);if(grade==5)assertTrue(related);else unrelatedAtSix|=!related;
            long numerator=id.equals("fracAdd")?a*d+c*b:a*d-c*b;assertTrue(numerator>=0);
            assertTrue(new Checker().check(q,List.of(),List.of(numerator+"/"+(b*d))).correct());
        }
        assertTrue(unrelatedAtSix);
    }
    @Test public void earlierFractionAndRegroupingRulesStayWithSavedSessions(){
        Learning.State s=new Learning.State();s.profile=profile(3);Learning.beginPractice(s,"practice",List.of("fractionPart"),10,false,new Random(11),Map.of("fractionPart",3));
        s.profile.grade=6;Question q=Learning.ensureQuestion(s,new Generator(new Random(91)));assertEquals("1",q.expression.split("/")[0]);assertTrue(Set.of("2","4","8").contains(q.expression.split("/")[1]));
        Question carry=new Question("add1000","199 + 111","199 + 111","310");
        assertFalse(GlobalCurriculum.limits(KE,"add1000",3).allows(carry));assertTrue(GlobalCurriculum.limits(KE,"add1000",4).allows(carry));
    }
    @Test public void malformedAndOutOfGradeLimitsAreRejected(){
        for(String rule:List.of("decimalPlaces=0","decimalPlaces=5","roundingUnits=1","roundingUnits=10,10")){
            try{new CurriculumLimits(rule);fail(rule);}catch(IllegalArgumentException expected){}
        }
        assertFalse(GlobalCurriculum.limits(KE,"el_decimal_place",4).allows(new Question("el_decimal_place","1.234에서 1000분의 1의 자리 숫자는?","","4")));
        assertFalse(GlobalCurriculum.limits(KE,"el_round",4).allows(new Question("el_round","450을 100의 자리까지 반올림하면?","450-50+100","500")));
    }
}
