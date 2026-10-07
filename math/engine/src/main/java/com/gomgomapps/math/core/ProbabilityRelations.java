package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
/** Probability relations derived only from stated public conditions and givens. */
public final class ProbabilityRelations {
 private ProbabilityRelations(){}
 public static boolean supports(String id){return Set.of("sec_probability_add","sec_probability_multiply").contains(id);}
 public static void attach(Question q){
  if(q==null||!supports(q.skillId)||q.prompt==null)return;
  StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="probability-relations-v1";
  if(q.skillId.equals("sec_probability_add")){
   Matcher m=Pattern.compile("같은 가능성의 결과가 (\\d+)개이고 서로 겹치지 않는 A가 (\\d+)개, B가 (\\d+)개입니다. A 또는 B일 확률은\\?").matcher(q.prompt);if(!m.matches())return;
   Rational total=Expression.number(m.group(1)),a=Expression.number(m.group(2)),b=Expression.number(m.group(3)),sum=a.add(b);if(total.compareTo(Rational.ZERO)<=0||sum.compareTo(total)>0)return;
   g.step("문제에서 A가 일어나는 경우의 수를 찾아 쓰세요.","A의 경우의 수 = ","",a.toString())
    .step("문제에서 B가 일어나는 경우의 수를 찾아 쓰세요.","B의 경우의 수 = ","",b.toString())
    .step("문제에서 전체 경우의 수를 찾아 쓰세요.","전체 경우의 수 = ","",total.toString())
    .step("A와 B는 서로 겹치지 않습니다. 두 경우의 수를 더하세요.","A의 경우의 수 + B의 경우의 수 = ","",sum.toString())
    .step("각 결과가 일어날 가능성은 같습니다. A 또는 B의 경우의 수를 전체 경우의 수로 나누세요.","A 또는 B의 경우의 수 ÷ 전체 경우의 수 = ","",sum.div(total).toString());
  }else{
   String number="(\\d+(?:\\.\\d+)?(?:/\\d+)?)";Matcher m=Pattern.compile("서로 독립인 두 시행에서 A가 일어날 확률은 "+number+", B가 일어날 확률은 "+number+"입니다. 둘 다 일어날 확률은\\?").matcher(q.prompt);if(!m.matches())return;
   Rational a=Expression.number(m.group(1)),b=Expression.number(m.group(2));if(!probability(a)||!probability(b))return;
   g.step("문제에서 A가 일어날 확률을 찾아 쓰세요.","A의 확률 = ","",a.toString())
    .step("문제에서 B가 일어날 확률을 찾아 쓰세요.","B의 확률 = ","",b.toString())
    .step("두 시행은 서로 독립입니다. A와 B가 모두 일어날 확률은 두 확률의 곱입니다.","A의 확률 × B의 확률 = ","",a.mul(b).toString());
  }q.studyGuide=g;
 }
 private static boolean probability(Rational x){return x.compareTo(Rational.ZERO)>=0&&x.compareTo(Rational.ONE)<=0;}
}
