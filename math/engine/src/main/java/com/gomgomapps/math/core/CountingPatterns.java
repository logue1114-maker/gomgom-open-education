package com.gomgomapps.math.core;
import java.util.*;

/** Exact grouped collections and additive patterns, without multiplication prerequisites. */
public final class CountingPatterns {
 private CountingPatterns(){}
 public static final List<Catalog.Skill> SKILLS=List.of(
  new Catalog.Skill("groupedCount","묶어서 수 세기",1,1,1,"","groupedCount",50,"count","같은 크기의 묶음을 세고 남은 동그라미를 더한다."),
  new Catalog.Skill("numberSteps","수 배열의 빈칸",1,1,1,"","numberSteps",99,"add9,sub9","늘거나 줄어드는 수의 차이를 찾아 빈칸을 채운다."));
 public static boolean supports(String id){return id.equals("groupedCount")||id.equals("numberSteps");}
 static Question next(Catalog.Skill skill,Random random,CurriculumLimits limits,Map<String,Integer> recent){
  Map<String,Question> pool=new LinkedHashMap<>();
  if(skill.id.equals("groupedCount")){
   for(int size=2;size<=5;size++)for(int groups=1;groups<=10;groups++)for(int loose=0;loose<size;loose++){
    int total=size*groups+loose;if(total>50)continue;
    Question q=new Question(skill.id,"동그라미는 모두 몇 개인가요?","",String.valueOf(total));q.stepSupport=false;
    q.diagram=new StudyDiagram("groupedCollection",new double[]{size,groups,loose});
    q.studyGuide=new StudyGuide().transfer(false)
     .step("한 묶음의 동그라미를 세세요.","한 묶음 = ","",String.valueOf(size))
     .step("동그라미 묶음이 몇 개인지 세세요.","묶음 수 = ","",String.valueOf(groups))
     .step("묶음 밖의 동그라미를 세세요.","남은 수 = ","",String.valueOf(loose))
     .step("묶음 안의 동그라미를 모두 세세요.","묶음 안의 수 = ","",String.valueOf(size*groups))
     .step("묶음 안의 수에 남은 수를 더하세요.","전체 = ","",String.valueOf(total));
    include(pool,q,limits);
   }
  }else{
   for(int delta=1;delta<=5;delta++)for(int start=0;start+4*delta<=99;start++)for(int blank=2;blank<=4;blank++)for(boolean descending:new boolean[]{false,true}){
    int first=descending?start+4*delta:start,step=descending?-delta:delta;StringBuilder prompt=new StringBuilder();
    for(int i=0;i<5;i++){if(i>0)prompt.append(" → ");prompt.append(i==blank?"□":String.valueOf(first+i*step));}
    int previous=first+(blank-1)*step,answer=first+blank*step;Question q=new Question(skill.id,prompt.toString(),previous+(descending?" - ":" + ")+delta,String.valueOf(answer));q.stepSupport=false;
    q.studyGuide=new StudyGuide().transfer(false)
     .step("이웃한 두 수의 차이를 구하세요.",descending?first+" − "+(first+step)+" = ":(first+step)+" − "+first+" = ","",String.valueOf(delta))
     .step(descending?"빈칸 앞의 수에서 같은 차이를 빼세요.":"빈칸 앞의 수에 같은 차이를 더하세요.",previous+(descending?" − ":" + ")+delta+" = ","",String.valueOf(answer));
    include(pool,q,limits);
   }
  }
  return FactFoundations.choose(pool,random,recent);
 }
 private static void include(Map<String,Question> pool,Question q,CurriculumLimits limits){if(limits.allows(q))pool.put(q.signature(),q);}
 static void choices(Question q,Random random){
  int answer=Integer.parseInt(q.answers[0]),maximum=q.skillId.equals("groupedCount")?50:99;
  List<Integer> pool=new ArrayList<>();for(int n=0;n<=maximum;n++)if(n!=answer)pool.add(n);Collections.shuffle(pool,random);
  List<Integer> options=new ArrayList<>(pool.subList(0,3));options.add(answer);Collections.shuffle(options,random);
  for(int n:options){if(n==answer)q.correctChoice=q.choices.size();q.choices.add(String.valueOf(n));q.distractorReasons.add(n==answer?"정답":"그림이나 수의 배열 확인");}
 }
}
