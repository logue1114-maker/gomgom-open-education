package com.gomgomapps.math.core;
import java.math.BigDecimal;import java.util.*;import java.util.regex.*;
public final class DecimalPercentReverse {
 private DecimalPercentReverse(){}
 public static final String ID="decimalToPercentage";
 public static final List<Catalog.Skill> SKILLS=List.of(new Catalog.Skill(ID,"소수를 백분율로",5,1,1,"","percentageReverse",1000,"percentDecimal","소수와 백분율의 관계를 반대 방향으로 나타낸다."));
 public static boolean supports(String id){return ID.equals(id);}
 public static Question make(int thousandths){if(thousandths<0||thousandths>1000)throw new IllegalArgumentException("Selected decimal rate scope");String raw=BigDecimal.valueOf(thousandths,3).toPlainString();Question q=new Question(ID,raw+"을 백분율로 나타내세요.","",BigDecimal.valueOf(thousandths,1).stripTrailingZeros().toPlainString());q.answerFormat="decimalValue";q.decimal=true;q.stepSupport=false;attach(q);return q;}
 static Question next(Random random,CurriculumLimits limits,Map<String,Integer> recent){return IndexedQuestionSupply.choose(1001,DecimalPercentReverse::make,random,limits,recent);}
 public static String read(Question q){if(q==null||q.prompt==null||!supports(q.skillId))return null;Matcher m=Pattern.compile("(\\d+\\.\\d{3})을 백분율로 나타내세요\\.").matcher(q.prompt);if(!m.matches())return null;BigDecimal v=new BigDecimal(m.group(1));return v.compareTo(BigDecimal.ONE)<=0?m.group(1):null;}
 public static void attach(Question q){String raw=read(q);if(raw==null)return;StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="decimal-percent-reverse-v1";Map<String,String> choices=new LinkedHashMap<>();choices.put("100","100개 중 몇 개인지");choices.put("10","10개 중 몇 개인지");choices.put("1","1개 중 몇 개인지");g.choice("%가 뜻하는 관계를 고르세요.",choices,"100");g.step("문제의 소수를 쓰세요.","소수 v = ","",raw);g.frames.get(1).inputFormat="decimalValue";g.step("백분율의 전체 부분 수를 쓰세요.","전체 부분 수 D = ","","100");g.step("소수에 전체 부분 수를 곱해 백분율을 구하세요.","백분율 p = v × D = ","",new BigDecimal(raw).movePointRight(2).stripTrailingZeros().toPlainString());g.frames.get(3).inputFormat="decimalValue";q.studyGuide=g;}
}
