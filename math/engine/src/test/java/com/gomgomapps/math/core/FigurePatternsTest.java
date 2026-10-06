package com.gomgomapps.math.core;
import org.junit.Test;
import java.util.*;
import static org.junit.Assert.*;
public class FigurePatternsTest {
 @Test public void hundredSquarePatternsAreSolvedByCountingPublicTiles(){verify("squareFigureCount");}
 @Test public void hundredStaircasePatternsAreSolvedByCountingPublicTiles(){verify("triangleFigureCount");}
 private void verify(String id){Generator g=new Generator(new Random(id.hashCode()));Set<String> seen=new HashSet<>();Set<Integer> targets=new HashSet<>(),answers=new HashSet<>();boolean square=id.equals("squareFigureCount");for(int k=0;k<100;k++){
  Question q=g.next(id,seen,k%2==0,GlobalCurriculum.limits("br-bncc-fundamental-2017-v1",id,8));assertTrue(seen.add(q.signature()));assertTrue(q.choices.isEmpty());
  double[] v=q.diagram.values;int difference=(int)(v[1]-v[0]);assertEquals(difference,(int)(v[2]-v[1]));int target=Integer.parseInt(q.prompt.split("N = ")[1].split("\n")[0]);targets.add(target);
  int size=(int)v[0];for(int i=1;i<target;i++)size+=difference;int total=0;for(int row=1;row<=size;row++)for(int col=1;col<=(square?size:row);col++)total++;answers.add(total);
  List<String> correct=List.of(""+size,""+total);assertTrue(new Checker().check(q,List.of(),correct).correct());assertEquals(""+size,q.answers[0]);assertEquals(""+total,q.answers[1]);
  assertEquals(1,new Checker().check(q,List.of(),List.of(""+size,""+(total+1))).index);assertEquals(0,new Checker().check(q,List.of(),List.of(""+(size+1),""+total)).index);
  assertEquals(Checker.Status.INPUT_NEEDED,new Checker().check(q,List.of(),List.of("","")).status);
  assertFalse(q.studyGuide.transfer);assertEquals(5,q.studyGuide.frames.size());List<Integer> help=List.of(difference,target-3,difference*(target-3),size,total);for(int i=0;i<5;i++)assertEquals(""+help.get(i),q.studyGuide.frames.get(i).expected);
 }assertEquals(5,targets.size());assertTrue(answers.size()>15);}
 @Test public void serializedHelpKeepsBothMainFieldsUnfilledAndNoTransfer()throws Exception{
  for(String id:List.of("squareFigureCount","triangleFigureCount")){Question q=new Generator(new Random(9)).next(id,Set.of(),false);java.io.ByteArrayOutputStream bytes=new java.io.ByteArrayOutputStream();new java.io.ObjectOutputStream(bytes).writeObject(q);Question restored=(Question)new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bytes.toByteArray())).readObject();assertEquals(q.signature(),restored.signature());assertFalse(restored.studyGuide.transfer);assertEquals(5,restored.studyGuide.frames.size());assertEquals(Checker.Status.INPUT_NEEDED,new Checker().check(restored,List.of(),List.of("","")).status);}
 }
 @Test public void currentBrazilGradeIsAvailableButExcludedFromPreviousGradeDiagnosis(){Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"BR");GlobalCurriculum.choosePack(p,"br-bncc-fundamental-2017-v1");p.grade=8;for(Catalog.Skill s:FigurePatterns.SKILLS){assertTrue(GlobalCurriculum.pack(p).inGrade(s.id,8));assertFalse(GlobalCurriculum.scope(p).stream().anyMatch(x->x.id.equals(s.id)));}}
}
