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
    static Question pythagoras(Catalog.Skill skill,Random random){return RightTriangleLength.create(skill,random);}
}
