package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
/** Chord and tangent lengths from public geometric conditions and public lengths. */
public final class CircleLengthRelations {
 private CircleLengthRelations(){}
 public static boolean supports(String id){return Set.of("sec_circle_chord","sec_circle_tangent").contains(id);}
 public static void attach(Question q){
  if(q==null||!supports(q.skillId)||q.prompt==null)return;String num="(\\d+(?:\\.\\d+)?(?:/\\d+)?)";
  StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="circle-length-relations-v1";
  if(q.skillId.equals("sec_circle_chord")){
   Matcher m=Pattern.compile("원의 중심 O에서 현 AB에 내린 수선의 발을 M이라 할 때 AM="+num+"입니다. 현 AB의 길이는\\?").matcher(q.prompt);if(!m.matches())return;Rational half=Expression.number(m.group(1));if(half.compareTo(Rational.ZERO)<=0)return;
   g.step("문제에서 AM의 길이를 찾아 쓰세요.","AM = ","",half.toString())
    .step("원의 중심에서 현에 내린 수선은 현을 이등분합니다. AM과 MB가 같으므로 AM을 두 배 하세요.","AM × 2 = ","",half.mul(Rational.of(2)).toString());
  }else{
   Matcher m=Pattern.compile("원의 반지름이 "+num+", 중심에서 원 밖의 점까지 거리가 "+num+"일 때 그 점에서 그은 접선의 길이는\\?").matcher(q.prompt);if(!m.matches())return;Rational r=Expression.number(m.group(1)),d=Expression.number(m.group(2));if(r.compareTo(Rational.ZERO)<=0||d.compareTo(r)<=0)return;
   Rational ds=d.mul(d),rs=r.mul(r),difference=ds.sub(rs),length;try{length=difference.sqrt();}catch(IllegalArgumentException unsupported){return;}
   g.step("문제에서 원의 반지름을 찾아 쓰세요.","반지름 = ","",r.toString())
    .step("문제에서 중심과 원 밖의 점 사이의 거리를 찾아 쓰세요.","중심과 점 사이의 거리 = ","",d.toString())
    .step("중심과 점 사이의 거리를 제곱하세요.","중심과 점 사이의 거리² = ","",ds.toString())
    .step("반지름을 제곱하세요.","반지름² = ","",rs.toString())
    .step("접점에서 반지름과 접선은 수직입니다. 피타고라스 정리에 따라 두 제곱값의 차를 구하세요.","중심과 점 사이의 거리² − 반지름² = ","",difference.toString())
    .step("접선의 길이는 양수입니다. 접선 길이의 제곱값의 양의 제곱근을 구하세요.","√(접선 길이의 제곱값) = ","",length.toString());
  }q.studyGuide=g;
 }
}
