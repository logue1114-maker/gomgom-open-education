package com.gomgomapps.math.core;
import java.util.*;
/** Public altitude splits an axis-base triangle into two right-triangle halves. */
public final class CoordinateAltitudeArea {
 private CoordinateAltitudeArea(){}
 public static final String ID="coordinateAltitudeArea";
 public static final List<Catalog.Skill> SKILLS=List.of(new Catalog.Skill(ID,"수선으로 나눈 삼각형 넓이",9,2,2,"",ID,16,CoordinateTriangle.AREA,"두 직각삼각형의 넓이를 더하거나 빼서 삼각형의 넓이를 구한다."));
 public static Question create(Catalog.Skill s,Random r){
  int w=4+r.nextInt(9),placement=r.nextInt(3),t=placement==0?1+r.nextInt(w-1):placement==1?-(1+r.nextInt(4)):w+1+r.nextInt(4),h=2+r.nextInt(11);while(h*h==t*(w-t))h=2+r.nextInt(11);int[][] p={{0,0},{w,0},{t,h},{t,0}};int turn=r.nextInt(4);boolean reflect=r.nextBoolean();for(int[] v:p){if(reflect)v[0]=-v[0];for(int k=0;k<turn;k++){int x=v[0];v[0]=-v[1];v[1]=x;}}
  int minX=Arrays.stream(p).mapToInt(v->v[0]).min().orElseThrow(),maxX=Arrays.stream(p).mapToInt(v->v[0]).max().orElseThrow(),minY=Arrays.stream(p).mapToInt(v->v[1]).min().orElseThrow(),maxY=Arrays.stream(p).mapToInt(v->v[1]).max().orElseThrow();int tx=-8-minX+r.nextInt(17-(maxX-minX)),ty=-8-minY+r.nextInt(17-(maxY-minY));for(int[] v:p){v[0]+=tx;v[1]+=ty;}
  String prompt="격자 한 칸의 길이는 1입니다.\n삼각형 ABC\nA("+p[0][0]+", "+p[0][1]+"), B("+p[1][0]+", "+p[1][1]+")\nC("+p[2][0]+", "+p[2][1]+"), D("+p[3][0]+", "+p[3][1]+")\nD는 C에서 AB에 내린 수선의 발입니다.\n삼각형 ABC의 넓이를 구하세요.";
  Question q=new Question(s.id,prompt,"",Rational.of(w*h).div(Rational.of(2)).toString());q.kind="number";q.stepSupport=false;q.labels=new String[]{"넓이"};q.resultSymbol="S";double[] values=new double[8];for(int i=0;i<4;i++){values[2*i]=p[i][0];values[2*i+1]=p[i][1];q.givenNumbers.put(""+(char)('A'+i)+"x",""+p[i][0]);q.givenNumbers.put(""+(char)('A'+i)+"y",""+p[i][1]);}q.diagram=new StudyDiagram("coordinateAltitudeTriangle",values,"A","B","C","D");
  int a=Math.abs(t),b=Math.abs(w-t),large=Math.max(a,b)*h,small=Math.min(a,b)*h;boolean exterior=t<0||t>w;
  q.studyGuide=new StudyGuide().transfer(false).step("AD의 칸 수를 세세요.","AD = "," 칸",""+a).step("DB의 칸 수를 세세요.","DB = "," 칸",""+b).step("두 삼각형의 높이 CD의 칸 수를 세세요.","CD = "," 칸",""+h).step("삼각형 ADC를 포함한 직사각형 ADCE의 넓이를 구하세요.","AD × CD = ","",""+(a*h)).step("삼각형 DBC를 포함한 직사각형 DBFC의 넓이를 구하세요.","DB × CD = ","",""+(b*h)).step(exterior?"큰 직사각형의 넓이에서 작은 직사각형의 넓이를 빼세요.":"두 직사각형의 넓이를 더하세요.",exterior?large+" − "+small+" = ":(a*h)+" + "+(b*h)+" = ","",""+(w*h)).step(exterior?"각 직각삼각형은 해당 직사각형의 절반입니다. 넓이의 차를 나눌 수를 쓰세요.":"각 직각삼각형은 해당 직사각형의 절반입니다. 넓이의 합을 나눌 수를 쓰세요.","나눌 수 = ","","2").fractionResult(5,6);return q;
 }
}
