package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
/** Long division checks the quotient digit, product and remainder without revealing them. */
public final class WholeDivisionRelations {
 private WholeDivisionRelations(){}
 public static boolean supports(String id){return Set.of("divide2","remainder","el_div_2x2_rem","el_div_3x2_rem").contains(id);}
 public static boolean remainder(String id){return supports(id)&&!id.equals("divide2");}
 public static void attach(Question q){
  if(q==null||!supports(q.skillId))return;Matcher m=Pattern.compile("^(\\d{1,6})\\s*÷\\s*(\\d{1,6})(?:의 몫과 나머지는\\?)?$").matcher(q.prompt);if(!m.matches())return;
  int a=Integer.parseInt(m.group(1)),b=Integer.parseInt(m.group(2));if(b==0||!remainder(q.skillId)&&a%b!=0)return;
  StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="whole-division-relations-v1";
  g.step("나누어지는 수를 쓰세요.","나누어지는 수 = ","",String.valueOf(a)).step("나누는 수를 쓰세요.","나누는 수 = ","",String.valueOf(b));
  String digits=String.valueOf(a);int end=1,current=Integer.parseInt(digits.substring(0,end));while(current<b&&end<digits.length())current=Integer.parseInt(digits.substring(0,++end));
  g.step("왼쪽에서 처음 나눌 수를 쓰세요.","처음 나눌 수 = ","",String.valueOf(current));
  for(;;){
   int digit=current/b,product=b*digit,left=current-product;
   g.step("이번 수에 나누는 수가 몇 번 들어가나요?","이번 수 ÷ 나누는 수의 몫 = ","",String.valueOf(digit));
   g.step("나누는 수에 몫의 숫자를 곱하세요.","나누는 수 × 몫의 숫자 = ","",String.valueOf(product));
   g.step("이번 수에서 곱한 수를 빼세요.","이번 수 − 곱한 수 = ","",String.valueOf(left));
   if(end==digits.length())break;int next=digits.charAt(end++)-'0';
   g.step("다음 자리 숫자를 내리세요.","내린 숫자 = ","",String.valueOf(next));
   current=left*10+next;g.step("남은 수에 다음 자리 숫자를 붙이세요.","남은 수 × 10 + 내린 숫자 = ","",String.valueOf(current));
  }
  g.step("확인한 몫의 숫자를 순서대로 쓰세요.","몫 = ","",String.valueOf(a/b));
  if(remainder(q.skillId))g.step("마지막에 남은 수를 쓰세요.","나머지 = ","",String.valueOf(a%b));q.studyGuide=g;
 }
}
