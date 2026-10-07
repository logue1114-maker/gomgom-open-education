package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;

/** Area and volume relationships from the published measurements, not answer metadata. */
public final class SolidFoundationRelations {
 private SolidFoundationRelations(){}
 public static boolean supports(String id){return Set.of("regularPolygonArea","pyramidVolume").contains(id);}
 public static void attach(Question q){
  if(q==null||!supports(q.skillId)||q.prompt==null)return;
  String n="(\\d+(?:\\.\\d+)?(?:/\\d+)?)";StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="solid-foundation-relations-v1";
  if(q.skillId.equals("regularPolygonArea")){
   Matcher m=Pattern.compile("(정오각형|정육각형) · 한 변 "+n+"cm\\n중심에서 변까지 수직거리 "+n+"cm\\n수직거리는 소수 첫째 자리 근삿값입니다\\.\\n주어진 길이로 넓이는 몇 cm²인가요\\?").matcher(q.prompt);if(!m.matches())return;
   Rational side=Expression.number(m.group(2)),distance=Expression.number(m.group(3)),count=Rational.of(m.group(1).equals("정오각형")?5:6),triangle=side.mul(distance).div(Rational.of(2));
   g.step("한 변의 길이를 찾아 쓰세요.","한 변 = "," cm",side.toString())
    .step("중심에서 변까지의 수직거리를 찾아 쓰세요.","중심에서 변까지 수직거리 = "," cm",distance.toString())
    .step("중심과 꼭짓점을 이으면 생기는 삼각형의 개수를 쓰세요.","삼각형 개수 = ","",count.toString())
    .step("한 변을 밑변으로 하는 삼각형 한 개의 넓이를 구하세요.","한 변 × 중심에서 변까지 수직거리 ÷ 2 = "," cm²",triangle.toString())
    .step("삼각형 한 개의 넓이에 삼각형 개수를 곱하세요.","삼각형 한 개 넓이 × 삼각형 개수 = "," cm²",triangle.mul(count).toString());
  }else{
   Matcher m=Pattern.compile("각뿔 · 밑면 (삼각형 · 밑변 "+n+"cm · 높이 "+n+"cm|직사각형 · 가로 "+n+"cm · 세로 "+n+"cm|정사각형 · 한 변 "+n+"cm)\\n수직 높이 "+n+"cm\\n부피는 몇 cm³인가요\\?").matcher(q.prompt);if(!m.matches())return;
   Rational height=Expression.number(m.group(7)),base;String frame;
   if(m.group(2)!=null){Rational b=Expression.number(m.group(2)),h=Expression.number(m.group(3));base=b.mul(h).div(Rational.of(2));
    g.step("밑면 삼각형의 밑변을 찾아 쓰세요.","밑면 밑변 = "," cm",b.toString()).step("밑면 삼각형의 높이를 찾아 쓰세요.","밑면 삼각형 높이 = "," cm",h.toString());frame="밑면 밑변 × 밑면 삼각형 높이 ÷ 2 = ";
   }else if(m.group(4)!=null){Rational w=Expression.number(m.group(4)),d=Expression.number(m.group(5));base=w.mul(d);
    g.step("밑면의 가로 길이를 찾아 쓰세요.","밑면 가로 = "," cm",w.toString()).step("밑면의 세로 길이를 찾아 쓰세요.","밑면 세로 = "," cm",d.toString());frame="밑면 가로 × 밑면 세로 = ";
   }else{Rational side=Expression.number(m.group(6));base=side.pow(2);g.step("밑면 정사각형의 한 변을 찾아 쓰세요.","밑면 한 변 = "," cm",side.toString());frame="밑면 한 변² = ";}
   Rational prism=base.mul(height);
   g.step("각뿔의 수직 높이를 찾아 쓰세요.","수직 높이 = "," cm",height.toString())
    .step("밑면의 넓이를 구하세요.",frame," cm²",base.toString())
    .step("밑면 넓이에 수직 높이를 곱하세요.","밑면 넓이 × 수직 높이 = "," cm³",prism.toString())
    .step("같은 밑면과 수직 높이인 기둥의 부피를 3으로 나누세요.","기둥 부피 ÷ 3 = "," cm³",prism.div(Rational.of(3)).toString());
  }
  q.studyGuide=g;
 }
}
