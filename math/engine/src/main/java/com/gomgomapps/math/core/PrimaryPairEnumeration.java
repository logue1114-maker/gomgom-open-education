package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
/** Public relation plus learner-owned pair list. The first two answer slots are the editable draft. */
public final class PrimaryPairEnumeration {
 private PrimaryPairEnumeration(){}
 public static final String ID="primaryPairEnumeration";
 public static final Catalog.Skill SKILL=new Catalog.Skill(ID,"가능한 쌍 모두 찾기",6,1,3,"","primaryAlgebra",36,PrimaryAlgebra.PAIR,"두 문자의 조사 범위를 정하고 가능한 정수 쌍을 빠짐없이 나열한다.");
 public static boolean selected(Question q){return q!=null&&ID.equals(q.skillId)&&"primaryPairSet".equals(q.kind);}
 public static int[] givens(Question q){
  if(!selected(q))return null;Matcher m=Pattern.compile("(\\d+)x \\+ (\\d+)y = (\\d+)\\nx와 y는 0 이상의 정수입니다.\\n식을 만족하는 모든 쌍을 쓰세요.").matcher(q.prompt);if(!m.matches())return null;
  int a=Integer.parseInt(m.group(1)),b=Integer.parseInt(m.group(2)),c=Integer.parseInt(m.group(3));return a>=1&&a<=6&&b>=1&&b<=6&&c>=2&&c<=36?new int[]{a,b,c}:null;
 }
 static Question create(int a,int b,int c){Question q=new Question(ID,a+"x + "+b+"y = "+c+"\nx와 y는 0 이상의 정수입니다.\n식을 만족하는 모든 쌍을 쓰세요.","","","");q.kind="primaryPairSet";q.labels=new String[]{"x","y"};q.stepSupport=false;attach(q);return q;}
 static Question next(Random random,Map<String,Integer> recent){Map<String,Question> pool=new LinkedHashMap<>();for(int a=1;a<=6;a++)for(int b=1;b<=6;b++)for(int c=2;c<=36;c++)if(PrimaryAlgebra.pairCount(a,b,c)>=2){Question q=create(a,b,c);pool.put(q.signature(),q);}return FactFoundations.choose(pool,random,recent);}
 private static Checker.Result input(String message){return new Checker.Result(Checker.Status.INPUT_NEEDED,-1,message);}
 private static Checker.Result wrong(String message){return new Checker.Result(Checker.Status.WRONG_ANSWER,-1,message);}
 private static int[] pair(String x,String y){if(x==null||y==null||!x.trim().matches("[0-9]{1,9}")||!y.trim().matches("[0-9]{1,9}"))return null;return new int[]{Integer.parseInt(x.trim()),Integer.parseInt(y.trim())};}
 private static String key(int[] pair){return pair[0]+","+pair[1];}
 private static boolean valid(int[] g,int[] pair){return (long)g[0]*pair[0]+(long)g[1]*pair[1]==g[2];}
 /** Successful add appends only the student's validated pair and clears only the draft slots. */
 public static Checker.Result add(Question q,List<String> answers){
  int[] g=givens(q);if(g==null||answers.size()<2||answers.size()%2!=0)return input("답 입력 필요");
  int[] draft=pair(answers.get(0),answers.get(1));if(draft==null)return input("답 입력 필요");if(!valid(g,draft))return wrong("이 쌍 확인");
  for(int i=2;i<answers.size();i+=2){int[] prior=pair(answers.get(i),answers.get(i+1));if(prior!=null&&key(prior).equals(key(draft)))return wrong("이미 쓴 쌍");}
  answers.add(""+draft[0]);answers.add(""+draft[1]);answers.set(0,"");answers.set(1,"");return new Checker.Result(Checker.Status.CORRECT,-1,"쌍 추가 완료");
 }
 public static void remove(List<String> answers,int index){int first=2+2*index;if(index<0||first+1>=answers.size())throw new IllegalArgumentException("Pair index");answers.remove(first+1);answers.remove(first);}
 /** Completeness is derived from the public domain, never a preferred hidden answer order. */
 public static Checker.Result check(Question q,List<String> answers){
  int[] g=givens(q);if(g==null||answers.size()<2||answers.size()%2!=0)return input("답 입력 필요");
  if(!answers.get(0).trim().isEmpty()||!answers.get(1).trim().isEmpty())return input("쌍 추가 필요");
  Set<String> own=new HashSet<>();for(int i=2;i<answers.size();i+=2){int[] v=pair(answers.get(i),answers.get(i+1));if(v==null)return input("답 입력 필요");if(!valid(g,v))return wrong("이 쌍 확인");if(!own.add(key(v)))return wrong("이미 쓴 쌍");}
  int count=PrimaryAlgebra.pairCount(g[0],g[1],g[2]);if(own.size()!=count)return wrong("아직 빠진 쌍이 있습니다.");return new Checker.Result(Checker.Status.CORRECT,-1,"정답");
 }
 public static void attach(Question q){
  int[] g=givens(q);if(g==null)return;StudyGuide guide=new StudyGuide().transfer(false);guide.teachingVersion="primary-pair-enumeration-v1";
  guide.step("x 앞의 수를 쓰세요.","x 앞의 수 = ","",""+g[0]).step("y 앞의 수를 쓰세요.","y 앞의 수 = ","",""+g[1]).step("등호 오른쪽의 수를 쓰세요.","오른쪽 수 = ","",""+g[2]);
  guide.step("x에 넣어 조사할 가장 큰 정수를 구하세요.","오른쪽 수 ÷ x 앞의 수의 몫 = ","",""+(g[2]/g[0])).step("y에 넣어 조사할 가장 큰 정수를 구하세요.","오른쪽 수 ÷ y 앞의 수의 몫 = ","",""+(g[2]/g[1]));q.studyGuide=guide;
 }
}
