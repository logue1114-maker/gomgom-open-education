package com.gomgomapps.math.core;

import java.util.*;
import java.util.regex.*;

/** Students read public coordinates and build their own intermediate calculations. */
public final class MovementCircleRelations {
    private MovementCircleRelations(){}
    public static boolean supports(String id){return Set.of("sec_circle_line_intersections","sec_translation","sec_reflection").contains(id);}
    public static void attach(Question q){
        if(q==null||q.prompt==null||!supports(q.skillId))return;
        String n="([+-]?\\d+(?:\\.\\d+)?(?:/\\d+)?)";
        Matcher m;StudyGuide g=new StudyGuide().transfer(false);
        if(q.skillId.equals("sec_circle_line_intersections")){
            m=Pattern.compile("원 x²\\+y²="+n+"과 직선 ([xy])="+n+"의 교점 수를 고르세요\\.").matcher(q.prompt);
            if(!m.matches())return;Rational s=num(m,1);if(s.compareTo(Rational.ZERO)<=0)return;
            Rational radius=s.sqrt(),t=num(m,3),distance=t.compareTo(Rational.ZERO)<0?t.neg():t,difference=distance.sub(radius);
            g.teachingVersion="circle-intersection-relations-v1";
            step(g,"원 방정식 오른쪽의 반지름 제곱값을 쓰세요.","반지름의 제곱 s = ",s);
            step(g,"반지름은 양수입니다. s의 양의 제곱근을 구하세요.","반지름 r = √s = ",radius);
            step(g,"직선 방정식 오른쪽의 좌표값을 부호까지 쓰세요.","직선 좌표 t = ",t);
            step(g,"원의 중심은 원점입니다. 직선 좌표값의 절댓값이 중심과 직선 사이의 거리입니다.","거리 d = |t| = ",distance);
            step(g,"거리에서 반지름을 빼세요.","비교값 v = d − r = ",difference);
            Map<String,String> labels=new LinkedHashMap<>();labels.put("2","2개");labels.put("1","1개");labels.put("0","없음");
            g.choice("비교값이 음수면 교점 2개, 0이면 1개, 양수면 없습니다.",labels,difference.compareTo(Rational.ZERO)<0?"2":difference.isZero()?"1":"0");
        }else if(q.skillId.equals("sec_translation")){
            m=Pattern.compile("점 P\\("+n+", "+n+"\\)를 벡터 \\("+n+", "+n+"\\)만큼 평행이동한 좌표는\\?").matcher(q.prompt);
            if(!m.matches())return;Rational x=num(m,1),y=num(m,2),a=num(m,3),b=num(m,4);
            g.teachingVersion="translation-relations-v1";
            coordinates(g,x,y);
            step(g,"벡터의 첫째 값은 가로 이동량입니다. 부호까지 쓰세요.","가로 이동 a = ",a);
            step(g,"벡터의 둘째 값은 세로 이동량입니다. 부호까지 쓰세요.","세로 이동 b = ",b);
            step(g,"처음 x좌표에 가로 이동량을 더하세요.","x′ = x + a = ",x.add(a));
            step(g,"처음 y좌표에 세로 이동량을 더하세요.","y′ = y + b = ",y.add(b));
        }else{
            m=Pattern.compile("점 \\("+n+", "+n+"\\)를 (y축|x축|원점)에 대하여 대칭이동한 좌표는\\?").matcher(q.prompt);
            if(!m.matches())return;Rational x=num(m,1),y=num(m,2);String target=m.group(3);
            boolean flipX=!target.equals("x축"),flipY=!target.equals("y축");
            g.teachingVersion="reflection-relations-v1";coordinates(g,x,y);
            step(g,flipX?"대칭이동하면 x좌표의 부호가 바뀝니다. 반대 수를 구하세요.":"x축 대칭에서는 x좌표가 그대로입니다.",flipX?"x′ = −x = ":"x′ = x = ",flipX?x.neg():x);
            step(g,flipY?"대칭이동하면 y좌표의 부호가 바뀝니다. 반대 수를 구하세요.":"y축 대칭에서는 y좌표가 그대로입니다.",flipY?"y′ = −y = ":"y′ = y = ",flipY?y.neg():y);
        }
        q.studyGuide=g;
    }
    private static void coordinates(StudyGuide g,Rational x,Rational y){
        step(g,"처음 점의 x좌표를 부호까지 쓰세요.","처음 x좌표 x = ",x);
        step(g,"처음 점의 y좌표를 부호까지 쓰세요.","처음 y좌표 y = ",y);
    }
    private static Rational num(Matcher m,int i){return Expression.number(m.group(i));}
    private static void step(StudyGuide g,String instruction,String before,Rational expected){g.step(instruction,before,"",expected.toString());}
}
