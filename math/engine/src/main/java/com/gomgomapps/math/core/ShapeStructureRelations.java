package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
/** Public outlines and fixed cuboid topology, with learner-entered counts. */
public final class ShapeStructureRelations {
 private ShapeStructureRelations(){}
 public static final Set<String> IDS=Set.of("el_shape_sides","el_shape_classify","el_3d_elements");
 public static boolean supports(String id){return IDS.contains(id);}
 public static double[][] outline(StudyDiagram d,int index){int count=(int)d.values[index];if(count<3||count>20)return new double[0][0];double rotation=d.rotationDegrees!=null&&index<d.rotationDegrees.length?d.rotationDegrees[index]:0,scale=d.horizontalScale!=null&&index<d.horizontalScale.length?d.horizontalScale[index]:1;double[][] points=new double[count][2];for(int i=0;i<count;i++){double angle=2*Math.PI*i/count-Math.PI/2+Math.toRadians(rotation);points[i][0]=Math.cos(angle)*scale;points[i][1]=Math.sin(angle);}return points;}
 public record Frame(String instruction,String label,String result,String expected,String diagramType){}
 private static Frame count(String instruction,String label,int expected,String type){return new Frame(instruction,label+" = ",label,""+expected,type);}
 public static List<Frame> frames(Question q){if(q==null||!supports(q.skillId)||q.prompt==null)return List.of();List<Frame> out=new ArrayList<>();
  if(q.skillId.equals("el_shape_sides")){if(q.diagram==null||!q.diagram.type.equals("polygon")||q.diagram.values.length!=1)return List.of();double sides=q.diagram.values[0];if(sides!=Math.rint(sides)||sides<3||sides>8)return List.of();if(!q.prompt.equals("그림에 있는 도형의 변은 몇 개인가요?")&&!q.prompt.equals((int)sides+"각형의 변은 몇 개인가요?"))return List.of();out.add(count("변은 두 꼭짓점을 잇는 곧은 선분입니다. 도형을 따라 한 번씩 세세요.","도형의 변 수",(int)sides,"polygon"));}
  else if(q.skillId.equals("el_shape_classify")){if(!q.prompt.equals("그림에 있는 도형 중 삼각형은 몇 개인가요?")||q.diagram==null||!q.diagram.type.equals("shapes")||q.diagram.values.length!=4)return List.of();String[] labels={"첫 번째 도형의 변 수","두 번째 도형의 변 수","세 번째 도형의 변 수","네 번째 도형의 변 수"};int triangles=0;for(int i=0;i<4;i++){double v=q.diagram.values[i];if(v!=0&&v!=3&&v!=4)return List.of();if(v==3)triangles++;out.add(count("곧은 변을 세세요. 원에는 곧은 변이 없습니다.",labels[i],(int)v,"shapes"));}if(triangles==0)return List.of();out.add(count("삼각형은 곧은 변이 세 개인 도형입니다. 해당하는 도형을 세세요.","삼각형인 도형 수",triangles,"shapes"));}
  else{if(q.diagram!=null){if(!q.diagram.type.equals("cuboidElements")||q.diagram.values.length!=3)return List.of();for(double v:q.diagram.values)if(!Double.isFinite(v)||v<2||v>9||v!=Math.rint(v))return List.of();}
   boolean all=q.prompt.equals("직육면체의 면, 모서리, 꼭짓점의 수는?");Matcher m=Pattern.compile("^직육면체의 (면은|모서리는|꼭짓점은) 모두 몇 개인가요\\?$").matcher(q.prompt);if(!all&&!m.matches())return List.of();String part=all?"all":m.group(1);
   if(all||part.equals("면은"))out.add(count("면은 평평한 부분입니다. 펼친 그림의 사각형을 한 번씩 세세요.","직육면체의 면 수",6,"cuboidElementsFaces"));
   if(all||part.equals("모서리는"))out.add(count("모서리는 두 면이 만나는 선분입니다. 보이지 않는 점선도 세세요.","직육면체의 모서리 수",12,"cuboidElementsEdges"));
   if(all||part.equals("꼭짓점은"))out.add(count("꼭짓점은 모서리들이 만나는 점입니다. 앞뒤의 점을 모두 세세요.","직육면체의 꼭짓점 수",8,"cuboidElementsVertices"));
  }return List.copyOf(out);
 }
 public static StudyDiagram teachingDiagram(Question q,int stage){var frames=frames(q);if(frames.isEmpty())return q.diagram;if(!q.skillId.equals("el_3d_elements"))return q.diagram;String type=frames.get(Math.min(stage,frames.size()-1)).diagramType();return new StudyDiagram(type,q.diagram==null?new double[]{4,3,2}:q.diagram.values);}
 public static void attach(Question q){var frames=frames(q);if(frames.isEmpty())return;StudyGuide guide=new StudyGuide().transfer(false);guide.teachingVersion="shape-structure-relations-v1";for(var f:frames)guide.step(f.instruction(),f.label(),"개",f.expected());q.studyGuide=guide;}
}
