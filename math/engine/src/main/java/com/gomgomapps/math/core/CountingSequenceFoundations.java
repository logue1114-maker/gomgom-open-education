package com.gomgomapps.math.core;
import java.util.*;

/** Independent choices, direct term rules and recursive term calculations. */
public final class CountingSequenceFoundations {
 private CountingSequenceFoundations(){}
 public static final List<Catalog.Skill> SKILLS=List.of(
  new Catalog.Skill("choiceProduct","곱셈 원리로 경우의 수 구하기",8,2,4,"","choiceProduct",4096,"tables","각 단계의 선택이 다른 단계의 선택을 제한하지 않으면 단계별 경우의 수를 곱한다."),
  new Catalog.Skill("directTermRule","항 번호로 수열의 값 구하기",11,1,3,"대수","directTermRule",200,"substitute,signedMul","항 번호 n을 식에 넣어 그 항의 값을 직접 구한다."),
  new Catalog.Skill("recursiveTermRule","이전 항으로 다음 항 구하기",11,1,3,"대수","recursiveTermRule",2000,"signedMul,signedAdd","첫째항에서 시작해 주어진 규칙을 한 번씩 적용하면 다음 항을 구할 수 있다."));
 public static boolean supports(String id){return SKILLS.stream().anyMatch(s->s.id.equals(id));}
 static Question create(Catalog.Skill s,Random r){return switch(s.id){case "choiceProduct"->choices(s,r);case "directTermRule"->direct(s,r);case "recursiveTermRule"->recursive(s,r);default->throw new IllegalArgumentException(s.id);};}
 private static Question choices(Catalog.Skill s,Random r){
  int a=2+r.nextInt(15),b=2+r.nextInt(15),c=r.nextBoolean()?0:2+r.nextInt(7);long ab=a*b,answer=c==0?ab:ab*c;
  String prompt="첫 단계에서 "+a+"가지, 둘째 단계에서 "+b+"가지"+(c==0?"":", 셋째 단계에서 "+c+"가지");
  prompt=prompt+" 중 각각 하나를 고릅니다.\n어떤 선택을 해도 다른 단계의 선택은 제한되지 않습니다.\n서로 다른 선택 결과는 모두 몇 가지인가요?";
  Question q=new Question(s.id,prompt,a+"*"+b+(c==0?"":"*"+c),String.valueOf(answer));q.stepSupport=false;
  q.studyGuide=new StudyGuide().transfer(false).step("첫 두 단계의 경우의 수를 곱하세요.",a+" × "+b+" = ","",String.valueOf(ab));
  if(c!=0)q.studyGuide.step("앞의 결과에 셋째 단계의 경우의 수를 곱하세요.",ab+" × "+c+" = ","",String.valueOf(answer));return q.withInputs(a,b,c);
 }
 private static Question direct(Catalog.Skill s,Random r){
  int p=(1+r.nextInt(8))*(r.nextBoolean()?1:-1),b=r.nextInt(25)-12,n=1+r.nextInt(20),product=p*n,answer=product+b;
  Question q=new Question(s.id,"a_n = ("+p+") × n + ("+b+")\nn = "+n+"일 때 a_n의 값을 구하세요.",p+"*"+n+"+("+b+")",String.valueOf(answer));q.stepSupport=false;
  q.studyGuide=new StudyGuide().transfer(false).step("항 번호를 넣고 먼저 곱하세요.","("+p+") × "+n+" = ","",String.valueOf(product)).step("곱한 값에 상수를 더하세요.","("+product+") + ("+b+") = ","",String.valueOf(answer));return q.withInputs(p,b,n);
 }
 private static Question recursive(Catalog.Skill s,Random r){
  int first=r.nextInt(21)-10,m=1+r.nextInt(3),b=r.nextInt(9)-4,n=2+r.nextInt(4);if(m==1&&b==0)b=1;
  long value=first;String expression=String.valueOf(first);StudyGuide guide=new StudyGuide().transfer(false);
  for(int i=2;i<=n;i++){long next=m*value+b;guide.step("앞에서 구한 항에 규칙을 한 번 적용하세요.","a_"+i+" = "+m+" × ("+value+") + ("+b+") = ","",String.valueOf(next));expression="("+m+"*("+expression+")+("+b+"))";value=next;}
  Question q=new Question(s.id,"a_1 = "+first+"\na_(n+1) = "+m+" × a_n + ("+b+")\na_"+n+"의 값을 구하세요.",expression,String.valueOf(value));q.stepSupport=false;q.studyGuide=guide;return q.withInputs(first,m,b,n);
 }
 static Map<Rational,String> errors(Question q){
  Map<Rational,String> result=new LinkedHashMap<>();long a=q.choiceInputs[0].n.longValueExact(),b=q.choiceInputs[1].n.longValueExact(),c=q.choiceInputs[2].n.longValueExact();Rational answer=Expression.number(q.answers[0]);
  if(q.skillId.equals("choiceProduct")){result.put(Rational.of(a+b+c),"단계별 경우의 수를 곱하지 않고 더함");result.put(Rational.of(a*b),"셋째 단계의 선택을 빠뜨림");result.put(Rational.of(a),"첫 단계만 셈");}
  else if(q.skillId.equals("directTermRule")){result.put(Rational.of(a*c),"상수를 더하지 않음");result.put(Rational.of(a*(c-1)+b),"항 번호를 하나 작게 넣음");result.put(Rational.of(a+b),"항 번호를 넣지 않음");}
  else {long value=a;int n=q.choiceInputs[3].n.intValueExact();for(int i=2;i<n;i++)value=b*value+c;result.put(Rational.of(value),"바로 앞 항에서 계산을 멈춤");result.put(Rational.of(b*a+c),"규칙을 한 번만 적용함");result.put(Rational.of(b*answer.n.longValueExact()+c),"규칙을 한 번 더 적용함");}
  for(int i=1;i<=4;i++){result.put(answer.add(Rational.of(i)),"규칙 계산 오류");if(!q.skillId.equals("choiceProduct")||answer.n.longValueExact()>i)result.put(answer.sub(Rational.of(i)),"규칙 계산 오류");}result.remove(answer);return result;
 }
}
