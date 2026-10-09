package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
/** Direct student expression writing; accepts algebraic equality, not text equality. */
public final class PrimaryExpressionWriting {
 private PrimaryExpressionWriting(){}
 public static final String ID="primaryWriteExpression";
 public static final Catalog.Skill SKILL=new Catalog.Skill(ID,"문자식 직접 쓰기",6,1,1,"","primaryExpressionWriting",100,"primaryExpression","문자로 나타낸 수의 계산 관계를 식으로 직접 쓴다.");
 static final String[] WORDS={"더할 수","뺄 수","곱할 수","나눌 수"};
 public static boolean selected(Question q){return q!=null&&ID.equals(q.skillId);}
 static String expression(int op,int n){return op==0?"x+"+n:op==1?"x-"+n:op==2?n+"x":"x/"+n;}
 static Question create(int op,int n){if(op<0||op>3||n<1||n>100)throw new IllegalArgumentException("expression domain");Question q=new Question(ID,"어떤 수를 x라고 합니다.\n"+WORDS[op]+": "+n+"\n계산 관계를 식으로 쓰세요.","",expression(op,n));q.kind="polynomial";q.labels=new String[]{"식"};q.stepSupport=false;attach(q);return q;}
 static Question next(Random random,Map<String,Integer> recent){Map<String,Question> pool=new LinkedHashMap<>();for(int op=0;op<4;op++)for(int n=1;n<=100;n++){Question q=create(op,n);pool.put(q.signature(),q);}return FactFoundations.choose(pool,random,recent);}
 static int[] givens(Question q){if(!selected(q)||q.prompt==null)return null;Matcher m=Pattern.compile("어떤 수를 x라고 합니다\\.\\n(더할 수|뺄 수|곱할 수|나눌 수): (\\d{1,3})\\n계산 관계를 식으로 쓰세요\\.").matcher(q.prompt);if(!m.matches())return null;int n=Integer.parseInt(m.group(2));return n>=1&&n<=100?new int[]{Arrays.asList(WORDS).indexOf(m.group(1)),n}:null;}
 static Checker.Result check(Question q,List<String> answers){int[] v=givens(q);if(v==null||answers.size()!=1||answers.get(0).isBlank())return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"식 입력 필요");String raw=answers.get(0);if(raw.length()>120)return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"식 입력 확인 필요");try{return Expression.parse(raw).equals(Expression.parse(expression(v[0],v[1])))?new Checker.Result(Checker.Status.CORRECT,-1,"정답"):new Checker.Result(Checker.Status.WRONG_ANSWER,0,"이 식 확인");}catch(RuntimeException e){return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"식 입력 확인 필요");}}
 public static void attach(Question q){int[] v=givens(q);if(v==null)return;StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="primary-expression-writing-v1";g.step("문제에 나온 계산할 수를 쓰세요.","계산할 수 = ","",""+v[1]);Map<String,String> labels=new LinkedHashMap<>();labels.put("0","더하기");labels.put("1","빼기");labels.put("2","곱하기");labels.put("3","나누기");g.choice("문제에 맞는 계산을 고르세요.",labels,""+v[0]);q.studyGuide=g;}
}
