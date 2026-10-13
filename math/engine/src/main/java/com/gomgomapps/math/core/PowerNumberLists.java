package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
/** Learner-sized complete power lists, bounded by the visible base interval rather than stored keys. */
public final class PowerNumberLists {
 private PowerNumberLists(){}
 public static final String SQUARE="listSquareNumbers",CUBE="listCubeNumbers";
 public static final List<Catalog.Skill> SKILLS=List.of(new Catalog.Skill(SQUARE,"제곱수 차례로 쓰기",7,1,1,"",SQUARE,10,"recogniseSquareNumber","문제에서 정한 정수 범위의 제곱수를 빠짐없이 쓴다."),new Catalog.Skill(CUBE,"세제곱수 차례로 쓰기",7,1,1,"",CUBE,10,"recogniseCubeNumber","문제에서 정한 정수 범위의 세제곱수를 빠짐없이 쓴다."));
 public static boolean supports(String id){return SQUARE.equals(id)||CUBE.equals(id);}
 static int exponent(String id){return SQUARE.equals(id)?2:3;}
 public static int read(Question q){if(q==null||!supports(q.skillId)||q.prompt==null)return -1;Matcher m=Pattern.compile("0 이상 ([0-9]{1,2}) 이하의 정수를 각각 "+(SQUARE.equals(q.skillId)?"제곱":"세제곱")+"한 수를 모두 쓰세요\\.\\n작은 수부터 쓰세요\\.").matcher(q.prompt);if(!m.matches())return -1;int upper=Integer.parseInt(m.group(1));return upper<=10?upper:-1;}
 static List<String> values(String id,int upper){List<String> values=new ArrayList<>();for(int b=0;b<=upper;b++)values.add(""+PrimaryPowers.power(b,exponent(id)));return values;}
 static Question make(String id,int upper){if(!supports(id)||upper<0||upper>10)throw new IllegalArgumentException("Power list base domain");Question q=new Question(id,"0 이상 "+upper+" 이하의 정수를 각각 "+(SQUARE.equals(id)?"제곱":"세제곱")+"한 수를 모두 쓰세요.\n작은 수부터 쓰세요.","",values(id,upper).toArray(String[]::new));q.kind=id;q.labels=new String[q.answers.length];for(int i=0;i<q.labels.length;i++)q.labels[i]=(SQUARE.equals(id)?"제곱수 ":"세제곱수 ")+(i+1);q.stepSupport=false;attach(q);return q;}
 static int count(){return 11;}
 static Question indexed(String id,int index){return make(id,index);}
 static Question next(String id,Random random,CurriculumLimits limits,Map<String,Integer>recent){return IndexedQuestionSupply.choose(count(),i->indexed(id,i),random,limits,recent);}
 static Checker.Result check(Question q,List<String>answers){int upper=read(q);if(upper<0||answers==null||answers.isEmpty())return new Checker.Result(Checker.Status.INPUT_NEEDED,-1,"수 입력 필요");Set<Integer> valid=new HashSet<>();for(String value:values(q.skillId,upper))valid.add(Integer.parseInt(value));Set<Integer>seen=new HashSet<>();int previous=-1;for(int i=0;i<answers.size();i++){String raw=answers.get(i);if(raw==null||!raw.trim().matches("[0-9]{1,4}"))return new Checker.Result(Checker.Status.INPUT_NEEDED,i,"수 입력 필요");int n=Integer.parseInt(raw.trim());if(!valid.contains(n))return new Checker.Result(Checker.Status.WRONG_ANSWER,i,SQUARE.equals(q.skillId)?"이 제곱수 확인":"이 세제곱수 확인");if(!seen.add(n))return new Checker.Result(Checker.Status.WRONG_ANSWER,i,"중복된 수 확인");if(n<=previous)return new Checker.Result(Checker.Status.WRONG_ANSWER,i,"작은 수부터 입력");previous=n;}if(answers.size()!=valid.size())return new Checker.Result(Checker.Status.WRONG_ANSWER,-1,SQUARE.equals(q.skillId)?"빠진 제곱수 확인":"빠진 세제곱수 확인");return new Checker.Result(Checker.Status.CORRECT,-1,"정답");}
 public static void attach(Question q){if(read(q)<0)return;StudyGuide guide=new StudyGuide().transfer(false);guide.teachingVersion="power-number-lists-v1";guide.step(SQUARE.equals(q.skillId)?"제곱은 같은 정수를 두 번 곱하는 것입니다. 지수를 쓰세요.":"세제곱은 같은 정수를 세 번 곱하는 것입니다. 지수를 쓰세요.","지수 = ","",""+exponent(q.skillId));q.studyGuide=guide;}
}
