package com.gomgomapps.math.core;
import java.math.BigDecimal;import java.util.*;import java.util.regex.*;
/** Given decimal digits and their values, including the ones position. */
public final class DecimalPlaceRelations {
 private DecimalPlaceRelations(){}
 public static final String ID="decimalDigitValue";
 public static final List<Catalog.Skill> SKILLS=List.of(new Catalog.Skill(ID,"소수의 자리 숫자와 값",4,1,1,"","decimalPlaceValue",1000,"el_decimal_place","자리 숫자와 그 숫자가 나타내는 양을 구별한다."));
 public static boolean supports(String id){return ID.equals(id);}
 static String place(int p){return p==0?"일의 자리":p==1?"소수 첫째 자리":p==2?"소수 둘째 자리":"소수 셋째 자리";}
 static Map<String,Question> candidates(CurriculumLimits limits){
  int max=limits.wholeMaximum(9);if(max>9)throw new IllegalArgumentException("Selected decimal digit value maximum is9");Map<String,Question> pool=new LinkedHashMap<>();
  for(int i=0;i<count(limits);i++){Question q=at(limits,i);if(limits.allows(q))pool.put(q.signature(),q);}return pool;
 }
 private static int count(CurriculumLimits limits){int p=limits.decimalPlaces(2);if(p<2||p>3||limits.wholeMaximum(9)>9)throw new IllegalArgumentException("Selected digit/value scope needs two/three decimal places through9");return (limits.wholeMaximum(9)+1)*(int)Math.pow(10,p)*(p+1);}
 private static Question at(CurriculumLimits limits,int index){int p=limits.decimalPlaces(2),position=index%(p+1),n=index/(p+1);BigDecimal x=BigDecimal.valueOf(n,p),unit=BigDecimal.ONE.movePointLeft(position);int digit=(n/(int)Math.pow(10,p-position))%10;Question q=new Question(ID,x.toPlainString()+"의 "+place(position)+" 숫자와 그 숫자가 나타내는 값을 쓰세요.","",""+digit,unit.multiply(BigDecimal.valueOf(digit)).stripTrailingZeros().toPlainString());q.labels=new String[]{"자리 숫자","숫자의 값"};q.answerFormat="decimalValue";q.decimal=true;q.stepSupport=false;attach(q);return q;}
 static Question next(Random random,CurriculumLimits limits,Map<String,Integer> recent){return IndexedQuestionSupply.choose(count(limits),i->at(limits,i),random,limits,recent);}
 public static void attach(Question q){
  if(q==null||!supports(q.skillId)||q.prompt==null)return;Matcher m=Pattern.compile("(\\d\\.\\d{2,3})의 (일의 자리|소수 첫째 자리|소수 둘째 자리|소수 셋째 자리) 숫자와 그 숫자가 나타내는 값을 쓰세요\\.").matcher(q.prompt);if(!m.matches())return;
  String raw=m.group(1);int position=m.group(2).equals("일의 자리")?0:m.group(2).equals("소수 첫째 자리")?1:m.group(2).equals("소수 둘째 자리")?2:3;if(position+1>=raw.length())return;int digit=raw.charAt(position==0?0:position+1)-'0';BigDecimal unit=BigDecimal.ONE.movePointLeft(position);
  StudyGuide guide=new StudyGuide().transfer(false);guide.teachingVersion="decimal-digit-value-v1";
  step(guide,"문제의 소수를 쓰세요.","소수 x = ",raw);
  step(guide,"문제에서 지정한 자리의 숫자를 쓰세요.","자리 숫자 a = ",""+digit);
  step(guide,"일의 자리에서 오른쪽으로 한 자리 옮길 때마다 한 단위를 10으로 나눕니다. 지정한 자리의 한 단위 크기를 쓰세요.","자리 단위 u = ",unit.toPlainString());
  step(guide,"자리 숫자에 그 자리의 한 단위 크기를 곱하세요.","숫자의 값 v = a × u = ",unit.multiply(BigDecimal.valueOf(digit)).toPlainString());q.studyGuide=guide;
 }
 private static void step(StudyGuide guide,String text,String before,String value){guide.step(text,before,"",value);guide.frames.get(guide.frames.size()-1).inputFormat="decimalValue";}
}
