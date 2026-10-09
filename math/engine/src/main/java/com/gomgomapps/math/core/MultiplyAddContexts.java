package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
/** Public equal-group-plus-extra and complete two-set correspondence problems. */
public final class MultiplyAddContexts {
 private MultiplyAddContexts(){}
 public static final String BOXES="multiplyAddBoxStories",LINKS="allConnectionStories";
 public static final List<Catalog.Skill> SKILLS=List.of(new Catalog.Skill(BOXES,"상자와 낱개 합하기",4,1,1,"",BOXES,900,"mul2,add1000","상자당 수를 십과 일로 나누어 곱한 뒤 상자 밖의 낱개 수를 더한다."),new Catalog.Skill(LINKS,"두 집단의 모든 연결",4,1,1,"",LINKS,400,"tables","한쪽의 각 점을 다른 쪽의 모든 점과 연결한 선의 수를 구한다."));
 private static final Pattern BOX_PROMPT=Pattern.compile("^상자마다 연필이 ([0-9]{2})자루 있습니다\\.\\n상자: ([0-9])개\\n상자 밖 연필: ([0-9])자루\\n연필은 모두 몇 자루인가요\\?$"),LINK_PROMPT=Pattern.compile("^한쪽 점: ([0-9]{1,2})개\\n다른 쪽 점: ([0-9]{1,2})개\\n한쪽의 각 점을 다른 쪽의 모든 점과 한 번씩 연결합니다\\.\\n연결선은 모두 몇 개인가요\\?$");
 public static boolean supports(String id){return BOXES.equals(id)||LINKS.equals(id);}
 static int count(String id){return BOXES.equals(id)?9000:LINKS.equals(id)?400:0;}
 static Question make(String id,int a,int b,int extra){if(!supports(id)||BOXES.equals(id)&&(a<10||a>99||b<0||b>9||extra<0||extra>9)||LINKS.equals(id)&&(a<1||a>20||b<1||b>20||extra!=0))throw new IllegalArgumentException("multiply-add context domain");String prompt=BOXES.equals(id)?"상자마다 연필이 "+a+"자루 있습니다.\n상자: "+b+"개\n상자 밖 연필: "+extra+"자루\n연필은 모두 몇 자루인가요?":"한쪽 점: "+a+"개\n다른 쪽 점: "+b+"개\n한쪽의 각 점을 다른 쪽의 모든 점과 한 번씩 연결합니다.\n연결선은 모두 몇 개인가요?";Question q=new Question(id,prompt,"",""+(a*b+extra));q.labels=new String[]{BOXES.equals(id)?"전체 연필 수":"연결선 수"};q.stepSupport=false;return q;}
 static Question indexed(String id,int i){if(i<0||i>=count(id))throw new IllegalArgumentException("context index");return BOXES.equals(id)?make(id,10+i/100,i/10%10,i%10):make(id,1+i/20,1+i%20,0);}
 public static int[] read(Question q){if(q==null||!supports(q.skillId)||q.prompt==null)return null;Matcher m=(BOXES.equals(q.skillId)?BOX_PROMPT:LINK_PROMPT).matcher(q.prompt);if(!m.matches())return null;int a=Integer.parseInt(m.group(1)),b=Integer.parseInt(m.group(2)),extra=BOXES.equals(q.skillId)?Integer.parseInt(m.group(3)):0;try{make(q.skillId,a,b,extra);}catch(IllegalArgumentException e){return null;}return new int[]{a,b,extra,a*b,a*b+extra};}
 static Question next(String id,Random random,CurriculumLimits limits,Map<String,Integer> recent){Question q=IndexedQuestionSupply.choose(count(id),i->indexed(id,i),random,limits,recent);attach(q);return q;}
 static Checker.Result check(Question q,List<String> answers){int[] v=read(q);if(v==null||answers==null||answers.size()!=1||answers.get(0)==null||!answers.get(0).trim().matches("[0-9]{1,3}"))return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"수 입력 필요");return Integer.parseInt(answers.get(0).trim())==v[4]?new Checker.Result(Checker.Status.CORRECT,-1,"정답"):new Checker.Result(Checker.Status.WRONG_ANSWER,0,"이 수 확인");}
 public static void attach(Question q){int[] v=read(q);if(v==null)return;StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="multiply-add-context-v1";
  if(BOXES.equals(q.skillId)){int tens=v[0]/10*10,ones=v[0]%10;g.step("상자당 수를 십의 자릿값과 일의 자리로 나눕니다. 십의 자릿값을 쓰세요.","상자당 십의 자릿값 = ","",""+tens);g.step("상자당 수의 일의 자리 숫자를 쓰세요.","상자당 일의 자리 숫자 = ","",""+ones);g.step("십의 자릿값에 상자 수를 곱하세요.","상자당 십의 자릿값 × 상자 수 = ","",""+(tens*v[1]));g.step("일의 자리 숫자에 상자 수를 곱하세요.","상자당 일의 자리 숫자 × 상자 수 = ","",""+(ones*v[1]));g.step("나누어 구한 두 곱을 더해 상자 안의 수를 구하세요.","십의 자리 곱 + 일의 자리 곱 = ","",""+v[3]);g.step("상자 밖의 낱개 수도 더하세요.","상자 안의 수 + 상자 밖의 수 = ","",""+v[4]);}
  else{g.step("한쪽 점 하나가 연결되는 다른 쪽 점의 수를 쓰세요.","점 하나의 연결 수 = ","",""+v[1]);g.step("한쪽 점의 수를 쓰세요.","한쪽 점의 수 = ","",""+v[0]);g.step("각 점의 연결 수를 한쪽 점의 수만큼 곱하세요.","점 하나의 연결 수 × 한쪽 점의 수 = ","",""+v[4]);}q.studyGuide=g;
 }
}
