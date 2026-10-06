package com.gomgomapps.math.core;
import java.util.*;

/** Discover a uniquely specified affine rule from public examples, then build its next-step flow. */
public final class SequenceDiscovery {
 private SequenceDiscovery(){}
 public static final List<Catalog.Skill> SKILLS=List.of(
  new Catalog.Skill("discoverIndexRule","항 번호와 값에서 규칙 찾기",11,1,3,"대수","discoverIndexRule",200,"directTermRule","항 번호와 값의 관계에서 곱하는 수와 더하는 수를 찾는다."),
  new Catalog.Skill("discoverRecursiveRule","연속된 항에서 규칙 찾기",11,1,3,"대수","discoverRecursiveRule",200,"recursiveTermRule","앞 항에 같은 수를 곱하고 같은 수를 더하는 규칙을 찾는다."));
 public static boolean supports(String id){return SKILLS.stream().anyMatch(s->s.id.equals(id));}
 static Question create(Catalog.Skill s,Random r){
  boolean recursive=s.id.equals("discoverRecursiveRule");
  int m=recursive?2+r.nextInt(3):(1+r.nextInt(8))*(r.nextBoolean()?1:-1),b=r.nextInt(21)-10;
  int first=recursive?r.nextInt(21)-10:m+b;
  // A constant sequence would leave infinitely many affine recurrences compatible with the examples.
  if(recursive&&(m-1)*first+b==0)b++;
  long second=recursive?m*first+b:2*m+b,third=recursive?m*second+b:3*m+b,next=recursive?m*third+b:4*m+b;
  String data=recursive?"a_1 = "+first+", a_2 = "+second+", a_3 = "+third:"n: 1, 2, 3\na_n: "+first+", "+second+", "+third;
  String rule=recursive?"다음 항 = 앞 항 × M + B":"항의 값 = 항 번호 × M + B";
  String source=recursive?"앞 항":"항 번호";
  String prompt=data+"\n규칙은 ‘"+rule+"’이며 M과 B는 모든 항에서 같습니다.\n순서도의 M과 B를 채우고 a_4를 구하세요.\n시작 → "+source+" 입력 → × [M] → + [B] → 값 출력 → 끝";
  Question q=new Question(s.id,prompt,"",String.valueOf(m),String.valueOf(b),String.valueOf(next));
  q.labels=new String[]{"M","B","a_4"};q.stepSupport=false;
  long difference=second-first,nextDifference=third-second;
  StudyGuide g=new StudyGuide().transfer(false)
   .step("둘째 값에서 첫째 값을 빼세요.","("+second+") − ("+first+") = ","",String.valueOf(difference));
  if(recursive)g.step("셋째 값에서 둘째 값을 빼세요.","("+third+") − ("+second+") = ","",String.valueOf(nextDifference))
   .step("두 차의 비로 곱하는 수 M을 찾으세요.","("+nextDifference+") ÷ ("+difference+") = ","",String.valueOf(m));
  else g.step("항 번호가 1 늘 때 값의 변화가 M입니다.","M = ","",String.valueOf(m));
  long input=recursive?first:1,output=recursive?second:first;
  g.step("예시에서 곱한 부분을 빼서 더하는 수 B를 찾으세요.","("+output+") − ("+input+") × ("+m+") = ","",String.valueOf(b))
   .step("찾은 규칙에 다음 입력을 넣으세요.","("+(recursive?third:4)+") × ("+m+") + ("+b+") = ","",String.valueOf(next));
  q.studyGuide=g;return q;
 }
}
