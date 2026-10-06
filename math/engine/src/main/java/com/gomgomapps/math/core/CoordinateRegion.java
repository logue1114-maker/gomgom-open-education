package com.gomgomapps.math.core;
import java.util.*;
/** Coordinate applications: count equal grid intervals and unit squares before calculating. */
public final class CoordinateRegion {
 private CoordinateRegion(){}
 public static final String PERIM="coordinateGridPerimeter",AREA="coordinateGridArea";
 public static final List<Catalog.Skill> SKILLS=List.of(
  new Catalog.Skill(PERIM,"격자에서 둘레 구하기",9,2,2,"",PERIM,16,CoordinateGrid.DIST,"네 변의 칸 수를 세고 더해 둘레를 구한다."),
  new Catalog.Skill(AREA,"격자에서 넓이 구하기",9,2,2,"",AREA,16,CoordinateGrid.DIST,"한 줄의 칸 수와 줄 수를 세어 넓이를 구한다."));
 public static boolean supports(String id){return PERIM.equals(id)||AREA.equals(id);}
 public static Question create(Catalog.Skill skill,Random r){
  int width=1+r.nextInt(12),height=1+r.nextInt(12),left=-8+r.nextInt(17-width),bottom=-8+r.nextInt(17-height),right=left+width,top=bottom+height;
  boolean area=AREA.equals(skill.id);String prompt="격자 한 칸의 길이는 1입니다.\n직사각형 ABCD\nA("+left+", "+bottom+"), B("+right+", "+bottom+")\nC("+right+", "+top+"), D("+left+", "+top+")\n"+(area?"직사각형 안의 단위 정사각형은 몇 개일까요?":"네 변의 길이를 모두 더하면 얼마일까요?");
  Question q=new Question(skill.id,prompt,"",String.valueOf(area?width*height:2*(width+height)));q.stepSupport=false;q.labels=new String[]{area?"넓이":"둘레"};q.resultSymbol=area?"S":"P";
  q.diagram=new StudyDiagram("coordinateRectangle",new double[]{left,bottom,right,top},"A","B","C","D");
  q.givenNumbers.put("left",""+left);q.givenNumbers.put("bottom",""+bottom);q.givenNumbers.put("right",""+right);q.givenNumbers.put("top",""+top);
  q.studyGuide=new StudyGuide().transfer(false);
  if(area)q.studyGuide.step("한 줄에 놓인 칸 수를 세세요.","한 줄 = "," 칸",String.valueOf(width)).step("세로로 몇 줄인지 세세요.","줄 수 = "," 줄",String.valueOf(height)).step("한 줄의 칸 수를 줄 수만큼 모으세요.",width+" × "+height+" = ","",String.valueOf(width*height));
  else{for(String side:new String[]{"AB","BC","CD","DA"})q.studyGuide.step(side+"의 칸 수를 세세요.",side+" = "," 칸",String.valueOf(side.equals("AB")||side.equals("CD")?width:height));q.studyGuide.step("네 변의 칸 수를 모두 더하세요.",width+" + "+height+" + "+width+" + "+height+" = ","",String.valueOf(2*(width+height)));}
  return q;
 }
 public static void choices(Question q,Random r){
  int w=(int)(q.diagram.values[2]-q.diagram.values[0]),h=(int)(q.diagram.values[3]-q.diagram.values[1]),answer=Integer.parseInt(q.answers[0]);Map<Integer,String> wrong=new LinkedHashMap<>();
  wrong.put(w+h,"서로 이웃한 두 변만 더한 경우");wrong.put(w*h,"단위 정사각형 수와 둘레를 혼동한 경우");wrong.put(2*(w+h),"둘레와 단위 정사각형 수를 혼동한 경우");wrong.put((w+1)*h,"격자 점 수를 칸 수로 센 경우");wrong.put(answer-1,"하나 적게 센 경우");wrong.put(answer+1,"하나 더 센 경우");wrong.put(answer+2,"두 개 더 센 경우");wrong.remove(answer);wrong.entrySet().removeIf(e->e.getKey()<=0);
  List<Integer> pool=new ArrayList<>(wrong.keySet());Collections.shuffle(pool,r);List<Integer> values=new ArrayList<>(pool.subList(0,3));values.add(answer);Collections.shuffle(values,r);for(int v:values){if(v==answer)q.correctChoice=q.choices.size();q.choices.add(""+v);q.distractorReasons.add(v==answer?"정답":wrong.get(v));}
 }
}
