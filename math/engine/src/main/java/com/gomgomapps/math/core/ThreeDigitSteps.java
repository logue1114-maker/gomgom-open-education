package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
/** Addition through 500 and three-digit subtraction starts with two/three-digit place decomposition, bounded independently of large-number practice. */
public final class ThreeDigitSteps {
 private ThreeDigitSteps(){}
 public static final String ADD="threeDigitStepAdd",SUB="threeDigitStepSub";
 public static final List<Catalog.Skill> SKILLS=List.of(
  new Catalog.Skill(ADD,"자리값으로 나눠 더하기",3,1,1,"","threeDigitStep",500,MentalSumStrategies.ADD,"둘째 수를 자리값으로 나눠 차례로 더한다."),
  new Catalog.Skill(SUB,"자리값으로 나눠 빼기",3,1,1,"","threeDigitStep",500,MentalSumStrategies.SUB,"둘째 수를 자리값으로 나눠 차례로 뺀다."));
 private static final int[] SECONDS=java.util.stream.IntStream.rangeClosed(10,500).filter(ThreeDigitSteps::multiplePlaces).toArray();
 public static boolean supports(String id){return ADD.equals(id)||SUB.equals(id);}
 static boolean multiplePlaces(int value){int places=0;for(int n=value;n>0;n/=10)if(n%10!=0)places++;return places>=2;}
 private static int available(int maximum){int at=Arrays.binarySearch(SECONDS,maximum);return at>=0?at+1:-at-1;}
 static int count(String id,int maximum){if(!supports(id)||maximum<100||maximum>500)throw new IllegalArgumentException("three-digit range");int count=0;for(int a=ADD.equals(id)?0:100;a<=maximum;a++)count+=available(ADD.equals(id)?maximum-a:a);return count;}
 static Question at(String id,int maximum,int index){
  if(index<0||index>=count(id,maximum))throw new IllegalArgumentException("three-digit index");
  for(int a=ADD.equals(id)?0:100;a<=maximum;a++){int width=available(ADD.equals(id)?maximum-a:a);if(index<width)return make(id,a,SECONDS[index]);index-=width;}
  throw new IllegalStateException("three-digit index exhausted");
 }
 static Question make(String id,int a,int b){
  boolean add=ADD.equals(id);int result=add?a+b:a-b;
  if(!supports(id)||a<(add?0:100)||a>500||b<10||b>500||result<0||result>500)throw new IllegalArgumentException("three-digit domain");
  Question q=new Question(id,a+(add?" + ":" − ")+b,"",Integer.toString(result));q.kind="threeDigitStep";q.labels=new String[]{"답"};q.stepSupport=false;attach(q);return q.withInputs(a,b);
 }
 public static int[] read(Question q){
  if(q==null||!supports(q.skillId)||q.prompt==null)return null;
  Matcher m=Pattern.compile("([0-9]{1,3}) ([+−-]) ([0-9]{2,3})").matcher(q.prompt);if(!m.matches()||ADD.equals(q.skillId)!=m.group(2).equals("+"))return null;
  int a=Integer.parseInt(m.group(1)),b=Integer.parseInt(m.group(3)),result=ADD.equals(q.skillId)?a+b:a-b;
  return a<(ADD.equals(q.skillId)?0:100)||a>500||b<10||b>500||result<0||result>500?null:new int[]{a,b,result};
 }
 static Question next(String id,Random random,CurriculumLimits limits,Map<String,Integer> recent){int maximum=limits.givenMaximum(500);return IndexedQuestionSupply.choose(count(id,maximum),i->at(id,maximum,i),random,limits,recent);}
 public static void attach(Question q){int[] v=read(q);if(v!=null)q.studyGuide=LargeMentalDecomposition.guide(v[0],v[1],ADD.equals(q.skillId),"three-digit-step-v1");}
 static Checker.Result check(Question q,List<String> answers){
  int[] v=read(q);if(v==null||answers==null||answers.size()!=1||answers.get(0)==null||!answers.get(0).trim().matches("[0-9]{1,3}"))return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"수 입력 필요");
  return Integer.parseInt(answers.get(0).trim())==v[2]?new Checker.Result(Checker.Status.CORRECT,-1,"계산 확인 완료"):new Checker.Result(Checker.Status.WRONG_ANSWER,0,"이 계산 확인");
 }
}
