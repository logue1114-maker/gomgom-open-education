package com.gomgomapps.math.core;

import java.util.*;
import java.util.regex.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class MassDensityTest {
    private static final String NUM="([0-9]+(?:\\.[0-9]+)?)";
    static Matcher match(String p,String regex){Matcher m=Pattern.compile(regex).matcher(p);assertTrue(p+" : "+regex,m.find());return m;}
    static Rational massScale(String unit){return switch(unit){case "mg"->Rational.of(1,1000);case "g"->Rational.ONE;case "kg"->Rational.of(1000);case "t"->Rational.of(1000000);default->throw new IllegalArgumentException(unit);};}
    static Rational volumeScale(String unit){return switch(unit){case "mL","cm³"->Rational.ONE;case "L"->Rational.of(1000);case "m³"->Rational.of(1000000);default->throw new IllegalArgumentException(unit);};}
    static List<Rational> solve(Question q,Set<String> cases){
        String p=q.prompt;List<Rational> steps=new ArrayList<>();
        if(q.skillId.endsWith("UnitConvert")){
            Matcher m=match(p,NUM+"(mg|kg|t|g|m³|cm³|mL|L) = □(mg|kg|t|g|m³|cm³|mL|L)");Rational given=Expression.number(m.group(1));String from=m.group(2),to=m.group(3);boolean mass=q.skillId.equals("massUnitConvert");Rational a=mass?massScale(from):volumeScale(from),b=mass?massScale(to):volumeScale(to);Rational ratio=a.div(b),factor=ratio.compareTo(Rational.ONE)>=0?ratio:Rational.ONE.div(ratio);cases.add(from+"→"+to);steps.add(factor);steps.add(given.mul(ratio));
        }else if(q.skillId.equals("massWeight")){
            Rational gravity=Expression.number(match(p,"중력계수 "+NUM+"N/kg").group(1));assertTrue(gravity.equals(Rational.of(10))||gravity.equals(Rational.of(49,5)));boolean weight=p.contains("무게는 몇");String wanted=match(p,"(?:무게|질량)[은는] 몇 (kg|g|N)인가요").group(1);cases.add("weight:"+gravity+":"+weight+":"+(p.contains("1kg=1000g")?"g":"kg"));
            if(weight){Matcher m=match(p,"\n질량 "+NUM+"(kg|g)");Rational kg=Expression.number(m.group(1)).mul(massScale(m.group(2))).div(Rational.of(1000));steps.add(kg);steps.add(kg.mul(gravity));}
            else {Rational force=Expression.number(match(p,"\n무게 "+NUM+"N").group(1)),kg=force.div(gravity);steps.add(kg);steps.add(kg.mul(Rational.of(1000)).div(massScale(wanted)));}
        }else{
            Matcher wanted=match(p,"(?:밀도|질량|부피)[은는] 몇 ([^\n]+)인가요");String target=wanted.group(1),mu,vu;Rational rho=null,m=null,v=null;
            if(q.skillId.equals("densityValue")){String[] units=target.split("/");mu=units[0];vu=units[1];}
            else {Matcher density=match(p,"밀도 "+NUM+"(kg|g)/(m³|cm³)");rho=Expression.number(density.group(1));mu=density.group(2);vu=density.group(3);}
            if(!q.skillId.equals("densityMass")){Matcher mass=match(p,"질량 "+NUM+"(kg|g)");m=Expression.number(mass.group(1)).mul(massScale(mass.group(2))).div(massScale(mu));cases.add(q.skillId+":mass:"+mass.group(2)+"→"+mu);steps.add(m);}
            if(!q.skillId.equals("densityVolume")){Matcher volume=match(p,"부피 "+NUM+"(m³|cm³|L)");v=Expression.number(volume.group(1)).mul(volumeScale(volume.group(2))).div(volumeScale(vu));cases.add(q.skillId+":volume:"+volume.group(2)+"→"+vu);steps.add(v);}
            if(q.skillId.equals("densityValue"))steps.add(m.div(v));
            else if(q.skillId.equals("densityMass")){assertEquals(mu,target);steps.add(rho.mul(v));}
            else {assertEquals(vu,target);steps.add(m.div(rho));}
        }
        if(q.skillId.equals("volumeUnitConvert")&&p.contains("1mL=1cm³"))assertEquals("크기가 같은 단위이므로 수는 그대로 쓰세요.",q.studyGuide.frames.get(1).instruction);
        return steps;
    }
    @Test public void publicUnitsAndRelationsDetermineAllAnswersAndStudentSteps(){
        Generator generator=new Generator(new Random(35673));Checker checker=new Checker();Set<String> cases=new HashSet<>();
        for(Catalog.Skill skill:MassDensity.SKILLS){Set<Integer> positions=new HashSet<>(),ranks=new HashSet<>();
            for(int i=0;i<2000;i++){
                Question q=generator.next(skill.id,List.of(),true);List<Rational> steps=solve(q,cases);Rational answer=steps.get(steps.size()-1);assertTrue(answer.compareTo(Rational.ZERO)>0);assertEquals(skill.id,answer,Expression.number(q.expression));assertTrue(checker.check(q,List.of(),List.of(answer.toString())).correct());assertFalse(checker.check(q,List.of(),List.of(answer.add(Rational.ONE).toString())).correct());
                HelpPlan plan=HelpPlan.forQuestion(q);assertFalse(plan.canTransfer());assertEquals(steps.size(),plan.size());for(int j=0;j<steps.size();j++){assertTrue(skill.id+" stage"+j,plan.step(j).accepts(steps.get(j).toString()));assertFalse(plan.step(j).accepts(steps.get(j).add(Rational.ONE).toString()));}
                assertEquals(4,q.choices.size());assertEquals(4,new HashSet<>(q.choices).size());assertEquals(answer,Expression.number(q.choices.get(q.correctChoice)));positions.add(q.correctChoice);int rank=0;for(String option:q.choices){Rational value=Expression.number(option);assertTrue(value.compareTo(Rational.ZERO)>0);assertEquals(answer.isInteger(),value.isInteger());if(q.prompt.contains("밀도는 몇 kg/m³"))assertTrue("Only the answer must not be a multiple of 100: "+q.choices,value.div(Rational.of(100)).isInteger());if(q.skillId.equals("massUnitConvert")&&answer.compareTo(Rational.of(1000))>0)assertTrue("Converted large-unit answers must not alone end in 00",value.div(Rational.of(100)).isInteger());if(value.compareTo(answer)<0)rank++;}ranks.add(rank);
            }
            assertEquals(Set.of(0,1,2,3),positions);assertEquals(Set.of(0,1,2,3),ranks);
        }
        assertTrue(cases.containsAll(Set.of("g→mg","mg→g","kg→g","g→kg","t→kg","kg→t","m³→cm³","cm³→m³","L→cm³","cm³→L","L→mL","mL→L","mL→cm³","cm³→mL")));
        for(String gravity:List.of("10","49/5"))for(boolean forward:List.of(true,false))for(String unit:List.of("g","kg"))assertTrue(cases.contains("weight:"+gravity+":"+forward+":"+unit));
        for(String id:List.of("densityValue","densityVolume"))assertTrue(cases.contains(id+":mass:kg→g"));for(String id:List.of("densityValue","densityMass"))assertTrue(cases.contains(id+":volume:L→cm³"));
    }
    @Test public void kenyaMappingsDoNotBecomeUnreviewedKoreanAutomaticDiagnosis(){
        Learning.State kr=new Learning.State();kr.profile.grade=10;kr.profile.term=2;kr.profile.schoolYear=2026;kr.profile.curriculum=2022;
        for(Catalog.Skill skill:MassDensity.SKILLS){
            assertFalse(Curriculum.inCurriculum(skill,2015));assertFalse(Curriculum.inCurriculum(skill,2022));assertFalse(Learning.diagnosticScope(kr.profile).contains(skill));assertFalse(Learning.learningScope(kr.profile).contains(skill));assertTrue(GlobalCurriculum.available(kr.profile).contains(skill));
            assertTrue(Learning.beginPractice(kr,"practice",List.of(skill.id),1,false,new Random(35675)).selected.contains(skill.id));kr.profile.currentSkill=skill.id;assertTrue(Learning.learningScope(kr.profile).contains(skill));kr.profile.currentSkill="";
        }
        Learning.Profile ke=new Learning.Profile();GlobalCurriculum.chooseCountry(ke,"KE");GlobalCurriculum.choosePack(ke,"ke-kicd-cbc-2024-v1");ke.grade=9;
        for(Catalog.Skill skill:MassDensity.SKILLS){assertTrue(GlobalCurriculum.available(ke).contains(skill));assertFalse(Learning.diagnosticScope(ke).contains(skill));ke.learnedSkills.add(skill.id);assertTrue(Learning.diagnosticScope(ke).contains(skill));}
    }
    @Test public void sixKenyaPlacementsSupplyOneHundredDistinctPublishedProblems(){
        Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"KE");GlobalCurriculum.choosePack(p,"ke-kicd-cbc-2024-v1");GlobalCurriculum.Pack pack=GlobalCurriculum.pack(p);Generator generator=new Generator(new Random(35674));
        for(Catalog.Skill skill:MassDensity.SKILLS){assertTrue(pack.inGrade(skill.id,9));Set<String> seen=new HashSet<>();LinkedList<String> recent=new LinkedList<>();for(int i=0;i<100;i++){Question q=generator.next(skill.id,recent,i%2==0,GlobalCurriculum.limits(pack.id,skill.id,9));assertTrue(skill.id,seen.add(q.signature()));recent.add(q.signature());}}
    }
}
