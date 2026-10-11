package com.gomgomapps.math.core;

import java.util.*;
import java.util.regex.*;

/** Add two public operands by doubling the smaller and adding their difference. */
public final class DoubleBasedAddition {
    private DoubleBasedAddition() {}
    public static final String ID="doubleBasedAddition";
    public static final String INSTRUCTION="작은 수의 두 배에 두 수의 차이를 더하세요.";
    public static final Catalog.Skill SKILL=new Catalog.Skill(ID,"두 배를 이용해 더하기",1,1,1,"","doubleAddition",500,"objectDouble,add20,sub20","작은 수를 두 번 더한 뒤 두 수의 차이를 더한다.");
    static int domainCount(int maximum) {
        if(maximum<1||maximum>500)throw new IllegalArgumentException("addition scope");
        return (maximum+1)*(maximum+2)/2;
    }
    static Question at(int maximum,int index) {
        if(index<0||index>=domainCount(maximum))throw new IllegalArgumentException("addition index");
        int sum=0;while(index>sum){index-=sum+1;sum++;}
        return make(index,sum-index,maximum);
    }
    static Question make(int a,int b,int maximum) {
        if(maximum<1||maximum>500||a<0||b<0||a+b>maximum)throw new IllegalArgumentException("addition operands");
        Question q=new Question(ID,INSTRUCTION+"\n"+a+" + "+b,"",String.valueOf(a+b));
        q.labels=new String[]{"답"};q.stepSupport=false;attach(q);return q;
    }
    public static int[] read(Question q) {
        if(q==null||!ID.equals(q.skillId)||q.prompt==null)return null;
        Matcher m=Pattern.compile(Pattern.quote(INSTRUCTION)+"\\n([0-9]{1,3}) \\+ ([0-9]{1,3})").matcher(q.prompt);
        if(!m.matches())return null;
        int a=Integer.parseInt(m.group(1)),b=Integer.parseInt(m.group(2));
        return a+b<=500?new int[]{a,b}:null;
    }
    static Question next(Random random,CurriculumLimits limits,Map<String,Integer> recent) {
        int maximum=Math.min(500,limits.wholeMaximum(20));
        return IndexedQuestionSupply.choose(domainCount(maximum),i->at(maximum,i),random,limits,recent);
    }
    public static void attach(Question q) {
        int[] v=read(q);if(v==null)return;
        int small=Math.min(v[0],v[1]),large=Math.max(v[0],v[1]);
        StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="double-based-addition-v1";
        g.step("두 수 중 작은 수를 쓰세요.","작은 수 = ","",String.valueOf(small))
         .step("두 수 중 큰 수를 쓰세요.","큰 수 = ","",String.valueOf(large))
         .step("큰 수에서 작은 수를 빼 두 수의 차이를 구하세요.","차이 = 큰 수 − 작은 수 = ","",String.valueOf(large-small))
         .step("작은 수를 두 번 더하세요.","두 배 = 작은 수 + 작은 수 = ","",String.valueOf(2*small))
         .step("작은 수의 두 배에 차이를 더하세요.","합 = 두 배 + 차이 = ","",String.valueOf(v[0]+v[1]));
        q.studyGuide=g;
    }
    static Checker.Result check(Question q,List<String> answers) {
        int[] v=read(q);
        if(v==null||answers==null||answers.size()!=1||answers.get(0)==null||!answers.get(0).trim().matches("[0-9]{1,3}"))
            return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"답 입력 필요");
        return Integer.parseInt(answers.get(0).trim())==v[0]+v[1]?new Checker.Result(Checker.Status.CORRECT,-1,"정답"):
            new Checker.Result(Checker.Status.WRONG_ANSWER,0,"두 배와 차이를 더한 값 확인");
    }
}
