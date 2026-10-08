package com.gomgomapps.math.core;
import java.util.*;
import java.util.regex.*;

/** Population spread from public data. No precomputed value is rendered in a frame. */
public final class StatisticsSpreadRelations {
 private StatisticsSpreadRelations(){}
 public static boolean supports(String id){return Set.of("sec_variance","sec_standard_deviation").contains(id);}
 static void attach(Question q){
  if(q==null||!supports(q.skillId))return;
  boolean root=q.skillId.equals("sec_standard_deviation");
  StudyGuide guide=new StudyGuide().transfer(false);guide.teachingVersion="spread-relations-v1";
  Matcher given=Pattern.compile("분산이 ([0-9]+(?:/[0-9]+)?)인 자료의 표준편차는\\?").matcher(q.prompt);
  if(root&&given.matches()){
   Rational variance=Expression.number(given.group(1));
   guide.step("문제에서 분산을 찾아 쓰세요.","분산 = ","",variance.toString())
    .step("입력한 분산의 제곱근을 구하세요.","√분산 = ","",variance.sqrt().toString());
   q.studyGuide=guide;return;
  }
  Matcher data=Pattern.compile("자료 \\[([0-9]+(?:,\\s*[0-9]+)+)\\] 전체의 (?:분산은|표준편차는)\\?").matcher(q.prompt);
  if(!data.matches())return;
  List<Rational> values=Arrays.stream(data.group(1).split(",\\s*")).map(Expression::number).toList();
  Rational sum=values.stream().reduce(Rational.ZERO,Rational::add),count=Rational.of(values.size()),mean=sum.div(count),squares=Rational.ZERO;
  guide.step("주어진 수를 모두 더하세요.","합계 = ","",sum.toString())
   .step("주어진 수가 몇 개인지 세세요.","자료의 개수 = ","",count.toString())
   .step("합계를 자료의 개수로 나누세요.","평균 = 합계 ÷ 자료의 개수 = ","",mean.toString());
  for(int i=0;i<values.size();i++){
   Rational delta=values.get(i).sub(mean),square=delta.mul(delta);squares=squares.add(square);
   guide.step("자료에서 평균을 빼세요.","자료의 "+(i+1)+"번째 수 − 평균 = ","",delta.toString())
    .step("방금 구한 편차를 제곱하세요.","편차 × 편차 = ","",square.toString());
  }
  Rational variance=squares.div(count);
  guide.step("입력한 편차 제곱을 모두 더하세요.","편차 제곱의 합 = ","",squares.toString())
   .step("편차 제곱의 합을 자료의 개수로 나누세요.","분산 = 편차 제곱의 합 ÷ 자료의 개수 = ","",variance.toString());
  if(root)guide.step("입력한 분산의 제곱근을 구하세요.","√분산 = ","",variance.sqrt().toString());
  q.studyGuide=guide;
 }
}
