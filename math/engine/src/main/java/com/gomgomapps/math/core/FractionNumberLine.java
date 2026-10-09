package com.gomgomapps.math.core;
import java.util.*;
/** Fractions as positions on a public zero-to-one line; no hidden-key dependence. */
public final class FractionNumberLine {
 private FractionNumberLine(){}
 public static final String ID="fractionNumberLine",PROMPT="0과 1 사이의 점 A와 B를 각각 분수로 나타내세요.";
 public static final Catalog.Skill SKILL=new Catalog.Skill(ID,"수직선의 분수",3,1,6,"","fractionNumberLine",10,"fractionPart","0과 1 사이의 같은 간격과 점의 위치를 보고 분수를 쓴다.");
 static Question make(int den,int a,int b){
  if(den<2||den>10||a<1||a>=b||b>den)throw new IllegalArgumentException("Fraction line domain");
  Question q=new Question(ID,PROMPT,"",Rational.of(a,den).toString(),Rational.of(b,den).toString());q.labels=new String[]{"점 A","점 B"};q.answerFormat="fraction";q.stepSupport=false;q.diagram=new StudyDiagram("fractionNumberLine",new double[]{den,a,b});return q;
 }
 public static int[] read(Question q){
  if(q==null||!ID.equals(q.skillId)||!PROMPT.equals(q.prompt)||q.diagram==null||!ID.equals(q.diagram.type)||q.diagram.values.length!=3)return null;
  int[] v=new int[3];for(int i=0;i<3;i++){double n=q.diagram.values[i];if(!Double.isFinite(n)||n!=(int)n)return null;v[i]=(int)n;}
  return v[0]>=2&&v[0]<=10&&v[1]>=1&&v[1]<v[2]&&v[2]<=v[0]?v:null;
 }
 static Question next(Random random,CurriculumLimits limits,Map<String,Integer> recent){
  Map<String,Question> pool=new LinkedHashMap<>();for(int d=2;d<=10;d++)for(int a=1;a<d;a++)for(int b=a+1;b<=d;b++){Question q=make(d,a,b);if(limits.allows(q))pool.put(q.signature(),q);}Question q=FactFoundations.choose(pool,random,recent);attach(q);return q;
 }
 public static void attach(Question q){int[] v=read(q);if(v==null)return;StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="fraction-number-line-v1";
  g.step("0과 1 사이의 전체 칸 수를 세세요.","전체 칸 수 = ","",""+v[0]);
  for(int i=1;i<=2;i++){String name=i==1?"A":"B";g.step("0에서 점 "+name+"까지 이동한 칸 수를 세세요.","점 "+name+"까지 칸 수 = ","",""+v[i]);g.step("이동한 칸 수를 분자로, 전체 칸 수를 분모로 쓰세요.","점 "+name+" = ","",v[i]+"/"+v[0]);}q.studyGuide=g;
 }
 static Checker.Result check(Question q,List<String> answers){int[] v=read(q);if(v==null||answers==null||answers.size()!=2)return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"답 입력 필요");
  for(int i=0;i<2;i++){String raw=answers.get(i);if(raw==null)return new Checker.Result(Checker.Status.INPUT_NEEDED,i,"답 입력 필요");String value=Expression.normalize(raw.trim());if(!value.matches("[+-]?\\d+(?:\\.\\d+)?(?:/[+-]?\\d+)?"))return new Checker.Result(Checker.Status.INPUT_NEEDED,i,"마지막 답은 수로 입력");try{if(!Expression.number(value).equals(Rational.of(v[i+1],v[0])))return new Checker.Result(Checker.Status.WRONG_ANSWER,i,"이 답 확인");}catch(IllegalArgumentException|ArithmeticException ex){return new Checker.Result(Checker.Status.INPUT_NEEDED,i,"답의 기호 확인 필요");}}
  return new Checker.Result(Checker.Status.CORRECT,-1,"정답");
 }
}
