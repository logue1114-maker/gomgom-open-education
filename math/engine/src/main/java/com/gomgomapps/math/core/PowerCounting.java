package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
/** Arbitrary whole-number starts, followed by two equal powers-of-ten steps. */
public final class PowerCounting {
 private PowerCounting(){}
 public static final String FORWARD="powerCountForward",BACKWARD="powerCountBackward";
 public static final List<Catalog.Skill> SKILLS=List.of(new Catalog.Skill(FORWARD,"10의 거듭제곱씩 앞으로 세기",5,1,1,"","powerCounting",3000000,"countSkip","같은 간격으로 다음 두 수를 쓴다."),new Catalog.Skill(BACKWARD,"10의 거듭제곱씩 뒤로 세기",5,1,1,"","powerCounting",2000000,"countBackward","같은 간격으로 앞의 두 수를 쓴다."));
 public static boolean supports(String id){return FORWARD.equals(id)||BACKWARD.equals(id);}
 private static int powers(int maximum){if(maximum<10||maximum>1000000)throw new IllegalArgumentException("Counting start range");int count=0;for(int step=10;step<=maximum;step*=10)count++;return count;}
 static int count(int maximum){return powers(maximum)*(maximum+1);}
 static Question at(String id,int index,int maximum){if(!supports(id)||index<0||index>=count(maximum))throw new IllegalArgumentException("Counting condition index");int power=index/(maximum+1),step=10;while(power-->0)step*=10;return make(id,index%(maximum+1),step);}
 public static Question make(String id,int start,int step){
  if(!supports(id)||start<0||start>1000000||!Set.of(10,100,1000,10000,100000,1000000).contains(step))throw new IllegalArgumentException("Power counting givens");
  int delta=FORWARD.equals(id)?step:-step;
  Question q=new Question(id,step+"씩 "+(delta>0?"앞으로":"뒤로")+" 두 번 세세요.\n"+start+" → □ → □","",""+(start+delta),""+(start+2*delta));q.kind="powerCounting";q.labels=new String[]{"첫 번째 수","두 번째 수"};q.stepSupport=false;attach(q);return q;
 }
 static Question next(Catalog.Skill s,Random random,CurriculumLimits limits,Map<String,Integer> recent){int maximum=limits.wholeMaximum(1000000);return IndexedQuestionSupply.choose(count(maximum),index->at(s.id,index,maximum),random,limits,recent);}
 public static int[] read(Question q){
  if(q==null||q.prompt==null||!supports(q.skillId))return null;boolean forward=FORWARD.equals(q.skillId);Matcher m=Pattern.compile("(10|100|1000|10000|100000|1000000)씩 "+(forward?"앞으로":"뒤로")+" 두 번 세세요\\.\\n(\\d{1,7}) → □ → □").matcher(q.prompt);if(!m.matches())return null;
  int step=Integer.parseInt(m.group(1)),start=Integer.parseInt(m.group(2));if(start>1000000)return null;int delta=forward?step:-step;return new int[]{start,step,start+delta,start+2*delta};
 }
 public static void attach(Question q){int[] v=read(q);if(v==null)return;boolean forward=FORWARD.equals(q.skillId);StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="power-counting-sequence-v1";g.step("시작하는 수를 쓰세요.","시작 수 s = ","",""+v[0]);g.step("세는 간격을 쓰세요.","간격 h = ","",""+v[1]);g.step(forward?"시작 수에 간격을 더하세요.":"시작 수에서 간격을 빼세요.",forward?"첫 수 a = s + h = ":"첫 수 a = s − h = ","",""+v[2]);g.step(forward?"첫 수에 같은 간격을 더하세요.":"첫 수에서 같은 간격을 빼세요.",forward?"둘째 수 b = a + h = ":"둘째 수 b = a − h = ","",""+v[3]);q.studyGuide=g;}
 static Checker.Result check(Question q,List<String> answers){int[] v=read(q);if(v==null||answers==null)return new Checker.Result(Checker.Status.INPUT_NEEDED,-1,"문제 확인 필요");for(int i=0;i<2;i++){if(answers.size()<=i||answers.get(i)==null)return new Checker.Result(Checker.Status.INPUT_NEEDED,i,"수 입력 필요");String raw=Expression.normalize(answers.get(i).trim());if(!raw.matches("[+-]?\\d{1,8}"))return new Checker.Result(Checker.Status.INPUT_NEEDED,i,"정수 입력 필요");if(Integer.parseInt(raw)!=v[i+2])return new Checker.Result(Checker.Status.WRONG_ANSWER,i,"이 수 확인");}return new Checker.Result(Checker.Status.CORRECT,-1,"정답");}
}
