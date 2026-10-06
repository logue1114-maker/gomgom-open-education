package com.gomgomapps.math.core;

import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class BrazilCurriculumTest {
    static final String PACK="br-bncc-fundamental-2017-v1";
    private Learning.Profile profile(int grade){Learning.Profile p=new Learning.Profile();p.languageTag="pt";GlobalCurriculum.chooseCountry(p,"BR");GlobalCurriculum.choosePack(p,PACK);p.grade=grade;return p;}
    @Test public void brazilIsSelectableWithoutChangingLanguageOrClaimingUnmappedLevels(){
        Learning.Profile p=profile(3);GlobalCurriculum.Pack pack=GlobalCurriculum.pack(p);
        assertEquals("pt",p.languageTag);assertEquals("BR",pack.country);assertEquals(List.of(1,2,3),pack.levels());assertEquals("3º ano",pack.level(3));
        assertEquals(23,pack.grades.size());assertTrue(pack.coverage.contains("remain unmapped"));
        assertFalse(pack.inGrade("divide",2));assertTrue(pack.inGrade("divide",3));assertFalse(pack.grades.containsKey("fractionPart"));
        assertEquals("9,999까지의 덧셈",GlobalCurriculum.title(PACK,"add1000",3));assertEquals(Catalog.get("add1000").title,GlobalCurriculum.title(PACK,"add1000",2));
    }
    @Test public void diagnosisUsesPreviousMappedGradesAndTheirOwnNumberLimits(){
        Learning.Profile p=profile(2);Set<String> ids=new HashSet<>();for(Catalog.Skill s:GlobalCurriculum.scope(p))ids.add(s.id);
        assertTrue(ids.contains("add100"));assertFalse(ids.contains("add1000"));assertFalse(ids.contains("tables"));assertFalse(ids.contains("divide"));
        assertEquals(1,GlobalCurriculum.reviewGrade(PACK,Catalog.get("add100"),2));
        p.grade=3;assertEquals(2,GlobalCurriculum.reviewGrade(PACK,Catalog.get("add1000"),3));
        assertTrue(GlobalCurriculum.scope(p).stream().anyMatch(s->s.id.equals("tables")));assertFalse(GlobalCurriculum.scope(p).stream().anyMatch(s->s.id.equals("divide")));
    }
    @Test public void everyPlacementGeneratesInNormalEngineWithIndependentArithmeticChecks(){
        Generator generator=new Generator(new Random(20261006031L));GlobalCurriculum.Pack pack=GlobalCurriculum.pack(profile(3));int placements=0,fourDigit=0;
        for(int grade:pack.levels())for(String id:pack.grades.keySet())if(pack.inGrade(id,grade)){
            placements++;List<String> recent=new ArrayList<>();CurriculumLimits limits=GlobalCurriculum.limits(PACK,id,grade);
            for(int i=0;i<100;i++){
                Question q=generator.next(id,recent,i%2==0,limits);assertEquals(id,q.skillId);assertTrue(id+"/"+grade+": "+q.prompt,limits.allows(q));
                if(Set.of("add100","sub100","add1000","sub1000").contains(id)){
                    String[] parts=q.prompt.split(" [+−-] ");int a=Integer.parseInt(parts[0]),b=Integer.parseInt(parts[1]);int maximum=grade==1?99:grade==2?999:9999;
                    int answer=id.startsWith("add")?a+b:a-b;assertTrue(a<=maximum&&b<=maximum&&answer>=0&&answer<=maximum);assertEquals(String.valueOf(answer),q.answers[0]);
                    if(grade==3&&(a>=1000||b>=1000))fourDigit++;
                    assertTrue(new Checker().check(q,List.of(),List.of(String.valueOf(answer))).correct());assertFalse(new Checker().check(q,List.of(),List.of(String.valueOf(answer+1))).correct());
                }
                if(id.equals("tables")||id.equals("divide")){
                    int answer=FactFormsTest.solve(q.prompt);assertEquals(String.valueOf(answer),q.answers[0]);String[] terms=q.prompt.replace("□",String.valueOf(answer)).split(" = ")[0].split(id.equals("tables")?" × ":" ÷ ");int a=Integer.parseInt(terms[0]),b=Integer.parseInt(terms[1]);
                    if(id.equals("tables")){Set<Integer> factors=grade==2?Set.of(2,3,4,5):Set.of(2,3,4,5,10);assertTrue(q.prompt+" grade="+grade,factors.contains(a)||factors.contains(b));assertTrue(q.prompt,a<=10&&b<=10);}else {int quotient=q.prompt.contains(" = ")?Integer.parseInt(q.prompt.split(" = ")[1]):answer;assertTrue(q.prompt,b>=2&&b<=10&&a==quotient*b);}
                }
                if(id.equals("remainder")){String[] terms=q.prompt.split(" ÷ ");int a=Integer.parseInt(terms[0]),b=Integer.parseInt(terms[1]);assertTrue(b>=2&&b<=10);assertEquals(String.valueOf(a/b),q.answers[0]);assertEquals(String.valueOf(a%b),q.answers[1]);}
                if(!q.choices.isEmpty())assertEquals(q.answers[0],q.choices.get(q.correctChoice));recent.add(q.signature());
            }
        }
        assertEquals(29,placements);assertTrue(fourDigit>100);
    }
    @Test public void eachMultiplicationGradeAndDivisionSuppliesOneHundredDistinctEquations(){
        Generator generator=new Generator(new Random(20261006032L));
        for(String id:List.of("tables","divide"))for(int grade:List.of(2,3)){
            if(id.equals("divide")&&grade==2)continue;Set<String> seen=new LinkedHashSet<>();
            for(int i=0;i<100;i++){Question q=generator.next(id,seen,false,GlobalCurriculum.limits(PACK,id,grade));assertTrue("Repeated "+q.prompt,seen.add(q.signature()));}
        }
    }
}
