package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
/** Base-ten inventory stories; evaluation reconstructs only the visible inventory. */
public final class PracticalPlaceStories {
 private PracticalPlaceStories(){}
 public static final String ID="practicalPlaceStories";
 public static final Catalog.Skill SKILL=new Catalog.Skill(ID,"묶음과 낱개 문제",3,1,1,"",ID,1000,"place1000,add1000","100개·10개 묶음과 낱개를 합쳐 전체 수를 구한다.");
 private static final List<String> OBJECTS=List.of("스티커","색연필");
 private static final Pattern PROMPT=Pattern.compile("^(스티커|색연필)\\n100개씩 든 상자: (10|[0-9])개\\n10개씩 든 봉지: ([0-9])개\\n낱개: ([0-9])개\\n모두 몇 개인가요\\?$");
 private static final Pattern UPPER_PROMPT=Pattern.compile("^(스티커|색연필)\\n1000개씩 든 큰 상자: (10|[0-9])개\\n100개씩 든 상자: ([0-9])개\\n10개씩 든 봉지: ([0-9])개\\n낱개: ([0-9])개\\n모두 몇 개인가요\\?$");
 public static boolean supports(String id){return ID.equals(id);}
 static Question make(int context,int number){
  if(context<0||context>=OBJECTS.size()||number<0||number>1000)throw new IllegalArgumentException("base-ten inventory domain");
  Question q=new Question(ID,OBJECTS.get(context)+"\n100개씩 든 상자: "+number/100+"개\n10개씩 든 봉지: "+number/10%10+"개\n낱개: "+number%10+"개\n모두 몇 개인가요?","",Integer.toString(number));
  q.kind="placeStory";q.labels=new String[]{"전체 수"};q.stepSupport=false;return q;
 }
 static Question next(Random random,CurriculumLimits limits,Map<String,Integer> recent){
  int maximum=Math.min(10000,limits.wholeMaximum(1000));if(maximum>1000){Question q=IndexedQuestionSupply.choose(2*(maximum+1),i->makeUpper(i/(maximum+1),i%(maximum+1)),random,limits,recent);attach(q);return q;}
  Map<String,Question> pool=new LinkedHashMap<>();for(int context=0;context<2;context++)for(int n=0;n<=1000;n++){Question q=make(context,n);if(limits.allows(q))pool.put(q.signature(),q);}
  Question q=FactFoundations.choose(pool,random,recent);attach(q);return q;
 }
 public static int[] read(Question q){
  if(q==null||!supports(q.skillId)||q.prompt==null)return null;Matcher m=PROMPT.matcher(q.prompt);if(!m.matches())return null;
  int h=Integer.parseInt(m.group(2)),t=Integer.parseInt(m.group(3)),o=Integer.parseInt(m.group(4)),n=h*100+t*10+o;if(n>1000)return null;return new int[]{h,t,o,n};
 }
 static Question makeUpper(int context,int number){
  if(context<0||context>=OBJECTS.size()||number<0||number>10000)throw new IllegalArgumentException("upper base-ten inventory domain");
  Question q=new Question(ID,OBJECTS.get(context)+"\n1000개씩 든 큰 상자: "+number/1000+"개\n100개씩 든 상자: "+number/100%10+"개\n10개씩 든 봉지: "+number/10%10+"개\n낱개: "+number%10+"개\n모두 몇 개인가요?","",Integer.toString(number));q.kind="placeStory";q.labels=new String[]{"전체 수"};q.stepSupport=false;return q;
 }
 public static int[] readUpper(Question q){
  if(q==null||!supports(q.skillId)||q.prompt==null)return null;Matcher m=UPPER_PROMPT.matcher(q.prompt);if(!m.matches())return null;
  int k=Integer.parseInt(m.group(2)),h=Integer.parseInt(m.group(3)),t=Integer.parseInt(m.group(4)),o=Integer.parseInt(m.group(5)),n=k*1000+h*100+t*10+o;if(n>10000)return null;return new int[]{k,h,t,o,n};
 }
 static Checker.Result check(Question q,List<String> answers){
  int[] upper=readUpper(q);if(upper!=null){if(answers==null||answers.size()!=1||answers.get(0)==null||!answers.get(0).trim().matches("[0-9]{1,5}"))return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"수 입력 필요");return Integer.parseInt(answers.get(0).trim())==upper[4]?new Checker.Result(Checker.Status.CORRECT,-1,"정답"):new Checker.Result(Checker.Status.WRONG_ANSWER,0,"이 수 확인");}
  int[] v=read(q);if(v==null||answers==null||answers.size()!=1||answers.get(0)==null||!answers.get(0).trim().matches("[0-9]{1,4}"))return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"수 입력 필요");
  return Integer.parseInt(answers.get(0).trim())==v[3]?new Checker.Result(Checker.Status.CORRECT,-1,"정답"):new Checker.Result(Checker.Status.WRONG_ANSWER,0,"이 수 확인");
 }
 public static void attach(Question q){
  int[] upper=readUpper(q);if(upper!=null){StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="upper-practical-place-v1";
   g.step("큰 상자에 든 수를 모두 구하세요.","큰 상자 하나에 든 수 × 큰 상자 수 = ","",Integer.toString(upper[0]*1000));
   g.step("상자에 든 수를 모두 구하세요.","상자 하나에 든 수 × 상자 수 = ","",Integer.toString(upper[1]*100));
   g.step("봉지에 든 수를 모두 구하세요.","봉지 하나에 든 수 × 봉지 수 = ","",Integer.toString(upper[2]*10));
   g.step("낱개의 수를 쓰세요.","낱개 수 = ","",Integer.toString(upper[3]));
   g.step("묶음에 든 수와 낱개를 합쳐 전체 수를 구하세요.","큰 상자에 든 수 + 상자에 든 수 + 봉지에 든 수 + 낱개 수 = ","",Integer.toString(upper[4]));q.studyGuide=g;return;}
  int[] v=read(q);if(v==null)return;StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="practical-place-stories-v1";
  g.step("100개씩 든 상자의 수를 쓰세요.","상자 수 = ","",Integer.toString(v[0]));
  g.step("10개씩 든 봉지의 수를 쓰세요.","봉지 수 = ","",Integer.toString(v[1]));
  g.step("낱개의 수를 쓰세요.","낱개 수 = ","",Integer.toString(v[2]));
  g.step("묶음에 든 수와 낱개를 합쳐 전체 수를 구하세요.","100 × 상자 수 + 10 × 봉지 수 + 낱개 수 = ","",Integer.toString(v[3]));q.studyGuide=g;
 }
}
