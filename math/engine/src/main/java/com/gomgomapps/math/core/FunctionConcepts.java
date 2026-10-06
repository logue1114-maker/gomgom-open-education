package com.gomgomapps.math.core;
import java.util.*;

/** Unique output is separate from one-to-one matching; square rules are nonlinear. */
public final class FunctionConcepts {
 private FunctionConcepts(){}
 public static final List<Catalog.Skill> SKILLS=List.of(
  new Catalog.Skill("functionUniqueOutput","함수인지 판단하기",9,1,3,"","functionUniqueOutput",100,"functionTableOutput","각 x에 서로 다른 y가 하나만 대응하면 함수이다. 서로 다른 x가 같은 y에 대응해도 함수이다."),
  new Catalog.Skill("functionSquareOutput","제곱이 있는 함수의 값 구하기",9,1,3,"","functionSquareOutput",100,"functionTableOutput,signedMul","음수 x도 먼저 제곱한다. 그 값에 계수를 곱하고 상수를 더한다."));
 public static boolean supports(String id){return SKILLS.stream().anyMatch(s->s.id.equals(id));}
 static Question create(Catalog.Skill s,Random r){return s.id.equals("functionUniqueOutput")?mapping(s,r):square(s,r);}
 private static Question mapping(Catalog.Skill s,Random r){
  boolean valid=r.nextBoolean();int variant=r.nextInt(valid?3:2);List<Integer> xs=new ArrayList<>();while(xs.size()<4){int x=r.nextInt(17)-8;if(!xs.contains(x))xs.add(x);}int[] y=new int[4];for(int i=0;i<4;i++)y[i]=r.nextInt(21)-10;
  if(valid&&variant==1){xs.set(3,xs.get(0));y[3]=y[0];}else if(valid&&variant==2){y[1]=y[0];y[2]=y[0];}else if(!valid){xs.set(1,xs.get(0));y[1]=y[0]+1+r.nextInt(4);if(variant==1){xs.set(2,xs.get(0));y[2]=y[1]+1+r.nextInt(4);}}
  List<Integer> order=new ArrayList<>(List.of(0,1,2,3));Collections.shuffle(order,r);double[] points=new double[8];for(int i=0;i<4;i++){int row=order.get(i);points[2*i]=xs.get(row);points[2*i+1]=y[row];}
  int maximum=maximumOutputs(points);String answer=maximum==1?"1":"0";Question q=new Question(s.id,"표의 대응이 함수인지 판단하세요.\n한 x에 서로 다른 y가 둘 이상 대응하면 함수가 아닙니다.","",answer);q.stepSupport=false;q.diagram=new StudyDiagram("mappingTable",points);q.choiceLabels.put("1","함수입니다");q.choiceLabels.put("0","함수가 아닙니다");
  q.studyGuide=new StudyGuide().transfer(false).step("각 x에 대응하는 서로 다른 y의 수 중 가장 큰 값을 쓰세요.","","",""+maximum).choice("각 x에 y가 하나씩 대응하면 함수입니다.",q.choiceLabels,answer);return q;
 }
 static int maximumOutputs(double[] points){Map<Double,Set<Double>> outputs=new LinkedHashMap<>();for(int i=0;i<points.length;i+=2)outputs.computeIfAbsent(points[i],x->new LinkedHashSet<>()).add(points[i+1]);return outputs.values().stream().mapToInt(Set::size).max().orElse(0);}
 private static Question square(Catalog.Skill s,Random r){
  int a=(1+r.nextInt(2))*(r.nextBoolean()?1:-1),b=r.nextInt(9)-4,x=r.nextInt(9)-4,squared=x*x,product=a*squared,y=product+b;String formula="y = "+a+"x² "+(b<0?"- "+(-b):"+ "+b);
  Question q=new Question(s.id,"제곱 함수식: "+formula+"\nx = "+x+"일 때 y를 구하세요.","("+a+")*("+x+")^2+("+b+")",""+y);q.stepSupport=false;q.labels=new String[]{"y값"};q.diagram=new StudyDiagram("functionSquareGraph",new double[]{a,b,x});
  q.studyGuide=new StudyGuide().transfer(false).step("x값을 제곱하세요.","("+x+")² = ","",""+squared).step("제곱한 값에 계수를 곱하세요.",a+" × "+squared+" = ","",""+product).step("곱한 값에 상수를 더하세요.","("+product+") + ("+b+") = ","",""+y);return q.withInputs(a,b,x);
 }
 static Map<Rational,String> errors(Question q){long a=q.choiceInputs[0].n.longValueExact(),b=q.choiceInputs[1].n.longValueExact(),x=q.choiceInputs[2].n.longValueExact(),y=a*x*x+b;Map<Rational,String> values=new LinkedHashMap<>();values.put(Rational.of(a*x+b),"x를 제곱하지 않음");values.put(Rational.of(2*a*x+b),"제곱 대신 2를 곱함");values.put(Rational.of(a*x*x),"상수를 빠뜨림");values.put(Rational.of(-a*x*x+b),"제곱한 값의 부호를 바꿈");for(int step:new int[]{-2,-1,1,2})values.put(Rational.of(y+step),"마지막 덧셈 계산 오류");return values;}
}
