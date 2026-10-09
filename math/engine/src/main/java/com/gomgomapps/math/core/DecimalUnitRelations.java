package com.gomgomapps.math.core;
import java.math.BigDecimal;import java.util.*;import java.util.regex.*;
/** The same amount expressed as counts of two visible decimal units. */
public final class DecimalUnitRelations {
 private DecimalUnitRelations(){}
 public static final String ID="decimalUnitCount";
 public static final List<Catalog.Skill> SKILLS=List.of(new Catalog.Skill(ID,"소수 단위의 개수",5,1,1,"","decimalUnitRelation",1000,"decimalDigitValue","같은 양을 십분의일·백분의일·천분의일의 개수로 나타낸다."));
 private static final int[][] PAIRS={{1,3},{2,3},{3,1},{3,2}};
 public static boolean supports(String id){return ID.equals(id);}
 private static Question at(int index){int[] pair=PAIRS[index%4];int n=index/4;BigDecimal u=BigDecimal.ONE.movePointLeft(pair[0]),v=BigDecimal.ONE.movePointLeft(pair[1]);String prompt=u.toPlainString()+" × "+n+" = "+v.toPlainString()+" × □";Question q=new Question(ID,prompt,"",u.multiply(BigDecimal.valueOf(n)).divide(v).stripTrailingZeros().toPlainString());q.decimal=true;q.answerFormat="decimalValue";q.stepSupport=false;attach(q);return q;}
 static Map<String,Question> candidates(){Map<String,Question> pool=new LinkedHashMap<>();for(int i=0;i<4000;i++){Question q=at(i);pool.put(q.signature(),q);}return pool;}
 static Question next(Random random,CurriculumLimits limits,Map<String,Integer> recent){return IndexedQuestionSupply.choose(4000,DecimalUnitRelations::at,random,limits,recent);}
 public static void attach(Question q){
  if(q==null||q.prompt==null||!supports(q.skillId))return;Matcher m=Pattern.compile("(0\\.1|0\\.01|0\\.001) × (\\d{1,3}) = (0\\.1|0\\.01|0\\.001) × □").matcher(q.prompt);if(!m.matches())return;
  BigDecimal u=new BigDecimal(m.group(1)),n=new BigDecimal(m.group(2)),v=new BigDecimal(m.group(3));if(u.equals(v)||u.scale()!=3&&v.scale()!=3)return;BigDecimal x=u.multiply(n);
  StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="decimal-unit-count-v1";
  step(g,"왼쪽의 한 단위 크기를 쓰세요.","왼쪽 단위 u = ",u);
  step(g,"왼쪽의 단위 개수를 쓰세요.","왼쪽 개수 N = ",n);
  step(g,"한 단위 크기에 개수를 곱해 같은 양을 구하세요.","같은 양 x = u × N = ",x);
  step(g,"오른쪽의 한 단위 크기를 쓰세요.","오른쪽 단위 v = ",v);
  step(g,"같은 양을 오른쪽 한 단위 크기로 나누어 개수를 구하세요.","오른쪽 개수 M = x ÷ v = ",x.divide(v));q.studyGuide=g;
 }
 private static void step(StudyGuide g,String text,String frame,BigDecimal value){g.step(text,frame,"",value.stripTrailingZeros().toPlainString());g.frames.get(g.frames.size()-1).inputFormat="decimalValue";}
}
