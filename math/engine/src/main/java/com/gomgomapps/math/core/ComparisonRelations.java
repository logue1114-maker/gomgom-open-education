package com.gomgomapps.math.core;
import java.math.BigDecimal;import java.util.*;import java.util.regex.*;
/** Comparison is derived from visible operands; all calculations and the sign are entered. */
public final class ComparisonRelations {
 private ComparisonRelations(){}
 public static boolean supports(String id){return Set.of("fracCompare","el_fraction_compare","el_decimal_compare").contains(id);}
 static Checker.Result check(Question q,List<String> answers){
  if(q==null||q.prompt==null||!supports(q.skillId)||answers==null||answers.size()!=1)return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"비교 기호 선택 필요");
  String answer=answers.get(0)==null?"":answers.get(0).trim();if(!Set.of("<","=",">").contains(answer))return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"비교 기호 선택 필요");
  String value=q.skillId.equals("el_decimal_compare")?"\\d{1,6}(?:\\.\\d{1,6})?":"\\d{1,6}/\\d{1,6}";Matcher m=Pattern.compile("("+value+")\\s+□\\s+("+value+")").matcher(q.prompt);if(!m.matches())return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"문제 확인 필요");
  try{if(q.skillId.equals("fracCompare")&&Integer.parseInt(m.group(1).split("/")[1])!=Integer.parseInt(m.group(2).split("/")[1]))return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"문제 확인 필요");Rational left=Expression.number(m.group(1)),right=Expression.number(m.group(2));int cmp=left.compareTo(right);String expected=cmp<0?"<":cmp>0?">":"=";boolean same=answer.equals(expected);return new Checker.Result(same?Checker.Status.CORRECT:Checker.Status.WRONG_ANSWER,same?-1:0,same?"계산 확인 완료":"이 기호 확인");}catch(RuntimeException error){return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"문제 확인 필요");}
 }
 public static void attach(Question q){
  if(q==null||q.prompt==null||!supports(q.skillId))return;StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="comparison-relations-v1";int cmp;
  if(!q.skillId.equals("el_decimal_compare")){
   Matcher m=Pattern.compile("(\\d+)/(\\d+)\\s+□\\s+(\\d+)/(\\d+)").matcher(q.prompt);if(!m.matches())return;
   long a=Long.parseLong(m.group(1)),b=Long.parseLong(m.group(2)),c=Long.parseLong(m.group(3)),d=Long.parseLong(m.group(4));if(b<=0||d<=0)return;
   if(q.skillId.equals("fracCompare")){
    if(b!=d)return;step(g,"같은 분모를 쓰세요.","같은 분모 b = ",b);step(g,"왼쪽 분자를 쓰세요.","왼쪽 분자 a = ",a);step(g,"오른쪽 분자를 쓰세요.","오른쪽 분자 c = ",c);cmp=Long.compare(a,c);
    choose(g,"분모가 같으므로 두 분자를 비교해 기호를 고르세요.",cmp);
   }else{
    step(g,"왼쪽 분자를 쓰세요.","왼쪽 분자 a = ",a);step(g,"왼쪽 분모를 쓰세요.","왼쪽 분모 b = ",b);step(g,"오른쪽 분자를 쓰세요.","오른쪽 분자 c = ",c);step(g,"오른쪽 분모를 쓰세요.","오른쪽 분모 d = ",d);
    step(g,"두 분모의 곱을 공통 분모로 쓰세요.","공통 분모 D = b × d = ",b*d);step(g,"왼쪽 분자에 오른쪽 분모를 곱하세요.","통분한 왼쪽 분자 A = a × d = ",a*d);step(g,"오른쪽 분자에 왼쪽 분모를 곱하세요.","통분한 오른쪽 분자 C = c × b = ",c*b);cmp=Long.compare(a*d,c*b);
    choose(g,"통분한 두 분자를 비교해 기호를 고르세요.",cmp);
   }
  }else{
   Matcher m=Pattern.compile("(\\d+(?:\\.\\d+)?)\\s+□\\s+(\\d+(?:\\.\\d+)?)").matcher(q.prompt);if(!m.matches())return;
   String left=m.group(1),right=m.group(2);String[] l=left.split("\\."),r=right.split("\\.");long lw=Long.parseLong(l[0]),rw=Long.parseLong(r[0]);step(g,"왼쪽 수의 정수 부분을 쓰세요.","왼쪽 정수 부분 = ",lw);step(g,"오른쪽 수의 정수 부분을 쓰세요.","오른쪽 정수 부분 = ",rw);
   String lf=l.length==2?l[1]:"",rf=r.length==2?r[1]:"";if(lw==rw){for(int i=0;i<Math.max(lf.length(),rf.length());i++){int ld=i<lf.length()?lf.charAt(i)-'0':0,rd=i<rf.length()?rf.charAt(i)-'0':0;step(g,"해당 자리가 없으면 0을 쓰세요.","왼쪽 소수점 아래 "+(i+1)+"번째 숫자 = ",ld);step(g,"해당 자리가 없으면 0을 쓰세요.","오른쪽 소수점 아래 "+(i+1)+"번째 숫자 = ",rd);if(ld!=rd)break;}}
   cmp=new BigDecimal(left).compareTo(new BigDecimal(right));choose(g,"같은 자리끼리 비교해 기호를 고르세요. 모든 자리의 값이 같으면 같은 수입니다.",cmp);
  }
  q.studyGuide=g;
 }
 private static void step(StudyGuide g,String text,String before,long expected){g.step(text,before,"",Long.toString(expected));}
 private static void choose(StudyGuide g,String text,int cmp){g.choice(text,Map.of("<","<","=","=",">",">"),cmp<0?"<":cmp>0?">":"=");}
}
