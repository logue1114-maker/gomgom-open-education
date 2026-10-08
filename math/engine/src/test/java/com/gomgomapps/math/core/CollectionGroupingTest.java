package com.gomgomapps.math.core;
import java.util.*;import java.io.*;import org.junit.Test;import static org.junit.Assert.*;
public class CollectionGroupingTest {
 private static final String NA="na-nied-primary-2024-v1";
 @Test public void allVisibleCollectionsAndRegroupingsPreserveCardinalityAndIdentity()throws Exception{
  Generator gen=new Generator(new Random(45));
  for(int grade:List.of(2,3)){
   CurriculumLimits limits=GlobalCurriculum.limits(NA,"collectionCount",grade);int max=grade==2?50:100;Set<String> seen=new LinkedHashSet<>();Set<Integer> totals=new HashSet<>();
   for(int i=0;i<4*max+1;i++){
    Question q=gen.next("collectionCount",seen,false,limits);assertTrue(seen.add(q.signature()));String identity=q.signature();double[][] initial=CollectionGrouping.points(q,0);int n=initial.length;assertTrue(n<=max);totals.add(n);
    List<Integer> sizes=grade==2?List.of(2,3,5):List.of(2,5,10);assertEquals(sizes,CollectionGrouping.sizes(q));
    for(int size:sizes){CollectionGrouping.group(q,size);assertEquals(identity,q.signature());double[][] grouped=CollectionGrouping.points(q,size);assertEquals(n,grouped.length);assertUnique(grouped);assertTrue(new Checker().check(q,List.of(),List.of(""+grouped.length)).correct());}
    assertUnique(initial);List<String> expected=List.of(""+(n/5),""+(n/5*5),""+(n%5),""+n);q.answers=new String[]{"999"};CollectionGrouping.attach(q);assertEquals(expected,q.studyGuide.frames.stream().map(f->f.expected).toList());assertFalse(HelpPlan.forQuestion(q).canTransfer());
    ByteArrayOutputStream bytes=new ByteArrayOutputStream();try(ObjectOutputStream stream=new ObjectOutputStream(bytes)){stream.writeObject(q);}Question restored;try(ObjectInputStream stream=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))){restored=(Question)stream.readObject();}assertEquals(CollectionGrouping.selected(q),CollectionGrouping.selected(restored));assertEquals(identity,restored.signature());
   }
   assertEquals(max+1,totals.size());assertTrue(totals.contains(0)&&totals.contains(max));
  }
 }
 private void assertUnique(double[][] points){Set<String> positions=new HashSet<>();for(double[] point:points){assertTrue(point[0]>=0&&point[0]<=1&&point[1]>=0&&point[1]<=1);assertTrue(positions.add(Arrays.toString(point)));}}
 @Test public void groupingSizesAndDiagnosisRespectSelectedGrades(){Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"NA");GlobalCurriculum.choosePack(p,NA);p.grade=2;assertFalse(GlobalCurriculum.scope(p).stream().anyMatch(s->s.id.equals("collectionCount")));p.grade=3;assertTrue(GlobalCurriculum.scope(p).stream().anyMatch(s->s.id.equals("collectionCount")));Question q=new Generator(new Random(3)).next("collectionCount",Set.of(),false,GlobalCurriculum.limits(NA,"collectionCount",2));try{CollectionGrouping.group(q,10);fail();}catch(IllegalArgumentException expected){}assertEquals(0,CollectionGrouping.selected(q));}
}
