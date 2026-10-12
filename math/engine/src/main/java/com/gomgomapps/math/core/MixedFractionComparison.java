package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
/** Compare original mixed/improper forms, deriving every result from public operands. */
public final class MixedFractionComparison {
 public static final String ID="mixedFractionCompare",ORDER="mixedFractionOrder";
 public static final List<Catalog.Skill> SKILLS=List.of(new Catalog.Skill(ID,"대분수와 가분수 비교",6,1,2,"","symbol",100,"fracMixed","대분수와 가분수의 크기를 비교한다."));
 public static List<String> operands(Question q){if(q==null||!ID.equals(q.skillId))return List.of();Matcher m=Pattern.compile("^("+MixedFractionValue.PATTERN+")  □  ("+MixedFractionValue.PATTERN+")$").matcher(q.prompt);return m.matches()?List.of(m.group(1),m.group(2)):List.of();}
 static Checker.Result check(Question q,List<String> answers){
  List<String>v=operands(q);if(v.size()!=2)return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"문제 확인 필요");
  if(answers==null||answers.size()!=1||!Set.of("<","=",">").contains(answers.get(0)))return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"비교 기호 선택 필요");
  try{int cmp=MixedFractionValue.value(v.get(0)).compareTo(MixedFractionValue.value(v.get(1)));boolean ok=answers.get(0).equals(cmp<0?"<":cmp>0?">":"=");return new Checker.Result(ok?Checker.Status.CORRECT:Checker.Status.WRONG_ANSWER,ok?-1:0,ok?"계산 확인 완료":"이 기호 확인");}catch(RuntimeException e){return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"문제 확인 필요");}
 }
 static String mixed(Random r,CurriculumLimits l){int[]den=Arrays.stream(l.fractionDenominators()).filter(d->d>=2&&d<=100).toArray();if(den.length==0)throw new IllegalArgumentException("mixed denominators");int d=den[r.nextInt(den.length)];return (1+r.nextInt(6))+" "+(1+r.nextInt(d-1))+"/"+d;}
 static String improper(String raw){var p=MixedFractionValue.read(raw);return p.improper()+"/"+p.denominator();}
 static Question create(Random r,CurriculumLimits l){
  String a=mixed(r,l),b;int relation=r.nextInt(3)-1;
  if(relation==0){var p=MixedFractionValue.read(a);int[]scales=Arrays.stream(l.fractionDenominators()).filter(d->d%p.denominator()==0&&d>=p.denominator()).map(d->d/p.denominator()).toArray();int scale=scales[r.nextInt(scales.length)];b=(p.improper()*scale)+"/"+(p.denominator()*scale);}else{
   var left=MixedFractionValue.read(a);var right=MixedFractionValue.read(mixed(r,l));boolean sameWhole=r.nextBoolean();
   for(int i=0;i<16&&sameWhole&&Rational.of(left.numerator(),left.denominator()).equals(Rational.of(right.numerator(),right.denominator()));i++)right=MixedFractionValue.read(mixed(r,l));
   if(sameWhole&&!Rational.of(left.numerator(),left.denominator()).equals(Rational.of(right.numerator(),right.denominator()))){
    if(Integer.signum(Rational.of(left.numerator(),left.denominator()).compareTo(Rational.of(right.numerator(),right.denominator())))!=relation){var temp=left;left=right;right=temp;}
    int w=left.whole();a=w+" "+left.numerator()+"/"+left.denominator();b=(w*right.denominator()+right.numerator())+"/"+right.denominator();
   }else{int w=relation<0?Math.min(5,left.whole()):Math.max(2,left.whole());a=w+" "+left.numerator()+"/"+left.denominator();int rw=relation<0?w+1+r.nextInt(6-w):1+r.nextInt(w-1);b=(rw*right.denominator()+right.numerator())+"/"+right.denominator();}
  }
  var bp=MixedFractionValue.read(b);String bm=(bp.improper()/bp.denominator())+" "+(bp.improper()%bp.denominator())+"/"+bp.denominator();
  int forms=r.nextInt(3);if(forms==1)b=bm;else if(forms==2)a=improper(a);
  if(r.nextBoolean()){String t=a;a=b;b=t;}return question(a,b);
 }
 static Question question(String a,String b){int cmp=MixedFractionValue.value(a).compareTo(MixedFractionValue.value(b));Question q=new Question(ID,a+"  □  "+b,"",cmp<0?"<":cmp>0?">":"=");q.kind="symbol";q.stepSupport=false;attach(q);return q;}
 private static void step(StudyGuide g,String text,String before,int n){g.step(text,before+" = ","",Integer.toString(n));}
 public static void attach(Question q){List<String>v=operands(q);if(v.size()!=2)return;try{
  var a=MixedFractionValue.read(v.get(0));var b=MixedFractionValue.read(v.get(1));StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="mixed-fraction-comparison-v1";
  part(g,a,"왼쪽");part(g,b,"오른쪽");step(g,"두 분모를 곱해 공통 분모를 구하세요.","공통 분모",a.denominator()*b.denominator());
  step(g,"왼쪽 가분수의 분자에 오른쪽 분모를 곱하세요.","통분한 왼쪽 분자",a.improper()*b.denominator());step(g,"오른쪽 가분수의 분자에 왼쪽 분모를 곱하세요.","통분한 오른쪽 분자",b.improper()*a.denominator());
  int cmp=a.value().compareTo(b.value());g.choice("통분한 두 분자를 비교해 기호를 고르세요.",Map.of("<","<","=","=",">",">"),cmp<0?"<":cmp>0?">":"=");q.studyGuide=g;
 }catch(RuntimeException ignored){}}
 public static int[] referenceIndices(Question q,int stage){
  List<String>v=operands(q);if(v.size()!=2)return new int[0];try{
   boolean lm=MixedFractionValue.read(v.get(0)).whole()>0,rm=MixedFractionValue.read(v.get(1)).whole()>0;int rightStart=lm?5:2,leftDen=lm?2:1,leftN=lm?4:0,rightDen=rightStart+(rm?2:1),rightN=rightStart+(rm?4:0),crossStart=rightStart+(rm?5:2);
   if(lm&&stage==3)return new int[]{0,2};if(lm&&stage==4)return new int[]{3,1};
   if(rm&&stage==rightStart+3)return new int[]{rightStart,rightDen};if(rm&&stage==rightStart+4)return new int[]{rightStart+3,rightStart+1};
   if(stage==crossStart)return new int[]{leftDen,rightDen};if(stage==crossStart+1)return new int[]{leftN,rightDen};if(stage==crossStart+2)return new int[]{rightN,leftDen};if(stage==crossStart+3)return new int[]{crossStart+1,crossStart+2};
  }catch(RuntimeException ignored){}return new int[0];
 }
 private static void part(StudyGuide g,MixedFractionValue.Parts p,String side){
  if(p.whole()>0)step(g,side+" 대분수의 자연수 부분을 쓰세요.",side+" 자연수 부분",p.whole());
  step(g,side+" 분자를 쓰세요.",side+" 분자",p.numerator());step(g,side+" 분모를 쓰세요.",side+" 분모",p.denominator());
  if(p.whole()>0){step(g,"자연수 부분에 분모를 곱하세요.",side+" 자연수와 분모의 곱",p.whole()*p.denominator());step(g,"곱에 원래 분자를 더하세요.",side+" 가분수 분자",p.improper());}
 }
}
