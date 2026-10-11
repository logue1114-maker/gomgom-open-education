package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
/** Whole-number multiples of ten, using public operands and a common unit. */
public final class TenMultipleMental {
 private TenMultipleMental(){}
 public static final String MUL="tenMultipleProduct",DIV="tenMultipleQuotient";
 public static final List<Catalog.Skill> SKILLS=List.of(
  new Catalog.Skill(MUL,"10의 배수끼리 곱하기",6,1,1,"","tenMultipleMental",1000000,"tables","각 수에서 10의 단위를 분리해 곱한다."),
  new Catalog.Skill(DIV,"10의 배수끼리 나누기",6,1,1,"","tenMultipleMental",1000000,"divide","같은 10의 단위로 두 수를 나누어 계산한다."));
 public static boolean supports(String id){return MUL.equals(id)||DIV.equals(id);}
 private static final int[] PRODUCTS=new int[10000],QUOTIENTS=new int[100000];
 static {int n=0;for(int i=0;i<PRODUCTS.length;i++){n+=1000000/((i+1)*10)/10;PRODUCTS[i]=n;}n=0;for(int i=0;i<QUOTIENTS.length;i++){n+=1000000/((i+1)*10);QUOTIENTS[i]=n;}}
 private static int[] blocks(String id){if(MUL.equals(id))return PRODUCTS;if(DIV.equals(id))return QUOTIENTS;throw new IllegalArgumentException("ten-multiple skill");}
 static int count(String id){int[] b=blocks(id);return b[b.length-1];}
 static Question at(String id,int index){int[] blocks=blocks(id);if(index<0||index>=count(id))throw new IllegalArgumentException("ten-multiple index");int block=Arrays.binarySearch(blocks,index+1);if(block<0)block=-block-1;int local=index-(block==0?0:blocks[block-1]),given=(block+1)*10;return MUL.equals(id)?make(id,given,(local+1)*10):make(id,given*(local+1),given);}
 static Question make(String id,int a,int b){if(a<10||b<10)throw new IllegalArgumentException("positive multiples required");Question q=new Question(id,a+(MUL.equals(id)?" × ":" ÷ ")+b,"",""+(MUL.equals(id)?(long)a*b:a/b));if(read(q)==null)throw new IllegalArgumentException("ten-multiple operands");q.stepSupport=false;attach(q);return q;}
 private static int unit(int n){int u=1;while(n%10==0){n/=10;u*=10;}return u;}
 public static int[] read(Question q){if(q==null||!supports(q.skillId)||q.prompt==null)return null;Matcher m=Pattern.compile("^([0-9]{2,7}) ([×÷]) ([0-9]{2,7})$").matcher(q.prompt);if(!m.matches()||MUL.equals(q.skillId)!=m.group(2).equals("×"))return null;int a=Integer.parseInt(m.group(1)),b=Integer.parseInt(m.group(3));if(a<10||b<10||a>1000000||b>1000000||a%10!=0||b%10!=0)return null;
  int ua=unit(a),ub=unit(b);long result=MUL.equals(q.skillId)?(long)a*b:a/b;if(result>1000000||DIV.equals(q.skillId)&&(a%b!=0||result<1))return null;
  return new int[]{a,b,(int)result,ua,ub,a/ua,b/ub};}
 static Question next(String id,Random r,CurriculumLimits limits,Map<String,Integer> recent){return IndexedQuestionSupply.choose(count(id),i->at(id,i),r,limits,recent);}
 static Checker.Result check(Question q,List<String> answers){int[] v=read(q);if(v==null||answers==null||answers.size()!=1||answers.get(0)==null||!answers.get(0).trim().matches("[0-9]{1,7}"))return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"답 입력 필요");return new Checker.Result(Integer.parseInt(answers.get(0).trim())==v[2]?Checker.Status.CORRECT:Checker.Status.WRONG_ANSWER,Integer.parseInt(answers.get(0).trim())==v[2]?-1:0,Integer.parseInt(answers.get(0).trim())==v[2]?"정답":"이 답 확인");}
 private static void step(StudyGuide g,String text,String relation,int value){g.step(text,relation+" = ","",""+value);}
 public static void attach(Question q){int[] v=read(q);if(v==null)return;StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="ten-multiple-mental-v1";
  if(MUL.equals(q.skillId)){step(g,"첫 수를 나누는 가장 큰 10의 거듭제곱을 쓰세요.","첫 단위",v[3]);step(g,"첫 수를 첫 단위로 나누세요.","첫 수 ÷ 첫 단위",v[5]);step(g,"둘째 수를 나누는 가장 큰 10의 거듭제곱을 쓰세요.","둘째 단위",v[4]);step(g,"둘째 수를 둘째 단위로 나누세요.","둘째 수 ÷ 둘째 단위",v[6]);step(g,"단위를 나눈 두 수를 곱하세요.","첫 몫 × 둘째 몫",v[5]*v[6]);step(g,"두 단위를 곱하세요.","첫 단위 × 둘째 단위",v[3]*v[4]);step(g,"두 곱을 곱해 전체 곱을 구하세요.","두 몫의 곱 × 두 단위의 곱",v[2]);}
  else{int common=Math.min(v[3],v[4]);step(g,"두 수를 함께 나눌 수 있는 가장 큰 10의 거듭제곱을 쓰세요.","공통 단위",common);step(g,"나누어지는 수를 공통 단위로 나누세요.","나누어지는 수 ÷ 공통 단위",v[0]/common);step(g,"나누는 수를 공통 단위로 나누세요.","나누는 수 ÷ 공통 단위",v[1]/common);step(g,"두 수를 같은 단위로 나누어도 몫은 같습니다. 앞의 몫을 뒤의 몫으로 나누세요.","앞의 몫 ÷ 뒤의 몫",v[2]);}q.studyGuide=g;}
}
