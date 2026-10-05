package com.gomgomapps.math.core;
import java.util.*;
/** Whole analog readings and equal digital representations of the same public hands. */
final class ClockReadings {
 static final List<Catalog.Skill> SKILLS=List.of(
  new Catalog.Skill("el_clock_time","시계 읽기 — 시와 분",2,2,3,"","clockTime",12,"el_clock_hour","짧은바늘이 지나온 시와 긴바늘이 가리키는 분을 읽습니다."),
  new Catalog.Skill("el_clock_digital_match","시계와 디지털 시각",2,2,3,"","clockDigitalMatch",12,"el_clock_time","디지털 시각은 시와 분을 콜론(:)으로 나누어 표시합니다.")
 );
 static boolean supports(String id){return id.equals("el_clock_time")||id.equals("el_clock_digital_match");}
 static Question next(Catalog.Skill skill,Random random,CurriculumLimits limits,Map<String,Integer> recent){
  Map<String,Question> candidates=new LinkedHashMap<>();int step=limits.minuteStep();
  for(int hour=1;hour<=12;hour++)for(int minute=0;minute<60;minute+=step){
   Question q=new Question(skill.id,skill.id.equals("el_clock_time")?"시계를 보고 시와 분을 입력하세요.":"시계와 같은 디지털 시각을 고르세요.","",skill.id.equals("el_clock_time")?new String[]{String.valueOf(hour),String.valueOf(minute)}:new String[]{String.valueOf(hour*60+minute)});
   q.stepSupport=false;q.diagram=new StudyDiagram("clock",new double[]{hour,minute});if(q.answers.length==2)q.labels=new String[]{"시","분"};
   q.studyGuide=new StudyGuide().transfer(false).step("짧은바늘이 지나온 시를 읽으세요.","시 = ","",String.valueOf(hour)).step("긴바늘이 가리키는 분을 읽으세요.","분 = ","",String.valueOf(minute));
   if(limits.allows(q))candidates.put(q.signature(),q);
  }
  Question q=FactFoundations.choose(candidates,random,recent);
  if(skill.id.equals("el_clock_digital_match")){
   int answer=Integer.parseInt(q.answers[0]);List<Integer> wrong=new ArrayList<>();for(int hour=1;hour<=12;hour++)for(int minute=0;minute<60;minute+=step){int value=hour*60+minute;if(value!=answer)wrong.add(value);}Collections.shuffle(wrong,random);
   q.choiceLabels.put(String.valueOf(answer),digital(answer));for(int i=0;i<3;i++){int value=wrong.get(i);q.choiceLabels.put(String.valueOf(value),digital(value));}
  }
  return q;
 }
 private static String digital(int value){return String.format(Locale.ROOT,"%02d:%02d",value/60,value%60);}
}
