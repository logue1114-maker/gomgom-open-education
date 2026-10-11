package com.gomgomapps.math.core;
import java.util.*;
import java.util.regex.*;
/** Classify the displayed number from its remainder, never from saved answers. */
public final class MultipleRecognition {
 private MultipleRecognition(){}
 public static final String ID="multipleRecognition";
 private static final List<Integer> BASES=List.of(15,30,45,60);
 public static final Catalog.Skill SKILL=new Catalog.Skill(ID,"배수인지 확인",6,1,1,"",ID,360,"divide","나머지가 0이면 나누어지는 수는 나누는 수의 배수이다.");
 private static final Pattern PROMPT=Pattern.compile("^([0-9]{1,3})은 ([0-9]{1,2})의 배수인가요\\?$");
 public static boolean supports(String id){return ID.equals(id);}
 public static int[] read(Question q){if(q==null||!supports(q.skillId)||q.prompt==null)return null;Matcher m=PROMPT.matcher(q.prompt);if(!m.matches())return null;int n=Integer.parseInt(m.group(1)),base=Integer.parseInt(m.group(2));return n<=360&&BASES.contains(base)?new int[]{n,base}:null;}
 private static Map<String,String> labels(){Map<String,String> labels=new LinkedHashMap<>();labels.put("1","배수이다");labels.put("0","배수가 아니다");return labels;}
 static Question make(int n,int base){Question q=new Question(ID,n+"은 "+base+"의 배수인가요?","","pending");int[] given=read(q);if(given==null)throw new IllegalArgumentException("multiple recognition domain");q.answers=new String[]{n%base==0?"1":"0"};q.stepSupport=false;q.choiceLabels=labels();q.choices=new ArrayList<>(q.choiceLabels.keySet());attach(q);return q;}
 static Question next(Random random,CurriculumLimits limits,Map<String,Integer> recent){
  int low=limits.givenMinimum(0),high=limits.givenMaximum(360);boolean multiple=random.nextBoolean();List<Integer> preferred=new ArrayList<>();
  for(int n=low;n<=high;n++)for(int b=0;b<BASES.size();b++)if((n%BASES.get(b)==0)==multiple)preferred.add(n*4+b);
  if(!preferred.isEmpty()){Question q=IndexedQuestionSupply.choose(preferred.size(),i->{int index=preferred.get(i);return make(index/4,BASES.get(index%4));},random,limits,recent);if(!recent.containsKey(q.signature()))return q;}
  return IndexedQuestionSupply.choose(361*4,i->make(i/4,BASES.get(i%4)),random,limits,recent);
 }
 static Checker.Result check(Question q,List<String> answers){int[] g=read(q);if(g==null||answers==null||answers.size()!=1||!labels().containsKey(answers.get(0)))return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"분류 선택 필요");boolean correct=(g[0]%g[1]==0?"1":"0").equals(answers.get(0));return new Checker.Result(correct?Checker.Status.CORRECT:Checker.Status.WRONG_ANSWER,correct?-1:0,correct?"정답":"선택한 분류 확인");}
 public static void attach(Question q){int[] g=read(q);if(g==null)return;int quotient=g[0]/g[1];StudyGuide h=new StudyGuide().transfer(false);h.teachingVersion="multiple-recognition-v1";h.step("주어진 수를 나누고 정수 몫을 쓰세요.","정수 몫 = ","",""+quotient);h.step("나누는 수에 정수 몫을 곱하세요.","나누는 수 × 정수 몫 = ","",""+(g[1]*quotient));h.step("주어진 수에서 확인한 곱을 빼세요.","나머지 = ","",""+(g[0]%g[1]));h.choice("나머지가 0이면 배수입니다.",labels(),g[0]%g[1]==0?"1":"0");q.studyGuide=h;}
}
