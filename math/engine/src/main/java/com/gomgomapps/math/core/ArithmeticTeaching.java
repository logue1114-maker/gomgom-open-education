package com.gomgomapps.math.core;
import java.util.regex.*;
/** Student-entered arithmetic frames, derived from the original visible operands. */
final class ArithmeticTeaching {
 private ArithmeticTeaching(){}
 static void attach(Question q){
  if(q.skillId.equals("reduce")){
   Matcher fraction=Pattern.compile("^(\\d+)/(\\d+)").matcher(q.prompt);if(!fraction.find())return;
   int numerator=Integer.parseInt(fraction.group(1)),denominator=Integer.parseInt(fraction.group(2)),common=gcd(numerator,denominator);
   q.studyGuide=new StudyGuide().transfer(false)
    .step("분자와 분모의 최대공약수를 구하세요.","gcd("+numerator+", "+denominator+") = ","",String.valueOf(common))
    .step("분자를 최대공약수로 나누세요.",numerator+" ÷ "+common+" = ","",String.valueOf(numerator/common))
    .step("분모를 같은 최대공약수로 나누세요.",denominator+" ÷ "+common+" = ","",String.valueOf(denominator/common));return;
  }
  if(!q.skillId.equals("signedAdd")&&!q.skillId.equals("signedMul"))return;
  Matcher operands=Pattern.compile("^\\(?(-?\\d+)\\)?\\s*([+−-]|×|÷)\\s*\\(?(-?\\d+)\\)?$").matcher(q.expression);if(!operands.matches())return;
  int a=Integer.parseInt(operands.group(1)),b=Integer.parseInt(operands.group(3));String operation=operands.group(2);StudyGuide guide=new StudyGuide().transfer(false);
  if(q.skillId.equals("signedAdd")){
   if(operation.equals("-")||operation.equals("−")){guide.step("빼는 수의 부호를 바꾸어 덧셈으로 바꾸세요.","−("+b+") = ","",String.valueOf(-b));b=-b;}
   int left=Math.abs(a),right=Math.abs(b);guide.step("첫 번째 수의 절댓값을 구하세요.","|"+a+"| = ","",String.valueOf(left)).step("두 번째 수의 절댓값을 구하세요.","|"+b+"| = ","",String.valueOf(right));
   boolean same=(a<0)==(b<0);int size=same?left+right:Math.abs(left-right);
   guide.step(same?"부호가 같으면 절댓값을 더하세요.":"부호가 다르면 큰 절댓값에서 작은 절댓값을 빼세요.",same?left+" + "+right+" = ":Math.max(left,right)+" − "+Math.min(left,right)+" = ","",String.valueOf(size));
   guide.step(same&&size==0?"계산한 절댓값을 결과에 쓰세요.":same?"공통 부호를 계산 결과에 붙이세요.":left==right?"크기가 같고 부호가 반대인 두 수의 합을 구하세요.":"절댓값이 큰 수의 부호를 계산 결과에 붙이세요.","결과 = ","",String.valueOf(a+b));
  }else{
   int left=Math.abs(a),right=Math.abs(b);boolean divide=operation.equals("÷");if(divide&&(right==0||left%right!=0))return;
   guide.step("첫 번째 수의 절댓값을 구하세요.","|"+a+"| = ","",String.valueOf(left)).step("두 번째 수의 절댓값을 구하세요.","|"+b+"| = ","",String.valueOf(right));
   guide.step(divide?"절댓값끼리 나누세요.":"절댓값끼리 곱하세요.",left+(divide?" ÷ ":" × ")+right+" = ","",String.valueOf(divide?left/right:left*right));
   guide.step(left==0||right==0?"계산한 절댓값을 결과에 쓰세요.":"같은 부호끼리는 양수, 다른 부호끼리는 음수로 나타내세요.","결과 = ","",String.valueOf(divide?a/b:a*b));
  }
  q.studyGuide=guide;
 }
 private static int gcd(int a,int b){while(b!=0){int next=a%b;a=b;b=next;}return Math.abs(a);}
}
