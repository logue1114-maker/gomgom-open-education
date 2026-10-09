package com.gomgomapps.math.core;
import java.math.BigDecimal;import java.util.*;import java.util.regex.*;
/** Division by10/100 with exact unit relationships, not floating-point shifts. */
public final class PowerTenDivision {
 private PowerTenDivision(){}
 public static final List<Catalog.Skill> SKILLS=List.of(
  new Catalog.Skill("wholeDivideTen","자연수를 10으로 나누기",3,1,1,"","powerTenDivision",100,"divide","자연수 한 단위를 같은 크기10부분으로 나눈다."),
  new Catalog.Skill("wholeDivideHundred","자연수를 100으로 나누기",4,1,1,"","powerTenDivision",100,"divide","자연수 한 단위를 같은 크기100부분으로 나눈다."),
  new Catalog.Skill("tenthsDivideTen","십분의일을 10으로 나누기",4,1,1,"","powerTenDivision",100,"divide","십분의일 한 단위를 같은 크기10부분으로 나눈다."));
 public static boolean supports(String id){return SKILLS.stream().anyMatch(s->s.id.equals(id));}
 static Map<String,Question> candidates(Catalog.Skill skill,CurriculumLimits limits){
  boolean tenth=skill.id.equals("tenthsDivideTen");int max=limits.wholeMaximum(tenth?9:99);if(max>99)throw new IllegalArgumentException("Selected power-ten division maximum is99");int last=tenth?max*10+9:max;int divisor=skill.id.equals("wholeDivideHundred")?100:10;
  Map<String,Question> pool=new LinkedHashMap<>();for(int units=0;units<=last;units++){
   BigDecimal value=BigDecimal.valueOf(units,tenth?1:0);String raw=value.toPlainString();Question q=new Question(skill.id,raw+" ÷ "+divisor,raw+" / "+divisor,value.divide(BigDecimal.valueOf(divisor)).stripTrailingZeros().toPlainString());q.decimal=true;q.answerFormat="decimalValue";q.stepSupport=false;attach(q);if(limits.allows(q))pool.put(q.signature(),q);
  }return pool;
 }
 static Question next(Catalog.Skill skill,Random random,CurriculumLimits limits,Map<String,Integer> recent){return FactFoundations.choose(candidates(skill,limits),random,recent);}
 public static void attach(Question q){
  if(q==null||q.prompt==null||!supports(q.skillId))return;Matcher m=Pattern.compile("(\\d+(?:\\.\\d)?) ÷ (10|100)").matcher(q.prompt);if(!m.matches())return;boolean tenth=q.skillId.equals("tenthsDivideTen");int divisor=q.skillId.equals("wholeDivideHundred")?100:10;if(Integer.parseInt(m.group(2))!=divisor||tenth!=m.group(1).contains("."))return;
  BigDecimal x=new BigDecimal(m.group(1)),d=BigDecimal.valueOf(divisor),unit=tenth?new BigDecimal("0.1"):BigDecimal.ONE,part=unit.divide(d),units=x.divide(unit);if(x.signum()<0||x.compareTo(tenth?new BigDecimal("9.9"):new BigDecimal("99"))>0)return;
  StudyGuide guide=new StudyGuide().transfer(false);guide.teachingVersion="power-ten-unit-division-v1";
  step(guide,"나누어지는 수를 쓰세요.","나누어지는 수 x = ",x,true);
  step(guide,"몇 부분으로 똑같이 나누는지 쓰세요.","같은 부분 수 d = ",d,false);
  step(guide,tenth?"십분의일 한 단위의 크기를 소수로 쓰세요.":"자연수 한 단위의 크기를 쓰세요.","한 단위 u = ",unit,true);
  step(guide,"한 단위를 d부분으로 똑같이 나눈 한 부분의 크기를 쓰세요.","한 부분 v = u ÷ d = ",part,true);
  step(guide,"나누어지는 수에 한 단위가 몇 개 있는지 쓰세요.","단위 개수 N = x ÷ u = ",units,false);
  step(guide,"한 부분의 크기에 단위 개수를 곱해 몫을 쓰세요.","몫 r = v × N = ",x.divide(d),true);q.studyGuide=guide;
 }
 private static void step(StudyGuide guide,String text,String before,BigDecimal expected,boolean decimal){guide.step(text,before,"",expected.stripTrailingZeros().toPlainString());if(decimal)guide.frames.get(guide.frames.size()-1).inputFormat="decimalValue";}
}
