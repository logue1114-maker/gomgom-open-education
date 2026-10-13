package com.gomgomapps.math.core;
import java.util.*;
import java.util.regex.*;
/** Public last-digit/digit-sum evidence for factor and multiple relations, including zero. */
public final class DivisibilityRules {
 private DivisibilityRules(){}
 public static final String ID="divisibilityRules";
 public static final Catalog.Skill SKILL=new Catalog.Skill(ID,"나눗셈 규칙으로 확인",7,1,1,"",ID,1000000,"tables","일의 자리와 각 자리 숫자의 합으로 약수와 배수 관계를 확인한다.");
 public record Givens(int number,int divisor,String relation){}
 private static final int[] DIVISORS={2,3,5};
 private static String particle(int n){return Set.of(2,4,5,9).contains(n%10)?"는":"은";}
 private static String question(int n,int d,String relation){return relation.equals("factor")?d+particle(d)+" "+n+"의 약수인가요?":n+particle(n)+" "+d+"의 배수인가요?";}
 public static Givens read(Question q){
  if(q==null||!ID.equals(q.skillId)||q.prompt==null)return null;Matcher m=Pattern.compile("주어진 수: ([0-9]{1,7}) · 나눌 수: (2|3|5)\\n(.+)").matcher(q.prompt);if(!m.matches())return null;int n=Integer.parseInt(m.group(1)),d=Integer.parseInt(m.group(2));if(n>1000000)return null;String relation=m.group(3).equals(question(n,d,"factor"))?"factor":m.group(3).equals(question(n,d,"multiple"))?"multiple":null;return relation==null?null:new Givens(n,d,relation);
 }
 public static int ruleValue(Givens v){if(v.divisor!=3)return v.number%10;int n=v.number,sum=0;while(n>0){sum+=n%10;n/=10;}return sum;}
 public static boolean verdict(Givens v){return v.number%v.divisor==0;}
 public static Map<String,String> options(Question q){Givens v=read(q);if(v==null)return Map.of();Map<String,String> choices=new LinkedHashMap<>();choices.put("0",v.relation.equals("factor")?"약수가 아니다":"배수가 아니다");choices.put("1",v.relation.equals("factor")?"약수이다":"배수이다");return choices;}
 static Question make(int n,int d,String relation){if(n<0||n>1000000||!Set.of(2,3,5).contains(d)||!Set.of("factor","multiple").contains(relation))throw new IllegalArgumentException("divisibility public domain");Givens v=new Givens(n,d,relation);Question q=new Question(ID,"주어진 수: "+n+" · 나눌 수: "+d+"\n"+question(n,d,relation),"",""+ruleValue(v),verdict(v)?"1":"0");q.kind=ID;q.labels=new String[]{d==3?"각 자리 숫자의 합":"일의 자리 숫자",relation.equals("factor")?"약수 확인":"배수 확인"};q.stepSupport=false;attach(q);return q;}
 static int count(int maximum){if(maximum<0||maximum>1000000)throw new IllegalArgumentException("divisibility maximum");return (maximum+1)*6;}
 static Question indexed(int index,int maximum){if(index<0||index>=count(maximum))throw new IllegalArgumentException("divisibility index");return make(index/6,DIVISORS[index%6/2],index%2==0?"factor":"multiple");}
 static int maximum(CurriculumLimits limits){return Math.min(1000000,Math.min(limits.wholeMaximum(1000000),limits.givenMaximum(1000000)));}
 static Question create(Random random,CurriculumLimits limits){int max=maximum(limits);return indexed(random.nextInt(count(max)),max);}
 static Question next(Random random,CurriculumLimits limits,Map<String,Integer> recent){int max=maximum(limits);return IndexedQuestionSupply.choose(count(max),index->indexed(index,max),random,limits,recent);}
 static Checker.Result check(Question q,List<String> answers){Givens v=read(q);if(v==null||answers==null||answers.size()!=2)return new Checker.Result(Checker.Status.INPUT_NEEDED,-1,"답 입력 필요");String value=answers.get(0);if(value==null||!value.trim().matches("[0-9]{1,2}"))return new Checker.Result(Checker.Status.INPUT_NEEDED,0,v.divisor==3?"자리 숫자의 합 입력 필요":"일의 자리 입력 필요");int witness=Integer.parseInt(value.trim()),max=v.divisor==3?54:9;if(witness>max)return new Checker.Result(Checker.Status.WRONG_ANSWER,0,v.divisor==3?"이 자리 숫자의 합 확인":"이 일의 자리 숫자 확인");if(witness!=ruleValue(v))return new Checker.Result(Checker.Status.WRONG_ANSWER,0,v.divisor==3?"이 자리 숫자의 합 확인":"이 일의 자리 숫자 확인");String choice=answers.get(1);if(!Set.of("0","1").contains(choice==null?"":choice))return new Checker.Result(Checker.Status.INPUT_NEEDED,1,"관계 선택 필요");boolean right=choice.equals(verdict(v)?"1":"0");return new Checker.Result(right?Checker.Status.CORRECT:Checker.Status.WRONG_ANSWER,right?-1:1,right?"정답":v.relation.equals("factor")?"약수 관계 확인":"배수 관계 확인");}
 public static void attach(Question q){Givens v=read(q);if(v==null)return;StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="divisibility-rules-v1";g.step("주어진 수를 쓰세요.","주어진 수 = ","",""+v.number);g.step(v.divisor==3?"주어진 수의 각 자리 숫자를 모두 더하세요.":"주어진 수의 일의 자리 숫자를 쓰세요.",v.divisor==3?"각 자리 숫자의 합 = ":"일의 자리 숫자 = ","",""+ruleValue(v));String rule=v.divisor==2?"일의 자리 숫자가 0, 2, 4, 6, 8이면 2로 나누어떨어집니다.":v.divisor==5?"일의 자리 숫자가 0 또는 5이면 5로 나누어떨어집니다.":"각 자리 숫자의 합이 3으로 나누어떨어지면 주어진 수도 3으로 나누어떨어집니다.";g.choice(rule+" 문제의 약수 또는 배수 관계를 고르세요.",options(q),verdict(v)?"1":"0");q.studyGuide=g;}
}
