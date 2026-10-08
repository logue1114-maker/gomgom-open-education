package com.gomgomapps.math.core;
import java.util.*;import java.io.*;import org.junit.Test;import static org.junit.Assert.*;
public class NamibiaSubitisingTest {
 private static final String NA="na-nied-primary-2024-v1";
 @Test public void selectedRangesUseEveryDistinctVisiblePatternBeforeRepeating()throws Exception{
  Generator generator=new Generator(new Random(47));
  for(int grade:List.of(1,2)){
   CurriculumLimits limits=GlobalCurriculum.limits(NA,"el_subitise",grade);int max=grade==1?6:10,domain=grade==1?465:35;List<String> seen=new ArrayList<>();Set<String> shapes=new HashSet<>();Set<Integer> counts=new HashSet<>();Set<Integer> correctPositions=new HashSet<>();
   for(int i=0;i<domain;i++){
    Question q=generator.next("el_subitise",seen,true,limits);assertFalse(seen.contains(q.signature()));seen.add(q.signature());int n=(q.diagram.values.length-1)/2;assertTrue(n>=1&&n<=max);counts.add(n);
    List<String> points=new ArrayList<>();for(int p=1;p<q.diagram.values.length;p+=2){double x=q.diagram.values[p],y=q.diagram.values[p+1];assertTrue(x>=0&&x<=1&&y>=0&&y<=1);points.add(x+":"+y);}Collections.sort(points);assertTrue(shapes.add(points.toString()));
    assertTrue(new Checker().check(q,List.of(),List.of(""+points.size())).correct());for(String option:q.choices)assertTrue(Integer.parseInt(option)>=1&&Integer.parseInt(option)<=max);assertEquals(4,q.choices.size());correctPositions.add(q.correctChoice);assertEquals(""+n,q.choices.get(q.correctChoice));
    q.answers=new String[]{"999"};DotCollections.attach(q);assertFalse(HelpPlan.forQuestion(q).canTransfer());assertEquals(""+n,q.studyGuide.frames.get(0).expected);
    if(i==0){ByteArrayOutputStream data=new ByteArrayOutputStream();new ObjectOutputStream(data).writeObject(q);Question restored=(Question)new ObjectInputStream(new ByteArrayInputStream(data.toByteArray())).readObject();assertEquals(q.signature(),restored.signature());assertArrayEquals(q.diagram.values,restored.diagram.values,0);}
   }
   assertEquals(max,counts.size());assertTrue(counts.contains(max));assertEquals(Set.of(0,1,2,3),correctPositions);assertEquals(seen.get(0),generator.next("el_subitise",seen,false,limits).signature());
  }
 }
 @Test public void australiaScopeAndDiagnosisBoundariesRemainSeparate(){
  CurriculumLimits australian=GlobalCurriculum.limits("au-acara-v9-primary-v1","el_subitise",0);assertFalse(australian.groupedSubitise());assertFalse(CurriculumLimits.NONE.groupedSubitise());
  Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"NA");GlobalCurriculum.choosePack(p,NA);p.grade=1;assertFalse(GlobalCurriculum.scope(p).stream().anyMatch(s->s.id.equals("el_subitise")));p.grade=2;assertTrue(GlobalCurriculum.scope(p).stream().anyMatch(s->s.id.equals("el_subitise")));
  try{new CurriculumLimits("groupedSubitise=yes");fail();}catch(IllegalArgumentException expected){}
 }
}
