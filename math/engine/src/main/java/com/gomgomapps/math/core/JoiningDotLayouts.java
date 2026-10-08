package com.gomgomapps.math.core;

import java.util.*;

/** Two visible collections, counted separately and then combined. */
final class JoiningDotLayouts {
    private JoiningDotLayouts(){}
    static void append(Map<String,Question> candidates,CurriculumLimits limits,int maximum){
        for(int whole=0;whole<=maximum;whole++)for(int left=0;left<=whole;left++){
            int right=whole-left;
            for(int leftMask:masks(left))for(int rightMask:masks(right)){
                Question q=new Question("join9","두 묶음의 동그라미는 모두 몇 개인가요?","",String.valueOf(whole)).withInputs(whole,left,right,0,maximum);
                q.diagram=new StudyDiagram("joiningDots",new double[]{leftMask,rightMask});q.stepSupport=false;
                q.studyGuide=new StudyGuide().transfer(false)
                    .step("왼쪽 묶음의 동그라미를 세어 쓰세요.","왼쪽 수 = ","",String.valueOf(left))
                    .step("오른쪽 묶음의 동그라미를 세어 쓰세요.","오른쪽 수 = ","",String.valueOf(right))
                    .step("두 묶음을 합쳐 전체 수를 쓰세요.","전체 수 = ","",String.valueOf(whole));
                q.studyGuide.teachingVersion="joining-dot-layouts-v1";
                if(limits.allows(q))candidates.put(q.signature(),q);
            }
        }
    }
    private static Set<Integer> masks(int count){
        int first=(1<<count)-1;
        // Top-left and bottom-right arrangements are visibly different except
        // for an empty or full tray. A set removes those exact duplicates.
        return new LinkedHashSet<>(Arrays.asList(first,first<<(9-count)));
    }
}
