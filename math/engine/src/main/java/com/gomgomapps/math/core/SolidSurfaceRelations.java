package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
/** Surface relationships computed exclusively from public length givens. */
public final class SolidSurfaceRelations {
 private SolidSurfaceRelations(){}
 public static boolean supports(String id){return Set.of("sec_prism_surface","sec_cylinder_surface","sec_cone_surface","sec_sphere_surface").contains(id);}
 public static void attach(Question q){
  if(q==null||!supports(q.skillId)||q.prompt==null)return;
  StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="solid-surface-relations-v1";
  if(q.skillId.equals("sec_prism_surface")){Matcher m=Pattern.compile("가로 (\\d+(?:\\.\\d+)?(?:/\\d+)?), 세로 (\\d+(?:\\.\\d+)?(?:/\\d+)?), 높이 (\\d+(?:\\.\\d+)?(?:/\\d+)?)인 직육면체의 겉넓이는\\?").matcher(q.prompt);if(!m.matches())return;Rational w=Expression.number(m.group(1)),d=Expression.number(m.group(2)),h=Expression.number(m.group(3));
   g
   .step("가로 길이를 찾아 쓰세요.","가로 = ","",w.toString())
   .step("세로 길이를 찾아 쓰세요.","세로 = ","",d.toString())
   .step("높이를 찾아 쓰세요.","높이 = ","",h.toString())
   .step("가로와 세로로 이루어진 면의 넓이를 구하세요.","가로 × 세로 = ","",w.mul(d).toString())
   .step("가로와 높이로 이루어진 면의 넓이를 구하세요.","가로 × 높이 = ","",w.mul(h).toString())
   .step("세로와 높이로 이루어진 면의 넓이를 구하세요.","세로 × 높이 = ","",d.mul(h).toString())
   .step("세 면의 넓이를 더하고 두 배 하세요.","2 × (가로세로 면 + 가로높이 면 + 세로높이 면) = ","",w.mul(d).add(w.mul(h)).add(d.mul(h)).mul(Rational.of(2)).toString());}
  else if(q.skillId.equals("sec_cylinder_surface")){Matcher m=Pattern.compile("반지름이 (\\d+(?:\\.\\d+)?(?:/\\d+)?), 높이가 (\\d+(?:\\.\\d+)?(?:/\\d+)?)인 원기둥의 겉넓이는 \\(□\\)π입니다\\. □는\\?").matcher(q.prompt);if(!m.matches())return;Rational radius=Expression.number(m.group(1)),h=Expression.number(m.group(2));
   g
   .step("문제에서 반지름을 찾아 쓰세요.","반지름 = ","",radius.toString())
   .step("높이를 찾아 쓰세요.","높이 = ","",h.toString())
   .step("두 밑면의 넓이를 (□)π로 나타낼 때 □를 구하세요.","2 × 반지름² = ","",radius.pow(2).mul(Rational.of(2)).toString())
   .step("옆면의 넓이를 (□)π로 나타낼 때 □를 구하세요.","2 × 반지름 × 높이 = ","",radius.mul(h).mul(Rational.of(2)).toString())
   .step("두 밑면과 옆면의 π계수를 더하세요.","두 밑면 π계수 + 옆면 π계수 = ","",radius.pow(2).add(radius.mul(h)).mul(Rational.of(2)).toString());}
  else if(q.skillId.equals("sec_cone_surface")){Matcher m=Pattern.compile("밑면 반지름이 (\\d+(?:\\.\\d+)?(?:/\\d+)?), 모선이 (\\d+(?:\\.\\d+)?(?:/\\d+)?)인 원뿔의 겉넓이는 \\(□\\)π입니다\\. □는\\?").matcher(q.prompt);if(!m.matches())return;Rational radius=Expression.number(m.group(1)),slant=Expression.number(m.group(2));
   g
   .step("문제에서 반지름을 찾아 쓰세요.","반지름 = ","",radius.toString())
   .step("모선의 길이를 찾아 쓰세요.","모선 = ","",slant.toString())
   .step("밑면의 넓이를 (□)π로 나타낼 때 □를 구하세요.","반지름² = ","",radius.pow(2).toString())
   .step("옆면의 넓이를 (□)π로 나타낼 때 □를 구하세요.","반지름 × 모선 = ","",radius.mul(slant).toString())
   .step("밑면과 옆면의 π계수를 더하세요.","밑면 π계수 + 옆면 π계수 = ","",radius.pow(2).add(radius.mul(slant)).toString());}
  else if(q.skillId.equals("sec_sphere_surface")){Matcher m=Pattern.compile("반지름이 (\\d+(?:\\.\\d+)?(?:/\\d+)?)인 구의 겉넓이는 \\(□\\)π입니다\\. □는\\?").matcher(q.prompt);if(!m.matches())return;Rational radius=Expression.number(m.group(1));
   g
   .step("문제에서 반지름을 찾아 쓰세요.","반지름 = ","",radius.toString())
   .step("반지름을 제곱하세요.","반지름² = ","",radius.pow(2).toString())
   .step("반지름의 제곱에 4를 곱해 π계수를 구하세요.","4 × 반지름 제곱 = ","",radius.pow(2).mul(Rational.of(4)).toString());}
  q.studyGuide=g;
 }
}
