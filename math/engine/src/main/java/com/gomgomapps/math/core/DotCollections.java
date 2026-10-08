package com.gomgomapps.math.core;
import java.util.*;
/** Small public dot-card arrangements, not hidden numeric answer labels. */
final class DotCollections {
 static final List<Catalog.Skill> SKILLS=List.of(new Catalog.Skill("el_subitise","한눈에 수 알기",1,1,1,"","dotFlash",5,"count","동그라미가 놓인 모양을 보고 수를 알아봅니다."));
 static boolean supports(String id){return id.equals("el_subitise");}
 static Question next(Catalog.Skill skill,Random random,CurriculumLimits limits,Map<String,Integer> recent){
  Map<String,Question> candidates=new LinkedHashMap<>();
  int maximum=Math.min(limits.groupedSubitise()?10:6,limits.wholeMaximum(5));
  if(limits.groupedSubitise()){
   for(int count=1;count<=maximum;count++)for(int layout=0;layout<4;layout++){
    List<double[]> dots=new ArrayList<>();for(int i=0;i<count;i++){double x=.12+(i%5)*.19,y=i<5?.3:.7;dots.add(new double[]{(layout&1)==0?x:1-x,(layout&2)==0?y:1-y});}
    // Sort public positions before signing: mirrored full rows are the same visible condition.
    dots.sort(Comparator.<double[]>comparingDouble(point->point[0]).thenComparingDouble(point->point[1]));
    double[] points=new double[1+2*count];for(int i=0;i<count;i++){points[2*i+1]=Math.round(dots.get(i)[0]*100)/100.0;points[2*i+2]=Math.round(dots.get(i)[1]*100)/100.0;}
    Question q=make(skill.id,points,maximum);if(limits.allows(q))candidates.putIfAbsent(q.signature(),q);
   }
   Question selected=FactFoundations.choose(candidates,random,recent);attach(selected);return selected;
  }
  for(int mask=1;mask<512;mask++){
   int count=Integer.bitCount(mask);if(count>maximum)continue;
   double[] points=new double[1+2*count];int at=1;
   for(int cell=0;cell<9;cell++)if((mask&(1<<cell))!=0){points[at++]=.25+(cell%3)*.25;points[at++]=.25+(cell/3)*.25;}
   Question q=make(skill.id,points,maximum);
   if(limits.allows(q))candidates.put(q.signature(),q);
  }
  Question selected=FactFoundations.choose(candidates,random,recent);attach(selected);return selected;
 }
 private static Question make(String id,double[] points,int maximum){Question q=new Question(id,"동그라미는 모두 몇 개인가요?","",String.valueOf((points.length-1)/2)).withInputs(maximum);q.stepSupport=false;q.diagram=new StudyDiagram("dotFlash",points);return q;}
 static void attach(Question q){int count=(q.diagram.values.length-1)/2;StudyGuide guide=new StudyGuide().transfer(false);guide.teachingVersion="dot-recount-v1";guide.step("동그라미를 하나씩 세어 확인하세요.","센 수 = ","",""+count);q.studyGuide=guide;}
 static void choices(Question q,Random random){
  int answer=Integer.parseInt(q.answers[0]),maximum=q.choiceInputs.length==0?Math.max(5,answer):q.choiceInputs[0].n.intValue();List<Integer> pool=new ArrayList<>();for(int n=1;n<=maximum;n++)if(n!=answer)pool.add(n);Collections.shuffle(pool,random);
  List<Integer> options=new ArrayList<>(pool.subList(0,Math.min(3,pool.size())));options.add(answer);Collections.shuffle(options,random);
  for(int value:options){if(value==answer)q.correctChoice=q.choices.size();q.choices.add(String.valueOf(value));q.distractorReasons.add(value==answer?"정답":"동그라미의 배치 확인");}
 }
}
