package com.gomgomapps.math.core;
import java.util.Random;

/** Small integer, half and quarter lengths; actual public right-angle and side labels. */
public final class RightTriangleLength {
 private RightTriangleLength(){}
 public static Question create(Catalog.Skill skill,Random random){
  int[] triangle=IntegerRightTriangles.next(random);int denominator=new int[]{1,2,4}[random.nextInt(3)],missing=random.nextInt(3);boolean leg=missing<2;
  Rational first=Rational.of(leg?triangle[2]:triangle[0],denominator),second=Rational.of(leg?triangle[1-missing]:triangle[1],denominator),answer=Rational.of(triangle[missing],denominator),square=answer.pow(2);
  String a=first.decimalText(),b=second.decimalText(),calculation="("+a+")^2"+(leg?"-":"+")+"("+b+")^2";
  String prompt="△ABC에서 ∠A = 90°입니다.\n"+(leg?"BC = "+a+", AC = "+b+". AB의 길이는?":"AB = "+a+", AC = "+b+". BC의 길이는?");
  Question q=new Question(skill.id,prompt,"sqrt("+calculation+")",answer.toString()).withInputs(first,second);q.stepSupport=false;q.decimal=true;
  q.diagram=new StudyDiagram("rightTriangleLength",new double[]{leg?1:0,Double.parseDouble(a),Double.parseDouble(b)});q.resultSymbol=leg?"AB":"BC";
  q.studyGuide=new StudyGuide().transfer(false)
   .step(leg?"빗변 BC의 제곱에서 AC의 제곱을 빼세요.":"직각을 낀 AB와 AC의 제곱을 더하세요.",calculation.replace("^2","²")+" = ","",square.toString())
   .step("길이는 양수입니다. 양의 제곱근을 구하세요.","√"+square.decimalText()+" = ","",answer.toString());
  return q;
 }
 public static double[][] vertices(StudyDiagram d){
  if(!d.type.equals("rightTriangleLength")||d.values.length!=3)throw new IllegalArgumentException("Unsupported right triangle diagram");
  double first=d.values[1],known=d.values[2],ab=d.values[0]==0?first:Math.sqrt(first*first-known*known);return new double[][]{{0,0},{ab,0},{0,known}};
 }
}
