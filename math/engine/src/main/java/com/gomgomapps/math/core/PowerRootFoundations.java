package com.gomgomapps.math.core;
import java.util.*;

/** Numerical power/root relations and pure square equations, with learner-filled frames. */
public final class PowerRootFoundations {
 private PowerRootFoundations(){}
 public static final List<Catalog.Skill> SKILLS=List.of(
  new Catalog.Skill("integerPowerValue","정수 지수의 거듭제곱",11,1,1,"대수","integerPowerValue",200,"signedMul,rational","0이 아닌 수의 0제곱은 1이다. 음의 지수는 양의 거듭제곱의 역수이며 음수인 밑은 괄호로 묶는다."),
  new Catalog.Skill("rootPowerLink","근과 분수 지수",11,1,1,"대수","rootPowerLink",136,"powerLaw","양수의 m/n제곱은 n제곱근을 구한 뒤 m제곱한 값과 같다."),
  new Catalog.Skill("pureSquareEquation","ax²=b 형태의 방정식",9,1,2,"","pureSquareEquation",400,"signedMul,linear","a가 0이 아닐 때 양변을 a로 나눈다. x²가 양수이면 서로 부호가 반대인 두 해, 0이면 해 0을 갖는다."));
 public static boolean supports(String id){return SKILLS.stream().anyMatch(s->s.id.equals(id));}
 static Map<Rational,String> errors(Question q){
  Map<Rational,String> errors=new LinkedHashMap<>();Rational answer=Expression.number(q.answers[0]);long base=q.choiceInputs[0].n.longValueExact();int exponent=q.choiceInputs[1].n.intValueExact();
  if(q.skillId.equals("integerPowerValue")){errors.put(Rational.of(base*exponent),"거듭제곱을 밑과 지수의 곱으로 계산함");errors.put(Rational.of(base).pow(Math.abs(exponent)),"음의 지수에서 역수를 빠뜨림");errors.put(answer.neg(),"밑 또는 계산 결과의 부호 오류");errors.put(Rational.of(base),"거듭제곱하지 않고 밑을 씀");}
  else {int degree=q.choiceInputs[2].n.intValueExact();errors.put(Rational.of(base*exponent),"근의 거듭제곱을 곱셈으로 계산함");errors.put(Rational.of(base).pow(degree),"분수 지수 대신 근호 안의 수를 씀");errors.put(Rational.of(base),"근을 구한 뒤 거듭제곱을 빠뜨림");}
  Rational step=Rational.of(1,answer.d.longValueExact());for(int i=1;i<=4;i++){errors.put(answer.add(step.mul(Rational.of(i))),"거듭제곱 계산 오류");errors.put(answer.sub(step.mul(Rational.of(i))),"거듭제곱 계산 오류");}errors.remove(answer);return errors;
 }
 static Question create(Catalog.Skill s,Random r){return switch(s.id){case "integerPowerValue"->integer(s,r);case "rootPowerLink"->root(s,r);case "pureSquareEquation"->square(s,r);default->throw new IllegalArgumentException(s.id);};}
 private static Question integer(Catalog.Skill s,Random r){
  int base=2+r.nextInt(11);if(r.nextBoolean())base=-base;int exponent=r.nextInt(11)-5;Rational answer=Rational.of(base).pow(exponent);
  String literal="("+base+")^("+exponent+")";StudyGuide guide=new StudyGuide().transfer(false);
  if(exponent==0)guide.step("0이 아닌 밑의 0제곱 규칙으로 값을 쓰세요.",literal+" = ","","1");
  else {int count=Math.abs(exponent);StringJoiner repeated=new StringJoiner(" × ");for(int i=0;i<count;i++)repeated.add("("+base+")");Rational power=Rational.of(base).pow(count);guide.step("지수의 절댓값만큼 밑을 곱하세요.",repeated+" = ","",power.toString());if(exponent<0)guide.step("음의 지수이므로 계산한 거듭제곱의 역수를 쓰세요.","1 ÷ ("+power+") = ","",answer.toString());}
  Question q=new Question(s.id,literal+"\n값을 구하세요.",literal,answer.toString());q.studyGuide=guide;q.stepSupport=false;if(exponent<0)q.answerFormat="fraction";return q.withInputs(base,exponent);
 }
 private static Question root(Catalog.Skill s,Random r){
  int base=2+r.nextInt(17),degree=2+r.nextInt(2),numerator=1+r.nextInt(4);long given=Rational.of(base).pow(degree).n.longValueExact();String root=(degree==2?"√":"∛")+given;Rational answer=Rational.of(base).pow(numerator);
  String literal=given+"^("+numerator+"/"+degree+") = ("+root+")^"+numerator;
  Question q=new Question(s.id,literal+"\n값을 구하세요.","("+base+")^"+numerator,answer.toString());q.stepSupport=false;
  q.studyGuide=new StudyGuide().transfer(false).step("근호의 차수에 맞는 양의 근을 구하세요.",root+" = ","",String.valueOf(base)).step("구한 근을 분자의 지수만큼 거듭제곱하세요.","("+base+")^"+numerator+" = ","",answer.toString());return q.withInputs(base,numerator,degree);
 }
 private static Question square(Catalog.Skill s,Random r){
  int coefficient=1+r.nextInt(12);if(r.nextBoolean())coefficient=-coefficient;int root=r.nextInt(8)==0?0:1+r.nextInt(25),right=coefficient*root*root;
  String[] answers=root==0?new String[]{"0"}:new String[]{String.valueOf(-root),String.valueOf(root)};
  Question q=new Question(s.id,coefficient+"x² = "+right+"\nx의 실수 해를 모두 구하세요.",coefficient+"*x^2 = "+right,answers);q.kind="roots";q.stepSupport=false;q.labels=root==0?new String[]{"해"}:new String[]{"해 1","해 2"};
  q.studyGuide=new StudyGuide().transfer(false).step("양변을 x²의 계수로 나누세요.",right+" ÷ ("+coefficient+") = ","",String.valueOf(root*root)).step("제곱해서 이 값이 되는 0 이상의 수를 구하세요.","√"+(root*root)+" = ","",String.valueOf(root));
  if(root>0)q.studyGuide.step("반대 부호의 해도 구하세요.","−("+root+") = ","",String.valueOf(-root));return q;
 }
}
