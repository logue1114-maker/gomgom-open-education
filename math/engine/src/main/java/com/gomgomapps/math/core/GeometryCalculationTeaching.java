package com.gomgomapps.math.core;
import java.util.Set;
import java.util.regex.*;
/** Break compound geometry calculations into separately entered operations. */
public final class GeometryCalculationTeaching {
 private GeometryCalculationTeaching(){}
 public static boolean relationArea(String id){return Set.of("el_triangle_area","el_rhombus_area","el_trapezoid_area").contains(id);}
 public static boolean relationAngles(String id){return Set.of("el_triangle_angle_sum","el_quadrilateral_angle_sum").contains(id);}
 private static StudyGuide relationshipGuide(){StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="elementary-area-relations-v1";return g;}
 private static void measure(StudyGuide g,String label,long value){g.step("문제에서 "+label+" 값을 찾아 쓰세요.",label+" = "," cm",Long.toString(value));}
 static void attach(Question q){
  if(Set.of("el_triangle_area","el_rhombus_area").contains(q.skillId)){
   Matcher m=Pattern.compile("(\\d+)\\*(\\d+)/2").matcher(q.expression);if(!m.matches())return;
   long a=Long.parseLong(m.group(1)),b=Long.parseLong(m.group(2)),product=a*b;
   StudyGuide g=relationshipGuide();boolean triangle=q.skillId.equals("el_triangle_area");
   measure(g,triangle?"밑변":"첫 번째 대각선",a);measure(g,triangle?"높이":"두 번째 대각선",b);
   q.studyGuide=g.step(triangle?"밑변과 높이를 곱하세요.":"두 대각선의 길이를 곱하세요.",triangle?"밑변 × 높이 = ":"첫 번째 대각선 × 두 번째 대각선 = ","",Long.toString(product))
    .step("계산한 곱을 2로 나누세요.","계산한 곱 ÷ 2 = "," cm²",Rational.of(product,2).toString());return;
  }
  if(q.skillId.equals("el_trapezoid_area")){
   Matcher m=Pattern.compile("\\((\\d+)\\+(\\d+)\\)\\*(\\d+)/2").matcher(q.expression);if(!m.matches())return;
   long a=Long.parseLong(m.group(1)),b=Long.parseLong(m.group(2)),height=Long.parseLong(m.group(3)),sum=a+b,product=sum*height;
   StudyGuide g=relationshipGuide();measure(g,"첫 번째 평행한 변",a);measure(g,"두 번째 평행한 변",b);
   g.step("평행한 두 변의 길이를 더하세요.","첫 번째 변 + 두 번째 변 = ","",Long.toString(sum));measure(g,"높이",height);
   q.studyGuide=g.step("두 변의 합에 높이를 곱하세요.","두 변의 합 × 높이 = ","",Long.toString(product))
    .step("계산한 곱을 2로 나누세요.","계산한 곱 ÷ 2 = "," cm²",Rational.of(product,2).toString());return;
  }
  if(relationAngles(q.skillId)){
   Matcher m=Pattern.compile("(180|360)-(\\d+)-(\\d+)(?:-(\\d+))?").matcher(q.expression);if(!m.matches())return;
   int total=Integer.parseInt(m.group(1)),a=Integer.parseInt(m.group(2)),b=Integer.parseInt(m.group(3)),c=m.group(4)==null?0:Integer.parseInt(m.group(4)),sum=a+b+c;
   boolean triangle=q.skillId.equals("el_triangle_angle_sum");
   if(sum>=total||a<=0||b<=0||triangle&&(total!=180||m.group(4)!=null)||!triangle&&(total!=360||m.group(4)==null||c<=0))return;
   StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="angle-sum-relations-v1";
   String[] labels={"첫 번째 각","두 번째 각","세 번째 각"};int[] values={a,b,c};
   for(int i=0;i<(triangle?2:3);i++)g.step("문제에서 "+labels[i]+"의 크기를 찾아 쓰세요.",labels[i]+" = ","°",Integer.toString(values[i]));
   q.studyGuide=g.step("주어진 각의 크기를 모두 더하세요.",triangle?"첫 번째 각 + 두 번째 각 = ":"첫 번째 각 + 두 번째 각 + 세 번째 각 = ","°",Integer.toString(sum))
    .step("내각의 합에서 계산한 합을 빼세요.",total+"° − 계산한 합 = ","°",Integer.toString(total-sum));
  }
 }
}
