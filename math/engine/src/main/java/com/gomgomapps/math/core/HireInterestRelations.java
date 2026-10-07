package com.gomgomapps.math.core;
import java.util.regex.*;
/** Public payment/interest givens followed by learner-calculated named relationships. */
public final class HireInterestRelations {
 private HireInterestRelations(){}
 public static boolean supports(String id){return "hirePurchase".equals(id)||"simpleInterest".equals(id);}
 public static void attach(Question q){
  if(q==null||!supports(q.skillId)||q.prompt==null)return;
  String n="(\\d+(?:\\.\\d+)?(?:/\\d+)?)";StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="hire-interest-relations-v1";
  if(q.skillId.equals("hirePurchase")){
   Matcher m=Pattern.compile("계약금: "+n+"\\n할부금: "+n+" × (\\d+)회 · 추가 비용 없음\\n(?:현금 가격: "+n+"\\n할부로 더 내는 금액은\\?|할부 총금액은\\?)").matcher(q.prompt);if(!m.matches())return;
   Rational deposit=Expression.number(m.group(1)),payment=Expression.number(m.group(2)),count=Expression.number(m.group(3)),all=payment.mul(count),total=deposit.add(all);boolean difference=m.group(4)!=null;
   g.step("계약금을 찾아 쓰세요.","계약금 = ","",deposit.toString())
    .step("한 번의 할부금을 찾아 쓰세요.","한 번의 할부금 = ","",payment.toString())
    .step("납부 횟수를 찾아 쓰세요.","납부 횟수 = "," 회",count.toString());
   Rational cash=difference?Expression.number(m.group(4)):Rational.ZERO;
   if(difference)g.step("현금 가격을 찾아 쓰세요.","현금 가격 = ","",cash.toString());
   g.step("모든 할부금의 합을 구하세요.","한 번의 할부금 × 납부 횟수 = ","",all.toString())
    .step("할부 총금액을 구하세요.","계약금 + 모든 할부금 = ","",total.toString());
   if(difference)g.step("할부로 더 내는 금액을 구하세요.","할부 총금액 − 현금 가격 = ","",total.sub(cash).toString());
  }else{
   Matcher m=Pattern.compile("원금: "+n+"\\n연이율: "+n+"% · 기간: (\\d+)년\\n단리로 계산한 이자는\\?").matcher(q.prompt);if(!m.matches())return;
   Rational principal=Expression.number(m.group(1)),rate=Expression.number(m.group(2)),years=Expression.number(m.group(3)),fraction=rate.div(Rational.of(100)),annual=principal.mul(fraction);
   g.step("원금을 찾아 쓰세요.","원금 = ","",principal.toString())
    .step("연이율을 찾아 쓰세요.","연이율 = "," %",rate.toString())
    .step("기간을 찾아 쓰세요.","기간 = "," 년",years.toString())
    .step("연이율을 100으로 나누세요.","연이율 ÷ 100 = ","",fraction.toString())
    .step("한 해의 이자를 구하세요.","원금 × 연이율의 비율 = ","",annual.toString())
    .step("기간 동안의 이자를 구하세요.","한 해의 이자 × 기간 = ","",annual.mul(years).toString());
  }q.studyGuide=g;
 }
}
