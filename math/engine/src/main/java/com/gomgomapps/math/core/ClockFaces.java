package com.gomgomapps.math.core;
import java.util.*;
/** Two-by-two clock alternatives require matching both hands, not a text label. */
final class ClockFaces {
 static final List<Catalog.Skill> SKILLS=List.of(new Catalog.Skill("el_digital_to_analog","디지털 시각에 맞는 시계",3,1,6,"","digitalToAnalog",12,"el_clock_digital_match","긴바늘은 분을 나타내고, 짧은바늘은 지난 시와 다음 시 사이를 움직입니다."));
 static boolean supports(String id){return id.equals("el_digital_to_analog");}
 static Question next(Catalog.Skill skill,Random random,CurriculumLimits limits,Map<String,Integer> recent){
  Map<String,Question> candidates=new LinkedHashMap<>();
  for(int hour=1;hour<=12;hour++)for(int minute=0;minute<60;minute+=limits.minuteStep()){
   Question q=new Question(skill.id,String.format(Locale.ROOT,"%02d:%02d",hour,minute)+"\n디지털 시각과 같은 시계를 고르세요.","",String.valueOf(hour*60+minute));q.stepSupport=false;
   q.studyGuide=new StudyGuide().transfer(false).step("디지털 시각의 시를 읽으세요.","시 = ","",String.valueOf(hour)).step("디지털 시각의 분을 읽으세요.","분 = ","",String.valueOf(minute));
   if(limits.allows(q))candidates.put(q.signature(),q);
  }
  Question q=FactFoundations.choose(candidates,random,recent);int value=Integer.parseInt(q.answers[0]),h=value/60,m=value%60;
  int otherHour=(h+random.nextInt(11))%12+1;List<Integer> wrongMinutes=new ArrayList<>();for(int minute=0;minute<60;minute+=limits.minuteStep())if(minute!=m)wrongMinutes.add(minute);Collections.shuffle(wrongMinutes,random);int otherMinute=wrongMinutes.get(0);
  for(int hour:new int[]{h,otherHour})for(int minute:new int[]{m,otherMinute})q.choiceDiagrams.put(String.valueOf(hour*60+minute),new StudyDiagram("clock",new double[]{hour,minute}));
  return q;
 }
}
