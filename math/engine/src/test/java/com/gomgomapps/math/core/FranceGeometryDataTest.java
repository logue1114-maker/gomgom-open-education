package com.gomgomapps.math.core;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class FranceGeometryDataTest {
    private static final String PACK="fr-men-cycles23-2024-2025-v1";
    @Test public void newGeometryAndDataEnterTheirPublishedGrades(){
        Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"FR");GlobalCurriculum.choosePack(p,PACK);GlobalCurriculum.Pack pack=GlobalCurriculum.pack(p);
        assertTrue(pack.inGrade("mean",7));assertTrue(pack.inGrade("sec_cylinder_volume",7));
        assertFalse(pack.inGrade("median",7));assertFalse(pack.inGrade("pythagoras",7));assertFalse(pack.inGrade("sec_cone_volume",7));
        assertTrue(pack.inGrade("median",8));assertTrue(pack.inGrade("pythagoras",8));assertTrue(pack.inGrade("sec_cone_volume",8));
        assertFalse(pack.inGrade("sec_sphere_volume",8));assertFalse(pack.inGrade("sec_trig_height",8));
        assertTrue(pack.inGrade("sec_sphere_volume",9));assertTrue(pack.inGrade("sec_trig_height",9));
        p.grade=7;assertFalse(Learning.diagnosticScope(p).contains(Catalog.get("mean")));
        p.grade=8;assertTrue(Learning.diagnosticScope(p).contains(Catalog.get("mean")));assertFalse(Learning.diagnosticScope(p).contains(Catalog.get("median")));
    }
    @Test public void volumeAndCircleSupplyRespectTheirFrenchLengthsWithoutChangingDefault(){
        Generator generator=new Generator(new Random(202610087));
        for(String id:List.of("el_cube_volume","el_circle_area","sec_sphere_volume")){
            Set<String> seen=new HashSet<>();int grade=id.equals("sec_sphere_volume")?9:7;boolean larger=false;
            for(int i=0;i<100;i++){
                Question q=generator.next(id,seen,i%2==0,GlobalCurriculum.limits(PACK,id,grade));assertTrue(seen.add(q.signature()));
                java.util.regex.Matcher m=java.util.regex.Pattern.compile("(\\d+)(?:cm|인)").matcher(q.prompt);assertTrue(q.prompt,m.find());int length=Integer.parseInt(m.group(1));assertTrue(length>=2&&length<=200);larger|=length>20;
                Rational expected=id.equals("el_cube_volume")?Rational.of((long)length*length*length):id.equals("el_circle_area")?Rational.of(157L*length*length,50):Rational.of(4L*length*length*length,3);
                assertEquals(expected,Expression.number(q.answers[0]));assertFalse(HelpPlan.forQuestion(q).canTransfer());
            }
            assertTrue(larger);
        }
        for(int i=0;i<100;i++){
            Question q=generator.next("el_cube_volume",List.of(),false);
            java.util.regex.Matcher m=java.util.regex.Pattern.compile("(\\d+)cm").matcher(q.prompt);assertTrue(m.find());assertTrue(Integer.parseInt(m.group(1))<=12);
        }
    }
}
