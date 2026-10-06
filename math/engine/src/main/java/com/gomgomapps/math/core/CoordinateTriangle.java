package com.gomgomapps.math.core;
import java.util.*;
/** Public right-triangle corners: congruent rectangle halves and exact grid-side perimeter. */
public final class CoordinateTriangle {
 private CoordinateTriangle(){}
 public static final String AREA="coordinateTriangleArea",PERIM="coordinateTrianglePerimeter";
 public static final List<Catalog.Skill> SKILLS=List.of(
  new Catalog.Skill(AREA,"격자 삼각형의 넓이",9,2,2,"",AREA,16,CoordinateRegion.AREA,"같은 직각삼각형 두 개가 만드는 직사각형의 넓이를 반으로 나눈다."),
  new Catalog.Skill(PERIM,"격자 삼각형의 둘레",9,2,2,"",PERIM,16,CoordinateDiagonal.ID,"두 직각변의 칸 수와 빗변의 정확한 길이를 더한다."));
 public static boolean supports(String id){return AREA.equals(id)||PERIM.equals(id);}
 public static Question create(Catalog.Skill s,Random r){
  int w=2+r.nextInt(11),h=2+r.nextInt(11),turn=r.nextInt(4);boolean reflect=r.nextBoolean();int[][] p={{0,0},{w,0},{0,h}};
  for(int[] v:p){if(reflect)v[0]=-v[0];for(int i=0;i<turn;i++){int x=v[0];v[0]=-v[1];v[1]=x;}}
  int minX=Arrays.stream(p).mapToInt(v->v[0]).min().orElseThrow(),maxX=Arrays.stream(p).mapToInt(v->v[0]).max().orElseThrow(),minY=Arrays.stream(p).mapToInt(v->v[1]).min().orElseThrow(),maxY=Arrays.stream(p).mapToInt(v->v[1]).max().orElseThrow();int tx=-8-minX+r.nextInt(17-(maxX-minX)),ty=-8-minY+r.nextInt(17-(maxY-minY));for(int[] v:p){v[0]+=tx;v[1]+=ty;}
  boolean area=AREA.equals(s.id);String prompt="격자 한 칸의 길이는 1입니다.\n직각삼각형 ABC\nA("+p[0][0]+", "+p[0][1]+"), B("+p[1][0]+", "+p[1][1]+")\nC("+p[2][0]+", "+p[2][1]+")\n"+(area?"삼각형 ABC의 넓이를 구하세요.":"삼각형 ABC의 둘레를 정확한 값으로 쓰세요.");
  int n=w*w+h*h,c=1;for(int k=2;k*k<=n;k++)if(n%(k*k)==0)c=k;int inside=n/(c*c);String answer=area?Rational.of(w*h).div(Rational.of(2)).toString():Radical.parse((w+h)+"+sqrt("+n+")").toString();Question q=new Question(s.id,prompt,"",answer);q.kind=area?"number":"radical";q.stepSupport=false;q.labels=new String[]{area?"넓이":"둘레"};q.resultSymbol=area?"S":"P";double[] values=new double[6];for(int i=0;i<3;i++){values[2*i]=p[i][0];values[2*i+1]=p[i][1];q.givenNumbers.put(""+(char)('A'+i)+"x",""+p[i][0]);q.givenNumbers.put(""+(char)('A'+i)+"y",""+p[i][1]);}q.diagram=new StudyDiagram("coordinateTriangle",values,"A","B","C");
  StudyGuide g=new StudyGuide().transfer(false);g.step("AB의 칸 수를 세세요.","AB = "," 칸",""+w).step("AC의 칸 수를 세세요.","AC = "," 칸",""+h);
  if(area){g.step("같은 삼각형 두 개가 만드는 직사각형의 넓이를 구하세요.","AB × AC = ","",""+(w*h)).step("직사각형을 똑같이 나눈 삼각형의 개수를 쓰세요.","나눈 개수 = ","","2");q.studyGuide=g.fractionResult(2,3);}
  else{g.step("AB를 한 변으로 하는 정사각형의 넓이를 구하세요.","AB × AB = ","",""+(w*w)).step("AC를 한 변으로 하는 정사각형의 넓이를 구하세요.","AC × AC = ","",""+(h*h)).step("BC를 한 변으로 하는 정사각형의 넓이는 다른 두 정사각형 넓이의 합입니다. 두 넓이를 더하세요.",(w*w)+" + "+(h*h)+" = ","",""+n).step("이 넓이를 나누어떨어지게 하는 가장 큰 제곱수를 찾으세요.",n+" = □ × …\n□ = ","",""+(c*c)).step("근호 안에 남는 수를 구하세요.",n+" ÷ "+(c*c)+" = ","",""+inside).step("찾은 제곱수의 양의 제곱근을 구하세요.","√("+(c*c)+") = ","",""+c).step("둘레는 AB와 AC의 길이에 빗변 BC의 길이를 더한 값입니다. AB와 AC의 길이를 더하세요.","AB + AC = ","",""+(w+h));q.studyGuide=g.radicalSumResult(8,7,6);}
  return q;
 }
 /** The perimeter frame retains the learner's two entered integer lengths without calculating their sum. */
 static boolean integerPerimeterSum(Question q,String raw){if(!PERIM.equals(q.skillId))return false;String s=Expression.normalize(raw);return s.matches("(?:\\([0-9]+\\)|[0-9]+)\\+(?:\\([0-9]+\\)|[0-9]+)");}
 public static void choices(Question q,Random r){
  Radical answer=Radical.parse(q.answers[0]);Map<Radical,String> errors=new LinkedHashMap<>();for(int d=1;d<=6;d++){errors.put(answer.add(Radical.parse(""+d)),"단위 수를 크게 계산한 경우");errors.put(answer.add(Radical.parse(""+(-d))),"단위 수를 작게 계산한 경우");}errors.entrySet().removeIf(v->!v.getKey().sameShape(answer)||v.getKey().choiceMagnitude()<=0);List<Radical> below=new ArrayList<>(),above=new ArrayList<>();for(Radical v:errors.keySet()){if(v.choiceMagnitude()<answer.choiceMagnitude())below.add(v);else above.add(v);}Collections.shuffle(below,r);Collections.shuffle(above,r);List<Integer> ranks=new ArrayList<>();for(int i=0;i<4;i++)if(below.size()>=i&&above.size()>=3-i)ranks.add(i);int rank=ranks.get(r.nextInt(ranks.size()));List<Radical> options=new ArrayList<>(below.subList(0,rank));options.addAll(above.subList(0,3-rank));options.add(answer);Collections.shuffle(options,r);for(Radical v:options){if(v.equals(answer))q.correctChoice=q.choices.size();q.choices.add(AREA.equals(q.skillId)?v.rational().toString():v.toString());q.distractorReasons.add(v.equals(answer)?"정답":errors.get(v));}
 }
}
