package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
/** Signed public line coefficients and squared radius determine symbolic student calculations. */
public final class LineCircleRelations {
 private LineCircleRelations(){}
 public static boolean supports(String id){return Set.of("sec_line_relation","sec_point_line_distance","sec_circle_equation").contains(id);}
 public static void attach(Question q){
  if(q==null||!supports(q.skillId)||q.prompt==null)return;String n="([+-]?\\d+(?:\\.\\d+)?(?:/\\d+)?)";Matcher m;StudyGuide g=new StudyGuide().transfer(false);
  if(q.skillId.equals("sec_line_relation")){
   m=Pattern.compile("두 직선 "+n+"x\\+\\("+n+"\\)y="+n+", "+n+"x\\+\\("+n+"\\)y="+n+"의 관계를 고르세요\\.").matcher(q.prompt);if(!m.matches())return;
   Rational a=num(m,1),b=num(m,2),c=num(m,3),aa=num(m,4),bb=num(m,5),cc=num(m,6),u=a.mul(bb),v=aa.mul(b),det=u.sub(v),p=a.mul(aa),z=b.mul(bb),dot=p.add(z);
   // Obsolete coincident-line prompts need a separate question migration, not a false parallel label.
   if(det.isZero()&&a.mul(cc).equals(aa.mul(c))&&b.mul(cc).equals(bb.mul(c)))return;
   g.teachingVersion="line-relation-relations-v1";
   step(g,"첫째 직선의 x계수 a를 부호까지 쓰세요.","첫째 x계수 a = ",a);step(g,"첫째 직선의 y계수 b를 쓰세요.","첫째 y계수 b = ",b);step(g,"둘째 직선의 x계수 c를 쓰세요.","둘째 x계수 c = ",aa);step(g,"둘째 직선의 y계수 d를 쓰세요.","둘째 y계수 d = ",bb);
   step(g,"첫째 x계수와 둘째 y계수를 곱하세요.","u = a × d = ",u);step(g,"둘째 x계수와 첫째 y계수를 곱하세요.","v = c × b = ",v);step(g,"두 곱의 차를 구하세요. D가 0이면 두 직선은 평행합니다.","D = u − v = ",det);
   step(g,"두 x계수를 곱하세요.","p = a × c = ",p);step(g,"두 y계수를 곱하세요.","q = b × d = ",z);step(g,"두 곱을 더하세요. S가 0이면 두 직선은 수직입니다.","S = p + q = ",dot);
   g.choice("D가 0이면 평행, S가 0이면 수직입니다. 두 조건이 모두 아니면 둘 다 아닙니다.",new LinkedHashMap<>(Map.of("1","평행","-1","수직","0","둘 다 아님")),det.isZero()?"1":dot.isZero()?"-1":"0");
  }else if(q.skillId.equals("sec_point_line_distance")){
   m=Pattern.compile("원점 O와 직선 "+n+"x"+n+"y\\+\\("+n+"\\)=0 사이의 거리는\\?").matcher(q.prompt);if(!m.matches())return;Rational a=num(m,1),b=num(m,2),c=num(m,3),u=a.pow(2),v=b.pow(2),s=u.add(v);if(s.isZero())return;Rational norm=s.sqrt(),absolute=c.compareTo(Rational.ZERO)<0?c.neg():c;
   g.teachingVersion="point-line-distance-relations-v1";
   step(g,"직선의 x계수 a를 부호까지 쓰세요.","x계수 a = ",a);step(g,"직선의 y계수 b를 부호까지 쓰세요.","y계수 b = ",b);step(g,"직선의 상수 c를 부호까지 쓰세요.","상수 c = ",c);
   step(g,"x계수를 제곱하세요.","u = a² = ",u);step(g,"y계수를 제곱하세요.","v = b² = ",v);step(g,"계수의 두 제곱을 더하세요.","s = u + v = ",s);step(g,"제곱합의 양의 제곱근을 구하세요.","분모 t = √s = ",norm);
   step(g,"원점의 좌표는 둘 다 0이므로 대입하면 c만 남습니다. c의 절댓값을 구하세요.","분자 k = |c| = ",absolute);step(g,"절댓값을 계수 제곱합의 제곱근으로 나누세요.","거리 = k ÷ t = ",absolute.div(norm));
  }else{
   m=Pattern.compile("\\(x-\\("+n+"\\)\\)²\\+\\(y-\\("+n+"\\)\\)²="+n+"인 원의 반지름은\\?").matcher(q.prompt);if(!m.matches())return;Rational s=num(m,3);if(s.compareTo(Rational.ZERO)<=0)return;g.teachingVersion="circle-radius-relations-v1";
   step(g,"원의 방정식 오른쪽은 반지름의 제곱입니다. 오른쪽 수를 쓰세요.","반지름의 제곱 s = ",s);step(g,"반지름은 양수입니다. s의 양의 제곱근을 구하세요.","반지름 r = √s = ",s.sqrt());
  }q.studyGuide=g;
 }
 private static Rational num(Matcher m,int i){return Expression.number(m.group(i));}
 private static void step(StudyGuide g,String instruction,String before,Rational expected){g.step(instruction,before,"",expected.toString());}
}
