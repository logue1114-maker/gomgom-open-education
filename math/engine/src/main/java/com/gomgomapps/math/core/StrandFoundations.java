package com.gomgomapps.math.core;

import java.util.*;

/** Student-completed frames for common factors, cancellation and observed frequencies. */
public final class StrandFoundations {
    private StrandFoundations(){}
    public static final List<Catalog.Skill> SKILLS=List.of(
        new Catalog.Skill("commonFactorFrame","공통인수로 묶기",8,1,2,"","commonFactorFrame",100,"gcd,likeTerms","각 항의 계수에서 가장 큰 양의 정수 공통인수를 찾아 괄호 밖으로 묶는다."),
        new Catalog.Skill("algebraCancel","문자식 분수의 약분",8,1,2,"","algebraCancel",100,"likeTerms,fracDiv","동류항의 계수를 계산한 뒤 분자와 분모의 0이 아닌 공통인수를 약분한다."),
        new Catalog.Skill("experimentalProbability","실험 결과의 확률",8,2,4,"","experimentalProbability",100,"el_ratio_fraction,percent","실험에서 사건이 일어난 횟수를 전체 실험 횟수로 나눈다. 실험 결과는 이론적인 확률과 다를 수 있다.")
    );
    public static boolean supports(String id){return SKILLS.stream().anyMatch(s->s.id.equals(id));}
    private static int n(Random r,int lo,int hi){return lo+r.nextInt(hi-lo+1);}
    static Question create(Catalog.Skill s,Random r){
        return switch(s.id){
            case "commonFactorFrame"->factor(s,r);
            case "algebraCancel"->cancel(s,r);
            case "experimentalProbability"->experimental(s,r);
            default->throw new IllegalArgumentException("Unknown strand foundation: "+s.id);
        };
    }
    private static Question factor(Catalog.Skill s,Random r){
        int a=n(r,1,30),b=n(r,1,30)*(r.nextBoolean()?1:-1);
        while(!java.math.BigInteger.valueOf(a).gcd(java.math.BigInteger.valueOf(b)).equals(java.math.BigInteger.ONE))b=n(r,1,30)*(r.nextBoolean()?1:-1);
        int common=n(r,2,20),left=common*a,right=common*b;
        String expression=left+"x + ("+right+")";
        Question q=new Question(s.id,expression+" = a(bx + c)\na는 가장 큰 양의 정수 공통인수입니다. a, b, c를 구하세요.",expression,""+common,""+a,""+b);
        q.kind="pair";q.labels=new String[]{"a","b","c"};
        q.studyGuide=new StudyGuide().step("두 계수의 최대공약수를 구하세요.","gcd("+left+", "+Math.abs(right)+") = ","",""+common)
            .step("x의 계수를 공통인수로 나누세요.",left+" ÷ "+common+" = ","",""+a)
            .step("상수항을 공통인수로 나누세요.",right+" ÷ "+common+" = ","",""+b).transfer(false);
        return q.withInputs(left,right);
    }
    private static Question cancel(Catalog.Skill s,Random r){
        int a=n(r,1,99)*(r.nextBoolean()?1:-1),b=n(r,1,99)*(r.nextBoolean()?1:-1),den=n(r,2,50),power=n(r,1,3);
        String variable=power==1?"x":"x^"+power;
        String expression="("+a+variable+" + ("+b+variable+")) / ("+den+variable+")";
        Rational answer=Rational.of(a+b,den);
        Question q=new Question(s.id,expression+"\nx ≠ 0. 약분한 값은?","",answer.toString());q.stepSupport=false;q.nonzeroVariables.add("x");
        q.studyGuide=new StudyGuide().step("동류항의 계수를 더하세요.",a+" + ("+b+") = ","",""+(a+b))
            .step("0이 아닌 같은 문자 인수를 약분하세요.",variable+" / "+variable+" = ","","1")
            .step("남은 계수의 비를 계산하세요.",(a+b)+" ÷ "+den+" = ","",answer.toString()).transfer(false);
        return q.withInputs(a,b,den);
    }
    private static Question experimental(Catalog.Skill s,Random r){
        int[] totals={20,25,40,50,80,100,125,200,250,400,500};int total=totals[r.nextInt(totals.length)],heads=n(r,1,total-1),mode=r.nextInt(3);
        Rational frequency=Rational.of(heads,total),answer=mode==2?frequency.mul(Rational.of(100)):frequency;
        String format=mode==0?"분수":mode==1?"소수":"백분율";
        String value=mode==0?answer.toString():answer.decimalText();if(mode!=0&&!value.contains("."))value+=".0";
        Question q=new Question(s.id,"동전을 "+total+"번 던져 앞면이 "+heads+"번 나왔습니다.\n실험 결과의 앞면 확률을 "+format+"로 나타내세요.",mode==2?heads+" / "+total+" * 100":heads+" / "+total,value);
        q.decimal=mode!=0;q.answerFormat=mode==0?"fraction":mode==1?"decimal":"";q.labels=new String[]{mode==2?"백분율 (%)":"답"};
        q.studyGuide=new StudyGuide().step("앞면 횟수를 전체 실험 횟수로 나누세요.",heads+" ÷ "+total+" = ","",frequency.toString());
        if(mode==1)q.studyGuide.step("분수를 소수로 바꾸세요.",frequency+" = ","",value);
        if(mode==2)q.studyGuide.step("실험 결과의 확률에 100을 곱하세요.",frequency+" × 100 = "," %",value);
        q.studyGuide.transfer(false);return q.withInputs(heads,total,mode);
    }
}
