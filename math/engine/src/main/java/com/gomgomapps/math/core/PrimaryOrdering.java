package com.gomgomapps.math.core;
import java.util.*;
/** Verbal comparison, three-number ordering and visible ordinal positions. No comparison symbols. */
public final class PrimaryOrdering {
 private PrimaryOrdering(){}
 public static final List<Catalog.Skill> SKILLS=List.of(
  new Catalog.Skill("numberCompareWords","수 비교하기",1,1,1,"","primaryOrdering",20,"count","두 수를 말로 비교한다."),
  new Catalog.Skill("objectCompareWords","물건 수 비교하기",1,1,1,"","primaryOrdering",20,"count","두 그림의 물건 수를 비교한다."),
  new Catalog.Skill("numberOrder","수 순서대로 쓰기",1,1,3,"","primaryOrdering",20,"count","수를 작은 순서나 큰 순서로 쓴다."),
  new Catalog.Skill("objectOrdinal","몇 번째일까요",1,1,1,"","primaryOrdering",10,"count","시작 방향에 따라 물건의 순서를 센다."));
 public static boolean supports(String id){return SKILLS.stream().anyMatch(s->s.id.equals(id));}
 static Question next(Catalog.Skill s,Random random,CurriculumLimits limits,Map<String,Integer> recent){
  int maximum=Math.min(s.id.equals("objectOrdinal")?10:20,limits.wholeMaximum(s.range));
  List<int[]> domain=conditions(s.id,maximum);
  // Normal practice selects a fresh public condition directly. Exhaustive selection is only needed
  // near exhaustion or for additional restrictive curriculum rules, not on every phone question.
  for(int attempt=0;attempt<64;attempt++){
   Question q=make(s.id,domain.get(random.nextInt(domain.size())));
   if(limits.allows(q)&&!recent.containsKey(q.signature())){attach(q);return q;}
  }
  // Keep only compact public-condition records while selecting; attach help to the selected item.
  Question selected=null;int fresh=0,oldest=Integer.MAX_VALUE,oldCount=0;
  for(int[] values:domain){
   Question candidate=make(s.id,values);if(!limits.allows(candidate))continue;
   Integer age=recent.get(candidate.signature());
   if(age==null){if(random.nextInt(++fresh)==0)selected=candidate;}
   else if(fresh==0){if(age<oldest){oldest=age;oldCount=0;}if(age==oldest&&random.nextInt(++oldCount)==0)selected=candidate;}
  }
  if(selected==null)throw new IllegalStateException("No primary ordering condition fits curriculum");
  attach(selected);return selected;
 }
 static List<int[]> conditions(String id,int max){
  List<int[]> result=new ArrayList<>();
  if(id.equals("numberOrder")){
   // All ordered presentations of three different public numbers, in both requested directions.
   for(int a=0;a<=max;a++)for(int b=0;b<=max;b++)if(a!=b)for(int c=0;c<=max;c++)if(c!=a&&c!=b)
    for(int reverse=0;reverse<2;reverse++)result.add(new int[]{a,b,c,reverse});
  }else if(id.equals("objectOrdinal")){
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
  }else if(id.equals("objectOrdinal")){
   q=new Question(id,(v[2]==0?"왼쪽":"오른쪽")+"부터 세면 별은 몇 번째인가요?","",""+(v[2]==0?v[1]+1:v[0]-v[1]));
   q.diagram=new StudyDiagram("primaryOrdinal",Arrays.stream(v).asDoubleStream().toArray());
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
  }else if(q.skillId.equals("objectOrdinal")){
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
