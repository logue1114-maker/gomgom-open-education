package com.gomgomapps.math.core;

import java.util.*;
import java.util.regex.*;

/** Counting is an explicit calculation strategy, separate from next-number exercises. */
public final class CountingCalculations {
    private CountingCalculations() {}
    public static final String ON="countOnAddition", BACK="countBackSubtraction", UP="countUpDifference";
    public static final List<Catalog.Skill> SKILLS=List.of(
        new Catalog.Skill(ON,"큰 수부터 이어 더하기",1,1,1,"","countCalculation",500,"count,add20","큰 수에서 시작해 작은 수만큼 앞으로 센다."),
        new Catalog.Skill(BACK,"거꾸로 세어 빼기",1,1,1,"","countCalculation",500,"countBackward,sub20","첫 수에서 빼는 수만큼 거꾸로 센다."),
        new Catalog.Skill(UP,"올라가 세어 차이 구하기",1,1,1,"","countCalculation",500,"countForward,sub20","작은 수에서 큰 수까지 올라간 간격을 합한다."));
    public static boolean supports(String id) { return ON.equals(id)||BACK.equals(id)||UP.equals(id); }
    public static String instruction(String id) {
        if(ON.equals(id))return "큰 수부터 이어 세어 더하세요.";
        if(BACK.equals(id))return "거꾸로 세어 빼세요.";
        if(UP.equals(id))return "작은 수에서 큰 수까지 올라가 세어 차이를 구하세요.";
        throw new IllegalArgumentException("counting strategy");
    }
    static Question make(String id,int a,int b,int maximum) {
        boolean addition=ON.equals(id);
        if(!supports(id)||maximum<1||maximum>500||a<0||b<0||a>maximum||b>maximum||
            (addition?a+b>maximum:a<b))throw new IllegalArgumentException("counting calculation domain");
        int result=addition?a+b:a-b;
        Question q=new Question(id,instruction(id)+"\n"+a+(addition?" + ":" − ")+b,"",String.valueOf(result));
        q.kind="countCalculation";q.labels=new String[]{"답"};q.stepSupport=false;
        StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="counting-calculations-v1";
        int start=addition?Math.max(a,b):UP.equals(id)?b:a;
        int distance=addition?Math.min(a,b):UP.equals(id)?a-b:b;
        g.step(addition?"두 수 중 큰 수에서 시작하세요.":UP.equals(id)?"작은 수에서 시작하세요.":"첫 수에서 시작하세요.","시작 수 = ","",String.valueOf(start));
        if(addition)g.step("두 수 중 작은 수만큼 앞으로 세세요.","앞으로 셀 수 = ","",String.valueOf(distance));
        int current=start,remaining=distance;boolean backward=BACK.equals(id);
        List<String> jumps=new ArrayList<>();
        int largest=addition||maximum<=20?1:maximum<=100?10:100;
        for(int unit=largest;unit>=1;unit/=10) {
            while(remaining>=unit) {
                int next=current+(backward?-unit:unit);
                String direction=backward?"거꾸로":"앞으로";
                g.step(unit+"씩 "+direction+" 세어 다음 수를 쓰세요.",current+(backward?" − ":" + ")+unit+" = ","",String.valueOf(next));
                jumps.add(String.valueOf(unit));current=next;remaining-=unit;
            }
        }
        if(UP.equals(id))g.step("올라간 간격을 모두 더해 차이를 구하세요.",(jumps.isEmpty()?"0":String.join(" + ",jumps))+" = ","",String.valueOf(result));
        else g.step("마지막으로 센 수를 답에 쓰세요.","마지막 수 = ","",String.valueOf(result));
        q.studyGuide=g;return q;
    }
    public static int[] read(Question q) {
        if(q==null||!supports(q.skillId)||q.prompt==null)return null;
        Matcher m=Pattern.compile(Pattern.quote(instruction(q.skillId))+"\\n([0-9]{1,3}) ([+−]) ([0-9]{1,3})").matcher(q.prompt);
        if(!m.matches())return null;
        int a=Integer.parseInt(m.group(1)),b=Integer.parseInt(m.group(3));boolean addition=ON.equals(q.skillId);
        return a<=500&&b<=500&&(addition==m.group(2).equals("+"))&&(addition?a+b<=500:a>=b)?new int[]{a,b}:null;
    }
    static int domainCount(String id,int maximum) {
        if(ON.equals(id)&&maximum>20)return (maximum-8)*10+45;
        if(UP.equals(id)&&maximum>20&&maximum<=100)return (maximum-8)*10+45;
        int count=(maximum+1)*(maximum+2)/2;
        return !ON.equals(id)&&maximum>100?count-5050:count;
    }
    static Question at(String id,int maximum,int index) {
        int count=domainCount(id,maximum);
        if(index<0||index>=count)throw new IllegalArgumentException("counting index");
        if(ON.equals(id)&&maximum>20) {
            int full=(maximum-8)*10;
            if(index<full)return make(id,index/10,index%10,maximum);
            index-=full;int a=maximum-8,length=9;
            while(index>=length){index-=length;a++;length--;}
            return make(id,a,index,maximum);
        }
        if(UP.equals(id)&&maximum>20&&maximum<=100&&index>=45) {
            int a=9+(index-45)/10,b=a-9+(index-45)%10;
            return make(id,a,b,maximum);
        }
        if(!ON.equals(id)&&maximum>100)index+=5050;
        // Increasing triangular rows keep every public operand pair independently addressable.
        int a=0;
        while(index>a){index-=a+1;a++;}
        return ON.equals(id)?make(id,index,a-index,maximum):make(id,a,index,maximum);
    }
    static Question next(Catalog.Skill skill,Random random,CurriculumLimits limits,Map<String,Integer> recent) {
        int maximum=Math.min(500,limits.wholeMaximum(20));
        return IndexedQuestionSupply.choose(domainCount(skill.id,maximum),i->at(skill.id,maximum,i),random,limits,recent);
    }
    static Checker.Result check(Question q,List<String> answers) {
        int[] v=read(q);
        if(v==null||answers==null||answers.size()!=1||answers.get(0)==null||!answers.get(0).trim().matches("[0-9]{1,3}"))
            return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"답 입력 필요");
        int expected=ON.equals(q.skillId)?v[0]+v[1]:v[0]-v[1];
        return Integer.parseInt(answers.get(0).trim())==expected?new Checker.Result(Checker.Status.CORRECT,-1,"정답"):
            new Checker.Result(Checker.Status.WRONG_ANSWER,0,"세는 방향과 간격을 확인하세요.");
    }
}
