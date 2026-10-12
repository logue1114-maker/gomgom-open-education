package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
/** Locate a public fraction on a public line, not a typed numerator drill. */
public final class FractionPlacement {
 public static final String ID="fractionPlacement";
 public static final Catalog.Skill SKILL=new Catalog.Skill(ID,"수직선에 분수 놓기",6,1,1,"","fractionPlacement",100,"mixedFractionCompare","진분수·가분수·대분수의 위치를 수직선에서 고른다.");
 public record Given(String raw,Rational value,int denominator,int end){public int tick(){return value.mul(Rational.of(denominator)).intValue();}}
 public static Given read(Question q){
  if(q==null||!ID.equals(q.skillId)||q.diagram==null||!ID.equals(q.diagram.type)||q.diagram.values.length!=3)return null;
  Matcher m=Pattern.compile("^수직선에서 위치를 고르세요\\.\\n("+MixedFractionValue.PATTERN+")$").matcher(q.prompt);if(!m.matches())return null;
  try{double[]v=q.diagram.values;int d=(int)v[2],end=(int)v[1];if(v[0]!=0||d!=v[2]||end!=v[1]||d<2||d>100||end<1||end>100)return null;
   String raw=m.group(1);int originalDen=Integer.parseInt(raw.substring(raw.lastIndexOf('/')+1));if(originalDen!=d)return null;
   Rational value=raw.contains(" ")?MixedFractionValue.value(raw):Expression.number(raw);if(value.compareTo(Rational.ZERO)<0||value.compareTo(Rational.of(end))>0)return null;return new Given(raw,value,d,end);
  }catch(RuntimeException e){return null;}
 }
 static Question make(String raw){int d=Integer.parseInt(raw.substring(raw.lastIndexOf('/')+1));Question q=new Question(ID,"수직선에서 위치를 고르세요.\n"+raw,"",raw.contains(" ")?MixedFractionValue.value(raw).toString():Expression.number(raw).toString());q.kind="fractionPlacement";q.labels=new String[]{"위치"};q.stepSupport=false;q.diagram=new StudyDiagram(ID,new double[]{0,7,d});if(read(q)==null)throw new IllegalArgumentException("placement givens");attach(q);return q;}
 static Question create(Random r,CurriculumLimits l){int[]den=Arrays.stream(l.fractionDenominators()).filter(d->d>=2&&d<=100).toArray();if(den.length==0)throw new IllegalArgumentException("placement denominators");int d=den[r.nextInt(den.length)],style=r.nextInt(3);String raw;
  if(style==0)raw=r.nextInt(d)+"/"+d;else if(style==1)raw=(d+r.nextInt(6*d+1))+"/"+d;else raw=(1+r.nextInt(6))+" "+(1+r.nextInt(d-1))+"/"+d;return make(raw);}
 public static int selectedTick(Question q,String raw){Given g=read(q);if(g==null||raw==null||!raw.matches("\\d{1,5}/\\d{1,3}"))return -1;try{Rational value=Expression.number(raw);if(value.compareTo(Rational.ZERO)<0||value.compareTo(Rational.of(g.end()))>0)return -1;Rational tick=value.mul(Rational.of(g.denominator()));return tick.isInteger()?tick.intValue():-1;}catch(RuntimeException e){return -1;}}
 static Checker.Result check(Question q,List<String>a){Given g=read(q);if(g==null)return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"문제 확인 필요");if(a==null||a.size()!=1||selectedTick(q,a.get(0))<0)return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"위치 선택 필요");boolean ok=selectedTick(q,a.get(0))==g.tick();return new Checker.Result(ok?Checker.Status.CORRECT:Checker.Status.WRONG_ANSWER,ok?-1:0,ok?"계산 확인 완료":"이 위치 확인");}
 public static void attach(Question q){Given g=read(q);if(g==null)return;int tick=g.tick(),d=g.denominator();StudyGuide guide=new StudyGuide().transfer(false);guide.teachingVersion="fraction-placement-v1";
  guide.step("한 단위를 몇 칸으로 나누는지 쓰세요.","한 단위 눈금 수 = ","",Integer.toString(d));guide.step("분수의 자연수 부분을 쓰세요.","자연수 부분 = ","",Integer.toString(tick/d));guide.step("자연수 부분 다음으로 몇 눈금 이동하는지 쓰세요.","다음 눈금 수 = ","",Integer.toString(tick%d));guide.step("자연수 부분과 다음 눈금 수를 합쳐 전체 눈금 수를 구하세요.","전체 눈금 수 = ","",Integer.toString(tick));guide.fractionResult(3,0);q.studyGuide=guide;}
}
