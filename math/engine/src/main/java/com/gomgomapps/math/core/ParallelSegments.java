package com.gomgomapps.math.core;
import java.util.Random;

/** Public ratios and one supplied length define a parallel cut of a triangle. */
public final class ParallelSegments {
 private ParallelSegments(){}
 public static Question create(Catalog.Skill s,Random random){
  int m=1+random.nextInt(5),n=1+random.nextInt(5),unit=2+random.nextInt(6),mode=random.nextInt(4);
  boolean upper=mode<2;int givenRatio=upper?m:n,targetRatio=upper?n:m,known=givenRatio*unit,result=targetRatio*unit;
  String[] givens={"AE","AD","EC","DB"},targets={"EC","DB","AE","AD"};String ratio=mode%2==0?"AD:DB":"AE:EC",given=givens[mode],target=targets[mode];
  String prompt="△ABC에서 D는 AB 위, E는 AC 위에 있고 DE ∥ BC입니다.\n"+ratio+" = "+m+":"+n+", "+given+" = "+known+". "+target+"의 길이는?";
  Question q=new Question(s.id,prompt,known+"/"+givenRatio+"*"+targetRatio,String.valueOf(result)).withInputs(Rational.of(known),Rational.of(givenRatio),Rational.of(targetRatio));q.stepSupport=false;
  q.resultSymbol=target;q.diagram=new StudyDiagram("parallelSegments",new double[]{mode,m,n,known});
  q.givenNumbers.put("known",String.valueOf(known));q.givenNumbers.put("givenRatio",String.valueOf(givenRatio));q.givenNumbers.put("targetRatio",String.valueOf(targetRatio));
  q.studyGuide=new StudyGuide().transfer(false)
   .step("DE ∥ BC이므로 AD:DB = AE:EC입니다. "+given+"에 대응하는 비의 수로 나누세요.",known+" ÷ "+givenRatio+" = ","",String.valueOf(unit))
   .step(target+"에 대응하는 비의 수를 곱하세요.",unit+" × "+targetRatio+" = ","",String.valueOf(result));
  return q;
 }
 public static double[][] vertices(StudyDiagram d){
  if(!d.type.equals("parallelSegments")||d.values.length!=4)throw new IllegalArgumentException("Unsupported parallel segment diagram");
  int mode=(int)d.values[0];double m=d.values[1],n=d.values[2],total=d.values[3]*(m+n)/(mode<2?m:n),scale=total/Math.hypot(mode%2==0?.7:.8,1),fraction=m/(m+n);
  double[] a={0,0},b={-.8*scale,scale},c={.7*scale,scale},p={b[0]*fraction,b[1]*fraction},e={c[0]*fraction,c[1]*fraction};return new double[][]{a,b,c,p,e};
 }
}
