package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
/** Whole-number mental splitting/bridging; all relations come from public operands. */
public final class MentalPlaceCalculations {
 private MentalPlaceCalculations(){}
 public static final String ADD="mentalPlaceAddition",SUB="mentalPlaceSubtraction",VERSION="mental-place-calculations-v1";
 public static final List<Catalog.Skill> SKILLS=List.of(new Catalog.Skill(ADD,"자리 단위로 더하기",2,1,1,"","mentalPlace",99,"place10,add20","첫 수를 묶음과 나머지로 나누고 자리 경계에서 둘째 수를 나눠 더한다."),new Catalog.Skill(SUB,"자리 단위로 빼기",2,1,1,"","mentalPlace",99,"place10,sub20","첫 수를 묶음과 나머지로 나누고 자리 경계에서 둘째 수를 나눠 뺀다."));
 public static boolean supports(String id){return ADD.equals(id)||SUB.equals(id);}
 static Question make(String id,int a,int b){boolean add=ADD.equals(id);if(!supports(id)||a<10||a>999||unit(b)==0||(!add&&a<b))throw new IllegalArgumentException("mental place domain");Question q=new Question(id,a+(add?" + ":" - ")+b,"",""+(add?a+b:a-b));q.kind="mentalPlace";q.labels=new String[]{"답"};q.stepSupport=false;attach(q);return q;}
 static int unit(int b){if(b<=0||b>900)return 0;int unit=1;while(b%10==0){b/=10;unit*=10;}return b<=9?unit:0;}
 static Question create(Catalog.Skill s,Random r,CurriculumLimits limits){int digits=limits.wholeDigits(2);if(digits<2||digits>3)throw new IllegalArgumentException("mental place digits");int min=digits==2?10:100,max=digits==2?99:999,a=min+r.nextInt(max-min+1),unit=(int)Math.pow(10,r.nextInt(digits)),maxDigit=SUB.equals(s.id)?Math.min(9,a/unit):9,b=(1+r.nextInt(maxDigit))*unit;return make(s.id,a,b);}
 static Question next(Catalog.Skill s,Random r,CurriculumLimits limits,Map<String,Integer> recent){Map<String,Question> pool=new LinkedHashMap<>();for(int i=0;i<64;i++){Question q=create(s,r,limits);if(limits.allows(q))pool.put(q.signature(),q);}return FactFoundations.choose(pool,r,recent);}
 // original first/second, result, grouping unit, groups, grouped value, remainder, cross boundary?
 public static int[] read(Question q){if(q==null||!supports(q.skillId)||q.prompt==null)return null;Matcher m=Pattern.compile("(\\d{2,3}) ([+−-]) (\\d{1,3})").matcher(q.prompt);if(!m.matches()||ADD.equals(q.skillId)!=m.group(2).equals("+"))return null;int a=Integer.parseInt(m.group(1)),b=Integer.parseInt(m.group(3)),u=unit(b);if(u==0||a<10||SUB.equals(q.skillId)&&a<b)return null;int block=u==1?10:u,groups=a/block,high=groups*block,rest=a-high;boolean add=ADD.equals(q.skillId),cross=u==1&&(add?rest+b>=block:rest<b);return new int[]{a,b,add?a+b:a-b,block,groups,high,rest,cross?1:0,u};}
 public static void attach(Question q){int[] v=read(q);if(v==null)return;boolean add=ADD.equals(q.skillId);StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion=VERSION;
  g.step(v[8]==1?"일의 자리를 나눌 묶음의 단위를 쓰세요.":"둘째 수의 자리 단위를 쓰세요.","나눌 단위 = ","",""+v[3]);g.step("첫 수에 묶음이 몇 개 들어가는지 쓰세요.","묶음 수 = ","",""+v[4]);g.step("묶음 수와 단위를 곱하세요.","묶음 수 × 나눌 단위 = ","",""+v[5]);g.step("첫 수에서 묶음값을 빼세요.","첫 수 − 묶음값 = ","",""+v[6]);
  if(v[8]>1){int secondGroups=v[1]/v[3],changed=add?v[4]+secondGroups:v[4]-secondGroups;g.step("둘째 수에 같은 묶음이 몇 개 들어가는지 쓰세요.","둘째 묶음 수 = ","",""+secondGroups);g.step(add?"두 묶음 수를 더하세요.":"첫 묶음 수에서 둘째 묶음 수를 빼세요.",add?"묶음 수 + 둘째 묶음 수 = ":"묶음 수 − 둘째 묶음 수 = ","",""+changed);g.step("바뀐 묶음 수와 단위를 곱하세요.","바뀐 묶음 수 × 나눌 단위 = ","",""+(changed*v[3]));g.step("바뀐 묶음값에 나머지를 더하세요.","바뀐 묶음값 + 나머지 = ","",""+v[2]);}
  else if(v[7]==0){g.step(add?"나머지에 둘째 수를 더하세요.":"나머지에서 둘째 수를 빼세요.",add?"나머지 + 둘째 수 = ":"나머지 − 둘째 수 = ","",""+(add?v[6]+v[1]:v[6]-v[1]));g.step("묶음값과 계산한 나머지를 더하세요.","묶음값 + 계산한 나머지 = ","",""+v[2]);}
  else if(add){int moved=v[3]-v[6],boundary=v[0]+moved;g.step("다음 묶음까지 얼마를 옮길지 구하세요.","나눌 단위 − 나머지 = ","",""+moved);g.step("첫 수에 옮길 수를 더하세요.","첫 수 + 옮길 수 = ","",""+boundary);g.step("둘째 수에서 옮긴 수를 빼세요.","둘째 수 − 옮길 수 = ","",""+(v[1]-moved));g.step("경계값에 남은 수를 더하세요.","경계값 + 남은 수 = ","",""+v[2]);}
  else{g.step("둘째 수에서 나머지를 먼저 빼세요.","둘째 수 − 나머지 = ","",""+(v[1]-v[6]));g.step("첫 수에서 나머지를 빼 경계까지 가세요.","첫 수 − 나머지 = ","",""+v[5]);g.step("경계값에서 남은 수를 빼세요.","경계값 − 남은 수 = ","",""+v[2]);}q.studyGuide=g;
 }
 static Checker.Result check(Question q,List<String> answers){int[] v=read(q);if(v==null||answers.size()!=1||!answers.get(0).trim().matches("[0-9]{1,4}"))return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"답 입력 필요");return Integer.parseInt(answers.get(0).trim())==v[2]?new Checker.Result(Checker.Status.CORRECT,-1,"정답"):new Checker.Result(Checker.Status.WRONG_ANSWER,0,"이 답 확인");}
}
