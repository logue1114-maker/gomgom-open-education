package com.gomgomapps.math.core;
import java.util.*;

/** Strict bounds for positive irrational roots, proved by integer square comparisons. */
public final class RealRootBounds {
 private RealRootBounds(){}
 public static final List<Catalog.Skill> SKILLS=List.of(
  new Catalog.Skill("rootIntegerBounds","제곱근이 있는 정수 구간",9,1,1,"","rootIntegerBounds",1000,"squareWhole","제곱수가 아닌 자연수의 제곱근은 무리수다. 이웃한 두 정수의 제곱과 비교해 제곱근의 위치를 찾는다."),
  new Catalog.Skill("rootTenthBounds","제곱근이 있는 소수 구간",9,1,1,"","rootTenthBounds",1000,"rootIntegerBounds,decimalMul","무리수의 소수는 끝나지 않고 일정하게 반복되지 않는다. 근호 안의 수를 100배하면 제곱근은 10배가 된다. 정수 구간을 찾고 10으로 나눠 소수 첫째 자리의 구간을 구한다."));
 public static boolean supports(String id){return SKILLS.stream().anyMatch(s->s.id.equals(id));}
 static Question create(Catalog.Skill skill,Random random){
  int integer=1+random.nextInt(25),n=integer*integer+1+random.nextInt(2*integer);boolean decimal=skill.id.equals("rootTenthBounds");int scale=decimal?10:1,scaled=n*scale*scale,lower=0;
  while((lower+1)*(lower+1)<scaled)lower++;
  String left=decimal?tenth(lower):""+lower,right=decimal?tenth(lower+1):""+(lower+1);
  Question q=new Question(skill.id,(decimal?"소수 첫째 자리에서 이웃한 두 수를 쓰세요.":"서로 이웃한 두 정수를 쓰세요.")+"\n□ < √"+n+" < □","",left,right);
  q.labels=new String[]{"왼쪽 수","오른쪽 수"};q.decimal=decimal;q.answerFormat=decimal?"decimal":"";q.stepSupport=false;
  StudyGuide guide=new StudyGuide().transfer(false);
  if(decimal)guide.step("주어진 수에 100을 곱하세요.",n+" × 100 = ","",""+scaled);
  guide.step("제곱이 주어진 수보다 작은 가장 큰 정수를 찾으세요.","□ × □ < "+scaled+" · □ = ","",""+lower)
   .step("찾은 정수의 제곱을 계산하세요.",lower+" × "+lower+" = ","",""+(lower*lower));
  if(decimal)guide.step("찾은 정수를 10으로 나눠 왼쪽 수를 구하세요.",lower+" ÷ 10 = ","",left)
   .step("찾은 정수에 1을 더한 뒤 10으로 나눠 오른쪽 수를 구하세요.","("+lower+" + 1) ÷ 10 = ","",right);
  else guide.step("찾은 정수에 1을 더하세요.",lower+" + 1 = ","",right)
   .step("다음 정수의 제곱을 계산하세요.",(lower+1)+" × "+(lower+1)+" = ","",""+((lower+1)*(lower+1)));
  q.studyGuide=guide;return q;
 }
 private static String tenth(int scaled){return (scaled/10)+"."+(scaled%10);}
}
