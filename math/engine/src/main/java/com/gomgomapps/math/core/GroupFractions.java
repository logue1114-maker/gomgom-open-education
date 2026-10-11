package com.gomgomapps.math.core;
import java.util.*;
/** Fractions of a collection: each visible object contributes one equal unit. */
public final class GroupFractions {
 private GroupFractions(){}
 public static final String NAME="groupFractionName",WRITE="groupFractionWrite";
 public static final List<Catalog.Skill> SKILLS=List.of(
  new Catalog.Skill(NAME,"그룹의 분수 이름",3,1,3,"",NAME,12,"fractionPart","전체 물체 중 색칠한 물체가 차지하는 부분의 이름을 고른다."),
  new Catalog.Skill(WRITE,"그룹을 분수로 나타내기",3,1,3,"",WRITE,12,"fractionPart","전체 물체 수를 분모, 색칠한 물체 수를 분자로 나타낸다."));
 public static boolean supports(String id){return NAME.equals(id)||WRITE.equals(id);}
 static final int[][] PARTS={{1,2},{1,4},{3,4},{1,3}};
 static final String[] NAMES={"절반","사분의 일","사분의 삼","삼분의 일"};
 static Question make(String id,int part,int multiple,int start,int shape){
  if(!supports(id)||part<0||part>3||multiple<1||multiple>3||shape<0||shape>1)throw new IllegalArgumentException("group fraction domain");
  int total=PARTS[part][1]*multiple,selected=PARTS[part][0]*multiple;
  if(start<0||start>=total)throw new IllegalArgumentException("group fraction start");
  int mask=0;for(int i=0;i<selected;i++)mask|=1<<((start+i)%total);
  String answer=PARTS[part][0]+"/"+PARTS[part][1];
  Question q=new Question(id,NAME.equals(id)?"색칠한 물체가 그룹에서 차지하는 부분의 이름을 고르세요.":"색칠한 물체가 그룹에서 차지하는 부분을 분수로 나타내세요.",answer,answer);
  q.stepSupport=false;q.diagram=new StudyDiagram("groupFraction",new double[]{total,mask,shape});
  if(NAME.equals(id)){q.kind="fractionName";for(int i=0;i<4;i++)q.choiceLabels.put(PARTS[i][0]+"/"+PARTS[i][1],NAMES[i]);}
  q.studyGuide=new StudyGuide().transfer(false).step("전체 물체를 세세요.","전체 물체 수 = ","",""+total).step("색칠한 물체를 세세요.","색칠한 물체 수 = ","",""+selected);
  q.studyGuide.teachingVersion="group-fractions-v1";return q;
 }
 static Question next(Catalog.Skill skill,Random random,CurriculumLimits limits,Map<String,Integer> recent){
  Map<String,Question> pool=new LinkedHashMap<>();for(int part=0;part<4;part++)for(int multiple=1;multiple<=3;multiple++)for(int start=0;start<PARTS[part][1]*multiple;start++)for(int shape=0;shape<2;shape++){Question q=make(skill.id,part,multiple,start,shape);if(limits.allows(q))pool.put(q.signature(),q);}return FactFoundations.choose(pool,random,recent);
 }
 public static int[] read(Question q){
  if(q==null||!supports(q.skillId)||q.diagram==null||!"groupFraction".equals(q.diagram.type)||q.diagram.values.length!=3)return null;
  double[] v=q.diagram.values;for(double d:v)if(!Double.isFinite(d)||d!=Math.rint(d))return null;
  int total=(int)v[0],mask=(int)v[1],shape=(int)v[2];if(total<2||total>12||mask<=0||mask>=(1<<total)||shape<0||shape>1)return null;
  int selected=Integer.bitCount(mask);Rational fraction=Rational.of(selected,total);boolean allowed=false;for(int[] part:PARTS)if(fraction.equals(Rational.of(part[0],part[1])))allowed=true;
  return allowed?new int[]{total,selected,mask,shape}:null;
 }
 static Checker.Result check(Question q,List<String> answers){
  int[] data=read(q);if(data==null||answers==null||answers.size()!=1||answers.get(0)==null||answers.get(0).isBlank())return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"부분의 이름이나 분수를 입력하세요.");
  try{String raw=answers.get(0).trim();if(!raw.matches("[0-9]{1,3}/[0-9]{1,3}"))throw new IllegalArgumentException();String[] pair=raw.split("/");Rational answer=Rational.of(Integer.parseInt(pair[0]),Integer.parseInt(pair[1]));return answer.equals(Rational.of(data[1],data[0]))?new Checker.Result(Checker.Status.CORRECT,-1,"그룹의 분수를 찾았습니다."):new Checker.Result(Checker.Status.WRONG_ANSWER,0,"전체 물체와 색칠한 물체 수를 비교하세요.");}catch(Exception e){return new Checker.Result(Checker.Status.INPUT_NEEDED,0,"분수로 입력하세요.");}
 }
}
