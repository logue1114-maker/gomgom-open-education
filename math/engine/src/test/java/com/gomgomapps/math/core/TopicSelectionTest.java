package com.gomgomapps.math.core;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;

public class TopicSelectionTest {
    private Learning.Profile american(){Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"US");GlobalCurriculum.choosePack(p,"us-ccss-2010-v1");p.grade=8;return p;}
    private Set<String> ids(TopicSelection.Group group){Set<String> result=new HashSet<>();for(Catalog.Skill s:group.skills())result.add(s.id);return result;}
    @Test public void foreignBrowseUsesMappedGradesIncludingKindergartenAndHighSchoolBands(){
        Learning.Profile p=american();List<TopicSelection.Group> groups=TopicSelection.groups(p,List.of(),false);
        assertEquals("grade:8",groups.get(TopicSelection.initial(groups,p,false)).id());
        GlobalCurriculum.Pack pack=GlobalCurriculum.pack(p);
        for(int grade:pack.levels()){
            TopicSelection.Group group=groups.stream().filter(g->g.id().equals("grade:"+grade)).findFirst().orElseThrow();
            Set<String> expected=new HashSet<>();for(Catalog.Skill s:Catalog.ALL)if(pack.inGrade(s.id,grade))expected.add(s.id);
            assertEquals(expected,ids(group));
        }
        assertTrue(groups.stream().anyMatch(g->g.label().equals("K")));
        assertTrue(ids(groups.stream().filter(g->g.id().equals("grade:9")).findFirst().orElseThrow()).contains("complexMultiply"));
        assertTrue(ids(groups.stream().filter(g->g.id().equals("grade:12")).findFirst().orElseThrow()).contains("complexMultiply"));
    }
    @Test public void browsingNeverBlocksOtherGradesOrUnmappedFoundations(){
        Learning.Profile p=american();List<TopicSelection.Group> groups=TopicSelection.groups(p,List.of(),false);
        assertEquals(Catalog.ALL.size(),groups.stream().filter(g->g.id().equals("all")).findFirst().orElseThrow().skills().size());
        assertTrue(groups.stream().anyMatch(g->g.id().equals("grade:12")));assertTrue(p.learnedSkills.isEmpty());
    }
    @Test public void homeworkSuggestionsAndCommonFoundationsRemainReachable(){
        Learning.Profile p=american();List<TopicSelection.Group> groups=TopicSelection.groups(p,List.of("add20","quadratic"),true);
        assertEquals(Set.of("add20","quadratic"),ids(groups.get(TopicSelection.initial(groups,p,true))));
        GlobalCurriculum.chooseCountry(p,"KE");groups=TopicSelection.groups(p,List.of(),false);assertEquals(1,groups.size());assertEquals(Catalog.ALL.size(),groups.get(0).skills().size());
    }
    @Test public void koreanCoursesKeepTheirCurriculumAssignment(){
        Learning.Profile p=new Learning.Profile();p.grade=2;
        List<TopicSelection.Group> groups=TopicSelection.groups(p,List.of(),false);
        assertEquals("grade:2",groups.get(TopicSelection.initial(groups,p,false)).id());
        assertTrue(groups.stream().anyMatch(g->g.id().startsWith("course:")));
    }    @Test public void chosenGradeControlsQuestionsAndSurvivesSavedSnapshot()throws Exception{
        Learning.State state=new Learning.State();GlobalCurriculum.chooseCountry(state.profile,"ZA");GlobalCurriculum.choosePack(state.profile,"za-caps-r12-2011-v2");state.profile.grade=2;
        List<TopicSelection.Group> groups=TopicSelection.groups(state.profile,List.of(),false);
        TopicSelection.Group grade4=groups.stream().filter(g->g.id().equals("grade:4")).findFirst().orElseThrow();
        assertEquals(4,TopicSelection.selectedGrade(state.profile,grade4,"tables"));
        Learning.beginPractice(state,"practice",List.of("tables"),100,false,new Random(83),Map.of("tables",4));
        Learning.State snapshot=LearningSnapshot.capture(state);state.session.selectedGrades.put("tables",2);
        java.io.ByteArrayOutputStream bytes=new java.io.ByteArrayOutputStream();new java.io.ObjectOutputStream(bytes).writeObject(snapshot);
        Learning.State restored=(Learning.State)new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bytes.toByteArray())).readObject();
        assertEquals(Integer.valueOf(4),restored.session.selectedGrades.get("tables"));assertEquals(2,restored.profile.grade);
        Generator generator=new Generator(new Random(536));boolean sawTwelve=false;
        for(int i=0;i<100;i++){
            Question q=Learning.ensureQuestion(restored,generator);String[] factors=q.expression.split(" × ");sawTwelve|=factors[0].equals("12")||factors[1].equals("12");
            Learning.finishQuestion(restored,false,java.time.LocalDate.now(),new Random(i));
        }
        assertTrue(sawTwelve);
    }

}
