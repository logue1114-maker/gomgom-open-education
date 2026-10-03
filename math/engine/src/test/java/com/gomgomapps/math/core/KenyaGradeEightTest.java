package com.gomgomapps.math.core;

import java.util.*;
import java.util.regex.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class KenyaGradeEightTest {
    private static final String KE="ke-kicd-cbc-2024-v1";
    private Learning.Profile profile(int grade){
        Learning.Profile p=new Learning.Profile();
        GlobalCurriculum.chooseCountry(p,"KE");GlobalCurriculum.choosePack(p,KE);p.grade=grade;return p;
    }
    @Test public void currentGradeNumbersDoNotBecomePreviouslyLearnedDiagnosis(){
        Learning.State s=new Learning.State();s.profile=profile(8);s.profile.currentSkill="signedAdd";
        Diagnosis.begin(s,new Random(820),true);
        for(String id:List.of("signedAdd","el_ratio_terms","el_ratio_fraction","proportion","el_proportional_split"))
            assertFalse(id,s.session.diagnosticRun.plan.scope.contains(id));
        assertEquals(Integer.valueOf(7),s.session.diagnosticRun.plan.placements.get("fracAdd"));
        assertEquals(Integer.valueOf(7),s.session.diagnosticRun.plan.placements.get("rootFraction"));
        assertTrue(GlobalCurriculum.scope(profile(9)).stream().anyMatch(x->x.id.equals("signedAdd")));
        assertFalse(GlobalCurriculum.pack(s.profile).inGrade("signedMul",8));
        assertFalse(GlobalCurriculum.pack(s.profile).inGrade("rational",8));
    }
    @Test public void signedArithmeticAndDirectProportionsUseIndependentPublicOperands(){
        Generator g=new Generator(new Random(821));Checker checker=new Checker();
        boolean negative=false,subtraction=false;
        for(int i=0;i<1000;i++){
            Question q=g.next("signedAdd",List.of(),false,GlobalCurriculum.limits(KE,"signedAdd",8));
            Matcher m=Pattern.compile("^\\(?(-?\\d+)\\)? ([+-]) \\(?(-?\\d+)\\)?$").matcher(q.prompt);
            assertTrue(q.prompt,m.matches());int a=Integer.parseInt(m.group(1)),b=Integer.parseInt(m.group(3));
            int answer=m.group(2).equals("+")?a+b:a-b;negative|=a<0||b<0;subtraction|=m.group(2).equals("-");
            assertTrue(checker.check(q,List.of(),List.of(String.valueOf(answer))).correct());
            assertFalse(checker.check(q,List.of(),List.of(String.valueOf(answer+1))).correct());
            q=g.next("proportion",List.of(),false,GlobalCurriculum.limits(KE,"proportion",8));
            m=Pattern.compile("^(\\d+) : (\\d+) = (\\d+) : x").matcher(q.prompt);assertTrue(q.prompt,m.find());
            int x=Integer.parseInt(m.group(2))*Integer.parseInt(m.group(3))/Integer.parseInt(m.group(1));
            assertTrue(checker.check(q,List.of(),List.of(String.valueOf(x))).correct());
            assertFalse(checker.check(q,List.of(),List.of(String.valueOf(x+1))).correct());
        }
        assertTrue(negative&&subtraction);
    }
    @Test public void savedGradeEightStudyKeepsItsCountryAndPlacement(){
        Learning.State s=new Learning.State();s.profile=profile(8);
        Learning.beginPractice(s,"practice",List.of("signedAdd"),100,false,new Random(822),Map.of("signedAdd",8));
        GlobalCurriculum.chooseCountry(s.profile,"KR");s.profile.grade=1;
        Question q=Learning.ensureQuestion(s,new Generator(new Random(823)));
        assertEquals("signedAdd",q.skillId);assertEquals(KE,s.session.educationSystem);
        assertEquals(Integer.valueOf(8),s.session.selectedGrades.get("signedAdd"));
        assertEquals(8,s.session.curriculumGrade);
    }
    @Test public void ratioFoundationsHaveOneHundredDifferentPromptsAndCorrectPublicAnswers(){
        Generator g=new Generator(new Random(824));Checker checker=new Checker();
        for(String id:List.of("el_ratio_terms","el_ratio_fraction")){
            LinkedList<String> recent=new LinkedList<>();Set<String> prompts=new HashSet<>();boolean twoDigits=false;
            for(int i=0;i<100;i++){
                Question q=g.next(id,recent,false,GlobalCurriculum.limits(KE,id,8));
                Matcher m=Pattern.compile("\\d+").matcher(q.prompt);assertTrue(m.find());int a=Integer.parseInt(m.group());
                assertTrue(m.find());int b=Integer.parseInt(m.group());assertTrue(a>=1&&a<=30&&b>=1&&b<=30);
                twoDigits|=a>9||b>9;
                String answer=id.equals("el_ratio_terms")?String.valueOf(a+b):a+"/"+b;
                if(id.equals("el_ratio_fraction"))assertTrue(a<b);
                assertTrue(checker.check(q,List.of(),List.of(answer)).correct());
                assertFalse(checker.check(q,List.of(),List.of("999999")).correct());
                prompts.add(q.prompt);recent.add(q.signature());
                assertNotNull(q.studyGuide);
            }
            assertEquals(id,100,prompts.size());assertTrue(twoDigits);
        }
    }
}
