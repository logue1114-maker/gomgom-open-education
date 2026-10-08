package com.gomgomapps.math.core;

import java.util.*;
import java.util.regex.*;

/** Compare public whole numbers by place value, without subtracting them. */
public final class WholeCompareRelations {
    private WholeCompareRelations() {}
    public static boolean supports(String id) { return "el_compare_10000".equals(id); }
    public record Frame(String instruction, String name, String expected, int[] prior, boolean choice) {}
    private static final String[] PLACES={"일","십","백","천","만"};
    public static List<Frame> frames(Question q) {
        if(q==null||q.prompt==null||!supports(q.skillId))return List.of();
        Matcher m=Pattern.compile("^(\\d+)\\s+□\\s+(\\d+)$").matcher(q.prompt);
        if(!m.matches())return List.of();
        try {
            int left=Integer.parseInt(m.group(1)),right=Integer.parseInt(m.group(2));
            if(left>10000||right>10000)return List.of();
            List<Frame> f=new ArrayList<>();
            f.add(new Frame("문제의 왼쪽 수를 쓰세요.","왼쪽 수",Integer.toString(left),new int[0],false));
            f.add(new Frame("문제의 오른쪽 수를 쓰세요.","오른쪽 수",Integer.toString(right),new int[0],false));
            int power=1,place=0;
            while(power<=Math.max(left,right)/10){power*=10;place++;}
            int li=0,ri=1;
            while(power>0){
                int ld=left/power%10,rd=right/power%10;
                String instruction="가장 큰 자리부터 비교합니다. 해당 자리가 없으면 0을 쓰세요.";
                li=f.size();f.add(new Frame(instruction,"왼쪽 수의 "+PLACES[place]+"의 자리 숫자",Integer.toString(ld),new int[]{0},false));
                ri=f.size();f.add(new Frame(instruction,"오른쪽 수의 "+PLACES[place]+"의 자리 숫자",Integer.toString(rd),new int[]{1},false));
                if(ld!=rd)break;
                power/=10;place--;
            }
            f.add(new Frame("처음 다른 자리의 숫자를 비교해 기호를 고르세요. 모든 자리가 같으면 =를 고르세요.","비교 기호",left<right?"<":left>right?">":"=",new int[]{0,1,li,ri},true));
            return List.copyOf(f);
        }catch(NumberFormatException invalid){return List.of();}
    }
    public static void attach(Question q){
        var frames=frames(q);if(frames.isEmpty())return;
        StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="whole-compare-relations-v1";
        for(var f:frames){
            if(f.choice())g.choice(f.instruction(),Map.of("<","<","=","=",">",">"),f.expected());
            else g.step(f.instruction(),f.name()+" = ","",f.expected());
        }
        q.studyGuide=g;
    }
}
