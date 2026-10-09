package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
/** A whole quantity's proper fraction, derived solely from its public statement. */
public final class QuantityFractionCheck {
 private QuantityFractionCheck(){}
 public static final String ID="el_fraction_of_number";
 private static final List<Pattern> PATTERNS=List.of(
  Pattern.compile("^(\\d{1,6})의 (\\d{1,3})/(\\d{1,3})은 얼마인가요\\?$"),
  Pattern.compile("^동그라미 (\\d{1,6})개의 (\\d{1,3})/(\\d{1,3})은 몇 개인가요\\?$"),
  Pattern.compile("^(\\d{1,6}) cm의 (\\d{1,3})/(\\d{1,3})은 몇 cm인가요\\?$"));
 public static int[] read(Question q){
  if(q==null||!ID.equals(q.skillId)||q.prompt==null)return null;
  for(Pattern p:PATTERNS){Matcher m=p.matcher(q.prompt);if(!m.matches())continue;int total=Integer.parseInt(m.group(1)),num=Integer.parseInt(m.group(2)),den=Integer.parseInt(m.group(3));if(total<1||den<2||num<1||num>=den||total%den!=0)return null;return new int[]{total,num,den,total/den,total/den*num};}return null;
 }
 static Checker.Result check(Question q,List<String> answers){
  int[] publicValues=read(q);if(publicValues==null||answers==null||answers.size()!=1||answers.get(0)==null)return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"답 입력 필요");
  String input=Expression.normalize(answers.get(0).trim());if(!input.matches("[+-]?\\d+(?:\\.\\d+)?(?:/[+-]?\\d+)?"))return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"마지막 답은 수로 입력");
  try{return Expression.number(input).equals(Rational.of(publicValues[4]))?new Checker.Result(Checker.Status.CORRECT,-1,"정답"):new Checker.Result(Checker.Status.WRONG_ANSWER,0,"이 답 확인");}catch(IllegalArgumentException|ArithmeticException ex){return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"답의 기호 확인 필요");}
 }
}
