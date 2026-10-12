package com.gomgomapps.math.core;
import java.util.*;
/** Rounding plausibility for the learner's own context answer; never exact-answer certification. */
public final class ContextEstimateCheck {
 private ContextEstimateCheck(){}
 public static final List<Integer> UNITS=List.of(10,100,1000,10000,100000,1000000);
 public record Plan(int operation,int a,int b,int unit,int roundedFirst,int roundedSecond,int proposal,Rational estimate,Rational tolerance,Rational difference){}
 public enum Status { INPUT_NEEDED, WRONG_FIRST, WRONG_SECOND, WRONG_ESTIMATE, WRONG_DIFFERENCE, WRONG_COMPARISON, WITHIN, OUTSIDE }
 private static Rational absolute(Rational v){return v.n.signum()<0?v.neg():v;}
 public static Plan plan(Question q,String proposal,int unit){
  var g=ContextCalculationMethods.read(q);if(g==null||proposal==null||!proposal.trim().matches("[0-9]{1,7}")||!UNITS.contains(unit))return null;
  int p=Integer.parseInt(proposal.trim());if(p>1000000)return null;
  int a=EstimateCalculationCheck.rounded(g.a(),unit),b=g.operation()<2?EstimateCalculationCheck.rounded(g.b(),unit):g.b();
  Rational ra=Rational.of(a),rb=Rational.of(b),half=Rational.of(unit,2);
  Rational estimate=switch(g.operation()){case 0->ra.add(rb);case 1->ra.sub(rb);case 2->ra.mul(rb);default->ra.div(rb);};
  Rational tolerance=switch(g.operation()){case 0,1->Rational.of(unit);case 2->half.mul(rb);default->half.div(rb);};
  return new Plan(g.operation(),g.a(),g.b(),unit,a,b,p,estimate,tolerance,absolute(Rational.of(p).sub(estimate)));
 }
 private static boolean same(String value,Rational expected){return value!=null&&value.length()<=40&&Expression.number(value).equals(expected);}
 public static Status check(Question q,String proposal,int unit,String first,String second,String estimate,String difference,String verdict){
  Plan p=plan(q,proposal,unit);if(p==null)return Status.INPUT_NEEDED;
  try{if(!same(first,Rational.of(p.roundedFirst)))return Status.WRONG_FIRST;if(p.operation<2&&!same(second,Rational.of(p.roundedSecond)))return Status.WRONG_SECOND;if(!same(estimate,p.estimate))return Status.WRONG_ESTIMATE;if(!same(difference,p.difference))return Status.WRONG_DIFFERENCE;}catch(RuntimeException ex){return Status.INPUT_NEEDED;}
  if(!Set.of("within","outside").contains(verdict==null?"":verdict))return Status.INPUT_NEEDED;
  boolean within=p.difference.compareTo(p.tolerance)<=0;if(within!=verdict.equals("within"))return Status.WRONG_COMPARISON;
  return within?Status.WITHIN:Status.OUTSIDE;
 }
 public static String message(Status s){return switch(s){case INPUT_NEEDED->"반올림값·어림값·차이와 범위 입력 필요";case WRONG_FIRST->"첫째 수 반올림값 다시 확인";case WRONG_SECOND->"둘째 수 반올림값 다시 확인";case WRONG_ESTIMATE->"어림한 계산값 다시 확인";case WRONG_DIFFERENCE->"내 답과 어림값의 차이 다시 확인";case WRONG_COMPARISON->"차이가 허용 차이 이하인지 다시 확인";case WITHIN->"어림 범위 안입니다. 정확한 답인지는 원래 계산으로 확인하세요.";case OUTSIDE->"어림 범위 밖입니다. 원래 답을 다시 확인하세요.";};}
}
