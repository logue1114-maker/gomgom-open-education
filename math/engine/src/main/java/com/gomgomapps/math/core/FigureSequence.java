package com.gomgomapps.math.core;
import java.util.*;

/** Public tile arrays; the constant row growth is inferred from the shown figures. */
public final class FigureSequence {
 private FigureSequence(){}
 public static boolean selected(Question q){return q.skillId.equals("figureSequenceLoop");}
 static Question create(Catalog.Skill skill,Random random){
  int width=2+random.nextInt(7),first=1+random.nextInt(3),growth=1+random.nextInt(3),n=4+random.nextInt(3);
  Question q=new Question(skill.id,"각 도형의 가로 칸 수는 같고, 줄 수는 일정하게 늘어납니다.\nN = "+n+"\ni: 도형 번호 · x: 현재 도형의 줄 수\n앞의 세 도형에서 규칙을 찾아 N번째 도형까지 만드는 순서도를 완성하세요.","","lt","update","end","rows:"+growth,"inc");
  q.diagram=new StudyDiagram("tileSequence",new double[]{width,first,first+growth,first+2*growth});
  q.kind="sequenceAlgorithm";q.stepSupport=false;
  q.labels=new String[]{"반복 조건","참일 때","거짓일 때","값 갱신","항 번호 갱신"};
  q.givenNumbers.put("N",""+n);q.givenNumbers.put("first",""+first);return q;
 }
 public static int growth(Question q){return (int)(q.diagram.values[2]-q.diagram.values[1]);}
 public static long first(Question q){return (long)q.diagram.values[1];}
 public static StudyDiagram output(Question q,List<Long> rows){double[] values=new double[rows.size()+1];values[0]=q.diagram.values[0];for(int i=0;i<rows.size();i++)values[i+1]=rows.get(i);return new StudyDiagram("tileSequence",values);}
}
