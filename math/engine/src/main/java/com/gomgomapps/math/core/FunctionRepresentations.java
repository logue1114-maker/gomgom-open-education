package com.gomgomapps.math.core;
import java.util.*;

/** Three views of a linear relationship; diagrams contain public givens only. */
public final class FunctionRepresentations {
 private FunctionRepresentations(){}
 public static final List<Catalog.Skill> SKILLS=List.of(
  new Catalog.Skill("functionTableOutput","함수 표의 빈칸 채우기",9,1,3,"","functionTableOutput",100,"substitute,signedAdd","함수식에 표의 x값을 넣어 대응하는 y값을 구한다."),
  new Catalog.Skill("functionTableRule","표에서 함수식 만들기",9,1,3,"","functionTableRule",100,"linearSlope,substitute","표에서 x와 y의 변화량을 비교해 a를 구하고 한 쌍의 값으로 b를 구한다."),
  new Catalog.Skill("functionGraphOutput","그래프에서 함숫값 읽기",9,1,3,"","functionGraphOutput",100,"linearValue,linearSlope","가로축의 x값에 대응하는 직선 위 점의 y값을 읽는다."));
 public static boolean supports(String id){return SKILLS.stream().anyMatch(s->s.id.equals(id));}
 static Map<Rational,String> errors(Question q){
  long a=q.choiceInputs[0].n.longValueExact(),b=q.choiceInputs[1].n.longValueExact(),x=q.choiceInputs[2].n.longValueExact(),y=a*x+b;Map<Rational,String> result=new LinkedHashMap<>();
  result.put(Rational.of(a*x),"상수를 빠뜨림");result.put(Rational.of(b*x+a),"기울기와 상수를 바꿈");result.put(Rational.of(-a*x+b),"x의 부호를 바꿈");result.put(Rational.of(b),"x가 0일 때의 y값을 사용함");
  for(int step:new int[]{-2,-1,1,2})if(!q.skillId.equals("functionGraphOutput")||Math.abs(y+step)<=10)result.put(Rational.of(y+step),q.skillId.equals("functionGraphOutput")?"y축 눈금을 다르게 읽음":"마지막 덧셈 계산 오류");return result;
 }
 static Question create(Catalog.Skill s,Random r){
  boolean graph=s.id.equals("functionGraphOutput"),rule=s.id.equals("functionTableRule");int a=(1+r.nextInt(graph?2:4))*(r.nextBoolean()?1:-1),b=r.nextInt(graph?7:13)-(graph?3:6),x1=graph?-2:-1-r.nextInt(4),x2=graph?2:1+r.nextInt(4),target=-3+r.nextInt(7),y1=a*x1+b,y2=a*x2+b,y=a*target+b;
  String formula="y = "+a+"x "+(b<0?"- "+(-b):"+ "+b);
  String prompt=rule?"표의 세 점은 함수 y = ax + b 위에 있습니다.\na와 b를 구하세요.":graph?"그래프는 함수 y = ax + b를 나타냅니다.\nx = "+target+"일 때 y를 구하세요.":"함수식: "+formula+"\n표에서 x = "+target+"에 대응하는 y를 구하세요.";
  Question q=new Question(s.id,prompt,rule?""+a:"("+a+")*("+target+")+("+b+")",rule?new String[]{""+a,""+b}:new String[]{""+y});q.stepSupport=false;
  if(rule){q.kind="pair";q.labels=new String[]{"기울기 a","상수 b"};q.diagram=new StudyDiagram("functionRuleTable",new double[]{x1,y1,0,b,x2,y2});}
  else {q.labels=new String[]{"y값"};q.diagram=new StudyDiagram(graph?"functionGraph":"functionValueTable",new double[]{x1,y1,x2,y2,target});}
  StudyGuide help=new StudyGuide().transfer(false);
  if(!rule&&!graph){help.step("x값에 기울기를 곱하세요.",a+" × ("+target+") = ","",""+(a*target)).step("곱한 값에 상수를 더하세요.","("+(a*target)+") + ("+b+") = ","",""+y);}
  else {int dy=y2-y1,dx=x2-x1;help.step("두 점의 y값 차이를 구하세요.","("+y2+") - ("+y1+") = ","",""+dy).step("y값 차이를 x값 차이로 나누세요.",dy+" ÷ ("+x2+" - ("+x1+")) = ","",""+a);
   if(rule)help.step("기울기에 한 점의 x값을 곱하세요.",a+" × ("+x1+") = ","",""+(a*x1)).step("그 점의 y값에서 곱한 값을 빼세요.","("+y1+") - ("+(a*x1)+") = ","",""+b);
   else help.step("주어진 x와 첫 점의 x값 차이를 구하세요.","("+target+") - ("+x1+") = ","",""+(target-x1)).step("첫 점의 y값에 변화량을 더하세요.","("+y1+") + ("+a+") × ("+(target-x1)+") = ","",""+y);
  }
  q.studyGuide=help;return q.withInputs(a,b,target);
 }
}
