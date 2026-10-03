package com.gomgomapps.math.core;

import java.util.Random;

/** Exact powers and logarithm definitions; every help entry is supplied by the learner. */
final class PowerLogPractice {
    private PowerLogPractice(){}
    static Question negativePower(Catalog.Skill skill,Random random){
        int base=2+random.nextInt(11),exponent=1+random.nextInt(5),form=random.nextInt(3);
        long power=power(base,exponent);StudyGuide guide=new StudyGuide().transfer(false);
        String prompt;Rational answer;
        if(form==0){
            prompt=base+"^(-"+exponent+")\n값은?";answer=Rational.of(1,power);
            guide.step("양의 지수로 거듭제곱을 계산합니다.",base+"^"+exponent+" = ","",String.valueOf(power));
            guide.step("음의 지수는 양의 거듭제곱의 역수입니다.","1 ÷ "+power+" = ","",answer.toString());
        }else if(form==1){
            prompt="(1/"+base+")^(-"+exponent+")\n값은?";answer=Rational.of(power);
            guide.step("음의 지수에서 밑의 역수를 구합니다.","1 ÷ (1/"+base+") = ","",String.valueOf(base));
            guide.step("역수를 양의 지수로 거듭제곱합니다.",base+"^"+exponent+" = ","",answer.toString());
        }else{
            prompt=base+"^□ = 1/"+power+"\n□에 들어갈 지수는?";answer=Rational.of(-exponent);
            guide.step("분모를 같은 밑의 거듭제곱으로 나타냅니다.",power+" = "+base+"^□ → □ = ","",String.valueOf(exponent));
            guide.step("역수가 되도록 지수의 부호를 정합니다.",base+"^□ = 1/"+power+" → □ = ","",answer.toString());
        }
        return question(skill,prompt,answer,guide).withInputs(base,exponent,form);
    }
    static Question logarithm(Catalog.Skill skill,Random random){
        int base=2+random.nextInt(11),exponent=random.nextInt(9)-3,form=random.nextInt(2);
        Rational argument=Rational.of(base).pow(exponent);StudyGuide guide=new StudyGuide().transfer(false);
        String prompt;Rational answer;
        if(form==0){
            prompt="log_"+base+" ("+argument+")\n값은?";answer=Rational.of(exponent);
            guide.step("로그를 같은 밑의 지수식으로 바꿉니다.",base+"^□ = "+argument+" → □ = ","",answer.toString());
        }else{
            prompt="log_"+base+" □ = "+exponent+"\n□에 들어갈 진수는?";answer=argument;
            if(exponent<0){
                long denominator=power(base,-exponent);
                guide.step("양의 지수로 거듭제곱을 계산합니다.",base+"^"+(-exponent)+" = ","",String.valueOf(denominator));
                guide.step("음의 지수는 양의 거듭제곱의 역수입니다.","1 ÷ "+denominator+" = ","",answer.toString());
            }else guide.step("로그의 값을 지수로 바꾸어 계산합니다.",base+"^"+exponent+" = ","",answer.toString());
        }
        return question(skill,prompt,answer,guide).withInputs(base,exponent,form);
    }
    static Question commonLog(Catalog.Skill skill,Random random){
        int form=random.nextInt(2),whole=random.nextInt(form==0?9:7),given;
        String prompt;int digits;StudyGuide guide=new StudyGuide().transfer(false);
        if(form==0){
            given=1+random.nextInt(99);String log=whole+"."+(given<10?"0":"")+given;
            prompt="양수 N에 대해 log₁₀ N = "+log+"입니다.\nN의 정수 부분은 몇 자리인가요?";digits=whole+1;
            guide.step("상용로그의 정수 부분을 찾습니다.","log₁₀ N = "+log+" → 정수 부분 = ","",String.valueOf(whole));
            guide.step("정수 부분에 1을 더해 자릿수를 구합니다.",whole+" + 1 = ","자리",String.valueOf(digits));
        }else{
            given=2+random.nextInt(98);int leading=String.valueOf(given).length();digits=leading+whole;
            prompt="자연수 N = "+given+" × 10^"+whole+"\nN은 몇 자리인가요?";
            guide.step("10의 거듭제곱 앞에 있는 수의 자릿수를 셉니다.",given+"의 자릿수 = ","자리",String.valueOf(leading));
            guide.step("10을 곱한 횟수만큼 자릿수를 더합니다.",leading+" + "+whole+" = ","자리",String.valueOf(digits));
        }
        return question(skill,prompt,Rational.of(digits),guide).withInputs(whole,given,form);
    }
    private static Question question(Catalog.Skill skill,String prompt,Rational answer,StudyGuide guide){
        Question q=new Question(skill.id,prompt,"",answer.toString());q.stepSupport=false;q.studyGuide=guide;return q;
    }
    private static long power(int base,int exponent){long answer=1;for(int i=0;i<exponent;i++)answer*=base;return answer;}
}
