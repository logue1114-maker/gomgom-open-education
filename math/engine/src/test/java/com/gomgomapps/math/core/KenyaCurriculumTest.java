package com.gomgomapps.math.core;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;

public class KenyaCurriculumTest {
    private static final String KE="ke-kicd-cbc-2024-v1";
    private Learning.Profile profile(int grade){Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"KE");GlobalCurriculum.choosePack(p,KE);p.grade=grade;return p;}
    @Test public void explicitPrimaryCoverageAndPastGradesControlDiagnosis(){
        GlobalCurriculum.Pack pack=GlobalCurriculum.pack(profile(1));assertEquals(List.of(1,2,3,4,5,6,7,8,9),pack.levels());
        assertTrue(pack.coverage.contains("Grade 1–9"));assertTrue(pack.coverage.contains("senior school are not complete"));assertEquals("Grade 3",pack.level(3));
        assertFalse(pack.inGrade("el_length_m_cm",3));assertFalse(pack.inGrade("fracAdd",3));assertFalse(pack.grades.containsKey("earlySolids"));
        assertEquals(2,pack.grade("fractionPart"));assertEquals(3,pack.grade("el_clock_minute"));
        assertFalse(GlobalCurriculum.scope(profile(2)).stream().anyMatch(s->s.id.equals("fractionPart")));
        assertTrue(GlobalCurriculum.scope(profile(3)).stream().anyMatch(s->s.id.equals("fractionPart")));
        Learning.State state=new Learning.State();state.profile=profile(2);state.profile.currentSkill="tables";Diagnosis.begin(state,new Random(22),true);
        assertFalse(state.session.diagnosticRun.plan.scope.contains("tables"));assertFalse(state.session.diagnosticRun.plan.scope.contains("add1000"));
        assertEquals(Integer.valueOf(1),state.session.diagnosticRun.plan.placements.get("add100"));
        // An official primary pack does not replace unrestricted self-study for older learners.
        GlobalCurriculum.choosePack(state.profile,GlobalCurriculum.COMMON);assertTrue(GlobalCurriculum.available(state.profile).stream().anyMatch(s->s.id.equals("log")));
    }
    @Test public void eachActualPlacementGeneratesAndNumericPublicGivensSolveIndependently(){
        Generator generator=new Generator(new Random(2003));GlobalCurriculum.Pack pack=GlobalCurriculum.pack(profile(1));
        for(int grade:pack.levels())for(String id:pack.grades.keySet())if(pack.inGrade(id,grade))for(int i=0;i<100;i++){
            CurriculumLimits limits=GlobalCurriculum.limits(KE,id,grade);Question q=generator.next(id,List.of(),i%2==0,limits);
            assertTrue(id+"/"+grade,limits.allows(q));
            if(q.expression.matches("\\d+ [+-] \\d+")){
                String[] terms=q.expression.split(" ");long left=Long.parseLong(terms[0]),right=Long.parseLong(terms[2]);
                long answer=terms[1].equals("+")?left+right:left-right;
                assertTrue(new Checker().check(q,List.of(),List.of(String.valueOf(answer))).correct());
                assertFalse(new Checker().check(q,List.of(),List.of(String.valueOf(answer+1))).correct());
            }
            if(!q.choices.isEmpty()){assertEquals(q.choices.size(),new HashSet<>(q.choices).size());assertTrue(new Checker().check(q,List.of(),List.of(q.choices.get(q.correctChoice))).correct());}
        }
    }
    @Test public void regroupingAndFirstGradeOperandLimitsUseTheShownDirection(){
        Generator generator=new Generator(new Random(509));boolean laterCarry=false,borrow=false;
        for(int i=0;i<700;i++)for(String id:List.of("add100","sub100")){
            Question q=generator.next(id,List.of(),i%2==0,GlobalCurriculum.limits(KE,id,1));String[] parts=q.expression.split(" ");
            int left=Integer.parseInt(parts[0]),right=Integer.parseInt(parts[2]);assertTrue(left<=50&&right<=9);
            assertTrue(q.expression,id.equals("add100")?left%10+right<10:left%10>=right);
            if(id.equals("add100"))assertTrue(left+right<=50);
            Question later=generator.next(id,List.of(),false,GlobalCurriculum.limits(KE,id,2));String[] p=later.expression.split(" ");
            int a=Integer.parseInt(p[0]),b=Integer.parseInt(p[2]);laterCarry|=id.equals("add100")&&a%10+b%10>=10;borrow|=id.equals("sub100")&&a%10<b%10;
        }
        assertTrue(laterCarry&&borrow);
        CurriculumLimits single=new CurriculumLimits("maxRegroups=1");
        assertFalse(single.allows(new Question("add1000","199 + 111","199 + 111","310")));
        assertFalse(single.allows(new Question("sub1000","300 - 111","300 - 111","189")));
        assertTrue(single.allows(new Question("el_missing_add","129 + □ = 140","140-129","11")));
        assertFalse(single.allows(new Question("el_missing_sub","□ - 111 = 189","189+111","300")));
        CurriculumLimits noCarry=new CurriculumLimits("maxRegroups=0;maxSecondOperand=9");
        assertTrue(noCarry.allows(new Question("el_missing_sub","24 - □ = 21","24-21","3")));
        assertFalse(noCarry.allows(new Question("el_missing_sub","20 - □ = 17","20-17","3")));
    }
    @Test public void exactDivisionUnitFractionsAndPatternsKeepTheirOwnGradeRules(){
        Generator g=new Generator(new Random(231));Set<String> thirds=new HashSet<>();
        for(int i=0;i<500;i++)for(int grade:List.of(1,2,3)){
            Question pattern=g.next("el_number_pattern",List.of(),false,GlobalCurriculum.limits(KE,"el_number_pattern",grade));
            String[] values=pattern.prompt.split("\\n",2)[0].split(" → ");int first=Integer.parseInt(values[0]),second=Integer.parseInt(values[1]),delta=second-first,max=grade==1?20:grade==2?100:1000;
            int blank=0;for(int j=0;j<values.length;j++){if(values[j].equals("□")){blank=j;continue;}assertEquals(first+j*delta,Integer.parseInt(values[j]));assertTrue(Integer.parseInt(values[j])<=max);}
            assertTrue(new Checker().check(pattern,List.of(),List.of(String.valueOf(first+blank*delta))).correct());
            if(grade==1)continue;
            Question fraction=g.next("fractionPart",List.of(),true,GlobalCurriculum.limits(KE,"fractionPart",grade));String[] f=fraction.expression.split("/");
            assertEquals("1",f[0]);assertTrue((grade==2?Set.of("2","4"):Set.of("2","4","8")).contains(f[1]));if(grade==3)thirds.add(f[1]);
            Question division=g.next("divide",List.of(),true,GlobalCurriculum.limits(KE,"divide",grade));String[] d=division.expression.split(" ÷ ");int dividend=Integer.parseInt(d[0]),divisor=Integer.parseInt(d[1]);
            assertEquals(0,dividend%divisor);assertTrue(dividend<=(grade==2?(divisor==2?20:25):99));assertTrue(divisor<=(grade==2?5:10));
            Question product=g.next("tables",List.of(),false,GlobalCurriculum.limits(KE,"tables",grade));String[] m=product.expression.split(" × ");int a=Integer.parseInt(m[0]),b=Integer.parseInt(m[1]);
            assertTrue(a<=10&&b<=10&&a*b<=90);if(grade==2)assertTrue(Set.of(0,1,2,3,4,5,10).contains(a)||Set.of(0,1,2,3,4,5,10).contains(b));
        }
        assertEquals(Set.of("2","4","8"),thirds);
        assertFalse(GlobalCurriculum.limits(KE,"divide",2).allows(new Question("divide","24 ÷ 2","24 ÷ 2","12")));
    }
    @Test public void savedGradeRulesSurviveCountryAndGradeChanges(){
        Learning.State state=new Learning.State();state.profile=profile(1);Learning.beginPractice(state,"practice",List.of("add100"),100,false,new Random(55),Map.of("add100",1));
        GlobalCurriculum.chooseCountry(state.profile,"RW");state.profile.grade=8;
        Question q=Learning.ensureQuestion(state,new Generator(new Random(81)));String[] terms=q.expression.split(" ");int a=Integer.parseInt(terms[0]),b=Integer.parseInt(terms[2]);
        assertEquals(KE,state.session.educationSystem);assertEquals("KE",state.session.countryCode);assertTrue(a+b<=50&&b<=9&&a%10+b<10);
    }
    @Test public void ninthGradeStaysWithinPublishedSelectedAlgebraAndDiagnosisUsesPriorLearning(){
        GlobalCurriculum.Pack pack=GlobalCurriculum.pack(profile(9));
        assertEquals("Grade 9",pack.level(9));assertEquals(9,pack.maxGrade());
        for(String id:List.of("signedAdd","signedMul","powerLaw","negativePower","sec_matrix_element","sec_matrix_add","linearSlope","linearXIntercept","linearYIntercept","linearInequality"))assertTrue(id,pack.inGrade(id,9));
        for(String id:List.of("sec_matrix_scalar","sec_matrix_product","log","commonLog","sec_linear_inequality_system","sec_quadratic_inequality"))assertFalse(id,pack.inGrade(id,9));
        assertTrue(pack.placements("sec_matrix_add").get(0).reference().contains("PDF22"));
        Learning.State state=new Learning.State();state.profile=profile(9);state.profile.currentSkill="sec_matrix_add";Diagnosis.begin(state,new Random(29),true);
        assertFalse(state.session.diagnosticRun.plan.scope.contains("sec_matrix_add"));assertFalse(state.session.diagnosticRun.plan.scope.contains("linearInequality"));
        assertTrue(GlobalCurriculum.scope(state.profile).stream().anyMatch(s->s.id.equals("linearSystem")));
        Learning.beginPractice(state,"practice",List.of("sec_matrix_add"),10,false,new Random(92),Map.of("sec_matrix_add",9));
        GlobalCurriculum.chooseCountry(state.profile,"US");state.profile.grade=3;
        assertEquals(KE,state.session.educationSystem);assertEquals(Integer.valueOf(9),state.session.selectedGrades.get("sec_matrix_add"));
        assertEquals("sec_matrix_add",Learning.ensureQuestion(state,new Generator(new Random(33))).skillId);
    }
}
