package com.gomgomapps.math.core;
import java.util.*;import java.io.*;import org.junit.Test;import static org.junit.Assert.*;
public class TwoStepMethodChoiceTest {
 @Test public void bothMethodsAreValidWhileWrongOperationsStayWrong()throws Exception{
  for(boolean first:List.of(false,true))for(boolean second:List.of(false,true)){
   Question q=TwoStepChangeStories.make(0,700000,499,first,305,second);q.answers=new String[]{"poison"};HelpPlan h=HelpPlan.forQuestion(q);assertEquals(6,h.size());assertFalse(h.canTransfer());
   assertFalse(h.step(0).accepts(first?"-":"+"));assertFalse(h.step(3).accepts(second?"-":"+"));
   for(int index:new int[]{1,4}){assertTrue(h.step(index).accepts("mental"));assertTrue(h.step(index).accepts("written"));assertFalse(h.step(index).accepts("guess"));assertFalse(h.step(index).accepts("+"));}
   int middle=700000+(first?499:-499),result=middle+(second?305:-305);HelpPlan.Draft d=h.restore(null,q.id);assertTrue(d.entries.stream().allMatch(String::isEmpty));d.entries=new ArrayList<>(List.of(first?"+":"-","mental",""+middle,second?"+":"-","written",""+result));d.stage=6;
   ByteArrayOutputStream bytes=new ByteArrayOutputStream();new ObjectOutputStream(bytes).writeObject(d);HelpPlan.Draft saved=(HelpPlan.Draft)new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray())).readObject();assertEquals(d.entries,h.restore(saved,q.id).entries.subList(0,6));assertEquals(6,saved.stage);
   assertTrue(new Checker().check(q,List.of(),List.of(""+result)).correct());assertEquals("poison",q.answers[0]);d.teachingVersion="two-step-change-v1";assertEquals(0,h.restore(d,q.id).stage);
  }
 }
 @Test public void oldFramesKeepTheirStrictChoiceAndUnknownAlternativesAreRejected()throws Exception{
  String old="rO0ABXNyACljb20uZ29tZ29tYXBwcy5tYXRoLmNvcmUuU3R1ZHlHdWlkZSRGcmFtZQAAAAAAAAABAgAJTAAFYWZ0ZXJ0ABJMamF2YS9sYW5nL1N0cmluZztMAAZiZWZvcmVxAH4AAUwACGV4cGVjdGVkcQB+AAFMAAtpbnB1dEZvcm1hdHEAfgABTAALaW5zdHJ1Y3Rpb25xAH4AAUwAB29wdGlvbnN0AA9MamF2YS91dGlsL01hcDtMABJwaWN0dXJlRGVub21pbmF0b3J0ABNMamF2YS9sYW5nL0ludGVnZXI7TAAMcGljdHVyZUV4dHJhcQB+AANMAA1waWN0dXJlRmlsbGVkcQB+AAN4cHQAAHEAfgAFdAABK3B0AAlvcGVyYXRpb25zcgAXamF2YS51dGlsLkxpbmtlZEhhc2hNYXA0wE5cEGzA+wIAAVoAC2FjY2Vzc09yZGVyeHIAEWphdmEudXRpbC5IYXNoTWFwBQfawcMWYNEDAAJGAApsb2FkRmFjdG9ySQAJdGhyZXNob2xkeHA/QAAAAAAAA3cIAAAABAAAAAJ0AAEtdAAIc3VidHJhY3RxAH4ABnQAA2FkZHgAcHBw";
  StudyGuide.Frame frame=(StudyGuide.Frame)new ObjectInputStream(new ByteArrayInputStream(Base64.getDecoder().decode(old))).readObject();assertNull(frame.acceptedChoices);Question q=new Question("legacyFrame","","","0");q.studyGuide=new StudyGuide().transfer(false);q.studyGuide.frames.add(frame);HelpPlan h=HelpPlan.forQuestion(q);assertTrue(h.step(0).accepts("+"));assertFalse(h.step(0).accepts("-"));
  assertThrows(IllegalArgumentException.class,()->new StudyGuide().choiceAlternatives("method",Map.of("mental","mental"),Set.of("unknown")));assertThrows(IllegalArgumentException.class,()->new StudyGuide().choiceAlternatives("method",Map.of("mental","mental"),Set.of()));
 }
 @Test public void sixthYearFreshSupplyRetainsOperationVariety(){Generator g=new Generator(new Random(198));CurriculumLimits limits=GlobalCurriculum.limits("england-primary-2021-v1",TwoStepChangeStories.ID,6);List<String> recent=new ArrayList<>();Set<String> operations=new HashSet<>();for(int i=0;i<150;i++){Question q=g.next(TwoStepChangeStories.ID,recent,false,limits);assertFalse(recent.contains(q.signature()));recent.add(q.signature());int[] v=TwoStepChangeStories.read(q);operations.add(v[3]+":"+v[4]);int result=v[0]+(v[3]==1?v[1]:-v[1])+(v[4]==1?v[2]:-v[2]);q.answers=new String[]{"poison"};assertTrue(new Checker().check(q,List.of(),List.of(""+result)).correct());assertEquals(6,HelpPlan.forQuestion(q).size());}assertEquals(4,operations.size());}
}
