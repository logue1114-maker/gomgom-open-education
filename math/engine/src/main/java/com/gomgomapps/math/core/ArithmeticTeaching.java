package com.gomgomapps.math.core;
import java.util.regex.*;
/** Student-entered arithmetic frames, derived from the original visible operands. */
final class ArithmeticTeaching {
 private ArithmeticTeaching(){}
 static void attach(Question q){
  ColumnArithmeticTeaching.attach(q);
  if(q.skillId.equals("reduce")){
   Matcher fraction=Pattern.compile("^(\\d+)/(\\d+)").matcher(q.prompt);if(!fraction.find())return;
   int numerator=Integer.parseInt(fraction.group(1)),denominator=Integer.parseInt(fraction.group(2)),common=gcd(numerator,denominator);
   q.studyGuide=new StudyGuide().transfer(false)
    .step("분자와 분모의 최대공약수를 구하세요.","gcd("+numerator+", "+denominator+") = ","",String.valueOf(common))
    .step("분자를 최대공약수로 나누세요.",numerator+" ÷ "+common+" = ","",String.valueOf(numerator/common))
    .step("분모를 같은 최대공약수로 나누세요.",denominator+" ÷ "+common+" = ","",String.valueOf(denominator/common));return;
  }
  BasicAlgebraRelations.attach(q);
 }
 private static int gcd(int a,int b){while(b!=0){int next=a%b;a=b;b=next;}return Math.abs(a);}
}
