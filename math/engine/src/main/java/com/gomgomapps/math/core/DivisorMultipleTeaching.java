package com.gomgomapps.math.core;
import java.util.regex.*;
/** Remainder calculations are entered by the student; the answer key is never consulted. */
final class DivisorMultipleTeaching {
 private DivisorMultipleTeaching(){}
 static void attach(Question q){
  if(!q.skillId.equals("gcd")&&!q.skillId.equals("lcm"))return;
  Matcher m=Pattern.compile("(\\d+)와\\s*(\\d+)의\\s*(최대공약수|최소공배수)는\\?").matcher(q.prompt);if(!m.matches())return;
  int first=Integer.parseInt(m.group(1)),second=Integer.parseInt(m.group(2));if(first<=0||second<=0)return;
  int a=Math.max(first,second),b=Math.min(first,second);StudyGuide guide=new StudyGuide().transfer(false);
  while(b!=0){
   int quotient=a/b,product=b*quotient,remainder=a-product;
   guide.step("큰 수를 작은 수로 나누어 몫을 구하세요.",a+" ÷ "+b+" = ","몫",Integer.toString(quotient));
   guide.step("나누는 수에 몫을 곱하세요.",b+" × "+quotient+" = ","",Integer.toString(product));
   guide.step("큰 수에서 계산한 곱을 빼서 나머지를 구하세요.",a+" − "+product+" = ","나머지",Integer.toString(remainder));
   a=b;b=remainder;
  }
  guide.step("나머지가 0일 때 마지막으로 나눈 수가 최대공약수입니다. 그 수를 쓰세요.","gcd("+first+", "+second+") = ","",Integer.toString(a));
  if(q.skillId.equals("lcm")){
   guide.step("첫 번째 수를 최대공약수로 나누세요.",first+" ÷ "+a+" = ","",Integer.toString(first/a));
   guide.step("계산한 몫에 두 번째 수를 곱하면 최소공배수가 됩니다.",(first/a)+" × "+second+" = ","",Long.toString((long)(first/a)*second));
  }
  q.studyGuide=guide;
 }
}
