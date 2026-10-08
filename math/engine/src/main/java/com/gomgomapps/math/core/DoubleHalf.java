package com.gomgomapps.math.core;
import java.util.*;
import java.util.regex.*;
/** Explicit doubling and halving from public objects, with empty student inputs. */
public final class DoubleHalf {
 private DoubleHalf(){}
 public static final List<Catalog.Skill> SKILLS=List.of(
  new Catalog.Skill("objectDouble","두 배",1,1,1,"","doubleHalf",100,"count","같은 수를 한 번 더 모은다."),
  new Catalog.Skill("objectHalf","절반",1,1,1,"","doubleHalf",50,"count","물건을 똑같이 둘로 나눈다."),
  new Catalog.Skill("objectHalfRemainder","둘로 나누고 남은 수",2,1,2,"","doubleHalf",19,"count","물건을 둘로 나누고 한쪽 몫과 남은 수를 구한다."));
 public static boolean supports(String id){return SKILLS.stream().anyMatch(s->s.id.equals(id));}
 static Question next(Catalog.Skill skill,Random random,CurriculumLimits limits,Map<String,Integer> recent){
  Map<String,Question> pool=new LinkedHashMap<>();
  int maximum=limits.wholeMaximum(skill.id.equals("objectDouble")?50:skill.id.equals("objectHalf")?50:19);
  for(int amount=1;amount<=maximum;amount++){
   if(!limits.doubleHalfInputs().isEmpty()&&!limits.doubleHalfInputs().contains(amount))continue;
   if(skill.id.equals("objectHalf")&&amount%2!=0)continue;
   if(skill.id.equals("objectHalfRemainder")&&(amount<3||amount%2==0))continue;
   boolean twice=skill.id.equals("objectDouble"),remainder=skill.id.equals("objectHalfRemainder");
   String prompt=twice?"물건 "+amount+"개의 두 배는 몇 개인가요?":remainder?"물건 "+amount+"개를 똑같이 둘로 나눠요.\n한쪽 몫과 남은 수를 쓰세요.":"물건 "+amount+"개의 절반은 몇 개인가요?";
   Question q=new Question(skill.id,prompt,"",remainder?new String[]{""+(amount/2),"1"}:new String[]{""+(twice?amount*2:amount/2)});
   if(remainder)q.labels=new String[]{"한쪽 몫","남은 수"};q.stepSupport=false;
   q.diagram=twice?new StudyDiagram("doubleObjects",new double[]{amount}):new StudyDiagram("sharingObjects",new double[]{amount,2,0});
   attach(q);if(limits.allows(q))pool.put(q.signature(),q);
  }
  return FactFoundations.choose(pool,random,recent);
 }
 public static void attach(Question q){
  if(q==null||!supports(q.skillId))return;
  Matcher m=Pattern.compile("^물건 (\\d+)개").matcher(q.prompt);if(!m.find())return;
  int amount=Integer.parseInt(m.group(1));boolean twice=q.skillId.equals("objectDouble");
  StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="double-half-v1";
  g.step("처음 물건 수를 쓰세요.","물건 수 = ","",""+amount);
  if(twice){
   g.step("같은 수를 한 번 더 모아요. 더 모을 수를 쓰세요.","더 모을 수 = ","",""+amount);
   g.step("모은 물건을 모두 세세요.","두 배 = ","",""+(amount*2));
  }else{
   g.step("물건을 두 개씩 짝지어요. 짝의 수를 쓰세요.","짝의 수 = ","",""+(amount/2));
   g.step("짝을 짓지 못한 물건 수를 쓰세요.","남은 수 = ","",""+(amount%2));
   g.step("짝마다 하나씩 나누면 한쪽에 몇 개가 있나요?","한쪽 몫 = ","",""+(amount/2));
  }
  q.studyGuide=g;
 }
}
