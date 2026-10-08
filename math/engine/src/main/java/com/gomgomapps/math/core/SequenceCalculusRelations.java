package com.gomgomapps.math.core;
import java.util.*;
import java.util.regex.*;

/** Rebuild blank relationships from the displayed statement, including restored questions. */
public final class SequenceCalculusRelations {
 private SequenceCalculusRelations(){}
 public static boolean supports(String id){return Set.of("arithmeticSeq","geometricSeq","limit","derivative","integral").contains(id);}
 public static void attach(Question q){
  if(q==null||!supports(q.skillId)||q.prompt==null)return;
  StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="sequence-calculus-relations-v1";
  Matcher m;String n="(-?\\d+)";
  if(q.skillId.endsWith("Seq")){
   boolean arithmetic=q.skillId.equals("arithmeticSeq");
   m=Pattern.compile("첫째항 "+n+", 공"+(arithmetic?"차":"비")+" "+n+"인 등"+(arithmetic?"차":"비")+"수열의 제(\\d+)항은\\?").matcher(q.prompt);if(!m.matches())return;
   Rational a=num(m,1),r=num(m,2),index=num(m,3),exponent=index.sub(Rational.ONE);
   step(g,"문제에서 첫째항을 찾아 쓰세요.","a = ",a);
   step(g,arithmetic?"문제에서 공차를 찾아 쓰세요.":"문제에서 공비를 찾아 쓰세요.",arithmetic?"d = ":"r = ",r);
   step(g,"구하려는 항의 번호를 쓰세요.","n = ",index);
   step(g,"첫째항에서 몇 번 이동하는지 구하세요.","k = n − 1 = ",exponent);
   Rational change=arithmetic?r.mul(exponent):r.pow(Integer.parseInt(exponent.toString()));
   step(g,arithmetic?"공차에 이동 횟수를 곱하세요.":"공비를 이동 횟수만큼 거듭제곱하세요.",arithmetic?"t = d × k = ":"t = r^k = ",change);
   step(g,arithmetic?"첫째항에 변화량을 더하세요.":"첫째항에 구한 거듭제곱을 곱하세요.",arithmetic?"a_n = a + t = ":"a_n = a × t = ",arithmetic?a.add(change):a.mul(change));
  }else if(q.skillId.equals("limit")){
   m=Pattern.compile("x가 "+n+"에 가까워질 때\\n"+n+"x² \\+ \\("+n+"\\)의 극한값은\\?").matcher(q.prompt);if(!m.matches())return;
   Rational v=num(m,1),a=num(m,2),b=num(m,3),square=v.pow(2),term=a.mul(square);
   step(g,"x가 가까워지는 값을 쓰세요.","v = ",v);
   step(g,"x²의 계수를 부호까지 쓰세요.","a = ",a);
   step(g,"상수항을 부호까지 쓰세요.","b = ",b);
   step(g,"다가가는 값을 제곱하세요.","s = v² = ",square);
   step(g,"계수와 제곱값을 곱하세요.","t = a × s = ",term);
   step(g,"다항식은 연속이므로 대입값이 극한값입니다. 상수항을 더하세요.","L = t + b = ",term.add(b));
  }else{
   boolean integral=q.skillId.equals("integral");
   m=Pattern.compile(integral?"∫₀\\^(\\d+) \\("+n+"x\\^(\\d+)\\) dx의 값은\\?":"f\\(x\\)="+n+"x\\^(\\d+)\\nf′\\("+n+"\\)의 값은\\?").matcher(q.prompt);if(!m.matches())return;
   Rational a=num(m,integral?2:1),p=num(m,integral?3:2),v=num(m,integral?1:3),power=p.add(Rational.of(integral?1:-1));
   step(g,"x의 거듭제곱 앞 계수를 부호까지 쓰세요.","a = ",a);
   step(g,"x의 지수를 쓰세요.","p = ",p);
   step(g,integral?"적분 구간의 위 끝값을 쓰세요.":"도함수에 대입할 값을 쓰세요.","v = ",v);
   step(g,integral?"원시함수의 지수는 원래 지수보다 1 큽니다.":"도함수의 지수는 원래 지수보다 1 작습니다.",integral?"k = p + 1 = ":"k = p − 1 = ",power);
   Rational coefficient=integral?a.div(power):a.mul(p),value=v.pow(Integer.parseInt(power.toString()));
   step(g,integral?"계수를 새 지수로 나누세요.":"계수에 원래 지수를 곱하세요.",integral?"c = a ÷ k = ":"c = a × p = ",coefficient);
   step(g,"대입할 값을 새 지수만큼 거듭제곱하세요.","t = v^k = ",value);
   step(g,integral?"아래 끝값이 0이고 새 지수가 양수이므로 아래 끝값의 원시함수 값은 0입니다. 위 끝값의 값을 구하세요.":"새 계수와 거듭제곱값을 곱하세요.",integral?"I = c × t = ":"f′(v) = c × t = ",coefficient.mul(value));
  }
  q.studyGuide=g;
 }
 private static Rational num(Matcher m,int i){return Expression.number(m.group(i));}
 private static void step(StudyGuide g,String text,String before,Rational value){g.step(text,before,"",value.toString());}
}
