package com.gomgomapps.math.core;
import java.util.*;
/** Roman givens are canonical text, not an expression or a hidden answer key. */
public final class RomanRead {
 private RomanRead(){}
 public static final String ID="romanRead",PROMPT="로마 숫자를 읽고 수를 쓰세요.";
 public static final Catalog.Skill SKILL=new Catalog.Skill(ID,"로마 숫자 읽기",4,1,1,"",ID,100,"place1000","I부터 C까지 로마 숫자를 읽는다.");
 private static final String[] TENS={"","X","XX","XXX","XL","L","LX","LXX","LXXX","XC"},ONES={"","I","II","III","IV","V","VI","VII","VIII","IX"};
 public static String numeral(int n){if(n<1||n>100)throw new IllegalArgumentException("Roman1..100");return n==100?"C":TENS[n/10]+ONES[n%10];}
 private static final Map<String,Integer> VALUES=new HashMap<>();static{for(int n=1;n<=100;n++)VALUES.put(numeral(n),n);}
 public static String displayed(String prompt){if(prompt==null)return null;String[] parts=prompt.split("\n",-1);return parts.length==2&&parts[0].equals(PROMPT)&&VALUES.containsKey(parts[1])?parts[1]:null;}
 public static Integer read(Question q){return q!=null&&ID.equals(q.skillId)?VALUES.get(displayed(q.prompt)):null;}
 public static String[] groups(String numeral){Integer n=VALUES.get(numeral);if(n==null)return null;if(n==100)return new String[]{"C"};List<String> groups=new ArrayList<>();if(n>=10)groups.add(TENS[n/10]);if(n%10>0)groups.add(ONES[n%10]);return groups.toArray(new String[0]);}
 static Question make(int n){Question q=new Question(ID,PROMPT+"\n"+numeral(n),"",Integer.toString(n));q.labels=new String[]{"수"};q.stepSupport=false;return q;}
 static Question next(Random random,CurriculumLimits limits,Map<String,Integer> recent){int maximum=Math.min(100,limits.wholeMaximum(100));Question q=IndexedQuestionSupply.choose(maximum,i->make(i+1),random,limits,recent);attach(q);return q;}
 static Checker.Result check(Question q,List<String> answers){Integer n=read(q);if(n==null||answers==null||answers.size()!=1||answers.get(0)==null||!answers.get(0).trim().matches("[0-9]{1,3}"))return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"수 입력 필요");return Integer.parseInt(answers.get(0).trim())==n?new Checker.Result(Checker.Status.CORRECT,-1,"정답"):new Checker.Result(Checker.Status.WRONG_ANSWER,0,"이 수 확인");}
 public static void attach(Question q){Integer n=read(q);if(n==null)return;StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="roman-read-v1";String[] groups=groups(displayed(q.prompt));for(int i=0;i<groups.length;i++){String instruction=i==0?"I=1 · V=5 · X=10 · L=50 · C=100\n작은 기호가 큰 기호 앞에 있으면 큰 값에서 빼세요.\n기호 묶음의 값을 쓰세요.":"기호 묶음의 값을 쓰세요.";g.step(instruction,"기호 묶음 ("+groups[i]+") = ","",Integer.toString(VALUES.get(groups[i])));}g.step("각 묶음의 값을 더해 전체 수를 구하세요.","각 묶음의 값의 합 = ","",Integer.toString(n));q.studyGuide=g;}
}
