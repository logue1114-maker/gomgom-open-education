package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
/** Arbitrary-start thousand changes, with public givens owning the solution. */
public final class ThousandChange {
 private ThousandChange(){}
 public static final String MORE="thousandMore",LESS="thousandLess";
 public static final List<Catalog.Skill> SKILLS=List.of(
  new Catalog.Skill(MORE,"1000만큼 큰 수",4,1,1,"","thousandChange",10000,"place1000,add1000","주어진 수에서 1000만큼 큰 수를 구한다."),
  new Catalog.Skill(LESS,"1000만큼 작은 수",4,1,1,"","thousandChange",10000,"place1000,sub1000","주어진 수에서 1000만큼 작은 수를 구한다."));
 public static boolean supports(String id){return MORE.equals(id)||LESS.equals(id);}
 public static String template(String id){return MORE.equals(id)?"%s보다 1000만큼 큰 수를 쓰세요.":LESS.equals(id)?"%s보다 1000만큼 작은 수를 쓰세요.":null;}
 private static final Map<String,Pattern> PATTERNS=new HashMap<>();static{for(Catalog.Skill s:SKILLS)PATTERNS.put(s.id,Pattern.compile("^"+Pattern.quote(template(s.id)).replace("%s","\\E([0-9]{1,5})\\Q")+"$"));}
 public static String displayed(String id,String prompt){if(!supports(id)||prompt==null)return null;Matcher m=PATTERNS.get(id).matcher(prompt);return m.matches()?m.group(1):null;}
 static Question make(String id,int start){if(!supports(id)||start<(MORE.equals(id)?0:1000)||start>(MORE.equals(id)?9000:10000))throw new IllegalArgumentException("Thousand change domain");Question q=new Question(id,String.format(Locale.ROOT,template(id),start),"",Integer.toString(start+(MORE.equals(id)?1000:-1000)));q.labels=new String[]{"수"};q.stepSupport=false;return q;}
 public static int[] read(Question q){if(q==null)return null;String raw=displayed(q.skillId,q.prompt);if(raw==null)return null;int start=Integer.parseInt(raw);if(start<(MORE.equals(q.skillId)?0:1000)||start>(MORE.equals(q.skillId)?9000:10000))return null;return new int[]{start,1000,start+(MORE.equals(q.skillId)?1000:-1000)};}
 static Question next(String id,Random random,CurriculumLimits limits,Map<String,Integer> recent){Question q=IndexedQuestionSupply.choose(9001,i->make(id,i+(LESS.equals(id)?1000:0)),random,limits,recent);attach(q);return q;}
 static Checker.Result check(Question q,List<String> answers){int[] v=read(q);if(v==null||answers==null||answers.size()!=1||answers.get(0)==null||!answers.get(0).trim().matches("[0-9]{1,5}"))return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"수 입력 필요");return Integer.parseInt(answers.get(0).trim())==v[2]?new Checker.Result(Checker.Status.CORRECT,-1,"정답"):new Checker.Result(Checker.Status.WRONG_ANSWER,0,"이 수 확인");}
 public static void attach(Question q){int[] v=read(q);if(v==null)return;String change=MORE.equals(q.skillId)?"늘어난 수":"줄어든 수";StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="thousand-change-v1";g.step("주어진 수를 쓰세요.","주어진 수 = ","",Integer.toString(v[0]));g.step(change+"를 쓰세요.",change+" = ","","1000");g.step("두 수의 관계로 답을 구하세요.","주어진 수"+(MORE.equals(q.skillId)?" + ":" − ")+change+" = ","",Integer.toString(v[2]));q.studyGuide=g;}
}
