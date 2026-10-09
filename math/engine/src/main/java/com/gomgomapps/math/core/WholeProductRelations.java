package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
/** Split both factors by place so every elementary product is a table fact. */
public final class WholeProductRelations {
 private WholeProductRelations(){}
 public static boolean supports(String id){return LargeWrittenProducts.supports(id)||Set.of("mul2","mul3","mul22","el_mul_3x2").contains(id);}
 public static void attach(Question q){
  if(q==null||!supports(q.skillId))return;Matcher m=Pattern.compile("^(\\d{1,6})\\s*×\\s*(\\d{1,6})(?:의 값은\\?)?$").matcher(q.prompt);if(!m.matches())return;
  long a=Long.parseLong(m.group(1)),b=Long.parseLong(m.group(2));StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="whole-product-relations-v1";
  g.step("첫 수를 쓰세요.","첫 수 = ","",String.valueOf(a)).step("둘째 수를 쓰세요.","둘째 수 = ","",String.valueOf(b));
  String[] places={"일의 자리","십의 자리","백의 자리","천의 자리","만의 자리","십만의 자리"};
  for(int right=0;right<String.valueOf(b).length();right++)for(int left=0;left<String.valueOf(a).length();left++){
   long x=a/(long)Math.pow(10,left)%10,y=b/(long)Math.pow(10,right)%10,partial=x*y,power=(long)Math.pow(10,left+right);
   g.step(places[left]+" · 첫 수의 이 자리 숫자를 쓰세요.","첫 자리 숫자 = ","",String.valueOf(x));
   g.step(places[right]+" · 둘째 수의 이 자리 숫자를 쓰세요.","둘째 자리 숫자 = ","",String.valueOf(y));
   g.step("확인한 두 자리 숫자를 곱하세요.","첫 자리 숫자 × 둘째 자리 숫자 = ","",String.valueOf(partial));
   if(power>1)g.step("이 자리의 자릿값을 반영하세요.","자리 곱 × "+power+" = ","",String.valueOf(partial*power));
  }
  g.step("자릿값을 맞춘 곱을 모두 더하세요.","각 자리의 곱의 합 = ","",String.valueOf(a*b));q.studyGuide=g;
 }
}
