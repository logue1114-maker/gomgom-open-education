package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
/** Public equations determine the relation; saved answers never determine help. */
public final class ConditionRelations {
 private ConditionRelations(){}
 public static void attach(Question q){
  if(q==null||!"sec_sufficient_condition".equals(q.skillId)||q.prompt==null)return;
  Matcher m=Pattern.compile("(?:실수 x에 대해 )?p: (.+), q: (.+)일 때 p가 q(?:의 (필요|충분)조건인지|이기 위한 충분조건인지) 고르세요\\.").matcher(q.prompt);if(!m.matches())return;
  boolean necessary="필요".equals(m.group(3));String p=m.group(1),r=m.group(2);boolean linearP=p.startsWith("x=");String linear=linearP?p:r,square=linearP?r:p;
  Matcher l=Pattern.compile("x=(-?\\d+)").matcher(linear),s=Pattern.compile("(?:x|\\(x-\\((-?\\d+)\\)\\))²=(\\d+)").matcher(square);if(!l.matches()||!s.matches())return;
  long c=Long.parseLong(l.group(1)),h=s.group(1)==null?0:Long.parseLong(s.group(1)),v=Long.parseLong(s.group(2)),delta=c-h,calculated=delta*delta,other=h-delta;
  boolean forward=!necessary,linearSource=forward==linearP,valid=linearSource?calculated==v:calculated==v&&other==c;
  StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="condition-relations-v1";
  Map<String,String> direction=new LinkedHashMap<>();direction.put("1","p → q");direction.put("0","q → p");
  g.choice(necessary?"p가 q의 필요조건이면 q → p가 항상 성립해야 합니다. 확인할 방향을 고르세요.":"p가 q의 충분조건이면 p → q가 항상 성립해야 합니다. 확인할 방향을 고르세요.",direction,forward?"1":"0");
  step(g,"제곱식의 중심 h를 쓰세요. x²이면 h는 0입니다.","중심 h = ",h);
  step(g,"x=값 조건의 값을 찾아 쓰세요.","조건의 값 c = ",c);
  step(g,"제곱식의 오른쪽 값을 찾아 쓰세요.","제곱식의 값 s = ",v);
  step(g,"조건의 값에서 중심을 빼세요.","차 d = c − h = ",delta);
  step(g,"구한 차를 제곱하여 제곱식의 값과 비교하세요.","대입한 값 t = d × d = ",calculated);
  step(g,"중심에서 구한 차를 빼서 반대쪽 해를 구하세요.","반대쪽 해 z = h − d = ",other);
  Map<String,String> result=new LinkedHashMap<>();String label=necessary?"필요조건":"충분조건";result.put("1",label+"이다");result.put("0",label+"이 아니다");
  g.choice("확인할 방향이 x=값에서 제곱식으로 향하면 대입한 값과 제곱식의 값을 비교하세요. 반대 방향이면 두 해가 모두 x=값을 만족하는지 확인하세요.",result,valid?"1":"0");q.studyGuide=g;
 }
 private static void step(StudyGuide g,String text,String before,long value){g.step(text,before,"",Long.toString(value));}
}
