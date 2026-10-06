package com.gomgomapps.math.core;
import java.util.*;

/** Synthetic population/area ratios; population is always a whole number. */
public final class PopulationDensity {
 private PopulationDensity(){}
 public static final List<Catalog.Skill> SKILLS=List.of(
  new Catalog.Skill("populationDensity","인구와 면적으로 인구밀도 구하기",9,1,4,"","populationDensity",100,"unitRate,decimalDiv","인구밀도는 1 km²당 인구수다. 인구를 면적으로 나눈다."),
  new Catalog.Skill("populationFromDensity","인구밀도와 면적으로 인구 구하기",9,1,4,"","populationFromDensity",100,"populationDensity,decimalMul","1 km²당 인구수에 면적을 곱하면 인구수가 된다."),
  new Catalog.Skill("areaFromPopulation","인구와 인구밀도로 면적 구하기",9,1,4,"","areaFromPopulation",100,"populationDensity,decimalDiv","인구를 1 km²당 인구수로 나누면 면적이 된다."));
 public static boolean supports(String id){return SKILLS.stream().anyMatch(s->s.id.equals(id));}
 static Question create(Catalog.Skill s,Random r){
  Rational area,density;int mode=r.nextInt(3);
  if(mode==0){area=Rational.of(1+r.nextInt(40));density=Rational.of(1+r.nextInt(500));}
  else if(mode==1){area=Rational.of(1+r.nextInt(80),2);density=Rational.of(2*(1+r.nextInt(250)));}
  else{area=Rational.of(10*(1+r.nextInt(4)));density=Rational.of(1+r.nextInt(4999),10);}
  Rational population=area.mul(density);if(!population.isInteger())throw new IllegalStateException("Fractional population");
  String a=area.decimalText(),d=density.decimalText(),p=population.toString();String given,formula,prompt,unit,expression;Rational answer,operand;
  switch(s.id){
   case "populationDensity"->{given="면적: "+a+" km² · 인구: "+p+"명";formula="인구밀도 = 인구 ÷ 면적";prompt="인구밀도는 몇 명/km²인가요?";unit="명/km²";answer=density;operand=area;expression=p+"/("+a+")";}
   case "populationFromDensity"->{given="면적: "+a+" km² · 인구밀도: "+d+"명/km²";formula="인구 = 인구밀도 × 면적";prompt="인구는 몇 명인가요?";unit="명";answer=population;operand=area;expression="("+d+")*("+a+")";}
   default->{given="인구: "+p+"명 · 인구밀도: "+d+"명/km²";formula="면적 = 인구 ÷ 인구밀도";prompt="면적은 몇 km²인가요?";unit="km²";answer=area;operand=density;expression=p+"/("+d+")";}
  }
  Question q=new Question(s.id,"인구밀도\n"+given+"\n"+formula+"\n"+prompt,expression,answer.toString());q.labels=new String[]{unit};q.decimal=true;q.stepSupport=false;
  StudyGuide help=new StudyGuide().transfer(false);
  if(s.id.equals("populationFromDensity"))help.step("곱할 면적을 쓰세요.","면적 = "," km²",operand.toString()).step("인구밀도에 면적을 곱하세요.",d+" × "+a+" = "," 명",p);
  else if(s.id.equals("populationDensity"))help.step("나눌 면적을 쓰세요.","면적 = "," km²",operand.toString()).step("인구를 면적으로 나누세요.",p+" ÷ "+a+" = "," 명/km²",answer.toString());
  else help.step("나눌 인구밀도를 쓰세요.","인구밀도 = "," 명/km²",operand.toString()).step("인구를 인구밀도로 나누세요.",p+" ÷ "+d+" = "," km²",answer.toString());
  q.studyGuide=help;return q.withInputs(population,area,density);
 }
 static Map<Rational,String> errors(Question q){Rational p=q.choiceInputs[0],a=q.choiceInputs[1],d=q.choiceInputs[2],answer=Expression.number(q.answers[0]);Map<Rational,String> out=new LinkedHashMap<>();
  if(q.skillId.equals("populationDensity")){out.put(p.mul(a),"나눗셈 대신 곱셈");out.put(a.div(p),"나눗셈 순서를 바꿈");out.put(p,"면적으로 나누지 않음");}
  else if(q.skillId.equals("populationFromDensity")){out.put(d.div(a),"곱셈 대신 나눗셈");out.put(d,"면적을 곱하지 않음");out.put(d.add(a),"곱셈 대신 덧셈");}
  else{out.put(p.mul(d),"나눗셈 대신 곱셈");out.put(d.div(p),"나눗셈 순서를 바꿈");out.put(p,"인구밀도로 나누지 않음");}
  for(Rational delta:List.of(Rational.ONE,Rational.of(-1),Rational.of(2),Rational.of(-2),Rational.of(1,10),Rational.of(-1,10)))out.put(answer.add(delta),"마지막 계산 오류");return out;
 }
}
