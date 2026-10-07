package com.gomgomapps.math.core;
import java.util.*;
import java.util.regex.*;
/** Blank relationships derived only from the public expression. */
public final class BasicAlgebraRelations {
 private BasicAlgebraRelations(){}
 public static boolean supports(String id){return Set.of("signedAdd","signedMul","likeTerms").contains(id);}
 public static void attach(Question q){
  if(q==null||!supports(q.skillId)||q.prompt==null)return;
  String publicText=q.prompt.replace('−','-');
  StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="basic-algebra-relations-v1";
  if(q.skillId.equals("likeTerms")){
   Matcher m=Pattern.compile("(-?\\d+)x \\+ \\((-?\\d+)x\\) \\+ \\((-?\\d+)\\)").matcher(publicText);if(!m.matches())return;
   long a=Long.parseLong(m.group(1)),b=Long.parseLong(m.group(2)),c=Long.parseLong(m.group(3));
   step(g,"첫 번째 x항의 계수를 부호와 함께 쓰세요.","첫 계수 a = ",a);
   step(g,"두 번째 x항의 계수를 부호와 함께 쓰세요.","둘째 계수 b = ",b);
   step(g,"문자가 없는 상수항을 부호와 함께 쓰세요.","상수항 c = ",c);
   step(g,"x항끼리 계수를 더하세요. 상수항은 그대로 둡니다.","(a + b)x + c = (",a+b,")x + c");
  }else{
   Matcher m=Pattern.compile("\\(?(-?\\d+)\\)?\\s*([+-]|×|÷)\\s*\\(?(-?\\d+)\\)?").matcher(publicText);if(!m.matches())return;
   long a=Long.parseLong(m.group(1)),b=Long.parseLong(m.group(3));String op=m.group(2);
   boolean add=q.skillId.equals("signedAdd"),subtract=op.equals("-"),divide=op.equals("÷");
   if(add&&!Set.of("+","-").contains(op)||!add&&!Set.of("×","÷").contains(op)||divide&&(b==0||a%b!=0))return;
   step(g,"첫 번째 수를 부호와 함께 쓰세요.","첫 수 a = ",a);
   step(g,"두 번째 수를 부호와 함께 쓰세요.","둘째 수 b = ",b);
   long second=add&&subtract?-b:b,left=Math.abs(a),right=Math.abs(second);
   if(subtract)step(g,"빼는 수의 부호를 바꾸어 덧셈으로 바꾸세요.","a − b = a + t, t = ",second);
   step(g,"첫 번째 수의 절댓값을 구하세요.","크기 u = |a| = ",left);
   step(g,"두 번째 수의 절댓값을 구하세요.",subtract?"크기 v = |t| = ":"크기 v = |b| = ",right);
   boolean same=(a<0)==(second<0);long size=add?(same?left+right:Math.abs(left-right)):(divide?left/right:left*right);
   step(g,add?(same?"부호가 같으면 절댓값을 더하세요.":"부호가 다르면 큰 절댓값에서 작은 절댓값을 빼세요."):(divide?"절댓값끼리 나누세요.":"절댓값끼리 곱하세요."),add?(same?"결과의 크기 w = u + v = ":"결과의 크기 w = 큰 크기 − 작은 크기 = "):(divide?"결과의 크기 w = u ÷ v = ":"결과의 크기 w = u × v = "),size);
   String instruction=size==0?"계산한 절댓값을 결과에 쓰세요.":add?(same?"공통 부호를 계산 결과에 붙이세요.":"절댓값이 큰 수의 부호를 계산 결과에 붙이세요."):"같은 부호끼리는 양수, 다른 부호끼리는 음수로 나타내세요.";
   step(g,instruction,"결과 = ",add?a+second:(divide?a/b:a*b));
  }
  q.studyGuide=g;
 }
 private static void step(StudyGuide g,String text,String before,long value){step(g,text,before,value,"");}
 private static void step(StudyGuide g,String text,String before,long value,String after){g.step(text,before,after,Long.toString(value));}
}
