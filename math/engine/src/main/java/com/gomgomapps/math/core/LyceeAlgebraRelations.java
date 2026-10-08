package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
/** Coefficients are reconstructed from visible equations, not stored answer metadata. */
public final class LyceeAlgebraRelations {
 private LyceeAlgebraRelations(){}
 public static boolean supports(String id){return Set.of("powerLaw","linearInequality","linearSlope","linearXIntercept","linearYIntercept","discriminant","quadraticRootSum","quadraticRootProduct").contains(id);}
 public static void attach(Question q){
  if(q==null||!supports(q.skillId)||q.prompt==null)return;
  StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="lycee-algebra-relations-v1";
  if(q.skillId.equals("powerLaw")){
   Matcher m=Pattern.compile("(\\d+)\\^(\\d+) × \\1\\^(\\d+)(?: = \\1\\^□\\n□에 들어갈 지수는\\?)?").matcher(q.prompt);if(!m.matches())return;
   Rational base=num(m,1),p=num(m,2),r=num(m,3),sum=p.add(r);
   if(q.prompt.contains("□")){g.teachingVersion=null;g.step("첫 번째 거듭제곱의 지수를 입력하세요.","첫 번째 지수 = ","",p.toString()).step("두 번째 거듭제곱의 지수를 입력하세요.","두 번째 지수 = ","",r.toString()).step("밑이 같으면 두 지수를 더합니다.","첫 번째 지수 + 두 번째 지수 = ","",sum.toString());q.studyGuide=g;return;}
   step(g,"같은 밑과 두 지수를 찾아 쓰세요.","a = ",base);step(g,"같은 밑과 두 지수를 찾아 쓰세요.","p = ",p);step(g,"같은 밑과 두 지수를 찾아 쓰세요.","q = ",r);
   step(g,"밑이 같은 곱의 지수는 두 지수의 합입니다.","k = p + q = ",sum);
   if(!q.prompt.contains("□"))step(g,"밑을 합한 지수만큼 거듭제곱하세요.","a^k = ",base.pow(Integer.parseInt(sum.toString())));
  }else if(q.skillId.equals("linearSlope")){
   Matcher m=Pattern.compile("일차함수의 그래프가 두 점\\nA\\((-?\\d+), (-?\\d+)\\), B\\((-?\\d+), (-?\\d+)\\)를 지납니다.\\n기울기는\\?").matcher(q.prompt);if(!m.matches())return;
   Rational x1=num(m,1),y1=num(m,2),x2=num(m,3),y2=num(m,4),dx=x2.sub(x1),dy=y2.sub(y1);if(dx.equals(Rational.ZERO))return;
   for(int i=1;i<=4;i++)step(g,"두 점의 좌표를 문제에서 찾아 쓰세요.",new String[]{"x_A = ","y_A = ","x_B = ","y_B = "}[i-1],num(m,i));
   step(g,"같은 방향으로 좌표의 차를 구하세요.","Δx = x_B − x_A = ",dx);step(g,"같은 방향으로 좌표의 차를 구하세요.","Δy = y_B − y_A = ",dy);step(g,"세로 변화량을 가로 변화량으로 나누세요.","m = Δy ÷ Δx = ",dy.div(dx));
  }else if(q.skillId.endsWith("Intercept")){
   int end=q.prompt.indexOf("의 그래프에서");if(end<0||!q.prompt.startsWith("y = "))return;
   Expression.Poly poly=Expression.parse(q.prompt.substring(4,end));Rational a=poly.coefficient(1),b=poly.coefficient(0);if(a.equals(Rational.ZERO))return;
   step(g,"식에서 x항 계수와 상수항을 찾아 쓰세요.","a = ",a);step(g,"식에서 x항 계수와 상수항을 찾아 쓰세요.","b = ",b);
   if(q.skillId.equals("linearXIntercept")){step(g,"x절편에서는 y가 0입니다. 상수항을 반대편으로 옮기세요.","t = −b = ",b.neg());step(g,"x항 계수로 나누세요.","x₀ = t ÷ a = ",b.neg().div(a));}
   else{step(g,"y절편에서는 x가 0입니다. x항의 값을 구하세요.","t = a × 0 = ",Rational.ZERO);step(g,"상수항을 더하세요.","y₀ = t + b = ",b);}
  }else if(q.skillId.equals("linearInequality")){
   String[] parts=LinearInequality.parts(q.prompt.split("\\n")[0]);Expression.Poly l=Expression.parse(parts[0]),r=Expression.parse(parts[2]);Rational a=l.coefficient(1),b=l.coefficient(0),c=r.coefficient(1),d=r.coefficient(0),k=a.sub(c),t=d.sub(b);if(k.equals(Rational.ZERO))return;
   step(g,"왼쪽과 오른쪽의 x항 계수와 상수항을 찾아 쓰세요.","a = ",a);step(g,"왼쪽과 오른쪽의 x항 계수와 상수항을 찾아 쓰세요.","b = ",b);step(g,"왼쪽과 오른쪽의 x항 계수와 상수항을 찾아 쓰세요.","c = ",c);step(g,"왼쪽과 오른쪽의 x항 계수와 상수항을 찾아 쓰세요.","d = ",d);
   step(g,"x항은 왼쪽, 상수항은 오른쪽으로 모으세요.","k = a − c = ",k);step(g,"x항은 왼쪽, 상수항은 오른쪽으로 모으세요.","t = d − b = ",t);step(g,"0이 아닌 x항 계수로 나누어 경계값을 구하세요.","v = t ÷ k = ",t.div(k));
   Map<String,String> signs=new LinkedHashMap<>();for(String sign:LinearInequality.SIGNS)signs.put(sign,LinearInequality.displaySign(sign));g.choice("음수로 나누면 부등호가 바뀝니다. x와 경계값 사이의 부등호를 고르세요.",signs,k.compareTo(Rational.ZERO)<0?LinearInequality.reverse(parts[1]):parts[1]);
  }else{
   String statement=q.prompt.substring(q.prompt.indexOf('\n')+1);Expression.Poly poly;
   if(q.skillId.equals("discriminant"))poly=Expression.parse(q.prompt.split(" = 0")[0]);else poly=Expression.parse(statement.split(" = 0")[0]);
   Rational a=poly.coefficient(2),b=poly.coefficient(1),c=poly.coefficient(0);if(a.equals(Rational.ZERO))return;
   step(g,"이차식에서 각 항의 계수를 부호까지 쓰세요.","a = ",a);step(g,"이차식에서 각 항의 계수를 부호까지 쓰세요.","b = ",b);step(g,"이차식에서 각 항의 계수를 부호까지 쓰세요.","c = ",c);
   if(q.skillId.equals("discriminant")){Rational square=b.pow(2),product=Rational.of(4).mul(a).mul(c);step(g,"x항 계수를 제곱하세요.","u = b² = ",square);step(g,"이차항 계수와 상수항을 곱하고 4배 하세요.","v = 4 × a × c = ",product);step(g,"제곱값에서 곱의 4배를 빼세요.","D = u − v = ",square.sub(product));}
   else if(q.skillId.equals("quadraticRootSum")){step(g,"x항 계수의 부호를 바꾸세요.","t = −b = ",b.neg());step(g,"이차항 계수로 나누세요.","S = t ÷ a = ",b.neg().div(a));}
   else step(g,"상수항을 이차항 계수로 나누세요.","P = c ÷ a = ",c.div(a));
  }
  q.studyGuide=g;
 }
 private static Rational num(Matcher m,int i){return Expression.number(m.group(i));}
 private static void step(StudyGuide g,String instruction,String before,Rational value){g.step(instruction,before,"",value.toString());}
}
