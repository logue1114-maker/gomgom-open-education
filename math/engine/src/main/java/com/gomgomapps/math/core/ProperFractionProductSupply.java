package com.gomgomapps.math.core;
import java.util.*;
/** Selected finite proper-fraction products/divisions reuse the public operand teaching. */
public final class ProperFractionProductSupply {
 private ProperFractionProductSupply(){}
 public static boolean supports(String id,CurriculumLimits limits){return Set.of("fracMul","fracDivInt").contains(id)&&limits.variedFacts();}
 private static List<int[]> parts(CurriculumLimits limits){List<int[]> result=new ArrayList<>();for(int d:limits.fractionDenominators())for(int n=1;n<d;n++)result.add(new int[]{n,d});return result;}
 public static Question make(String id,int a,int b,int c,int d){
  boolean mul=id.equals("fracMul");if(!Set.of("fracMul","fracDivInt").contains(id)||b<2||b>12||a<1||a>=b||c<1||mul&&(d<2||d>12||c>=d)||!mul&&(d!=1||c>12))throw new IllegalArgumentException("Selected proper fraction scope");
  Rational x=Rational.of(a,b),y=Rational.of(c,d);String prompt=a+"/"+b+(mul?" × ":" ÷ ")+(mul?c+"/"+d:Integer.toString(c));Question q=new Question(id,prompt,"("+a+"/"+b+")"+(mul?" * ":" / ")+(mul?"("+c+"/"+d+")":Integer.toString(c)),(mul?x.mul(y):x.div(y)).toString());q.stepSupport=false;q.answerFormat="fraction";if(mul)q.kind="reduced";attach(q);return q.withInputs(x,y);
 }
 static Question next(Catalog.Skill s,Random random,CurriculumLimits limits,Map<String,Integer> recent){List<int[]> values=parts(limits);int size=values.size(),count=s.id.equals("fracMul")?size*size:size*12;return IndexedQuestionSupply.choose(count,i->{int[] first=values.get(s.id.equals("fracMul")?i/size:i/12);int[] second=s.id.equals("fracMul")?values.get(i%size):new int[]{i%12+1,1};return make(s.id,first[0],first[1],second[0],second[1]);},random,limits,recent);}
 public static void attach(Question q){if(q==null||!Set.of("fracMul","fracDivInt").contains(q.skillId))return;var publicOperands=FractionProducts.read(q.prompt);if(publicOperands==null||publicOperands.left.mixed()||publicOperands.right.mixed()||publicOperands.left.denominator==1)return;if(q.skillId.equals("fracMul")&&(publicOperands.right.denominator==1||!publicOperands.operator.equals("*")))return;if(q.skillId.equals("fracDivInt")&&(publicOperands.right.denominator!=1||!publicOperands.operator.equals("/")))return;Question proxy=new Question("fracMulInt",q.prompt,"","poison");FractionProductRelations.attach(proxy);q.studyGuide=proxy.studyGuide;}
}
