package com.gomgomapps.math.core;
import java.util.*;import org.junit.Test;import static org.junit.Assert.*;
public class PolygonConstructionTest {
 private static List<String> program(int sides){return sides==3?List.of("segment","circleA","circleB","intersection","join"):List.of("segment","circleA","walk","closure","join");}
 @Test public void hundredDistinctNormalPromptsAndIndependentGeometry(){
  Generator g=new Generator(new Random(17));Set<String> seen=new HashSet<>();Set<Integer> sidesSeen=new HashSet<>();
  for(int i=0;i<100;i++){Question q=g.next(PolygonConstruction.ID,seen,true);assertTrue(seen.add(q.signature()));int n=q.prompt.contains("정삼각형")?3:6;int length=Integer.parseInt(q.prompt.split(" cm")[0].split(": ")[1]);sidesSeen.add(n);
   assertTrue(new Checker().check(q,List.of(),program(n)).correct());PolygonConstruction.Execution actual=PolygonConstruction.execute(q,program(n));assertEquals(n,actual.vertices.length);
   for(int k=0;k<n;k++){double[] a=actual.vertices[k],b=actual.vertices[(k+1)%n];assertEquals(length,Math.hypot(a[0]-b[0],a[1]-b[1]),1e-8);}assertTrue(q.choices.isEmpty());assertEquals(5,q.answers.length);
  }assertEquals(Set.of(3,6),sidesSeen);
 }
 @Test public void validCircleOrderAndAnswerMetadataIndependence(){Question q=PolygonConstruction.create(Catalog.get(PolygonConstruction.ID),3,7);Arrays.fill(q.answers,"wrong");assertTrue(PolygonConstruction.check(q,List.of("segment","circleB","circleA","intersection","join")).correct());}
 @Test public void firstCausalErrorAndBlankAreIdentified(){Question q=PolygonConstruction.create(Catalog.get(PolygonConstruction.ID),6,9);assertEquals(1,PolygonConstruction.check(q,List.of("segment","walk","circleA","closure","join")).index);assertEquals(1,PolygonConstruction.check(q,List.of("segment","half","walk","closure","join")).index);assertEquals(Checker.Status.INPUT_NEEDED,PolygonConstruction.check(q,List.of("segment","","walk","closure","join")).status);}
 @Test public void everyPermutationExecutesWithOnlyValidOrdersAccepted(){for(int n:new int[]{3,6}){Question q=PolygonConstruction.create(Catalog.get(PolygonConstruction.ID),n,5);List<List<String>> permutations=new ArrayList<>();permute(new ArrayList<>(program(n)),0,permutations);int correct=0;for(List<String> p:permutations){boolean accepted=PolygonConstruction.check(q,p).correct();boolean expected=p.get(0).equals("segment")&&p.get(4).equals("join")&&(n==3?p.get(3).equals("intersection")&&Set.of(p.get(1),p.get(2)).equals(Set.of("circleA","circleB")):p.equals(program(6)));assertEquals(expected,accepted);if(accepted)correct++;}assertEquals(n==3?2:1,correct);}}
 private static void permute(List<String> a,int k,List<List<String>> out){if(k==a.size()){out.add(List.copyOf(a));return;}for(int i=k;i<a.size();i++){Collections.swap(a,k,i);permute(a,k+1,out);Collections.swap(a,k,i);}}
}
