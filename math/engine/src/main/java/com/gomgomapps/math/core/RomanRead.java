package com.gomgomapps.math.core;
import java.util.*;
/** Roman givens are canonical text, not an expression or a hidden answer key. */
public final class RomanRead {
 private RomanRead(){}
 public static final String ID="romanRead",YEAR="romanYearRead",PROMPT="로마 숫자를 읽고 수를 쓰세요.",YEAR_PROMPT="기록에 적힌 로마 숫자 연도를 읽고 몇 년인지 쓰세요.";
 public static final Catalog.Skill YEAR_SKILL=new Catalog.Skill(YEAR,"로마 숫자로 적힌 연도 읽기",5,1,1,"",ID,3999,ID,"기록의 연도를 로마 숫자로 읽는다.");
 public static boolean supports(String id){return ID.equals(id)||YEAR.equals(id);}
 public static final Catalog.Skill SKILL=new Catalog.Skill(ID,"로마 숫자 읽기",4,1,1,"",ID,100,"place1000","I부터 C까지 로마 숫자를 읽는다.");
 private static final String[] TENS={"","X","XX","XXX","XL","L","LX","LXX","LXXX","XC"},ONES={"","I","II","III","IV","V","VI","VII","VIII","IX"};
 private static final String[] HUNDREDS={"","C","CC","CCC","CD","D","DC","DCC","DCCC","CM"};
 public static String numeral(int n){if(n<1||n>3999)throw new IllegalArgumentException("Roman1..3999");return "M".repeat(n/1000)+HUNDREDS[n/100%10]+TENS[n/10%10]+ONES[n%10];}
 private static final Map<String,Integer> VALUES=new HashMap<>();static{for(int n=1;n<=3999;n++)VALUES.put(numeral(n),n);}
 public static String displayed(String prompt){if(prompt==null)return null;String[] parts=prompt.split("\n",-1);if(parts.length!=2)return null;Integer n=VALUES.get(parts[1]);return n!=null&&(parts[0].equals(YEAR_PROMPT)||parts[0].equals(PROMPT)&&n<=1000)?parts[1]:null;}
 public static Integer read(Question q){if(q==null||!supports(q.skillId)||q.prompt==null)return null;String header=YEAR.equals(q.skillId)?YEAR_PROMPT:PROMPT;return q.prompt.startsWith(header+"\n")?VALUES.get(displayed(q.prompt)):null;}
 public static String[] groups(String numeral){Integer n=VALUES.get(numeral);if(n==null)return null;List<String> groups=new ArrayList<>();if(n>=1000)groups.add("M".repeat(n/1000));if(n/100%10>0)groups.add(HUNDREDS[n/100%10]);if(n/10%10>0)groups.add(TENS[n/10%10]);if(n%10>0)groups.add(ONES[n%10]);return groups.toArray(new String[0]);}
 static Question make(int n){Question q=new Question(ID,PROMPT+"\n"+numeral(n),"",Integer.toString(n));q.labels=new String[]{"수"};q.stepSupport=false;return q;}
 static Question makeYear(int n){Question q=new Question(YEAR,YEAR_PROMPT+"\n"+numeral(n),"",Integer.toString(n));q.labels=new String[]{"연도"};q.stepSupport=false;return q;}
 static Question next(Random random,CurriculumLimits limits,Map<String,Integer> recent){return next(ID,random,limits,recent);}
 static Question next(String id,Random random,CurriculumLimits limits,Map<String,Integer> recent){boolean year=YEAR.equals(id);int maximum=Math.min(year?3999:1000,limits.wholeMaximum(year?3999:100));Question q=IndexedQuestionSupply.choose(maximum,i->year?makeYear(i+1):make(i+1),random,limits,recent);attach(q);return q;}
 static Checker.Result check(Question q,List<String> answers){Integer n=read(q);if(n==null||answers==null||answers.size()!=1||answers.get(0)==null||!answers.get(0).trim().matches("[0-9]{1,4}"))return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"수 입력 필요");return Integer.parseInt(answers.get(0).trim())==n?new Checker.Result(Checker.Status.CORRECT,-1,"정답"):new Checker.Result(Checker.Status.WRONG_ANSWER,0,"이 수 확인");}
 public static void attach(Question q){Integer n=read(q);if(n==null)return;StudyGuide g=new StudyGuide().transfer(false);boolean upper=YEAR.equals(q.skillId)||n>100;g.teachingVersion=upper?"roman-read-upper-v1":"roman-read-v1";String[] groups=groups(displayed(q.prompt));for(int i=0;i<groups.length;i++){String instruction=i==0?(upper?"I=1 · V=5 · X=10 · L=50 · C=100 · D=500 · M=1000":"I=1 · V=5 · X=10 · L=50 · C=100")+"\n작은 기호가 큰 기호 앞에 있으면 큰 값에서 빼고, 나머지는 더하세요.\n기호 묶음의 값을 쓰세요.":"기호 묶음의 값을 쓰세요.";g.step(instruction,"기호 묶음 ("+groups[i]+") = ","",Integer.toString(VALUES.get(groups[i])));}g.step("각 묶음의 값을 더해 전체 수를 구하세요.","각 묶음의 값의 합 = ","",Integer.toString(n));q.studyGuide=g;}
}
