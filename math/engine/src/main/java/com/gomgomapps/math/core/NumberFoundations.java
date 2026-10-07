package com.gomgomapps.math.core;

import java.util.*;

/** Reading, composing and grouping use the same small number domains. */
final class NumberFoundations {
    private NumberFoundations(){}
    static Question place50(Catalog.Skill skill,Random random){
        int value=10+random.nextInt(41),tens=value/10,ones=value%10,mode=random.nextInt(4);
        String prompt;int answer;
        StudyGuide guide=new StudyGuide();
        if(mode==0){
            int place=random.nextInt(2);answer=place==0?ones:tens;
            prompt=value+"에서 "+(place==0?"일":"십")+"의 자리 숫자는?";
            guide.step(place==0?"오른쪽 첫 번째 숫자를 읽으세요.":"오른쪽 두 번째 숫자를 읽으세요.",(place==0?"일":"십")+"의 자리 = ","",String.valueOf(answer));
        }else if(mode==1){
            prompt="10이 "+tens+"개, 1이 "+ones+"개입니다.\n모두 얼마인가요?";answer=value;
            guide.step("10씩 묶은 수를 구하세요.","10 × "+tens+" = ","",String.valueOf(tens*10))
                 .step("낱개를 더하세요.",tens*10+" + "+ones+" = ","",String.valueOf(value));
        }else{
            boolean blankOnes=mode==2;answer=blankOnes?ones:tens*10;
            prompt=value+" = "+(blankOnes?tens*10+" + □":"□ + "+ones)+"\n□에 들어갈 수는?";
            guide.step(blankOnes?"일의 자리 숫자를 읽으세요.":"십의 자리 숫자만큼 10씩 묶으세요.",value+" = "+(blankOnes?tens*10+" + ":""),blankOnes?"":" + "+ones,String.valueOf(answer));
        }
        Question q=new Question(skill.id,prompt,"",String.valueOf(answer));q.stepSupport=false;q.studyGuide=guide;
        return q.withInputs(value,0,mode);
    }
    static Question repeat(Catalog.Skill skill,Random random,CurriculumLimits limits){
        int each=0,groups=0;
        // The full product must fit even when the requested blank is a factor.
        for(int attempt=0;attempt<2048;attempt++){
            each=2+random.nextInt(8);groups=2+random.nextInt(5);
            Question total=new Question(skill.id,each+"씩 "+groups+"묶음은 모두 얼마인가요?",each+"*"+groups,String.valueOf(each*groups));
            if(limits.allows(total))break;
            if(attempt==2047)throw new IllegalStateException("No grouping question matches curriculum limits");
        }
        int mode=random.nextInt(4),answer=mode==2?groups:mode==3?each:each*groups;
        String sum=String.join(" + ",Collections.nCopies(groups,String.valueOf(each)));
        String prompt=mode==0?each+"씩 "+groups+"묶음은 모두 얼마인가요?":sum+" = "+(mode==1?"□":mode==2?each+" × □":"□ × "+groups)+"\n□에 들어갈 수는?";
        Question q=new Question(skill.id,prompt,mode<=1?each+"*"+groups:"",String.valueOf(answer));q.stepSupport=false;RepeatedGroupingRelations.attach(q);
        return q.withInputs(each,groups,mode);
    }
}
