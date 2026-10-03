package com.gomgomapps.math.core;

import java.util.*;

/** Browsing groups, independent of learned/diagnostic scope. Students may select any grade. */
public final class TopicSelection {
    private TopicSelection(){}
    public record Group(String id,String label,List<Catalog.Skill> skills){}
    public static List<Group> groups(Learning.Profile profile,Collection<String> recommended,boolean homework){
        List<Group> result=new ArrayList<>();
        if(homework)result.add(new Group("recommended","추천 계산",Catalog.ALL.stream().filter(s->recommended.contains(s.id)).toList()));
        if(GlobalCurriculum.foreign(profile)){
            GlobalCurriculum.Pack pack=GlobalCurriculum.pack(profile);
            if(pack!=null){
                result.add(new Group("curriculum","교육과정 전체",GlobalCurriculum.available(profile)));
                for(int grade:pack.levels())result.add(new Group("grade:"+grade,pack.level(grade),GlobalCurriculum.available(profile).stream().filter(s->pack.inGrade(s.id,grade)).toList()));
            }
            result.add(new Group("all","전체 기초 계산",List.copyOf(Catalog.ALL)));
        }else{
            int version=Curriculum.version(profile);
            List<Catalog.Skill> ordered=new ArrayList<>(Catalog.ALL);ordered.sort(Comparator.comparingInt(s->Curriculum.order(s,version)));
            result.add(new Group("all","전체 계산",List.copyOf(ordered)));
            for(int g=1;g<=9;g++){int grade=g;result.add(new Group("grade:"+g,g<=6?"초등 "+g+"학년":"중등 "+(g-6)+"학년",ordered.stream().filter(s->Curriculum.grade(s,version)==grade).toList()));}
            for(String course:Curriculum.courses(version))result.add(new Group("course:"+course,course,ordered.stream().filter(s->Curriculum.course(s,version).equals(course)).toList()));
        }
        return List.copyOf(result);
    }
    public static int selectedGrade(Learning.Profile profile,Group group,String skill){
        if(group.id.startsWith("grade:"))return Integer.parseInt(group.id.substring(6));
        GlobalCurriculum.Pack pack=GlobalCurriculum.foreign(profile)?GlobalCurriculum.pack(profile):null;
        if(pack==null||pack.placements(skill).isEmpty()||pack.inGrade(skill,profile.grade))return profile.grade;
        int previous=pack.completedBefore(skill,profile.grade);
        return previous>=0?previous:pack.placements(skill).stream().mapToInt(GlobalCurriculum.Placement::from).min().orElse(profile.grade);
    }
    public static int initial(List<Group> groups,Learning.Profile profile,boolean homework){
        String id=homework?"recommended":"grade:"+profile.grade;
        for(int i=0;i<groups.size();i++)if(groups.get(i).id.equals(id))return i;
        return 0;
    }
}
