package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
/** Complete positive common multiples through 100, including an explicit none answer. */
public final class CommonMultipleLists {
 private CommonMultipleLists(){}
 public static final String ID="commonMultiplesWithin100",NONE="@no-common-multiples";
 public static final Catalog.Skill SKILL=new Catalog.Skill(ID,"100까지 모든 공배수",6,1,1,"",ID,100,"tables,divide","두 수의 배수 중 100 이하인 양의 공배수를 빠짐없이 찾는다.");
 private static final Pattern PROMPT=Pattern.compile("^([0-9]{1,2})와 ([0-9]{1,2})의 공배수 중 100 이하인 양의 수를 모두 쓰세요\\.\\n작은 수부터 쓰세요\\.$");
 private static final List<int[]> PAIRS=new ArrayList<>();static{for(int a=1;a<=12;a++)for(int b=a;b<=12;b++)PAIRS.add(new int[]{a,b});}
 public static boolean supports(String id){return ID.equals(id);}
 public static int[] read(Question q){if(q==null||!supports(q.skillId)||q.prompt==null)return null;Matcher m=PROMPT.matcher(q.prompt);if(!m.matches())return null;int a=Integer.parseInt(m.group(1)),b=Integer.parseInt(m.group(2));return a>=1&&a<=b&&b<=12?new int[]{a,b}:null;}
 public static boolean noneExpected(Question q){int[] g=read(q);return g!=null&&values(g).isEmpty();}
 private static List<String> values(int[] g){List<String> result=new ArrayList<>();for(int n=1;n<=100;n++)if(n%g[0]==0&&n%g[1]==0)result.add(""+n);return result;}
 private static int first(int[] g){int n=Math.max(g[0],g[1]);while(n%g[0]!=0||n%g[1]!=0)n++;return n;}
 static Question make(int a,int b){Question q=new Question(ID,a+"와 "+b+"의 공배수 중 100 이하인 양의 수를 모두 쓰세요.\n작은 수부터 쓰세요.","");int[] g=read(q);if(g==null)throw new IllegalArgumentException("common multiple list domain");List<String> list=values(g);q.answers=list.isEmpty()?new String[]{NONE}:list.toArray(String[]::new);q.labels=new String[q.answers.length];for(int i=0;i<q.labels.length;i++)q.labels[i]="공배수 "+(i+1);q.stepSupport=false;attach(q);return q;}
 static Question indexed(int i){if(i<0||i>=PAIRS.size())throw new IllegalArgumentException("common multiple list index");int[] g=PAIRS.get(i);return make(g[0],g[1]);}
 static Question next(Random random,CurriculumLimits limits,Map<String,Integer> recent){boolean none=random.nextBoolean();List<Integer> preferred=new ArrayList<>();for(int i=0;i<PAIRS.size();i++)if((first(PAIRS.get(i))>100)==none)preferred.add(i);Question q=IndexedQuestionSupply.choose(preferred.size(),i->indexed(preferred.get(i)),random,limits,recent);return recent.containsKey(q.signature())?IndexedQuestionSupply.choose(PAIRS.size(),CommonMultipleLists::indexed,random,limits,recent):q;}
 static Checker.Result check(Question q,List<String> answers){int[] g=read(q);if(g==null||answers==null||answers.isEmpty())return new Checker.Result(Checker.Status.INPUT_NEEDED,-1,"공배수 입력 또는 없음 선택");List<String> expected=values(g);
  // The final marker is an explicit learner selection; preceding numbers remain an inactive saved draft.
  if(NONE.equals(answers.get(answers.size()-1)))return new Checker.Result(expected.isEmpty()?Checker.Status.CORRECT:Checker.Status.WRONG_ANSWER,-1,expected.isEmpty()?"정답":"공배수 있음 확인");
  Set<Integer> seen=new HashSet<>();int previous=0;for(int i=0;i<answers.size();i++){String raw=answers.get(i);if(raw==null||!raw.trim().matches("[0-9]{1,3}"))return new Checker.Result(Checker.Status.INPUT_NEEDED,i,"공배수 입력 또는 없음 선택");int n=Integer.parseInt(raw.trim());if(n<1||n>100||n%g[0]!=0||n%g[1]!=0)return new Checker.Result(Checker.Status.WRONG_ANSWER,i,"이 공배수 확인");if(!seen.add(n))return new Checker.Result(Checker.Status.WRONG_ANSWER,i,"중복된 수 확인");if(n<=previous)return new Checker.Result(Checker.Status.WRONG_ANSWER,i,"작은 수부터 입력");previous=n;}if(answers.size()!=expected.size())return new Checker.Result(Checker.Status.WRONG_ANSWER,-1,"빠진 공배수 확인");return new Checker.Result(Checker.Status.CORRECT,-1,"정답");
 }
 public static void attach(Question q){int[] g=read(q);if(g==null)return;int f=first(g);StudyGuide h=new StudyGuide().transfer(false);h.teachingVersion="common-multiple-list-v1";h.step("첫째 수의 배수에서 둘째 수로도 나누어떨어지는 가장 작은 양의 수를 찾으세요.","첫 공배수 = ","",""+f);h.step("첫 공배수를 첫째 수로 나누세요.","첫 공배수 ÷ 첫째 수 = ","",""+(f/g[0]));h.step("첫 공배수를 둘째 수로 나누세요.","첫 공배수 ÷ 둘째 수 = ","",""+(f/g[1]));Map<String,String> options=new LinkedHashMap<>();options.put("1","100 이하");options.put("0","100 초과");h.choice("첫 공배수가 100 이하인지 확인하세요.",options,f<=100?"1":"0");if(f<=100){h.step("100을 첫 공배수로 나눈 정수 몫을 쓰세요.","마지막 곱하는 수 = ","",""+(100/f));h.step("첫 공배수에 마지막 곱하는 수를 곱하세요.","마지막 공배수 = ","",""+(f*(100/f)));}q.studyGuide=h;}
}
