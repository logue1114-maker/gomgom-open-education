package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
/** Blank concept frames derived from the visible mixed, equivalent or quantity fraction. */
public final class FractionConceptRelations {
 private FractionConceptRelations(){}
 public static boolean supports(String id){return DecimalFractionRelations.supports(id)||Set.of("el_mixed_to_improper","el_improper_to_mixed","el_fraction_common_den","el_fraction_of_number").contains(id);}
 public static void attach(Question q){
  if(q==null||!supports(q.skillId)||q.prompt==null)return;if(DecimalFractionRelations.supports(q.skillId)){DecimalFractionRelations.attach(q);return;}StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="fraction-concept-relations-v1";Matcher m;
  switch(q.skillId){
   case "el_mixed_to_improper":
    m=Pattern.compile("(\\d+)와 (\\d+)/(\\d+)을 가분수로 나타내세요\\.\\n□/(\\d+)").matcher(q.prompt);if(!m.matches()||!m.group(3).equals(m.group(4)))return;
    long w=number(m,1),a=number(m,2),b=number(m,3);if(b==0||a>=b)return;
    step(g,"대분수의 자연수 부분을 쓰세요.","자연수 부분 w = ",w);step(g,"문제의 분자를 쓰세요.","분자 a = ",a);step(g,"문제의 분모를 쓰세요.","분모 b = ",b);
    step(g,"자연수 부분에 분모를 곱하세요.","곱 t = w × b = ",w*b);step(g,"앞에서 구한 곱에 분자를 더하세요.","가분수 분자 n = t + a = ",w*b+a);g.fractionResult(4,2);break;
   case "el_improper_to_mixed":
    m=Pattern.compile("(\\d+)/(\\d+)을 대분수로 나타내세요\\.\\n□와 □/(\\d+)").matcher(q.prompt);if(!m.matches()||!m.group(2).equals(m.group(3)))return;
    long n=number(m,1),d=number(m,2);if(d==0)return;
    step(g,"문제의 분자를 쓰세요.","분자 n = ",n);step(g,"문제의 분모를 쓰세요.","분모 d = ",d);
    step(g,"분자를 분모로 나눈 몫이 자연수 부분입니다. 몫을 쓰세요.","자연수 부분 w = n ÷ d의 몫 = ",n/d);
    step(g,"분자에서 몫과 분모의 곱을 빼세요. 나머지가 새 분자입니다.","새 분자 r = n − w × d = ",n%d);g.mixedResult(2,3,1);break;
   case "el_fraction_common_den":
    m=Pattern.compile("(\\d+)/(\\d+)을 분모가 (\\d+)인 분수로 통분하세요\\.\\n□/(\\d+)").matcher(q.prompt);if(!m.matches()||!m.group(3).equals(m.group(4)))return;
    long top=number(m,1),den=number(m,2),target=number(m,3);if(den==0||target==0||target%den!=0)return;
    step(g,"문제의 분자를 쓰세요.","분자 a = ",top);step(g,"문제의 분모를 쓰세요.","분모 b = ",den);step(g,"문제에서 정한 새 분모를 쓰세요.","새 분모 D = ",target);
    step(g,"원래 분모를 새 분모로 바꾸는 배수를 구하세요.","배수 k = D ÷ b = ",target/den);step(g,"분자에도 같은 배수를 곱하세요.","새 분자 n = a × k = ",top*(target/den));g.fractionResult(4,2);break;
   default:
    m=Pattern.compile("(\\d+)의 (\\d+)/(\\d+)은 얼마인가요\\?").matcher(q.prompt);if(!m.matches())return;
    long total=number(m,1),num=number(m,2),bottom=number(m,3);if(bottom==0||total%bottom!=0)return;
    step(g,"전체의 양을 쓰세요.","전체 T = ",total);step(g,"문제의 분자를 쓰세요.","분자 a = ",num);step(g,"문제의 분모를 쓰세요.","분모 b = ",bottom);
    step(g,"전체를 분모만큼 똑같이 나누세요.","한 부분 P = T ÷ b = ",total/bottom);step(g,"한 부분의 양에 필요한 부분의 수를 곱하세요.","필요한 양 = P × a = ",(total/bottom)*num);
  }
  q.studyGuide=g;
 }
 private static long number(Matcher m,int index){return Long.parseLong(m.group(index));}
 private static void step(StudyGuide g,String text,String before,long value){g.step(text,before,"",Long.toString(value));}
}
