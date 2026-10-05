package com.gomgomapps.math.core;
import java.util.*;
/** Small public dot-card arrangements, not hidden numeric answer labels. */
final class DotCollections {
 static final List<Catalog.Skill> SKILLS=List.of(new Catalog.Skill("el_subitise","한눈에 수 알기",1,1,1,"","dotFlash",5,"count","동그라미가 놓인 모양을 보고 수를 알아봅니다."));
 static boolean supports(String id){return id.equals("el_subitise");}
 static Question next(Catalog.Skill skill,Random random,CurriculumLimits limits,Map<String,Integer> recent){
  Map<String,Question> candidates=new LinkedHashMap<>();
  for(int mask=1;mask<512;mask++){
   int count=Integer.bitCount(mask);if(count>5)continue;
   double[] points=new double[1+2*count];int at=1;
   for(int cell=0;cell<9;cell++)if((mask&(1<<cell))!=0){points[at++]=.25+(cell%3)*.25;points[at++]=.25+(cell/3)*.25;}
   Question q=new Question(skill.id,"동그라미는 모두 몇 개인가요?","",String.valueOf(count));q.stepSupport=false;q.diagram=new StudyDiagram("dotFlash",points);
   if(limits.allows(q))candidates.put(q.signature(),q);
  }
  return FactFoundations.choose(candidates,random,recent);
 }
 static void choices(Question q,Random random){
  int answer=Integer.parseInt(q.answers[0]);List<Integer> pool=new ArrayList<>(List.of(1,2,3,4,5));pool.remove(Integer.valueOf(answer));Collections.shuffle(pool,random);
  List<Integer> options=new ArrayList<>(pool.subList(0,3));options.add(answer);Collections.shuffle(options,random);
  for(int value:options){if(value==answer)q.correctChoice=q.choices.size();q.choices.add(String.valueOf(value));q.distractorReasons.add(value==answer?"정답":"동그라미의 배치 확인");}
 }
}
