package com.gomgomapps.math.core;

import java.util.*;
import java.util.regex.*;

/** Complete ordered lists, derived from the visible numbers rather than stored answer keys. */
public final class CompleteFactorPractice {
    private CompleteFactorPractice(){}
    public static final String PAIRS="allFactorPairs", COMMON="allCommonFactors", MULTIPLES="firstFiveMultiples";
    static final int MAXIMUM=144;
    public static final List<Catalog.Skill> SKILLS=List.of(
        new Catalog.Skill(PAIRS,"모든 곱셈짝 찾기",5,1,1,"",PAIRS,MAXIMUM,"tables,divide","곱해서 주어진 수가 되는 모든 두 수의 짝을 찾는다."),
        new Catalog.Skill(COMMON,"모든 공약수 찾기",5,1,1,"",COMMON,MAXIMUM,"tables,divide","두 수를 모두 나누어떨어지게 하는 수를 빠짐없이 찾는다."),
        new Catalog.Skill(MULTIPLES,"배수 차례로 쓰기",5,1,1,"",MULTIPLES,MAXIMUM,"tables","주어진 수의 배수를 작은 수부터 차례로 쓴다."));
    private static final Pattern PAIR_PROMPT=Pattern.compile("^([0-9]{1,3})의 모든 곱셈짝을 쓰세요\\.\\n각 짝은 작은 수부터, 짝의 순서도 작은 수부터 쓰세요\\.$");
    private static final Pattern COMMON_PROMPT=Pattern.compile("^([0-9]{1,3})와 ([0-9]{1,3})의 공약수를 모두 쓰세요\\.\\n작은 수부터 쓰세요\\.$");
    private static final Pattern MULTIPLE_PROMPT=Pattern.compile("^([0-9]{1,3})의 첫 다섯 배수를 쓰세요\\.\\n양의 배수를 작은 수부터 쓰세요\\.$");
    public static boolean supports(String id){return PAIRS.equals(id)||COMMON.equals(id)||MULTIPLES.equals(id);}
    public static boolean learnerList(String id){return PAIRS.equals(id)||COMMON.equals(id);}
    /** Only public givens are returned; malformed or out-of-scope prompts are rejected. */
    public static int[] read(Question q){
        if(q==null||!supports(q.skillId)||q.prompt==null)return null;
        Matcher m=(PAIRS.equals(q.skillId)?PAIR_PROMPT:COMMON.equals(q.skillId)?COMMON_PROMPT:MULTIPLE_PROMPT).matcher(q.prompt);
        if(!m.matches())return null;
        int a=Integer.parseInt(m.group(1));
        if(a<1||a>MAXIMUM||MULTIPLES.equals(q.skillId)&&a<2)return null;
        if(COMMON.equals(q.skillId)){int b=Integer.parseInt(m.group(2));return b>a&&b<=MAXIMUM?new int[]{a,b}:null;}
        return new int[]{a};
    }
    private static List<Integer> values(String id,int[] given){
        List<Integer> result=new ArrayList<>();int n=given[0];
        if(PAIRS.equals(id)){for(int a=1;a<=n/a;a++)if(n%a==0){result.add(a);result.add(n/a);}}
        else if(COMMON.equals(id)){for(int d=1;d<=n;d++)if(n%d==0&&given[1]%d==0)result.add(d);}
        else for(int k=1;k<=5;k++)result.add(n*k);
        return result;
    }
    static Question make(String id,int a,int b){
        String prompt=PAIRS.equals(id)?a+"의 모든 곱셈짝을 쓰세요.\n각 짝은 작은 수부터, 짝의 순서도 작은 수부터 쓰세요.":
            COMMON.equals(id)?a+"와 "+b+"의 공약수를 모두 쓰세요.\n작은 수부터 쓰세요.":
            a+"의 첫 다섯 배수를 쓰세요.\n양의 배수를 작은 수부터 쓰세요.";
        Question q=new Question(id,prompt,"");int[] givens=read(q);
        if(givens==null)throw new IllegalArgumentException("complete factor practice domain");
        List<Integer> values=values(id,givens);q.answers=values.stream().map(String::valueOf).toArray(String[]::new);q.labels=new String[values.size()];
        for(int i=0;i<q.labels.length;i++)q.labels[i]=PAIRS.equals(id)?"곱셈짝 "+(i/2+1)+" · "+(i%2==0?"작은 수":"큰 수"):
            (COMMON.equals(id)?"공약수 ":"배수 ")+(i+1);
        q.stepSupport=false;return q;
    }
    static int count(String id){if(!supports(id))throw new IllegalArgumentException("factor practice skill");return COMMON.equals(id)?MAXIMUM*(MAXIMUM-1)/2:MULTIPLES.equals(id)?MAXIMUM-1:MAXIMUM;}
    static Question indexed(String id,int index){
        if(index<0||index>=count(id))throw new IllegalArgumentException("factor practice index");
        if(!COMMON.equals(id))return make(id,index+(MULTIPLES.equals(id)?2:1),0);
        for(int a=1;a<MAXIMUM;a++){int row=MAXIMUM-a;if(index<row)return make(id,a,a+1+index);index-=row;}
        throw new IllegalArgumentException("factor practice index");
    }
    static Question next(String id,Random random,CurriculumLimits limits,Map<String,Integer> recent){
        Question q=IndexedQuestionSupply.choose(count(id),i->indexed(id,i),random,limits,recent);attach(q);return q;
    }
    static Checker.Result check(Question q,List<String> answers){
        int[] givens=read(q);if(givens==null||answers==null)return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"수 입력 필요");
        List<Integer> expected=values(q.skillId,givens);
        if(learnerList(q.skillId)){
            int width=PAIRS.equals(q.skillId)?2:1;
            if(answers.isEmpty()||answers.size()%width!=0)return new Checker.Result(Checker.Status.INPUT_NEEDED,-1,"수 입력 필요");
            Set<Integer> seen=new HashSet<>();int previous=0;
            for(int i=0;i<answers.size();i+=width){
                for(int j=0;j<width;j++)if(answers.get(i+j)==null||!answers.get(i+j).trim().matches("[0-9]{1,3}"))return new Checker.Result(Checker.Status.INPUT_NEEDED,i+j,"수 입력 필요");
                int first=Integer.parseInt(answers.get(i).trim());
                if(first<1)return new Checker.Result(Checker.Status.WRONG_ANSWER,i,"이 수 확인");
                if(width==2){int second=Integer.parseInt(answers.get(i+1).trim());if(givens[0]%first!=0)return new Checker.Result(Checker.Status.WRONG_ANSWER,i,"이 수 확인");if(first>second)return new Checker.Result(Checker.Status.WRONG_ANSWER,i,"작은 수부터 입력");if(second!=givens[0]/first)return new Checker.Result(Checker.Status.WRONG_ANSWER,i+1,"이 수 확인");}
                else if(givens[0]%first!=0||givens[1]%first!=0)return new Checker.Result(Checker.Status.WRONG_ANSWER,i,"이 공약수 확인");
                if(!seen.add(first))return new Checker.Result(Checker.Status.WRONG_ANSWER,i,"중복된 수 확인");
                if(first<=previous)return new Checker.Result(Checker.Status.WRONG_ANSWER,i,"작은 수부터 입력");previous=first;
            }
            if(answers.size()!=expected.size())return new Checker.Result(Checker.Status.WRONG_ANSWER,-1,width==2?"빠진 곱셈짝 확인":"빠진 공약수 확인");
            return new Checker.Result(Checker.Status.CORRECT,-1,"정답");
        }
        if(answers.size()!=expected.size())return new Checker.Result(Checker.Status.INPUT_NEEDED,Math.min(answers.size(),expected.size()-1),"모든 수 입력 필요");
        for(int i=0;i<answers.size();i++){
            String raw=answers.get(i);if(raw==null||!raw.trim().matches("[0-9]{1,3}"))return new Checker.Result(Checker.Status.INPUT_NEEDED,i,"수 입력 필요");
            if(Integer.parseInt(raw.trim())!=expected.get(i))return new Checker.Result(Checker.Status.WRONG_ANSWER,i,"이 수 확인");
        }
        return new Checker.Result(Checker.Status.CORRECT,-1,"정답");
    }
    public static void attach(Question q){
        int[] givens=read(q);if(givens==null)return;
        List<Integer> expected=values(q.skillId,givens);StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="complete-factor-practice-v1";
        if(PAIRS.equals(q.skillId))for(int i=0;i<expected.size();i+=2){
            g.step("주어진 수를 나누어떨어지게 하는 작은 수를 차례로 찾으세요.","곱셈짝의 작은 수 = ","",""+expected.get(i));
            g.step("주어진 수를 작은 수로 나누세요.","주어진 수 ÷ 작은 수 = ","",""+expected.get(i+1));
            g.step("두 수의 곱이 주어진 수인지 확인하세요.","작은 수 × 큰 수 = ","",""+givens[0]);
        }
        else if(COMMON.equals(q.skillId))for(int d:expected){
            g.step("두 수를 모두 나누어떨어지게 하는 수를 작은 수부터 찾으세요.","공약수 = ","",""+d);
            g.step("첫째 수를 찾은 공약수로 나누세요.","첫째 수 ÷ 공약수 = ","",""+(givens[0]/d));
            g.step("둘째 수도 같은 공약수로 나누세요.","둘째 수 ÷ 공약수 = ","",""+(givens[1]/d));
        }
        else for(int k=1;k<=5;k++){
            g.step("주어진 수를 곱할 차례를 쓰세요.","곱할 수 = ","",""+k);
            g.step("주어진 수에 곱할 수를 곱하세요.","주어진 수 × 곱할 수 = ","",""+expected.get(k-1));
        }
        q.studyGuide=g;
    }
}
