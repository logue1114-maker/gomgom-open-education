package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;import org.junit.Test;import static org.junit.Assert.*;
public class MovementCircleRelationsTest {
 private List<Rational> values(Question q){
  List<Rational> n=new ArrayList<>();Matcher m=Pattern.compile("[+-]?\\d+(?:\\.\\d+)?(?:/\\d+)?").matcher(q.prompt);while(m.find())n.add(Expression.number(m.group()));
  if(q.skillId.equals("sec_circle_line_intersections")){assertEquals(2,n.size());Rational s=n.get(0),r=s.sqrt(),t=n.get(1),d=t.compareTo(Rational.ZERO)<0?t.neg():t,v=d.sub(r);return List.of(s,r,t,d,v,Rational.of(v.compareTo(Rational.ZERO)<0?2:v.isZero()?1:0));}
  Rational x=n.get(0),y=n.get(1);
  if(q.skillId.equals("sec_translation")){assertEquals(4,n.size());return List.of(x,y,n.get(2),n.get(3),x.add(n.get(2)),y.add(n.get(3)));}
  assertEquals(2,n.size());return List.of(x,y,q.prompt.contains("x축")?x:x.neg(),q.prompt.contains("y축")?y:y.neg());
 }
 private void verify(Question q){
  List<Rational> v=values(q);boolean pair=!q.skillId.equals("sec_circle_line_intersections");List<String> answers=pair?List.of(v.get(v.size()-2).toString(),v.get(v.size()-1).toString()):List.of(v.get(v.size()-1).toString());
  assertTrue(new Checker().check(q,List.of(),answers).correct());String id=q.id,prompt=q.prompt,signature=q.signature();StudyDiagram diagram=q.diagram;
  Arrays.fill(q.answers,"99999");q.givenNumbers.replaceAll((k,x)->"99999");q.choiceInputs=new Rational[]{Rational.of(99999)};
  HelpPlan p=HelpPlan.forQuestion(q);assertFalse(p.canTransfer());assertEquals(v.size(),p.size());assertEquals(id,q.id);assertEquals(prompt,q.prompt);assertEquals(signature,q.signature());assertSame(diagram,q.diagram);
  for(int i=0;i<p.size();i++){assertTrue(p.step(i).accepts(v.get(i).toString()));assertFalse(p.step(i).accepts(v.get(i).add(Rational.of(10)).toString()));assertEquals("",p.step(i).after);assertFalse(p.step(i).before.matches(".*[0-9].*"));}
 }
 @Test public void publicGivensDriveAllGeneratedCalculations(){
  Generator g=new Generator(new Random(2401));for(String skill:List.of("sec_circle_line_intersections","sec_translation","sec_reflection")){
   Set<String> prompts=new HashSet<>(),categories=new HashSet<>(),axes=new HashSet<>(),signs=new HashSet<>();Set<Integer> positions=new HashSet<>();
   for(int i=0;i<2400;i++){Question q=g.next(skill,List.of(),i%2==0);prompts.add(q.prompt);List<Rational> v=values(q);
    if(skill.equals("sec_circle_line_intersections")){categories.add(v.get(5).toString());axes.add(q.prompt.contains("직선 x=")?"x":"y");signs.add(String.valueOf(v.get(2).n.signum()));positions.add(q.correctChoice);}
    if(skill.equals("sec_reflection"))axes.add(q.prompt.contains("x축")?"x":q.prompt.contains("y축")?"y":"O");verify(q);
   }
   assertTrue(skill+prompts.size(),prompts.size()>100);if(skill.equals("sec_circle_line_intersections")){assertEquals(Set.of("0","1","2"),categories);assertEquals(Set.of("x","y"),axes);assertEquals(Set.of("-1","0","1"),signs);assertEquals(Set.of(0,1,2),positions);}if(skill.equals("sec_reflection"))assertEquals(Set.of("x","y","O"),axes);
  }
 }
 @Test public void oldPublicProblemsFractionsAndZeroCoordinatesReplaceOldDrafts(){
  for(Question q:List.of(new Question("sec_circle_line_intersections","원 x²+y²=9/4과 직선 x=-3/2의 교점 수를 고르세요.","","1"),new Question("sec_translation","점 P(0, -1/2)를 벡터 (-2, 1/2)만큼 평행이동한 좌표는?","","-2","0"),new Question("sec_reflection","점 (0, -1/2)를 원점에 대하여 대칭이동한 좌표는?","","0","1/2"))){
   q.studyGuide=new StudyGuide().step("옛 도움","9 − 3 = ","","6");verify(q);HelpPlan p=HelpPlan.forQuestion(q);HelpPlan.Draft draft=new HelpPlan.Draft();draft.questionId=q.id;draft.stage=1;draft.entries=new ArrayList<>(List.of("9"));
   p.restore(draft,q.id);assertEquals(0,draft.stage);assertEquals("",draft.entries.get(0));assertTrue(draft.teachingVersion.endsWith("relations-v1"));draft.entries.set(0,values(q).get(0).toString());draft.stage=1;assertEquals(1,HelpPlan.forQuestion(q).restore(draft.copy(),q.id).stage);
  }
 }
}
