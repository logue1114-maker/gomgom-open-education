package com.gomgomapps.math.core;
import java.util.regex.*;
/** Public horizontal distance and the equal legs of a 45-degree right triangle. */
public final class TrigHeightRelations {
 private TrigHeightRelations(){}
 public static boolean supports(String id){return "sec_trig_height".equals(id);}
 public static void attach(Question q){
  if(q==null||!supports(q.skillId)||q.prompt==null)return;
  Matcher m=Pattern.compile("어떤 지점에서 건물 꼭대기를 올려다본 각이 45°이고 건물 밑까지의 수평 거리가 (\\d+(?:\\.\\d+)?(?:/\\d+)?)m입니다. 눈높이를 0m로 보면 건물 높이는\\?").matcher(q.prompt);if(!m.matches())return;
  Rational distance=Expression.number(m.group(1));if(distance.compareTo(Rational.ZERO)<=0)return;
  StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="trig-height-relations-v1";
  g.step("문제에서 건물 밑까지의 수평 거리를 찾아 쓰세요.","수평 거리(m) = ","",distance.toString())
   .step("45° 직각삼각형에서는 높이와 밑변의 길이가 같습니다. 높이를 밑변으로 나눈 값을 구하세요.","tan 45° = 높이 ÷ 밑변 = ","",Rational.ONE.toString())
   .step("눈높이는 0m입니다. 수평 거리에 tan 45°를 곱해 건물 높이를 구하세요.","수평 거리 × tan 45° = ","",distance.toString());
  q.studyGuide=g;
 }
}
