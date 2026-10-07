package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
/** Positive elementary fraction operations, with learner-entered symbolic frames. */
public final class PrimaryFractionRelations {
 private PrimaryFractionRelations(){}
 public static boolean supports(String id){return Set.of("fracAddLike","fracSubLike","fracAdd","fracSub","fracMul","fracDiv").contains(id);}
 public static void attach(Question q){
  if(q==null||!supports(q.skillId)||q.prompt==null)return;
  Matcher m=Pattern.compile("\\((\\d+)/(\\d+)\\)\\s*([+*/×÷-])\\s*\\((\\d+)/(\\d+)\\)").matcher(q.prompt.replace('−','-'));if(!m.matches())return;
  long a=Long.parseLong(m.group(1)),b=Long.parseLong(m.group(2)),c=Long.parseLong(m.group(4)),d=Long.parseLong(m.group(5));String op=m.group(3);
  boolean add=op.equals("+"),sub=op.equals("-"),div=op.equals("/")||op.equals("÷"),like=q.skillId.endsWith("Like");
  if(b==0||d==0||div&&c==0||like&&b!=d)return;
  long n,den;StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="primary-fraction-relations-v1";
  step(g,"첫 번째 분수의 분자를 쓰세요.","첫 분자 a = ",a);
  if(!like)step(g,"첫 번째 분수의 분모를 쓰세요.","첫 분모 b = ",b);
  step(g,"두 번째 분수의 분자를 쓰세요.","둘째 분자 c = ",c);
  step(g,like?"두 분수에 같은 분모를 쓰세요.":"두 번째 분수의 분모를 쓰세요.",like?"같은 분모 b = ":"둘째 분모 d = ",like?b:d);
  if(add||sub){
   den=b/gcd(b,d)*d;long u=a*(den/b),v=c*(den/d);n=add?u+v:u-v;if(n<0)return;
   if(like){step(g,add?"분모는 그대로 두고 분자끼리 더하세요.":"분모는 그대로 두고 분자끼리 빼세요.",add?"계산한 분자 N = a + c = ":"계산한 분자 N = a − c = ",n);}
   else{
    step(g,"두 분모의 최소공배수를 구하세요.","공통분모 L = lcm(b, d) = ",den);
    step(g,"첫 분모를 공통분모로 바꾸는 수를 첫 분자에도 곱하세요.","통분한 첫 분자 u = a × (L ÷ b) = ",u);
    step(g,"둘째 분모를 공통분모로 바꾸는 수를 둘째 분자에도 곱하세요.","통분한 둘째 분자 v = c × (L ÷ d) = ",v);
    step(g,add?"통분한 분자끼리 더하세요.":"통분한 분자끼리 빼세요.",add?"계산한 분자 N = u + v = ":"계산한 분자 N = u − v = ",n);
   }
  }else{
   long top=div?d:c,bottom=div?c:d;
   if(div){step(g,"나누는 분수를 뒤집어 역수의 분자를 쓰세요.","역수의 분자 e = d = ",top);step(g,"역수의 분모에 원래 분자를 쓰세요.","역수의 분모 f = c = ",bottom);}
   n=a*top;den=b*bottom;
   step(g,"분자끼리 곱하세요.",div?"계산한 분자 N = a × e = ":"계산한 분자 N = a × c = ",n);
   step(g,"분모끼리 곱하세요.",div?"계산한 분모 D = b × f = ":"계산한 분모 D = b × d = ",den);
  }
  if(like){q.studyGuide=g.fractionResult(3,2);return;}
  long common=gcd(n,den);String symbol=like?"b":add||sub?"L":"D";
  step(g,"계산한 분자와 분모를 모두 나눌 수 있는 가장 큰 수를 구하세요.","최대공약수 g = gcd(N, "+symbol+") = ",common);
  step(g,"계산한 분자를 최대공약수로 나누세요.","최종 분자 = N ÷ g = ",n/common);
  step(g,"분모도 같은 최대공약수로 나누세요.","최종 분모 = "+symbol+" ÷ g = ",den/common);
  q.studyGuide=g.fractionResult(g.frames.size()-2,g.frames.size()-1);
 }
 private static long gcd(long a,long b){while(b!=0){long r=a%b;a=b;b=r;}return a;}
 private static void step(StudyGuide g,String instruction,String before,long value){g.step(instruction,before,"",Long.toString(value));}
}
