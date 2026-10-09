package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
/** Two changes; public events own the operators, intermediate count and result. */
public final class TwoStepChangeStories {
 private TwoStepChangeStories(){}
 public static final String ID="twoStepChangeStories";
 public static final Catalog.Skill SKILL=new Catalog.Skill(ID,"두 번 변한 수 구하기",4,1,1,"",ID,9999,"add1000,sub1000","두 번의 변화에 맞는 계산을 골라 지금 수를 구한다.");
 static final int[] CHANGES={1,7,25,60,99,150,305,499};static final int COUNT=41472;
 private static final String[] OBJECTS={"스티커","색연필"};
 private static final Pattern PROMPT=Pattern.compile("^(스티커|색연필)\\n처음: ([0-9]{1,4})개\\n([0-9]{1,4})개를 (더 받았어요|나눠 줬어요)\\.\\n그다음 ([0-9]{1,4})개를 (더 받았어요|나눠 줬어요)\\.\\n지금 몇 개인가요\\?$");
 public static boolean supports(String id){return ID.equals(id);}
 static Question make(int context,int start,int first,boolean addFirst,int second,boolean addSecond){int middle=start+(addFirst?first:-first),result=middle+(addSecond?second:-second);if(context<0||context>1||start<0||start>9999||first<0||first>9999||second<0||second>9999||middle<0||middle>9999||result<0||result>9999)throw new IllegalArgumentException("two-step quantity domain");Question q=new Question(ID,OBJECTS[context]+"\n처음: "+start+"개\n"+first+"개를 "+(addFirst?"더 받았어요":"나눠 줬어요")+".\n그다음 "+second+"개를 "+(addSecond?"더 받았어요":"나눠 줬어요")+".\n지금 몇 개인가요?","",Integer.toString(result));q.labels=new String[]{"지금 수"};q.stepSupport=false;return q;}
 static Question indexed(int i){if(i<0||i>=COUNT)throw new IllegalArgumentException("two-step index");int second=CHANGES[i%8];i/=8;int first=CHANGES[i%8];i/=8;int start=1000+100*(i%81);i/=81;int operations=i%4;i/=4;return make(i,start,first,(operations&1)==0,second,(operations&2)==0);}
 public static int[] read(Question q){if(q==null||!supports(q.skillId)||q.prompt==null)return null;Matcher m=PROMPT.matcher(q.prompt);if(!m.matches())return null;int start=Integer.parseInt(m.group(2)),first=Integer.parseInt(m.group(3)),second=Integer.parseInt(m.group(5));boolean a=m.group(4).equals("더 받았어요"),b=m.group(6).equals("더 받았어요");int middle=start+(a?first:-first),result=middle+(b?second:-second);return middle<0||middle>9999||result<0||result>9999?null:new int[]{start,first,second,a?1:0,b?1:0,middle,result};}
 static Question next(Random random,CurriculumLimits limits,Map<String,Integer> recent){Question q=IndexedQuestionSupply.choose(COUNT,TwoStepChangeStories::indexed,random,limits,recent);attach(q);return q;}
 static Checker.Result check(Question q,List<String> answers){int[] v=read(q);if(v==null||answers==null||answers.size()!=1||answers.get(0)==null||!answers.get(0).trim().matches("[0-9]{1,4}"))return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"수 입력 필요");return Integer.parseInt(answers.get(0).trim())==v[6]?new Checker.Result(Checker.Status.CORRECT,-1,"정답"):new Checker.Result(Checker.Status.WRONG_ANSWER,0,"이 수 확인");}
 public static void attach(Question q){int[] v=read(q);if(v==null)return;Map<String,String> options=new LinkedHashMap<>();options.put("+","늘었으므로 더하기");options.put("-","줄었으므로 빼기");StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="two-step-change-v1";g.choice("첫 변화에 맞는 계산과 이유를 고르세요.",options,v[3]==1?"+":"-");g.step("선택한 계산으로 첫 변화 뒤의 수를 구하세요.","처음 수와 첫 변화량의 관계 = ","",Integer.toString(v[5]));g.choice("둘째 변화에 맞는 계산과 이유를 고르세요.",options,v[4]==1?"+":"-");g.step("선택한 계산으로 지금 수를 구하세요.","첫 변화 뒤의 수와 둘째 변화량의 관계 = ","",Integer.toString(v[6]));q.studyGuide=g;}
}
