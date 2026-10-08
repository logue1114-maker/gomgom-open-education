package com.gomgomapps.math.core;

import java.util.*;

/** Prime-factor drill with learner-filled factors and exponents, scoped by curriculum. */
final class PrimeExponentPractice {
    private PrimeExponentPractice(){}
    static Question create(Catalog.Skill skill,Random random){
        int value=4+random.nextInt(9996),remaining=value,total=0,index=0;
        StudyGuide guide=new StudyGuide().transfer(false);
        for(int prime=2;prime<=remaining;prime++)if(remaining%prime==0){
            int exponent=0;while(remaining%prime==0){remaining/=prime;exponent++;}
            index++;total+=exponent;
            guide.step("작은 소인수부터 입력하세요.",index+"번째 소인수 = ","",String.valueOf(prime));
            guide.step("그 소인수가 몇 번 곱해지는지 입력하세요.",index+"번째 소인수의 지수 = ","",String.valueOf(exponent));
        }
        guide.step("입력한 지수들을 더하세요.","지수의 합 = ","",String.valueOf(total));
        Question q=new Question(skill.id,value+"을 소인수분해했을 때 모든 지수의 합은?","",String.valueOf(total));
        q.studyGuide=guide;q.stepSupport=false;return q.withInputs(value);
    }
}
