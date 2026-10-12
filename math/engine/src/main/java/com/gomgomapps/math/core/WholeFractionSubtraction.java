package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
/** Subtract original proper fraction parts from a visible positive whole number. */
public final class WholeFractionSubtraction {
 private WholeFractionSubtraction(){}
 public static final String ID="fracWholeSub";
 static int[] read(Question q){
  if(q==null||q.prompt==null||!ID.equals(q.skillId))return null;
  Matcher m=Pattern.compile("(\\d{1,2})\\s*[−-]\\s*\\((\\d{1,2})/(\\d{1,2})\\)").matcher(q.prompt);if(!m.matches())return null;
  int w=Integer.parseInt(m.group(1)),n=Integer.parseInt(m.group(2)),d=Integer.parseInt(m.group(3));return w>=1&&w<=12&&d>=2&&d<=12&&n>=1&&n<d?new int[]{w,1,n,d}:null;
 }
 static Question make(int w,int n,int d){Question q=new Question(ID,w+" - ("+n+"/"+d+")","",Rational.of(w*d-n,d).toString());if(read(q)==null)throw new IllegalArgumentException("Whole fraction subtraction scope");q.answerFormat="fraction";q.stepSupport=false;attach(q);return q.withInputs(w,n,d);}
 static Question next(Random random,CurriculumLimits limits,Map<String,Integer> recent){
  List<int[]> parts=new ArrayList<>();for(int d:limits.fractionDenominators())if(d>=2&&d<=12)for(int n=1;n<d;n++)parts.add(new int[]{n,d});
  return IndexedQuestionSupply.choose(parts.size()*12,i->{int[] f=parts.get(i/12);return make(i%12+1,f[0],f[1]);},random,limits,recent);
 }
 public static void attach(Question q){int[] v=read(q);if(v==null)return;int w=v[0],n=v[2],d=v[3],top=w*d-n,g=Generator.gcd(top,d);
  StudyGuide guide=new StudyGuide().transfer(false);guide.teachingVersion="whole-fraction-subtraction-v1";
  step(guide,"문제의 자연수를 쓰세요.","자연수 w",w);step(guide,"빼는 분수의 분자를 쓰세요.","빼는 분자 a",n);step(guide,"빼는 분수의 분모를 쓰세요.","분모 d",d);
  step(guide,"자연수에 분모를 곱해 같은 분모의 분자로 바꾸세요.","자연수를 바꾼 분자 W = w × d",w*d);
  step(guide,"같은 분모의 분자끼리 빼세요.","남은 분자 N = W − a",top);
  step(guide,"남은 분자와 분모의 최대공약수를 구하세요.","최대공약수 g = gcd(N, d)",g);
  step(guide,"남은 분자를 최대공약수로 나누세요.","최종 분자 = N ÷ g",top/g);step(guide,"분모도 같은 최대공약수로 나누세요.","최종 분모 = d ÷ g",d/g);
  q.studyGuide=guide.fractionResult(6,7);
 }
 private static void step(StudyGuide g,String instruction,String before,int value){g.step(instruction,before+" = ","",Integer.toString(value));}
}
