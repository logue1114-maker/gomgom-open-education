package com.gomgomapps.math.core;
import java.util.*;

/** A stated constant size-growth family; tile counts are nonlinear in the figure number. */
public final class FigurePatterns {
 private FigurePatterns(){}
 public static final List<Catalog.Skill> SKILLS=List.of(
  new Catalog.Skill("squareFigureCount","정사각형 배열의 규칙과 칸 수",11,1,3,"대수","squareFigureCount",1000,"el_number_pattern,tables","한 변의 증가 규칙을 찾아 가로 칸 수와 세로 칸 수를 곱한다."),
  new Catalog.Skill("triangleFigureCount","계단 배열의 규칙과 칸 수",11,1,3,"대수","triangleFigureCount",1000,"el_number_pattern,add100","줄 수의 증가 규칙을 찾아 각 줄의 칸 수를 더한다."));
 public static boolean supports(String id){return SKILLS.stream().anyMatch(s->s.id.equals(id));}
 static Question create(Catalog.Skill skill,Random random){
  boolean square=skill.id.equals("squareFigureCount");int first=1+random.nextInt(8),growth=1+random.nextInt(4),target=4+random.nextInt(5),second=first+growth,third=second+growth,extra=target-3,change=extra*growth,size=third+change;
  int count=square?size*size:size*(size+1)/2;
  String dimension=square?"한 변의 칸 수":"줄 수";
  String shape=square?"정사각형 배열":"계단 배열";
  Question q=new Question(skill.id,shape+"\n"+dimension+"는 일정하게 늘어납니다.\nN = "+target+"\nN번째 도형의 "+dimension+"와 전체 칸 수를 구하세요.","",""+size,""+count);
  q.labels=new String[]{dimension,"전체 칸 수"};q.stepSupport=false;
  q.diagram=new StudyDiagram(square?"squareFigures":"triangleFigures",new double[]{first,second,third});
  q.studyGuide=new StudyGuide().transfer(false)
   .step("앞의 두 도형에서 증가량을 구하세요.",second+" − "+first+" = ","",""+growth)
   .step("셋째 도형에서 목표 도형까지 늘어나는 횟수를 구하세요.",target+" − 3 = ","",""+extra)
   .step("증가량에 늘어나는 횟수를 곱하세요.",growth+" × "+extra+" = ","",""+change)
   .step(square?"셋째 도형의 한 변에 늘어난 칸 수를 더하세요.":"셋째 도형의 줄 수에 늘어난 줄 수를 더하세요.",third+" + "+change+" = ","",""+size)
   .step(square?"가로 칸 수와 세로 칸 수를 곱하세요.":"각 줄의 칸 수를 더하세요.",square?size+" × "+size+" = ":"1 + 2 + … + "+size+" = ","",""+count);
  return q;
 }
}
