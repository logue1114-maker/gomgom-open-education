package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
/** Greatest positive common divisor and least positive common multiple from public pairs. */
public final class WholeFactorExtrema {
 private WholeFactorExtrema(){}
 public static final String KIND="wholeFactorExtrema",NONE="@no-factor-extremum";
 public record Givens(int a,int b){}
 public static boolean supports(String id){return "gcd".equals(id)||"lcm".equals(id);}
 public static boolean selected(Question q){return q!=null&&KIND.equals(q.kind)&&supports(q.skillId);}
 public static Givens read(Question q){if(!selected(q)||q.prompt==null)return null;Matcher m=Pattern.compile("주어진 수: ([0-9]{1,7}), ([0-9]{1,7})\\n"+(q.skillId.equals("gcd")?"최대공약수를 쓰세요\\.":"가장 작은 양의 공배수를 쓰세요\\.")).matcher(q.prompt);if(!m.matches())return null;int a=Integer.parseInt(m.group(1)),b=Integer.parseInt(m.group(2));return a<=1000000&&b<=1000000?new Givens(a,b):null;}
 public static boolean undefined(Question q){Givens v=read(q);return v!=null&&(q.skillId.equals("gcd")?v.a==0&&v.b==0:v.a==0||v.b==0);}
 static long gcd(long a,long b){while(b!=0){long remainder=a%b;a=b;b=remainder;}return a;}
 static long value(String id,Givens v){long divisor=gcd(v.a,v.b);return id.equals("gcd")?divisor:divisor==0?0:(long)v.a/divisor*v.b;}
 static Question make(String id,int a,int b){if(!supports(id)||a<0||a>1000000||b<0||b>1000000)throw new IllegalArgumentException("whole factor extrema public domain");Question q=new Question(id,"주어진 수: "+a+", "+b+"\n"+(id.equals("gcd")?"최대공약수를 쓰세요.":"가장 작은 양의 공배수를 쓰세요."),"","");q.kind=KIND;Givens v=read(q);q.answers=new String[]{undefined(q)?NONE:""+value(id,v)};q.labels=new String[]{id.equals("gcd")?"최대공약수":"가장 작은 양의 공배수"};q.stepSupport=false;attach(q);return q;}
 static long count(int min,int max){if(min<0||max>1000000||min>max)throw new IllegalArgumentException("whole factor extrema range");long size=max-min+1L;return size*(size+1)/2;}
 static Question indexed(String id,long index,int min,int max){long count=count(min,max);if(index<0||index>=count)throw new IllegalArgumentException("whole factor extrema index");int low=0,high=max-min;while(low<high){int middle=(low+high+1)>>>1;long start=(long)middle*(middle+1)/2;if(start<=index)low=middle;else high=middle-1;}int row=low,column=(int)(index-(long)row*(row+1)/2);return make(id,min+column,min+row);}
 static Question next(String id,Random random,CurriculumLimits limits,Map<String,Integer>recent){int min=Math.max(0,limits.givenMinimum(0)),max=Math.min(1000000,limits.wholeMaximum(1000000));if(max<min)throw new IllegalStateException("No whole factor extrema question matches limits");return IndexedQuestionSupply.chooseLarge(count(min,max),index->indexed(id,index,min,max),random,limits,recent);}
 static Checker.Result check(Question q,List<String>answers){Givens v=read(q);if(v==null||answers==null||answers.isEmpty())return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"수 입력 또는 없음 선택");if(NONE.equals(answers.get(answers.size()-1)))return new Checker.Result(undefined(q)?Checker.Status.CORRECT:Checker.Status.WRONG_ANSWER,undefined(q)?-1:0,undefined(q)?"정답":"없음 선택 확인");if(answers.size()!=1||answers.get(0)==null||!answers.get(0).trim().matches("[0-9]{1,12}"))return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"수 입력 또는 없음 선택");long answer=Long.parseLong(answers.get(0).trim());boolean correct=!undefined(q)&&answer>0&&answer==value(q.skillId,v);return new Checker.Result(correct?Checker.Status.CORRECT:Checker.Status.WRONG_ANSWER,correct?-1:0,correct?"정답":q.skillId.equals("gcd")?"최대공약수 확인":"가장 작은 양의 공배수 확인");}
 public static void attach(Question q){Givens v=read(q);if(v==null)return;StudyGuide guide=new StudyGuide().transfer(false);guide.teachingVersion="whole-factor-extrema-v1";
  if(undefined(q)){Map<String,String>choices=new LinkedHashMap<>();choices.put("1",q.skillId.equals("gcd")?"가장 큰 양의 공약수가 있다":"양의 공배수가 있다");choices.put("0",q.skillId.equals("gcd")?"가장 큰 양의 공약수가 없다":"양의 공배수가 없다");guide.choice(q.skillId.equals("gcd")?"모든 양의 정수는 0의 약수입니다. 0과 0의 양의 공약수에는 가장 큰 수가 없습니다.":"0의 배수는 0뿐입니다. 어느 한 수가 0이면 양의 공배수가 없습니다.",choices,"0");}
  else if(v.a==0||v.b==0)guide.step("모든 양의 정수는 0의 약수입니다. 다른 수의 가장 큰 양의 약수를 쓰세요.","다른 수의 가장 큰 양의 약수 = ","",""+Math.max(v.a,v.b));
  else{long big=Math.max(v.a,v.b),small=Math.min(v.a,v.b),divisor=gcd(v.a,v.b);while(small!=0){long remainder=big%small;guide.step("나머지가 0이 될 때까지 큰 수를 작은 수로 나누세요.",big+" ÷ "+small+"의 나머지 = ","",""+remainder);big=small;small=remainder;}guide.step("마지막으로 0이 아닌 나누는 수를 쓰세요.","최대공약수 = ","",""+divisor);if(q.skillId.equals("lcm")){guide.step("첫째 수를 최대공약수로 나누세요.","첫째 수 ÷ 최대공약수 = ","",""+(v.a/divisor));guide.step("그 몫에 둘째 수를 곱하세요.","몫 × 둘째 수 = ","",""+value(q.skillId,v));}}
  q.studyGuide=guide;
 }
}
