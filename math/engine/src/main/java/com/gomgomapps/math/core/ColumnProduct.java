package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
/** Carry-based written multiplication from public two-digit/one-digit operands. */
public final class ColumnProduct {
 private ColumnProduct(){}
 public static final String ID="columnProduct2";
 public static final Catalog.Skill SKILL=new Catalog.Skill(ID,"두 자리 수 세로곱셈",3,1,4,"",ID,99,"tables,add100","일의 자리 곱에서 받아올릴 수를 구하고 십의 자리 곱에 더한다.");
 static final Set<Integer> FACTORS=Set.of(0,1,2,3,4,5,8);
 public static boolean supports(String id){return ID.equals(id);}
 static Question make(int a,int b){if(a<10||a>99||!FACTORS.contains(b))throw new IllegalArgumentException("column product domain");Question q=new Question(ID,a+" × "+b,"",Integer.toString(a*b));q.kind="columnProduct";q.labels=new String[]{"답"};q.stepSupport=false;return q;}
 public static int[] read(Question q){if(q==null||!supports(q.skillId)||q.prompt==null)return null;Matcher m=Pattern.compile("^(\\d{2}) × ([0-9])$").matcher(q.prompt);if(!m.matches())return null;int a=Integer.parseInt(m.group(1)),b=Integer.parseInt(m.group(2));if(a<10||a>99||!FACTORS.contains(b))return null;return new int[]{a,b,a%10,a/10,a%10*b,a%10*b%10,a%10*b/10,a/10*b+a%10*b/10,a*b};}
 static Question next(Random random,CurriculumLimits limits,Map<String,Integer> recent){Map<String,Question> pool=new LinkedHashMap<>();for(int a=10;a<=99;a++)for(int b:FACTORS){Question q=make(a,b);if(limits.allows(q))pool.put(q.signature(),q);}Question q=FactFoundations.choose(pool,random,recent);attach(q);return q;}
 static Checker.Result check(Question q,List<String> answers){int[] v=read(q);if(v==null||answers.size()!=1||answers.get(0)==null||!answers.get(0).trim().matches("[0-9]{1,3}"))return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"답 입력 필요");return Integer.parseInt(answers.get(0).trim())==v[8]?new Checker.Result(Checker.Status.CORRECT,-1,"정답"):new Checker.Result(Checker.Status.WRONG_ANSWER,0,"이 답 확인");}
 public static void attach(Question q){int[] v=read(q);if(v==null)return;StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="column-product-v1";
  g.step("두 자리 수의 일의 자리 숫자를 쓰세요.","일의 자리 숫자 = ","",Integer.toString(v[2]));
  g.step("두 자리 수의 십의 자리 숫자를 쓰세요.","십의 자리 숫자 = ","",Integer.toString(v[3]));
  g.step("곱하는 한 자리 수를 쓰세요.","곱하는 수 = ","",Integer.toString(v[1]));
  g.step("일의 자리 숫자에 한 자리 수를 곱하세요.","일의 자리 숫자 × 곱하는 수 = ","",Integer.toString(v[4]));
  g.step("곱한 수에서 일의 자리 숫자를 남기세요.","일의 자리 곱의 일의 자리 = ","",Integer.toString(v[5]));
  g.step("곱한 수의 십의 자리 숫자를 받아올립니다. 없으면 0입니다.","받아올릴 수 = ","",Integer.toString(v[6]));
  g.step("십의 자리 숫자에 한 자리 수를 곱하고 받아올릴 수를 더하세요.","십의 자리 숫자 × 곱하는 수 + 받아올릴 수 = ","",Integer.toString(v[7]));
  g.step("십의 자리 계산값과 일의 자리에 남긴 숫자를 합쳐 답을 쓰세요.","십의 자리 계산값 × 10 + 남긴 일의 자리 숫자 = ","",Integer.toString(v[8]));q.studyGuide=g;
 }
}
