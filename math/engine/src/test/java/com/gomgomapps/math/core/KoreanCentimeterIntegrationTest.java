package com.gomgomapps.math.core;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;

public class KoreanCentimeterIntegrationTest {
    private static final List<String> IDS=List.of(KoreanCentimeterMeasurement.EXACT,KoreanCentimeterMeasurement.APPROX,KoreanCentimeterMeasurement.UNITS);
    private Question publicQuestion(String id,double end,int start,int step){
        String prompt=id.equals(KoreanCentimeterMeasurement.EXACT)?KoreanCentimeterMeasurement.EXACT_PROMPT:id.equals(KoreanCentimeterMeasurement.APPROX)?KoreanCentimeterMeasurement.APPROX_PROMPT:KoreanCentimeterMeasurement.UNITS_PROMPT;
        Question q=new Question(id,prompt,"private expression","999");q.diagram=new StudyDiagram("centimeterRuler",new double[]{id.equals(KoreanCentimeterMeasurement.UNITS)?21:15,start,end,step},id.equals(KoreanCentimeterMeasurement.UNITS)?"단위":"cm");return q;
    }
    @Test public void ActualCreateAndNextReachPublicTypedAndMultipleChoiceMeasurements(){
        Checker checker=new Checker();for(String id:IDS){
            Question created=new Generator(new Random(3751)).create(Catalog.get(id));assertNotNull(KoreanCentimeterMeasurement.read(created));assertTrue(checker.check(created,List.of(),List.of(""+KoreanCentimeterMeasurement.read(created).value())).correct());
            for(boolean mc:List.of(false,true)){
                Generator generator=new Generator(new Random(3752));List<String> recent=new ArrayList<>();int count=id.equals(KoreanCentimeterMeasurement.EXACT)?12:id.equals(KoreanCentimeterMeasurement.APPROX)?24:10;
                for(int i=0;i<count;i++){
                    Question q=generator.next(id,recent,mc);var g=KoreanCentimeterMeasurement.read(q);assertNotNull(g);assertFalse(recent.contains(q.signature()));recent.add(q.signature());assertEquals(0,g.start());assertTrue(checker.check(q,List.of(),List.of(""+g.value())).correct());assertEquals(Checker.Status.WRONG_ANSWER,checker.check(q,List.of(),List.of(""+(g.value()+1))).status);
                    if(mc){assertEquals(4,q.choices.size());assertEquals(""+g.value(),q.choices.get(q.correctChoice));assertEquals(1,q.choices.stream().filter(v->checker.check(q,List.of(),List.of(v)).correct()).count());}else assertTrue(q.choices.isEmpty());
                    HelpPlan h=HelpPlan.forQuestion(q);assertNotNull(h);assertFalse(h.canTransfer());assertEquals(id.equals(KoreanCentimeterMeasurement.UNITS)?3:5,h.size());for(int stage=0;stage<h.size();stage++)assertTrue(h.step(stage).accepts(KoreanCentimeterMeasurement.guide(q).frames.get(stage).expected));
                }
                assertEquals(recent.get(0),generator.next(id,recent,mc).signature());
            }
        }
    }

    @Test public void CheckerNotebookAndExplicitWorkAnswerUsePublicMeasurementWithNoGuideTransfer(){
        Checker checker=new Checker();for(Question q:List.of(publicQuestion(KoreanCentimeterMeasurement.EXACT,8,3,1),publicQuestion(KoreanCentimeterMeasurement.APPROX,4.8,0,1),publicQuestion(KoreanCentimeterMeasurement.UNITS,15,0,3))){
            q.answers=null;q.kind="complexRoots";StudyGuide cached=new StudyGuide().step("private","private = ","","999");q.studyGuide=cached;q.withInputs(999);q.calculationMethod="written";
            String full=q.skillId.equals(KoreanCentimeterMeasurement.EXACT)?"8-3=5":q.skillId.equals(KoreanCentimeterMeasurement.UNITS)?"15/3=5":"5=5";List<Checker.StepKind> kinds=List.of(Checker.StepKind.FULL);
            assertTrue(checker.check(q,List.of(full),List.of("5"),kinds).correct());assertTrue(checker.checkSteps(q,List.of(full),kinds).correct());assertEquals(List.of("5"),WorkAnswer.writtenAnswers(q,List.of(full),kinds));assertTrue(WorkAnswer.writtenAnswers(q,List.of("2+3=5"),List.of(Checker.StepKind.PARTIAL)).isEmpty());
            Checker.Result bad=checker.check(q,List.of("999=999"),List.of("5"),kinds);assertEquals(Checker.Status.WRONG_STEP,bad.status);assertEquals(Checker.InputTarget.WORK,bad.inputTarget);assertFalse(checker.checkSteps(q,List.of("999=999"),kinds).correct());assertTrue(WorkAnswer.writtenAnswers(q,List.of("999=999"),kinds).isEmpty());
            assertTrue(checker.check(q,List.of(),List.of("5")).correct());assertEquals(Checker.Status.INPUT_NEEDED,checker.check(q,List.of(),List.of("")).status);
            HelpPlan plan=HelpPlan.forQuestion(q);assertFalse(plan.canTransfer());assertSame(cached,q.studyGuide);assertNull(q.answers);assertEquals("private expression",q.expression);assertEquals("complexRoots",q.kind);
            HelpPlan.Draft draft=plan.restore(null,q.id);draft.entries.set(0,"1");draft.stage=1;draft.entries.add("");HelpPlan.Draft restored=plan.restore(draft,q.id);assertEquals("1",restored.entries.get(0));assertFalse(plan.canTransfer());
            if(q.skillId.equals(KoreanCentimeterMeasurement.APPROX))assertEquals(Checker.Status.WRONG_STEP,checker.checkSteps(q,List.of("4.8=5"),kinds).status);
            q.prompt="invalid public prompt";assertNull(HelpPlan.forQuestion(q));assertEquals(Checker.Status.INPUT_NEEDED,checker.check(q,List.of(),List.of("999")).status);assertEquals(Checker.Status.INPUT_NEEDED,checker.checkSteps(q,List.of(full),kinds).status);assertTrue(WorkAnswer.writtenAnswers(q,List.of(full),kinds).isEmpty());assertSame(cached,q.studyGuide);
        }
    }

    @Test public void NewMeasurementCoresBelongOnlyToLearnedKorean2022LengthUnit(){
        Learning.Profile p=new Learning.Profile();p.grade=6;p.term=2;p.schoolYear=2026;p.curriculum=2022;p.foundationAscending=true;p.lastLearnedSkill="unit:KR|2022|2|1|3";
        assertTrue(KoreanFoundationUnits.scope(p).stream().noneMatch(IDS::contains));p.lastLearnedSkill="unit:KR|2022|2|1|4";assertTrue(KoreanFoundationUnits.scope(p).containsAll(IDS));
        KoreanFoundationUnits.Unit unit=KoreanFoundationUnits.learned(p).stream().filter(u->u.key().equals("KR|2022|2|1|4")).findFirst().orElseThrow();assertEquals(Set.copyOf(IDS),Set.copyOf(unit.members()));assertEquals(Set.copyOf(IDS),Set.copyOf(unit.representatives()));assertFalse(unit.members().contains("el_length_m_cm"));
        p.excluded.add(KoreanCentimeterMeasurement.APPROX);assertFalse(KoreanFoundationUnits.scope(p).contains(KoreanCentimeterMeasurement.APPROX));assertTrue(KoreanFoundationUnits.scope(p).contains(KoreanCentimeterMeasurement.EXACT));
        for(String id:IDS){assertTrue(Curriculum.inCurriculum(Catalog.get(id),2022));assertFalse(Curriculum.inCurriculum(Catalog.get(id),2015));}
        p.curriculum=2015;assertTrue(KoreanFoundationUnits.available(p).stream().noneMatch(u->u.members().stream().anyMatch(IDS::contains)));p.curriculum=2022;p.countryCode="NA";p.educationSystem="na-national";assertTrue(KoreanFoundationUnits.available(p).isEmpty());
    }
}
