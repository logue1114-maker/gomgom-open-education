package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
/** Selected finite two/three-digit written products; existing other-grade supply stays intact. */
public final class WrittenSingleProducts {
 private WrittenSingleProducts(){}
 public static final String KIND="writtenSingleProduct";
 static boolean selected(String id,CurriculumLimits limits){return Set.of("mul2","mul3").contains(id)&&limits.variedFacts();}
 static int count(String id){return id.equals("mul2")?900:id.equals("mul3")?9000:0;}
 static Question indexed(String id,int index){int count=count(id);if(index<0||index>=count)throw new IllegalArgumentException("written product index");int a=(id.equals("mul2")?10:100)+index/10,b=index%10;Question q=new Question(id,a+" × "+b,"",""+(a*b));q.kind=KIND;q.labels=new String[]{"답"};q.stepSupport=false;WholeProductRelations.attach(q);return q;}
 static Question next(String id,Random random,CurriculumLimits limits,Map<String,Integer> recent){return IndexedQuestionSupply.choose(count(id),i->indexed(id,i),random,limits,recent);}
 public static int[] read(Question q){if(q==null||!KIND.equals(q.kind)||!Set.of("mul2","mul3").contains(q.skillId)||q.prompt==null)return null;Matcher m=Pattern.compile("^([0-9]{2,3}) × ([0-9])$").matcher(q.prompt);if(!m.matches())return null;int a=Integer.parseInt(m.group(1)),b=Integer.parseInt(m.group(2)),minimum=q.skillId.equals("mul2")?10:100,maximum=q.skillId.equals("mul2")?99:999;return a<minimum||a>maximum?null:new int[]{a,b,a*b};}
 static Checker.Result check(Question q,List<String> answers){int[] v=read(q);if(v==null||answers==null||answers.size()!=1||answers.get(0)==null||!answers.get(0).trim().matches("[0-9]{1,4}"))return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"답 입력 필요");return Integer.parseInt(answers.get(0).trim())==v[2]?new Checker.Result(Checker.Status.CORRECT,-1,"정답"):new Checker.Result(Checker.Status.WRONG_ANSWER,0,"이 답 확인");}
}
