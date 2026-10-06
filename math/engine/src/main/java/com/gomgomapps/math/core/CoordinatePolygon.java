package com.gomgomapps.math.core;
import java.util.*;
/** Six public corners of a concave orthogonal polygon; rotated/reflected grid applications. */
public final class CoordinatePolygon {
 private CoordinatePolygon(){}
 public static final String PERIM="coordinatePolygonPerimeter",AREA="coordinatePolygonArea";
 public static final List<Catalog.Skill> SKILLS=List.of(
  new Catalog.Skill(PERIM,"격자 다각형의 둘레",9,2,2,"",PERIM,16,CoordinateRegion.PERIM,"여섯 변의 칸 수를 더해 오목한 다각형의 둘레를 구한다."),
  new Catalog.Skill(AREA,"격자 다각형의 넓이",9,2,2,"",AREA,16,CoordinateRegion.AREA,"다각형을 두 직사각형으로 나누고 넓이를 더한다."));
 public static boolean supports(String id){return PERIM.equals(id)||AREA.equals(id);}
 public static Question create(Catalog.Skill s,Random r){
  int w=4+r.nextInt(9),h=4+r.nextInt(9),cut=2+r.nextInt(w-3),low=2+r.nextInt(h-3),turn=r.nextInt(4);boolean reflect=r.nextBoolean();
  int[][] p={{0,0},{w,0},{w,low},{cut,low},{cut,h},{0,h},{0,low}};
  for(int[] v:p){if(reflect)v[0]=-v[0];for(int i=0;i<turn;i++){int x=v[0];v[0]=-v[1];v[1]=x;}}
  int minX=Arrays.stream(p).mapToInt(v->v[0]).min().orElseThrow(),maxX=Arrays.stream(p).mapToInt(v->v[0]).max().orElseThrow(),minY=Arrays.stream(p).mapToInt(v->v[1]).min().orElseThrow(),maxY=Arrays.stream(p).mapToInt(v->v[1]).max().orElseThrow();
  int tx=-8-minX+r.nextInt(17-(maxX-minX)),ty=-8-minY+r.nextInt(17-(maxY-minY));for(int[] v:p){v[0]+=tx;v[1]+=ty;}
  int[] lengths=new int[6];for(int i=0;i<6;i++)lengths[i]=Math.abs(p[(i+1)%6][0]-p[i][0])+Math.abs(p[(i+1)%6][1]-p[i][1]);
  boolean area=AREA.equals(s.id);int a=lengths[0]*lengths[1],b=lengths[3]*lengths[4],answer=area?a+b:Arrays.stream(lengths).sum();StringBuilder prompt=new StringBuilder("격자 한 칸의 길이는 1입니다.\n다각형 ABCDEF\n");
  for(int i=0;i<6;i++){prompt.append((char)('A'+i)).append('(').append(p[i][0]).append(", ").append(p[i][1]).append(')').append(i%2==0?", ":"\n");}
  prompt.append(area?"도형 안의 단위 정사각형 수를 구하세요.":"여섯 변의 길이를 모두 더하세요.");Question q=new Question(s.id,prompt.toString(),"",""+answer);q.stepSupport=false;q.labels=new String[]{area?"넓이":"둘레"};q.resultSymbol=area?"S":"P";
  double[] values=new double[14];for(int i=0;i<7;i++){values[2*i]=p[i][0];values[2*i+1]=p[i][1];}q.diagram=new StudyDiagram("coordinatePolygon",values,"A","B","C","D","E","F");
  for(int i=0;i<6;i++){q.givenNumbers.put(""+(char)('A'+i)+"x",""+p[i][0]);q.givenNumbers.put(""+(char)('A'+i)+"y",""+p[i][1]);}
  StudyGuide guide=new StudyGuide().transfer(false);String[] sides={"AB","BC","CD","DE","EF","FA"};
  if(area){guide.step("AB의 칸 수를 세세요.","AB = "," 칸",""+lengths[0]).step("BC의 칸 수를 세세요.","BC = "," 칸",""+lengths[1]).step("점선으로 나눈 첫 번째 직사각형의 넓이를 구하세요.","AB × BC = ","",""+a).step("EF의 칸 수를 세세요.","EF = "," 칸",""+lengths[4]).step("DE의 칸 수를 세세요.","DE = "," 칸",""+lengths[3]).step("점선으로 나눈 두 번째 직사각형의 넓이를 구하세요.","EF × DE = ","",""+b).step("두 직사각형의 넓이를 더하세요.",a+" + "+b+" = ","",""+answer);}
  else{for(int i=0;i<6;i++)guide.step(sides[i]+"의 칸 수를 세세요.",sides[i]+" = "," 칸",""+lengths[i]);guide.step("여섯 변의 길이를 모두 더하세요.","AB + BC + CD + DE + EF + FA = ","",""+answer);}
  q.studyGuide=guide;return q;
 }
 public static void choices(Question q,Random r){
  double[] p=q.diagram.values;int[] edges=new int[6];for(int i=0;i<6;i++)edges[i]=(int)(Math.abs(p[(i+1)%6*2]-p[i*2])+Math.abs(p[(i+1)%6*2+1]-p[i*2+1]));int a=edges[0]*edges[1],b=edges[3]*edges[4],answer=Integer.parseInt(q.answers[0]);Map<Integer,String> errors=new LinkedHashMap<>();
  errors.put(a,"첫 번째 직사각형의 넓이만 구한 경우");errors.put(b,"두 번째 직사각형의 넓이만 구한 경우");errors.put(a+b,"넓이와 둘레를 혼동한 경우");errors.put(Arrays.stream(edges).sum(),"둘레와 넓이를 혼동한 경우");errors.put(Math.abs(a-b),"두 넓이를 더하지 않고 뺀 경우");errors.put(answer-edges[2]-edges[3],"오목한 부분의 두 변을 빠뜨린 경우");for(int i=1;i<=4;i++){errors.put(answer+i,"칸 수를 크게 계산한 경우");errors.put(answer-i,"칸 수를 작게 계산한 경우");}errors.remove(answer);errors.entrySet().removeIf(v->v.getKey()<=0);List<Integer> pool=new ArrayList<>(errors.keySet());Collections.shuffle(pool,r);List<Integer> values=new ArrayList<>(pool.subList(0,3));values.add(answer);Collections.shuffle(values,r);for(int value:values){if(value==answer)q.correctChoice=q.choices.size();q.choices.add(""+value);q.distractorReasons.add(value==answer?"정답":errors.get(value));}
 }
}
