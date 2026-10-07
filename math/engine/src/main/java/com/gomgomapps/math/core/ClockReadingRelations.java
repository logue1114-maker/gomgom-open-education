package com.gomgomapps.math.core;

import java.util.*;

/** Learner readings of the publicly drawn hands; hidden answers are never used. */
public final class ClockReadingRelations {
    private ClockReadingRelations(){}
    public static final Set<String> IDS=Set.of("el_clock_hour","el_clock_minute","el_clock_second");
    public static boolean supports(String id){return IDS.contains(id);}
    public record Frame(String instruction,String label,String result,String expected,int[] prior){}
    private static boolean whole(double value,int minimum,int maximum){return Double.isFinite(value)&&value==Math.rint(value)&&value>=minimum&&value<=maximum;}
    public static List<Frame> frames(Question q){
        if(q==null||!supports(q.skillId)||q.diagram==null||!"clock".equals(q.diagram.type)||q.diagram.values==null)return List.of();
        double[] hands=q.diagram.values;if(hands.length<2||hands.length>3||!whole(hands[0],1,12)||!whole(hands[1],0,59)||hands.length==3&&!whole(hands[2],0,59))return List.of();
        if(q.skillId.equals("el_clock_hour"))return List.of(new Frame("짧은 바늘이 지난 숫자를 쓰세요.","시침에서 읽은 시 = ","시침에서 읽은 시",String.valueOf((int)hands[0]),new int[0]));
        boolean seconds=q.skillId.equals("el_clock_second");if(seconds&&hands.length!=3)return List.of();
        int value=(int)hands[seconds?2:1],remainder=value%5;
        String unit=seconds?"초":"분",hand=seconds?"빨간 초침":"긴 바늘",result=(seconds?"초침":"분침")+"에서 읽은 "+unit;
        String rule="큰 눈금은 5"+unit+"씩 늘어납니다. 12는 0"+unit+"입니다.";
        if(remainder==0)return List.of(new Frame(rule,result+" = ",result,String.valueOf(value),new int[0]));
        String coarse="큰 눈금에서 읽은 "+unit,small="작은 눈금에서 읽은 "+unit;
        return List.of(new Frame(rule,coarse+" = ",coarse,String.valueOf(value-remainder),new int[0]),
                new Frame("마지막 큰 눈금에서 "+hand+"까지 작은 눈금을 세세요.",small+" = ",small,String.valueOf(remainder),new int[0]),
                new Frame("큰 눈금과 작은 눈금에서 읽은 값을 더하세요.",coarse+" + "+small+" = ",result,String.valueOf(value),new int[]{0,1}));
    }
    public static void attach(Question q){
        List<Frame> frames=frames(q);if(frames.isEmpty())return;
        StudyGuide guide=new StudyGuide().transfer(false);guide.teachingVersion="clock-reading-relations-v1";
        for(Frame frame:frames)guide.step(frame.instruction,frame.label,"",frame.expected);
        q.studyGuide=guide;
    }
}
