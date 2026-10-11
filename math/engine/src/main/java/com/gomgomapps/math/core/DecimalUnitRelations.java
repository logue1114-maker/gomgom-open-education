package com.gomgomapps.math.core;
import java.math.BigDecimal;import java.util.*;import java.util.regex.*;
/** The same amount expressed as counts of two visible decimal units. */
public final class DecimalUnitRelations {
 private DecimalUnitRelations(){}
 public static final String ID="decimalUnitCount";
 public static final List<Catalog.Skill> SKILLS=List.of(new Catalog.Skill(ID,"소수 단위의 개수",5,1,1,"","decimalUnitRelation",1000,"decimalDigitValue","같은 양을 십분의일·백분의일·천분의일의 개수로 나타낸다."));
 private static final int[][] PAIRS={{1,3},{2,3},{3,1},{3,2},{1,2},{2,1}};
 private static final String UNIT="(?:0\\.1|0\\.01|0\\.001|1/10|1/100|1/1000)";
 private static final Pattern VISIBLE=Pattern.compile("("+UNIT+") × (\\d{1,3}) = ("+UNIT+") × □");
 public static boolean supports(String id){return ID.equals(id);}
 static Question at(int index){if(index<0||index>=24000)throw new IllegalArgumentException("Decimal unit index");int[] pair=PAIRS[index%6];int representation=index/6%4,n=index/24;String u=unit(pair[0],(representation&1)!=0),v=unit(pair[1],(representation&2)!=0);Question q=new Question(ID,u+" × "+n+" = "+v+" × □","",decimal(u).multiply(BigDecimal.valueOf(n)).divide(decimal(v)).stripTrailingZeros().toPlainString());q.decimal=true;q.answerFormat="decimalValue";q.stepSupport=false;attach(q);return q;}
 private static String unit(int places,boolean fraction){return fraction?"1/"+(int)Math.pow(10,places):BigDecimal.ONE.movePointLeft(places).toPlainString();}
 private static BigDecimal decimal(String raw){if(!raw.contains("/"))return new BigDecimal(raw);return BigDecimal.ONE.divide(new BigDecimal(raw.substring(2)));}
 static Map<String,Question> candidates(){Map<String,Question> pool=new LinkedHashMap<>();for(int i=0;i<24000;i++){Question q=at(i);pool.put(q.signature(),q);}return pool;}
 static Question next(Random random,CurriculumLimits limits,Map<String,Integer> recent){return IndexedQuestionSupply.choose(24000,DecimalUnitRelations::at,random,limits,recent);}
 static Checker.Result check(Question q,List<String> answers){try{Matcher m=VISIBLE.matcher(q.prompt);if(!m.matches()||decimal(m.group(1)).compareTo(decimal(m.group(3)))==0)throw new IllegalArgumentException();if(answers==null||answers.size()!=1||answers.get(0)==null)return new Checker.Result(Checker.Status.INPUT_NEEDED,-1,"답 입력 필요");String input=Expression.normalize(answers.get(0).trim());if(input.length()>120||!input.matches("[+-]?\\d+(?:\\.\\d+)?"))return new Checker.Result(Checker.Status.INPUT_NEEDED,-1,"수로 입력 필요");BigDecimal expected=decimal(m.group(1)).multiply(new BigDecimal(m.group(2))).divide(decimal(m.group(3)));boolean same=new BigDecimal(input).compareTo(expected)==0;return new Checker.Result(same?Checker.Status.CORRECT:Checker.Status.WRONG_ANSWER,same?-1:0,same?"계산 확인 완료":"이 답 확인");}catch(RuntimeException ex){return new Checker.Result(Checker.Status.INPUT_NEEDED,-1,"수식 입력 확인 필요");}}
 public static void attach(Question q){
  if(q==null||q.prompt==null||!supports(q.skillId))return;Matcher m=VISIBLE.matcher(q.prompt);if(!m.matches())return;
  BigDecimal u=decimal(m.group(1)),n=new BigDecimal(m.group(2)),v=decimal(m.group(3));if(u.compareTo(v)==0)return;BigDecimal x=u.multiply(n);
  StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion=!q.prompt.contains("/")&&(u.scale()==3||v.scale()==3)?"decimal-unit-count-v1":"decimal-fraction-unit-count-v2";
  step(g,m.group(1).contains("/")?"왼쪽의 한 단위 크기를 소수로 쓰세요.":"왼쪽의 한 단위 크기를 쓰세요.","왼쪽 단위 u = ",u);
  step(g,"왼쪽의 단위 개수를 쓰세요.","왼쪽 개수 N = ",n);
  step(g,"한 단위 크기에 개수를 곱해 같은 양을 구하세요.","같은 양 x = u × N = ",x);
  step(g,m.group(3).contains("/")?"오른쪽의 한 단위 크기를 소수로 쓰세요.":"오른쪽의 한 단위 크기를 쓰세요.","오른쪽 단위 v = ",v);
  step(g,"같은 양을 오른쪽 한 단위 크기로 나누어 개수를 구하세요.","오른쪽 개수 M = x ÷ v = ",x.divide(v));q.studyGuide=g;
 }
 private static void step(StudyGuide g,String text,String frame,BigDecimal value){g.step(text,frame,"",value.stripTrailingZeros().toPlainString());g.frames.get(g.frames.size()-1).inputFormat="decimalValue";}
}
