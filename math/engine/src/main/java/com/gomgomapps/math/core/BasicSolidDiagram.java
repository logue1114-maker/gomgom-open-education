package com.gomgomapps.math.core;
import java.util.regex.*;

/** Derive display geometry from public lengths without mutating the saved question identity. */
public final class BasicSolidDiagram {
 private BasicSolidDiagram(){}
 public static StudyDiagram forQuestion(Question q){
  if(q==null)return null;
  if(q.prompt==null)return InteriorAngleDiagram.forQuestion(q);
  String n="(\\d+(?:\\.\\d+)?(?:/\\d+)?)";
  String pattern;String type;String[] labels;
  switch(q.skillId){
   case "sec_prism_surface":pattern="가로 "+n+", 세로 "+n+", 높이 "+n+"인 직육면체의 겉넓이는\\?";type="basicSolidCuboid";labels=new String[]{"가로","세로","높이"};break;
   case "sec_cylinder_surface":pattern="반지름이 "+n+", 높이가 "+n+"인 원기둥의 겉넓이는 \\(□\\)π입니다\\. □는\\?";type="basicSolidCylinder";labels=new String[]{"반지름","높이"};break;
   case "sec_cone_surface":pattern="밑면 반지름이 "+n+", 모선이 "+n+"인 원뿔의 겉넓이는 \\(□\\)π입니다\\. □는\\?";type="basicSolidConeSlant";labels=new String[]{"반지름","모선"};break;
   case "sec_cone_volume":pattern="반지름이 "+n+", 높이가 "+n+"인 원뿔의 부피는 \\(□\\)π입니다\\. □는\\?";type="basicSolidConeHeight";labels=new String[]{"반지름","높이"};break;
   case "sec_sphere_surface":case "sec_sphere_volume":pattern="반지름이 "+n+"인 구의 "+(q.skillId.equals("sec_sphere_surface")?"겉넓이":"부피")+"는 \\(□\\)π입니다\\. □는\\?";type="basicSolidSphere";labels=new String[]{"반지름"};break;
   default:return InteriorAngleDiagram.forQuestion(q);
  }
  Matcher m=Pattern.compile(pattern).matcher(q.prompt);if(!m.matches())return InteriorAngleDiagram.forQuestion(q);
  double[] values=new double[m.groupCount()];for(int i=0;i<values.length;i++){Rational v=Expression.number(m.group(i+1));values[i]=v.n.doubleValue()/v.d.doubleValue();}
  return new StudyDiagram(type,values,labels);
 }
}
