package com.gomgomapps.math.core;
import java.util.*;
/** Two paths above the same public baseline form trapezoids whose area difference is ABC. */
public final class CoordinateObliqueArea {
 private CoordinateObliqueArea(){}
 public static final String ID="coordinateObliqueArea";
 public static final List<Catalog.Skill> SKILLS=List.of(new Catalog.Skill(ID,"격자에서 기울어진 삼각형 넓이",9,2,2,"",ID,16,CoordinateAltitudeArea.ID,"같은 기준선에서 만든 삼각형과 사다리꼴의 넓이를 비교한다."));
 public static Question create(Catalog.Skill s,Random r){
  int[][] p;int cross;do{p=new int[3][2];for(int[] v:p){v[0]=r.nextInt(17)-8;v[1]=r.nextInt(17)-8;}Arrays.sort(p,Comparator.comparingInt(v->v[0]));cross=0;for(int i=0;i<3;i++)cross+=p[i][0]*p[(i+1)%3][1]-p[(i+1)%3][0]*p[i][1];}while(p[0][0]==p[1][0]||p[1][0]==p[2][0]||p[0][1]==p[1][1]||p[0][1]==p[2][1]||p[1][1]==p[2][1]||Math.abs(cross)<8);
  int base=Math.min(p[0][1],Math.min(p[1][1],p[2][1])),u=p[1][0]-p[0][0],v=p[2][0]-p[1][0],w=u+v,a=p[0][1]-base,b=p[1][1]-base,c=p[2][1]-base,ab=u*(a+b),bc=v*(b+c),ac=w*(a+c),sum=ab+bc,n=Math.abs(sum-ac);
  String prompt="격자 한 칸의 길이는 1입니다.\n삼각형 ABC\nA("+p[0][0]+", "+p[0][1]+"), B("+p[1][0]+", "+p[1][1]+"), C("+p[2][0]+", "+p[2][1]+")\n기준선: y = "+base+"\nD, E, F는 각각 A, B, C에서 기준선에 내린 수선의 발입니다.\n삼각형 ABC의 넓이를 구하세요.";
  Question q=new Question(s.id,prompt,"",Rational.of(n).div(Rational.of(2)).toString());q.kind="number";q.stepSupport=false;q.labels=new String[]{"넓이"};q.resultSymbol="S";double[] values=new double[6];for(int i=0;i<3;i++)for(int k=0;k<2;k++)values[2*i+k]=p[i][k];q.diagram=new StudyDiagram("coordinateObliqueTriangle",values,"A","B","C");
  StudyGuide g=new StudyGuide().transfer(false);String[] widths={"DE","EF","DF"};int[] counts={u,v,w};for(int i=0;i<3;i++)g.step(widths[i]+"의 칸 수를 세세요.",widths[i]+" = "," 칸",""+counts[i]);String[] heights={"AD","BE","CF"};int[] hs={a,b,c};for(int i=0;i<3;i++)g.step(heights[i]+"의 칸 수를 세세요. 두 점이 겹치면 0입니다.",heights[i]+" = "," 칸",""+hs[i]);
  g.step("AD와 BE를 더하세요.","AD + BE = ","",""+(a+b)).step("두 높이의 합에 간격 DE를 곱하세요. ABED 부분 넓이의 두 배입니다.",(a+b)+" × "+u+" = ","",""+ab).step("BE와 CF를 더하세요.","BE + CF = ","",""+(b+c)).step("두 높이의 합에 간격 EF를 곱하세요. BCFE 부분 넓이의 두 배입니다.",(b+c)+" × "+v+" = ","",""+bc).step("AD와 CF를 더하세요.","AD + CF = ","",""+(a+c)).step("두 높이의 합에 간격 DF를 곱하세요. ACFD 부분 넓이의 두 배입니다.",(a+c)+" × "+w+" = ","",""+ac).step("ABED와 BCFE 부분 넓이의 두 배를 더하세요.",ab+" + "+bc+" = ","",""+sum).step("합친 두 부분과 ACFD 부분을 비교하세요. 큰 값에서 작은 값을 빼세요.",Math.max(sum,ac)+" − "+Math.min(sum,ac)+" = ","",""+n).step("넓이의 두 배를 구했습니다. 넓이를 구하려면 나눌 수는 얼마인가요?","나눌 수 = ","","2");q.studyGuide=g.fractionResult(13,14);return q;
 }
}
