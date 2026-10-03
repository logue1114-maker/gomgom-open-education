package com.gomgomapps.math.core;

import org.junit.Test;
import static org.junit.Assert.*;
import java.time.LocalDate;
import java.util.*;

public class CurriculumTest {
    @Test public void cohortAndMarchSchoolYearBoundaryDetermineTheSuggestedVersion(){
        assertEquals(2026,Curriculum.schoolYear(LocalDate.of(2027,2,28)));assertEquals(2027,Curriculum.schoolYear(LocalDate.of(2027,3,1)));
        assertEquals(2022,Curriculum.suggestedVersion(6,2026));assertEquals(2022,Curriculum.suggestedVersion(8,2026));assertEquals(2015,Curriculum.suggestedVersion(9,2026));assertEquals(2022,Curriculum.suggestedVersion(11,2026));assertEquals(2015,Curriculum.suggestedVersion(12,2026));assertEquals(2022,Curriculum.suggestedVersion(12,2027));
    }
    @Test public void courseLabelsAndKnownPlacementDifferencesFollowTheSelectedCurriculum(){
        assertEquals(List.of("수학","수학 Ⅰ","수학 Ⅱ","확률과 통계","미적분","기하"),Curriculum.courses(2015));assertTrue(Curriculum.courses(2022).contains("대수"));
        assertEquals(9,Curriculum.grade(Catalog.get("median"),2015));assertEquals(7,Curriculum.grade(Catalog.get("median"),2022));assertEquals(8,Curriculum.grade(Catalog.get("pythagoras"),2015));assertEquals(8,Curriculum.grade(Catalog.get("pythagoras"),2022));
    }
    @Test public void currentUnitExclusionOverridesCompletedCourseCheckbox(){
        Learning.Profile p=new Learning.Profile();p.grade=11;p.schoolYear=2026;p.currentSkill="arithmeticSeq";p.learnedCourses.add("대수");Set<String> ids=ids(p);
        assertTrue(ids.contains("log"));assertTrue(ids.contains("negativePower"));assertFalse(ids.contains("arithmeticSeq"));assertFalse(ids.contains("geometricSeq"));assertFalse(ids.contains("limit"));
        assertTrue(Learning.learningScope(p).stream().anyMatch(s->s.id.equals("arithmeticSeq")));
    }
    @Test public void oldCommonMathCourseRespectsTheCurrentUnitAcrossBothTerms(){
        Learning.Profile p=new Learning.Profile();p.grade=12;p.schoolYear=2026;p.currentSkill="function";p.learnedCourses.add("수학");Set<String> ids=ids(p);
        assertTrue(ids.contains("combination"));assertTrue(ids.contains("setCount"));assertFalse(ids.contains("function"));assertFalse(ids.contains("compose"));
    }
    @Test public void anOldPartialCourseChoiceDoesNotSilentlyWidenToBothCommonCourses(){
        Learning.Profile p=new Learning.Profile();p.grade=12;p.schoolYear=2026;p.learnedCourses.add("공통수학 1");Set<String> ids=ids(p);
        assertTrue(ids.contains("combination"));assertFalse(ids.contains("setCount"));assertFalse(ids.contains("function"));
    }
    @Test public void gradeSemesterAndExplicitExclusionsContinueToConstrainTheScope(){
        Learning.Profile p=new Learning.Profile();p.grade=8;p.schoolYear=2026;p.term=1;assertTrue(ids(p).contains("median"));p.curriculum=2015;assertFalse(ids(p).contains("median"));
        p.grade=9;p.term=1;assertTrue(ids(p).contains("pythagoras"));assertFalse(ids(p).contains("root"));p.excluded.add("pythagoras");assertFalse(ids(p).contains("pythagoras"));
    }
    private Set<String> ids(Learning.Profile p){Set<String> ids=new HashSet<>();for(Catalog.Skill skill:Learning.diagnosticScope(p))ids.add(skill.id);return ids;}
}
