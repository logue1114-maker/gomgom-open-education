package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
/** Distances between signed public positions; distances themselves are nonnegative. */
public final class SignedIntervalStories {
 private SignedIntervalStories(){}
 public static final String TEMPERATURE="temperatureInterval",HEIGHT="heightInterval";
 public static final List<Catalog.Skill> SKILLS=List.of(new Catalog.Skill(TEMPERATURE,"두 기온의 차이",6,1,1,"","signedIntervals",100,"temperatureChanges","두 기온 사이의 차이를 구한다."),new Catalog.Skill(HEIGHT,"두 지점의 높이 차이",6,1,1,"","signedIntervals",100,"negativeCountBackward","해수면을 기준으로 두 지점 사이의 높이 차이를 구한다."));
 public static boolean supports(String id){return TEMPERATURE.equals(id)||HEIGHT.equals(id);}
 public static Question make(String id,int first,int second){if(!supports(id)||first< -50||first>50||second< -50||second>50)throw new IllegalArgumentException("Signed interval domain");String prompt=TEMPERATURE.equals(id)?"첫 기온: "+first+" °C\n둘째 기온: "+second+" °C\n두 기온의 차이는 몇 °C인가요?":"해수면의 높이: 0 m\n첫 지점: "+first+" m\n둘째 지점: "+second+" m\n두 지점의 높이 차이는 몇 m인가요?";Question q=new Question(id,prompt,"",""+Math.abs(first-second));q.stepSupport=false;attach(q);return q;}
 static Question at(String id,int index){if(index<0||index>=10201)throw new IllegalArgumentException("Signed interval index");return make(id,index/101-50,index%101-50);}
 static Question next(String id,Random random,CurriculumLimits limits,Map<String,Integer> recent){return IndexedQuestionSupply.choose(10201,i->at(id,i),random,limits,recent);}
 public static int[] read(Question q){if(q==null||q.prompt==null||!supports(q.skillId))return null;String pattern=TEMPERATURE.equals(q.skillId)?"첫 기온: ([−-]?\\d{1,2}) °C\\n둘째 기온: ([−-]?\\d{1,2}) °C\\n두 기온의 차이는 몇 °C인가요\\?":"해수면의 높이: 0 m\\n첫 지점: ([−-]?\\d{1,2}) m\\n둘째 지점: ([−-]?\\d{1,2}) m\\n두 지점의 높이 차이는 몇 m인가요\\?";Matcher m=Pattern.compile(pattern).matcher(q.prompt);if(!m.matches())return null;int a=Integer.parseInt(m.group(1).replace('−','-')),b=Integer.parseInt(m.group(2).replace('−','-'));if(a< -50||a>50||b< -50||b>50)return null;return new int[]{a,b};}
 static Checker.Result check(Question q,List<String> answers){int[] v=read(q);if(v==null||answers==null||answers.size()!=1||answers.get(0)==null)return new Checker.Result(Checker.Status.INPUT_NEEDED,-1,"수 입력 필요");String raw=Expression.normalize(answers.get(0).trim());if(!raw.matches("[+-]?\\d{1,3}"))return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"정수 입력 필요");boolean same=Integer.parseInt(raw)==Math.abs(v[0]-v[1]);return new Checker.Result(same?Checker.Status.CORRECT:Checker.Status.WRONG_ANSWER,same?-1:0,same?"계산 확인 완료":"이 답 확인");}
 public static void attach(Question q){int[] v=read(q);if(v==null)return;int low=Math.min(v[0],v[1]),high=Math.max(v[0],v[1]);StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="signed-intervals-v1";
  g.step("더 작은 수를 쓰세요.","작은 수 = ","",""+low);g.step("더 큰 수를 쓰세요.","큰 수 = ","",""+high);
  g.step("작은 수에서 0까지의 거리를 쓰세요.","작은 수와 0 사이의 거리 = ","",""+Math.abs(low));g.step("큰 수에서 0까지의 거리를 쓰세요.","큰 수와 0 사이의 거리 = ","",""+Math.abs(high));
  boolean across=low<=0&&high>=0;Map<String,String> choices=new LinkedHashMap<>();choices.put("add","두 거리를 더하기");choices.put("subtract","큰 거리에서 작은 거리 빼기");g.choice("0이 두 수 사이에 있으면 더하기, 없으면 빼기를 고르세요.",choices,across?"add":"subtract");
  g.step(across?"두 거리를 더하세요.":"큰 거리에서 작은 거리를 빼세요.",across?"첫 거리 + 둘째 거리 = ":"큰 거리 − 작은 거리 = ","",""+(high-low));q.studyGuide=g;
 }
}
