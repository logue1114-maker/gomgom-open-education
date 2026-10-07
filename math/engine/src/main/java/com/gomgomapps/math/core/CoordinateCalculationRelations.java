package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
/** Reconstruct symbolic steps from public coordinates, never answer metadata. */
public final class CoordinateCalculationRelations {
 private CoordinateCalculationRelations(){}
 public static boolean supports(String id){return Set.of("sec_point_distance","sec_internal_division","sec_line_equation").contains(id);}
 public static void attach(Question q){
  if(q==null||!supports(q.skillId)||q.prompt==null)return;
  String n="([+-]?\\d+(?:\\.\\d+)?(?:/\\d+)?)";Matcher m;StudyGuide g=new StudyGuide().transfer(false);
  if(q.skillId.equals("sec_point_distance")){
   m=Pattern.compile("두 점 \\("+n+", "+n+"\\), \\("+n+", "+n+"\\) 사이의 거리는\\?").matcher(q.prompt);if(!m.matches())return;
   Rational dx=num(m,3).sub(num(m,1)),dy=num(m,4).sub(num(m,2)),u=dx.pow(2),v=dy.pow(2),s=u.add(v);
   g.teachingVersion="point-distance-relations-v1";
   step(g,"첫째 점을 A, 둘째 점을 B로 두고 x좌표의 차를 구하세요.","Δx = xB − xA = ",dx);
   step(g,"같은 순서로 y좌표의 차를 구하세요.","Δy = yB − yA = ",dy);
   step(g,"x좌표의 차를 제곱하세요.","u = (Δx)² = ",u);step(g,"y좌표의 차를 제곱하세요.","v = (Δy)² = ",v);
   step(g,"두 제곱을 더하세요.","s = u + v = ",s);step(g,"제곱합의 음이 아닌 제곱근을 구하세요.","거리 = √s = ",s.sqrt());
  }else if(q.skillId.equals("sec_internal_division")){
   m=Pattern.compile("A\\("+n+", "+n+"\\)와 B\\("+n+", "+n+"\\)를 AP:PB="+n+":"+n+"로 내분하는 P의 좌표는\\?").matcher(q.prompt);if(!m.matches())return;
   Rational a=num(m,5),b=num(m,6);if(a.compareTo(Rational.ZERO)<=0||b.compareTo(Rational.ZERO)<=0)return;Rational t=a.add(b),dx=num(m,3).sub(num(m,1)),dy=num(m,4).sub(num(m,2)),ux=dx.div(t),uy=dy.div(t),ox=a.mul(ux),oy=a.mul(uy);
   g.teachingVersion="internal-division-relations-v1";
   step(g,"AP:PB에서 AP에 해당하는 비 m을 쓰세요.","AP의 비 m = ",a);step(g,"PB에 해당하는 비 n을 쓰세요.","PB의 비 n = ",b);step(g,"전체 선분의 비를 더하세요.","t = m + n = ",t);
   step(g,"A에서 B까지 x좌표의 차를 구하세요.","Δx = xB − xA = ",dx);step(g,"x좌표의 차를 전체 비로 나누세요.","한 몫의 x 이동 = Δx ÷ t = ",ux);step(g,"A에서 P까지는 m몫입니다. x 이동량을 구하세요.","x 이동량 = m × 한 몫의 x 이동 = ",ox);step(g,"A의 x좌표에 이동량을 더하세요.","xP = xA + x 이동량 = ",num(m,1).add(ox));
   step(g,"A에서 B까지 y좌표의 차를 구하세요.","Δy = yB − yA = ",dy);step(g,"y좌표의 차를 전체 비로 나누세요.","한 몫의 y 이동 = Δy ÷ t = ",uy);step(g,"A에서 P까지 y 이동량을 구하세요.","y 이동량 = m × 한 몫의 y 이동 = ",oy);step(g,"A의 y좌표에 이동량을 더하세요.","yP = yA + y 이동량 = ",num(m,2).add(oy));
  }else{
   m=Pattern.compile("직선 y="+n+"x"+n+" 위에서 x="+n+"일 때 y는\\?").matcher(q.prompt);if(!m.matches())return;
   Rational a=num(m,1),b=num(m,2),x=num(m,3),p=a.mul(x);g.teachingVersion="line-value-relations-v1";
   step(g,"y=ax+b에서 x의 계수 a를 부호까지 쓰세요.","기울기 a = ",a);step(g,"상수 b를 부호까지 쓰세요.","상수 b = ",b);step(g,"문제에서 주어진 x를 쓰세요.","입력 x = ",x);step(g,"기울기에 x를 곱하세요.","p = a × x = ",p);step(g,"곱에 상수 b를 더하세요.","y = p + b = ",p.add(b));
  }
  q.studyGuide=g;
 }
 private static Rational num(Matcher m,int i){return Expression.number(m.group(i));}
 private static void step(StudyGuide g,String instruction,String frame,Rational value){g.step(instruction,frame,"",value.toString());}
}
