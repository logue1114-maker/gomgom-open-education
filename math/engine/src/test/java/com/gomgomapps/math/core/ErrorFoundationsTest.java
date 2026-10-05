package com.gomgomapps.math.core;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.*;
import org.junit.Test;
import static org.junit.Assert.*;

/** Registered-flow tests using only the published actual and estimated values. */
public class ErrorFoundationsTest {
    private static BigDecimal read(String p,String label){Matcher m=Pattern.compile(label+": ([0-9]+(?:\\.[0-9]+)?)").matcher(p);assertTrue(p,m.find());return new BigDecimal(m.group(1));}
    private static Rational rational(BigDecimal value){return Expression.number(value.stripTrailingZeros().toPlainString());}
    @Test public void registeredQuestionsPreserveAbsoluteAndPercentageErrorIncludingZero(){
        Generator g=new Generator(new Random(41422027));Checker checker=new Checker();
        for(Catalog.Skill skill:ErrorFoundations.SKILLS){Set<Integer> signs=new HashSet<>(),positions=new HashSet<>(),positiveRanks=new HashSet<>();Set<String> prompts=new HashSet<>();int zeroCount=0;
            for(int i=0;i<2000;i++){
                Question q=g.next(skill.id,List.of(),true);BigDecimal actual=read(q.prompt,"실제값"),estimated=read(q.prompt,"추정값"),difference=estimated.subtract(actual),absolute=difference.abs();
                boolean percent=skill.id.equals("percentageMeasurementError");BigDecimal expected=percent?absolute.divide(actual).multiply(BigDecimal.valueOf(100)):absolute;Rational answer=rational(expected);
                assertTrue(actual.signum()>0&&estimated.signum()>0);assertEquals(answer,Expression.number(q.answers[0]));assertEquals(answer,Expression.number(q.expression));assertTrue(checker.check(q,List.of(),List.of(expected.toPlainString())).correct());assertFalse(checker.check(q,List.of(),List.of(expected.add(BigDecimal.ONE).toPlainString())).correct());
                HelpPlan help=HelpPlan.forQuestion(q);List<BigDecimal> stages=new ArrayList<>(List.of(difference,absolute));if(percent){stages.add(absolute.divide(actual));stages.add(expected);}assertFalse(help.canTransfer());assertEquals(stages.size(),help.size());for(int j=0;j<stages.size();j++){assertTrue(help.step(j).accepts(stages.get(j).toPlainString()));assertFalse(help.step(j).accepts(stages.get(j).add(BigDecimal.ONE).toPlainString()));}
                assertEquals(4,q.choices.size());Set<Rational> distinct=new HashSet<>();int rank=0;for(String choice:q.choices){Rational v=Expression.number(choice);assertTrue(v.compareTo(Rational.ZERO)>=0);assertEquals(answer.isInteger(),v.isInteger());assertTrue(v.div(MassDensity.choiceUnit(answer)).isInteger());assertTrue(distinct.add(v));if(v.compareTo(answer)<0)rank++;}assertEquals(answer,Expression.number(q.choices.get(q.correctChoice)));
                if(answer.isZero()){zeroCount++;assertEquals(0,rank);}else positiveRanks.add(rank);positions.add(q.correctChoice);signs.add(difference.signum());prompts.add(q.signature());
            }
            assertTrue(zeroCount>0);assertEquals(Set.of(-1,0,1),signs);assertEquals(Set.of(0,1,2,3),positions);assertEquals(Set.of(0,1,2,3),positiveRanks);assertTrue(prompts.size()>100);
        }
    }
    @Test public void kenyaLearnedScopeAndHundredDistinctProblemsDoNotChangeKoreanDiagnosis(){
        Learning.Profile ke=new Learning.Profile();GlobalCurriculum.chooseCountry(ke,"KE");GlobalCurriculum.choosePack(ke,"ke-kicd-cbc-2024-v1");ke.grade=9;GlobalCurriculum.Pack pack=GlobalCurriculum.pack(ke);Learning.State kr=new Learning.State();kr.profile.grade=10;kr.profile.term=2;kr.profile.curriculum=2022;Generator g=new Generator(new Random(41422028));
        for(Catalog.Skill skill:ErrorFoundations.SKILLS){assertTrue(pack.inGrade(skill.id,9));assertFalse(Learning.diagnosticScope(ke).contains(skill));ke.learnedSkills.add(skill.id);assertTrue(Learning.diagnosticScope(ke).contains(skill));assertFalse(Curriculum.inCurriculum(skill,2015));assertFalse(Curriculum.inCurriculum(skill,2022));assertFalse(Learning.diagnosticScope(kr.profile).contains(skill));assertFalse(Learning.learningScope(kr.profile).contains(skill));assertTrue(Learning.beginPractice(kr,"practice",List.of(skill.id),1,false,new Random(41422029)).selected.contains(skill.id));kr.profile.currentSkill=skill.id;assertTrue(Learning.learningScope(kr.profile).contains(skill));kr.profile.currentSkill="";LinkedList<String> recent=new LinkedList<>();Set<String> distinct=new HashSet<>();for(int i=0;i<100;i++){Question q=g.next(skill.id,recent,i%2==0,GlobalCurriculum.limits(pack.id,skill.id,9));assertTrue(distinct.add(q.signature()));recent.add(q.signature());}}
    }
}
