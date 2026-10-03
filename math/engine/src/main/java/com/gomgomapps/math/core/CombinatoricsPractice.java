package com.gomgomapps.math.core;

import java.util.Random;

/** Distinct cards, no replacement; fixed or forbidden cards change the counting problem. */
final class CombinatoricsPractice {
    private CombinatoricsPractice(){}
    static Question create(Catalog.Skill skill,Random random){
        boolean combination=skill.family.equals("combination");
        int form=random.nextInt(4),minimum=form==0?4:form==3?6:5;
        int total=minimum+random.nextInt(13-minimum),chosen=(form==3?3:2);
        chosen+=random.nextInt(Math.min(total-1,5)-chosen+1);
        int fixed=form==1?1:form==3?2:0,excluded=form==2?1:0;
        int available=total-fixed-excluded,remaining=chosen-fixed;
        String included=form==0?"":form==3?"(A, B 포함)":"(A 포함)";
        String prompt="서로 다른 "+total+"개의 카드"+included+" 중 "+chosen+"개를 중복 없이 고릅니다.\n";
        if(form==1)prompt+=combination?"카드 A를 반드시 포함합니다.\n":"카드 A는 반드시 첫 자리에 놓습니다.\n";
        if(form==2)prompt+="카드 A는 고를 수 없습니다.\n";
        if(form==3)prompt+=combination?"카드 A와 카드 B를 반드시 포함합니다.\n":"카드 A는 첫 자리, 카드 B는 마지막 자리에 놓습니다.\n";
        prompt+=combination?"고른 순서는 구분하지 않습니다. 경우의 수는?":"고른 카드를 순서대로 놓는 경우의 수는?";
        long ordered=1,orders=1;
        for(int i=0;i<remaining;i++)ordered*=available-i;
        for(int i=2;i<=remaining;i++)orders*=i;
        Question q=new Question(skill.id,prompt,"",String.valueOf(combination?ordered/orders:ordered));
        q.stepSupport=false;
        StudyGuide guide=new StudyGuide().transfer(false);
        if(form!=0){
            guide.step(excluded>0?"제외한 카드를 빼고 남은 카드 수를 구합니다.":"이미 정해진 카드를 빼고 남은 카드 수를 구합니다.",total+" - "+(fixed+excluded)+" = ","개",String.valueOf(available));
            if(fixed>0)guide.step("이미 정해진 카드를 빼고 더 골라야 할 수를 구합니다.",chosen+" - "+fixed+" = ","개",String.valueOf(remaining));
        }
        String orderedProduct=product(available,remaining),sameGroupOrders=product(remaining,remaining);
        guide.step("한 번 고른 카드는 빼고 다음 자리를 셉니다.",orderedProduct+" = ","가지",String.valueOf(ordered));
        if(combination){
            guide.step("같은 카드 묶음을 순서만 바꿔 놓는 방법을 셉니다.",sameGroupOrders+" = ","가지",String.valueOf(orders));
            guide.step("순서만 다른 경우를 하나로 묶습니다.","("+orderedProduct+") ÷ ("+sameGroupOrders+") = ","가지",String.valueOf(ordered/orders));
        }
        q.studyGuide=guide;
        return q.withInputs(available,remaining,total,chosen,form);
    }
    private static String product(int first,int count){
        StringBuilder text=new StringBuilder();
        for(int i=0;i<count;i++){if(i>0)text.append(" × ");text.append(first-i);}
        return text.toString();
    }
}
