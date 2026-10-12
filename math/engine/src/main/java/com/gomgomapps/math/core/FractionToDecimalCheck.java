package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;import java.math.BigInteger;
/** Exact decimal value is derived from the public fraction, including unreduced parts. */
public final class FractionToDecimalCheck {
 private FractionToDecimalCheck(){}
 public static final String ID="el_fraction_decimal";
 public static Rational read(Question q){if(q==null||!ID.equals(q.skillId)||q.prompt==null)return null;Matcher m=Pattern.compile("(\\d{1,20})/(\\d{1,20})의 값을 소수로 나타내세요\\.").matcher(q.prompt);if(!m.matches())return null;BigInteger n=new BigInteger(m.group(1)),d=new BigInteger(m.group(2));if(d.signum()==0)return null;Rational value=new Rational(n,d);BigInteger t=value.d;while(t.mod(BigInteger.valueOf(2)).signum()==0)t=t.divide(BigInteger.valueOf(2));while(t.mod(BigInteger.valueOf(5)).signum()==0)t=t.divide(BigInteger.valueOf(5));return t.equals(BigInteger.ONE)?value:null;}
 static Checker.Result check(Question q,List<String> answers){Rational expected=read(q);if(expected==null)return new Checker.Result(Checker.Status.INPUT_NEEDED,-1,"문제 확인 필요");if(answers==null||answers.size()!=1||answers.get(0)==null)return new Checker.Result(Checker.Status.INPUT_NEEDED,-1,"답 입력 필요");String raw=Expression.normalize(answers.get(0).trim());if(!raw.matches("[+-]?\\d+\\.\\d+")||raw.length()>120)return new Checker.Result(Checker.Status.INPUT_NEEDED,-1,"소수로 입력 필요");try{return Expression.number(raw).equals(expected)?new Checker.Result(Checker.Status.CORRECT,-1,"계산 확인 완료"):new Checker.Result(Checker.Status.WRONG_ANSWER,0,"이 답 확인");}catch(RuntimeException e){return new Checker.Result(Checker.Status.INPUT_NEEDED,-1,"소수 입력 확인 필요");}}
}
