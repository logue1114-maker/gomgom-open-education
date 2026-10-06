package com.gomgomapps.math.core;
import org.junit.Test;import static org.junit.Assert.*;import java.util.*;import java.util.regex.*;
public class ParallelSegmentsTest {
 static int[] publicCalculation(String p){
  Matcher ratio=Pattern.compile("(?:AD:DB|AE:EC) = ([0-9]+):([0-9]+)").matcher(p),given=Pattern.compile("(?<!:)(AD|DB|AE|EC) = ([0-9]+)\\.").matcher(p);assertTrue(ratio.find());assertTrue(given.find());
  boolean upper=given.group(1).equals("AD")||given.group(1).equals("AE");int known=Integer.parseInt(given.group(2)),a=Integer.parseInt(ratio.group(1)),b=Integer.parseInt(ratio.group(2)),unit=known/(upper?a:b);assertEquals(0,known%(upper?a:b));return new int[]{unit,unit*(upper?b:a)};
 }
 static double length(double[] a,double[] b){return Math.hypot(a[0]-b[0],a[1]-b[1]);}
 @Test public void hundredDistinctPublicProblemsCoverFourRequestedSegmentsAndVariableChoices(){
  Generator g=new Generator(new Random(813));Set<String> seen=new HashSet<>(),targets=new HashSet<>(),answers=new HashSet<>();Set<Integer> ranks=new HashSet<>();
  for(int i=0;i<100;i++){Question q=g.next("sec_parallel_segment_ratio",seen,true);assertTrue(seen.add(q.signature()));int[] result=publicCalculation(q.prompt);assertTrue(q.prompt.contains("DE ∥ BC"));Matcher m=Pattern.compile("(EC|DB|AE|AD)의 길이는").matcher(q.prompt);assertTrue(m.find());targets.add(m.group(1));String answer=String.valueOf(result[1]);answers.add(answer);
   assertTrue(new Checker().check(q,List.of(),List.of(answer)).correct());assertFalse(new Checker().check(q,List.of(),List.of(String.valueOf(result[1]+1))).correct());HelpPlan plan=HelpPlan.forQuestion(q);assertEquals(2,plan.size());assertFalse(plan.canTransfer());assertTrue(plan.step(0).accepts(String.valueOf(result[0])));assertTrue(plan.step(1).accepts(answer));
   assertEquals(4,q.choices.size());assertEquals(1,Collections.frequency(q.choices,answer));assertEquals(answer,q.choices.get(q.correctChoice));assertEquals(4,new HashSet<>(q.choices).size());ranks.add(q.correctChoice);assertEquals("parallelSegments",q.diagram.type);assertEquals(4,q.diagram.values.length);
  }assertEquals(Set.of("AD","DB","AE","EC"),targets);assertTrue(answers.size()>15);assertEquals(Set.of(0,1,2,3),ranks);
 }
 @Test public void actualParallelCutAndBothSideRatiosMatchPublicLengths(){Random random=new Random(983);for(int i=0;i<3000;i++){
  Question q=ParallelSegments.create(Catalog.get("sec_parallel_segment_ratio"),random);double[][] v=ParallelSegments.vertices(q.diagram);double m=q.diagram.values[1],n=q.diagram.values[2];assertEquals(m/n,length(v[0],v[3])/length(v[3],v[1]),1e-9);assertEquals(m/n,length(v[0],v[4])/length(v[4],v[2]),1e-9);
  assertEquals(0,(v[4][0]-v[3][0])*(v[2][1]-v[1][1])-(v[4][1]-v[3][1])*(v[2][0]-v[1][0]),1e-9);assertTrue(length(v[1],v[2])>0);int mode=(int)q.diagram.values[0];double ae=length(v[0],v[4]),ec=length(v[4],v[2]),ad=length(v[0],v[3]),db=length(v[3],v[1]);double[] known={ae,ad,ec,db},target={ec,db,ae,ad};assertEquals(q.diagram.values[3],known[mode],1e-9);assertEquals(publicCalculation(q.prompt)[1],target[mode],1e-9);
 }}
}
