package com.gomgomapps.math.core;
import java.util.*;

/** Positive whole-number inverse operations and preservation of both sides of an equality. */
public final class EqualityFoundations {
 private EqualityFoundations(){}
 public static final List<Catalog.Skill> SKILLS=List.of(
  new Catalog.Skill("el_missing_mul","□가 있는 곱셈",4,1,3,"","missingMul",240,"tables,divide","곱을 알려진 수로 나누어 빈 수를 구한다."),
  new Catalog.Skill("el_missing_div","□가 있는 나눗셈",4,1,3,"","missingDiv",240,"tables,divide","곱셈과 나눗셈의 관계로 빈 수를 구한다."),
  new Catalog.Skill("el_equality_add_sub","등식 양쪽에 더하기와 빼기",4,1,3,"","equalityAddSub",200,"add100,sub100","등식 양쪽에 같은 수를 더하거나 빼면 등식이 유지된다."),
  new Catalog.Skill("el_equality_mul_div","등식 양쪽에 곱하기와 나누기",5,1,3,"","equalityMulDiv",200,"tables,divide","등식 양쪽에 같은 수를 곱하거나 0이 아닌 같은 수로 나누면 등식이 유지된다."));
 public static boolean supports(String id){return SKILLS.stream().anyMatch(s->s.id.equals(id));}
 static boolean selected(String id){return supports(id)||id.equals("el_missing_add")||id.equals("el_missing_sub");}
 static void choices(Question q,Random random,CurriculumLimits limits){
  int answer=Integer.parseInt(q.answers[0]);List<Integer> candidates=new ArrayList<>();
  for(int delta=1;delta<=32;delta++)for(int value:new int[]{answer-delta,answer+delta})if(value>=0&&limits.allowsChoice(String.valueOf(value)))candidates.add(value);
  Collections.shuffle(candidates,random);if(candidates.size()<3)return;List<Integer> options=new ArrayList<>(candidates.subList(0,3));options.add(answer);Collections.shuffle(options,random);
  for(int value:options){if(value==answer)q.correctChoice=q.choices.size();q.choices.add(String.valueOf(value));q.distractorReasons.add(value==answer?"정답":"양쪽 값과 역연산 확인");}
 }
 static Question create(Catalog.Skill skill,Random random){
  if(skill.id.equals("el_missing_mul")||skill.id.equals("el_missing_div")){
   int a=1+random.nextInt(20),b=2+random.nextInt(11),product=a*b;boolean first=random.nextBoolean();String prompt,expression,instruction;int answer;
   if(skill.id.equals("el_missing_mul")){answer=first?a:b;int known=first?b:a;prompt=(first?"□ × "+b:a+" × □")+" = "+product;expression=product+" ÷ "+known;instruction="곱을 알려진 수로 나누세요.";}
   else if(first){answer=product;prompt="□ ÷ "+b+" = "+a;expression=a+" × "+b;instruction="몫과 나누는 수를 곱하세요.";}
   else{answer=b;prompt=product+" ÷ □ = "+a;expression=product+" ÷ "+a;instruction="나누어지는 수를 몫으로 나누세요.";}
   return scalar(skill,prompt,expression,answer,new StudyGuide().transfer(false).step(instruction,expression+" = ","",String.valueOf(answer)));
  }
  boolean mulDiv=skill.id.equals("el_equality_mul_div"),second=random.nextBoolean();int change=mulDiv?2+random.nextInt(11):1+random.nextInt(30);
  int base=mulDiv&&second?change*(2+random.nextInt(14)):40+random.nextInt(121);
  int a=1+random.nextInt(base-1),b=base-a,c=1+random.nextInt(base-1),d=base-c;
  if(a==c){c=c==base-1?1:c+1;d=base-c;}
  String op=mulDiv?(second?"÷":"×"):(second?"−":"+");int transformed=switch(op){case "+"->base+change;case "−"->base-change;case "×"->base*change;default->base/change;};
  String inverse=switch(op){case "+"->transformed+" − "+base;case "−"->base+" − "+transformed;case "×"->transformed+" ÷ "+base;default->base+" ÷ "+transformed;};
  String prompt=a+" + "+b+" = "+c+" + "+d+"\n("+a+" + "+b+") "+op+" "+change+" = ("+c+" + "+d+") "+op+" □";
  StudyGuide guide=new StudyGuide().transfer(false)
   .step("원래 등식의 왼쪽을 계산하세요.",a+" + "+b+" = ","",String.valueOf(base))
   .step("원래 등식의 오른쪽을 계산하세요.",c+" + "+d+" = ","",String.valueOf(base))
   .step("왼쪽에 주어진 계산을 하세요.",base+" "+op+" "+change+" = ","",String.valueOf(transformed))
   .step("오른쪽도 같은 값이 되도록 빈 수를 구하세요.",inverse+" = ","",String.valueOf(change));
  return scalar(skill,prompt,inverse,change,guide);
 }
 private static Question scalar(Catalog.Skill skill,String prompt,String expression,int answer,StudyGuide guide){Question q=new Question(skill.id,prompt,expression,String.valueOf(answer));q.studyGuide=guide;q.stepSupport=false;return q;}
}
