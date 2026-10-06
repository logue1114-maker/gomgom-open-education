package com.gomgomapps.math.core;
import java.util.regex.*;

/** Numeric scaffolds are derived from the public expression, not answers or choice inputs. */
final class RadicalTeaching {
 private RadicalTeaching(){}
 private static int coefficient(String raw){return raw.isEmpty()||raw.equals("+")?1:raw.equals("-")?-1:Integer.parseInt(raw);}
 private static int outside(int n){int best=1;for(int k=2;k*k<=n;k++)if(n%(k*k)==0)best=k;return best;}
 private static StudyGuide step(StudyGuide g,String instruction,String before,long value){return g.step(instruction,before,"",String.valueOf(value));}
 static void attach(Question q){
  if(q.studyGuide!=null||!RadicalWork.SKILLS.contains(q.skillId))return;
  String source=Expression.normalize(q.expression);StudyGuide g=new StudyGuide().transfer(false);Matcher m;
  switch(q.skillId){
   case "rootSimplify": {
    m=Pattern.compile("(-?)sqrt\\((\\d+)\\)").matcher(source);if(!m.matches())throw new IllegalArgumentException("근호 정리의 주어진 식 확인 필요");
    int sign=m.group(1).isEmpty()?1:-1,n=Integer.parseInt(m.group(2)),k=outside(n),square=k*k;
    step(g,"근호 안의 수를 나누어떨어지게 하는 가장 큰 제곱수를 찾으세요.",n+" = □ × …\n□ = ",square);
    step(g,"근호 안의 수를 찾은 제곱수로 나누세요.",n+" ÷ "+square+" = ",n/square);
    step(g,"찾은 제곱수의 양의 제곱근을 구하세요.","√("+square+") = ",k);
    step(g,"원래 근호 앞의 부호를 유지해 계수를 쓰세요.","("+sign+") × "+k+" = ",sign*k);break;
   }
   case "rootAddSub": {
    m=Pattern.compile("(-?)sqrt\\((\\d+)\\)([+-])sqrt\\((\\d+)\\)").matcher(source);if(!m.matches())throw new IllegalArgumentException("근호 덧셈의 주어진 식 확인 필요");
    int sign=m.group(1).isEmpty()?1:-1,n=Integer.parseInt(m.group(2)),other=Integer.parseInt(m.group(4)),k=outside(n),b=outside(other),d=n/(k*k);if(other/(b*b)!=d)throw new IllegalArgumentException("같은 근호 확인 필요");
    step(g,"첫 근호 안의 수를 나누어떨어지게 하는 가장 큰 제곱수를 찾으세요.",n+" = □ × …\n□ = ",k*k);
    step(g,"첫 근호 안에 남는 수를 구하세요.",n+" ÷ "+(k*k)+" = ",d);
    step(g,"첫 근호에서 밖으로 꺼낼 양의 정수를 구하세요.","√("+(k*k)+") = ",k);
    step(g,"둘째 근호에서 같은 근호 밖으로 꺼낼 양의 정수를 구하세요.","√("+other+" ÷ "+d+") = ",b);
    int signed=sign*k,result=m.group(3).equals("+")?signed+b:signed-b;
    step(g,"부호를 유지하고 같은 근호의 계수끼리 계산하세요.","("+signed+") "+(m.group(3).equals("+")?"+":"−")+" "+b+" = ",result);break;
   }
   case "rootProduct":case "rootQuotient": {
    m=Pattern.compile("\\(([+-]?\\d*)sqrt\\((\\d+)\\)\\)([*/])\\(([+-]?\\d*)sqrt\\((\\d+)\\)\\)").matcher(source);if(!m.matches())throw new IllegalArgumentException("근호 곱셈의 주어진 식 확인 필요");
    int a=coefficient(m.group(1)),d=Integer.parseInt(m.group(2)),b=coefficient(m.group(4)),e=Integer.parseInt(m.group(5)),combined=d*e,k=outside(combined),inside=combined/(k*k);boolean divide=q.skillId.equals("rootQuotient");
    if(!m.group(3).equals(divide?"/":"*"))throw new IllegalArgumentException("계산 기호 확인 필요");
    Rational c=divide?Rational.of(a,b):Rational.of((long)a*b),finalCoefficient=c.mul(Rational.of(k,divide?e:1));
    g.step(divide?"근호 앞의 계수끼리 나누세요.":"근호 앞의 계수끼리 곱하세요.","("+a+") "+(divide?"÷":"×")+" ("+b+") = ","",c.toString());
    step(g,divide?"분자와 분모에 분모의 근호를 곱합니다. 두 근호 안의 수를 곱하세요.":"두 근호 안의 수를 곱하세요.",d+" × "+e+" = ",combined);
    step(g,"곱한 수의 근호에서 밖으로 꺼낼 양의 정수를 구하세요.","√("+combined+") = □ × √(…)\n□ = ",k);
    step(g,"근호 안에 남는 수를 구하세요.",combined+" ÷ "+(k*k)+" = ",inside);
    g.step(divide?"꺼낸 정수를 계수에 곱하고 유리화한 분모로 나누세요.":"꺼낸 정수를 계수에 곱하세요.","("+c+") × "+k+(divide?" ÷ "+e:"")+" = ","",finalCoefficient.toString());break;
   }
   case "rootRationalize": {
    m=Pattern.compile("([+-]?\\d+)/\\((?:(\\d+)\\+)?sqrt\\((\\d+)\\)\\)").matcher(source);if(!m.matches())throw new IllegalArgumentException("유리화의 주어진 식 확인 필요");
    int a=Integer.parseInt(m.group(1)),b=m.group(2)==null?0:Integer.parseInt(m.group(2)),d=Integer.parseInt(m.group(3));
    if(b==0){
     step(g,"분모와 분자에 같은 근호를 곱합니다. 분모의 곱을 구하세요.","√("+d+") × √("+d+") = ",d);
     step(g,"분자에 곱한 근호 앞의 계수를 쓰세요.",a+" × √("+d+") = □ × √("+d+") · □ = ",a);
     g.step("유리화한 분모의 수로 계수를 나누세요.","("+a+") ÷ "+d+" = ","",Rational.of(a,d).toString());
    }else{
     long den=(long)b*b-d,rational=(long)a*b,root=-a;
     step(g,"분모와 분자에 부호를 바꾼 식을 곱합니다. 정수항을 제곱하세요.","("+b+" + √("+d+")) × ("+b+" − √("+d+")) · "+b+" × "+b+" = ",b*b);
     step(g,"정수항의 제곱에서 근호 안의 수를 빼세요.",(b*b)+" − "+d+" = ",den);
     step(g,"분자의 수와 부호를 바꾼 식의 정수항을 곱하세요.","("+a+") × "+b+" = ",rational);
     step(g,"분자의 수와 근호 항의 계수 −1을 곱하세요.","("+a+") × (−1) = ",root);
     g.step("정수항의 계수를 유리화한 분모로 나누세요.","("+rational+") ÷ ("+den+") = ","",Rational.of(rational,den).toString());
     g.step("근호 항의 계수를 유리화한 분모로 나누세요.","("+root+") ÷ ("+den+") = ","",Rational.of(root,den).toString());
    }break;
   }
   default:throw new IllegalArgumentException("근호 학습 유형 확인 필요");
  }
  q.studyGuide=g;
 }
}
