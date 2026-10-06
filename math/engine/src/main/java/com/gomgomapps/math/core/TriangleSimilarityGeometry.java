package com.gomgomapps.math.core;

/** Two triangles derived from public givens, in the labelled correspondence order. */
public final class TriangleSimilarityGeometry {
 private TriangleSimilarityGeometry(){}
 public static double[][][] vertices(StudyDiagram diagram){
  if(!diagram.type.equals("triangleSimilarity")||diagram.values.length!=7)throw new IllegalArgumentException("Unsupported similarity diagram");
  double[] v=diagram.values;int mode=(int)v[0],condition=mode%3;
  if(condition==0)return new double[][][]{angles(v[1],v[2]),angles(v[4],v[5])};
  if(condition==1)return new double[][][]{sas(v[1],v[2],v[3]),sas(v[4],mode==4?v[2]*v[4]/v[1]:v[5],v[6])};
  return new double[][][]{sides(v[1],v[2],v[3]),sides(v[4],v[5],mode==5?v[3]*v[4]/v[1]:v[6])};
 }
 private static double[][] angles(double a,double b){double ac=Math.sin(Math.toRadians(b))/Math.sin(Math.toRadians(180-a-b));return sas(1,ac,a);}
 private static double[][] sas(double ab,double ac,double angle){double r=Math.toRadians(angle);return new double[][]{{0,0},{ab,0},{ac*Math.cos(r),ac*Math.sin(r)}};}
 private static double[][] sides(double ab,double ac,double bc){double x=(ab*ab+ac*ac-bc*bc)/(2*ab),square=ac*ac-x*x;if(square<=0)throw new IllegalArgumentException("Degenerate triangle");return new double[][]{{0,0},{ab,0},{x,Math.sqrt(square)}};}
}
