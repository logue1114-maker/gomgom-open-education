package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
/** Derive a new addition/subtraction from a stated fact; never fill the student answer. */
public final class RelatedCalculationFacts {
 private RelatedCalculationFacts(){}
 public static final String ADD="relatedAdditionFacts",SUB="relatedSubtractionFacts",VERSION="related-calculation-facts-v1";
 public static final List<Catalog.Skill> SKILLS=List.of(new Catalog.Skill(ADD,"아는 덧셈으로 풀기",2,1,1,"","relatedFact",100,"add20","20 이내의 덧셈에서 100 이내의 관련 계산을 이끌어 낸다."),new Catalog.Skill(SUB,"아는 뺄셈으로 풀기",2,1,1,"","relatedFact",100,"sub20","20 이내의 뺄셈에서 100 이내의 관련 계산을 이끌어 낸다."));
 public static boolean supports(String id){return ADD.equals(id)||SUB.equals(id);}
 static Question make(String id,int a,int b,int change,boolean scale){
  boolean add=ADD.equals(id);int base=add?a+b:a-b,A=scale?a*10:a+change,B=scale?b*10:b,result=add?A+B:A-B;
  if(!supports(id)||a<0||b<0||a>20||b>20||base<0||base>20||A>100||B>100||result>100||result<0||(scale?change!=10||a+b==0:change<10||change%10!=0))throw new IllegalArgumentException("related fact domain");String op=add?" + ":" − ";Question q=new Question(id,"아는 계산: "+a+op+b+" = "+base+"\n"+A+op+B+" = □","",""+result);q.kind="relatedFact";q.labels=new String[]{"답"};q.stepSupport=false;return q;
 }
 static Map<String,Question> pool(String id,CurriculumLimits limits){Map<String,Question> pool=new LinkedHashMap<>();boolean add=ADD.equals(id);for(int a=0;a<=20;a++)for(int b=0;b<=20;b++){int base=add?a+b:a-b;if(base<0||base>20)continue;if(a<=10&&b<=10&&base<=10&&a+b>0){Question q=make(id,a,b,10,true);if(limits.allows(q))pool.put(q.signature(),q);}for(int change=10;a+change<=100&&(add?base+change<=100:true);change+=10){Question q=make(id,a,b,change,false);if(limits.allows(q))pool.put(q.signature(),q);}}return pool;}
 static Question next(Catalog.Skill s,Random r,CurriculumLimits limits,Map<String,Integer> recent){Question q=FactFoundations.choose(pool(s.id,limits),r,recent);attach(q);return q;}
 static Question create(Catalog.Skill s,Random r,CurriculumLimits limits){return next(s,r,limits,Map.of());}
 // first/second/base result/target first/target second/target result/scale/change
 static int[] read(Question q){if(q==null||!supports(q.skillId)||q.prompt==null)return null;boolean add=ADD.equals(q.skillId);String op=add?"\\+":"−";Matcher m=Pattern.compile("아는 계산: (\\d{1,2}) "+op+" (\\d{1,2}) = (\\d{1,2})\\n(\\d{1,3}) "+op+" (\\d{1,3}) = □").matcher(q.prompt);if(!m.matches())return null;int a=Integer.parseInt(m.group(1)),b=Integer.parseInt(m.group(2)),base=Integer.parseInt(m.group(3)),A=Integer.parseInt(m.group(4)),B=Integer.parseInt(m.group(5));if(a>20||b>20||base!=(add?a+b:a-b)||base<0||base>20||A>100||B>100)return null;boolean scale=A==a*10&&B==b*10;int change=A-a,result=add?A+B:A-B;if(scale&&a+b==0)return null;if(result<0||result>100||(!scale&&(B!=b||change<10||change%10!=0)))return null;return new int[]{a,b,base,A,B,result,scale?1:0,change};}
 public static void attach(Question q){int[] v=read(q);if(v==null)return;StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion=VERSION;g.step("아는 계산의 결과를 쓰세요.","아는 계산의 결과 = ","",""+v[2]);
  if(v[6]==1){g.step("0이 아닌 수가 몇 배가 되었는지 쓰세요.","바뀐 수 ÷ 원래 수 = ","","10");g.step("첫 수를 10배로 바꾸세요.","원래 첫 수 × 배수 = ","",""+v[3]);g.step("둘째 수를 10배로 바꾸세요.","원래 둘째 수 × 배수 = ","",""+v[4]);g.step("계산 결과도 10배로 바꾸세요.","아는 계산의 결과 × 배수 = ","",""+v[5]);}
  else{g.step("첫 수가 얼마나 커졌는지 쓰세요.","바뀐 첫 수 − 원래 첫 수 = ","",""+v[7]);g.step("첫 수가 커진 만큼 결과에 더하세요.","아는 계산의 결과 + 커진 값 = ","",""+v[5]);}q.studyGuide=g;
 }
 static Checker.Result check(Question q,List<String> answers){int[] v=read(q);if(v==null||answers.size()!=1||!answers.get(0).trim().matches("[0-9]{1,3}"))return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"답 입력 필요");return Integer.parseInt(answers.get(0).trim())==v[5]?new Checker.Result(Checker.Status.CORRECT,-1,"정답"):new Checker.Result(Checker.Status.WRONG_ANSWER,0,"이 답 확인");}
}
