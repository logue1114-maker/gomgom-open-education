package com.gomgomapps.math.core;
import java.util.*;
/** Visible collections through100; learner grouping is presentation state, never question identity. */
public final class CollectionGrouping {
 private CollectionGrouping(){}
 public static final List<Catalog.Skill> SKILLS=List.of(new Catalog.Skill("collectionCount","묶어서 수 세기",2,1,1,"","collectionCount",100,"count","물건을 묶어 보기 쉽게 배열하고 센다."));
 public static boolean supports(String id){return id.equals("collectionCount");}
 static Question next(Catalog.Skill s,Random random,CurriculumLimits limits,Map<String,Integer> recent){
  Map<String,Question> pool=new LinkedHashMap<>();int max=Math.min(100,limits.wholeMaximum(100));String[] sizes=(limits==CurriculumLimits.NONE?List.of(2,5,10):limits.objectGroupSizes()).stream().map(String::valueOf).toArray(String[]::new);
  for(int n=limits.includeZeroCount()?0:1;n<=max;n++)for(int layout=0;layout<(n==0?1:4);layout++){
   Question q=new Question(s.id,"동그라미는 모두 몇 개인가요?","",""+n);q.stepSupport=false;q.diagram=new StudyDiagram("countCollection",new double[]{n,layout},sizes);if(limits.allows(q))pool.put(q.signature(),q);
  }
  Question q=FactFoundations.choose(pool,random,recent);attach(q);return q;
 }
 public static List<Integer> sizes(Question q){if(q==null||q.diagram==null||!supports(q.skillId))return List.of();return Arrays.stream(q.diagram.labels).map(Integer::valueOf).toList();}
 public static void group(Question q,int size){if(size!=0&&!sizes(q).contains(size))throw new IllegalArgumentException("Group size outside this curriculum");q.collectionGroupSize=size;}
 public static int selected(Question q){return q.collectionGroupSize==null?0:q.collectionGroupSize;}
 /** Normalized positions: one dot per visible object, with no overlap or answer text. */
 public static double[][] points(Question q,int size){
  int n=(int)q.diagram.values[0],variant=(int)q.diagram.values[1];if(n<0||n>100)throw new IllegalArgumentException("Collection range");double[][] result=new double[n][2];
  if(size==0){
   for(int i=0;i<n;i++){int cell=(i*37+variant*23)%100;result[i][0]=.04+(cell%10+.5+(((cell+variant*3)%5)-2)*.07)*.092;result[i][1]=.04+(cell/10+.5+(((cell*3+variant)%5)-2)*.07)*.092;}
  }else{
   if(!sizes(q).contains(size))throw new IllegalArgumentException("Grouping outside curriculum");int cells=Math.max(1,(n+size-1)/size),columns=Math.min(10,cells),rows=(cells+columns-1)/columns,dotColumns=size>5?5:size,dotRows=(size+dotColumns-1)/dotColumns;
   for(int i=0;i<n;i++){int cell=i/size,index=i%size;result[i][0]=(.04+((cell%columns)+(index%dotColumns+1.0)/(dotColumns+1))*.92/columns);result[i][1]=(.04+((cell/columns)+(index/dotColumns+1.0)/(dotRows+1))*.92/rows);}
  }
  return result;
 }
 public static void attach(Question q){
  if(q==null||!supports(q.skillId)||q.diagram==null)return;int n=points(q,0).length;
  StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="collection-grouping-v1";
  g.step("5개씩 묶어 보세요. 완성된 묶음 수를 쓰세요.","5개씩 묶음 수 = ","",""+(n/5));
  g.step("묶인 동그라미는 모두 몇 개인가요?","묶인 수 = ","",""+(n/5*5));
  g.step("묶이지 않은 동그라미 수를 쓰세요.","남은 수 = ","",""+(n%5));
  g.step("묶인 수와 남은 수를 모으세요.","모두 = ","",""+n);q.studyGuide=g;
 }
}
