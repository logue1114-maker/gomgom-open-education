package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
/** Base-ten inventory stories; evaluation reconstructs only the visible inventory. */
public final class PracticalPlaceStories {
 private PracticalPlaceStories(){}
 public static final String ID="practicalPlaceStories";
 public static final Catalog.Skill SKILL=new Catalog.Skill(ID,"묶음과 낱개 문제",3,1,1,"",ID,1000,"place1000,add1000","100개·10개 묶음과 낱개를 합쳐 전체 수를 구한다.");
 private static final List<String> OBJECTS=List.of("스티커","색연필");
 private static final Pattern PROMPT=Pattern.compile("^(스티커|색연필)\\n100개씩 든 상자: (10|[0-9])개\\n10개씩 든 봉지: ([0-9])개\\n낱개: ([0-9])개\\n모두 몇 개인가요\\?$");
 public static boolean supports(String id){return ID.equals(id);}
 static Question make(int context,int number){
  if(context<0||context>=OBJECTS.size()||number<0||number>1000)throw new IllegalArgumentException("base-ten inventory domain");
  Question q=new Question(ID,OBJECTS.get(context)+"\n100개씩 든 상자: "+number/100+"개\n10개씩 든 봉지: "+number/10%10+"개\n낱개: "+number%10+"개\n모두 몇 개인가요?","",Integer.toString(number));
  q.kind="placeStory";q.labels=new String[]{"전체 수"};q.stepSupport=false;return q;
 }
 static Question next(Random random,CurriculumLimits limits,Map<String,Integer> recent){
  Map<String,Question> pool=new LinkedHashMap<>();for(int context=0;context<2;context++)for(int n=0;n<=1000;n++){Question q=make(context,n);if(limits.allows(q))pool.put(q.signature(),q);}
  Question q=FactFoundations.choose(pool,random,recent);attach(q);return q;
 }
 public static int[] read(Question q){
  if(q==null||!supports(q.skillId)||q.prompt==null)return null;Matcher m=PROMPT.matcher(q.prompt);if(!m.matches())return null;
  int h=Integer.parseInt(m.group(2)),t=Integer.parseInt(m.group(3)),o=Integer.parseInt(m.group(4)),n=h*100+t*10+o;if(n>1000)return null;return new int[]{h,t,o,n};
 }
 static Checker.Result check(Question q,List<String> answers){
  int[] v=read(q);if(v==null||answers.size()!=1||answers.get(0)==null||!answers.get(0).trim().matches("[0-9]{1,4}"))return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"수 입력 필요");
  return Integer.parseInt(answers.get(0).trim())==v[3]?new Checker.Result(Checker.Status.CORRECT,-1,"정답"):new Checker.Result(Checker.Status.WRONG_ANSWER,0,"이 수 확인");
 }
 public static void attach(Question q){
  int[] v=read(q);if(v==null)return;StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="practical-place-stories-v1";
  g.step("100개씩 든 상자의 수를 쓰세요.","상자 수 = ","",Integer.toString(v[0]));
  g.step("10개씩 든 봉지의 수를 쓰세요.","봉지 수 = ","",Integer.toString(v[1]));
  g.step("낱개의 수를 쓰세요.","낱개 수 = ","",Integer.toString(v[2]));
  g.step("묶음에 든 수와 낱개를 합쳐 전체 수를 구하세요.","100 × 상자 수 + 10 × 봉지 수 + 낱개 수 = ","",Integer.toString(v[3]));q.studyGuide=g;
 }
}
