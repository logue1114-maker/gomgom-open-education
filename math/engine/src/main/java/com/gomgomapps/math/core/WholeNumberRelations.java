package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
/** Blank counting and make-ten frames read only the original public operands. */
public final class WholeNumberRelations {
 private WholeNumberRelations(){}
 public static boolean supports(String id){return Set.of("add9","sub9","add20","sub20","addThree9","addThree100","subThree9","subThree100").contains(id);}
 public static void attach(Question q){
  if(q==null||!supports(q.skillId))return;
  boolean three=q.skillId.contains("Three"),add=q.skillId.startsWith("add");
  Matcher m=Pattern.compile(three?"^\\s*(\\d{1,6})\\s*([+−-])\\s*(\\d{1,6})\\s*([+−-])\\s*(\\d{1,6})\\s*$":"^\\s*(\\d{1,2})\\s*([+−-])\\s*(\\d{1,2})\\s*$").matcher(q.prompt);
  if(!m.matches()||m.group(2).equals("+")!=add||three&&m.group(4).equals("+")!=add)return;
  int a=Integer.parseInt(m.group(1)),b=Integer.parseInt(m.group(3)),c=three?Integer.parseInt(m.group(5)):0;if(!add&&(a<b||a-b<c))return;
  StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="whole-number-relations-v1";
  g.step("첫 수를 쓰세요.","첫 수 = ","",String.valueOf(a));g.step("둘째 수를 쓰세요.","둘째 수 = ","",String.valueOf(b));
  if(three){
   g.step("셋째 수를 쓰세요.","셋째 수 = ","",String.valueOf(c));
   g.step(add?"앞의 두 수를 더하세요.":"첫 수에서 둘째 수를 빼세요.",add?"첫 수 + 둘째 수 = ":"첫 수 − 둘째 수 = ","",String.valueOf(add?a+b:a-b));
   g.step(add?"앞에서 구한 수에 셋째 수를 더하세요.":"앞에서 구한 수에서 셋째 수를 빼세요.",add?"앞에서 구한 수 + 셋째 수 = ":"앞에서 구한 수 − 셋째 수 = ","",String.valueOf(add?a+b+c:a-b-c));
  }else if(add&&a>0&&a<10&&b<10&&a+b>=10){
   int moved=10-a;
   g.step("첫 수에 얼마를 더하면 10이 되나요?","10 − 첫 수 = ","",String.valueOf(moved)).quantity(a,b,10);
   g.step("둘째 수에서 옮긴 수를 빼세요.","둘째 수 − 옮긴 수 = ","",String.valueOf(b-moved));
   g.step("10과 남은 수를 더하세요.","10 + 남은 수 = ","",String.valueOf(a+b));
  }else if(!add&&a>=10&&a<=18&&b>a%10&&b<=9){
   g.step("첫 수에서 10을 빼고 남은 수를 쓰세요.","첫 수 − 10 = ","",String.valueOf(a-10));
   g.step("10에서 둘째 수를 빼세요.","10 − 둘째 수 = ","",String.valueOf(10-b)).quantity(10,-b,10);
   g.step("두 곳에 남은 수를 더하세요.","10에서 남은 수 + 처음 남겨 둔 수 = ","",String.valueOf(a-b));
  }else{
   g.step(add?"두 묶음을 모아 세세요.":"지운 동그라미를 빼고 세세요.",add?"첫 수 + 둘째 수 = ":"첫 수 − 둘째 수 = ","",String.valueOf(add?a+b:a-b));
   if(a<=9&&b<=9)g.quantity(a,add?b:-b,10);
  }
  q.studyGuide=g;
 }
}
