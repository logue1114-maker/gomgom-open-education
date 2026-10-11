package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
/** Public three-term expressions, with learner-entered operands and intermediate values. */
public final class MixedOperationOrder {
 private MixedOperationOrder(){}
 public record Work(Rational left,Rational right,String firstOperation,Rational intermediate,Rational remainingLeft,Rational remainingRight,String remainingOperation,String priority,Rational answer){}
 private static Rational calculate(Rational a,Rational b,String op){return switch(op){case "+"->a.add(b);case "-"->a.sub(b);case "*"->a.mul(b);case "/"->a.div(b);default->throw new IllegalArgumentException();};}
 public static Work read(Question q){
  if(q==null||!"mixed".equals(q.skillId)||q.prompt==null)return null;
  String s=q.prompt.replace('×','*').replace('÷','/').replace('−','-').replaceAll("\\s+","");
  Matcher m=Pattern.compile("^(\\()?([0-9]{1,7})([+*/-])([0-9]{1,7})(\\))?([+*/-])([0-9]{1,7})$").matcher(s);
  if(!m.matches()||(m.group(1)==null)!=(m.group(5)==null))return null;
  try{
   Rational a=Rational.of(Long.parseLong(m.group(2))),b=Rational.of(Long.parseLong(m.group(4))),c=Rational.of(Long.parseLong(m.group(7)));String first=m.group(3),second=m.group(6);boolean brackets=m.group(1)!=null;
   Set<String> shapes=brackets?Set.of("+*","-*","+/","-/"):Set.of("+*","-*","+/","-/","*/","/*","+-","-+");if(!shapes.contains(first+second))return null;
   boolean rightFirst=!brackets&&(first.equals("+")||first.equals("-"))&&(second.equals("*")||second.equals("/"));
   Rational left=rightFirst?b:a,right=rightFirst?c:b;String op=rightFirst?second:first;Rational intermediate=calculate(left,right,op),remainingLeft=rightFirst?a:intermediate,remainingRight=rightFirst?intermediate:c;String remainingOperation=rightFirst?first:second;Rational answer=calculate(remainingLeft,remainingRight,remainingOperation);
   if(!intermediate.isInteger()||!answer.isInteger()||intermediate.compareTo(Rational.ZERO)<0||answer.compareTo(Rational.ZERO)<0)return null;
   return new Work(left,right,op,intermediate,remainingLeft,remainingRight,remainingOperation,brackets?"brackets":rightFirst?"right":"left",answer);
  }catch(IllegalArgumentException|ArithmeticException e){return null;}
 }
 static Checker.Result check(Question q,List<String> answers){Work w=read(q);if(w==null||answers==null||answers.size()!=1||answers.get(0)==null||!answers.get(0).trim().matches("[0-9]{1,15}"))return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"수 입력 필요");boolean correct=w.answer().toString().equals(answers.get(0).trim().replaceFirst("^0+(?!$)",""));return new Checker.Result(correct?Checker.Status.CORRECT:Checker.Status.WRONG_ANSWER,correct?-1:0,correct?"정답":"이 수 확인");}
 private static String display(String op){return op.equals("*")?"×":op.equals("/")?"÷":op.equals("-")?"−":op;}
 public static void attach(Question q){Work w=read(q);if(w==null)return;StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="mixed-operation-order-v3";Map<String,String> choices=new LinkedHashMap<>();if(w.priority().equals("brackets")){choices.put("brackets","괄호 안");choices.put("outside","괄호 밖");}else{choices.put("left","왼쪽 연산");choices.put("right","오른쪽 연산");}
  g.choice("먼저 계산할 부분을 고르세요.",choices,w.priority())
   .step("먼저 계산할 식의 왼쪽 수를 쓰세요.","첫 계산의 왼쪽 수 = ","",w.left().toString())
   .step("먼저 계산할 식의 오른쪽 수를 쓰세요.","첫 계산의 오른쪽 수 = ","",w.right().toString())
   .step("고른 두 수를 계산하세요.","왼쪽 수 "+display(w.firstOperation())+" 오른쪽 수 = ","",w.intermediate().toString())
   .step("남은 식의 왼쪽 수를 쓰세요.","남은 식의 왼쪽 수 = ","",w.remainingLeft().toString())
   .step("남은 식의 오른쪽 수를 쓰세요.","남은 식의 오른쪽 수 = ","",w.remainingRight().toString())
   .step("남은 두 수를 계산하세요.","왼쪽 수 "+display(w.remainingOperation())+" 오른쪽 수 = ","",w.answer().toString());q.studyGuide=g;
 }
}
