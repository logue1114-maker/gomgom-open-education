package com.gomgomapps.math.core;
import java.util.regex.*;
/** Reduction frames derived from the visible fraction, without numeric prefill. */
public final class FractionReductionRelations {
 private FractionReductionRelations(){}
 public static boolean supports(String id){return "reduce".equals(id);}
 public static void attach(Question q){
  if(q==null||!supports(q.skillId)||q.prompt==null)return;
  Matcher m=Pattern.compile("(\\d+)/(\\d+)을 기약분수로 나타내세요\\.").matcher(q.prompt);if(!m.matches())return;
  long n=Long.parseLong(m.group(1)),d=Long.parseLong(m.group(2));if(d==0)return;
  long a=n,b=d;while(b!=0){long r=a%b;a=b;b=r;}long g=a;
  StudyGuide guide=new StudyGuide().transfer(false);guide.teachingVersion="fraction-reduction-relations-v1";
  guide.step("문제의 분자를 쓰세요.","분자 N = ","",String.valueOf(n));
  guide.step("문제의 분모를 쓰세요.","분모 D = ","",String.valueOf(d));
  guide.step("분자와 분모를 모두 나눌 수 있는 가장 큰 수를 구하세요.","최대공약수 g = ","",String.valueOf(g));
  guide.step("분자를 최대공약수로 나누세요.","약분한 분자 = N ÷ g = ","",String.valueOf(n/g));
  guide.step("분모를 같은 최대공약수로 나누세요.","약분한 분모 = D ÷ g = ","",String.valueOf(d/g));
  q.studyGuide=guide.fractionResult(3,4);
 }
}
