package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
/** Selected whole-number linear sequences, including decreasing rules. */
public final class PrimaryLinearSequences {
 private PrimaryLinearSequences(){}
 public static final String BUILD="primarySequenceBuild",RULE="primarySequenceRule";
 public static final List<Catalog.Skill> SKILLS=List.of(
  new Catalog.Skill(BUILD,"규칙으로 수열 만들기",6,1,2,"","primarySequence",100,"el_number_pattern","시작 수에 같은 수를 더해 다음 세 수를 만든다."),
  new Catalog.Skill(RULE,"수열의 규칙 나타내기",6,1,2,"","primarySequence",100,BUILD,"앞의 수에 더할 수를 찾아 선형 수열의 규칙을 나타낸다."));
 public static boolean supports(String id){return BUILD.equals(id)||RULE.equals(id);}
 static Question create(String id,int start,int delta){
  if(!supports(id)||delta==0||Math.abs(delta)>12||start<0||start>100||start+3*delta<0||start+3*delta>100)throw new IllegalArgumentException("sequence domain");
  Question q;
  if(BUILD.equals(id)){q=new Question(id,"시작 수: "+start+"\n매번 더할 수: "+delta+"\n다음 세 수를 쓰세요.","",""+(start+delta),""+(start+2*delta),""+(start+3*delta));q.labels=new String[]{"첫째","둘째","셋째"};}
  else q=new Question(id,start+" → "+(start+delta)+" → "+(start+2*delta)+" → "+(start+3*delta)+"\n앞의 수 + □ = 다음 수\n매번 더할 수를 쓰세요.","",""+delta);
  q.stepSupport=false;attach(q);return q;
 }
 static Question next(Catalog.Skill skill,Random random,Map<String,Integer> recent){
  Map<String,Question> pool=new LinkedHashMap<>();for(int d=-12;d<=12;d++)if(d!=0)for(int a=0;a<=100;a++)if(a+3*d>=0&&a+3*d<=100){Question q=create(skill.id,a,d);pool.put(q.signature(),q);}return FactFoundations.choose(pool,random,recent);
 }
 static int[] givens(Question q){
  if(q==null||!supports(q.skillId)||q.prompt==null)return null;
  Matcher m=Pattern.compile("시작 수: (\\d+)\\n매번 더할 수: (-?\\d+)\\n다음 세 수를 쓰세요\\.").matcher(q.prompt);int a,d;
  try{
   if(BUILD.equals(q.skillId)){if(!m.matches())return null;a=Integer.parseInt(m.group(1));d=Integer.parseInt(m.group(2));}
   else {m=Pattern.compile("(\\d+) → (\\d+) → (\\d+) → (\\d+)\\n앞의 수 \\+ □ = 다음 수\\n매번 더할 수를 쓰세요\\.").matcher(q.prompt);if(!m.matches())return null;a=Integer.parseInt(m.group(1));d=Integer.parseInt(m.group(2))-a;if(Integer.parseInt(m.group(3))!=a+2*d||Integer.parseInt(m.group(4))!=a+3*d)return null;}
   return a>=0&&a<=100&&d!=0&&Math.abs(d)<=12&&a+3*d>=0&&a+3*d<=100?new int[]{a,d}:null;
  }catch(NumberFormatException e){return null;}
 }
 public static void attach(Question q){
  int[] v=givens(q);if(v==null)return;int a=v[0],d=v[1];StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="primary-linear-sequences-v1";
  if(BUILD.equals(q.skillId)){
   g.step("시작 수를 쓰세요.","시작 수 = ","",""+a).step("매번 더할 수를 쓰세요.","매번 더할 수 = ","",""+d)
    .step("같은 수를 더해 첫째 수를 구하세요.","시작 수 + 매번 더할 수 = ","",""+(a+d))
    .step("같은 수를 더해 둘째 수를 구하세요.","첫째 수 + 매번 더할 수 = ","",""+(a+2*d))
    .step("같은 수를 더해 셋째 수를 구하세요.","둘째 수 + 매번 더할 수 = ","",""+(a+3*d));
  }else g.step("첫 번째 수를 쓰세요.","첫 번째 수 = ","",""+a).step("두 번째 수를 쓰세요.","두 번째 수 = ","",""+(a+d)).step("두 수의 차이로 규칙을 나타내세요.","두 번째 수 − 첫 번째 수 = ","",""+d);
  q.studyGuide=g;
 }
}
