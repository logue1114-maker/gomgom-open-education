package com.gomgomapps.math.core;
import java.util.Arrays;
import java.util.regex.*;
/** Entered calculation stages from visible data; answer metadata is never used. */
public final class StatisticsAngleTeaching {
 private StatisticsAngleTeaching(){}
 public static boolean relationships(String id){return id.equals("mean")||id.equals("angles");}
 static void attach(Question q){
  if(q==null||q.studyGuide!=null&&!relationships(q.skillId))return;
  if(q.skillId.equals("mean")||q.skillId.equals("median")){
   Matcher m=Pattern.compile("^([0-9]+(?:,\\s*[0-9]+)+)의 (?:평균|중앙값)은\\?$").matcher(q.prompt);
   if(!m.matches())return;
   int[] values=Arrays.stream(m.group(1).split(",\\s*")).mapToInt(Integer::parseInt).toArray();
   StudyGuide guide=new StudyGuide().transfer(false);
   if(q.skillId.equals("mean")){
    long sum=Arrays.stream(values).asLongStream().sum();
    guide.step("주어진 수를 모두 더하세요.","합계 = ","",Long.toString(sum))
     .step("주어진 수가 몇 개인지 세세요.","자료의 개수 = ","",Integer.toString(values.length))
     .step("합계를 자료의 개수로 나누세요.","계산한 합계 ÷ 자료의 개수 = ","",Rational.of(sum,values.length).toString());
   }else{
    if(values.length%2==0)return;
    Arrays.sort(values);
    for(int i=0;i<values.length;i++)guide.step("주어진 수를 작은 순서대로 쓰세요.","정렬한 수의 "+(i+1)+"번째 = ","",Integer.toString(values[i]));
    guide.step("정렬한 수에서 가운데 수를 쓰세요.","중앙값 = ","",Integer.toString(values[values.length/2]));
   }
   q.studyGuide=guide;return;
  }
  if(q.skillId.equals("angles")){
   Matcher m=Pattern.compile("180-(\\d+)-(\\d+)").matcher(q.expression);if(!m.matches())return;
   int a=Integer.parseInt(m.group(1)),b=Integer.parseInt(m.group(2));if(a<=0||b<=0||a+b>=180)return;
   StudyGuide guide=new StudyGuide().transfer(false);guide.teachingVersion="statistics-angle-relations-v1";
   guide.step("문제에서 첫 번째 각의 크기를 찾아 쓰세요.","첫 번째 각 = ","°",Integer.toString(a))
    .step("문제에서 두 번째 각의 크기를 찾아 쓰세요.","두 번째 각 = ","°",Integer.toString(b))
    .step("주어진 각의 크기를 모두 더하세요.","첫 번째 각 + 두 번째 각 = ","°",Integer.toString(a+b));
   q.studyGuide=guide.step("내각의 합에서 계산한 합을 빼세요.","180° − 계산한 합 = ","°",Integer.toString(180-a-b));
  }
 }
}
