package com.gomgomapps.math.core;
import org.junit.Test;import static org.junit.Assert.*;import java.util.*;
public class AngleRelationsTest {
 @Test public void twoHundredDistinctQuestionsSolvedFromPublishedNumbers(){
  for(String id:List.of("sec_parallel_angle","sec_circle_inscribed")){
   Generator g=new Generator(new Random(73));Set<String> seen=new HashSet<>();Set<Integer> modes=new HashSet<>(),ranks=new HashSet<>();Set<String> answers=new HashSet<>();
   for(int i=0;i<100;i++){
    Question q=g.next(id,seen,true);assertTrue(seen.add(q.signature()));java.util.regex.Matcher value=java.util.regex.Pattern.compile("[0-9]+(?=°)").matcher(q.prompt);assertTrue(value.find());int given=Integer.parseInt(value.group()),mode=id.equals("sec_parallel_angle")?(q.prompt.contains("동위각")?0:q.prompt.contains("엇각")?1:2):(q.prompt.contains("작은 호 AB = ")?2:q.prompt.contains("∠ACB = ")?1:0);modes.add(mode);
    assertTrue(q.prompt.contains(given+"°"));int result=id.equals("sec_parallel_angle")?(mode==2?180-given:given):(mode==1?2*given:given/2);answers.add(""+result);
    assertTrue(new Checker().check(q,List.of(),List.of(""+result)).correct());assertFalse(new Checker().check(q,List.of(),List.of(""+(result+1))).correct());
    assertEquals(2,q.diagram.values.length);assertEquals(given,q.diagram.values[0],0);assertEquals(mode,q.diagram.values[1],0);
    HelpPlan h=HelpPlan.forQuestion(q);assertFalse(h.canTransfer());assertEquals(2,h.size());assertTrue(h.step(0).accepts(""+given));assertTrue(h.step(1).accepts(""+result));
    assertEquals(4,q.choices.size());assertEquals(1,Collections.frequency(q.choices,""+result));ranks.add(q.choices.indexOf(""+result));
   }
   assertEquals(Set.of(0,1,2),modes);assertEquals(Set.of(0,1,2,3),ranks);assertTrue(answers.size()>40);
  }
 }
 @Test public void brazilGradeNineSelectsBothExistingTypes(){Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"BR");GlobalCurriculum.choosePack(p,"br-bncc-fundamental-2017-v1");for(String id:List.of("sec_parallel_angle","sec_circle_inscribed"))assertTrue(GlobalCurriculum.pack(p).inGrade(id,9));}
 private double angle(double[] u,double[] v){double dot=u[0]*v[0]+u[1]*v[1],norm=Math.hypot(u[0],u[1])*Math.hypot(v[0],v[1]);return Math.toDegrees(Math.acos(Math.max(-1,Math.min(1,dot/norm))));}
 @Test public void circlePointsHaveTheGivenCentralAndInscribedAngles(){for(int mode=0;mode<3;mode++)for(int inscribed=7;inscribed<=83;inscribed++){
  double given=mode==1?inscribed:2*inscribed;double[][] v=AngleRelationGeometry.vertices(new StudyDiagram("circleAngleRelation",new double[]{given,mode}));
  for(int i=0;i<3;i++)assertEquals(1,Math.hypot(v[i][0],v[i][1]),1e-10);
  assertEquals(2*inscribed,angle(v[0],v[1]),1e-8);assertEquals(inscribed,angle(new double[]{v[0][0]-v[2][0],v[0][1]-v[2][1]},new double[]{v[1][0]-v[2][0],v[1][1]-v[2][1]}),1e-8);
 }}
 @Test public void transversalIntersectionsGiveBothPublishedAndTargetWedges(){for(int mode=0;mode<3;mode++)for(int given=25;given<=155;given++){
  double[][] v=AngleRelationGeometry.vertices(new StudyDiagram("parallelAngleRelation",new double[]{given,mode}));double[] up={v[0][0]-v[1][0],v[0][1]-v[1][1]},down={-up[0],-up[1]};
  double a=mode==0?angle(new double[]{1,0},up):mode==1?angle(new double[]{-1,0},down):angle(new double[]{1,0},down),b=angle(new double[]{1,0},up);
  assertEquals(given,a,1e-8);assertEquals(mode==2?180-given:given,b,1e-8);
 }}
}
