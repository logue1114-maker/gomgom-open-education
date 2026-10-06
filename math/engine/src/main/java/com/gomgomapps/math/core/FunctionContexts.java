package com.gomgomapps.math.core;
import java.util.*;

/** Contextual constant-rate growth distinguishes initial amount from change per input. */
public final class FunctionContexts {
 private FunctionContexts(){}
 public static final List<Catalog.Skill> SKILLS=List.of(
  new Catalog.Skill("functionContextRule","상황에서 함수식 만들기",9,1,3,"","functionContextRule",100,"functionTableRule","y=ax+b에서 a는 x가 1 늘 때 증가하는 양이고 b는 처음 양이다."),
  new Catalog.Skill("functionContextOutput","상황에서 함숫값 구하기",9,1,3,"","functionContextOutput",100,"functionContextRule,linearValue","처음 양에 한 번에 늘어나는 양과 횟수의 곱을 더한다. x가 0이면 처음 양만 남는다."));
 public static boolean supports(String id){return SKILLS.stream().anyMatch(s->s.id.equals(id));}
 static Question create(Catalog.Skill s,Random r){
  boolean rule=s.id.equals("functionContextRule");int a=1+r.nextInt(12),b=r.nextInt(31),x=r.nextInt(13),context=r.nextInt(3),product=a*x,y=product+b;
  String given=switch(context){
   case 0->"생활 속 함수 · 물통\n처음 물의 양은 "+b+" L입니다.\n1분마다 "+a+" L씩 넣습니다.\nx는 지난 시간(분), y는 물의 양(L)입니다.";
   case 1->"생활 속 함수 · 블록\n처음 블록은 "+b+"개입니다.\n1번 넣을 때마다 "+a+"개씩 추가합니다.\nx는 넣은 횟수, y는 블록의 수입니다.";
   default->"생활 속 함수 · 책\n처음 책은 "+b+"권입니다.\n1번 정리할 때마다 "+a+"권씩 추가합니다.\nx는 정리한 횟수, y는 책의 수입니다.";
  };
  String prompt=given+"\n"+(rule?"y = ax + b에서 a와 b를 구하세요.":"x = "+x+"일 때 y를 구하세요.");
  Question q=new Question(s.id,prompt,rule?""+a:a+"*"+x+"+"+b,rule?new String[]{""+a,""+b}:new String[]{""+y});q.stepSupport=false;
  q.labels=rule?new String[]{"기울기 a","상수 b"}:new String[]{"y값"};if(rule)q.kind="pair";
  StudyGuide help=new StudyGuide().transfer(false);
  if(rule)help.step("x가 1 늘어날 때 y가 늘어나는 양을 쓰세요.","a = ","",""+a).step("x가 0일 때의 y값을 쓰세요.","b = ","",""+b);
  else help.step("매번 늘어나는 양에 x값을 곱하세요.",a+" × "+x+" = ","",""+product).step("처음 양에 늘어난 양을 더하세요.",b+" + "+product+" = ","",""+y);
  q.studyGuide=help;return q.withInputs(a,b,x);
 }
 static Map<Rational,String> errors(Question q){long a=q.choiceInputs[0].n.longValueExact(),b=q.choiceInputs[1].n.longValueExact(),x=q.choiceInputs[2].n.longValueExact(),y=a*x+b;Map<Rational,String> out=new LinkedHashMap<>();out.put(Rational.of(a*x),"처음 양을 빠뜨림");out.put(Rational.of(b*x+a),"처음 양과 증가량을 바꿈");out.put(Rational.of(a+b),"횟수를 1로 계산함");out.put(Rational.of(b),"증가량을 더하지 않음");for(int delta:new int[]{-2,-1,1,2})if(y+delta>=0)out.put(Rational.of(y+delta),"마지막 덧셈 계산 오류");return out;}
}
