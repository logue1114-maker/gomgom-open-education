package com.gomgomapps.math.core;

import java.util.*;

/** Constant, equal per-person rates; exact ratios, with student-completed frames. */
public final class RateFoundations {
    private RateFoundations(){}
    public static final List<Catalog.Skill> SKILLS=List.of(
        new Catalog.Skill("compoundProportion","인원·시간·작업량의 비례",9,1,3,"","compoundProportion",100,"proportion,sec_inverse_proportion","한 사람의 작업 속도가 일정하고 모두 같을 때 작업량은 인원과 시간에 각각 정비례한다. 같은 작업량을 끝내는 시간은 인원에 반비례한다."),
        new Catalog.Skill("combinedWorkTime","함께 일할 때 걸리는 시간",9,1,3,"","combinedWorkTime",100,"fracAdd,fracDiv","같은 일을 각자 끝내는 시간이 주어지면 한 시간에 하는 일의 비율을 더한다. 전체 일을 끝내는 시간은 합친 작업률의 역수다.")
    );
    public static boolean supports(String id){return SKILLS.stream().anyMatch(s->s.id.equals(id));}
    private static int n(Random r,int lo,int hi){return lo+r.nextInt(hi-lo+1);}
    static Question create(Catalog.Skill s,Random r){
        if(s.id.equals("compoundProportion"))return compound(s,r);
        int a=n(r,8,24),b=n(r,8,24);return work(s,Math.min(a,b),Math.max(a,b));
    }
    static Question nextWork(Catalog.Skill s,Random r,CurriculumLimits limits,Map<String,Integer> recent){
        List<int[]> pairs=new ArrayList<>();for(int a=8;a<=24;a++)for(int b=a;b<=24;b++)pairs.add(new int[]{a,b});Collections.shuffle(pairs,r);
        Question oldest=null;int age=Integer.MAX_VALUE;
        for(int[] pair:pairs){Question q=work(s,pair[0],pair[1]);if(!limits.allows(q))continue;Integer index=recent.get(q.signature());if(index==null)return q;if(index<age){oldest=q;age=index;}}
        return oldest;
    }
    private static Question compound(Catalog.Skill s,Random r){
        int workers=n(r,2,12),hours=n(r,2,12),rate=n(r,1,9),newWorkers=n(r,2,12),newHours=n(r,4,12);
        while(newWorkers==workers)newWorkers=n(r,2,12);
        while(newHours==hours)newHours=n(r,4,12);
        int amount=workers*hours*rate,newAmount=newWorkers*newHours*rate;boolean findTime=r.nextBoolean();
        String prompt="각 사람의 작업 속도는 일정하고 모두 같습니다.\n"+workers+"명이 "+hours+"시간에 "+amount+"개를 만듭니다.\n"+newWorkers+"명이 "+(findTime?newAmount+"개를 만드는 데 몇 시간이 걸리나요?":newHours+"시간에 만드는 개수는?");
        Rational people=Rational.of(findTime?workers:newWorkers,findTime?newWorkers:workers),other=Rational.of(findTime?newAmount:newHours,findTime?amount:hours);
        int original=findTime?hours:amount,answer=findTime?newHours:newAmount;
        String expression=original+" * ("+people+") * ("+other+")";
        Question q=new Question(s.id,prompt,expression,""+answer);q.labels=new String[]{findTime?"시간 (시간)":"개수 (개)"};
        q.studyGuide=new StudyGuide().step(findTime?"인원이 늘면 시간이 줄어듭니다. 인원의 역비를 쓰세요.":"인원이 늘면 작업량도 늘어납니다. 인원의 비를 쓰세요.",
            (findTime?workers:newWorkers)+" ÷ "+(findTime?newWorkers:workers)+" = ","",people.toString())
            .step(findTime?"만들 개수의 비를 쓰세요.":"작업 시간의 비를 쓰세요.",(findTime?newAmount:newHours)+" ÷ "+(findTime?amount:hours)+" = ","",other.toString())
            .step("처음 값에 두 비율을 곱하세요.",original+" × ("+people+") × ("+other+") = ","",String.valueOf(answer)).transfer(false);
        return q.withInputs(workers,hours,amount,newWorkers,findTime?newAmount:newHours,findTime?1:0);
    }
    private static Question work(Catalog.Skill s,int a,int b){
        Rational first=Rational.of(1,a),second=Rational.of(1,b),sum=first.add(second),time=Rational.ONE.div(sum);
        String prompt="같은 일을 A는 혼자 "+a+"시간, B는 혼자 "+b+"시간에 끝냅니다.\n각자의 작업 속도는 일정합니다.\n둘이 동시에 시작하면 몇 시간이 걸리나요?";
        Question q=new Question(s.id,prompt,"1 / (1 / "+a+" + 1 / "+b+")",time.toString());q.labels=new String[]{"시간 (시간)"};
        q.studyGuide=new StudyGuide().step("A가 한 시간에 하는 일의 비율을 쓰세요.","1 ÷ "+a+" = ","",first.toString())
            .step("B가 한 시간에 하는 일의 비율을 쓰세요.","1 ÷ "+b+" = ","",second.toString())
            .step("한 시간에 하는 일의 비율을 더하세요.",first+" + "+second+" = ","",sum.toString())
            .step("전체 일을 합친 작업률로 나누세요.","1 ÷ ("+sum+") = "," 시간",time.toString()).transfer(false);
        return q.withInputs(a,b);
    }
    static Map<Rational,String> errors(Question q){
        Map<Rational,String> wrong=new LinkedHashMap<>();Rational answer=Expression.number(q.answers[0]);Rational[] in=q.choiceInputs;
        if(q.skillId.equals("combinedWorkTime")){
            wrong.put(in[0].add(in[1]),"각자 걸리는 시간을 더함");wrong.put(in[0].add(in[1]).div(Rational.of(2)),"시간의 평균을 구함");wrong.put(Rational.ONE.div(in[0]).add(Rational.ONE.div(in[1])),"작업률의 역수를 구하지 않음");
        }else{
            boolean time=in[5].intValue()==1;Rational original=time?in[1]:in[2],people=time?in[0].div(in[3]):in[3].div(in[0]),other=time?in[4].div(in[2]):in[4].div(in[1]);
            wrong.put(original.mul(people),"두 번째 비율을 곱하지 않음");wrong.put(original.mul(other),"인원의 비율을 곱하지 않음");wrong.put(original.div(people).mul(other),"인원의 정비례와 반비례를 바꾸어 적용함");
        }
        Rational step=answer.isInteger()?Rational.ONE:Rational.of(1,answer.d.longValueExact());
        for(int i=1;i<=4;i++){wrong.put(answer.add(step.mul(Rational.of(i))),"분자 또는 일의 자리 계산 오류");wrong.put(answer.sub(step.mul(Rational.of(i))),"분자 또는 일의 자리 계산 오류");}
        return wrong;
    }
}
