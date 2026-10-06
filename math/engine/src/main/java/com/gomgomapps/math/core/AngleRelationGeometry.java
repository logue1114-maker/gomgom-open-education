package com.gomgomapps.math.core;

/** Unit coordinates derived solely from the diagram's public given and relation. */
public final class AngleRelationGeometry {
 private AngleRelationGeometry(){}
 /** Circle: A,B,C,O. Parallel lines: upper and lower transversal intersections. */
 public static double[][] vertices(StudyDiagram d){
  double given=d.values[0];int mode=(int)d.values[1];
  if(d.type.equals("circleAngleRelation")){
   double central=mode==1?2*given:given,half=Math.toRadians(central/2);
   return new double[][]{{-Math.sin(half),Math.cos(half)},{Math.sin(half),Math.cos(half)},{0,-1},{0,0}};
  }
  if(d.type.equals("parallelAngleRelation")){
   double theta=Math.toRadians(mode==2?180-given:given),dx=Math.cos(theta)/Math.sin(theta);
   return new double[][]{{dx/2,.5},{-dx/2,-.5}};
  }
  throw new IllegalArgumentException("Unsupported angle diagram: "+d.type);
 }
}
