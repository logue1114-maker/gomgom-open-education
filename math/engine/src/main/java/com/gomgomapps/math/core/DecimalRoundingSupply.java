package com.gomgomapps.math.core;
import java.math.*;import java.util.*;
/** Indexed finite decimal rounding using explicit selected target places. */
public final class DecimalRoundingSupply {
 private DecimalRoundingSupply(){}
 static boolean supports(String id,CurriculumLimits limits){return id.equals("el_decimal_round")&&!limits.roundingDecimalTargets().isEmpty();}
 static int count(CurriculumLimits limits){int places=limits.decimalPlaces(1);if(places<1||places>3||limits.wholeMaximum(99)>99||limits.roundingDecimalTargets().isEmpty()||limits.roundingDecimalTargets().stream().anyMatch(p->p>=places))throw new IllegalArgumentException("Selected rounding needs one to three decimals through99");return (limits.wholeMaximum(99)+1)*(int)Math.pow(10,places)*limits.roundingDecimalTargets().size();}
 static Question at(CurriculumLimits limits,int index){
  List<Integer> targets=limits.roundingDecimalTargets();int target=targets.get(index%targets.size()),n=index/targets.size(),places=limits.decimalPlaces(1);BigDecimal x=BigDecimal.valueOf(n,places),answer=x.setScale(target,RoundingMode.HALF_UP);
  String prompt=x.toPlainString()+(target==0?"을 가장 가까운 정수로 반올림하면?":"을 소수 "+List.of("첫째","둘째","셋째").get(target-1)+" 자리까지 반올림하면?");Question q=new Question("el_decimal_round",prompt,"",answer.toPlainString());q.answerFormat=target==0?"decimalValue":"decimal";q.decimal=true;q.stepSupport=false;RoundingRelations.attach(q);return q;
 }
 static Map<String,Question> candidates(CurriculumLimits limits){Map<String,Question> pool=new LinkedHashMap<>();for(int i=0;i<count(limits);i++){Question q=at(limits,i);if(limits.allows(q))pool.put(q.signature(),q);}return pool;}
 static Question next(Random random,CurriculumLimits limits,Map<String,Integer> recent){return IndexedQuestionSupply.choose(count(limits),i->at(limits,i),random,limits,recent);}
}
