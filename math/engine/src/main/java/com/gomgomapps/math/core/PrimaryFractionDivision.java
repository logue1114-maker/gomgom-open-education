package com.gomgomapps.math.core;
import java.math.BigInteger;import java.util.*;import java.util.regex.*;
/** Exact public division syntax; saved keys and internal expressions are never operands. */
public final class PrimaryFractionDivision {
 private PrimaryFractionDivision(){}
 private static final String F="([0-9]+)\\s*/\\s*([0-9]+)";
 private static final Pattern PAIR=Pattern.compile("^\\s*\\(\\s*"+F+"\\s*\\)\\s*[/÷]\\s*\\(\\s*"+F+"\\s*\\)\\s*$"),BARE_PAIR=Pattern.compile("^\\s*"+F+"\\s*÷\\s*"+F+"\\s*$");
 private static final Pattern WHOLE=Pattern.compile("^\\s*\\(\\s*"+F+"\\s*\\)\\s*[/÷]\\s*\\(\\s*([0-9]+)\\s*\\)\\s*$"),PLAIN_WHOLE=Pattern.compile("^\\s*\\(\\s*"+F+"\\s*\\)\\s*[/÷]\\s*([0-9]+)\\s*$"),BARE_WHOLE=Pattern.compile("^\\s*"+F+"\\s*÷\\s*([0-9]+)\\s*$");
 public static final class Givens {public final BigInteger a,b,c,d;private Givens(BigInteger a,BigInteger b,BigInteger c,BigInteger d){this.a=a;this.b=b;this.c=c;this.d=d;}}
 public static boolean supports(String id){return "fracDiv".equals(id)||"fracDivInt".equals(id);}
 public static Givens read(Question q){
  if(q==null||!supports(q.skillId)||q.prompt==null||q.prompt.length()>1024)return null;boolean whole="fracDivInt".equals(q.skillId);Matcher m=(whole?WHOLE:PAIR).matcher(q.prompt);if(!m.matches()){m=(whole?PLAIN_WHOLE:BARE_PAIR).matcher(q.prompt);if(!m.matches()){if(!whole)return null;m=BARE_WHOLE.matcher(q.prompt);if(!m.matches())return null;}}
  try{BigInteger a=new BigInteger(m.group(1)),b=new BigInteger(m.group(2)),c=new BigInteger(m.group(3)),d=whole?BigInteger.ONE:new BigInteger(m.group(4));if(a.signum()<0||b.signum()<=0||c.signum()<=0||d.signum()<=0)return null;
   // Retain the common exact-arithmetic size boundary rather than a supplier digit cap.
   new Rational(a,b);new Rational(c,d);return new Givens(a,b,c,d);
  }catch(RuntimeException invalid){return null;}
 }
 static Checker.Result check(Question q,List<String> answers){
  Givens g=read(q);if(g==null)return new Checker.Result(Checker.Status.INPUT_NEEDED,-1,"문제 확인 필요");
  if(answers!=null&&answers.size()==1&&answers.get(0)!=null){String input=Expression.normalize(answers.get(0).trim());if(!input.isEmpty()&&"decimalValue".equals(q.answerFormat)&&input.contains("/"))return new Checker.Result(Checker.Status.INPUT_NEEDED,-1,"소수로 입력 필요");if(!input.isEmpty()&&"hundredthsFraction".equals(q.answerFormat)&&!PercentageNotation.hundredths(input))return new Checker.Result(Checker.Status.WRONG_ANSWER,0,"이 답 확인");}
  try{return PublicFractionArithmetic.checkValue(q,answers,new Rational(g.a,g.b).div(new Rational(g.c,g.d)));}catch(RuntimeException invalid){return new Checker.Result(Checker.Status.INPUT_NEEDED,-1,"문제 확인 필요");}
 }
}
