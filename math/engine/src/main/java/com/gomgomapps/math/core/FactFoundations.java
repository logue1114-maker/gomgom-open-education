package com.gomgomapps.math.core;

import java.util.*;

/** Equivalent fact practice; no larger operands are introduced to inflate supply. */
final class FactFoundations {
    private FactFoundations(){}
    static boolean blank(Question q){return Set.of("tables","divide").contains(q.skillId)&&q.prompt.contains("□");}
    static Question next(Catalog.Skill skill,Random random,CurriculumLimits limits,Map<String,Integer> recent){
        LinkedHashMap<String,Question> candidates=new LinkedHashMap<>();int maximum=limits.timesTableMax();boolean multiply=skill.id.equals("tables");
        for(int a=multiply?0:limits.divisionMinQuotient();a<=maximum;a++)for(int b=multiply?0:limits.divisionMinDivisor();b<=maximum;b++)for(int form=0;form<3;form++){
            Question q=make(skill,a,b,form,limits);if(limits.allows(q))candidates.putIfAbsent(q.signature(),q);
        }
        return choose(candidates,random,recent);
    }
    static Question choose(Map<String,Question> candidates,Random random,Map<String,Integer> recent){
        List<Question> fresh=candidates.values().stream().filter(q->!recent.containsKey(q.signature())).toList();
        if(!fresh.isEmpty())return fresh.get(random.nextInt(fresh.size()));
        int oldest=candidates.keySet().stream().mapToInt(key->recent.getOrDefault(key,-1)).min().orElseThrow(()->new IllegalStateException("No fact matches curriculum limits"));
        List<Question> available=candidates.values().stream().filter(q->recent.getOrDefault(q.signature(),-1)==oldest).toList();return available.get(random.nextInt(available.size()));
    }
    static Question create(Catalog.Skill skill,Random random,CurriculumLimits limits){
        int maximum=limits.timesTableMax();boolean multiply=skill.id.equals("tables");
        int a=multiply?random.nextInt(maximum+1):limits.divisionMinQuotient()+random.nextInt(maximum-limits.divisionMinQuotient()+1);
        int b=multiply?random.nextInt(maximum+1):limits.divisionMinDivisor()+random.nextInt(maximum-limits.divisionMinDivisor()+1);
        return make(skill,a,b,random.nextInt(3),limits);
    }
    private static Question make(Catalog.Skill skill,int a,int b,int form,CurriculumLimits limits){
        int maximum=limits.timesTableMax();boolean multiply=skill.id.equals("tables");
        int dividend=a*b;
        String original=multiply?a+" × "+b:dividend+" ÷ "+b;
        Question base=new Question(skill.id,original,original,String.valueOf(multiply?dividend:a)).withInputs(multiply?a:dividend,b);
        // Check the entire original fact before choosing a missing term.
        if(!limits.allows(base)||a==0||b==0)return base;
        if(form==0)return base;
        String prompt,expression;int answer;StudyGuide guide=new StudyGuide().transfer(false);
        if(multiply){
            int known=form==1?a:b;answer=form==1?b:a;
            prompt=form==1?a+" × □ = "+dividend:"□ × "+b+" = "+dividend;
            expression=dividend+" / "+known;
            guide.step("곱을 알려진 수로 나누세요.",dividend+" ÷ "+known+" = ","",String.valueOf(answer));
        }else if(form==1){
            answer=dividend;prompt="□ ÷ "+b+" = "+a;expression=a+" * "+b;
            guide.step("나눗셈을 곱셈으로 바꾸세요.",a+" × "+b+" = ","",String.valueOf(answer));
        }else{
            answer=b;prompt=dividend+" ÷ □ = "+a;expression=dividend+" / "+a;
            guide.step("나눗셈을 곱셈으로 바꾸세요.",a+" × "," = "+dividend,String.valueOf(answer));
        }
        Question q=new Question(skill.id,prompt,expression,String.valueOf(answer));q.stepSupport=false;q.studyGuide=guide;
        return q.withInputs(maximum,form,multiply?1:0,a,b);
    }
    static void choices(Question q,Random random){
        int answer=Integer.parseInt(q.answers[0]);int maximum=q.choiceInputs[0].n.intValue();
        boolean dividend=q.skillId.equals("divide")&&q.prompt.startsWith("□");
        int ceiling=dividend?maximum*maximum:maximum;
        int unit=dividend?q.choiceInputs[4].n.intValue():1;
        LinkedHashSet<Integer> wrong=new LinkedHashSet<>();
        for(int delta=1;delta<=maximum&&wrong.size()<6;delta++)for(int sign:List.of(-1,1)){
            int value=answer+sign*delta*unit;if(value>=0&&value<=ceiling&&value!=answer)wrong.add(value);
        }
        if(wrong.size()<3)return;
        List<Integer> pool=new ArrayList<>(wrong);Collections.shuffle(pool,random);List<Integer> options=new ArrayList<>(pool.subList(0,3));options.add(answer);Collections.shuffle(options,random);
        for(int value:options){if(value==answer)q.correctChoice=q.choices.size();q.choices.add(String.valueOf(value));q.distractorReasons.add(value==answer?"정답":"곱셈·나눗셈 관계 확인");}
    }
}
