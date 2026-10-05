package com.gomgomapps.math.core;
import org.junit.Test;
import java.util.*;
import static org.junit.Assert.*;
public class AustraliaCurriculumTest {
    static final String PACK="au-acara-v9-primary-v1";
    @Test public void nationalPrimaryLevelsAndPastLearningKeepCountryAndLanguageIndependent(){
        Learning.Profile p=new Learning.Profile();p.languageTag="fr";GlobalCurriculum.chooseCountry(p,"AU");GlobalCurriculum.choosePack(p,PACK);p.grade=3;
        GlobalCurriculum.Pack pack=GlobalCurriculum.pack(p);assertEquals("fr",p.languageTag);assertEquals(List.of(0,1,2,3,4,5,6),pack.levels());assertEquals("Foundation",pack.level(0));assertEquals("Year 3",pack.level(3));assertTrue(pack.coverage.contains("state syllabuses"));assertEquals(71,pack.grades.size());
        assertTrue(GlobalCurriculum.scope(p).stream().anyMatch(s->s.id.equals("tables")));assertFalse(GlobalCurriculum.scope(p).stream().anyMatch(s->s.id.equals("decimalAdd")));assertFalse(pack.inGrade("signedAdd",6));assertEquals("120까지 수",GlobalCurriculum.title(PACK,"place1000",1));
    }
    @Test public void gradeSpecificFactsFractionsAndDecimalPrecisionFollowOfficialSelectedComponents(){
        Generator g=new Generator(new Random(20261005231L));
        for(int i=0;i<60;i++){
            for(int grade:List.of(2,3,4)){
                Question q=g.next("tables",List.of(),false,GlobalCurriculum.limits(PACK,"tables",grade));int expected=FactFormsTest.solve(q.prompt);String[] terms=q.prompt.replace("□",String.valueOf(expected)).split(" = ")[0].split(" × ");int a=Integer.parseInt(terms[0]),b=Integer.parseInt(terms[1]);assertTrue(a<=10&&b<=10);if(grade==2)assertTrue(a==2||b==2);if(grade==3)assertTrue(Set.of(3,4,5,10).contains(a)||Set.of(3,4,5,10).contains(b));
            }
            Question part=g.next("fractionPart",List.of(),false,GlobalCurriculum.limits(PACK,"fractionPart",2));assertTrue(Set.of("1/2","1/4","1/8").contains(part.answers[0]));
            Question decimal=g.next("el_fraction_decimal",List.of(),false,GlobalCurriculum.limits(PACK,"el_fraction_decimal",4));assertTrue(decimal.prompt.matches("(?:[1-9]/10|[1-9][0-9]?/100).*"));
        }
    }
    @Test public void everySelectedPlacementActuallyGeneratesWithItsLevelLimits(){
        Generator g=new Generator(new Random(20261005232L));Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"AU");GlobalCurriculum.choosePack(p,PACK);GlobalCurriculum.Pack pack=GlobalCurriculum.pack(p);int count=0;
        for(int grade:pack.levels())for(String id:pack.grades.keySet())if(pack.inGrade(id,grade)){
            Question q=g.next(id,List.of(),false,GlobalCurriculum.limits(PACK,id,grade));assertEquals(id,q.skillId);assertTrue(GlobalCurriculum.limits(PACK,id,grade).allows(q));count++;
        }
        assertEquals(113,count);
    }
}
