package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
/** Scaffolds factor and multiple searches from public givens, without reverse-answer hints. */
final class FactorMultipleTeaching {
 private FactorMultipleTeaching(){}
 static void attach(Question q){
  StudyGuide guide=new StudyGuide().transfer(false);Matcher m;
  switch(q.skillId){
   case "el_divisor":
    m=Pattern.compile("(\\d+)의 약수 중 (\\d+)번째로 작은 수는\\?").matcher(q.prompt);if(!m.matches())return;
    int number=Integer.parseInt(m.group(1)),order=Integer.parseInt(m.group(2));List<Integer> factors=new ArrayList<>();
    for(int candidate=1;candidate<=number/candidate;candidate++){
     division(guide,number,candidate);
     if(number%candidate==0){factors.add(candidate);if(candidate!=number/candidate)factors.add(number/candidate);}
    }
    Collections.sort(factors);if(order<1||order>factors.size())return;
    guide.step("나머지가 0인 식의 나누는 수와 몫이 약수입니다. 약수를 작은 수부터 정리해 해당 순서의 수를 쓰세요.",order+"번째 약수 = ","",Integer.toString(factors.get(order-1)));break;
   case "el_multiple":
    m=Pattern.compile("(\\d+)의 (\\d+)번째 배수는\\?").matcher(q.prompt);if(!m.matches())return;
    int base=Integer.parseInt(m.group(1)),count=Integer.parseInt(m.group(2));
    for(int i=1;i<=count;i++)guide.step("1부터 차례로 곱해 배수를 구하세요.",base+" × "+i+" = ","",Integer.toString(base*i));break;
   case "el_common_divisor":
    m=Pattern.compile("(\\d+)과 (\\d+)의 공약수 중 두 번째로 작은 수는\\?").matcher(q.prompt);if(!m.matches())return;
    int left=Integer.parseInt(m.group(1)),right=Integer.parseInt(m.group(2)),found=0;
    for(int candidate=1;candidate<=Math.min(left,right);candidate++){
     division(guide,left,candidate);division(guide,right,candidate);
     if(left%candidate==0&&right%candidate==0&&++found==2){guide.step("두 식의 나머지가 모두 0이면 공약수입니다. 두 번째로 작은 공약수를 쓰세요.","두 번째 공약수 = ","",Integer.toString(candidate));break;}
    }if(found!=2)return;break;
   case "el_common_multiple":
    m=Pattern.compile("(\\d+)과 (\\d+)의 (\\d+)번째 공배수는\\?").matcher(q.prompt);if(!m.matches())return;
    int a=Integer.parseInt(m.group(1)),b=Integer.parseInt(m.group(2)),nth=Integer.parseInt(m.group(3));
    int larger=Math.max(a,b),smaller=Math.min(a,b),first=0;
    for(int multiplier=1;multiplier<=smaller;multiplier++){
     int candidate=larger*multiplier;guide.step("큰 수의 배수를 차례로 구하세요.",larger+" × "+multiplier+" = ","",Integer.toString(candidate));division(guide,candidate,smaller);
     if(candidate%smaller==0){first=candidate;break;}
    }
    if(first==0)return;
    guide.step("작은 수로도 나누어떨어지는 첫 배수가 최소공배수입니다. 이 수에 공배수의 순서를 곱하세요.",first+" × "+nth+" = ","",Integer.toString(first*nth));break;
   default:return;
  }
  q.studyGuide=guide;
 }
 private static void division(StudyGuide guide,int number,int divisor){
  int quotient=number/divisor,product=divisor*quotient;
  guide.step("나눗셈의 몫을 구하세요.",number+" ÷ "+divisor+" = ","몫",Integer.toString(quotient));
  guide.step("나누는 수에 몫을 곱하세요.",divisor+" × "+quotient+" = ","",Integer.toString(product));
  guide.step("곱을 빼서 나머지를 구하세요. 나머지가 0이면 나누어떨어집니다.",number+" − "+product+" = ","나머지",Integer.toString(number-product));
 }
}
