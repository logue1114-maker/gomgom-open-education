package com.gomgomapps.math.core;
import java.math.BigDecimal;
import java.util.*;
import java.util.regex.*;
/** Exact next-number practice; help is reconstructed from public givens only. */
public final class DecimalCounting {
 private DecimalCounting(){}
 public static final List<Catalog.Skill> SKILLS=List.of(
  new Catalog.Skill("decimalCountForward","소수 앞으로 세기",3,1,1,"","decimalCount",1000,"count","같은 소수 간격으로 앞으로 센다."),
  new Catalog.Skill("decimalCountBackward","소수 거꾸로 세기",3,1,1,"","decimalCount",1000,"count","같은 소수 간격으로 거꾸로 센다."));
 public static boolean supports(String id){return SKILLS.stream().anyMatch(s->s.id.equals(id));}
 static String number(int units,int places){return BigDecimal.valueOf(units,places).stripTrailingZeros().toPlainString();}
 static Map<String,Question> candidates(Catalog.Skill skill,CurriculumLimits limits){
  int places=limits.decimalPlaces(1);
  if(places>2)throw new IllegalArgumentException("Decimal counting supports tenths and hundredths");
  int scale=places==1?10:100;
  int max=limits.wholeMaximum(10);
  if(max<1||max>100)throw new IllegalArgumentException("Decimal counting whole maximum must be 1..100");
  int end=max*scale;boolean back=skill.id.equals("decimalCountBackward");
  Map<String,Question> pool=new LinkedHashMap<>();
  for(int start=back?1:0;start<=(back?end:end-1);start++){
   String prompt=number(1,places)+"씩 "+(back?"거꾸로":"앞으로")+" 세세요.\n"+number(start,places)+" → □";
   Question q=new Question(skill.id,prompt,"",number(start+(back?-1:1),places));q.stepSupport=false;
   if(limits.allows(q))pool.put(q.signature(),q);
  }
  return pool;
 }
 static Question next(Catalog.Skill skill,Random random,CurriculumLimits limits,Map<String,Integer> recent){
  Question q=FactFoundations.choose(candidates(skill,limits),random,recent);attach(q);return q;
 }
 public static void attach(Question q){
  if(q==null||!supports(q.skillId)||q.prompt==null)return;
  Matcher m=Pattern.compile("(0\\.1|0\\.01)씩 (앞으로|거꾸로) 세세요\\.\\n(\\d+(?:\\.\\d{1,2})?) → □").matcher(q.prompt);
  if(!m.matches())return;
  BigDecimal step=new BigDecimal(m.group(1)),start=new BigDecimal(m.group(3));boolean back=m.group(2).equals("거꾸로");
  BigDecimal answer=back?start.subtract(step):start.add(step);if(answer.signum()<0)return;
  StudyGuide guide=new StudyGuide().transfer(false);guide.teachingVersion="decimal-counting-v1";
  guide.step("시작하는 수를 쓰세요.","시작 수 = ","",start.stripTrailingZeros().toPlainString());
  guide.step("몇씩 세는지 쓰세요.","세는 간격 = ","",step.toPlainString());
  guide.step(back?"그 간격만큼 거꾸로 세세요.":"그 간격만큼 앞으로 세세요.","다음 수 = ","",answer.stripTrailingZeros().toPlainString());q.studyGuide=guide;
 }
}
