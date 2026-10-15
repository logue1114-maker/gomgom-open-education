package com.gomgomapps.math.core;

import java.util.*;
import java.util.regex.*;

/** Three existing angle questions, read only from their visible sentences. */
public final class KoreanRightAngles {
    private KoreanRightAngles(){}
    public static final String ID="el_shape_angle",VERSION="korean-right-angles-v1",ACTION_FORMAT="rightAngleAction";
    public static final String RECT="RECT",SPLIT="SPLIT",LARGER="LARGER";
    public static final String OLD_VERSION="elementary-split-angle-relations-v1";
    public record Givens(String kind,int known){}
    private static final Pattern SPLIT_PROMPT=Pattern.compile("직각을 두 각으로 나누었습니다\\. 한 각이 ([0-9]{1,2})도일 때, 다른 각은 몇 도인가요\\?");
    private static final Pattern LARGER_PROMPT=Pattern.compile("직각보다 ([0-9]{1,2})도 큰 각은 몇 도인가요\\?");
    public static boolean supports(String id){return ID.equals(id);}
    public static Givens read(Question q){
        if(q==null||!supports(q.skillId)||q.prompt==null||q.prompt.length()>160)return null;
        if(q.prompt.equals("직사각형의 한 각은 몇 도인가요?"))return new Givens(RECT,0);
        Matcher split=SPLIT_PROMPT.matcher(q.prompt),larger=LARGER_PROMPT.matcher(q.prompt);
        String kind;int known;
        if(split.matches()){kind=SPLIT;known=Integer.parseInt(split.group(1));}
        else if(larger.matches()){kind=LARGER;known=Integer.parseInt(larger.group(1));}else return null;
        return known>=1&&known<=(LARGER.equals(kind)?90:89)?new Givens(kind,known):null;
    }
    private static Map<String,String> relationships(){
        Map<String,String> choices=new LinkedHashMap<>();choices.put("same","직각 한 개와 같음");choices.put("sumRight","두 각을 합하면 직각");choices.put("rightPlus","직각에 주어진 각을 더함");return choices;
    }
    public static StudyGuide guide(Question q){
        Givens g=read(q);if(g==null)return null;
        StudyGuide h=new StudyGuide().transfer(false);h.teachingVersion=VERSION;
        h.step("한 바퀴는 360도입니다. 네 등분한 한 부분까지 펼쳐 직각 90도를 살펴보세요.","","","ok");h.frames.get(0).inputFormat=ACTION_FORMAT;
        h.choice(RECT.equals(g.kind)?"직사각형의 한 각과 직각을 비교하세요.":SPLIT.equals(g.kind)?"나눈 두 각과 직각의 관계를 고르세요.":"문제의 각과 직각의 관계를 고르세요.",relationships(),RECT.equals(g.kind)?"same":SPLIT.equals(g.kind)?"sumRight":"rightPlus");
        if(!RECT.equals(g.kind)){
            Map<String,String> choices=new LinkedHashMap<>();choices.put("given",Integer.toString(g.known));choices.put("subtract","90 − "+g.known);choices.put("add","90 + "+g.known);
            h.choice(SPLIT.equals(g.kind)?"알려진 부분을 빼고 남은 각을 구하는 식을 고르세요.":"직각에 더 커진 부분을 더하는 식을 고르세요.",choices,SPLIT.equals(g.kind)?"subtract":"add");
        }
        int result=RECT.equals(g.kind)?90:SPLIT.equals(g.kind)?90-g.known:90+g.known;
        h.step("원래 문제의 각의 크기를 직접 쓰세요.","각의 크기 = "," 도",Integer.toString(result));return h;
    }
    /** Public-model checks only; no cached guide or answer supplies a feedback premise. */
    public static String feedback(Question q,int stage,String raw){
        Givens g=read(q);StudyGuide h=guide(q);if(g==null||raw==null||raw.isBlank()||raw.length()>120||stage<0||stage>=h.frames.size())return null;
        StudyGuide.Frame f=h.frames.get(stage);if(stage==0)return "직각까지 펼치는 움직임을 끝까지 본 뒤 확인하세요.";
        if(new HelpPlan.Step(f).accepts(raw))return null;
        if(!f.options.isEmpty()&&!f.options.containsKey(raw.trim()))return null;
        if(f.options.isEmpty())try{Expression.number(raw);}catch(RuntimeException invalid){return null;}
        if(RECT.equals(g.kind))return "직사각형의 한 각은 직각입니다. 펼쳐 본 직각 한 개와 비교하세요.";
        if(SPLIT.equals(g.kind))return "두 부분을 모으면 직각 한 개가 됩니다. 알려진 부분을 빼고 남은 부분을 다시 살펴보세요.";
        return "문제의 각은 직각보다 더 큽니다. 직각에 더 커진 부분을 더했는지 확인하세요.";
    }
}
