package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
/** Known multiplication and division facts connected in both directions. */
public final class InverseProductFacts {
 private InverseProductFacts(){}
 public static final String ID="inverseProductFact";
 public static final Catalog.Skill SKILL=new Catalog.Skill(ID,"곱셈과 나눗셈 연결",3,1,1,"",ID,100,"tables,divide","전체와 한 묶음의 수, 묶음 수를 연결해 반대 연산을 구한다.");
 static final List<int[]> DOMAIN=new ArrayList<>();static{for(int a:List.of(2,3,5,10))for(int b=1;b<=10;b++)if(a*b>=10)for(int mode=0;mode<2;mode++)DOMAIN.add(new int[]{a,b,mode});}
 public static boolean supports(String id){return ID.equals(id);}
 static Question make(int a,int b,int mode){if(!List.of(2,3,5,10).contains(a)||b<1||b>10||a*b<10||mode<0||mode>1)throw new IllegalArgumentException("inverse product domain");int total=a*b;String known=mode==0?a+" × "+b+" = "+total:total+" ÷ "+a+" = "+b,target=mode==0?total+" ÷ "+a:a+" × "+b;Question q=new Question(ID,"아는 계산: "+known+"\n"+target+" = □","",""+(mode==0?b:total));q.kind=ID;q.labels=new String[]{"연결한 계산의 답"};q.stepSupport=false;attach(q);return q.withInputs(a,b,total);}
 public static int[] read(Question q){if(q==null||!supports(q.skillId)||q.prompt==null)return null;Matcher m=Pattern.compile("아는 계산: ([0-9]{1,3}) ([×÷]) ([0-9]{1,2}) = ([0-9]{1,3})\n([0-9]{1,3}) ([×÷]) ([0-9]{1,2}) = □").matcher(q.prompt);if(!m.matches())return null;int x=Integer.parseInt(m.group(1)),y=Integer.parseInt(m.group(3)),z=Integer.parseInt(m.group(4)),X=Integer.parseInt(m.group(5)),Y=Integer.parseInt(m.group(7));boolean multiply=m.group(2).equals("×");int a=multiply?x:y,b=multiply?y:z,total=multiply?z:x;try{makeDomain(a,b);}catch(IllegalArgumentException e){return null;}if(total!=a*b||X!=(multiply?total:a)||Y!=(multiply?a:b)||m.group(6).equals(multiply?"÷":"×")==false)return null;return new int[]{a,b,total,multiply?0:1,multiply?b:total};}
 private static void makeDomain(int a,int b){if(!List.of(2,3,5,10).contains(a)||b<1||b>10||a*b<10)throw new IllegalArgumentException("inverse fact");}
 static Question next(Random random,CurriculumLimits limits,Map<String,Integer> recent){return IndexedQuestionSupply.choose(DOMAIN.size(),i->{int[] v=DOMAIN.get(i);return make(v[0],v[1],v[2]);},random,limits,recent);}
 public static void attach(Question q){int[] v=read(q);if(v==null)return;StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="inverse-product-fact-v1";g.step("아는 계산에서 전체를 쓰세요.","전체 = ","",""+v[2]).step("아는 계산에서 한 묶음의 수를 쓰세요.","한 묶음의 수 = ","",""+v[0]).step("아는 계산에서 묶음 수를 쓰세요.","묶음 수 = ","",""+v[1]);g.step(v[3]==0?"전체를 한 묶음의 수로 나누어 묶음 수를 구하세요.":"한 묶음의 수에 묶음 수를 곱해 전체를 구하세요.",v[3]==0?"전체 ÷ 한 묶음의 수 = ":"한 묶음의 수 × 묶음 수 = ","",""+v[4]);q.studyGuide=g;}
 static Checker.Result check(Question q,List<String> answers){int[] v=read(q);if(v==null||answers==null||answers.size()!=1||answers.get(0)==null||!answers.get(0).trim().matches("[0-9]{1,3}"))return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"수 입력 필요");return Integer.parseInt(answers.get(0).trim())==v[4]?new Checker.Result(Checker.Status.CORRECT,-1,"계산 확인 완료"):new Checker.Result(Checker.Status.WRONG_ANSWER,0,"이 계산 확인");}
}
