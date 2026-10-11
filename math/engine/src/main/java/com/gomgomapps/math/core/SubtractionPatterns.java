package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
public final class SubtractionPatterns {
 private SubtractionPatterns(){}public static final String ID="subtractionPatterns";
 public static final Catalog.Skill SKILL=new Catalog.Skill(ID,"뺄셈 계산의 변화",3,1,2,"",ID,500,"sub100","같은 수를 빼며 앞 수의 변화와 답의 변화를 비교한다.");
 public static boolean supports(String id){return ID.equals(id);}
 static Question make(int a,int b,int step){if(a<100||a>500||b<1||b>99||!Set.of(10,100,-10,-100).contains(step)||a+2*step<100||a+2*step>500)throw new IllegalArgumentException("pattern domain");
  Question q=new Question(ID,"계산의 변화를 살펴보고 빈칸을 채우세요.\n"+a+" − "+b+" = "+(a-b)+"\n"+(a+step)+" − "+b+" = □\n"+(a+2*step)+" − "+b+" = □","",""+(a+step-b),""+(a+2*step-b));q.kind=ID;q.stepSupport=false;q.labels=new String[]{"둘째 계산의 답","셋째 계산의 답"};
  q.studyGuide=new StudyGuide().transfer(false).step("빼는 수는 같습니다. 앞 수가 얼마나 변했나요?","앞 수의 변화 = ","",""+step).step("첫 답에 같은 변화를 적용하세요.",(a-b)+(step>0?" + ":" − ")+Math.abs(step)+" = ","",""+(a+step-b)).step("다음 답에도 같은 변화를 적용하세요.",(a+step-b)+(step>0?" + ":" − ")+Math.abs(step)+" = ","",""+(a+2*step-b));return q;
 }
 public static int[] read(Question q){if(q==null||!supports(q.skillId)||q.prompt==null)return null;Matcher m=Pattern.compile("계산의 변화를 살펴보고 빈칸을 채우세요.\\n(\\d{3}) − (\\d{1,2}) = (\\d{1,3})\\n(\\d{3}) − (\\d{1,2}) = □\\n(\\d{3}) − (\\d{1,2}) = □").matcher(q.prompt);if(!m.matches())return null;int a=Integer.parseInt(m.group(1)),b=Integer.parseInt(m.group(2)),c=Integer.parseInt(m.group(3)),next=Integer.parseInt(m.group(4)),last=Integer.parseInt(m.group(6)),step=next-a;
  if(a<100||a>500||b<1||b>99||c!=a-b||!Set.of(10,100,-10,-100).contains(step)||last!=next+step||last<100||last>500||Integer.parseInt(m.group(5))!=b||Integer.parseInt(m.group(7))!=b)return null;return new int[]{a,b,step};}
 static Question next(Random r,CurriculumLimits limits,Map<String,Integer> recent){return IndexedQuestionSupply.choose(1188,i->{int b=1+i%99,group=i/99,step=(group%2==0?10:100)*(group<6?1:-1),a=group<6?100+group/2*100:300+(group-6)/2*100;return make(a,b,step);},r,limits,recent);}
 static Checker.Result check(Question q,List<String> answers){int[] v=read(q);if(v==null||answers==null||answers.size()!=2)return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"두 계산의 답을 입력하세요.");for(int i=0;i<2;i++){String raw=answers.get(i);if(raw==null||!raw.matches("[0-9]{1,3}"))return new Checker.Result(Checker.Status.INPUT_NEEDED,i,"계산의 답을 입력하세요.");if(Integer.parseInt(raw)!=v[0]+(i+1)*v[2]-v[1])return new Checker.Result(Checker.Status.WRONG_ANSWER,i,"앞 수와 답의 변화를 비교하세요.");}return new Checker.Result(Checker.Status.CORRECT,-1,"계산의 변화를 찾았습니다.");}
}
