package com.gomgomapps.math.core;

import java.util.*;

/** Exact-value alternatives for new short calculation drills, shuffled independently on every question. */
final class FoundationChoices {
    private FoundationChoices(){}
    static void build(Question q,Catalog.Skill skill,Random random){
        if(!q.kind.equals("number")||q.answers.length!=1||!q.choices.isEmpty())return;
        Rational answer=Expression.number(q.answers[0]);LinkedHashMap<Rational,String> pool=new LinkedHashMap<>();
        boolean probability=skill.id.toLowerCase(Locale.ROOT).contains("probability")||skill.id.equals("distributionMissing");
        boolean spread=Set.of("sec_variance","sec_standard_deviation").contains(skill.id);
        List<Rational> candidates=new ArrayList<>();
        // A learner may stop at an intermediate value. Only plausible domain-compatible values survive below.
        if(q.studyGuide!=null)for(StudyGuide.Frame frame:q.studyGuide.frames)try{candidates.add(Expression.number(frame.expected));}catch(RuntimeException ignored){}
        if(probability){for(int den=2;den<=20;den++)for(int num=1;num<den;num++)candidates.add(Rational.of(num,den));}
        else{
            for(int delta=1;delta<=16;delta++){candidates.add(answer.add(Rational.of(delta)));candidates.add(answer.sub(Rational.of(delta)));}
            candidates.add(answer.neg());candidates.add(answer.mul(Rational.of(2)));candidates.add(answer.div(Rational.of(2)));
            if(!answer.isInteger())for(int den=2;den<=12;den++){candidates.add(answer.add(Rational.of(1,den)));candidates.add(answer.sub(Rational.of(1,den)));}
        }
        Collections.shuffle(candidates,random);
        for(Rational value:candidates){
            if(value.equals(answer)||value.isInteger()!=answer.isInteger())continue;
            if(skill.grade<=6&&value.compareTo(Rational.ZERO)<0)continue;
            if(spread&&value.compareTo(Rational.ZERO)<0)continue;
            if(probability&&(value.compareTo(Rational.ZERO)<0||value.compareTo(Rational.ONE)>0))continue;
            if(q.decimal&&value.decimalText().contains("/"))continue;
            if(Set.of("el_clock_hour","earlyClock").contains(skill.id)&&(value.compareTo(Rational.ONE)<0||value.compareTo(Rational.of(12))>0))continue;
            if(skill.id.equals("earlyClassify")&&value.compareTo(Rational.of(q.diagram.values.length))>0)continue;
            if(skill.id.equals("earlyStacks")&&value.compareTo(Rational.of(9))>0)continue;
            if(skill.id.equals("el_clock_minute")&&(value.compareTo(Rational.ZERO)<0||value.compareTo(Rational.of(59))>0))continue;
            pool.put(value,probability?"기준이 되는 전체와 해당 경우의 수 확인":spread?"평균·편차 제곱·자료 개수 확인":"식과 계산 순서 확인");
        }
        if(pool.size()<3)return;
        List<Rational> options=new ArrayList<>(pool.keySet());Collections.shuffle(options,random);options=new ArrayList<>(options.subList(0,3));options.add(answer);Collections.shuffle(options,random);
        for(Rational value:options){if(value.equals(answer))q.correctChoice=q.choices.size();String text=q.decimal?value.decimalText():value.toString();if(q.decimal&&!text.contains("."))text+=".0";q.choices.add(text);q.distractorReasons.add(value.equals(answer)?"정답":pool.get(value));}
    }
}
