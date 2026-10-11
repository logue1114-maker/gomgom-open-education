package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
/** The same whole and two parts connect one addition and two subtractions. */
public final class AddSubFamilies {
 private AddSubFamilies(){}
 public static final String ID="addSubFamily";
 public static final List<Catalog.Skill> SKILLS=List.of(new Catalog.Skill(ID,"덧셈·뺄셈 관계",1,1,1,"",ID,99,"add20,sub20","같은 세 수로 덧셈과 뺄셈의 관계를 알아본다."));
 public static boolean supports(String id){return ID.equals(id);}
 static int count(int maximum){if(maximum<1||maximum>99)throw new IllegalArgumentException("family range");return (maximum+1)*(maximum+2);}
 static Question at(int maximum,int index){
  if(index<0||index>=count(maximum))throw new IllegalArgumentException("family index");
  boolean givenAddition=index%2==0;int pair=index/2,total=0;
  while(pair>total){pair-=total+1;total++;}
  return make(pair,total-pair,givenAddition);
 }
 static Question make(int a,int b,boolean givenAddition){
  int total=a+b;if(a<0||b<0||total>99)throw new IllegalArgumentException("family domain");
  String known=givenAddition?a+" + "+b+" = "+total:total+" − "+a+" = "+b;
  String first=givenAddition?total+" − "+a:b+" + "+a;
  Question q=new Question(ID,"아는 계산: "+known+"\n"+first+" = □\n"+total+" − "+b+" = □","",Integer.toString(givenAddition?b:total),Integer.toString(a));
  q.kind=ID;q.labels=new String[]{"첫 관계식의 답","둘째 관계식의 답"};q.stepSupport=false;attach(q);return q.withInputs(a,b,total);
 }
 // first part, second part, whole, known addition flag; no answer-key dependency.
 public static int[] read(Question q){
  if(q==null||!supports(q.skillId)||q.prompt==null)return null;
  Matcher m=Pattern.compile("아는 계산: (\\d{1,2}) ([+−]) (\\d{1,2}) = (\\d{1,2})\\n(\\d{1,2}) ([+−]) (\\d{1,2}) = □\\n(\\d{1,2}) − (\\d{1,2}) = □").matcher(q.prompt);
  if(!m.matches())return null;
  int x=Integer.parseInt(m.group(1)),y=Integer.parseInt(m.group(3)),z=Integer.parseInt(m.group(4));boolean add=m.group(2).equals("+");
  int a=y,b=add?y:z,total=add?z:x;if(add)a=x;
  if(a+b!=total||total>99)return null;
  if(Integer.parseInt(m.group(5))!=(add?total:b)||!m.group(6).equals(add?"−":"+")||Integer.parseInt(m.group(7))!=a||Integer.parseInt(m.group(8))!=total||Integer.parseInt(m.group(9))!=b)return null;
  return new int[]{a,b,total,add?1:0};
 }
 static Question next(Random random,CurriculumLimits limits,Map<String,Integer> recent){int maximum=Math.min(99,limits.wholeMaximum(20));return IndexedQuestionSupply.choose(count(maximum),i->at(maximum,i),random,limits,recent);}
 static Checker.Result check(Question q,List<String> answers){
  int[] v=read(q);if(v==null||answers==null||answers.size()!=2)return new Checker.Result(Checker.Status.INPUT_NEEDED,-1,"답 입력 필요");
  int[] expected={v[3]==1?v[1]:v[2],v[0]};
  for(int i=0;i<2;i++){String value=answers.get(i);if(value==null||!value.trim().matches("[0-9]{1,2}"))return new Checker.Result(Checker.Status.INPUT_NEEDED,i,"수 입력 필요");if(Integer.parseInt(value.trim())!=expected[i])return new Checker.Result(Checker.Status.WRONG_ANSWER,i,i==0?"첫 관계식 확인":"둘째 관계식 확인");}
  return new Checker.Result(Checker.Status.CORRECT,-1,"관계 확인 완료");
 }
 public static void attach(Question q){
  int[] v=read(q);if(v==null)return;boolean add=v[3]==1;
  StudyGuide h=new StudyGuide().transfer(false);h.teachingVersion="add-sub-family-v1";
  h.step("전체 수를 쓰세요.","전체 수 = ","",""+v[2]);
  h.step("첫 부분의 수를 쓰세요.","첫 부분 = ","",""+v[0]);
  h.step("둘째 부분의 수를 쓰세요.","둘째 부분 = ","",""+v[1]);
  h.step(add?"전체에서 첫 부분을 빼면 둘째 부분이 남습니다. 계산하세요.":"두 부분을 더하면 전체가 됩니다. 계산하세요.",add?"전체 수 − 첫 부분 = ":"둘째 부분 + 첫 부분 = ","",""+(add?v[1]:v[2]));
  h.step("전체에서 둘째 부분을 빼면 첫 부분이 남습니다. 계산하세요.","전체 수 − 둘째 부분 = ","",""+v[0]);q.studyGuide=h;
 }
}
