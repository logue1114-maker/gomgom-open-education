package com.gomgomapps.math.core;
import java.util.regex.*;
/** Relative frequency from the two published counts, never reduced answer metadata. */
public final class RelativeFrequencyRelations {
 private RelativeFrequencyRelations(){}
 public static boolean supports(String id){return "sec_relative_frequency".equals(id);}
 public static StudyDiagram diagram(Question q){
  if(q==null||!supports(q.skillId)||q.prompt==null)return null;
  Matcher m=Pattern.compile("전체 도수가 (\\d+)이고 한 계급의 도수가 (\\d+)일 때 그 계급의 상대도수는\\?").matcher(q.prompt);if(!m.matches())return null;
  double total=Double.parseDouble(m.group(1)),frequency=Double.parseDouble(m.group(2));if(total<=0||frequency>total)return null;
  return new StudyDiagram("bars",new double[]{frequency,total-frequency},"이 계급의 자료","다른 계급의 자료");
 }
 public static void attach(Question q){
  if(q==null||!supports(q.skillId)||q.prompt==null)return;
  Matcher m=Pattern.compile("전체 도수가 (\\d+)이고 한 계급의 도수가 (\\d+)일 때 그 계급의 상대도수는\\?").matcher(q.prompt);if(!m.matches())return;
  Rational total=Expression.number(m.group(1)),frequency=Expression.number(m.group(2));if(total.compareTo(Rational.ZERO)<=0||frequency.compareTo(total)>0)return;
  StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="relative-frequency-relations-v1";
  g.step("문제에서 이 계급에 속한 자료의 수를 찾아 쓰세요.","계급 도수 = ","",frequency.toString())
   .step("문제에서 모든 자료의 수를 찾아 쓰세요.","전체 도수 = ","",total.toString())
   .step("계급 도수를 전체 도수로 나누세요.","계급 도수 ÷ 전체 도수 = ","",frequency.div(total).toString());q.studyGuide=g;
 }
}
