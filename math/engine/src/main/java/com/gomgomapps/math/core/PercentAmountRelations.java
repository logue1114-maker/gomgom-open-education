package com.gomgomapps.math.core;
import java.math.*;import java.util.*;import java.util.regex.*;
/** Percent amounts and comparison use original visible bases and rates. */
public final class PercentAmountRelations {
 private PercentAmountRelations(){}
 public static final String COMPARE="percentAmountsCompare";
 public static final List<Catalog.Skill> SKILLS=List.of(new Catalog.Skill(COMPARE,"백분율에 해당하는 양 비교",6,1,1,"","percentAmountsCompare",100,"percent","서로 다른 기준량의 백분율에 해당하는 양을 계산해 비교한다."));
 public static boolean supports(String id){return id.equals("percent")||id.equals(COMPARE);}
 static boolean selected(String id,CurriculumLimits l){return id.equals(COMPARE)||id.equals("percent")&&l.variedFacts();}
 public static int domainSize(String id){return id.equals(COMPARE)?1020100:101101;}
 public record Givens(int base,int percent){}
 public static Givens readAmount(Question q){if(q==null||!q.skillId.equals("percent")||q.prompt==null)return null;Matcher m=Pattern.compile("(\\d+)의 (\\d+)%는\\?").matcher(q.prompt);if(!m.matches())return null;return new Givens(Integer.parseInt(m.group(1)),Integer.parseInt(m.group(2)));}
 public static Question amount(int base,int p){Question q=new Question("percent",base+"의 "+p+"%는?",base+"*"+p+"/100",plain(value(base,p))).withInputs(p,base);q.answerFormat="decimalValue";q.decimal=true;q.stepSupport=false;attach(q);return q;}
 static Question compare(int a,int p,int b,int qRate){BigDecimal left=value(a,p),right=value(b,qRate);String sign=left.compareTo(right)<0?"<":left.compareTo(right)>0?">":"=";Question q=new Question(COMPARE,a+"의 "+p+"%  □  "+b+"의 "+qRate+"%","",sign);q.kind="symbol";q.stepSupport=false;attach(q);return q;}
 static Question at(String id,int i){if(id.equals("percent"))return amount(i/101,i%101);int q=i%101;i/=101;int p=i%101;i/=101;int b=(i%10+1)*10,a=(i/10+1)*10;return compare(a,p,b,q);}
 static Question next(String id,Random random,CurriculumLimits l,Map<String,Integer> recent){return IndexedQuestionSupply.choose(domainSize(id),i->at(id,i),random,l,recent);}
 public static int[] readCompare(Question q){if(q==null||!COMPARE.equals(q.skillId)||q.prompt==null)return null;Matcher m=Pattern.compile("(\\d{1,3})의 (\\d{1,3})%  □  (\\d{1,3})의 (\\d{1,3})%").matcher(q.prompt);if(!m.matches())return null;int[] v=new int[4];for(int i=0;i<4;i++)v[i]=Integer.parseInt(m.group(i+1));if(v[0]<10||v[0]>100||v[0]%10!=0||v[2]<10||v[2]>100||v[2]%10!=0||v[1]>100||v[3]>100)return null;return v;}
 public static void attach(Question q){if(q==null)return;if(q.skillId.equals("percent")){Givens v=readAmount(q);if(v==null)return;StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="ratio-value-relations-v1";g.step("문제에서 전체 수를 찾아 쓰세요.","전체 수 = ","",""+v.base());g.step("문제에서 백분율을 찾아 쓰세요.","백분율 = ","%",""+v.percent());g.step("백분율을 100으로 나누어 소수로 나타내세요.","백분율 ÷ 100 = ","",plain(BigDecimal.valueOf(v.percent(),2)));g.step("전체 수에 계산한 비율을 곱하세요.","전체 수 × 계산한 비율 = ","",plain(value(v.base(),v.percent())));q.studyGuide=g;return;}int[] v=readCompare(q);if(v==null)return;StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="percent-amount-comparison-v1";for(int side=0;side<2;side++){String prefix=side==0?"왼쪽":"오른쪽";int base=v[side*2],rate=v[side*2+1];g.step(prefix+"의 기준량을 쓰세요.",prefix+" 기준량 B = ","",""+base);g.step(prefix+"의 백분율을 쓰세요.",prefix+" 백분율 p = ","",""+rate);g.step("백분율을 100으로 나누어 비율을 소수로 쓰세요.",prefix+" 소수 비율 v = p ÷ 100 = ","",plain(BigDecimal.valueOf(rate,2)));g.step("기준량에 소수 비율을 곱하세요.",prefix+" 양 A = B × v = ","",plain(value(base,rate)));}for(var f:g.frames)f.inputFormat="decimalValue";int cmp=value(v[0],v[1]).compareTo(value(v[2],v[3]));Map<String,String> choices=new LinkedHashMap<>();for(String sign:List.of("<","=",">"))choices.put(sign,sign);g.choice("계산한 왼쪽 양과 오른쪽 양을 비교해 기호를 고르세요.",choices,cmp<0?"<":cmp>0?">":"=");q.studyGuide=g;}
 private static BigDecimal value(int base,int p){return BigDecimal.valueOf((long)base*p,2);}
 private static String plain(BigDecimal x){return x.stripTrailingZeros().toPlainString();}
}
