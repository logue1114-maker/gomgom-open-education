package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
/** Up-to-four-digit written products, with the same public operands used by the column workspace. */
public final class LargeWrittenProducts {
 private LargeWrittenProducts(){}
 public static final String SINGLE="writtenProduct4x1",DOUBLE="writtenProduct4x2",KIND="writtenLargeProduct";
 public static final List<Catalog.Skill> SKILLS=List.of(
  new Catalog.Skill(SINGLE,"네 자리까지 한 자리 수 곱셈",5,1,1,"",SINGLE,9999,"tables,divide","자리별로 곱하고 받아올린 수를 더해 세로셈으로 계산한다."),
  new Catalog.Skill(DOUBLE,"네 자리까지 두 자리 수 곱셈",5,1,1,"",DOUBLE,9999,"tables,add100","일의 자리와 십의 자리의 부분곱을 구한 뒤 자릿값을 맞추어 더한다."));
 public static boolean supports(String id){return SINGLE.equals(id)||DOUBLE.equals(id);}
 public static int[] read(Question q){if(q==null||!supports(q.skillId)||!KIND.equals(q.kind)||q.prompt==null)return null;Matcher m=Pattern.compile("^([0-9]{1,4}) × ([0-9]{1,2})$").matcher(q.prompt);if(!m.matches())return null;int a=Integer.parseInt(m.group(1)),b=Integer.parseInt(m.group(2));if(a<1||a>9999||b<(SINGLE.equals(q.skillId)?0:10)||b>(SINGLE.equals(q.skillId)?9:99))return null;return new int[]{a,b,a*b};}
 static int count(String id){if(!supports(id))throw new IllegalArgumentException("written product skill");return 9999*(SINGLE.equals(id)?10:90);}
 static Question indexed(String id,int index){if(index<0||index>=count(id))throw new IllegalArgumentException("written product index");int width=SINGLE.equals(id)?10:90,a=1+index/width,b=index%width+(SINGLE.equals(id)?0:10);Question q=new Question(id,a+" × "+b,"",""+(a*b));q.kind=KIND;q.stepSupport=false;return q;}
 static Question next(String id,Random random,CurriculumLimits limits,Map<String,Integer> recent){Question q=IndexedQuestionSupply.choose(count(id),i->indexed(id,i),random,limits,recent);WholeProductRelations.attach(q);return q;}
 static Checker.Result check(Question q,List<String> answers){int[] g=read(q);if(g==null||answers==null||answers.size()!=1||answers.get(0)==null||!answers.get(0).trim().matches("[0-9]{1,6}"))return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"답 입력 필요");return Integer.parseInt(answers.get(0).trim())==g[2]?new Checker.Result(Checker.Status.CORRECT,-1,"정답"):new Checker.Result(Checker.Status.WRONG_ANSWER,0,"이 답 확인");}
}
