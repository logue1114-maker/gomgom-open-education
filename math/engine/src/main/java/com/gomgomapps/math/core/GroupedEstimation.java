package com.gomgomapps.math.core;
import java.util.*;
/** An estimate is a learner observation, not an exact-answer test. Counting is scored separately. */
public final class GroupedEstimation {
 private GroupedEstimation(){}
 public static final String ID="collectionEstimate";
 public static final List<Catalog.Skill> SKILLS=List.of(new Catalog.Skill(ID,"어림하고 세어 보기",2,1,1,"","groupedEstimation",30,"count","어림한 수를 남기고 묶어서 세어 확인한다."));
 public static boolean supports(String id){return ID.equals(id);}
 public static boolean counting(Question q){return supports(q.skillId)&&q.learnerEstimate!=null;}
 static Question next(Catalog.Skill skill,Random random,CurriculumLimits limits,Map<String,Integer> recent){
  Map<String,Question> pool=new LinkedHashMap<>();int maximum=Math.min(30,limits.wholeMaximum(30));
  for(int n=1;n<=maximum;n++)for(int layout=0;layout<4;layout++){Question q=new Question(ID,"몇 개쯤 될까요?","",""+n);q.kind="estimateCount";q.labels=new String[]{"어림한 수"};q.stepSupport=false;q.diagram=new StudyDiagram("countCollection",new double[]{n,layout},"5");if(limits.allows(q))pool.put(q.signature(),q);}
  return FactFoundations.choose(pool,random,recent);
 }
 /** Commits the student's own guess; does not complete a question or change a timer or error count. */
 public static boolean beginCounting(Learning.Session session){
  Question q=session.question;if(!supports(q.skillId)||counting(q)||session.answers.isEmpty())return false;
  String guess=session.answers.get(0).trim();if(!guess.matches("[0-9]{1,9}"))return false;
  q.learnerEstimate=guess;q.collectionGroupSize=5;q.labels=new String[]{"센 수"};session.answers.set(0,"");attach(q);return true;
 }
 public static void attach(Question q){
  if(!counting(q))return;int n=CollectionGrouping.points(q,0).length;StudyGuide guide=new StudyGuide().transfer(false);guide.teachingVersion="estimate-recount-v1";
  guide.step("완성된 5개 묶음 수를 쓰세요.","5개씩 묶음 수 = ","",""+(n/5));
  guide.step("남은 동그라미 수를 쓰세요.","남은 수 = ","",""+(n%5));
  guide.step("묶음과 남은 동그라미를 모두 세세요.","센 수 = ","",""+n);q.studyGuide=guide;
 }
 static Checker.Result phaseGuard(Question q){return counting(q)?null:new Checker.Result(Checker.Status.INPUT_NEEDED,-1,"어림한 수 먼저 입력");}
}
