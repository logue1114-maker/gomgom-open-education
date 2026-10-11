package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
/** Two left-to-right additions/subtractions; every intermediate stays in the taught range. */
public final class AddSubChains {
 private AddSubChains(){}
 public static final String ID="addSubChain";
 public static final List<Catalog.Skill> SKILLS=List.of(new Catalog.Skill(ID,"덧셈·뺄셈 이어 계산하기",1,1,1,"",ID,500,"add9,sub9","앞의 계산 결과에 다음 수를 더하거나 뺀다."));
 public static boolean supports(String id){return ID.equals(id);}
 static int maximum(CurriculumLimits limits){return Math.min(500,limits.wholeMaximum(10));}
 static int count(int max){if(max<1||max>500)throw new IllegalArgumentException("chain range");return (max+1)*(max+1)*(max+1);}
 static Question at(int max,int index){if(index<0||index>=count(max))throw new IllegalArgumentException("chain index");int w=max+1,finish=index%w,middle=index/w%w,start=index/w/w;return make(start,Math.abs(middle-start),middle>=start,Math.abs(finish-middle),finish>=middle);}
 static Question make(int a,int b,boolean plus,int c,boolean plus2){int middle=a+(plus?b:-b),answer=middle+(plus2?c:-c);if(a<0||a>500||b<0||b>500||c<0||c>500||middle<0||middle>500||answer<0||answer>500)throw new IllegalArgumentException("chain domain");Question q=new Question(ID,a+(plus?" + ":" − ")+b+(plus2?" + ":" − ")+c+" = □","",""+answer);q.labels=new String[]{"계산 결과"};q.stepSupport=false;attach(q);return q.withInputs(a,b,c);}
 public static int[] read(Question q){if(q==null||!supports(q.skillId)||q.prompt==null)return null;Matcher m=Pattern.compile("(\\d{1,3}) ([+−]) (\\d{1,3}) ([+−]) (\\d{1,3}) = □").matcher(q.prompt);if(!m.matches())return null;int a=Integer.parseInt(m.group(1)),b=Integer.parseInt(m.group(3)),c=Integer.parseInt(m.group(5)),s=m.group(2).equals("+")?1:-1,t=m.group(4).equals("+")?1:-1,middle=a+s*b,answer=middle+t*c;return a>500||b>500||c>500||middle<0||middle>500||answer<0||answer>500?null:new int[]{a,b,c,s,t,middle,answer};}
 static Question next(Random random,CurriculumLimits limits,Map<String,Integer> recent){int max=maximum(limits);return IndexedQuestionSupply.choose(count(max),i->at(max,i),random,limits,recent);}
 static Checker.Result check(Question q,List<String> answers){int[] v=read(q);if(v==null||answers==null||answers.size()!=1||answers.get(0)==null||!answers.get(0).trim().matches("\\d{1,3}"))return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"수 입력 필요");boolean correct=Integer.parseInt(answers.get(0).trim())==v[6];return new Checker.Result(correct?Checker.Status.CORRECT:Checker.Status.WRONG_ANSWER,correct?-1:0,correct?"계산 확인 완료":"이 계산 확인");}
 public static void attach(Question q){int[] v=read(q);if(v==null)return;StudyGuide h=new StudyGuide().transfer(false);h.teachingVersion="add-sub-chain-v1";h.step("앞의 두 수를 먼저 계산하세요.",v[3]>0?"첫 수 + 둘째 수 = ":"첫 수 − 둘째 수 = ","",""+v[5]);h.step("앞의 계산 결과에 마지막 수를 더하거나 빼세요.",v[4]>0?"앞의 계산 결과 + 마지막 수 = ":"앞의 계산 결과 − 마지막 수 = ","",""+v[6]);q.studyGuide=h;}
}
