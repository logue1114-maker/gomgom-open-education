package com.gomgomapps.math.core;
import java.util.*;
/** Whole factors including zero/one, with public product within the selected number range. */
final class BoundedWholeProduct {
 static final String KIND="boundedWholeProduct";
 static boolean selected(String id,CurriculumLimits l){return id.equals("mul22")&&!l.hasWholeDigits()&&l.wholeMaximum(-1)>=10000&&l.wholeMaximum(-1)<=1000000&&l.givenMinimum(-1)==0;}
 static Question make(int a,int b){if(a<0||b<0||a>1000000||b>1000000||(long)a*b>1000000)throw new IllegalArgumentException("whole product bound");Question q=new Question("mul22",a+" × "+b,"",""+((long)a*b));q.kind=KIND;WholeProductRelations.attach(q);return q.withInputs(a,b);}
 static Question next(Random random,CurriculumLimits l,Map<String,Integer>recent){int max=l.wholeMaximum(-1);for(int i=0;i<Math.max(256,recent.size()+1);i++){int magnitude=1+random.nextInt(String.valueOf(max).length()),ceiling=(int)Math.min(max,Math.pow(10,magnitude)-1),a=random.nextInt(ceiling+1),b=random.nextInt((a==0?max:max/a)+1);if(random.nextBoolean()){int t=a;a=b;b=t;}Question q=make(a,b);if(l.allows(q)&&!recent.containsKey(q.signature()))return q;}throw new IllegalStateException("No fresh bounded whole product");}
 static long[] read(Question q){
  if(q==null||q.prompt==null)return null;
  var m=java.util.regex.Pattern.compile("(0|[1-9][0-9]{0,6}) × (0|[1-9][0-9]{0,6})").matcher(q.prompt);
  if(!m.matches())return null;
  long a=Long.parseLong(m.group(1)),b=Long.parseLong(m.group(2));
  return a<=1000000&&b<=1000000&&a*b<=1000000?new long[]{a,b}:null;
 }
 static Question workQuestion(Question q){
  long[] n=read(q);return n==null?null:new Question(q.skillId,q.prompt,n[0]+" * "+n[1],Long.toString(n[0]*n[1]));
 }
 static Checker.Result check(Question q,List<String>answers){if(q==null||q.prompt==null||answers==null||answers.size()!=1||answers.get(0)==null)return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"답 입력 필요");long[] n=read(q);if(n==null)return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"문제 확인 필요");long a=n[0],b=n[1];String input=Expression.normalize(answers.get(0).trim());if(input.length()>120||!input.matches("[+-]?[0-9]+(?:\\.[0-9]+)?(?:/[+-]?[0-9]+)?"))return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"수로 입력 필요");try{boolean correct=Expression.number(input).equals(Rational.of(a*b));return new Checker.Result(correct?Checker.Status.CORRECT:Checker.Status.WRONG_ANSWER,correct?-1:0,correct?"정답":"이 답 확인");}catch(RuntimeException invalid){return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"수로 입력 필요");}}
}
