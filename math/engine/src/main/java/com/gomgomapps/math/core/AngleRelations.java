package com.gomgomapps.math.core;
import java.util.Random;

/** Existing angle topics with varied public relations and numeric, non-transferring help. */
public final class AngleRelations {
 private AngleRelations(){}
 public static boolean supports(String id){return id.equals("sec_parallel_angle")||id.equals("sec_circle_inscribed");}
 public static Question create(Catalog.Skill skill,Random random){
  int mode=random.nextInt(3),given,answer;String prompt,instruction,before;
  if(skill.id.equals("sec_parallel_angle")){
   given=25+random.nextInt(131);answer=mode==2?180-given:given;
   String relation=mode==0?"동위각":mode==1?"엇각":"같은 쪽 내각";
   prompt="l ∥ m\n∠A = "+given+"°\n∠B는 ∠A의 "+relation+"입니다. ∠B는 몇 도인가요?";
   instruction=mode==2?"같은 쪽 내각의 합은 180°입니다. ∠B를 구하세요.":mode==0?"평행선의 동위각은 크기가 같습니다. ∠B를 구하세요.":"평행선의 엇각은 크기가 같습니다. ∠B를 구하세요.";
   before=mode==2?"180 − "+given+" = ":"∠B = ";
  }else{
   int inscribed=7+random.nextInt(77);given=mode==1?inscribed:2*inscribed;answer=mode==1?2*given:given/2;
   String value=mode==0?"∠AOB = "+given+"°":mode==1?"∠ACB = "+given+"°":"작은 호 AB = "+given+"°";
   prompt="O는 원의 중심입니다. C는 작은 호 AB 밖의 원 위에 있습니다.\n"+value+"\n"+(mode==1?"∠AOB":"∠ACB")+"는 몇 도인가요?";
   instruction=mode==1?"같은 호의 중심각은 원주각의 두 배입니다.":"같은 호의 원주각은 중심각의 절반입니다.";
   before=given+(mode==1?" × 2 = ":" ÷ 2 = ");
  }
  Question q=new Question(skill.id,prompt,Integer.toString(answer),Integer.toString(answer));q.stepSupport=false;
  q.diagram=new StudyDiagram(skill.id.equals("sec_parallel_angle")?"parallelAngleRelation":"circleAngleRelation",new double[]{given,mode});
  q.givenNumbers.put("given",Integer.toString(given));q.givenNumbers.put("relation",Integer.toString(mode));
  StudyGuide help=new StudyGuide().transfer(false);
  if(skill.id.equals("sec_parallel_angle"))help.step("그림에 주어진 ∠A의 크기를 쓰세요.","∠A = ","°",Integer.toString(given));
  else help.step(mode==2?"작은 호의 크기와 중심각의 크기는 같습니다.":"그림에 주어진 각의 크기를 쓰세요.",mode==1?"∠ACB = ":"∠AOB = ","°",Integer.toString(given));
  help.step(instruction,before,"°",Integer.toString(answer));q.studyGuide=help;return q.withInputs(given);
 }
}
