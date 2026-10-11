package com.gomgomapps.math.core;
import java.math.BigDecimal;import java.util.*;import java.util.regex.*;
/** One public fraction expressed independently as a decimal and a percentage. */
public final class FractionPercentEquivalence {
 private FractionPercentEquivalence(){}
 public static final String ID="fractionDecimalPercent";
 public static final List<Catalog.Skill> SKILLS=List.of(new Catalog.Skill(ID,"분수·소수·백분율",5,1,1,"","percentageEquivalence",1000,"percentDecimal","같은 양을 분수·소수·백분율로 나타낸다."));
 private static final int[] LEGACY_DENOMINATORS={2,4,5,10,20,25,40,50,100,125,200,250,500,1000};
 private static final int[] DENOMINATORS=java.util.stream.IntStream.rangeClosed(1,1000).filter(d->d%10==0||d%25==0||Arrays.stream(LEGACY_DENOMINATORS).anyMatch(v->v==d)).toArray();
 private static final List<Givens> CONDITIONS=conditions();
 private static List<Givens> conditions(){List<Givens> values=new ArrayList<>();for(int d:DENOMINATORS)for(int n=0;n<=d;n++)if(n*1000%d==0)values.add(new Givens(n,d));return List.copyOf(values);}
 public static boolean supports(String id){return ID.equals(id);}
 public record Givens(int numerator,int denominator){}
 public static int domainSize(){return CONDITIONS.size();}
 public static Question make(int n,int d){
  if(Arrays.stream(DENOMINATORS).noneMatch(v->v==d)||n<0||n>d||n*1000%d!=0)throw new IllegalArgumentException("Selected fraction equivalence scope");BigDecimal v=BigDecimal.valueOf(n).divide(BigDecimal.valueOf(d));
  Question q=new Question(ID,n+"/"+d+"를 소수와 백분율로 나타내세요.","",plain(v),plain(v.movePointRight(2)));q.labels=new String[]{"소수 값","백분율 (%)"};q.answerFormat="decimalValue";q.decimal=true;q.stepSupport=false;attach(q);return q;
 }
 static Givens conditionAt(int index){if(index<0||index>=CONDITIONS.size())throw new IllegalArgumentException("Index out of bounds");return CONDITIONS.get(index);}
 private static Question at(int index){Givens v=conditionAt(index);return make(v.numerator(),v.denominator());}
 static Question next(Random random,CurriculumLimits limits,Map<String,Integer> recent){return IndexedQuestionSupply.choose(domainSize(),FractionPercentEquivalence::at,random,limits,recent);}
 public static Givens read(Question q){if(q==null||!supports(q.skillId)||q.prompt==null)return null;Matcher m=Pattern.compile("(\\d{1,4})/(\\d{1,4})를 소수와 백분율로 나타내세요\\.").matcher(q.prompt);if(!m.matches())return null;int n=Integer.parseInt(m.group(1)),d=Integer.parseInt(m.group(2));return n<=d&&Arrays.stream(DENOMINATORS).anyMatch(v->v==d)&&n*1000%d==0?new Givens(n,d):null;}
 static Checker.Result check(Question q,List<String> answers){Givens x=read(q);if(x==null||answers==null||answers.size()!=2)return new Checker.Result(Checker.Status.INPUT_NEEDED,-1,"답 입력 필요");BigDecimal value=BigDecimal.valueOf(x.numerator()).divide(BigDecimal.valueOf(x.denominator()));for(int i=0;i<2;i++){String raw=answers.get(i);if(raw==null)return new Checker.Result(Checker.Status.INPUT_NEEDED,-1,"답 입력 필요");raw=Expression.normalize(raw.trim());if(raw.length()>120||!raw.matches("[+-]?\\d+(?:\\.\\d+)?"))return new Checker.Result(Checker.Status.INPUT_NEEDED,-1,"수로 입력 필요");if(new BigDecimal(raw).compareTo(i==0?value:value.movePointRight(2))!=0)return new Checker.Result(Checker.Status.WRONG_ANSWER,i,"이 답 확인");}return new Checker.Result(Checker.Status.CORRECT,-1,"계산 확인 완료");}
 public static void attach(Question q){
  Givens x=read(q);if(x==null)return;q.labels=new String[]{"소수 값","백분율 (%)"};boolean legacy=Arrays.stream(LEGACY_DENOMINATORS).anyMatch(d->d==x.denominator());BigDecimal v=BigDecimal.valueOf(x.numerator()).divide(BigDecimal.valueOf(x.denominator()));StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion=legacy?"fraction-percent-equivalence-v1":"fraction-percent-equivalence-v2";
  g.step("문제의 분모를 쓰세요.","분모 d = ","",Integer.toString(x.denominator()));g.step("문제의 분자를 쓰세요.","분자 n = ","",Integer.toString(x.numerator()));
  if(legacy){step(g,"전체를 같은 크기로 나눈 한 부분의 크기를 소수로 쓰세요.","한 부분 u = 1 ÷ d = ",BigDecimal.ONE.divide(BigDecimal.valueOf(x.denominator())));step(g,"한 부분의 크기에 부분 수를 곱하세요.","소수 v = u × n = ",v);}else step(g,"분자를 분모로 나누어 소수로 쓰세요.","소수 v = n ÷ d = ",v);
  step(g,"소수에 100을 곱해 백분율을 구하세요.","백분율 p = v × 100 = ",v.movePointRight(2));q.studyGuide=g;
 }
 private static void step(StudyGuide g,String text,String frame,BigDecimal value){g.step(text,frame,"",plain(value));g.frames.get(g.frames.size()-1).inputFormat="decimalValue";}
 private static String plain(BigDecimal value){return value.stripTrailingZeros().toPlainString();}
}
