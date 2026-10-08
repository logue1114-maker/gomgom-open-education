package com.gomgomapps.math.core;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;
import java.util.regex.*;

public class NamibiaPrimaryTest {
    private static final String NA="na-nied-primary-2024-v1";
    private Learning.Profile profile(int grade){Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"NA");GlobalCurriculum.choosePack(p,NA);p.grade=grade;return p;}
    @Test public void primaryScopeAndPreviouslyLearnedDiagnosis(){
        GlobalCurriculum.Pack pack=GlobalCurriculum.pack(profile(1));assertEquals(7,pack.levels().size());
        for(String id:List.of("compare","tables","divide","fractionPart","decimalAdd"))assertFalse(id,pack.inGrade(id,1));
        assertTrue(pack.inGrade("sec_prime_factor",7));
        Learning.State state=new Learning.State();state.profile=profile(3);state.profile.currentSkill="divide";Diagnosis.begin(state,new Random(300),true);
        assertFalse(state.session.diagnosticRun.plan.scope.contains("divide"));
        assertFalse(state.session.diagnosticRun.plan.scope.contains("decimalAdd"));
    }
    @Test public void unmappedIntroductorySkillsDoNotOverrideTheNationalScope(){
        Learning.Profile p=profile(1);p.currentSkill="count";assertTrue(GlobalCurriculum.scope(p).isEmpty());
        p.currentSkill="add20";assertTrue(GlobalCurriculum.scope(p).isEmpty());
        p.learnedSkills.add("count");assertEquals(List.of("count"),GlobalCurriculum.scope(p).stream().map(s->s.id).toList());
        p.learnedSkills.add("compare");assertTrue(GlobalCurriculum.scope(p).stream().anyMatch(s->s.id.equals("compare")));
        p=profile(2);p.currentSkill="add100";assertTrue(GlobalCurriculum.scope(p).stream().anyMatch(s->s.id.equals("count")));assertFalse(GlobalCurriculum.scope(p).stream().anyMatch(s->s.id.equals("compare")));
    }
    @Test public void everyPlacementSuppliesOneHundredValidProblems(){
        Generator generator=new Generator(new Random(20241008));GlobalCurriculum.Pack pack=GlobalCurriculum.pack(profile(1));
        for(int grade:pack.levels())for(String id:pack.grades.keySet())if(pack.inGrade(id,grade))for(int i=0;i<100;i++){
            CurriculumLimits limits=GlobalCurriculum.limits(NA,id,grade);
            Question q=generator.next(id,List.of(),i%2==0,limits);
            assertEquals(id,q.skillId);assertTrue(id+"/"+grade+": "+q.prompt,limits.allows(q));
            assertTrue(id+"/"+grade,new Checker().check(q,List.of(),Arrays.asList(q.answers)).correct());
        }
    }
    @Test public void countingCardsRemainVisibleAndDoNotRepeatFirstHundred(){
        Generator generator=new Generator(new Random(230));List<String> recent=new ArrayList<>();Set<Integer> counts=new HashSet<>();
        for(int i=0;i<600;i++){
            Question q=generator.next("count",recent,false,GlobalCurriculum.limits(NA,"count",1));
            if(i<100)assertFalse(recent.contains(q.signature()));recent.add(q.signature());
            assertEquals("dotCollection",q.diagram.type);int count=(q.diagram.values.length-1)/2;
            assertEquals(count,Integer.parseInt(q.answers[0]));assertTrue(count>=0&&count<=20);counts.add(count);
            Set<String> positions=new HashSet<>();for(int n=1;n<q.diagram.values.length;n+=2){double x=q.diagram.values[n],y=q.diagram.values[n+1];assertTrue(x>0&&x<1&&y>0&&y<1);assertTrue(positions.add(x+","+y));}
        }
        assertTrue(counts.contains(0));assertTrue(counts.contains(20));assertEquals(21,counts.size());
    }
    @Test public void comparisonsAreVariedWithinOneHundredAndDefaultIsPreserved(){
        Generator generator=new Generator(new Random(77));Set<String> seen=new HashSet<>();boolean small=false;
        for(int i=0;i<300;i++){
            Question q=generator.next("el_compare_10000",seen,false,GlobalCurriculum.limits(NA,"el_compare_10000",2));seen.add(q.signature());
            Matcher m=Pattern.compile("\\d+").matcher(q.prompt);List<Integer> values=new ArrayList<>();while(m.find())values.add(Integer.parseInt(m.group()));
            assertEquals(2,values.size());int a=values.get(0),b=values.get(1);assertTrue(a<=100&&b<=100);small|=a<100||b<100;
            assertEquals(a==b?"=":a>b?">":"<",q.answers[0]);
        }
        assertTrue(small);assertTrue(seen.size()>=100);
        for(int i=0;i<30;i++){Question q=generator.next("el_compare_10000",List.of(),false);Matcher m=Pattern.compile("\\d+").matcher(q.prompt);while(m.find())assertTrue(Integer.parseInt(m.group())>=100);}
    }
    @Test public void decimalLimitsRejectExcessPrecisionAndFractionResultExceedingWhole(){
        CurriculumLimits product=GlobalCurriculum.limits(NA,"decimalMul",7),division=GlobalCurriculum.limits(NA,"decimalDiv",7);
        assertFalse(product.allows(new Question("decimalMul","1.11 * 1.11","1.11 * 1.11","1.2321")));
        assertFalse(division.allows(new Question("decimalDiv","1.21 / 1.21","1.21 / 1.21","1")));
        assertTrue(product.allows(new Question("decimalMul","1.2 * 1.11","1.2 * 1.11","1.332")));
        CurriculumLimits fraction=GlobalCurriculum.limits(NA,"fracAddLike",4);
        assertFalse(fraction.allows(new Question("fracAddLike","(3/4) + (3/4)","(3/4) + (3/4)","3/2")));
    }
}
