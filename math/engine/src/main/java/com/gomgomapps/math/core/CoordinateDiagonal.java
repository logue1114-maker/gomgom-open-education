package com.gomgomapps.math.core;
import java.util.*;
/** Arbitrary oblique grid distances; exact square-root values, never decimal approximations. */
public final class CoordinateDiagonal {
 private CoordinateDiagonal(){}
 public static final String ID="coordinateGridDiagonal";
 public static final List<Catalog.Skill> SKILLS=List.of(new Catalog.Skill(ID,"격자에서 대각선 거리 구하기",9,2,2,"",ID,16,"coordinateGridDistance,pythagoras,rootSimplify","가로·세로 이동으로 만든 두 정사각형의 넓이를 더해 대각선 길이를 구한다."));
 public static boolean supports(String id){return ID.equals(id);}
 public static Question create(Catalog.Skill s,Random r){
  int ax=r.nextInt(17)-8,ay=r.nextInt(17)-8,bx,by;do{bx=r.nextInt(17)-8;}while(bx==ax);do{by=r.nextInt(17)-8;}while(by==ay);
  int dx=Math.abs(bx-ax),dy=Math.abs(by-ay),n=dx*dx+dy*dy,c=1;for(int k=2;k*k<=n;k++)if(n%(k*k)==0)c=k;int inside=n/(c*c);
  String prompt="격자 한 칸의 길이는 1입니다.\nA("+ax+", "+ay+"), B("+bx+", "+by+")\nAB의 길이를 가장 간단한 근호 꼴로 쓰세요. 정수이면 정수로 쓰세요.";
  Question q=new Question(s.id,prompt,"",Radical.parse("sqrt("+n+")").toString());q.kind="radical";q.stepSupport=false;q.labels=new String[]{"AB"};q.resultSymbol="AB";
  q.diagram=new StudyDiagram("coordinateDiagonal",new double[]{ax,ay,bx,by,0,0,0},"A","B");
  q.givenNumbers.put("ax",""+ax);q.givenNumbers.put("ay",""+ay);q.givenNumbers.put("bx",""+bx);q.givenNumbers.put("by",""+by);
  StudyGuide g=new StudyGuide().transfer(false);
  g.step("A에서 B까지 가로로 몇 칸인지 세세요.","가로 = "," 칸",""+dx).step("A에서 B까지 세로로 몇 칸인지 세세요.","세로 = "," 칸",""+dy);
  g.step("가로 이동을 한 변으로 하는 정사각형의 넓이를 구하세요.",dx+" × "+dx+" = ","",""+(dx*dx)).step("세로 이동을 한 변으로 하는 정사각형의 넓이를 구하세요.",dy+" × "+dy+" = ","",""+(dy*dy));
  g.step("직각삼각형의 빗변을 한 변으로 하는 정사각형의 넓이는 다른 두 정사각형 넓이의 합입니다. 두 넓이를 더하세요.",(dx*dx)+" + "+(dy*dy)+" = ","",""+n);
  g.step("이 넓이를 나누어떨어지게 하는 가장 큰 제곱수를 찾으세요.",n+" = □ × …\n□ = ","",""+(c*c)).step("근호 안에 남는 수를 구하세요.",n+" ÷ "+(c*c)+" = ","",""+inside).step("찾은 제곱수의 양의 제곱근을 구하세요.","√("+(c*c)+") = ","",""+c);
  q.studyGuide=g.radicalResult(7,6);return q;
 }
 public static void choices(Question q,Random r){
  int dx=(int)Math.abs(q.diagram.values[2]-q.diagram.values[0]),dy=(int)Math.abs(q.diagram.values[3]-q.diagram.values[1]),n=dx*dx+dy*dy;
  Radical answer=Radical.parse(q.answers[0]);Map<Radical,String> errors=new LinkedHashMap<>();
  errors.put(Radical.parse(""+dx),"가로 이동만 센 경우");errors.put(Radical.parse(""+dy),"세로 이동만 센 경우");errors.put(Radical.parse(""+(dx+dy)),"가로·세로 이동을 더한 경우");errors.put(Radical.parse("sqrt("+(n+1)+")"),"넓이를 하나 크게 계산한 경우");errors.put(Radical.parse("sqrt("+(n-1)+")"),"넓이를 하나 작게 계산한 경우");errors.put(answer.add(Radical.parse("1")),"길이를 하나 크게 계산한 경우");errors.remove(answer);
  for(int delta=1;delta<=8;delta++){
   errors.put(answer.coefficientError(delta),"근호 밖의 수를 크게 계산한 경우");errors.put(answer.coefficientError(-delta),"근호 밖의 수를 작게 계산한 경우");
   errors.put(Radical.parse("sqrt("+(n+delta)+")"),"정사각형 넓이의 합을 크게 계산한 경우");if(n>delta)errors.put(Radical.parse("sqrt("+(n-delta)+")"),"정사각형 넓이의 합을 작게 계산한 경우");
  }
  errors.entrySet().removeIf(e->!e.getKey().sameShape(answer)||e.getKey().choiceMagnitude()<=0);
  List<Radical> below=new ArrayList<>(),above=new ArrayList<>();for(Radical v:errors.keySet()){if(v.choiceMagnitude()<answer.choiceMagnitude())below.add(v);else above.add(v);}Collections.shuffle(below,r);Collections.shuffle(above,r);
  List<Integer> ranks=new ArrayList<>();for(int rank=0;rank<4;rank++)if(below.size()>=rank&&above.size()>=3-rank)ranks.add(rank);int rank=ranks.get(r.nextInt(ranks.size()));
  List<Radical> values=new ArrayList<>(below.subList(0,rank));values.addAll(above.subList(0,3-rank));values.add(answer);Collections.shuffle(values,r);
  for(Radical v:values){if(v.equals(answer))q.correctChoice=q.choices.size();q.choices.add(v.toString());q.distractorReasons.add(v.equals(answer)?"정답":errors.get(v));}
 }
}
