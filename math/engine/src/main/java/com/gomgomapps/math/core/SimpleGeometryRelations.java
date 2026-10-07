package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
/** Relationship-only teaching derived from public measurements, never the answer key. */
public final class SimpleGeometryRelations {
 private SimpleGeometryRelations(){}
 public static final Set<String> IDS=Set.of("el_rectangle_area","el_square_area","el_parallelogram_area","el_circle_area","el_circle_circumference","el_rect_prism_volume","el_rect_prism_surface","el_cube_volume","el_cube_surface","el_circle_diameter","el_circle_radius","el_rectangle_perimeter","el_square_perimeter","el_triangle_perimeter","el_parallelogram_perimeter");
 public static boolean supports(String id){return IDS.contains(id);}
 public static void attach(Question q){
  if(q==null||!supports(q.skillId)||q.prompt==null)return;
  List<Rational> v=new ArrayList<>();Matcher m=Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*cm").matcher(q.prompt);while(m.find())v.add(Expression.number(m.group(1)));
  int needed=Set.of("el_rectangle_area","el_parallelogram_area","el_rectangle_perimeter","el_parallelogram_perimeter").contains(q.skillId)?2:q.skillId.startsWith("el_rect_prism")||q.skillId.equals("el_triangle_perimeter")?3:1;if(v.size()!=needed)return;
  StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="simple-geometry-relations-v1";Rational a=v.get(0),b=needed>1?v.get(1):a,h=needed>2?v.get(2):a;
  switch(q.skillId){
   case "el_circle_diameter":measure(g,"반지름",a);calculate(g,"지름을 구하세요.","반지름 × 2 = ","cm",a.mul(Rational.of(2)));break;
   case "el_circle_radius":measure(g,"지름",a);calculate(g,"반지름을 구하세요.","지름 ÷ 2 = ","cm",a.div(Rational.of(2)));break;
   case "el_rectangle_perimeter":measure(g,"가로",a);measure(g,"세로",b);calculate(g,"두 변의 길이를 더하세요.","가로 + 세로 = ","cm",a.add(b));calculate(g,"둘레를 구하세요.","계산한 길이 합 × 2 = ","cm",a.add(b).mul(Rational.of(2)));break;
   case "el_parallelogram_perimeter":measure(g,"첫 번째 변",a);measure(g,"두 번째 변",b);calculate(g,"두 변의 길이를 더하세요.","첫 번째 변 + 두 번째 변 = ","cm",a.add(b));calculate(g,"둘레를 구하세요.","계산한 길이 합 × 2 = ","cm",a.add(b).mul(Rational.of(2)));break;
   case "el_square_perimeter":measure(g,"한 변",a);calculate(g,"둘레를 구하세요.","한 변 × 4 = ","cm",a.mul(Rational.of(4)));break;
   case "el_triangle_perimeter":measure(g,"첫 번째 변",a);measure(g,"두 번째 변",b);measure(g,"세 번째 변",h);calculate(g,"세 변의 길이를 더하세요.","첫 번째 변 + 두 번째 변 + 세 번째 변 = ","cm",a.add(b).add(h));break;
   case "el_rectangle_area":measure(g,"가로",a);measure(g,"세로",b);calculate(g,"넓이를 구하세요.","가로 × 세로 = ","cm²",a.mul(b));break;
   case "el_square_area":measure(g,"한 변",a);calculate(g,"넓이를 구하세요.","한 변 × 한 변 = ","cm²",a.mul(a));break;
   case "el_parallelogram_area":measure(g,"밑변",a);measure(g,"높이",b);calculate(g,"넓이를 구하세요.","밑변 × 높이 = ","cm²",a.mul(b));break;
   case "el_circle_area":measure(g,"반지름",a);calculate(g,"반지름을 두 번 곱하세요.","반지름 × 반지름 = ","",a.mul(a));calculate(g,"원의 넓이를 구하세요.","계산한 곱 × 3.14 = ","cm²",a.mul(a).mul(Rational.of(157,50)));break;
   case "el_circle_circumference":measure(g,"반지름",a);calculate(g,"지름을 구하세요.","반지름 × 2 = ","cm",a.mul(Rational.of(2)));calculate(g,"원의 둘레를 구하세요.","지름 × 3.14 = ","cm",a.mul(Rational.of(157,25)));break;
   case "el_rect_prism_volume":measure(g,"가로",a);measure(g,"세로",b);calculate(g,"밑면의 넓이를 구하세요.","가로 × 세로 = ","cm²",a.mul(b));measure(g,"높이",h);calculate(g,"부피를 구하세요.","밑면 넓이 × 높이 = ","cm³",a.mul(b).mul(h));break;
   case "el_rect_prism_surface":measure(g,"가로",a);measure(g,"세로",b);measure(g,"높이",h);calculate(g,"밑면의 넓이를 구하세요.","가로 × 세로 = ","cm²",a.mul(b));calculate(g,"앞면의 넓이를 구하세요.","가로 × 높이 = ","cm²",a.mul(h));calculate(g,"옆면의 넓이를 구하세요.","세로 × 높이 = ","cm²",b.mul(h));Rational sum=a.mul(b).add(a.mul(h)).add(b.mul(h));calculate(g,"서로 다른 세 면의 넓이를 더하세요.","밑면 넓이 + 앞면 넓이 + 옆면 넓이 = ","cm²",sum);calculate(g,"겉넓이를 구하세요.","세 면의 넓이 합 × 2 = ","cm²",sum.mul(Rational.of(2)));break;
   case "el_cube_volume":measure(g,"모서리",a);calculate(g,"밑면의 넓이를 구하세요.","모서리 × 모서리 = ","cm²",a.mul(a));calculate(g,"부피를 구하세요.","밑면 넓이 × 모서리 = ","cm³",a.pow(3));break;
   case "el_cube_surface":measure(g,"모서리",a);calculate(g,"한 면의 넓이를 구하세요.","모서리 × 모서리 = ","cm²",a.mul(a));calculate(g,"겉넓이를 구하세요.","한 면의 넓이 × 6 = ","cm²",a.mul(a).mul(Rational.of(6)));break;
  }q.studyGuide=g;
 }
 private static void measure(StudyGuide g,String label,Rational v){g.step("문제에서 "+label+" 값을 찾아 쓰세요.",label+" = "," cm",v.toString());}
 private static void calculate(StudyGuide g,String instruction,String before,String unit,Rational v){g.step(instruction,before,unit.isEmpty()?"":" "+unit,v.toString());}
}
