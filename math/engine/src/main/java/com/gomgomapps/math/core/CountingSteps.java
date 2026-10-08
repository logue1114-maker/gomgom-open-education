package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
/** Next counting number, using only the publicly specified direction, step and start. */
public final class CountingSteps {
 private CountingSteps(){}
 public static final List<Catalog.Skill> SKILLS=List.of(
  new Catalog.Skill("countForward","앞으로 세기",1,1,1,"","countSteps",1000,"count","앞으로 하나씩 센다."),
  new Catalog.Skill("countBackward","거꾸로 세기",1,1,1,"","countSteps",1000,"count","거꾸로 센다."),
  new Catalog.Skill("countSkip","뛰어 세기",1,1,1,"","countSteps",1000,"count","같은 수만큼 건너뛰며 센다."));
 public static boolean supports(String id){return SKILLS.stream().anyMatch(s->s.id.equals(id));}
 static Question next(Catalog.Skill s,Random random,CurriculumLimits limits,Map<String,Integer> recent){
  Question q=FactFoundations.choose(candidates(s,limits),random,recent);attach(q);return q;
 }
 static Map<String,Question> candidates(Catalog.Skill s,CurriculumLimits limits){
  Map<String,Question> pool=new LinkedHashMap<>();boolean back=s.id.equals("countBackward");
  Map<Integer,Integer> bounds=limits.countingBounds();if(bounds.isEmpty())bounds=Map.of(1,100,2,30,5,100,10,100);
  for(var bound:bounds.entrySet()){
   int step=bound.getKey(),maximum=bound.getValue();
   if(s.id.equals("countForward")&&step!=1||s.id.equals("countSkip")&&step==1)continue;
   for(int start=back?step:0;start<=(back?maximum:maximum-step);start+=limits.countingAnyStart()?1:step){
    int answer=back?start-step:start+step;
    Question q=new Question(s.id,step+"씩 "+(back?"거꾸로":"앞으로")+" 세세요.\n"+start+" → □","",""+answer);q.stepSupport=false;
    if(limits.allows(q))pool.put(q.signature(),q);
   }
  }
  return pool;
 }
 public static void attach(Question q){
  if(q==null||!supports(q.skillId))return;
  Matcher m=Pattern.compile("(\\d+)씩 (앞으로|거꾸로) 세세요\\.\\n(\\d+) → □").matcher(q.prompt);if(!m.matches())return;
  int step=Integer.parseInt(m.group(1)),start=Integer.parseInt(m.group(3));boolean back=m.group(2).equals("거꾸로");
  StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="counting-steps-v1";
  g.step("시작하는 수를 쓰세요.","시작 수 = ","",""+start);
  g.step("몇씩 세는지 쓰세요.","세는 간격 = ","",""+step);
  g.step(back?"그 간격만큼 거꾸로 세세요.":"그 간격만큼 앞으로 세세요.","다음 수 = ","",""+(back?start-step:start+step));q.studyGuide=g;
 }
}
