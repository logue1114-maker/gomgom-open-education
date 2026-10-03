package com.gomgomapps.math.core;

import java.util.*;
import java.util.regex.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class MoneyFoundationsTest {
    private static final String NUM="([0-9]+(?:\\.[0-9]+)?)";
    private static Matcher match(String s,String pattern){Matcher m=Pattern.compile(pattern).matcher(s);assertTrue(s+" : "+pattern,m.find());return m;}
    private static Rational number(String s){return Expression.number(s);}
    static List<Rational> steps(Question q,Set<String> branches){
        String p=q.prompt;assertTrue(p.contains("학습용"));List<Rational> result=new ArrayList<>();
        if(q.skillId.equals("currencyExchange")){
            Matcher rate=match(p,"1(USD|EUR|JPY|GBP|UGX|TZS)="+NUM+"KSh"),given=match(p,"바꿀 금액: "+NUM+"(KSh|USD|EUR|JPY|GBP|UGX|TZS)");boolean toKsh=!given.group(2).equals("KSh");assertTrue(p.contains("수수료는 없습니다."));assertTrue(p.contains("몇 "+(toKsh?"KSh":rate.group(1))+"인가요?"));branches.add("currency:"+rate.group(1));branches.add("direction:"+toKsh);Rational amount=number(given.group(1)),factor=number(rate.group(2));result.add(toKsh?amount.mul(factor):amount.div(factor));assertEquals(toKsh?"KSh":rate.group(1),q.labels[0]);
        }else{
            assertTrue(p.contains("다른 세금·비용은 없습니다."));Matcher rate=match(p,"율: "+NUM+"%"),given=match(p,"(?:과세가격|VAT 전 가격|VAT 포함 가격): "+NUM+"KSh");Rational amount=number(given.group(1)),fraction=number(rate.group(1)).div(Rational.of(100));result.add(fraction);
            if(q.skillId.equals("vatIncluded")){Rational multiplier=Rational.ONE.add(fraction),base=amount.div(multiplier);result.add(multiplier);result.add(base);boolean vat=p.contains("포함된 VAT는");if(vat)result.add(amount.sub(base));branches.add("included-vat:"+vat);assertTrue(base.compareTo(amount)<0);}
            else{Rational tax=amount.mul(fraction);result.add(tax);boolean total=p.contains("VAT 포함 가격은");if(total)result.add(amount.add(tax));if(q.skillId.equals("vatAmount"))branches.add("vat-total:"+total);if(q.skillId.equals("importDuty"))assertTrue(p.contains("통관 과세가격:"));}
            assertEquals("KSh",q.labels[0]);
        }
        return result;
    }
    @Test public void publicPracticeRatesAndTaxBasesDetermineAnswersGuidesAndAllChoices(){
        Generator g=new Generator(new Random(39540));Checker checker=new Checker();Set<String> branches=new HashSet<>();
        for(Catalog.Skill skill:MoneyFoundations.SKILLS){Set<Integer> positions=new HashSet<>(),ranks=new HashSet<>();Set<String> answers=new HashSet<>();
            for(int i=0;i<2000;i++){
                Question q=g.next(skill.id,List.of(),true);List<Rational> expected=steps(q,branches);Rational answer=expected.get(expected.size()-1);answers.add(answer.toString());assertEquals(answer,number(q.expression));assertEquals(answer,number(q.answers[0]));assertFalse(q.answers[0].contains("/"));assertTrue(answer.compareTo(Rational.ZERO)>0);assertTrue(checker.check(q,List.of(),List.of(answer.toString())).correct());assertEquals(Checker.Status.WRONG_ANSWER,checker.check(q,List.of(),List.of(answer.add(Rational.ONE).toString())).status);
                HelpPlan plan=HelpPlan.forQuestion(q);assertFalse(plan.canTransfer());assertEquals(expected.size(),plan.size());for(int j=0;j<expected.size();j++){assertTrue(plan.step(j).accepts(expected.get(j).toString()));assertFalse(plan.step(j).accepts(expected.get(j).add(Rational.ONE).toString()));}
                assertEquals(4,q.choices.size());Set<Rational> distinct=new HashSet<>();int rank=0;for(String choice:q.choices){Rational value=number(choice);distinct.add(value);assertTrue(value.compareTo(Rational.ZERO)>0);assertEquals(answer.isInteger(),value.isInteger());assertTrue(value.div(MassDensity.choiceUnit(answer)).isInteger());if(value.compareTo(answer)<0)rank++;}assertEquals(4,distinct.size());assertEquals(answer,number(q.choices.get(q.correctChoice)));positions.add(q.correctChoice);ranks.add(rank);
            }
            assertTrue(answers.size()>100);assertEquals(Set.of(0,1,2,3),positions);assertEquals(Set.of(0,1,2,3),ranks);
        }
        assertTrue(branches.containsAll(Set.of("direction:true","direction:false","vat-total:true","vat-total:false","included-vat:true","included-vat:false")));for(String code:List.of("USD","EUR","JPY","GBP","UGX","TZS"))assertTrue(branches.contains("currency:"+code));
    }
    @Test public void selectedKenyaLearningAndSupplyDoNotBecomeKoreanAutomaticMappings(){
        Learning.Profile ke=new Learning.Profile();GlobalCurriculum.chooseCountry(ke,"KE");GlobalCurriculum.choosePack(ke,"ke-kicd-cbc-2024-v1");ke.grade=9;GlobalCurriculum.Pack pack=GlobalCurriculum.pack(ke);Generator g=new Generator(new Random(39541));Learning.State kr=new Learning.State();kr.profile.grade=10;kr.profile.term=2;kr.profile.curriculum=2022;
        for(Catalog.Skill skill:MoneyFoundations.SKILLS){assertTrue(pack.inGrade(skill.id,9));assertFalse(Learning.diagnosticScope(ke).contains(skill));ke.learnedSkills.add(skill.id);assertTrue(Learning.diagnosticScope(ke).contains(skill));assertFalse(Curriculum.inCurriculum(skill,2015));assertFalse(Curriculum.inCurriculum(skill,2022));assertFalse(Learning.diagnosticScope(kr.profile).contains(skill));assertFalse(Learning.learningScope(kr.profile).contains(skill));assertTrue(Learning.beginPractice(kr,"practice",List.of(skill.id),1,false,new Random(39542)).selected.contains(skill.id));kr.profile.currentSkill=skill.id;assertTrue(Learning.learningScope(kr.profile).contains(skill));kr.profile.currentSkill="";Set<String> seen=new HashSet<>();LinkedList<String> recent=new LinkedList<>();for(int i=0;i<100;i++){Question q=g.next(skill.id,recent,i%2==0,GlobalCurriculum.limits(pack.id,skill.id,9));assertTrue(seen.add(q.signature()));recent.add(q.signature());}}
    }
}
