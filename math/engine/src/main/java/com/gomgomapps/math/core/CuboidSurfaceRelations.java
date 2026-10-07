package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
/** Learner-entered cuboid net and surface-path relationships from public edges. */
public final class CuboidSurfaceRelations {
 private CuboidSurfaceRelations(){}
 public static boolean supports(String id){return Set.of("cuboidNetArea","cuboidSurfacePath").contains(id);}
 public static void attach(Question q){
  if(q==null||!supports(q.skillId)||q.prompt==null)return;
  boolean net=q.skillId.equals("cuboidNetArea");Matcher m=Pattern.compile("직육면체: a=(\\d+(?:\\.\\d+)?(?:/\\d+)?), b=(\\d+(?:\\.\\d+)?(?:/\\d+)?), c=(\\d+(?:\\.\\d+)?(?:/\\d+)?)cm\\n"+(net?"전개도의 전체 넓이는 몇 cm²인가요\\?":"A와 B는 마주 보는 꼭짓점입니다.\\n표면을 따라가는 최단거리는 몇 cm인가요\\?")).matcher(q.prompt);if(!m.matches())return;
  Rational a=Expression.number(m.group(1)),b=Expression.number(m.group(2)),c=Expression.number(m.group(3));
  Rational first=a.add(b).pow(2).add(c.pow(2)),second=a.add(c).pow(2).add(b.pow(2)),third=b.add(c).pow(2).add(a.pow(2)),minimum=Collections.min(List.of(first,second,third)),root=null;
  if(!net){try{root=minimum.sqrt();}catch(IllegalArgumentException ex){return;}}
  StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="cuboid-surface-relations-v1";
  g
   .step("변 a의 길이를 찾아 쓰세요.","a = "," cm",a.toString())
   .step("변 b의 길이를 찾아 쓰세요.","b = "," cm",b.toString())
   .step("변 c의 길이를 찾아 쓰세요.","c = "," cm",c.toString());
  if(net){g
   .step("a와 b로 이루어진 면의 넓이를 구하세요.","a × b = "," cm²",a.mul(b).toString())
   .step("a와 c로 이루어진 면의 넓이를 구하세요.","a × c = "," cm²",a.mul(c).toString())
   .step("b와 c로 이루어진 면의 넓이를 구하세요.","b × c = "," cm²",b.mul(c).toString())
   .step("세 면의 넓이를 더하고 두 배 하세요.","2 × (ab면 넓이 + ac면 넓이 + bc면 넓이) = "," cm²",a.mul(b).add(a.mul(c)).add(b.mul(c)).mul(Rational.of(2)).toString());}else{g
   .step("a와 b를 나란히 펼친 경로의 길이 제곱을 구하세요.","(a + b)² + c² = "," cm²",first.toString())
   .step("a와 c를 나란히 펼친 경로도 계산하세요.","(a + c)² + b² = "," cm²",second.toString())
   .step("b와 c를 나란히 펼친 경로도 계산하세요.","(b + c)² + a² = "," cm²",third.toString())
   .step("세 경로의 길이 제곱을 비교해 가장 작은 값을 쓰세요.","세 경로 제곱 중 최솟값 = "," cm²",minimum.toString())
   .step("가장 작은 값의 양의 제곱근을 구하세요.","√최솟값 = "," cm",root.toString());}
  q.studyGuide=g;
 }
}
