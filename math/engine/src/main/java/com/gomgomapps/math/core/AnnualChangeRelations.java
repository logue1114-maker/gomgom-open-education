package com.gomgomapps.math.core;
import java.util.regex.*;
/** Annual balances stay learner-computed; named year relationships use public givens only. */
public final class AnnualChangeRelations {
 private AnnualChangeRelations(){}
 public static boolean supports(String id){return "annualCompound".equals(id)||"annualValueChange".equals(id);}
 public static void attach(Question q){
  if(q==null||!supports(q.skillId)||q.prompt==null)return;
  String n="(\\d+(?:\\.\\d+)?(?:/\\d+)?)";boolean compound=q.skillId.equals("annualCompound");
  Matcher m=Pattern.compile(compound?"원금: "+n+"\\n연이율: "+n+"% · 기간: ([1-3])년\\n매년 이자를 잔액에 더합니다\\.\\n마지막 총금액은\\?":"처음 금액: "+n+"\\n매년 "+n+"% (하락|상승) · 기간: ([1-3])년\\n마지막 금액은\\?").matcher(q.prompt);if(!m.matches())return;
  Rational balance=Expression.number(m.group(1)),rate=Expression.number(m.group(2)),fraction=rate.div(Rational.of(100));boolean fall=!compound&&m.group(3).equals("하락");int years=Integer.parseInt(m.group(compound?3:4));
  StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="annual-change-relations-v1";
  g.step("처음 금액을 찾아 쓰세요.","처음 금액 = ","",balance.toString())
   .step(compound?"연이율을 찾아 쓰세요.":"변화율을 찾아 쓰세요.",compound?"연이율 = ":"변화율 = "," %",rate.toString())
   .step("기간을 찾아 쓰세요.","기간 = "," 년",String.valueOf(years))
   .step(compound?"연이율을 100으로 나누세요.":"변화율을 100으로 나누세요.",compound?"연이율 ÷ 100 = ":"변화율 ÷ 100 = ","",fraction.toString());
  for(int year=1;year<=years;year++){
   String start=year==1?"처음 금액":(year-1)+"년 뒤 금액",change=year+"년째 "+(compound?"이자":fall?"감소 금액":"증가 금액");Rational difference=balance.mul(fraction),next=fall?balance.sub(difference):balance.add(difference);
   g.step(change+(compound?"를":"을")+" 구하세요.",start+" × "+(compound?"연이율":"변화율")+"의 비율 = ","",difference.toString())
    .step(year+"년 뒤 금액을 구하세요.",start+(fall?" − ":" + ")+change+" = ","",next.toString());balance=next;
  }q.studyGuide=g;
 }
}
