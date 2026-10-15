package com.gomgomapps.math.core;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;

public class KoreanCentimeterMeasurementTest {
    private Question question(String id,int maximum,int start,double end,int step){
        String prompt=id.equals(KoreanCentimeterMeasurement.EXACT)?KoreanCentimeterMeasurement.EXACT_PROMPT:id.equals(KoreanCentimeterMeasurement.APPROX)?KoreanCentimeterMeasurement.APPROX_PROMPT:KoreanCentimeterMeasurement.UNITS_PROMPT;
        Question q=new Question(id,prompt,"private expression","999");q.diagram=new StudyDiagram("centimeterRuler",new double[]{maximum,start,end,step},id.equals(KoreanCentimeterMeasurement.UNITS)?"단위":"cm");return q;
    }
    private List<String> entries(StudyGuide h){return h.frames.stream().map(f->f.expected).toList();}

    @Test public void ExactRulerUsesIntervalsBetweenBothEndsAcrossTheWholePublicDomain(){
        for(int max=1;max<=30;max++)for(int start=0;start<max;start++)for(int end=start+1;end<=max;end++){
            Question q=question(KoreanCentimeterMeasurement.EXACT,max,start,end,1);var g=KoreanCentimeterMeasurement.read(q);assertNotNull(g);assertEquals(end-start,g.value());assertEquals(start,g.start());assertEquals(end,g.end(),0);assertTrue(KoreanCentimeterMeasurement.check(q,List.of(""+(end-start))).correct());
            if(start>0)assertEquals(Checker.Status.WRONG_ANSWER,KoreanCentimeterMeasurement.check(q,List.of(""+end)).status);
        }
    }

    @Test public void ApproximationIsNearestWholeCentimeterWithNoTieOrFalseExactEquality(){
        Set<String> identities=new HashSet<>();for(int target=0;target<=30;target++)for(int offset:new int[]{-4,-3,-2,-1,1,2,3,4}){
            int tenths=10*target+offset;if(tenths<=0||tenths>300)continue;Question q=question(KoreanCentimeterMeasurement.APPROX,30,0,tenths/10.0,1);var g=KoreanCentimeterMeasurement.read(q);assertNotNull(g);assertEquals(target,g.value());assertEquals(tenths,g.endTenths());assertTrue(g.lower()<g.end()&&g.end()<g.upper());assertTrue(identities.add(KoreanCentimeterMeasurement.identity(q)));assertTrue(KoreanCentimeterMeasurement.check(q,List.of(""+target)).correct());assertEquals(""+target,KoreanCentimeterMeasurement.workQuestion(q).expression);
        }
        for(double end:new double[]{1,1.01,1.49,1.5,1.51,2.0})assertNull(KoreanCentimeterMeasurement.read(question(KoreanCentimeterMeasurement.APPROX,15,0,end,1)));
        assertNull(KoreanCentimeterMeasurement.read(question(KoreanCentimeterMeasurement.APPROX,15,1,2.2,1)));
    }

    @Test public void ArbitraryUnitsCountReferenceBlocksRatherThanClaimCentimetersOrNewScaledFacts(){
        for(int step=1;step<=3;step++)for(int count=1;count<=10;count++){
            Question q=question(KoreanCentimeterMeasurement.UNITS,(count+2)*step,0,count*step,step);var g=KoreanCentimeterMeasurement.read(q);assertNotNull(g);assertTrue(g.arbitrary());assertEquals(count,g.value());assertTrue(KoreanCentimeterMeasurement.check(q,List.of(""+count)).correct());if(step>1)assertEquals(Checker.Status.WRONG_ANSWER,KoreanCentimeterMeasurement.check(q,List.of(""+(count*step))).status);
            Question scaled=question(KoreanCentimeterMeasurement.UNITS,count+2,0,count,1);assertEquals(KoreanCentimeterMeasurement.identity(scaled),KoreanCentimeterMeasurement.identity(q));
        }
        Question offset=question(KoreanCentimeterMeasurement.UNITS,30,2,14,3);assertEquals(4,KoreanCentimeterMeasurement.read(offset).value());assertNull(KoreanCentimeterMeasurement.read(question(KoreanCentimeterMeasurement.UNITS,15,0,7,3)));
        Question exact=question(KoreanCentimeterMeasurement.EXACT,15,2,8,1),space=question(KoreanCentimeterMeasurement.EXACT,30,2,8,1);assertEquals(KoreanCentimeterMeasurement.identity(exact),KoreanCentimeterMeasurement.identity(space));assertNotEquals(KoreanCentimeterMeasurement.identity(exact),KoreanCentimeterMeasurement.identity(question(KoreanCentimeterMeasurement.EXACT,15,0,6,1)));
        Question approx=question(KoreanCentimeterMeasurement.APPROX,15,0,3.2,1);assertEquals(KoreanCentimeterMeasurement.identity(approx),KoreanCentimeterMeasurement.identity(question(KoreanCentimeterMeasurement.APPROX,30,0,3.2,1)));assertNotEquals(KoreanCentimeterMeasurement.identity(approx),KoreanCentimeterMeasurement.identity(question(KoreanCentimeterMeasurement.APPROX,15,0,2.8,1)));
    }

    @Test public void GuidesUseOnlyPublicGivensCheckedReferencesAndFadeTheFinalPicture(){
        for(Question q:List.of(question(KoreanCentimeterMeasurement.EXACT,15,3,8,1),question(KoreanCentimeterMeasurement.APPROX,15,0,4.8,1),question(KoreanCentimeterMeasurement.UNITS,21,0,15,3))){
            var g=KoreanCentimeterMeasurement.read(q);StudyGuide h=KoreanCentimeterMeasurement.guide(q);assertFalse(h.transfer);assertEquals(KoreanCentimeterMeasurement.VERSION,h.teachingVersion);assertEquals(g.arbitrary()?3:5,h.frames.size());assertEquals(""+g.value(),h.frames.get(h.frames.size()-1).expected);
            assertEquals(g.arbitrary()?List.of("1","5","5"):g.approximate()?List.of("1","4","5","5","5"):List.of("1","3","8","5","5"),entries(h));
            for(int i=0;i<h.frames.size()-1;i++){StudyDiagram picture=KoreanCentimeterMeasurement.picture(q,i);assertNotNull(picture);assertNotSame(q.diagram.values,picture.values);picture.values[0]=999;assertEquals(g.maximum(),q.diagram.values[0],0);}
            assertNull(KoreanCentimeterMeasurement.picture(q,h.frames.size()-1));assertNull(KoreanCentimeterMeasurement.picture(q,-1));assertFalse(KoreanCentimeterMeasurement.references(q,h.frames.size()-1,entries(h)).isEmpty());assertTrue(KoreanCentimeterMeasurement.references(q,0,entries(h)).isEmpty());assertTrue(KoreanCentimeterMeasurement.references(q,h.frames.size()-1,Collections.nCopies(h.frames.size(),"999")).isEmpty());
            StudyGuide stored=new StudyGuide().step("private","private","","999");q.studyGuide=stored;q.answers=null;q.kind="radical";q.withInputs(999);assertEquals(g,KoreanCentimeterMeasurement.read(q));assertEquals(entries(h),entries(KoreanCentimeterMeasurement.guide(q)));assertTrue(KoreanCentimeterMeasurement.check(q,List.of(""+g.value())).correct());assertSame(stored,q.studyGuide);assertEquals("private expression",q.expression);assertNull(q.answers);
            Question proxy=KoreanCentimeterMeasurement.workQuestion(q);assertEquals("number",proxy.kind);assertNotSame(q.diagram.values,proxy.diagram.values);assertEquals(""+g.value(),proxy.answers[0]);assertEquals(g,KoreanCentimeterMeasurement.read(proxy));
            KoreanCentimeterMeasurement.attach(q);assertNotSame(stored,q.studyGuide);assertFalse(q.studyGuide.transfer);
        }
    }

    @Test public void InvalidPublicPicturesNeverFallBackToPrivateMathAndInputsHaveTruthfulStatus(){
        assertFalse(KoreanCentimeterMeasurement.supports(null));assertNull(KoreanCentimeterMeasurement.read(null));
        for(double[] values:List.of(new double[]{31,0,8,1},new double[]{15,0,16,1},new double[]{15,5,5,1},new double[]{15,8,5,1},new double[]{15,0,5.2,1},new double[]{15,0,5,2},new double[]{15,0,5,0},new double[]{Double.NaN,0,5,1},new double[]{15,0,Double.POSITIVE_INFINITY,1},new double[]{15,0,5})){
            Question q=question(KoreanCentimeterMeasurement.EXACT,15,0,5,1);q.diagram.values=values;assertNull(KoreanCentimeterMeasurement.read(q));assertNull(KoreanCentimeterMeasurement.guide(q));assertNull(KoreanCentimeterMeasurement.identity(q));assertEquals(Checker.Status.INPUT_NEEDED,KoreanCentimeterMeasurement.check(q,List.of("999")).status);
        }
        Question q=question(KoreanCentimeterMeasurement.EXACT,15,0,5,1);q.prompt="5cmの答";assertNull(KoreanCentimeterMeasurement.read(q));q.prompt=KoreanCentimeterMeasurement.EXACT_PROMPT;q.diagram.labels=new String[]{"m"};assertNull(KoreanCentimeterMeasurement.read(q));q.diagram.labels=new String[]{"cm"};q.diagram.type="primaryNumberLine";assertNull(KoreanCentimeterMeasurement.read(q));q.diagram.type="centimeterRuler";q.diagram.rotationDegrees=new double[]{90};assertNull(KoreanCentimeterMeasurement.read(q));q.diagram.rotationDegrees=null;
        assertEquals(Checker.Status.INPUT_NEEDED,KoreanCentimeterMeasurement.check(q,null).status);for(String raw:List.of("","abc","1/0","9".repeat(121)))assertEquals(Checker.Status.INPUT_NEEDED,KoreanCentimeterMeasurement.check(q,List.of(raw)).status);assertEquals(Checker.Status.WRONG_ANSWER,KoreanCentimeterMeasurement.check(q,List.of("4")).status);assertTrue(KoreanCentimeterMeasurement.check(q,List.of("5.0")).correct());assertEquals(Checker.InputTarget.ANSWER,KoreanCentimeterMeasurement.check(q,List.of("4")).inputTarget);
        assertNull(KoreanCentimeterMeasurement.read(question(KoreanCentimeterMeasurement.UNITS,37,0,9,3)));assertNull(KoreanCentimeterMeasurement.read(question(KoreanCentimeterMeasurement.UNITS,20,0,8,4)));
    }

    @Test public void NormalSupplyIsFreshByLearningIdentityThenReusesGlobalOldestAndChoicesArePublic(){
        for(String id:List.of(KoreanCentimeterMeasurement.EXACT,KoreanCentimeterMeasurement.APPROX,KoreanCentimeterMeasurement.UNITS)){
            int count=id.equals(KoreanCentimeterMeasurement.EXACT)?12:id.equals(KoreanCentimeterMeasurement.APPROX)?24:10;Map<String,Integer> recent=new LinkedHashMap<>();String oldest=null;Set<Integer> steps=new HashSet<>();Random random=new Random(375);
            for(int i=0;i<count;i++){
                Question q=KoreanCentimeterMeasurement.next(id,random,CurriculumLimits.NONE,recent);var g=KoreanCentimeterMeasurement.read(q);assertNotNull(g);assertEquals(0,g.start());assertFalse(recent.containsKey(q.signature()));assertEquals(KoreanCentimeterMeasurement.identity(q),q.signature());if(oldest==null)oldest=q.signature();recent.put(q.signature(),i);steps.add(g.unitStep());assertTrue(g.arbitrary()?g.value()>=1&&g.value()<=10:g.value()>=1&&g.value()<=12);assertFalse(q.studyGuide.transfer);
                if(g.arbitrary()){assertEquals((g.value()+2)*g.unitStep(),g.maximum());assertEquals(g.value()*g.unitStep(),g.end(),0);}else assertEquals(15,g.maximum());
                q.answers=new String[]{"999"};KoreanCentimeterMeasurement.choices(q,random);assertEquals(4,q.choices.size());assertEquals(4,new HashSet<>(q.choices).size());assertEquals(4,q.distractorReasons.size());assertEquals(""+g.value(),q.choices.get(q.correctChoice));int correct=0;for(String option:q.choices)if(KoreanCentimeterMeasurement.check(q,List.of(option)).correct())correct++;assertEquals(1,correct);assertArrayEquals(new String[]{"999"},q.answers);
            }
            assertEquals(oldest,KoreanCentimeterMeasurement.next(id,random,CurriculumLimits.NONE,recent).signature());if(id.equals(KoreanCentimeterMeasurement.UNITS))assertTrue(steps.size()>=2);
        }
    }
}
