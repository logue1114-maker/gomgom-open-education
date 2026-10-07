package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
/** Exact volume coefficients from public length givens, never answer metadata. */
public final class RoundSolidVolumeRelations {
 private RoundSolidVolumeRelations(){}
 public static boolean supports(String id){return Set.of("sec_cone_volume","sec_sphere_volume").contains(id);}
 public static void attach(Question q){
  if(q==null||!supports(q.skillId)||q.prompt==null)return;
  String number="(\\d+(?:\\.\\d+)?(?:/\\d+)?)";StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="round-solid-volume-relations-v1";
  if(q.skillId.equals("sec_cone_volume")){
   Matcher m=Pattern.compile("반지름이 "+number+", 높이가 "+number+"인 원뿔의 부피는 \\(□\\)π입니다\\. □는\\?").matcher(q.prompt);if(!m.matches())return;
   Rational radius=Expression.number(m.group(1)),h=Expression.number(m.group(2)),base=radius.pow(2),cylinder=base.mul(h);
   g.step("문제에서 반지름을 찾아 쓰세요.","반지름 = ","",radius.toString())
    .step("높이를 찾아 쓰세요.","높이 = ","",h.toString())
    .step("밑면의 넓이를 (□)π로 나타낼 때 □를 구하세요.","반지름² = ","",base.toString())
    .step("같은 밑면과 높이인 원기둥의 부피를 (□)π로 나타낼 때 □를 구하세요.","밑면 π계수 × 높이 = ","",cylinder.toString())
    .step("원뿔은 같은 밑면과 높이인 원기둥 부피의 3분의 1입니다.","원기둥 부피 π계수 ÷ 3 = ","",cylinder.div(Rational.of(3)).toString());
  }else{
   Matcher m=Pattern.compile("반지름이 "+number+"인 구의 부피는 \\(□\\)π입니다\\. □는\\?").matcher(q.prompt);if(!m.matches())return;
   Rational radius=Expression.number(m.group(1)),cube=radius.pow(3);
   g.step("문제에서 반지름을 찾아 쓰세요.","반지름 = ","",radius.toString())
    .step("반지름을 세제곱하세요.","반지름³ = ","",cube.toString())
    .step("반지름의 세제곱에 4를 곱하고 3으로 나눠 π계수를 구하세요.","4 × 반지름 세제곱 ÷ 3 = ","",cube.mul(Rational.of(4)).div(Rational.of(3)).toString());
  }
  q.studyGuide=g;
 }
 private static double value(Rational v){return v.n.doubleValue()/v.d.doubleValue();}
}
