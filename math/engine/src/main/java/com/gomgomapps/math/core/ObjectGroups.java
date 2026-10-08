package com.gomgomapps.math.core;
import java.util.*;
import java.util.regex.*;
/** Public objects and empty sharing spaces; operation symbols are not prerequisites. */
public final class ObjectGroups {
 private ObjectGroups(){}
 public static final List<Catalog.Skill> SKILLS=List.of(
  new Catalog.Skill("objectGroupTotal","같은 묶음 세기",1,1,1,"","objectGroups",50,"count","같은 크기의 묶음에 있는 물건을 모두 센다."),
  new Catalog.Skill("objectShare","똑같이 나누기",1,1,1,"","objectGroups",50,"count","물건을 하나씩 나누어 한 사람 몫을 구한다."),
  new Catalog.Skill("objectGroupRemainder","묶고 남은 수",1,1,1,"","objectGroups",50,"count","같은 크기로 묶고 묶음 수와 남은 수를 구한다."));
 public static boolean supports(String id){return SKILLS.stream().anyMatch(s->s.id.equals(id));}
 static Question next(Catalog.Skill skill,Random random,CurriculumLimits limits,Map<String,Integer> recent){
  Map<String,Question> pool=new LinkedHashMap<>();int maximum=Math.min(50,limits.wholeMaximum(50));
  for(int unit:limits.objectGroupSizes())for(int total=unit;total<=maximum;total++){
   int groups=total/unit,left=total%unit;
   if(skill.id.equals("objectShare")&&left!=0)continue;
   if(skill.id.equals("objectGroupTotal")&&(left!=0||groups>10))continue;
   String prompt=skill.id.equals("objectGroupTotal")?"한 묶음에 "+unit+"개씩 "+groups+"묶음이 있어요.\n모두 몇 개인가요?":skill.id.equals("objectShare")?"물건 "+total+"개를 "+unit+"명이 똑같이 나눠요.\n한 사람은 몇 개를 받나요?":"물건 "+total+"개를 "+unit+"개씩 묶어요.\n묶음 수와 남은 수를 쓰세요.";
   Question q=new Question(skill.id,prompt,"",skill.id.equals("objectGroupTotal")?new String[]{""+total}:skill.id.equals("objectShare")?new String[]{""+groups}:new String[]{""+groups,""+left});
   if(skill.id.equals("objectShare"))q.labels=new String[]{"한 사람 몫"};if(q.answers.length==2)q.labels=new String[]{"묶음 수","남은 수"};q.stepSupport=false;
   q.diagram=skill.id.equals("objectGroupTotal")?new StudyDiagram("groupedCollection",new double[]{unit,groups,0}):new StudyDiagram("sharingObjects",new double[]{total,unit,skill.id.equals("objectShare")?0:1});
   attach(q);if(limits.allows(q))pool.put(q.signature(),q);
  }
  return FactFoundations.choose(pool,random,recent);
 }
 public static void attach(Question q){
  if(q==null||!supports(q.skillId))return;
  Matcher total=Pattern.compile("한 묶음에 (\\d+)개씩 (\\d+)묶음이 있어요\\.\\n모두 몇 개인가요\\?").matcher(q.prompt);
  Matcher sharing=Pattern.compile("물건 (\\d+)개를 (\\d+)명이 똑같이 나눠요\\.\\n한 사람은 몇 개를 받나요\\?").matcher(q.prompt);
  Matcher grouping=Pattern.compile("물건 (\\d+)개를 (\\d+)개씩 묶어요\\.\\n묶음 수와 남은 수를 쓰세요\\.").matcher(q.prompt);
  StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="object-groups-v1";
  if(total.matches()){
   int unit=Integer.parseInt(total.group(1)),groups=Integer.parseInt(total.group(2));
   g.step("한 묶음의 물건을 세세요.","한 묶음 = ","",""+unit).step("묶음을 세세요.","묶음 수 = ","",""+groups);
   for(int n=1;n<=groups;n++)g.step("여기까지 센 물건 수를 쓰세요.",n+"번째 묶음까지 = ","",""+(n*unit));
  }else{
   boolean share=sharing.matches();if(!share&&!grouping.matches())return;Matcher m=share?sharing:grouping;
   int amount=Integer.parseInt(m.group(1)),unit=Integer.parseInt(m.group(2)),groups=amount/unit,left=amount%unit;
   g.step("전체 물건 수를 쓰세요.","물건 수 = ","",""+amount).step(share?"나눠 받을 사람 수를 쓰세요.":"한 묶음에 넣을 수를 쓰세요.",share?"사람 수 = ":"한 묶음 = ","",""+unit);
   for(int n=1;n<=groups;n++)g.step(share?"한 사람에게 하나씩 나눈 뒤 남은 수를 쓰세요.":"한 묶음을 만든 뒤 남은 수를 쓰세요.",n+"번 뒤 남은 수 = ","",""+(amount-n*unit));
   g.step(share?"한 사람이 받은 수를 쓰세요.":"만든 묶음 수를 쓰세요.",share?"한 사람 몫 = ":"묶음 수 = ","",""+groups);
   if(!share)g.step("묶지 못한 물건 수를 쓰세요.","남은 수 = ","",""+left);
  }
  q.studyGuide=g;
 }
}
