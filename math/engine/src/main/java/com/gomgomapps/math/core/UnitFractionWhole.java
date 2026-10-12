package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
/** Recover a whole distance from its visible unit fractional part, without unit conversion. */
public final class UnitFractionWhole {
 private UnitFractionWhole(){}
 public static final String ID="unitFractionWhole";
 static int[] read(Question q){if(q==null||!ID.equals(q.skillId)||q.prompt==null)return null;Matcher m=Pattern.compile("전체 거리의 1/(\\d{1,2})이 (\\d{1,4}) km입니다\\.\\n전체 거리는 몇 km인가요\\?").matcher(q.prompt);if(!m.matches())return null;int d=Integer.parseInt(m.group(1)),p=Integer.parseInt(m.group(2));return d>=2&&d<=12&&p>=1&&p<=1000?new int[]{d,p}:null;}
 static Question make(int d,int part){Question q=new Question(ID,"전체 거리의 1/"+d+"이 "+part+" km입니다.\n전체 거리는 몇 km인가요?","",Integer.toString(d*part));if(read(q)==null)throw new IllegalArgumentException("Unit fraction whole scope");q.stepSupport=false;attach(q);return q.withInputs(d,part);}
 static Question next(Random r,CurriculumLimits limits,Map<String,Integer> recent){List<Integer> denominators=new ArrayList<>();for(int d:limits.fractionDenominators())if(d>=2&&d<=12)denominators.add(d);return IndexedQuestionSupply.choose(denominators.size()*1000,i->make(denominators.get(i/1000),i%1000+1),r,limits,recent);}
 static Checker.Result check(Question q,List<String> answers){int[] v=read(q);if(v==null)return new Checker.Result(Checker.Status.INPUT_NEEDED,-1,"문제 확인 필요");return PublicFractionArithmetic.checkValue(q,answers,Rational.of(v[0]*v[1]));}
 public static void attach(Question q){int[] v=read(q);if(v==null)return;StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="unit-fraction-whole-v1";
  g.step("단위분수의 분모를 쓰세요.","같은 크기 부분의 수 d = ","",Integer.toString(v[0]));
  g.step("한 부분의 거리를 쓰세요. 단위는 km입니다.","한 부분의 거리 p (km) = ","",Integer.toString(v[1]));
  g.step("한 부분의 거리에 부분의 수를 곱해 전체 거리를 구하세요.","전체 거리 T = p × d (km) = ","",Integer.toString(v[0]*v[1]));
  q.studyGuide=g;
 }
}
