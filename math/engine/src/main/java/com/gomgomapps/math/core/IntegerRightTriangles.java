package com.gomgomapps.math.core;

import java.util.*;

/** Small exact lengths for foundation drills; do not enlarge numbers to inflate a question count. */
final class IntegerRightTriangles {
    private IntegerRightTriangles(){}
    private static final List<int[]> TRIANGLES=build();
    private static List<int[]> build(){
        List<int[]> result=new ArrayList<>();
        for(int a=1;a<50;a++)for(int b=a;b<50;b++){
            int square=a*a+b*b,c=(int)Math.sqrt(square);
            if(c<=50&&c*c==square)result.add(new int[]{a,b,c});
        }
        return List.copyOf(result);
    }
    static int[] next(Random random){return TRIANGLES.get(random.nextInt(TRIANGLES.size())).clone();}
    static Question pythagoras(Catalog.Skill skill,Random random){
        int[] sides=next(random);int missing=random.nextInt(3);
        boolean leg=missing<2;int first=leg?sides[2]:sides[0],second=leg?sides[1-missing]:sides[1];
        String calculation=first+"^2"+(leg?"-":"+")+second+"^2";
        String prompt=leg?"빗변의 길이가 "+first+", 직각을 낀 한 변의 길이가 "+second+"일 때 다른 변의 길이는?":
                "직각을 낀 두 변의 길이가 "+first+", "+second+"일 때 빗변의 길이는?";
        Question q=new Question(skill.id,prompt,"sqrt("+calculation+")",String.valueOf(sides[missing])).withInputs(first,second,leg?1:0);
        q.studyGuide=new StudyGuide()
                .step(leg?"빗변의 제곱에서 알고 있는 다른 변의 제곱을 빼세요.":"직각을 낀 두 변의 제곱을 더하세요.",calculation+" = ","",String.valueOf(sides[missing]*sides[missing]))
                .step("길이는 양수입니다. 양의 제곱근을 구하세요.","√("+calculation+") = ","",String.valueOf(sides[missing]));
        return q;
    }
}
