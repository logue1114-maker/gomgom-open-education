package com.gomgomapps.math.core;
import java.util.*;

/** Public grid endpoints; midpoint walking and comparison with a supplied reference segment. */
public final class CoordinateGrid {
 private CoordinateGrid(){}
 public static final String MID="coordinateGridMidpoint",DIST="coordinateGridDistance";
 public static final List<Catalog.Skill> SKILLS=List.of(
  new Catalog.Skill(MID,"격자에서 중점 찾기",9,2,2,"",MID,16,"sec_coordinate_move","두 끝에서 같은 만큼 이동해 선분의 가운데 점을 찾는다."),
  new Catalog.Skill(DIST,"격자에서 두 점 사이의 거리",9,2,2,"",DIST,16,"sec_coordinate_move,proportion","같은 간격을 세거나 길이가 주어진 기준 선분과 비교한다."));
 public static boolean supports(String id){return id.equals(MID)||id.equals(DIST)||CoordinateRegion.supports(id)||CoordinateDiagonal.supports(id)||CoordinatePolygon.supports(id);}
 public static void choices(Question q,Random r){
  int answer=Integer.parseInt(q.answers[0]);
  int dx=(int)Math.abs(q.diagram.values[2]-q.diagram.values[0]),dy=(int)Math.abs(q.diagram.values[3]-q.diagram.values[1]);
  Map<Integer,String> errors=new LinkedHashMap<>();
  errors.put(dx,"가로 이동만 센 경우");errors.put(dy,"세로 이동만 센 경우");
  if(dx>0&&dy>0)errors.put(dx+dy,"가로와 세로 이동을 더한 경우");
  errors.put(answer-1,"칸 수를 하나 적게 센 경우");errors.put(answer+1,"칸 수를 하나 더 센 경우");errors.put(answer+2,"칸 수를 두 개 더 센 경우");
  errors.remove(answer);errors.entrySet().removeIf(e->e.getKey()<=0);
  if(errors.size()<3)errors.put(answer+3,"칸 수를 세 개 더 센 경우");
  List<Integer> pool=new ArrayList<>(errors.keySet());Collections.shuffle(pool,r);
  List<Integer> options=new ArrayList<>(pool.subList(0,3));options.add(answer);Collections.shuffle(options,r);
  for(int v:options){if(v==answer)q.correctChoice=q.choices.size();q.choices.add(String.valueOf(v));q.distractorReasons.add(v==answer?"정답":errors.get(v));}
 }
 private static int between(Random r,int lo,int hi){return lo+r.nextInt(hi-lo+1);}
 private static int start(Random r,int delta){return between(r,-8-Math.min(0,delta),8-Math.max(0,delta));}
 public static Question create(Catalog.Skill s,Random r){
  if(CoordinateDiagonal.supports(s.id))return CoordinateDiagonal.create(s,r);
  if(CoordinatePolygon.supports(s.id))return CoordinatePolygon.create(s,r);
  if(CoordinateRegion.supports(s.id))return CoordinateRegion.create(s,r);
  boolean midpoint=s.id.equals(MID);int dx,dy,mode=r.nextInt(3),multiple=1+r.nextInt(2);
  if(midpoint){dx=2*between(r,-6,6);dy=2*between(r,-6,6);if(dx==0&&dy==0)dx=2;}
  else if(mode<2){int length=between(r,1,12)*(r.nextBoolean()?1:-1);dx=mode==0?length:0;dy=mode==1?length:0;}
  else{dx=3*multiple*(r.nextBoolean()?1:-1);dy=4*multiple*(r.nextBoolean()?1:-1);}
  int ax=start(r,dx),ay=start(r,dy),bx=ax+dx,by=ay+dy;String coordinates="A("+ax+", "+ay+"), B("+bx+", "+by+")";
  boolean reference=!midpoint&&mode==2;String prompt="격자 한 칸의 길이는 1입니다.\n"+coordinates+"\n"+(reference?"기준 선분: 가로 3칸 · 세로 4칸 · 길이 5\n":"")+(midpoint?"AB의 중점 M의 좌표는?":"AB의 길이는?");
  Question q=midpoint?new Question(s.id,prompt,"",String.valueOf(ax+dx/2),String.valueOf(ay+dy/2)):new Question(s.id,prompt,"",String.valueOf(reference?5*multiple:Math.abs(dx)+Math.abs(dy)));
  q.stepSupport=false;q.labels=midpoint?new String[]{"x","y"}:new String[]{"AB"};q.resultSymbol=midpoint?"M":"AB";
  q.diagram=new StudyDiagram("coordinateGrid",new double[]{ax,ay,bx,by,reference?3:0,reference?4:0,reference?5:0},"A","B");
  q.givenNumbers.put("ax",""+ax);q.givenNumbers.put("ay",""+ay);q.givenNumbers.put("bx",""+bx);q.givenNumbers.put("by",""+by);
  q.studyGuide=new StudyGuide().transfer(false);
  if(midpoint){q.studyGuide.step("A에서 B까지 가로 이동의 절반을 쓰세요.","가로 이동 = "," 칸",String.valueOf(dx/2)).step("A의 x좌표에서 그만큼 이동하세요.",ax+" + ("+dx/2+") = ","",String.valueOf(ax+dx/2)).step("A에서 B까지 세로 이동의 절반을 쓰세요.","세로 이동 = "," 칸",String.valueOf(dy/2)).step("A의 y좌표에서 그만큼 이동하세요.",ay+" + ("+dy/2+") = ","",String.valueOf(ay+dy/2));}
  else if(reference){q.studyGuide.step("기준 선분과 비교해 가로·세로 이동의 배율을 쓰세요.","배율 = "," 배",String.valueOf(multiple)).step("기준 길이에 같은 배율을 곱하세요.","5 × "+multiple+" = ","",String.valueOf(5*multiple));}
  else{q.studyGuide.choice("A에서 B로 이동하는 방향을 고르세요.",Map.of("h","가로","v","세로","both","둘 다"),dx==0?"v":"h").step("두 점 사이의 칸 수를 세세요.","AB = ","",String.valueOf(Math.abs(dx)+Math.abs(dy)));}
  if(!midpoint)q.withInputs(Math.abs(dx),Math.abs(dy),reference?5:1);return q;
 }
}
