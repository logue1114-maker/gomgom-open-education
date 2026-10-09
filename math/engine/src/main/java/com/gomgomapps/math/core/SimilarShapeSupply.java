package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
/** Selected integer enlargement; drawings hold only public givens. */
public final class SimilarShapeSupply {
 private SimilarShapeSupply(){}
 public static final String ID="sec_similarity_length";
 static boolean selected(String id,CurriculumLimits l){return ID.equals(id)&&l.variedFacts();}
 public static int domainSize(){return 1078;}
 static Question at(int i){int mode=i%2,pair=i/2,a=pair%49+2,k=pair/49+2;Question q;
 if(mode==0){q=new Question(ID,"두 닮은 도형의 닮음비가 1:"+k+"입니다. 작은 도형의 대응변이 "+a+"일 때 큰 도형의 대응변은?","",""+(a*k));SimilarityMeasureRelations.attach(q);}
 else{q=new Question(ID,"두 닮은 도형에서 작은 대응변은 "+a+", 큰 대응변은 "+(a*k)+"입니다. 작은 도형에서 큰 도형으로의 배율은?","",""+k);attach(q);}
 q.stepSupport=false;q.diagram=new StudyDiagram("similarShapePair",new double[]{mode,a,mode==0?k:a*k});return q;}
 static Question next(Random random,CurriculumLimits limits,Map<String,Integer> recent){return IndexedQuestionSupply.choose(domainSize()+FractionShapeScale.domainSize(),i->i<domainSize()?at(i):FractionShapeScale.at(i-domainSize()),random,limits,recent);}
 public static int[] read(Question q){if(q==null||!ID.equals(q.skillId)||q.prompt==null)return null;Matcher m=Pattern.compile("두 닮은 도형에서 작은 대응변은 (\\d+), 큰 대응변은 (\\d+)입니다. 작은 도형에서 큰 도형으로의 배율은\\?").matcher(q.prompt);if(!m.matches())return null;int a=Integer.parseInt(m.group(1)),b=Integer.parseInt(m.group(2));return a>0&&b>a?new int[]{a,b}:null;}
 public static void attach(Question q){if(FractionShapeScale.read(q)!=null){FractionShapeScale.attach(q);return;}int[] v=read(q);if(v==null)return;StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="similar-shape-scale-v1";g.step("작은 도형의 대응변 길이를 쓰세요.","작은 길이 a = ","",""+v[0]);g.step("큰 도형의 대응변 길이를 쓰세요.","큰 길이 b = ","",""+v[1]);g.step("큰 대응변 길이를 작은 대응변 길이로 나누세요.","배율 k = b ÷ a = ","",Rational.of(v[1],v[0]).toString());q.studyGuide=g;}
}
