package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
/** Two visible operations on the same measured quantity; keys are not authority. */
public final class TwoStepFractionStories {
 private TwoStepFractionStories(){}
 public static final String ID="twoStepFractionStory";
 public record Givens(int total,int numerator,int denominator,int extra,boolean remaining){}
 public static String prompt(int t,int n,int d,int extra){return extra==0?"길이가 "+t+" m인 끈의 "+n+"/"+d+"을 사용했습니다.\n남은 끈의 길이는 몇 m인가요?":"물이 "+t+" L 들어 있는 통에서 물의 "+n+"/"+d+"을 빈 통에 따르고, 다른 통의 물 "+extra+" L를 더 부었습니다.\n새 통의 물은 모두 몇 L인가요?";}
 public static Givens read(Question q){if(q==null||!ID.equals(q.skillId)||q.prompt==null)return null;Matcher m=Pattern.compile("길이가 (\\d{1,3}) m인 끈의 (\\d{1,2})/(\\d{1,2})을 사용했습니다\\.\\n남은 끈의 길이는 몇 m인가요\\?").matcher(q.prompt);boolean remaining=m.matches();if(!remaining){m=Pattern.compile("물이 (\\d{1,3}) L 들어 있는 통에서 물의 (\\d{1,2})/(\\d{1,2})을 빈 통에 따르고, 다른 통의 물 (\\d{1,3}) L를 더 부었습니다\\.\\n새 통의 물은 모두 몇 L인가요\\?").matcher(q.prompt);if(!m.matches())return null;}int t=Integer.parseInt(m.group(1)),n=Integer.parseInt(m.group(2)),d=Integer.parseInt(m.group(3)),e=remaining?0:Integer.parseInt(m.group(4));return t>=1&&t<=100&&d>=2&&d<=12&&n>=1&&n<d&&(remaining||e>=1&&e<=100)?new Givens(t,n,d,e,remaining):null;}
 static Rational portion(Givens v){return Rational.of(v.total*v.numerator,v.denominator);}
 static Rational answer(Givens v){return v.remaining?Rational.of(v.total).sub(portion(v)):portion(v).add(Rational.of(v.extra));}
 static Question make(int t,int n,int d,int extra){Question q=new Question(ID,prompt(t,n,d,extra),"","");Givens v=read(q);if(v==null)throw new IllegalArgumentException("Two-step quantity scope");q.answers=new String[]{answer(v).toString()};q.answerFormat="fraction";q.stepSupport=false;attach(q);return q.withInputs(t,n,d,extra);}
 static Question next(Random random,CurriculumLimits limits,Map<String,Integer> recent){List<int[]> fractions=new ArrayList<>();for(int d:limits.fractionDenominators())if(d>=2&&d<=12)for(int n=1;n<d;n++)fractions.add(new int[]{n,d});boolean remaining=random.nextBoolean();int extras=remaining?1:100;return IndexedQuestionSupply.choose(fractions.size()*100*extras,i->{int extra=remaining?0:i%extras+1;i/=extras;int total=i%100+1;int[] f=fractions.get(i/100);return make(total,f[0],f[1],extra);},random,limits,recent);}
 static Checker.Result check(Question q,List<String> answers){Givens v=read(q);return v==null?new Checker.Result(Checker.Status.INPUT_NEEDED,-1,"문제 확인 필요"):PublicFractionArithmetic.checkValue(q,answers,answer(v));}
 public static void attach(Question q){Givens v=read(q);if(v==null)return;StudyGuide h=new StudyGuide().transfer(false);h.teachingVersion="two-step-fraction-story-v1";
 h.step("처음 전체 양을 쓰세요.","전체 양 T = ","",Integer.toString(v.total));h.step("원래 분자를 쓰세요.","원래 분자 n = ","",Integer.toString(v.numerator));h.step("원래 분모를 쓰세요.","원래 분모 d = ","",Integer.toString(v.denominator));
 h.step("전체 양에 주어진 분수를 곱해 첫 번째 양을 구하세요.","첫 번째 양 P = T × n/d = ","",portion(v).toString());
 h.step(v.remaining?"전체 양에서 사용한 양을 빼세요.":"먼저 따른 양에 추가로 부은 양을 더하세요.",v.remaining?"남은 길이 R = T − P (m) = ":"모두 부은 양 R = P + 추가 양 (L) = ","",answer(v).toString());q.studyGuide=h;
 }
 public static int errorPart(Question q,String raw){Givens v=read(q);if(v==null||raw==null||raw.length()>120||FractionInput.invalidPart(raw)!=0)return 0;String[] p=FractionInput.parts(raw);Rational target=answer(v);java.math.BigInteger n=new java.math.BigInteger(p[0]),d=new java.math.BigInteger(p[1]);if(n.multiply(target.d).equals(target.n.multiply(d)))return 0;if(d.equals(target.d))return 1;if(n.equals(target.n))return 2;return 0;}
}
