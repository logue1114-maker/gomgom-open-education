package com.gomgomapps.math.core;
import java.util.*;
/** Verbal comparison, three-number ordering and visible ordinal positions. No comparison symbols. */
public final class PrimaryOrdering {
 private PrimaryOrdering(){}
 public static final List<Catalog.Skill> SKILLS=List.of(
  new Catalog.Skill("numberCompareWords","수 비교하기",1,1,1,"","primaryOrdering",20,"count","두 수를 말로 비교한다."),
  new Catalog.Skill("objectCompareWords","물건 수 비교하기",1,1,1,"","primaryOrdering",20,"count","두 그림의 물건 수를 비교한다."),
  new Catalog.Skill("numberOrder","수 순서대로 쓰기",1,1,3,"","primaryOrdering",20,"count","수를 작은 순서나 큰 순서로 쓴다."),
  new Catalog.Skill("objectOrdinal","몇 번째일까요",1,1,1,"","primaryOrdering",10,"count","시작 방향에 따라 물건의 순서를 센다."),
  new Catalog.Skill("ordinalName","순서 이름과 기호",1,1,1,"","primaryOrdering",10,"count","첫째부터 열째까지 순서의 이름과 기호를 연결한다."));
 static boolean ordinal(String id){return id.equals("objectOrdinal")||id.equals("ordinalName");}
 private static final String[] ORDINAL_NAMES={"첫째 (1st)","둘째 (2nd)","셋째 (3rd)","넷째 (4th)","다섯째 (5th)","여섯째 (6th)","일곱째 (7th)","여덟째 (8th)","아홉째 (9th)","열째 (10th)"};
 public static boolean supports(String id){return SKILLS.stream().anyMatch(s->s.id.equals(id));}
 static Question next(Catalog.Skill s,Random random,CurriculumLimits limits,Map<String,Integer> recent){
  int cap=s.id.equals("ordinalName")?10:s.id.equals("objectOrdinal")?30:s.id.equals("numberOrder")?500:20;
  int maximum=Math.min(cap,limits.wholeMaximum(s.range)),size=conditionCount(s.id,maximum);
  // Normal practice selects a fresh public condition directly. Exhaustive selection is only needed
  // near exhaustion or for additional restrictive curriculum rules, not on every phone question.
  for(int attempt=0;attempt<64;attempt++){
   Question q=make(s.id,conditionAt(s.id,maximum,random.nextInt(size)));
   if(limits.allows(q)&&!recent.containsKey(q.signature()))return finish(q,random);
  }
  // Keep only compact public-condition records while selecting; attach help to the selected item.
  Question selected=null;int oldest=Integer.MAX_VALUE,oldCount=0,start=random.nextInt(size);
  for(int offset=0;offset<size;offset++){
   Question candidate=make(s.id,conditionAt(s.id,maximum,(start+offset)%size));if(!limits.allows(candidate))continue;
   Integer age=recent.get(candidate.signature());
   if(age==null)return finish(candidate,random);
   if(age<oldest){oldest=age;oldCount=0;}if(age==oldest&&random.nextInt(++oldCount)==0)selected=candidate;
  }
  if(selected==null)throw new IllegalStateException("No primary ordering condition fits curriculum");
  return finish(selected,random);
 }
 private static Question finish(Question q,Random random){
  if(q.skillId.equals("ordinalName")){
   List<String> distractors=new ArrayList<>(q.choiceLabels.keySet());distractors.remove(q.answers[0]);Collections.shuffle(distractors,random);
   Set<String> retained=new HashSet<>(distractors.subList(0,3));retained.add(q.answers[0]);q.choiceLabels.keySet().retainAll(retained);
  }
  attach(q);return q;
 }
 static int conditionCount(String id,int max){
  return id.equals("numberOrder")?(max+1)*max*(max-1)*2:ordinal(id)?max*(max+1):max*max;
 }
 /** Decode a public condition without allocating the cubic sorting domain. */
 static int[] conditionAt(String id,int max,int index){
  if(index<0||index>=conditionCount(id,max))throw new IndexOutOfBoundsException();
  if(id.equals("numberOrder")){
   int reverse=index%2,k=index/2,n=max+1,a=k/((n-1)*(n-2));k%=(n-1)*(n-2);
   int b=k/(n-2);if(b>=a)b++;
   int c=k%(n-2),low=Math.min(a,b),high=Math.max(a,b);if(c>=low)c++;if(c>=high)c++;
   return new int[]{a,b,c,reverse};
  }
  if(ordinal(id)){int count=1;while(index>=count*2)index-=count++*2;return new int[]{count,index/2,index%2};}
  return new int[]{index/max+1,index%max+1};
 }
 static List<int[]> conditions(String id,int max){
  List<int[]> result=new ArrayList<>();
  if(id.equals("numberOrder")){
   // All ordered presentations of three different public numbers, in both requested directions.
   for(int a=0;a<=max;a++)for(int b=0;b<=max;b++)if(a!=b)for(int c=0;c<=max;c++)if(c!=a&&c!=b)
    for(int reverse=0;reverse<2;reverse++)result.add(new int[]{a,b,c,reverse});
  }else if(ordinal(id)){
   for(int count=1;count<=max;count++)for(int mark=0;mark<count;mark++)for(int right=0;right<2;right++)result.add(new int[]{count,mark,right});
  }else for(int a=1;a<=max;a++)for(int b=1;b<=max;b++)result.add(new int[]{a,b});
  return result;
 }
 static Map<String,String> comparisonOptions(String id){boolean objects=id.equals("objectCompareWords");Map<String,String> result=new LinkedHashMap<>();result.put("0",objects?"왼쪽에 더 적어요":"왼쪽이 더 작아요");result.put("1",objects?"양쪽 수가 같아요":"같아요");result.put("2",objects?"왼쪽에 더 많아요":"왼쪽이 더 커요");return result;}
 static Question make(String id,int[] v){
  Question q;
  if(id.equals("numberOrder")){
   int[] sorted=Arrays.copyOf(v,3);Arrays.sort(sorted);if(v[3]==1){int t=sorted[0];sorted[0]=sorted[2];sorted[2]=t;}
   q=new Question(id,v[3]==0?"작은 수부터 쓰세요.":"큰 수부터 쓰세요.","",Arrays.stream(sorted).mapToObj(String::valueOf).toArray(String[]::new));
   q.labels=new String[]{"첫 번째 수","두 번째 수","세 번째 수"};q.diagram=new StudyDiagram("primaryNumberOrder",Arrays.stream(v).asDoubleStream().toArray());
  }else if(ordinal(id)){
   q=new Question(id,(v[2]==0?"왼쪽":"오른쪽")+"부터 세면 별은 몇 번째인가요?","",""+(v[2]==0?v[1]+1:v[0]-v[1]));
   q.diagram=new StudyDiagram("primaryOrdinal",Arrays.stream(v).asDoubleStream().toArray());
   if(id.equals("ordinalName")){
    q.prompt=v[2]==0?"왼쪽부터 세어 별의 순서 이름과 기호를 고르세요.":"오른쪽부터 세어 별의 순서 이름과 기호를 고르세요.";
    // Stable numeric keys; labels are localized only when displayed. Three wrong
    // names are independently sampled in finish, then Generator shuffles positions.
    for(int option=1;option<=10;option++)q.choiceLabels.put(""+option,ORDINAL_NAMES[option-1]);
   }
  }else{
   q=new Question(id,id.equals("objectCompareWords")?"왼쪽과 오른쪽의 물건 수를 비교하세요.":"왼쪽 수와 오른쪽 수를 비교하세요.","",""+(Integer.compare(v[0],v[1])+1));
   q.diagram=new StudyDiagram(id.equals("objectCompareWords")?"primaryObjectPair":"primaryNumberPair",Arrays.stream(v).asDoubleStream().toArray());
   q.choiceLabels.putAll(comparisonOptions(id));
  }
  q.stepSupport=false;return q;
 }
 public static void attach(Question q){
  if(q==null||!supports(q.skillId)||q.diagram==null)return;double[] v=q.diagram.values;StudyGuide guide=new StudyGuide().transfer(false);guide.teachingVersion="primary-ordering-v1";
  if(q.skillId.equals("numberOrder")){
   int[] sorted=Arrays.stream(v).limit(3).mapToInt(n->(int)n).sorted().toArray();boolean backwards=v[3]==1;
   if(backwards){int t=sorted[0];sorted[0]=sorted[2];sorted[2]=t;}
   guide.step(backwards?"가장 큰 수를 쓰세요.":"가장 작은 수를 쓰세요.","첫 번째 수 = ","",""+sorted[0]);
   guide.step(backwards?"남은 두 수 중 큰 수를 쓰세요.":"남은 두 수 중 작은 수를 쓰세요.","두 번째 수 = ","",""+sorted[1]);
   guide.step("마지막 남은 수를 쓰세요.","세 번째 수 = ","",""+sorted[2]);
  }else if(ordinal(q.skillId)){
   int before=v[2]==0?(int)v[1]:(int)v[0]-(int)v[1]-1;
   guide.step("시작 쪽에서 별 앞까지 동그라미를 세세요.","별 앞의 수 = ","",""+before);
   guide.step("별도 하나로 세면 몇 번째인가요?","순서 = ","",""+(before+1));
  }else{
   guide.step("왼쪽 수를 쓰세요.","왼쪽 = ","",""+(int)v[0]);
   guide.step("오른쪽 수를 쓰세요.","오른쪽 = ","",""+(int)v[1]);
   guide.choice("왼쪽과 오른쪽을 비교하세요.",comparisonOptions(q.skillId),""+(Double.compare(v[0],v[1])+1));
  }
  q.studyGuide=guide;
 }
}
