package com.gomgomapps.math.core;

import java.util.*;

/** The same small-number relations, with a result or either operand to find. */
final class SumFoundations {
    private SumFoundations(){}
    static boolean supports(String id){return Set.of("add9","sub9","add20","sub20").contains(id);}
    static boolean blank(Question q){return supports(q.skillId)&&q.prompt.contains("□");}
    static Question next(Catalog.Skill skill,Random random,CurriculumLimits limits,Map<String,Integer> recent){
        Map<String,Question> candidates=new LinkedHashMap<>();boolean add=skill.family.equals("add"),small=skill.range==9;
        for(int a=small?0:add?1:10;a<=(small?9:add?9:18);a++)for(int b=small?0:1;b<=9;b++){
            if(add?(small?a+b>9:a+b<10):b>a||(!small&&b<=a%10))continue;
            int result=add?a+b:a-b;String op=add?" + ":" - ",original=a+op+b;
            Question base=new Question(skill.id,original,original,String.valueOf(result)).withInputs(a,b);
            if(!limits.allows(base))continue;candidates.put(base.signature(),base);
            for(int form=1;form<=2;form++){
                int answer=form==1?a:b;String prompt=form==1?"□"+op+b+" = "+result:a+op+"□ = "+result;
                String expression;StudyGuide guide=new StudyGuide().transfer(false);
                if(add){int known=form==1?b:a;expression=result+" - "+known;guide.step("알고 있는 수에서 전체 수까지 이어 세세요.",known+" + "," = "+result,String.valueOf(answer));}
                else if(form==1){expression=result+" + "+b;guide.step("남은 수와 뺀 수를 더하세요.",result+" + "+b+" = ","",String.valueOf(answer));}
                else{expression=a+" - "+result;guide.step("전체에서 남은 수를 빼세요.",a+" - "+result+" = ","",String.valueOf(answer));}
                Question q=new Question(skill.id,prompt,expression,String.valueOf(answer));q.stepSupport=false;q.studyGuide=guide;
                int ceiling=!add&&form==1&&!small?18:9;q.withInputs(ceiling);
                if(limits.allows(q))candidates.put(q.signature(),q);
            }
        }
        return FactFoundations.choose(candidates,random,recent);
    }
    static void choices(Question q,Random random){
        int answer=Integer.parseInt(q.answers[0]),ceiling=q.choiceInputs[0].n.intValue();List<Integer> wrong=new ArrayList<>();
        int floor=q.skillId.equals("sub20")&&q.prompt.startsWith("□")?10:q.skillId.equals("add20")?1:0;
        for(int n=floor;n<=ceiling;n++)if(n!=answer)wrong.add(n);Collections.shuffle(wrong,random);
        List<Integer> options=new ArrayList<>(wrong.subList(0,3));options.add(answer);Collections.shuffle(options,random);
        for(int n:options){if(n==answer)q.correctChoice=q.choices.size();q.choices.add(String.valueOf(n));q.distractorReasons.add(n==answer?"정답":"덧셈·뺄셈 관계 확인");}
    }
}
