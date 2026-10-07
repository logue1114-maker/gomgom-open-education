package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
/** Public set sizes determine student retrieval, sums and removal of shared elements. */
public final class SetCountRelations {
 private SetCountRelations(){}
 public static boolean supports(String id){return Set.of("sec_set_intersection","sec_set_union","sec_set_difference").contains(id);}
 public static void attach(Question q){
  if(q==null||q.prompt==null||!supports(q.skillId))return;
  Matcher complement=Pattern.compile("전체집합 U에서 A⊆U이고 n\\(U\\)=(\\d+), n\\(A\\)=(\\d+)일 때 A의 여집합의 원소 수는\\?").matcher(q.prompt);
  if(q.skillId.equals("sec_set_difference")&&complement.matches()){
   Rational u=Expression.number(complement.group(1)),a=Expression.number(complement.group(2));
   if(a.compareTo(u)>0)return;
   StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="set-complement-relations-v1";
   step(g,"전체집합 U의 원소 수를 찾아 쓰세요.","전체집합 원소 수 u = ",u);
   step(g,"집합 A의 원소 수를 찾아 쓰세요.","A의 원소 수 a = ",a);
   step(g,"여집합은 전체집합에서 A에 속하지 않는 원소입니다. 전체 원소 수에서 A의 원소 수를 빼세요.","n(Aᶜ) = u − a = ",u.sub(a));
   q.studyGuide=g;return;
  }
  Matcher m=Pattern.compile("n\\(A\\)=(\\d+), n\\(B\\)=(\\d+), n\\(A([∩∪])B\\)=(\\d+)일 때 (교집합|합집합|차집합 A-B)의 원소 수는\\?").matcher(q.prompt);
  if(!m.matches())return;Rational a=Expression.number(m.group(1)),b=Expression.number(m.group(2)),c=Expression.number(m.group(4));boolean givenUnion=m.group(3).equals("∪");String target=m.group(5);
  if(givenUnion&&!target.equals("교집합"))return;
  Rational sum=a.add(b),common=givenUnion?sum.sub(c):c;
  if(common.compareTo(Rational.ZERO)<0||common.compareTo(a)>0||common.compareTo(b)>0)return;
  StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="set-count-relations-v1";
  if(target.equals("교집합")&&!givenUnion){
   // Existing published reading exercises remain intact; no supplied numeric help or answer transfer.
   g.teachingVersion="set-intersection-read-relations-v1";
   step(g,"교집합은 두 집합에 함께 있는 원소입니다. 문제에서 n(A∩B)의 값을 찾아 쓰세요.","교집합 원소 수 n(A∩B) = ",common);
  }else{
   step(g,"집합 A의 원소 수를 찾아 쓰세요.","A의 원소 수 a = ",a);
   if(target.equals("차집합 A-B")){
    step(g,"두 집합에 함께 있는 원소 수를 찾아 쓰세요.","공통 원소 수 c = ",common);
    step(g,"A에서 공통 원소를 빼세요. B에만 있는 원소는 A에서 빼지 않습니다.","n(A−B) = a − c = ",a.sub(common));
   }else{
    step(g,"집합 B의 원소 수를 찾아 쓰세요.","B의 원소 수 b = ",b);
    step(g,givenUnion?"두 집합을 합친 원소 수를 찾아 쓰세요.":"두 집합에 함께 있는 원소 수를 찾아 쓰세요.",givenUnion?"합집합 원소 수 u = ":"공통 원소 수 c = ",c);
    step(g,"두 집합의 원소 수를 더하세요.","합 s = a + b = ",sum);
    step(g,givenUnion?"두 집합의 원소 수의 합에서 합집합 원소 수를 빼세요.":"공통 원소는 두 번 세었으므로 한 번 빼세요.",givenUnion?"n(A∩B) = s − u = ":"n(A∪B) = s − c = ",givenUnion?common:sum.sub(common));
   }
  }q.studyGuide=g;
 }
 private static void step(StudyGuide g,String text,String before,Rational expected){g.step(text,before,"",expected.toString());}
}
