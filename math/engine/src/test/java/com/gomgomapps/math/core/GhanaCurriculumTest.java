package com.gomgomapps.math.core;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;
import java.util.regex.*;

public class GhanaCurriculumTest {
    private static final String GH="gh-nacca-core-2019-2023-v1";
    private Learning.Profile profile(int grade){Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"GH");GlobalCurriculum.choosePack(p,GH);p.grade=grade;return p;}
    @Test public void actualLevelsAndCoreScopeDoNotBorrowKoreanOrElectiveGradeAssignments(){
        GlobalCurriculum.Pack pack=GlobalCurriculum.pack(profile(1));assertEquals(12,pack.levels().size());
        assertEquals("B1",pack.level(1));assertEquals("JHS1 (B7)",pack.level(7));assertEquals("SHS3",pack.level(12));
        assertEquals(3,pack.grade("fractionPart"));assertEquals(3,pack.grade("tables"));assertEquals(12,pack.grade("quadratic"));
        assertEquals(7,pack.grade("el_shape_angle"));assertEquals(8,pack.grade("angles"));
        assertEquals(8,pack.grade("el_triangle_angle_sum"));assertEquals(8,pack.grade("el_quadrilateral_angle_sum"));
        for(String id:List.of("derivative","integral","complexMultiply","binomial","geometricSeries"))assertFalse(id,pack.grades.containsKey(id));
        Learning.State state=new Learning.State();state.profile=profile(3);state.profile.currentSkill="add1000";
        Diagnosis.begin(state,new Random(307),true);Diagnosis.Plan plan=state.session.diagnosticRun.plan;
        assertFalse(plan.scope.contains("fractionPart"));assertFalse(plan.scope.contains("tables"));assertFalse(plan.scope.contains("add1000"));
        assertEquals(Integer.valueOf(2),plan.placements.get("el_missing_add"));
        state.profile.grade=4;state.profile.currentSkill="mul3";Diagnosis.begin(state,new Random(308),true);
        assertEquals(Integer.valueOf(3),state.session.diagnosticRun.plan.placements.get("tables"));
    }
    @Test public void everyGhanaPlacementHasQuestionsAndAValidAnswerInBothInputModes(){
        Generator generator=new Generator(new Random(100219));GlobalCurriculum.Pack pack=GlobalCurriculum.pack(profile(1));
        for(int grade:pack.levels())for(String id:pack.grades.keySet())if(pack.inGrade(id,grade))for(int i=0;i<30;i++){
            CurriculumLimits limits=GlobalCurriculum.limits(GH,id,grade);Question q=generator.next(id,List.of(),i%2==0,limits);
            assertEquals(id,q.skillId);assertTrue(id+"/"+grade,limits.allows(q));assertTrue(q.answers.length>0);
            if(!q.choices.isEmpty()){
                assertEquals(id,q.choices.size(),new HashSet<>(q.choices).size());
                assertTrue(id,new Checker().check(q,List.of(),List.of(q.choices.get(q.correctChoice))).correct());
            }
        }
    }
    @Test public void primaryBlankCalculationsAndGroupingRespectPublicGivens(){
        Generator generator=new Generator(new Random(770));Set<Integer> totals=new HashSet<>();Set<String> signatures=new HashSet<>();
        for(int i=0;i<500;i++){
            for(String id:List.of("el_missing_add","el_missing_sub")){
                Question q=generator.next(id,List.of(),i%2==0,GlobalCurriculum.limits(GH,id,1));
                Matcher m=Pattern.compile("\\d+").matcher(q.prompt);while(m.find())assertTrue(q.prompt,Integer.parseInt(m.group())<=20);
                assertTrue(Integer.parseInt(q.answers[0])<=20);signatures.add(q.signature());
                String filled=q.prompt.replace("□",q.answers[0]);String[] sides=filled.split(" = ");
                String[] terms=sides[0].split(" [+-] ");int left=Integer.parseInt(terms[0]),right=Integer.parseInt(terms[1]);
                assertEquals(q.prompt,sides[0].contains(" + ")?left+right:left-right,Integer.parseInt(sides[1]));
                if(id.equals("el_missing_sub"))totals.add(Integer.parseInt(filled.split(" - ")[0]));
            }
            Question q=generator.next("mulIntro",List.of(),true,GlobalCurriculum.limits(GH,"mulIntro",3));
            int each,groups;Matcher grouping=Pattern.compile("(\\d+)씩 (\\d+)묶음.*").matcher(q.prompt);
            if(grouping.matches()){each=Integer.parseInt(grouping.group(1));groups=Integer.parseInt(grouping.group(2));}
            else{String[] terms=q.prompt.split(" = ")[0].split(" \\+ ");each=Integer.parseInt(terms[0]);groups=terms.length;for(String term:terms)assertEquals(each,Integer.parseInt(term));}
            assertTrue(each<=5&&groups<=5);assertTrue(each*groups<=25);
            Question t=generator.next("tables",List.of(),true,GlobalCurriculum.limits(GH,"tables",3));String[] f=t.expression.split(" × ");
            assertTrue(Integer.parseInt(f[0])<=5&&Integer.parseInt(f[1])<=5);
        }
        assertTrue(totals.size()>12);assertTrue(signatures.size()>150);
    }
    @Test public void subtractionAndPerfectSquareLimitsWidenOnlyAtTheirOwnLevel(){
        Generator generator=new Generator(new Random(331));boolean laterNegativeSubtraction=false,laterSurd=false;
        for(int i=0;i<250;i++){
            Question early=generator.next("signedAdd",List.of(),false,GlobalCurriculum.limits(GH,"signedAdd",6));assertFalse(early.expression.contains(" - (-"));
            Question later=generator.next("signedAdd",List.of(),false,GlobalCurriculum.limits(GH,"signedAdd",7));laterNegativeSubtraction|=later.expression.contains(" - (-");
            Question root=generator.next("root",List.of(),false,GlobalCurriculum.limits(GH,"root",8));assertTrue(Expression.number(root.answers[0]).isInteger());
            Question surd=generator.next("rootSimplify",List.of(),false,GlobalCurriculum.limits(GH,"rootSimplify",9));laterSurd|=surd.answers[0].contains("√");
        }
        assertTrue(laterNegativeSubtraction);assertTrue(laterSurd);
    }
    @Test public void aSavedSessionKeepsItsGhanaGradeRulesAfterChangingCountry(){
        Learning.State state=new Learning.State();state.profile=profile(8);
        Learning.beginPractice(state,"practice",List.of("tables"),100,true,new Random(512),Map.of("tables",3));
        GlobalCurriculum.chooseCountry(state.profile,"KR");Question q=Learning.ensureQuestion(state,new Generator(new Random(17)));
        assertEquals(GH,state.session.educationSystem);assertEquals("GH",state.session.countryCode);
        String[] f=q.expression.split(" × ");assertTrue(Integer.parseInt(f[0])<=5&&Integer.parseInt(f[1])<=5);
    }
}
