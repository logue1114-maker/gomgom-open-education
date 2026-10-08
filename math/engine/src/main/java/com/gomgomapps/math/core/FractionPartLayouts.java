package com.gomgomapps.math.core;

import java.util.*;

/** Equal-area parts with observable colored positions, without a written numerator. */
public final class FractionPartLayouts {
    private FractionPartLayouts(){}
    static void append(Map<String,Question> candidates,CurriculumLimits limits){
        for(int denominator=2;denominator<=12;denominator++)for(int selected=1;selected<denominator;selected++)for(int start=0;start<denominator;start++){
            int mask=0;for(int offset=0;offset<selected;offset++)mask|=1<<((start+offset)%denominator);
            Question q=new Question("fractionPart","색칠한 부분을 분수로 나타내세요.",selected+"/"+denominator,Rational.of(selected,denominator).toString()).withInputs(selected,denominator);
            q.diagram=new StudyDiagram("fractionSelection",new double[]{denominator,mask});q.stepSupport=false;
            q.studyGuide=new StudyGuide().transfer(false)
                .step("전체 칸을 세어 분모를 쓰세요.","전체 칸 수 = ","",String.valueOf(denominator))
                .step("색칠한 칸을 세어 분자를 쓰세요.","색칠한 칸 수 = ","",String.valueOf(selected)).fractionResult(1,0);
            q.studyGuide.teachingVersion="fraction-part-layouts-v1";
            if(limits.allows(q))candidates.put(q.signature(),q);
        }
    }
    public static int columns(int denominator){
        if(denominator<2||denominator>12)throw new IllegalArgumentException("Part layout requires 2–12 parts");
        for(int candidate=2;candidate<=denominator;candidate++)if(candidate*candidate>=denominator&&denominator%candidate==0)return candidate;
        throw new IllegalStateException("Missing rectangular layout");
    }
}
