package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
/** Repeated addition and place-value products from visible whole-number factors. */
public final class PrimaryProductSteps {
 private PrimaryProductSteps(){}
 public static final String REPEAT="repeatedTwoDigitProduct",SPLIT="splitTwoDigitProduct";
 public static final List<Catalog.Skill> SKILLS=List.of(
  new Catalog.Skill(REPEAT,"같은 수를 더해 곱하기",2,1,1,"","primaryProductSteps",500,"mulIntro","첫 수를 묶음 수만큼 더해 곱을 구한다."),
  new Catalog.Skill(SPLIT,"십과 일로 나눠 곱하기",3,1,1,"","primaryProductSteps",500,"tables","첫 수를 십과 일의 자리값으로 나누어 각각 곱한 뒤 더한다."));
 public static boolean supports(String id){return REPEAT.equals(id)||SPLIT.equals(id);}
 static List<int[]> domain(String id,int maximum){if(!supports(id))throw new IllegalArgumentException("product skill");List<int[]> out=new ArrayList<>();for(int a=10;a<=(REPEAT.equals(id)?Math.min(50,maximum):99);a++)for(int b=1;b<=10;b++)if(REPEAT.equals(id)||Set.of(2,3,4,5,10).contains(b)&&a*b>=100&&a*b<=500)out.add(new int[]{a,b});return out;}
 static Question make(String id,int a,int b){if(!supports(id)||a<10||a>(REPEAT.equals(id)?50:99)||b<1||b>10||SPLIT.equals(id)&&(!Set.of(2,3,4,5,10).contains(b)||a*b<100||a*b>500))throw new IllegalArgumentException("product domain");Question q=new Question(id,a+" × "+b,"",Integer.toString(a*b));q.kind="primaryProductSteps";q.labels=new String[]{"곱"};q.stepSupport=false;attach(q);return q.withInputs(a,b);}
 public static int[] read(Question q){if(q==null||!supports(q.skillId)||q.prompt==null)return null;Matcher m=Pattern.compile("([0-9]{2}) × ([0-9]{1,2})").matcher(q.prompt);if(!m.matches())return null;int a=Integer.parseInt(m.group(1)),b=Integer.parseInt(m.group(2));if(a<10||a>(REPEAT.equals(q.skillId)?50:99)||b<1||b>10||SPLIT.equals(q.skillId)&&(!Set.of(2,3,4,5,10).contains(b)||a*b<100||a*b>500))return null;return new int[]{a,b,a*b};}
 static Question next(String id,Random random,CurriculumLimits limits,Map<String,Integer> recent){List<int[]> values=domain(id,limits.wholeMaximum(50));return IndexedQuestionSupply.choose(values.size(),i->{int[] v=values.get(i);return make(id,v[0],v[1]);},random,limits,recent);}
 public static void attach(Question q){int[] v=read(q);if(v==null)return;int a=v[0],b=v[1];StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="primary-product-steps-v1";
  if(REPEAT.equals(q.skillId)){g.step("한 묶음의 수를 쓰세요.","한 묶음의 수 = ","",""+a).step("묶음 수를 쓰세요.","묶음 수 = ","",""+b);for(int group=1;group<=b;group++)g.step("같은 수를 한 번 더하세요.","앞의 합 + 한 묶음의 수 = ","",""+(a*group));}
  else{int tens=a/10*10,ones=a%10;g.step("첫 수의 십 자리값을 쓰세요.","십 자리값 = ","",""+tens).step("첫 수의 일 자리값을 쓰세요.","일 자리값 = ","",""+ones).step("십 자리값에 둘째 수를 곱하세요.","십 자리값 × 둘째 수 = ","",""+(tens*b)).step("일 자리값에 둘째 수를 곱하세요.","일 자리값 × 둘째 수 = ","",""+(ones*b)).step("두 자리의 곱을 더하세요.","십 자리 곱 + 일 자리 곱 = ","",""+v[2]);}q.studyGuide=g;
 }
 static Checker.Result check(Question q,List<String> answers){int[] v=read(q);if(v==null||answers==null||answers.size()!=1||answers.get(0)==null||!answers.get(0).trim().matches("[0-9]{1,3}"))return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"수 입력 필요");return Integer.parseInt(answers.get(0).trim())==v[2]?new Checker.Result(Checker.Status.CORRECT,-1,"계산 확인 완료"):new Checker.Result(Checker.Status.WRONG_ANSWER,0,"이 계산 확인");}
}
