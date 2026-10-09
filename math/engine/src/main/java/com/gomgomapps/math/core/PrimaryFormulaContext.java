package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
/** Learner authors the general total formula, then evaluates a specified x. */
public final class PrimaryFormulaContext {
 private PrimaryFormulaContext(){}
 public static final String ID="primaryFormulaContext";
 public static final String TEMPLATE="전체 수 = 한 묶음의 수 × x + 낱개의 수";
 public static final Catalog.Skill SKILL=new Catalog.Skill(ID,"전체 수의 식과 값 구하기",6,1,2,"","primaryFormulaContext",252,PrimaryExpressionWriting.ID,"묶음과 낱개의 관계를 문자식으로 나타내고 주어진 묶음 수를 넣어 계산한다.");
 public static boolean selected(Question q){return q!=null&&ID.equals(q.skillId);}
 static Question create(int story,int each,int loose,int x){if(story<0||story>1||each<1||each>12||loose<0||loose>12||x<0||x>20)throw new IllegalArgumentException("formula domain");String line=story==0?"색연필: 한 상자 "+each+"개, 낱개 "+loose+"개":"딱지: 한 봉지 "+each+"개, 낱개 "+loose+"개";
  Question q=new Question(ID,line+"\n묶음 수: x, 이번에는 x = "+x+"\nx로 전체 수의 식을 쓰고 전체 수를 구하세요.","",each+"x+"+loose,""+(each*x+loose));q.kind="polynomial";q.labels=new String[]{"식","전체 수"};q.stepSupport=false;attach(q);return q;}
 static Question next(Random random,Map<String,Integer> recent){Map<String,Question> pool=new LinkedHashMap<>();for(int story=0;story<2;story++)for(int a=1;a<=12;a++)for(int b=0;b<=12;b++)for(int x=0;x<=20;x++){Question q=create(story,a,b,x);pool.put(q.signature(),q);}return FactFoundations.choose(pool,random,recent);}
 static int[] givens(Question q){if(!selected(q)||q.prompt==null)return null;Matcher m=Pattern.compile("(?:색연필: 한 상자|딱지: 한 봉지) (\\d{1,2})개, 낱개 (\\d{1,2})개\\n묶음 수: x, 이번에는 x = (\\d{1,2})\\nx로 전체 수의 식을 쓰고 전체 수를 구하세요\\.").matcher(q.prompt);if(!m.matches())return null;int a=Integer.parseInt(m.group(1)),b=Integer.parseInt(m.group(2)),x=Integer.parseInt(m.group(3));return a>=1&&a<=12&&b<=12&&x<=20?new int[]{a,b,x}:null;}
 static Checker.Result check(Question q,List<String> answers){int[] v=givens(q);if(v==null||answers.size()!=2||answers.get(0).isBlank())return new Checker.Result(Checker.Status.INPUT_NEEDED,-1,"식 입력 필요");String raw=answers.get(0),number=answers.get(1).trim();if(raw.length()>120)return new Checker.Result(Checker.Status.INPUT_NEEDED,-1,"식 입력 확인 필요");
  try{if(!Expression.parse(raw).equals(Expression.parse(v[0]+"x+"+v[1])))return new Checker.Result(Checker.Status.WRONG_ANSWER,0,"이 식 확인");}catch(RuntimeException e){return new Checker.Result(Checker.Status.INPUT_NEEDED,-1,"식 입력 확인 필요");}
  if(!number.matches("[0-9]{1,9}"))return new Checker.Result(Checker.Status.INPUT_NEEDED,-1,"전체 수 입력 필요");return Integer.parseInt(number)==v[0]*v[2]+v[1]?new Checker.Result(Checker.Status.CORRECT,-1,"정답"):new Checker.Result(Checker.Status.WRONG_ANSWER,1,"이 전체 수 확인");
 }
 public static void attach(Question q){int[] v=givens(q);if(v==null)return;StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="primary-formula-context-v1";
  g.step("한 묶음의 수를 쓰세요.","한 묶음의 수 = ","",""+v[0]).step("낱개의 수를 쓰세요.","낱개의 수 = ","",""+v[1]).step("이번 묶음 수를 쓰세요.","묶음 수 = ","",""+v[2]).step("묶음에 들어 있는 전체 수를 구하세요.","한 묶음의 수 × 묶음 수 = ","",""+(v[0]*v[2])).step("묶음의 물건과 낱개를 합하세요.","묶음의 전체 수 + 낱개의 수 = ","",""+(v[0]*v[2]+v[1]));q.studyGuide=g;
 }
}
