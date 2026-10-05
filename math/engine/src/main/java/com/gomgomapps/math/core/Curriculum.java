package com.gomgomapps.math.core;

import java.time.LocalDate;
import java.util.*;

/** Placement of the currently supported calculation types; not whole-curriculum coverage. */
public final class Curriculum {
    private Curriculum(){}
    public static final int MAPPING_VERSION=3;
    public static int schoolYear(LocalDate date){return date.getYear()-(date.getMonthValue()<3?1:0);}
    public static int suggestedVersion(int grade,int schoolYear){
        int start=grade<=2?2024:grade<=4?2025:grade<=6?2026:grade<=9?2025+grade-7:2025+grade-10;
        return schoolYear>=start?2022:2015;
    }
    public static int version(Learning.Profile p){
        return p.curriculum==2015||p.curriculum==2022?p.curriculum:suggestedVersion(p.grade,p.schoolYear);
    }
    public static void restoreProfile(Learning.Profile p){if(p.schoolYear==0)p.schoolYear=schoolYear(LocalDate.now());}
    public static String course(Catalog.Skill skill,int version){
        if(version!=2015)return skill.course;
        return switch(skill.course){case "공통수학 1","공통수학 2"->"수학";case "대수"->"수학 Ⅰ";case "미적분 Ⅰ"->"수학 Ⅱ";case "미적분 Ⅱ"->"미적분";default->skill.course;};
    }
    /** Version-specific additions stay available for chosen practice, but never enter older-course diagnosis. */
    public static boolean inCurriculum(Catalog.Skill skill,int version){
        // Common recommended grades are not a reviewed Korean placement.
        // Keep these Kenya additions available for explicitly chosen practice.
        if(ClockNotation.supports(skill.id)||ClockReadings.supports(skill.id)||DotCollections.supports(skill.id)||MetricConversions.added(skill.id)||LargePlaceFoundations.supports(skill.id)||MassDensity.supports(skill.id)||MotionFoundations.supports(skill.id)||MoneyFoundations.supports(skill.id)||ErrorFoundations.supports(skill.id)||GradientFoundations.supports(skill.id))return false;
        if(version==2015)return !(skill.id.startsWith("sec_matrix_")||Set.of("vectorPlane","vectorLine","sampleProportion","boxplotRead","boxplotCompare").contains(skill.id));
        return !Set.of("circularPermutation","spaceExternalSection").contains(skill.id);
    }
    public static boolean learnedCourse(Learning.Profile p,Catalog.Skill skill){
        // Old per-course choices stay exact; a partial old common-math choice never becomes both courses.
        return p.learnedCourses.contains(skill.course)||p.learnedCourses.contains(course(skill,version(p)));
    }
    public static List<String> courses(int version){
        Set<String> names=new LinkedHashSet<>();for(Catalog.Skill skill:Catalog.ALL)if(skill.grade>=10)names.add(course(skill,version));return new ArrayList<>(names);
    }
    public static int grade(Catalog.Skill skill,int version){return version==2015&&Set.of("median","sec_mode").contains(skill.id)?9:skill.grade;}
    public static int unit(Catalog.Skill skill,int version){if(version==2015&&skill.course.equals("기하"))return skill.unit==2?3:skill.unit==3?2:skill.unit;return skill.unit;}
    public static int order(Catalog.Skill skill,int version){return grade(skill,version)*100+skill.term*10+unit(skill,version);}
    public static String level(Catalog.Skill skill,int version){
        int grade=grade(skill,version);
        return grade==0?"수 세기와 비교":grade<=6?"초등 "+grade+"학년 · "+skill.term+"학기":grade<=9?"중등 "+(grade-6)+"학년 · "+skill.term+"학기":course(skill,version);
    }
}
