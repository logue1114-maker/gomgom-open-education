package com.gomgomapps.math.core;
import java.util.*;
/** One fictional June journey, lasting1–240 minutes, with explicit dates and24-hour times. */
final class TimetableQuestions {
 static final List<Catalog.Skill> SKILLS=List.of(new Catalog.Skill("el_trip_departure","시간표 — 출발 시각",6,1,5,"","tripDeparture",24,"el_time_difference","도착 시각에서 걸린 시간을 빼면 출발 시각을 구할 수 있습니다."));
 static boolean added(String id){return id.equals("el_trip_departure");}
 static boolean supports(String id){return added(id)||id.equals("el_time_add")||id.equals("el_time_difference");}
 static Question create(Catalog.Skill skill,Random random,CurriculumLimits limits){
  int step=limits.minuteStep(),day=1+random.nextInt(27),start=step*random.nextInt(1440/step),duration=step*(1+random.nextInt(240/step));
  return create(skill.id,day,start,duration);
 }
 static Question create(String id,int day,int start,int duration){
  if(day<1||day>27||start<0||start>=1440||duration<1||duration>240)throw new IllegalArgumentException("Invalid practice journey");
  int end=start+duration,endDay=day+end/1440,endClock=end%1440;boolean arrival=id.equals("el_time_add"),elapsed=id.equals("el_time_difference");
  String prompt="시간표 · 24시간제\n"+(elapsed?"출발\n"+date(day,start)+"\n도착\n"+date(endDay,endClock)+"\n걸린 시간을 시간과 분으로 입력하세요.":arrival?"출발\n"+date(day,start)+"\n걸린 시간\n"+duration+"분\n도착 날짜(일), 시, 분을 입력하세요.":"도착\n"+date(endDay,endClock)+"\n걸린 시간\n"+duration+"분\n출발 날짜(일), 시, 분을 입력하세요.");
  Question q=new Question(id,prompt,"",elapsed?new String[]{String.valueOf(duration/60),String.valueOf(duration%60)}:arrival?new String[]{String.valueOf(endDay),String.valueOf(endClock/60),String.valueOf(endClock%60)}:new String[]{String.valueOf(day),String.valueOf(start/60),String.valueOf(start%60)});
  q.kind="pair";q.stepSupport=false;q.labels=elapsed?new String[]{"시간","분"}:new String[]{"날짜(일)","시","분"};StudyGuide guide=new StudyGuide().transfer(false);
  if(elapsed){
   guide.step("출발 시각을 분으로 바꾸세요.",start/60+" × 60 + "+start%60+" = ","",String.valueOf(start));
   guide.step(end>=1440?"다음날 도착 시각에는 하루 1440분을 더하세요.":"도착 시각을 분으로 바꾸세요.",(end>=1440?"1440 + ":"")+endClock/60+" × 60 + "+endClock%60+" = ","",String.valueOf(end));
   guide.step("도착 시각에서 출발 시각을 빼세요.",end+" − "+start+" = ","",String.valueOf(duration));
   parts(guide,duration);
  }else if(arrival){
   guide.step("출발 시각을 분으로 바꾸세요.",start/60+" × 60 + "+start%60+" = ","",String.valueOf(start));
   guide.step("걸린 시간을 더하세요.",start+" + "+duration+" = ","",String.valueOf(end));
   guide.step("1440분부터는 다음날입니다. 날짜(일)를 구하세요.",day+" + "+end/1440+" = ","",String.valueOf(endDay));
   if(end>=1440)guide.step("하루 1440분을 빼서 도착 시각을 구하세요.",end+" − 1440 = ","",String.valueOf(endClock));
   parts(guide,endClock);
  }else{
   guide.step(end>=1440?"전날부터 세려면 도착 시각에 하루 1440분을 더하세요.":"도착 시각을 분으로 바꾸세요.",(end>=1440?"1440 + ":"")+endClock/60+" × 60 + "+endClock%60+" = ","",String.valueOf(end));
   guide.step("걸린 시간을 빼세요.",end+" − "+duration+" = ","",String.valueOf(start));
   guide.step("출발 날짜(일)를 구하세요.",endDay+" − "+end/1440+" = ","",String.valueOf(day));parts(guide,start);
  }
  q.studyGuide=guide;return q;
 }
 private static void parts(StudyGuide guide,int minutes){guide.step("60으로 나눈 몫을 입력하세요.",minutes+" ÷ 60의 몫 = ","",String.valueOf(minutes/60)).step("60으로 나눈 나머지를 분으로 쓰세요.",minutes+" ÷ 60의 나머지 = ","",String.valueOf(minutes%60));}
 private static String date(int day,int minutes){return String.format(Locale.ROOT,"6월 %d일 %02d:%02d",day,minutes/60,minutes%60);}
}
