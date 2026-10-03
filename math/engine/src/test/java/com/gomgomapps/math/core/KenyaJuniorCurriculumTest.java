package com.gomgomapps.math.core;

import java.util.*;
import java.util.regex.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class KenyaJuniorCurriculumTest {
    private static final String KE="ke-kicd-cbc-2024-v1";
    private Learning.Profile profile(){Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"KE");GlobalCurriculum.choosePack(p,KE);p.grade=7;return p;}
    @Test public void currentGradeStudyDoesNotEnterPriorGradeDiagnosis(){
        Learning.State s=new Learning.State();s.profile=profile();s.profile.currentSkill="fracMul";
        assertTrue(GlobalCurriculum.pack(s.profile).inGrade("fracMul",7));
        assertTrue(GlobalCurriculum.available(s.profile).stream().anyMatch(x->x.id.equals("fracMul")));
        Diagnosis.begin(s,new Random(710),true);
        for(String id:List.of("fracMul","linear","root","sec_prime_factor"))assertFalse(s.session.diagnosticRun.plan.scope.contains(id));
        assertEquals(Integer.valueOf(6),s.session.diagnosticRun.plan.placements.get("largePlace"));
        assertFalse(GlobalCurriculum.pack(s.profile).inGrade("linearInequality",7));
    }
    @Test public void nineDigitPlaceValuesAndRoundingSolveFromPublicGivens(){
        Generator g=new Generator(new Random(711));Checker checker=new Checker();
        Map<String,Integer> names=Map.of("일",0,"십",1,"백",2,"천",3,"만",4,"십만",5,"백만",6,"천만",7,"억",8);
        Set<Integer> queried=new HashSet<>(),units=new HashSet<>();boolean reachesBillion=false;
        for(int i=0;i<1000;i++){
            Question place=g.next("largePlace",List.of(),false,GlobalCurriculum.limits(KE,"largePlace",7));
            Matcher m=Pattern.compile("^(\\d+)에서 (.+)의 자리").matcher(place.prompt);assertTrue(m.find());
            long value=Long.parseLong(m.group(1));int p=names.get(m.group(2));queried.add(p);assertTrue(value>=100000000&&value<=999999999);
            long digit=value/(long)Math.pow(10,p)%10;
            assertTrue(checker.check(place,List.of(),List.of(String.valueOf(digit))).correct());
            assertFalse(checker.check(place,List.of(),List.of(String.valueOf((digit+1)%10))).correct());
            Question round=g.next("el_round",List.of(),false,GlobalCurriculum.limits(KE,"el_round",7));
            m=Pattern.compile("^(\\d+)을 (\\d+)의 자리").matcher(round.prompt);assertTrue(m.find());
            long n=Long.parseLong(m.group(1)),unit=Long.parseLong(m.group(2));units.add((int)unit);
            long answer=(n+unit/2)/unit*unit;reachesBillion|=answer==1000000000L;
            assertTrue(checker.check(round,List.of(),List.of(String.valueOf(answer))).correct());
            assertFalse(checker.check(round,List.of(),List.of(String.valueOf(answer+1))).correct());
        }
        assertEquals(new HashSet<>(names.values()),queried);assertEquals(Set.of(10,100,1000,10000,100000,1000000,10000000,100000000),units);assertTrue(reachesBillion);
    }
    @Test public void polygonsStopAtHexagonsWithoutRestrictingOtherCountries(){
        Generator g=new Generator(new Random(712));boolean unrestrictedLarge=false;
        for(String id:List.of("sec_polygon_interior","sec_polygon_exterior"))for(int i=0;i<300;i++){
            Question q=g.next(id,List.of(),false,GlobalCurriculum.limits(KE,id,7));Matcher m=Pattern.compile("(\\d+)각형").matcher(q.prompt);assertTrue(m.find());int sides=Integer.parseInt(m.group(1));assertTrue(sides>=3&&sides<=6);
            int answer=id.endsWith("interior")?(sides-2)*180:360/sides;
            assertTrue(new Checker().check(q,List.of(),List.of(String.valueOf(answer))).correct());
            q=g.next(id,List.of(),false);m=Pattern.compile("(\\d+)각형").matcher(q.prompt);assertTrue(m.find());unrestrictedLarge|=Integer.parseInt(m.group(1))>6;
        }
        assertTrue(unrestrictedLarge);
        for(String rule:List.of("polygonSides=2","polygonSides=21","wholeMaximum=1000000000","roundingUnits=1000000000")){
            try{new CurriculumLimits(rule);fail(rule);}catch(IllegalArgumentException expected){}
        }
    }
    @Test public void savedSelectedGradeRetainsJuniorLimitsAfterProfileChanges(){
        Learning.State s=new Learning.State();s.profile=profile();
        Learning.beginPractice(s,"practice",List.of("largePlace"),10,false,new Random(713),Map.of("largePlace",7));
        s.profile.grade=4;Generator g=new Generator(new Random(714));Question q=Learning.ensureQuestion(s,g);
        assertTrue(Long.parseLong(q.prompt.split("에서")[0])>=100000000);
        assertEquals(Integer.valueOf(7),s.session.selectedGrades.get("largePlace"));
        assertEquals(99999,GlobalCurriculum.limits(KE,"largePlace",4).wholeMaximum(99999));
    }
}
