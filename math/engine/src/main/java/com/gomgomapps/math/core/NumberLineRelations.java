package com.gomgomapps.math.core;

import java.util.*;

/** Read an unlabelled tick from visible neighbouring numbers and the public scale. */
public final class NumberLineRelations {
    private NumberLineRelations() {}
    public static final String ID="numberLineRead", VERSION="number-line-relations-v1";
    public static final Catalog.Skill SKILL=new Catalog.Skill(ID,"수직선 읽기",1,1,1,"","numberLine",100,"count","숫자 눈금과 한 칸의 크기를 보고 A의 수를 찾는다.");
    public static boolean supports(String id){return ID.equals(id);}
    static Question make(int start,int step,int mark){
        if(start<0||!(step==1||step==10)||start+10*step>100||mark<0||mark>10)throw new IllegalArgumentException("number line domain");
        Question q=new Question(ID,"A가 나타내는 수를 쓰세요.","",""+(start+step*mark));
        q.kind="numberLine";q.stepSupport=false;q.labels=new String[]{"답"};
        // Marker index is public geometry, not a hidden correct-answer number.
        q.diagram=new StudyDiagram("primaryNumberLine",new double[]{start,step,10,mark});return q;
    }
    static Question next(Random random,CurriculumLimits limits,Map<String,Integer> recent){
        int maximum=Math.min(100,limits.wholeMaximum(100));Map<String,Question> pool=new LinkedHashMap<>();
        for(int step:new int[]{1,10})for(int start=0;start+10*step<=maximum;start++)for(int mark=0;mark<=10;mark++){
            Question q=make(start,step,mark);if(limits.allows(q))pool.put(q.signature(),q);
        }
        Question selected=FactFoundations.choose(pool,random,recent);attach(selected);return selected;
    }
    public static int[] read(Question q){
        if(q==null||!supports(q.skillId)||q.diagram==null||!"primaryNumberLine".equals(q.diagram.type)||q.diagram.values.length!=4)return null;
        int[] v=new int[4];for(int i=0;i<4;i++){double value=q.diagram.values[i];if(!Double.isFinite(value)||value!=(int)value)return null;v[i]=(int)value;}
        return v[0]>=0&&(v[1]==1||v[1]==10)&&v[2]==10&&v[0]+v[1]*10<=100&&v[3]>=0&&v[3]<=10?v:null;
    }
    public static void attach(Question q){
        int[] v=read(q);if(v==null)return;
        int first=v[3]==0?1:0,anchor=v[0]+first*v[1],distance=Math.abs(v[3]-first),move=distance*v[1],answer=v[0]+v[3]*v[1];
        StudyGuide guide=new StudyGuide().transfer(false);guide.teachingVersion=VERSION;
        guide.step("숫자가 적힌 첫 눈금의 수를 쓰세요.","기준값 = ","",""+anchor);
        guide.step("눈금 한 칸의 크기를 쓰세요.","한 칸의 크기 = ","",""+v[1]);
        guide.step("첫 숫자 눈금에서 A까지 몇 칸인지 세세요.","칸 수 = ","",""+distance);
        guide.step("한 칸의 크기와 칸 수를 곱하세요.","한 칸의 크기 × 칸 수 = ","",""+move);
        guide.step(v[3]<first?"기준값에서 이동값을 빼세요.":"기준값에 이동값을 더하세요.",v[3]<first?"기준값 − 이동값 = ":"기준값 + 이동값 = ","",""+answer);
        q.studyGuide=guide;
    }
    static Checker.Result check(Question q,List<String> answers){
        int[] v=read(q);if(v==null||answers.size()!=1||!answers.get(0).trim().matches("[0-9]{1,3}"))return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"답 입력 필요");
        return Integer.parseInt(answers.get(0).trim())==v[0]+v[1]*v[3]?new Checker.Result(Checker.Status.CORRECT,-1,"정답"):new Checker.Result(Checker.Status.WRONG_ANSWER,0,"이 답 확인");
    }
}
