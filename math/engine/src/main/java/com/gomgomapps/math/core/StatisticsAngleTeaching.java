package com.gomgomapps.math.core;
import java.util.Arrays;
import java.util.regex.*;
/** Entered calculation stages from visible data; answer metadata is never used. */
final class StatisticsAngleTeaching {
 private StatisticsAngleTeaching(){}
 static void attach(Question q){
  if(q.studyGuide!=null)return;
  if(q.skillId.equals("mean")||q.skillId.equals("median")){
   Matcher m=Pattern.compile("^([0-9]+(?:,\\s*[0-9]+)+)의 (?:평균|중앙값)은\\?$").matcher(q.prompt);
   if(!m.matches())return;
   int[] values=Arrays.stream(m.group(1).split(",\\s*")).mapToInt(Integer::parseInt).toArray();
   StudyGuide guide=new StudyGuide().transfer(false);
   if(q.skillId.equals("mean")){
    long sum=Arrays.stream(values).asLongStream().sum();
    guide.step("주어진 수를 모두 더하세요.",m.group(1)+" → 합계 = ","",Long.toString(sum))
     .step("주어진 수가 몇 개인지 세세요.","자료의 개수 = ","",Integer.toString(values.length))
     .step("합계를 자료의 개수로 나누세요.",sum+" ÷ "+values.length+" = ","",Rational.of(sum,values.length).toString());
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
   q.studyGuide=new StudyGuide().transfer(false)
    .step("주어진 두 각의 크기를 더하세요.",a+" + "+b+" = ","°",Integer.toString(a+b))
    .step("180도에서 두 각의 합을 빼세요.","180 − "+(a+b)+" = ","°",Integer.toString(180-a-b));
  }
 }
}
