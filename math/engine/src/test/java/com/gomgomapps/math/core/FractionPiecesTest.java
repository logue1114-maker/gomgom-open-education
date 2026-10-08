package com.gomgomapps.math.core;
import org.junit.Test;import static org.junit.Assert.*;import java.util.*;import java.io.*;
public class FractionPiecesTest {
 private String solve(Question q){
  if(q.skillId.equals("fractionPiecePicture")){int den=q.prompt.contains("사분의 일")?4:2;return q.choiceDiagrams.entrySet().stream().filter(e->e.getValue().values[0]==den&&Integer.bitCount((int)e.getValue().values[1])==1).findFirst().orElseThrow().getKey();}
  if(q.skillId.equals("fractionPieceCount"))return ""+(int)q.diagram.values[1];return ""+((int)q.diagram.values[1]*Integer.bitCount((int)q.diagram.values[2]));
 }
 @Test public void allSelectedPublicConditionsHaveNoSymbolicFractionsAndIndependentSolutions(){
  Generator g=new Generator(new Random(53));String pack="gh-nacca-core-2019-2023-v1";
  for(int grade=1;grade<=2;grade++)for(String id:List.of("fractionPiecePicture","fractionPieceCount","fractionPieceWhole")){
   if(grade==1&&id.equals("fractionPieceWhole"))continue;CurriculumLimits l=GlobalCurriculum.limits(pack,id,grade);int size=id.equals("fractionPiecePicture")?32*grade:id.equals("fractionPieceCount")?36*grade:24;
   Set<String> seen=new LinkedHashSet<>();Set<Integer> positions=new HashSet<>();
   for(int i=0;i<size;i++){Question q=g.next(id,seen,true,l);assertTrue(seen.add(q.signature()));String answer=solve(q);assertTrue(new Checker().check(q,List.of(),List.of(answer)).correct());assertFalse(q.prompt.matches("(?s).*\\d+\\s*/\\s*\\d+.*"));assertEquals("",q.expression);assertFalse(HelpPlan.forQuestion(q).canTransfer());if(!q.choices.isEmpty())positions.add(q.choices.indexOf(answer));q.answers=new String[]{"999"};FractionPieces.attach(q);assertFalse(q.studyGuide.transfer);for(StudyGuide.Frame f:q.studyGuide.frames){assertFalse(f.before.contains("/"));assertFalse(f.instruction.contains("분자"));assertFalse(f.instruction.contains("분모"));}}
   assertTrue(seen.contains(g.next(id,seen,false,l).signature()));if(id.equals("fractionPiecePicture"))assertEquals(4,positions.size());
  }
 }
 @Test public void selectedMembershipAndPictureIdentitySurviveRestoreAndButtonShuffle()throws Exception{
  Learning.Profile profile=new Learning.Profile();GlobalCurriculum.chooseCountry(profile,"GH");GlobalCurriculum.choosePack(profile,"gh-nacca-core-2019-2023-v1");assertFalse(GlobalCurriculum.pack(profile).inGrade("fractionPart",1));assertFalse(GlobalCurriculum.pack(profile).inGrade("fractionPart",2));
  Question q=new Generator(new Random(530)).next("fractionPiecePicture",List.of(),false,GlobalCurriculum.limits("gh-nacca-core-2019-2023-v1","fractionPiecePicture",2));String identity=q.signature();Collections.reverse(q.choices);assertEquals(identity,q.signature());
  ByteArrayOutputStream bytes=new ByteArrayOutputStream();new ObjectOutputStream(bytes).writeObject(q);Question restored=(Question)new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray())).readObject();assertEquals(identity,restored.signature());assertEquals(q.prompt,restored.prompt);assertTrue(new Checker().check(restored,List.of(),List.of(solve(restored))).correct());
  assertThrows(IllegalArgumentException.class,()->new CurriculumLimits("picturePartitions=3"));
 }
}
