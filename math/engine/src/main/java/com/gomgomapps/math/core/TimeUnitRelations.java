package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
/** Public time quantities become student-entered relationships, never prefilled calculations. */
public final class TimeUnitRelations {
 private TimeUnitRelations(){}
 public static final Set<String> IDS=Set.of("el_hours_to_minutes","el_minutes_to_hours","el_time_add","el_time_difference","el_days_week","el_days_to_weeks","el_time_to_seconds","el_time_second_add","el_time_second_difference");
 public static boolean supports(String id){return IDS.contains(id);}
 public record Frame(String instruction,String label,String result,String expected,int[] prior){}
 private static final class Builder {
  final List<Frame> frames=new ArrayList<>();
  int add(String instruction,String label,String result,int value,int... prior){frames.add(new Frame(instruction,label,result,String.valueOf(value),prior));return frames.size()-1;}
  int given(String label,int value){return add("문제에서 이 값을 찾아 쓰세요.",label+" = ",label,value);}
  int rule(String label,int value){return add(label.equals("1주일의 일 수")?"1주일은 7일입니다.":"시간과 분, 분과 초는 60씩 묶습니다.",label+" = ",label,value);}
  int calc(String relation,String result,int value,int... prior){return add("관계식에 맞게 계산하세요.",relation+" = ",result,value,prior);}
 }
 private static int[] match(String prompt,String regex){Matcher m=Pattern.compile(regex).matcher(prompt);if(!m.matches())return null;int[] n=new int[m.groupCount()];for(int i=0;i<n.length;i++){n[i]=Integer.parseInt(m.group(i+1));if(n[i]>1000000)return null;}return n;}
 private static int clock(Builder b,String prefix,int hour,int minute,int second,boolean seconds,int unit,int minuteUnit){
  if(hour>23||minute>59||second>59)return -1;
  int h=b.given(prefix+"의 시간 수",hour),m=b.given(prefix+"의 분 수",minute),s=seconds?b.given(prefix+"의 초 수",second):-1;
  int hm=b.calc(prefix+"의 시간 수 × 1시간의 분 수",prefix+"의 시간을 바꾼 분",hour*60,h,minuteUnit);
  int tm=b.calc(prefix+"의 시간을 바꾼 분 + "+prefix+"의 분 수",prefix+"의 전체 분",hour*60+minute,hm,m);
  if(!seconds)return tm;
  int ts=b.calc(prefix+"의 전체 분 × 1분의 초 수",prefix+"의 분을 바꾼 초",(hour*60+minute)*60,tm,unit);
  return b.calc(prefix+"의 분을 바꾼 초 + "+prefix+"의 초 수",prefix+"의 전체 초",hour*3600+minute*60+second,ts,s);
 }
 public static List<Frame> frames(Question q){
  if(q==null||!supports(q.skillId)||q.prompt==null)return List.of();Builder b=new Builder();int[] n;String id=q.skillId,p=q.prompt;
  if(id.equals("el_hours_to_minutes")||id.equals("el_time_to_seconds")){
   boolean seconds=id.equals("el_time_to_seconds");n=match(p,seconds?"(\\d+)분 (\\d+)초는 모두 몇 초인가요\\?":"(\\d+)시간 (\\d+)분은 모두 몇 분인가요\\?");if(n==null||n[1]>59)return List.of();
   String big=seconds?"분 수":"시간 수",small=seconds?"남은 초":"남은 분",unit=seconds?"1분의 초 수":"1시간의 분 수",converted=seconds?"분을 바꾼 초":"시간을 바꾼 분";
   int a=b.given(big,n[0]),c=b.given(small,n[1]),u=b.rule(unit,60),v=b.calc(big+" × "+unit,converted,n[0]*60,a,u);b.calc(converted+" + "+small,seconds?"전체 초":"전체 분",n[0]*60+n[1],v,c);
  }else if(id.equals("el_minutes_to_hours")||id.equals("el_days_to_weeks")){
   boolean days=id.equals("el_days_to_weeks"),pair=p.contains(days?"며칠":"몇 분"),two=p.contains("합하면");int scale=days?7:60;
   String small=days?"일":"분",big=days?"주일":"시간",unit=days?"1주일의 일 수":"1시간의 분 수",quantity=days?"일 수":"분 수",whole=days?"전체 일 수":"전체 분";
   String regex=two?"(\\d+)"+small+"과 (\\d+)"+small+"을 합하면 몇 "+big+" "+(days?"며칠":"몇 분")+"인가요\\?":pair?"(\\d+)"+small+"은 몇 "+big+" "+(days?"며칠":"몇 분")+"인가요\\?":"(\\d+)"+small+"은 몇 "+big+"인가요\\?";
   n=match(p,regex);if(n==null)return List.of();int total=n[0];int a;
   if(two){int first=b.given("첫 "+quantity,n[0]),second=b.given("둘째 "+quantity,n[1]);total+=n[1];a=b.calc("첫 "+quantity+" + 둘째 "+quantity,whole,total,first,second);}else a=b.given(whole,total);
   int u=b.rule(unit,scale),h=b.calc(whole+" ÷ "+unit+"의 몫",big+" 수",total/scale,a,u);
   if(pair)b.calc(whole+" − "+big+" 수 × "+unit,days?"남은 일":"남은 분",total%scale,a,h,u);
  }else if(id.equals("el_days_week")){
   n=match(p,"(\\d+)주일은 며칠인가요\\?");if(n!=null){int w=b.given("주일 수",n[0]),u=b.rule("1주일의 일 수",7);b.calc("주일 수 × 1주일의 일 수","전체 일 수",n[0]*7,w,u);}
   else{boolean two=p.contains("합하면");n=match(p,two?"(\\d+)주일 (\\d+)일과 (\\d+)주일 (\\d+)일을 합하면 모두 며칠인가요\\?":"(\\d+)주일 (\\d+)일은 모두 며칠인가요\\?");if(n==null||n[1]>6||(two&&n[3]>6))return List.of();
    int w=b.given("첫 기간의 주일 수",n[0]),d=b.given("첫 기간의 남은 일",n[1]),w2=-1,d2=-1;if(two){w2=b.given("둘째 기간의 주일 수",n[2]);d2=b.given("둘째 기간의 남은 일",n[3]);}int u=b.rule("1주일의 일 수",7);
    int converted=b.calc("첫 기간의 주일 수 × 1주일의 일 수","첫 기간의 주일을 바꾼 일",n[0]*7,w,u),first=b.calc("첫 기간의 주일을 바꾼 일 + 첫 기간의 남은 일","첫 기간의 전체 일",n[0]*7+n[1],converted,d);
    if(two){int c2=b.calc("둘째 기간의 주일 수 × 1주일의 일 수","둘째 기간의 주일을 바꾼 일",n[2]*7,w2,u),second=b.calc("둘째 기간의 주일을 바꾼 일 + 둘째 기간의 남은 일","둘째 기간의 전체 일",n[2]*7+n[3],c2,d2);b.calc("첫 기간의 전체 일 + 둘째 기간의 전체 일","전체 일 수",(n[0]+n[2])*7+n[1]+n[3],first,second);}
   }
  }else{
   boolean seconds=id.contains("second"),difference=id.contains("difference");
   String time="(\\d+)시 (\\d+)분"+(seconds?" (\\d+)초":""),measure=seconds?"초":"분";
   n=match(p,difference?time+"부터 "+time+"까지 몇 "+measure+"인가요\\?":time+"에서 (\\d+)"+measure+" 뒤의 시각은\\?");if(n==null)return List.of();
   int width=seconds?3:2,minuteUnit=b.rule("1시간의 분 수",60),u=seconds?b.rule("1분의 초 수",60):minuteUnit;
   // Hours and minutes share the same base of 60; name the relevant unit in each relation.
   int start=clock(b,"처음 시각",n[0],n[1],seconds?n[2]:0,seconds,u,minuteUnit);if(start<0)return List.of();int startValue=n[0]*(seconds?3600:60)+n[1]*(seconds?60:1)+(seconds?n[2]:0);
   if(difference){int end=clock(b,"끝 시각",n[width],n[width+1],seconds?n[width+2]:0,seconds,u,minuteUnit);if(end<0)return List.of();int endValue=n[width]*(seconds?3600:60)+n[width+1]*(seconds?60:1)+(seconds?n[width+2]:0);if(endValue<startValue)return List.of();b.calc("끝 시각의 전체 "+measure+" − 처음 시각의 전체 "+measure,"걸린 "+measure+" 수",endValue-startValue,end,start);}
   else{int duration=b.given("더할 "+measure+" 수",n[width]),total=startValue+n[width];if(total>=(seconds?86400:1440))return List.of();int end=b.calc("처음 시각의 전체 "+measure+" + 더할 "+measure+" 수","끝 시각의 전체 "+measure,total,start,duration);
    if(seconds){int allMinutes=b.calc("끝 시각의 전체 초 ÷ 1분의 초 수의 몫","끝 시각의 전체 분",total/60,end,u);b.calc("끝 시각의 전체 초 − 끝 시각의 전체 분 × 1분의 초 수","끝 시각의 초 수",total%60,end,allMinutes,u);int hour=b.calc("끝 시각의 전체 분 ÷ 1시간의 분 수의 몫","끝 시각의 시간 수",total/3600,allMinutes,minuteUnit);b.calc("끝 시각의 전체 분 − 끝 시각의 시간 수 × 1시간의 분 수","끝 시각의 분 수",(total/60)%60,allMinutes,hour,minuteUnit);}
    else{int hour=b.calc("끝 시각의 전체 분 ÷ 1시간의 분 수의 몫","끝 시각의 시간 수",total/60,end,u);b.calc("끝 시각의 전체 분 − 끝 시각의 시간 수 × 1시간의 분 수","끝 시각의 분 수",total%60,end,hour,u);}
   }
  }return List.copyOf(b.frames);
 }
 public static void attach(Question q){List<Frame> frames=frames(q);if(frames.isEmpty())return;StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="time-unit-relations-v1";for(Frame f:frames)g.step(f.instruction,f.label,"",f.expected);q.studyGuide=g;}
}
