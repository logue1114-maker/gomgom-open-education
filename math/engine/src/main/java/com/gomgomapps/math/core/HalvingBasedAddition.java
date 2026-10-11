package com.gomgomapps.math.core;

import java.util.*;
import java.util.regex.*;

/** Add two even public operands by halving, adding and doubling the smaller sum. */
public final class HalvingBasedAddition {
    private HalvingBasedAddition() {}
    public static final String ID="halvingBasedAddition";
    public static final String INSTRUCTION="두 수를 각각 반으로 줄여 더한 뒤 두 배로 되돌리세요.";
    public static final Catalog.Skill SKILL=new Catalog.Skill(ID,"절반을 이용해 더하기",1,1,1,"","halvingAddition",500,"objectHalf,objectDouble,add20","두 짝수의 절반을 더한 뒤 두 배로 되돌린다.");
    static int domainCount(int maximum) {
        if(maximum<2||maximum>500)throw new IllegalArgumentException("halving addition scope");
        int half=maximum/2;return (half+1)*(half+2)/2;
    }
    static Question at(int maximum,int index) {
        if(index<0||index>=domainCount(maximum))throw new IllegalArgumentException("halving addition index");
        int halfSum=0;while(index>halfSum){index-=halfSum+1;halfSum++;}
        return make(index*2,(halfSum-index)*2,maximum);
    }
    static Question make(int a,int b,int maximum) {
        domainCount(maximum);
        if(a<0||b<0||a%2!=0||b%2!=0||a+b>maximum)throw new IllegalArgumentException("halving addition operands");
        Question q=new Question(ID,INSTRUCTION+"\n"+a+" + "+b,"",String.valueOf(a+b));
        q.labels=new String[]{"답"};q.stepSupport=false;attach(q);return q;
    }
    public static int[] read(Question q) {
        if(q==null||!ID.equals(q.skillId)||q.prompt==null)return null;
        Matcher m=Pattern.compile(Pattern.quote(INSTRUCTION)+"\\n([0-9]{1,3}) \\+ ([0-9]{1,3})").matcher(q.prompt);
        if(!m.matches())return null;
        int a=Integer.parseInt(m.group(1)),b=Integer.parseInt(m.group(2));
        return a%2==0&&b%2==0&&a+b<=500?new int[]{a,b}:null;
    }
    static Question next(Random random,CurriculumLimits limits,Map<String,Integer> recent) {
        int maximum=Math.min(500,limits.wholeMaximum(20));
        return IndexedQuestionSupply.choose(domainCount(maximum),i->at(maximum,i),random,limits,recent);
    }
    public static void attach(Question q) {
        int[] v=read(q);if(v==null)return;
        StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="halving-based-addition-v1";
        g.step("첫째 수의 절반을 쓰세요.","첫째 수의 절반 = ","",String.valueOf(v[0]/2))
         .step("둘째 수의 절반을 쓰세요.","둘째 수의 절반 = ","",String.valueOf(v[1]/2))
         .step("두 절반을 더하세요.","절반의 합 = ","",String.valueOf(v[0]/2+v[1]/2))
         .step("절반의 합을 두 배로 되돌리세요.","원래 두 수의 합 = ","",String.valueOf(v[0]+v[1]));
        q.studyGuide=g;
    }
    static Checker.Result check(Question q,List<String> answers) {
        int[] v=read(q);
        if(v==null||answers==null||answers.size()!=1||answers.get(0)==null||!answers.get(0).trim().matches("[0-9]{1,3}"))
            return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"답 입력 필요");
        return Integer.parseInt(answers.get(0).trim())==v[0]+v[1]?new Checker.Result(Checker.Status.CORRECT,-1,"정답"):
            new Checker.Result(Checker.Status.WRONG_ANSWER,0,"절반의 합을 두 배로 되돌린 값 확인");
    }
}
