package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
/** Learner composes an additive verbal rule from operation and interval. */
public final class LinearSequenceDescription {
 private LinearSequenceDescription(){}
 public static final String ID="primarySequenceDescription";
 public static final Catalog.Skill SKILL=new Catalog.Skill(ID,"수열의 규칙 설명하기",6,1,2,"","primarySequenceDescription",100,PrimaryLinearSequences.RULE,"매번 몇씩 더하거나 빼는지 규칙을 설명한다.");
 public static boolean selected(Question q){return q!=null&&ID.equals(q.skillId);}
 static Question create(int a,int d){
  if(a< -100||a>100||d==0||Math.abs(d)>12||a+3*d< -100||a+3*d>100)throw new IllegalArgumentException("sequence domain");
  Question q=new Question(ID,a+" → "+(a+d)+" → "+(a+2*d)+" → "+(a+3*d)+"\n매번 몇씩 더하거나 빼는지 규칙을 나타내세요.","","","");q.labels=new String[]{"계산","간격"};q.kind="sequenceDescription";q.stepSupport=false;attach(q);return q;
 }
 static Question next(Random random,Map<String,Integer> recent){Map<String,Question> pool=new LinkedHashMap<>();for(int d=-12;d<=12;d++)if(d!=0)for(int a=-100;a<=100;a++)if(a+3*d>=-100&&a+3*d<=100){Question q=create(a,d);pool.put(q.signature(),q);}return FactFoundations.choose(pool,random,recent);}
 static int[] givens(Question q){
  if(!selected(q)||q.prompt==null)return null;Matcher m=Pattern.compile("(-?\\d+) → (-?\\d+) → (-?\\d+) → (-?\\d+)\\n매번 몇씩 더하거나 빼는지 규칙을 나타내세요\\.").matcher(q.prompt);if(!m.matches())return null;
  try{int a=Integer.parseInt(m.group(1)),d=Integer.parseInt(m.group(2))-a;if(a< -100||a>100||d==0||Math.abs(d)>12||Integer.parseInt(m.group(3))!=a+2*d||Integer.parseInt(m.group(4))!=a+3*d||a+3*d< -100||a+3*d>100)return null;return new int[]{a,d};}catch(NumberFormatException e){return null;}
 }
 static Checker.Result check(Question q,List<String> answers){
  int[] v=givens(q);if(v==null||answers.size()!=2)return new Checker.Result(Checker.Status.INPUT_NEEDED,-1,"답 입력 필요");String op=answers.get(0),amount=answers.get(1).trim();
  if(!List.of("0","1").contains(op))return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"계산 선택 필요");
  if(!amount.matches("[0-9]{1,9}"))return new Checker.Result(Checker.Status.INPUT_NEEDED,1,"간격 입력 필요");
  if(!op.equals(v[1]>0?"0":"1"))return new Checker.Result(Checker.Status.WRONG_ANSWER,0,"이 계산 확인");
  return Integer.parseInt(amount)==Math.abs(v[1])?new Checker.Result(Checker.Status.CORRECT,-1,"정답"):new Checker.Result(Checker.Status.WRONG_ANSWER,1,"이 간격 확인");
 }
 public static void attach(Question q){int[] v=givens(q);if(v==null)return;int a=v[0],d=v[1];StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="linear-sequence-description-v1";
  g.step("첫 번째 수를 쓰세요.","첫 번째 수 = ","",""+a).step("두 번째 수를 쓰세요.","두 번째 수 = ","",""+(a+d)).step("두 수의 차이를 구하세요.","두 번째 수 − 첫 번째 수 = ","",""+d).step("차이의 크기로 간격을 구하세요.","차이의 크기 = ","",""+Math.abs(d));q.studyGuide=g;
 }
}
