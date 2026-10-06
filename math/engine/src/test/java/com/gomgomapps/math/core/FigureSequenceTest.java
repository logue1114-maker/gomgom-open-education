package com.gomgomapps.math.core;
import org.junit.Test;
import java.util.*;
import static org.junit.Assert.*;
public class FigureSequenceTest {
 @Test public void hundredDistinctPublicFiguresDriveTheSelectedProgram(){
  Generator generator=new Generator(new Random(6117));Set<String> seen=new HashSet<>();
  for(int k=0;k<100;k++){
   Question q=generator.next("figureSequenceLoop",seen,k%2==0,GlobalCurriculum.limits("br-bncc-fundamental-2017-v1","figureSequenceLoop",8));assertTrue(seen.add(q.signature()));
   double[] shown=q.diagram.values;int columns=(int)shown[0],rows=(int)shown[1],difference=(int)(shown[2]-shown[1]);assertEquals(difference,(int)(shown[3]-shown[2]));
   int n=Integer.parseInt(q.prompt.split("N = ")[1].split("\n")[0]);List<Long> expected=new ArrayList<>();for(int i=0;i<n;i++){expected.add((long)rows);rows+=difference;}
   List<String> program=List.of("lt","update","end","rows:"+difference,"inc");assertEquals(expected,SequenceAlgorithm.execute(q,program).output);assertTrue(new Checker().check(q,List.of(),program).correct());assertTrue(q.choices.isEmpty());
   StudyDiagram output=FigureSequence.output(q,expected);for(int i=1;i<output.values.length;i++){int tiles=0;for(int row=0;row<output.values[i];row++)for(int col=0;col<columns;col++)tiles++;assertEquals(expected.get(i-1)*columns,tiles);}
   q.answers=new String[]{"wrong"};q.givenNumbers.put("first","999");assertTrue(SequenceAlgorithm.check(q,program).correct());
   String[] wrong={"le","end","update","rows:"+(difference==1?2:1),"stay"};for(int slot=0;slot<5;slot++){List<String> bad=new ArrayList<>(program);bad.set(slot,wrong[slot]);assertEquals(slot,SequenceAlgorithm.check(q,bad).index);assertFalse(SequenceAlgorithm.check(q,bad).correct());}
   assertTrue(SequenceAlgorithm.check(q,List.of("ge","end","update","rows:"+difference,"inc")).correct());
  }
 }
 @Test public void missingBlocksAndNonterminationDoNotRevealOrAcceptAProgram(){
  Question q=new Generator(new Random(512)).next("figureSequenceLoop",Set.of(),false);List<String> empty=Arrays.asList("","","","","");assertEquals(Checker.Status.INPUT_NEEDED,SequenceAlgorithm.check(q,empty).status);
  List<String> endless=List.of("lt","update","update","rows:1","stay");assertFalse(SequenceAlgorithm.execute(q,endless).halted);assertEquals(33,SequenceAlgorithm.execute(q,endless).output.size());assertFalse(SequenceAlgorithm.check(q,endless).correct());
 }
 @Test public void savedPublicFiguresAndBrazilPreviousGradeBoundaryRemainStable()throws Exception{
  Question q=new Generator(new Random(97)).next("figureSequenceLoop",Set.of(),false);java.io.ByteArrayOutputStream bytes=new java.io.ByteArrayOutputStream();new java.io.ObjectOutputStream(bytes).writeObject(q);Question restored=(Question)new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bytes.toByteArray())).readObject();assertEquals(q.signature(),restored.signature());assertArrayEquals(q.diagram.values,restored.diagram.values,0);
  Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"BR");GlobalCurriculum.choosePack(p,"br-bncc-fundamental-2017-v1");p.grade=8;assertTrue(GlobalCurriculum.pack(p).inGrade(q.skillId,8));assertFalse(GlobalCurriculum.scope(p).stream().anyMatch(s->s.id.equals(q.skillId)));
 }
}
