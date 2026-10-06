package com.gomgomapps.math.core;
import java.util.*;

/** Ruler/compass actions executed from public side length, not answer metadata. */
public final class PolygonConstruction {
 private PolygonConstruction(){}
 public static final String ID="regularPolygonConstruction";
 public static final List<Catalog.Skill> SKILLS=List.of(new Catalog.Skill(ID,"정다각형의 작도 순서",9,2,2,"",ID,100,"sec_polygon_exterior","주어진 변의 길이를 유지하며 자와 컴퍼스로 점과 선분을 만든다."));
 public static boolean selected(Question q){return q!=null&&q.kind.equals("polygonConstruction")&&q.skillId.equals(ID);}
 public static Question create(Catalog.Skill skill,Random random){return create(skill,random.nextBoolean()?3:6,2+random.nextInt(50));}
 static Question create(Catalog.Skill skill,int sides,int length){
  if((sides!=3&&sides!=6)||length<=0)throw new IllegalArgumentException("Constructible polygon and positive side required");
  Question q=new Question(skill.id,"한 변의 길이: "+length+" cm\n"+(sides==3?"정삼각형":"정육각형")+"을 자와 컴퍼스로 만드는 순서를 완성하세요.","",new String[5]);
  q.answers=sides==3?new String[]{"segment","circleA","circleB","intersection","join"}:new String[]{"segment","circleA","walk","closure","join"};q.kind="polygonConstruction";q.stepSupport=false;q.labels=new String[]{"1단계","2단계","3단계","4단계","5단계"};
  q.givenNumbers.put("sides",String.valueOf(sides));q.givenNumbers.put("side",String.valueOf(length));q.diagram=new StudyDiagram("polygonConstruction",new double[]{sides,length,0});return q;
 }
 /** Finite public supply: both figures for each side length, no answer-position cycling. */
 static Question next(Catalog.Skill skill,Random random,Map<String,Integer> recent){
  List<Question> all=new ArrayList<>();for(int n:new int[]{3,6})for(int length=2;length<=51;length++)all.add(create(skill,n,length));Collections.shuffle(all,random);
  Question oldest=null;int age=Integer.MAX_VALUE;for(Question q:all){Integer seen=recent.get(q.signature());if(seen==null)return q;if(seen<age){age=seen;oldest=q;}}return oldest;
 }
 public static Map<String,String> options(Question q){
  if(!selected(q))throw new IllegalArgumentException("Construction question required");
  int length=Integer.parseInt(q.givenNumbers.get("side"));LinkedHashMap<String,String> out=new LinkedHashMap<>();
  out.put("segment","자: AB를 "+length+" cm로 긋기");out.put("circleA","컴퍼스: 중심 A, 반지름 AB인 원 그리기");
  if(sides(q)==3){out.put("circleB","컴퍼스: 중심 B, 반지름 AB인 원 그리기");out.put("intersection","두 원의 위쪽 교점을 C로 표시하기");out.put("join","자: AC와 BC 잇기");}
  else{out.put("walk","컴퍼스: B부터 원 위에 AB 길이를 5번 옮겨 점 표시하기");out.put("closure","컴퍼스: 마지막 점에서 AB 길이를 옮겨 B에 닿는지 확인하기");out.put("join","자: 원 위의 이웃한 여섯 점 잇기");}
  out.put("half","컴퍼스: 중심 A, 반지름 AB의 절반인 원 그리기");out.put("connectEarly","자: 아직 표시하지 않은 점 잇기");return out;
 }
 private static int sides(Question q){return Integer.parseInt(q.givenNumbers.get("sides"));}
 public static final class Execution {
  public final int errorIndex,stage;public final boolean complete;public final double[][] vertices;
  Execution(int error,int stage,boolean complete,double[][] vertices){this.errorIndex=error;this.stage=stage;this.complete=complete;this.vertices=vertices;}
 }
 public static Execution execute(Question q,List<String> actions){
  if(!selected(q))throw new IllegalArgumentException("Construction question required");
  boolean base=false,a=false,b=false,points=false,closed=false,joined=false;int stage=0;double[][] vertices=new double[0][];
  double length=Double.parseDouble(q.givenNumbers.get("side"));int n=sides(q);
  for(int i=0;i<actions.size();i++){
   String action=actions.get(i);boolean valid;
   switch(action){
    case "segment":valid=!base&&!a&&!b&&!points;base|=valid;break;
    case "circleA":valid=base&&!a&&!points;a|=valid;break;
    case "circleB":valid=n==3&&base&&!b&&!points;b|=valid;break;
    case "intersection":valid=n==3&&a&&b&&!points;if(valid){points=true;vertices=new double[][]{{0,0},{length,0},{length/2,Math.sqrt(3)*length/2}};}break;
    case "walk":valid=n==6&&a&&!points;if(valid){points=true;vertices=new double[6][2];for(int k=0;k<6;k++){vertices[k][0]=length*Math.cos(k*Math.PI/3);vertices[k][1]=length*Math.sin(k*Math.PI/3);}}break;
    case "closure":valid=n==6&&points&&!closed;if(valid){double gap=Math.hypot(vertices[5][0]-vertices[0][0],vertices[5][1]-vertices[0][1]);valid=Math.abs(gap-length)<1e-8;closed=valid;}break;
    case "join":valid=points&&!joined&&(n==3||closed);joined|=valid;break;
    default:valid=false;
   }
   if(!valid)return new Execution(i,stage,false,vertices);stage++;
  }
  return new Execution(-1,stage,joined&&actions.size()==5,vertices);
 }
 public static Checker.Result check(Question q,List<String> actions){
  if(actions.size()!=5)return new Checker.Result(Checker.Status.INPUT_NEEDED,-1,"작도 단계 선택 필요");
  for(int i=0;i<5;i++)if(!options(q).containsKey(actions.get(i)))return new Checker.Result(Checker.Status.INPUT_NEEDED,i,"작도 단계 선택 필요");
  Execution result=execute(q,actions);return result.complete?new Checker.Result(Checker.Status.CORRECT,-1,"정답"):new Checker.Result(Checker.Status.WRONG_ANSWER,result.errorIndex,"이 단계 확인");
 }
}
