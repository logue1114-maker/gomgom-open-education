package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
/** Small-prime recall; learner-sized list, never choice recognition or fixed answer-count slots. */
public final class PrimeRecall {
 private PrimeRecall(){}
 public static final String ID="smallPrimeRecall";
 public static final Catalog.Skill SKILL=new Catalog.Skill(ID,"작은 소수 직접 쓰기",5,1,1,"",ID,19,"primeCompositeClassify","19까지의 소수를 떠올려 직접 쓴다. 소수의 양의 약수는 1과 자기 자신뿐이다.");
 private static final Pattern PROMPT=Pattern.compile("^2 이상 ([0-9]{1,2}) 이하의 소수를 모두 쓰세요\\.\\n작은 수부터 쓰세요\\.$");
 public static boolean supports(String id){return ID.equals(id);}
 public static int read(Question q){if(q==null||!supports(q.skillId)||q.prompt==null)return -1;Matcher m=PROMPT.matcher(q.prompt);if(!m.matches())return -1;int n=Integer.parseInt(m.group(1));return n>=2&&n<=19?n:-1;}
 static boolean prime(int n){if(n<2)return false;for(int d=2;d*d<=n;d++)if(n%d==0)return false;return true;}
 static List<String> values(int max){List<String> values=new ArrayList<>();for(int n=2;n<=max;n++)if(prime(n))values.add(""+n);return values;}
 static Question make(int max){Question q=new Question(ID,"2 이상 "+max+" 이하의 소수를 모두 쓰세요.\n작은 수부터 쓰세요.","",values(max).toArray(String[]::new));if(read(q)<0)throw new IllegalArgumentException("small prime recall range");q.labels=new String[q.answers.length];for(int i=0;i<q.labels.length;i++)q.labels[i]="소수 "+(i+1);q.stepSupport=false;attach(q);return q;}
 static Question next(Random random,CurriculumLimits limits,Map<String,Integer> recent){return IndexedQuestionSupply.choose(18,i->make(i+2),random,limits,recent);}
 static Checker.Result check(Question q,List<String> answers){int max=read(q);if(max<0||answers==null||answers.isEmpty())return new Checker.Result(Checker.Status.INPUT_NEEDED,-1,"수 입력 필요");Set<Integer> seen=new HashSet<>();int previous=0;for(int i=0;i<answers.size();i++){String s=answers.get(i);if(s==null||!s.trim().matches("[0-9]{1,2}"))return new Checker.Result(Checker.Status.INPUT_NEEDED,i,"수 입력 필요");int n=Integer.parseInt(s.trim());if(n>max||!prime(n))return new Checker.Result(Checker.Status.WRONG_ANSWER,i,"이 소수 확인");if(!seen.add(n))return new Checker.Result(Checker.Status.WRONG_ANSWER,i,"중복된 수 확인");if(n<=previous)return new Checker.Result(Checker.Status.WRONG_ANSWER,i,"작은 수부터 입력");previous=n;}if(answers.size()!=values(max).size())return new Checker.Result(Checker.Status.WRONG_ANSWER,-1,"빠진 소수 확인");return new Checker.Result(Checker.Status.CORRECT,-1,"정답");}
 public static void attach(Question q){if(read(q)<0)return;StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="small-prime-recall-v1";g.step("소수는 1과 자기 자신으로만 나누어떨어지는 자연수입니다. 양의 약수가 몇 개인지 쓰세요.","소수의 양의 약수 개수 = ","","2");q.studyGuide=g;}
}
