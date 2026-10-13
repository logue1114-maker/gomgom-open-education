package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
/** Distinct prime divisors, not a repeated-factor product; learner owns the list length. */
public final class PrimeFactorLists {
 private PrimeFactorLists(){}
 public static final String ID="distinctPrimeFactorList",NONE="@no-prime-factors";
 public static final Catalog.Skill SKILL=new Catalog.Skill(ID,"서로 다른 소인수 쓰기",7,1,1,"",ID,99,"primeCompositeClassify,allPositiveFactors","주어진 수의 서로 다른 소인수를 빠짐없이 쓴다.");
 public static boolean supports(String id){return ID.equals(id);}
 public static int read(Question q){if(q==null||!supports(q.skillId)||q.prompt==null)return -1;Matcher m=Pattern.compile("([0-9]{1,2})의 서로 다른 소인수를 모두 쓰세요\\.\\n작은 수부터 쓰세요\\.").matcher(q.prompt);if(!m.matches())return -1;int n=Integer.parseInt(m.group(1));return n>=1&&n<=99?n:-1;}
 public static boolean noneExpected(Question q){return read(q)==1;}
 private static List<String> values(int n){List<String> result=new ArrayList<>();for(int d=2;d<=n;d++)if(n%d==0&&PrimeRecall.prime(d))result.add(""+d);return result;}
 static Question make(int n){if(n<1||n>99)throw new IllegalArgumentException("prime factor list domain");List<String>list=values(n);Question q=new Question(ID,n+"의 서로 다른 소인수를 모두 쓰세요.\n작은 수부터 쓰세요.","",list.isEmpty()?new String[]{NONE}:list.toArray(String[]::new));q.kind=ID;q.labels=new String[q.answers.length];for(int i=0;i<q.labels.length;i++)q.labels[i]="소인수 "+(i+1);q.stepSupport=false;attach(q);return q;}
 static int count(){return 99;}
 static Question indexed(int index){if(index<0||index>=count())throw new IllegalArgumentException("prime factor list index");return make(index+1);}
 static Question next(Random random,CurriculumLimits limits,Map<String,Integer>recent){return IndexedQuestionSupply.choose(count(),PrimeFactorLists::indexed,random,limits,recent);}
 static Checker.Result check(Question q,List<String>answers){int n=read(q);if(n<1||answers==null||answers.isEmpty())return new Checker.Result(Checker.Status.INPUT_NEEDED,-1,"소인수 입력 또는 없음 선택");if(NONE.equals(answers.get(answers.size()-1)))return new Checker.Result(n==1?Checker.Status.CORRECT:Checker.Status.WRONG_ANSWER,-1,n==1?"정답":"소인수 있음 확인");Set<Integer>seen=new HashSet<>();int previous=0;for(int i=0;i<answers.size();i++){String raw=answers.get(i);if(raw==null||!raw.trim().matches("[0-9]{1,2}"))return new Checker.Result(Checker.Status.INPUT_NEEDED,i,"소인수 입력 또는 없음 선택");int d=Integer.parseInt(raw.trim());if(d<2||d>n||!PrimeRecall.prime(d)||n%d!=0)return new Checker.Result(Checker.Status.WRONG_ANSWER,i,"이 소인수 확인");if(!seen.add(d))return new Checker.Result(Checker.Status.WRONG_ANSWER,i,"중복된 수 확인");if(d<=previous)return new Checker.Result(Checker.Status.WRONG_ANSWER,i,"작은 수부터 입력");previous=d;}if(answers.size()!=values(n).size())return new Checker.Result(Checker.Status.WRONG_ANSWER,-1,"빠진 소인수 확인");return new Checker.Result(Checker.Status.CORRECT,-1,"정답");}
 public static void attach(Question q){int n=read(q);if(n<1)return;StudyGuide guide=new StudyGuide().transfer(false);guide.teachingVersion="distinct-prime-factor-list-v1";if(n==1){Map<String,String>options=new LinkedHashMap<>();options.put("0","소인수가 없다");options.put("1","소인수가 있다");guide.choice("1의 양의 약수는 1뿐이며, 1은 소수가 아닙니다. 소인수가 있는지 고르세요.",options,"0");}else guide.step("소인수는 주어진 수의 약수이면서 소수인 수입니다. 소수의 양의 약수 개수를 쓰세요.","소수의 양의 약수 개수 = ","","2");q.studyGuide=guide;}
}
