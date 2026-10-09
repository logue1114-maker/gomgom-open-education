package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
/** Original GBP shop contexts, exact integer pence and learner-filled relations. */
public final class MoneyBasicStories {
 private MoneyBasicStories(){}
 public static final String TOTAL="moneyBasicTotal",CHANGE="moneyBasicChange";
 public static final List<Catalog.Skill> SKILLS=List.of(new Catalog.Skill(TOTAL,"두 물건의 가격 합계",4,1,1,"","basicMoney",20,"decimalAdd","두 물건의 가격을 더한다."),new Catalog.Skill(CHANGE,"지불한 돈과 거스름돈",4,1,1,"","basicMoney",10,"decimalSub","지불한 돈에서 가격을 뺀다."));
 public static boolean supports(String id){return TOTAL.equals(id)||CHANGE.equals(id);}
 private static String amount(int pence){return String.format(Locale.ROOT,"%d.%02d",pence/100,pence%100);}
 public static Question make(String id,int first,int second){
  if(!supports(id)||first<0||first>999||second<0||second>999||CHANGE.equals(id)&&first<second)throw new IllegalArgumentException("Selected shop amounts");
  String prompt=TOTAL.equals(id)?"첫 물건 가격: £"+amount(first)+"\n둘째 물건 가격: £"+amount(second)+"\n두 물건은 모두 몇 파운드인가요?":"지불한 돈: £"+amount(first)+"\n물건 가격: £"+amount(second)+"\n거스름돈은 몇 파운드인가요?";
  int result=TOTAL.equals(id)?first+second:first-second;Question q=new Question(id,prompt,"",amount(result));q.decimal=true;q.answerFormat="decimalValue";q.stepSupport=false;attach(q);return q;
 }
 static int count(String id){return TOTAL.equals(id)?1000000:500500;}
 static Question at(String id,int index){if(!supports(id)||index<0||index>=count(id))throw new IllegalArgumentException("Invalid shop condition");int first,second;if(TOTAL.equals(id)){first=index/1000;second=index%1000;}else{first=(int)((Math.sqrt(8.0*index+1)-1)/2);second=index-first*(first+1)/2;}return make(id,first,second);}
 static Question next(Catalog.Skill skill,Random random,CurriculumLimits limits,Map<String,Integer> recent){return IndexedQuestionSupply.choose(count(skill.id),index->at(skill.id,index),random,limits,recent);}
 public static int[] read(Question q){
  if(q==null||q.prompt==null||!supports(q.skillId))return null;String value="£(\\d)\\.(\\d{2})";String pattern=TOTAL.equals(q.skillId)?"첫 물건 가격: "+value+"\\n둘째 물건 가격: "+value+"\\n두 물건은 모두 몇 파운드인가요\\?":"지불한 돈: "+value+"\\n물건 가격: "+value+"\\n거스름돈은 몇 파운드인가요\\?";Matcher m=Pattern.compile(pattern).matcher(q.prompt);if(!m.matches())return null;int first=Integer.parseInt(m.group(1))*100+Integer.parseInt(m.group(2)),second=Integer.parseInt(m.group(3))*100+Integer.parseInt(m.group(4));if(CHANGE.equals(q.skillId)&&first<second)return null;return new int[]{first,second,TOTAL.equals(q.skillId)?first+second:first-second};
 }
 public static void attach(Question q){int[] v=read(q);if(v==null)return;StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="basic-shop-money-v1";boolean total=TOTAL.equals(q.skillId);g.step(total?"첫 물건의 가격을 쓰세요.":"지불한 금액을 쓰세요.","첫 금액 a = ","",amount(v[0]));g.step(total?"둘째 물건의 가격을 쓰세요.":"물건의 가격을 쓰세요.","둘째 금액 b = ","",amount(v[1]));g.step(total?"두 가격을 더해 합계를 쓰세요.":"지불한 돈에서 가격을 빼세요.",total?"합계 = a + b = ":"거스름돈 = a − b = ","",amount(v[2]));q.studyGuide=g;}
 static Checker.Result check(Question q,List<String> answers){int[] v=read(q);if(v==null||answers==null||answers.size()!=1||answers.get(0)==null)return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"답 입력 필요");String raw=Expression.normalize(answers.get(0).trim());if(!raw.matches("[+−-]?\\d+(?:\\.\\d{1,2})?"))return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"금액은 소수 둘째 자리까지 입력");try{return Expression.number(raw).equals(Rational.of(v[2],100))?new Checker.Result(Checker.Status.CORRECT,-1,"정답"):new Checker.Result(Checker.Status.WRONG_ANSWER,0,"이 답 확인");}catch(RuntimeException error){return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"답의 기호 확인 필요");}}
}
