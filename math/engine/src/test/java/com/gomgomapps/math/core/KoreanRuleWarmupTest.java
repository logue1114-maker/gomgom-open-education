package com.gomgomapps.math.core;

import org.junit.Test;
import static org.junit.Assert.*;
import java.time.LocalDate;
import java.util.*;

/** Selected lesson supply is smaller; retained questions and other routes keep their contracts. */
public class KoreanRuleWarmupTest {
    private static final LocalDate DAY=LocalDate.of(2026,10,15);
    private static final String UNIT="KR|2022|4|2|3",WARM="el_number_pattern";
    private static final List<String> TARGETS=List.of(KoreanRulesRelations.NUMBER,KoreanRulesRelations.FIGURE,
            KoreanRulesRelations.CALC,KoreanRulesRelations.EQUAL,KoreanRulesRelations.FILL);

    private Learning.State start(String mode,long seed,boolean allTargets){
        Learning.State state=new Learning.State();
        state.profile.grade=6;state.profile.term=2;state.profile.curriculum=2022;
        state.profile.schoolYear=2026;state.profile.ready=true;state.profile.foundationAscending=true;
        state.profile.lastLearnedSkill="unit:KR|2022|6|2|5";
        Random random=new Random(seed);Learning.beginDiagnostic(state,random);
        Diagnosis.Plan plan=Diagnosis.currentPlan(state);
        Diagnosis.UnitScreen unit=plan.unitScreens.get(UNIT);assertNotNull(unit);
        for(String id:allTargets?TARGETS:List.of(KoreanRulesRelations.NUMBER))
            KoreanFoundationStudy.flagged(state,unit,id,"unable",DAY);
        assertTrue(Diagnosis.studyUnits(state).contains(unit));
        if(mode.equals("foundationStudy"))Diagnosis.beginStudy(state,unit,random);
        else Diagnosis.beginRecheck(state,unit,random);
        assertEquals(mode,state.session.mode);assertEquals(UNIT,state.session.foundationUnitKey);
        assertEquals(WARM,state.session.queue.get(0));return state;
    }
    private Diagnosis.UnitScreen unit(Learning.State state){
        return state.diagnostics.get(state.session.foundationPlanKey).unitScreens.get(UNIT);
    }
    private Question next(Learning.State state,Random random){
        return Learning.ensureQuestion(state,new Generator(random),DAY);
    }
    /** Reconstruct all five terms from the visible array, rather than trusting the stored key. */
    private int[] terms(Question q){
        assertEquals(WARM,q.skillId);String[] tokens=q.prompt.split("\\R",-1)[0].split(" → ",-1);
        assertEquals(5,tokens.length);int[] terms=new int[5];int blank=-1,delta=0;
        for(int i=0;i<5;i++){
            if(tokens[i].equals("□")){assertEquals(-1,blank);blank=i;}
            else terms[i]=Integer.parseInt(tokens[i]);
        }
        assertTrue(blank>=0);
        for(int i=0;i<4;i++)if(i!=blank&&i+1!=blank){delta=terms[i+1]-terms[i];break;}
        assertTrue(delta>0);
        terms[blank]=blank==0?terms[1]-delta:terms[blank-1]+delta;
        for(int i=1;i<5;i++)assertEquals(delta,terms[i]-terms[i-1]);
        assertEquals(Integer.toString(terms[blank]),q.answers[0]);return terms;
    }
    private void submitIndependent(Learning.State state,Random random){
        Question q=state.session.question;assertNotNull(q);
        // Unit fixtures use the published answer to exercise ordinary finish, not to prove its math.
        state.session.answers.clear();state.session.answers.addAll(Arrays.asList(q.answers));
        assertTrue(new Checker().check(q,state.session.steps,state.session.answers,state.session.stepKinds).correct());
        Learning.finishQuestion(state,false,DAY,random);
    }

    @Test public void homeStudyAndRecheckSupplySmallVariedPatternsAcrossSixtyFourSeeds(){
        for(String mode:List.of("foundationStudy","foundationRecheck")){
            Set<String> facts=new HashSet<>();Set<Integer> differences=new HashSet<>();
            for(int seed=0;seed<64;seed++){
                Learning.State state=start(mode,38600+seed,false);Random random=new Random(386000+seed);
                for(int i=0;i<2;i++){
                    Question q=next(state,random);int[] values=terms(q);
                    for(int value:values)assertTrue(mode+" "+q.prompt,value>=1&&value<=20);
                    int difference=values[1]-values[0];assertTrue(difference>=1&&difference<=4);
                    differences.add(difference);facts.add(q.signature());submitIndependent(state,random);
                }
            }
            assertTrue("Normal selected supply must vary its facts",facts.size()>8);
            assertTrue("More than one small step must remain available",differences.size()>1);
        }
    }

    @Test public void existingLargeQuestionAnswerAndHelpStayExactAcrossColdRestore(){
        Learning.State state=start("foundationStudy",38601,false);
        Question old=new Question(WARM,"447 → 456 → □ → 474 → 483\n수의 규칙을 찾아 빈 수를 구하세요.","447+2*9","465");
        old.studyGuide=new StudyGuide().step("두 항 사이의 일정한 차를 찾습니다.","456 - 447 = ","","9")
                .step("빈 자리까지 같은 차를 반복합니다.","447 + 2 × 9 = ","","465");
        state.session.question=old;state.session.advancePending=false;state.session.answers.clear();state.session.answers.add("123");
        HelpPlan plan=HelpPlan.forQuestion(old);assertNotNull(plan);
        state.session.conceptHelp=plan.restore(null,old.id);state.session.conceptHelp.entries.set(0,"888");
        String signature=old.signature(),version=state.session.conceptHelp.teachingVersion;
        assertSame(old,next(state,new Random(38602)));assertEquals(List.of("123"),state.session.answers);
        assertEquals(List.of("888"),state.session.conceptHelp.entries);
        Learning.State cold=LearningSnapshot.capture(state);Question restored=cold.session.question;
        assertSame(restored,next(cold,new Random(38603)));assertEquals(old.id,restored.id);
        assertEquals(signature,restored.signature());assertEquals(old.prompt,restored.prompt);
        assertArrayEquals(new String[]{"465"},restored.answers);assertEquals("447+2*9",restored.expression);
        assertEquals(List.of("123"),cold.session.answers);assertEquals(List.of("888"),cold.session.conceptHelp.entries);
        assertEquals(version,cold.session.conceptHelp.teachingVersion);assertEquals(0,cold.session.conceptHelp.stage);
        // HelpPlan rebuilds the old cached guide from its visible array before the snapshot.
        assertEquals("447",restored.studyGuide.frames.get(0).expected);
    }

    @Test public void twoIndependentWarmupsReturnToOriginalTargetWithoutRecoveringTheUnit(){
        for(String mode:List.of("foundationStudy","foundationRecheck")){
            Learning.State state=start(mode,38604,true);Diagnosis.UnitScreen unit=unit(state);
            Random random=new Random(38605);assertEquals(30,state.session.target);
            Map<String,String> reasons=new LinkedHashMap<>(unit.reasons);
            for(int i=0;i<2;i++){terms(next(state,random));submitIndependent(state,random);}
            assertEquals(2,state.session.completed);assertEquals(2,state.progress(WARM).firstCorrect);
            Question target=next(state,random);assertEquals(KoreanRulesRelations.NUMBER,target.skillId);
            assertNotNull(KoreanRulesRelations.read(target));assertEquals(List.of(""),state.session.answers);
            assertEquals(Diagnosis.UnitStatus.NEEDS_STUDY,unit.status);assertEquals(reasons,unit.reasons);
            assertTrue(unit.recoveredSkills.isEmpty());assertFalse(unit.freshRechecks.containsKey(KoreanRulesRelations.NUMBER));
            assertTrue(Review.track(state.progress(KoreanRulesRelations.NUMBER)).pending);
            assertTrue(Diagnosis.studyUnits(state).contains(unit));assertFalse(state.session.finished);
            Learning.State cold=LearningSnapshot.capture(state);
            assertEquals(target.id,next(cold,new Random(38606)).id);assertEquals(List.of(""),cold.session.answers);
            assertEquals(Diagnosis.UnitStatus.NEEDS_STUDY,unit(cold).status);
        }
    }

    @Test public void invalidOrUnrelatedLessonBindingsCannotReceiveTheSmallSupplyOverride(){
        for(String boundary:List.of("ordinary","other-unit","NA","foreign-system","2015","no-plan",
                "plan-key","not-screening","outside-scope","missing-member","missing-representative","unselected")){
            Learning.State state=start("foundationStudy",38607,false);Learning.Session session=state.session;
            Diagnosis.Plan plan=state.diagnostics.get(session.foundationPlanKey);Diagnosis.UnitScreen unit=unit(state);
            switch(boundary){
                case "ordinary" -> session.mode="practice";
                case "other-unit" -> session.foundationUnitKey="KR|2022|4|2|2";
                case "NA" -> session.countryCode="NA";
                case "foreign-system" -> session.educationSystem="na-nied-primary-2024-v1";
                case "2015" -> plan.curriculum=2015;
                case "no-plan" -> state.diagnostics.remove(session.foundationPlanKey);
                case "plan-key" -> plan.key="other-saved-plan";
                case "not-screening" -> plan.unitScreening=false;
                case "outside-scope" -> plan.scope.remove(WARM);
                case "missing-member" -> unit.members.remove(KoreanRulesRelations.NUMBER);
                case "missing-representative" -> unit.representatives.remove(KoreanRulesRelations.NUMBER);
                case "unselected" -> session.selected.remove(WARM);
            }
            CurriculumLimits actual=KoreanFoundationStudy.questionLimits(state,session,WARM,CurriculumLimits.NONE);
            assertSame(boundary,CurriculumLimits.NONE,actual);
            Question original=new Generator(new Random(38610)).next(WARM,List.of(),false,CurriculumLimits.NONE);
            Question untouched=new Generator(new Random(38610)).next(WARM,List.of(),false,actual);
            assertEquals(boundary,original.signature(),untouched.signature());
            assertArrayEquals(boundary,original.answers,untouched.answers);
        }
    }

    @Test public void ordinaryPracticeKeepsItsOriginalSeededQuestionsAndLargeNumberRange(){
        boolean sawAboveTwenty=false;
        for(int seed=0;seed<64;seed++){
            Learning.State state=start("foundationStudy",38608,false);Random setup=new Random(seed);
            Learning.beginPractice(state,"practice",List.of(WARM),20,false,setup);
            Learning.Session session=state.session;
            CurriculumLimits baseline=GlobalCurriculum.limits(session.educationSystem,WARM,
                    session.selectedGrades.getOrDefault(WARM,session.curriculumGrade));
            assertSame(baseline,KoreanFoundationStudy.questionLimits(state,session,WARM,baseline));
            Question expected=new Generator(new Random(seed)).next(WARM,Review.avoid(state,WARM),false,baseline);
            Question actual=next(state,new Random(seed));
            assertEquals(expected.signature(),actual.signature());assertArrayEquals(expected.answers,actual.answers);
            for(int value:terms(actual))if(value>20)sawAboveTwenty=true;
        }
        assertTrue("Ordinary practice must not silently acquire the warm-up cap",sawAboveTwenty);
    }

    @Test public void excludingNumberRulesKeepsSmallPrerequisitesForTheRemainingFigureTarget(){
        for(String mode:List.of("foundationStudy","foundationRecheck")){
            Learning.State state=new Learning.State();state.profile.grade=6;state.profile.term=2;state.profile.curriculum=2022;
            state.profile.schoolYear=2026;state.profile.ready=true;state.profile.foundationAscending=true;
            state.profile.lastLearnedSkill="unit:KR|2022|6|2|5";state.profile.excluded.add(KoreanRulesRelations.NUMBER);
            Random random=new Random(38611);Learning.beginDiagnostic(state,random);Diagnosis.Plan plan=Diagnosis.currentPlan(state);
            Diagnosis.UnitScreen unit=plan.unitScreens.get(UNIT);assertFalse(unit.representatives.contains(KoreanRulesRelations.NUMBER));
            assertTrue(unit.representatives.contains(KoreanRulesRelations.FIGURE));KoreanFoundationStudy.flagged(state,unit,KoreanRulesRelations.FIGURE,"unable",DAY);
            if(mode.equals("foundationStudy"))Diagnosis.beginStudy(state,unit,random);else Diagnosis.beginRecheck(state,unit,random);
            for(int i=0;i<2;i++){Question q=next(state,random);for(int value:terms(q))assertTrue(value>=1&&value<=20);submitIndependent(state,random);}
            assertEquals(KoreanRulesRelations.FIGURE,next(state,random).skillId);assertFalse(state.session.selected.contains(KoreanRulesRelations.NUMBER));
            assertEquals(Diagnosis.UnitStatus.NEEDS_STUDY,unit.status);assertTrue(unit.recoveredSkills.isEmpty());
        }
    }

    @Test public void explicitLimitsAndOtherSkillsKeepTheirOriginalConstraintObjectAndSeededSupply(){
        Learning.State state=start("foundationStudy",38609,false);
        CurriculumLimits custom=new CurriculumLimits("maxGiven=60;maxResult=60;nonnegative=true;answerDomain=integer");
        assertSame(custom,KoreanFoundationStudy.questionLimits(state,state.session,WARM,custom));
        CurriculumLimits explicitEmpty=new CurriculumLimits("");
        assertSame(explicitEmpty,KoreanFoundationStudy.questionLimits(state,state.session,WARM,explicitEmpty));
        for(String id:TARGETS)assertSame(CurriculumLimits.NONE,
                KoreanFoundationStudy.questionLimits(state,state.session,id,CurriculumLimits.NONE));
        for(int seed=0;seed<16;seed++){
            Question before=new Generator(new Random(seed)).next(WARM,List.of(),false,custom);
            Question after=new Generator(new Random(seed)).next(WARM,List.of(),false,
                    KoreanFoundationStudy.questionLimits(state,state.session,WARM,custom));
            assertEquals(before.signature(),after.signature());assertArrayEquals(before.answers,after.answers);
            for(int value:terms(after))assertTrue(value>=1&&value<=60);
        }
    }
}
