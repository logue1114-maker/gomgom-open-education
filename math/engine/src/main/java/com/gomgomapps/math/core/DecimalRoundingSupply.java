package com.gomgomapps.math.core;
import java.math.*;import java.util.*;
/** Finite nearest-whole practice using the existing decimal rounding skill. */
public final class DecimalRoundingSupply {
 private DecimalRoundingSupply(){}
 static boolean supports(String id,CurriculumLimits limits){return id.equals("el_decimal_round")&&limits.roundsToWhole();}
 static Map<String,Question> candidates(CurriculumLimits limits){
  if(limits.decimalPlaces(1)!=1||limits.wholeMaximum(99)>99)throw new IllegalArgumentException("Selected nearest-whole practice needs tenths through99.9");
  Map<String,Question> pool=new LinkedHashMap<>();for(int n=0;n<(limits.wholeMaximum(99)+1)*10;n++){
   BigDecimal x=BigDecimal.valueOf(n,1),answer=x.setScale(0,RoundingMode.HALF_UP);Question q=new Question("el_decimal_round",x.toPlainString()+"을 가장 가까운 정수로 반올림하면?","",answer.toPlainString());q.answerFormat="decimalValue";q.decimal=true;q.stepSupport=false;RoundingRelations.attach(q);if(limits.allows(q))pool.put(q.signature(),q);
  }return pool;
 }
 static Question next(Random random,CurriculumLimits limits,Map<String,Integer> recent){return FactFoundations.choose(candidates(limits),random,recent);}
}
