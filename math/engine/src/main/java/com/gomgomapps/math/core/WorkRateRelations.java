package com.gomgomapps.math.core;
import java.util.regex.*;
/** Public work givens and learner-calculated ratios, independent of answer keys. */
public final class WorkRateRelations {
 private WorkRateRelations(){}
 public static boolean supports(String id){return "compoundProportion".equals(id)||"combinedWorkTime".equals(id);}
 public static void attach(Question q){
  if(q==null||!supports(q.skillId)||q.prompt==null)return;
  String n="(\\d+(?:\\.\\d+)?(?:/\\d+)?)";StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="work-rate-relations-v1";
  if(q.skillId.equals("compoundProportion")){
   Matcher m=Pattern.compile("각 사람의 작업 속도는 일정하고 모두 같습니다\\.\\n"+n+"명이 "+n+"시간에 "+n+"개를 만듭니다\\.\\n"+n+"명이 "+n+"(개를 만드는 데 몇 시간이 걸리나요\\?|시간에 만드는 개수는\\?)").matcher(q.prompt);if(!m.matches())return;
   Rational workers=Expression.number(m.group(1)),hours=Expression.number(m.group(2)),items=Expression.number(m.group(3)),newWorkers=Expression.number(m.group(4)),other=Expression.number(m.group(5));if(workers.isZero()||hours.isZero()||items.isZero()||newWorkers.isZero())return;
   boolean time=m.group(6).startsWith("개");Rational peopleRatio=time?workers.div(newWorkers):newWorkers.div(workers),otherRatio=time?other.div(items):other.div(hours),result=(time?hours:items).mul(peopleRatio).mul(otherRatio);
   g.step("처음 인원을 찾아 쓰세요.","처음 인원 = "," 명",workers.toString())
    .step("처음 작업 시간을 찾아 쓰세요.","처음 시간 = "," 시간",hours.toString())
    .step("처음 만든 개수를 찾아 쓰세요.","처음 개수 = "," 개",items.toString())
    .step("새 인원을 찾아 쓰세요.","새 인원 = "," 명",newWorkers.toString())
    .step(time?"새로 만들 개수를 찾아 쓰세요.":"새 작업 시간을 찾아 쓰세요.",time?"새 개수 = ":"새 시간 = ",time?" 개":" 시간",other.toString())
    .step(time?"시간 계산에 쓸 인원의 역비를 구하세요.":"인원의 비를 구하세요.",time?"처음 인원 ÷ 새 인원 = ":"새 인원 ÷ 처음 인원 = ","",peopleRatio.toString())
    .step(time?"만들 개수의 비를 구하세요.":"작업 시간의 비를 구하세요.",time?"새 개수 ÷ 처음 개수 = ":"새 시간 ÷ 처음 시간 = ","",otherRatio.toString())
    .step(time?"새 작업 시간을 구하세요.":"새로 만들 개수를 구하세요.",time?"처음 시간 × 인원의 역비 × 개수의 비 = ":"처음 개수 × 인원의 비 × 시간의 비 = ",time?" 시간":" 개",result.toString());
  }else{
   Matcher m=Pattern.compile("같은 일을 A는 혼자 "+n+"시간, B는 혼자 "+n+"시간에 끝냅니다\\.\\n각자의 작업 속도는 일정합니다\\.\\n둘이 동시에 시작하면 몇 시간이 걸리나요\\?").matcher(q.prompt);if(!m.matches())return;
   Rational a=Expression.number(m.group(1)),b=Expression.number(m.group(2));if(a.isZero()||b.isZero())return;Rational first=Rational.ONE.div(a),second=Rational.ONE.div(b),sum=first.add(second);
   g.step("A가 혼자 걸리는 시간을 찾아 쓰세요.","A의 시간 = "," 시간",a.toString())
    .step("B가 혼자 걸리는 시간을 찾아 쓰세요.","B의 시간 = "," 시간",b.toString())
    .step("A가 한 시간에 하는 일의 비율을 구하세요.","1 ÷ A의 시간 = ","",first.toString())
    .step("B가 한 시간에 하는 일의 비율을 구하세요.","1 ÷ B의 시간 = ","",second.toString())
    .step("둘이 한 시간에 하는 일의 비율을 구하세요.","A의 작업률 + B의 작업률 = ","",sum.toString())
    .step("둘이 함께 걸리는 시간을 구하세요.","1 ÷ 합친 작업률 = "," 시간",Rational.ONE.div(sum).toString());
  }q.studyGuide=g;
 }
}
