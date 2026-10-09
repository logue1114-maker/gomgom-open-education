package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
/** Public corresponding-side ratios: fractional enlargement/reduction and identity. */
public final class FractionShapeScale {
 private FractionShapeScale(){}
 public static final List<Rational> FACTORS=factors();
 public static final List<String> SHAPES=List.of("정사각형","직사각형","삼각형");
 private static List<Rational> factors(){Set<Rational> values=new LinkedHashSet<>();for(int n=1;n<=8;n++)for(int d=1;d<=8;d++)values.add(Rational.of(n,d));return List.copyOf(values);}
 public static int domainSize(){return FACTORS.size()*49*3*2;}
 static Question at(int i){int mode=i%2;i/=2;int shape=i%3;i/=3;int a=i%49+2;Rational k=FACTORS.get(i/49),b=Rational.of(a).mul(k);String prefix="두 닮은 "+SHAPES.get(shape)+"에서 첫째 대응변은 ";Rational answer=mode==0?b:k;Question q=new Question(SimilarShapeSupply.ID,mode==0?prefix+a+"입니다. 첫째에서 둘째로의 배율은 "+k+"입니다. 둘째 대응변은?":prefix+a+", 둘째 대응변은 "+b+"입니다. 첫째에서 둘째로의 배율은?","",answer.toString());if(!answer.isInteger())q.answerFormat="fraction";q.stepSupport=false;q.diagram=new StudyDiagram("fractionShapeScalePair",new double[]{shape,mode,a,(mode==0?k:b).n.doubleValue()/(mode==0?k:b).d.doubleValue()},""+a,mode==0?"?":b.toString());attach(q);return q;}
 public record Givens(int shape,boolean known,Rational first,Rational other){}
 public static Givens read(Question q){if(q==null||!SimilarShapeSupply.ID.equals(q.skillId)||q.prompt==null)return null;String num="(\\d+(?:/\\d+)?)";Matcher m=Pattern.compile("두 닮은 (정사각형|직사각형|삼각형)에서 첫째 대응변은 "+num+"입니다. 첫째에서 둘째로의 배율은 "+num+"입니다. 둘째 대응변은\\?").matcher(q.prompt);boolean known=m.matches();if(!known){m=Pattern.compile("두 닮은 (정사각형|직사각형|삼각형)에서 첫째 대응변은 "+num+", 둘째 대응변은 "+num+"입니다. 첫째에서 둘째로의 배율은\\?").matcher(q.prompt);if(!m.matches())return null;}Rational first=Expression.number(m.group(2)),other=Expression.number(m.group(3));return first.compareTo(Rational.ZERO)>0&&other.compareTo(Rational.ZERO)>0?new Givens(SHAPES.indexOf(m.group(1)),known,first,other):null;}
 public static void attach(Question q){Givens v=read(q);if(v==null)return;StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="fraction-shape-scale-v1";g.step("첫째 도형의 대응변 길이를 쓰세요.","첫째 길이 a = ","",v.first.toString());if(v.known){g.step("첫째에서 둘째로의 배율을 쓰세요.","배율 k = ","",v.other.toString());g.step("첫째 대응변 길이에 배율을 곱하세요.","둘째 길이 b = a × k = ","",v.first.mul(v.other).toString());}else{g.step("둘째 도형의 대응변 길이를 쓰세요.","둘째 길이 b = ","",v.other.toString());g.step("둘째 대응변 길이를 첫째 대응변 길이로 나누세요.","배율 k = b ÷ a = ","",v.other.div(v.first).toString());}q.studyGuide=g;}
}
