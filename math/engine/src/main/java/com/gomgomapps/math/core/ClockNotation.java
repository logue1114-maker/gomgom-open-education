package com.gomgomapps.math.core;
import java.util.*;
/** Conversion of an explicitly supplied time, without inferring AM/PM from analog hands. */
final class ClockNotation {
 static final List<Catalog.Skill> SKILLS=List.of(
  new Catalog.Skill("el_time_12_to_24","12시간제 → 24시간제",5,1,4,"","time12To24",24,"el_clock_time","AM은 오전, PM은 오후입니다. 12 AM은 00시, 12 PM은 12시입니다."),
  new Catalog.Skill("el_time_24_to_12","24시간제 → 12시간제",5,1,4,"","time24To12",24,"el_time_12_to_24","00시부터 11시까지는 AM, 12시부터 23시까지는 PM입니다.")
 );
 static boolean supports(String id){return id.equals("el_time_12_to_24")||id.equals("el_time_24_to_12");}
 static Question next(Catalog.Skill skill,Random random,CurriculumLimits limits,Map<String,Integer> recent){
  Map<String,Question> candidates=new LinkedHashMap<>();
  for(int time=0;time<1440;time+=limits.minuteStep()){
   Question q=create(skill.id,time);if(limits.allows(q))candidates.put(q.signature(),q);
  }
  Question q=FactFoundations.choose(candidates,random,recent);
  if(skill.id.equals("el_time_24_to_12")){
   int value=Integer.parseInt(q.answers[0]);
   // The opposite period is a plausible error, but its position is shuffled normally.
   q.choiceLabels.put(String.valueOf(value),twelve(value));
   int opposite=(value+720)%1440;q.choiceLabels.put(String.valueOf(opposite),twelve(opposite));
   for(int period=0;period<2;period++){
    List<Integer> wrong=new ArrayList<>();for(int hour=period*12;hour<(period+1)*12;hour++)if(hour%12!=value/60%12)wrong.add(hour*60+value%60);
    Collections.shuffle(wrong,random);int distractor=wrong.get(0);q.choiceLabels.put(String.valueOf(distractor),twelve(distractor));
   }
  }
  return q;
 }
 static Question create(String id,int time){
  int h=time/60,m=time%60,h12=h%12==0?12:h%12;boolean forward=id.equals("el_time_12_to_24");
  Question q=new Question(id,forward?"24시간제로 바꿔 시와 분을 입력하세요.":"같은 시각을 12시간제로 고르세요.",forward?twelve(time):String.format(Locale.ROOT,"%02d:%02d",h,m),forward?new String[]{String.valueOf(h),String.valueOf(m)}:new String[]{String.valueOf(time)});
  // Include the supplied expression in identity even though no diagram is necessary.
  q.prompt=q.expression+"\n"+q.prompt;q.expression="";q.stepSupport=false;
  if(forward)q.labels=new String[]{"시","분"};
  String calculation=forward?(h==0?"12 − 12":h==12?"12":h<12?String.valueOf(h12):h12+" + 12"):(h==0?"12":h<=12?String.valueOf(h):h+" − 12");
  String instruction=forward?(h==0?"12 AM의 시를 0으로 바꾸세요.":h==12?"12 PM의 시는 12를 유지하세요.":h<12?"AM의 시는 그대로 쓰세요.":"PM의 시에 12를 더하세요."):(h==0?"00시의 시를 12로 바꾸세요.":h<=12?"시를 그대로 쓰세요.":"시에서 12를 빼세요.");
  StudyGuide guide=new StudyGuide().transfer(false).step(instruction,calculation+" = ","",String.valueOf(forward?h:h12));
  if(!forward)guide.choice("오전 AM 또는 오후 PM을 고르세요.",new LinkedHashMap<>(Map.of("AM","AM","PM","PM")),h<12?"AM":"PM");
  guide.step("분은 그대로 쓰세요.",String.format(Locale.ROOT,"%02d",m)+" → ","",String.valueOf(m));q.studyGuide=guide;
  return q;
 }
 static String twelve(int time){int h=time/60;return String.format(Locale.ROOT,"%02d:%02d %s",h%12==0?12:h%12,time%60,h<12?"AM":"PM");}
}
