package com.gomgomapps.math.core;

import java.util.regex.*;

/** Geometry is constructed from the public givens, never from the answer key. */
public final class InteriorAngleDiagram {
    private InteriorAngleDiagram(){}
    public static StudyDiagram forQuestion(Question q){
        if(!q.skillId.equals("el_triangle_angle_sum")&&!q.skillId.equals("el_quadrilateral_angle_sum"))return q.diagram;
        Matcher m=Pattern.compile("(180|360)-(\\d+)-(\\d+)(?:-(\\d+))?").matcher(q.expression);
        if(!m.matches())return q.diagram;
        boolean triangle=q.skillId.equals("el_triangle_angle_sum");
        if(triangle&&!m.group(1).equals("180")||!triangle&&!m.group(1).equals("360"))return q.diagram;
        if(triangle&&m.group(4)!=null||!triangle&&m.group(4)==null)return q.diagram;
        double[] angles=triangle?new double[]{Double.parseDouble(m.group(2)),Double.parseDouble(m.group(3))}:new double[]{Double.parseDouble(m.group(2)),Double.parseDouble(m.group(3)),Double.parseDouble(m.group(4))};
        if(!triangle&&angles[0]+angles[1]+angles[2]==180)return new StudyDiagram("angleGivens",angles,"사각형 내각");
        return new StudyDiagram(triangle?"triangleAngles":"quadrilateralAngles",angles,triangle?"삼각형 내각":"사각형 내각");
    }
    public static double[][] vertices(double... angles){
        if(angles.length!=2&&angles.length!=3)throw new IllegalArgumentException("Two or three given angles required");
        for(double angle:angles)if(!(angle>0&&angle<180))throw new IllegalArgumentException("Given angles must be between zero and 180 degrees");
        double a=Math.toRadians(angles[0]),b=Math.toRadians(angles[1]);
        double[] rayA={Math.cos(a),Math.sin(a)},rayB={-Math.cos(b),Math.sin(b)};
        if(angles.length==2){
            if(angles[0]+angles[1]>=180)throw new IllegalArgumentException("Triangle angles must sum below 180");
            double scale=cross(new double[]{1,0},rayB)/cross(rayA,rayB);
            return new double[][]{{0,0},{1,0},{scale*rayA[0],scale*rayA[1]}};
        }
        double sum=angles[0]+angles[1]+angles[2];
        if(sum>=360||Math.abs(sum-180)<1e-9)throw new IllegalArgumentException("Nondegenerate quadrilateral required");
        double direction=Math.toRadians(360-angles[1]-angles[2]);double[] rayC={Math.cos(direction),Math.sin(direction)};
        double[] originB={1,0};double denominatorC=cross(rayC,rayA),denominatorA=cross(rayA,rayC);
        double[] constants={-cross(originB,rayA)/denominatorC,cross(originB,rayC)/denominatorA};
        double[] coefficients={-cross(rayB,rayA)/denominatorC,cross(rayB,rayC)/denominatorA};
        double lower=0,upper=Double.POSITIVE_INFINITY;
        for(int i=0;i<2;i++){
            if(Math.abs(coefficients[i])<1e-12){if(constants[i]<=0)throw new IllegalArgumentException("No positive edge length");continue;}
            double boundary=-constants[i]/coefficients[i];
            if(coefficients[i]>0)lower=Math.max(lower,boundary);else upper=Math.min(upper,boundary);
        }
        if(upper<=lower)throw new IllegalArgumentException("No simple quadrilateral for these givens");
        if(!Double.isFinite(upper))upper=lower+Math.max(2,lower*2);
        double[][] best=null;double score=-1;
        for(int i=1;i<=200;i++){
            double length=lower+(upper-lower)*i/201.0;double[] c={1+length*rayB[0],length*rayB[1]};
            double alongC=-cross(c,rayA)/cross(rayC,rayA),alongA=cross(c,rayC)/cross(rayA,rayC);
            if(alongC<=1e-8||alongA<=1e-8)continue;
            double shortest=Math.min(1,Math.min(length,Math.min(alongC,alongA))),longest=Math.max(1,Math.max(length,Math.max(alongC,alongA)));
            if(shortest/longest>score){score=shortest/longest;best=new double[][]{{0,0},{1,0},c,{alongA*rayA[0],alongA*rayA[1]}};}
        }
        if(best==null)throw new IllegalArgumentException("No simple quadrilateral for these givens");
        return best;
    }
    private static double cross(double[] a,double[] b){return a[0]*b[1]-a[1]*b[0];}
}
