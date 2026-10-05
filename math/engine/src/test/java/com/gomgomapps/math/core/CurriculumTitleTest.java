package com.gomgomapps.math.core;

import org.junit.Test;
import static org.junit.Assert.*;

public class CurriculumTitleTest {
    private static final String SG="sg-moe-primary-2021-v1";
    @Test public void selectedPlacementUsesItsActualRange(){
        assertEquals("1,000까지 수",GlobalCurriculum.title(SG,"place1000",2));
        assertEquals("10,000까지 수",GlobalCurriculum.title(SG,"largePlace",3));
        assertEquals("100,000까지 수",GlobalCurriculum.title(SG,"largePlace",4));
        assertEquals(Catalog.get("place1000").title,GlobalCurriculum.title("kr-national","place1000",2));
        assertEquals(Catalog.get("largePlace").title,GlobalCurriculum.title(GlobalCurriculum.COMMON,"largePlace",4));
        assertEquals(Catalog.get("tables").title,GlobalCurriculum.title(SG,"tables",2));
    }
    @Test public void browsingAnotherGradeAndSavedSessionUseTheirOwnPlacement(){
        Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"SG");GlobalCurriculum.choosePack(p,SG);p.grade=2;
        TopicSelection.Group third=TopicSelection.groups(p,java.util.List.of(),false).stream().filter(g->g.id().equals("grade:3")).findFirst().orElseThrow();
        assertEquals("10,000까지 수",GlobalCurriculum.title(p.educationSystem,"largePlace",TopicSelection.selectedGrade(p,third,"largePlace")));
        Learning.Session s=new Learning.Session();GlobalCurriculum.stamp(s,p);s.selectedGrades.put("largePlace",3);
        GlobalCurriculum.chooseCountry(p,"JP");
        assertEquals("10,000까지 수",GlobalCurriculum.title(s,"largePlace"));
        assertEquals("1,000까지 수",GlobalCurriculum.title(s,"place1000"));
        Learning.Session legacy=new Learning.Session();assertEquals(Catalog.get("place1000").title,GlobalCurriculum.title(legacy,"place1000"));
    }
    @Test public void diagnosisTitleUsesReviewGradeInsteadOfCurrentGrade(){
        Learning.Session s=new Learning.Session();s.educationSystem=SG;s.curriculumGrade=4;
        s.diagnosticRun=new Diagnosis.Run(new Diagnosis.Plan(),true,4);
        s.diagnosticRun.current=new Diagnosis.Probe("largePlace",3,false);
        assertEquals("10,000까지 수",GlobalCurriculum.title(s,"largePlace"));
        assertEquals("1,000까지 수",GlobalCurriculum.title(s,"place1000"));
        assertEquals(Catalog.get("tables").title,GlobalCurriculum.title(s,"tables"));
        s.diagnosticRun.plan.placements.put("largePlace",3);
        s.diagnosticRun.current=new Diagnosis.Probe("place1000",2,false);
        assertEquals("10,000까지 수",GlobalCurriculum.title(s,"largePlace"));
    }
}
