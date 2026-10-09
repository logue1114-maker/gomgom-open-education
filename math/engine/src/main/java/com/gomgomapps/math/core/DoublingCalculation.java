package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
/** Repeated doubling/halving from the visible operands. No prefilled intermediate values. */
public final class DoublingCalculation {
 private DoublingCalculation(){}
 public static final String MULTIPLY="mentalDoublingProduct",DIVIDE="mentalHalvingQuotient";
 public static final List<Catalog.Skill> SKILLS=List.of(
  new Catalog.Skill(MULTIPLY,"두 배씩 곱하기",3,1,4,"","mentalDouble",99,"tables,add100","2·4·8을 곱할 때 두 배씩 계산한다."),
  new Catalog.Skill(DIVIDE,"반씩 나누기",3,1,4,"","mentalHalf",792,"divide","2·4·8로 나눌 때 반씩 계산한다."));
 public static boolean supports(String id){return MULTIPLY.equals(id)||DIVIDE.equals(id);}
 static Question make(String id,int n,int factor){
  if(!supports(id)||n<10||n>99||!Set.of(2,4,8).contains(factor))throw new IllegalArgumentException("Doubling calculation domain");
  boolean mul=MULTIPLY.equals(id);int a=mul?n:n*factor;Question q=new Question(id,a+(mul?" × ":" ÷ ")+factor,"",Integer.toString(mul?n*factor:n));q.stepSupport=false;return q;
 }
 public static int[] read(Question q){
  if(q==null||!supports(q.skillId)||q.prompt==null)return null;Matcher m=Pattern.compile("^(\\d{2,3}) ([×÷]) ([248])$").matcher(q.prompt);if(!m.matches()||m.group(2).equals("×")!=MULTIPLY.equals(q.skillId))return null;
  int a=Integer.parseInt(m.group(1)),b=Integer.parseInt(m.group(3));boolean mul=MULTIPLY.equals(q.skillId);if(!mul&&a%b!=0)return null;int n=mul?a:a/b;if(n<10||n>99)return null;return new int[]{a,b,mul?a*b:n,b==2?1:b==4?2:3};
 }
 static Question next(String id,Random random,CurriculumLimits limits,Map<String,Integer> recent){
  Map<String,Question> pool=new LinkedHashMap<>();for(int n=10;n<=99;n++)for(int b:List.of(2,4,8)){Question q=make(id,n,b);if(limits.allows(q))pool.put(q.signature(),q);}Question q=FactFoundations.choose(pool,random,recent);attach(q);return q;
 }
 static Checker.Result check(Question q,List<String> answers){int[] v=read(q);if(v==null||answers==null||answers.size()!=1||answers.get(0)==null||!answers.get(0).trim().matches("[0-9]{1,3}"))return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"답 입력 필요");return Integer.parseInt(answers.get(0).trim())==v[2]?new Checker.Result(Checker.Status.CORRECT,-1,"정답"):new Checker.Result(Checker.Status.WRONG_ANSWER,0,"이 답 확인");}
 public static void attach(Question q){
  int[] v=read(q);if(v==null)return;boolean mul=MULTIPLY.equals(q.skillId);StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="doubling-calculation-v1";
  g.step("처음 계산할 수를 쓰세요.","시작 수 = ","",Integer.toString(v[0]));
  g.step(mul?"곱하는 수를 쓰세요.":"나누는 수를 쓰세요.",mul?"곱하는 수 = ":"나누는 수 = ","",Integer.toString(v[1]));
  int value=v[0];for(int i=0;i<v[3];i++){value=mul?value+value:value/2;g.step(mul?"직전 수를 두 배로 계산하세요.":"직전 수를 반으로 나누세요.",mul?"직전 수 + 직전 수 = ":"직전 수 ÷ 2 = ","",Integer.toString(value));}q.studyGuide=g;
 }
}
