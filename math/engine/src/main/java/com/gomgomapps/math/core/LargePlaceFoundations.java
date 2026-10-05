package com.gomgomapps.math.core;

import java.util.*;

/** Exact integer place reading through the thousand-trillion place; legacy int domains stay unchanged. */
public final class LargePlaceFoundations {
    private LargePlaceFoundations(){}
    public static boolean supports(String id){return id.equals("largePlaceTrillion");}
    public static Question create(Catalog.Skill skill,Random random){
        int mode=random.nextInt(3);long value,unit,answer;String prompt;StudyGuide guide=new StudyGuide().transfer(false);
        if(mode<2){
            int digits=9+random.nextInt(8);StringBuilder number=new StringBuilder().append(1+random.nextInt(9));
            for(int i=1;i<digits;i++)number.append(random.nextInt(10));
            value=Long.parseLong(number.toString());int place=8+random.nextInt(digits-8);unit=1;for(int i=0;i<place;i++)unit*=10;
            long digit=(value/unit)%10;answer=mode==0?digit:digit*unit;
            prompt=value+"\n"+unit+(mode==0?"의 자리 숫자는?":"의 자리 숫자가 나타내는 값은?");
            guide.step("해당 자리 숫자를 쓰세요.",unit+" → ","",String.valueOf(digit));
            if(mode==1)guide.step("숫자에 자릿값을 곱하세요.",digit+" × "+unit+" = ","",String.valueOf(answer));
        }else{
            long trillions=1+random.nextInt(9999),hundredMillions=random.nextInt(10000);
            unit=100000000L;value=trillions*1000000000000L+hundredMillions*unit;answer=value;
            prompt="1000000000000 × "+trillions+" + 100000000 × "+hundredMillions+" = □\n□에 들어갈 수는?";
            guide.step("각 묶음의 수를 곱하세요.","1000000000000 × "+trillions+" = ","",String.valueOf(trillions*1000000000000L))
                 .step("두 묶음의 수를 더하세요.",trillions*1000000000000L+" + 100000000 × "+hundredMillions+" = ","",String.valueOf(answer));
        }
        Question q=new Question(skill.id,prompt,"",String.valueOf(answer));q.stepSupport=false;q.studyGuide=guide;
        return q.withInputs(value,unit,mode);
    }
    public static void choices(Question q,Random random){
        int mode=q.choiceInputs[2].intValue();long unit=q.choiceInputs[1].n.longValueExact(),answer=Long.parseLong(q.answers[0]);
        LinkedHashMap<Long,String> pool=new LinkedHashMap<>();
        if(mode<2){for(long d=0;d<=9;d++){long candidate=mode==0?d:d*unit;if(candidate!=answer)pool.put(candidate,mode==0?"다른 자리 숫자 선택":"자리 숫자와 자릿값의 곱 오류");}}
        else for(int delta=-9;delta<=9;delta++){long candidate=answer+delta*unit;if(delta!=0&&candidate>=0)pool.put(candidate,"큰 수 묶음의 합 오류");}
        List<Long> wrong=new ArrayList<>(pool.keySet());Collections.shuffle(wrong,random);List<Long> options=new ArrayList<>(wrong.subList(0,3));options.add(answer);Collections.shuffle(options,random);
        for(long option:options){if(option==answer){q.correctChoice=q.choices.size();q.distractorReasons.add("정답");}else q.distractorReasons.add(pool.get(option));q.choices.add(String.valueOf(option));}
    }
}
