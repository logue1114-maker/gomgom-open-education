package com.gomgomapps.math.core;
import java.util.*;

/** Successive price changes use the updated amount as the next base. */
public final class SuccessivePercent {
 private SuccessivePercent(){}
 public static final List<Catalog.Skill> SKILLS=List.of(
  new Catalog.Skill("successivePercentPrice","두 번 바뀐 가격 구하기",9,1,2,"","successivePercentPrice",100,"percent,decimalMul","첫 변화 뒤의 가격을 구한 다음 그 가격을 기준으로 둘째 변화를 계산한다."),
  new Catalog.Skill("successivePercentRate","두 번 바뀐 가격의 변화율 구하기",9,1,2,"","successivePercentRate",100,"percent,decimalMul,decimalDiv","최종 가격과 처음 가격의 차이를 처음 가격으로 나누고 100을 곱한다. 인상은 양수, 인하는 음수이다."));
 public static boolean supports(String id){return SKILLS.stream().anyMatch(s->s.id.equals(id));}
 static Question create(Catalog.Skill s,Random r){
  int start=(1+r.nextInt(99))*100,p=(1+r.nextInt(8))*5,q=(1+r.nextInt(8))*5;
  int first=r.nextBoolean()?1:-1,second=r.nextBoolean()?1:-1;
  Rational initial=Rational.of(start),factor1=Rational.of(100+first*p,100),factor2=Rational.of(100+second*q,100);
  Rational middle=initial.mul(factor1),end=middle.mul(factor2),difference=end.sub(initial),rate=difference.div(initial).mul(Rational.of(100));boolean net=s.id.equals("successivePercentRate");
  String context="처음 가격: "+start+"\n첫째 변화: "+p+"% "+(first>0?"인상":"인하")+"\n둘째 변화: "+q+"% "+(second>0?"인상":"인하");
  String instruction=net?"최종 가격과 전체 변화율을 구하세요.\n변화율은 인상이면 양수, 인하면 음수로 쓰세요.":"최종 가격을 구하세요.";
  Question question=new Question(s.id,context+"\n"+instruction,start+"*("+factor1+")*("+factor2+")",net?new String[]{end.decimalText(),rate.decimalText()}:new String[]{end.decimalText()});
  question.decimal=true;question.stepSupport=false;if(net)question.kind="pair";question.labels=net?new String[]{"최종 가격","변화율(%)"}:new String[]{"최종 가격"};
  StudyGuide help=new StudyGuide().transfer(false)
   .step("첫 변화 뒤의 비율을 소수로 쓰세요.","(100 "+(first>0?"+":"-")+" "+p+") ÷ 100 = ","",factor1.decimalText())
   .step("처음 가격에 첫 비율을 곱하세요.",start+" × "+factor1.decimalText()+" = ","",middle.decimalText())
   .step("둘째 변화 뒤의 비율을 소수로 쓰세요.","(100 "+(second>0?"+":"-")+" "+q+") ÷ 100 = ","",factor2.decimalText())
   .step("첫 변화 뒤의 가격에 둘째 비율을 곱하세요.",middle.decimalText()+" × "+factor2.decimalText()+" = ","",end.decimalText());
  if(net)help.step("최종 가격에서 처음 가격을 빼세요.",end.decimalText()+" - "+start+" = ","",difference.decimalText())
   .step("가격 차이를 처음 가격으로 나누고 백분율로 바꾸세요.","("+difference.decimalText()+") ÷ "+start+" × 100 = "," %",rate.decimalText());
  question.studyGuide=help;return question.withInputs(initial,Rational.of(first*p),Rational.of(second*q));
 }
 static Map<Rational,String> errors(Question q){
  Rational initial=q.choiceInputs[0],p=q.choiceInputs[1],s=q.choiceInputs[2],hundred=Rational.of(100);Map<Rational,String> errors=new LinkedHashMap<>();
  errors.put(initial.mul(Rational.ONE.add(p.add(s).div(hundred))),"처음 가격을 두 변화의 공통 기준으로 사용함");
  errors.put(initial.mul(Rational.ONE.add(p.div(hundred))),"둘째 변화를 빠뜨림");
  errors.put(initial.mul(Rational.ONE.add(s.div(hundred))),"첫째 변화를 빠뜨림");
  errors.put(initial,"두 변화를 계산하지 않음");return errors;
 }
}
