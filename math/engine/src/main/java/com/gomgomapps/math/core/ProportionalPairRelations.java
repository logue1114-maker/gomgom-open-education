package com.gomgomapps.math.core;
import java.util.regex.*;
/** Learner operands and named relationships derived only from the public pair. */
public final class ProportionalPairRelations {
 private ProportionalPairRelations(){}
 public static boolean supports(String id){return "sec_direct_proportion".equals(id)||"sec_inverse_proportion".equals(id);}
 public static void attach(Question q){
  if(q==null||!supports(q.skillId)||q.prompt==null)return;
  String number="([+-]?\\d+(?:\\.\\d+)?(?:/\\d+)?)";
  Matcher m=Pattern.compile("y는 x에 (정비례|반비례)하고 x="+number+"일 때 y="+number+"입니다\\. x="+number+"일 때 y는\\?").matcher(q.prompt.replace('−','-'));if(!m.matches())return;
  boolean direct=q.skillId.equals("sec_direct_proportion");if(direct!=m.group(1).equals("정비례"))return;
  Rational x=Expression.number(m.group(2)),y=Expression.number(m.group(3)),next=Expression.number(m.group(4));if(x.equals(Rational.ZERO)||!direct&&next.equals(Rational.ZERO))return;
  Rational constant=direct?y.div(x):x.mul(y);
  StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="proportional-pair-relations-v1";
  g.step("문제에서 처음 x의 값을 찾아 쓰세요.","처음 x = ","",x.toString())
   .step("문제에서 처음 y의 값을 찾아 쓰세요.","처음 y = ","",y.toString())
   .step("문제에서 새 x의 값을 찾아 쓰세요.","새 x = ","",next.toString())
   .step(direct?"비례상수를 구하세요.":"일정한 곱을 구하세요.",direct?"처음 y ÷ 처음 x = ":"처음 x × 처음 y = ","",constant.toString())
   .step("새 y의 값을 구하세요.",direct?"비례상수 × 새 x = ":"일정한 곱 ÷ 새 x = ","",(direct?constant.mul(next):constant.div(next)).toString());q.studyGuide=g;
 }
}
