package com.gomgomapps.math.core;

import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class GradeSevenFactorListTest {
    private static List<String> solve(String id,int given){
        List<String> result=new ArrayList<>();
        if(CompleteFactorPractice.FACTORS.equals(id)){
            for(int divisor=1;divisor<=given;divisor++)if(given%divisor==0)result.add(""+divisor);
        }else for(int multiplier=1;multiplier<=5;multiplier++)result.add(""+(given*multiplier));
        return result;
    }

    @Test public void gradeSevenNormalGeneratorExhaustsSelectedPositiveGivensWithoutEarlyRepeat(){
        String pack="na-nied-primary-2024-v1";
        Learning.Profile profile=new Learning.Profile();
        GlobalCurriculum.chooseCountry(profile,"NA");GlobalCurriculum.choosePack(profile,pack);
        for(String id:List.of(CompleteFactorPractice.FACTORS,CompleteFactorPractice.MULTIPLES)){
            assertTrue(GlobalCurriculum.pack(profile).inGrade(id,7));
            CurriculumLimits limits=GlobalCurriculum.limits(pack,id,7);
            Generator generator=new Generator(new Random(337));
            List<String> recent=new ArrayList<>();Set<Integer> givens=new HashSet<>();
            for(int i=0;i<99;i++){
                Question question=generator.next(id,recent,false,limits);
                assertTrue(limits.allows(question));
                int given=CompleteFactorPractice.read(question)[0];
                assertTrue(given>=1&&given<100);assertTrue(givens.add(given));
                assertFalse(recent.contains(question.signature()));recent.add(question.signature());
                List<String> answer=solve(id,given);
                if(CompleteFactorPractice.MULTIPLES.equals(id))assertTrue(Integer.parseInt(answer.get(4))<=495);
                question.answers=new String[]{"poison"};question.expression="poison";
                assertTrue(new Checker().check(question,List.of(),answer).correct());
            }
            assertTrue(givens.contains(1)&&givens.contains(99));
            assertEquals(recent.get(0),generator.next(id,recent,false,limits).signature());
            assertFalse(limits.allows(CompleteFactorPractice.make(id,100,0)));
        }
    }

    @Test public void gradeSixAndEnglandKeepTheirExistingUpperRangesAndPositiveListMeaning(){
        String na="na-nied-primary-2024-v1",en="england-primary-2021-v1";
        assertTrue(GlobalCurriculum.limits(na,CompleteFactorPractice.FACTORS,6).allows(
            CompleteFactorPractice.make(CompleteFactorPractice.FACTORS,100,0)));
        assertTrue(GlobalCurriculum.limits(en,CompleteFactorPractice.MULTIPLES,5).allows(
            CompleteFactorPractice.make(CompleteFactorPractice.MULTIPLES,144,0)));
        assertTrue(GlobalCurriculum.limits(en,CompleteFactorPractice.MULTIPLES,5).allows(
            CompleteFactorPractice.make(CompleteFactorPractice.MULTIPLES,1,0)));
        Question one=CompleteFactorPractice.make(CompleteFactorPractice.MULTIPLES,1,0);
        assertTrue(new Checker().check(one,List.of(),List.of("1","2","3","4","5")).correct());
        assertFalse(new Checker().check(one,List.of(),List.of("0","1","2","3","4")).correct());
        assertTrue(new Checker().check(CompleteFactorPractice.make(CompleteFactorPractice.FACTORS,1,0),
            List.of(),List.of("1")).correct());
    }
}
