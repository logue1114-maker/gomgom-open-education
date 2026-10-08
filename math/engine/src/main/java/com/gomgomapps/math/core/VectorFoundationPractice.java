package com.gomgomapps.math.core;
import java.util.*;
import java.util.regex.*;

/** Coordinate drills with help rebuilt exclusively from visible vector conditions. */
public final class VectorFoundationPractice {
 private VectorFoundationPractice(){}
 public static final Set<String> IDS=Set.of("vectorOperation","positionVector","vectorDot","vectorNorm","vectorLine","planeVectorLine","planeVectorCircle","vectorPlane");
 public static boolean supports(String id){return IDS.contains(id);}
 private static int signed(Random r){return r.nextInt(25)-12;}
 private static String tuple(int... values){StringJoiner j=new StringJoiner(",","(",")");for(int v:values)j.add(String.valueOf(v));return j.toString();}
 public static Question create(Catalog.Skill skill,Random r){
  int a=signed(r),b=signed(r),c=signed(r),d=signed(r),k=signed(r);String axis=r.nextBoolean()?"x":"y",prompt;long answer;
  switch(skill.id){
   case "vectorOperation":prompt="u="+tuple(a,b)+", v="+tuple(c,d)+", k="+k+"\nku−v의 "+axis+"성분은?";answer=axis.equals("x")?k*a-c:k*b-d;break;
   case "positionVector":prompt="A="+tuple(a,b)+", B="+tuple(c,d)+"\n벡터 AB의 "+axis+"성분은?";answer=axis.equals("x")?c-a:d-b;break;
   case "vectorDot":prompt="정규직교좌표입니다.\nu="+tuple(a,b)+", v="+tuple(c,d)+"\nu·v의 값은?";answer=a*c+b*d;break;
   case "vectorNorm":{int[] t=IntegerRightTriangles.next(r);boolean swap=r.nextBoolean();a=t[swap?1:0]*(r.nextBoolean()?1:-1);b=t[swap?0:1]*(r.nextBoolean()?1:-1);prompt="정규직교좌표에서 벡터 v="+tuple(a,b)+"의 크기는?";answer=t[2];break;}
   case "vectorLine":{int e=signed(r),f=signed(r);while(c==0&&d==0&&e==0)c=signed(r);int index=r.nextInt(3);axis=new String[]{"x","y","z"}[index];prompt="P="+tuple(a,b,f)+", v="+tuple(c,d,e)+", t="+k+"\nP+t v의 "+axis+"좌표는?";answer=new int[]{a,b,f}[index]+k*new int[]{c,d,e}[index];break;}
   case "planeVectorLine":while(c==0&&d==0)c=signed(r);prompt="P="+tuple(a,b)+", v="+tuple(c,d)+", t="+k+"\nP+t v의 "+axis+"좌표는?";answer=axis.equals("x")?a+k*c:b+k*d;break;
   case "planeVectorCircle":{int[] t=IntegerRightTriangles.next(r);c=a+t[0]*(r.nextBoolean()?1:-1);d=b+t[1]*(r.nextBoolean()?1:-1);prompt="정규직교좌표입니다.\nC="+tuple(a,b)+", P="+tuple(c,d)+"\n원의 중심은 C이고 P는 원 위의 점입니다. 반지름은?";answer=t[2];break;}
   case "vectorPlane":{int e=signed(r),f=signed(r);while(a==0&&b==0&&c==0)a=signed(r);prompt="n="+tuple(a,b,c)+", A="+tuple(d,e,f)+"\nA를 지나고 법선벡터가 n인 평면 n·(x,y,z)=d에서 d는?";answer=a*d+b*e+c*f;break;}
   default:throw new IllegalArgumentException(skill.id);
  }
  Question q=new Question(skill.id,prompt,"",String.valueOf(answer));q.stepSupport=false;attach(q);return q;
 }
 public static void attach(Question q){
  if(q==null||!supports(q.skillId)||q.prompt==null)return;
  String prompt=q.prompt.replace('−','-');if(!recognized(q.skillId,prompt))return;
  List<Integer> ns=new ArrayList<>();Matcher matcher=Pattern.compile("-?\\d+").matcher(prompt);while(matcher.find())ns.add(Integer.parseInt(matcher.group()));
  StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="vector-foundations-v1";
  int axis=prompt.matches("(?s).*y(?:성분|좌표).*?")||prompt.endsWith("y는?")?1:prompt.matches("(?s).*z좌표.*?")?2:0;
  int a,b,c,d;
  switch(q.skillId){
   case "vectorOperation":if(ns.size()!=5)return;a=ns.get(axis);b=ns.get(2+axis);c=ns.get(4);step(g,"계산할 성분에서 u의 값을 쓰세요.","u_i = ",a);step(g,"같은 성분에서 v의 값을 쓰세요.","v_i = ",b);step(g,"실수배 계수 k를 부호까지 쓰세요.","k = ",c);step(g,"u의 해당 성분에 k를 곱하세요.","w = k × u_i = ",c*a);step(g,"실수배한 성분에서 v의 성분을 빼세요.","성분 = w − v_i = ",c*a-b);break;
   case "positionVector":if(ns.size()!=4)return;a=ns.get(axis);b=ns.get(2+axis);step(g,"시작점 A의 해당 좌표를 쓰세요.","A_i = ",a);step(g,"끝점 B의 해당 좌표를 쓰세요.","B_i = ",b);step(g,"끝점 좌표에서 시작점 좌표를 빼세요.","AB_i = B_i − A_i = ",b-a);break;
   case "vectorDot":if(ns.size()!=4)return;a=ns.get(0);b=ns.get(1);c=ns.get(2);d=ns.get(3);step(g,"첫 번째 벡터의 x성분을 쓰세요.","u_x = ",a);step(g,"첫 번째 벡터의 y성분을 쓰세요.","u_y = ",b);step(g,"두 번째 벡터의 x성분을 쓰세요.","v_x = ",c);step(g,"두 번째 벡터의 y성분을 쓰세요.","v_y = ",d);step(g,"두 x성분을 곱하세요.","p = u_x × v_x = ",a*c);step(g,"두 y성분을 곱하세요.","q = u_y × v_y = ",b*d);step(g,"대응 성분의 곱을 더하세요.","u·v = p + q = ",a*c+b*d);break;
   case "vectorNorm":if(ns.size()!=2)return;a=ns.get(0);b=ns.get(1);step(g,"벡터의 x성분을 부호까지 쓰세요.","x = ",a);step(g,"벡터의 y성분을 부호까지 쓰세요.","y = ",b);squares(g,a,b);break;
   case "vectorLine":case "planeVectorLine":int dimension=q.skillId.equals("vectorLine")?3:2;if(ns.size()!=2*dimension+1)return;a=ns.get(axis);b=ns.get(dimension+axis);c=ns.get(2*dimension);step(g,"기준점 P의 해당 좌표를 쓰세요.","P_i = ",a);step(g,"방향벡터의 같은 성분을 쓰세요.","v_i = ",b);step(g,"매개변수 t의 값을 쓰세요.","t = ",c);step(g,"방향벡터의 성분에 t를 곱해 이동량을 구하세요.","이동량 = t × v_i = ",c*b);step(g,"기준점 좌표에 이동량을 더하세요.","좌표 = P_i + 이동량 = ",a+c*b);break;
   case "planeVectorCircle":if(ns.size()!=4)return;a=ns.get(0);b=ns.get(1);c=ns.get(2);d=ns.get(3);step(g,"중심 C의 x좌표를 쓰세요.","C_x = ",a);step(g,"중심 C의 y좌표를 쓰세요.","C_y = ",b);step(g,"원 위 점 P의 x좌표를 쓰세요.","P_x = ",c);step(g,"원 위 점 P의 y좌표를 쓰세요.","P_y = ",d);step(g,"점 P에서 중심 C의 x좌표를 빼세요.","x = P_x − C_x = ",c-a);step(g,"같은 순서로 y좌표의 차를 구하세요.","y = P_y − C_y = ",d-b);squares(g,c-a,d-b);break;
   case "vectorPlane":if(ns.size()!=6&&ns.size()!=9)return;String[] names={"n_x","n_y","n_z","A_x","A_y","A_z"};for(int i=0;i<6;i++)step(g,i<3?"법선벡터의 성분을 순서대로 쓰세요.":"평면 위 점 A의 좌표를 순서대로 쓰세요.",names[i]+" = ",ns.get(i));a=ns.get(0)*ns.get(3);b=ns.get(1)*ns.get(4);c=ns.get(2)*ns.get(5);step(g,"법선벡터와 점 A의 x성분을 곱하세요.","p = n_x × A_x = ",a);step(g,"같은 방법으로 y성분을 곱하세요.","q = n_y × A_y = ",b);step(g,"같은 방법으로 z성분을 곱하세요.","r = n_z × A_z = ",c);step(g,"세 성분의 곱을 더해 평면식의 상수 d를 구하세요.","d = p + q + r = ",a+b+c);break;
  }
  q.studyGuide=g;
 }
 private static boolean recognized(String id,String p){
  return switch(id){
   case "vectorOperation"->p.matches("(?s)(?:u=\\(.*|벡터 u=\\(.*)(?:x|y)성분은\\?");
   case "positionVector"->p.matches("(?s)A(?:=)?\\(.*벡터 AB의 (?:x|y)성분은\\?");
   case "vectorDot"->p.matches("(?s)(?:정규직교좌표입니다.\\n)?u=\\(.*(?:u·v의 값|내적 u·v)는?은?\\?")||p.endsWith("u·v는?");
   case "vectorNorm"->p.matches("(?:정규직교좌표에서 )?벡터 v=\\(.*\\)의 크기는\\?");
   case "vectorLine","planeVectorLine"->p.matches("(?s)P=\\(.*P\\+t v의 [xyz]좌표는\\?")||p.startsWith("직선의 벡터식이")||p.startsWith("직선 위 점 P의 위치벡터는");
   case "planeVectorCircle"->p.startsWith("정규직교좌표입니다.\nC=")||p.startsWith("평면에서 중심 C의 위치벡터는");
   case "vectorPlane"->p.startsWith("n=(")||p.startsWith("법선벡터가 (");
   default->false;
  };
 }
 private static void squares(StudyGuide g,int x,int y){long a=(long)x*x,b=(long)y*y,s=a+b;step(g,"x성분을 제곱하세요.","p = x² = ",a);step(g,"y성분을 제곱하세요.","q = y² = ",b);step(g,"두 제곱을 더하세요.","s = p + q = ",s);g.step("제곱합의 음이 아닌 제곱근을 구하세요.","크기 = √s = ","",Rational.of(s).sqrt().toString());}
 private static void step(StudyGuide g,String text,String before,long answer){g.step(text,before,"",String.valueOf(answer));}
}
