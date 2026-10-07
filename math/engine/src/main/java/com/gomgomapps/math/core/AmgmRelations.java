package com.gomgomapps.math.core;
import java.util.regex.*;
/** Symbolic AM-GM calculation plus the positive point attaining the lower bound. */
public final class AmgmRelations {
 private AmgmRelations(){}
 public static void attach(Question q){
  if(q==null||!"sec_amgm_minimum".equals(q.skillId)||q.prompt==null)return;
  Matcher m=Pattern.compile("x>0일 때 (\\d*)x\\+(\\d+)/x(?:\\+\\((-?\\d+)\\))?의 최솟값은\\?").matcher(q.prompt);if(!m.matches())return;
  long p=m.group(1).isEmpty()?1:Long.parseLong(m.group(1)),n=Long.parseLong(m.group(2)),c=m.group(3)==null?0:Long.parseLong(m.group(3));if(p<=0||n<=0||n%p!=0)return;
  long product=p*n,root=(long)Math.sqrt(product),ratio=n/p,point=(long)Math.sqrt(ratio);if(root*root!=product||point*point!=ratio)return;
  StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="amgm-relations-v1";
  step(g,"x항의 계수를 쓰세요. x만 있으면 계수는 1입니다.","x항의 계수 p = ",p);
  step(g,"x로 나누는 항의 분자를 찾아 쓰세요.","분자 q = ",n);
  step(g,"상수를 찾아 쓰세요. 상수항이 없으면 0입니다.","상수 c = ",c);
  step(g,"양수인 두 항의 곱에서 x가 약분됩니다. 계수와 분자를 곱하세요.","두 항의 곱 t = p × q = ",product);
  step(g,"두 항의 곱의 양의 제곱근을 구하세요.","양의 제곱근 u = √t = ",root);
  step(g,"두 항이 같아지는 x를 찾으려면 분자를 계수로 나누세요.","x의 제곱 v = q ÷ p = ",ratio);
  step(g,"x는 양수입니다. 앞에서 구한 값의 양의 제곱근을 구하세요.","두 항이 같아지는 x = √v = ",point);
  step(g,"양수 두 항의 합은 곱의 제곱근을 두 번 더한 값 이상입니다. 두 항이 같아지는 x에서 이 값을 얻습니다.","두 항의 최소 합 m = u + u = ",2*root);
  step(g,"두 항의 최소 합에 상수를 더하세요.","최솟값 M = m + c = ",2*root+c);q.studyGuide=g;
 }
 private static void step(StudyGuide g,String text,String before,long value){g.step(text,before,"",Long.toString(value));}
}
