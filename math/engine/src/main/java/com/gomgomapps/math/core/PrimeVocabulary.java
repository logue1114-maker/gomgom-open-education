package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
/** Prime vocabulary checked from public integers, independent of saved answer keys. */
public final class PrimeVocabulary {
 private PrimeVocabulary(){}
 public static final String CLASSIFY="primeCompositeClassify",FACTOR="primeFactorIdentify";
 public static final List<Catalog.Skill> SKILLS=List.of(
  new Catalog.Skill(CLASSIFY,"소수와 합성수 구분",5,1,1,"",CLASSIFY,144,"tables,divide","양의 약수가 두 개인 자연수는 소수, 두 개보다 많은 자연수는 합성수이다. 1은 둘 다 아니다."),
  new Catalog.Skill(FACTOR,"소인수인지 확인",5,1,1,"",FACTOR,144,"tables,divide","소인수는 주어진 수의 약수이면서 소수인 수이다."));
 private static final Pattern C=Pattern.compile("^([0-9]{1,3})은 소수, 합성수, 둘 다 아님 중 무엇인가요\\?$"),F=Pattern.compile("^([0-9]{1,3})은 ([0-9]{1,3})의 소인수인가요\\?$" );
 static final List<int[]> FACTORS=new ArrayList<>();
 static {for(int n=2;n<=144;n++)for(int d=2;d<=n;d++)if(n%d==0||d<=13&&prime(d))FACTORS.add(new int[]{n,d});}
 public static boolean supports(String id){return CLASSIFY.equals(id)||FACTOR.equals(id);}
 static int divisorCount(int n){int count=0;for(int d=1;d<=n;d++)if(n%d==0)count++;return count;}
 static boolean prime(int n){return n>1&&divisorCount(n)==2;}
 public static int[] read(Question q){if(q==null||q.prompt==null||!supports(q.skillId))return null;Matcher m=(CLASSIFY.equals(q.skillId)?C:F).matcher(q.prompt);if(!m.matches())return null;int a=Integer.parseInt(m.group(1));if(CLASSIFY.equals(q.skillId))return a>=1&&a<=144?new int[]{a}:null;int n=Integer.parseInt(m.group(2));return n>=2&&n<=144&&a>=2&&a<=n&&(n%a==0||a<=13&&prime(a))?new int[]{n,a}:null;}
 private static String solution(String id,int[] g){return CLASSIFY.equals(id)?(g[0]==1?"0":prime(g[0])?"1":"2"):(prime(g[1])&&g[0]%g[1]==0?"1":"0");}
 static Map<String,String> labels(String id){Map<String,String> m=new LinkedHashMap<>();if(CLASSIFY.equals(id)){m.put("1","소수");m.put("2","합성수");m.put("0","둘 다 아님");}else{m.put("1","소인수이다");m.put("0","소인수가 아니다");}return m;}
 static Question make(String id,int n,int candidate){Question q=new Question(id,CLASSIFY.equals(id)?n+"은 소수, 합성수, 둘 다 아님 중 무엇인가요?":candidate+"은 "+n+"의 소인수인가요?","","pending");int[] g=read(q);if(g==null)throw new IllegalArgumentException("prime vocabulary domain");q.answers=new String[]{solution(id,g)};q.stepSupport=false;q.choiceLabels=labels(id);q.choices=new ArrayList<>(q.choiceLabels.keySet());attach(q);return q;}
 static int count(String id){if(!supports(id))throw new IllegalArgumentException("prime vocabulary skill");return CLASSIFY.equals(id)?144:FACTORS.size();}
 static Question indexed(String id,int index){if(index<0||index>=count(id))throw new IllegalArgumentException("prime vocabulary index");if(CLASSIFY.equals(id))return make(id,index+1,0);int[] g=FACTORS.get(index);return make(id,g[0],g[1]);}
 static Question next(String id,Random random,CurriculumLimits limits,Map<String,Integer> recent){return IndexedQuestionSupply.choose(count(id),i->indexed(id,i),random,limits,recent);}
 static Checker.Result check(Question q,List<String> answers){int[] g=read(q);if(g==null||answers==null||answers.size()!=1||!labels(q.skillId).containsKey(answers.get(0)))return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"분류 선택 필요");return new Checker.Result(solution(q.skillId,g).equals(answers.get(0))?Checker.Status.CORRECT:Checker.Status.WRONG_ANSWER,solution(q.skillId,g).equals(answers.get(0))?-1:0,solution(q.skillId,g).equals(answers.get(0))?"정답":"선택한 분류 확인");}
 public static void attach(Question q){int[] g=read(q);if(g==null)return;StudyGuide guide=new StudyGuide().transfer(false);guide.teachingVersion="prime-vocabulary-v1";
  if(CLASSIFY.equals(q.skillId)){guide.step("주어진 수를 나누어떨어지게 하는 양의 약수를 세세요. 같은 약수는 한 번만 셉니다.","양의 약수 개수 = ","",""+divisorCount(g[0]));guide.choice("양의 약수가 두 개면 소수, 두 개보다 많으면 합성수입니다. 1은 둘 다 아닙니다.",labels(q.skillId),solution(q.skillId,g));}
  else{guide.step("확인할 수의 양의 약수를 세세요.","확인할 수의 양의 약수 개수 = ","",""+divisorCount(g[1]));guide.step("주어진 수를 확인할 수로 나누고 나머지를 쓰세요.","주어진 수 ÷ 확인할 수의 나머지 = ","",""+(g[0]%g[1]));guide.choice("양의 약수가 두 개이고 주어진 수를 나누어떨어지게 하면 소인수입니다.",labels(q.skillId),solution(q.skillId,g));}q.studyGuide=guide;}
}
