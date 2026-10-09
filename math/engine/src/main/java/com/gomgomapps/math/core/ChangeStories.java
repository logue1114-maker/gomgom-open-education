package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
/** Quantity-change stories with public givens and a learner-authored unknown. */
public final class ChangeStories {
 private ChangeStories(){}
 public static final String ID="quantityChangeStories";
 public static final Catalog.Skill SKILL=new Catalog.Skill(ID,"늘고 줄어든 수 구하기",3,1,1,"",ID,1000,"add1000,sub1000","처음 수·늘거나 줄어든 수·지금 수의 관계로 빈칸을 구한다.");
 private static final String[] OBJECTS={"스티커","색연필"};
 private static final Pattern PROMPT=Pattern.compile("^(스티커|색연필)가 (□|[0-9]{1,4})개 있었어요\\.\\n(□|[0-9]{1,4})개를 (더 받았어요|나눠 줬어요)\\.\\n지금은 (□|[0-9]{1,4})개예요\\.\\n빈칸의 수를 쓰세요\\.$");
 public static boolean supports(String id){return ID.equals(id);}
 static Question make(int context,boolean add,int whole,int part,int blank){
  if(context<0||context>1||whole<0||whole>1000||part<0||part>whole||blank<0||blank>2)throw new IllegalArgumentException("quantity change domain");
  int[] values=add?new int[]{part,whole-part,whole}:new int[]{whole,part,whole-part};String[] shown=Arrays.stream(values).mapToObj(Integer::toString).toArray(String[]::new);shown[blank]="□";
  Question q=new Question(ID,OBJECTS[context]+"가 "+shown[0]+"개 있었어요.\n"+shown[1]+"개를 "+(add?"더 받았어요":"나눠 줬어요")+".\n지금은 "+shown[2]+"개예요.\n빈칸의 수를 쓰세요.","",Integer.toString(values[blank]));q.kind="changeStory";q.labels=new String[]{"빈칸의 수"};q.stepSupport=false;return q;
 }
 // before/change/after, addition flag, blank index, derived answer
 public static int[] read(Question q){
  if(q==null||!supports(q.skillId)||q.prompt==null)return null;Matcher m=PROMPT.matcher(q.prompt);if(!m.matches())return null;boolean add=m.group(4).equals("더 받았어요");String[] shown={m.group(2),m.group(3),m.group(5)};int[] values=new int[6];int blank=-1;
  for(int i=0;i<3;i++){if(shown[i].equals("□")){if(blank>=0)return null;blank=i;}else{values[i]=Integer.parseInt(shown[i]);if(values[i]>1000)return null;}}
  if(blank<0)return null;values[blank]=blank==2?(add?values[0]+values[1]:values[0]-values[1]):blank==0?(add?values[2]-values[1]:values[2]+values[1]):(add?values[2]-values[0]:values[0]-values[2]);
  if(values[blank]<0||values[blank]>1000)return null;values[3]=add?1:0;values[4]=blank;values[5]=values[blank];return values;
 }
 static Question next(Random random,CurriculumLimits limits,Map<String,Integer> recent){return nextDomain(random,limits,recent,limits.wholeMaximum(1000));}
 static Question nextDomain(Random random,CurriculumLimits limits,Map<String,Integer> recent,int max){
  if(max<0||max>1000)throw new IllegalArgumentException("quantity change maximum");
  for(int i=0;i<64;i++){int whole=random.nextInt(max+1),part=random.nextInt(whole+1);Question q=make(random.nextInt(2),random.nextBoolean(),whole,part,random.nextInt(3));if(limits.allows(q)&&!recent.containsKey(q.signature())){attach(q);return q;}}
  Question oldest=null;int age=Integer.MAX_VALUE;
  for(int whole=0;whole<=max;whole++)for(int part=0;part<=whole;part++)for(int context=0;context<2;context++)for(boolean add:new boolean[]{true,false})for(int blank=0;blank<3;blank++){
   Question q=make(context,add,whole,part,blank);if(!limits.allows(q))continue;Integer seen=recent.get(q.signature());if(seen==null){attach(q);return q;}if(seen<age){age=seen;oldest=q;}
  }
  if(oldest==null)throw new IllegalStateException("No quantity-change story matches curriculum");attach(oldest);return oldest;
 }
 static Checker.Result check(Question q,List<String> answers){int[] v=read(q);if(v==null||answers.size()!=1||answers.get(0)==null||!answers.get(0).trim().matches("[0-9]{1,4}"))return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"수 입력 필요");return Integer.parseInt(answers.get(0).trim())==v[5]?new Checker.Result(Checker.Status.CORRECT,-1,"정답"):new Checker.Result(Checker.Status.WRONG_ANSWER,0,"이 수 확인");}
 public static void attach(Question q){
  int[] v=read(q);if(v==null)return;boolean add=v[3]==1;int blank=v[4];int first=blank==0||blank==1&&add?2:0,second=blank==1?(add?0:2):1;
  String change=add?"더 받은 수":"나눠 준 수";String[] labels={"처음 수",change,"지금 수"};String[] instructions={"처음 있던 수를 쓰세요.",add?"더 받은 수를 쓰세요.":"나눠 준 수를 쓰세요.","지금 있는 수를 쓰세요."};
  StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="quantity-change-stories-v1";g.step(instructions[first],labels[first]+" = ","",Integer.toString(v[first]));g.step(instructions[second],labels[second]+" = ","",Integer.toString(v[second]));
  boolean sum=blank==2?add:blank==0&&!add;String relationship=labels[first]+(sum?" + ":" − ")+labels[second];g.step(sum?labels[first]+"에 "+labels[second]+"를 더하세요.":labels[first]+"에서 "+labels[second]+"를 빼세요.",relationship+" = ","",Integer.toString(v[5]));q.studyGuide=g;
 }
}
